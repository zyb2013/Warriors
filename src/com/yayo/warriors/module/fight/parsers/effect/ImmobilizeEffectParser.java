package com.yayo.warriors.module.fight.parsers.effect;

import org.springframework.stereotype.Component;

import com.yayo.warriors.basedb.model.SkillConfig;
import com.yayo.warriors.basedb.model.SkillEffectConfig;
import com.yayo.warriors.module.buffer.model.Buffer;
import com.yayo.warriors.module.fight.helper.BufferHelper;
import com.yayo.warriors.module.fight.model.ChangeBuffer;
import com.yayo.warriors.module.fight.model.Context;
import com.yayo.warriors.module.fight.model.FightAttribute;
import com.yayo.warriors.module.fight.model.FightReport;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.fight.parsers.effect.context.AbstractSkillEffectParser;
import com.yayo.warriors.module.fight.rule.FightRule;
import com.yayo.warriors.module.skill.type.SkillEffectType;
import com.yayo.warriors.module.user.model.Fightable;

/**
 * 定身效果 DEBUFF 的效果解析器
 * 
 * @author Hyint
 */
@Component
public class ImmobilizeEffectParser extends AbstractSkillEffectParser {
	
	
	protected int getType() {
		return SkillEffectType.IMMOBILIZE_EFFECT.getCode();
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
		UnitId attackId = attacker.getUnitId();
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
		
		SkillConfig skill = skillEffect.getSkillConfig();
		if(skill == null) {
			return;
		}

		int skillLevel = attackSkillable.getAttribute(skillId);								//技能等级
		Buffer unitBuffer = BufferHelper.getUnitBuffer(targeter.getBuffers(), bufferType);
		if(!BufferHelper.canAddBuffer2Unit(unitBuffer, skillLevel)) {
			return;
		}
		
		//验证技能效果触发几率. 这里还需要细细看
		if(!FightRule.validSkillTrigger(targeter, skillEffect)) {
			return;
		}
		
		int cycle = skillEffect.getCycle();
		int delayMoves = skill.getDelayMoves();
		int effectTime = skillEffect.getEffectTime();
		long endTime = System.currentTimeMillis() + effectTime;
		if(unitBuffer != null) {
			targeter.removeBuffer(unitBuffer.getId());
			context.addFightReport(FightReport.buffer(targetId, skillEffectId, 0));
			context.addChangeBuffers(targetId, ChangeBuffer.removeBuffer( unitBuffer.getId()));
		}
		
		context.addFightReport(FightReport.buffer(targetId, skillEffectId, endTime + delayMoves));
		targeter.addBuffer(Buffer.valueOf(skillEffectId, skillLevel, delayMoves, 0, cycle, endTime, attackId));
		context.addChangeBuffers(targetId, ChangeBuffer.addDeBuffer(skillEffectId, skillLevel, cycle, delayMoves, endTime, 0, attackId));
	}

}
