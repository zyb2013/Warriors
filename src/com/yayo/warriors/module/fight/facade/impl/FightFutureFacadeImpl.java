package com.yayo.warriors.module.fight.facade.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.basedb.adapter.OnlineActiveService;
import com.yayo.warriors.basedb.model.BigMapConfig;
import com.yayo.warriors.basedb.model.BulletinConfig;
import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.basedb.model.SkillEffectConfig;
import com.yayo.warriors.common.helper.FightPushHelper;
import com.yayo.warriors.common.helper.FormulaHelper;
import com.yayo.warriors.common.helper.MonsterHelper;
import com.yayo.warriors.common.helper.NoticePushHelper;
import com.yayo.warriors.module.achieve.facade.AchieveFacade;
import com.yayo.warriors.module.achieve.model.AchieveType;
import com.yayo.warriors.module.achieve.model.FirstType;
import com.yayo.warriors.module.active.manager.ActiveManager;
import com.yayo.warriors.module.battlefield.facade.BattleFieldFacade;
import com.yayo.warriors.module.battlefield.rule.BattleFieldRule;
import com.yayo.warriors.module.buffer.entity.UserBuffer;
import com.yayo.warriors.module.buffer.facade.BufferFacade;
import com.yayo.warriors.module.buffer.model.Buffer;
import com.yayo.warriors.module.campbattle.facade.CampBattleFacade;
import com.yayo.warriors.module.campbattle.rule.CampBattleRule;
import com.yayo.warriors.module.drop.facade.LootFacade;
import com.yayo.warriors.module.dungeon.manager.DungeonManager;
import com.yayo.warriors.module.fight.facade.FightFutureFacade;
import com.yayo.warriors.module.fight.model.Context;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.fight.rule.FightRule;
import com.yayo.warriors.module.friends.manager.FriendManager;
import com.yayo.warriors.module.friends.type.FriendType;
import com.yayo.warriors.module.logger.log.ExpLogger;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.GameScreen;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.map.types.ScreenType;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.manager.MonsterManager;
import com.yayo.warriors.module.monster.model.Monster;
import com.yayo.warriors.module.monster.model.MonsterBattle;
import com.yayo.warriors.module.monster.model.MonsterBuffer;
import com.yayo.warriors.module.monster.type.Classification;
import com.yayo.warriors.module.monster.type.MonsterType;
import com.yayo.warriors.module.notice.rule.NoticeRule;
import com.yayo.warriors.module.notice.type.NoticeID;
import com.yayo.warriors.module.notice.type.NoticeType;
import com.yayo.warriors.module.onhook.facade.TrainFacade;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.props.facade.PropsFacade;
import com.yayo.warriors.module.props.rule.PropsRule;
import com.yayo.warriors.module.task.facade.AllianceTaskFacade;
import com.yayo.warriors.module.task.facade.CampTaskFacade;
import com.yayo.warriors.module.task.facade.EscortTaskFacade;
import com.yayo.warriors.module.task.facade.TaskMainFacade;
import com.yayo.warriors.module.team.facade.TeamFacade;
import com.yayo.warriors.module.team.model.Team;
import com.yayo.warriors.module.treasure.facade.TreasureFacade;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.module.vip.manager.VipManager;
import com.yayo.warriors.module.vip.model.VipDomain;
import com.yayo.warriors.module.vip.model.VipFunction;
import com.yayo.warriors.type.ElementType;
import com.yayo.warriors.type.FormulaKey;

/**
 * 战斗善后处理(处理经验, 任务, 等其他功能)
 * 
 * @author Hyint
 */
