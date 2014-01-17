package com.yayo.warriors.module.user.facade.impl;
import static com.yayo.warriors.module.pack.type.BackpackType.*;
import static com.yayo.warriors.module.pet.constant.PetConstant.*;
import static com.yayo.warriors.module.user.rule.PlayerRule.*;
import static com.yayo.warriors.module.vip.model.VipFunction.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap.Builder;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.event.EventBus;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.DateUtil;
import com.yayo.common.utility.EnumUtils;
import com.yayo.warriors.basedb.adapter.BattleFieldService;
import com.yayo.warriors.basedb.adapter.CampBattleService;
import com.yayo.warriors.basedb.adapter.PetService;
import com.yayo.warriors.basedb.model.BattlePointConfig;
import com.yayo.warriors.basedb.model.PetConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.basedb.model.RevivePointConfig;
import com.yayo.warriors.basedb.model.VipConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.EquipPushHelper;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.common.helper.VipPushHelper;
import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.event.CreatePlayerEvent;
import com.yayo.warriors.module.battlefield.constant.BattleFieldConstant;
import com.yayo.warriors.module.battlefield.facade.BattleFieldFacade;
import com.yayo.warriors.module.campbattle.constant.CampBattleConstant;
import com.yayo.warriors.module.campbattle.facade.CampBattleFacade;
import com.yayo.warriors.module.chat.facade.ChannelFacade;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.dungeon.manager.DungeonManager;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.map.model.RevivePosition;
import com.yayo.warriors.module.map.rule.MapRule;
import com.yayo.warriors.module.map.types.ScreenType;
import com.yayo.warriors.module.onlines.manager.OnlineStatisticManager;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.pet.entity.Pet;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.pet.facade.PetFacade;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.pet.rule.PetRule;
import com.yayo.warriors.module.pet.types.PetJob;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.module.props.type.PropsChildType;
import com.yayo.warriors.module.task.entity.UserEscortTask;
import com.yayo.warriors.module.task.manager.EscortTaskManager;
import com.yayo.warriors.module.user.constant.UserConstant;
import com.yayo.warriors.module.user.entity.DailyRecord;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.entity.PlayerMotion;
import com.yayo.warriors.module.user.facade.UserFacade;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.BindSessionResult;
import com.yayo.warriors.module.user.model.InitCreateInfo;
import com.yayo.warriors.module.user.model.LoginResult;
import com.yayo.warriors.module.user.model.LoginWrapper;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.rule.AttributeRule;
import com.yayo.warriors.module.user.rule.PlayerRule;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.module.user.type.Capacity;
import com.yayo.warriors.module.user.type.FightMode;
import com.yayo.warriors.module.user.type.Job;
import com.yayo.warriors.module.user.type.KickCode;
import com.yayo.warriors.module.user.type.ReceiveInfo;
import com.yayo.warriors.module.user.type.Sex;
import com.yayo.warriors.module.vip.entity.PlayerVip;
import com.yayo.warriors.module.vip.facade.VipFacade;
import com.yayo.warriors.module.vip.manager.VipManager;
import com.yayo.warriors.module.vip.model.VipDomain;
import com.yayo.warriors.socket.handler.user.UserHandler;
import com.yayo.warriors.type.AdultState;
import com.yayo.warriors.type.ElementType;
import com.yayo.warriors.type.GoodsType;
import com.yayo.warriors.util.GameConfig;
import com.yayo.warriors.util.NameUtils;

/**
 * 用户接口实现类
 * 
 * @author Hyint
 */
@Component
public class UserFacadeImpl implements UserFacade, UserConstant {
	@Autowired
	private EventBus eventBus;
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private MapFacade mapFacade;
	@Autowired
	private PetFacade petFacade;
	@Autowired
	private VipFacade vipFacade;
	@Autowired
	private PetManager petManager;
	@Autowired
	private PetService petService;
	@Autowired
	private VipManager vipManager;
	@Autowired
	private DbService cachedService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private ChannelFacade channelFacade;
	@Autowired
	private DungeonManager dungeonManager;
	@Autowired
	private EscortTaskManager escortTaskManager;
	@Autowired
	private OnlineStatisticManager onlineStatisticManager;
	@Autowired
	private UserHandler userHandler;
	@Autowired
	private EscortTaskManager escortManager;
	@Autowired
	private CampBattleFacade campBattleFacade;
	@Autowired
	private CampBattleService campBattleService;
	@Autowired
	private BattleFieldFacade battleFieldFacade;
	@Autowired
	private BattleFieldService battleFieldService;
	
	@Autowired(required=false)
	@Qualifier("game.update.fight.mode.flag")
	private Boolean isOpenUpdateFightMode = true;
	
	/** 日志格式 */
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	/** 角色名注册缓存.*/
	private static final ConcurrentHashSet<String> PLAYER_NAME_CACHE = new ConcurrentHashSet<String>();
	/** 用户名锁集合. { 帐号名, 对象锁 }*/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private final ConcurrentLinkedHashMap<String, Object> OBJECT_LOCKERS = new ConcurrentLinkedHashMap.Builder().maximumWeightedCapacity(500).build();
	/** 登录信息集合 */
	private static final Builder<String, LoginWrapper> BUILDER = new ConcurrentLinkedHashMap.Builder<String, LoginWrapper>();
	private static final ConcurrentLinkedHashMap<String, LoginWrapper> LOGIN_WARPPERS = BUILDER.maximumWeightedCapacity(5000).build();
 
