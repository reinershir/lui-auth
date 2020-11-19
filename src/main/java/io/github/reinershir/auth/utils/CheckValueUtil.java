package io.github.reinershir.auth.utils;

import org.springframework.util.StringUtils;

import io.github.reinershir.auth.annotation.Permission;

public class CheckValueUtil {

	public static boolean checkPermissionCode(Permission hasPermission) {
		return hasPermission!=null&&!StringUtils.isEmpty(hasPermission.value())?true:false;
	}
}
