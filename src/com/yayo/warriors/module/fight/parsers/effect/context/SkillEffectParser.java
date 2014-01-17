package com.yayo.warriors.module.fight.parsers.effect.context;

import com.yayo.warriors.basedb.model.SkillEffectConfig;
import com.yayo.warriors.module.fight.model.Context;
import com.yayo.warriors.module.fight.model.FightAttribute;
 

/**
 * 技能效果解析处理器
 * 
 * @author Hyint
 */
public interface SkillEffectParser {

	/**
	 * 技能解析方法
	 * 
	 * @param   context				战斗上下文
	 * @param   attacker			攻击者
	 * @param   targeter			被攻击者
	 * @param   skillEffect			技能效果对象
	 */
	void parser(Context context, FightAttribute attacker, FightAttribute targeter, SkillEffectConfig skillEffect);
}
