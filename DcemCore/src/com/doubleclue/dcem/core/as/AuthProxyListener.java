package com.doubleclue.dcem.core.as;

import com.doubleclue.dcem.core.entities.DomainEntity;

public interface AuthProxyListener {
	
	public void onClose ();
	
	public void onReceive (byte [] data);
	
	public DomainEntity getDomainEntity();


}
