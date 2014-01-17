package com.yayo.warriors.module.user.manager.impl;

import static com.yayo.warriors.module.pack.type.BackpackType.*;
import static com.yayo.warriors.module.props.type.EquipType.*;
import static com.yayo.warriors.module.user.type.AttributeKeys.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap.Builder;
import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.common.utility.DateUtil;
import com.yayo.common.utility.TimeConstant;
import com.yayo.warriors.basedb.model.EquipConfig;
import com.yayo.warriors.basedb.model.RevivePointConfig;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.module.alliance.entity.PlayerAlliance;
import com.yayo.warriors.module.alliance.manager.AllianceManager;
import com.yayo.warriors.module.buffer.entity.UserBuffer;
import com.yayo.warriors.module.buffer.facade.BufferFacade;
import com.yayo.warriors.module.campbattle.facade.CampBattleFacade;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.horse.facade.HorseFacade;
import com.yayo.warriors.module.logger.log.GoldLogger;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.map.domain.GameScreen;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.map.rule.MapRule;
import com.yayo.warriors.module.onhook.facade.TrainFacade;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.type.EquipType;
import com.yayo.warriors.module.rank.manager.RankManager;
import com.yayo.warriors.module.skill.entity.UserSkill;
import com.yayo.warriors.module.task.manager.EscortTaskManager;
import com.yayo.warriors.module.task.manager.TaskManager;
import com.yayo.warriors.module.user.dao.UserDao;
import com.yayo.warriors.module.user.entity.DailyRecord;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.entity.PlayerMotion;
import com.yayo.warriors.module.user.helper.UserHelper;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.InitCreateInfo;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.rule.AttributeRule;
import com.yayo.warriors.module.user.rule.PlayerRule;
import com.yayo.warriors.module.user.type.Job;
import com.yayo.warriors.module.user.type.PlayerStateKey;
import com.yayo.warriors.module.vip.manager.VipManager;
import com.yayo.warriors.module.vip.model.VipDomain;
import com.yayo.warriors.type.ElementType;

/**
 * 用户管理接口
 * 
 * @author Hyint
 */
@Service
public class UserManagerImpl extends CachedServiceAdpter implements UserManager {
	@Autowired
	private UserDao userDao;
	@Autowired
	private DbService dbService;
	@Autowired
	private VipManager vipManager;
	@Autowired
	private PetManager petManager;
	@Autowired
	private TaskManager taskManager;
	@Autowired
	private TrainFacade trainFacade;
	@Autowired
	private RankManager rankManager;
	@Autowired
	private HorseFacade horseFacade;
	@Autowired
	private BufferFacade bufferFacade;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private AllianceManager allianceManager;
	@Autowired
	private ResourceService resourceService; 
	@Autowired
	private EscortTaskManager escortTaskManager;
	@Autowired
	private CampBattleFacade battleFacade;
	
	@Autowired(required=false)
	@Qualifier("daily.login.rewards.golden")
	private Integer dailyLoginRewardGolden = 0;
	
	/** 日志 */
	private final Logger logger = LoggerFactory.getLogger(getClass());
	// LRU MAP Builder
	private static final Builder<Long, UserDomain> BUILDER = new ConcurrentLinkedHashMap.Builder<Long, UserDomain>();
	//用户域模型集合
	private static final ConcurrentLinkedHashMap<Long, UserDomain> USER_DOMAINS = BUILDER.maximumWeightedCapacity(30000).build();
	
	/**
	 * 角色登出保存数据接口
	 * 
	 * @param playerId	角色ID
	 */
	
	@SuppressWarnings("unchecked")
	public void onLogoutEvent(UserDomain userDomain) {
		Player player = userDomain.getPlayer();
		userDomain.getUserSkill().resetSkillVOMap();
		player = this.savePlayerLogoutState(player);
		player.removeAttribute(PlayerStateKey.BRANCHING);
		UserBuffer userBuffer = processUserBufferUpdate(userDomain.getUserBuffer());
		bufferFacade.removeBufferFromScheduler(userDomain.getUnitId());
		dbService.submitUpdate2Queue(player, userDomain.getMotion(), userDomain.getBattle(), userBuffer);
	}
	
