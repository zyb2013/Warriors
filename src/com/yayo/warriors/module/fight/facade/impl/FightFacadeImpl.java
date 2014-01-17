package com.yayo.warriors.module.fight.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.fight.constant.FightConstant.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.basedb.adapter.SkillService;
import com.yayo.warriors.basedb.model.CoolTimeConfig;
import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.basedb.model.SkillConfig;
import com.yayo.warriors.basedb.model.SkillEffectConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.FightPushHelper;
import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.buffer.entity.UserBuffer;
import com.yayo.warriors.module.cooltime.entity.UserCoolTime;
import com.yayo.warriors.module.cooltime.manager.CoolTimeManager;
import com.yayo.warriors.module.cooltime.model.CoolTime;
import com.yayo.warriors.module.cooltime.model.PetCoolTime;
import com.yayo.warriors.module.cooltime.rule.CoolTimeRule;
import com.yayo.warriors.module.fight.facade.FightFacade;
import com.yayo.warriors.module.fight.helper.BufferHelper;
import com.yayo.warriors.module.fight.helper.FightHelper;
import com.yayo.warriors.module.fight.helper.FightProcessor;
import com.yayo.warriors.module.fight.model.AreaContext;
import com.yayo.warriors.module.fight.model.AreaVO;
import com.yayo.warriors.module.fight.model.Context;
import com.yayo.warriors.module.fight.model.FightEvent;
import com.yayo.warriors.module.fight.model.JumpInfo;
import com.yayo.warriors.module.fight.rule.FightRule;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.GameScreen;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.map.util.MapUtils;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.facade.MonsterFacade;
import com.yayo.warriors.module.monster.model.Monster;
import com.yayo.warriors.module.monster.model.MonsterBattle;
import com.yayo.warriors.module.monster.model.MonsterBuffer;
import com.yayo.warriors.module.pet.entity.Pet;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.pet.model.PetMotion;
import com.yayo.warriors.module.server.listener.LogoutListener;
import com.yayo.warriors.module.skill.entity.UserSkill;
import com.yayo.warriors.module.skill.type.CastTarget;
import com.yayo.warriors.module.skill.type.SkillType;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.FightMode;
import com.yayo.warriors.type.ElementType;

/**
 * 战斗实现类
 * 
 * @author Hyint
 */
