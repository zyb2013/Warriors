package com.yayo.warriors.module.search.vo;

import java.io.Serializable;

import com.yayo.warriors.module.user.type.Job;

public class CommonSearchVo implements Serializable {

	private static final long serialVersionUID = -7208487443658661080L;
	
	/** 玩家Id*/
	private long playerId;
	
	/** 玩家名字*/
	private String name;
	
	/** 玩家等级*/
	private int level;
	
	/** 玩家职业*/
	private Job clazz;
	
	/** 阵营*/
	private int camp;
	
	/**
	 * 构造函数
	 * @param name    名字
	 * @param level   等级
	 * @param clazz   职业
	 * @param camp    阵营
	 * @return
	 */
	public static CommonSearchVo valueOf(long playerId, String name,int level,Job clazz,int camp){
		CommonSearchVo vo = new CommonSearchVo();
		vo.playerId = playerId;
		vo.name  = name;
		vo.level = level;
		vo.clazz = clazz;
		vo.camp  = camp;
		return vo;
	}

	
	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public Job getClazz() {
		return clazz;
	}

	public void setClazz(Job clazz) {
		this.clazz = clazz;
	}

	public int getCamp() {
		return camp;
	}
	
	public void setCamp(int camp) {
		this.camp = camp;
	}

	@Override
	public String toString() {
		return "CommonSearchVo [playerId=" + playerId + ", name=" + name
				+ ", level=" + level + ", clazz=" + clazz + ", camp=" + camp
				+ "]";
	}
}
