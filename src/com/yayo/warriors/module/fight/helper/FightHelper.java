package com.yayo.warriors.module.fight.helper;

import static com.yayo.warriors.type.ElementType.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.EnumUtils;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.basedb.model.SkillConfig;
import com.yayo.warriors.basedb.model.SkillEffectConfig;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.buffer.entity.UserBuffer;
import com.yayo.warriors.module.buffer.facade.BufferFacade;
import com.yayo.warriors.module.buffer.model.Buffer;
import com.yayo.warriors.module.fight.model.AreaContext;
import com.yayo.warriors.module.fight.model.AreaVO;
import com.yayo.warriors.module.fight.model.ChangeBuffer;
import com.yayo.warriors.module.fight.model.Context;
import com.yayo.warriors.module.fight.model.FightAttribute;
import com.yayo.warriors.module.fight.model.FightReport;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.fight.type.FightCasting;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.GameScreen;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.facade.MonsterFacade;
import com.yayo.warriors.module.monster.model.MonsterBattle;
import com.yayo.warriors.module.monster.model.MonsterBuffer;
import com.yayo.warriors.module.pet.entity.Pet;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.skill.entity.UserSkill;
import com.yayo.warriors.module.skill.model.SkillVO;
import com.yayo.warriors.module.skill.type.SkillEffectType;
import com.yayo.warriors.module.skill.type.TargetSite;
import com.yayo.warriors.module.team.facade.TeamFacade;
import com.yayo.warriors.module.team.model.Team;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.PlayerStatus;
import com.yayo.warriors.module.user.model.StatusElement;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.module.user.type.FightMode;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.module.user.type.Job;
import com.yayo.warriors.module.user.type.StatusType;
import com.yayo.warriors.type.ElementType;

/**
 * 战斗帮助类
 * 
 * @author Hyint
 */
@Component
public class FightHelper {
	@Autowired
	private PetManager petManager;
	@Autowired
	private MapFacade mapFacade;
	@Autowired
	private TeamFacade teamFacade;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BufferFacade bufferFacade;
	@Autowired
	private MonsterFacade monsterFacade;
	@Autowired
	private ResourceService resourceService;

	private static final Logger LOGGER = LoggerFactory.getLogger(FightHelper.class);
	private static ObjectReference<FightHelper> ref = new ObjectReference<FightHelper>();
	
	@PostConstruct
	protected void init() {
		ref.set(this);
	}
	
	/**
	 * 获得战斗帮助类的实例
	 * 
	 * @return {@link FightHelper}
	 */
	private static FightHelper getInstance() {
		return ref.get();
	}
	
	/**
	 * 更新战斗上下文
	 * 
	 * @param totalContext		总的战斗上下文
	 * @param newContext		新的战斗上下文
	 */
	public static void updateFightingContext(Context totalContext, Context newContext, FightAttribute...attributes) {
		totalContext.updateCritical(newContext.isCritical());
		totalContext.setCasterPoint(newContext.getCasterPoint());
		totalContext.setTargetPoint(newContext.getTargetPoint());
		totalContext.getHitTargets().putAll(newContext.getHitTargets());				//更新命中状态
		getInstance().calculateContextDamageValue(totalContext, newContext);			//更新战斗信息
		getInstance().calculateContextFightReports(totalContext, newContext);			//战报信息
		getInstance().calculateContextChangeBuffers(totalContext, newContext);			//BUFF变换
		getInstance().calculateContextFightDeadUnits(totalContext, attributes);			//战斗单位死亡
		getInstance().calculateContextAttributeChanges(totalContext, newContext);		//属性变换
		totalContext.getChangePointUnits().putAll(newContext.getChangePointUnits());	//场景切换
	}
	
	/**
	 * 计算上下文伤害量
	 * 
	 * @param totalContext		总的任务上下文
	 * @param newContext		新的上下文
	 */
	private void calculateContextDamageValue(Context totalContext, Context newContext) {
		for (Entry<ISpire, Integer> entry : newContext.getHurtInfo().entrySet()) {
			ISpire ispire = entry.getKey();
			Integer damageValue = entry.getValue();
			totalContext.addUnitHurtValue(ispire, damageValue == null ? 0 : damageValue);
		}
	}
	
	/**
	 * 计算战斗上下文战斗单元死亡列表
	 * 
	 * @param totalContext		总战斗上下文
	 * @param attributes		当前新的战斗上下文
	 */
	private void calculateContextFightDeadUnits(Context totalContext, FightAttribute...attributes) {
		for (FightAttribute attribute : attributes) {
			if(attribute != null && attribute.isDead()) {
				totalContext.addFightDeadUnit(attribute.getiSpire());
			}
		}
	}

	/**
	 * 计算战斗上下文战报变化
	 * 
	 * @param totalContext		总战斗上下文
	 * @param newContext		当前新的战斗上下文
	 */
	private void calculateContextFightReports(Context totalContext, Context newContext) {
		List<FightReport> fightReports = newContext.getFightReports();
		if(fightReports != null && !fightReports.isEmpty()) {
			totalContext.getFightReports().addAll(fightReports);
		}
	}
	
	/**
	 * 计算战斗上下文属性值变化
	 * 
	 * @param totalContext		总战斗上下文
	 * @param newContext		当前新的战斗上下文
	 */
	private void calculateContextChangeBuffers(Context totalContext, Context newContext) {
		Map<UnitId, List<ChangeBuffer>> changeBuffers = newContext.getChangeBuffers();
		if(changeBuffers == null || changeBuffers.isEmpty()) {
			return;
		}
		
		for (Entry<UnitId, List<ChangeBuffer>> entry : changeBuffers.entrySet()) {
			UnitId unitId = entry.getKey();
			List<ChangeBuffer> changes = entry.getValue();
			if(unitId == null || changes == null || changes.isEmpty()) {
				continue;
			}
			
			Map<UnitId, List<ChangeBuffer>> buffers = totalContext.getChangeBuffers();
			List<ChangeBuffer> list = buffers.get(unitId);
			if(list == null) {
				list = new ArrayList<ChangeBuffer>();
				buffers.put(unitId, list);
			}
			
			list.addAll(changes);
		}
	}

