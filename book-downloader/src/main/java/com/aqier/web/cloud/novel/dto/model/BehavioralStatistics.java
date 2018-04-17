package com.aqier.web.cloud.novel.dto.model;

import com.aqier.web.cloud.core.dto.model.BaseModel;

public class BehavioralStatistics extends BaseModel {

	private static final long serialVersionUID = 1L;

	private String source;

    private String target;

    private String data;

    private Integer useTime;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source == null ? null : source.trim();
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target == null ? null : target.trim();
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data == null ? null : data.trim();
    }

    public Integer getUseTime() {
        return useTime;
    }

    public void setUseTime(Integer useTime) {
        this.useTime = useTime;
    }
}