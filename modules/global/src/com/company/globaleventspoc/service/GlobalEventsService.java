package com.company.globaleventspoc.service;

import com.company.globaleventspoc.GlobalApplicationEvent;

public interface GlobalEventsService {
    String NAME = "globevnt_GlobalEventsService";

    void sendEvent(GlobalApplicationEvent event);
}