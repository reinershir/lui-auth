package io.github.reinershir.auth.entity;

import java.util.Date;

public class RequestLog {

	private Date date;
	
	private String requestName;
	
	private String requestIp;
	
	private String requestUri;
	
	private String userId;
	
	private String requestBody;

	public RequestLog(String requestName, String requestIp, String requestUri, String userId,String requestBody) {
		super();
		this.requestName = requestName;
		this.requestIp = requestIp;
		this.requestUri = requestUri;
		this.userId = userId;
		this.requestBody=requestBody;
		this.date = new Date();
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getRequestName() {
		return requestName;
	}

	public void setRequestName(String requestName) {
		this.requestName = requestName;
	}

	public String getRequestIp() {
		return requestIp;
	}

	public void setRequestIp(String requestIp) {
		this.requestIp = requestIp;
	}

	public String getRequestUri() {
		return requestUri;
	}

	public void setRequestUri(String requestUri) {
		this.requestUri = requestUri;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRequestBody() {
		return requestBody;
	}

	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}
	
	
}
