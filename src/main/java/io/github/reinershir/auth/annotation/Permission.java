package io.github.reinershir.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface Permission {
	/**
	 * @date:   2019年5月16日 下午3:50:45   
	 * @Description: 权限标识代码,声明在类上表示该Controller使用同一权限码验证
	 * @see 不配置此项该方法权限验证将无效！
	 * @param: @return      
	 * @return: String      
	 * @throws
	 */
	OptionType[] value() default OptionType.CUSTOM;
	
	/**
	 * @date:   2019年5月17日 上午10:55:59   
	 * @Description: 如果使用自定义操作权限码，请在此配置
	 * @param: @return      
	 * @return: String      
	 * @throws
	 */
	String customPermissionCode() default "";
	
	/**
	 * @Title: name
	 * @Description:   权限名字（用于生成菜单数据用,可重复）
	 * @author xh
	 * @date 2020年11月12日
	 * @return
	 */
	String name() default "";
}
