package com.company.globaleventspoc.web;

import com.company.globaleventspoc.GlobalCacheResetEvent;
import com.company.globaleventspoc.GlobalNotificationEvent;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component("globevnt_ClientTester")
public class ClientTester implements ClientTesterMBean {

    private static final Logger log = LoggerFactory.getLogger(ClientTester.class);

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    private GlobalWebEvents globalWebEvents;

    @Inject
    private WebSocketClient wsClient;

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
            globalWebEvents.publish(new GlobalNotificationEvent(this, msg));
        });
        return "done";
    }

    @Override
    public String connect() {
        wsClient.connect();
        return "done";
    }

    @Override
    public String disconnect() {
        wsClient.disconnect();
        return "done";
    }

    @EventListener
    public void cacheReset(GlobalCacheResetEvent event) {
        log.info("GlobalCacheResetEvent received: " + event);
    }
}