	/**
	 * 查询登陆返回值
	 * 
	 * @param accountId					账号ID
	 * @return {@link ResultObject}		返回值对象, 内容: {@link LoginResult}
	 */
	
	public LoginResult getLoginResult(String userName) {
		LoginResult loginResult = LoginResult.valueOf();
		List<Long> playerIds = userManager.listPlayerIdByUserName(userName);
		for (Long playerId : playerIds) {
			if(userManager.isOnline(playerId)) {
				loginResult.addOnlinePlayers(playerId);
			}
			loginResult.addLoginVo2List(voFactory.getLoginVO(playerId));
		}
		return loginResult;
	}
	
	/**
	 * 查询对象锁
	 * 
	 * @param playerName
	 * @return
	 */
	private Object getObjectLock(String userName) {
		Object lock = OBJECT_LOCKERS.get(userName);
		if(lock == null) {
			OBJECT_LOCKERS.putIfAbsent(userName, new Object());
			lock = OBJECT_LOCKERS.get(userName);
		}
		return lock;
	}
	
	/**
	 * 创建角色对象
	 * 
	 * @param  userName				用户名
	 * @param  password				密     码
	 * @param  playerName			角色名
	 * @param  job					角色职业
	 * @param  sex					角色性别
	 * @param  icon					角色图标
	 * @return {@link ResultObject}	返回值对象
	 */
	
	public int createPlayer(String userName, String password, String playerName, int job, int sex, int icon) {
		if(StringUtils.isBlank(userName)) {
			return PARAM_INVALID;
		}
		
		if(!NameUtils.validPlayerName(playerName)) {
			return PARAM_INVALID;
		}
		
		Player player = userManager.getPlayer(playerName);
		if(player != null) {
			return PLAYER_NAME_REPEAT;
		}
		
		if(!GameConfig.canCreateCharacter(userName)) { //没有开启帐号创建开关
			return PARAM_INVALID;
		}
		
		List<Long> playerIds = userManager.listPlayerIdByUserName(userName);
		if(playerIds != null && playerIds.size() >= PlayerRule.MAX_CREATE_LIMIT) {
			return CREATE_CHARACT_MAX_LIMIT;
		}
		
		Job jobType = EnumUtils.getEnum(Job.class, job);
		if(jobType == null) {
			return PARAM_INVALID;
		}
		
		Sex sexType = EnumUtils.getEnum(Sex.class, sex);
		if(sexType == null) {
			return PARAM_INVALID;
		} else if(!ArrayUtils.contains(jobType.getSex(), sex)) {
			return SEX_FORBIT_BY_JOB;
		}
		
		boolean removePlayerName = false;
		InitCreateInfo initCreateInfo = null;
		ChainLock lock = LockUtils.getLock(this.getObjectLock(userName));
		try {
			lock.lock();
			if(!(removePlayerName = PLAYER_NAME_CACHE.add(playerName))) {
				return PLAYER_NAME_REPEAT;
			}
			
			player = userManager.getPlayer(playerName);
			if(player != null) {
				return PLAYER_NAME_REPEAT;
			}
			
			playerIds = userManager.listPlayerIdByUserName(userName);
			if(playerIds != null && playerIds.size() >= PlayerRule.MAX_CREATE_LIMIT) {
				return CREATE_CHARACT_MAX_LIMIT;
			}
			
			initCreateInfo = userManager.createPlayer(PlayerRule.initCreateInfo(userName, password, playerName, sexType, icon, jobType));
			userManager.addPlayerId2UserNameCache(userName, initCreateInfo.getPlayerId());
			userManager.addPlayerId2PlayerNameCache(playerName, initCreateInfo.getPlayerId());
			eventBus.post(CreatePlayerEvent.valueOf(initCreateInfo.getPlayerId()));
		} catch (Exception e) {
			LOGGER.error("账号:[{}] 创建角色异常:[{}] ", userName, e);
			return FAILURE;
		} finally {
			if(removePlayerName) {
				PLAYER_NAME_CACHE.remove(playerName);
			}
			lock.unlock();
		}
		
		//统计阵营,职业注册
		onlineStatisticManager.addCampRegisterRecord(initCreateInfo.getCamp());
		onlineStatisticManager.addJobRegisterRecord(jobType);
		return SUCCESS;
	}

	/**
	 * 登陆选择角色
	 * 
	 * @param  userName				账号名
	 * @param  password				密码
	 * @param  playerId				角色ID
	 * @param  branching			分线ID 
	 * @return {@link ResultObject} 返回值对象
	 */
	
	public ResultObject<String> selectPlayer(String userName, String password, long playerId, int branching, String clientIp) {
		List<Long> playerIds = userManager.listPlayerIdByUserName(userName);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("帐号:[{}] 选择:[{}] 角色ID列表:[{}] ", new Object[] { userName, playerId, playerIds });
		}
		
