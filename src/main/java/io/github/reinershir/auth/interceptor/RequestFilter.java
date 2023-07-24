package io.github.reinershir.auth.interceptor;

import java.io.IOException;

import io.github.reinershir.auth.core.security.reqlog.BodyCacheHttpServletRequest;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

public class RequestFilter implements Filter{
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		ServletRequest requestWrapper = null;
		String contentType = request.getContentType();
		if(contentType!=null) {
			if(contentType.startsWith("application/json")||contentType.startsWith("application/xml")||contentType.startsWith("application/x-www-form-urlencoded")) {
				if (request instanceof HttpServletRequest) {
				    requestWrapper = new BodyCacheHttpServletRequest((HttpServletRequest) request);
				}
			}
		}
		if (null == requestWrapper) {
		     chain.doFilter(request, response);
		} else {
		     chain.doFilter(requestWrapper, response);
		}
		
	}

}
