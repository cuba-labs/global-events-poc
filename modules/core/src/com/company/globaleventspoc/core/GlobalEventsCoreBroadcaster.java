package com.company.globaleventspoc.core;

import com.company.globaleventspoc.GlobalApplicationEvent;
import com.haulmont.cuba.core.app.ClusterListenerAdapter;
import com.haulmont.cuba.core.app.ClusterManagerAPI;
import com.haulmont.cuba.core.global.Events;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.UUID;

@Component("globevnt_GlobalEventsCoreBroadcaster")
public class GlobalEventsCoreBroadcaster {

    private UUID origin = UUID.randomUUID();

    @Inject
    private WebSocketServer wsServer;

    @Inject
    private Events events;

    private ClusterManagerAPI clusterManagerAPI;

    @Inject
    public void setClusterManager(ClusterManagerAPI clusterManagerAPI) {
        this.clusterManagerAPI = clusterManagerAPI;
        clusterManagerAPI.addListener(GlobalApplicationEvent.class, new ClusterListenerAdapter<GlobalApplicationEvent>() {
            @Override
            public void receive(GlobalApplicationEvent event) {
                events.publish(event);
            }
        });
    }

    @EventListener
    public void onGlobalEvent(GlobalApplicationEvent event) {
        if (event.getServerOrigin() == null) {
            event.setServerOrigin(origin);
            clusterManagerAPI.send(event);
        }
        wsServer.sendEvent(event);
    }
}
