package com.yayo.warriors.module.fight.model;

import java.io.Serializable;

/**
 * 变更的BUFF对象
 * 
 * @author Hyint
 */
public class ChangeBuffer implements Serializable {
	private static final long serialVersionUID = 5357742195764426539L;

	/** 变更类型: true-增加, false-移除 */
	private boolean add;

	/** 是否是Buffer. */
	private boolean buffer;
	
	/** 增加或者减少的BUFFID */
	private int bufferId;

	/** 变换BUFF的等级 */
	private int level;
	
	/** 伤害值 */
	private int damage;
	
	/** BUFF的刷新周期 */
	private int cycle;
	
	/** 效果开始的时间 */
	private long startTime;
	
	/** 结束时间. 单位: 秒 */
	private long endTime;
	
	/** 战斗单位ID */
	private UnitId unitId;
	
	public boolean isAdd() {
		return add;
	}

	public void setAdd(boolean add) {
		this.add = add;
	}

	public int getCycle() {
		return cycle;
	}
	
	public void setCycle(int cycle) {
		this.cycle = cycle;
	}

	public int getBufferId() {
		return bufferId;
	}

	public void setBufferId(int bufferId) {
		this.bufferId = bufferId;
	}
	
	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public boolean isBuffer() {
		return buffer;
	}

	public void setBuffer(boolean buffer) {
		this.buffer = buffer;
	}

	public UnitId getUnitId() {
		return unitId;
	}

	public void setUnitId(UnitId unitId) {
		this.unitId = unitId;
	}

	/**
	 * 新增一个BUFF
	 * 
	 * @param  bufferId					bufferId
	 * @param  level					DeBuffer的等级
	 * @param  cycle					周期时间
	 * @param  endTime					结束时间. 单位:毫秒
	 * @param  damage					伤害值.
	 * @return {@link ChangeBuffer}		变换的BUFF对象
	 */
	public static ChangeBuffer addBuffer(int bufferId, int level, int cycle, long endTime, int damage, UnitId unitId) {
		return addBuffer(bufferId, level, cycle, 0, endTime, damage, unitId);
	}

	/**
	 * 新增一个BUFF
	 * 
	 * @param  bufferId					bufferId
	 * @param  level					DeBuffer的等级
	 * @param  cycle					周期时间
	 * @param  endTime					结束时间. 单位:毫秒
	 * @param  damage					伤害值.
	 * @return {@link ChangeBuffer}		变换的BUFF对象
	 */
	public static ChangeBuffer addBuffer(int bufferId, int level, int cycle, int revise, long endTime, int damage, UnitId unitId) {
		ChangeBuffer changeBuffer = new ChangeBuffer();
		changeBuffer.add = true;
		changeBuffer.buffer = true;
		changeBuffer.cycle = cycle;
		changeBuffer.level = level;
		changeBuffer.damage = damage;
		changeBuffer.unitId = unitId;
		changeBuffer.bufferId = bufferId;
		changeBuffer.endTime = endTime + revise;
		changeBuffer.startTime = System.currentTimeMillis() + revise;
		return changeBuffer;
	}

	/**
	 * 新增一个DEBUFF
	 * 
	 * @param  bufferId					bufferId
	 * @param  level					DeBuffer的等级
	 * @param  cycle					周期时间
	 * @param  endTime					结束时间. 单位:毫秒
	 * @param  damage					伤害值.
	 * @param  unitId					释放该DEBUFF的单位ID
	 * @return {@link ChangeBuffer}		变换的BUFF对象
	 */
	public static ChangeBuffer addDeBuffer(int bufferId, int level, int cycle, long endTime, int damage, UnitId unitId) {
		ChangeBuffer changeBuffer = new ChangeBuffer();
		changeBuffer.add = true;
		changeBuffer.level = level;
		changeBuffer.cycle = cycle;
		changeBuffer.buffer = false;
		changeBuffer.damage = damage;
		changeBuffer.unitId = unitId;
		changeBuffer.endTime = endTime;
		changeBuffer.bufferId = bufferId;
		changeBuffer.startTime = System.currentTimeMillis();
		return changeBuffer;
	}
	
	/**
	 * 新增一个DEBUFF
	 * 
	 * @param  bufferId					bufferId
	 * @param  level					DeBuffer的等级
	 * @param  cycle					周期时间
	 * @param  revise					误差时间
	 * @param  endTime					结束时间. 单位:毫秒
	 * @param  damage					伤害值.
	 * @param  unitId					释放该DEBUFF的单位ID
	 * @return {@link ChangeBuffer}		变换的BUFF对象
	 */
	public static ChangeBuffer addDeBuffer(int bufferId, int level, int cycle, int revise, long endTime, int damage, UnitId unitId) {
		ChangeBuffer changeBuffer = new ChangeBuffer();
		changeBuffer.add = true;
		changeBuffer.level = level;
		changeBuffer.cycle = cycle;
		changeBuffer.buffer = false;
		changeBuffer.damage = damage;
		changeBuffer.unitId = unitId;
		changeBuffer.bufferId = bufferId;
		changeBuffer.endTime = endTime + revise;
		changeBuffer.startTime = System.currentTimeMillis() + revise;
		return changeBuffer;
	}

	/**
	 * 移除BUFF效果
	 * 
	 * @param  bufferId				效果ID
	 * @return {@link ChangeBuffer}	变换的效果ID
	 */
	public static ChangeBuffer removeBuffer(int bufferId) {
		ChangeBuffer buffer = new ChangeBuffer();
		buffer.add = false;
		buffer.bufferId = bufferId;
		return buffer;
	}

	/**
	 * 移除DEBUFF效果
	 * 
	 * @param  bufferId				效果ID
	 * @return {@link ChangeBuffer}	效果ID
	 */
	public static ChangeBuffer removeDeBuffer(int bufferId) {
		ChangeBuffer buffer = new ChangeBuffer();
		buffer.add = false;
		buffer.buffer = false;
		buffer.bufferId = bufferId;
		return buffer;
	}
	
}
