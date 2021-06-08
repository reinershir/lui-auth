package io.github.reinershir.auth.core.security.reqlimit.impl;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.github.reinershir.auth.core.Feedbacker;

public class RequestLimitFeedbacker implements Feedbacker{

	@Override
	public void feedBack(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,int status) {
		httpServletResponse.setStatus(403);
		httpServletResponse.setContentType("application/json");
		httpServletResponse.setCharacterEncoding("UTF-8");
		Writer writer = null;
		try {
			writer = httpServletResponse.getWriter();
			String responseMsg = "{\"message\":\"Requests are too frequent, please try again later\"}";
			writer.write(responseMsg);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if(writer!=null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
		}
		
	}

}
