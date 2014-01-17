package com.yayo.warriors.module.fight.parsers.effect;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.yayo.warriors.basedb.model.SkillEffectConfig;
import com.yayo.warriors.module.buffer.model.Buffer;
import com.yayo.warriors.module.fight.helper.BufferHelper;
import com.yayo.warriors.module.fight.helper.FightHelper;
import com.yayo.warriors.module.fight.model.AttackDamageVO;
import com.yayo.warriors.module.fight.model.Context;
import com.yayo.warriors.module.fight.model.FightAttribute;
import com.yayo.warriors.module.fight.model.FightReport;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.fight.parsers.effect.context.AbstractSkillEffectParser;
import com.yayo.warriors.module.fight.rule.FightRule;
import com.yayo.warriors.module.fight.type.State;
import com.yayo.warriors.module.skill.type.SkillEffectType;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.type.AttributeKeys;

/**
 * 连击伤害的效果解析器
 * 
 * @author Hyint
 */
@Component
public class DoubleHitEffectParser extends AbstractSkillEffectParser {
	
	
	protected int getType() {
		return SkillEffectType.DOUBLE_HIT_EFFECT.getCode();
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
		int skillEffectId = skillEffect.getId();						//技能效果ID
		int skillId = skillEffect.getSkillId();							//技能ID
		UnitId attackId = attacker.getUnitId();							//攻击者ID
		UnitId targetId = targeter.getUnitId();							//被攻击者ID
		Fightable skillable = attacker.getSkillable();					//攻击者的技能集合
		Fightable targeterable = targeter.getAttributes();				//被攻击者的战斗对象
		Map<Integer, Buffer> attackerBuffers = attacker.getBuffers();
		if(targeter.isDead() || BufferHelper.isUnitInImmobilize(attackerBuffers)) {
			return;
		}
		
		int attribute = AttributeKeys.HP;
		int currentValue = targeterable.getAttribute(attribute);	//被攻击者当前的HP值
		if(currentValue <= 0) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("攻击方:[{}] 发起攻击. 被攻击方:[{}] 已经死亡, 不能发起攻击", attackId, targetId);
			}
			return;
		}
		
		int skillLevel = skillable.getAttribute(skillId);
		boolean knockFlyDefense = targeter.isKnockFlyDefense();								//死亡是否可以被击飞
		int baseDamage = FightHelper.getBeAttackDamageValue(targeter);						//基础伤害值
		context.updateCritical(FightRule.isAttackCritical(attacker, targeter));
		AttackDamageVO damageVO = AttackDamageVO.valueOf(context.isCritical(), attacker, targeter);
		int damageValue = FightRule.calculateFightingAttackDamage(damageVO);
		int skillDamage = skillEffect.calcSkillEffect(damageValue, skillLevel).intValue();
		int skillEffectDamageValue = Math.max(FightRule.MIN_FIGHT_DAMAGE, skillDamage + baseDamage);
		context.addAttributeChanges(targetId, attribute, -skillEffectDamageValue);			//被攻击者被攻击提示的值
		int finalAttributeValue = Math.min(currentValue, skillEffectDamageValue);			//计算最大值
		context.addUnitHurtValue(targeter.getiSpire(), finalAttributeValue);				//更新伤害值
		targeterable.set(attribute, Math.max(0, currentValue - finalAttributeValue));		//被攻击者最终的HP值
		State state = targeterable.get(AttributeKeys.HP) <= 0 ? State.DEATH : State.NORMAL;	//角色状态
		context.addFightReport(FightReport.attack(targetId, skillEffectId, attribute, -skillEffectDamageValue, context.isCritical(), knockFlyDefense, state));
	}

}
