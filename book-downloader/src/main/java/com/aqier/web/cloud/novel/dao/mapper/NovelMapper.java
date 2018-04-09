package com.aqier.web.cloud.novel.dao.mapper;

import com.aqier.web.cloud.novel.dto.model.Novel;
import com.aqier.web.cloud.novel.dto.model.NovelExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface NovelMapper {
    long countByExample(NovelExample example);

    int deleteByExample(NovelExample example);

    int deleteByPrimaryKey(String id);

    int insert(Novel record);

    int insertSelective(Novel record);

    List<Novel> selectByExample(NovelExample example);

    Novel selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") Novel record, @Param("example") NovelExample example);

    int updateByExample(@Param("record") Novel record, @Param("example") NovelExample example);

    int updateByPrimaryKeySelective(Novel record);

    int updateByPrimaryKey(Novel record);
}