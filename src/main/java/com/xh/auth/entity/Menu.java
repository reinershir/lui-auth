package com.xh.auth.entity;

import java.util.LinkedList;
import java.util.List;

public class Menu {

	private String name;
	
	private String permissionCode;
	
	private List<Menu> children = new LinkedList<>();
	
	
	public Menu(String name, String permissionCode) {
		super();
		this.name = name;
		this.permissionCode = permissionCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPermissionCode() {
		return permissionCode;
	}

	public void setPermissionCode(String permissionCode) {
		this.permissionCode = permissionCode;
	}

	public List<Menu> getChildren() {
		return children;
	}

	public void setChildren(List<Menu> children) {
		this.children = children;
	}

	
	
	
}
