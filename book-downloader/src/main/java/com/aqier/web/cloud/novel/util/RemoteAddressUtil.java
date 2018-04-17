/*
 * Domain Aqier.com Reserve Copyright
 * @author yuloang.wang@Aqier.com
 * @since 2017年8月24日
 */
package com.aqier.web.cloud.novel.util;

/**
 * 
 * @author yulong.wang@Aqier.com
 * @since 2018年4月17日
 */
public class RemoteAddressUtil {

	private static String[] proxyHeaderNames = new String[]{"x-forwarded-for", "Proxy-Client-IP"};
	
	/**
	 * 获得客户端的主机名  
	 * @param request
	 * @return
	 * @author yulong.wang@Aqier.com
	 * @since 2018年4月17日
	 */
	public static String getRemoteHost(javax.servlet.http.HttpServletRequest request) {
		// header 名称关键看代理服务器(如:nginx)中配置的: proxy_set_header X-Real-IP $remote_addr;
		String ip = null;
		boolean isFound = false;
		for (String headerName : proxyHeaderNames) {
			ip = request.getHeader(headerName);
			if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
				isFound = true;
				break;
			}
		}
		if(!isFound) {
			ip = request.getRemoteAddr();
		}
		return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
	}
}
