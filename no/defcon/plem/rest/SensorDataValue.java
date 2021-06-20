package no.defcon.plem.rest;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
//import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
//import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

//@JacksonXmlRootElement(localName = "sensorvalue")
@JsonPropertyOrder({ "timestamp", "value" })
public class SensorDataValue {

		@JsonProperty("timestamp")
		//@JacksonXmlProperty(isAttribute = false, localName = "timestamp", namespace = "")
		private Long timestamp;

		@JsonProperty("value")
		//@JacksonXmlProperty(isAttribute = false, localName = "value", namespace = "")
		private Double value;

		public SensorDataValue()
		{
				this.timestamp = new Long(0);
				this.value = new Double(0.0);
		}
		public SensorDataValue(Long timestamp, Double value)
		{
				this.setTimestamp(timestamp);
				this.setValue(value);
		}

		public Long getTimestamp()
		{
				return this.timestamp;
		}
		public void setTimestamp( Long timestamp )
		{
				this.timestamp = timestamp;
		}
		public Double getValue()
		{
				return this.value;
		}
		public void setValue(Double value)
		{
				this.value = value;
		}

		@Override
		public String toString()
		{
				return "SensorDataValue [timestamp=" + timestamp + ", value=" + value + "]";
		}
}
