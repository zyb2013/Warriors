package com.yayo.warriors.module.onlines.model;

import java.io.Serializable;

import com.yayo.common.utility.Splitable;

/**
 * 角色等级记录 分布统计记录
 * @author liuyuhua
 */
public class UserLevelRecord implements Serializable , Comparable<UserLevelRecord>{
	private static final long serialVersionUID = 1L;
	
	/** 等级*/
	private Integer level;
	
	/** 等级总人数*/
	private int count;
	
	/** 等级总共在线人数*/
	private int onlineCount;
	
	/**
	 * 构造方法
	 * @param level  等级
	 * @return 用户等级统计
	 */
	public static UserLevelRecord valueOf(int level){
		UserLevelRecord record = new UserLevelRecord();
		record.level = level;
		return record;
	}
	
	/**
	 * 增加数量
	 */
	public void addCount(){
		this.count += 1;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getOnlineCount() {
		return onlineCount;
	}

	public void setOnlineCount(int onlineCount) {
		this.onlineCount = onlineCount;
	}
	

	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((level == null) ? 0 : level.hashCode());
		return result;
	}

	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserLevelRecord other = (UserLevelRecord) obj;
		if (level == null) {
			if (other.level != null)
				return false;
		} else if (!level.equals(other.level))
			return false;
		return true;
	}

	/**
	 * 返回格式
	 * {等级_当前等级总人数_当前等级总在线人数}
	 */
	
	public String toString() {
		return level + Splitable.ATTRIBUTE_SPLIT + count + Splitable.ATTRIBUTE_SPLIT + onlineCount;
	}

	
	public int compareTo(UserLevelRecord o) {
		if(o == null) {
			return -1;
		}
		Integer targetLevel = o.level;
		return targetLevel.compareTo(this.level);
	}
	
}
