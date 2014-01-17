package com.yayo.warriors.module.user.model;

import java.io.Serializable;

import com.yayo.warriors.module.user.type.StatusType;

/**
 * 状态元素列表 
 * 
 * @author Hyint
 */
public class StatusElement implements Serializable {
	private static final long serialVersionUID = -7031400308657183128L;

	/** 
	 * 角色状态类型
	 */
	private StatusType type;
	
	/** 
	 * 状态的结束时间(单位: 毫秒)
	 */
	private long endTime;

	private StatusElement() {}

	public StatusType getType() {
		return type;
	}

	public void setType(StatusType type) {
		this.type = type;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public boolean isTimeOut() {
		return this.endTime <= System.currentTimeMillis();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (endTime ^ (endTime >>> 32));
		result = prime * result + (type == null ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		
		StatusElement other = (StatusElement) obj;
		return type == other.type;
	}
	
	/**
	 * 构建状态Element
	 * 	
	 * @param type						状态类型
	 * @param reviseTime				误差时间
	 * @param effectTime				效果时间(单位: 毫秒)
	 * @return {@link StatusElement}	状态类型
	 */	
	public static StatusElement add(StatusType type, long effectTime) {
		return valueOf(type, System.currentTimeMillis() + effectTime);
	}

	/**
	 * 构建状态Element
	 * 
	 * @param  type						状态类型
	 * @param  skillId					技能ID
	 * @param  reviseTime				修正时间(该修正时间用于控制型技能). 单位:毫秒
	 * @param  endTime					结束时间(就是该效果的结束时间.), 单位:毫秒
	 * @return {@link StatusElement}	角色的状态元素
	 */
	public static StatusElement valueOf(StatusType type, long endTime) {
		StatusElement element = new StatusElement();
		element.type = type;
		element.endTime = endTime;
		return element;
	}
	
	/**
	 * 构建状态Element
	 * 
	 * @param  type						状态类型
	 * @return {@link StatusElement}	角色的状态元素
	 */
	public static StatusElement remove(StatusType type) {
		StatusElement element = new StatusElement();
		element.type = type;
		return element;
	}

	@Override
	public String toString() {
		return "StatusElement [type=" + type + ", endTime=" + endTime + "]";
	}
}
