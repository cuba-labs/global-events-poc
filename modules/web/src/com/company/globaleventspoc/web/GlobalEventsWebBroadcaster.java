package com.company.globaleventspoc.web;

import com.company.globaleventspoc.GlobalApplicationEvent;
import com.company.globaleventspoc.GlobalUiEvent;
import com.company.globaleventspoc.service.GlobalEventsService;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.security.app.TrustedClientService;
import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.auth.WebAuthConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.UUID;

@Component("globevnt_GlobalEventsWebBroadcaster")
public class GlobalEventsWebBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(GlobalEventsWebBroadcaster.class);

    private UUID origin = UUID.randomUUID();

    @Inject
    private GlobalUiEvents globalUiEvents;

    @Inject
    private GlobalEventsService globalEventsService;

    @Inject
    private TrustedClientService trustedClientService;

    @Inject
    private WebAuthConfig webAuthConfig;

    public UUID getOrigin() {
        return origin;
    }

    @EventListener
    public void onGlobalEvent(GlobalApplicationEvent event) {
        if (event.getClientOrigin() != null) {
            log.debug("Event from another client, ignoring it");
            return;
        }
        if (event.getServerOrigin() != null) {
            log.debug("Event from server, ignoring it");
            return;
        }
        event.setClientOrigin(origin);

        if (event instanceof GlobalUiEvent) {
            globalUiEvents.publish(event);
        }

        UserSession session;
        try {
            session = trustedClientService.getSystemSession(webAuthConfig.getTrustedClientPassword());
        } catch (LoginException e) {
            throw new RuntimeException("Unable to get system session for sending global event", e);
        }
        AppContext.withSecurityContext(new SecurityContext(session), () ->
                globalEventsService.sendEvent(event));
    }
}
