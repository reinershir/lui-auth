package io.github.reinershir.auth.interceptor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import io.github.reinershir.auth.annotation.Permission;
import io.github.reinershir.auth.annotation.RequestLimit;
import io.github.reinershir.auth.config.AuthorizationProperty;
import io.github.reinershir.auth.contract.AuthContract;
import io.github.reinershir.auth.core.CustomManager;
import io.github.reinershir.auth.core.SecurityFactory;
import io.github.reinershir.auth.core.SecurityGuard;
import io.github.reinershir.auth.core.security.reqlimit.RequestLimitHandler;
import io.github.reinershir.auth.core.security.reqlimit.RequestLimitStorage;
import io.github.reinershir.auth.core.security.reqlimit.impl.MemoryStorage;
import io.github.reinershir.auth.core.security.reqlimit.impl.RedisStorage;
import io.github.reinershir.auth.core.security.reqlimit.impl.RequestLimitFeedbacker;
import io.github.reinershir.auth.core.security.reqlog.RequestLogger;
import io.github.reinershir.auth.core.support.AuthorizeManager;
import io.github.reinershir.auth.entity.RequestLog;
import io.github.reinershir.auth.utils.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用于验证权限的拦截器
 * @date:   2019年5月16日 下午3:45:49   
 * @Description:
 */
public class AuthenticationInterceptor implements HandlerInterceptor{
	RedisTemplate<String,String> redisTemplate;
	AuthorizationProperty property;
	AuthorizeManager authorizeManager;
	CustomManager manager;
	RequestLimitHandler limitHandler;
	RequestLimitFeedbacker requestLimitFeedbacker;
	Long requestTime;
	Integer requestLimit;
	RequestLogger requestLogger;
	private Boolean enableRequestLog = false;
	private String tokenHeaderName;
	private String tokenSalt;
	
	public AuthenticationInterceptor(RedisTemplate<String,String> redisTemplate,AuthorizationProperty property,
			CustomManager manager,AuthorizeManager authorizeManager,RequestLogger requestLogger) {
		this.redisTemplate=redisTemplate;
		this.property=property;
		this.manager=manager;
		this.authorizeManager=authorizeManager;
		//初始化IP限流验证器
		if(property.getSecurityConfig()!=null&&property.getSecurityConfig().getEnableRequestLimit()) {
			this.requestTime = property.getSecurityConfig().getRequestTime();
			this.requestLimit = property.getSecurityConfig().getRequestLimit();
			RequestLimitStorage storage = property.getSecurityConfig().getRequestLimitStorage().equalsIgnoreCase("memory")?
					new MemoryStorage(requestTime):new RedisStorage(redisTemplate,requestTime);
			limitHandler = new RequestLimitHandler(storage);
			requestLimitFeedbacker = new RequestLimitFeedbacker();
		}
		this.requestLogger=requestLogger;
		if(property.getSecurityConfig()!=null) {
			this.enableRequestLog = property.getSecurityConfig().getEnableRequestLog();
			this.tokenHeaderName = property.getAuthrizationConfig().getTokenHeaderName();
			this.tokenSalt = property.getAuthrizationConfig().getTokenSalt();
		}
		
	}
	
	@Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
        if(handler instanceof HandlerMethod) {
            HandlerMethod h = (HandlerMethod)handler;
            if(requestLogger!=null&&enableRequestLog) {
            	Permission permission = h.getMethodAnnotation(Permission.class);
            	RequestLog requestLog = SecurityUtil.getRequesttLog(httpServletRequest, permission!=null?permission.name():null,
            			tokenHeaderName, tokenSalt);
            	requestLogger.processRequestLog(httpServletRequest,requestLog);
            }
            if(limitHandler!=null) {
            	 RequestLimit classAnno = h.getBeanType().getAnnotation(RequestLimit.class);
            	 RequestLimit methodAnno = h.getMethodAnnotation(RequestLimit.class);
            	 RequestLimit limitAnno = methodAnno!=null?methodAnno:classAnno;
            	 Long reqTime=null;
            	 Integer reqLimit=null;
            	 if(limitAnno!=null) {
            		 reqTime = limitAnno.requestTime();
            		 reqLimit = limitAnno.requestLimit();
            	 }else {
            		 reqTime = this.requestTime;
            		 reqLimit = this.requestLimit;
            	 }
            	if(!limitHandler.checkLimit(httpServletRequest,reqTime, reqLimit)) {
            		requestLimitFeedbacker.feedBack(httpServletRequest, httpServletResponse, AuthContract.AUTHORIZATION_STATUS_TOO_FREQUENT);
            		return false;
            	}
            }
	        SecurityGuard securityGuard = SecurityFactory.train(h, httpServletRequest,property,manager,authorizeManager);
	        if(securityGuard!=null) {
	        	int result = securityGuard.authorization(h, httpServletRequest);
	        	if(result==AuthContract.AUTHORIZATION_STATUS_SUCCESS) {
	        		return true;
	        	}else {
	        		SecurityFactory.selectFeedbacker(manager).feedBack(httpServletRequest, httpServletResponse,result);
	        		return false;
	        	}
	        }
        	//throw new RuntimeException("Incorrect configuration, no matching processor.");
        
        }
        return true;
    }
	
	

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		if(requestLogger!=null) {
        	requestLogger.processResponseLog(response);
        }
		HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
	}

}
