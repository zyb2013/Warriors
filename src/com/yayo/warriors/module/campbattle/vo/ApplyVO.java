package com.yayo.warriors.module.campbattle.vo;

import java.io.Serializable;

import com.yayo.warriors.module.user.type.Job;

/**
 * 申请VO
 * @author jonsai
 *
 */
public class ApplyVO implements Serializable {
	private static final long serialVersionUID = -3062992452208023614L;

	/** 角色id */
	private long playerId;
	
	/** 角色名 */
	private String playerName;
	
	/** 等级 */
	private int level;
	
	/** 职业 */
	private Job job;
	
	/** 战斗力 */
	private int fightCapacity;
	
	/** 联盟名 */
	private String allianceName;

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public int getFightCapacity() {
		return fightCapacity;
	}

	public void setFightCapacity(int fightCapacity) {
		this.fightCapacity = fightCapacity;
	}

	public String getAllianceName() {
		return allianceName;
	}

	public void setAllianceName(String allianceName) {
		this.allianceName = allianceName;
	}
	
}
