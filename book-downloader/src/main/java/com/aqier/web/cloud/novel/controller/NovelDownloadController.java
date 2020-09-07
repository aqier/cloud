/*
 * Domain Aqier.com Reserve Copyright
 * 
 * @author yuloang.wang@Aqier.com
 * 
 * @since 2017年8月24日
 */
package com.aqier.web.cloud.novel.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aqier.web.cloud.core.constants.Constants;
import com.aqier.web.cloud.core.constants.YesOrNo;
import com.aqier.web.cloud.core.dto.Page;
import com.aqier.web.cloud.core.utils.ChineseNumberUtil;
import com.aqier.web.cloud.core.utils.CommonUtil;
import com.aqier.web.cloud.novel.dao.ChapterDAO;
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
    private ChapterDAO chapterDAO;

    @Autowired
    private ChapterMapper chapterMapper;

    @GetMapping("/getChapter")
    public Chapter getChapter(String id, Integer serialNumber) {
        if (serialNumber == null) {
            serialNumber = 1;
        }
        ChapterExample example = new ChapterExample();
        ChapterExample.Criteria c = example.createCriteria();
        c.andNovelIdEqualTo(id);
        c.andDeletedFlagEqualTo(YesOrNo.no.toString());
        c.andSerialNumberEqualTo(serialNumber);
        List<Chapter> chapters = chapterMapper.selectByExampleWithBLOBs(example);
        Chapter chapter = null;
        if (!chapters.isEmpty()) {
            chapter = chapters.get(0);
        } else {
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
        if (!CommonUtil.isBlank(page.getFirstRow().getTitle())) {
            c.andTitleLike("%" + page.getFirstRow().getTitle() + "%");
        }
        PageHelper.startPage(page.getPageNo(), page.getPageSize());
        List<Chapter> chapters = chapterMapper.selectByExample(example);
        return convertToPage(chapters, null);
    }

    @GetMapping("/search")
    public List<NovelDTO> search(String novelName, Boolean update) {
        if (update == null) {
            update = false;
        }
        for (INovelDownloader novelDownloader : novelDownloaders) {
            List<NovelDTO> novel = novelDownloader.searchNovel(novelName, update);
            if (!novel.isEmpty()) {
                return novel;
            }
        }
        NovelDTO notFount = new NovelDTO();
        notFount.setName("Novel [" + novelName + "] not found");
        return Arrays.asList(notFount);
    }

    @GetMapping("/sync")
    public List<NovelDTO> sync(String novelName, Boolean update) {
        if (update == null) {
            update = false;
        }
        for (INovelDownloader novelDownloader : novelDownloaders) {
            List<NovelDTO> novels = novelDownloader.searchNovel(novelName, update);
            if (!novels.isEmpty()) {
                new Thread(() -> {
                    for (NovelDTO novel : novels) {
                        String novelTag = novel.getAuthor() + "-" + novel.getName();
                        try {
                            System.out.println("同步小说章节：" + novelTag);
                            novelDownloader.searchChapter(novel);
                        } catch (Exception e) {
                            System.err.println("同步小说章节失败：" + novelTag);
                            e.printStackTrace();
                        }
                    }
                }).start();
                return novels;
            }
        }
        return Collections.emptyList();
    }

    @GetMapping("/scan")
    public void scan() {
        for (INovelDownloader novelDownloader : novelDownloaders) {
            novelDownloader.scan();
        }
    }

    @PostMapping("/query")
    public Page<NovelDTO> queryNovel(@RequestBody Page<NovelDTO> page) {
        NovelExample example = new NovelExample();
        Criteria c = example.createCriteria();
        c.andDeletedFlagEqualTo(YesOrNo.no.toString());
        NovelDTO param = page.getFirstRow();
        if (!CommonUtil.isBlank(param.getId())) {
            c.andIdEqualTo(param.getId());
        }
        if (!CommonUtil.isBlank(param.getName())) {
            c.andNameLike("%" + param.getName() + "%");
        }
        if (!CommonUtil.isBlank(param.getAuthor())) {
            c.andAuthorLike("%" + param.getAuthor() + "%");
        }
        PageHelper.startPage(page.getPageNo(), page.getPageSize());
        if (!CommonUtil.isBlank(page.getOrderBy())) {
            example.setOrderByClause(page.getOrderBy());
        }
        List<Novel> result = novelMapper.selectByExample(example);
        Page<NovelDTO> pageDatas = convertToPage(result, NovelDTO.class);
        if (!result.isEmpty()) {
            List<String> novelIds = result.stream().map(Novel::getId).collect(Collectors.toList());
            List<NovelDTO> countList = chapterDAO.countChapter(novelIds);
            Map<String, List<NovelDTO>> countsMap = countList.stream().collect(Collectors.groupingBy(NovelDTO::getId));
            for (NovelDTO novelDTO : pageDatas.getRows()) {
                List<NovelDTO> nd = countsMap.get(novelDTO.getId());
                if (nd != null) {
                    novelDTO.setDownloadChapterNum(nd.get(0).getDownloadChapterNum());
                } else {
                    novelDTO.setDownloadChapterNum(0);
                }
            }
        }
        if (!CommonUtil.isBlank(param.getName()) && result.isEmpty()) {
            sync(param.getName(), false);
        }
        return pageDatas;
    }

    /**
     * @param result
     * @return
     * @author yulong.wang@aqier.com
     * @since 2018年3月29日
     */
    private <T, R> Page<R> convertToPage(List<T> list, Class<R> clazz) {
        Page<R> page = new Page<>();
        if (list != null && !list.isEmpty()) {
            try {
                if (clazz != null && !list.get(0).getClass().equals(clazz)) {
                    List<R> rows = new ArrayList<>();
                    for (T t : list) {
                        R r = clazz.newInstance();
                        BeanUtils.copyProperties(t, r);
                        rows.add(r);
                    }
                    page.setRows(rows);
                } else {
                    page.setRows(CommonUtil.caseObject(list));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            page.setRows(CommonUtil.caseObject(list));
        }
        if (list instanceof com.github.pagehelper.Page) {
            com.github.pagehelper.Page<T> pageResult = CommonUtil.caseObject(list);
            page.setTotal((int) pageResult.getTotal());
            page.setPageNo(pageResult.getPageNum());
            page.setPageSize(pageResult.getPageSize());
        }
        return page;
    }

    @GetMapping("/download")
    public void download(String novelName, String id, HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            System.out.println(headerName + ":" + request.getHeader(headerName));
        }
        NovelExample novelExample = new NovelExample();
        NovelExample.Criteria c1 = novelExample.createCriteria();
        c1.andDeletedFlagEqualTo(YesOrNo.no.toString());
        String msg = "";
        if (!CommonUtil.isBlank(novelName)) {
            c1.andNameEqualTo(novelName);
            msg += "书籍名称:[" + novelName + "] ";
        }
        if (!CommonUtil.isBlank(id)) {
            c1.andIdEqualTo(id);
            msg += "书籍ID:[" + id + "] ";
        }
        List<Novel> novels = novelMapper.selectByExample(novelExample);
        if (novels.isEmpty()) {
            response.getWriter().print("小说没有同步, 或无法找到, " + msg);
            return;
        }
        Novel novel = novels.get(0);
        ChapterExample chapterExample = new ChapterExample();
        ChapterExample.Criteria c2 = chapterExample.createCriteria();
        c2.andNovelIdEqualTo(novel.getId());
        c2.andDeletedFlagEqualTo(YesOrNo.no.toString());
        List<Chapter> chapters = chapterMapper.selectByExampleWithBLOBs(chapterExample);
        if (chapters.isEmpty()) {
            response.getWriter().print("小说没有同步, " + msg);
            return;
        }
        setDownloadResponse(response, novel.getName() + "_" + novel.getAuthor() + ".txt", -1);
        response.setContentType("application/os-stream;charset=UTF-8");
        ServletOutputStream os = response.getOutputStream();
        for (Chapter chapter : chapters) {
            String title = chapter.getTitle();
            if (title != null) {
                if (!title.matches(".*第.+章.*")) { // 如果标题没有第N章, 这里自动添加 第N章
                    title = "第" + ChineseNumberUtil.format(chapter.getSerialNumber(), false) + "章 " + title;
                }
                os.write(title.getBytes(Constants.DEFAULT_CHARSET));
                os.write(enterByte);
            }
            if (chapter.getContent() != null) {
                os.write(chapter.getContent().getBytes(Constants.DEFAULT_CHARSET));
                os.write(enterByte);
            }
        }
        os.flush();
    }

    /**
     * set download file response header
     * 
     * @param response response
     * @param fileName fileName
     * @param contentLength contentLength
     * @author yulong.wang@Aqier.com
     * @since 2020-01-16 21:25:51
     */
    public static void setDownloadResponse(HttpServletResponse response, String fileName, long contentLength) {
        response.reset();
        if (contentLength >= 0) {
            response.setContentLengthLong(contentLength);
        }
        String contentDisposition = ContentDisposition.builder("attachment").filename(fileName, StandardCharsets.UTF_8)
            .build().toString();
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
    }

    private byte[] enterByte = "\n".getBytes(Constants.DEFAULT_CHARSET);
}
