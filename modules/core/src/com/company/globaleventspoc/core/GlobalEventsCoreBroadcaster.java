package com.company.globaleventspoc.core;

import com.company.globaleventspoc.GlobalEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component("globevnt_GlobalEventsCoreBroadcaster")
public class GlobalEventsCoreBroadcaster {

    @Inject
    private WebSocketServer wsServer;

    @EventListener
    public void onGlobalEvent(GlobalEvent event) {
        wsServer.sendEvent(event);
    }
}
