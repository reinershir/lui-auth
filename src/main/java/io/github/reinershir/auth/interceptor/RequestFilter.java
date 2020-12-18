package io.github.reinershir.auth.interceptor;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import io.github.reinershir.auth.core.security.reqlog.BodyCacheHttpServletRequest;

public class RequestFilter implements Filter{

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		ServletRequest requestWrapper = null;
		if (request instanceof HttpServletRequest) {
		    requestWrapper = new BodyCacheHttpServletRequest((HttpServletRequest) request);
		}
		if (null == requestWrapper) {
		     chain.doFilter(request, response);
		} else {
		     chain.doFilter(requestWrapper, response);
		}
		
	}

}
