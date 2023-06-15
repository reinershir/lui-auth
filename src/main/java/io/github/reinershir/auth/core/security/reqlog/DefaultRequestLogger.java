package io.github.reinershir.auth.core.security.reqlog;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import io.github.reinershir.auth.entity.RequestLog;

public class DefaultRequestLogger implements RequestLogger{
	
	Logger logger = LoggerFactory.getLogger(getClass());
	private ThreadLocal<RequestLog> threadLocal = new ThreadLocal<>();
	

	@Override
	public void processRequestLog(HttpServletRequest request,RequestLog log) {
		String uri = log.getRequestUri();
		String userId = log.getUserId();
		String ip = log.getRequestIp();
		String body = log.getRequestBody();
		String requestName = log.getRequestName();
		logger.info("Request name:{} \t Request uri:{} \t Request User id:{} \t  Request ip:{} \n  Request body:{}",requestName,uri,userId,ip,
				StringUtils.isEmpty(body)?request.getQueryString():body);
		log.setDate(new Date());
		threadLocal.set(log);
		
	}

	@Override
	public void processResponseLog(HttpServletResponse response) {
		RequestLog log = threadLocal.get();
		if(log!=null) {
			logger.info("Request uri:{} \t Request name:{} \t Time consumed:{}ms ",log.getRequestUri(),log.getRequestName(),System.currentTimeMillis()-log.getDate().getTime());
		}
		
	}

}
