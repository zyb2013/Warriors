package com.yayo.warriors.module.alliance.vo;

import java.io.Serializable;

/**
 * 申请加入者 
 * @author liuyuhua
 */
public class ApplyVo implements Serializable {
	private static final long serialVersionUID = 8386734983139443699L;
	
	/** 玩家的ID*/
	private long playerId;
	
	/** 玩家的职业*/
	private int job;
	
	private String name;
	
	/** 等级*/
	private int level;
	
	/** 申请时间*/
	private long date;
	
	/**
	 * 构造方法
	 * @param playerId    玩家的ID
	 * @param name        玩家的名字
	 * @param job         职业
	 * @param level       等级
	 * @param date        申请时间
	 * @return {@link ApplyVo} 申请者对象
	 */
	public static ApplyVo valueOf(long playerId,String name,int job,int level,long date) {
		ApplyVo vo = new ApplyVo();
		vo.playerId = playerId;
		vo.name     = name;
		vo.job      = job;
		vo.level    = level;
		vo.date     = date;
		return vo;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public int getJob() {
		return job;
	}

	public void setJob(int job) {
		this.job = job;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "ApplyVo [playerId=" + playerId + ", job=" + job + ", name="
				+ name + ", level=" + level + ", date=" + date + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (playerId ^ (playerId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ApplyVo other = (ApplyVo) obj;
		if (playerId != other.playerId)
			return false;
		return true;
	}

}
