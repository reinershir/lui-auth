package com.xh.auth.config;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.aop.support.AopUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import com.xh.auth.annotation.OptionType;
import com.xh.auth.annotation.Permission;
import com.xh.auth.annotation.PermissionMapping;
import com.xh.auth.entity.Menu;

/**
 * 扫描权限注解
 * @author xh
 *
 */
public class PermissionScanner implements CommandLineRunner, ApplicationContextAware{
    
    Set<String> permissionCodes=null;
    Map<String,Menu> menMap = null;
    
    /**
     * 获取Spring框架的上下文
     */
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext arg0) {
        this.applicationContext = arg0;
    }
    
    
    public Set<String> getPermissionCodes() {
		return permissionCodes;
	}

	public Map<String, Menu> getMenMap() {
		return menMap;
	}

	@Override
	public void run(String... args) throws Exception {
		 Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(PermissionMapping.class);
		 menMap = new HashMap<>();
         permissionCodes = new HashSet<>();
         for (Map.Entry<String, Object> entry : controllers.entrySet()) {
        	 Object v = entry.getValue();
        	 Class<?> clazz = AopUtils.getTargetClass(v);
             PermissionMapping mapping = clazz.getAnnotation(PermissionMapping.class);
             if(mapping==null) {
            	 continue;
             }
             String nodeName = mapping.name();
             //父节点
             if(!StringUtils.isEmpty(nodeName)&&mapping.isParentNode()) {
             	if(!menMap.containsKey(nodeName)) {
             		Menu menu = new Menu(nodeName,mapping.parentPermissionCode());
             		menMap.put(nodeName, menu);
             		permissionCodes.add(mapping.parentPermissionCode());
             	}
             }
             Method[] methods = clazz.getMethods();
             for (Method method : methods) {
	             Permission permission = method.getAnnotation(Permission.class);
                 if (permission == null) {
                     continue;
                 }
	             OptionType[] optionTypes = permission.value();
	     		 for(OptionType optionType : optionTypes) {
	                 String permissionCode = mapping.value()+":"+(optionType==OptionType.CUSTOM?permission.customPermissionCode():optionType.toString());
	         		if(!StringUtils.isEmpty(permissionCode)) {
	         			Menu parentMenu = menMap.get(nodeName);
	         			if(parentMenu!=null) {
	         				Menu menu = new Menu(permission.name(),permissionCode);
	         				//将子节点加入数组
	         				parentMenu.getChildren().add(menu);
	         			}
	         			permissionCodes.add(permissionCode);
	         		}
	     		}
             }
         }
         //System.out.println(JacksonUtil.toJSon(permissionCodes));
	}
    
    
}