/*
 * Domain Aqier.com Reserve Copyright
 * @author yulong.wang@Aqier.com
 * @since 2017年8月24日
 */
package com.aqier.web.cloud.novel.service;

/**
 * 
 * @author yulong.wang@Aqier.com
 * @since 2018年4月17日
 */
public interface IBehavioralStatisticsService {

	void insert(String source, String target, Object data, long useTime);

}
