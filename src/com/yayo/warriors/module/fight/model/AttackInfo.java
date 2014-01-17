package com.yayo.warriors.module.fight.model;

/**
 * 攻击技能实体
 * 
 * @author Hyint
 */
public class AttackInfo {

	/** 技能ID */
	private int skillId;

	/** 技能等级 */
	private int damage;

	/** 被攻击的战斗单元 */
	private UnitId targetId;

	public int getSkillId() {
		return skillId;
	}

	public void setSkillId(int skillId) {
		this.skillId = skillId;
	}

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

	public static AttackInfo valueOf(int skillId, int damage, UnitId targetId) {
		AttackInfo shapeAttack = new AttackInfo();
		shapeAttack.damage = damage;
		shapeAttack.skillId = skillId;
		shapeAttack.targetId = targetId;
		return shapeAttack;
	}
}
