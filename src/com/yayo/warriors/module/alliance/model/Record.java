package com.yayo.warriors.module.alliance.model;

import java.io.Serializable;

/**
 * 捐献/消耗记录 
 * @author liuyuhua
 */
public class Record implements Serializable {
	
	private static final long serialVersionUID = 8441481152553129992L;
	//TODO 为什么不挪出去单独做一个接口静态定义, 而放在类里面? 优源. 2012年5月16日21:37:20
	//TODO 这些变量是一些相对于这个Record类的独有应用,和外界其他业务没有相关,所以不需要放出去外面. yuhua 
	/** 钱币类型*/
	public static final int SILVER_TYPE  = 1;
	/** 物品类型*/
	public static final int PROPS_TYPE   = 2;
	/** 帮派升级类型*/
	public static final int LEVELUP_TYPE = 3;
	/** 建筑升级类型*/
	public static final int BUILD_TYPE   = 4;
	/** 加入帮派类型*/
	public static final int JOIN_ALLIANCE_TYPE = 5;
	/** 退出帮派类型*/
	public static final int QUIT_ALLIANCE_TYPE = 6;
	/** 转移帮主*/
	public static final int DEVOLVE_MASTER_TYPE = 7;
	
	/** 玩家的名字*/
	private String name;
	
	/** 类型*/
	private int type;
	
	/** 时间(单位:秒)*/
	private long time;
	
	/** 数量*/
	private long number;
	
	/** 等级*/
	private int level;
	
	/** 建筑物*/
	private int build;
	
	/** 转移者的名字*/
	private String devoleName;
	
	/**
	 * 转移帮主记录信息
	 * @param devoleName  原帮主名字
	 * @param name        现帮主名字
	 * @return {@link Record} 记录对象
	 */
	public static Record log4Devole(String devoleName,String name){
		Record record = new Record();
		record.name = name;
		record.devoleName = devoleName;
		record.time = System.currentTimeMillis();
		record.type = Record.DEVOLVE_MASTER_TYPE;
		return record;
	}
	
	
	/**
	 * 退出帮派记录信息
	 * @param name    名字
	 * @return {@link Record} 记录对象
	 */
	public static Record log4Quit(String name){
		Record record = new Record();
		record.name = name;
		record.time = System.currentTimeMillis();
		record.type = Record.QUIT_ALLIANCE_TYPE;
		return record;
	}
	
	/**
	 * 记录加入帮派信息
	 * @param name     名字
	 * @return {@link Record} 记录对象     
	 */
	public static Record log4Join(String name){
		Record record = new Record();
		record.name = name;
		record.time = System.currentTimeMillis();
		record.type = Record.JOIN_ALLIANCE_TYPE;
		return record;
	}
	
	
	/**
	 *  记录建筑物升级
	 * @param name     名字
	 * @param level    等级
	 * @param build    建筑物类型
	 * @return {@link Record} 记录对象
	 */
	public static Record log4Build(String name,int level,int build) {
		Record record = new Record();
		record.name = name;
		record.level = level;
		record.build = build;
		record.time = System.currentTimeMillis();
		record.type = Record.BUILD_TYPE;
		return record;
	}
	
	/**
	 * 记录升级
	 * @param name    名字
	 * @param level   等级
	 * @return {@link Record} 记录对象
	 */
	public static Record log4Level(String name,int level) {
		Record record = new Record();
		record.name = name;
		record.level = level;
		record.time = System.currentTimeMillis();
		record.type = Record.LEVELUP_TYPE;
		return record;
	}
	
	/**
	 * 记录金钱
	 * @param name      名字
	 * @param number    数量
	 * @return {@link Record} 记录对象
	 */
	public static Record log4Silver(String name,long number) {
		Record record = new Record();
		record.name = name;
		record.number = number;
		record.time = System.currentTimeMillis();
		record.type = Record.SILVER_TYPE;
		return record;
	}

	/**
	 * 记录道具
	 * @param name     名字
	 * @param count    数量
	 * @return {@link Record} 记录对象
	 */
	public static Record log4Props(String name,int count) {
		Record record = new Record();
		record.name = name;
		record.number = count;
		record.time = System.currentTimeMillis();
		record.type = Record.PROPS_TYPE;
		return record;
	}

	//Getter and Setter...

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getBuild() {
		return build;
	}

	public void setBuild(int build) {
		this.build = build;
	}

	public String getDevoleName() {
		return devoleName;
	}

	public void setDevoleName(String devoleName) {
		this.devoleName = devoleName;
	}

	@Override
	public String toString() {
		return "Record [name=" + name + ", type=" + type + ", time=" + time
				+ ", number=" + number + ", level=" + level + ", build="
				+ build + ", devoleName=" + devoleName + "]";
	}

}
