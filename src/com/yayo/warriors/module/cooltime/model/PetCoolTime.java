package com.yayo.warriors.module.cooltime.model;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 家将CD对象
 * 
 * @author Hyint
 */
public class PetCoolTime {

	private long userPetId;
	
	/** 是否忙碌中 */
	private volatile boolean busy = false;
	
	/** 冷却时间集合 */ 
	private ConcurrentHashMap<Integer, CoolTime> coolTimes = new ConcurrentHashMap<Integer, CoolTime>(0);

	public long getUserPetId() {
		return userPetId;
	}

	public void setUserPetId(long userPetId) {
		this.userPetId = userPetId;
	}

	public boolean isBusy() {
		return busy;
	}

	public void setBusy(boolean busy) {
		this.busy = busy;
	}
	
	public void addCoolTime(CoolTime coolTime) {
		if(coolTime != null) {
			coolTimes.put(coolTime.getId(), coolTime);
		}
	}
	
	public CoolTime getCoolTime(int coolTimeId) {
		return coolTimes.get(coolTimeId);
	}
	
	/**
	 * 是否在CD中..
	 * 
	 * @param  coolTimeIds		CDID列表
	 * @return {@link Boolean}	true-CD中, false-没在CD中
	 */
	public boolean isCoolTiming(int...coolTimeIds) {
		for (int coolId : coolTimeIds) {
			CoolTime cd = coolTimes.get(coolId);
			if(cd != null && !cd.isTimeOut()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 增加冷却时间
	 * 
	 * @param coolTimeId		冷却时间CDID
	 * @param endTimeMillis		CD时长
	 */
	public void addCoolTime(int coolId, int len) {
		CoolTime coolTime = coolTimes.get(coolId);
		if(coolTime == null && coolId >= 0 && len > 0) {
			coolTime = CoolTime.valueOf(coolId, 0);
			coolTimes.put(coolId, coolTime);
		}
		
		if(coolTime != null) {
			coolTime.setEndTime(System.currentTimeMillis() + len);
			coolTimes.put(coolId, coolTime);
		}
	}
}
