package io.github.reinershir.auth.contract;

public class AuthContract {

	public static final String SERVICE_SECRET_HEADER = "Service-Secret-Header";
	
	public static final String PERMISSION_CODE_KEY="-permissionCodes";
	
	public static final String TEMPORARY_PERMISSION_KEY="-temporary-permission-key";
	
	public static final String USER_ROLE_BIND_KEY="LUI-AUTH-BIND-ROLE-KEY-";
	
	public static final int AUTHORIZATION_STATUS_SUCCESS=0;
	
	public static final int AUTHORIZATION_STATUS_NO_TOKEN=1;
	
	public static final int AUTHORIZATION_STATUS_ILLEGAL=2;
	
	public static final int AUTHORIZATION_STATUS_NO_PERMISSION=3;
	
	public static final int AUTHORIZATION_STATUS_UNKNOWN=4;
	
}
