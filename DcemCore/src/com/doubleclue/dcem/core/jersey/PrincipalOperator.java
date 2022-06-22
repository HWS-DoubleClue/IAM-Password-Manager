package com.doubleclue.dcem.core.jersey;

import java.security.Principal;

import com.doubleclue.dcem.core.entities.DcemUser;
 
/**
 *
 * 
*/
public class PrincipalOperator implements Principal {
   
	DcemUser dcemUser;
 
    @Override
    public String getName() {
        return dcemUser.getLoginId();
    }

	public DcemUser getDcemUser() {
		return dcemUser;
	}

	public void setDcemUser(DcemUser dcemUser) {
		this.dcemUser = dcemUser;
	}

	
}