package io.github.reinershir.auth.core.security.reqlog;

import io.github.reinershir.auth.entity.RequestLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
		logger.info("Request name:{} \t Request uri:{} \t Request User id:{} \t  Request ip:{} \n  Request body:{}",requestName,uri,userId,ip,body);
		threadLocal.set(log);
	}

	@Override
	public void processResponseLog(HttpServletResponse response) {
		try {
			RequestLog log = threadLocal.get();
			logger.info("Request uri:{} \t Request name:{} \t Time consumed:{}ms ", log.getRequestUri(), log.getRequestName(), System.currentTimeMillis() - log.getDate().getTime());
		}finally {
			threadLocal.remove();
		}
	}

}
