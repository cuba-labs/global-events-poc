package com.company.globaleventspoc.core;


import com.company.globaleventspoc.GlobalApplicationEvent;
import com.haulmont.cuba.core.app.ServerConfig;
import com.haulmont.cuba.core.sys.serialization.SerializationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component("globevnt_Server")
public class WebSocketServer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    private Map<WebSocketSession, Boolean> sessions = new ConcurrentHashMap<>();

    @Inject
    private ServerConfig serverConfig;

    public void sendEvent(GlobalApplicationEvent event) {
        Iterator<Map.Entry<WebSocketSession, Boolean>> it = sessions.entrySet().iterator();
        if (it.hasNext()) {
            byte[] bytes = SerializationSupport.serialize(event);
            String str;
            try {
                str = new String(Base64.getEncoder().encode(bytes), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            while (it.hasNext()) {
                Map.Entry<WebSocketSession, Boolean> entry = it.next();
                if (entry.getValue()) { // if the session is authenticated
                    WebSocketSession session = entry.getKey();
                    try {
                        TextMessage message = new TextMessage(str);
                        log.info("Sending message {} to {}", message, session);
                        session.sendMessage(message);
                    } catch (IOException e) {
                        log.warn("Error sending message, removing the session: " + e);
                        it.remove();
                    }
                }
            }
        }
    }

    public void addSession(WebSocketSession session) {
        sessions.put(session, false);
    }

    public void removeSession(WebSocketSession session) {
        sessions.remove(session);
    }

    public void onMessage(WebSocketSession session, TextMessage message) {
        Boolean authenticated = sessions.get(session);
        if (authenticated != null) {
            if (!authenticated) {
                String payload = message.getPayload();
                if (serverConfig.getTrustedClientPassword().equals(payload)) {
                    sessions.put(session, true);
                    log.info("Authenticated session: " + session);
                } else {
                    log.warn("Invalid credentials, removing session " + session);
                    sessions.remove(session);
                }
            } else {
                log.info("Session {} is already authenticated");
            }
        } else {
            log.warn("Unknown session: " + session);
        }
    }
}
