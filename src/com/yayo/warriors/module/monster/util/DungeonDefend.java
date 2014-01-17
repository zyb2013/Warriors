package com.yayo.warriors.module.monster.util;

import com.yayo.common.utility.DateUtil;

/**
 * 副本怪物守护
 * <per>用于定时回收怪物</per>
 * @author liuyuhua
 */
public class DungeonDefend {
	
	/** 怪物的ID*/
	private long monsterId;

	/** 创建的时间(单位:秒)*/
	private long date;
	
	/** 副本增量ID*/
	private long dungeonId;
	
	/** 回收时间(单位:秒 sec)*/
	private long DEFEND_TIME = 10800;
	

	/**
	 * 构造方法
	 * @param monsterId   怪物的唯一标识ID
	 * @param dungeonId   副本的唯一标识ID
	 * @return
	 */
	public static DungeonDefend valueOf(long monsterId,long dungeonId){
		DungeonDefend defend = new DungeonDefend();
		defend.monsterId = monsterId;
		defend.dungeonId = dungeonId;
		defend.date = DateUtil.getCurrentSecond();
		return defend;
	}
	
	/**
	 * 是否过期
	 * @return true 时间已经过期,false没有过期
	 */
	public boolean isOverTime(){
		long time = DateUtil.getCurrentSecond();
		return (time - this.date) >= this.DEFEND_TIME;
	}
	
	//Getter and Setter...

	public long getMonsterId() {
		return monsterId;
	}

	public void setMonsterId(long monsterId) {
		this.monsterId = monsterId;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public long getDungeonId() {
		return dungeonId;
	}

	public void setDungeonId(long dungeonId) {
		this.dungeonId = dungeonId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (date ^ (date >>> 32));
		result = prime * result + (int) (dungeonId ^ (dungeonId >>> 32));
		result = prime * result + (int) (monsterId ^ (monsterId >>> 32));
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
		DungeonDefend other = (DungeonDefend) obj;
		if (date != other.date)
			return false;
		if (dungeonId != other.dungeonId)
			return false;
		if (monsterId != other.monsterId)
			return false;
		return true;
	}
	
}
