package com.company.globaleventspoc;

public class GlobalCacheResetEvent extends GlobalApplicationEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public GlobalCacheResetEvent(Object source) {
        super(source);
    }
}