	/**
	 * 数据清理事件接口
	 * 
	 * @param messageInfo	消息信息
	 */
	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		USER_DOMAINS.remove(messageInfo.getPlayerId());
	}



	/**
	 * 处理用户Buffer更新
	 * 
	 * @param  userBuffer			用户Buffer对象
	 * @return {@link UserBuffer}	用户Buffer对象
	 */
	private UserBuffer processUserBufferUpdate(UserBuffer userBuffer) {
		if(userBuffer.isAllBufferEmpty()) {
			return userBuffer;
		}
		
		ChainLock lock = LockUtils.getLock(userBuffer);
		try {
			lock.lock();
			
			if(!userBuffer.isBufferEmpty()) {
				userBuffer.updateBufferInfos(true);
			}

			if(!userBuffer.isDeBufferEmpty()) {
				userBuffer.updateDeBufferInfos(true);
			}
			if(!userBuffer.isItemBufferEmpty()) {
				userBuffer.updateItemBufferInfos(true);
			}
		} finally {
			lock.unlock();
		}
		return userBuffer;
	}
	
	
	/**
	 * 保存角色的登陆状态
	 * 
	 * @param   player		角色对象
	 */
	private Player savePlayerLogoutState(Player player) {
		Date nowDate = new Date();
		ChainLock lock = LockUtils.getLock(player);
		try {
			lock.lock();
			Date loginTime = player.getLoginTime();				//本次登录的时间
			boolean isToday = DateUtil.isToday(loginTime);		//登录是否今天
			if(isToday) { //登陆时间是今天, 时间直接累加
				long betweenOnlineTime = nowDate.getTime() - loginTime.getTime();
				player.addOnlineTimes(betweenOnlineTime /  TimeConstant.ONE_SECOND_MILLISECOND);
			} else { //登陆时间不是今天, 则从今天0点开始计算
				long zeroTimeMillis = DateUtil.getDate0AM(nowDate).getTime();
				player.setOnlineTimes(Math.max(0, (nowDate.getTime() - zeroTimeMillis) / TimeConstant.ONE_SECOND_MILLISECOND));
			}
			
			if(!isToday) { 				//登入的时间距离今天的自然天数, 超过一天则记录
				int calc2DateTDOADays = DateUtil.calc2DateTDOADays(nowDate, loginTime);
				player.addLoginDays(calc2DateTDOADays);		//登录到现在, 相隔多少天, 设置到登录天数
				player.addContinueDays(calc2DateTDOADays);	//登录到现在, 相隔多少天, 设置到连续登录天数
				player.updateContinueMaxDays(player.getContinueDays());
			}
			
			//保存新手步骤
			player.saveGuides();
			player.setLogoutTime(new Date());
		} finally {
			lock.unlock();
		}
		return player;
	}
	 
	/**
	 * 验证角色是否在线
	 * 
	 * @param  playerId					角色ID
	 * @return {@link Boolean}			true-角色在线, false-角色不在线
	 */
	
	public boolean isOnline(long playerId) {
		return sessionManager.isOnline(playerId);
	}
	
	/**
	 * 保存角色的登陆状态
	 * 
	 * @param   player		角色信息
	 */
	
	public void savePlayerLoginState(Player player) {
		Date nowDate = new Date();
		boolean pushGolden2Client = false;
		ChainLock lock = LockUtils.getLock(player);
		try {
			lock.lock();
			Date loginTime = player.getLoginTime();					//上次的登陆时间
			Date logoutTime = player.getLogoutTime();				//上次的登出时间
			boolean isTodayLogin = DateUtil.isToday(loginTime);		//是否今天登陆过.
			if(isTodayLogin) { //上次登陆的时间是今天, 则计算一些今天的规则
				if(player.getContinueDays() <= 0) {	//从来没有连续登陆过, 则需要设置为连续登陆了一天
					player.setContinueDays(1);
					player.updateContinueMaxDays(player.getContinueDays());
				}
				if(player.getLoginDays() <= 0) {	//从来没有登陆过, 则需要设置为连续登陆了一天
					player.setLoginDays(1);
				}
			} else { //上次登陆的时间不是今天, 则计算一些今天的规则
				player.setOnlineTimes(0L);			//上次登录时间不是今天, 则直接清空当天在线时间
				if(pushGolden2Client = dailyLoginRewardGolden > 0) {
					player.increaseGolden(dailyLoginRewardGolden);
				}
				
				boolean isTodayLogout = DateUtil.isToday(logoutTime);	//是否今天登出的
				if(!isTodayLogout) { 									//上次登出时间都不是今天, 重置登录天数
					Date now0AM = DateUtil.getDate0AM(nowDate);						//今天的0点
					Date off0AM = DateUtil.getDate0AM(logoutTime);					//登出的0点
					int betweenDays = DateUtil.calc2DateTDOADays(now0AM, off0AM);	//相隔的天数
//					player.addLoginDays(betweenDays);                               //这里修改下总登录天数　---- 超平
					player.addLoginDays(1);
					if(betweenDays > 1) {
						player.setContinueDays(1);
					} else {
						player.addContinueDays(1);
						player.updateContinueMaxDays(player.getContinueDays());
					}
				}
			}
			
			player.addLoginCount(1);				// 增加登录次数
			player.setLoginTime(nowDate);			// 设置当前登陆
		} finally {
			lock.unlock();
		}
		
		if(pushGolden2Client) {
			long playerId = player.getId();
			dbService.updateEntityIntime(player);
			List<Long> playerIds = Arrays.asList(playerId);
			List<UnitId> unitIdList = Arrays.asList(UnitId.valueOf(playerId, ElementType.PLAYER));
			UserPushHelper.pushAttribute2AreaMember(playerId, playerIds, unitIdList, GOLDEN);
			GoldLogger.inCome(Source.PLAYER_DAILYLOGIN_REWARD_GOLDEN, dailyLoginRewardGolden, player);
		} else {
			dbService.submitUpdate2Queue(player);
		}
	}
	
	/**
	 * 查询用户的域模型对象
	 * 
	 * @param  playerId				角色ID
	 * @return {@link UserDomain}	用户域模型对象
	 */
	
	public UserDomain getUserDomain(long playerId) {
		if(playerId <= 0L) {
			return null;
		}
		
		UserDomain userDomain = USER_DOMAINS.get(playerId);
		if(userDomain != null) {
			return userDomain;
		}
		
		Player player = userDao.get(playerId, Player.class);
		if(player == null) {
			return null;
		}
		
		PlayerBattle battle = userDao.get(playerId, PlayerBattle.class);
		if(battle == null) {
			return null;
		}

		PlayerMotion motion = userDao.get(playerId, PlayerMotion.class);
		if(motion == null) {
			synchronized (this) {
				motion = userDao.get(playerId, PlayerMotion.class);
				if(motion == null) {
					motion = new PlayerMotion();
					motion.setId(playerId);
					if(battle.getLevel() > 10){
						motion.setMapId(MapRule.DEFAUL_REVIVE_MAPID);
						motion.setX(MapRule.DEFAUL_REVIVE_X);
						motion.setY(MapRule.DEFAUL_REVIVE_Y);
						
					} else if(battle.getLevel() < 10){
						motion.setMapId(PlayerRule.INIT_MAP_ID);
						motion.setX(PlayerRule.INIT_POSITION_X);
						motion.setY(PlayerRule.INIT_POSITION_Y);
					}
					userDao.save(motion);
				}
			}
		}

		UserSkill userSkill = this.getUserSkill(battle);
		if(userSkill == null) {
			return null;
		}
		
		UserBuffer userBuffer = this.getUserBuffer(playerId);
		if(userBuffer == null) {
			return null;
		}
		
		USER_DOMAINS.putIfAbsent(playerId, UserDomain.valueOf(player, battle, motion, userBuffer, userSkill));
		return USER_DOMAINS.get(playerId);
	}

	/**
	 * 查询用户技能对象
	 * 
	 * @param  playerId				角色ID
	 * @return {@link UserSkill}	用户技能对象
	 */
	private UserSkill getUserSkill(PlayerBattle battle) {
		if(battle == null) {
			return null;
		}
		
		
		long playerId = battle.getId();
		Job playerJob = battle.getJob();
		UserSkill userSkill = commonDao.get(playerId, UserSkill.class);
		if(userSkill == null) {
			try {
				commonDao.save(UserSkill.valueOf(playerId));
				userSkill = commonDao.get(playerId, UserSkill.class);
			} catch (Exception e) {
				return null;
			}
		}
		
		if(PlayerRule.refreshInnateSkillInfo(playerJob, userSkill)) {
			dbService.submitUpdate2Queue(userSkill);
		}
		return userSkill;
	}
	
	/**
	 * 用户BUFF对象
	 * 
	 * @param  playerId				角色ID
	 * @return {@link UserBuffer}	用户BUFF对象
	 */
	private UserBuffer getUserBuffer(long playerId) {
		UserBuffer userBuffer = commonDao.get(playerId, UserBuffer.class);
		if(userBuffer == null) {
			try {
				commonDao.save(UserBuffer.valueOf(playerId));
				userBuffer = commonDao.get(playerId, UserBuffer.class);
			} catch (Exception e) {
				return null;
			}
		}
		return userBuffer;
	}
	
	/**
	 * 创建角色对象. 
	 * 
	 * @param initCreateInfo			角色创建信息
	 */
	
	public InitCreateInfo createPlayer(InitCreateInfo initCreateInfo) {
		userDao.createPlayerInfo(initCreateInfo);
		this.put2EntityCache(initCreateInfo.getMeridian());
		this.put2EntityCache(initCreateInfo.getCoolTime());
		this.put2EntityCache(initCreateInfo.getPlayerVip());
		this.put2EntityCache(initCreateInfo.getPlayerTitle());
		this.put2EntityCache(initCreateInfo.getTaskComplete());
		this.put2EntityCache(initCreateInfo.getPlayerDungeon());
		this.put2EntityCache(initCreateInfo.getUserMortalBody());
		
		propsManager.createPlayerPack(initCreateInfo.getPlayer());
		taskManager.initilaizeCreateTaskIdList(initCreateInfo.getPlayerId());
		USER_DOMAINS.putIfAbsent(initCreateInfo.getPlayerId(), UserDomain.valueOf(initCreateInfo));
		return initCreateInfo;
	}




	/** 模块号 */
	private static final String PREFIX = "MODEL_PLAYER_";
	/** 用户名 */
	private static final String USER_NAME = "_USERNAME_";
	/** 角色名 */
	private static final String PLAYER_NAME = "_PLAYERNAME_";
	
	/**
	 * 获得角色名SubKey
	 * 
	 * @param  playerName		角色名
	 * @return {@link String}	SubKey
	 */
	private String getPlayerNameKey(String playerName) {
		return new StringBuffer().append(PLAYER_NAME).append(playerName).toString();
	}

	/**
	 * 获得帐号名SubKey
	 * 
	 * @param  userName			用户名
	 * @return {@link String}	SubKey
	 */
	private String getUserNameKey(String userName) {
		return new StringBuffer().append(USER_NAME).append(StringUtils.defaultIfBlank(userName, "")).toString();
	}
	
	
	public long getPlayerId(String playerName) {
		String subKey = getPlayerNameKey(playerName);
		Long playerId = (Long)cachedService.getFromCommonCache(PREFIX, subKey);
		if(playerId == null) {
			playerId = userDao.getPlayerId(playerName);
			cachedService.put2CommonHashCache(PREFIX, subKey, playerId);
		}
		return playerId;
	}

	/**
	 * 根据角色名查询角色对象
	 * 
	 * @param  playerName			角色名
	 * @return {@link Player}		角色对象
	 */
	
	public Player getPlayer(String playerName) {
		long playerId = this.getPlayerId(playerName);
		UserDomain userDomain = this.getUserDomain(playerId);
		return userDomain == null ? null : userDomain.getPlayer();
	}

	/**
	 * 根据用户ID查询角色角色ID列表
	 * 
	 * @param  accountId 				用户帐号ID
	 * @return {@link List} 			角色ID列表
	 */
	
	@SuppressWarnings("unchecked")
	public List<Long> listPlayerIdByUserName(String userName) {
		String subKey = this.getUserNameKey(userName);
		List<Long> playerIds = (List<Long>) cachedService.getFromCommonCache(PREFIX, subKey);
		if(playerIds == null) {
			playerIds = new CopyOnWriteArrayList<Long>(userDao.listPlayerIdByUserName(userName, false));
			cachedService.put2CommonHashCache(PREFIX, subKey, playerIds);
		}
		return playerIds;
	}

	/**
	 * 移除用户名通用缓存
	 * 
	 * @param userName					用户名
	 */
	
	public void addPlayerId2UserNameCache(String userName, long playerId) {
		List<Long> playerIdList = this.listPlayerIdByUserName(userName);
		if(playerIdList != null && !playerIdList.contains(playerId)) {
			playerIdList.add(playerId);
		}
	}

	/**
	 * 移除角色名通用缓存
	 * 
	 * @param playerName				角色名
	 */
	
	public void addPlayerId2PlayerNameCache(String playerName, long playerId) {
		cachedService.put2CommonHashCache(PREFIX, getPlayerNameKey(playerName), playerId);
	}
	
	/**
	 * 更新角色信息. 对于一些依赖数据库事务的操作
	 * 
	 * @param player					角色信息
	 */
	
	public void updatePlayer(Player player) {
		userDao.update(player);
	}

	/**
	 * 增加角色的经验(该接口不记录日志. 调用方自己记录日志)
	 * 
	 * @param  playerId					角色ID
	 * @param  addExp					增加的经验值
	 * @param  update					是否需要更新入库
	 * @return {@link Boolean}			是否增加经验成功
	 */
	
	public boolean addPlayerExp(long playerId, long addExp, boolean update) {
		UserDomain userDomain = this.getUserDomain(playerId);
		if(userDomain == null) {
			return false;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle == null) {
			return false;
		}

		GameScreen currentScreen = userDomain.getCurrentScreen();
		if(currentScreen == null) {
			return false;
		}
		
		ChainLock lock = LockUtils.getLock(battle);
		try {
			lock.lock();
			battle.increaseExp(addExp);
		} finally {
			lock.unlock();
		}
		
		if(update) { 
			dbService.submitUpdate2Queue(battle);
		}
		
		Collection<UnitId> unitIds = Arrays.asList(userDomain.getUnitId());
		Collection<Long> playerIds = currentScreen.getSpireIdCollection(ElementType.PLAYER);
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIds, unitIds, AttributeRule.PLAYER_EXP);
		return true;
	}

	/**
	 * 查询角色的属性值
	 * 
	 * @param  playerId					角色ID
	 * @param  params					参数对象
	 * @return Object[]					返回的属性参数
	 */
	
	public Object[] getPlayerAttributes(ISpire spire, Object... params) {
		Object[] values = new Object[params.length];
		UserDomain userDomain = (UserDomain)spire;
		if(userDomain != null) {
			Player player = userDomain.getPlayer();
			long playerId = userDomain.getPlayerId();
			PlayerBattle battle = userDomain.getBattle();
			PlayerMotion motion = userDomain.getMotion();
			VipDomain vipDomain = vipManager.getVip(playerId);         // VIP域 --超平
			Map<Integer, Integer> models = this.getPlayerModels(userDomain, battle.getJob(), params);
			PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
			long allianceId = playerAlliance == null ? 0 : playerAlliance.getAllianceId();//帮派ID
			String allianceName =  playerAlliance == null ? "" : playerAlliance.getAllianceName(); //帮派名称
			long dayOnlineTime = DateUtil.isToday(player.getLoginTime()) ? player.getOnlineTimes() : 0L; // 玩家上线获取在线时间
			for (int index = 0; index < params.length; index++) {
				Integer attribute = (Integer) params[index];
				if(attribute == null) {
					values[index] = null;
					continue;
				}
				
				switch (attribute) {
					case X:					values[index] = motion.getX();								break;
					case Y:					values[index] = motion.getY();								break;
					case EXP:				values[index] = battle.getExp();							break;
					case RIDE:				values[index] = this.isRide(battle);             			break; //TODO
					case ICON:				values[index] = player.getIcon();							break;
					case CAMP:				values[index] = player.getCamp();							break;
					case NAME:				values[index] = player.getName();							break;
					case TITLE:				values[index] = player.getTitle();							break;
					case SILVER:			values[index] = player.getSilver();							break;
					case GOLDEN:			values[index] = player.getGolden();							break;
					case COUPON:			values[index] = player.getCoupon();							break;
					case SEX:				values[index] = player.getSex().ordinal();					break;
					case JOB:				values[index] = battle.getJob().ordinal();					break;
					case TEAM_ID:			values[index] = player.getTeamId();							break;
					case BASE_ID:			values[index] = player.getServerId();						break;
					case CAPACITY:			values[index] = player.getCapacity().ordinal();				break;
					case SERVER_ID:			values[index] = player.getServerId();						break;
					case LOGIN_TIME:		values[index] = player.getLoginTime();						break;
					case LOGIN_DAYS:		values[index] = player.getContinueMaxDays();				break; // 连续登录天数   ---- 2012.8.11超平
					case FIGHT_MODE:		values[index] = battle.getMode().ordinal();					break;
					case WEAPON_FOOT:		values[index] = models.get(WEAPON_TYPE);					break;
					case WEAPON_RIDE:		values[index] = models.get(WEAPON_TYPE);					break;
					case CLOTHING:			values[index] = models.get(CLOTHES_TYPE); 					break;
					case CREATE_TIME:		values[index] = player.getCreateTime();						break;
					case FORBID_CHAT:		values[index] = player.getForbidChat();						break;
					case LOGOUT_TIME:		values[index] = player.getLogoutTime();						break;
					case LOGIN_COUNT:		values[index] = player.getLoginCount();						break;
					case FORBID_LOGIN:		values[index] = player.getForbidLogin();					break;
					case ONLINE_TIMES:		values[index] = dayOnlineTime;								break;
					case BACKPACK_SIZE:		values[index] = player.getMaxBackSize();					break;
					case STORAGE_SIZE:		values[index] = player.getMaxStoreSize();					break;
					case TRAIN_STATUS:      values[index] = trainFacade.isTrainStatus(playerId);        break;
					case ALLIANCE_ID:		values[index] = allianceId;                 	            break;
					case ALLIANCE_NAME:		values[index] = allianceName;                               break;
					case RANK_TITLE:		values[index] = rankManager.getRankTitleByPlayerId(playerId);break;
					case CAMP_TITLE:		values[index] = battleFacade.getCampBattleTitle(playerId, null).ordinal();	break;
					case GUIDE_INFO:		values[index] = player.getGuides().toArray();				break;
					case VIP_INFO:          values[index] = vipDomain.vipLevel();                       break;
					case INDULGE_STATE:     values[index] = player.getAdult().getCode();                break;
					case EQUIP_BLINK:       values[index] = player.getBlinkType().ordinal();            break;
					case USER_PET_MERGED :  values[index] = petManager.getUserPetMerged(battle);        break;
					case PET_SLOT_SIZE   :  values[index] = player.getMaxPetSlotSize();                 break;
					case FASHION_EQUIP_VIEW:values[index] = player.isFashionShow();                     break;
					case TOTAL_LOGIN_DAYS:  values[index] = player.getLoginDays();                      break; // 总登录天数 ---- 2012.8.11超平

					
					case HP:				values[index] = battle.isDead() ? 0 : battle.getAttribute(attribute);	break;
					case MP:				values[index] = battle.getAttribute(attribute);				break;
					case GAS:				values[index] = battle.getAttribute(attribute);				break;
					case HIT:				values[index] = battle.getAttribute(attribute);				break;
					case LEVEL:				values[index] = battle.getAttribute(attribute);				break;
					case DODGE:				values[index] = battle.getAttribute(attribute);				break;
					case BLOCK:				values[index] = battle.getAttribute(attribute);				break;	
					case HP_MAX:			values[index] = battle.getAttribute(attribute);				break;
					case MP_MAX:			values[index] = battle.getAttribute(attribute);				break;
					case HP_BAG:			values[index] = battle.getHpBag();							break;
					case MP_BAG:			values[index] = battle.getMpBag();							break;
					case EXP_MAX:			values[index] = battle.getExpMax();							break;
					case PIERCE:			values[index] = battle.getAttribute(attribute);				break;
					case GAS_MAX:			values[index] = battle.getAttribute(attribute);				break;
					case RAPIDLY:			values[index] = battle.getAttribute(attribute);				break;
					case STRENGTH:			values[index] = battle.getAttribute(attribute);				break;
					case DEXERITY:			values[index] = battle.getAttribute(attribute);				break;
					case DUCTILITY:			values[index] = battle.getAttribute(attribute);				break;
					case INTELLECT:			values[index] = battle.getAttribute(attribute);				break;
					case PET_HP_BAG:		values[index] = battle.getPetHpBag();						break;
					case MOVE_SPEED:		values[index] = this.getSpeed(battle);       				break;
					case CONSTITUTION:		values[index] = battle.getAttribute(attribute);				break;
					case SPIRITUALITY:		values[index] = battle.getAttribute(attribute);				break;
					case THEURGY_ATTACK:	values[index] = battle.getAttribute(attribute);				break;
					case THEURGY_DEFENSE:	values[index] = battle.getAttribute(attribute);				break;
					case THEURGY_CRITICAL:	values[index] = battle.getAttribute(attribute);				break;
					case PHYSICAL_ATTACK:	values[index] = battle.getAttribute(attribute);				break;
					case PHYSICAL_DEFENSE:	values[index] = battle.getAttribute(attribute);				break;
					case PHYSICAL_CRITICAL:	values[index] = battle.getAttribute(attribute);				break;
					case PLAYER_RECEIVE_INFO:values[index] = player.getReceiveSet().toArray();			break;
					
					
					//----------------------------------------> 战斗力查询 <----------------------------------------
					case EQUIP_BASE_CAPACITY:		values[index] = battle.getAttribute(attribute);		break;
					case EQUIP_SUIT_CAPACITY:		values[index] = battle.getAttribute(attribute);		break;
					case EQUIP_STAR_CAPACITY:		values[index] = battle.getAttribute(attribute);		break;
					case FIGHT_TOTAL_CAPACITY:		values[index] = battle.getAttribute(attribute);		break;
					case EQUIP_TOTAL_CAPACITY:		values[index] = battle.getAttribute(attribute);		break;
					case FIGHT_HORSE_CAPACITY:		values[index] = battle.getAttribute(attribute);		break;
					case PLAYER_FIGHT_CAPACITY:		values[index] = battle.getAttribute(attribute);		break;
					case PLAYER_BASIC_CAPACITY:		values[index] = battle.getAttribute(attribute);		break;
					case PLAYER_MORTAL_CAPACITY:	values[index] = battle.getAttribute(attribute);		break;
					case EQUIP_ADDITION_CAPACITY:	values[index] = battle.getAttribute(attribute);		break;
					case DUNGEON_REWARD_CAPACITY:	values[index] = battle.getAttribute(attribute);		break;
					case EQUIP_ENCHANGE_CAPACITY:	values[index] = battle.getAttribute(attribute);		break;
					case PLAYER_MERIDIAN_CAPACITY:	values[index] = battle.getAttribute(attribute);		break;
					case FIGHT_HORSE_STAR_CAPACITY:	values[index] = battle.getAttribute(attribute);		break;
					case MOUNT:                     values[index] = this.getMount(battle);              break;
					default: 					values[index] = 0;										break;//TODO
				}	
			}
		}
		return values;
	}
	
	/**
	 * 获取移动速度
	 * @param battle
	 * @return {@link Integer} 移动速度
	 */
	private int getSpeed(PlayerBattle battle){
		if(battle == null){
			return 0;
		}
		return battle.getAttribute(MOVE_SPEED) + escortTaskManager.getMoveSpeed(battle);
	}
	
	/**
	 * 获取坐骑或押镖的外观
	 * <pre>坐骑和押镖状态都需要骑乘状态<pre>
	 * @param battle   玩家战斗对象 
	 * @return {@link Integer}  外观的ID
	 */
	private int getMount(PlayerBattle battle){
		int mount = 0;
		if(battle == null){
			return mount;
		}
		long playerId = battle.getId();
		mount = this.escortTaskManager.getEscortMount(battle);
		if(mount > 0){//直接以押镖状态下的外观为坐骑显示的最优先级别
			return mount;
		}
		mount = horseFacade.getHorseMount(playerId);
		return mount;
	}
	
	
	/**
	 * 是否骑乘状态
	 * <pre>坐骑和押镖状态都需要骑乘状态<pre>
	 * @param battle   玩家战斗对象 
	 * @return {@link Boolean} true 有骑乘 false 没有骑乘
	 */
	private boolean isRide(PlayerBattle battle){
		boolean isRide = false;
		if(battle == null){
			return isRide;
		}
		long playerId = battle.getId();
		isRide = escortTaskManager.isRide(battle);
		if(isRide) {//直接以押镖状态下的外观为坐骑显示的最优先级别
			return isRide;
		}
		isRide = horseFacade.isRide(playerId);
		return isRide;
	}
	
	/**
	 * 查询角色的模型对象集合
	 * 
	 * @param  playerId		角色ID
	 * @param  params		角色查询的数据对象
	 * @return {@link Map}	{装备类型, 模型ID}
	 */
	@SuppressWarnings("unchecked")
	private Map<Integer, Integer> getPlayerModels(UserDomain userDomain, Job job, Object... params) {
		if(params.length <= 0) {
			return null;
		}
		
		List<Object> paramList = Arrays.asList(params);
		List<Object> modelList = Arrays.asList(AttributeRule.PLAYER_MODEL_PARAMS);
		Collection<Object> retainAll = CollectionUtils.retainAll(paramList, modelList);
		if(retainAll == null || retainAll.isEmpty()) { //没有需要查询的信息
			return null;
		}
		
		long playerId = userDomain.getPlayerId();
		Map<Integer, Integer> attributeMap = new HashMap<Integer, Integer>(retainAll.size());
		attributeMap.put(EquipType.CLOTHES_TYPE, job.getClosing());
		
		List<UserEquip> userEquips = propsManager.listUserEquip(playerId, DRESSED_BACKPACK);
		if(userEquips == null || userEquips.isEmpty()) {
			return attributeMap;
		}
		
		UserEquip fationEquip = null;
		for (UserEquip userEquip : userEquips) {
			EquipConfig equipConfig = userEquip.getEquipConfig();
			if(equipConfig == null) {
				continue;
			}
			
			if(equipConfig.isFaction()) {
				fationEquip = userEquip;
				continue;
			}
			
			if(userEquip.isOutOfExpiration()) { //时装超时
				continue;
			} 

			if(equipConfig.getModelId() > 0) {
				attributeMap.put(equipConfig.getPropsType(), equipConfig.getModelId());
			}
		}
		
		if(fationEquip != null) {
			if(fationEquip.isOutOfExpiration()) {
				UserHelper.processFationOutExpiration(userDomain, true, true);
			} else if(userDomain.getPlayer().isFashionShow()) {
				attributeMap.put(EquipType.CLOTHES_TYPE, fationEquip.getEquipConfig().getModelId());
			}
		}
		
		return attributeMap;
	}

	
	public List<Object[]> findPlayers(int paramType, String paramValue, int roleType, int levelBegin, int levelEnd, int pageSize, int currentPage) {
		return userDao.findPlayers(paramType, paramValue, roleType, levelBegin, levelEnd, pageSize, currentPage);
	}

	
	public int findPlayersCount(int paramType, String paramValue, int levelBegin, int levelEnd) {
		return userDao.findPlayersCount(paramType, paramValue, levelBegin, levelEnd);
	}

	/**
	 * 更新角色的刷新状态
	 * 
	 * @param  playerId					角色ID
	 * @param  flushable				角色的刷新状态
	 */
	
	public void updateFlushable(long playerId, int flushable) {
		UserDomain userDomain = this.getUserDomain(playerId);
		if(userDomain == null) {
			return;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		ChainLock lock = LockUtils.getLock(battle);
		try {
			lock.lock();
			battle.setFlushable(flushable);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * 查询角色的属性值
	 * 
	 * @param  playerId					角色ID
	 * @param  params					参数对象
	 * @return Object[]					返回的属性参数
	 */
	
	public Object[] getPlayerAttributes(long playerId, Object... params) {
		UserDomain userDomain = this.getUserDomain(playerId);
		return this.getPlayerAttributes(userDomain, params);
	}

	
	public RevivePointConfig getRevivePointConfig(int mapId) {
		return resourceService.get(mapId, RevivePointConfig.class);
	}

	
	public DailyRecord getDailyRecord(long playerId) {
		if(playerId > 0){
			return this.get(playerId, DailyRecord.class);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		T entity = super.getEntityFromDB(id, clazz);
		if(id != null && clazz == DailyRecord.class) {
			DailyRecord dailyRecord = commonDao.get((Long)id, DailyRecord.class);
			if(dailyRecord == null){
				try {
					dailyRecord = DailyRecord.valueOf((Long)id);
					commonDao.save(dailyRecord);
				} catch (Exception e) {
					dailyRecord = null;
					logger.error("角色:[{}] 创建dailyRecord异常:{}", id, e);
					logger.error("{}", e);
				}
			}
			return (T) dailyRecord;
		}
		
		return entity;
	}
	
	
}
