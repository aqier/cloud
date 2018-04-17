package com.aqier.web.cloud.novel.dao.mapper;

import com.aqier.web.cloud.novel.dto.model.BehavioralStatistics;
import com.aqier.web.cloud.novel.dto.model.BehavioralStatisticsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BehavioralStatisticsMapper {
    long countByExample(BehavioralStatisticsExample example);

    int deleteByExample(BehavioralStatisticsExample example);

    int deleteByPrimaryKey(String id);

    int insert(BehavioralStatistics record);

    int insertSelective(BehavioralStatistics record);

    List<BehavioralStatistics> selectByExample(BehavioralStatisticsExample example);

    BehavioralStatistics selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") BehavioralStatistics record, @Param("example") BehavioralStatisticsExample example);

    int updateByExample(@Param("record") BehavioralStatistics record, @Param("example") BehavioralStatisticsExample example);

    int updateByPrimaryKeySelective(BehavioralStatistics record);

    int updateByPrimaryKey(BehavioralStatistics record);
}