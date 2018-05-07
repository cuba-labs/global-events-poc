package com.company.globaleventspoc.core;


import com.company.globaleventspoc.GlobalApplicationEvent;
import com.haulmont.cuba.core.sys.serialization.SerializationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component("globevnt_Server")
public class WebSocketServer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    private List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public void sendEvent(GlobalApplicationEvent event) {
        byte[] bytes = SerializationSupport.serialize(event);
        String str;
        try {
            str = new String(Base64.getEncoder().encode(bytes), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        for (WebSocketSession session : new ArrayList<>(sessions)) {
            try {
                TextMessage message = new TextMessage(str);
                log.info("Sending message {} to {}", message, session);
                session.sendMessage(message);
            } catch (IOException e) {
                log.warn("Error sending message: " + e);
                removeSession(session);
            }
        }
    }

    public void addSession(WebSocketSession session) {
        sessions.add(session);
    }

    public void removeSession(WebSocketSession session) {
        sessions.remove(session);
    }
}
