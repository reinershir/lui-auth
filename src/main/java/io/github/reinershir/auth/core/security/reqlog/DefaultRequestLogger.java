package io.github.reinershir.auth.core.security.reqlog;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import io.github.reinershir.auth.entity.RequestLog;
import io.github.reinershir.auth.utils.DESUtil;

public class DefaultRequestLogger implements RequestLogger{
	
	Logger logger = LoggerFactory.getLogger(getClass());
	String tokenHeaderName;
	String tokenSalt;
	ThreadLocal<RequestLog> threadLocal = new ThreadLocal<>();
	
	public DefaultRequestLogger(String tokenHeaderName,String tokenSalt) {
		this.tokenHeaderName=tokenHeaderName;
		this.tokenSalt=tokenSalt;
	}

	@Override
	public void processRequestLog(HttpServletRequest request,String requestName) {
		String ip = getIpAddress(request);
		String token = request.getHeader(tokenHeaderName);
		String userId = null;
		String body = null;
		String uri = request.getRequestURI();
		if(!StringUtils.isEmpty(token)) {
			try {
				String userIdStr = token.split("_")[1];
				userId = DESUtil.encryption(userIdStr,tokenSalt);
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
		logger.info("Request uri:{} \t Request User id:{} \n Request name:{} \t Request ip:{} \n  Request body:{}",uri,userId,requestName,ip,body);
		threadLocal.set(new RequestLog(requestName,ip,uri,userId));
	}

	@Override
	public void processResponseLog(HttpServletResponse response) {
		RequestLog log = threadLocal.get();
		logger.info("Request uri:{} \t Request name:{} \t Time consumed:{}ms ",log.getRequestUri(),log.getRequestName(),System.currentTimeMillis()-log.getDate().getTime());
		
	}

	
	private String getIpAddress(HttpServletRequest request) {  
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
