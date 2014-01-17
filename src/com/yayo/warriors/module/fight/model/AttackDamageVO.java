package com.yayo.warriors.module.fight.model;


/**
 * 攻击伤害VO
 * 
 * @author Hyint
 */
public class AttackDamageVO {

	/** 是否暴击伤害 */
	private boolean critical;
	
	/** 攻击者属性值 */
	private FightAttribute attacker;
	
	/** 被攻击者属性值 */
	private FightAttribute targeter;

	public boolean isCritical() {
		return critical;
	}

	public void setCritical(boolean critical) {
		this.critical = critical;
	}

	public FightAttribute getAttacker() {
		return attacker;
	}

	public void setAttacker(FightAttribute attacker) {
		this.attacker = attacker;
	}

	public FightAttribute getTargeter() {
		return targeter;
	}

	public void setTargeter(FightAttribute targeter) {
		this.targeter = targeter;
	}
	
	/**
	 * 构建攻击伤害VO对象
	 * 
	 * @param  critical					是否暴击
	 * @param  attacker					攻击者属性
	 * @param  targeter					被攻击者属性
	 * @return {@link AttackDamageVO}	攻击者伤害VO
	 */
	public static AttackDamageVO valueOf(boolean critical, FightAttribute attacker, FightAttribute targeter) {
		AttackDamageVO attackDamageVO = new AttackDamageVO();
		attackDamageVO.critical = critical;
		attackDamageVO.attacker = attacker;
		attackDamageVO.targeter = targeter;
		return attackDamageVO;
	}

	@Override
	public String toString() {
		return "AttackDamageVO [critical=" + critical + ", attacker=" + attacker + ", targeter="
				+ targeter + "]";
	}
	
	
}
