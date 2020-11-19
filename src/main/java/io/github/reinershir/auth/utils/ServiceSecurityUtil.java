package io.github.reinershir.auth.utils;

import io.github.reinershir.auth.entity.ServiceAuthInfo;

public class ServiceSecurityUtil {

	public String encode(String applicationName,String serviceSecurity) throws Exception {
		String authJson = JacksonUtil.toJSon(new ServiceAuthInfo(applicationName,MD5.encode(applicationName+serviceSecurity)));
		//TODO SERVICE AUTH HEADER配置
		return DESUtil.encryption(authJson, serviceSecurity);
	}
	
}
