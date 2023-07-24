package io.github.reinershir.auth.core.security.reqlimit;

import java.util.Date;

import io.github.reinershir.auth.entity.RequestCount;
import jakarta.servlet.http.HttpServletRequest;

public class RequestLimitHandler {

	RequestLimitStorage storage;
	public RequestLimitHandler(RequestLimitStorage storage) {
		this.storage=storage;
	}
	
	public boolean checkLimit(HttpServletRequest request,Long time,Integer reqLimit) {
		String key = this.getIpAddress(request)+"|"+request.getRequestURI();
		RequestCount c = storage.get(key);
		Date now = new Date();
		long timespan = now.getTime()-c.getStartDate().getTime();
		//System.out.println("请求时间差："+timespan +" 请求次数："+c.getCount() + "请求uri:"+key);
		if(timespan<time) {
			if(c.getCount()>reqLimit) {
				return false;
			}
			storage.addCount(key,c);
		}else {
			storage.newCount(key);
		}
		return true;
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
