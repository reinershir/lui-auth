package io.github.reinershir.auth.entity;

import java.util.Date;

public class RequestCount {

	private Date startDate;
	
	private Integer count;

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}
	
	
}
