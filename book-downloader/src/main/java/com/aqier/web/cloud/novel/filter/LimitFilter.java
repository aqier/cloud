/*
 * Domain Aqier.com Reserve Copyright
 * @author yuloang.wang@Aqier.com
 * @since 2018年3月30日
 */
package com.aqier.web.cloud.novel.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aqier.web.cloud.core.utils.CommonUtil;

/**
 * 
 * @author yulong.wang@aqier.com
 * @since 2018年3月30日
 */
public class LimitFilter implements Filter {
	
	private static final String LIMIT_ACCESS_ATTR = "LIMIT_ACCESS_ATTR";
	
	private static final List<String> resources = Arrays.asList(new String[] {"/index.html", 
			"/css/aqier.css", "/aqierJParser.js", "/message.zh_CN.js"});
	
	private static final Log log = LogFactory.getLog(LimitFilter.class);
	
	/* 
	 * @author yulong.wang@aqier.com
	 * @since 2018年3月30日
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
	}

	/* 
	 * @author yulong.wang@aqier.com
	 * @since 2018年3月30日
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		// 转换
		HttpServletRequest req = (HttpServletRequest)request;
		HttpSession session = req.getSession();
		Set<String> resourceSet = CommonUtil.caseObject(session.getAttribute(LIMIT_ACCESS_ATTR));
		if(resourceSet == null) {
			resourceSet = new HashSet<>();
			session.setAttribute(LIMIT_ACCESS_ATTR, resourceSet);
		}
		String requestURI = req.getRequestURI();
		if("/".equals(requestURI)) {
			requestURI = "/index.html";
		}
		if(requestURI.contains(".")) { // 资源文件
			resourceSet.add(requestURI);
		}else {
			for (String resource : resources) {
				if(!resourceSet.contains(resource)) {
					log.warn("Illegal access ["+requestURI+"], remote address:" + req.getRemoteAddr());
					((HttpServletResponse)response).sendRedirect("/");
					return;
				}
			}
			log.info("Request data ["+requestURI+"], remote address:" + req.getRemoteAddr());
		}
		chain.doFilter(request, response);
	}

	/* 
	 * @author yulong.wang@aqier.com
	 * @since 2018年3月30日
	 */
	@Override
	public void destroy() {
		
	}

}
