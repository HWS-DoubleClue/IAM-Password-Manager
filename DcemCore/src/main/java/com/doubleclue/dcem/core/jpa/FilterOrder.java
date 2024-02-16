package com.doubleclue.dcem.core.jpa;

import java.util.ArrayList;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;

public class FilterOrder{
	
	ArrayList<Attribute<?, ?>> attributes;
	String name;
	boolean desc;
	int rank;
	
	
	public FilterOrder(ArrayList<Attribute<?, ?>> attributes, String name, boolean desc, int rank) {
		super();
	
		this.attributes = attributes;
		this.name = name;
		this.desc = desc;
		this.rank = rank;
	}
	
	
	public boolean isDesc() {
		return desc;
	}
	public void setDesc(boolean desc) {
		this.desc = desc;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}


	public ArrayList<Attribute<?, ?>> getAttributes() {
		return attributes;
	}


	public void setAttributes(ArrayList<Attribute<?, ?>> attributes) {
		this.attributes = attributes;
	}


	public int getRank() {
		return rank;
	}


	public void setRank(int rank) {
		this.rank = rank;
	}
	
	

}