@Component
public class FightFutureFacadeImpl implements FightFutureFacade {
	@Autowired
	private LootFacade lootFacade;
	@Autowired
	private VipManager vipManager;
	@Autowired
	private TeamFacade teamFacade;
	@Autowired
	private PetManager petManager;
	@Autowired
	private TrainFacade trainFacade;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PropsFacade propsFacade;
	@Autowired
	private BufferFacade bufferFacade;
	@Autowired
	private ActiveManager activeManager;
	@Autowired
	private FriendManager friendManager;
	@Autowired
	private MonsterManager monsterManager;
	@Autowired
	private DungeonManager dungeonManager;
	@Autowired
	private TaskMainFacade taskEntityFcade;
	@Autowired
	private AchieveFacade achieveFacade;
	@Autowired
	private CampTaskFacade campTaskFacade;
	@Autowired
	private TreasureFacade treasureFacade;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private CampBattleFacade campBattleFacade;
	@Autowired
	private EscortTaskFacade escortTaskFacade;
	@Autowired
	private AllianceTaskFacade allianceTaskFacade;
	@Autowired
	private OnlineActiveService onlineActiveService;
	@Autowired
	private BattleFieldFacade battleFieldFacade;
	
	/** 日志 */
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	/**
	 * 处理怪物的伤害
	 * 
	 * @param  monsterDomain	怪物的域模型
	 * @param  attacker			攻击者
	 * @param  hurtHp			伤害值
	 */
	
	public void processMonsterFightHurt(MonsterDomain monsterDomain, UnitId attacker, int hurtHp) {
		campBattleFacade.processMonsterHurt(monsterDomain, attacker, hurtHp);
	}

	/**
	 * 处理战斗上下文的善后工作. 包括奖励, 任务, 经验等
	 * 
	 * @param context		战斗上下文
	 */
	
	public void executeFightContext(ISpire attackUnit, Context context) {
		if(context != null) {
			processCancelPlayerTrain(context);
			monsterDeadExecutor(attackUnit, context);
			processFightUnitDamage(attackUnit, context);
		}
	}
	
	/**
	 * 处理战斗单位伤害值
	 * 
	 * @param  attacker		攻击者
	 * @param  context		上下文
	 */
	private void processFightUnitDamage(ISpire attacker, Context context) {
		for (Entry<ISpire, Integer> entry : context.getHurtInfo().entrySet()) {
			ISpire target = entry.getKey();
			if(target == null || target.getType() != ElementType.MONSTER) {
				continue;
			}
			
			Integer value = entry.getValue();
			if(value == null || value <= 0) {
				continue;
			}
			
			try {
				processMonsterFightHurt((MonsterDomain) target, attacker.getUnitId(), value);
			} catch (Exception e) {
				LOGGER.error("{}", e);
			}
		}
	}
	
	/**
	 * 取消角色挂机操作
	 * 
	 * @param context		战斗上下文
	 */
	private void processCancelPlayerTrain(Context context) {
		for (ISpire unitId : context.getPlayerFightings()) {
			if(unitId.getType() == ElementType.PLAYER) {
				trainFacade.cancelSingleTrain(unitId.getId());
			} else if(unitId.getType() == ElementType.PET) {
				PetDomain petDomain = petManager.getPetDomain(unitId.getId());
				if(petDomain != null) {
					trainFacade.cancelSingleTrain(petDomain.getPlayerId());
				}
			}
		}
	}

