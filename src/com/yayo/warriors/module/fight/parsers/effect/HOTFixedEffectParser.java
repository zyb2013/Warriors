package com.yayo.warriors.module.fight.parsers.effect;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.yayo.warriors.basedb.model.SkillEffectConfig;
import com.yayo.warriors.module.buffer.model.Buffer;
import com.yayo.warriors.module.fight.helper.BufferHelper;
import com.yayo.warriors.module.fight.helper.FightHelper;
import com.yayo.warriors.module.fight.model.Context;
import com.yayo.warriors.module.fight.model.FightAttribute;
import com.yayo.warriors.module.fight.model.FightReport;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.fight.parsers.effect.context.AbstractSkillEffectParser;
import com.yayo.warriors.module.fight.rule.FightRule;
import com.yayo.warriors.module.skill.type.SkillEffectType;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.type.AttributeKeys;

/**
 * 治疗效果解析器
 * 
 * @author Hyint
 */
@Component
public class HOTFixedEffectParser extends AbstractSkillEffectParser {
	
	
	protected int getType() {
		return SkillEffectType.HOT_FIXED_EFFECT.getCode();
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
		int skillEffectId = skillEffect.getId();
		int skillId = skillEffect.getSkillId();							//技能ID
		UnitId attackId = attacker.getUnitId();							//攻击者ID
		UnitId targetId = targeter.getUnitId();							//被攻击者ID
		Fightable targeterable = targeter.getAttributes();				//被攻击者的战斗对象
		Fightable attackSkillable = attacker.getSkillable();			//攻击者的技能集合
		
		Map<Integer, Buffer> attackerBuffers = attacker.getBuffers();
		boolean unitInImmobilize = BufferHelper.isUnitInImmobilize(attackerBuffers);
		if(unitInImmobilize || targeter.isDead()) {
			return;
		}
		
		int attribute = AttributeKeys.HP;
		int currentValue = targeterable.getAttribute(attribute);
		int skillLevel = attackSkillable.getAttribute(skillId);		//技能等级
		boolean isCritical = FightRule.isAttackCritical(attacker, targeter);
		int calcSkillEffectValue = skillEffect.calcSkillEffect(1, skillLevel).intValue(); 
		int skillEffectValue = FightRule.calculateCriticalValue(calcSkillEffectValue, isCritical);
		double restrainTreatRates = FightHelper.getRestrainTreatRates(attackerBuffers.values());
		int hotAddHpValue = (int)(Math.max(0, skillEffectValue - skillEffectValue * restrainTreatRates));
		if(hotAddHpValue < 0) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("战斗单位:[{}] 给 战斗单位:[{}] 治疗, 治疗量:[{}] 不合法", new Object[] { attackId, targetId, hotAddHpValue });
			}
			return;
		}
		
		context.updateCritical(isCritical);
		context.addAttributeChanges(targetId, attribute, hotAddHpValue);			//被攻击者被攻击提示的值
		targeterable.set(attribute, Math.max(0, currentValue + hotAddHpValue));		//被攻击者最终的HP值
		context.addFightReport(FightReport.attack(targetId, skillEffectId, attribute, hotAddHpValue, false, isCritical));
	}

}
