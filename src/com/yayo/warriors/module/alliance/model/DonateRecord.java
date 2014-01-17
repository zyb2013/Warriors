package com.yayo.warriors.module.alliance.model;

import java.io.Serializable;

/**
 * 帮派玩家当日捐献值得记录 
 * @author liuyuhua
 */
public class DonateRecord implements Serializable ,Comparable<DonateRecord>{
	private static final long serialVersionUID = 3594967734800029447L;

	/** 玩家的ID*/
	private long playerId;
	
	/** 名字*/
	private String name; 
	
	/** 今天贡献值*/
	private int donate;
	
	/**
	 * 构造方法
	 * @param playerId   玩家的ID
	 * @param name       玩家的名字
	 * @param donate     今日贡献值
	 * @return {@link DonateRecord} 玩家当日贡献值记录
	 */
	public static DonateRecord valueOf(long playerId,String name,int donate){
		DonateRecord record = new DonateRecord();
		record.playerId = playerId;
		record.name = name;
		record.donate = donate;
		return record;
	}

	/**
	 * 增加贡献值
	 * @param donate  贡献值
	 */
	public void increaseDonate(int donate){
		this.donate += donate;
	}
	
	//Getter and Setter...
	
	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDonate() {
		return donate;
	}

	public void setDonate(int donate) {
		this.donate = donate;
	}

	
	public String toString() {
		return "DonateRecord [playerId=" + playerId + ", name=" + name
				+ ", donate=" + donate + "]";
	}

	
	public int compareTo(DonateRecord o) {
		if(o == null) {
			return -1;
		}
		
		Integer targetDonate = o.getDonate();
		int donateCompare = targetDonate.compareTo(this.donate);
		return donateCompare;
	}
}
