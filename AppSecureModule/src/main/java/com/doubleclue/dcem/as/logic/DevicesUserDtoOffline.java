package com.doubleclue.dcem.as.logic;

/**
 *  
 * 
 * @author Emanuel Galea
 * 
 * 
 */
public class DevicesUserDtoOffline {
	
		
	

	public DevicesUserDtoOffline(int id, byte[] udid, byte[] offlineKey) {
		super();
		this.id = id;
		this.udid = udid;
		this.offlineKey = offlineKey;
	}


	int id;

	
	private byte[] udid;

	private byte[] offlineKey;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	
	public byte[] getUdid() {
		return udid;
	}

	public void setUdid(byte[] udid) {
		this.udid = udid;
	}

	public byte[] getOfflineKey() {
		return offlineKey;
	}

	public void setOfflineKey(byte[] offlineKey) {
		this.offlineKey = offlineKey;
	}

	
}
	

