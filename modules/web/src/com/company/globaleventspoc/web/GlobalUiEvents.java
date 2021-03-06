package com.company.globaleventspoc.web;

import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.AppUI;
import com.haulmont.cuba.web.security.events.AppStartedEvent;
import com.vaadin.server.VaadinSession;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component("globevnt_GlobalUiEvents")
public class GlobalUiEvents {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final List<VaadinSession> sessions = new ArrayList<>();

    @EventListener
    public void onAppStart(AppStartedEvent event) {
        lock.writeLock().lock();
        try {
            sessions.add(VaadinSession.getCurrent());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void publish(ApplicationEvent event) {
        ArrayList<VaadinSession> activeSessions;

        lock.readLock().lock();
        try {
            activeSessions = new ArrayList<>(sessions);
        } finally {
            lock.readLock().unlock();
        }

        for (VaadinSession session : activeSessions) {
            // obtain lock on session state
            session.accessSynchronously(() -> {
                if (session.getState() == VaadinSession.State.OPEN) {
                    // active app in this session
                    App app = App.getInstance();

                    // notify all opened web browser tabs
                    List<AppUI> appUIs = app.getAppUIs();
                    for (AppUI ui : appUIs) {
                        if (!ui.isClosing()) {
                            // work in context of UI
                            ui.accessSynchronously(() -> {
                                ui.getUiEventsMulticaster().multicastEvent(event);
                            });
                        }
                    }
                }
            });
        }
    }
}
