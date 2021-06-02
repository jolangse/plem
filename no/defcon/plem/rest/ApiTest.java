package no.defcon.plem.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericEntity;

import java.util.ArrayList;
import java.util.List;

/*
 * https://zetcode.com/jersey/json/
 * https://stackoverflow.com/questions/10339451/configuring-jersey-jetty-jsp
 * https://sangupta.com/tech/rest-server-with-jetty.html
 * https://docs.oracle.com/cd/E19798-01/821-1841/6nmq2cp1v/index.html
 * https://eclipse-ee4j.github.io/jersey/
 * http://jlunaquiroga.blogspot.com/2014/01/restful-web-services-with-jetty-and.html
 *
 */

@Path("/test")
public class ApiTest {

	@GET
	// Supporting XML for this example does not work, because the serialiser
	// does not know what to use as a xml root element.
	// See https://stackoverflow.com/questions/15618061/a-message-body-writer-for-java-class-java-util-arraylist-and-mime-media-type-t
	//@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	// On the other hand, JSON does not need a root element:
	@Produces(MediaType.APPLICATION_JSON)
	public Response root() {
		List<String> items = new ArrayList<>();
		items.add(new String("Hello"));
		items.add(new String("World"));
		items.add(new String("this"));
		items.add(new String("is"));
		items.add(new String("a"));
		items.add(new String("test"));

		GenericEntity<List<String>> mEntity = new GenericEntity<List<String>>(items) {};
		return Response.ok(mEntity).build();
	}


	@GET
	@Path("/available")
	@Produces(MediaType.TEXT_PLAIN)
	public String available() {
		return "yes";
	}
}

