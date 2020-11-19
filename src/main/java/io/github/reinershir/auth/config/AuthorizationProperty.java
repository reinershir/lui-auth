package io.github.reinershir.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lui-auth")
public class AuthorizationProperty {
	
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
	private Integer cacheSize = 500;
	
	/**
	 * token失效时间(单为秒)
	 */
	private Long tokenExpireTime=30l*60l;
	
	/**
	 * token在header中的名称
	 */
	private String tokenHeaderName="Access-Token";
	
	@Value("spring.application.name")
	private String applicationName;
	
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

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
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


	
}
