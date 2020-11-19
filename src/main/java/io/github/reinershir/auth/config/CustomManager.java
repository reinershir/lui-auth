package io.github.reinershir.auth.config;

import io.github.reinershir.auth.core.Feedbacker;
import io.github.reinershir.auth.core.SecurityGuard;

public class CustomManager {
	
	Feedbacker feedbacker;
	
	SecurityGuard securityGuard;

	public void setCustomFeedback(Feedbacker feedbacker) {
		this.feedbacker=feedbacker;
	}
	
	/**
	 * @Title: setCustomSecurityGuard
	 * @Description:   自定义权限验证，将会使默认的权限验证功能失效
	 * @author xh
	 * @date 2020年11月11日
	 * @param securityGuard
	 */
	public void setCustomSecurityGuard(SecurityGuard securityGuard) {
		this.securityGuard=securityGuard;
	}

	public Feedbacker getFeedbacker() {
		return feedbacker;
	}

	public SecurityGuard getSecurityGuard() {
		return securityGuard;
	}
	
	
}
