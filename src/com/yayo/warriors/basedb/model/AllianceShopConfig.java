package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;

/**
 * 帮派商店配置表
 * @author liuyuhua
 */
@Resource
public class AllianceShopConfig {
	
	/**
	 * 序号
	 */
	@Id
	private int id;
	
	/** 所需要帮派商店的等级*/
	private int level;
	
	/** 兑换道具ID*/
	private int propsId;
	
	/** 所需要的贡献值*/
	private int donate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getPropsId() {
		return propsId;
	}

	public void setPropsId(int propsId) {
		this.propsId = propsId;
	}

	public int getDonate() {
		return donate;
	}

	public void setDonate(int donate) {
		this.donate = donate;
	}
	

}
