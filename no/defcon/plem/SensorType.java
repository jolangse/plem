package no.defcon.plem;

public enum SensorType
{
	UINT8_MIN  	(  0, "None-type zero, reserved", "void", 0.0f, 0.0f ),
	THERMO_INT	(  7, "Generic integer temperature", "temperature", 0.0f, 255.0f ),
	DS18x20_RAW	(  8, "DS18S20 temperature sensor", "temperature", -40.0f, 155.0f ),	// signed   16bit x16 multiple
	BARO_24B	( 16, "Barometric pressure (generic)", "pressure", 0.0f, 2048.0f ),	// unsigned 24bit 4096 count
	HUMI_8B		( 32, "Relative humidity (generic)", "humidity", 0.0f, 100.0f ),	// 8 bit unsigned
	LUMI_16B	( 64, "Illumination in lux (generic)", "light", 0.0f, 65000f ),		// 16 bit unsigned
	LUMI_8B		( 65, "Light level in percent (generic)", "light", 0.0f, 100f ),		// 16 bit unsigned
	UINT8_MAX  	(255, "None-type FF, reserved", "void", 0.0f, 0.0f );

	private final int val;
	private final String description;
	private final String family;
	private final float min;
	private final float max;

	SensorType ( int val, String description, String family, float min, float max )
	{
		this.val = val;
		this.description = description;
		this.family = family;
		this.min = min;
		this.max = max;

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
	public float getMin()
	{
		return min;
	}
	public float getMax()
	{
		return max;
	}

	public static void main(String[] args)
	{
		System.out.println( SensorType.DS18x20_RAW );
		System.out.println( SensorType.DS18x20_RAW.toInt() );
		System.out.println( SensorType.DS18x20_RAW.getFamily() );
		System.out.println( SensorType.fromInt(8) );
		System.out.println( SensorType.fromInt(8).getFamily() );
	}
}
