/*
 * Domain Aqier.com Reserve Copyright
 * @author yuloang.wang@Aqier.com
 * @since 2017年8月24日
 */
package com.aqier.web.cloud.novel.dto;

import com.aqier.web.cloud.novel.dto.model.Chapter;

/**
 * 
 * @author yulong.wang@Aqier.com
 * @since 2018年3月27日
 */
public class ChapterDTO extends Chapter {

	private static final long serialVersionUID = 1L;
	
	private String link;

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
}
