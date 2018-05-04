package com.company.globaleventspoc;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class GlobalApplicationEvent extends ApplicationEvent {

    private UUID serverOrigin;

    private UUID clientOrigin;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public GlobalApplicationEvent(Object source) {
        super(source);
    }

    public UUID getServerOrigin() {
        return serverOrigin;
    }

    public void setServerOrigin(UUID serverOrigin) {
        this.serverOrigin = serverOrigin;
    }

    public UUID getClientOrigin() {
        return clientOrigin;
    }

    public void setClientOrigin(UUID clientOrigin) {
        this.clientOrigin = clientOrigin;
    }
}
