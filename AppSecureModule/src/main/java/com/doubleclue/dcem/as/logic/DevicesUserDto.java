package com.doubleclue.dcem.as.logic;

/**
 *  
 * 
 * @author Emanuel Galea
 * 
 * 
 */
public class DevicesUserDto {
	
		
	public DevicesUserDto() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DevicesUserDto(int id, DeviceStatus status, Integer nodeId) {
		super();
		this.id = id;
		this.status = status;
		this.nodeId = nodeId;		
	}


	int id;
	DeviceStatus status;
	Integer nodeId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public DeviceStatus getStatus() {
		return status;
	}

	public void setStatus(DeviceStatus status) {
		this.status = status;
	}

	public Integer getNodeId() {
		return nodeId;
	}

	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}

			
}
	

