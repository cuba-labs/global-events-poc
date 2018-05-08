package com.company.globaleventspoc.core;

import com.company.globaleventspoc.GlobalApplicationEvent;
import com.company.globaleventspoc.LocalRegistry;
import com.haulmont.cuba.core.sys.serialization.SerializationSupport;
import org.springframework.stereotype.Component;

@Component("globevnt_LocalServer")
public class LocalServer {

    public void sendEvent(GlobalApplicationEvent event) {
        byte[] bytes = SerializationSupport.serialize(event);
        LocalRegistry.getInstance().notifyListeners(bytes);
    }
}
