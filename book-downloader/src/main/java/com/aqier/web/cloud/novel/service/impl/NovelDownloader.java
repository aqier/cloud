/*
 * Domain Aqier.com Reserve Copyright
 * 
 * @author yuloang.wang@Aqier.com
 * 
 * @since 2017年8月24日
 */
package com.aqier.web.cloud.novel.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.aqier.web.cloud.core.constants.Constants;
import com.aqier.web.cloud.core.constants.YesOrNo;
import com.aqier.web.cloud.core.utils.ChineseNumberUtil;
import com.aqier.web.cloud.core.utils.CommonUtil;
import com.aqier.web.cloud.novel.dao.mapper.ChapterMapper;
import com.aqier.web.cloud.novel.dao.mapper.NovelMapper;
import com.aqier.web.cloud.novel.dto.ChapterDTO;
import com.aqier.web.cloud.novel.dto.NovelDTO;
import com.aqier.web.cloud.novel.dto.model.Chapter;
import com.aqier.web.cloud.novel.dto.model.ChapterExample;
import com.aqier.web.cloud.novel.dto.model.ChapterExample.Criteria;
import com.aqier.web.cloud.novel.dto.model.Novel;
import com.aqier.web.cloud.novel.dto.model.NovelExample;
import com.aqier.web.cloud.novel.service.INovelDownloader;

/**
 * 
 * @author yulong.wang@Aqier.com
 * @since 2018年3月13日
 */
public class NovelDownloader implements INovelDownloader {

    protected RestTemplate restTemplate = new RestTemplate();

    protected HttpHeaders headers;

    protected HttpEntity<String> request;

    protected long successSleepTime = 400;

    protected long failSleepTime = 1000;

    /** 保存书籍的目录 **/
    protected String novelDir = "/books";

    protected String searchUrl;

    protected Boolean isScaning = false;

    @Autowired
    protected NovelMapper novelMapper;

    @Autowired
    protected ChapterMapper chapterMapper;

