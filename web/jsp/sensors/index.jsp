<%-- This example JSP file is scheduled for removal --%>
<%-- ----------------------------------------------
     This file implements a simple JSON-response
     listing all sensors defined in configuration
     that also has data logged.
     This feature is now implemented by the 
     Jetty+Jersey+Jaxon REST API.
     The replacement URI is /api/sensors
     ---------------------------------------------- --%>
<%@page 
	language="java" 
	import="java.util.Set"
	import="java.io.File"
	import="no.defcon.plem.Core"
	contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
%>
{ "sensors": [
<%
	Set<String> sensorList = Core.configManager().getSensorConfig().getSections();
	boolean n = false;
	for ( String sensorID : sensorList )
	{
		File exportDir = new File( Core.configManager().getPlemConfig().getString("exportdir", "var/export") );
		File sensorDir = new File( exportDir, sensorID );
		File nameFile = new File ( sensorDir, "name" );
		// The name file will only exist if there has been at least one data dump
		// for the given sensor. By testing for that file, we can filter from configured
		// sensors to sensors that we have the minimal amount of data for.
		if ( nameFile.exists() )
		{
			if (n) {
				out.print(", ");
			}
			out.print("\"" + sensorID + "\"");
			n = true;
		}
	}	
%>
]}
