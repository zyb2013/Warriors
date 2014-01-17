package com.yayo.warriors.module.fight.parsers.combat;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.basedb.model.SkillEffectConfig;
import com.yayo.warriors.module.buffer.entity.UserBuffer;
import com.yayo.warriors.module.buffer.model.Buffer;
import com.yayo.warriors.module.fight.helper.BufferHelper;
import com.yayo.warriors.module.fight.helper.FightHelper;
import com.yayo.warriors.module.fight.model.Context;
import com.yayo.warriors.module.fight.model.FightAttribute;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.fight.parsers.combat.context.AbstractFightParser;
import com.yayo.warriors.module.fight.parsers.combat.context.FightParserContext;
import com.yayo.warriors.module.fight.parsers.effect.context.SkillEffectContext;
import com.yayo.warriors.module.fight.parsers.effect.context.SkillEffectParser;
import com.yayo.warriors.module.fight.rule.FightRule;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.pet.entity.Pet;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.skill.entity.UserSkill;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.type.ElementType;

/**
 * 人类攻击家将解析器
 * 
 * @author Hyint
 */
@Component
public class PersonToPetParser extends AbstractFightParser {
	@Autowired
	private SkillEffectContext skillEffectContext;
	
	
	protected String getType() {
		return FightParserContext.toType(ElementType.PLAYER, ElementType.PET);
	}

	/**
	 * 开始战斗
	 * 
	 * @param  context				战斗上下文
	 * @param  attacker				攻击者ID
	 * @param  targeter				被攻击者ID
	 * @param  skillEffect			技能效果对象
	 */
	
	public void actionAttack(Context context, ISpire attacker, ISpire targeter, SkillEffectConfig skillEffect) {
		int skillId = skillEffect.getSkillId();

		UnitId attackUnitId = attacker.getUnitId();
		UnitId targetUnitId = targeter.getUnitId();
		PetDomain targeterDomain = (PetDomain) targeter;
		long targetOwnerId = targeterDomain.getPlayerId();
		UserDomain attackerDomain = (UserDomain) attacker;
		PetBattle targetBattle = targeterDomain.getBattle();
		UserSkill attackSkill = attackerDomain.getUserSkill();
		Context fieldContext = Context.defaultContext(context);		//初始化默认上下文
		
		Pet targetPet = targeterDomain.getPet();
		PlayerBattle attackBattle = attackerDomain.getBattle();
		UserBuffer attackBuffer = attackerDomain.getUserBuffer();
		ChainLock lock = LockUtils.getLock(attackBattle, targetBattle);
		try {
			lock.lock();
			if(!userManager.isOnline(targetOwnerId)) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("被攻击者:[{}] 不在线, 不能发起攻击 ", attackUnitId);
				}
				return;
			} 

			if(!userManager.isOnline(attacker.getId())) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("攻击者:[{}] 不在线, 不能发起攻击", attacker);
				}
				return;
			} 

			if(attackBattle.isDead()) {
				context.addAttributeChanges(attackUnitId, AttributeKeys.HP, 0);
				return;
			}
			
			if(targetBattle.isDeath()) {
				context.addAttributeChanges(targetUnitId, AttributeKeys.HP, 0);
				return;
			}
			
			FightAttribute attackAttribute = FightHelper.getPlayerAttribute(attackerDomain, attackBattle, attackSkill, attackBuffer);
			if(attackAttribute.isDead()) {
				context.addAttributeChanges(attackUnitId, AttributeKeys.HP, 0);
				return;
			}
			
			Map<Integer, Buffer> attackBuffers = attackAttribute.getBuffers();
			if(BufferHelper.isUnitInImmobilize(attackBuffers)) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("攻击者:[{}] 有定身状态, 不能发起攻击 ", attacker);
				}
				return;
			}
			
			FightAttribute targetAttribute = FightHelper.getPetAttribute(targeterDomain, targetPet, targetBattle);
			if(targetAttribute.isDead()) {
				context.addAttributeChanges(targetUnitId, AttributeKeys.HP, 0);
				return;
			}
			
			int skillEffectType = skillEffect.getEffectType();
			if(!FightRule.validAttackHit(fieldContext, skillEffectType, attackAttribute, targetAttribute)) {
				context.updateHitTargets(targetUnitId, false);
				context.addMissFightReport(targetUnitId, skillId);
				return;
			}
			
			SkillEffectParser parser = skillEffectContext.getParser(skillEffectType);
			parser.parser(fieldContext, attackAttribute, targetAttribute, skillEffect);
			FightHelper.reducePlayerAttribute(attackBattle, fieldContext.getAttributeChanges(attackUnitId));
			FightHelper.reduceUserPetAttribute(targetBattle, fieldContext.getAttributeChanges(targetUnitId));
			FightHelper.updateFightingContext(context, fieldContext, attackAttribute, targetAttribute);
		} finally {
			lock.unlock();
		}
		FightHelper.updateChangeBuffer(fieldContext.getChangeBuffers());
		return;
	}
}
