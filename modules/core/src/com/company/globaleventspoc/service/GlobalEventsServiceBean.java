package com.company.globaleventspoc.service;

import com.company.globaleventspoc.GlobalEvent;
import org.springframework.stereotype.Service;

@Service(GlobalEventsService.NAME)
public class GlobalEventsServiceBean implements GlobalEventsService {

    @Override
    public void sendEvent(GlobalEvent event) {
        // todo: how to avoid cycles?
    }
}