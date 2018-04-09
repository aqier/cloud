package com.aqier.web.cloud.novel.dto.model;

import com.aqier.web.cloud.core.dto.model.BaseModel;

public class Chapter extends BaseModel {
	
	private static final long serialVersionUID = 1L;

	private String novelId;
	
	private Integer serialNumber;

    private String title;

    private String contentUrl;
    
    private String content;

    public Integer getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(Integer serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

	public String getContentUrl() {
		return contentUrl;
	}

	public void setContentUrl(String contentUrl) {
		this.contentUrl = contentUrl;
	}

	public String getNovelId() {
		return novelId;
	}

	public void setNovelId(String novelId) {
		this.novelId = novelId;
	}
    
}