package com.yayo.warriors.module.active.model;

import java.util.List;

/**
 * 活动掉落 
 * @author liuyuhua
 */
public class ActiveDrop {
	
	/** 开始时间*/
	private long startTime;
	
	/** 结束时间*/
	private long endTime;
	
	/** 掉落列表*/ 
	private List<Integer> dorplist;
	
	/**
	 * 构造方法
	 * @param startTime    开始时间
	 * @param endTime      结束时间
	 * @param dorplist     掉落列表
	 * @return
	 */
	public static ActiveDrop valueOf(long startTime,long endTime,List<Integer> dorplist){
		ActiveDrop activeDrop = new ActiveDrop();
		activeDrop.startTime = startTime;
		activeDrop.endTime = endTime;
		activeDrop.dorplist = dorplist;
		return activeDrop;
	}
	
	/**
	 * 是否能够掉落
	 * @return {@link Boolean} true 可以掉落 false 不可以掉落
	 */
	public boolean canDrop(){
		long currentTime = System.currentTimeMillis();
		if(currentTime >= startTime && currentTime <= endTime){
			return true;
		}else{
			return false;
		}
	}
	
	//Getter and Setter...

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public List<Integer> getDorplist() {
		return dorplist;
	}

	public void setDorplist(List<Integer> dorplist) {
		this.dorplist = dorplist;
	}

	@Override
	public String toString() {
		return "ActiveDrop [startTime=" + startTime + ", endTime=" + endTime
				+ ", dorplist=" + dorplist + "]";
	}
}

