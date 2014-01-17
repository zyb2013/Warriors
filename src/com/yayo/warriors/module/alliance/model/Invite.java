package com.yayo.warriors.module.alliance.model;

import com.yayo.common.utility.DateUtil;

/**
 * 邀请对象 
 * @author liuyuhua
 */
public class Invite {

	/** 玩家的ID*/
	private long playerId;
	
	/** 申请时间(单位:秒)*/
	private long date;
	
	/**
	 * 构造方法
	 * @param playerId  玩家的ID(被邀请者)
	 * @param inviterId 邀请者ID
	 * @return {@link Invite} 邀请对象
	 */
	public static Invite valueOf(long playerId){
		Invite invite = new Invite();
		invite.playerId  = playerId;
		invite.date      = DateUtil.getCurrentSecond();
		return invite;
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
		if((this.date + 120) <= DateUtil.getCurrentSecond()){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public String toString() {
		return "Invite [playerId=" + playerId + "]";
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
		Invite other = (Invite) obj;
		if (playerId != other.playerId)
			return false;
		return true;
	}
}
