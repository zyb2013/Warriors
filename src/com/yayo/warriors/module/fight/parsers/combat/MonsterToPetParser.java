package com.yayo.warriors.module.fight.parsers.combat;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.basedb.model.SkillEffectConfig;
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
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.model.MonsterBattle;
import com.yayo.warriors.module.monster.model.MonsterBuffer;
import com.yayo.warriors.module.pet.entity.Pet;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.type.ElementType;

/**
 * 怪物攻击家将解析器
 * 
 * @author Hyint
 */
@Component
public class MonsterToPetParser extends AbstractFightParser {
	@Autowired
	private SkillEffectContext skillEffectContext;
	
	
	protected String getType() {
		return FightParserContext.toType(ElementType.MONSTER, ElementType.PET);
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
		UnitId attackUnitId = attacker.getUnitId();
		UnitId targetUnitId = targeter.getUnitId();
		PetDomain targetDomain = (PetDomain) targeter;
		PetBattle targetBattle = targetDomain.getBattle();
		
		Pet targetPet = targetDomain.getPet();					//被攻击的家将
		long ownerId = targetPet.getPlayerId();					//被攻击者的主人
		Context fieldContext = Context.defaultContext(context);	//初始化默认上下文
		MonsterDomain attackDomain = (MonsterDomain) attacker;
		MonsterBattle attackBattle = attackDomain.getMonsterBattle();
		MonsterBuffer attackerBuffer = attackDomain.getMonsterBuffer(true);
		
		ChainLock lock = LockUtils.getLock(attackBattle, targetBattle);
		try {
			lock.lock();
			if(attackBattle.isDead()) {
				context.addAttributeChanges(attackUnitId, AttributeKeys.HP, 0);
			} 

			if(targetBattle.isDeath()) {
				context.addAttributeChanges(targetUnitId, AttributeKeys.HP, 0);
				return;
			}
			
			if(!userManager.isOnline(ownerId)) {
				return;
			}
			
			FightAttribute attackAttribute = FightHelper.getMonsterAttribute(attackDomain, attackBattle, attackerBuffer);
			if(attackAttribute.isDead()) {
				context.addAttributeChanges(attackUnitId, AttributeKeys.HP, 0);
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("攻击者:[{}] 已死亡, 不能发起攻击", targetUnitId);
				}
				return;
			}
			
			Map<Integer, Buffer> attackBuffers = attackAttribute.getBuffers();
			if(BufferHelper.isUnitInImmobilize(attackBuffers)) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("攻击者:[{}] 有定身状态, 不能发起攻击 ", attackUnitId);
				}
				return;
			}
			
			FightAttribute targetAttribute = FightHelper.getPetAttribute(targetDomain, targetPet, targetBattle);
			if(targetAttribute.isDead()) {
				context.addAttributeChanges(targetUnitId, AttributeKeys.HP, 0);
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("被攻击者:[{}] 已死亡, 不能发起攻击", targeter);
				}
				return;
			}
			
			Boolean isHitTargets = context.getHitTargets(targetUnitId);
			if(isHitTargets != null && !isHitTargets) {
				return;
			}
			
			int skillId = skillEffect.getSkillId();				//技能ID
			int effectType = skillEffect.getEffectType();		//技能类型
			if(!FightRule.validAttackHit(fieldContext, effectType, attackAttribute, targetAttribute)) {
				context.updateHitTargets(targetUnitId, false);
				context.addMissFightReport(targetUnitId, skillId);
				return;
			}
			
			SkillEffectParser parser = skillEffectContext.getParser(skillEffect.getEffectType());
			parser.parser(fieldContext, attackAttribute, targetAttribute, skillEffect);
			FightHelper.reduceMonsterAttribute(attackBattle, fieldContext.getAttributeChanges(attackUnitId));
			FightHelper.reduceUserPetAttribute(targetBattle, fieldContext.getAttributeChanges(targetUnitId));
			FightHelper.updateFightingContext(context, fieldContext, attackAttribute, targetAttribute);
		} finally {
			lock.unlock();
		}
		FightHelper.updateChangeBuffer(fieldContext.getChangeBuffers());
		return;
	}
}
