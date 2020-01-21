/*
 * Domain Aqier.com Reserve Copyright
 * @author yuloang.wang@Aqier.com
 * @since 2018年4月17日
 */
package com.aqier.web.cloud.novel.config;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aqier.web.cloud.novel.service.IBehavioralStatisticsService;
import com.aqier.web.cloud.novel.util.ClientIpUtil;

/**
 * 请求处理时间AOP处理器
 * @author yulong.wang@aqier.com
 * @since 2018年4月17日
 */
@Aspect
@Component
public class RequestProcessingTimeAspect {
	
	@Autowired
	private IBehavioralStatisticsService behavioralStatisticsService;
	
	@Autowired
	private HttpServletRequest request;
	
	@Pointcut("execution(* com.aqier.web.cloud.*.controller.**.*(..)) and @annotation(org.springframework.web.bind.annotation.RequestMapping)")
	public void controller() {}
	
	@Around("controller()")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		long start = System.currentTimeMillis();
		Object[] arguments = joinPoint.getArgs();
		String source = ClientIpUtil.getClientIp(request);
		String target = request.getRequestURI();
		try {
			Object returnValue = joinPoint.proceed(arguments);
			return returnValue;
		}finally {
			behavioralStatisticsService.insert(source, target, arguments, (System.currentTimeMillis() - start));
		}
	}

}
