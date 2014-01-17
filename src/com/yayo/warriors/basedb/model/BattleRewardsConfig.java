package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;

/**
 * 乱武战场奖励配置
 * @author jonsai
 *
 */
@Resource
public class BattleRewardsConfig {
	/** 序号 */
	@Id
	private int id;

	/** 最低荣誉 */
	private int minHonor;
	
	/** 最高荣誉 */
	private int maxHonor;

	/** 经验奖励 */
	private String exp;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getMinHonor() {
		return minHonor;
	}

	public void setMinHonor(int minHonor) {
		this.minHonor = minHonor;
	}

	public int getMaxHonor() {
		return maxHonor;
	}

	public void setMaxHonor(int maxHonor) {
		this.maxHonor = maxHonor;
	}

	public String getExp() {
		return exp;
	}

	public void setExp(String exp) {
		this.exp = exp;
	}

}
