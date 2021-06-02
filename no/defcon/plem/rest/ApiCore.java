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


@Path("/")
public class ApiCore {

	// @GET
	// // @Path("/")
	// @Produces(MediaType.APPLICATION_JSON)
	// public Response root() {
	// 	List<String> items = new ArrayList<>();
	// 	items.add(new String("Hello"));
	// 	items.add(new String("World"));
	// 	items.add(new String("this"));
	// 	items.add(new String("is"));
	// 	items.add(new String("a"));
	// 	items.add(new String("test"));

	// 	GenericEntity<List<String>> mEntity = new GenericEntity<List<String>>(items) {};
	// 	return Response.status(200).entity(mEntity).build();
	// }


	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String root() {
		return "TL;DR";
	}

}