		if (playerIds == null || playerIds.isEmpty()) {
			return ResultObject.ERROR(ACCOUNT_NO_CHARACTER);
		} else if (!playerIds.contains(playerId)) {
			return ResultObject.ERROR(SELECT_CHARACTER_INVALID);
		} 

		if(!channelFacade.validateLogin(branching)) {
			return ResultObject.ERROR(BRANCHING_INVALID);
		}
		
		List<Long> kickPlayers = new ArrayList<Long>();
		for (Long accountPlayerId : playerIds) {
			if (accountPlayerId != playerId && userManager.isOnline(accountPlayerId)) {
				kickPlayers.add(accountPlayerId);
			}
		}
		
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		if(player.isForbid2Login()) {
			return ResultObject.ERROR(PLAYER_FORBID_LOGIN);
		}
		
		UserPushHelper.pushKickOff(KickCode.LOGIN_DUPLICATE, kickPlayers);
		return ResultObject.SUCCESS(putSerialNum2Cache(playerId, branching, clientIp));
	}


	public ResultObject<BindSessionResult> bindNewSession(IoSession session, String serialNum, String clientIp) {
		if(StringUtils.isBlank(serialNum)) {
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		LoginWrapper loginWrapper = this.getLoginWrapper(serialNum);
		if(loginWrapper == null) {
			return ResultObject.ERROR(LOGIN_KEYWORD_TIMEOUT);
		}
		
		long playerId = loginWrapper.getPlayerId();
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		List<Long> playerIds = userManager.listPlayerIdByUserName(player.getUserName());
		if (playerIds == null || playerIds.isEmpty()) {
			return ResultObject.ERROR(ACCOUNT_NO_CHARACTER);
		} else if (!playerIds.contains(playerId)) {
			return ResultObject.ERROR(SELECT_CHARACTER_INVALID);
		} 

		String cacheIp = loginWrapper.getClientIp();
		if(cacheIp == null || clientIp == null) {
			return ResultObject.ERROR(LOGIN_KEYWORD_TIMEOUT);
		} else if(!cacheIp.trim().equals(clientIp.trim())) {
			return ResultObject.ERROR(LOGIN_KEYWORD_TIMEOUT);
		}
		
		this.removeLoginWrapper(serialNum);
		int branching = loginWrapper.getBranching();
		PlayerMotion motion = userDomain.getMotion();
		
		Object[] params = AttributeRule.HEROVO_INFO;						//角色的信息
		Object[] values = userManager.getPlayerAttributes(playerId, params);//角色的信息值
		PlayerDungeon playerDungeon = this.dungeonManager.getPlayerDungeon(playerId);
		userHandler.bindLoginPlayerSession(player, session, branching, clientIp);	// 都成功了, 可以绑定角色的Session到SessionManager中
		mapFacade.doLoginFilter(userDomain, playerDungeon);                 //过滤
		
		return ResultObject.SUCCESS(BindSessionResult.valueOf(branching, player, motion, playerDungeon, params, values));
	}

	/**
	 * 查询登陆封装对象
	 * 
	 * @param  serialNum				随机SN
	 * @return {@link LoginWrapper}		登陆封装对象
	 */
	
	public LoginWrapper getLoginWrapper(String serialNum) {
		LoginWrapper loginWrapper = LOGIN_WARPPERS.get(serialNum);
		if(loginWrapper != null && loginWrapper.isTimeOut()) {
			LOGIN_WARPPERS.remove(serialNum);
			return null;
		}
		return loginWrapper;
	}

	/**
	 * 移除登陆封装对象
	 * 
	 * @param  serialNum				随机SN
	 * @return {@link LoginWrapper}		登陆封装对象
	 */
	
	public void removeLoginWrapper(String serialNum) {
		LOGIN_WARPPERS.remove(serialNum);
	}

	/**
	 * 把登陆序号封装类设置到缓存中
	 * 
	 * @param  playerId 				角色ID
	 * @param  branching				分线号
	 * @param  address					地址信息
	 * @return {@link String}			Key
	 */
	
	public String putSerialNum2Cache(long playerId, int branching, String address) {
		String serialNum = UUID.randomUUID().toString();
		LoginWrapper loginWrapper = LoginWrapper.valueOf(playerId, branching, address);
		LOGIN_WARPPERS.put(serialNum, loginWrapper);
		return serialNum;
	}
	
	/***
	 * 更新角色的战斗模式
	 * 
	 * @param  playerId					角色ID
	 * @param  mode						战斗模式. 详细见:{@link FightMode}
	 * @param  byClient					是否通过客户端修改
	 * @return {@link Integer}			返回值		
	 */
	
	public ResultObject<Integer> updateFightMode(long playerId, int mode, boolean byClient) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		} 

		PlayerBattle battle = userDomain.getBattle();
