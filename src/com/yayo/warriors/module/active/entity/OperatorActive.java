package com.yayo.warriors.module.active.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;


import com.yayo.common.db.model.BaseModel;
import com.yayo.warriors.basedb.model.ActiveOperatorConfig;
import com.yayo.warriors.module.active.vo.OperatorActiveVo;


/**
 * 运营活动
 * @author liuyuhua
 */
@Entity
@Table(name = "operatorActive")
public class OperatorActive extends BaseModel<Long> implements Comparable<OperatorActive>{
	private static final long serialVersionUID = -1629875183856441269L;
	
	/** 活动ID,由管理后台提供*/
	@Id
	@Column(name="activeId")
	private Long id;
	
	/** 运营活动基础ID 参见:{@link ActiveOperatorConfig#getId()}*/
	private int activeBaseId;
	
	/** 活动标题*/
	@Lob
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
	@Lob
	private String description = "";
	
	/** 运营活动表*/
	@Transient
	private transient ActiveOperatorConfig operatorConfig = null;
	
	/**
	 * 构造函数
	 * @param vo                       运营活动Vo对象
	 * @param config                   运营活动配置
	 * @return {@link OperatorActive}  运营活动
	 */
	public static OperatorActive valueOf(OperatorActiveVo vo,ActiveOperatorConfig config){
		OperatorActive active = new OperatorActive();
		active.id = vo.getId();
		active.activeBaseId = vo.getActiveBaseId();
		active.title = vo.getTitle();
		active.sort = vo.getSort();
		active.startTime = vo.getStartTime();
		active.endTime = vo.getEndTime();
		active.lostTime = vo.getLostTime();
		active.opened = vo.isOpened();
		active.showed = vo.isShowed();
		active.description = vo.getDescription();
		active.operatorConfig = config;
		return active;
	}
	
	
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

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
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

	public ActiveOperatorConfig getOperatorConfig() {
		return operatorConfig;
	}

	public void setOperatorConfig(ActiveOperatorConfig operatorConfig) {
		this.operatorConfig = operatorConfig;
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
	

	
	public String toString() {
		return "OperatorActive [id=" + id + ", activeBaseId=" + activeBaseId
				+ ", title=" + title + ", sort=" + sort + ", startTime="
				+ startTime + ", endTime=" + endTime + ", lostTime=" + lostTime
				+ ", opened=" + opened + ", showed=" + showed
				+ ", description=" + description + "]";
	}


	
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		OperatorActive other = (OperatorActive) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	
	public int compareTo(OperatorActive o) {
		if(o == null) {
			return -1;
		}
		
		Integer sort = o.getSort();
		return sort.compareTo(this.sort);
	}

}
