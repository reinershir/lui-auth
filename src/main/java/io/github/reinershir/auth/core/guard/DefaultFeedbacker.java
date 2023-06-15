package io.github.reinershir.auth.core.guard;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.reinershir.auth.contract.AuthContract;
import io.github.reinershir.auth.core.Feedbacker;
import io.github.reinershir.auth.utils.SecurityUtil;

public class DefaultFeedbacker implements Feedbacker{
	
	Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void feedBack(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,int status) {
		httpServletResponse.setStatus(401);
		httpServletResponse.setContentType("application/json");
		httpServletResponse.setCharacterEncoding("UTF-8");
		String responseMsg = "{\"message\":\"authentication faild\"}";
		Writer writer = null;
		try {
			writer = httpServletResponse.getWriter();
			switch(status) {
			case AuthContract.AUTHORIZATION_STATUS_ILLEGAL:
				logger.warn("authentication faild! request uri:{}",httpServletRequest.getRequestURI());
				responseMsg = "{\"message\":\"authentication faild\"}";
				break;
			case AuthContract.AUTHORIZATION_STATUS_NO_PERMISSION:
				responseMsg = "{\"message\":\"no permission!\"}";
				break;
			case AuthContract.AUTHORIZATION_STATUS_IP_MISMATCH:
				logger.warn("The requested IP does not match the binding IP! request uri:{} ,request ip:{}",httpServletRequest.getRequestURI(),SecurityUtil.getIpAddress(httpServletRequest));
				responseMsg = "{\"message\":\"Illegal request!\"}";
				break;
			}
			writer.write(responseMsg);
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
