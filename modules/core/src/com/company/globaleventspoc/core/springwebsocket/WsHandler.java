package com.company.globaleventspoc.core.springwebsocket;

import com.company.globaleventspoc.core.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class WsHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(WsHandler.class);

    private WebSocketServer webSocketServer;

    public WsHandler() {
        super();
        log.info("Created TextWebSocketHandler");
    }

    @SuppressWarnings("unused")
    public void setWebSocketServer(WebSocketServer webSocketServer) {
        this.webSocketServer = webSocketServer;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("Received {} from {}", message, session);
        webSocketServer.onMessage(session, message);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Opened session {}", session);
        webSocketServer.addSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Closed session {}", session);
        webSocketServer.removeSession(session);
    }
}
