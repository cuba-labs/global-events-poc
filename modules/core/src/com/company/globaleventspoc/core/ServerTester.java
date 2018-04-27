package com.company.globaleventspoc.core;

import com.company.globaleventspoc.GlobalCacheResetEvent;
import com.company.globaleventspoc.GlobalNotificationEvent;
import com.haulmont.cuba.core.global.Events;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component("globevnt_ServerTester")
public class ServerTester implements ServerTesterMBean {

    @Inject
    private Events events;

    @Override
    public String sendGlobalMessage(String message) {
        events.publish(new GlobalNotificationEvent(this, message));
        return "done";
    }

    @Override
    public String sendGlobalCacheReset() {
        events.publish(new GlobalCacheResetEvent(this));
        return "done";
    }
}