	/**
	 * 计算战斗上下文属性值变化
	 * 
	 * @param totalContext		总战斗上下文
	 * @param newContext		当前新的战斗上下文
	 */
	private void calculateContextAttributeChanges(Context totalContext, Context newContext) {
		Map<UnitId, Map<Integer, Integer>> attributeChanges = newContext.getAttributeChanges();
		if(attributeChanges == null || attributeChanges.isEmpty()) {
			return;
		}
		
		for (Entry<UnitId, Map<Integer, Integer>> entry : attributeChanges.entrySet()) {
			UnitId unitId = entry.getKey();
			Map<Integer, Integer> attributes = entry.getValue();
			if(unitId == null || attributes == null || attributes.isEmpty()) {
				continue;
			}
			
			for (Entry<Integer, Integer> attributeEntry : attributes.entrySet()) {
				Integer attribute = attributeEntry.getKey();
				Integer attrValue = attributeEntry.getValue();
				if(attribute != null && attrValue != null) {
					totalContext.addAttributeChanges(unitId, attribute, attrValue);
				}
			}
		}
	}
	
	/**
	 * 获得角色区域性攻击
	 * 
	 * @param  context				战斗上下文
	 * @param  areaVO				区域VO对象
	 * @param  skillEffects			技能效果列表
	 * @return {@link AreaContext}	区域上下文对象
	 */
	public static AreaContext getFightUnitWithArea(Context context, AreaVO areaVO, SkillConfig skill) {
		AreaContext areaContext = new AreaContext();
		if(context == null || areaVO == null || skill == null) {
			return areaContext;
		}
		
		//把战斗中所有参与单位的精灵设置进入列表中
		areaContext.addViewPlayerISpire(areaVO.getAttacker());
		areaContext.addViewPlayerISpire(areaVO.getTargeter());
		areaContext.addViewPlayerISpire(areaVO.getAttackOwner());
		areaContext.addViewPlayerISpire(areaVO.getTargetOwner());
		
		//技能类型.
		List<SkillEffectConfig> skillEffects = skill.getSkillEffects();
		if(skillEffects == null || skillEffects.isEmpty()) {
			return areaContext;
		}
		
		for (SkillEffectConfig skillEffect : skillEffects) {
			TargetSite targetSite = EnumUtils.getEnum(TargetSite.class, skillEffect.getTargetSite());
			switch (targetSite) {
				case MYSELF:	getInstance().toSelfUnits(areaVO, skillEffect, areaContext); 				break;
				case ENEMY:		getInstance().toEnemyUnits(context, areaVO, skillEffect, areaContext);		break;
				case FRIENDLY:	getInstance().toFriendlyUnits(areaVO, skillEffect, areaContext); 			break;
			}
		}
		return areaContext;
	}
	
	/**
	 * 构建自己的战斗单位			
	 * 
	 * @param  areaVO				区域VO
	 * @param  skillEffect			技能效果对象
	 * @param  fightUnits			战斗单位集合
	 */
	private void toSelfUnits(AreaVO areaVO, SkillEffectConfig skillEffect, AreaContext areaContext) {
		areaContext.addISpireFightUnits(skillEffect.getId(), areaVO.getAttacker());
	}

