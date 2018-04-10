/*
 * Domain Aqier.com Reserve Copyright
 * @author yuloang.wang@Aqier.com
 * @since 2017年8月24日
 */
package com.aqier.web.cloud.novel.controller;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aqier.web.cloud.core.constants.YesOrNo;
import com.aqier.web.cloud.core.dto.Page;
import com.aqier.web.cloud.core.utils.CommonUtil;
import com.aqier.web.cloud.novel.dao.mapper.ChapterMapper;
import com.aqier.web.cloud.novel.dao.mapper.NovelMapper;
import com.aqier.web.cloud.novel.dto.NovelDTO;
import com.aqier.web.cloud.novel.dto.model.Chapter;
import com.aqier.web.cloud.novel.dto.model.ChapterExample;
import com.aqier.web.cloud.novel.dto.model.Novel;
import com.aqier.web.cloud.novel.dto.model.NovelExample;
import com.aqier.web.cloud.novel.dto.model.NovelExample.Criteria;
import com.aqier.web.cloud.novel.service.INovelDownloader;
import com.github.pagehelper.PageHelper;

/**
 * 
 * @author yulong.wang@Aqier.com
 * @since 2018年3月23日
 */
@RestController
@RequestMapping("/novel")
public class NovelDownloadController {

	@Autowired
	private Set<INovelDownloader> novelDownloaders;
	
	@Autowired
	private NovelMapper novelMapper;
	
	@Autowired
	private ChapterMapper chapterMapper;
	
	@GetMapping("/getChapter")
	public Chapter getChapter(String id, Integer serialNumber) {
		if(serialNumber == null) {
			serialNumber = 1;
		}
		ChapterExample example = new ChapterExample();
		ChapterExample.Criteria c = example.createCriteria();
		c.andNovelIdEqualTo(id);
		c.andDeletedFlagEqualTo(YesOrNo.no.toString());
		c.andSerialNumberEqualTo(serialNumber);
		List<Chapter> chapters = chapterMapper.selectByExampleWithBLOBs(example);
		Chapter chapter = null;
		if(!chapters.isEmpty()) {
			chapter = chapters.get(0);
		}else {
			chapter = new Chapter();
			chapter.setTitle("无法找到该章节");
			chapter.setContent("");
			chapter.setSerialNumber(1);
		}
		return chapter;
	}
	
	@PostMapping("/getChapters")
	public Page<Chapter> getChapters(@RequestBody Page<Chapter> page) {
		ChapterExample example = new ChapterExample();
		ChapterExample.Criteria c = example.createCriteria();
		c.andNovelIdEqualTo(page.getFirstRow().getNovelId());
		c.andDeletedFlagEqualTo(YesOrNo.no.toString());
		if(!CommonUtil.isBlank(page.getFirstRow().getTitle())) {
			c.andTitleLike("%"+page.getFirstRow().getTitle()+"%");
		}
		PageHelper.startPage(page.getPageNo(), page.getPageSize());
		List<Chapter> chapters = chapterMapper.selectByExample(example);
		return convertToPage(chapters);
	}
	
	@GetMapping("/search")
	public NovelDTO search(String novelName, Boolean update) {
		if(update == null) {
			update = false;
		}
		for(INovelDownloader novelDownloader : novelDownloaders) {
			NovelDTO novel = novelDownloader.searchNovel(novelName, update);
			if(novel != null) {
				return novel;
			}
		}
		NovelDTO notFount = new NovelDTO();
		notFount.setName("Novel ["+novelName+"] not found");
		return notFount;
	}
	
	@GetMapping("/sync")
	public NovelDTO sync(String novelName, Boolean update) {
		if(update == null) {
			update = false;
		}
		for(INovelDownloader novelDownloader : novelDownloaders) {
			NovelDTO novel = novelDownloader.searchNovel(novelName, update);
			if(novel != null) {
				new Thread(() -> {
					novelDownloader.searchChapter(novel);
				}).start();
				return novel;
			}
		}
		return null;
	}
	
	@GetMapping("/scan")
	public void scan() {
		for(INovelDownloader novelDownloader : novelDownloaders) {
			novelDownloader.scan();
		}
	}
	
	@PostMapping("/query")
	public Page<Novel> queryNovel(@RequestBody Page<NovelDTO> page) {
		NovelExample example = new NovelExample();
		Criteria c = example.createCriteria();
		c.andDeletedFlagEqualTo(YesOrNo.no.toString());
		NovelDTO param = page.getFirstRow();
		if(!CommonUtil.isBlank(param.getName())) {
			c.andNameLike("%" + param.getName() + "%");
		}
		if(!CommonUtil.isBlank(param.getAuthor())) {
			c.andAuthorLike("%" + param.getAuthor() + "%");
		}
		PageHelper.startPage(page.getPageNo(), page.getPageSize());
		List<Novel> result = novelMapper.selectByExample(example);
		if(!CommonUtil.isBlank(param.getName()) && result.isEmpty()) {
			sync(param.getName(), false);
		}
		return convertToPage(result);
	}
	
	/**
	 * @param result
	 * @return
	 * @author yulong.wang@aqier.com
	 * @since 2018年3月29日
	 */
	private <T> Page<T> convertToPage(List<T> list) {
		Page<T> page = new Page<>();
		page.setRows(list);
		if(list instanceof com.github.pagehelper.Page) {
			com.github.pagehelper.Page<T> pageResult = CommonUtil.caseObject(list);
			page.setTotal((int)pageResult.getTotal());
			page.setPageNo(pageResult.getPageNum());
			page.setPageSize(pageResult.getPageSize());
		}
		return page;
	}

	@GetMapping("/download")
	public void download(String novelName, String id, HttpServletResponse response) throws IOException {
		NovelExample novelExample = new NovelExample();
		NovelExample.Criteria c1 = novelExample.createCriteria();
		c1.andDeletedFlagEqualTo(YesOrNo.no.toString());
		String msg = "";
		if(!CommonUtil.isBlank(novelName)) {
			c1.andNameEqualTo(novelName);
			msg += "书籍名称:[" + novelName + "] ";
		}
		if(!CommonUtil.isBlank(id)) {
			c1.andIdEqualTo(id);
			msg += "书籍ID:[" + id + "] ";
		}
		List<Novel> novels = novelMapper.selectByExample(novelExample);
		if(novels.isEmpty()) {
			response.getWriter().print("小说没有同步, 或无法找到, " + msg);
			return;
		}
		Novel novel = novels.get(0);
		ChapterExample chapterExample = new ChapterExample();
		ChapterExample.Criteria c2 = chapterExample.createCriteria();
		c2.andNovelIdEqualTo(novel.getId());
		c2.andDeletedFlagEqualTo(YesOrNo.no.toString());
		List<Chapter> chapters = chapterMapper.selectByExampleWithBLOBs(chapterExample);
		if(chapters.isEmpty()) {
			response.getWriter().print("小说没有同步, " + msg);
			return;
		}
		CommonUtil.setResponseFileHeader(response, novel.getName()+"_" + novel.getAuthor()+".txt");
		ServletOutputStream os = response.getOutputStream();
		for (Chapter chapter : chapters) {
			if(chapter.getContent() != null) {
				os.write(chapter.getContent().getBytes());
				os.write(enterByte);
			}
		}
		os.flush();
	}
	
	private byte[] enterByte = "\n".getBytes();
}
