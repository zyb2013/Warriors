package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.type.IndexName;

/**
 * 装备 (升阶/属性) 规则配置对象
 * 
 * @author Hyint
 */
@Resource
public class EquipRankRuleConfig implements Comparable<EquipRankRuleConfig> {

	/** 自增ID */
	@Id
	private int id;
	
	/** 当前星级/附加属性条数 */
	@Index(name=IndexName.EQUIP_RANKRULE_ATTRIBUTE, order = 0)
	private int attribute;
	
	/** 星级掉落 */
	private int star;
	
	/** 星级概率 */
	private int starRate;
	
	/** 最大概率 */
	private int maxRate;
	
	/** 属性条数 */
	private int addition;

	/** 属性概率 */
	private int additionRate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getAttribute() {
		return attribute;
	}

	public void setAttribute(int attribute) {
		this.attribute = attribute;
	}

	public int getStar() {
		return star;
	}

	public void setStar(int star) {
		this.star = star;
	}

	public int getStarRate() {
		return starRate;
	}

	public void setStarRate(int starRate) {
		this.starRate = starRate;
	}

	public int getMaxRate() {
		return maxRate;
	}

	public void setMaxRate(int maxRate) {
		this.maxRate = maxRate;
	}

	public int getAddition() {
		return addition;
	}

	public void setAddition(int addition) {
		this.addition = addition;
	}

	public int getAdditionRate() {
		return additionRate;
	}

	public void setAdditionRate(int additionRate) {
		this.additionRate = additionRate;
	}

	
	public int compareTo(EquipRankRuleConfig o) {
		return o == null || this.id < o.getId() ? -1 : (this.id == o.getId() ? 0 : 1);
	}
	
	
	public String toString() {
		return "EquipRankRuleConfig [id=" + id + ", attribute=" + attribute + ", star=" + star
				+ ", starRate=" + starRate + ", maxRate=" + maxRate + ", addition=" + addition
				+ ", additionRate=" + additionRate + "]";
	}
}
