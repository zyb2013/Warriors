package com.yayo.warriors.module.buffer.model;

import java.util.Calendar;
import java.util.Date;

import com.yayo.common.utility.DatePattern;
import com.yayo.common.utility.DateUtil;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.module.fight.model.UnitId;

/**
 * BUFF对象
 * 
 * @author Hyint
 */
public class Buffer {

	/** 效果ID */
	private int id;
	
	/** 释放该效果时的技能等级 */
	private int level;
	
	/** 附加值/伤害量. 有正负号 */
	private int damage;
	
	/** 跳动周期. (当跳动周期 > 0时, 表示需要周期计算. 单位: 毫秒) */
	private int cycle;
	
	/** 起始生效时间. 单位: 毫秒 */
	private long startTime;
	
	/**  效果时间. 单位: 毫秒 */
	private long endTime;
	
	/** 释放该Buffer的单位Id */
	private long castId;
	
	/** 释放该Buffer的单位类型 */
	private int unitType = -1;
	
	/** 上次结算的时间 */
	private volatile long lastCalcTime;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public long getLastCalcTime() {
		return lastCalcTime;
	}

	public void setLastCalcTime(long lastCalcTime) {
		this.lastCalcTime = lastCalcTime;
	}

	public int getCycle() {
		return cycle;
	}

