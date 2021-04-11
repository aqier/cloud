/*
 * Domain Aqier.com Reserve Copyright
 * @author yulong.wang@Aqier.com
 * @since 2017年8月24日
 */
package com.aqier.web.cloud.novel.dto;

import com.aqier.web.cloud.novel.dto.model.Novel;

/**
 * 
 * @author yulong.wang@Aqier.com
 * @since 2018年3月28日
 */
public class NovelDTO extends Novel {

	private static final long serialVersionUID = 1L;
	
	private Integer downloadChapterNum;
	
	public Integer getDownloadChapterNum() {
		return downloadChapterNum;
	}

	public void setDownloadChapterNum(Integer downloadChapterNum) {
		this.downloadChapterNum = downloadChapterNum;
	}
	
}
