package com.yayo.warriors.module.skill.type;

import static com.yayo.warriors.module.user.type.AttributeKeys.*;

import org.apache.commons.lang.ArrayUtils;

import com.yayo.common.utility.EnumUtils;

/**
 * 技能效果类型
 * 
 * @author Hyint
 *
 */
public enum SkillEffectType {
	
	/** 100	- 无伤害效果 */
	NO_DAMAGE(100, -1, false, true, 1),
	
	/** 101	- 增加力量 */
	ADD_STRENGTH(101, STRENGTH, true, true, 1),
	
	/** 102	- 增加敏捷 */
	ADD_DEXERITY(102, DEXERITY, true, true, 1),
	
	/** 103	- 增加体质 */
	ADD_CONSTITUTION(103, CONSTITUTION, true, true, 1),
	
	/** 104	- 增加精神 */
	ADD_SPIRITUALITY(104, SPIRITUALITY, true, true, 1),
	
	/** 105	- 增加智力 */
	ADD_INTELLECT(105, INTELLECT, true, true, 1),
	
	/** 106	- 增加HP值 */
	ADD_HP(106, HP, true, true, 1),
	
	/** 107	- 增加MP值 */
	ADD_MP(107, MP, true, true, 1),
	
	/** 108	增加物攻 */
	ADD_PHYSICAL_ATTACK(108, PHYSICAL_ATTACK, true, true, 1),
	
	/** 109	增加物防 */
	ADD_PHYSICAL_DEFENSE(109, PHYSICAL_DEFENSE, true, true, 1),
	
	/** 110	增加法攻 */
	ADD_THEURGY_ATTACK(110, THEURGY_ATTACK, true, true, 1),
	
	/** 111	增加法防 */
	ADD_THEURGY_DEFENSE(111, THEURGY_DEFENSE, true, true, 1),
	
	/** 112	增加物暴 */
	ADD_PHYSICAL_CRITICAL(112, PHYSICAL_CRITICAL, true, true, 1),
	
	/** 113	增加法暴 */
	ADD_THEURGY_CRITICAL(113, THEURGY_CRITICAL, true, true, 1),
	
	/** 114	增加命中 */
	ADD_HIT(114, HIT, true, true, 1),
	
	/** 115	增加闪避 */
	ADD_DODGE(115, DODGE, true, true, 1),
	
	/** 116	穿透 */
	ADD_PIERCE(116, PIERCE, true, true, 1),
	
	/** 117	格挡 */
	ADD_BLOCK(117, BLOCK, true, true, 1),
	
	/** 118	急速 */
	ADD_RAPIDLY(118, RAPIDLY, true, true, 1),
	
	/** 119	坚韧 */
	ADD_DUCTILITY(119, DUCTILITY, true, true, 1),
	
	/** 120 - 增加移动速度 */
	ADD_MOVE_SPEED(120, MOVE_SPEED, true, true, 1),

	/** 121 - 增加生命上限 */
	ADD_HP_MAX(121, HP_MAX, true, true, 1),
	
	/** 122 - 增加法力上限 */
	ADD_MP_MAX(122, MP_MAX, true, true, 1),
	
	//------------ 战斗技能属性 ---------------------
	/** 201 - 基础伤害百分比 */
	DPS_PERCENT_EFFECT(201, -1, false, true, 1),
	
	/** 202	- 基础伤害固定值 */
	DPS_FIXED_EFFECT(202, -1, false, true, 1),
	
	/** 203	- 持续伤害百分比 */
	DOT_PERCENT_EFFECT(203, -1, false, true, 1),
	
	/** 204	- 持续伤害固定值 */
	DOT_FIXED_EFFECT(204, -1, false, true, 1),
	
	/** 205	- 连击 (有一定几率触发连续攻击2次)*/
	DOUBLE_HIT_EFFECT(205, -1, false, true, 1),
	
	/** 206	- 虚弱效果 (减少对方百分比物理防御、法术防御和百分比移动速度)*/
	WEAKNESS_EFFECT(206, -1, false, true, 1),
	
	/** 207	- 定身效果 (让对方眩晕效果)*/
	IMMOBILIZE_EFFECT(207, -1, false, true, 1),
	
	/** 208	- 治疗效果百分比. (恢复目标血量，物攻/法攻百分比) . TODO 会找策划详细了解*/
	HOT_PERCENT_EFFECT(208, -1, true, true, 1),
	
