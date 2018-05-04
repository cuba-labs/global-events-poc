package com.company.globaleventspoc;

import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.core.sys.events.AppContextInitializedEvent;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class LoggingMdcSetup {

    @Inject
    private GlobalConfig config;

    @EventListener(AppContextInitializedEvent.class)
    public void setup() {
        MDC.put("cubaBlock", String.format("%s:%s/%s", config.getWebHostName(), config.getWebPort(), config.getWebContextName()));
    }
}
