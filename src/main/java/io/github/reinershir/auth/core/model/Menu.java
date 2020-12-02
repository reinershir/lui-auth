package io.github.reinershir.auth.core.model;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Menu implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8235686637656316772L;

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
	
	private Date createDate;
	
	private Date updateDate;
	
	private List<Menu> children = new LinkedList<>();
	
	/**
	 * 菜单结构左值，用于区分节点位置(前端页面用不上此值)
	 */
	private Integer leftValue;
	
	/**
	 * 菜单结构右值，用于区分节点位置(前端页面用不上此值)
	 */
	private Integer rightValue;
	/**
	 * 菜单结构层级标识
	 */
	private Integer level;

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

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public Integer getLeftValue() {
		return leftValue;
	}

	public void setLeftValue(Integer leftValue) {
		this.leftValue = leftValue;
	}

	public Integer getRightValue() {
		return rightValue;
	}

	public void setRightValue(Integer rightValue) {
		this.rightValue = rightValue;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public List<Menu> getChildren() {
		return children;
	}

	public void setChildren(List<Menu> children) {
		this.children = children;
	}
	
	
}
