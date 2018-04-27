/*
 * Copyright 2008-2018 Async-IO.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.company.globaleventspoc.core.atmosphere;

import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.Heartbeat;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Ready;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.atmosphere.cpr.ApplicationConfig.MAX_INACTIVE;

@ManagedService(path = "/atmosphere", atmosphereConfig = MAX_INACTIVE + "=120000")
public class AtmosphereManagedService {

    private final Logger log = LoggerFactory.getLogger(AtmosphereManagedService.class);

    @Heartbeat
    public void onHeartbeat(final AtmosphereResourceEvent event) {
        log.trace("Heartbeat sent by {}", event.getResource());
    }

    /**
     * Invoked when the connection has been fully established and suspended, that is, ready for receiving messages.
     *
     */
    @Ready
    public void onReady(AtmosphereResource resource) {
        log.info("Client {} connected", resource.uuid());
        log.info("Broadcaster {}", resource.getBroadcaster().getID());

        // can do some authentication here:
        // String authHeader = resource.getRequest().getHeader("Authorization");
    }

    /**
     * Invoked when the client disconnects or when the underlying connection is closed unexpectedly.
     */
    @Disconnect
    public void onDisconnect(AtmosphereResourceEvent event) {
        if (event.isCancelled()) {
            log.info("Client {} disconnected", event.getResource().uuid());
        } else if (event.isClosedByClient()) {
            log.info("Client {} closed the connection", event.getResource().uuid());
        }
    }
}
