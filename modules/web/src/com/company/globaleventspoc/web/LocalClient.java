package com.company.globaleventspoc.web;

import com.company.globaleventspoc.GlobalApplicationEvent;
import com.company.globaleventspoc.GlobalUiEvent;
import com.company.globaleventspoc.LocalRegistry;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.haulmont.cuba.core.global.Events;
import com.haulmont.cuba.core.sys.events.AppContextStartedEvent;
import com.haulmont.cuba.core.sys.events.AppContextStoppedEvent;
import com.haulmont.cuba.core.sys.serialization.SerializationSupport;
import com.haulmont.cuba.web.WebConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class LocalClient {

    private static final Logger log = LoggerFactory.getLogger(LocalClient.class);

    @Inject
    private WebConfig webConfig;

    @Inject
    private GlobalEventsWebBroadcaster globalEventsWebBroadcaster;

    @Inject
    private GlobalUiEvents globalUiEvents;

    @Inject
    private Events events;

    private ExecutorService executor = Executors.newFixedThreadPool(5,
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("GE-LocalClient-%d")
                    .setThreadFactory(Executors.defaultThreadFactory())
                    .build());

    @EventListener(AppContextStartedEvent.class)
    public void init() {
        if (webConfig.getUseLocalServiceInvocation()) {
            LocalRegistry.getInstance().addListener(this::onMessage);
        }
    }

    @EventListener(AppContextStoppedEvent.class)
    public void dispose() {
        executor.shutdownNow();
    }

    public void onMessage(byte[] message) {
        GlobalApplicationEvent event = (GlobalApplicationEvent) SerializationSupport.deserialize(message);

        if (globalEventsWebBroadcaster.getOrigin().equals(event.getClientOrigin())) {
            log.debug("Received own event, ignoring it");
            return;
        }

        // decouple from calling thread that might lock Vaadin session
        executor.submit(() -> {
            if (event instanceof GlobalUiEvent) {
                globalUiEvents.publish(event);
            } else {
                events.publish(event);
            }
        });
    }
}
