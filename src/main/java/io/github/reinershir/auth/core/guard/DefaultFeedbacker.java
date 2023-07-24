package io.github.reinershir.auth.core.guard;

import java.io.IOException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.reinershir.auth.contract.AuthContract;
import io.github.reinershir.auth.core.Feedbacker;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
