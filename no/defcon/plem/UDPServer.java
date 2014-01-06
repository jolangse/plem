package no.defcon.plem;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.LinkedBlockingQueue;

public class UDPServer extends Thread
{
	private static Logger log = LoggerFactory.getLogger(UDPServer.class);
	private static DatagramSocket serverSocket;
	private static int port;
	private static boolean running;

	public UDPServer ( int serverPort )
	{
		port = serverPort;
	}

	public void terminate()
	{
		log.trace("Telling UDP Server to stop.");
		running = false;
		this.interrupt();
	}
	
	public void run ()
	{
		log.info("Starting UDP receiving on port " + port);
		try
		{
			serverSocket = new DatagramSocket(port);
		}
		catch ( SocketException e )
		{
			log.error("Unable to bind to UDP port " + port );
			return;
		}

		running = true;

		try {
			serverSocket.setSoTimeout(2000);
		}
		catch (SocketException e) {
			log.error("Failed to set socket timeout. Waiting for packets with blocking IO.");
		}

		while(running && !Thread.currentThread().isInterrupted()) 
		{
			this.getPacket();
		}
		log.info("UDP server thread interrupted, stopping.");

		serverSocket.close();
	}

	public void getPacket()
	{
		byte[] buffer = new byte[1024];

		DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
		try
		{
			serverSocket.receive(receivePacket);
		}
		catch ( SocketTimeoutException e )
		{
			return;
		}
		catch ( java.io.IOException e )
		{
			log.error("IOException: " + e.getMessage());
			return;
		}

		log.debug("Recieved packet from " + 
				receivePacket.getAddress().toString() + ":" 
				+ receivePacket.getPort() + 
				" of length " + receivePacket.getLength());

		byte[] receiveData;
		receiveData = receivePacket.getData();

		String preamble = new String(Arrays.copyOfRange(receiveData, 0, 5));

		// ---------- HANDLER FOR LEMP1 -----------
		if ( preamble.equals( LEMP1Data.preamble ) )
		{
			log.debug("Packet is Protocol version 1");
			LEMP1Data d = new LEMP1Data();
			log.debug("Trying to parse data");
			if ( d.parseDataUnit( receiveData ) )
			{
				log.debug("Parse OK, attempting to add to Queue");
				LinkedBlockingQueue<ProtocolData> q = Core.getDataQueue();
				try
				{
					q.put(d);
					log.info("Protocol version 1 packet from " +
						receivePacket.getAddress().toString() + ":" 
						+ receivePacket.getPort() + 
						" for sensor ID " + d.getNodeID() +
						" recieved and queued");
				}
				catch (java.lang.InterruptedException e)
				{
					log.error("I got interrupted. Packet lost.");
					return;
				}
			}

			else
			{
				log.info("Invalid packet for Protocol Version 1");
			}
		}

		// ---------- UNKNOWN CONTENT -------------
		else
		{
			log.info("Unknown packet format. Preamble was: " + preamble);
		}
	}
}