	/**
	 * 构建攻击敌方单位列表	
	 * 
	 * @param  context				上下文
	 * @param  areaVO				区域VO
	 * @param  skill				技能对象
	 * @param  skillEffect			技能效果对象
	 * @param  fightUnits			战斗单位集合
	 */
	private void toEnemyUnits(Context context, AreaVO areaVO, SkillEffectConfig skillEffect, AreaContext areaContext) {
		int grid = skillEffect.getArea();
		int skillEffectId = skillEffect.getId();
		int attackCamp = areaVO.getAttackCamp();
		ISpire attackSpire = areaVO.getAttacker();
		ISpire targetSpire = areaVO.getTargeter();
		int attackNum = skillEffect.getTargetCount();
		FightMode attackMode = areaVO.getFightMode();
		areaContext.addViewPlayerISpire(attackSpire);				//攻击者可视区域
		Set<Long> ignorePets = new HashSet<Long>();					//忽略的角色家将ID
		Set<Long> ignorePlayers = new HashSet<Long>();				//忽略的角色ID
		Set<Long> ignoreMonsters = new HashSet<Long>();				//忽略的怪物ID
		SkillConfig skill = skillEffect.getSkillConfig();			//效果所属于的技能
		switch (attackSpire.getType()) {
			case PLAYER: 
			{
				ignorePlayers.add(attackSpire.getId());					//忽略攻击者自己
				if(targetSpire != null) {								//外部已经校验好, 这里就不需要校验了.
					if(targetSpire.getType() == MONSTER){
						attackNum --;
						ignoreMonsters.add(targetSpire.getId());
						areaContext.addISpireFightUnits(skillEffectId, targetSpire);
					} else if(targetSpire.getType() == PLAYER) {
						attackNum --;
						ignorePlayers.add(targetSpire.getId());
						areaContext.addISpireFightUnits(skillEffectId, targetSpire);
					} else if(targetSpire != null && targetSpire.getType() == PET) {
						attackNum --;
						ignorePets.add(targetSpire.getId());
						areaContext.addISpireFightUnits(skillEffectId, targetSpire);
					}
				}
				
				context.addTeamMemberIds(attackSpire.getId());					// 把自己添加到组队成员列表中
				Team team = teamFacade.getPlayerTeam(attackSpire.getId());		// 查询组队
				if(team != null) {
					ignorePlayers.addAll(team.getMembers());					//忽略队伍中的成员, 则不能攻击这些成员
					context.addTeamMemberIds(team.getMembers());				// 有组队则把组队的所有人加入队伍列表
				}
				
				UserDomain userDomain = (UserDomain) attackSpire;
				Player player = userDomain.getPlayer();
				if(!player.isProtection()) { 					//攻击者是保护模式, 释放AOE技能则不能打人
					Collection<Long> memberIds = getAreaFightUnits(grid, PLAYER, areaVO);
					Set<ISpire> ispires = getFight2EnemyUnits(PLAYER, PLAYER, skill, attackMode, attackCamp, attackNum, ignorePlayers, memberIds);
					areaContext.addISpireFightUnits(skillEffectId, ispires);	//战斗的角色精灵列表
					attackNum = attackNum - ispires.size();
					if(attackNum > 0) {
						PetDomain petDomian = petManager.getFightingPet(attackSpire.getId());
						if(petDomian != null) {
							ignorePets.add(petDomian.getId());
						}
						
						Collection<Long> monsterIds = getAreaFightUnits(grid * 5, PET, areaVO);
						Set<ISpire> petUnits = getFight2EnemyUnits(PLAYER, PET, skill, attackMode, attackCamp, attackNum, ignorePets, monsterIds);
						areaContext.addISpireFightUnits(skillEffectId, petUnits);
						attackNum = attackNum - petUnits.size();
					}
				}

				if(attackNum > 0) {
					Collection<Long> monsterIds = getAreaFightUnits(grid, MONSTER, areaVO);
					Set<ISpire> monsterUnits = getFight2EnemyUnits(PLAYER, MONSTER, skill, attackMode, attackCamp, attackNum, ignoreMonsters, monsterIds);
					areaContext.addISpireFightUnits(skillEffectId, monsterUnits);
					attackNum = attackNum - monsterUnits.size();
				}
			}
			break;
			case MONSTER: {
				ignoreMonsters.add(attackSpire.getId());
				if(targetSpire != null) {
					if(targetSpire.getType() == PLAYER) {
						attackNum --;
						ignorePlayers.add(targetSpire.getId());
						areaContext.addISpireFightUnits(skillEffectId, targetSpire);	//战斗的角色精灵列表
					} else if(targetSpire.getType() == MONSTER) {
						attackNum --;
						ignoreMonsters.add(targetSpire.getId());
						areaContext.addISpireFightUnits(skillEffectId, targetSpire);	//战斗的角色精灵列表
					} else if(targetSpire.getType() == PET) {
						attackNum --;
						ignorePets.add(targetSpire.getId());
						areaContext.addISpireFightUnits(skillEffectId, targetSpire);	//战斗的角色精灵列表
					}
				}
				
				if(attackNum > 0) {//先攻击玩家.. 
					Collection<Long> memberIds = getAreaFightUnits(grid, PLAYER, areaVO);
					Set<ISpire> playerUnits = getFight2EnemyUnits(MONSTER, PLAYER, skill, attackMode, attackCamp, attackNum, ignorePlayers, memberIds);
					areaContext.addISpireFightUnits(skillEffectId, playerUnits);			//战斗单位
					attackNum -= playerUnits.size();
				}
				
				if(attackNum > 0) {	//再攻击家将 ..
					Collection<Long> petIdList = getAreaFightUnits(grid * 5, PET, areaVO);
					Set<ISpire> petUnits = getFight2EnemyUnits(MONSTER, PET, skill, attackMode, attackCamp, attackNum, ignorePets, petIdList);
					areaContext.addISpireFightUnits(skillEffectId, petUnits);			//战斗单位
				}
			}
			break;
			case PET: //攻击者是家将
			{	
				PetDomain petDomain = (PetDomain) attackSpire;
				ignorePets.add(attackSpire.getId());
				ignorePlayers.add(petDomain.getPlayerId());
				if(targetSpire != null) {
					if(targetSpire.getType() == MONSTER){
						attackNum --;
						ignoreMonsters.add(targetSpire.getId());
						areaContext.addISpireFightUnits(skillEffectId, targetSpire);	//战斗的角色精灵列表
					} else if(targetSpire.getType() == PLAYER) {
						attackNum --;
						ignorePlayers.add(targetSpire.getId());
						areaContext.addISpireFightUnits(skillEffectId, targetSpire);	//战斗的角色精灵列表
					} else if(targetSpire.getType() == PET) {
						attackNum --;
						ignorePets.add(targetSpire.getId());
						areaContext.addISpireFightUnits(skillEffectId, targetSpire);	//战斗的角色精灵列表
					}
				}
				
				
				long petOwnerPlayerId = petDomain.getPlayerId();						//家将所属的角色ID
				context.addTeamMemberIds(petOwnerPlayerId);								// 把自己添加到组队成员列表中
				Team team = teamFacade.getPlayerTeam(petOwnerPlayerId);					// 查询组队
				if(team != null) {
					context.addTeamMemberIds(team.getMembers());						// 有组队则把组队的所有人加入
				}
				
				ignorePlayers.addAll(context.getTeamMemberIds());
				UserDomain userDomain = userManager.getUserDomain(petOwnerPlayerId);
				if(userDomain != null && !userDomain.getPlayer().isProtection()) {	//攻击者的主人是保护模式, 释放AOE技能则不能打人
					Collection<Long> memberIds = getAreaFightUnits(grid, PLAYER, areaVO);
					Set<ISpire> playerUnits = getFight2EnemyUnits(PET, PLAYER, skill, attackMode, attackCamp, attackNum, ignorePlayers, memberIds);
					areaContext.addISpireFightUnits(skillEffectId, playerUnits);			//战斗单位
					attackNum = attackNum - playerUnits.size();
					
					if(attackNum > 0) {
						Collection<Long> monsterIds = getAreaFightUnits(grid * 5, PET, areaVO);
						Set<ISpire> petUnits = getFight2EnemyUnits(PET, PET, skill, attackMode, attackCamp, attackNum, ignoreMonsters, monsterIds);
						areaContext.addISpireFightUnits(skillEffectId, petUnits);			//战斗单位
						attackNum = attackNum - petUnits.size();
					}
				}
				
				if(attackNum > 0) {
					Collection<Long> monsterIds = getAreaFightUnits(grid, MONSTER, areaVO);
					Set<ISpire> monsterUnits = getFight2EnemyUnits(PET, MONSTER, skill, attackMode, attackCamp, attackNum, ignoreMonsters, monsterIds);
					areaContext.addISpireFightUnits(skillEffectId, monsterUnits);			//战斗单位
				}
			}
			break;
		}
	}
	
	/**
	 * 是否可以攻击对方目标
	 * 
	 * @param  skill			技能对象
	 * @param  attackMode		攻击者的模式
	 * @param  targetMode		被攻击者的模式
	 * @return
	 */
	public static boolean canAttackPlayer(SkillConfig skill, FightMode attackMode, FightMode targetMode, int playerCamp, int targetCamp) {
		switch (attackMode) {
			case PEACE: 	return skill.validFightModeWithP2P(attackMode.ordinal());		//攻击者是和平模式
			case KILLING: 	return skill.validFightModeWithP2P(attackMode.ordinal());		//攻击者是杀戮模式		
			case CAMPING: 	return playerCamp != targetCamp;								//攻击者是阵营模式. 验证阵营模式不相等则可以攻击
		}
		return false;
	}
	
	/**
	 * 怪物攻击阵营校验器
	 * 
	 * @param  monsterCamp		怪物的阵营	
	 * @param  targetCamp		攻击目标的阵营
	 * @return {@link Boolean}	true-可以攻击/false-不可以攻击
	 */
	public static boolean monster2MonsterCampValidator(int monsterCamp, int targetCamp) {
		return monsterCamp != Camp.NONE.ordinal() && targetCamp != Camp.NONE.ordinal() && monsterCamp != targetCamp;
	}

	/**
	 * 攻击阵营校验器
	 * 
	 * @param  monsterCamp		攻击者的阵营	
	 * @param  targetCamp		攻击目标的阵营
	 * @return {@link Boolean}	true-可以攻击/false-不可以攻击
	 */
	public static boolean otherCampValidator(int attackCamp, int targetCamp) {
		return attackCamp == Camp.NONE.ordinal() || targetCamp == Camp.NONE.ordinal() || attackCamp != targetCamp;
	}

