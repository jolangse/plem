package no.defcon.plem;

import org.rrd4j.DsType;

public class RrdSettings
{
	private String sourceName;
	private DsType type;
	private int step;
	private int heartbeat;
	private int dailySamples;

	public RrdSettings (String sourceName, DsType type, int step, int heartbeat)
	{
		this.sourceName = sourceName;
		this.type = type;
		this.step = step;
		this.heartbeat = heartbeat;
		this.dailySamples = (int) ( 1440.0f / ( step / 60.f) );
	}


	public String getSourceName()
	{
		return sourceName;
	}

	public DsType getDsType()
	{
		return type;
	}

	public int getStep()
	{
		return step;
	}

	public int getHeartbeat()
	{
		return heartbeat;
	}

	public int getDailySamples()
	{
		return dailySamples;
	}

	public int getSamplesDay ()
	{
		return Core.configManager().getPlemConfig().getInt("samples_daily", 288);
	}

	public int getSamplesWeek ()
	{
		return Core.configManager().getPlemConfig().getInt("samples_weekly", 336);
	}

	public int getSamplesMonth ()
	{
		return Core.configManager().getPlemConfig().getInt("samples_monthly", 1344);
	}

	public int getSamplesYear ()
	{
		return Core.configManager().getPlemConfig().getInt("samples_yearly", 730);
	}

	public int getAvgnumDay ()
	{
		int num = dailySamples/getSamplesDay();
		if (num == 0) num = 1;
		return num;
	}

	public int getAvgnumWeek ()
	{
		int num = 7*dailySamples/getSamplesWeek();
		if ( num == 0) num = 1;
		return num;
	}

	public int getAvgnumMonth ()
	{
		int num = 30*dailySamples/getSamplesMonth();
		if ( num == 0) num = 1;
		return num;
	}

	public int getAvgnumYear ()
	{
		int num = 365*dailySamples/getSamplesYear();
		if ( num == 0) num = 1;
		return num;
	}
}
