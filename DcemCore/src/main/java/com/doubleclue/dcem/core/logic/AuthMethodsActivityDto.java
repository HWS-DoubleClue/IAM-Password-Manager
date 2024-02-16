package com.doubleclue.dcem.core.logic;

import com.doubleclue.dcem.admin.logic.ReportAction;

public class AuthMethodsActivityDto {
	
	private ReportAction id;
	private Long count;

	public AuthMethodsActivityDto() {
		super();
	}

	public AuthMethodsActivityDto(ReportAction id, Long count) {
		super();
		this.id = id;
		this.count = count;	
	}

	public ReportAction getId() {
		return id;
	}

	public void setId(ReportAction id) {
		this.id = id;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}
}
