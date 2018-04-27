package com.company.globaleventspoc;

import org.springframework.context.ApplicationEvent;

public class GlobalNotificationEvent extends ApplicationEvent implements GlobalUiEvent {

    private String text;

    public GlobalNotificationEvent(Object source, String text) {
        super(source);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "GlobalNotificationEvent{" +
                "text='" + text + '\'' +
                '}';
    }
}
