package com.yayo.warriors.module.onhook.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.yayo.common.db.model.BaseModel;

/**
 * 玩家闭关时间实体
 * 
 * @author huachaoping
 */
@Entity
@Table(name="userTrain")
public class UserTrain extends BaseModel<Long> {
	
	private static final long serialVersionUID = 6828384908895894821L;

	/** 玩家Id*/
	@Id
	@Column(name="playerId")
	private Long id;
	
	/**
	 * 是否能领取奖励
	 */
	private boolean received;
	
	/**
	 * 开启闭关的时间(毫秒)
	 */
	private long startTime = 0L;
	
	/*
	 * 今天 --> 开启。 
	 * 结束---> 领取
	 * 过一天--> 领取
	 */
	public static UserTrain valueOf(long playerId){
		UserTrain userTrain = new UserTrain();
		userTrain.id = playerId;
		return userTrain;
	}
		
	public boolean isReceived() {
		return received;
	}

	public void setReceived(boolean received) {
		this.received = received;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserTrain other = (UserTrain) obj;
		return id != null && other.id != null && id.equals(other.id);
	}
	
	
}
