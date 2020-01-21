/*
 * Domain Aqier.com Reserve Copyright
 * @author yuloang.wang@Aqier.com
 * @since 2017年8月24日
 */
package com.aqier.web.cloud.novel;

import java.util.Date;

import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.aqier.web.cloud.core.config.H2DatabaseServerStarter;
import com.aqier.web.cloud.core.config.MyBatisSessionFactoryConfig;
import com.aqier.web.cloud.core.service.DatabaseInitService;
import com.aqier.web.cloud.novel.filter.LimitFilter;
import com.aqier.web.cloud.novel.service.IBehavioralStatisticsService;

/**
 * 小说下载程序
 * 
 * @author yulong.wang@Aqier.com
 * @since 2018年3月16日
 */
@Controller
@EnableAsync
@SpringBootApplication
@EnableAutoConfiguration
@ImportAutoConfiguration(classes = {MyBatisSessionFactoryConfig.class})
public class BookDownloaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookDownloaderApplication.class, args);
	}
	
	@GetMapping("/")
	public String homePage() {
		return "index.html?" + new Date().getTime();
	}
	
	/**
	 * 启动H2 database 数据库
	 * 
	 * @param environment
	 * @return
	 * @author yulong.wang@Aqier.com
	 * @since 2017年12月21日
	 */
	@Bean // 只有在 连接池配置为 H2 数据库是才启动
	@ConditionalOnProperty(name = "spring.datasource.driverClassName", havingValue = "org.h2.Driver")
	public H2DatabaseServerStarter h2Database(Environment environment) {
		return new H2DatabaseServerStarter(environment);
	}
	
	/**
	 * 启动时自动初始化数据库
	 * 
	 * @param dataSource
	 * @return
	 * @author yulong.wang@Aqier.com
	 * @since 2018年3月27日
	 */
	@Bean
	public DatabaseInitService autoInitDatabase(DataSource dataSource) {
		DatabaseInitService databaseInitService = new DatabaseInitService(dataSource);
		databaseInitService.initDatabase("init.sql");
		return databaseInitService;
	}
	
	@Bean
	public FilterRegistrationBean limitFilter(IBehavioralStatisticsService behavioralStatisticsService) {
		FilterRegistrationBean filter = new FilterRegistrationBean();
		filter.setFilter(new LimitFilter(behavioralStatisticsService));
		filter.addUrlPatterns("/123456");
		return filter;
	}
	
}