//		if(battle.isDead()) {
//			return ResultObject.ERROR(PLAYER_DEADED);
//		} else 
//		if(byClient && battle.getPlayerStatus().isFighting()) {
//			return ResultObject.ERROR(ROLE_FIGHTING);
//		}
		
		FightMode fightMode = EnumUtils.getEnum(FightMode.class, mode);
		if(fightMode == null) {
			return ResultObject.ERROR(TYPE_INVALID);
		}
		
		int playerLevel = battle.getLevel();
		if(!fightMode.canChangeMode(playerLevel)) {
			return ResultObject.ERROR(LEVEL_INVALID);
		} else if(battle.getMode() == fightMode) {
			return ResultObject.SUCCESS(battle.getMode().ordinal());
		}
		
		Integer mapId = userDomain.getMapId();
		GameMap gameMapData = userDomain.getCurrentScreen().getGameMap();
		if(gameMapData == null) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int screenType = gameMapData.getScreenType();
		ScreenType screenEnum = EnumUtils.getEnum(ScreenType.class, screenType);
		if(screenEnum == null) {
			return ResultObject.ERROR(FAILURE);
		} 
		
		FightMode[] fightModes = screenEnum.getFightModes();
		if(isOpenUpdateFightMode && !ArrayUtils.contains(fightModes, fightMode)) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		ChainLock lock = LockUtils.getLock(battle);
		try {
			lock.lock();
			if(!fightMode.canChangeMode(playerLevel)) {
				return ResultObject.ERROR(LEVEL_INVALID);
			} else if(battle.getMode() == fightMode) {
				return ResultObject.SUCCESS(battle.getMode().ordinal());
			}
			battle.setMode(fightMode);
		} finally {
			lock.unlock();
		}
		
		Collection<Long> screenViews = mapFacade.getScreenViews(playerId);
		UserPushHelper.pushAttribute2AreaMember(playerId, screenViews, Arrays.asList(userDomain.getUnitId()), AttributeKeys.FIGHT_MODE);
		if(!byClient) {
			UserPushHelper.pushFightMode2Member(playerId, fightMode.ordinal(), mapId);
		}
		return ResultObject.SUCCESS(battle.getMode().ordinal());
	}
	
	
	public int saveGuideStep(long playerId, int stepId) {
		if(stepId < 0 || stepId > 500){
			return UserConstant.INPUT_VALUE_INVALID;
		}
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain != null){
			Player player = userDomain.getPlayer();
			Set<Integer> guides = player.getGuides();
			synchronized (guides) {
				guides.add(stepId);
			}
			
			return UserConstant.SUCCESS;
		}
		return UserConstant.PET_NOT_FOUND;
	}

	/**
	 * 获得角色的属性
	 * 
	 * @param  playerId					角色属性
	 * @param  params					Key参数
	 * @return {@link Object[]}			返回值参数			
	 */
	
	public Object[] getPlayerAttribute(long playerId, Object[] params) {
		return userManager.getPlayerAttributes(playerId, params);
	}

	
	public int backRevive(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		PlayerMotion playerMotion = userDomain.getMotion();
		if(!battle.isDead()){
			return PLAYER_NOT_DEATH;
		}
		
		GameMap gameMap = userDomain.getGameMap();
		if(gameMap == null){
			return FAILURE;
		}
		
		UserEscortTask userEscortTask = escortManager.getEscortTask(battle);
		if(userEscortTask != null && !userEscortTask.isNoTask()){//押镖状态下,无论怎么样复活都是原地复活
			return recurrentWithLevelLimit(userDomain, gameMap);
		}
		
		if(battleFieldFacade.isInBattleField(playerId)){
			return battleFieldRevive(playerId);
		}
		
		if(campBattleFacade.isInCampBattle(userDomain)){
			return campBattleRevive(playerId);
		}
		
		ChainLock lock = LockUtils.getLock(battle);
		try {
			lock.lock();
			if(!battle.isDead()){
				return PLAYER_NOT_DEATH;
			}
			battle.setHp(battle.getHpMax());
		}finally{
			lock.unlock();
		}
		
		//优先判断是否有传送点的配置
		RevivePointConfig reviveConfig = userManager.getRevivePointConfig(playerMotion.getMapId());
		if(reviveConfig != null && player.getCamp() != Camp.NONE){
			RevivePosition revivePosition = null;
			for(RevivePosition position : reviveConfig.getRevivePositions()){
				if(player.getCamp().ordinal() == position.getCamp()){
					revivePosition = position;
				}
			}
			
			if(revivePosition != null){
				Collection<Long> viewPlayers = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
				UserPushHelper.pushPlayerRecurrent(userDomain.getUnitId(), battle.getHp(), viewPlayers);
				mapFacade.go(playerId, revivePosition.getMapId(), revivePosition.getX(), revivePosition.getY(),5);
				return SUCCESS;
			}
		}
		
		ElementType type = ElementType.PLAYER;
		Collection<Long> viewPlayers = gameMap.getCanViewsSpireIdCollection(userDomain, type);
		UserPushHelper.pushPlayerRecurrent(userDomain.getUnitId(), battle.getHp(), viewPlayers);
		mapFacade.go(playerId, MapRule.DEFAUL_REVIVE_MAPID, MapRule.DEFAUL_REVIVE_X, MapRule.DEFAUL_REVIVE_Y,5);
		return SUCCESS;
	}

	
	public int propsRevive(long playerId, long propsId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		PlayerDungeon playerDungeon = dungeonManager.getPlayerDungeon(playerId);
		if(playerDungeon == null){
			return PLAYER_NOT_FOUND;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(!battle.isDead()){
			return PLAYER_NOT_DEATH;
		}
		
		GameMap gameMap = userDomain.getGameMap();
		if(gameMap == null) { 
			return FAILURE;
		}
		
//		策划允许可用原地复活
//		if(battleFieldFacade.isInBattleField(playerId)){
//			return battleFieldRevive(playerId);
//		}
		
		if(campBattleFacade.isInCampBattle(userDomain)){
			return campBattleRevive(playerId);
		}
		
		UserEscortTask userEscortTask = escortTaskManager.getEscortTask(battle);
		if(userEscortTask != null && !userEscortTask.isNoTask()){ //当玩家为运镖状态时,无需道具就能复活
			return recurrentWithLevelLimit(userDomain, gameMap);
		}

		if(battle.getLevel() > PlayerRule.REVIVE_NEED_PROPS_LEVEL) {
			return recurrentWithProps(userDomain, propsId, gameMap);
		} else {
			return recurrentWithLevelLimit(userDomain, gameMap);
		}
	}
	
	
	public int campBattleRevive(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return UserConstant.PLAYER_NOT_FOUND;
		}
		
		GameMap gameMap = campBattleFacade.getCampBattleGameMap(userDomain);
		if(gameMap == null){
			return CampBattleConstant.NOT_IN_CAMP_BATTLE;
		}
		
		Player player = userDomain.getPlayer();
		PlayerMotion playerMotion = userDomain.getMotion();
		PlayerBattle battle = userDomain.getBattle();
		Camp camp = player.getCamp();
		Collection<Integer> ownCampBattlePoints = campBattleFacade.getOwnCampBattlePoints(camp);
		Point nearestRevivePoint = campBattleService.getNearestRevivePoint(camp, ownCampBattlePoints, playerMotion.getX(), playerMotion.getY());
		int targetMapId = MapRule.DEFAUL_REVIVE_MAPID;
		int targetX = MapRule.DEFAUL_REVIVE_X;
		int targetY = MapRule.DEFAUL_REVIVE_Y;
		if(nearestRevivePoint != null){
			targetMapId = gameMap.getMapId();
			targetX = nearestRevivePoint.getX();
			targetY = nearestRevivePoint.getY();
		}
		
		ChainLock lock = LockUtils.getLock(battle);
		try {
			lock.lock();
			if(!battle.isDead()) {
				return PLAYER_NOT_DEATH;
			}
			battle.setHp(battle.getHpMax());
		} finally {
			lock.unlock();
		}
		cachedService.submitUpdate2Queue(battle);
		Collection<Long> viewPlayers = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
		UserPushHelper.pushPlayerRecurrent(userDomain.getUnitId(), battle.getHp(), viewPlayers);
		
		mapFacade.go(playerId, targetMapId, targetX, targetY,5);
		return UserConstant.SUCCESS;
	}
	
	
	public int battleFieldRevive(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return UserConstant.PLAYER_NOT_FOUND;
		}
		
		GameMap gameMap = battleFieldFacade.getBattleFieldGameMap(userDomain);
		if(gameMap == null){
			return BattleFieldConstant.BATTLE_FIELD_NOT_START_OR_OVER;
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		Camp camp = player.getCamp();
		int targetMapId = MapRule.DEFAUL_REVIVE_MAPID;
		int targetX = MapRule.DEFAUL_REVIVE_X;
		int targetY = MapRule.DEFAUL_REVIVE_Y;
		BattlePointConfig battlePointConfig = battleFieldService.getBattlePointConfig(2, camp);
		if(battlePointConfig != null){
			targetMapId = gameMap.getMapId();
			targetX = battlePointConfig.getX();
			targetY = battlePointConfig.getY();
		} else {
			LOGGER.error("乱武战场阵营[{}]的复活点基础数据不存在", camp);
			return BattleFieldConstant.BASEDATA_NOT_FOUND;
		}
		
		ChainLock lock = LockUtils.getLock(battle);
		try {
			lock.lock();
			if(!battle.isDead()) {
				return PLAYER_NOT_DEATH;
			}
			battle.setHp(battle.getHpMax());
		} finally {
			lock.unlock();
		}
		cachedService.submitUpdate2Queue(battle);
		Collection<Long> viewPlayers = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
		UserPushHelper.pushPlayerRecurrent(userDomain.getUnitId(), battle.getHp(), viewPlayers);
		
		mapFacade.go(playerId, targetMapId, targetX, targetY, 10);
		return UserConstant.SUCCESS;
	}

	/**
	 * 使用道具复活
	 * 
	 * @param userDomain		用户域模型对象
	 * @param propsId			使用道具的用户道具ID
	 * @param gameMap			角色所在的地图对象
	 * @return {@link Integer}	使用道具复活返回值
	 */
	@SuppressWarnings("unchecked")
	private int recurrentWithProps(UserDomain userDomain, long propsId, GameMap gameMap) {
		long playerId = userDomain.getPlayerId();
		PlayerBattle battle = userDomain.getBattle();
		UserProps userProps = propsManager.getUserProps(propsId);
		if (userProps == null) {
			return ITEM_NOT_ENOUGH;
		}

		Player player = userDomain.getPlayer();
		if (userProps.getPlayerId() != playerId) {
			return ITEM_NOT_ENOUGH;
		} else if (!userProps.validBackpack(DEFAULT_BACKPACK)) {
			return BACKPACK_INVALID;
		} else if (userProps.getCount() < PlayerRule.REVIVE_USE_PROPS_COUNT) {
			return ITEM_NOT_ENOUGH;
		} else if (userProps.isOutOfExpiration()) {
			return OUT_OF_EXPIRATION;
		} else if (userProps.isTrading()) {
			return ITEM_CANNOT_USE;
		}

		PropsConfig propsConfig = userProps.getPropsConfig();
		if (propsConfig == null) {
			return BASEDATA_NOT_FOUND;
		} else if (propsConfig.getChildType() != PropsChildType.RESURRECT_ITEM) {
			return BELONGS_INVALID;
		}

		//玩家是否阵营战场里
		GameMap campBattleGameMap = campBattleFacade.getCampBattleGameMap(userDomain);
		if(campBattleGameMap != null){
			return IN_CAMP_BATTLE;
		}

		ChainLock lock = LockUtils.getLock(player, battle, userDomain.getPackLock());
		try {
			lock.lock();
			if (!battle.isDead()) {
				return PLAYER_NOT_DEATH;
			} else if (userProps.getPlayerId() != playerId) {
				return ITEM_NOT_ENOUGH;
			} else if (!userProps.validBackpack(DEFAULT_BACKPACK)) {
				return BACKPACK_INVALID;
			} else if (userProps.getCount() < PlayerRule.REVIVE_USE_PROPS_COUNT) {
				return ITEM_NOT_ENOUGH;
			} else if (userProps.isOutOfExpiration()) {
				return OUT_OF_EXPIRATION;
			}
			
		    int backHp = (int)(battle.getHpMax() * 0.3f);//只恢复30%的血量
			battle.setHp(backHp);
			player.setReviveProteTime(DateUtil.getCurrentSecond() + 3); //复活无敌保护时间
			userProps.decreaseItemCount(PlayerRule.REVIVE_USE_PROPS_COUNT);
			propsManager.removeUserPropsIfCountNotEnough(playerId, DEFAULT_BACKPACK, userProps);
		} finally {
			lock.unlock();
		}

		int baseId = userProps.getBaseId();
		cachedService.submitUpdate2Queue(battle, userProps);
		MessagePushHelper.pushUserProps2Client(playerId, DEFAULT_BACKPACK, false, userProps);
		Collection<Long> viewPlayers = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
		UserPushHelper.pushPlayerRecurrent(userDomain.getUnitId(), battle.getHp(), viewPlayers);
		GoodsLogger.goodsLogger(player, Source.PLAYER_RECURRENT, LoggerGoods.outcomeProps(propsId, baseId, PlayerRule.REVIVE_USE_PROPS_COUNT));
		return SUCCESS;
	}
	
	/**
	 * 等级未达到使用道具等级时的复活.
	 * 
	 * @param  userDomain		用户域模型对象
	 * @param  gameMap			地图模型对象
	 * @return {@link Integer}	返回值
	 */
	private int recurrentWithLevelLimit(UserDomain userDomain, GameMap gameMap) {
		//玩家是否阵营战场里
		GameMap campBattleGameMap = campBattleFacade.getCampBattleGameMap(userDomain);
		if(campBattleGameMap != null){
			return IN_CAMP_BATTLE;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		ChainLock lock = LockUtils.getLock(battle);
		try {
			lock.lock();
			if(!battle.isDead()) {
				return PLAYER_NOT_DEATH;
			}
			battle.setHp(battle.getHpMax());
		} finally {
			lock.unlock();
		}
		
		cachedService.submitUpdate2Queue(battle);
		Collection<Long> viewPlayers = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
		UserPushHelper.pushPlayerRecurrent(userDomain.getUnitId(), battle.getHp(), viewPlayers);
		return SUCCESS;
	}

	/**
	 * 领取教学信息奖励
	 * 
	 * @param  playerId					角色ID
	 * @param  rewardId					奖励ID
	 * @return {@link Integer}			用户模块返回值
	 */
	
	public int receiveGuideRewards(long playerId, int rewardId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		ReceiveInfo receiveInfo = EnumUtils.getEnum(ReceiveInfo.class, rewardId);
		if(receiveInfo == null) {
			return PARAM_INVALID;
		}
		
		Player player = userDomain.getPlayer();
		if(player.containsReceives(rewardId)) {
			return FAILURE;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(receiveInfo.isLevelLimit(battle.getLevel())) {
			return LEVEL_INVALID;
		}
		
		switch (receiveInfo) {
			case PET_PROPS 		 :    return receivePetProps(player, receiveInfo); 
			case RECEIVE_VIP     :	  return receiveVipProps(userDomain, receiveInfo);
			case GARNER_REWARD   :    return garnerRewards(player, receiveInfo);
			case EXPAND_BACKPACK :    return expandBackpacks(userDomain, receiveInfo); 
		}
		return FAILURE;
	}
	
	/**
	 * 领取家将道具, 直接开出家将, 加入家将背包中.
	 * 
	 * @param  player			角色对象
	 * @param  receiveInfo		领取信息
	 * @return
	 */
	private int receivePetProps(Player player, ReceiveInfo receiveInfo) {
		long playerId = player.getId();
		int propsId = receiveInfo.getPropsId();
		PropsConfig props = propsManager.getPropsConfig(propsId);
		if(props == null) {
			return ITEM_NOT_FOUND;
		}

		if(props.getChildType() != PropsChildType.PET_EGG_TYPE){
	    	return TYPE_INVALID;
	    }
		
		int dropNo = props.getAttrValueInt();						//家将组编号
		PetConfig petConfig = petService.initEggPet(dropNo); 		//获取家将ID;家将基础数据(随机出来的)
		if(petConfig == null){
			return BASEDATA_NOT_FOUND;
		}
		
		int jobValue = petConfig.getJob();							//家将职业
		int aptitudeNo = petConfig.getAptitudeNo();					//家将的资质编号
		int quality = petService.initPetQuality(aptitudeNo);		//家将品质(随机出来的)
		PetJob job = EnumUtils.getEnum(PetJob.class, jobValue);		//家将的职业 
		ChainLock lock = LockUtils.getLock(player);
		try {
			lock.lock();
			if(player.containsReceives(receiveInfo.ordinal())) {
				return FAILURE;
			}
			
			int petSlot = player.getMaxPetSlotSize();
			List<PetDomain> petDomains = petManager.getPetDomains(playerId);
			int currentSlot = petDomains == null ? 0 : petDomains.size();
			if(currentSlot >= petSlot){
				return PET_SLOT_ENOUGH;
			}
			
			Pet pet = PetRule.createPet(playerId, petConfig);			//创建家将实体
			PetBattle battle = PetRule.createPetBattle(job, quality);	//创建战斗实体
			petManager.createPetDomain(pet, battle);					//入库
			petFacade.removePetFamousCache(playerId); 					//重建名将录类表缓存
			player.addReceiveInfo(receiveInfo.ordinal(), true);
			cachedService.submitUpdate2Queue(player);
		} catch (Exception e) {
			LOGGER.error("玩家[{}], 领取家将卡并开蛋, 加入缓存,异常:{}", playerId, e);
			return FAILURE;
		} finally{
			lock.unlock();
		}
		return SUCCESS;
	}
	
	/**
	 * 领取VIP卡信息
	 * @param player
	 * @param receiveInfo
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private int receiveVipProps(UserDomain userDomain, ReceiveInfo receiveInfo) {
		PropsConfig props = propsManager.getPropsConfig(receiveInfo.getPropsId());
		if (props == null) {
			return ITEM_NOT_FOUND;
		}
		
		long playerId = userDomain.getId();
		PlayerVip playerVip = vipManager.getPlayerVip(playerId);
		if (playerVip == null) {
			return FAILURE;
		}
		
		VipConfig vipConfig = vipManager.getVipConfig(props.getAttrValueInt());
		if (vipConfig == null) {
			return BASEDATA_NOT_FOUND;
		}
		
		VipDomain vipDomain = VipDomain.valueOf(playerVip, vipConfig);
		
		Player player = userDomain.getPlayer();
		ChainLock lock = LockUtils.getLock(player, playerVip);
		try {
			lock.lock();
			if(player.containsReceives(receiveInfo.ordinal())) {
				return FAILURE;
			}
			
			player.addReceiveInfo(receiveInfo.ordinal(), true);
			long vipOutOfTime = vipDomain.longValue(VipOutOfDateTime);   // 获取VIP时长
			playerVip.alterEndTime(vipConfig.getLevel(), vipOutOfTime);
			playerVip.initVipParams();
		} finally {
			lock.unlock();
		}
		
		cachedService.submitUpdate2Queue(player, playerVip);
		vipManager.put2VipCache(playerId, vipDomain);
		
		List<Long> receiver = Arrays.asList(playerId);
		List<UnitId> playerUnits = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, receiver, playerUnits, AttributeKeys.GOLDEN);
		
		Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, playerUnits, AttributeKeys.VIP_INFO);
		return SUCCESS;
	}
	
	
	/**
	 * 扩展背包引导(超平增加)
	 * @param player
	 * @param receiveInfo
	 * @return
	 */
	private int expandBackpacks(UserDomain userDomain, ReceiveInfo receiveInfo) {
		Player player = userDomain.getPlayer();
		ChainLock lock = LockUtils.getLock(player);
		try {
			lock.lock();
			Set<Integer> receiveSet = player.getReceiveSet();
			if(receiveSet.contains(receiveInfo.ordinal())) {
				return FAILURE;
			}
			
			int maxBackSize = player.getMaxBackSize();
			if (maxBackSize >= PAGE_BACKPACK_SIZE * 2) {
				return SUCCESS;
			}
			
			receiveSet.add(receiveInfo.ordinal());
			player.updateReceiveInfo();
			player.setMaxBackSize(PAGE_BACKPACK_SIZE * 2);
		} finally {
			lock.unlock();
		}
		
		cachedService.submitUpdate2Queue(player);
		List<Long> playerIds = Arrays.asList(player.getId());
		List<UnitId> playerUnitIds = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(player.getId(), playerIds, playerUnitIds, AttributeKeys.BACKPACK_SIZE);
		return SUCCESS;
	}

	
	/**
	 * 收藏游戏奖励
	 * @param player
	 * @param receiveInfo
	 * @return
	 */
	private int garnerRewards(Player player, ReceiveInfo receiveInfo) {
		PropsConfig props = propsManager.getPropsConfig(receiveInfo.getPropsId());
		if(props == null) {
			return ITEM_NOT_FOUND;
		}
		
		int backpackType = BackpackType.DEFAULT_BACKPACK;
		int playerBackSize  = propsManager.getBackpackSize(player.getId(), DEFAULT_BACKPACK);
		UserProps userProps = UserProps.valueOf(player.getId(), DEFAULT_BACKPACK, 1, props, true);
		ChainLock lock = LockUtils.getLock(player, player.getPackLock());
		try {
			lock.lock();
			if (!player.canAddNew2Backpack(playerBackSize + 1, backpackType)) {
				return BACKPACK_FULLED;
			}
			if(player.containsReceives(receiveInfo.ordinal())) {
				return FAILURE;
			}
			player.addReceiveInfo(receiveInfo.ordinal(), true);
			propsManager.createUserProps(userProps);
			propsManager.put2UserPropsIdsList(player.getId(), DEFAULT_BACKPACK, userProps);
			cachedService.submitUpdate2Queue(player);
		} finally {
			lock.unlock();
		}
		
		MessagePushHelper.pushUserProps2Client(player.getId(), DEFAULT_BACKPACK, false, userProps);
		
		GoodsVO goodsVO = GoodsVO.valueOf(props.getId(), GoodsType.PROPS, 1);
		MessagePushHelper.pushGoodsCountChange2Client(player.getId(), goodsVO);
		GoodsLogger.goodsLogger(player, Source.GARNER_GIFT, LoggerGoods.incomeProps(receiveInfo.getPropsId(), 1));  // 道具日志
		return SUCCESS;
	}
	
	
	/**
	 * 保存玩家防沉迷信息
	 * 
	 * @param playerId                  角色ID
	 * @param state                     状态(是否成人)
	 * @return {@link CommonConstant}   返回值
	 */
	
	public int saveAdultMessage(long playerId, int state) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		AdultState adultSt = AdultState.getElementEnumById(state);
		if (adultSt == null) {
			return TYPE_INVALID;
		}
		
		Player player = userDomain.getPlayer();
		
		ChainLock lock = LockUtils.getLock(player);
		try {
			lock.lock();
			AdultState adult = player.getAdult() ;
			if (adult != AdultState.NONE) {              // 如果已验证, 返回失败
				return FAILURE;
			}
			player.setAdult(adultSt);
			cachedService.submitUpdate2Queue(player);
		} finally {
			lock.unlock();
		}
		return SUCCESS;
	}

	
	
	public int saveFashionShow(long playerId, boolean state) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return PLAYER_NOT_FOUND;
		}

		Player player = userDomain.getPlayer();
		if (player.isFashionShow() == state) {
			return INPUT_VALUE_INVALID;
		}

		ChainLock lock = LockUtils.getLock(player);
		try {
			lock.lock();
			if (player.isFashionShow() == state) {
				return INPUT_VALUE_INVALID;
			}
			player.setFashionShow(state);
		} finally {
			lock.unlock();
		}

		cachedService.submitUpdate2Queue(player);
		EquipPushHelper.pushDressAttributeChanges(userDomain, AttributeRule.CHANGE_FATION_VIEW);
		return SUCCESS;
	}

	/**
	 * 更新玩家的级别
	 * 
	 * @param  playerId					角色ID
	 * @param  type						级别类型						
	 * @return {@link Integer}			返回值
	 */
	
	public int updatePlayerCapacity(long playerId, int type) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		Capacity capacity = EnumUtils.getEnum(Capacity.class, type);
		if(capacity == null || capacity == Capacity.NONE) {
			return INPUT_VALUE_INVALID;
		}
		
		Player player = userDomain.getPlayer();
		if(player.getCapacity() != Capacity.NONE) {
			return FAILURE;
		}

		ChainLock lock = LockUtils.getLock(player);
		try {
			lock.lock();
			if(player.getCapacity() != Capacity.NONE) {
				return FAILURE;
			}
			player.setCapacity(capacity);
		} finally {
			lock.unlock();
		}
		cachedService.submitUpdate2Queue(player);
		return SUCCESS;
	}

	
	public DailyRecord getDailyRecord(long playerId) {
		
		return null;
	}

	
	public void onLoginEvent(UserDomain userDomain, int branching) {
	}
}