	/** 209	- 抑制治疗. (减少对方恢复效果百分比) . TODO 会找策划详细了解*/
	RESTRAIN_HOT_EFFECT (209, -1, false, true, 1),
	
	/** 210 - 治疗效果固定值. (恢复目标血量，固定值)*/
	HOT_FIXED_EFFECT(210, -1, true, true, 1),
	
	/** 211 - 受到伤害增加. (使目标受到伤害增加) */
	INCREASE_BE_DAMAGE_EFFECT(211, -1, false, true, 1),
	
	/** 212 - 持续伤害百分比和固定值 */
	DOT_PERCENT_AND_FIXED_EFFECT(212, -1, true, true, 1),
	
	/** 301 - 打怪经验加成 */
	FIGHT_EXP_PERCENT_EFFECT(301, FIGHT_EXP_RATE, true, false, 1000),
	
	/** 302 - 打坐经验加成 */
	TRAIN_EXP_PERCENT_EFFECT(302, TRAIN_EXP_RATE, true, false, 1000),
	
	/** 303 - 打坐真气加成 */
	TRAIN_GAS_PERCENT_EFFECT(303, TRAIN_GAS_RATE, true, false, 1000),
	
	/** 304 - 物攻百分比加成 */
	ADD_PHYSICAL_ATTACK_RATE(304, PHYSICAL_ATTACK_RATIO, true, true, 1000),

	/** 305 - 物防百分比加成*/
	ADD_PHYSICAL_DEFENSE_RATE(305, PHYSICAL_DEFENSE_RATIO, true, true, 1000),
	
	/** 306 - 法功百分比加成 */
	ADD_THEURGE_ATTACK_RATE(306, THEURGY_ATTACK_RATIO, true, true, 1000),
	
	/** 307 - 法防百分比加成 */
	ADD_THEURGE_DEFENSE_RATE(307, THEURGY_DEFENSE_RATIO, true, true, 1000),

	/** 400 - 冲锋打击技能效果 */
	CHARGE_AGAINST_EFFECT(400, -1, true, true, 1),
	
	/** 401 - 擒拿手技能效果 */
	GRABBING_HAND_EFFECT(401, -1, true, true, 1),
	
	/** 402 - 击退目标技能效果 */
	KNOCK_BACK_EFFECT(402, -1, true, true, 1);
	
	/** 效果类型值 */
	private int code;

	/** 附加的效果值 */
	private int attribute;
	
	/** 是否叠加 */
	private boolean add;
	
	/** 倍率值. 如果是百分比的, 则需要乘以1000*/
	private int rateValue;
	
	/** 是否忽略闪避 */
	private boolean ignoreDodge = false;
	
	/**
	 * 构造器
	 * 
	 * @param code			参数值
	 * @param attribute		附加的角色属性
	 * @param ignoreDodge	忽略躲闪
	 * @param add			是否直接增加.true-增加, false-重新赋值
	 */
	SkillEffectType(int code, int attribute, boolean ignoreDodge, boolean add, int rateValue) {
		this.add = add;
		this.code = code;
		this.attribute = attribute;
		this.rateValue = rateValue;
		this.ignoreDodge = ignoreDodge;
	}

	public int getCode() {
		return code;
	}

	public int getAttribute() {
		return attribute;
	}

	public void setAttribute(int attribute) {
		this.attribute = attribute;
	}

	public boolean isIgnoreDodge() {
		return ignoreDodge;
	}
	
	public boolean isAdd() {
		return add;
	}

	public int getRateValue() {
		return rateValue;
	}

	private static int[] CODES = null;
	
	/**
	 * 获得效果的Code数组
	 * @return
	 */
	private static int[] getCodes() {
		if(CODES != null) {
			return CODES;
		}
		
		synchronized (SkillEffectType.class) {
			SkillEffectType[] values = SkillEffectType.values();
			CODES = new int[values.length];
			for (int i = 0; i < values.length; i++) {
				SkillEffectType skillEffectType = values[i];
				CODES[skillEffectType.ordinal()] = skillEffectType.getCode();
			}
		}
		return CODES;
	}
	
	/**
	 * 获得技能效果类型
	 * 
	 * @param  type						效果类型
	 * @return {@link SkillEffectType}	效果类型
	 */
	public static SkillEffectType getSkillEffectType(int type) {
		 int ordinal = ArrayUtils.indexOf(getCodes(), type);
		 return EnumUtils.getEnum(SkillEffectType.class, ordinal);
	}	 
}
