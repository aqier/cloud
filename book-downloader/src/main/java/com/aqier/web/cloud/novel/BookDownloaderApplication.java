/*
 * Domain Aqier.com Reserve Copyright
 * @author yulong.wang@Aqier.com
 * @since 2017年8月24日
 */
package com.aqier.web.cloud.novel;

import java.util.Date;
import java.util.UUID;

import javax.servlet.Filter;
import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.aqier.web.cloud.core.config.MyBatisSessionFactoryConfig;
import com.aqier.web.cloud.core.service.DatabaseInitService;
import com.aqier.web.cloud.novel.filter.LimitFilter;
import com.aqier.web.cloud.novel.service.IBehavioralStatisticsService;
import com.aqier.web.cloud.sso.client.feign.IUserService;

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
@EnableFeignClients(basePackageClasses = IUserService.class)
@ImportAutoConfiguration(classes = { MyBatisSessionFactoryConfig.class })
public class BookDownloaderApplication {

    public static void main(String[] args) {
        System.out.println(UUID.randomUUID().toString().replace("-", ""));
        SpringApplication.run(BookDownloaderApplication.class, args);
    }

    @GetMapping("/")
    public String homePage() {
        return "index.html?" + new Date().getTime();
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
    public FilterRegistrationBean<Filter> limitFilter(IBehavioralStatisticsService behavioralStatisticsService) {
        FilterRegistrationBean<Filter> filter = new FilterRegistrationBean<>();
        filter.setFilter(new LimitFilter(behavioralStatisticsService));
        filter.addUrlPatterns("/123456");
        return filter;
    }

}
