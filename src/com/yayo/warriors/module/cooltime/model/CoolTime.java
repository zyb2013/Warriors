package com.yayo.warriors.module.cooltime.model;

import java.io.Serializable;

import com.yayo.common.utility.Splitable;
import com.yayo.warriors.module.cooltime.rule.CoolTimeRule;

/**
 * CD时间对象
 * 
 * @author Hyint
 */
public class CoolTime implements Serializable {
	private static final long serialVersionUID = -790643522308555940L;
	//GCD ID 
	public static final int GLOBAL_COOLTIME_ID = 1;
	
	/** CD开始时间 */
	private int id;

	/** CD结束时间.(毫秒) */
	private Long endTime = 0L;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Long getEndTime() {
		if(isTimeOut() && endTime > 0L) {
			endTime = 0L;
		}
		return endTime;
	}

	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	@Override
	public String toString() {
		return id + Splitable.ATTRIBUTE_SPLIT +endTime;
	}

	/**
	 * 构建一个CD对象
	 * 
	 * @param  cdId				CD时间表的ID
	 * @param  timeMillis		结束时间的毫秒
	 * @return {@link CoolTime}	冷却时间对象
	 */
	public static CoolTime valueOf(int cdId, long endTimeMillis) {
		CoolTime coolTime = new CoolTime();
		coolTime.id = cdId;
		coolTime.endTime = endTimeMillis;
		return coolTime;
	}
	
	public boolean isTimeOut() {
		return this.endTime / 10 <= (System.currentTimeMillis() + CoolTimeRule.MODIFIRE_COOLTIME) / 10;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		
		CoolTime other = (CoolTime) obj;
		return id == other.id;
	}
	
	
}
