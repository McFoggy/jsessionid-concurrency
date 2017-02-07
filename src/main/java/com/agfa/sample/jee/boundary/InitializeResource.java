package com.agfa.sample.jee.boundary;

import com.agfa.sample.jee.control.Constants;
import com.agfa.sample.jee.control.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("init")
public class InitializeResource {
    private final static Logger LOGGER = LoggerFactory.getLogger(InitializeResource.class);

    @Context private HttpServletRequest servletRequest;

    @Inject
    IdGenerator generator;

    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
    @GET
    public Response init() {
        HttpSession session = servletRequest.getSession();
        long id = generator.generateId();

        session.setAttribute(Constants.ID_KEY, Long.valueOf(id));
        String message = String.format("/init [%s::%s] application id generated: %d", servletRequest.getRequestedSessionId(), session.getId(), id);
        LOGGER.info(message);
        return Response.ok().entity(message).build();
    }
}
