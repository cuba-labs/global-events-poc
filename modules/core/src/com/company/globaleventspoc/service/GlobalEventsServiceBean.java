package com.company.globaleventspoc.service;

import com.company.globaleventspoc.GlobalApplicationEvent;
import com.haulmont.cuba.core.global.Events;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service(GlobalEventsService.NAME)
public class GlobalEventsServiceBean implements GlobalEventsService {

    @Inject
    private Events events;

    @Override
    public void sendEvent(GlobalApplicationEvent event) {
        events.publish(event);
    }
}