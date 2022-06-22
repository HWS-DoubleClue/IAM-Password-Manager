package com.doubleclue.portaldemo.radius;

public enum RadiusAttributeEnum {
	
	UserName (1), UserPassword (2), ReplyMessage (18), State (24), ProxyState (33);
	
		
	
	int type;

	private RadiusAttributeEnum(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	
	
	

}
