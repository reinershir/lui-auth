package io.github.reinershir.auth.core.model;

import java.io.Serializable;
import java.util.Date;

public class Role implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -19073640445718254L;

	private Long id;
	
	private String roleName;
	
	private String description;
	
	private Date createDate;
	
	private Date updateDate;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
	
	
}
