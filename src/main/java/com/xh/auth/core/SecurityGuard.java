package com.xh.auth.core;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.method.HandlerMethod;

/**
 * 身份验证接口
 * @author xh
 *
 */
public interface SecurityGuard {

	/**
	 * @Title: authorization
	 * @Description: 用于拦截器验证用户的请求权限
	 * @author xh
	 * @date 2020年11月16日
	 * @param handler
	 * @param httpServletRequest
	 * @return int 返回0=正常通过，1=未传token，2=token不正确或无效，3=无权限，4=未知错误
	 * @throws AuthenticationException
	 */
	public int authorization(HandlerMethod handler,HttpServletRequest httpServletRequest) throws AuthenticationException;
}
