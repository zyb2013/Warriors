package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;

/**
 * 装备升阶配置对象
 * 
 * @author Hyint
 */
@Resource
public class EquipRankConfig {
	
	/** 升阶需要的装备ID */
	@Id
	private int equipId;
	
	/** 成功的概率 */
	private int rate;
	
	/** 最大概率值 */
	private int maxRate;

	/** 需要消耗的道具ID */
	private int itemId;
	
	/** 圣洁需要消耗的道具数量 */
	private int itemCount;
	
	/** 升阶需要的游戏币系数 */ 
	private double silver;
	
	/** 升阶成功后的装备ID */
	private int nextEquipId;
	
	/** 下一阶构建用户装备信息 */
	private EquipConfig nextEquip;
	
	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public int getEquipId() {
		return equipId;
	}

	public void setEquipId(int equipId) {
		this.equipId = equipId;
	}

	public double getSilver() {
		return silver;
	}

	public void setSilver(double silver) {
		this.silver = silver;
	}

	public int getMaxRate() {
		return maxRate;
	}

	public void setMaxRate(int maxRate) {
		this.maxRate = maxRate;
	}

	public int getNextEquipId() {
		return nextEquipId;
	}

	public void setNextEquipId(int nextEquipId) {
		this.nextEquipId = nextEquipId;
	}

	public int getItemId() {
		return itemId;
	}
	
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	
	public int getItemCount() {
		return itemCount;
	}

	public void setItemCount(int itemCount) {
		this.itemCount = itemCount;
	}

	public EquipConfig getNextEquip() {
		return nextEquip;
	}

	public void setNextEquip(EquipConfig nextEquip) {
		this.nextEquip = nextEquip;
	}

	public boolean isAscentRankSuccess(int luckyCount) {
		return  Tools.getRandomInteger(this.maxRate) < this.rate + luckyCount * 30 ;
	}

	@Override
	public String toString() {
		return "EquipRankConfig [equipId=" + equipId + ", rate=" + rate + ", maxRate=" + maxRate
				+ ", itemId=" + itemId + ", itemCount=" + itemCount + ", silver=" + silver
				+ ", nextEquipId=" + nextEquipId + "]";
	}
}