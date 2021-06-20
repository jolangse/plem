package no.defcon.plem.rest;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.GenericEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
//import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
//import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

//@JacksonXmlRootElement(localName = "sensordata")
@JsonPropertyOrder({ "id", "type", "aggregate", "dataset" })
public class SensorDataList {

		@JsonProperty("id")
		//@JacksonXmlProperty(isAttribute = false, localName = "id", namespace = "")
		private String id;
		@JsonProperty("type")
		//@JacksonXmlProperty(isAttribute = false, localName = "type", namespace = "")
		private int type;
		@JsonProperty("aggregate")
		//@JacksonXmlProperty(isAttribute = false, localName = "aggregate", namespace = "")
		private String aggregate;
		@JsonProperty("dataset")
		//@JacksonXmlProperty(localName = "dataset", namespace = "")
		private List<SensorDataValue> dataset;

		public SensorDataList ()
		{
				this.dataset = new ArrayList<SensorDataValue>();
		}
		public SensorDataList (String id, int type, String aggregate)
		{
				this.id = id;
				this.type = type;
				this.aggregate = aggregate;
				this.dataset = new ArrayList<SensorDataValue>();
		}

		public void setId(String id)
		{ this.id = id; }
		public String getId()
		{ return this.id; }

		public void setType(int type)
		{ this.type = type; }
		public int getType()
		{ return this.type; }

		public void setAggregate(String aggregate)
		{ this.aggregate = aggregate; }
		public String getAggregate()
		{ return this.aggregate; }

		public void setDataset(List<SensorDataValue> data)
		{
				this.dataset.clear();
				if ( data != null )
				{
						this.dataset.addAll(data);
				}
		}
		public List<SensorDataValue> getDataset()
		{
				List<SensorDataValue> copySet = new ArrayList<SensorDataValue>();
				copySet.addAll(this.dataset);
				return copySet;
		}

		public void add(SensorDataValue value)
		{
				dataset.add(value);
		}
		public int length()
		{
				return dataset.size();
		}
}
