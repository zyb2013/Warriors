package com.yayo.warriors.module.fight.parsers.combat.context;

import com.yayo.warriors.basedb.model.SkillEffectConfig;
import com.yayo.warriors.module.fight.model.Context;
import com.yayo.warriors.module.map.domain.ISpire;

 

/**
 * 战斗原型处理器
 * 
 * @author Hyint
 */
public interface FightParser {
	
	/**
	 * 开始战斗
	 * 
	 * @param  context				战斗上下文
	 * @param  attacker				攻击者对象
	 * @param  targeter				被攻击者对象
	 * @param  skillEffect			技能效果对象
	 */
	void actionAttack(Context context, ISpire attacker, ISpire targeter, SkillEffectConfig skillEffect);
	
}
