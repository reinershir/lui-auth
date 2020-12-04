package io.github.reinershir.auth.annotation;

/**
 * 权限操作类型枚举
 * @date:   2019年5月17日 上午10:53:53   
 * @Description: DETAIL:表示查询详情,LIST：查询列表，UPDATE：全量更新该条数据,UPDATE_SELECTIVE:非全量更新数据，
 * DISABLE:禁用数据，ENABLE:启动数据，CUSTOM:自定义权限码 ,SKIP:跳过验证,LOGIN:仅验证是否登陆
 * ALL:标识该控制器下所有方法都用此码验证，controller级别控制请把hasPermission注解加控制器类上。
 */
public enum OptionType {

	DETAIL,LIST,ADD,UPDATE,UPDATE_SELECTIVE,DELETE,DISABLE,ENABLE,CUSTOM,ALL,SKIP,LOGIN
}
