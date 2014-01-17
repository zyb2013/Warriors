package com.yayo.warriors.module.fight.rule;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.skill.type.SkillEffectType.*;
import static com.yayo.warriors.type.FormulaKey.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yayo.common.rhino.RhinoHelper;
import com.yayo.common.utility.NumberUtil;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.model.SkillEffectConfig;
import com.yayo.warriors.common.helper.FormulaHelper;
import com.yayo.warriors.module.fight.model.AttackDamageVO;
import com.yayo.warriors.module.fight.model.Context;
import com.yayo.warriors.module.fight.model.FightAttribute;
import com.yayo.warriors.module.fight.type.FightCasting;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.StatusType;
import com.yayo.warriors.type.FormulaKey;

/**
 * 战斗规则对象
 * 
 * @author Hyint
 */
public class FightRule {
	
	public static final int JUMP_SCOPE = 490;
	/** 跳跃一次需要扣除的能量值, 体力值 */
	public static final int JUMP_SP_VALUE = 1;
	/** 物理攻击等级 */
	public static final int PHYSICAL_LEVEL = 1;
	/** 战斗的保底伤害值 */
	public static final int MIN_FIGHT_DAMAGE = 10;
	/** 二段跳技能ID */
	public static final int SECOND_JUMP_SKILLID = 90005;
	/** 战斗超时. 也就是说进入战斗后, 多久不动则脱离战斗. 单位: 毫秒*/
	public static final int FIGHT_TIMEOUT = 10000;
	/** 角色释放技能, 5秒CD */
	public static final int PLAYER_SKILL_SING_TIME = 5;
	/** 怪物攻击的A*格子数 */
	public static final int MAX_PET_FIGHT_DISTANCE = 33;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FightRule.class);
	
	/** 死亡需要移除的BUFF类型 */
	public static final int[] DEAD_REMOVE_BUFFER_TYPES = { WEAKNESS_EFFECT.getCode(),  				IMMOBILIZE_EFFECT.getCode(), 
														   RESTRAIN_HOT_EFFECT.getCode(), 			INCREASE_BE_DAMAGE_EFFECT.getCode(), 
														   DOT_PERCENT_AND_FIXED_EFFECT.getCode() };
	
	/**
	 * 验证攻击者的状态
	 * 
	 * @param  attacker				攻击者
	 * @param  statusTypes			状态类型数组
	 * @return {@link Integer}		战斗模块返回值
	 */
	public static int validateAttackStatus(FightAttribute attacker, StatusType...statusTypes) {
		for (StatusType statusType : statusTypes) {
			if(attacker.hasElementStatus(statusType)) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("{}", attacker);
				}
				return FAILURE;
			}
		}
		return SUCCESS;
	}
	
	/**
	 * 战斗公式计算
	 * 
	 * @param  expression		公式
	 * @param  unit				攻击单位
	 * @param  target			目标单位
	 * @param  resultType		返回类型
	 * @return T				公式的结果
	 */
	public static <T> T fightInvoker(String expression, Fightable unit, Fightable target, Class<T> resultType) {
		if(StringUtils.isBlank(expression)) {
			return (T)NumberUtil.valueOf(resultType, 0);
		}
		
		Map<String, Object> ctx = new HashMap<String, Object>(2);
		if(unit != null) {
			ctx.put("X", unit.getAttributes());
		}
		
		if(target != null) {
			ctx.put("Y", target.getAttributes());
		}
		
		return (T) RhinoHelper.invoke(expression, ctx, resultType);
	}

	/**
	 * 计算DEBUFF的效果值
	 * 
	 * @param  expression		效果公式
	 * @param  params			需要传入的参数
	 * @return {@link Integer}	返回的伤害值
	 */
	public static int getDebufferEffectValue(String expression, Number...params) {
		return RhinoHelper.invoke(expression, params).intValue();
	}
	
	/**
	 * 验证是否攻击命中
	 * 
	 * @param  context				战斗上下文
	 * @param  skillEffectType		技能类型
	 * @param  attacker				攻击者属性
	 * @param  targeter				被攻击者属性
	 */
	public static boolean validAttackHit(Context fieldContext, int skillEffectType, FightAttribute attacker, FightAttribute targeter) {
		Boolean isHitTarget = fieldContext.getHitTargets(targeter.getUnitId());
		if(isHitTarget == null) {
			boolean isIgnore = FightRule.isIgnoreDodge(skillEffectType);
			Fightable targeterable = targeter.getAttributes();
			Fightable attackerable = attacker.getAttributes();
			int attackHit = attackerable.getAttribute(AttributeKeys.HIT);
			
			int attackLevel = attackerable.getAttribute(AttributeKeys.LEVEL);
			int targetLevel = targeterable.getAttribute(AttributeKeys.LEVEL);
			int randomHitRate = Tools.getRandomInteger(AttributeKeys.RATE_BASE);	//随机命中概率
			int targetDodge = isIgnore ? 0 : targeterable.getAttribute(AttributeKeys.DODGE);
			
			
			int attackHitRate = FormulaHelper.invoke(HIT_RATE_FORMULA, attackLevel, attackHit).intValue();
			int targetDodgeRate = FormulaHelper.invoke(DODGE_RATE_FORMULA, targetLevel, targetDodge).intValue();
			
			int currentHitRate = FormulaHelper.invoke(FIGHT_HIT_FORMULA, attackHitRate, targetDodgeRate, attackLevel, targetLevel).intValue();
			isHitTarget = randomHitRate < currentHitRate;
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("技能命中概率: [{}] 随机概率:[{}] 是否命中: [{}] ",  new Object[] { currentHitRate, randomHitRate, isHitTarget });
			}
			fieldContext.updateHitTargets(targeter.getUnitId(), isHitTarget);		
		}
		return isHitTarget;
	}
	
	/**
	 * 验证技能的触发概率 
	 * 
	 * @param  target			被攻击者属性
	 * @param  skillEffect		技能效果对象
	 * @return {@link Boolean}	true-可以定身, false-不可以定身
	 */
	public static boolean validSkillTrigger(FightAttribute target, SkillEffectConfig skillEffect) {
		//n1:(技能组合表)效果触发几率 ； n2:防御方抗眩晕属性值
		int rateValue = skillEffect.getRateValue(0);
		Fightable attributes = target.getAttributes();
		int randomRate = Tools.getRandomInteger(AttributeKeys.RATE_BASE) + 1;
		int immobilizeDefense = attributes.get(AttributeKeys.IMMOBILIZE_DEFENSE);
		int immobilizeValue = FormulaHelper.invoke(FormulaKey.IMMOBILIZE_DEFENSE_FORMULA, rateValue, immobilizeDefense).intValue();
		return randomRate <= immobilizeValue;
	}
	
	/**
	 * 是否攻击暴击
	 * 
	 * @param  skillId			技能ID
	 * @param  attacker			攻击者战斗属性
	 * @param  targeter			被攻击者战斗属性
	 * @return {@link Boolean}	是否暴击. true-暴击, false-未暴击
	 */
	public static boolean isAttackCritical(FightAttribute attacker, FightAttribute targeter) {
		int currentCriticalRate = 0;
		Fightable attackerable = attacker.getAttributes();
		Fightable targeterable = targeter.getAttributes();
		FightCasting attackCasting = attacker.getFightCasting();
		int attackLevel = attackerable.getAttribute(AttributeKeys.LEVEL);
		int currentRandom = Tools.getRandomInteger(AttributeKeys.RATE_BASE);
		if(attackCasting == FightCasting.PHYSICAL) {			//物理伤害
			int physicalDefense = targeterable.getAttribute(AttributeKeys.PHYSICAL_DEFENSE);
			int physicalCritical = attackerable.getAttribute(AttributeKeys.PHYSICAL_CRITICAL);
			int criticalValue = FormulaHelper.invoke(PHYSICAL_CRITICAL_RATE_FORMULA, attackLevel, physicalCritical).intValue();
			currentCriticalRate = FormulaHelper.invoke(FIGHT_PHYSICAL_FORMULA, criticalValue, attackLevel, physicalDefense).intValue();
		} else if(attackCasting == FightCasting.THEURGY) {		//法术伤害
			int theurgyDefense = targeterable.getAttribute(AttributeKeys.THEURGY_DEFENSE);
			int theurgyCritical = attackerable.getAttribute(AttributeKeys.THEURGY_CRITICAL);
			int criticalValue = FormulaHelper.invoke(THEURGY_CRITICAL_RATE_RATE, attackLevel, theurgyCritical).intValue();
			currentCriticalRate = FormulaHelper.invoke(FIGHT_THEURGY_CRITICAL_FORMULA, criticalValue, attackLevel, theurgyDefense).intValue();
		}
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("暴击概率: [{}] 随机概率:[{}] 是否暴击: [{}] ",  new Object[] { currentCriticalRate , currentRandom, currentRandom < currentCriticalRate});
		}
		return currentRandom < currentCriticalRate;
	}
	
	/**
	 * 是否连击
	 * 
	 * @param  skillId			技能ID
	 * @param  attacker			攻击者属性对象
	 * @param  targeter			被攻击者属性对象
	 * @return {@link Boolean}	是否连击
	 */
	public static boolean isTriggerComboAttack(int skillId, FightAttribute attacker, FightAttribute targeter) {
		return Tools.getRandomInteger(AttributeKeys.RATE_BASE) < 500;
	}
	
	/**
	 * 计算攻击伤害值
	 * 
	 * <pre>
	 * <li>
	 * 物攻技能普通伤害 n1:物理攻击力;n2:物理防御力;n3:攻击方等级;n4:防御方等级;n5:技能加成效果;n6:技能直接效果
	 * Math.floor(Math.max(3*n1-2*n2,n1*0.25)*Math.max(1+(n3-n4)*0,0)*(1+n5)+n6)
	 * </li>
	 * <li>
	 * 法攻技能普通伤害 n1:法术攻击力;n2:法术防御力;n3:攻击方等级;n4:防御方等级;n5:技能加成效果;n6:技能直接效果
	 * Math.floor(Math.max(3*n1-2*n2,n1*0.25)*Math.max(1+(n3-n4)*0,0)*(1+n5)+n6)
	 * </li>
	 * <li>
	 * 物攻技能暴击伤害 n1:物理攻击力;n2:物理防御力;n3:攻击方等级;n4:防御方等级;n5:技能加成效果;n6:技能直接效果
	 * Math.floor((Math.max(3*n1-2*n2,n1*0.25)*Math.max(1+(n3-n4)*0,0)*(1+n5)+n6)*2)
	 * </li>
	 * <li>
	 * 法攻技能暴击伤害 n1:法术攻击力;n2:法术防御力;n3:攻击方等级;n4:防御方等级;n5:技能加成效果;n6:技能直接效果
	 * Math.floor((Math.max(3*n1-2*n2,n1*0.25)*Math.max(1+(n3-n4)*0,0)*(1+n5)+n6)*2)
	 * </li>
	 * </pre>
	 * 
	 * @param  attackerDamageVO			攻击伤害VO对象
	 * @return {@link Integer} 			技能的伤害值
	 */
	public strictfp static int calculateFightingAttackDamage(AttackDamageVO attackerDamageVO) {
		int formulaId = 0;
		int attackerAttack = 0;
		int targeterDefense = 0;
		boolean isCritical = attackerDamageVO.isCritical();
		Fightable attackerable = attackerDamageVO.getAttacker().getAttributes();
		Fightable targeterable = attackerDamageVO.getTargeter().getAttributes();
		FightCasting fightCasting = attackerDamageVO.getAttacker().getFightCasting();
		int attackLevel = attackerable.getAttribute(AttributeKeys.LEVEL);	//攻击方等级
		int targetLevel = targeterable.getAttribute(AttributeKeys.LEVEL);	//防守方等级
		if(fightCasting == FightCasting.PHYSICAL) {
			attackerAttack = attackerable.getAttribute(AttributeKeys.PHYSICAL_ATTACK);		//攻击方物理攻击
			targeterDefense = targeterable.getAttribute(AttributeKeys.PHYSICAL_DEFENSE);	//防守方的物理防御
			formulaId = isCritical ? PHYSICAL_CRITICAL_DAMAGE_FORMULA : PHYSICAL_ATTACK_DAMAGE_FORMULA;
		} else if(fightCasting == FightCasting.THEURGY){
			attackerAttack = attackerable.getAttribute(AttributeKeys.THEURGY_ATTACK);	//攻击方法术攻击
			targeterDefense = targeterable.getAttribute(AttributeKeys.THEURGY_DEFENSE);	//防守方的法术防御
			formulaId = isCritical ? THEURGY_CRITICAL_DAMAGE_FORMULA : THEURGY_ATTACK_DAMAGE_FORMULA;
		}
		
		int calculateDamage = FormulaHelper.invoke(formulaId, attackerAttack, 
						targeterDefense, attackLevel, targetLevel).intValue();
		
		//[0.99, 1.01] 浮动值
		int baseDamageValue = (int) (calculateDamage * 0.99);		// 90%的固定伤害量
		int tweenPercentValue = (int) (calculateDamage * 0.01);		// 20%的浮动值
		int finalValue = baseDamageValue + Tools.getRandomInteger(tweenPercentValue + 1);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("最低攻击值:[{}] 最高攻击值:[{}] 当前值:[{}]", new Object[] { baseDamageValue, baseDamageValue + baseDamageValue, finalValue});
		}
		return finalValue;
	}
	
	/**
	 * 计算暴击概率
	 * 
	 * @param  damageValue		伤害值
	 * @param  isCritical		是否暴击. true-暴击, false-为暴击
	 * @return {@link Integer}	计算后的值
	 */
	public static int calculateCriticalValue(int damageValue, boolean isCritical) {
		return damageValue * (isCritical ? 2 : 1);
	}
	
	private static final int[] IGNORE_ARRAYS = { ADD_STRENGTH.getCode(),		ADD_MP.getCode(),	
												 ADD_DEXERITY.getCode(),		ADD_HP.getCode(), 
												 ADD_INTELLECT.getCode(),		ADD_HIT.getCode(),
												 ADD_SPIRITUALITY.getCode(),	ADD_PIERCE.getCode(),
												 ADD_CONSTITUTION.getCode(),	ADD_DODGE.getCode(),
												 ADD_PHYSICAL_DEFENSE.getCode(),ADD_BLOCK.getCode(),
												 ADD_THEURGY_DEFENSE.getCode(),	ADD_PHYSICAL_CRITICAL.getCode(),
												 ADD_THEURGY_CRITICAL.getCode(),ADD_THEURGY_ATTACK.getCode(),			
												 ADD_PHYSICAL_ATTACK.getCode(),	ADD_RAPIDLY.getCode(),
												 ADD_DUCTILITY.getCode(),		ADD_MOVE_SPEED.getCode(),	
												 HOT_PERCENT_EFFECT.getCode(),	ADD_MP_MAX.getCode(), 
												 HOT_FIXED_EFFECT.getCode(),	ADD_HP_MAX.getCode(),	
												 DOT_PERCENT_AND_FIXED_EFFECT.getCode()};
	
	/**
	 * 是否忽略躲闪
	 * 
	 * @param  skillEffectType	技能类型
	 * @return {@link Boolean}	true-忽略躲闪, false-不忽略躲闪
	 */
	public static boolean isIgnoreDodge(int skillEffectType) {
		return ArrayUtils.contains(IGNORE_ARRAYS, skillEffectType);
	}
}
