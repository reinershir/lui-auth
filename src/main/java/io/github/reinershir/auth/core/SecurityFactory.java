package io.github.reinershir.auth.core;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;

import io.github.reinershir.auth.annotation.Permission;
import io.github.reinershir.auth.annotation.PermissionMapping;
import io.github.reinershir.auth.config.AuthorizationProperty;
import io.github.reinershir.auth.contract.AuthContract;
import io.github.reinershir.auth.core.guard.DefaultFeedbacker;
import io.github.reinershir.auth.core.guard.InsideSecurity;
import io.github.reinershir.auth.core.guard.UserSecurity;
import io.github.reinershir.auth.core.support.AuthorizeManager;
import io.github.reinershir.auth.utils.CheckValueUtil;

public class SecurityFactory {
	
	/**
	 * 
	 * @Title: 根据参数返回不同的认证执行者
	 * @Description:   
	 * @author reinershir
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
        }else if(property.getAuthrizationConfig().getServiceCommunication()&&!StringUtils.isEmpty(serviceToken)) { 
        	return new InsideSecurity(property.getAuthrizationConfig().getServiceSecret());
        }else if(mapping!=null&&!StringUtils.isEmpty(mapping.value())) {
        	if(CheckValueUtil.checkPermissionCode(methodPermission)||CheckValueUtil.checkPermissionCode(classPermission)) {
        		Boolean isBindIp = property.getSecurityConfig()!=null?property.getSecurityConfig().getBindIp():false;
        		return new UserSecurity(authorizeManager,property.getAuthrizationConfig().getTokenHeaderName(),isBindIp);
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
