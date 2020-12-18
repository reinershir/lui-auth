package io.github.reinershir.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface RequestLimit {

	/**
	 * @Title: requestTime
	 * @Description:  限流时间（毫秒），即每requestTime内只能访问requestLimit次
	 * @date 2020年12月16日
	 * @return
	 */
	long requestTime() default 1000;
	
	/**
	 * @Title: requestLimit
	 * @Description:  限制次数 
	 * @date 2020年12月16日
	 * @return
	 */
	int requestLimit() default 3;
}
