package com.company.globaleventspoc.web;

import com.company.globaleventspoc.GlobalEvent;
import com.company.globaleventspoc.service.GlobalEventsService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component("globevnt_GlobalEventsWebBroadcaster")
public class GlobalEventsWebBroadcaster {

    @Inject
    private GlobalEventsService globalEventsService;

    @EventListener
    public void onGlobalEvent(GlobalEvent event) {
        // todo: need authentication
//        globalEventsService.sendEvent(event);
    }
}
