package no.defcon.plem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import java.awt.Color;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;

public class DataHandler
{
	private static Logger log = LoggerFactory.getLogger(DataHandler.class);
	private String dataDir;
	private String graphDir;
	private String exportDir;

	private String nodeID;

	protected ConfigManager cm;

	public DataHandler ( String nodeID ) throws FileNotFoundException
	{
		this.cm = Core.configManager();

		dataDir = cm.getPlemConfig().getString("datadir", "var/rrd");
		log.trace("Asked for datadir, got " + dataDir );

		graphDir = cm.getPlemConfig().getString("graphdir", "var/graphs");
		log.trace("Asked for graphdir, got " + graphDir );

		exportDir = cm.getPlemConfig().getString("exportdir", "var/export");
		log.trace("Asked for exportdir, got " + exportDir );

		File d = this.getDataDir();
		if ( !d.exists() || !d.isDirectory() || !d.canWrite() )
		{
			log.error("Configured data directory does not exist, or is not writeable");
			throw new FileNotFoundException("Error accessing data directory: " + dataDir );
		}

		d = this.getGraphDir();
		if ( !d.exists() || !d.isDirectory() || !d.canWrite() )
		{
			log.error("Configured graph directory does not exist, or is not writeable");
			throw new FileNotFoundException("Error accessing graph directory: " + graphDir );
		}

		d = this.getExportDir();
		if ( !d.exists() || !d.isDirectory() || !d.canWrite() )
		{
			log.error("Configured export directory does not exist, or is not writeable");
			throw new FileNotFoundException("Error accessing export directory: " + exportDir );
		}

		log.trace("Data directories exist..");

		this.nodeID = nodeID;
		log.trace("Created RRDHandler for sensor ID " + nodeID + " with NULL data packet");
	}

	public String getNodeID() {
		return nodeID;
	}

	protected File getDataDir() throws FileNotFoundException
	{
		File dd = new File(dataDir);
		if (!dd.exists() || !dd.isDirectory() || !dd.canWrite()) {
			log.trace("Configured data directory does not exist, or is not writeable");
			throw new FileNotFoundException("Error accessing data directory: " + dd.getAbsolutePath());
		}
		return dd;
	}

	protected File getGraphDir() throws FileNotFoundException
	{
		File gd = new File(graphDir);
		if (!gd.exists() || !gd.isDirectory() || !gd.canWrite()) {
			log.trace("Configured graph directory does not exist, or is not writeable");
			throw new FileNotFoundException("Error accessing graph directory: " + gd.getAbsolutePath());
		}
		return gd;
	}

	protected File getExportDir() throws FileNotFoundException
	{
		File ed = new File(exportDir);
		if (!ed.exists() || !ed.isDirectory() || !ed.canWrite()) {
			log.trace("Configured export directory does not exist, or is not writeable");
			throw new FileNotFoundException("Error accessing export directory: " + ed.getAbsolutePath());
		}
		return ed;
	}

	protected boolean checkRange( float value )
	{
		log.trace("Testing if sensor value is in range, sensor value is " + Float.toString(value));
		float cfg_min       = cm.getSensorConfig().getFloat(this.getNodeID() + ".min",    -40.0f);
		float cfg_max       = cm.getSensorConfig().getFloat(this.getNodeID() + ".max",    100.0f);
		if ( value < cfg_min || value > cfg_max )
		{
			return false;
		}
		return true;
	}

