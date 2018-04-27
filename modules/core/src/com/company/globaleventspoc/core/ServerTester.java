package com.company.globaleventspoc.core;

import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component("globevnt_ServerTester")
public class ServerTester implements ServerTesterMBean {

    @Inject
    private WebSocketServer wsServer;


    @Override
    public String test(String message) {
        wsServer.sendMessage(message);
        return "done";
    }
}
