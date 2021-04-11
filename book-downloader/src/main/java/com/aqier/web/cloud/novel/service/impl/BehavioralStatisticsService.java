/*
 * Domain Aqier.com Reserve Copyright
 * @author yulong.wang@Aqier.com
 * @since 2017年8月24日
 */
package com.aqier.web.cloud.novel.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.aqier.web.cloud.core.utils.CommonUtil;
import com.aqier.web.cloud.novel.dao.mapper.BehavioralStatisticsMapper;
import com.aqier.web.cloud.novel.dto.model.BehavioralStatistics;
import com.aqier.web.cloud.novel.service.IBehavioralStatisticsService;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * 行为统计服务
 * @author yulong.wang@Aqier.com
 * @since 2018年4月17日
 */
@Service
public class BehavioralStatisticsService implements IBehavioralStatisticsService {

	@Autowired
	private BehavioralStatisticsMapper behavioralStatisticsMapper;
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	private Log log = LogFactory.getLog(BehavioralStatisticsService.class);
	
	@SuppressWarnings("deprecation")
    public BehavioralStatisticsService() {
		objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
		objectMapper.setSerializationInclusion(Include.NON_NULL);
	}
	
	@Async
	@Override
	public void insert(String source, String target, Object data, long useTime) {
		BehavioralStatistics record = new BehavioralStatistics();
		record.setSource(source);
		record.setTarget(target);
		record.setData(CommonUtil.maxByteString(toJSONString(data), 4000));
		record.setUseTime((int)useTime);
		CommonUtil.initInsertParam(record);
		behavioralStatisticsMapper.insert(record);
	}
	
	private String toJSONString(Object obj) {
		String s = null;
		try {
			s = objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			log.error(e.getMessage());
			s = obj == null ? null : obj.toString();
		}
		return s;
	}
}
