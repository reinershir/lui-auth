package io.github.reinershir.auth.core.guard;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.github.reinershir.auth.contract.AuthContract;
import io.github.reinershir.auth.core.Feedbacker;

public class DefaultFeedbacker implements Feedbacker{

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