	public void setCycle(int cycle) {
		this.cycle = cycle;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getCastId() {
		return castId;
	}

	public void setCastId(long castId) {
		this.castId = castId;
	}

	public int getUnitType() {
		return unitType;
	}

	public void setUnitType(int unitType) {
		this.unitType = unitType;
	}

	/**
	 * 构建 {@link Buffer} 对象
	 * 
	 * @param  id				效果ID
	 * @param  level			施放该效果时的等级
	 * @param  damage			施放该效果时造成的伤害量/附加量
	 * @param  cycle			跳动周期
	 * @param  endTime			效果结束时间
	 * @param  unitId			释放Buffer的单位ID
	 * @return {@link Buffer}	效果对象
	 */
	public static Buffer valueOf(int id, int level, int damage, int cycle, long endTime, UnitId unitId) {
		long currentTimeMillis = System.currentTimeMillis();
		Buffer buffer = new Buffer();
		buffer.id = id;
		buffer.cycle = cycle;
		buffer.level = level;
		buffer.damage = damage;
		buffer.endTime = endTime;
		buffer.startTime = currentTimeMillis;
		buffer.lastCalcTime = currentTimeMillis;
		buffer.castId = unitId == null ? -1 : unitId.getId();
		buffer.unitType = unitId == null ? -1 : unitId.getType().ordinal();
		return buffer;
	}
	
	/**
	 * 构建 {@link Buffer} 对象
	 * 
	 * @param  id				效果ID
	 * @param  level			施放该效果时的等级
	 * @param  damage			施放该效果时造成的伤害量/附加量
	 * @param  cycle			跳动周期
	 * @param  endTime			效果结束时间
	 * @return {@link Buffer}	效果对象
	 */
	public static Buffer valueOf(int id, int level, int damage, int cycle, long endTime, long castId, int unitType) {
		long currentTimeMillis = System.currentTimeMillis();
		Buffer buffer = new Buffer();
		buffer.id = id;
		buffer.cycle = cycle;
		buffer.level = level;
		buffer.damage = damage;
		buffer.castId = castId;
		buffer.endTime = endTime;
		buffer.unitType = unitType;
		buffer.startTime = currentTimeMillis;
		buffer.lastCalcTime = currentTimeMillis;
		return buffer;
	}

	/**
	 * 构建 {@link Buffer} 对象
	 * 
	 * @param  id				效果ID
	 * @param  level			施放该效果时的等级
	 * @param  damage			施放该效果时造成的伤害量/附加量
	 * @param  cycle			跳动周期
	 * @param  endTime			效果结束时间
	 * @param  startTime		效果开始时间
	 * @param  unitId			单位ID
	 * @return {@link Buffer}	效果对象
	 */
	public static Buffer valueOf(int id, int level, int damage, int cycle, long startTime, long endTime, UnitId unitId) {
		long currentTimeMillis = System.currentTimeMillis();
		Buffer buffer = new Buffer();
		buffer.id = id;
		buffer.cycle = cycle;
		buffer.level = level;
		buffer.damage = damage;
		buffer.endTime = endTime;
		buffer.startTime = currentTimeMillis;
		buffer.lastCalcTime = currentTimeMillis;
		buffer.castId = unitId == null ? -1 : unitId.getId();
		buffer.unitType = unitId == null ? -1 : unitId.getType().ordinal();
		return buffer;
	}
	
	/**
	 * 构建 {@link Buffer} 对象
	 * 
	 * @param  id				效果ID
	 * @param  level			施放该效果时的等级
	 * @param  damage			施放该效果时造成的伤害量/附加量
	 * @param  cycle			跳动周期
	 * @param  endTime			效果结束时间
	 * @param  startTime		效果开始时间
	 * @return {@link Buffer}	效果对象
	 */
	public static Buffer valueOf(int id, int level, int damage, int cycle, long startTime, long endTime, long castId, int unitType) {
		long currentTimeMillis = System.currentTimeMillis();
		Buffer buffer = new Buffer();
		buffer.id = id;
		buffer.cycle = cycle;
		buffer.level = level;
		buffer.damage = damage;
		buffer.castId = castId;
		buffer.endTime = endTime;
		buffer.unitType = unitType;
		buffer.startTime = currentTimeMillis;
		buffer.lastCalcTime = currentTimeMillis;
		return buffer;
	}

	/**
	 * 构建 {@link Buffer} 对象
	 * 
	 * @param  id				效果ID
	 * @param  level			施放该效果时的等级
	 * @param  revise			误差时间(单位: 毫秒).
	 * @param  damage			施放该效果时造成的伤害量/附加量
	 * @param  cycle			跳动周期
	 * @param  endTime			效果结束时间
	 * @return {@link Buffer}	效果对象
	 */
	public static Buffer valueOf(int id, int level, int revise, int damage, int cycle, long endTime, long castId, int unitType) {
		long currentTimeMillis = System.currentTimeMillis();
		Buffer buffer = new Buffer();
		buffer.id = id;
		buffer.cycle = cycle;
		buffer.level = level;
		buffer.damage = damage;
		buffer.castId = castId;
		buffer.unitType = unitType;
		buffer.endTime = endTime + revise;
		buffer.startTime = currentTimeMillis + revise;
		buffer.lastCalcTime = currentTimeMillis + revise;
		return buffer;
	}
	
	@Override
	public String toString() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(this.getEndTime());
		Date endTimeDate = calendar.getTime();
		String pattern = DatePattern.PATTERN_YYYYMMDDHHMMSS;
		return new StringBuilder().append(id).append(Splitable.ATTRIBUTE_SPLIT)
								  .append(level).append(Splitable.ATTRIBUTE_SPLIT)
								  .append(damage).append(Splitable.ATTRIBUTE_SPLIT)
								  .append(cycle).append(Splitable.ATTRIBUTE_SPLIT)
				  			      .append(DateUtil.date2String(endTimeDate, pattern)).append(Splitable.ATTRIBUTE_SPLIT)
				  			      .append(castId).append(Splitable.ATTRIBUTE_SPLIT)
				  			      .append(unitType)
				  			      .toString();
	}
	
	/**
	 * Buffer 生效时间.
	 * 
	 * @return {@link Boolean}
	 */
	public boolean isStart() {
		return DateUtil.getCurrentSecond() >= DateUtil.toSecond(this.getStartTime());
	}
	
	/**
	 * 是否超时
	 * 
	 * @return {@link Boolean}	true-已超时, false-未超时
	 */
	public boolean isTimeOut() {
		return System.currentTimeMillis() >= this.getEndTime();
	}
}
