package com.doubleclue.dcem.ps.logic;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AppHubAction implements Serializable {
	     
		private static final long serialVersionUID = 1L;
		
		@JsonProperty("t")		
		public String type;
		
		@JsonProperty("s")
		public String selector;
		
		@JsonProperty("v")
		public Object outputValue;		
		
		@JsonProperty("dn")
		public String valueParameter;
		
		@JsonProperty("i")
		private int index;

		@JsonProperty("vs")
		private String valueSourceType;
		
		
		public AppHubAction(String type, String selector, String valueSourceType, String valueParameter) {
			super();
			this.type = type;
			this.selector = selector;
			this.valueParameter = valueParameter;
			this.valueSourceType = valueSourceType;
		}
		
		public AppHubAction() {
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getId() {
			return selector;
		}

		public void setId(String id) {
			this.selector = id;
		}

		public Object getOutputValue () {
			return outputValue;
		}

		public void setOutputValue(Object value) {
			this.outputValue = value;
		}

		public String getSelector() {
			return selector;
		}

		public void setSelector(String selector) {
			this.selector = selector;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public String getValueParameter() {
			return valueParameter;
		}

		public void setValueParameter(String valueParameter) {
			this.valueParameter = valueParameter;
		}

		public String getValueSourceType() {
			return valueSourceType;
		}

		public void setValueSourceType(String valueSourceType) {
			this.valueSourceType = valueSourceType;
		}

		@Override
		public String toString() {
			return "AppHubAction [type=" + type + ", selector=" + selector + ", valueParameter=" + valueParameter + ", index=" + index + ", valueSourceType="
					+ valueSourceType + "]";
		}

		@Override
		public int hashCode() {
			return Objects.hash(index, selector, type, valueParameter, valueSourceType);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AppHubAction other = (AppHubAction) obj;
			return index == other.index && Objects.equals(selector, other.selector) && Objects.equals(type, other.type)
					&& Objects.equals(valueParameter, other.valueParameter) && Objects.equals(valueSourceType, other.valueSourceType);
		}
		


		


	    
	}