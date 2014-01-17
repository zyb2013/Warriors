package com.yayo.warriors.module.fight.parsers.effect;

import org.springframework.stereotype.Component;

import com.yayo.warriors.basedb.model.SkillEffectConfig;
import com.yayo.warriors.module.fight.model.Context;
import com.yayo.warriors.module.fight.model.FightAttribute;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.fight.parsers.effect.context.AbstractSkillEffectParser;
import com.yayo.warriors.module.skill.type.SkillEffectType;

/**
 * 没有伤害的效果解析器
 * 
 * @author Hyint
 */
@Component
public class NoDamageEffectParser extends AbstractSkillEffectParser {
	
	
	protected int getType() {
		return SkillEffectType.NO_DAMAGE.getCode();
	}

	/**
	 * 技能解析方法
	 * 
	 * @param   context				战斗上下文
	 * @param   attacker			攻击者
	 * @param   targeter			被攻击者
	 * @param   skillEffect			技能效果对象
	 */
	
	public void parser(Context context, FightAttribute attacker, 
			FightAttribute targeter, SkillEffectConfig skillEffect) {
		UnitId unitId = attacker.getUnitId();
		int skillId = skillEffect.getSkillId();
		int skillEffectId = skillEffect.getId();
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("攻击者:[{}] 使用技能:[{}] 触发没有伤害效果:[{}]", new Object[] { unitId, skillId, skillEffectId });
		}
	}
}
