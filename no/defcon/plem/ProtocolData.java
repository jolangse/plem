package no.defcon.plem;

public class ProtocolData
{

	public static String preamble = "";

	private int version;
	private String nodeID;
	private SensorType type;
	private float value;
	private long timestamp;

	public ProtocolData ( )
	{
		this.nodeID = null;
	}

	public ProtocolData ( String id )
	{
		this.nodeID = id;
	}

	public ProtocolData ( String id, SensorType type )
	{
		this.nodeID = id;
		this.type = type;
	}

	public ProtocolData ( String id, SensorType type, float value )
	{
		this.nodeID = id;
		this.type = type;
		this.value = value;
	}

	public String getNodeID ( )
	{
		return nodeID;
	}

	public void setNodeID ( String id )
	{
		this.nodeID = id;
	}

	public SensorType getType()
	{
		return type;
	}

	public void setType ( SensorType type )
	{
		this.type = type;
	}

	public float getValue()
	{
		return this.value;
	}

	public void setValue( float value )
	{
		this.value = value;
	}

	public void setTimestamp( long time )
	{
		this.timestamp = time;
	}

	public long getTimestamp ()
	{
		return this.timestamp;
	}

	public void dump()
	{
		System.out.println("Version:   " + version);
		System.out.println("Timestamp: " + timestamp);
		System.out.println("Type:      " + type);
		System.out.println("Node ID:   " + nodeID);
		System.out.println("Value      " + value);
	}
}
