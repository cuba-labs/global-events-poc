package com.company.globaleventspoc.service;


import com.company.globaleventspoc.GlobalEvent;

public interface GlobalEventsService {
    String NAME = "globevnt_GlobalEventsService";

    void sendEvent(GlobalEvent event);
}