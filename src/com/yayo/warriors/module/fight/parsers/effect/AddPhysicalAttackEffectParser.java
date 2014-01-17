package com.yayo.warriors.module.fight.parsers.effect;

import org.springframework.stereotype.Component;

import com.yayo.warriors.basedb.model.SkillEffectConfig;
import com.yayo.warriors.module.buffer.model.Buffer;
import com.yayo.warriors.module.fight.helper.BufferHelper;
import com.yayo.warriors.module.fight.model.ChangeBuffer;
import com.yayo.warriors.module.fight.model.Context;
import com.yayo.warriors.module.fight.model.FightAttribute;
import com.yayo.warriors.module.fight.model.FightReport;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.fight.parsers.effect.context.AbstractSkillEffectParser;
import com.yayo.warriors.module.skill.type.SkillEffectType;
import com.yayo.warriors.module.user.model.Fightable;

/**
 * 增加物理攻击BUFF的效果解析器
 * 
 * @author Hyint
 */
@Component
public class AddPhysicalAttackEffectParser extends AbstractSkillEffectParser {
	
	
	protected int getType() {
		return SkillEffectType.ADD_PHYSICAL_ATTACK.getCode();
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
		int skillId = skillEffect.getSkillId();												//技能ID
		UnitId attackId = attacker.getUnitId();												//攻击者ID
		UnitId targetId = targeter.getUnitId();												//被攻击者ID
		if(!targetId.getType().isCanAddBuffer()) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("战斗单位:[{}] 不能被触发BUFF..", targetId);
			}
			return;
		}
		
		int skillEffectId = skillEffect.getId();											//技能效果ID
		int bufferType = skillEffect.getBuffType();										//BUFFER的类型
		Fightable attackSkillable = attacker.getSkillable();								//攻击者的技能集合
		if(BufferHelper.isUnitInImmobilize(attacker.getBuffers()) || targeter.isDead()) {
			return;
		}
		
		int skillLevel = attackSkillable.getAttribute(skillId);								//技能等级
		Buffer unitBuffer = BufferHelper.getUnitBuffer(targeter.getBuffers(), bufferType);
		if(!BufferHelper.canAddBuffer2Unit(unitBuffer, skillLevel)) {
			return;
		}
		
		int cycle = skillEffect.getCycle();
		int skillDamage = skillEffect.calcSkillEffect(1, skillLevel).intValue();
		if(skillDamage <= 0) {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("攻击者:[{}] 释放技能:[{}] 给目标者:[{}] . 效果ID:[{}], 计算伤害量:[{}] 不能触发:[{}] 型效果", 
					new Object[] { attackId, skillId, targetId, skillEffectId, 0, getType() });
			}
			return;
		}
		
		int effectTime = skillEffect.getEffectTime();
		long endTime = System.currentTimeMillis() + effectTime;
		if(unitBuffer != null) {
			targeter.removeBuffer(unitBuffer.getId());
			context.addFightReport(FightReport.buffer(targetId, skillEffectId, 0));
			context.addChangeBuffers(targetId, ChangeBuffer.removeBuffer(unitBuffer.getId()));
		}
		
		context.addFightReport(FightReport.buffer(targetId, skillEffectId, endTime));
		targeter.addBuffer(Buffer.valueOf(skillEffectId, skillLevel, skillDamage, cycle, endTime, attackId));
		context.addChangeBuffers(targetId, ChangeBuffer.addBuffer(skillEffectId, skillLevel, cycle, endTime, skillDamage, attackId));
		return;
	}


}
