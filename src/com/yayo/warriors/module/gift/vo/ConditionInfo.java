package com.yayo.warriors.module.gift.vo;

import java.io.Serializable;

import com.yayo.warriors.module.mail.model.ConditionType;

/**
 * 条件信息
 * 
 * @author huachaoping
 */
public class ConditionInfo implements Serializable {
	
	private static final long serialVersionUID = -5964727098050116288L;

	/** 条件分类: 如等级条件,　时间条件... */
	private String type;
	
	/** 限制: 如大于, 等于, 小于.. {@link ConditionType} */
	private String limit;
	
	/** 值 */
	private String value;

	
	public static ConditionInfo valueOf(String type, String limit, String value) {
		ConditionInfo cInfo = new ConditionInfo();
		cInfo.type = type;
		cInfo.limit = limit;
		cInfo.value = value;
		return cInfo;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLimit() {
		return limit;
	}

	public void setLimit(String limit) {
		this.limit = limit;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
}
