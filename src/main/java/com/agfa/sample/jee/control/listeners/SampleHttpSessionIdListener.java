package com.agfa.sample.jee.control.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionIdListener;

@WebListener
public class SampleHttpSessionIdListener implements HttpSessionIdListener {
    private final static Logger LOGGER = LoggerFactory.getLogger(SampleHttpSessionIdListener.class);

    @Override
    public void sessionIdChanged(HttpSessionEvent event, String oldSessionId) {
        LOGGER.info("HTTP session id changed from {} to {}", oldSessionId, event.getSession().getId());
    }
}
