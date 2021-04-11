/*
 * Domain Aqier.com Reserve Copyright
 * @author yulong.wang@Aqier.com
 * @since 2018年3月14日
 */
package com.aqier.web.cloud.novel.service.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.stereotype.Service;

/**
 * 
 * @author yulong.wang@aqier.com
 * @since 2018年3月14日
 */
@Service
public class NovelDownloader4Luoxia extends NovelDownloader {
	
	public NovelDownloader4Luoxia() {
		this.searchUrl = "https://www.luoxia.com/?s=";
		this.successSleepTime = 0;
	}
	
	@Test
	public void download() throws Exception {
		String url = "http://www.luoxia.com/?s=";
		NovelDownloader4Luoxia downloader = new NovelDownloader4Luoxia();
		
		// 下载武炼巅峰
		String novelName = "三体";
		downloader.getChapters(url, novelName, true);
	}
	
	/* 
	 * @author yulong.wang@aqier.com
	 * @since 2018年3月14日
	 */
	@Override
	protected String getChapterContentHtml(String html) {
		html = StringUtils.substringBetween(html, "<p>", "<div ");
		return super.getChapterContentHtml(html);
	}
	
	/* 
	 * @author yulong.wang@aqier.com
	 * @since 2018年3月15日
	 */
	@Override
	protected List<String> getCatalogLinks(String catalogUrl) {
		String pageContent = loadPageContent(catalogUrl);
		pageContent = StringUtils.substringBetween(pageContent, "book-list clearfix", "<script");
		List<String> links = pairMatch("<a", "</a>", true, pageContent);
		return links;
	}
	
	/* 
	 * @author yulong.wang@aqier.com
	 * @since 2018年3月15日
	 */
	@Override
	protected String getChapterContentUrl(String catalogUrl, String link) {
		String chapterContentLink = getLinkHref(catalogUrl, link, ".+>.+<.+");
		return chapterContentLink;
	}
	
	/* 
	 * @author yulong.wang@aqier.com
	 * @since 2018年3月28日
	 */
	@Override
	protected String getAuthor(String pageContent, String linkText, String novelName) {
		String text = StringUtils.substringBetween(pageContent, ">"+novelName+"</a>", "</tr>");
		if(text != null) {
			text = StringUtils.substringBetween(text, "class=\"odd\">", "</td>");
		}
		return text == null ? "佚名" : text;
	}
}
