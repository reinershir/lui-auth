package com.xh.auth.annotation;

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
	
	/**
	 * @Title: isParentNode
	 * @Description:   是否是父节点（用于生成树结构菜单时标记用）
	 * @author xh
	 * @date 2020年11月12日
	 * @return
	 */
	boolean isParentNode() default false;
	
	/**
	 * @Title: parentPermissionCode
	 * @Description:   父节点的权限码
	 * @author xh
	 * @date 2020年11月12日
	 * @return
	 */
	String parentPermissionCode() default "";
	
	/**
	 * @Title: name
	 * @Description:   菜单名称(此名称需要保持唯一)
	 * @author xh
	 * @date 2020年11月12日
	 * @return
	 */
	String name() default "";
}
