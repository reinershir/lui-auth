package io.github.reinershir.auth.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface PermissionMapping {

	/**
	 * @date:   2019年5月17日 上午10:49:14   
	 * @Description: 权限码的前缀
	 * @param: @return      
	 * @return: String      
	 * @throws
	 */
	String value();
	
}
