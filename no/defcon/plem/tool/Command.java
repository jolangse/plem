package no.defcon.plem.tool;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.security.MessageDigest;
import javax.xml.bind.DatatypeConverter;

public class Command
{
	public static void main (String[] args )
	{
		int port;
		String key;

		if ( args.length < 1 )
		{
			System.out.println("Invalid usage."); 
			System.exit(1);
		}

		String action = null;

		if ( args[0] != null )
		{
			if ( args[0].equals("stop") || 
					args[0].equals("reload") || 
					args[0].equals("render") ||
					args[0].equals("keygen")
			   ) action = args[0];
		}
		if ( action == null ) 
		{
			System.out.println("Invalid usage."); 
			System.exit(1);
		}

		if ( action.equals("keygen") )
		{

			try {
				if ( args.length < 2)
					throw new java.lang.IllegalArgumentException("Not enough parameters for command");
				if ( args[1] == null)
					throw new java.lang.IllegalArgumentException("No valid key found on command line");

				String password = args[1];
				MessageDigest md = MessageDigest.getInstance("SHA1");
				md.update(password.getBytes("UTF8"));
				String outKey = DatatypeConverter.printBase64Binary( md.digest() );
				System.out.println(outKey);	
			}
			catch ( Exception e )
			{
				System.out.println("Failed to create key hash: " + e.getMessage());
				System.exit(1);
			}
			System.exit(0);
		} 

		key = System.getProperty("plem.key");
		if ( key == null )
		{
			String keyFileName = System.getProperty("plem.keyfile");
			if ( keyFileName != null )
			{
				try {
					File keyFile = new File(keyFileName);
					if ( ! keyFile.exists() )
					{
						throw new java.io.FileNotFoundException("Given key file does not exist");
					}
					BufferedReader br = new BufferedReader( new FileReader( keyFile ));
					String fromFile = br.readLine();
					if ( fromFile == null )
					{
						throw new java.io.IOException("Empty file");
					}
					key = fromFile;
				}
				catch(Exception e)
				{
					System.out.println("Failed to read key from file: " + e.getMessage());
					System.exit(1);
				}
			}
		}

		try
		{
			String portString = System.getProperty("plem.controlport", "8006");
			port = Integer.parseInt( portString );	

			URL url = new URL("http://[::1]:" + portString + "/" + action);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			if ( key != null )
			{
				connection.addRequestProperty("X-plem-key", key);
			}

			connection.setRequestMethod("GET");
			if ( connection.getResponseCode() == 200 )
			{
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String recieved;
				while ((recieved = in.readLine()) != null) {
					System.out.println(recieved);
				}
				in.close();
			}
			else
			{
				System.out.println("Request failed.");
				System.exit(1);
			}
		}
		catch (java.net.ConnectException e)
		{
			System.out.println("Unable to connect to control port.");
			System.exit(1);
		}
		catch (java.lang.NumberFormatException e)
		{
			System.out.println("Defined stopport could not be parsed."); 
			System.exit(1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
}
