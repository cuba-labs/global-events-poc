package com.company.globaleventspoc.web;

import com.company.globaleventspoc.GlobalApplicationEvent;
import com.company.globaleventspoc.GlobalUiEvent;
import com.haulmont.cuba.core.global.Events;
import com.haulmont.cuba.core.sys.events.AppContextStoppedEvent;
import com.haulmont.cuba.core.sys.remoting.discovery.ServerSelector;
import com.haulmont.cuba.core.sys.remoting.discovery.StickySessionServerSelector;
import com.haulmont.cuba.core.sys.serialization.SerializationSupport;
import com.haulmont.cuba.web.auth.WebAuthConfig;
import com.haulmont.cuba.web.security.events.AppStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component("globevnt_WebSocketClient")
public class WebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(WebSocketClient.class);

    private WebSocketSession webSocketSession;

    @Resource(name = ServerSelector.NAME)
    private ServerSelector serverSelector;

    @Inject
    private GlobalUiEvents globalUiEvents;

    @Inject
    private Events events;

    @Inject
    private GlobalEventsWebBroadcaster globalEventsWebBroadcaster;

    @Inject
    private WebAuthConfig webAuthConfig;

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
        if (webSocketSession != null)
            return;
        log.info("Opening session");

        Object context = serverSelector.initContext();
        String url = getUrl(context);
        if (url == null) {
            throw new RuntimeException("Unable to open session: no available server URLs");
        }
        while (true) {
            try {
                webSocketSession = tryConnect(url);
                break;
            } catch (ExecutionException e) {
                if (e.getCause() instanceof ResourceAccessException) {
                    log.info("Unable to open session: {}", e.getCause().toString());
                    serverSelector.fail(context);
                    url = getUrl(context);
                    if (url != null)
                        log.debug("Trying next URL");
                    else
                        throw new RuntimeException("Unable to open session: no more server URLs available");
                } else {
                    throw new RuntimeException("Error opening session", e);
                }
            } catch (InterruptedException e) {
                log.warn("Interrupted attempt to open session");
                Thread.currentThread().interrupt();
                break;
            }
        }
        authenticate();
    }

    private void authenticate() {
        if (webSocketSession == null || !webSocketSession.isOpen()) {
            log.error("Invalid session: " + webSocketSession);
            return;
        }
        try {
            webSocketSession.sendMessage(new TextMessage(webAuthConfig.getTrustedClientPassword()));
        } catch (IOException e) {
            throw new RuntimeException("Error sending auth message", e);
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

    private WebSocketSession tryConnect(String serverUrl) throws ExecutionException, InterruptedException {
        log.debug("Connecting to " + serverUrl);

        StandardWebSocketClient standardWebSocketClient = new StandardWebSocketClient();
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(standardWebSocketClient));
        SockJsClient sockJsClient = new SockJsClient(transports);

        return sockJsClient
                .doHandshake(new ClientWebSocketHandler(), serverUrl + "websocket/wsHandler")
                .get();
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
        log.info("Closing session");
        try {
            webSocketSession.close();
        } catch (IOException e) {
            log.warn("Error closing session: " + e);
        }
        webSocketSession = null;
    }

    private class ClientWebSocketHandler extends TextWebSocketHandler {

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            log.info("Opened " + session);
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
            log.info("Closed " + session);
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            log.info("Received message {} from {}", message, session);
            byte[] bytes = Base64.getDecoder().decode(message.getPayload().getBytes("UTF-8"));
            GlobalApplicationEvent event = (GlobalApplicationEvent) SerializationSupport.deserialize(bytes);
            publishEvent(event);
        }
    }
}
