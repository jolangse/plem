package no.defcon.plem.jetty;

import no.defcon.plem.Core;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.xml.bind.DatatypeConverter;

public class ControlServlet extends HttpServlet {

	private static Server s;

	public ControlServlet ( Server jettyServer )
	{
		super();
		s = jettyServer;
	}

	private void callStop()
	{
		new Thread()
		{
			public void run() {
				try {
					s.stop();
				} catch (Exception ex) {
					System.out.println("Error when stopping Jetty server: "+ex.getMessage());
				}
			}
		}.start();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException 
	{
		PrintWriter out = response.getWriter();
		String controlKey = Core.configManager().getPlemConfig().getString("controlkey", null);
		if ( controlKey != null )
		{
			String headerKey = request.getHeader("X-plem-key");
			if ( headerKey == null )
			{
				out.println("Missing control key");
				out.flush();
				return;
			}
			try {
				byte[] check = DatatypeConverter.parseBase64Binary( controlKey );
				MessageDigest md = MessageDigest.getInstance("SHA1");
				md.update(headerKey.getBytes("UTF8"));
				byte[] digest = md.digest();

				if ( ! Arrays.equals( digest, check ) )
				{
					out.println("Control key fails verification.");
					return;
				}
			}
			catch ( Exception e )
			{
				out.println("Unable to verify key: " + e.getMessage() );
				return;
			}
		}

		if ( request.getRequestURI().equals("/stop"))
		{
			out.println("Closing down non-control services.");
			out.flush();
			Core.stopServices();
			out.println("Shutting down service.");
			callStop();
		}
		else if ( 
				request.getRequestURI().equals("/restart") || 
				request.getRequestURI().equals("/reload"))
		{
			out.println("Closing down non-control services.");
			out.flush();
			Core.stopServices();
			out.println("Re-initializing configuration manager.");
			out.flush();
			Core.reloadConfigManager();
			out.println("Starting services up.");
			out.flush();
			Core.startServices();
			out.println("Done reloading.");
		}
		else
		{
			out.println("Unknown request:");
			out.println(request.getRequestURI());
		}
	}
}
