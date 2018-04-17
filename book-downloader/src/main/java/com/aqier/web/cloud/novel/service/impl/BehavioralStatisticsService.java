/*
 * Domain Aqier.com Reserve Copyright
 * @author yuloang.wang@Aqier.com
 * @since 2017年8月24日
 */
package com.aqier.web.cloud.novel.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.aqier.web.cloud.core.utils.CommonUtil;
import com.aqier.web.cloud.novel.dao.mapper.BehavioralStatisticsMapper;
import com.aqier.web.cloud.novel.dto.model.BehavioralStatistics;
import com.aqier.web.cloud.novel.service.IBehavioralStatisticsService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 行为统计服务
 * @author yulong.wang@Aqier.com
 * @since 2018年4月17日
 */
@Service
public class BehavioralStatisticsService implements IBehavioralStatisticsService {

	@Autowired
	private BehavioralStatisticsMapper behavioralStatisticsMapper;
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	public BehavioralStatisticsService() {
	}
	
	@Async
	@Override
	public void insert(String source, String target, Object data, long useTime) {
		BehavioralStatistics record = new BehavioralStatistics();
		record.setSource(source);
		record.setTarget(target);
		record.setData(CommonUtil.maxByteString(CommonUtil.toJSONString(data), 4000));
		record.setUseTime((int)useTime);
		CommonUtil.initInsertParam(record);
		behavioralStatisticsMapper.insert(record);
	}
}
