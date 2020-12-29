package io.github.reinershir.auth.utils;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import io.github.reinershir.auth.core.security.reqlog.BodyCacheHttpServletRequest;
import io.github.reinershir.auth.entity.RequestLog;
import io.github.reinershir.auth.entity.ServiceAuthInfo;

public class SecurityUtil {
	
	private static Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

	public String encode(String applicationName,String serviceSecurity) throws Exception {
		String authJson = JacksonUtil.toJSon(new ServiceAuthInfo(applicationName,MD5.encode(applicationName+serviceSecurity)));
		return DESUtil.encryption(authJson, serviceSecurity);
	}
	
	
	public static RequestLog geRequesttLog(HttpServletRequest request,String requestName,String tokenHeaderName,String tokenSalt) {
		String ip = getIpAddress(request);
		String token = request.getHeader(tokenHeaderName);
		String userId = null;
		String body = null;
		String uri = request.getRequestURI();
		if(!StringUtils.isEmpty(token)) {
			try {
				String userIdStr = token.split("_")[1];
				userId = DESUtil.decryption(userIdStr,tokenSalt);
			} catch (Exception e) {
				logger.error("parse token error ",e);
			}
		}
		try {
			request = new BodyCacheHttpServletRequest(request);
			body = ((BodyCacheHttpServletRequest) request).getBody();
		} catch (IOException e) {
			logger.error("read body exception",e);
		}
		return new RequestLog(requestName,ip,uri,userId,body);
	}

	
	public static String getIpAddress(HttpServletRequest request) {  
        String ip = request.getHeader("X-forwarded-for");  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("Proxy-Client-IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("WL-Proxy-Client-IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("HTTP_CLIENT_IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getRemoteAddr();  
        }  
        return ip;  
    }  
}
