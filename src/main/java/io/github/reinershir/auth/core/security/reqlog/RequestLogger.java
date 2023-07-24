package io.github.reinershir.auth.core.security.reqlog;

import io.github.reinershir.auth.entity.RequestLog;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface RequestLogger {

	/**
	 * @Title: processRequestLog
	 * @Description:   每次请求都会调用此方法，用于日志打印，request类已重新包装可重复读取IO流
	 * @author reinershir
	 * @date 2020年12月18日
	 * @param request 包装后的request(可重复获取requestBody内容)
	 * @param requestLog 本次请求的参数
	 */
	public void processRequestLog(HttpServletRequest request,RequestLog requestLog);
	
	/**
	 * @Title: processResponseLog
	 * @Description:   controller处理完成后的处理方法，用于计算耗时
	 * @author reinershir
	 * @date 2020年12月18日
	 * @param response
	 */
	public void processResponseLog(HttpServletResponse response);
}
