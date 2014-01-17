package com.yayo.warriors.module.active.vo;

import java.io.Serializable;

import com.yayo.warriors.basedb.model.ActiveOperatorConfig;

/**
 * 运营活动VO
 * @author liuyuhua
 */
public class OperatorActiveVo implements Serializable {
	private static final long serialVersionUID = -5407257129785458590L;
	
	/** 增量ID*/
	private Long id;
	
	/** 运营活动基础ID 参见:{@link ActiveOperatorConfig#getId()}*/
	private int activeBaseId;
	
	/** 活动标题*/
	private String title = "";
	
	/** 排序*/
	private Integer sort;
	
	/** 活动开启时间*/
	private long startTime;
	
	/** 活动结束时间*/
	private long endTime;
	
	/** 活动失效时间(活动下架时间)*/
	private long lostTime;
	
	/** 是否已经开启(优先所有时间的判断)*/
	private boolean opened = false;
	
	/** 是否显示(客户端用)*/
	private boolean showed = false;
	
	/** 说明*/
	private String description = "";
	
	//Getter and Setter...

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getActiveBaseId() {
		return activeBaseId;
	}

	public void setActiveBaseId(int activeBaseId) {
		this.activeBaseId = activeBaseId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getLostTime() {
		return lostTime;
	}

	public void setLostTime(long lostTime) {
		this.lostTime = lostTime;
	}

	public boolean isOpened() {
		return opened;
	}

	public void setOpened(boolean opened) {
		this.opened = opened;
	}

	public boolean isShowed() {
		return showed;
	}

	public void setShowed(boolean showed) {
		this.showed = showed;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "OperatorActiveVo [id=" + id + ", activeBaseId=" + activeBaseId
				+ ", title=" + title + ", sort=" + sort + ", startTime="
				+ startTime + ", endTime=" + endTime + ", lostTime=" + lostTime
				+ ", opened=" + opened + ", showed=" + showed
				+ ", description=" + description + "]";
	}

}
