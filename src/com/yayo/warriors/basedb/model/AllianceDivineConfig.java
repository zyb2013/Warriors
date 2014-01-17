package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;

/**
 * 帮派占卜配置表
 * @author liuyuhua
 */
@Resource
public class AllianceDivineConfig {
	
	@Id
	private int id;
	
	/** 道具ID*/
	private int propsId;
	
	/** 几率区间*/
	private int rate;
	
	/** 满值区间*/
	private int fullRate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPropsId() {
		return propsId;
	}

	public void setPropsId(int propsId) {
		this.propsId = propsId;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public int getFullRate() {
		return fullRate;
	}

	public void setFullRate(int fullRate) {
		this.fullRate = fullRate;
	}
	

}
