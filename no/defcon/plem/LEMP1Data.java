package no.defcon.plem;

import java.util.Arrays;

public class LEMP1Data extends ProtocolData
{
	public static String preamble = "LEMP1";

	public LEMP1Data ( )
	{
		super( );
	}

	public LEMP1Data ( String id, SensorType type, float value)
	{
		super( id, type, value );
	}

	public boolean parseDataUnit ( byte[] data )
	{
		int p;
		int tp;
		int len;

		byte[] idBytes;

		setTimestamp( System.currentTimeMillis() / 1000L );

		String preamble = new String(Arrays.copyOfRange(data, 0, 5));
		if ( ! preamble.equals("LEMP1") ) return false;

		p = 6;

		tp = data[p++];
		len = data[p++];

		if ( tp == 0x01 )
		{
			idBytes = Arrays.copyOfRange(data, p, (len+p));
		}
		else return false;

		p += len;
		tp = data[p++];
		len = data[p++];

		if ( SensorType.fromInt(tp).equals(SensorType.DS18x20_RAW) )
		{

			setType( SensorType.DS18x20_RAW );

			if ( len != 2 ) return false;

			short tmp;
			tmp =  (short) ( (data[p++] & 0xFF)<<8 );
			tmp |= (short) (data[p++] & 0xFF);

			float val = (float)Math.round(tmp/16.0f*10.0f)/10.0f;

			setValue( val );
		}
		else if ( SensorType.fromInt(tp).equals(SensorType.BARO_24B) )
		{

			setType( SensorType.BARO_24B );

			if ( len != 3 ) return false;

			int tmp;
			tmp  = (int) ( (data[p++] & 0xFF)<<16 );
			tmp |= (int) ( (data[p++] & 0xFF)<<8 );
			tmp |= (char) (data[p++] & 0xFF);

			float val = ((float)tmp)/4096;

			setValue( val );
		}
		else if ( SensorType.fromInt(tp).equals(SensorType.HUMI_8B) )
		{
			setType( SensorType.HUMI_8B );
			if ( len != 1 ) return false;

			short tmp = (short) (data[p++] & 0xFF);
			float val = (float)tmp;

			setValue( val );
		}
		else if ( SensorType.fromInt(tp).equals(SensorType.LUMI_8B) )
		{
			setType( SensorType.LUMI_8B );
			if ( len != 1 ) return false;

			short tmp = (short) (data[p++] & 0xFF);
			float val = (float)tmp;

			setValue( val );
		}
		else if ( SensorType.fromInt(tp).equals(SensorType.LUMI_16B) )
		{

			setType( SensorType.LUMI_16B );

			if ( len != 2 ) return false;

			char tmp;
			tmp =  (char) ( (data[p++] & 0xFF)<<8 );
			tmp |= (char) (data[p++] & 0xFF);

			float val = (float)tmp;

			setValue( val );
		}
		else if ( SensorType.fromInt(tp).equals(SensorType.LUMI_24B) )
		{

			setType( SensorType.LUMI_24B );

			if ( len != 3 ) return false;

			int tmp;
			tmp  = (int) ( (data[p++] & 0xFF)<<16 );
			tmp |= (int) ( (data[p++] & 0xFF)<<8 );
			tmp |= (char) (data[p++] & 0xFF);

			float val = ((float)tmp)/32.0f;

			setValue( val );
		}
		else return false;

		StringBuilder sb = new StringBuilder();
		for(byte b: idBytes)
			sb.append(String.format("%02x", b&0xff));
		setNodeID(sb.toString());

		return true;
	}
}
