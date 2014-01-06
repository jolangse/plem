<%@page 
	language="java" 
	import="java.util.Set"
	import="java.io.File"
	import="no.defcon.plem.Core"
	contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Boxed Temperatures - plem demo</title>

<style>
	.wrap {
		width: 22vw;
		height: 8vw;
		border: 2px outset;
		float: left;
		position: relative;
		padding: 2px;
		margin: 2px;
		background-color: #e0e0e0;
	}

	.name {
		font-size: 1.5vw;
	}
	
	.temperature {
		font-size: 4.5vw;
		font-weight: bold;
		position:absolute;
		bottom:0;
		width: 100%;
		text-align: center;
	}
	
	.graphs {
		position: absolute;
		bottom: 2vw;
	}
	
	img.graph { 
		width: 46vw;
	}

	body {
		background: url("/static/plem-logo-large.png") no-repeat fixed center;
	}
</style>

<script src="/static/jquery-2.0.3.min.js"></script>

<script type="text/javascript">
function getDisplayName ( sensorID ) {
	$.get("/cache/"+sensorID+"/name",
	function( response ) {
		$("#name-"+sensorID).text(response);
	});
}

function getTemperature ( sensorID ) {
	$.get("/cache/"+sensorID+"/last",
	function( response ) {
		$("#temp-"+sensorID).text(response);
	});
	setTimeout( function() { getTemperature( sensorID ) }, 60000 );
}

function reloadGraphs()
{
	var day = $('#graph-day').attr('src');
	$('#graph-day').attr('src', '');
	$('#graph-day').attr('src', day);
	
	var week = $('#graph-week').attr('src');
	$('#graph-week').attr('src', '');
	$('#graph-week').attr('src', week);
	
	setTimeout( function() { reloadGraphs() }, 120000 );
}

function loadGraph( sensorID ) {
	$("#graphs").html(
		"<img class='graph' id='graph-week' src='/graphs/" + sensorID + "/week.png'>" +
		"<img class='graph' id='graph-day' src='/graphs/" + sensorID + "/day.png'>"
	);
}
</script>

</head>
<body>
<%
	Set<String> sensorList = Core.configManager().getSensorConfig().getSections();
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
			out.println("<div class='wrap' id='wrap-" + sensorID + "'>");
			out.println("<div class='name' id='name-" + sensorID + "'>");
			out.println("</div>\n");
			out.println("<div class='temperature' id='temp-" + sensorID + "'>");
			out.println("</div>\n");
			out.println("</div>\n");
		}
	}	
%>
<div class="graphs" id="graphs">
</div>
</body>

<script type="text/javascript">
<%	for ( String sensorID : sensorList )
	{
		out.println("getDisplayName('" +  sensorID + "');");
		out.println("getTemperature('" +  sensorID + "');");
		out.println("$('#wrap-" +  sensorID + "').click( function(){loadGraph('" +  sensorID + "')});");
	} %>

	reloadGraphs();
</script>
</html>
