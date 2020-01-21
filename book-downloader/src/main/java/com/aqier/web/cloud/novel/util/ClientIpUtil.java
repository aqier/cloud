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
public class ClientIpUtil {

	private static String[] proxyHeaderNames = new String[]{"X-Forwarded-For", "Proxy-Client-IP"};
	
	/**
	 * 获得客户端的主机名  
	 * @param request
	 * @return
	 * @author yulong.wang@Aqier.com
	 * @since 2018年4月17日
	 */
	public static String getClientIp(javax.servlet.http.HttpServletRequest request) {
		// header 名称关键看代理服务器(如:nginx)中配置的: proxy_set_header X-Real-IP $remote_addr;
		String ip = null;
		boolean isFound = false;
		for (String headerName : proxyHeaderNames) {
			ip = request.getHeader(headerName);
			if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
				if(ip.contains(",")) { // 原IP地址可能被代理多次, x-forwarded-for 会以逗号隔开存储多个 
					ip = ip.split(",")[0];
				}
				isFound = true;
				break;
			}
		}
		if(!isFound) {
			ip = request.getRemoteAddr();
		}
		return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
	}
}
