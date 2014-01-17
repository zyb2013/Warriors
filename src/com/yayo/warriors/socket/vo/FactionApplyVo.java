package com.yayo.warriors.socket.vo;

import java.io.Serializable;

import com.yayo.warriors.module.user.type.Job;

/**
 * 帮派申请者
 * @author liuyuhua
 */
public class FactionApplyVo implements Serializable {
	private static final long serialVersionUID = -2254978899069403608L;
	
	private long playerId;
	
	private String playerName;
	
	private int level;
	
	private Job job;
	
	private long applyDate;
	
	/**
	 * 构造函数
	 * @param playerId     玩家的ID
	 * @param playerName   玩家的名字
	 * @param level        玩家的等级
	 * @param job          玩家的职业
	 * @param applyDate    申请时间
	 * @return
	 */
	public static FactionApplyVo valueOf(long playerId,String playerName,int level,Job job,long applyDate){
		FactionApplyVo vo = new FactionApplyVo();
		vo.playerId = playerId;
		vo.job = job;
		vo.level = level;
		vo.applyDate = applyDate;
		vo.playerName = playerName;
		return vo;
	}
	
	//Getter and Setter...

	public String getPlayerName() {
		return playerName;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
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

	public long getApplyDate() {
		return applyDate;
	}

	public void setApplyDate(long applyDate) {
		this.applyDate = applyDate;
	}
	
}
