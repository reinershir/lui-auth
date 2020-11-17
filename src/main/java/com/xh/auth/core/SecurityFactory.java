package com.xh.auth.core;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;

import com.xh.auth.annotation.Permission;
import com.xh.auth.annotation.PermissionMapping;
import com.xh.auth.config.AuthorizationProperty;
import com.xh.auth.config.CustomManager;
import com.xh.auth.contract.AuthContract;
import com.xh.auth.core.guard.DefaultFeedbacker;
import com.xh.auth.core.guard.InsideSecurity;
import com.xh.auth.core.guard.UserSecurity;
import com.xh.auth.core.support.AuthorizeManager;
import com.xh.auth.utils.CheckValueUtil;

public class SecurityFactory {
	
	/**
	 * 
	 * @Title: 根据参数返回不同的认证执行者
	 * @Description:   
	 * @author xh
	 * @date 2020年11月11日
	 * @param handler
	 * @param httpServletRequest
	 * @return
	 */
	public static SecurityGuard train(HandlerMethod handler,HttpServletRequest httpServletRequest,
				AuthorizationProperty property,CustomManager manager,AuthorizeManager authorizeManager) {
		 //权限码前缀
        PermissionMapping mapping = handler.getBeanType().getAnnotation(PermissionMapping.class);
        //类上的权限验证注解 
        Permission classPermission = handler.getBeanType().getAnnotation(Permission.class);
        //方法上的权限验证注解
        Permission methodPermission = handler.getMethodAnnotation(Permission.class);
        
        String serviceToken = httpServletRequest.getHeader(AuthContract.SERVICE_SECRET_HEADER);
        if(manager!=null&&manager.getSecurityGuard()!=null) {
        	return manager.getSecurityGuard();
        }else if(property.getServiceCommunication()&&!StringUtils.isEmpty(serviceToken)) { 
        	return new InsideSecurity(property.getServiceSecret());
        }else if(mapping!=null&&!StringUtils.isEmpty(mapping.value())) {
        	if(CheckValueUtil.checkPermissionCode(methodPermission)||CheckValueUtil.checkPermissionCode(classPermission)) {
        		return new UserSecurity(authorizeManager,property.getTokenHeaderName());
        	}
        }
		return null;
	}
	
	public static Feedbacker selectFeedbacker(CustomManager manager) {
		if(manager!=null&&manager.getFeedbacker()!=null) {
			return manager.getFeedbacker();
		}
		return new DefaultFeedbacker();
	}
}
