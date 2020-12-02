package io.github.reinershir.auth.entity;

import java.util.LinkedList;
import java.util.List;

public class MenuInfo {

	private String name;
	
	private String[] permissionCode;
	
	private Integer orderIndex;
	
	private List<MenuInfo> children;
	
	
	public MenuInfo(String name, String[] permissionCode,Integer orderIndex) {
		super();
		this.name = name;
		this.permissionCode = permissionCode;
		this.orderIndex=orderIndex;
	}
	
	public MenuInfo(String name, String permissionCode,Integer orderIndex) {
		super();
		this.name = name;
		this.permissionCode = new String[1];
		this.permissionCode[0] = permissionCode;
		this.orderIndex=orderIndex;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getPermissionCode() {
		return permissionCode;
	}

	public void setPermissionCode(String[] permissionCode) {
		this.permissionCode = permissionCode;
	}

	public List<MenuInfo> getChildren() {
		return children;
	}

	public void setChildren(List<MenuInfo> children) {
		this.children = children;
	}

	public Integer getOrderIndex() {
		return orderIndex;
	}

	public void setOrderIndex(Integer orderIndex) {
		this.orderIndex = orderIndex;
	}

	public void addChild(MenuInfo menu) {
		if(children==null) {
			children = new LinkedList<>();
		}
		children.add(menu);
	}
	
	
}
