package io.github.reinershir.auth.core.integrate.vo;

import java.io.Serializable;


public class MenuVO implements Serializable{
/**
	 * 
	 */
	private static final long serialVersionUID = -5482932993898926983L;

	/**
	 * 修改数据时需要传ID
	 */
	private Long id;
	
	private String name;
	
	private String url;
	
	private String icon;
	
	/**
	 * 访问该菜单所需的权限码，配置为@PermissionMapping + @Permission的值，如 USER:ADD
	 */
	private String permissionCodes;
	
	private String description;
	
	private String property;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getPermissionCodes() {
		return permissionCodes;
	}

	public void setPermissionCodes(String permissionCodes) {
		this.permissionCodes = permissionCodes;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}
	
	
}
