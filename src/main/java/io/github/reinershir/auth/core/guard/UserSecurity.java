package io.github.reinershir.auth.core.guard;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.method.HandlerMethod;

import io.github.reinershir.auth.annotation.OptionType;
import io.github.reinershir.auth.annotation.Permission;
import io.github.reinershir.auth.annotation.PermissionMapping;
import io.github.reinershir.auth.contract.AuthContract;
import io.github.reinershir.auth.core.SecurityGuard;
import io.github.reinershir.auth.core.support.AuthorizeManager;
import io.github.reinershir.auth.utils.CheckValueUtil;

public class UserSecurity implements SecurityGuard{
	
	//private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	final String tokenHeaderName;
	AuthorizeManager authorizeManager;
	
	public UserSecurity(AuthorizeManager authorizeManager,final String tokenHeaderName) {
		this.authorizeManager=authorizeManager;
		this.tokenHeaderName=tokenHeaderName;
	}
    
	@Override
	public int authorization(HandlerMethod handler, HttpServletRequest httpServletRequest) throws AuthenticationException {
		String token = httpServletRequest.getHeader(tokenHeaderName);
		//权限码前缀
        PermissionMapping mapping = handler.getBeanType().getAnnotation(PermissionMapping.class);
		 //类上的权限验证注解 
        Permission classPermission = handler.getBeanType().getAnnotation(Permission.class);
        //方法上的权限验证注解
        Permission methodPermission = handler.getMethodAnnotation(Permission.class);
		//先使用方法级别验证，如果没配置则使用Controller级别验证
		Permission hasPermission = CheckValueUtil.checkPermissionCode(methodPermission)?methodPermission:classPermission;
		//类权限注解和方法权限注解有其一则需要验证权限
		if(hasPermission!=null) {
			OptionType[] optionTypes = hasPermission.value();
			for(OptionType optionType : optionTypes) {
				if(optionType==OptionType.SKIP) {
					continue;
				}
				//如果自定义权限码则拼接customPermissionCode,否则使用枚举
				String permissionCode = mapping.value()+":"+(optionType==OptionType.CUSTOM?hasPermission.customPermissionCode():optionType.toString());
				return authorizeManager.authentication(token, permissionCode);
				
			}
		}
		return AuthContract.AUTHORIZATION_STATUS_SUCCESS;
	}
	
}
