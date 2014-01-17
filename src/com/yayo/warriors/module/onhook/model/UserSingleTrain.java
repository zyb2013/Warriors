package com.yayo.warriors.module.onhook.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 玩家打坐状态
 * 
 * @author huachaoping 
 */
public class UserSingleTrain implements Serializable{

	private static final long serialVersionUID = -5880061013571048537L;
	
	/** 玩家ID*/
	private long playerId;
	
	/** 目标 */
	private long targetId;
	
	/** 是否在打坐状态*/
	private boolean singleTrain;
	
	/** 开始打坐时间*/
	private long startTime = 0L;
	
	/** 打坐累计时间 */
	private int accumulateTime = 0;
	
	/** 被邀请列表 */
	private Set<Long> inviterIds = Collections.synchronizedSet( new HashSet<Long>() );

	
	/** 构造函数 */
	public static UserSingleTrain valueOf(Long playerId){
		UserSingleTrain train = new UserSingleTrain();
		train.playerId = playerId;
		return train;
	}

	
	public long getTargetId() {
		return targetId;
	}
	
	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}

	public boolean isSingleTrain() {
		return singleTrain;
	}

	public void setSingleTrain(boolean singleTrain) {
		this.singleTrain = singleTrain;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public int getAccumulateTime() {
		return accumulateTime;
	}
	
	/** 加入被邀请列表 */
	public void add2InviteSet(long playerId) {
		synchronized (inviterIds) {
			inviterIds.add(playerId);
		}
	}
	
	/** 将玩家从自己的被邀请列表删除 */
	public void removeInviter(long playerId) {
		synchronized (inviterIds) {
			inviterIds.remove(playerId);
		}
	}
	
	/** 清空被邀请列表 */
	public void clearInviteCache() {
		synchronized (inviterIds) {
			inviterIds.clear();
		}
	}
	
	
	public boolean isContainInviter(long playerId) {
		synchronized (inviterIds) {
			if (inviterIds.contains(playerId)) {
				return true;
			}
		}
		return false;
	}

	/** 是否在双修 */
	public boolean isCoupleTrain() {
		return this.singleTrain && this.targetId > 0L;
	}
	
	/** 双修目标 */
	public void addTarget(long targetId) {
		if (this.targetId <= 0L) {
			this.targetId = targetId;
		}
	}
	
	/** 获得邀请人数 */
	public int inviterCount() {
		synchronized (inviterIds) {
			return inviterIds.size();
		}
	}
	
	/** 移除双修目标 */
	public void removeTarget() {
		this.targetId = 0L;
	}
	
	/** 双修目标 */
	public boolean isContainTarget() {
		return this.targetId > 0L;
	}
	
	/** 增加打坐累计时间,　取消打坐计算一次 */
	public void addAccumulateTime(long lagTime) {
		this.accumulateTime += lagTime;
	}
	
	/** 累计时间清0 */
	public void clearAccumulateTime() {
		this.accumulateTime = 0;
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
		if (!(obj instanceof UserSingleTrain))
			return false;
		UserSingleTrain other = (UserSingleTrain) obj;
		return playerId != other.playerId;
	}


}
