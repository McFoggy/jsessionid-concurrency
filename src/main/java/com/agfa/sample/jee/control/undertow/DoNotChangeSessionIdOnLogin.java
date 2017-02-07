package com.agfa.sample.jee.control.undertow;

import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;

import javax.servlet.ServletContext;


public class DoNotChangeSessionIdOnLogin implements ServletExtension {
    @Override
    public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext) {
        deploymentInfo.setChangeSessionIdOnLogin(false);
    }
}