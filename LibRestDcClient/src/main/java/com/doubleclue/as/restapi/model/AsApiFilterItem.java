/*
 * DoubleClue REST API
 * DoubleClue URL http://yourhost:8001/dcem/restApi/as
 *
 * OpenAPI spec version: 1.5.0
 * Contact: emanuel.galea@hws-gruppe.de
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package com.doubleclue.as.restapi.model;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * AsApiFilterItem
 */

public class AsApiFilterItem implements Serializable {
  private static final long serialVersionUID = 1L;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("value")
  private String value = null;
  
  

  public AsApiFilterItem(String name, String value, SortOrderEnum sortOrder, OperatorEnum operator) {
	super();
	this.name = name;
	this.value = value;
	this.sortOrder = sortOrder;
	this.operator = operator;
}

/**
   * Gets or Sets sortOrder
   */
  public enum SortOrderEnum {
    NONE("NONE"),
    
    ASCENDING("ASCENDING"),
    
    DESCENDING("DESCENDING");

    private String value;

    SortOrderEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static SortOrderEnum fromValue(String text) {
      for (SortOrderEnum b : SortOrderEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("sortOrder")
  private SortOrderEnum sortOrder = null;

  /**
   * Gets or Sets operator
   */
  public enum OperatorEnum {
    EQUALS("EQUALS"),
    
    LIKE("LIKE"),
    
    GREATER("GREATER"),
    
    LESSER("LESSER"),
    
    NOT_EQUALS("NOT_EQUALS"),
    
    NONE("NONE");

    private String value;

    OperatorEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static OperatorEnum fromValue(String text) {
      for (OperatorEnum b : OperatorEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("operator")
  private OperatorEnum operator = null;

  public AsApiFilterItem name(String name) {
    this.name = name;
    return this;
  }

   /**
   * This muss be the exact name of the column variable
   * @return name
  **/
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public AsApiFilterItem value(String value) {
    this.value = value;
    return this;
  }

   /**
   * Ths value have to be deserialzed
   * @return value
  **/
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public AsApiFilterItem sortOrder(SortOrderEnum sortOrder) {
    this.sortOrder = sortOrder;
    return this;
  }

   /**
   * Get sortOrder
   * @return sortOrder
  **/
  public SortOrderEnum getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(SortOrderEnum sortOrder) {
    this.sortOrder = sortOrder;
  }

  public AsApiFilterItem operator(OperatorEnum operator) {
    this.operator = operator;
    return this;
  }

   /**
   * Get operator
   * @return operator
  **/
  public OperatorEnum getOperator() {
    return operator;
  }

  public void setOperator(OperatorEnum operator) {
    this.operator = operator;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AsApiFilterItem asApiFilterItem = (AsApiFilterItem) o;
    return Objects.equals(this.name, asApiFilterItem.name) &&
        Objects.equals(this.value, asApiFilterItem.value) &&
        Objects.equals(this.sortOrder, asApiFilterItem.sortOrder) &&
        Objects.equals(this.operator, asApiFilterItem.operator);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value, sortOrder, operator);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AsApiFilterItem {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("    sortOrder: ").append(toIndentedString(sortOrder)).append("\n");
    sb.append("    operator: ").append(toIndentedString(operator)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}
