package no.defcon.plem;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.HierarchicalINIConfiguration;

public class ConfigManager
{
	private static PropertiesConfiguration plemConfig;
	private static HierarchicalINIConfiguration sensorConfig;

	private static Logger log = LoggerFactory.getLogger(ConfigManager.class);

	public ConfigManager() throws org.apache.commons.configuration.ConfigurationException
	{
		log.info("Loading main configuration file");
		String plemCfgFile = System.getProperty("plem.config", "plem.cfg");
		plemConfig = new PropertiesConfiguration(plemCfgFile);

		File sensorFile = new File( plemConfig.getString("sensorconfig", "sensors.ini") );
		log.info("Loading sensors configuration file from " + sensorFile.getAbsolutePath() );
		sensorConfig = new HierarchicalINIConfiguration( sensorFile.getAbsolutePath() );
	}

	public PropertiesConfiguration getPlemConfig()
	{
		return plemConfig;
	}

	public HierarchicalINIConfiguration getSensorConfig()
	{
		return sensorConfig;
	}
}
