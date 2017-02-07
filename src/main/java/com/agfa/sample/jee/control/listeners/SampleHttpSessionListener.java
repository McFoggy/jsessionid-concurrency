package com.agfa.sample.jee.control.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@WebListener
public class SampleHttpSessionListener implements HttpSessionListener {
    private final static Logger LOGGER = LoggerFactory.getLogger(SampleHttpSessionListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        LOGGER.info("HTTP session created {}", se.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
    }
}
