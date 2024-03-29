package io.github.reinershir.auth.core;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface Feedbacker {

	/**
	 * @Title: feedBack
	 * @Description:   认证失败时的处理者
	 * @author reinershir
	 * @date 2020年11月11日
	 * @param httpServletRequest
	 * @param httpServletResponse
	 */
	public void feedBack(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,int status);
}
