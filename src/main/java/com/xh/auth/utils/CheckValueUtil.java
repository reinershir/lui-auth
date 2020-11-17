package com.xh.auth.utils;

import org.springframework.util.StringUtils;

import com.xh.auth.annotation.Permission;

public class CheckValueUtil {

	public static boolean checkPermissionCode(Permission hasPermission) {
		return hasPermission!=null&&!StringUtils.isEmpty(hasPermission.value())?true:false;
	}
}
