package no.defcon.plem.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * https://eclipse-ee4j.github.io/jersey.github.io/documentation/latest/representations.html#d0e6518
 * https://stackoverflow.com/questions/10955584/global-exception-handling-in-jersey/10956394
 * https://howtodoinjava.com/jersey/jaxrs-jersey-exceptionmapper/
 */ 
@Provider
public class NotFoundMapper implements ExceptionMapper<java.io.FileNotFoundException>
{
	private static Logger log = LoggerFactory.getLogger(NotFoundMapper.class);
  
    public Response toResponse(java.io.FileNotFoundException e)
    {
		log.warn("FileNotFoundException in API: " + e.getMessage());
        return Response.status(404).
			entity("Could not find requested data.").
			type("text/plain").
			build();
    }
}
