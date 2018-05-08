package com.company.globaleventspoc.web.screens;

import com.company.globaleventspoc.GlobalNotificationEvent;
import com.haulmont.cuba.gui.components.AbstractWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

public class Screen extends AbstractWindow {

    private static final Logger log = LoggerFactory.getLogger(Screen.class);

    @EventListener
    public void onGlobalNotification(GlobalNotificationEvent globalNotificationEvent) {
        log.info("Global Notification: " + globalNotificationEvent.getText());
        showNotification("Global Notification: " + globalNotificationEvent.getText());
    }
}