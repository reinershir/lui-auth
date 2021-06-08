package io.github.reinershir.auth.entity;

public class ServiceAuthInfo {

	/**
	 * 服务ID
	 */
	private String serviceId;
	/**
	 * 密钥+参数签名
	 */
	private String secret;
	
	public ServiceAuthInfo() {}
	
	public ServiceAuthInfo(String serviceId, String secret) {
		super();
		this.serviceId = serviceId;
		this.secret = secret;
	}
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	public String getSecret() {
		return secret;
	}
	public void setSecret(String secret) {
		this.secret = secret;
	}
	
	
}
