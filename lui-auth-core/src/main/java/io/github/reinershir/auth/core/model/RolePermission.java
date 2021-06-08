package io.github.reinershir.auth.core.model;

import java.io.Serializable;

public class RolePermission implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3587750059291697874L;
	private Long id;
	private Long roleId;
	private Long menuId;
	private String permissionCodes;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getRoleId() {
		return roleId;
	}
	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}
	public Long getMenuId() {
		return menuId;
	}
	public void setMenuId(Long menuId) {
		this.menuId = menuId;
	}
	public String getPermissionCodes() {
		return permissionCodes;
	}
	public void setPermissionCodes(String permissionCodes) {
		this.permissionCodes = permissionCodes;
	}


	
	
	
}
