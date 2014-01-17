package com.yayo.warriors.module.buffer.vo;

import java.io.Serializable;

/**
 * 持续掉血信息
 * 
 * @author Hyint
 */
public class DOTInfoVO implements Serializable {
	private static final long serialVersionUID = -9003443937153429301L;

	/** 效果ID */
	private int id;
	
	/** 伤害量 */
	private int damage;

	/** 角色是否死亡 */
	private boolean state;
	
	/** 释放的单位ID */
	private transient long castId;
	
	/** 释放的单位类型 */
	private transient int unitType;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public int getDamage() {
		return damage;
	}

	public long getCastId() {
		return castId;
	}

	public int getUnitType() {
		return unitType;
	}

	public boolean isState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "DOTInfoVO [id=" + id + ", damage=" + damage + ", state=" + state + "]";
	}
	
	/**
	 * 构建{@link DOTInfoVO}对象
	 * 
	 * @param  effectId				效果ID
	 * @param  damage				伤害量
	 * @param  castId 				释放的ID
	 * @param  unitType				单位类型
	 * @return {@link DOTInfoVO}	持续掉血信息
	 */
	public static DOTInfoVO valueOf(int effectId, int damage, long castId, int unitType) {
		DOTInfoVO dotInfo = new DOTInfoVO();
		dotInfo.id = effectId;
		dotInfo.damage = damage;
		dotInfo.castId = castId;
		dotInfo.unitType = unitType;
		return dotInfo;
	}
}
