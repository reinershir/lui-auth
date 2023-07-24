package io.github.reinershir.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @date:   2019年8月19日 下午5:49:04   
 * @author reinershir 
 * @Description:内部服务权限控制，加上此注解表示只有指定的内部服务才能调用
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface ServicePermission {

	/**
	 * @date:   2019年8月19日 下午5:49:32   
	 * @author reinershir 
	 * @Description: 指定哪些内部服务可以调用，value填serviceId,可填多个
	 * @param: @return      
	 * @return: String[]      
	 * @throws
	 */
	String[] value() default "ALL";
}
