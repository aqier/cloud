/*
 * Domain Aqier.com Reserve Copyright
 * @author yuloang.wang@Aqier.com
 * @since 2017年8月24日
 */
package com.aqier.web.cloud.novel.dao;

import java.util.List;

import com.aqier.web.cloud.novel.dto.NovelDTO;

/**
 * 
 * @author yulong.wang@Aqier.com
 * @since 2018年4月16日
 */
public interface ChapterDAO {

	/**
	 * 
	 * @param novelIds
	 * @return
	 * @author yulong.wang@Aqier.com
	 * @since 2018年4月16日
	 */
	List<NovelDTO> countChapter(List<String> novelIds);
}
