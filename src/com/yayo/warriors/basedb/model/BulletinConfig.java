package com.yayo.warriors.basedb.model;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Splitable;

/**
 * 公告基础表
 * 
 * @author huachaoping
 */
@Resource
public class BulletinConfig {
	
	/** 公告ID */
	@Id
	private int id;
	
	/** 公告类型 */
	private int noticeType;
	
	/** 优先级 */
	private int priority;
	
	/** 公告的条件 */
	private String condition; 
	
	/** 发公告所需条件 */
	@JsonIgnore
	private Set<Integer> conditionSet = new HashSet<Integer>();
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getNoticeType() {
		return noticeType;
	}

	public void setNoticeType(int noticeType) {
		this.noticeType = noticeType;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public Set<Integer> getConditions() {
		if (!this.conditionSet.isEmpty()) {
			return this.conditionSet;
		}
		if (this.condition != null) {
			String[] array = this.condition.split(Splitable.ATTRIBUTE_SPLIT);
			for (String element : array) {
				this.conditionSet.add(Integer.valueOf(element));
			}
		}
		return this.conditionSet;
	}

}
