package io.github.reinershir.auth.config.property;

import org.springframework.beans.factory.annotation.Value;

public class AuthrizationConfig {

	/**
	 * 服务内部通信密钥
	 */
	private String serviceSecret;
	
	/**
	 * token加密的盐
	 */
	private String tokenSalt;
	
	/**
	 * 是否开启服务内部通信密钥认证
	 */
	private Boolean serviceCommunication=false;
	
	/**
	 * 用户认证数据缓存数量
	 */
	private Integer cacheSize = 50000;
	
	/**
	 * token失效时间(单为秒)
	 */
	private Long tokenExpireTime=30l*60l;
	
	/**
	 * token在header中的名称
	 */
	private String tokenHeaderName="Access-Token";
	
	/**
	 * 超级管理员的用户ID，用于防止将角色全部删除后无法进入系统的情况
	 * 配置为用户表中管理员的userId
	 */
	private String administratorId;
	
	@Value("spring.application.name")
	private String applicationName;
	
	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	
	public String getServiceSecret() {
		if(serviceSecret==null) {
			return applicationName+"DefaultServiceSalt";
		}
		return serviceSecret;
	}

	public void setServiceSecret(String serviceSecret) {
		this.serviceSecret = serviceSecret;
	}

	public Boolean getServiceCommunication() {
		return serviceCommunication;
	}

	public void setServiceCommunication(Boolean serviceCommunication) {
		this.serviceCommunication = serviceCommunication;
	}

	public Integer getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(Integer cacheSize) {
		this.cacheSize = cacheSize;
	}

	public Long getTokenExpireTime() {
		return tokenExpireTime;
	}

	public void setTokenExpireTime(Long tokenExpireTime) {
		this.tokenExpireTime = tokenExpireTime;
	}

	public String getTokenSalt() {
		if(tokenSalt==null) {
			return applicationName+"DefaultSalt";
		}
		return tokenSalt;
	}

	public void setTokenSalt(String tokenSalt) {
		this.tokenSalt = tokenSalt;
	}

	public String getTokenHeaderName() {
		return tokenHeaderName;
	}

	public void setTokenHeaderName(String tokenHeaderName) {
		this.tokenHeaderName = tokenHeaderName;
	}

	public String getAdministratorId() {
		return administratorId;
	}

	public void setAdministratorId(String administratorId) {
		this.administratorId = administratorId;
	}
	
	
}
