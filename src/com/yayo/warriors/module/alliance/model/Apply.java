package com.yayo.warriors.module.alliance.model;

import com.yayo.common.utility.DateUtil;

/**
 * 申请加入帮派者
 * @author liuyuhua
 */
public class Apply {
	
	/** 申请者的ID*/
	private long playerId;

	/** 申请时间(单位:秒)*/
	private long date;
	
	/**
	 * 构造函数
	 * @param playerId 玩家的ID
	 * @return {@link Apply} 申请对象
	 */
	public static Apply valueOf(long playerId){
		Apply apply = new Apply();
		apply.playerId = playerId;
		apply.date = DateUtil.getCurrentSecond();
		return apply;
	}
	
	
	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}
	
	/**
	 * 是否过期
	 * @return true 需要删除 false 反之
	 */
	public boolean isOverTime(){
		if(this.date >= DateUtil.getCurrentSecond() + 43200){
			return true;
		}else{
			return false;
		}
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
		Apply other = (Apply) obj;
		if (playerId != other.playerId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Apply [playerId=" + playerId + ", date=" + date + "]";
	} 
}
