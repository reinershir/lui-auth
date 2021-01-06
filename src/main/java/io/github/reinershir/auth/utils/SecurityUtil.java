package io.github.reinershir.auth.utils;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import io.github.reinershir.auth.core.security.reqlog.BodyCacheHttpServletRequest;
import io.github.reinershir.auth.entity.RequestLog;

public class SecurityUtil {
	
	private static Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

//	public String encode(String applicationName,String serviceSecurity) throws Exception {
//		String authJson = JacksonUtil.toJSon(new ServiceAuthInfo(applicationName,MD5.encode(applicationName+serviceSecurity)));
//		return DESUtil.encryption(authJson, serviceSecurity);
//	}
	 
	
	/**
	 * @Title: geRequesttLog
	 * @Description:   读取requestBody里的内容并缓存流
	 * @author reinershir
	 * @date 2021年1月5日
	 * @param request
	 * @param requestName 权限注解上配置的功能名称
	 * @param tokenHeaderName token header名称
	 * @param tokenSalt token盐
	 * @return
	 */
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
		if(request instanceof BodyCacheHttpServletRequest) {
			body = ((BodyCacheHttpServletRequest) request).getBody();
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
