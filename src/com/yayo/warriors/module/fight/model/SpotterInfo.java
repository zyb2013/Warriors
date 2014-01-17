package com.yayo.warriors.module.fight.model;

/**
 * 溅射信息对象
 * 
 * @author Hyint
 */
public class SpotterInfo {
	
	/**
	 * 触发溅射时的伤害值
	 */
	private int damage;
	
	/**
	 * 触发了溅射的技能ID
	 */
	private int skillId;
	
	/**
	 * 被攻击的对象
	 */
	private UnitId targetId;

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public UnitId getTargetId() {
		return targetId;
	}

	public void setTargetId(UnitId targetId) {
		this.targetId = targetId;
	}
	
	public int getSkillId() {
		return skillId;
	}

	public void setSkillId(int skillId) {
		this.skillId = skillId;
	}

	public static SpotterInfo valueOf(UnitId targetId, int skillId, int damage) {
		SpotterInfo spotterInfo = new SpotterInfo();
		spotterInfo.damage = damage;
		spotterInfo.skillId = skillId;
		spotterInfo.targetId = targetId;
		return spotterInfo;
	}
	
}
