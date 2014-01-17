package com.yayo.warriors.module.user.model;

import com.yayo.common.utility.TimeConstant;

/**
 * 登录封装类
 * 
 * @author Hyint
 */
public class LoginWrapper {

	/**
	 * 角色ID
	 */
	private long playerId;
	
	/**
	 * 分线号
	 */
	private int branching;
	
	/**
	 * 失效时间
	 */
	private long endTime;

	/**
	 * 客户端的IP
	 */
	private String clientIp;
	
	public long getPlayerId() {
		return playerId;
	}
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	
	public int getBranching() {
		return branching;
	}
	public void setBranching(int branching) {
		this.branching = branching;
	}
	public boolean isTimeOut() {
		return this.endTime <= System.currentTimeMillis();
	}
	
	public String getClientIp() {
		return clientIp;
	}
	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}
	public static LoginWrapper valueOf(long playerId, int branching, String clientIp) {
		LoginWrapper loginSwrapper = new LoginWrapper();
		loginSwrapper.clientIp = clientIp;
		loginSwrapper.playerId = playerId;
		loginSwrapper.branching = branching;
		loginSwrapper.endTime = System.currentTimeMillis() + TimeConstant.ONE_MINUTE_MILLISECOND;
		return loginSwrapper;
	}
	@Override
	public String toString() {
		return "LoginSwrapper [playerId=" + playerId + ", endTime=" + endTime + "]";
	}
}
