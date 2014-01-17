package com.yayo.warriors.module.fight.helper;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.warriors.basedb.model.SkillConfig;
import com.yayo.warriors.basedb.model.SkillEffectConfig;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.module.fight.model.Context;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.fight.parsers.combat.context.FightParser;
import com.yayo.warriors.module.fight.parsers.combat.context.FightParserContext;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.type.ElementType;

/**
 * 战斗方法
 * 
 * @author Hyint
 */
@Component
public class FightProcessor {
	
	private static final ObjectReference<FightProcessor> ref = new ObjectReference<FightProcessor>();
	
	@Autowired
	private FightParserContext fightParserContext;
	
	@PostConstruct
	protected void init() {
		ref.set(this);
	}
	
	/** 获得战斗方法对象 */
	private static FightProcessor getInstance() {
		return ref.get();
	}
	
	/**
	 * 自动战斗处理
	 * 
	 * @param  context			战斗上下文
	 * @param  attacker			攻击者ID
	 * @param  skill			用户技能对象
	 * @param  targets			被攻击者ID数组
	 */
	public static void processSkillEffectToFight(Context context, ISpire attacker, SkillConfig skill, Map<Integer, List<ISpire>> targets) {
		context.addPlayerFightings(attacker);
		ElementType attackType = attacker.getType();
		List<SkillEffectConfig> skillEffectList = skill.getSkillEffects();
		for (SkillEffectConfig skillEffect : skillEffectList) {					//迭代技能效果
			int skillEffectId = skillEffect.getId();							//技能效果ID
			List<ISpire> unitIdList = targets.get(skillEffectId);				//验证攻击单位.
			if(unitIdList == null || unitIdList.isEmpty()) {					//被攻击单位不存在.
				continue;
			}
			
			boolean ignoreHitAttack = skillEffect.isIgnoreHitAttack();
			for (ISpire targeter : unitIdList) {								//计算效果
				UnitId targetUnitId = targeter.getUnitId();
				Boolean isHitTarget = context.getHitTargets(targetUnitId);		//null为没有计算过, true-命中, false-未命中
				if(isHitTarget == null || isHitTarget || ignoreHitAttack) {		//该技能未命中, 所以直接跳过
					context.addPlayerFightings(targeter);
					ElementType targetType = targeter.getType();
					FightParser parser = getFightParser(attackType, targetType);
					parser.actionAttack(context, attacker, targeter, skillEffect);
				}
			}
		}
		
		FightHelper.updatePlayerFightStatus(context.getPlayerFightings());
		//处理战斗单位转换坐标点
		FightHelper.processTotalFightUnitChangePoint(context);
	}
	
	/**
	 * 获得战斗解析器
	 * 
	 * @param  attackType		攻击者类型
	 * @param  targetType		被攻击者类型
	 * @return
	 */
	private static FightParser getFightParser(ElementType attackType, ElementType targetType) {
		return getInstance().fightParserContext.getParser(attackType, targetType);
	}
}
