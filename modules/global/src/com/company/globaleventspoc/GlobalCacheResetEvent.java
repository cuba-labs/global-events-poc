package com.company.globaleventspoc;

import org.springframework.context.ApplicationEvent;

public class GlobalCacheResetEvent extends ApplicationEvent implements GlobalEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public GlobalCacheResetEvent(Object source) {
        super(source);
    }
}
