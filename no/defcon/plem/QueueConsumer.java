package no.defcon.plem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class QueueConsumer extends Thread
{
	private static Logger log = LoggerFactory.getLogger(QueueConsumer.class);
	private static boolean running;
	private int delay;

	public QueueConsumer ()
	{
	}

	public void setDelay( int delay )
	{
		this.delay = delay;
	}

	public int getDelay ()
	{
		return delay;
	}

	public void terminate()
	{
		log.trace("Telling Queue Consumer to stop.");
		running = false;
		this.interrupt();
	}

	public void run ()
	{
		log.info("Packet data queue consumer started.");
		LinkedBlockingQueue<ProtocolData> queue = Core.getDataQueue();
		Set<String> sensorList = Core.configManager().getSensorConfig().getSections();

		running = true;
		while(running && !Thread.currentThread().isInterrupted()) 
		{
			try
			{
				if ( queue.size() > 0 )
				{
					log.debug("Queue size is " + queue.size() + ", trying to pop one off.");
					ProtocolData d = queue.take();
					log.trace("I fhink I got one... " + d.getNodeID());

					if ( sensorList.contains( d.getNodeID() ) )
					{
						try {
							log.trace("List does contain " + d.getNodeID());
							DataHandler h = new DataHandler(d.getNodeID());
							h.processData(d);
						} catch (java.io.FileNotFoundException e) {
							log.error("Something went wrong, I recieved a FileNotFound exception: " + e.getMessage());
						} catch (java.io.IOException e) {
							log.error("Something went wrong, I recieved a FileNotFound exception: " + e.getMessage());
						}
					}
					else
					{
						log.info("Unknown sensor ID " + d.getNodeID() + " of type " + d.getType().toInt() + " (" + d.getType().toString() + ") reports value " + d.getValue() );
					}
				}
				else if ( delay > 0) 
					sleep(delay);
			}
			catch (java.lang.InterruptedException e)
			{
				if ( running )
				{
					log.error("Caught unexpected InterruptedException: " + e.getMessage());
					return;
				}

				log.trace("Caught InterruptedException while terminating normally.");
				return;
			}
		}

		log.info("Stopping queue consumer");
	}
}
