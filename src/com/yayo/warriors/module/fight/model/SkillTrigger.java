package com.yayo.warriors.module.fight.model;


/**
 * 技能触发者对象
 * 
 * @author Hyint
 */
public class SkillTrigger {

	/** 所在的地图ID */
	private int mapId;

	/** 技能ID */
	private int skillId;
	
	/** 攻击点X坐标 */
	private int positionX;
	
	/** 攻击点Y坐标*/
	private int positionY;

	/** 所在的逻辑分线 */
	private int branching;
	
	/** 伤害值 */
	private int damageValue;
	
	/** 攻击伤害的单元格 */
	private int attackGride;
	
	/** 被攻击的对象 */
	private UnitId	unitId;
	
	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	public int getPositionX() {
		return positionX;
	}

	public void setPositionX(int positionX) {
		this.positionX = positionX;
	}

	public int getPositionY() {
		return positionY;
	}

	public void setPositionY(int positionY) {
		this.positionY = positionY;
	}

	public int getBranching() {
		return branching;
	}

	public void setBranching(int branching) {
		this.branching = branching;
	}

	public int getAttackGride() {
		return attackGride;
	}

	public void setAttackGride(int attackGride) {
		this.attackGride = attackGride;
	}

	public int getDamageValue() {
		return damageValue;
	}

	public void setDamageValue(int damageValue) {
		this.damageValue = damageValue;
	}

	public int getSkillId() {
		return skillId;
	}

	public void setSkillId(int skillId) {
		this.skillId = skillId;
	}

	public UnitId getUnitId() {
		return unitId;
	}

	public void setUnitId(UnitId unitId) {
		this.unitId = unitId;
	}

	/**
	 * 构建溅射详细信息
	 * 
	 * @param  triggerId			触发者ID
	 * @param  skillId				技能ID
	 * @param  mapId				地图ID
	 * @param  attackGrid			攻击格子数
	 * @param  positionX			地图的X坐标
	 * @param  positionY			地图的Y坐标
	 * @param  branching			逻辑分线
	 * @param  triggerDamage		触发的伤害
	 * @return {@link SkillTrigger}		溅射对象
	 */
	public static SkillTrigger valueOf(UnitId triggerId, int skillId, int mapId, int positionX, 
				  int positionY, int attackGrid, int branching, int triggerDamage) {
		SkillTrigger spotter = new SkillTrigger();
		spotter.mapId = mapId;
		spotter.skillId = skillId;
		spotter.positionX = positionX;
		spotter.positionY = positionY;
		spotter.branching = branching;
		spotter.attackGride = attackGrid;
		spotter.damageValue = triggerDamage;
		return spotter;
	}
}