	/**
	 * 怪物死亡善后处理
	 * 
	 * @param attackId			攻击者ID
	 * @param context			战斗上下文对象
	 */
	private void monsterDeadExecutor(ISpire attackUnit, Context context) {
		if(context == null || context.getFightDeadUnits().isEmpty()) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("[战斗]. 死亡列表为空, 不需要计算死亡处理");
			}
			return;
		}
		
		long attackId = attackUnit.getId();
		int attackUnitType = attackUnit.getType().ordinal();
		Set<Long> teamMemberIds = context.getTeamMemberIds();
		for (ISpire deadUnit : context.getFightDeadUnits()) {
			switch (deadUnit.getType()) {
				case PLAYER: 
					processPlayerDead((UserDomain) deadUnit, attackUnit); 
					updateTaskKillPlayer(attackUnit, deadUnit); 
					break;
				case MONSTER: 	
					processMonsterDead(deadUnit, attackId, attackUnitType, teamMemberIds);	
					break;
			}
		}
	}
	
	/**
	 * 处理角色死亡操作
	 * 
	 * @param userDomain	用户域模型
	 */
	
	public void processPlayerDead(UserDomain userDomain, UnitId attackerUnitId) {
		ISpire attacker = null;
		if(attackerUnitId != null) {
			if(attackerUnitId.getType() == ElementType.PLAYER) {
				attacker = userManager.getUserDomain(attackerUnitId.getId());
			} else if(attackerUnitId.getType() == ElementType.MONSTER) {
				attacker = monsterManager.getMonsterDomain(attackerUnitId.getId());
			} else if(attackerUnitId.getType() == ElementType.PET) {
				attacker = petManager.getPetDomain(attackerUnitId.getId());
			}
		}
		processPlayerDead(userDomain, attacker);
	}
	
	/**
	 * 处理角色死亡
	 * 
	 * @param userDomain	用户域模型对象
	 * @param attacker		攻击者精灵接口
	 */
	private void processPlayerDead(UserDomain userDomain, ISpire attacker) {
		if(userDomain == null) {
			return;
		}
		
		try {
			long playerId = userDomain.getPlayerId();
			int damageValue = PropsRule.DEAD_DAMAGE_EQUIP_VALUE;
			propsFacade.damageUserEquipEndure(playerId, damageValue);
		} catch (Exception e) {
			LOGGER.error("{}", e);
		}
		
		try {
			processPlayerBufferClear(userDomain);
		} catch (Exception e) {
			LOGGER.error("{}", e);
		}
		
		if(attacker.getType() == ElementType.PLAYER) {
			try {
				UserDomain attackerDomain = (UserDomain)attacker;
				campBattleFacade.processKillPlayers( attackerDomain, userDomain);
				battleFieldFacade.processKillPlayers(attackerDomain, userDomain);
				pushCampMemberKilledNotice(attackerDomain, userDomain);//杀人公告
			} catch (Exception e) {
				LOGGER.error("{}", e);
			}
		}
		
		if(attacker.getType() == ElementType.PET){
			try {
				PetDomain petDomain = (PetDomain)attacker;
				if(petDomain != null){
					UserDomain attackerDomain = userManager.getUserDomain(petDomain.getPlayerId());
					updateTaskKillPlayer(attackerDomain, userDomain);//处理任务
					pushCampMemberKilledNotice(attackerDomain, userDomain);//杀人公告
				}
			} catch (Exception e) {
				LOGGER.error("{}", e);
			}
		}
		
		
	}
	
	
	/**
	 * 处理角色死亡Buffer移除
	 * 
	 * @param userDomain		用户域模型
	 */
	private void processPlayerBufferClear(UserDomain userDomain) {
		UserBuffer userBuffer = userDomain.getUserBuffer();
		if(userBuffer == null) {
			return;
		}
		
		Map<Integer, Buffer> bufferInfos = userBuffer.getBufferInfos();
		Map<Integer, Buffer> deBufferInfos = userBuffer.getDeBufferInfos();
		if(bufferInfos.isEmpty() && deBufferInfos.isEmpty()) {
			return;
		}
		
		Set<Integer> bufferIds = new HashSet<Integer>();;
		ChainLock lock = LockUtils.getLock(userBuffer);
		try {
			lock.lock();
			bufferInfos = userBuffer.getBufferInfos();
			deBufferInfos = userBuffer.getDeBufferInfos();
			if(bufferInfos.isEmpty() && deBufferInfos.isEmpty()) {
				return;
			}
			
			if(deBufferInfos != null && !deBufferInfos.isEmpty()) {
				bufferIds.addAll(deBufferInfos.keySet());
				deBufferInfos.clear();
				userBuffer.updateDeBufferInfos(false);
			}
			
			if(bufferInfos != null && !bufferInfos.isEmpty()) {
				boolean hasRemove = false;
				for (Iterator<Entry<Integer, Buffer>> it = bufferInfos.entrySet().iterator(); it.hasNext();) {
					Entry<Integer, Buffer> entry = it.next();
					Integer bufferId = entry.getKey();
					Buffer buffer = entry.getValue();
					if(bufferId == null || buffer == null || buffer.isTimeOut()) {
						if(bufferId != null) bufferIds.add(bufferId);
						hasRemove = true;
						it.remove();
						continue;
					}

					SkillEffectConfig skillEffect = resourceService.get(bufferId, SkillEffectConfig.class);
					if(skillEffect == null) {
						it.remove();
						bufferIds.add(bufferId);
						hasRemove = true;
						continue;
					}
					
					int effectType = skillEffect.getEffectType();
					if(ArrayUtils.contains(FightRule.DEAD_REMOVE_BUFFER_TYPES, effectType)) {
						it.remove();
						bufferIds.add(bufferId);
						hasRemove = true;
						continue;
					}
				}
				
				if(hasRemove) {
					userBuffer.updateBufferInfos(false);
				}
			}
		} finally {
			lock.unlock();
		}
		
		if(!bufferIds.isEmpty()) {
			GameMap gameMap = userDomain.getGameMap();
			if(gameMap == null) {
				return;
			}
		
			Set<Long> playerIds = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
			FightPushHelper.pushUnitBufferClear(userDomain.getUnitId(), bufferIds.toArray(), playerIds);
		}
	}
	
	/**
	 * 处理怪物死亡
	 * 
	 * @param iSpire			怪物ID
	 * @param castId			释放技能的单位ID
	 * @param unitType			释放技能的单位类型
	 * @param teamMemberIds		队伍成员列表
	 */
	
	public void processMonsterDead(ISpire iSpire, long castId, int unitType, Set<Long> teamMemberIds) {
		if(iSpire != null && iSpire.getType() == ElementType.MONSTER) {
			MonsterDomain monsterDomain = (MonsterDomain) iSpire;
			try {
				processMonsterDeadBufferClear(monsterDomain);
			} catch (Exception e) {
				LOGGER.error("{}", e);
			}
			try {
				processMonsterExp(monsterDomain, castId, unitType, teamMemberIds);
			} catch (Exception e) {
				LOGGER.error("{}", e);
			}
			try {
				processMonsterDeadNotice(monsterDomain, castId, unitType);
			} catch (Exception e) {
				LOGGER.error("{}", e);
			}
			try {
				processFriendsValue(castId, unitType, teamMemberIds);        // 好友度处理 ---- 超平
			} catch (Exception e) {
				LOGGER.error("{}", e);
			}
		}
	}
	
	/**
	 * 处理怪物死亡公告
	 * 
	 * @param  iSpire
	 * @param  castId
	 * @param  unitType
	 */
	private void processMonsterDeadNotice(MonsterDomain monsterDomain, long castId, int unitType) {
		MonsterBattle monsterBattle = monsterDomain.getMonsterBattle();
		MonsterFightConfig monsterFight = monsterBattle.getMonsterFight();
		if(monsterFight.getMonsterType() != MonsterType.BOSS.getValue()) { //不是BOSS
			return;
		}
		
		long playerId = castId;
		if(unitType == ElementType.PET.ordinal()) {
			PetDomain petDomain = petManager.getPetDomain(castId);
			if(petDomain != null) {
				playerId = petDomain.getPlayerId();
			}
		}
		
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return;
		}
		Player player = userDomain.getPlayer();
		
		GameMap gameMap = monsterDomain.getGameMap();
		if (gameMap != null && gameMap.getScreenType() == ScreenType.NEUTRAL.ordinal()) {
			BulletinConfig bulletinConfig = NoticePushHelper.getConfig(NoticeID.KILL_BOSS_NOTICE, BulletinConfig.class);
			if (bulletinConfig != null) {
				int priority = bulletinConfig.getPriority();
				Map<String, Object> params = new HashMap<String, Object>(3);
				params.put(NoticeRule.playerId, playerId);
				params.put(NoticeRule.playerName, player.getName());
				params.put(NoticeRule.monsterBaseId, monsterFight.getName());
				NoticePushHelper.pushNotice(NoticeID.KILL_BOSS_NOTICE, NoticeType.HONOR, params, priority);
			}
		}
		
		if(gameMap != null){
			if(!monsterDomain.isPrepareResurrection() && monsterFight.canRevive()){
				monsterDomain.prepareResurrection();
			}
			long reviveTime = monsterDomain.getTiredTime(MonsterDomain.RESURRECTION_TIME);
			MonsterHelper.recordMonsterResurrection(monsterDomain.getBranching(), monsterDomain.getMonsterConfig().getId(), reviveTime, playerId, monsterFight.isBoss(), gameMap.getMapConfig().getScreenType() );
		}
	}
	
	
	
	/**
	 * 处理怪物死亡, Buffer 清除
	 * 
	 * @param monsterDomain		怪物域模型
	 */
	private void processMonsterDeadBufferClear(MonsterDomain monsterDomain) {
		bufferFacade.removeBufferFromScheduler(monsterDomain.getUnitId());
		MonsterBuffer monsterBuffer = monsterDomain.getMonsterBuffer(false);
		if(monsterBuffer == null || monsterBuffer.isAllBufferEmpty()) {
			return;
		}
		
		Set<Integer> bufferIds = null;
		ChainLock lock = LockUtils.getLock(monsterBuffer);
		try {
			lock.lock();
			if(monsterBuffer.isAllBufferEmpty()) {
				return;
			}
			bufferIds = monsterBuffer.removeAllBufferIds();
		} finally {
			lock.unlock();
		}
		
		if(bufferIds == null || bufferIds.isEmpty()) {
			return;
		}

		GameScreen currentScreen = monsterDomain.getCurrentScreen();
		if(currentScreen == null) {
			return;
		}
		
		GameMap gameMap = currentScreen.getGameMap();
		if(gameMap == null) {
			return;
		}
		
		ElementType playerElement = ElementType.PLAYER;
		Set<Long> playerIds = gameMap.getCanViewsSpireIdCollection(monsterDomain, playerElement);
		FightPushHelper.pushUnitBufferClear(monsterDomain.getUnitId(), bufferIds.toArray(), playerIds);
	}
	
	
	/**
	 * 处理怪物经验
	 * 
	 * @param monsterDomain
	 * @param castId
	 * @param unitType
	 * @param teamMemberIds
	 */
	private void processMonsterExp(MonsterDomain monsterDomain, long castId, int unitType, Set<Long> teamMemberIds) {
		long playerId = 0L;
		long userPetId = 0L;
		long monsterId = monsterDomain.getId();
		Monster monster = monsterDomain.getMonster();
		MonsterFightConfig fightConfig = monster.getMonsterFightConfig();
		if(fightConfig.getExp() <= 0) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("怪物:[{}] 名字:[{}] 没有经验奖励, 不需要处理", monsterId, fightConfig.getName());
			}
			return;
		}

		if(unitType == ElementType.PLAYER.ordinal()) {
			playerId = castId;
			PetDomain petDomain = petManager.getFightingPet(playerId);
			if(petDomain != null) {
				userPetId = petDomain.getBattle().getId();
			}
		} else if(unitType == ElementType.PET.ordinal()) {
			PetDomain petDomain = petManager.getPetDomain(castId);
			if(petDomain != null) {
				userPetId = petDomain.getPet().getId();
				playerId = petDomain.getPet().getPlayerId();
			}
		}
		
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return;
		}
		
		GameMap gameMap = userDomain.getGameMap();
		if(gameMap == null) {
			return;
		}
		
		int totalExp = 0;
		teamMemberIds = teamMemberIds == null ? new HashSet<Long>(0) : teamMemberIds;
		Set<Long> views = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
		teamMemberIds.retainAll(views);
		int members = teamMemberIds.size();
		int outputExp = calcMonsterExpOutput(userDomain, members, fightConfig);			//怪物产出的经验
		final boolean treasureMonster = treasureFacade.isTreasureMonster(monsterId);	//是否藏宝图怪物
		if( !treasureMonster ) {	//家将打死, 也是人类的功劳.
			for (Long memberId : teamMemberIds) {
				if(memberId == playerId) { //主动发起攻击者
					this.updateTaskInfo(userDomain, fightConfig);									//更新主动攻击者的任务
					this.doFightMonsterDrop(userDomain, monsterDomain);								//怪物掉落
					int expReward = getExpReward(userDomain, members, true, fightConfig, outputExp);
					if(this.doFightMonsterExpReward(memberId, userPetId, expReward)) {					//战斗经验奖励
						ExpLogger.fightExp(userDomain, fightConfig, expReward);
					}
					totalExp += expReward;
				} else {
					UserDomain domain = userManager.getUserDomain(memberId);       
					if(domain != null) {
						this.updateTaskInfo(domain, fightConfig);									//更新主动攻击者的任务
						int expReward = getExpReward(domain, members, false, fightConfig, outputExp);
						if(this.doFightMonsterExpReward(memberId, userPetId, expReward)) {			//战斗经验奖励
							ExpLogger.fightExp(userDomain, fightConfig, expReward);
						}
						totalExp += expReward;
					}
				}
			}
			
		} else {	//藏宝图怪物
			final int expReward = getExpReward(userDomain, members, true, fightConfig, outputExp);
			treasureFacade.rewardMonsterExp(userDomain, userPetId, expReward, monsterId);
			this.doFightMonsterDrop(userDomain, monsterDomain);	//怪物掉落
		}
		
		if(monster.isDungeon()){
			long dungeonId = monster.getDungeonId();
			this.updateDungeon(dungeonId, monsterId, totalExp);
		} else if (!monster.isDungeon() && fightConfig.isBoss()) {
			activeManager.modifyMonsterKiller(userDomain.getBranching(), fightConfig.getId(), userDomain.getPlayer().getName());
		}
		
		// 处理击杀怪物达成 成就
		achieveFacade.killMonsterAchieved(playerId, 1);
		achieveFacade.firstAchieved(playerId, AchieveType.FIRST_ACHIEVE, FirstType.FIRST_KILL_MONSTER);
	}
	
	/**
	 * 计算经验产出(策划确认: 怪物的经验产出, 是按照打死那个怪的玩家来确认的);
	 * 
	 * @param  userDomain		角色域模型
	 * @param  teamMembers		组队人数
	 * @param  fightConfig		怪物的战斗对象
	 * @return {@link Integer} 	总共产出的经验
	 */
	private int calcMonsterExpOutput(UserDomain userDomain, int teamMembers, MonsterFightConfig fightConfig) {
		int totalExp = fightConfig.getExp();
		if(teamMembers > 1) {
			totalExp = FormulaHelper.invoke(FormulaKey.TEAM_PLAYER_ATTACK_EXP, fightConfig.getExp(), teamMembers).intValue();
		}
		
		if(fightConfig.isExpAddition()) {						//杀死怪物的单位做等级受限
			int monsterLevel = fightConfig.getLevel();			//怪物的等级
			int formulaId = FormulaKey.PLAYER_FIGHT_EXP_RATE;	//角色战斗经验概率
			int playerLevel = userDomain.getBattle().getLevel();//角色的等级
			totalExp *= FormulaHelper.invoke(formulaId, playerLevel, monsterLevel).doubleValue();
		}
		return totalExp;
	}
	
	/**
	 * 计算经验加成
	 * 
	 * @param  userDomain		角色对象
	 * @param  fightExp			战斗经验
	 * @param  teamMembers		组队成员数量
	 * @param  isAttacker		是否发起攻击者
	 * @return {@link Integer}	经验加成
	 */
	private int getExpReward(UserDomain userDomain, int teamMembers, boolean isAttacker, MonsterFightConfig fightConfig, int addExp) {
		int totalExp = addExp; 			//打怪的经验
		if(teamMembers > 1 && !isAttacker) { 	//策划要求同屏有2个人以上(含2人) 才有队伍加成
			int teamShareExpId = FormulaKey.TEAM_PLAYER_SHAREEXP;
			totalExp = FormulaHelper.invoke(teamShareExpId, totalExp, teamMembers).intValue();
		}
		
		//是否经验受限. 
		//true - 受BUFF药、vip加成、等级压制等影响
		//false - 不受任何条件影响，直接取值(这里是要计算组队经验的),  
		if(!fightConfig.isExpAddition()) {
			return totalExp;
		}
		
		//VIP 加成
		double addRatio = 1D;
		PlayerBattle battle = userDomain.getBattle();	//角色战斗对象
		VipDomain vip = vipManager.getVip(battle.getId());
		if(vip != null && vip.isVip()) {
			addRatio += vip.floatValue(VipFunction.MonsterExpPercent);
		}
		
		//BUFF经验丹处理
		if(fightConfig.getClassification() != Classification.DUNGEON) {
			addRatio += (battle.getAttributeRate(AttributeKeys.FIGHT_EXP_RATE) - 1);
		}
		
		//活动经验加成
		addRatio += onlineActiveService.getMonsterProfit(userDomain);
		return userDomain.getPlayer().calcIndulgeProfit((int)(totalExp * addRatio));
	}
	
	/**
	 * 更新任务信息
	 * 
	 * @param  userDomain			角色的域模型
	 * @param  fightConfig			战斗配置信息
	 */
	private void updateTaskInfo(UserDomain userDomain, MonsterFightConfig fightConfig) {
		this.updateTaskFightCount(userDomain.getPlayerId(), fightConfig);
	}
	
	/**
	 * 处理攻击怪物掉落奖励
	 * 
	 * @param playerId			角色ID
	 * @param monsterDomain		怪物的域模型
	 */
	private void doFightMonsterDrop(UserDomain userDomain, MonsterDomain monsterDomain) {
		Player player = userDomain.getPlayer();
		if(player.isGoodsReward()) {
			long playerId = userDomain.getPlayerId();
			Monster monster = monsterDomain.getMonster();
			MonsterFightConfig fightConfig = monster.getMonsterFightConfig();
			Map<Integer, Integer> dropMap = fightConfig.getDropMap();
			Collection<Long> sharePlayers = getShareRewardPlayerIds(playerId);
			lootFacade.createFightLoot(userDomain, sharePlayers, monsterDomain, dropMap);
		}
	}
	
	/**
	 * 获得共享此奖励的角色ID
	 * 
	 * @param  playerId				角色ID
	 * @return {@link Collection}	角色ID列表
	 */
	private Collection<Long> getShareRewardPlayerIds(long playerId) {
		Set<Long> playerIds = new HashSet<Long>();
		Team playerTeam = teamFacade.getPlayerTeam(playerId);
		if(playerTeam != null) {
			playerIds.addAll(playerTeam.getMembers());
		} else {
			playerIds.add(playerId);
		}
		return playerIds;
	}
	
	/**
	 * 处理经验计算(包括将来组队加成, BUFF加成, 夫妻加成等.)
	 * 
	 * @param playerId		角色ID
	 * @param userPetId		用户召唤兽ID
	 * @param addExp		需要增加的经验值
	 */
	private boolean doFightMonsterExpReward(long playerId, long userPetId, int addExp) {
		if(addExp <= 0) {
			return false;
		}
		return this.userManager.addPlayerExp(playerId, addExp, false);
	}
	
	/**
	 * 更新任务的数量
	 * 
	 * @param  playerId				角色ID						
	 * @param  monsterId			怪物ID
	 */
	private void updateTaskFightCount(long playerId, MonsterFightConfig monsterFight) {
		if(monsterFight != null) {
			taskEntityFcade.updateFightMonsterTask(playerId, monsterFight);
			campTaskFacade.updateFightMonsterTask(playerId,  monsterFight.getBaseId());
			allianceTaskFacade.updateKillMonster(playerId,   monsterFight.getBaseId());
		}
	}
	
	/**
	 * 更新副本中的怪物数量
	 * @param dungeonId        副本的ID
	 * @param monsterId        怪物的ID
	 * @param exp              杀死怪物后获得的经验
	 */
	private void updateDungeon(long dungeonId,long monsterId,int exp) {
		dungeonManager.killDungeonMonster(dungeonId, monsterId,exp);
	}
	
	/**
	 * 更新阵营任务杀死玩家数量
	 * @param attackUnitId    
	 * @param deadUnitId
	 */
	private void updateTaskKillPlayer(ISpire attackUnit, ISpire deadUnit) {
		if(attackUnit == null || deadUnit == null){
			return;
		}
		
		if(attackUnit.getType() == ElementType.PLAYER && deadUnit.getType() == ElementType.PLAYER){ //更新护送任务劫镖 
			escortTaskFacade.updatePlayerPlunderEscort(attackUnit.getId(), deadUnit.getId());
			
			UserDomain deadDomain = (UserDomain) deadUnit; //阵营任务,杀人任务
			if(deadDomain != null){
				Camp camp = deadDomain.getPlayer().getCamp();
				campTaskFacade.updateFightPlayerTask(attackUnit.getId(), camp);
			}
		}
		
		if(attackUnit.getType() == ElementType.MONSTER && deadUnit.getType() == ElementType.PLAYER){ //更新护送任务劫镖
			escortTaskFacade.updateMonsterPlunderEscort(attackUnit.getId(), deadUnit.getId());
		}
	}
	
	
	/**
	 * 发送杀人公告
	 * @param attackId
	 * @param playerId
	 */
	private void pushCampMemberKilledNotice(UserDomain attackDomain, UserDomain deadDomain) {
		if (attackDomain == null || deadDomain == null || attackDomain.getMapId() == CampBattleRule.CAMP_BATTLE_MAPID || attackDomain.getMapId() == BattleFieldRule.BATTLE_FIELD_MAPID) {
			return;
		}
		
		Player player = attackDomain.getPlayer();
		Player target = deadDomain.getPlayer();
		
		if (player.getCamp() != target.getCamp()) {
			BulletinConfig bConfig = resourceService.get(NoticeID.KILL_CAMP_MEMBER, BulletinConfig.class);
			if (bConfig != null) {
				BigMapConfig config = resourceService.get(attackDomain.getMapId(), BigMapConfig.class);
				Map<String, Object> params = new HashMap<String, Object>(5);
				params.put(NoticeRule.playerName, player.getName());
				params.put(NoticeRule.targetName, target.getName());
				params.put(NoticeRule.campName, player.getCamp().getName());
				params.put(NoticeRule.campName2, target.getCamp().getName());
				params.put(NoticeRule.map, config == null ? "" : config.getName());
				NoticePushHelper.pushNotice(NoticeID.KILL_CAMP_MEMBER, NoticeType.HONOR, params, bConfig.getPriority());
			}
		} else {
			BulletinConfig bConfig = resourceService.get(NoticeID.KILL_SELF_CAMP_MEMBER, BulletinConfig.class);   // 杀自己阵营玩家
			if (bConfig != null) {
				Map<String, Object> params = new HashMap<String, Object>(3);
				params.put(NoticeRule.playerName, player.getName());
				params.put(NoticeRule.targetName, target.getName());
				params.put(NoticeRule.campName, player.getCamp().getName());
				NoticePushHelper.pushNotice(bConfig.getId(), NoticeType.HONOR, params, bConfig.getPriority());
			}
		}
	}
	
	private void processFriendsValue(long castId, int unitType, Set<Long> teamMemberIds) {
		if (unitType != ElementType.PLAYER.ordinal()) {
			return;
		}
		
		if (teamMemberIds == null || teamMemberIds.isEmpty()) {
			return;
		}
		
		long playerId = castId;
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return;
		}
		
		for (long memberId : teamMemberIds) {
			if (playerId == memberId) {
				continue;
			}
			
			UserDomain domain = userManager.getUserDomain(memberId);
			if (domain == null) {
				continue;
			}
			
			if (friendManager.isFriend(playerId, memberId, FriendType.FRIENDLY)) {
				friendManager.plusKill4Monster(playerId, memberId);
			}
			if (friendManager.isFriend(memberId, playerId, FriendType.FRIENDLY)) {
				friendManager.plusKill4Monster(memberId, playerId);
			}
		}
	}
	
	
}
