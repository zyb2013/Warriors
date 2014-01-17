package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.type.IndexName;

/**
 * 角色升级详细信息对象
 * 
 * @author Hyint
 */
@Resource
public class RoleUpgradeConfig {

	@Id
	private int id;
	
	/** 角色的职业 */
	@Index(name = IndexName.USER_JOB_LEVEL, order = 0)
	private int job;
	
	/** 角色的等级 */
	@Index(name = IndexName.USER_JOB_LEVEL, order = 1)
	private int level;

	/** 角色升级需要的经验 */
	private long exp;

	/** 角色在当前等级的力量值 */
	private int strength;

	/** 角色在当前等级的敏捷值 */
	private int dexerity;

	/** 角色在当前等级的智力值 */
	private int intellect;
	
	/** 角色在当前等级的体力值 */
	private int constitution;
	
	/** 角色在当前等级的精神值 */
	private int spirituality;

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

	public int getJob() {
		return job;
	}

	public void setJob(int job) {
		this.job = job;
	}

	public long getExp() {
		return exp;
	}

	public void setExp(long exp) {
		this.exp = exp;
	}

	public int getStrength() {
		return strength;
	}

	public void setStrength(int strength) {
		this.strength = strength;
	}

	public int getDexerity() {
		return dexerity;
	}

	public void setDexerity(int dexerity) {
		this.dexerity = dexerity;
	}

	public int getIntellect() {
		return intellect;
	}

	public void setIntellect(int intellect) {
		this.intellect = intellect;
	}

	public int getConstitution() {
		return constitution;
	}

	public void setConstitution(int constitution) {
		this.constitution = constitution;
	}

	public int getSpirituality() {
		return spirituality;
	}

	public void setSpirituality(int spirituality) {
		this.spirituality = spirituality;
	}

	@Override
	public String toString() {
		return "RoleUpgradeConfig [id=" + id + ", level=" + level + ", job=" + job + ", exp=" + exp
				+ ", strength=" + strength + ", dexerity=" + dexerity + ", intellect=" + intellect
				+ ", constitution=" + constitution + ", spirituality=" + spirituality + "]";
	}
}
