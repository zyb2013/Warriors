package com.yayo.warriors.module.fight.type;

/**
 * 战斗触发的类型
 * 
 * @author Hyint
 */
public enum DamageType {
	
	/** 0 - 未命中/闪避 */
	MISS_MODE,
	
	/** 1 - 造成伤害 */
	DAMAGE_MODE,

	/** 2 - 反弹伤害 */
	REFLEX_MODE,
	
	/** 3 - 吸血伤害 */
	SUCK_BLOOD_MODE,
	
	/** 4 - 无伤害类型 */
	NO_DAMAGE_MODE,

	/** 5 - 加血 */
	BLOOD_MODE,
}
