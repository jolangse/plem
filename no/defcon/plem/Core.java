package no.defcon.plem;

import no.defcon.plem.jetty.ControlServlet;
import java.io.FileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Set;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

public class Core 
{
	private static Logger log = LoggerFactory.getLogger(Core.class);

	private static UDPServer udpServer;
	private static QueueConsumer consumer;
	private static HouseKeeper houseKeeper;
	private static Server jettyServer;
	private static Server controlServer;
	
	protected static LinkedBlockingQueue<ProtocolData> dataQueue;
	protected static ConfigManager cm;

	public static LinkedBlockingQueue<ProtocolData> getDataQueue()
	{
		if ( dataQueue == null )
		{
			log.debug("No dataQueue exists, creating new.");
			dataQueue = new LinkedBlockingQueue<ProtocolData>();
		}
		return dataQueue;
	}

	public static ConfigManager configManager()
	{
		if ( cm == null )
		{
			log.debug("No configuration manager present. Creating it.");
			try
			{
				cm = new ConfigManager();
			}
			catch ( org.apache.commons.configuration.ConfigurationException cex )
			{
				log.error("Failed to create configuration manager");
				log.info("For stack trace, enable debug logging.");

				StringBuilder sb = new StringBuilder();
				sb.append("\n");
				sb.append( cex.getMessage());
				sb.append("\n");
				for (StackTraceElement element : cex.getStackTrace()) 
				{
					sb.append("\t");
					sb.append(element.toString());
					sb.append("\n");
				}
				log.debug(sb.toString());

				log.error("Stopping using Runtime Exit");
				Runtime.getRuntime().exit(1);
			}
		}
		return cm;
	}

	public static void reloadConfigManager()
	{
		cm = null;
		configManager();
	}

        private static Server createJettyServer()
        {
		Server s = new Server(Core.configManager().getPlemConfig().getInt("webport", 8080));

		ResourceHandler cacheData = new ResourceHandler();
		cacheData.setResourceBase(Core.configManager().getPlemConfig().getString("exportdir", "var/export"));
		cacheData.setDirectoriesListed(true);
		ContextHandler cacheDataHandler = new ContextHandler();
		cacheDataHandler.setContextPath("/cache");
		cacheDataHandler.setHandler(cacheData);

		ResourceHandler graphData = new ResourceHandler();
		graphData.setResourceBase(Core.configManager().getPlemConfig().getString("graphdir", "var/graphs"));
		graphData.setDirectoriesListed(true);
		ContextHandler graphDataHandler = new ContextHandler();
		graphDataHandler.setContextPath("/graphs");
		graphDataHandler.setHandler(graphData);

		ResourceHandler staticData = new ResourceHandler();
		staticData.setResourceBase(Core.configManager().getPlemConfig().getString("static_web", "web/static"));
		staticData.setDirectoriesListed(true);
		ContextHandler staticDataHandler = new ContextHandler();
		staticDataHandler.setContextPath("/static");
		staticDataHandler.setHandler(staticData);

		// Create WebAppContext for JSP files.
		WebAppContext jspContext = new WebAppContext();
		jspContext.setContextPath("/");
		jspContext.setResourceBase(Core.configManager().getPlemConfig().getString("dynamic_web", "web/jsp"));

		// Create a handler list to store our static, jsp and servlet context handlers.
		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { cacheDataHandler, graphDataHandler, staticDataHandler, jspContext });

		s.setHandler(handlers);
                return s;
        }
        
	public static void startServices()
	{
		udpServer = new UDPServer(Core.configManager().getPlemConfig().getInt("lemport", 13579));
		udpServer.start();

		consumer = new QueueConsumer();
		consumer.setDelay(Core.configManager().getPlemConfig().getInt("queue_wait", 10));
		consumer.start();

		houseKeeper = new HouseKeeper();
		houseKeeper.start();

                jettyServer = createJettyServer();
		try {
			jettyServer.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void stopServices()
	{
		try {
			jettyServer.stop();
			jettyServer.join();

			udpServer.terminate();
			udpServer.join();

			consumer.terminate();
			consumer.join();

			houseKeeper.terminate();
			houseKeeper.join();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception 
	{
		log.info("Starting plem Core.");

		getDataQueue();

		startServices();

		controlServer = new Server(Core.configManager().getPlemConfig().getInt("controlport", 8006));
		controlServer.getConnectors()[0].setHost("::1");

		ServletContextHandler controlHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		controlHandler.setContextPath("/");
		controlHandler.addServlet(new ServletHolder(new ControlServlet( controlServer )), "/*");

		HandlerList commandHandlers = new HandlerList();
		commandHandlers.setHandlers(new Handler[] { controlHandler });

		controlServer.setHandler(commandHandlers);
		controlServer.start();
		controlServer.join();
	}

	static class HouseKeeper extends Thread
	{
		private volatile boolean running;

		public void terminate()
		{
			log.trace("Setting running flag to false for termination");
			running = false;
			this.interrupt();
		}

		public void run() 
		{
			running = true;
			try {

				Set<String> sensorList = Core.configManager().getSensorConfig().getSections();
				int loop_interval = Core.configManager().getPlemConfig().getInt("graph_render_interval", 300);

				// Let UDP and Queue threads wake up, get some data before first loop
				Thread.sleep( loop_interval * 1000L );

				while ( running )
				{
					long loopstartTime = System.currentTimeMillis();
					int count = 0;

					for ( String sensorID : sensorList )
					{
						log.trace("Main-loop picked up " + sensorID);
						try
						{
							DataHandler h = new DataHandler(sensorID);
							long currentTime = System.currentTimeMillis() / 1000L;
							h.generateGraph("day", (currentTime - 3600 * 24L), currentTime);
							h.generateGraph("week", (currentTime - 3600L * 24 * 7), currentTime);
							h.generateGraph("month", (currentTime - 3600L * 24 * 30), currentTime);
							h.generateGraph("year", (currentTime - 3600L * 24 * 365), currentTime);
							count++;                                            
						}
						catch ( FileNotFoundException e )
						{
							log.warn("House-keeping processing of " + sensorID + " failed: " + e.getMessage());
						}
					}
					long elapsedTime = System.currentTimeMillis() - loopstartTime;
					log.info("Woke up and generated graphs for " + count + " sensors. Active time was " + elapsedTime + " ms.");
					long sleepTime = (loop_interval*1000L) - elapsedTime;
					if ( sleepTime > 0 )
					{
						log.debug("Going to sleep for " + sleepTime + " ms...");
						Thread.sleep( sleepTime );
					}

				}
			}
			catch ( Exception e )
			{
				log.warn( e.getMessage() );
			}
			log.debug("Housekeeper thread terminating");
		}
	}
}
