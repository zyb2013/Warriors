package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.type.IndexName;

/**
 * 洗练类型配置
 * 
 * @author Hyint
 */
@Resource
public class WashTypeConfig implements Comparable<WashTypeConfig>{
	
	/** 附加属性ID */
	@Id
	private int id;
	
	/** 附加属性生成概率 */
	private int rate;

	/** 附加属性值 */
	@Index(name=IndexName.EQUIP_WASH_ADDITION, order=0)
	private int addition;
	
	/** 最大的概率上线 */
	private int maxRate = 0;
	
	/** 附加的角色属性类型 */
	private int attribute = 0;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public int getAddition() {
		return addition;
	}

	public void setAddition(int addition) {
		this.addition = addition;
	}

	public int getMaxRate() {
		return maxRate;
	}

	public void setMaxRate(int maxRate) {
		this.maxRate = maxRate;
	}

	public int getAttribute() {
		return attribute;
	}

	public void setAttribute(int attribute) {
		this.attribute = attribute;
	}

	
	public String toString() {
		return "WashTypeConfig [id=" + id + ", rate=" + rate + ", addition=" + addition + ", maxRate=" + maxRate + ", attribute=" + attribute + "]";
	}

	
	public int compareTo(WashTypeConfig o) {
		return o == null || this.id < o.getId() ? -1 : (this.id == o.getId() ? 0 : 1);
	}
	
	
}
