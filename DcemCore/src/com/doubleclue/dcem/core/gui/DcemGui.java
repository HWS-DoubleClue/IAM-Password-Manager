package com.doubleclue.dcem.core.gui;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
/**
 * @author Emanuel Galea
 */

import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.jpa.FilterOperator;
import com.doubleclue.dcem.core.jpa.VariableType;
import com.doubleclue.dcem.core.utils.DisplayModes;
@Retention(RetentionPolicy.RUNTIME)
public @interface DcemGui {
	
	public boolean password() default false;  		// Field is a password field
	public DisplayModes displayMode() default DisplayModes.ALL;	  			// Display mode 1 =
	public String [] choose () default {};	  		//This is String should be converted into a Choose-Box 	
	public String help () default "";		  		// This is the Help for the field in english.	
	public String style () default "";		  		// Additional JSF styles for this field
	public String styleClass () default "";		  	// Additional JSF styles Class for this field
	public String name () default "";		  		// the displayed name of the field.
	public String subClass () default "";		  	// the displayed name of the field.
	public String columnWidth () default "";	  	// the displayed name of the field.
	public String converterId () default "";		// the converter.
	public String dbMetaAttributeName () default "";		// the db parent Name
	public String separator() default "";
	
	public FilterOperator filterOperator() default FilterOperator.NONE;
	public String filterValue() default "";
	public String filterToValue() default "";
	public SortOrder sortOrder() default SortOrder.UNSORTED;
	public int sortRank () default 0;
	public boolean autoComplete() default false;
	public boolean visible() default true;
	public boolean required() default false;
	public boolean masterOnly() default false;
	public VariableType variableType() default VariableType.UNKNOWN;
}
