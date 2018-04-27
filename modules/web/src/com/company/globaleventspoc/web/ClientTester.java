package com.company.globaleventspoc.web;

import com.google.common.base.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component("globevnt_ClientTester")
public class ClientTester implements ClientTesterMBean {

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    private UiNotifier uiNotifier;

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
            uiNotifier.sendMessage(msg);
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
}
