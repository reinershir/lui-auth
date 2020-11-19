package io.github.reinershir.auth.config;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.aop.support.AopUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import io.github.reinershir.auth.annotation.OptionType;
import io.github.reinershir.auth.annotation.Permission;
import io.github.reinershir.auth.annotation.PermissionMapping;
import io.github.reinershir.auth.entity.Menu;

/**
 * 扫描权限注解
 * @author xh
 *
 */
public class PermissionScanner implements CommandLineRunner, ApplicationContextAware{
    
    Set<String> permissionCodes=null;
    List<Menu> menus = null;
    
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

	public List<Menu> getMenus() {
		return menus;
	}

	@Override
	public void run(String... args) throws Exception {
		 Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(PermissionMapping.class);
		 menus = new ArrayList<>();
         permissionCodes = new HashSet<>();
         for (Map.Entry<String, Object> entry : controllers.entrySet()) {
        	 Object v = entry.getValue();
        	 Class<?> clazz = AopUtils.getTargetClass(v);
             PermissionMapping mapping = clazz.getAnnotation(PermissionMapping.class);
             if(mapping==null) {
            	 continue;
             }
             String nodeName = mapping.name();
             Menu parentMenu = null;
             //父节点
             if(!StringUtils.isEmpty(nodeName)&&mapping.isParentNode()) {
         		parentMenu = new Menu(nodeName,mapping.parentPermissionCode(),mapping.orderIndex());
         		menus.add(parentMenu);
         		permissionCodes.add(mapping.parentPermissionCode());
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
	         			if(parentMenu!=null) {
	         				Menu menu = new Menu(permission.name(),permissionCode,mapping.orderIndex());
	         				//将子节点加入数组
	         				parentMenu.addChild(menu);
	         			}
	         			permissionCodes.add(permissionCode);
	         		}
	     		}
             }
         }
         if(menus.size()>0) {
        	 Collections.sort(menus,new Comparator<Menu>() {
                 //升序排序
                 public int compare(Menu o1,
                		 Menu o2) {
                     return o1.getOrderIndex().compareTo(o2.getOrderIndex());
                 }
                  
             });
         }
         //System.out.println(JacksonUtil.toJSon(menus));
	}
    
    
}