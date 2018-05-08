package com.company.globaleventspoc.core.springwebsocket;

import com.haulmont.cuba.core.sys.servlet.ServletRegistrationManager;
import com.haulmont.cuba.core.sys.servlet.events.ServletContextInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.Servlet;

@Component
public class WsDispatcherServletInitializer {

    @Inject
    private ServletRegistrationManager servletRegistrationManager;

    @EventListener
    public void initializeServlets(ServletContextInitializedEvent e) {
        Servlet myServlet = servletRegistrationManager.createServlet(e.getApplicationContext(),
                "com.company.globaleventspoc.core.springwebsocket.WsDispatcherServlet");

        e.getSource().addServlet("ws_servlet", myServlet)
                .addMapping("/websocket/*");
    }
}
