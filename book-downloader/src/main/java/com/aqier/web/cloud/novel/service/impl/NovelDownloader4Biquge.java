/*
 * Domain Aqier.com Reserve Copyright
 * @author yuloang.wang@Aqier.com
 * @since 2018年3月13日
 */
package com.aqier.web.cloud.novel.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.aqier.web.cloud.novel.dto.ChapterDTO;

/**
 * 笔趣阁小说下载器
 * 
 * @author yulong.wang@aqier.com
 * @since 2018年3月13日
 */
@Service
public class NovelDownloader4Biquge extends NovelDownloader {

	private String url = "https://www.biquge5200.com/modules/article/search.php?searchkey=";
	
	public NovelDownloader4Biquge() {
		this.searchUrl = url;
	}
	
	/* 
	 * @author yulong.wang@Aqier.com
	 * @since 2018年3月21日
	 */
	public List<ChapterDTO> getChapters(String novelName) {
		return super.getChapters(url, novelName, true);
	}
	
	/*
	 * @author yulong.wang@aqier.com
	 * 
	 * @since 2018年3月13日
	 */
	@Override
	protected List<String> getCatalogLinks(String catalogUrl) {
		String pageContent = loadPageContent(catalogUrl);
		int index = pageContent.indexOf("正文</dt>");
		pageContent = pageContent.substring(index == -1 ? 0 : index); // 去掉最新章节
		List<String> links = pairMatch("<a", "</a>", true, pageContent);
		return links;
	}

	/*
	 * @author yulong.wang@aqier.com
	 * 
	 * @since 2018年3月13日
	 */
	@Override
	protected String getChapterContentHtml(String html) {
		String result = StringUtils.substringBetween(html, "<div id=\"content\">", "</div>");
		if(result == null) {
			result = StringUtils.substringBetween(html, "<div id=\"nr1\">", "</div>");
		}
		return result;
	}
	
	/*
	 * @author yulong.wang@aqier.com
	 * @since 2018年3月28日
	 */
	protected String getAuthor(String pageContent, String linkText, String novelName) {
		String text = StringUtils.substringBetween(pageContent, ">"+novelName+"</a>", "</tr>");
		if(text != null) {
			text = StringUtils.substringBetween(text, "class=\"odd\">", "</td>");
		}
		return text == null ? "佚名" : text;
	}
	
	/* 
	 * @author yulong.wang@aqier.com
	 * @since 2018年3月28日
	 */
	@Override
	protected List<String> scanNovelName() {
		String pageContent = loadPageContent("https://www.biquge5200.com/xiaoshuodaquan/");
		String content = StringUtils.substringBetween(pageContent, "<div id=\"main\">", "<div class=\"dahengfu\">");
		List<String> links = pairMatch("<a ", "</a>", true, content);
		List<String> novelNames = new ArrayList<>();
		for (String link : links) {
			String name = StringUtils.substringBetween(link, ">", "<");
			if(name != null) {
				novelNames.add(name);
			}
		}
		return novelNames;
	}
}
