package no.defcon.plem.rest;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
// import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
// import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

//@JacksonXmlRootElement(localName = "sensorlist")
@JsonPropertyOrder({ "sensors" })
public class SensorListMsh {
		@JsonProperty("sensors")
		//@JacksonXmlProperty(localName = "sensors", namespace = "")
		private List<String> sensors;

		public SensorListMsh()
		{
				this.sensors = new ArrayList<String>();
		}

		public void setSensors(List<String> data)
		{
				this.sensors.clear();
				if ( data != null )
				{
						this.sensors.addAll(data);
				}
		}
		public List<String> getSensors()
		{
				List<String> copySet = new ArrayList<String>();
				copySet.addAll(this.sensors);
				return copySet;
		}
		public void add(String value)
		{
				sensors.add(value);
		}
		public int length()
		{
				return sensors.size();
		}
}

