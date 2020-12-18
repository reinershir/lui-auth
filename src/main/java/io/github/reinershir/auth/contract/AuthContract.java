package io.github.reinershir.auth.contract;

public class AuthContract {

	public static final String SERVICE_SECRET_HEADER = "Service-Secret-Header";
	
	public static final String PERMISSION_CODE_KEY="-permissionCodes";
	
	public static final String TEMPORARY_PERMISSION_KEY="-temporary-permission-key";
	
	public static final String USER_ROLE_BIND_KEY="LUI-AUTH-BIND-ROLE-KEY-";
	
	/**
	 * 身份验证状态：成功
	 */
	public static final int AUTHORIZATION_STATUS_SUCCESS=0;
	/**
	 * 身份验证状态：无token
	 */
	public static final int AUTHORIZATION_STATUS_NO_TOKEN=1;
	/**
	 * 身份验证状态：token错误
	 */
	public static final int AUTHORIZATION_STATUS_ILLEGAL=2;
	/**
	 * 身份验证状态：没有权限
	 */
	public static final int AUTHORIZATION_STATUS_NO_PERMISSION=3;
	/**
	 * 身份验证状态：未知状态
	 */
	public static final int AUTHORIZATION_STATUS_UNKNOWN=4;
	/**
	 * 请求过于频繁
	 */
	public static final int AUTHORIZATION_STATUS_TOO_FREQUENT=5;
	
}
