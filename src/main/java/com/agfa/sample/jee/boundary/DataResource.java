package com.agfa.sample.jee.boundary;

import com.agfa.sample.jee.control.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("data")
public class DataResource {
    private final static Logger LOGGER = LoggerFactory.getLogger(DataResource.class);

    @Context private HttpServletRequest servletRequest;

    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
    @GET
    public Response data(@QueryParam("id") String clientId) {
        HttpSession session = servletRequest.getSession(false);

        if (session == null) {
            LOGGER.error("[{}::null] no http session exists for data resource", servletRequest.getRequestedSessionId());
            return Response.status(Response.Status.BAD_REQUEST).entity(String.format("/data no http session found for requested HTTP session id: %s", servletRequest.getRequestedSessionId())).build();
        }

        Long id = (Long)session.getAttribute(Constants.ID_KEY);
        String httpSessionId = session.getId();

        if (id == null) {
            LOGGER.warn("[{}::{}] no application identifier found in HTTP session", servletRequest.getRequestedSessionId(), httpSessionId);
            return Response.status(Response.Status.BAD_REQUEST).entity(String.format("/data [%s::%s] for client{%s} no generated id found", servletRequest.getRequestedSessionId(), httpSessionId, clientId)).build();
        }

        String message = String.format("/data [%s::%s] data initialized for client{%s} on app {%d}", servletRequest.getRequestedSessionId(), httpSessionId, clientId, id);
        LOGGER.info(message);
        return Response.ok().entity(message).build();
    }
}
