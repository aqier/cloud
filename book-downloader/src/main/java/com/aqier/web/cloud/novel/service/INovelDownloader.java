/*
 * Domain Aqier.com Reserve Copyright
 * @author yulong.wang@Aqier.com
 * @since 2017年8月24日
 */
package com.aqier.web.cloud.novel.service;

import java.io.OutputStream;
import java.util.List;

import org.springframework.scheduling.annotation.Async;

import com.aqier.web.cloud.novel.dto.NovelDTO;
import com.aqier.web.cloud.novel.dto.model.Chapter;
import com.aqier.web.cloud.novel.dto.model.Novel;

/**
 * 
 * @author yulong.wang@Aqier.com
 * @since 2018年3月23日
 */
public interface INovelDownloader {

	/**
	 * 搜索
	 * @param novelVO
	 * @return 所有章节名称
	 * @author yulong.wang@Aqier.com
	 * @since 2018年3月23日
	 */
	List<Chapter> searchChapter(Novel novelVO);
	
	/**
	 * 下载
	 * @param novel
	 * @param os
	 * @author yulong.wang@Aqier.com
	 * @since 2018年3月23日
	 */
	void download(String novel, OutputStream os);

	List<NovelDTO> searchNovel(String novelName, boolean update);
	
	@Async
	void scan();
}
