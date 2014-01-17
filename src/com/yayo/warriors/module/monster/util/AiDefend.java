package com.yayo.warriors.module.monster.util;

import com.yayo.common.utility.DateUtil;

/**
 * 防止线程死亡,或者挂失
 * @author liuyuhua
 */
public class AiDefend {
	
	/** 线程实例*/
	private Runnable runnable;

	/** 创建时间*/
	private long date;
	
	/** 线程挂失丢弃时间 (单位:秒 sec)*/
	private final int DEFEND_TIME = 15;
	
	/**
	 * 构造函数
	 * @param runnable  线程实例
	 * @return
	 */
	public static AiDefend valueOf(Runnable runnable){
		AiDefend aiDefend = new AiDefend();
		aiDefend.runnable = runnable;
		aiDefend.date = DateUtil.getCurrentSecond();
		return aiDefend;
	}

	public Runnable getRunnable() {
		return runnable;
	}

	public void setRunnable(Runnable runnable) {
		this.runnable = runnable;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}
	
	/**
	 * 是否过期
	 * @return true 时间已经过期,false没有过期
	 */
	public boolean isOverTime(){
		long time = DateUtil.getCurrentSecond();
		return (time - this.date) >= this.DEFEND_TIME;
	}
	
	
}
