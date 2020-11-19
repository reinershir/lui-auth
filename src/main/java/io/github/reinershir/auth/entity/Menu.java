package io.github.reinershir.auth.entity;

import java.util.LinkedList;
import java.util.List;

public class Menu {

	private String name;
	
	private String permissionCode;
	
	private Integer orderIndex;
	
	private List<Menu> children;
	
	
	public Menu(String name, String permissionCode,Integer orderIndex) {
		super();
		this.name = name;
		this.permissionCode = permissionCode;
		this.orderIndex=orderIndex;
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

	public Integer getOrderIndex() {
		return orderIndex;
	}

	public void setOrderIndex(Integer orderIndex) {
		this.orderIndex = orderIndex;
	}

	public void addChild(Menu menu) {
		if(children==null) {
			children = new LinkedList<>();
		}
		children.add(menu);
	}
	
	
}
