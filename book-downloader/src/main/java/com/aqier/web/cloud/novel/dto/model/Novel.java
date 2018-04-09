package com.aqier.web.cloud.novel.dto.model;

import com.aqier.web.cloud.core.dto.model.BaseModel;

public class Novel extends BaseModel {
	
	private static final long serialVersionUID = 1L;

	private String name;

    private String author;

    private String catalogUrl;

    private Integer chapterNumber;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author == null ? null : author.trim();
    }

    public String getCatalogUrl() {
        return catalogUrl;
    }

    public void setCatalogUrl(String catalogUrl) {
        this.catalogUrl = catalogUrl == null ? null : catalogUrl.trim();
    }

    public Integer getChapterNumber() {
        return chapterNumber;
    }

    public void setChapterNumber(Integer chapterNumber) {
        this.chapterNumber = chapterNumber;
    }

}