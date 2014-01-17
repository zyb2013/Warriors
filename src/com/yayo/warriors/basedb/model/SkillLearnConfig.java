package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.type.IndexName;

/**
 * 技能学习消耗信息
 * 
 * @author Hyint
 */
@Resource
public class SkillLearnConfig {

	/**
	 * 效果ID
	 */
	@Id
	private int id;

	/**
	 * 技能等级
	 */
	private int level;

	/**
	 * 所属的技能ID
	 */
	@Index(name=IndexName.SKILL_LEARN_SKILLID, order = 0)
	private int skillId;

	/**
	 * 升级到该等级需要的真气. 
	 * 
	 * </br><strong>娱乐一下：</strong></br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;董刚说:给角色定义成煤气, 角色修炼真气就是修炼煤气, 没有煤气就没办法炸技能
	 */
	private int gas;

	/**
	 * 学习限制(学习等级)
	 */
	private int restrict;

	/**
	 * 学习需要的银币
	 */
	private int silver;

	/**
	 * 需要的道具ID
	 */
	@Index(name = IndexName.SKILL_LEARN_ITEMID, order = 0, expression = "itemId > 0")
	private int itemId;

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

	public int getSkillId() {
		return skillId;
	}

	public void setSkillId(int skillId) {
		this.skillId = skillId;
	}

	public int getGas() {
		return gas;
	}

	public void setGas(int gas) {
		this.gas = gas;
	}

	public int getRestrict() {
		return restrict;
	}

	public void setRestrict(int restrict) {
		this.restrict = restrict;
	}

	public int getSilver() {
		return silver;
	}

	public void setSilver(int silver) {
		this.silver = silver;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public boolean isNeedItem() {
		return this.itemId > 0;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + level;
		result = prime * result + skillId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		
		SkillLearnConfig other = (SkillLearnConfig) obj;
		return level == other.level && skillId == other.skillId;
	}

	@Override
	public String toString() {
		return "SkillLearnConfig [id=" + id + ", level=" + level + ", skillId=" + skillId
				+ ", gas=" + gas + ", restrict=" + restrict + ", silver=" + silver + "]";
	}
	
	
}