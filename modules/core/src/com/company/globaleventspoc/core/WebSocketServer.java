package com.company.globaleventspoc.core;


import com.company.globaleventspoc.GlobalApplicationEvent;
import com.haulmont.cuba.core.sys.serialization.SerializationSupport;
import com.haulmont.cuba.core.sys.servlet.events.ServletContextInitializedEvent;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRegistration;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

@Component("globevnt_Server")
public class WebSocketServer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    private BroadcasterFactory broadcasterFactory;

    @EventListener
    public void init(ServletContextInitializedEvent e) {
        log.info("Creating Atmosphere");
        AtmosphereServlet s = new AtmosphereServlet();

        ServletRegistration servletRegistration = e.getSource().addServlet("AtmosphereServlet", s);

        servletRegistration.addMapping("/atmosphere/*");
        servletRegistration.setInitParameter("org.atmosphere.cpr.packages", "com.sample.cubawebsockets.core.atmosphere");
        servletRegistration.setInitParameter("org.atmosphere.interceptor.HeartbeatInterceptor.clientHeartbeatFrequencyInSeconds", "10");
        ((ServletRegistration.Dynamic) servletRegistration).setAsyncSupported(true);

        broadcasterFactory = s.framework().getBroadcasterFactory();
        log.info("broadcasterFactory=" + broadcasterFactory);
    }

    public void sendEvent(GlobalApplicationEvent event) {
        byte[] bytes = SerializationSupport.serialize(event);
        String str;
        try {
            str = new String(Base64.getEncoder().encode(bytes), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        Broadcaster broadcaster = broadcasterFactory.lookup("/atmosphere");
        log.info("'/atmosphere' broadcaster=" + broadcaster);
        broadcaster.broadcast(str);
    }
}
