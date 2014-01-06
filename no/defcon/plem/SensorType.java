package no.defcon.plem;

public enum SensorType
{
	THERMO_INT	(7, "Generic integer temperature sensor", "temperature" ),
	DS18x20_RAW	(8, "DS18S20 1Wire format temperature sensor", "temperature" );

	private final int val;
	private final String description;
	private final String family;

	SensorType ( int val, String description, String family )
	{
		this.val = val;
		this.description = description;
		this.family = family;

	}
	
	public static SensorType fromInt ( int value )
	{
		for ( SensorType t: values() )
		{
			if ( value == t.toInt() )
				return t;
		}
		return null;
	}

	public String toString ()
	{ 
		return description;
	}

	public int toInt()
	{
		return val;
	}

	public String getFamily()
	{
		return family;
	}

	public static void main(String[] args)
	{
		System.out.println( SensorType.DS18x20_RAW );
		System.out.println( SensorType.DS18x20_RAW.toInt() );
		System.out.println( SensorType.DS18x20_RAW.getFamily() );
		System.out.println( SensorType.fromInt(8).getFamily() );
	}
}
