package com.company.globaleventspoc.core;

public interface ServerTesterMBean {

    String sendGlobalMessage(String message);

    String sendGlobalCacheReset();
}
