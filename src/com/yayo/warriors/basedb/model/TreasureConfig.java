package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.type.IndexName;

/**
 * 藏宝基础表
 * @author jonsai
 *
 */
@Resource
public class TreasureConfig {
	/** id */
	@Id
	private int id;
	
	/** 奖励id(藏宝图道具效果值) */
	@Index(name = IndexName.TREASURE_REWARDID )
	private int rewardId;
	
	/** 奖励id */
	@Index(name = IndexName.TREASURE_REWARDID_QUALITY, order = 0)
	private int copyOfRewardId;
	
	/** 品质 */
	@Index(name = IndexName.TREASURE_REWARDID_QUALITY, order = 1)
	private int quality;
	
	/** 生成概率 */
	private int rate;
	
	/** 概率满值  */
	private int fullRate;
	
	/** 副本Id */
	private int dungeonBaseId;
	
	/** 最多可开箱子数量 */
	private int maxOpen;

	//----------------------------------------------
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRewardId() {
		return rewardId;
	}

	public void setRewardId(int rewardId) {
		this.rewardId = rewardId;
		this.copyOfRewardId = this.rewardId;
	}

	public int getDungeonBaseId() {
		return dungeonBaseId;
	}

	public void setDungeonBaseId(int dungeonBaseId) {
		this.dungeonBaseId = dungeonBaseId;
	}

	public int getMaxOpen() {
		return maxOpen;
	}

	public void setMaxOpen(int maxOpen) {
		this.maxOpen = maxOpen;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
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

	public int getCopyOfRewardId() {
		return copyOfRewardId;
	}

	public void setCopyOfRewardId(int copyOfRewardId) {
	}
	
}
