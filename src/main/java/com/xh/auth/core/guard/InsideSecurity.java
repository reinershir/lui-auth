package com.xh.auth.core.guard;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;

import com.xh.auth.annotation.ServicePermission;
import com.xh.auth.contract.AuthContract;
import com.xh.auth.core.SecurityGuard;
import com.xh.auth.entity.ServiceAuthInfo;
import com.xh.auth.utils.DESUtil;
import com.xh.auth.utils.JacksonUtil;
import com.xh.auth.utils.MD5;

public class InsideSecurity implements SecurityGuard{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	String serviceSecret;
	public InsideSecurity(String serviceSecret) {
		this.serviceSecret = serviceSecret;
	}

	@Override
	public int authorization(HandlerMethod handler, HttpServletRequest httpServletRequest) {
		ServiceAuthInfo authInfo = null;
		 ServicePermission servicePermission = handler.getMethodAnnotation(ServicePermission.class);
		 String serviceToken = httpServletRequest.getHeader(AuthContract.SERVICE_SECRET_HEADER);
    	try {
			authInfo = JacksonUtil.readValue(DESUtil.decryption(serviceToken, serviceSecret),ServiceAuthInfo.class);
		} catch (Exception e) {
			logger.error("Token parsing failed! message：{}",e.getMessage(),e);
    		return AuthContract.AUTHORIZATION_STATUS_ILLEGAL;
		}
    	boolean flag = false;
    	//使用了servicePermission注解，只有指定的服务可以调用 
    	if(servicePermission!=null) {
    		String[] serviceIds = servicePermission.value();
    		for (String serviceId : serviceIds) {
    			String sign = MD5.encode(serviceId + serviceSecret);
    			if(sign.equals(authInfo.getSecret())&&serviceId.equals(authInfo.getServiceId())) {
					flag = true;
					break;
				}
			}
    	}else {
    		//未使用注解，仅验证是否是合法的内部应用调用
    		String sign = MD5.encode(authInfo.getServiceId() + serviceSecret);
        	if(sign.equals(authInfo.getSecret())) {
				flag = true;
			}
    	}
    	if(!flag) {
    		logger.warn("Illegal token! Service ID:{},request method：{}",authInfo.getServiceId(),handler.getMethod().getName());
    		return AuthContract.AUTHORIZATION_STATUS_ILLEGAL;
    	}
		return AuthContract.AUTHORIZATION_STATUS_SUCCESS;
	}

}
