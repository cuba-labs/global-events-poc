package com.company.globaleventspoc.web;

import com.company.globaleventspoc.Message;
import com.haulmont.cuba.core.sys.events.AppContextStoppedEvent;
import com.haulmont.cuba.core.sys.serialization.SerializationSupport;
import org.atmosphere.wasync.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Base64;

@Component
public class WebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(WebSocketClient.class);

    private Socket socket;

    @Inject
    private UiNotifier uiNotifier;

    public synchronized void connect() {
        if (socket != null)
            return;
        log.info("Opening socket");
        Client client = ClientFactory.getDefault().newClient();

        RequestBuilder request = client.newRequestBuilder()
                .method(Request.METHOD.GET)
                .uri("http://localhost:8080/app-core/atmosphere")
                .decoder(new Decoder<String, Message>() {
                    @Override
                    public Message decode(Event type, String str) {
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
                                return (Message) SerializationSupport.deserialize(bytes);
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
        try {
            socket
                    .on("message", new Function<Message>() {
                        @Override
                        public void on(Message m) {
                            log.info("message payload: " + m.getPayload());
                            uiNotifier.sendMessage(m.getPayload());
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

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @EventListener(AppContextStoppedEvent.class)
    public synchronized void disconnect() {
        log.info("Closing socket");
        socket.close();
        socket = null;
    }
}
