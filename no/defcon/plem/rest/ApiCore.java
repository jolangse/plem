package no.defcon.plem.rest;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.GenericEntity;

import java.util.Map;
import java.util.HashMap;

import java.util.Set;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.defcon.plem.Core;
import no.defcon.plem.DataHandler;

import org.rrd4j.core.RrdDb;
import org.rrd4j.ConsolFun;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;

@Path("/")
public class ApiCore {

	private static Logger log = LoggerFactory.getLogger(ApiTest.class);
	private Set<String> sensorList;
	private int lemport;
	private int controlport;
	private final String APIversion = "v0.1-dev-1";

	public ApiCore()
	{
		this.sensorList = Core.configManager().getSensorConfig().getSections();
		this.lemport = Core.configManager().getPlemConfig().getInt("lemport", 13579);
		this.controlport = Core.configManager().getPlemConfig().getInt("controlport", 8006);
	}

	/*
	 * For now, the API provides JSON format data.
	 * Supporting XML does not work for now, because the serialiser
	 * does not know what to use as a xml root element.
	 * See https://stackoverflow.com/questions/15618061/a-message-body-writer-for-java-class-java-util-arraylist-and-mime-media-type-t
	 * Later, when needed changes to get XML marshalling/serialization 
	 * is added, update the @Produces lines to:
	 * @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	*/

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response root() 
	{
		HashMap<String,String> meta = new HashMap<>();
		meta.put("apiversion", APIversion);
		meta.put("lemport", Integer.toString(lemport));
		meta.put("uptime", Long.toString(Core.getUptime()));
		GenericEntity<Map<String,String>> mEntity = 
			new GenericEntity<Map<String,String>>(meta){};
		log.debug("API call to root, returning metadata.");
		return Response.ok(mEntity).build();
	}

	@GET
	@Path("/sensors")
	@Produces(MediaType.APPLICATION_JSON)
	public Response p_sensors() 
	{
		SensorListMsh sensorsMsh = new SensorListMsh();

		for ( String sensorID : sensorList )
		{
			File exportDir = new File( Core.configManager().getPlemConfig().getString("exportdir", "var/export") );
			File sensorDir = new File( exportDir, sensorID );
			File nameFile = new File ( sensorDir, "name" );
			if ( nameFile.exists() )
			{
				sensorsMsh.add(sensorID);
			}
		}
		log.debug("API call to /sensors returning " + Integer.toString(sensorsMsh.length()) + " sensor IDs" );
		return Response.ok(sensorsMsh).build();
	}


	@GET
	@Path("/sensors/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public DataHandler p_sensor( @PathParam("id") String id,
			@DefaultValue("1440") @QueryParam("interval") String intervalStr
		) throws FileNotFoundException, NumberFormatException
	{
		Integer interval = Integer.parseInt(intervalStr); 
		log.debug("API call sensor/"+ id +" with interval=" + interval.toString());

		DataHandler h = new DataHandler(id);
		h.setDefAvgTime(interval);
		return h;
	}

	@GET
	@Path("/sensors/{id}/data")
	@Produces(MediaType.APPLICATION_JSON)
	public SensorDataList p_sensor_data_avg( @PathParam("id") String id,
			@DefaultValue("0") @QueryParam("start") String startStr,
			@DefaultValue("0") @QueryParam("end") String endStr
		) throws FileNotFoundException, IOException
	{
		return p_sensor_data( id, startStr, endStr, ConsolFun.AVERAGE);
	}

	@GET
	@Path("/sensors/{id}/data/min")
	@Produces(MediaType.APPLICATION_JSON)
	public SensorDataList p_sensor_data_min( @PathParam("id") String id,
			@DefaultValue("0") @QueryParam("start") String startStr,
			@DefaultValue("0") @QueryParam("end") String endStr
		) throws FileNotFoundException, IOException
	{
		return p_sensor_data( id, startStr, endStr, ConsolFun.MIN);
	}

	@GET
	@Path("/sensors/{id}/data/max")
	@Produces(MediaType.APPLICATION_JSON)
	public SensorDataList p_sensor_data_max( @PathParam("id") String id,
			@DefaultValue("0") @QueryParam("start") String startStr,
			@DefaultValue("0") @QueryParam("end") String endStr
		) throws FileNotFoundException, IOException
	{
		return p_sensor_data( id, startStr, endStr, ConsolFun.MAX);
	}

	public SensorDataList p_sensor_data(String id, 
			String startStr, String endStr, 
			ConsolFun consolidation
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
			log.warn("api/sensors/{id}/data(/*) failed to parse start or end");
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

		log.debug("API call sensor/"+ id +"/data(/*) with start=" + Long.toString(start) + " end=" + Long.toString(end) + ", using aggregate function " + consolidation.toString());

		// Exceptional exception handling, bro
		DataHandler h = new DataHandler(id);
		RrdDb rrdDb = h.fetchRrdDb();
		FetchRequest fetchRequest = rrdDb.createFetchRequest(consolidation, start, end); 
		FetchData fetchData = fetchRequest.fetchData();
		if ( fetchData.getRowCount() < 1 )
		{
			log.warn("Tried to get aggregate value for sensor " + id + ", but got no results. ");
			throw new IOException("No results returned");
		}

		String[] names = fetchData.getDsNames();
		log.trace("Names in data source: ");	
		for ( int i = 0; i<names.length; i++)
		{
			log.trace("  -> " + names[i]);
		}

		long[] timestamps = fetchData.getTimestamps();
		double[] values = fetchData.getValues(names[0]);

		log.trace("Number of elements in timestamps: " + Integer.toString(timestamps.length));
		log.trace("Number of elements in values: " + Integer.toString(values.length));

		SensorDataList set = new SensorDataList(
				h.getNodeID(),
				h.getCachedType().toInt(),
				consolidation.toString());

		for ( int i = 0; i<timestamps.length; i++)
		{
			Long ts = new Long(timestamps[i]);
			Double v = new Double(values[i]);
			if (!v.isNaN())
			{
				SensorDataValue dsValues = new SensorDataValue( ts, v );
				set.add(dsValues);
			}
		}

		log.debug("Returning dataset with " + Integer.toString(set.length()) + " elements.");
		//return Response.ok(set).build();
		return set;
	}

}

