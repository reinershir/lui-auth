package io.github.reinershir.auth.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import io.github.reinershir.auth.config.AuthorizationProperty;
import io.github.reinershir.auth.config.CustomManager;
import io.github.reinershir.auth.contract.AuthContract;
import io.github.reinershir.auth.core.SecurityFactory;
import io.github.reinershir.auth.core.SecurityGuard;
import io.github.reinershir.auth.core.support.AuthorizeManager;

/**
 * 用于验证权限的拦截器
 * @date:   2019年5月16日 下午3:45:49   
 * @Description:
 */
public class AuthenticationInterceptor implements HandlerInterceptor{
	StringRedisTemplate redisTemplate;
	AuthorizationProperty property;
	AuthorizeManager authorizeManager;
	CustomManager manager;
	public AuthenticationInterceptor(StringRedisTemplate redisTemplate,AuthorizationProperty property,CustomManager manager,AuthorizeManager authorizeManager) {
		this.redisTemplate=redisTemplate;
		this.property=property;
		this.manager=manager;
		this.authorizeManager=authorizeManager;
	}
	
	@SuppressWarnings("unused")
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
        if(handler instanceof HandlerMethod) {
            HandlerMethod h = (HandlerMethod)handler;
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
	
}
