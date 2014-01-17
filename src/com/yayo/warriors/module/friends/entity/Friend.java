
package com.yayo.warriors.module.friends.entity;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.yayo.common.db.model.BaseModel;
import com.yayo.warriors.module.friends.type.FriendState;
import com.yayo.warriors.module.friends.type.FriendType;

@Entity
@Table(name="friend")
public class Friend extends BaseModel<Long>{

	private static final long serialVersionUID = -4751740539019967835L;

	/** 主键*/
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	/** 所属玩家ID*/
	private Long playerId;
	
	/** 目标ID*/
	private Long targetId;
	
	/** 类型*/
	@Enumerated
	private FriendType type;
	
	/** 数值(好友度)*/
	private int value;
	
	/** 状态 -1 删除状态 0 活跃状态*/
	private int state;
	
	/**
	 * 构造函数
	 * @param playerId    所属玩家的ID
	 * @param targetId    目标玩家ID
	 * @param type        类型
	 * @return
	 */
	public static Friend valueOf(Long playerId, Long targetId, FriendType type) {
		Friend friend = new Friend();
		friend.playerId = playerId;
		friend.targetId = targetId;
		friend.value = 0;
		friend.type = type;
		friend.state = FriendState.active;
		return friend;
	}

	
	public static Friend valueOf(long playerId, long targetId, int value, FriendType type) {
		Friend friend = new Friend();
		friend.playerId = playerId;
		friend.targetId = targetId;
		friend.value = value;
		friend.type = type;
		friend.state = FriendState.active;
		return friend;
	}
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Long getTargetId() {
		return targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

	public FriendType getType() {
		return type;
	}

	public void setType(FriendType type) {
		this.type = type;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	public void increaseValue(int value) {
		this.value += value;
	}

	public void decreaseValue(int value) {
		this.value -= value;
	}
	
	public Long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
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
		Friend other = (Friend) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Friend [id=" + id + ", playerId=" + playerId + ", targetId="
				+ targetId + ", type=" + type + ", value=" + value + ", state="
				+ state + "]";
	}

}
