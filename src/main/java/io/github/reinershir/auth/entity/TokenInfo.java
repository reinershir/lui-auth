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
	private String random;
	
	/**
	 * 保存在token中的用户信息
	 */
	private Object userInfo;
	
	public TokenInfo() {}
	
	public TokenInfo(Integer userType, String userId,String random,Object userInfo) {
		super();
		this.userType = userType;
		this.userId = userId;
		this.random=random;
		this.userInfo=userInfo;
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

	public String getRandom() {
		return random;
	}

	public void setRandom(String random) {
		this.random = random;
	}

	public Object getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(Object userInfo) {
		this.userInfo = userInfo;
	}
	
	
	
}
