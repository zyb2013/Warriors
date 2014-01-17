package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.type.IndexName;

/**
 * 乱武战场采集任务配置
 * @author jonsai
 *
 */
@Resource
public class BattleCollectConfig {

	/** 序号 */
	@Id
	private int id;

	/** 阵营 */
	@Index(name = IndexName.BATTLE_FIELD_CAMP_COLLECT)
	private int camp;

	/** 采集物ID(寻路用) */
	private int npcId;
	
	/** 发布任务的npc */
	private int taskNpc;
	
	/** 采集物 */
	private int baseId;
	
	/** 单次采集最大数量 */
	private int num;

	/** 单次奖励荣誉 */
	private int honor;

	/** 采集最多次数 */
	private int maxCount;

	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCamp() {
		return camp;
	}

	public void setCamp(int camp) {
		this.camp = camp;
	}

	public int getNpcId() {
		return npcId;
	}

	public void setNpcId(int npcId) {
		this.npcId = npcId;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public int getHonor() {
		return honor;
	}

	public void setHonor(int honor) {
		this.honor = honor;
	}

	public int getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}

	public int getBaseId() {
		return baseId;
	}

	public void setBaseId(int baseId) {
		this.baseId = baseId;
	}

	public int getTaskNpc() {
		return taskNpc;
	}

	public void setTaskNpc(int taskNpc) {
		this.taskNpc = taskNpc;
	}
	
}
