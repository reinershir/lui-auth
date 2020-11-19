package io.github.reinershir.auth.entity;

public class TokenInfo {

	/**
	 * 用户类型
	 */
	private Integer userType;
	/**
	 * 用户ID
	 */
	private String userId;
	/**
	 * 随机生成的token,用于保证每次生成的token不一致
	 */
	private String token;
	
	public TokenInfo() {}
	
	public TokenInfo(Integer userType, String userId,String token) {
		super();
		this.userType = userType;
		this.userId = userId;
		this.token=token;
	}
	public Integer getUserType() {
		return userType;
	}
	public void setUserType(Integer userType) {
		this.userType = userType;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	
}
