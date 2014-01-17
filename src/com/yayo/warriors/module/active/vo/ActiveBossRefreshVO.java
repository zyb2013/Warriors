package com.yayo.warriors.module.active.vo;

import java.io.Serializable;

import com.yayo.common.utility.Splitable;

/**
 * 世界BOSS刷新VO
 * 
 * @author huachaoping
 */
public class ActiveBossRefreshVO implements Serializable {
	
	private static final long serialVersionUID = 7079808519193713369L;

	/** 活动Id */
	private int id;
	
	/** 怪物Id */
	private int monsterId;
	
	/** 刷新时间 */
	private String freshTime;
	
	/** 杀死怪物的玩家名字 */
	private String playerName = "";
	

	public static ActiveBossRefreshVO valueOf(int id, int monsterId, String playerName) {
		ActiveBossRefreshVO vo = new ActiveBossRefreshVO();
		vo.id = id;
		vo.monsterId = monsterId;
		vo.playerName = playerName;
		return vo;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getMonsterId() {
		return monsterId;
	}

	public void setMonsterId(int monsterId) {
		this.monsterId = monsterId;
	}

	public String getFreshTime() {
		return freshTime;
	}

	public void setFreshTime(String freshTime) {
		this.freshTime = freshTime;
	}
	
	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	
	
//	public void convertToIntArray(String refreshTime) {
//		freshTime = refreshTime.split(Splitable.ATTRIBUTE_SPLIT);
//	}

}