@Component
public class FightFacadeImpl implements FightFacade, LogoutListener {
	@Autowired
	private PetManager petManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private SkillService skillService;
	@Autowired
	private CoolTimeManager cdManager;
	@Autowired
	private MonsterFacade monsterFacade;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FightFacadeImpl.class);
	
	/** 更新信息 */
	private final ConcurrentHashMap<Long, JumpInfo> JUMP_INFOS = new ConcurrentHashMap<Long, JumpInfo>(0);
	
	/**
	 * 家将攻击
	 * 
	 * @param  playerId								角色ID
	 * @param  attackId								攻击者ID
	 * @param  targetId								被攻击者ID
	 * @param  targetType							被攻击者的类型
	 * @param  skillId								使用的技能ID
	 * @param  positionX							指定的X坐标
	 * @param  positionY							指定的Y坐标
	 * @return {@link ResultObject}					返回值信息
	 */
	
	public ResultObject<FightEvent> userPetFight(long playerId, long attackId, long targetId, 
								int targetType, int skillId, int positionX, int positionY) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerBattle playerBattle = userDomain.getBattle();
		if(playerBattle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		}
		
		PetDomain petDomain = petManager.getPetDomain(attackId);
		if(petDomain == null) {
			return ResultObject.ERROR(PET_NOT_FOUND);
		} 
		
		Pet pet = petDomain.getPet();
		PetBattle petBattle = petDomain.getBattle();
		if(pet.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(petBattle.isDeath()) {
			return ResultObject.ERROR(PET_DEAD);
		}
		
		PetMotion petMotion = petDomain.getMotion();
		if(petMotion == null) {
			return ResultObject.ERROR(PET_NOT_FOUND);
		}
		
		//基础技能对象
		SkillConfig skill = skillService.getSkillConfig(skillId);
		if(skill == null) {
			return ResultObject.ERROR(SKILL_NOT_FOUND);
		} else if(!skill.isActivity()) {
			return ResultObject.ERROR(SKILL_TYPE_INVALID);
		} else if(!pet.hasSkill(skillId)) {
			return ResultObject.ERROR(SKILL_NOT_FOUND);
		}
		
		ResultObject<FightEvent> resultObject = null;
		if(skill.getCastTarget() == CastTarget.AREA.ordinal()) {
			resultObject = this.petCastAreaSkill(userDomain, petDomain, skill, positionX, positionY);
		} else if(skill.getCastTarget() == CastTarget.TARGET.ordinal() && targetType == ElementType.PLAYER.ordinal()) {
			resultObject = this.petCastSkill2Player(userDomain, petDomain, skill, targetId);
		} else if(skill.getCastTarget() == CastTarget.TARGET.ordinal() && targetType == ElementType.MONSTER.ordinal()) {
			resultObject = this.petCastSkill2Monster(userDomain, petDomain, skill, targetId);
		} else if(skill.getCastTarget() == CastTarget.TARGET.ordinal() && targetType == ElementType.PET.ordinal()) {
			resultObject = this.petCastSkill2Pet(userDomain, petDomain, skill, targetId);
		} else {
			resultObject = ResultObject.ERROR(SKILL_TYPE_INVALID);
		}
		
		if(resultObject.getResult() >= SUCCESS) {
			userDomain.getPlayer().setReviveProteTime(0L);
		}
		return resultObject;
	}

	/**
	 * 家将攻击家将
	 * 
	 * @param  pet				发起家将对象
	 * @param  petBattle		家将战斗对象
	 * @param  petMotion		家将移动对象
	 * @param  skill			基础技能对象
	 * @param  targetId			被攻击的家将对象
	 * @return
	 */
	private ResultObject<FightEvent> petCastSkill2Pet(UserDomain userDomain, PetDomain currPetDomain, SkillConfig skill, long targetId) {
		PetDomain targetPetDomain = petManager.getPetDomain(targetId);
		if(targetPetDomain == null) {
			return ResultObject.ERROR(PET_NOT_FOUND);
		} 
		
		Pet targetPet = targetPetDomain.getPet();
		PetBattle targetBattle = targetPetDomain.getBattle();
		if(targetBattle.isDeath()) {
			return ResultObject.ERROR(PET_DEAD);
		} else if(!targetPet.isFighting()) {
			return ResultObject.ERROR(FAILURE);
		}
		
		long userPetId = currPetDomain.getPetId();
		long currentPlayerId = currPetDomain.getPlayerId();
		long targetPlayerId = targetPetDomain.getPlayerId();
		if(targetPlayerId == currentPlayerId) {		//玩家的2只家将不能互相攻击
			return ResultObject.ERROR(FIGHT_SELF_INVALID);
		} else if(userPetId == targetId) {			//家将不能攻击家将自己
			return ResultObject.ERROR(FIGHT_SELF_INVALID);
		}
		
		PlayerBattle currPlayerBattle = userDomain.getBattle();
		UserDomain targetDomain = userManager.getUserDomain(targetPlayerId);
		if(targetDomain == null) {
			return ResultObject.ERROR(TARGET_NOT_FOUND);
		}
		
		//模式验证
		Player player = userDomain.getPlayer();
		Player target = targetDomain.getPlayer();

		int attackCamp = player.getCamp().ordinal();
		int targetCamp = target.getCamp().ordinal();
		FightMode attackMode = currPlayerBattle.getMode();
		PlayerBattle targetPlayerBattle = targetDomain.getBattle();
		FightMode targetMode = targetPlayerBattle.getMode();
		//需要杀戮模式才可以使用. 则表示
		if(!FightHelper.canAttackPlayer(skill, attackMode, targetMode, attackCamp, targetCamp)) {
			return ResultObject.ERROR(FIGHT_MODE_INVALID);
		} else if(!targetMode.canBeAttack(targetPlayerBattle.getLevel())) {
			return ResultObject.ERROR(TARGET_PEACE_MODE_INVALID);
		} else if(!FightHelper.validProtectedAttack(skill, player)) {
			return ResultObject.ERROR(FIGHT_MODE_INVALID);
		} else if(!FightHelper.validProtectedAttack(skill, target)) {
			return ResultObject.ERROR(TARGET_PROTECTED_INVALID);
		}
		
		int currentPX = currPetDomain.getX();						//发起攻击者的X坐标
		int currentPY = currPetDomain.getY();						//发起攻击者的Y坐标
		int targetPX = targetPetDomain.getX();						//被攻击者的X坐标
		int targetPY = targetPetDomain.getY();						//被攻击者的Y坐标
		PetMotion currPetMotion = currPetDomain.getMotion();		//当前家将的移动对象		
		PetMotion targetMotion = targetPetDomain.getMotion();		//目标家将的移动对象
		if(currPetMotion == null || targetMotion == null) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		GameScreen currScreen = currPetDomain.getCurrentScreen();	//当前家将所在的场景
		GameScreen targetScreen = targetPetDomain.getCurrentScreen();	//目标家将所在的场景
		if(currScreen == null || targetScreen == null) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		GameMap currentGameMap = currScreen.getGameMap();			//当前家将所在的地图
		GameMap targetGameMap = targetScreen.getGameMap();			//目标家将所在的地图
		if(targetGameMap == null || currentGameMap == null) {
			return ResultObject.ERROR(POSITION_INVALID);
		} else if(targetGameMap != currentGameMap) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		int distance = FightRule.MAX_PET_FIGHT_DISTANCE;
		if(!MapUtils.checkPosScopeInfloat(currentPX, currentPY, targetPX, targetPY, distance)) {
			return ResultObject.ERROR(POSITION_DISTANCE_INVALID);
		}
		
		//技能效果列表
		List<SkillEffectConfig> skillEffects = skill.getSkillEffects();
		if(skillEffects == null || skillEffects.isEmpty()) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int skillId = skill.getId();							//技能ID
		PetBattle currPetBattle = currPetDomain.getBattle();	//当前家将战斗对象
		int result = validatePetCoolTime(currPetBattle, skill);
		if(result < SUCCESS) {
			return ResultObject.ERROR(result);
		}
		
		int currentCoolTimeId = skill.getCdId();			//技能CDID
		Point castPoint = new Point(currentPX, currentPY);	//释放者的坐标
		Point targetPoint = new Point(targetPX, targetPY);	//目标点的坐标
		Context context = Context.valueOf(castPoint, targetPoint, currentCoolTimeId, currentGameMap);	//上下文对象
		AreaVO areaVO = AreaVO.petToPet(currPetDomain, userDomain, targetPetDomain, targetDomain, 
						attackMode, attackCamp, targetPX, targetPY, currentGameMap);
		
		AreaContext areaContext = FightHelper.getFightUnitWithArea(context, areaVO, skill);						//取得区域玩家信息
		FightProcessor.processSkillEffectToFight(context, currPetDomain, skill, areaContext.getFightUnits());	//处理技能效果
		Collection<Long> viewPlayers = getFightCanViewPlayerIds(areaContext.getViewPlayerSpire());
		return ResultObject.SUCCESS(FightEvent.valueOf(currPetDomain, targetPetDomain, skillId, context, viewPlayers));
	}

	/**
	 * 召唤兽施放技能攻击怪物
	 * 
	 * @param  userDomain				用户域模型对象
	 * @param  petDomain				召唤兽域模型对象
	 * @param  skill					使用的技能对象
	 * @param  targetId					怪物的ID 
	 * @return {@link ResultObject}		释放技能返回值
	 */
	private ResultObject<FightEvent> petCastSkill2Monster(UserDomain userDomain, PetDomain petDomain, SkillConfig skill, long targetId) {
		MonsterDomain targetDomain = monsterFacade.getMonsterDomain(targetId);
		if(targetDomain == null) {
			return ResultObject.ERROR(TARGET_NOT_FOUND);
		}
		
		if(skill.validateSkillType(SkillType.ACTIVE_TREAT.ordinal())) {
			return ResultObject.ERROR(TYPE_INVALID);
		}
		
		MonsterBattle targetBattle = targetDomain.getMonsterBattle();
		if(targetBattle == null) {
			return ResultObject.ERROR(TARGET_NOT_FOUND);
		} else if(targetBattle.isDead()) {
			return ResultObject.ERROR(TARGET_DEADED);
		} 
		
		int petPX = petDomain.getX();								//发起攻击者的X坐标
		int petPY = petDomain.getY();								//发起攻击者的Y坐标
		int targetPX = targetDomain.getX();							//被攻击者的X坐标
		int targetPY = targetDomain.getY();							//被攻击者的Y坐标
		GameScreen currentScreen = petDomain.getCurrentScreen();	//家将的当前场景
		GameScreen targetScreen = targetDomain.getCurrentScreen();	//怪物的移动场景
		if(currentScreen == null || targetScreen == null) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		GameMap gameMap = currentScreen.getGameMap();
		GameMap targetGameMap = currentScreen.getGameMap();
		if(gameMap == null || targetGameMap == null) {
			return ResultObject.ERROR(POSITION_INVALID);
		} else if(gameMap != targetGameMap) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		int distance = FightRule.MAX_PET_FIGHT_DISTANCE;
		if(!MapUtils.checkPosScopeInfloat(petPX, petPY, targetPX, targetPY, distance)) {
			return ResultObject.ERROR(POSITION_DISTANCE_INVALID);
		}
		
		//技能效果列表
		List<SkillEffectConfig> skillEffects = skill.getSkillEffects();
		if(skillEffects == null || skillEffects.isEmpty()) {
			return ResultObject.ERROR(FAILURE);
		}
		
		MonsterFightConfig monsterFight = targetBattle.getMonsterFight();
		if(monsterFight == null) {
			return ResultObject.ERROR(FAILURE);
		}
		
		Player player = userDomain.getPlayer();
		int attackCamp = player.getCamp().ordinal();	//角色的阵营
		int targetCamp = targetDomain.getMonsterCamp();	//被攻击者的阵营
		if(!FightHelper.otherCampValidator(attackCamp, targetCamp)) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int skillId = skill.getId();					//技能ID
		int currentCDId = skill.getCdId();				//技能CDID
		PetBattle petBattle = petDomain.getBattle();	//家将的战斗对象
		int result = validatePetCoolTime(petBattle, skill);
		if(result < SUCCESS) {
			return ResultObject.ERROR(result);
		}
		
		Point castPoint = new Point(petPX, petPY);					//释放者的坐标
		FightMode mode = userDomain.getBattle().getMode();			//用户战斗模式
		Point targetPoint = new Point(targetPX, targetPY);			//目标点的坐标
		Context context = Context.valueOf(castPoint, targetPoint, currentCDId, gameMap);		//上下文对象
		AreaVO areaVO = AreaVO.petToSpire(petDomain, userDomain, targetDomain, mode, attackCamp, targetPX, targetPY, gameMap);
		AreaContext areaContext = FightHelper.getFightUnitWithArea(context, areaVO, skill);						//取得区域玩家信息
		FightProcessor.processSkillEffectToFight(context, petDomain, skill, areaContext.getFightUnits());
		Collection<Long> viewPlayers = getFightCanViewPlayerIds(areaContext.getViewPlayerSpire()); 
		return ResultObject.SUCCESS(FightEvent.valueOf(petDomain, targetDomain, skillId, context, viewPlayers));
	}

	/** 
	 * 家将对角色释放技能
	 * 
	 * @param  player					角色对象
	 * @param  battle					角色对性爱那个
	 * @param  pet						家将对象
	 * @param  petBattle				家将战斗对象
	 * @param  petMotion				家将移动对象
	 * @param  petSkill					家将技能对象
	 * @param  skill					基础技能对象
	 * @param  targetId					目标角色ID
	 * @return {@link ResultObject}		返回值对象
	 */
	private ResultObject<FightEvent> petCastSkill2Player(UserDomain userDomain, PetDomain petDomain, SkillConfig skill, long targetId) {
		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(targetDomain == null) {
			return ResultObject.ERROR(TARGET_NOT_FOUND);
		}
		
		PlayerBattle targetBattle = targetDomain.getBattle();
		if(targetBattle.isDead()) {
			return ResultObject.ERROR(TARGET_DEADED);
		} 

		if(petDomain.getPlayerId() == targetId) {							//攻击的是自己的主人, 不合法
			return ResultObject.ERROR(FIGHT_SELF_INVALID);
		}
		
		//模式验证
		Player player = userDomain.getPlayer();
		Player target = targetDomain.getPlayer();
		int playerCamp = player.getCamp().ordinal();
		int targetCamp = target.getCamp().ordinal();
		PlayerBattle attackBattle = userDomain.getBattle();
		FightMode attackMode = attackBattle.getMode();
		FightMode targetMode = targetBattle.getMode();
		if(!FightHelper.canAttackPlayer(skill, attackMode, targetMode, playerCamp, targetCamp)) {
			return ResultObject.ERROR(FIGHT_MODE_INVALID);
		} else if(!targetMode.canBeAttack(targetBattle.getLevel())) {
			return ResultObject.ERROR(TARGET_PEACE_MODE_INVALID);
		} else if(!FightHelper.validProtectedAttack(skill, player)) {
			return ResultObject.ERROR(FIGHT_MODE_INVALID);
		} else if(!FightHelper.validReviveAttack(skill, target)) { //验证无敌时间
			return ResultObject.ERROR(TARGET_PROTECTED_INVALID);	
		} else if(!FightHelper.validProtectedAttack(skill, target)) {//验证保护状态
			return ResultObject.ERROR(TARGET_PROTECTED_INVALID);
		}
		 
		int petPX = petDomain.getX();								//发起攻击者的X坐标
		int petPY = petDomain.getY();								//发起攻击者的Y坐标
		int targetPX = targetDomain.getX();							//被攻击者的X坐标
		int targetPY = targetDomain.getY();							//被攻击者的Y坐标
		GameScreen currentScreen = petDomain.getCurrentScreen();
		GameScreen targetScreen = targetDomain.getCurrentScreen();
		if(currentScreen == null || targetScreen == null) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		GameMap currentGameMap = currentScreen.getGameMap();
		GameMap targetGameMap = targetScreen.getGameMap();
		if(currentGameMap == null || targetGameMap == null) {
			return ResultObject.ERROR(POSITION_INVALID);
		} else if(currentGameMap != targetGameMap) {
			return ResultObject.ERROR(POSITION_INVALID);
		}

		int distance = FightRule.MAX_PET_FIGHT_DISTANCE;		//技能的攻击距离
		if(!MapUtils.checkPosScopeInfloat(petPX, petPY, targetPX, targetPY, distance)) {
			return ResultObject.ERROR(POSITION_DISTANCE_INVALID);
		}

		//技能效果列表
		List<SkillEffectConfig> skillEffects = skill.getSkillEffects();
		if(skillEffects == null || skillEffects.isEmpty()) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int skillId = skill.getId();						//技能ID
		PetBattle petBattle = petDomain.getBattle();		//家将的战斗对象
		int result = validatePetCoolTime(petBattle, skill);
		if(result < SUCCESS) {
			return ResultObject.ERROR(result);
		}
		
		int currentCDId = skill.getCdId();														//技能CD的ID
		Point castPoint = new Point(petPX, petPY);												//释放者的坐标
		Point targetPoint = new Point(targetPX, targetPY);										//目标的坐标
		Context context = Context.valueOf(castPoint, targetPoint, currentCDId, currentGameMap);	//上下文对象
		AreaVO areaVO = AreaVO.petToSpire(petDomain, userDomain, targetDomain, 
					attackMode, playerCamp, targetPX, targetPY, currentGameMap);
		AreaContext areaContext = FightHelper.getFightUnitWithArea(context, areaVO, skill);
		FightProcessor.processSkillEffectToFight(context, petDomain, skill, areaContext.getFightUnits());
		Collection<Long> viewPlayers = getFightCanViewPlayerIds(areaContext.getViewPlayerSpire());
		return ResultObject.SUCCESS(FightEvent.valueOf(petDomain, targetDomain, skillId, context, viewPlayers));
	}

	/**
	 * 家将发起区域攻击
	 * 
	 * @param  player					角色对象
	 * @param  pet						家将对象
	 * @param  petBattle				家将战斗对象
	 * @param  petMotion				家将移动对象
	 * @param  skill					基础技能对象
	 * @param  positionX				攻击的 X 坐标
	 * @param  positionY				攻击的 Y 坐标
	 * @return {@link ResultObject}		返回值对象
	 */
	private ResultObject<FightEvent> petCastAreaSkill(UserDomain userDomain, PetDomain petDomain, SkillConfig skill, int positionX, int positionY) {
		int petPX = petDomain.getX();							//发起攻击者的X坐标
		int petPY = petDomain.getY();							//发起攻击者的Y坐标
//		PetMotion petMotion = petDomain.getMotion();			//家将的移动对象
		GameScreen currentScreen = petDomain.getCurrentScreen();//家将当前场景
		if(currentScreen == null) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		GameMap gameMap = currentScreen.getGameMap();
		if(gameMap == null) {
			return ResultObject.ERROR(POSITION_INVALID);
		}

		int distance = FightRule.MAX_PET_FIGHT_DISTANCE;
		if(!MapUtils.checkPosScopeInfloat(petPX, petPY, positionX, positionY, distance)) {
			return ResultObject.ERROR(POSITION_DISTANCE_INVALID);
		}
		
		//技能效果列表
		List<SkillEffectConfig> skillEffects = skill.getSkillEffects();
		if(skillEffects == null || skillEffects.isEmpty()) {
			return ResultObject.ERROR(FAILURE);
		}
		
		PetBattle petBattle = petDomain.getBattle();
		int result = validatePetCoolTime(petBattle, skill);
		if(result < SUCCESS) {
			return ResultObject.ERROR(result);
		}
		
		int skillId = skill.getId();
		int currentCDId = skill.getCdId();
		Player player = userDomain.getPlayer();
		int camp = player.getCamp().ordinal();
		Point castPoint = new Point(petPX, petPY);											//释放者的坐标
		FightMode mode = userDomain.getBattle().getMode();									//角色的模式
		Point targetPoint = new Point(positionX, positionY);								//目标的坐标
		Context context = Context.valueOf(castPoint, targetPoint, currentCDId, gameMap);	//上下文对象
		AreaVO areaVO = AreaVO.spireAoe(petDomain, userDomain, mode, camp, positionX, positionY, gameMap);
		AreaContext areaContext = FightHelper.getFightUnitWithArea(context, areaVO, skill);
		FightProcessor.processSkillEffectToFight(context, petDomain, skill, areaContext.getFightUnits());
		Collection<Long> viewPlayers = getFightCanViewPlayerIds(areaContext.getViewPlayerSpire());
		return ResultObject.SUCCESS(FightEvent.valueOf(petDomain, skillId, context, viewPlayers));
	}

	/**
	 * 验证用户的技能冷却时间
	 * 
	 * @param  petBattle			家将战斗对象
	 * @param  skill				技能基础对象
	 * @return {@link Integer}		返回值
	 */
	private int validatePetCoolTime(PetBattle petBattle, SkillConfig skill) {
		long userPetId = petBattle.getId();
		PetCoolTime petCoolTime = cdManager.getPetCoolTime(userPetId);
		if(petCoolTime == null) {
			return CDTIME_NOT_FOUND;
		}
		
		int globalCooltimeId = CoolTime.GLOBAL_COOLTIME_ID;							//全局数据ID
		CoolTimeConfig globalCD = cdManager.getCoolTimeConfig(globalCooltimeId);		
		if(globalCD == null) {
			return BASEDATA_NOT_FOUND;
		}
		
		int currentCdId = skill.getCdId();											//当前技能ID
		CoolTimeConfig currentCD = cdManager.getCoolTimeConfig(currentCdId);
		if(currentCD == null) {
			return BASEDATA_NOT_FOUND;
		}

		int delayMoves = skill.getDelayMoves();
		ChainLock lock = LockUtils.getLock(petBattle, petCoolTime);
		try {
			lock.lock();
			if(petBattle.isDeath()) {
				return PET_DEAD;
			}
			
			CoolTime globalCoolTime = petCoolTime.getCoolTime(globalCooltimeId);
			if(globalCoolTime != null && !globalCoolTime.isTimeOut()) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("******* 全局CD:[{}] ", globalCoolTime);
				}
				return COOL_TIMING;
			}
			
			CoolTime skillCoolTime = petCoolTime.getCoolTime(currentCdId);
			if(skillCoolTime != null && !skillCoolTime.isTimeOut()) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("******* 技能CD:[{}] ", skillCoolTime);
				}
				return COOL_TIMING;
			}
			petCoolTime.addCoolTime(globalCooltimeId, globalCD.getCoolTime());
			petCoolTime.addCoolTime(currentCdId, currentCD.getCoolTime() + delayMoves);
		} finally {
			lock.unlock();
		}
		
		return SUCCESS;
	}
	
	/**
	 * 角色主动发起战斗
	 * 
	 * @param  playerId								主动发起的玩家ID
	 * @param  targetId								被攻击者的ID
	 * @param  unitType   							被攻击者的类型
	 * @param  skillId								释放的技能ID
	 * @param  positionX							AOE攻击时需要指定的X坐标
	 * @param  positionY							AOE攻击时需要指定的Y坐标
	 * @return {@link ResultObject<FightEvent>}		战斗模块返回值
	 */
	
	public ResultObject<FightEvent> playerFight(long playerId, long targetId, 
					int unitType, int skillId, int positionX, int positionY) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		}
		
		//基础技能对象
 		SkillConfig skill = skillService.getSkillConfig(skillId);
		if(skill == null) {
			return ResultObject.ERROR(SKILL_NOT_FOUND);
		} else if(!skill.isActivity()) {
			return ResultObject.ERROR(SKILL_TYPE_INVALID);
		}
		
		UserSkill userSkill = userDomain.getUserSkill();
		if(userSkill == null) {
			return ResultObject.ERROR(SKILL_NOT_FOUND);
		} else if(!userSkill.hasSkill(skillId, true)) {
			return ResultObject.ERROR(SKILL_NOT_FOUND);
		}
		
		//验证定身效果
		UserBuffer userBuffer = userDomain.getUserBuffer();
		if(BufferHelper.isPlayerInImmobilize(userBuffer)) { 
			return ResultObject.ERROR(HAS_BUFFER_LIMITTED);
		}
		
		ResultObject<FightEvent> resultObject = null;
		if(skill.getCastTarget() == CastTarget.AREA.ordinal()) {
			resultObject = this.playerCastAOE(userDomain, userSkill, skill, positionX, positionY);
		} else if(skill.getCastTarget() == CastTarget.NONE.ordinal()) {
			resultObject = this.playerCastAOE(userDomain, userSkill, skill, userDomain.getX(), userDomain.getY());
		} else if(skill.getCastTarget() == CastTarget.TARGET.ordinal() && unitType == ElementType.PET.ordinal()) {
			resultObject = this.playerCastSkill2Pet(userDomain, userSkill, skill, targetId);
		} else if(skill.getCastTarget() == CastTarget.TARGET.ordinal() && unitType == ElementType.PLAYER.ordinal()) {
			resultObject = this.playerCastSkill2Player(userDomain, userSkill, skill, targetId);
		} else if(skill.getCastTarget() == CastTarget.TARGET.ordinal() && unitType == ElementType.MONSTER.ordinal()) {
			resultObject = this.playerCastSkill2Monster(userDomain, userSkill, skill, targetId);
		} else {
			resultObject = ResultObject.ERROR(SKILL_TYPE_INVALID);
		}
		
		if(resultObject.getResult() >= SUCCESS) {
			userDomain.getPlayer().setReviveProteTime(0L);
		}
		return resultObject;
	}

	/**
	 * 角色使用技能攻击家将. 
	 * 
	 * @param  userDomain			用户域模型
	 * @param  userSkill			用户技能对象
	 * @param  skill				基础技能对象
	 * @param  targetId				被攻击的家将ID
	 * @return {@link ResultObject}	返回值对象
	 */
	private ResultObject<FightEvent> playerCastSkill2Pet(UserDomain userDomain, UserSkill userSkill, SkillConfig skill, long targetId) {
		PetDomain petDomain = petManager.getPetDomain(targetId);
		if(petDomain == null) {
			return ResultObject.ERROR(TARGET_NOT_FOUND);
		}
		
		Pet targetPet = petDomain.getPet();
		long targetPlayerId = targetPet.getPlayerId();
		PetBattle targetBattle = petDomain.getBattle();
		if(!targetPet.isFighting()) {	//家将没有出战
			return ResultObject.ERROR(CANNOT_FIGHTING);
		}
		
		PetMotion targetMotion = petDomain.getMotion();
		UserDomain targetDomain = userManager.getUserDomain(targetPlayerId);
		if(targetDomain == null) {
			return ResultObject.ERROR(TARGET_NOT_FOUND);
		}
		
		long playerId = userDomain.getPlayerId();
		PlayerBattle targetPlayerBattle = targetDomain.getBattle();
		if(targetPlayerId == playerId) {
			return ResultObject.ERROR(FIGHT_SELF_INVALID);
		} else if(targetBattle.isDeath() || targetPlayerBattle.isDead()) {
			return ResultObject.ERROR(TARGET_DEADED);
		}
		
		//需要杀戮模式才可以使用. 则表示
		Player attackPlayer = userDomain.getPlayer();
		Player targetPlayer = targetDomain.getPlayer();
		PlayerBattle playerBattle = userDomain.getBattle();
		FightMode attackMode = playerBattle.getMode();
		int playerCamp = attackPlayer.getCamp().ordinal();
		int targetCamp = targetPlayer.getCamp().ordinal();
		FightMode targetMode = targetPlayerBattle.getMode();
		if(!FightHelper.canAttackPlayer(skill, attackMode, targetMode, playerCamp, targetCamp)) { //模式不匹配
			return ResultObject.ERROR(FIGHT_MODE_INVALID);
		} else if(!targetMode.canBeAttack(targetPlayerBattle.getLevel())) {
			return ResultObject.ERROR(TARGET_PEACE_MODE_INVALID);
		} else if(!FightHelper.validProtectedAttack(skill, attackPlayer)) {
			return ResultObject.ERROR(FIGHT_MODE_INVALID);
		} else if(!FightHelper.validProtectedAttack(skill, targetPlayer)) {
			return ResultObject.ERROR(TARGET_PROTECTED_INVALID);
		}
		
		int playerPX = userDomain.getX();								//发起攻击者的X坐标
		int playerPY = userDomain.getY();								//发起攻击者的Y坐标
		int targetPX = targetMotion.getX();								//被攻击者的X坐标
		int targetPY = targetMotion.getY();								//被攻击者的Y坐标
		
		GameScreen targetScreen = targetDomain.getCurrentScreen();		//目标玩家的场景
		GameScreen currentScreen = userDomain.getCurrentScreen();		//角色当前所在的场景
		if(currentScreen == null || targetScreen == null) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		GameMap userGameMap = currentScreen.getGameMap();				//目标玩家的地图
		GameMap targetGameMap = targetScreen.getGameMap();				//角色当前所在的地图
		if(userGameMap == null || targetGameMap == null) {
			return ResultObject.ERROR(POSITION_INVALID);
		} else if(userGameMap != targetGameMap) {					//所在地图验证
			return ResultObject.ERROR(POSITION_INVALID);
		} 

		int distance = FightRule.MAX_PET_FIGHT_DISTANCE;
		if(!MapUtils.checkPosScopeInfloat(playerPX, playerPY, targetPX, targetPY, distance)) {
			return ResultObject.ERROR(POSITION_DISTANCE_INVALID);
		}
		
		//技能效果列表
		List<SkillEffectConfig> skillEffects = skill.getSkillEffects();
		if(skillEffects == null || skillEffects.isEmpty()) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int skillId = skill.getId();									//技能ID
		int currentCoolTimeId = skill.getCdId();						//技能CDID
		int skillLevel = userSkill.getSkillLevel(skillId, true);		//技能等级
		int castCostMp = skill.getSkillCostMp(skillLevel);
		int result = validateUserCoolTime(playerBattle, skill, castCostMp);
		if(result < SUCCESS) {
			return ResultObject.ERROR(result);
		}
		
		Point castPoint = new Point(playerPX, playerPY);
		Point targetPoint = new Point(targetPX, targetPY);
		Context context = Context.valueOf(castPoint, targetPoint, currentCoolTimeId, userGameMap);	//上下文对象
		context.addAttributeChanges(userDomain.getUnitId(), AttributeKeys.MP, castCostMp);			//角色属性发生变化
		AreaVO areaVO = AreaVO.spireToPet(userDomain, petDomain, targetDomain, 
						attackMode, playerCamp, targetPX, targetPY, userGameMap);
		AreaContext areaContext = FightHelper.getFightUnitWithArea(context, areaVO, skill);
		FightProcessor.processSkillEffectToFight(context, userDomain, skill, areaContext.getFightUnits());	//处理自动战斗
		Collection<Long> viewPlayers = getFightCanViewPlayerIds(areaContext.getViewPlayerSpire());
		return ResultObject.SUCCESS(FightEvent.valueOf(userDomain, petDomain, skillId, context, viewPlayers));
	}

	/**
	 * 角色释放区域性技能
	 * 
	 * @param  monsterDomain			怪物域模型
	 * @param  skill					角色技能对象
	 * @param  positionX				角色释放攻击点的X坐标
	 * @param  positionY				角色释放攻击点的Y坐标
	 * @return {@link ResultObject}		返回值对象
	 */
	private int monsterCastAreaSkill(MonsterDomain monsterDomain, SkillConfig skill, int positionX, int positionY) {
		int playerPX = monsterDomain.getX();							//发起攻击者的X坐标
		int playerPY = monsterDomain.getY();							//发起攻击者的Y坐标
		int distance = skill.getDistance();								//技能的攻击距离
		GameScreen currentScreen = monsterDomain.getCurrentScreen();	//当前场景			
		if(currentScreen == null) {
			return POSITION_INVALID;
		}
		
		GameMap gameMap = currentScreen.getGameMap();
		if(gameMap == null) {
			return POSITION_INVALID;
		}
		
		distance *= 2;
		if(!MapUtils.checkPosScopeInfloat(playerPX, playerPY, positionX, positionY, distance)) {
			return POSITION_DISTANCE_INVALID;
		}
		
		int skillId = skill.getId();
		MonsterBattle battle = monsterDomain.getMonsterBattle();
		if(!battle.hasSkill(skillId) || battle.getSkillLevel(skillId) <= 0) {
			return SKILL_NOT_FOUND;
		}

		MonsterFightConfig monsterFight = battle.getMonsterFight();
		if(monsterFight == null) {
			return FAILURE;
		}
		
		//技能效果列表
		List<SkillEffectConfig> skillEffects = skill.getSkillEffects();
		if(skillEffects == null || skillEffects.isEmpty()) {
			return FAILURE;
		}
		
		int camp = monsterDomain.getMonsterCamp();
		long dungeonId = monsterDomain.getDungeonId();									//怪物地下城ID
		Point casterPoint = new Point(playerPX, playerPY);								//释放者的坐标
		Point targetPoint = new Point(positionX, positionY);							//目标的坐标
		Context context = Context.valueOf(casterPoint, targetPoint, -1, gameMap);		//上下文对象
		AreaVO areaVO = AreaVO.spireAoe(monsterDomain, FightMode.KILLING, camp, positionX, positionY, gameMap);
		AreaContext areaContext = FightHelper.getFightUnitWithArea(context, areaVO, skill);	//取得区域玩家信息
		FightProcessor.processSkillEffectToFight(context, monsterDomain, skill, areaContext.getFightUnits());
		pushMosterAttackFightReport(context, monsterDomain, null, skillId, dungeonId, areaContext.getViewPlayerSpire());
		return SUCCESS;
	}
	
	/**
	 * 角色释放区域性技能
	 * 
	 * @param  userDomain				用户域模型对象
	 * @param  userSkill				用户技能对象
	 * @param  skill					角色技能对象
	 * @param  positionX				角色释放攻击点的X坐标
	 * @param  positionY				角色释放攻击点的Y坐标
	 * @return {@link ResultObject}		返回值对象
	 */
	private ResultObject<FightEvent> playerCastAOE(UserDomain userDomain, UserSkill userSkill, SkillConfig skill, int positionX, int positionY) {
		int playerPX = userDomain.getX();							//发起攻击者的X坐标
		int playerPY = userDomain.getY();							//发起攻击者的Y坐标
		int distance = skill.getDistance();							//技能的攻击距离
		GameScreen currentScreen = userDomain.getCurrentScreen();	//当前场景			
		if(currentScreen == null) {
			return ResultObject.ERROR(POSITION_INVALID);
		}

		GameMap gameMap = currentScreen.getGameMap();
		if(gameMap == null) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		distance *= 2;
		if(!MapUtils.checkPosScopeInfloat(playerPX, playerPY, positionX, positionY, distance)) {
			return ResultObject.ERROR(POSITION_DISTANCE_INVALID);
		}

		//技能效果列表
		List<SkillEffectConfig> skillEffects = skill.getSkillEffects();
		if(skillEffects == null || skillEffects.isEmpty()) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int skillId = skill.getId();								//技能ID
		int currentCoolTimeId = skill.getCdId();					//技能CD
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();				//角色战斗对象
		int skillLevel = userSkill.getSkillLevel(skillId, true);	//技能等级
		int castSkillCostMp = skill.getSkillCostMp(skillLevel);		//释放技能需要扣除的MP
		int result = validateUserCoolTime(battle, skill, castSkillCostMp);
		if(result < SUCCESS) {
			return ResultObject.ERROR(result);
		}
		
		FightMode mode = battle.getMode();
		int camp = player.getCamp().ordinal();
		Point castPoint = new Point(playerPX, playerPY);
		Point targetPoint = new Point(playerPX, playerPY);
		AreaVO areaVO = AreaVO.spireAoe(userDomain, mode, camp, positionX, positionY, gameMap);				//构建区域对象VO
		Context context = Context.valueOf(castPoint, targetPoint, currentCoolTimeId, gameMap);						//上下文对象
		context.addAttributeChanges(userDomain.getUnitId(), AttributeKeys.MP, castSkillCostMp);				//角色属性发生变化
		AreaContext areaContext = FightHelper.getFightUnitWithArea(context, areaVO, skill);					//区域上下文
		FightProcessor.processSkillEffectToFight(context, userDomain, skill, areaContext.getFightUnits());	//处理战斗信息
		Collection<Long> viewPlayers = getFightCanViewPlayerIds(areaContext.getViewPlayerSpire());			//获得战斗中可以看到的角色ID列表
		return ResultObject.SUCCESS(FightEvent.valueOf(userDomain, skillId, context, viewPlayers));
	}
	
	/**
	 * 验证用户的技能冷却时间
	 * 
	 * @param  battle				角色的战斗对象
	 * @param  coolTime				角色的冷却时间
	 * @param  skill				技能基础对象
	 * @param  castCostMp			释放扣除的MP
	 * @return {@link Integer}		返回值
	 */
	private int validateUserCoolTime(PlayerBattle battle, SkillConfig skill, int castCostMp) {
		long playerId = battle.getId();
		UserCoolTime userCoolTime = cdManager.getUserCoolTime(playerId);
		if(userCoolTime == null) {
			return CDTIME_NOT_FOUND;
		}
		
		int globalCooltimeId = CoolTime.GLOBAL_COOLTIME_ID;							//全局数据ID
		CoolTimeConfig globalCD = cdManager.getCoolTimeConfig(globalCooltimeId);		
		if(globalCD == null) {
			return BASEDATA_NOT_FOUND;
		}
		
		int currentCdId = skill.getCdId();											//当前技能ID
		CoolTimeConfig currentCD = cdManager.getCoolTimeConfig(currentCdId);
		if(currentCD == null) {
			return BASEDATA_NOT_FOUND;
		}

		ChainLock lock = LockUtils.getLock(battle, userCoolTime);
		try {
			lock.lock();
			if(battle.isDead()) {
				return PLAYER_DEADED;
			}
			
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("角色当前的MP:[{}] , 释放技能需要的MP:[{}] ", battle.getMp(), castCostMp);
			}
			if(battle.getMp() < castCostMp) {
				return MP_NOT_ENOUGH;
			}
			
			CoolTime globalCoolTime = userCoolTime.getCoolTime(globalCooltimeId);
			if(globalCoolTime != null && !globalCoolTime.isTimeOut()) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("******* 全局CD信息中:[{}] 当前时间:[{}]", globalCoolTime, System.currentTimeMillis());
				}
				return COOL_TIMING;
			}
			
			CoolTime skillCoolTime = userCoolTime.getCoolTime(currentCdId);
			if(skillCoolTime != null && !skillCoolTime.isTimeOut()) {
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("********************CDID:[{}] ", currentCdId);
					LOGGER.debug("******* 技能CD中:[{}] 当前时间:[{}] ", skillCoolTime.getEndTime() / 10, (System.currentTimeMillis() +  + CoolTimeRule.MODIFIRE_COOLTIME) / 10);
				}
				return COOL_TIMING;
			}
			
			battle.decreaseMp(castCostMp);
			userCoolTime.addCoolTime(globalCooltimeId, globalCD.getCoolTime());
			userCoolTime.addCoolTime(currentCdId, currentCD.getCoolTime());
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("全局CD结束时间:[{}] 技能:[{}] CD结束时间:[{}] ", new Object[] { userCoolTime.getCoolTime(globalCooltimeId), skill.getId(), userCoolTime.getCoolTime(currentCdId) });
			}
		} finally {
			lock.unlock();
		}
		return SUCCESS;
	}
	
	/**
	 * 角色施放有目标的技能
	 * 
	 * @param  player					角色对象
	 * @param  battle					角色战斗对象
	 * @param  playerMotion				角色的移动对象
	 * @param  userSkill				用户技能对象
	 * @param  skillConfig				角色技能对象
	 * @param  targetId					角色ID
	 * @return {@link ResultObject}		返回值对象
	 */
	private ResultObject<FightEvent> playerCastSkill2Player(UserDomain userDomain, UserSkill userSkill, SkillConfig skillConfig, long targetId) {
		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(targetDomain == null) {
			return ResultObject.ERROR(TARGET_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		Player target = targetDomain.getPlayer();
		PlayerBattle playerBattle = userDomain.getBattle();
		PlayerBattle targetBattle = targetDomain.getBattle();
		if(targetBattle.isDead()) {
			return ResultObject.ERROR(TARGET_DEADED);
		}
		
		//需要杀戮模式才可以使用. 则表示
		int attackCamp = player.getCamp().ordinal();
		int targetCamp = target.getCamp().ordinal();
		FightMode attackMode = playerBattle.getMode();
		FightMode targetMode = targetBattle.getMode();
		if(!FightHelper.canAttackPlayer(skillConfig, attackMode, targetMode, attackCamp, targetCamp)) { //验证模式是否匹配.
			return ResultObject.ERROR(FIGHT_MODE_INVALID);
		} else if(!targetMode.canBeAttack(targetBattle.getLevel())) {
			return ResultObject.ERROR(TARGET_PEACE_MODE_INVALID);
		} else if(!FightHelper.validProtectedAttack(skillConfig, player)) {
			return ResultObject.ERROR(FIGHT_MODE_INVALID);
		} else if(!FightHelper.validProtectedAttack(skillConfig, target)) { //验证保护模式
			return ResultObject.ERROR(TARGET_PROTECTED_INVALID);
		} else if(!FightHelper.validReviveAttack(skillConfig, target)) { //验证无敌时间
			return ResultObject.ERROR(TARGET_PROTECTED_INVALID);
		}
		
		int playerPX = userDomain.getX();							//发起攻击者的X坐标
		int playerPY = userDomain.getY();							//发起攻击者的Y坐标
		int targetPX = targetDomain.getX();							//被攻击者的X坐标
		int targetPY = targetDomain.getY();							//被攻击者的Y坐标
		int distance = skillConfig.getDistance();					//技能的攻击距离
		GameScreen currentScreen = userDomain.getCurrentScreen();	//角色当前场景
		GameScreen targetScreen = targetDomain.getCurrentScreen();	//目标玩家的场景
		if(currentScreen == null || targetScreen == null) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		GameMap userGameMap = currentScreen.getGameMap();
		GameMap targetGameMap = targetScreen.getGameMap();
		if(userGameMap == null || targetGameMap == null) {
			return ResultObject.ERROR(POSITION_INVALID);
		} else if(userGameMap != targetGameMap) {
			return ResultObject.ERROR(POSITION_INVALID);
		}

		distance += 2;
		if(!MapUtils.checkPosScopeInfloat(playerPX, playerPY, targetPX, targetPY, distance)) {
			return ResultObject.ERROR(POSITION_DISTANCE_INVALID);
		};
		
		//技能效果列表
		List<SkillEffectConfig> skillEffects = skillConfig.getSkillEffects();
		if(skillEffects == null || skillEffects.isEmpty()) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int skillId = skillConfig.getId();										//技能ID
		int currentCoolTimeId = skillConfig.getCdId();							//技能CDID
		int skillLevel = userSkill.getSkillLevel(skillId, true);				//技能等级
		int castCostMp = skillConfig.getSkillCostMp(skillLevel);
		int result = validateUserCoolTime(playerBattle, skillConfig, castCostMp);
		if(result < SUCCESS) {
			return ResultObject.ERROR(result);
		}
		
		Point castPoint = new Point(playerPX, playerPY);
		Point targetPoint = new Point(targetPX, targetPY);
		Context context = Context.valueOf(castPoint, targetPoint, currentCoolTimeId, userGameMap);		//上下文对象
		context.addAttributeChanges(userDomain.getUnitId(), AttributeKeys.MP, castCostMp);				//角色属性发生变化
		AreaVO areaVO = AreaVO.spireToSpire(userDomain, targetDomain, attackMode, attackCamp, targetPX, targetPY, userGameMap);
		AreaContext areaContext = FightHelper.getFightUnitWithArea(context, areaVO, skillConfig); 			//取得区域玩家信息
		FightProcessor.processSkillEffectToFight(context, userDomain, skillConfig, areaContext.getFightUnits());
		Collection<Long> viewPlayers = getFightCanViewPlayerIds(areaContext.getViewPlayerSpire());
		return ResultObject.SUCCESS(FightEvent.valueOf(userDomain, targetDomain, skillId, context, viewPlayers));
	}
	
	/**
	 * 角色施放有目标的技能
	 * 
	 * @param  player  					角色对象
	 * @param  battle					角色战斗对象
	 * @param  motion					角色的移动对象
	 * @param  userSkill				用户技能对象
	 * @param  skillConfig				角色技能对象
	 * @param  targetId					角色ID
	 * @return {@link ResultObject}		返回值对象
	 */
	private ResultObject<FightEvent> playerCastSkill2Monster(UserDomain userDomain, 
					UserSkill userSkill, SkillConfig skillConfig, long targetId) {
		MonsterDomain targetDomain = monsterFacade.getMonsterDomain(targetId);
		if(targetDomain == null) {
			return ResultObject.ERROR(TARGET_NOT_FOUND);
		}
		
		if(skillConfig.validateSkillType(SkillType.ACTIVE_TREAT.ordinal())) {
			return ResultObject.ERROR(TYPE_INVALID);
		}
		
		MonsterBattle targetBattle = targetDomain.getMonsterBattle();
		if(targetBattle == null) {
			return ResultObject.ERROR(TARGET_NOT_FOUND);
		} else if(targetBattle.isDead()) {
			return ResultObject.ERROR(TARGET_DEADED);
		} 
		
		
		int playerPX = userDomain.getX();							//发起攻击者的X坐标
		int playerPY = userDomain.getY();							//发起攻击者的Y坐标
		int targetPX = targetDomain.getX();							//被攻击者的X坐标
		int targetPY = targetDomain.getY();							//被攻击者的Y坐标
		int distance = skillConfig.getDistance();					//技能的攻击距离
		PlayerBattle playerBattle = userDomain.getBattle();			//攻击者的战斗对象
		GameScreen currentScreen = userDomain.getCurrentScreen();	//角色所在的场景
		GameScreen targetScreen = targetDomain.getCurrentScreen();	//目标所在的场景
		if(currentScreen == null || targetScreen == null) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		GameMap userGameMap = currentScreen.getGameMap();
		GameMap targetGameMap = targetScreen.getGameMap();
		if(userGameMap == null || targetGameMap == null) {
			return ResultObject.ERROR(POSITION_INVALID);
		} else if(userGameMap != targetGameMap) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		//海明修改 扩大1个距离
		distance += 2;
		if(!MapUtils.checkPosScopeInfloat(playerPX, playerPY, targetPX, targetPY, distance)) {
			return ResultObject.ERROR(POSITION_DISTANCE_INVALID);
		}
		
		//技能效果列表
		List<SkillEffectConfig> skillEffects = skillConfig.getSkillEffects();
		if(skillEffects == null || skillEffects.isEmpty()) {
			return ResultObject.ERROR(FAILURE);
		}
		
		MonsterFightConfig monsterFight = targetBattle.getMonsterFight();
		if(monsterFight == null) {
			return ResultObject.ERROR(FAILURE);
		}
		
		
		Player player = userDomain.getPlayer();
		int attackCamp = player.getCamp().ordinal();
		int targetCamp = targetDomain.getMonsterCamp();
		if(!FightHelper.otherCampValidator(attackCamp, targetCamp)) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int skillId = skillConfig.getId();									//技能ID
		int currentCoolTimeId = skillConfig.getCdId();						//技能CDID
		int skillLevel = userSkill.getSkillLevel(skillId, true);			//技能等级
		int castCostMp = skillConfig.getSkillCostMp(skillLevel);			//
		int result = validateUserCoolTime(playerBattle, skillConfig, castCostMp);
		if(result < SUCCESS) {
			return ResultObject.ERROR(result);
		}
		
		FightMode mode = playerBattle.getMode();
		Point castPoint = new Point(playerPX, playerPY);
		Point targetPoint = new Point(targetPX, targetPY);
		Context context = Context.valueOf(castPoint, targetPoint, currentCoolTimeId, userGameMap);							//上下文对象
		context.addAttributeChanges(userDomain.getUnitId(), AttributeKeys.MP, -castCostMp);									//角色属性发生变化
		AreaVO areaVO = AreaVO.spireToSpire(userDomain, targetDomain, mode, attackCamp, targetPX, targetPY, userGameMap);	//构建区域对象VO
		AreaContext areaContext = FightHelper.getFightUnitWithArea(context, areaVO, skillConfig);							//取得区域玩家信息
		FightProcessor.processSkillEffectToFight(context, userDomain, skillConfig, areaContext.getFightUnits());			//处理自动战斗
		Collection<Long> viewPlayers = getFightCanViewPlayerIds(areaContext.getViewPlayerSpire());
		return ResultObject.SUCCESS(FightEvent.valueOf(userDomain, targetDomain, skillId, context, viewPlayers));
	}
	
	/**
	 * 怪物攻击玩家. 根据技能ID攻击, 如果是AOE的话, 会根据x, y来遍历玩家
	 * 
	 * @param  monsterId							怪物ID
	 * @param  targetId								被攻击者的ID
	 * @param  unitType								战斗单位类型
	 * @param  xPoint								释放技能的X坐标点
	 * @param  yPoint								释放技能的Y坐标点
	 * @param  skillId								使用的技能ID
	 * @return {@link Integer}						战斗模块返回值
	 */
	
	public int monsterFight(long monsterId, long targetId, ElementType unitType, int xPoint, int yPoint, int skillId) {
		MonsterDomain monsterDomain = monsterFacade.getMonsterDomain(monsterId);
		if(monsterDomain == null) {
			return TARGET_NOT_FOUND;
		}
		
		//skillId = 216;
		MonsterBattle monsterBattle = monsterDomain.getMonsterBattle();
		if(monsterBattle == null) {
			return TARGET_NOT_FOUND;
		} else if(monsterBattle.isDead()) {
			return TARGET_DEADED;
		}
		
		//验证定身效果
		MonsterBuffer monsterBuffer = monsterDomain.getMonsterBuffer(true);
		if(BufferHelper.isMonsterInImmobilize(monsterBuffer)) { 
			return HAS_BUFFER_LIMITTED;
		}
		
		//基础技能对象
 		SkillConfig skill = skillService.getSkillConfig(skillId);
		if(skill == null) {
			return SKILL_NOT_FOUND;
		} else if(!skill.isActivity()) {
			return SKILL_TYPE_INVALID;
		} else if(!monsterBattle.hasSkill(skillId) || monsterBattle.getSkillLevel(skillId) <= 0) {
			return SKILL_NOT_FOUND;
		}
		
		if(skill.getCastTarget() == CastTarget.AREA.ordinal() || skill.getCastTarget() == CastTarget.NONE.ordinal()) {
			return monsterCastAreaSkill(monsterDomain, skill, xPoint, yPoint);
		} else if(skill.getCastTarget() == CastTarget.TARGET.ordinal() && unitType == ElementType.PET) {
			return monsterCastSkill2Pet(monsterDomain, skill, targetId);
		} else if(skill.getCastTarget() == CastTarget.TARGET.ordinal() && unitType == ElementType.PLAYER) {
			return monsterCastSkill2Player(monsterDomain, skill, targetId);
		} else if(skill.getCastTarget() == CastTarget.TARGET.ordinal() && unitType == ElementType.MONSTER) {
			return monsterCastSkill2Monster(monsterDomain, skill, targetId);
		}
		
		return SKILL_TYPE_INVALID;
	}
	
	/**
	 * 怪物攻击家将.
	 * 
	 * @param  monsterDomain	怪物域模型
	 * @param  skillConfig		技能对象
	 * @param  targetId			被攻击的家将ID
	 * @return {@link Integer}	返回值信息
	 */
	private int monsterCastSkill2Pet(MonsterDomain monsterDomain, SkillConfig skillConfig, long targetId) {
		MonsterBattle monsterBattle = monsterDomain.getMonsterBattle();
		PetDomain petDomain = petManager.getPetDomain(targetId);
		if(petDomain == null) {
			return PET_NOT_FOUND;
		}
		
		Pet pet = petDomain.getPet();
		PetBattle petBattle = petDomain.getBattle();
		PetMotion targetMotion = petDomain.getMotion();
		if(petBattle.isDeath()) {
			return PET_DEAD;
		}
		
		long targetPlayerId = pet.getPlayerId();
		UserDomain targetDomain = userManager.getUserDomain(targetPlayerId);
		if(targetDomain == null) {
			return TARGET_NOT_FOUND;
		}
		
		PlayerBattle targetBattle = targetDomain.getBattle();
		if(targetBattle.isDead()) {
			return TARGET_DEADED;
		}
		
		int targetPX = targetMotion.getX();								//被攻击者的X坐标
		int targetPY = targetMotion.getY();								//被攻击者的Y坐标
		int monsterPX = targetDomain.getX();							//发起攻击者的X坐标
		int monsterPY = targetDomain.getY();							//发起攻击者的Y坐标
		GameScreen targetScreen = petDomain.getCurrentScreen();			//被攻击者的Screen对象
		GameScreen currentScreen = targetDomain.getCurrentScreen();		//攻击者的Screen对象
		if(currentScreen == null || targetScreen == null) {
			return POSITION_INVALID;
		}
		
		GameMap currentGameMap = currentScreen.getGameMap();
		GameMap targetGameMap = targetScreen.getGameMap();
		if(currentGameMap == null || targetGameMap == null) {
			return POSITION_INVALID;
		} else if(currentGameMap != targetGameMap) {
			return POSITION_INVALID;
		}
		
		int distance = FightRule.MAX_PET_FIGHT_DISTANCE;
		if(!MapUtils.checkPosScopeInfloat(monsterPX, monsterPY, targetPX, targetPY, distance)) {
			return POSITION_DISTANCE_INVALID;
		}
		
		int skillId = skillConfig.getId();
		if(!monsterBattle.hasSkill(skillId) || monsterBattle.getSkillLevel(skillId) <= 0) {
			return SKILL_NOT_FOUND;
		}
		
		List<SkillEffectConfig> skillEffects = skillConfig.getSkillEffects();
		if(skillEffects == null || skillEffects.isEmpty()) {
			return FAILURE;
		}
		
		MonsterFightConfig monsterFight = monsterBattle.getMonsterFight();
		if(monsterFight == null) {
			return FAILURE;
		}
		
		Player player = targetDomain.getPlayer();
		if(player == null) { //保护时间里
			return FAILURE;
		}
		
		if(!FightHelper.validReviveAttack(skillConfig, player)) { //验证无敌时间
			monsterDomain.removeAttackTarget();
			return TARGET_PROTECTED_INVALID;	
		}
		
		int targetCamp = player.getCamp().ordinal();
		int monsterCamp = monsterDomain.getMonsterCamp();
		if(!FightHelper.otherCampValidator(monsterCamp, targetCamp)) {
			return FAILURE;
		}
		
		FightMode mode = FightMode.KILLING;
		long dungeonId = monsterDomain.getDungeonId();									//怪物地下城ID
		Point casterPoint = new Point(monsterPX, monsterPY);							//释放者的坐标
		Point targetPoint = new Point(targetPX, targetPY);								//释放者的坐标
		Context context = Context.valueOf(casterPoint, targetPoint, -1, currentGameMap);//上下文对象
		AreaVO areaVO = AreaVO.spireToPet(monsterDomain, petDomain, targetDomain, mode, monsterCamp, targetPX, targetPY, currentGameMap);
		AreaContext areaContext = FightHelper.getFightUnitWithArea(context, areaVO, skillConfig);	//取得区域玩家信息
		FightProcessor.processSkillEffectToFight(context, monsterDomain, skillConfig, areaContext.getFightUnits());
		pushMosterAttackFightReport(context, monsterDomain, petDomain, skillId, dungeonId, areaContext.getViewPlayerSpire());
		return SUCCESS;
	}

	/**
	 * 怪物对角色释放技能
	 * 
	 * @param  monster			施法者对象
	 * @param  battle			施法者战斗对象
	 * @param  motion			施法者移动对象
	 * @param  skill			施法者使用的技能对象
	 * @param  targetId			角色ID
	 * @return {@link Integer}	战斗模块返回值
	 */
	private int monsterCastSkill2Player(MonsterDomain monsterDomain, SkillConfig skillConfig, long targetId) {
		MonsterBattle monsterBattle = monsterDomain.getMonsterBattle();
		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(targetDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		int targetPX = targetDomain.getX();							//被攻击者的X坐标
		int targetPY = targetDomain.getY();							//被攻击者的Y坐标
		int monsterPX = targetDomain.getX();						//发起攻击者的X坐标
		int monsterPY = targetDomain.getY();						//发起攻击者的Y坐标
		int distance = skillConfig.getDistance();					//技能的攻击距离
		GameScreen currentScreen = monsterDomain.getCurrentScreen();
		GameScreen targetScreen = targetDomain.getCurrentScreen();
		if(currentScreen == null || targetScreen == null) {
			return POSITION_INVALID;
		}
		
		GameMap targetGameMap = targetScreen.getGameMap();
		GameMap currentGameMap = currentScreen.getGameMap();
		if(currentGameMap == null || targetGameMap == null) {
			return POSITION_INVALID;
		} else if(currentGameMap != targetGameMap) {
			return POSITION_INVALID;
		}

		distance *= 2 ;
		if(!MapUtils.checkPosScopeInfloat(monsterPX, monsterPY, targetPX, targetPY, distance)) {
			return POSITION_DISTANCE_INVALID;
		}
		
		int skillId = skillConfig.getId();
		PlayerBattle targetBattle = targetDomain.getBattle();
		if(targetBattle.isDead()) {
			return TARGET_DEADED;
		} else if(monsterBattle.getSkillLevel(skillId) <= 0) {
			return SKILL_NOT_FOUND;
		}
		
		List<SkillEffectConfig> skillEffects = skillConfig.getSkillEffects();
		if(skillEffects == null || skillEffects.isEmpty()) {
			return FAILURE;
		}
		
		MonsterFightConfig monsterFight = monsterBattle.getMonsterFight();
		if(monsterFight == null) {
			return FAILURE;
		}
		
		Player player = targetDomain.getPlayer();
		if(player == null) {
			return FAILURE;
		}
		
		if(!FightHelper.validReviveAttack(skillConfig, player)) { //验证无敌时间
			monsterDomain.removeAttackTarget();
			return TARGET_PROTECTED_INVALID;	
		}
		
		int targetCamp = player.getCamp().ordinal();
		int monsterCamp = monsterDomain.getMonsterCamp();
		if(!FightHelper.otherCampValidator(monsterCamp, targetCamp)) {
			return FAILURE;
		}
		
		FightMode mode = FightMode.KILLING;
		long dungeonId = monsterDomain.getDungeonId();									//怪物地下城ID
		Point casterPoint = new Point(monsterPX, monsterPY);							//释放者的坐标
		Point targetPoint = new Point(targetPX, targetPY);								//被攻击者的坐标
		Context context = Context.valueOf(casterPoint, targetPoint, -1, currentGameMap);//上下文对象
		AreaVO areaVO = AreaVO.spireToSpire(monsterDomain, targetDomain, mode, monsterCamp, targetPX, targetPY, currentGameMap);	//区域VO
		AreaContext areaContext = FightHelper.getFightUnitWithArea(context, areaVO, skillConfig);
		FightProcessor.processSkillEffectToFight(context, monsterDomain, skillConfig, areaContext.getFightUnits());
		pushMosterAttackFightReport(context, monsterDomain, targetDomain, skillId, dungeonId, areaContext.getViewPlayerSpire());
		return SUCCESS;
	}
	
	/**
	 * 获得当前精灵对象能看到的角色列表
	 * 
	 * @param  iSpire				获得角色精灵列表
	 * @return {@link Collection}	角色精灵列表对象
	 */
	private Collection<Long> getISpireGameMapPlayers(ISpire iSpire) {
		if(iSpire == null) {
			return Collections.emptySet();
		}
		
		GameScreen currentScreen = iSpire.getCurrentScreen();
		if(currentScreen == null) {
			return Collections.emptySet();
		}
		
		GameMap gameMap = currentScreen.getGameMap();
		if(gameMap == null) {
			return Collections.emptySet();
		}
		
		return gameMap.getCanViewsSpireIdCollection(iSpire, ElementType.PLAYER);
	}
	
	/**
	 * 查询可以看到本次战斗的角色ID列表
	 * 
	 * @param  castUnit				释放技能的精灵
	 * @param  castOwner			释放技能的拥有者(释放技能者是家将则传主人精灵类)
	 * @param  targetUnit			被攻击的精灵
	 * @param  targetOwner			被攻击的精灵(被攻击者是家将则传主人精灵类)
	 * @param  targetUnitIds		战斗攻击的精灵列表
	 * @return {@link Collection} 	角色ID列表
	 */
	private Collection<Long> getFightCanViewPlayerIds(Collection<ISpire> targetUnitIds) {
		Collection<Long> viewPlayers = new HashSet<Long>();
		if(targetUnitIds != null && !targetUnitIds.isEmpty()) {
			for (ISpire spire : targetUnitIds) {
				viewPlayers.addAll(getISpireGameMapPlayers(spire));
			}
		}
		return viewPlayers;
	}
	
	/**
	 * 推送怪物攻击战报
	 * 
	 * @param context				战斗上下文
	 * @param castUnitId			施放技能的单位
	 * @param targetId				施放技能的目标单位
	 * @param skillId				技能ID
	 * @param mapId					怪物所在的地图
	 * @param branching				怪物所在的分线
	 * @param dungeonId				地下城ID
	 * @param positionIds			所在的据点ID数组
	 */
	private void pushMosterAttackFightReport(Context context, ISpire castUnit, ISpire targetUnit, int skillId, long dungeonId, Collection<ISpire> targetUnitIds) {
		Collection<Long> viewPlayers = getFightCanViewPlayerIds(targetUnitIds);
		FightPushHelper.pushReport2Client(FightEvent.valueOf(castUnit, targetUnit, skillId, context, viewPlayers));
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("怪物: [{}] 攻击目标:[{}] , 可以看到这次技能:[{}], 攻击的玩家:[{}] 战报:[{}] ", new Object[] { castUnit.getUnitId(), targetUnit == null ? null : targetUnit.getUnitId(), skillId, viewPlayers,  Arrays.toString(context.getFightReports().toArray()) });
		}
	}

	/**
	 * 怪物对怪物释放技能
	 * 
	 * @param  monsterDomain	怪物域模型
	 * @param  skillConfig		施法者使用的技能对象
	 * @param  targetId			怪物ID
	 * @return {@link Integer}	战斗模块返回值
	 */
	private int monsterCastSkill2Monster(MonsterDomain monsterDomain, SkillConfig skillConfig, long targetId) {
		MonsterBattle monsterBattle = monsterDomain.getMonsterBattle();
		MonsterDomain targetDomain = monsterFacade.getMonsterDomain(targetId);
		if(targetDomain == null) {
			return TARGET_NOT_FOUND;
		}
		
		Monster target = targetDomain.getMonster();
		MonsterBattle targetBattle = targetDomain.getMonsterBattle();
		if(target == null || targetBattle == null) {
			return POSITION_INVALID;
		} else if(targetBattle.isDead()) {
			return TARGET_DEADED;
		} 
		
		int targetPX = targetDomain.getX();							//被攻击者的X坐标
		int targetPY = targetDomain.getY();							//被攻击者的Y坐标
		int monsterPX = monsterDomain.getX();						//发起攻击者的X坐标
		int monsterPY = monsterDomain.getY();						//发起攻击者的Y坐标
		int distance = skillConfig.getDistance();					//技能的攻击距离
		GameScreen currentScreen = monsterDomain.getCurrentScreen();
		GameScreen targetScreen = targetDomain.getCurrentScreen();
		if(currentScreen == null || targetScreen == null) {
			return POSITION_INVALID;
		}
		
		GameMap targetGameMap = targetScreen.getGameMap();
		GameMap currentGameMap = currentScreen.getGameMap();
		if(currentGameMap == null || targetGameMap == null) {
			return POSITION_INVALID;
		} else if(currentGameMap != targetGameMap) {
			return POSITION_INVALID;
		}
		
		distance *= 2;
		if(!MapUtils.checkPosScopeInfloat(monsterPX, monsterPY, targetPX, targetPY, distance)) {
			return POSITION_DISTANCE_INVALID;
		}
		
		int skillId = skillConfig.getId();
		if(!monsterBattle.hasSkill(skillId) || monsterBattle.getSkillLevel(skillId) <= 0) {
			return SKILL_NOT_FOUND;
		}
		
		List<SkillEffectConfig> skillEffects = skillConfig.getSkillEffects();
		if(skillEffects == null || skillEffects.isEmpty()) {
			return FAILURE;
		}
		
		MonsterFightConfig monsterFight = monsterBattle.getMonsterFight();
		MonsterFightConfig targetFight = targetBattle.getMonsterFight();
		if(monsterFight == null || targetFight == null) {
			return FAILURE;
		}
		
		int targetCamp = targetDomain.getMonsterCamp();		//被攻击的怪物的阵营
		int monsterCamp = monsterDomain.getMonsterCamp();	//发起攻击的怪物的阵营
		if(!FightHelper.monster2MonsterCampValidator(monsterCamp, targetCamp)) {
			return FAILURE;
		}
		
		long dungeonId = monsterDomain.getDungeonId();									//怪物地下城ID
		Point casterPoint = new Point(monsterPX, monsterPY);							//释放者的坐标
		Point targetPoint = new Point(targetPX, targetPY);								//被攻击者的坐标
		Context context = Context.valueOf(casterPoint, targetPoint, -1, currentGameMap);//上下文对象
		AreaVO areaVO = AreaVO.spireToSpire(monsterDomain, targetDomain, FightMode.KILLING, monsterCamp, targetPX, targetPY, currentGameMap);	//区域VO
		AreaContext areaContext = FightHelper.getFightUnitWithArea(context, areaVO, skillConfig); //取得区域玩家信息
		FightProcessor.processSkillEffectToFight(context, monsterDomain, skillConfig, areaContext.getFightUnits());
		pushMosterAttackFightReport(context, monsterDomain, targetDomain, skillId, dungeonId, areaContext.getViewPlayerSpire());
		return SUCCESS;
	}
	
	/**
	 * 更新角色的跳跃信息
	 * 
	 * @param playerId								角色ID
	 */
	
	public void updatePlayerJumpInfo(long playerId) {
		if(!JUMP_INFOS.containsKey(playerId)) {
			return;
		}
		
		JumpInfo jumpInfo = JUMP_INFOS.remove(playerId);
		if(jumpInfo == null) {
			return;
		}
		
		int sourceX = jumpInfo.getSourceX();
		int sourceY = jumpInfo.getSourceY();
		int targetX = jumpInfo.getTargetX();
		int targetY = jumpInfo.getTargetY();
//		mapFacade.jump(playerId, sourceX, sourceY, targetX, targetY);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("角色:[{}] 更新跳跃信息. Source:[{}-{}], Target:[{}-{}] ", 
					new Object[] { playerId, sourceX, sourceY, targetX, targetY });
		}
	}

	
	public void onLogoutEvent(UserDomain userDomain) {
		JUMP_INFOS.remove(userDomain.getPlayerId());
	}

}