    public NovelDownloader() {
        headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, "text/html");
        headers.add(HttpHeaders.USER_AGENT,
            "User-Agent:Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36");
        request = new HttpEntity<>(headers);
    }

    /**
     * 获取章节
     * 
     * @param url 所搜地址
     * @param novelName 小说名称
     * @param save 是否保存, 保存路径参考 {@link #novelDir}
     * @return
     * @author yulong.wang@aqier.com
     * @since 2018年3月13日
     */
    protected Map<NovelDTO, List<ChapterDTO>> getChapters(String url, String novelName, boolean save) {
        List<NovelDTO> catalogAndAuthors = getCatalogAndAuthor(url, novelName);
        if (catalogAndAuthors.isEmpty()) {
            info("无法获取到[" + novelName + "]的章节目录");
            return Collections.emptyMap();
        }
        Map<NovelDTO, List<ChapterDTO>> noveMap = new HashMap<>();
        for (NovelDTO catalogAndAuthor : catalogAndAuthors) {
            String filePath = null;
            if (save) {
                filePath = novelDir + File.separator + novelName + ".txt";
            }
            OutputStream os = null;
            try {
                os = new FileOutputStream(filePath);
                List<ChapterDTO> chapters = getChaptersByCatalog(catalogAndAuthor.getCatalogUrl(), os, true);
                if (chapters.isEmpty()) {
                    info("没有获取到任何章节内容");
                }
                noveMap.put(catalogAndAuthor, chapters);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                CommonUtil.tryClose(os);
            }
        }
        return noveMap;
    }

    /**
     * 获取章节目录
     * 
     * @param url 网站地址
     * @param novelName 小说名称
     * @return
     * @author yulong.wang@Aqier.com
     * @since 2018年3月13日
     */
    protected List<NovelDTO> getCatalogAndAuthor(String url, String novelName) {
        url += novelName;
        String pageContent = loadPageContent(url);
        // 获取该页面的所有超链接
        List<String> links = pairMatch("<a ", "</a>", true, pageContent);
        List<NovelDTO> noveDtos = new ArrayList<>();
        for (String linkText : links) {
            String result = getLinkHref(url, linkText, ".+>" + novelName + "<.+");
            if (result != null) {
                info("获取到[" + novelName + "]的章节目录地址为:" + result);
                NovelDTO novelDTO = new NovelDTO();
                novelDTO.setCatalogUrl(result);
                novelDTO.setAuthor(getAuthor(pageContent, linkText, novelName));
                noveDtos.add(novelDTO);
            }
        }
        return noveDtos;
    }

    /**
     * @param pageContent
     * @return
     * @author yulong.wang@aqier.com
     * @param novelName
     * @param linkText
     * @since 2018年3月28日
     */
    protected String getAuthor(String pageContent, String linkText, String novelName) {
        return "佚名";
    }

    /**
     * 根据章节目录获取章节内容
     * 
     * @param catalogUrl
     * @param os
     * @param getContent 是否获取章节内容
     * @return
     * @author yulong.wang@aqier.com
     * @since 2018年3月13日
     */
    protected List<ChapterDTO> getChaptersByCatalog(String catalogUrl, OutputStream os, boolean getContent) {
        // 章节目录内容
        List<ChapterDTO> chapters = new ArrayList<>();
        try {
            List<String> links = getCatalogLinks(catalogUrl);
            for (String link : links) {
                // 第 n 章内容链接
                String chapterContentLink = getChapterContentUrl(catalogUrl, link);
                if (chapterContentLink != null) {
                    ChapterDTO chapter = new ChapterDTO();
                    chapter.setLink(link);
                    chapter.setContentUrl(chapterContentLink);
                    setChapterName(chapter);
                    if (getContent) {
                        setChapterContent(chapter);
                        writeChapter(chapter, os);
                    }
                    chapters.add(chapter);
                }
            }
            // 过滤掉相同章节
            links.clear();
            chapters = chapters.stream().filter((e) -> {
                if (links.contains(e.getContentUrl())) {
                    return false;
                } else {
                    links.add(e.getContentUrl());
                }
                return true;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return chapters;
    }

    /**
     * 解析章节内容的URL
     * 
     * @param catalogUrl
     * @param link
     * @return
     * @author yulong.wang@aqier.com
     * @since 2018年3月15日
     */
    protected String getChapterContentUrl(String catalogUrl, String link) {
        String chapterContentLink = getLinkHref(catalogUrl, link, ".+>.*第.+章.+<.+");
        return chapterContentLink;
    }

    /**
     * 获取章节列表
     * 
     * @param catalogUrl
     * @return
     * @author yulong.wang@aqier.com
     * @since 2018年3月13日
     */
    protected List<String> getCatalogLinks(String catalogUrl) {
        String pageContent = loadPageContent(catalogUrl);
        List<String> links = pairMatch("<a", "</a>", true, pageContent);
        return links;
    }

    /**
     * 写入章节到文件
     * 
     * @param chapter
     * @param os
     * @author yulong.wang@aqier.com
     * @throws IOException
     * @since 2018年3月13日
     */
    protected void writeChapter(ChapterDTO chapter, OutputStream os) throws IOException {
        String text = chapter.getTitle() + "\n" + chapter.getContent() + "\n\n";
        os.write(text.getBytes());
        os.flush();
    }

    /**
     * @param chapter
     * @author yulong.wang@aqier.com
     * @since 2018年3月13日
     */
    protected void setChapterName(ChapterDTO chapter) {
        String name = StringUtils.substringBetween(chapter.getLink(), ">", "<");
        int index = name.indexOf("章");
        if (index > 0) {
            // 有些是大写的章节名称, 导致APP无法正确解析章节目录
            name = ChineseNumberUtil.toLowerCase(name.substring(0, index)) + name.substring(index);
        }
        chapter.setTitle(name);
    }

    /**
     * @param chapter
     * @return
     * @author yulong.wang@aqier.com
     * @since 2018年3月13日
     */
    protected void setChapterContent(ChapterDTO chapter) {
        String contentUrl = chapter.getContentUrl();
        String htmlContent = loadPageContent(contentUrl);
        htmlContent = getChapterContentHtml(htmlContent);
        htmlContent = trimHtml(htmlContent);
        String content = getText(htmlContent);
        chapter.setContent(content);
        info("找到章节:" + chapter.getTitle() + " [" + chapter.getContentUrl() + "], 总字数:" + chapter.getContent().length());
    }

    /**
     * 通过HTML获取章节内容所在的HTML
     * 
     * @param html
     * @return 章节所在HTML标签
     * @author yulong.wang@aqier.com
     * @since 2018年3月13日
     */
    protected String getChapterContentHtml(String html) {
        return html;
    }

    /**
     * @param url
     * @param result
     * @return
     * @author yulong.wang@Aqier.com
     * @since 2018年3月13日
     */
    public String getAbstractUrl(String rootUrl, String relativeUrl) {
        if (!relativeUrl.toLowerCase().matches("^https?://.*$")) { // 相对路径
            if (rootUrl.endsWith("/")) {
                rootUrl = rootUrl.substring(0, rootUrl.length() - 1);
            }
            String parentUrl = rootUrl.substring(0, rootUrl.lastIndexOf("/"));
            relativeUrl = parentUrl + relativeUrl;
        }
        return relativeUrl;
    }

    /**
     * 获取内容为contentRegex的链接, 如果内容不匹配将返回null
     * 
     * @param parentUrl 针对相对路径用
     * @param linkText 如 &lt;a href="baidu.com">百度&lt;/a>
     * @param contentRegex ^百度.*$
     * @return baidu.com
     * @author yulong.wang@Aqier.com
     * @since 2018年3月13日
     */
    public String getLinkHref(String parentUrl, String linkText, String contentRegex) {
        if (linkText.matches(contentRegex)) { // <a href='/wapbook-111-75040/'>第二章 撞破南墙不回头<span></span></a>
            List<String> result = getHtmlAttr(linkText, "a", "href");
            if (!result.isEmpty()) {
                String url = result.get(0);
                return getAbstractUrl(parentUrl, url);
            }
        }
        return null;
    }

    /**
     * 请求网址, 直到成功
     * 
     * @param restTemplate
     * @param entry
     * @param url
     * @return
     * @author yulong.wang@Aqier.com
     * @since 2018年2月1日
     */
    public String loadPageContent(String url) {
        ResponseEntity<byte[]> r = null;
        Charset charset = Constants.DEFAULT_CHARSET;
        HttpStatus hs = HttpStatus.NOT_FOUND;
        int n = 10;
        while (!hs.is2xxSuccessful()) {
            if (n < 0) {
                System.err.println("retry 10 times andcan not load page content: " + url);
                return "";
            }
            try {
                r = restTemplate.exchange(url, getHttpMethod(url), request, byte[].class);
                hs = r.getStatusCode();
                MediaType contentType = r.getHeaders().getContentType();
                Charset encode = contentType == null ? null : contentType.getCharset();
                if (encode != null) {
                    charset = encode;
                }
                try {
                    Thread.sleep(successSleepTime);
                } catch (InterruptedException e1) {
                }
            } catch (RestClientException e) {
                System.err.println(url + ": " + e.getMessage());
                try {
                    Thread.sleep(failSleepTime);
                } catch (InterruptedException e1) {
                }
            }
            n--;
        }
        byte[] body = r.getBody();
        if (body == null) {
            System.err.println("can not load page content: " + url);
            return "";
        }
        return new String(body, charset);
    }

    /**
     * 调整HTML中的换行(&lt;br>)为java中的换行(\n)
     * 
     * @param html
     * @return
     * @author yulong.wang@aqier.com
     * @since 2018年3月13日
     */
    public String trimHtml(String html) {
        html = html.replace("<p>", "\n\t").replace("<br>", "\n").replace("<br/>", "\n");
        return html;
    }

    /**
     * 获取html内容
     * 
     * @param html
     * @return
     * @author yulong.wang@aqier.com
     * @since 2018年3月13日
     */
    public final String getText(String html) {
        String txtcontent = html.replaceAll("</?[^>]+>", ""); // 剔出<html>的标签
        txtcontent = txtcontent.replaceAll("<a>\\s*|\t|\r|\n</a>", "");// 去除字符串中的空格,回车,换行符,制表符
        return txtcontent;
    }

    /**
     * 获取请求方式, 默认全部是GET方式
     * 
     * @param url
     * @return
     * @author yulong.wang@Aqier.com
     * @since 2018年3月13日
     */
    public HttpMethod getHttpMethod(String url) {
        return HttpMethod.GET;
    }

    /**
     * @param left
     * @param right
     * @param inner
     * @param text
     * @return
     * @author yulong.wang@Aqier.com
     * @since 2018年2月1日
     */
    public final List<String> pairMatch(String left, String right, boolean inner, String text) {
        if (text == null) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        int fromIndex = 0;
        int leftIndex = -1;
        int rightIndex = -1;
        leftIndex = text.indexOf(left, fromIndex);
        if (leftIndex >= 0) {
            rightIndex = text.indexOf(right, leftIndex);
            fromIndex = rightIndex + right.length();
        }
        while (rightIndex >= 0) {
            result.add(text.substring(leftIndex, fromIndex));
            leftIndex = text.indexOf(left, fromIndex);
            if (leftIndex >= 0) {
                rightIndex = text.indexOf(right, leftIndex);
                fromIndex = rightIndex + right.length();
                ;
            } else {
                rightIndex = -1;
            }
        }
        return result;
    }

    /**
     * 获取指定HTML标签的指定属性的值
     * 
     * @param source 要匹配的源文本
     * @param element 标签名称
     * @param attr 标签的属性名称
     * @return 属性值列表
     */
    public final List<String> getHtmlAttr(String source, String element, String attr) {
        List<String> result = new ArrayList<String>();
        String reg = "<" + element + "[^<>]*?\\s" + attr + "=['\"]?(.*?)['\"]?(\\s.*?)?>";
        Matcher m = Pattern.compile(reg).matcher(source);
        while (m.find()) {
            String r = m.group(1);
            result.add(r);
        }
        return result;
    }

    /**
     * @param text
     * @author yulong.wang@Aqier.com
     * @since 2018年3月13日
     */
    public void info(String text) {
        System.out.println(text);
    }

    /**
     * 
     * @param novelName
     * @return
     * @author yulong.wang@Aqier.com
     * @since 2018年3月28日
     */
    @Override
    public List<NovelDTO> searchNovel(String novelName, boolean update) {
        NovelExample example = new NovelExample();
        NovelExample.Criteria c1 = example.createCriteria();
        c1.andNameEqualTo(novelName);
        c1.andDeletedFlagEqualTo(YesOrNo.no.toString());
        List<Novel> novels = novelMapper.selectByExample(example);
        List<NovelDTO> novelDtos = new ArrayList<>();
        if (novels.isEmpty()) {
            List<NovelDTO> catalogAndAuthors = getCatalogAndAuthor(searchUrl, novelName);
            if (!catalogAndAuthors.isEmpty()) {
                for (NovelDTO catalogAndAuthor : catalogAndAuthors) {
                    List<ChapterDTO> chapters = getChaptersByCatalog(catalogAndAuthor.getCatalogUrl(), null, false);
                    if (!CommonUtil.isBlank(chapters)) {
                        NovelDTO novelDTO = new NovelDTO();
                        novelDTO.setName(novelName);
                        novelDTO.setCatalogUrl(catalogAndAuthor.getCatalogUrl());
                        novelDTO.setChapterNumber(chapters.size());
                        novelDTO.setAuthor(catalogAndAuthor.getAuthor());
                        novelDTO.setDownloadChapterNum(0);
                        CommonUtil.initInsertParam(novelDTO);
                        novelMapper.insert(novelDTO);
                        novelDtos.add(novelDTO);
                    }
                }
            }
        } else {
            NovelDTO novelDTO = new NovelDTO();
            BeanUtils.copyProperties(novels.get(0), novelDTO);
            ChapterExample chapterExample = new ChapterExample();
            chapterExample.createCriteria().andNovelIdEqualTo(novelDTO.getId());
            int count = (int) chapterMapper.countByExample(chapterExample);
            novelDTO.setDownloadChapterNum(count);
            if (update) {
                List<NovelDTO> catalogAndAuthors = getCatalogAndAuthor(searchUrl, novelName);
                if (!catalogAndAuthors.isEmpty()) {
                    for (NovelDTO catalogAndAuthor : catalogAndAuthors) {
                        List<ChapterDTO> chapters = getChaptersByCatalog(catalogAndAuthor.getCatalogUrl(), null, false);
                        if (!CommonUtil.isBlank(chapters)) {
                            novelDTO.setName(novelName);
                            novelDTO.setCatalogUrl(catalogAndAuthor.getCatalogUrl());
                            novelDTO.setChapterNumber(chapters.size());
                            novelDTO.setAuthor(catalogAndAuthor.getAuthor());
                            novelDTO.setDownloadChapterNum(count);
                            CommonUtil.initUpdateParam(novelDTO);
                            novelMapper.updateByPrimaryKeySelective(novelDTO);
                            novelDtos.add(novelDTO);
                        }
                    }
                }
            }
        }
        return novelDtos;
    }

    /*
     * @author yulong.wang@Aqier.com
     * 
     * @since 2018年3月23日
     */
    @Override
    public List<Chapter> searchChapter(Novel novelVO) {
        if (novelVO == null) {
            return null;
        }
        ChapterExample chapterExample = new ChapterExample();
        Criteria c2 = chapterExample.createCriteria();
        c2.andNovelIdEqualTo(novelVO.getId());
        c2.andDeletedFlagEqualTo(YesOrNo.no.toString());
        List<Chapter> chapters = chapterMapper.selectByExample(chapterExample);
        if (chapters.size() < novelVO.getChapterNumber()) {
            List<Integer> numbers = chapters.stream().map(Chapter::getSerialNumber).collect(Collectors.toList());
            String catalogUrl = novelVO.getCatalogUrl();
            List<ChapterDTO> chapterDTOs = getChaptersByCatalog(catalogUrl, null, false);
            for (int i = 0; i < chapterDTOs.size(); i++) {
                if (!numbers.contains(i + 1)) { // 没有该章节
                    ChapterDTO chapter = chapterDTOs.get(i);
                    setChapterContent(chapter);
                    chapter.setNovelId(novelVO.getId());
                    chapter.setSerialNumber(i + 1);
                    CommonUtil.initInsertParam(chapter);
                    chapterMapper.insert(chapter);
                    chapters.add(chapter);
                }
            }
            info("书籍[" + novelVO.getName() + "]总计" + chapterDTOs.size() + "个章节同步完成");
        } else {
            info("书籍[" + novelVO.getName() + "]总计" + chapters.size() + "个章节已经存在");
        }
        return chapters;
    }

    /*
     * @author yulong.wang@Aqier.com
     * 
     * @since 2018年3月23日
     */
    @Override
    public void download(String novel, OutputStream os) {
        File novelFile = new File(novelDir + "/" + novel + ".txt");
        if (novelFile.isFile()) {

        }
        List<NovelDTO> catalogAndAuthors = getCatalogAndAuthor(searchUrl, novel);
        if (!catalogAndAuthors.isEmpty()) {
            for (NovelDTO catalogAndAuthor : catalogAndAuthors) {
                List<ChapterDTO> chapters = getChaptersByCatalog(catalogAndAuthor.getCatalogUrl(), null, false);
                if (!CommonUtil.isBlank(chapters)) {

                }
            }
        }
    }

    /*
     * @author yulong.wang@Aqier.com
     * 
     * @since 2018年3月28日
     */
    @Override
    public void scan() {
        if (!isScaning) {
            try {
                isScaning = true;
                info(getClass() + " [开始扫描] " + Thread.currentThread().getName());
                List<String> novelNames = scanNovelName();
                if (novelNames != null) {
                    for (String novelName : novelNames) {
                        List<NovelDTO> novelVOs = searchNovel(novelName, true);
                        for (NovelDTO novelDTO : novelVOs) {
                            searchChapter(novelDTO);
                        }
                    }
                }
                info(getClass() + " [结束扫描] " + Thread.currentThread().getName());
            } finally {
                isScaning = false;
            }
        } else {
            info(getClass() + " [已在扫描] " + Thread.currentThread().getName());
        }
    }

    /**
     * 扫描小说的名称
     * 
     * @return
     * @author yulong.wang@aqier.com
     * @since 2018年3月28日
     */
    protected List<String> scanNovelName() {
        return null;
    }
}
