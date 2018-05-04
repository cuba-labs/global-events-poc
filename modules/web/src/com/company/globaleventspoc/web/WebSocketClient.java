package com.company.globaleventspoc.web;

import com.company.globaleventspoc.GlobalApplicationEvent;
import com.company.globaleventspoc.GlobalUiEvent;
import com.haulmont.cuba.core.global.Events;
import com.haulmont.cuba.core.sys.events.AppContextStoppedEvent;
import com.haulmont.cuba.core.sys.remoting.discovery.ServerSelector;
import com.haulmont.cuba.core.sys.remoting.discovery.StickySessionServerSelector;
import com.haulmont.cuba.core.sys.serialization.SerializationSupport;
import com.haulmont.cuba.web.security.events.AppStartedEvent;
import org.atmosphere.wasync.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Base64;

@Component("globevnt_WebSocketClient")
public class WebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(WebSocketClient.class);

    private Socket socket;

    @Resource(name = ServerSelector.NAME)
    private ServerSelector serverSelector;

    @Inject
    private GlobalUiEvents globalUiEvents;

    @Inject
    private Events events;

    @Inject
    private GlobalEventsWebBroadcaster globalEventsWebBroadcaster;

    @EventListener(AppStartedEvent.class)
    public void init() {
        // connect on first web request
        connect();
    }

    @EventListener(AppContextStoppedEvent.class)
    public void dispose() {
        // disconnect on server shutdown
        disconnect();
    }

    public synchronized void connect() {
        if (socket != null)
            return;
        log.info("Opening socket");

        Object context = serverSelector.initContext();
        String url = getUrl(context);
        if (url == null) {
            throw new RuntimeException("Unable to open WebSocket: no available server URLs");
        }
        while (true) {
            try {
                tryConnect(url);
                break;
            } catch (IOException e) {
                serverSelector.fail(context);
                url = getUrl(context);
                if (url != null)
                    log.debug("Trying next URL");
                else
                    throw new RuntimeException("Unable to open WebSocket: no more server URLs available");
            }

        }
    }

    private String getUrl(Object context) {
        String url = serverSelector.getUrl(context);
        if (url != null && serverSelector instanceof StickySessionServerSelector) {
            String servletPath = ((StickySessionServerSelector) serverSelector).getServletPath();
            url = url.substring(0, url.lastIndexOf(servletPath));
        }
        return url;
    }

    private void tryConnect(String serverUrl) throws IOException {
        log.debug("Connecting to " + serverUrl);

        Client client = ClientFactory.getDefault().newClient();

        RequestBuilder request = client.newRequestBuilder()
                .method(Request.METHOD.GET)
                .uri(serverUrl + "atmosphere")
                .decoder(new Decoder<String, GlobalApplicationEvent>() {
                    @Override
                    public GlobalApplicationEvent decode(Event type, String str) {
                        str = str.trim();

                        // Padding from Atmosphere, skip
                        if (str.length() == 0) {
                            return null;
                        }

                        if (type.equals(Event.MESSAGE)) {
                            try {
                                int i = str.indexOf('|');
                                if (i > -1) {
                                    str = str.substring(i + 1);
                                }
                                byte[] bytes = Base64.getDecoder().decode(str.getBytes("UTF-8"));
                                return (GlobalApplicationEvent) SerializationSupport.deserialize(bytes);
                            } catch (IOException e) {
                                log.info("Invalid message {}", str);
                                return null;
                            }
                        } else {
                            return null;
                        }
                    }
                })
                .transport(Request.TRANSPORT.WEBSOCKET)
                .transport(Request.TRANSPORT.LONG_POLLING);

        socket = client.create();
        socket
                .on("message", new Function<GlobalApplicationEvent>() {
                    @Override
                    public void on(GlobalApplicationEvent event) {
                        log.info("Received GlobalApplicationEvent: " + event);
                        publishEvent(event);
                    }
                })
                .on(new Function<Throwable>() {
                    @Override
                    public void on(Throwable t) {
                        log.info("error: " + t);
                    }
                })
                .on(new Function<String>() {
                    @Override
                    public void on(String o) {
                        log.info("event: " + o);
                    }
                })
                .open(request.build());
    }

    private void publishEvent(GlobalApplicationEvent event) {
        if (globalEventsWebBroadcaster.getOrigin().equals(event.getClientOrigin())) {
            log.debug("Received own event, ignoring it");
            return;
        }

        if (event instanceof GlobalUiEvent) {
            globalUiEvents.publish(event);
        } else {
            events.publish(event);
        }
    }

    @EventListener(AppContextStoppedEvent.class)
    public synchronized void disconnect() {
        log.info("Closing socket");
        socket.close();
        socket = null;
    }
}
