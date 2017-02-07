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
import java.time.ZonedDateTime;

@Path("time")
public class TimeResource {
    private final static Logger LOGGER = LoggerFactory.getLogger(TimeResource.class);

    @Context private HttpServletRequest servletRequest;

    @Inject
    IdGenerator generator;

    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
    @GET
    public Response time() {
        HttpSession session = servletRequest.getSession();

        String message = String.format("[%s::%s] it is %s", servletRequest.getRequestedSessionId(), session.getId(), ZonedDateTime.now().toString());
        LOGGER.info(message);
        return Response.ok().entity(message).build();
    }
}