	protected boolean checkDelta(float value) {
		float maxDelta = cm.getSensorConfig().getFloat(this.getNodeID() + ".max_delta",
				cm.getPlemConfig().getFloat("max_delta", 0.0f));

		if (maxDelta != 0.0f) {
			try {
				log.debug("Doing simple delta-filtering, based on recorded 10-minute average.");
				float average = this.getAverage(10);
				float delta = Math.abs(value - average);
				log.trace("Max delta: " + Float.toString(maxDelta));
				log.trace("Read AVERAGE value: " + Float.toString(average));
				log.trace("Recieved value:" + Float.toString(value));
				log.trace("Calculated delta: " + Float.toString(delta));
				if (delta > maxDelta) {
					log.warn("Sensor ID " + this.getNodeID()
							+ " reported value exceeds max delta : reported "
							+ Float.toString(value)
							+ " with stored " + Float.toString(average)
							+ " gives delta " + Float.toString(delta)
							+ " exceeding max delta " + Float.toString(maxDelta));
					return false;
				}
			} catch (IOException e) {
				log.warn("Delta test for sensor " + this.getNodeID() + " failed because of IO exception: " + e.getMessage());
				return false;
			}
		}
		return true;
	}

	public File getDataFile(ProtocolData data) throws FileNotFoundException, IOException
	{
		File d = this.getDataDir();

		File rrdFile = new File( d, data.getNodeID() + ".jrrd" );
		if ( rrdFile.exists() )
		{
			return rrdFile;
		}

		log.debug("RRD file of name " + rrdFile.toString() + " does not exits, will try to create it.");

		int   interval  = cm.getSensorConfig().getInt(data.getNodeID() + ".interval", 60);
		int   heartbeat = interval *
			cm.getSensorConfig().getInt(data.getNodeID() + ".max_loss", 
				cm.getPlemConfig().getInt("max_loss", 10) );

		long currentTime = System.currentTimeMillis() / 1000L;
		String sourceName = data.getType().getFamily();
		RrdSettings s = new RrdSettings(sourceName, DsType.GAUGE, interval, heartbeat);

		// TODO: Add handler for java.lang.IllegalArgumentException
		// rrdAddArchive +++ will throw one of those every now and again,
		// may be a nice source for bugs :P
		try
		{
			log.trace("Creating RRD file with source name " + sourceName + ", step " + interval + " and heartbeat " + heartbeat);
			log.trace("Samples/day is " + s.getDailySamples() );
			RrdDef rrdDef = new RrdDef(rrdFile.getAbsolutePath(), interval);
			rrdDef.setStartTime( (currentTime - heartbeat) );
			rrdDef.addDatasource( s.getSourceName(), s.getDsType(), s.getHeartbeat(), Double.NaN, Double.NaN );
			// Raw values, store last recieved
			rrdDef.addArchive(ConsolFun.LAST, 0.5, 1, s.getDailySamples()/24);   // Every sample, one hour of data
			// Averaged values
			rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 1, s.getDailySamples());   // Every sample, one day
			rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, s.getAvgnumDay(), s.getSamplesDay());  // One day of data
			rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, s.getAvgnumWeek(), s.getSamplesWeek());  // One week of data
			rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, s.getAvgnumMonth(), s.getSamplesMonth());  // One Month of data
			rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, s.getAvgnumYear(), s.getSamplesYear());  // One year of data
			// Minimum values
			rrdDef.addArchive(ConsolFun.MIN, 0.5, s.getAvgnumDay(), s.getSamplesDay());  // One day of data
			rrdDef.addArchive(ConsolFun.MIN, 0.5, s.getAvgnumWeek(), s.getSamplesWeek());  // One week of data
			rrdDef.addArchive(ConsolFun.MIN, 0.5, s.getAvgnumMonth(), s.getSamplesMonth());  // One Month of data
			rrdDef.addArchive(ConsolFun.MIN, 0.5, s.getAvgnumYear(), s.getSamplesYear());  // One year of data
			// Maximum values
			rrdDef.addArchive(ConsolFun.MAX, 0.5, s.getAvgnumDay(), s.getSamplesDay());  // One day of data
			rrdDef.addArchive(ConsolFun.MAX, 0.5, s.getAvgnumWeek(), s.getSamplesWeek());  // One week of data
			rrdDef.addArchive(ConsolFun.MAX, 0.5, s.getAvgnumMonth(), s.getSamplesMonth());  // One Month of data
			rrdDef.addArchive(ConsolFun.MAX, 0.5, s.getAvgnumYear(), s.getSamplesYear());  // One year of data

			RrdDb rrdDb = new RrdDb(rrdDef);
			rrdDb.close();
			log.info("RRD file created: " + rrdFile.getAbsolutePath());
		}
		catch (java.io.IOException e)
		{
			// TODO: Clean up this, the exception handling is WAYYY bad.
			log.error(e.getMessage());
			throw e;
		}

		if ( rrdFile.exists() )
		{
			return rrdFile;
		}
		else
		{
			throw new IOException("Unable to create new RRD file at " + rrdFile.getAbsolutePath());
		}
	}

	public File getDataFile(String nodeID)  throws FileNotFoundException, IOException
	{
		File dd = this.getDataDir();
		File rrdFile = new File( dd, this.getNodeID() + ".jrrd" );
		if ( !rrdFile.exists() )
		{
			log.warn("Graph generation for " + this.getNodeID() + " failed, data file does not exist.");
			throw new FileNotFoundException("Data file does not exist: " + rrdFile.getAbsolutePath() );
		}
		return rrdFile;
	}

	public boolean processData(ProtocolData data) throws FileNotFoundException, NullPointerException, IOException
	{
		log.trace("Starting to process data");
		if ( data == null )
		{
			log.error("Tried to do RRD processing of NULL data.");
			throw new NullPointerException();
		}
		log.debug("Processing for sensor ID " +data.getNodeID());

		String sourceName = data.getType().getFamily();
		long currentTime = System.currentTimeMillis() / 1000L;
		File rrdFile = this.getDataFile(data);

		log.trace("Using RRD file to save data at path " + rrdFile.getAbsolutePath());

		checkRange(data.getValue());

		if ( !checkDelta( data.getValue() ) )
		{
			log.warn("Delta test for sensor " + data.getNodeID() + " failed. Not storing value.");   
			return false;
		}
		log.debug("Trying to add sensor value to data file" + rrdFile.getAbsolutePath());
		double value = (double)data.getValue();
		try
		{
			RrdDb rrdDb = new RrdDb( rrdFile.getAbsolutePath() );
			Sample sample = rrdDb.createSample();
			sample.setTime( currentTime );
			sample.setValue( sourceName, value );
			sample.update();
			rrdDb.close();
			log.info("Sensor ID " + data.getNodeID() + " reported value " +  Float.toString(data.getValue()) + ", value saved to data file.");

			FileWriter w;
			File ed = this.getExportDir();

			// Export files to one-dir-per-sensor.
			// Set up sensor dir file object
			File sd = new File( ed, data.getNodeID() );
			// Approach to creating needed directories after verifying parent:
			if (!sd.exists() && !sd.mkdirs()) {
				throw new IOException("Unable to create " + sd.getAbsolutePath());
			}

			File idFile = new File( sd, "id" );
			File nameFile = new File( sd, "name" );
			File typeFile = new File( sd, "type" );
			File valueFile = new File( sd, "last" );
			File avgFile = new File( sd, "avg" );
			File maxFile = new File( sd, "max" );
			File minFile = new File( sd, "min" );

			float average = this.getAverage(60);
			float min = this.getMin(60);
			float max = this.getMax(60);

			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(2);

			String displayName = cm.getSensorConfig().getString(this.getNodeID() + ".displayname", "Unnamed sensor");
			int type = data.getType().toInt();

			w = new FileWriter( idFile );
			w.write( data.getNodeID() );
			w.write( System.getProperty( "line.separator" ) );
			w.close();

			w = new FileWriter( nameFile );
			w.write( displayName );
			w.write( System.getProperty( "line.separator" ) );
			w.close();

			w = new FileWriter( typeFile );
			w.write( Integer.toString(type) );
			w.write( System.getProperty( "line.separator" ) );
			w.close();

			w = new FileWriter( valueFile );
			w.write( df.format( data.getValue() ) );
			w.write( System.getProperty( "line.separator" ) );
			w.close();

			w= new FileWriter ( avgFile );
			w.write( df.format(average) );
			w.write( System.getProperty( "line.separator" ) );
			w.close();

			w= new FileWriter ( minFile );
			w.write( df.format(min) );
			w.write( System.getProperty( "line.separator" ) );
			w.close();

			w= new FileWriter ( maxFile );
			w.write( df.format(max) );
			w.write( System.getProperty( "line.separator" ) );
			w.close();

			log.debug("Dumped values to export files..");
		}
		catch (java.io.IOException e)
		{
			// TODO: Clean up this, the exception handling is WAYYY bad.
			log.error(e.getMessage());
		}

		log.trace("Processing complete..");
		return true;
	}
        
	public void generateGraph( String name, long start, long end ) throws FileNotFoundException, IOException
	{
		log.trace("Graph generation requested");
		if ( this.getNodeID() == null )
		{
			log.error("Tried to generate graph for NULL identifier.");
			throw new NullPointerException();
		}
		log.trace("Processing for sensor ID " + this.getNodeID());

		File rrdFile = this.getDataFile(this.getNodeID());

		File gd = this.getGraphDir();

		// Set up sensor dir file object
		File sd = new File( gd, this.getNodeID());
		// Approach to creating needed directories after verifying parent:
		if (!sd.exists() && !sd.mkdirs()) {
			throw new IOException("Unable to create " + sd.getAbsolutePath());
		}

		File imageFile = new File(sd, name + ".png");
		String dbFile = rrdFile.getAbsolutePath();
		String sourceName = this.getCachedType().getFamily();

		log.debug("Will try to render graph from " + dbFile);
		log.debug("Will try to render graph to " + imageFile.getAbsolutePath());

		String graphTitle = cm.getSensorConfig().getString(this.getNodeID() + ".displayname", "Unnamed sensor");
		String graphLabel = "Graph for " + name;

		RrdGraphDef graphDef = new RrdGraphDef();
		graphDef.setTimeSpan(start, end);

		graphDef.setAltAutoscale( cm.getSensorConfig().getBoolean(this.getNodeID() + ".autoscale", false ));

		// TODO: Get graph size info from configuration
		graphDef.setWidth(600);
		graphDef.setHeight(200);
		graphDef.setNoLegend(false);
		graphDef.setShowSignature(false);

		graphDef.setAntiAliasing(true);
		graphDef.setTitle(graphTitle);
		graphDef.setVerticalLabel(graphLabel);

		Color white = new Color(0xFF, 0xFF, 0xFF);
		Color babyblue = new Color(0xAA, 0xBB, 0xEE);
		Color black = new Color(0, 0, 0);
		log.trace("Setting up graph sources and content");
		graphDef.datasource("value", dbFile, sourceName, ConsolFun.AVERAGE);
		graphDef.datasource("min", dbFile, sourceName, ConsolFun.MIN);
		graphDef.datasource("max", dbFile, sourceName, ConsolFun.MAX);
		graphDef.datasource("delta", "max,min,-");
		graphDef.datasource("zero", "0");
		graphDef.area("min", white);
		graphDef.stack("delta", babyblue);
		graphDef.line("min", babyblue, "Min", 1);
		graphDef.line("max", babyblue, "Max", 1);
		graphDef.line("value", black, "Average", 1);
		if ( cm.getSensorConfig().getBoolean(this.getNodeID() + ".graph_zero", false ) )
			graphDef.line("zero", black, 1);

		graphDef.setImageFormat("png");
		graphDef.setFilename( imageFile.getAbsolutePath() );

		log.trace("Rendering graph file");
		// then actually draw the graph
		RrdGraph graph = new RrdGraph(graphDef);
		BufferedImage bi = new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
		graph.render(bi.getGraphics());
		log.trace("Graph file rendered.");
	}

	public float getAverage() throws FileNotFoundException, IOException
	{
		return getAverage( 10 );
	}

	public float getAverage( int minutes ) throws FileNotFoundException, IOException
	{
		return (float) getAggregate( ConsolFun.AVERAGE, minutes );
	}

	public float getMax() throws FileNotFoundException, IOException
	{
		return getMax( 10 );
	}

	public float getMax( int minutes ) throws FileNotFoundException, IOException
	{
		return (float) getAggregate( ConsolFun.MAX, minutes );
	}

	public float getMin() throws FileNotFoundException, IOException
	{
		return getMin( 10 );
	}

	public float getMin( int minutes ) throws FileNotFoundException, IOException
	{
		return (float) getAggregate( ConsolFun.MIN, minutes );
	}

	private Float getAggregate( ConsolFun consolidation, int minutes ) throws FileNotFoundException, IOException
	{
		log.trace("Poking for aggregate recorded value over " + minutes + " minutes");
		if ( getNodeID() == null )
		{
			log.error("Tried to get aggregate for NULL identifier.");
			throw new NullPointerException();
		}
		log.trace("Processing for sensor ID " +this.getNodeID());

		File dd = this.getDataDir();
		File rrdFile = new File( dd, this.getNodeID() + ".jrrd" );
		if ( !rrdFile.exists() )
		{
			throw new FileNotFoundException("Data file does not exist: " + rrdFile.getAbsolutePath() );
		}

		log.trace("Opening RRD file " + rrdFile.getAbsolutePath());
		RrdDb rrdDb = new RrdDb(rrdFile.getAbsolutePath());

		long currentTime = System.currentTimeMillis() / 1000L;

		log.trace("Fetching " + minutes + " minutes values from RRD");
		FetchRequest fetchRequest = rrdDb.createFetchRequest(consolidation, currentTime-(minutes*60L) , currentTime); 
		FetchData fetchData = fetchRequest.fetchData();

		if ( fetchData.getRowCount() < 1 )
		{
			log.warn("Tried to get aggregate value for sensor " + this.getNodeID() + ", but got no results. ");
			return Float.NaN;
		}

		try
		{
			Float average = new Float( fetchData.getAggregate(this.getCachedType().getFamily(), consolidation));
			log.debug("Query for aggregate " + consolidation.toString() +  
					" value for sensor " + this.getNodeID() + 
					" returns "  +  average.toString() );

			return average;
		}
		catch ( FileNotFoundException e )
		{
			log.debug("Triggered a file not found exception. Cache may not be ready. Returning NaN.");
			return Float.NaN;
		}

	}

	public SensorType getCachedType() throws FileNotFoundException, IOException
	{
		File ed = this.getExportDir();

		// Export files to one-dir-per-sensor.
		// Set up sensor dir file object
		File sd = new File( ed, this.getNodeID() );
		// Approach to creating needed directories after verifying parent:
		if (!sd.exists() ) {
			throw new FileNotFoundException("Directory " + sd.getAbsolutePath() + " does not exist");
		}
		File typeFile = new File( sd, "type" );
		if (!typeFile.exists() ) {
			throw new FileNotFoundException("Sensor type file " + typeFile.getAbsolutePath() + " does not exist");
		}

		BufferedReader br = new BufferedReader( new FileReader( typeFile ));
		String fromFile = br.readLine();
		if ( fromFile == null )
			throw new IOException("Got no data from typeFile " + typeFile.getAbsolutePath());

		try
		{
			int i = Integer.parseInt(fromFile);
			return SensorType.fromInt(i);
		}
		catch(NumberFormatException e)
		{
			log.warn("Reading type information from file failed number parsing: " + e.getMessage() );
			throw new IOException("Could not get type value from file " + typeFile.getAbsolutePath());
		}
	}
}



