package com.doubleclue.dcem.core.logic;

import java.util.List;

import com.doubleclue.dcem.core.entities.DcemUser;

public class DomainUsers {
	
	int pageSize;
	boolean nextPageAvaliable;
	List<DcemUser> users;
	
	
	public DomainUsers(int pageSize, boolean nextPageAvaliable, List<DcemUser> users) {
		super();
		this.pageSize = pageSize;
		this.nextPageAvaliable = nextPageAvaliable;
		this.users = users;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public boolean isNextPageAvaliable() {
		return nextPageAvaliable;
	}
	public void setNextPageAvaliable(boolean nextPageAvaliable) {
		this.nextPageAvaliable = nextPageAvaliable;
	}
	public List<DcemUser> getUsers() {
		return users;
	}
	public void setUsers(List<DcemUser> users) {
		this.users = users;
	}
	
	public boolean isEmpty () {
		return users.isEmpty();
	}

}
