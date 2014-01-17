package com.yayo.warriors.module.fight.parsers.effect;

import org.springframework.stereotype.Component;

import com.yayo.warriors.basedb.model.SkillEffectConfig;
import com.yayo.warriors.common.util.astar.Point;
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
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.skill.type.SkillEffectType;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.type.AttributeKeys;

/**
 * 擒拿手效果解析器
 * 
 * @author Hyint
 */
@Component
public class GarbbingHandEffectParser extends AbstractSkillEffectParser {
	
	
	protected int getType() {
		return SkillEffectType.GRABBING_HAND_EFFECT.getCode();
	}

	/**
	 * 技能解析方法
	 * 
	 * @param   context				战斗上下文
	 * @param   attacker			攻击者
	 * @param   targeter			被攻击者
	 * @param   skillEffect			技能效果对象
	 */
	
	public void parser(Context context, FightAttribute attacker, FightAttribute targeter, SkillEffectConfig skillEffect) {
		int skillId = skillEffect.getSkillId();												//技能ID
		UnitId attackId = attacker.getUnitId();												//攻击者ID
		UnitId targetId = targeter.getUnitId();												//被攻击者ID
		int skillEffectId = skillEffect.getId();											//技能效果ID
		if(targeter.isDead()) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("被攻击者: [{}] 已经死亡, 不能接受擒拿手效果.");
			}
			return;
		}
		
		if(BufferHelper.isUnitInImmobilize(attacker.getBuffers())) { 
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("攻击者: [{}] 有定身效果, 不能发起擒拿手效果.");
			}
			return;
		} 

		int attribute = AttributeKeys.HP;
		Fightable skillable = attacker.getSkillable();									//攻击者的技能集合
		Fightable targeterable = targeter.getAttributes();								//被攻击者的战斗对象
		int currentValue = targeterable.getAttribute(attribute);						//被攻击者当前的HP值
		if(currentValue <= 0) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("攻击方:[{}] 发起擒拿手. 被攻击方:[{}] 已经死亡, 不能发擒拿手", attackId, targetId);
			}
			return;
		}
		
		if(targeter.isGarbbingDefense()) {
			context.addImmuneFightReport(targetId, skillId);
			return;
		};
		
		
		int skillLevel = skillable.getAttribute(skillId);
		int baseDamage = FightHelper.getBeAttackDamageValue(targeter);
		boolean attackCritical = FightRule.isAttackCritical(attacker, targeter);
		int damageValue = FightRule.calculateFightingAttackDamage(AttackDamageVO.valueOf(context.isCritical(), attacker, targeter));
		int skillDamage = skillEffect.calcSkillEffect(damageValue, skillLevel).intValue();
		int skillEffectDamageValue = Math.max(FightRule.MIN_FIGHT_DAMAGE, skillDamage + baseDamage);
//		if(skillEffectDamageValue <= 0) { //伤害小于0, 就不需要表现暴击了
//			attackCritical = false;
//			skillEffectDamageValue = 0;
//		}
		
		context.updateCritical(attackCritical);												//更新暴击状态
		GameMap gameMap = context.getGameMap();
		Point casterPoint = context.getCasterPoint();										//施法者的位置
		Point targetPoint = context.getTargetPoint();										//目标者的位置
		boolean knockFlyDefense = targeter.isKnockFlyDefense();								//死亡是否会被击飞
		Point newPoint = calcGarbbingHandPoint(casterPoint, targetPoint, gameMap);			//新的位置
		context.updateFightUnitChangePoints(targeter.getiSpire(), newPoint, false);			//设置会改变坐标的单位ID
		context.addAttributeChanges(targetId, attribute, -skillEffectDamageValue);			//被攻击者被攻击提示的值
		int finalAttributeValue = Math.min(currentValue, skillEffectDamageValue);			//计算最大值
		targeterable.set(attribute, Math.max(0, currentValue - finalAttributeValue));		//被攻击者最终的HP值
		State state = targeterable.get(AttributeKeys.HP) <= 0 ? State.DEATH : State.NORMAL;	//角色状态
		context.addUnitHurtValue(targeter.getiSpire(),finalAttributeValue);					//更新角色最后伤害值
		context.addFightReport(FightReport.attack(targetId, skillEffectId, attribute, -skillEffectDamageValue, attackCritical, knockFlyDefense, state, newPoint));
	}
}
