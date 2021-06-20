package no.defcon.plem.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * https://eclipse-ee4j.github.io/jersey.github.io/documentation/latest/representations.html#d0e6518
 */ 
@Provider
public class NumberFormatExMapper implements ExceptionMapper<java.lang.NumberFormatException>
{
	private static Logger log = LoggerFactory.getLogger(NotFoundMapper.class);
  
    public Response toResponse(java.lang.NumberFormatException e)
    {
		log.warn("NumberFormatException in API: " + e.getMessage());
        return Response.status(500).
			entity("Failed to parse number.").
			type("text/plain").
			build();
    }
}