	/**
	 * 验证攻击保护模式
	 * 
	 * @param  skill			技能对象
	 * @param  player			角色对象
	 * @return {@link Boolean}	true-可以攻击, false-不能攻击
	 */
	public static boolean validProtectedAttack(SkillConfig skill, Player player) {
		Set<Integer> modes = skill.getFight2PlayerModeSet();
		return modes.contains(FightMode.KILLING.ordinal()) && !player.isProtection();
	}

	/**
	 * 验证是否可以攻击. 如果角色有无敌状态则不能攻击
	 * 
	 * @param  skill			技能对象
	 * @param  player			被攻击的角色对象
	 * @return {@link Boolean}	true-可以攻击, false-不能攻击
	 */
	public static boolean validReviveAttack(SkillConfig skill, Player player) {
		Set<Integer> modes = skill.getFight2PlayerModeSet();
		return modes.contains(FightMode.KILLING.ordinal()) && !player.isReviveProteTime();
	}
	
	
	/**
	 * 战斗单位攻击或者辅助友方
	 * 
	 * @param  playerId					角色ID
	 * @param  attackNum				攻击的人数
	 * @param  ignores					忽略列表
	 * @param  memberIdList				区域玩家ID列表
	 * @return {@link Set}				战斗单位ID列表
	 */
	private Set<ISpire> getFight2FriendlyUnits(ElementType unitType, int attackNum, Set<Long> ignores, Collection<Long> memberIdList) {
		if(memberIdList == null) {
			return Collections.emptySet();
		}
		
		if(ignores != null && !ignores.isEmpty()) {
			memberIdList.removeAll(ignores);
		}
		
		if(memberIdList.isEmpty()) {
			return Collections.emptySet();
		}
		
		Set<ISpire> spireSet = new HashSet<ISpire>();
		List<Long> targetIdList = new ArrayList<Long>(memberIdList);
		while (!targetIdList.isEmpty() && spireSet.size() < attackNum) {
			Long memberId = targetIdList.remove(Tools.getRandomInteger(targetIdList.size()));
			if(unitType == PLAYER) {
				UserDomain userDomain = userManager.getUserDomain(memberId);
				if(userDomain != null) {
					if(!userDomain.getBattle().isDead()) {
						spireSet.add(userDomain);
					}
				}
			} else if(unitType == MONSTER) {
				MonsterDomain monsterDomain = getInstance().monsterFacade.getMonsterDomain(memberId);
				if(monsterDomain != null) {
					MonsterBattle monsterBattle = monsterDomain.getMonsterBattle();
					if(!monsterBattle.isDead()) {
						spireSet.add(monsterDomain);
					}
				} 
			}
		}
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("单位类型: [{}] 战斗单位ID: [{}]", unitType, spireSet);
		}
		
