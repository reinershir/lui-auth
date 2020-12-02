package io.github.reinershir.auth.config.property;

public class IntegrateConfig {

	/**
	 * 自动生成集成的角色、菜单增删改功能
	 */
	private Boolean enable=false;
	
	
	/**
	 * 角色表表名
	 */
	private String roleTableName="ROLE";
	/*
	 * 菜单表表名
	 */
	private String menuTableName="MENU";
	
	public Boolean getEnable() {
		return enable;
	}
	public void setEnable(Boolean enable) {
		this.enable = enable;
	}
	public String getRoleTableName() {
		return roleTableName;
	}
	public void setRoleTableName(String roleTableName) {
		this.roleTableName = roleTableName;
	}
	public String getMenuTableName() {
		return menuTableName;
	}
	public void setMenuTableName(String menuTableName) {
		this.menuTableName = menuTableName;
	}
	
	
}
