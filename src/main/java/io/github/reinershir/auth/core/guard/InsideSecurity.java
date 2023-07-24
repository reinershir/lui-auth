package io.github.reinershir.auth.core.guard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;

import io.github.reinershir.auth.annotation.ServicePermission;
import io.github.reinershir.auth.contract.AuthContract;
import io.github.reinershir.auth.core.SecurityGuard;
import io.github.reinershir.auth.entity.ServiceAuthInfo;
import io.github.reinershir.auth.utils.DESUtil;
import io.github.reinershir.auth.utils.JacksonUtil;
import io.github.reinershir.auth.utils.MD5;
import jakarta.servlet.http.HttpServletRequest;

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