		return spireSet;
	}
	
	/**
	 * 角色攻击敌人
	 * 
	 * @param  attackType				攻击者的类型
	 * @param  getType					获得列表的类型
	 * @param  skill					技能对象
	 * @param  attackMode				攻击者模式
	 * @param  attackCamp				攻击者的阵营
	 * @param  attackNum				需要获得的战斗单位数量
	 * @param  ignores					忽略的ID列表
	 * @return {@link Set}				战斗单位列表
	 */
	private Set<ISpire> getFight2EnemyUnits(ElementType attackType, ElementType getType, SkillConfig skill, FightMode attackMode, int attackCamp, int attackNum, Set<Long> ignores, Collection<Long> memberIdList) {
		if(memberIdList != null && !memberIdList.isEmpty() && ignores != null && !ignores.isEmpty()) {
			memberIdList.removeAll(ignores);
		}
		
		if(memberIdList == null || memberIdList.isEmpty()) {
			return Collections.emptySet();
		}
		
		Set<ISpire> unitIds = new HashSet<ISpire>();
		List<Long> targetIdList = new ArrayList<Long>(memberIdList);
		while (!targetIdList.isEmpty() && unitIds.size() < attackNum) {
			ISpire iSpire = null;
			Long memberId = targetIdList.remove(Tools.getRandomInteger(targetIdList.size()));
			if(getType == PLAYER) {
				iSpire = validAttackPlayer(attackType, memberId, skill, attackMode, attackCamp);
			} else if(getType == PET) {
				iSpire = validAttackPet(attackType, memberId, skill, attackMode, attackCamp);
			} else if(getType == MONSTER) {
				iSpire = validAttackMonster(attackType, memberId, skill, attackMode, attackCamp);
			}
			if(iSpire != null) {
				unitIds.add(iSpire);
			}
		}
	
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("单位类型: [{}] 战斗单位ID: [{}]", getType, unitIds);
		}
		
		return unitIds;
	}
	
	/**
	 * 验证是否可以攻击角色
	 * 
	 * @param  attackType		攻击类型
	 * @param  memberId			单位ID
	 * @param  skill			技能对象
	 * @param  attackMode		攻击者模式
	 * @param  attackCamp		攻击者阵营
	 * @return {@link ISpire}	战斗单位
	 */
	private ISpire validAttackPlayer(ElementType attackType, long memberId, SkillConfig skill, FightMode attackMode, int attackCamp) {
		UserDomain userDomain = userManager.getUserDomain(memberId);
		if(userDomain == null) {
			return null;
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		if(battle.isDead()) {
			return null;
		}
		
		int playerLevel = battle.getLevel();
		FightMode targetMode = battle.getMode();
		int targetCamp = player.getCamp().ordinal();
		if(attackType == ElementType.MONSTER) { //攻击者是怪物
			if(!monster2MonsterCampValidator(attackCamp, targetCamp)) { //同阵营的怪, 不能攻击同阵营的玩家
				return null;
			}
		} else { //攻击者是玩家 OR 家将
			if(!canAttackPlayer(skill, attackMode, targetMode, attackCamp, targetCamp)) {  //模式不匹配则直接跳过
				return null;
			} 

			//被攻击者的等级不匹配则直接跳过, 不能攻击保护模式的角色
			if(!targetMode.canBeAttack(playerLevel)) {
				return null;
			} 
			
			//保护模式中, 或者无敌CD中, 则直接不能攻击
			if(player.isProtection() || player.isReviveProteTime()) { 
				return null;
			}
		}
		return userDomain;
	}

	/**
	 * 验证是否可以攻击怪物
	 * 
	 * @param  attackType		攻击类型
	 * @param  memberId			单位ID
	 * @param  skill			技能对象
	 * @param  attackMode		攻击者模式
	 * @param  attackCamp		攻击者阵营
	 * @return {@link ISpire}	战斗单位
	 */
	private ISpire validAttackMonster(ElementType attackType, long memberId, SkillConfig skill, FightMode attackMode, int attackCamp) {
		MonsterDomain monsterDomain = getInstance().monsterFacade.getMonsterDomain(memberId);
		if(monsterDomain == null) {
			return null;
		}
		
		MonsterBattle monsterBattle = monsterDomain.getMonsterBattle();
		if(monsterBattle == null || monsterBattle.isDead()) {
			return null;
		}
		
		MonsterFightConfig monsterFight = monsterBattle.getMonsterFight();
		if(monsterFight == null) {
			return null;
		}
		
		int targetCamp = monsterDomain.getMonsterCamp();
		if(attackType == ElementType.MONSTER) { //怪物, 则要检验阵营
			if(!monster2MonsterCampValidator(attackCamp, targetCamp)) {
				return null;
			}
		} else { //攻击者是家将或者玩家
			if(!otherCampValidator(attackCamp, targetCamp)) {
				return null;
			}
		}
		return monsterDomain;
	}
	
	/**
	 * 验证是否可以攻击家将
	 * 
	 * @param  attackType		攻击类型
	 * @param  memberId			单位ID
	 * @param  skill			技能对象
	 * @param  attackMode		攻击者模式
	 * @param  attackCamp		攻击者阵营
	 * @return {@link ISpire}	战斗单位
	 */
	private ISpire validAttackPet(ElementType attackType, long memberId, SkillConfig skill, FightMode attackMode, int attackCamp) {
		PetDomain petDomain = petManager.getPetDomain(memberId);
		if(petDomain == null) {
			return null;
		} 

		Pet pet = petDomain.getPet();
		if(!pet.isFighting()) { //没有出战
			return null;
		}

		PetBattle petBattle = petDomain.getBattle();
		if(petBattle.isDeath()) {
			return null;
		}
		
		long playerId = petDomain.getPlayerId();
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return null;
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		if(battle.isDead()) {
			return null;
		}
		
		int playerLevel = battle.getLevel();
		FightMode targetMode = battle.getMode();
		int targetCamp = player.getCamp().ordinal();
		if(attackType == ElementType.MONSTER) { //攻击者是怪物
			if(!monster2MonsterCampValidator(attackCamp, targetCamp)) { //同阵营的怪, 不能攻击同阵营的玩家
				return null;
			}
		} else { //攻击者是玩家 OR 家将
			if(!canAttackPlayer(skill, attackMode, targetMode, attackCamp, targetCamp)) {  //模式不匹配则直接跳过
				return null;
			} 

			if(!targetMode.canBeAttack(playerLevel)) {
				return null;
			} 

			if(player.isProtection() || player.isReviveProteTime()) { //被攻击者的等级不匹配则直接跳过, 不能攻击保护模式的角色
				return null;
			}
		}
		return petDomain;
	}

	/**
	 * 构建自己的战斗单位			
	 * 
	 * @param  areaVO				区域VO
	 * @param  skillEffect			技能效果对象
	 * @param  areaContext			区域上下文
	 */
	private void toFriendlyUnits(AreaVO areaVO, SkillEffectConfig skillEffect, AreaContext areaContext) {
		int grid = skillEffect.getArea();
		int skillEffectId = skillEffect.getId();
		ISpire attackSpire = areaVO.getAttacker();
		ISpire targetSpire = areaVO.getTargeter();
		int attackNum = skillEffect.getTargetCount();
		FightMode attackMode = areaVO.getFightMode();
		areaContext.addViewPlayerISpire(attackSpire);
		Set<Long> ignorePlayers = new HashSet<Long>();				//忽略的角色ID
		Set<Long> ignoreMonsters = new HashSet<Long>();				//忽略的怪物ID
		SkillConfig skill = skillEffect.getSkillConfig();			//技能等级
		if(attackSpire.getType() == PLAYER) {						//攻击者是人类
			ignorePlayers.add(attackSpire.getId());					//忽略攻击者自己
			if(skill.validFightModeWithP2P(attackMode.ordinal())) {
				if(targetSpire != null && targetSpire.getType() == PLAYER) {
					attackNum --;
					ignorePlayers.add(targetSpire.getId());
					areaContext.addISpireFightUnits(skillEffectId, targetSpire);
				}
				
				Collection<Long> memberIds = getAreaFightUnits(grid, PLAYER, areaVO);
				Set<ISpire> playerUnits = getFight2FriendlyUnits(PLAYER, attackNum, ignorePlayers, memberIds);
				areaContext.addISpireFightUnits(skillEffectId, playerUnits);
			}
		} else if(attackSpire.getType() == MONSTER) {
			ignoreMonsters.add(attackSpire.getId());
			if(targetSpire != null && targetSpire.getType() == MONSTER) {
				attackNum --;
				ignoreMonsters.add(targetSpire.getId());
				areaContext.addISpireFightUnits(skillEffectId, targetSpire);
			}
			
			Collection<Long> monsterIds = getAreaFightUnits(grid, MONSTER, areaVO);
			Set<ISpire> monsterUnits = getFight2FriendlyUnits(MONSTER, attackNum, ignoreMonsters, monsterIds);
			areaContext.addISpireFightUnits(skillEffectId, monsterUnits);
		} else if(attackSpire.getType() == PET) {		//攻击者是家将
			if(skill.validFightModeWithP2P(attackMode.ordinal())) {
				if(targetSpire != null && targetSpire.getType() == PLAYER) {
					attackNum --;
					ignorePlayers.add(targetSpire.getId());
					areaContext.addISpireFightUnits(skillEffectId, targetSpire);
				}
				
				Collection<Long> memberIds = getAreaFightUnits(grid, PLAYER, areaVO);
				Set<ISpire> playerUnits = getFight2FriendlyUnits(PLAYER, attackNum, ignorePlayers, memberIds);
				areaContext.addISpireFightUnits(skillEffectId, playerUnits);
			}
		}
	}
	
	/**
	 * 验证可以战斗单元
	 * 
	 * @param  ignores			忽略战斗的战斗单位
	 * @param  grid				攻击的格子数
	 * @param  attackNum		攻击人数
	 * @param  unitType			战斗单位类型
	 * @param  areaVO			区域VO对象
	 * @return {@link Set}		被攻击单位的列表
	 */
	private Collection<Long> getAreaFightUnits(int grid, ElementType unitType, AreaVO areaVO) {
		int targetX = areaVO.getPositionX();
		int targetY = areaVO.getPositionY();
		GameMap gameMap = areaVO.getGameMap();
		return gameMap.getSpireIdCollectionByGird(targetX, targetY, grid, unitType);
	}
	
	/**
	 * 查询角色的战斗属性信息
	 * 
	 * @param  playerBattle				角色的战斗属性
	 * @return {@link FightAttribute}	战斗属性对象
	 */
	public static FightAttribute getPlayerAttribute(UserDomain userDomain, PlayerBattle battle, UserSkill userSkill, UserBuffer userBuffer) {
		Job playerJob = battle.getJob();
		FightCasting casting = playerJob.getCasting();
		FightAttribute attribute = FightAttribute.newInstance(userDomain, casting);
		getInstance().convertState2FightAttribute(battle, attribute);
		getInstance().convertUserSkill2FightAttribute(userSkill, attribute);
		getInstance().convertUserBuffer2FightAttribute(userBuffer, attribute);
		getInstance().convertUserAttribute2FightAttribute(battle, attribute);
		return attribute;
	}
	
	/**
	 * 更新角色的BUFF状态
	 * 
	 * @param  changeBuffers		变更的BUFF集合
	 */
	public static void updateChangeBuffer(Map<UnitId, List<ChangeBuffer>> changeBuffers) {
		if(changeBuffers == null || changeBuffers.isEmpty()) {
			return;
		}
		
		for (Entry<UnitId, List<ChangeBuffer>> entry : changeBuffers.entrySet()) {
			UnitId unitId = entry.getKey();
			List<ChangeBuffer> bufferList = entry.getValue();
			if(unitId == null || bufferList == null || bufferList.isEmpty()) {
				continue;
			}
			
			long memberId = unitId.getId();
			if(unitId.getType() == PLAYER) {
				getInstance().addPlayerBuffer(memberId, bufferList);
			} else if(unitId.getType() == MONSTER) {
				getInstance().addMonsterBuffer(memberId, bufferList);
			} else if(unitId.getType() == PET) {
				//这里不需要增加BUFF
			}
		}
	}
	
	/**
	 * 更新战斗单位的坐标点
	 * 
	 * @param context	战斗上下文
	 */
	public static void processTotalFightUnitChangePoint(Context context) {
		Map<ISpire, Point> changePointUnits = context.getChangePointUnits();
		if(changePointUnits == null || changePointUnits.isEmpty()) {
			return;
		}
		
		for (Entry<ISpire, Point> entry : changePointUnits.entrySet()) {
			ISpire ispire = entry.getKey();			//需要转换坐标点的角色
			Point toPoint = entry.getValue();		//角色需要飞到的目标点
			if(ispire == null || toPoint == null) {
				continue;
			}
			
			GameScreen currentScreen = ispire.getCurrentScreen();
			if(currentScreen == null) {
				continue;
			}
			
			GameMap gameMap = currentScreen.getGameMap();
			if(gameMap == null) {
				continue;
			}

			int toX = toPoint.getX();
			int toY = toPoint.getY();
			if(ispire instanceof UserDomain) {
				((UserDomain) ispire).getMotion().clearPath();	//清除行走路径
			} else if(ispire instanceof MonsterDomain) {		
				((MonsterDomain) ispire).clearPath();			//清除行走路径
			}
			
			try {
				getInstance().mapFacade.skillChangeMap(ispire, gameMap, toX, toY);
			} catch (Exception e) {
				LOGGER.error("角色处理瞬移异常: {}", e);
			}
		}
	}
	
	
	/**
	 * 增加角色的BUFF信息
	 * 
	 * @param playerId			角色ID
	 * @param changeBuffers		变更的BUFF信息
	 */
	private void addPlayerBuffer(long playerId, List<ChangeBuffer> changeBuffers) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return;
		}
		
		UserBuffer userBuffer = userDomain.getUserBuffer();
		if(userBuffer == null) {
			return;
		}
		
		boolean hasBufferModify = false;						//是否有需要重新刷新属性的BUFF增删
		boolean hasDeBufferModify = false;						//是否有需要重新刷新属性的BUFF增删
		ChainLock lock = LockUtils.getLock(userBuffer);
		try {
			lock.lock();
			for (ChangeBuffer changeBuffer : changeBuffers) {
				int bufferId = changeBuffer.getBufferId();
				boolean isBuffer = changeBuffer.isBuffer();
				hasBufferModify = isBuffer ? true : hasBufferModify;
				hasDeBufferModify = isBuffer ? hasDeBufferModify : true;
				if(!changeBuffer.isAdd()) {
					userBuffer.removeUserBuffer(bufferId, isBuffer);
					continue;
				}

				int level = changeBuffer.getLevel();
				int cycle = changeBuffer.getCycle();
				int damage = changeBuffer.getDamage();
				long endTime = changeBuffer.getEndTime();
				UnitId unitId = changeBuffer.getUnitId();
				long startTime = changeBuffer.getStartTime();
				long castId = unitId ==null ? -1L : unitId.getId();
				int unitType = unitId == null ? -1 : unitId.getType().ordinal();
				Buffer buffer = Buffer.valueOf(bufferId, level, damage, cycle, startTime, endTime, castId, unitType);
				userBuffer.addBuffer(isBuffer, buffer);
			}
			
			if(hasBufferModify) {
				userBuffer.updateBufferInfos(false);
			}
			if(hasDeBufferModify) {
				userBuffer.updateDeBufferInfos(false);
			}
		} finally {
			lock.unlock();
		}
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("用户的BUFF信息: {}", userBuffer);
		}
		
		if(hasBufferModify || hasDeBufferModify) {
			bufferFacade.addUserBufferQueue(playerId, false);
			userManager.updateFlushable(playerId, Flushable.FLUSHABLE_NORMAL);
		}
		
	}
	
	/**
	 * 增加怪物的BUFF信息
	 * 
	 * @param monsterId			怪物ID
	 * @param changeBuffers		变更的BUFF信息
	 */
	private void addMonsterBuffer(long monsterId, List<ChangeBuffer> changeBuffers) {
		if(changeBuffers == null || changeBuffers.isEmpty()) {
			return;
		}
		
		MonsterDomain monsterDomain = monsterFacade.getMonsterDomain(monsterId);
		if(monsterDomain == null) {
			return;
		}
		
		MonsterBattle monsterBattle = monsterDomain.getMonsterBattle();
		if(monsterBattle == null) {
			return;
		}

		MonsterBuffer monsterBuffer = monsterDomain.getMonsterBuffer(true);
		if(monsterBuffer == null) {
			return;
		}
		
		boolean hasBufferModify = false;
		boolean hasDeBufferModify = false;	//是否有需要重新刷新属性的BUFF增删
		ChainLock lock = LockUtils.getLock(monsterBuffer, monsterBattle);
		try {
			lock.lock();
			for (ChangeBuffer changeBuffer : changeBuffers) {
				int bufferId = changeBuffer.getBufferId();
				boolean isBuffer = changeBuffer.isBuffer();
				hasBufferModify = isBuffer ? hasBufferModify : true;
				hasDeBufferModify = !isBuffer ? hasDeBufferModify : true;
				if(!changeBuffer.isAdd()) {
					if(monsterBuffer.removeBuffer(bufferId, isBuffer)) {
						monsterBattle.updateFlushable(Flushable.FLUSHABLE_NORMAL);
					}
				} else {
					int level = changeBuffer.getLevel();
					int cycle = changeBuffer.getCycle();
					int damage = changeBuffer.getDamage();
					long endTime = changeBuffer.getEndTime();
					UnitId unitId = changeBuffer.getUnitId();
					long startTime = changeBuffer.getStartTime();
					long castId = unitId ==null ? -1L : unitId.getId();
					int unitType = unitId == null ? -1 : unitId.getType().ordinal();
					Buffer buffer = Buffer.valueOf(bufferId, level, damage, cycle, startTime, endTime, castId, unitType);
					monsterBuffer.addBuffer(buffer, isBuffer);
					monsterBattle.updateFlushable(Flushable.FLUSHABLE_NORMAL);
				}
			}
		} finally {
			lock.unlock();
		}
		
		if(hasDeBufferModify || hasBufferModify) {
			bufferFacade.addMonsterBufferQueue(monsterId);
		}
	}
	
	/**
	 * 查询角色的战斗属性信息
	 * 
	 * @param  monsterBattle			怪物战斗对象
	 * @return {@link FightAttribute}	战斗属性对象
	 */
	public static FightAttribute getMonsterAttribute(MonsterDomain monsterDomain, MonsterBattle monsterBattle, MonsterBuffer monsterBuffer) {
		FightCasting fightCasting = monsterBattle.getFightCasting();
		MonsterFightConfig monsterFight = monsterBattle.getMonsterFight();
		FightAttribute attribute = FightAttribute.newInstance(monsterDomain, fightCasting);
		attribute.setGarbbingDefense(monsterFight.isGarbbingDefense());
		attribute.setKnockFlyDefense(monsterFight.isKnockFlyDefense());
		attribute.setKnockBackDefense(monsterFight.isKnockBackDefense());
		attribute.getAttributes().addAll(monsterBattle.getAttributes());
		attribute.getBuffers().putAll(monsterBuffer.getBufferInfoMap());
		attribute.getBuffers().putAll(monsterBuffer.getDebufferInfoMap());
		attribute.getSkillable().addAll(monsterBattle.getSkillFightables());
		return attribute;
	}
	
	/**
	 * 查询角色的战斗属性信息
	 * 
	 * @param  petBattle				召唤兽战斗对象
	 * @return {@link FightAttribute}	战斗属性对象
	 */
	public static FightAttribute getPetAttribute(PetDomain petDomain, Pet pet, PetBattle battle) {
		FightCasting casting = battle.getJob().getCasting();
		FightAttribute attribute = FightAttribute.newInstance(petDomain, casting);
		attribute.getAttributes().addAll(battle.getAttributes());
		getInstance().convertPetSkillAttribute2FightAttribute(pet, attribute);
		return attribute;
	}
	
	/**
	 * 构建召唤兽技能属性
	 * 
	 * @param petBattle
	 * @param attribute
	 */
	private void convertPetSkillAttribute2FightAttribute(Pet pet, FightAttribute attribute) {
		Map<Integer, Integer> skillMap = pet.getSkillMap();
		if(skillMap == null || skillMap.isEmpty()) {
			return;
		}
		
		for (Entry<Integer, Integer> entry : skillMap.entrySet()) {
			Integer skillId = entry.getKey();
			Integer skillLevel = entry.getValue();
			attribute.getSkillable().set(skillId, skillLevel);
		}
	}
	
	/**
	 * 扣减战斗中属性消耗
	 * 
	 * @param playerBattle	角色的战斗对象	
	 * @param fightChanges	变化的值
	 */
	public static void reducePlayerAttribute(PlayerBattle playerBattle, Map<Integer, Integer> fightChanges) {
		if(playerBattle == null || fightChanges == null || fightChanges.isEmpty()) {
			return;
		}
		
		for (Entry<Integer, Integer> entry : fightChanges.entrySet()) {
			Integer attribute = entry.getKey();
			Integer attrValue = entry.getValue();
			if(attribute != null && attrValue != null && attrValue != 0) {
				int currentValue = playerBattle.getAttribute(attribute);
				playerBattle.setAttribute(attribute, Math.max(0, currentValue + attrValue));
			}
		}
	}
	
	/**
	 * 扣减怪物战斗中属性消耗
	 * 
	 * @param playerBattle	角色的战斗对象	
	 * @param fightChanges	变化的值
	 */
	public static int reduceMonsterAttribute(MonsterBattle monsterBattle, Map<Integer, Integer> fightChanges) {
		if(monsterBattle == null || fightChanges == null || fightChanges.isEmpty()) {
			return 0;
		}
		
		int totalReduce = 0;
		for (Entry<Integer, Integer> entry : fightChanges.entrySet()) {
			Integer attribute = entry.getKey();
			Integer attrValue = entry.getValue();
			if(attribute != null && attrValue != null && attrValue != 0) {
				int cacheAttrValue = monsterBattle.getAttribute(attribute);
				int maxAttributeValue = Math.max(0, cacheAttrValue + attrValue);
				monsterBattle.setAttribute(attribute, maxAttributeValue);
				if(attribute == AttributeKeys.HP && attrValue < 0) {
					totalReduce += Math.min(cacheAttrValue, Math.abs(attrValue));
				}
			}
		}
		return totalReduce;
	}
	
	/**
	 * 增加战斗伤害值到怪物身上
	 * 
	 * @param battle		被攻击的怪物
	 * @param spire			攻击者
	 * @param skillId		技能ID
	 * @param attributes	伤害属性值
	 */
	public static void addFightDamage2Monster(MonsterDomain monsterDomain, ISpire spire, int skillId, Map<Integer, Integer> attributes) {
		if(attributes != null && !attributes.isEmpty()) {
			Integer damage = attributes.get(AttributeKeys.HP);
			if(damage != null) {
				monsterDomain.addFightInfo(spire, damage, skillId);
			}
			return;
		}
	} 

	/**
	 * 更新角色的战斗状态
	 * 
	 * @param  attack
	 * @param  target
	 */
	public static void updatePlayerFightStatus(Collection<ISpire> ispires) {
		if(ispires == null || ispires.isEmpty()) {
			return;
		}
		
		for (ISpire iSpire : ispires) {
			if(iSpire == null) {
				continue;
			}
			
			try {
				if(iSpire.getType() == ElementType.PET) {
					getInstance().updatePetFightStatus((PetDomain) iSpire);
				} else if(iSpire.getType() == ElementType.PLAYER) {
					getInstance().updatePlayerFightStatus((UserDomain) iSpire);
				}
			} finally {}
		}
	}
	
	/**
	 * 更新角色的战斗状态
	 * 
	 * @param userDomain		角色域模型
	 */
	private void updatePlayerFightStatus(UserDomain userDomain) {
		PlayerBattle battle = userDomain.getBattle();
		if(battle != null) {
			battle.getPlayerStatus().updateFightStatus();
		}
	}
	
	/**
	 * 更新家将的战斗状态
	 * 
	 * @param petDomain		家将域模型
	 */
	private void updatePetFightStatus(PetDomain petDomain) {
		long playerId = petDomain.getPlayerId();
		UserDomain userDomain = getInstance().userManager.getUserDomain(playerId);
		if(userDomain != null) {
			updatePlayerFightStatus(userDomain);
		}
	}
	
	/**
	 * 扣减怪物战斗中属性消耗
	 * 
	 * @param playerBattle	角色的战斗对象	
	 * @param fightChanges	变化的值
	 */
	public static void reduceUserPetAttribute(PetBattle petBattle, Map<Integer, Integer> fightChanges) {
		if(petBattle == null || fightChanges == null || fightChanges.isEmpty()) {
			return;
		}
		
		for (Entry<Integer, Integer> entry : fightChanges.entrySet()) {
			Integer attribute = entry.getKey();
			Integer attrValue = entry.getValue();
			if(attribute != null && attrValue != null && attrValue != 0) {
				if(petBattle.constains(attribute)) {
					int cacheAttrValue = petBattle.getAttribute(attribute);
					petBattle.setAttribute(attribute, Math.max(0, cacheAttrValue + attrValue));
				}
			}
		}
	}
	
	/**
	 * 抑制治疗总概率
	 * 
	 * @param  buffers			Buffer列表
	 * @return {@link Double}	返回抑制治疗效果的概率
	 */
	public static double getRestrainTreatRates(Collection<Buffer> buffers) {
		double rates = 0D;
		if(buffers == null || buffers.isEmpty()) {
			return rates;
		}
		
		int effectType = SkillEffectType.RESTRAIN_HOT_EFFECT.getCode();
		for (Buffer buffer : buffers) {
			int effectId = buffer.getId();
			SkillEffectConfig skillEffect = getSkillEffectConfig(effectId);
			if(skillEffect == null) {
				continue;
			} 

			int skillEffectType = skillEffect.getEffectType();
			if(skillEffectType != effectType || !buffer.isStart() || buffer.isTimeOut()) {
				continue;
			}
			rates += Tools.divideAndRoundDown(buffer.getDamage(), AttributeKeys.RATE_BASE, 3);
		}
		return rates;
	}
 
	/**
	 * 查询技能效果对象
	 * 
	 * @param skillEffectId
	 * @return
	 */
	private static SkillEffectConfig getSkillEffectConfig(int skillEffectId) {
		return getInstance().resourceService.get(skillEffectId, SkillEffectConfig.class);
	}
	
	/**
	 * 构建用户属性到战斗封装对象中
	 * 
	 * @param battle			角色战斗对象
	 * @param attribute			战斗属性对象
	 */
	private void convertUserAttribute2FightAttribute(PlayerBattle battle, FightAttribute attribute) {
		if(battle != null && attribute != null) {
			attribute.getAttributes().addAll(battle.getAndCopyAttributes());
		}
	}

	/**
	 * 构建用户BUFFER到战斗属性中
	 * 
	 * @param userBuffer		用户Buffer对象
	 * @param attribute			战斗属性封装对象
	 */
	private void convertUserBuffer2FightAttribute(UserBuffer userBuffer, FightAttribute attribute) {
		if(userBuffer != null) {
			attribute.getBuffers().putAll(userBuffer.getBufferInfos());
			attribute.getBuffers().putAll(userBuffer.getDeBufferInfos());
		}
	}
	
	/**
	 * 构建状态战斗属性对象
	 * 
	 * @param battle			角色战斗属性
	 * @param attribute			战斗属性封装类
	 */
	private void convertState2FightAttribute(PlayerBattle battle, FightAttribute attribute) {
		PlayerStatus playerStatus = battle.getPlayerStatus();
		for (StatusType statusType : StatusType.values()) {
			StatusElement statusElement = playerStatus.getStatusElement(statusType);
			if(statusElement != null) {
				attribute.getStatusCache().put(statusType, statusElement);
			}
		}
	}
	
	/**
	 * 获得怪物的技能信息
	 *  
	 * @param playerId		角色ID
	 * @param attribute		战斗属性封装对象
	 */
	private void convertUserSkill2FightAttribute(UserSkill userSkill, FightAttribute attribute) {
		if(userSkill != null) {
			Map<Integer, SkillVO> skillVOMap = new HashMap<Integer, SkillVO>(5);
			skillVOMap.putAll(userSkill.getActiveSkillMap());
			skillVOMap.putAll(userSkill.getPassiveSkillMap());
			for (SkillVO skillVO : skillVOMap.values()) {
				int skillId = skillVO.getId();
				int skillLevel = skillVO.getLevel();
				attribute.getSkillable().put(skillId, skillLevel);
			}
		}
	}
	
	/**
	 * 检查被攻击伤害BUFF, 伤害的量
	 * 
	 * @param  attribute	战斗属性对象
	 * @return {@link Integer}
	 */
	public static int getBeAttackDamageValue(FightAttribute attribute) {
		if(attribute == null) {
			return 0;
		}
		
		Map<Integer, Buffer> buffers = attribute.getBuffers();
		if(buffers == null || buffers.isEmpty()) {
			return 0;
		}
		
		int totalValue = 0;
		for (Buffer buffer : buffers.values()) {
			if(buffer == null || !buffer.isStart() || buffer.isTimeOut()) {
				continue;
			}
			
			int bufferEffectId = buffer.getId();
			SkillEffectConfig skillEffect = getSkillEffectConfig(bufferEffectId);
			if(skillEffect == null || skillEffect.getEffectType() != SkillEffectType.INCREASE_BE_DAMAGE_EFFECT.getCode()) {
				continue;
			} 
			totalValue += buffer.getDamage();
		}
		return totalValue;
	}
}
