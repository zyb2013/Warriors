package com.yayo.warriors.module.skill.rule;

import static com.yayo.warriors.module.skill.type.SkillType.*;

import com.yayo.warriors.module.skill.type.Classify;

/**
 * 技能规则对象
 * 
 * @author Hyint
 */
public class SkillRule {
	
	/** 可以学习的技能分类 */
	public static final int[] SKILL_CLASSIFY_ARRAY = { Classify.VOCATION_SKILL.ordinal() };
	/** 主动技能类型 */
	public static final int[] ACTIVE_SKILL_TYPES = { ACTIVE_ATTACK.ordinal(), ACTIVE_BUFFER.ordinal(), ACTIVE_TREAT.ordinal()};
	/** 可以学习的技能类型 */
	public static final int[] CANLEARN_SKILL_TYPES = { ACTIVE_ATTACK.ordinal(), ACTIVE_BUFFER.ordinal(), ACTIVE_TREAT.ordinal(), PASSIVE_SKILL.ordinal() };



}
