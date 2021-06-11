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

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String root() {
		return "TL;DR";
	}

}

