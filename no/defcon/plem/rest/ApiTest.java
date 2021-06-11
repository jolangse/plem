package no.defcon.plem.rest;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.GenericEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.util.Set;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.rrd4j.core.RrdDb;
// import org.rrd4j.core.RrdDef;
// import org.rrd4j.core.Sample;
import org.rrd4j.ConsolFun;
// import org.rrd4j.DsType;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
// import java.awt.image.BufferedImage;
// import java.io.BufferedReader;
// import java.io.FileReader;

import no.defcon.plem.Core;
import no.defcon.plem.DataHandler;

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

	private static Logger log = LoggerFactory.getLogger(ApiTest.class);
	private Set<String> sensorList;

	public ApiTest()
	{
		this.sensorList = Core.configManager().getSensorConfig().getSections();
	}

	@GET
	// Supporting XML for this example does not work, because the serialiser
	// does not know what to use as a xml root element.
	// See https://stackoverflow.com/questions/15618061/a-message-body-writer-for-java-class-java-util-arraylist-and-mime-media-type-t
	//@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	// On the other hand, JSON does not need a root element:
	@Produces(MediaType.APPLICATION_JSON)
	public Response root() 
	{
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
	public String p_available() 
	{
		return "yes";
	}



	// Sensors are starting to become candidates for moving to ApiCore,
	// missing things afaik are:
	//  * Data dumping
	//  * Exception/error handling
	@GET
	@Path("/sensors")
	@Produces(MediaType.APPLICATION_JSON)
	public Response p_sensors() 
	{
		List<String> sensors = new ArrayList<>();

		for ( String sensorID : sensorList )
		{
			File exportDir = new File( Core.configManager().getPlemConfig().getString("exportdir", "var/export") );
			File sensorDir = new File( exportDir, sensorID );
			File nameFile = new File ( sensorDir, "name" );
			if ( nameFile.exists() )
			{
				sensors.add(sensorID);
			}
		}	
		GenericEntity<List<String>> mEntity = new GenericEntity<List<String>>(sensors) {};
		return Response.ok(mEntity).build();
	}

	@GET
	@Path("/sensors/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public DataHandler p_sensor( @PathParam("id") String id,
			@DefaultValue("1440") @QueryParam("interval") String intervalStr
		) throws FileNotFoundException
	{
		Integer interval;
		try { interval = Integer.parseInt(intervalStr); }
		catch (Exception e)
		{
			log.warn("api/sensors/{id} failed to parse time interval");
			return null;
		}

		DataHandler h;
		//try
		//{
			h = new DataHandler(id);
		/*}
		catch (Exception e)
		{
			log.warn("api/sensors/{id} caught exception!");
			return null; // "Failed.";
		}*/
		h.setDefAvgTime(interval);
		return h;
	}

	@GET
	@Path("/sensors/{id}/data")
	@Produces(MediaType.APPLICATION_JSON)
	public Response p_sensor_data( @PathParam("id") String id,
			@DefaultValue("0") @QueryParam("start") String startStr,
			@DefaultValue("0") @QueryParam("end") String endStr
		) throws FileNotFoundException, IOException
	{
		long start = 0;
		long end = 0;
		try
		{
			start = Integer.parseInt(startStr);
			end = Integer.parseInt(endStr);
		}
		catch (Exception e)
		{
			log.warn("api/sensors/{id}/data failed to parse start or end");
			throw new IOException("Failed parsing start and/or end");
		}
		long unixTimeNow = System.currentTimeMillis() / 1000L;

		if (start == 0 || start > unixTimeNow )
		{
			start = unixTimeNow - 3600;
		}
		if (end == 0 || end < start)
		{
			end = unixTimeNow;
		}

		log.trace("API call sensor/"+ id +"/data with start=" + Long.toString(start) + " end=" + Long.toString(end));

		// Exceptional exception handling, bro
		DataHandler h;
		h = new DataHandler(id);
		RrdDb rrdDb = h.fetchRrdDb();
		FetchRequest fetchRequest = rrdDb.createFetchRequest(ConsolFun.AVERAGE, start, end); 
		FetchData fetchData = fetchRequest.fetchData();
		if ( fetchData.getRowCount() < 1 )
		{
			log.warn("Tried to get aggregate value for sensor " + id + ", but got no results. ");
			throw new IOException("No results returned");
		}

		String[] names = fetchData.getDsNames();
		log.debug("Names in data source: ");	
		for ( int i = 0; i<names.length; i++)
		{
			log.debug("  -> " + names[i]);
		}

		long[] timestamps = fetchData.getTimestamps();
		double[] values = fetchData.getValues(names[0]);

		List<Map<Long,Double>> set = new ArrayList<>();
		for ( int i = 0; i<timestamps.length; i++)
		{
			Long ts = new Long(timestamps[i]);
			Double v = new Double(values[i]);
			HashMap<Long, Double> dsValues = new HashMap<>();
			if (!v.isNaN())
			{
				dsValues.put( ts, v); 
				set.add(dsValues);
			}
		}
		//System.out.println(dsValues);

		log.debug("Number of elements in timestamps: " + Integer.toString(timestamps.length));
		log.debug("Number of elements in values: " + Integer.toString(values.length));
		
		GenericEntity<List<Map<Long, Double>>>mEntity = new GenericEntity<List<Map<Long, Double>>>(set) {};
		return Response.ok(mEntity).build();
	}

	// I really should investigate a better way to do this, and not copypaste so much bad code.
	@GET
	@Path("/sensors/{id}/data/min")
	@Produces(MediaType.APPLICATION_JSON)
	public Response p_sensor_data_min( @PathParam("id") String id,
			@DefaultValue("0") @QueryParam("start") String startStr,
			@DefaultValue("0") @QueryParam("end") String endStr
		) throws FileNotFoundException, IOException
	{
		long start = 0;
		long end = 0;
		try
		{
			start = Integer.parseInt(startStr);
			end = Integer.parseInt(endStr);
		}
		catch (Exception e)
		{
			log.warn("api/sensors/{id}/data failed to parse start or end");
			throw new IOException("Failed parsing start and/or end");
		}
		long unixTimeNow = System.currentTimeMillis() / 1000L;

		if (start == 0 || start > unixTimeNow )
		{
			start = unixTimeNow - 3600;
		}
		if (end == 0 || end < start)
		{
			end = unixTimeNow;
		}

		log.trace("API call sensor/"+ id +"/data with start=" + Long.toString(start) + " end=" + Long.toString(end));

		// Exceptional exception handling, bro
		DataHandler h;
		h = new DataHandler(id);
		RrdDb rrdDb = h.fetchRrdDb();
		FetchRequest fetchRequest = rrdDb.createFetchRequest(ConsolFun.MIN, start, end); 
		FetchData fetchData = fetchRequest.fetchData();
		if ( fetchData.getRowCount() < 1 )
		{
			log.warn("Tried to get aggregate MIN value for sensor " + id + ", but got no results. ");
			throw new IOException("No results returned");
		}

		String[] names = fetchData.getDsNames();
		log.debug("Names in data source: ");	
		for ( int i = 0; i<names.length; i++)
		{
			log.debug("  -> " + names[i]);
		}

		long[] timestamps = fetchData.getTimestamps();
		double[] values = fetchData.getValues(names[0]);

		List<Map<Long,Double>> set = new ArrayList<>();
		for ( int i = 0; i<timestamps.length; i++)
		{
			Long ts = new Long(timestamps[i]);
			Double v = new Double(values[i]);
			HashMap<Long, Double> dsValues = new HashMap<>();
			if (!v.isNaN())
			{
				dsValues.put( ts, v); 
				set.add(dsValues);
			}
		}
		//System.out.println(dsValues);

		log.debug("Number of elements in timestamps: " + Integer.toString(timestamps.length));
		log.debug("Number of elements in values: " + Integer.toString(values.length));
		
		GenericEntity<List<Map<Long, Double>>>mEntity = new GenericEntity<List<Map<Long, Double>>>(set) {};
		return Response.ok(mEntity).build();
	}

	// I really should investigate a better way to do this, and not copypaste so much bad code.
	@GET
	@Path("/sensors/{id}/data/max")
	@Produces(MediaType.APPLICATION_JSON)
	public Response p_sensor_data_max( @PathParam("id") String id,
			@DefaultValue("0") @QueryParam("start") String startStr,
			@DefaultValue("0") @QueryParam("end") String endStr
		) throws FileNotFoundException, IOException
	{
		long start = 0;
		long end = 0;
		try
		{
			start = Integer.parseInt(startStr);
			end = Integer.parseInt(endStr);
		}
		catch (Exception e)
		{
			log.warn("api/sensors/{id}/data failed to parse start or end");
			throw new IOException("Failed parsing start and/or end");
		}
		long unixTimeNow = System.currentTimeMillis() / 1000L;

		if (start == 0 || start > unixTimeNow )
		{
			start = unixTimeNow - 3600;
		}
		if (end == 0 || end < start)
		{
			end = unixTimeNow;
		}

		log.trace("API call sensor/"+ id +"/data with start=" + Long.toString(start) + " end=" + Long.toString(end));

		// Exceptional exception handling, bro
		DataHandler h;
		h = new DataHandler(id);
		RrdDb rrdDb = h.fetchRrdDb();
		FetchRequest fetchRequest = rrdDb.createFetchRequest(ConsolFun.MAX, start, end); 
		FetchData fetchData = fetchRequest.fetchData();
		if ( fetchData.getRowCount() < 1 )
		{
			log.warn("Tried to get aggregate MIN value for sensor " + id + ", but got no results. ");
			throw new IOException("No results returned");
		}

		String[] names = fetchData.getDsNames();
		log.debug("Names in data source: ");	
		for ( int i = 0; i<names.length; i++)
		{
			log.debug("  -> " + names[i]);
		}

		long[] timestamps = fetchData.getTimestamps();
		double[] values = fetchData.getValues(names[0]);

		List<Map<Long,Double>> set = new ArrayList<>();
		for ( int i = 0; i<timestamps.length; i++)
		{
			Long ts = new Long(timestamps[i]);
			Double v = new Double(values[i]);
			HashMap<Long, Double> dsValues = new HashMap<>();
			if (!v.isNaN())
			{
				dsValues.put( ts, v); 
				set.add(dsValues);
			}
		}
		//System.out.println(dsValues);

		log.debug("Number of elements in timestamps: " + Integer.toString(timestamps.length));
		log.debug("Number of elements in values: " + Integer.toString(values.length));
		
		GenericEntity<List<Map<Long, Double>>>mEntity = new GenericEntity<List<Map<Long, Double>>>(set) {};
		return Response.ok(mEntity).build();
	}


}

