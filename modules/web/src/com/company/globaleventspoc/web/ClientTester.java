package com.company.globaleventspoc.web;

import com.company.globaleventspoc.GlobalCacheResetEvent;
import com.company.globaleventspoc.GlobalNotificationEvent;
import com.google.common.base.Strings;
import com.haulmont.cuba.core.global.Events;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component("globevnt_ClientTester")
public class ClientTester implements ClientTesterMBean {

    private static final Logger log = LoggerFactory.getLogger(ClientTester.class);

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    private Events events;

    private WebSocketSession webSocketSession;

    @PreDestroy
    public void destroy() {
        executor.shutdownNow();
    }

    @Override
    public String sendMessage(String message) {
        String msg = Strings.isNullOrEmpty(message) ? "default message" : message;

        executor.submit(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            events.publish(new GlobalNotificationEvent(this, msg));
        });
        return "done";
    }

    @Override
    public String connect() {
        org.springframework.web.socket.client.WebSocketClient simpleWebSocketClient = new StandardWebSocketClient();
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(simpleWebSocketClient));
        SockJsClient sockJsClient = new SockJsClient(transports);

        WebSocketConnectionManager connectionManager = new WebSocketConnectionManager(sockJsClient,
                new TextWebSocketHandler() {

                    @Override
                    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                        log.info("Opened session: " + session);
                        webSocketSession = session;
                    }

                    @Override
                    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                        log.info("Received message {} from {}", message, session);
                    }
                },
                "http://localhost:8080/app-core/websocket/wsHandler");
        connectionManager.start();

        return "done";
    }

    @Override
    public String disconnect() {
        try {
            webSocketSession.close();
            webSocketSession = null;
        } catch (IOException e) {
            return ExceptionUtils.getFullStackTrace(e);
        }
        return "done";
    }

    @EventListener
    public void cacheReset(GlobalCacheResetEvent event) {
        log.info("GlobalCacheResetEvent received: " + event);
    }
}
