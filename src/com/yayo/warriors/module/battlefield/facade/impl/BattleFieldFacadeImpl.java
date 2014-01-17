package com.yayo.warriors.module.battlefield.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.pack.type.BackpackType.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.scheduling.Scheduled;
import com.yayo.common.scheduling.ScheduledTask;
import com.yayo.common.scheduling.Scheduler;
import com.yayo.common.scheduling.ValueType;
import com.yayo.common.scheduling.impl.CronSequenceGenerator;
import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.common.socket.message.Response;
import com.yayo.common.utility.DateUtil;
import com.yayo.common.utility.TimeConstant;
import com.yayo.warriors.basedb.adapter.BattleFieldService;
import com.yayo.warriors.basedb.model.BattleCollectConfig;
import com.yayo.warriors.basedb.model.BattlePointConfig;
import com.yayo.warriors.basedb.model.BattleRewardsConfig;
import com.yayo.warriors.basedb.model.MonsterConfig;
import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.FormulaHelper;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.battlefield.constant.BattleFieldConstant;
import com.yayo.warriors.module.battlefield.entity.PlayerBattleField;
import com.yayo.warriors.module.battlefield.facade.BattleFieldFacade;
import com.yayo.warriors.module.battlefield.manager.BattleFieldManager;
import com.yayo.warriors.module.battlefield.model.BattleRoom;
import com.yayo.warriors.module.battlefield.rule.BattleFieldRule;
import com.yayo.warriors.module.battlefield.vo.BattleFieldVO;
import com.yayo.warriors.module.battlefield.vo.CollectTaskVO;
import com.yayo.warriors.module.campbattle.constant.CampBattleConstant;
import com.yayo.warriors.module.campbattle.facade.CampBattleFacade;
import com.yayo.warriors.module.campbattle.rule.CampBattleRule;
import com.yayo.warriors.module.chat.facade.ChannelFacade;
import com.yayo.warriors.module.chat.model.Channel;
import com.yayo.warriors.module.chat.type.ChatChannel;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.dungeon.facade.DungeonFacade;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.logger.log.BattleFieldLogger;
import com.yayo.warriors.module.logger.log.ExpLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.map.manager.GameMapManager;
import com.yayo.warriors.module.map.rule.MapRule;
import com.yayo.warriors.module.map.util.MapUtils;
import com.yayo.warriors.module.monster.manager.MonsterManager;
import com.yayo.warriors.module.npc.facade.NpcFacade;
import com.yayo.warriors.module.npc.model.Npc;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.pet.facade.PetFacade;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.pet.types.PetStatus;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.module.props.model.PropsStackResult;
import com.yayo.warriors.module.props.rule.PropsHelper;
import com.yayo.warriors.module.server.listener.DataRemoveListener;
import com.yayo.warriors.module.server.listener.LogoutListener;
import com.yayo.warriors.module.task.entity.UserEscortTask;
import com.yayo.warriors.module.task.manager.EscortTaskManager;
import com.yayo.warriors.module.task.type.TaskStatus;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.entity.PlayerMotion;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.battlefield.BattleFieldCmd;
import com.yayo.warriors.socket.handler.pet.PetCmd;
import com.yayo.warriors.socket.vo.ChangeScreenVo;
import com.yayo.warriors.type.ElementType;
import com.yayo.warriors.type.IndexName;

/**
 * 乱武战场业务实现方法
 * @author jonsai
 *
 */
@Service
public class BattleFieldFacadeImpl implements BattleFieldFacade, LogoutListener, DataRemoveListener, ApplicationListener<ContextRefreshedEvent>{
	@Autowired
	private UserManager userManager;
	@Autowired
	private GameMapManager gameMapManager;
	@Autowired
	private MapFacade mapFacade;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private MonsterManager monsterManager;
	@Autowired
	private BattleFieldManager battleFieldManager;
	@Autowired
	private DbService dbService;
	@Autowired
	private BattleFieldService battleFieldService;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private Scheduler scheduler;
	@Autowired
	private PetManager petManager;
	@Autowired
	private PetFacade petFacade;
	@Autowired
	private ChannelFacade channelFacade;
	@Autowired
	private NpcFacade npcFacade;
	@Autowired
	private CampBattleFacade campBattleFacade;
	@Autowired
	private DungeonFacade dungeonFacade;
	@Autowired
	private EscortTaskManager escortTaskManager;
	@Autowired
	private SessionManager sessionManager;
	
	@Autowired
	@Qualifier("BATTLE_FIELD_START_TIME")
	private String BATTLE_FIELD_START_TIME;		//战场开始时间
	
	@Autowired
	@Qualifier("BATTLE_FIELD_TIMEOUT")
	private Integer BATTLE_FIELD_TIMEOUT = 30;	//战场持续的分钟数
	
	/** 日志 */
	private final Logger logger = LoggerFactory.getLogger(getClass());
	/** 战场状态  0-初始(或结束)状态，1-阵营战开始，2-计算战场结果 */
	private AtomicInteger battleStatus = new AtomicInteger();
	/** 房间锁 */
	private final ReentrantLock roomLock = new ReentrantLock();
	/** 战场房间 */
	private final CopyOnWriteArrayList<BattleRoom> rooms = new CopyOnWriteArrayList<BattleRoom>();
	/** 角色战场房间 */
	private final ConcurrentMap<Long, BattleRoom> playerRoomMap = new ConcurrentHashMap<Long, BattleRoom>();
	/** 当时战场时间 */
	private Date battleDate = null;
	/** 本次战场结束时间 */
	private Date battleStopDate = null;
	/** 战场开启时间cron解析器 */
	private CronSequenceGenerator startTimeGenerator;
	
	
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if(startTimeGenerator == null){
			startTimeGenerator = new CronSequenceGenerator(BATTLE_FIELD_START_TIME, TimeZone.getDefault() );
		}
		
		if(battleStatus.get() != CampBattleRule.STATUS_INIT){
			return ;
		}
		
		Date now = new Date();
		Date startTimeDate = startTimeGenerator.next( DateUtil.changeDateTime(now, 0, 0, 0, 0) );	//启服后第一个战场开始时间
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startTimeDate);
		calendar.add(Calendar.MINUTE, BATTLE_FIELD_TIMEOUT);
		
		if( now.after(startTimeDate) && now.before(calendar.getTime()) ){	//调度开始时间过了
			scheduleStartBattleField();
		}
		
	}
	
	/** 开始本次阵营战  */
	@Scheduled(name="开始乱舞战场", type=ValueType.BEANNAME, value = "BATTLE_FIELD_START_TIME")
	private synchronized void scheduleStartBattleField(){
		if(this.battleStatus.get() != BattleFieldRule.STATUS_INIT){
			return ;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		this.battleDate = calendar.getTime();	//战场开始时间
		
		calendar.add(Calendar.MINUTE, BATTLE_FIELD_TIMEOUT);
		battleStopDate = calendar.getTime();	//战场结束时间
		scheduler.schedule(new ScheduledTask() {
			
			public void run() {
				handleBattleStop();
			}
			
			public String getName() { return "乱武战场结束线程";	}
		}, battleStopDate);
		battleStatus.set(BattleFieldRule.STATUS_START);
		
		handleBattleFieldStart();
	}
	
	/**
	 * 处理战场开始
	 */
	private void handleBattleFieldStart(){
		Camp[] camps = Camp.values();
		Collection<Long> invitePlayerIds = new ArrayList<Long>();
		for(Camp camp : camps){
			if(camp == null || camp == Camp.NONE){
				continue;
			}
			
			Channel channels = Channel.valueOf(ChatChannel.CAMP_CHANNEL.ordinal(), camp.ordinal());
			Collection<Long> players = channelFacade.getChannelPlayers(channels);
			if(players != null){
				for(Long playerId : players){
					UserDomain userDomain = userManager.getUserDomain(playerId);
					if( userDomain == null ){
						continue;
					}
					PlayerBattle battle = userDomain.getBattle();
					if(battle.getLevel() < BattleFieldRule.BATTLE_FIELD_MIN_LEVEL ){
						continue;
					}
					invitePlayerIds.add(playerId);
				}
			}
			
		}
		
		sessionManager.write(invitePlayerIds, Response.defaultResponse(Module.BATTLE_FIELD, BattleFieldCmd.PUSH_BATTLE_FIELD_CMD, 1) );
	}
	
	/**
	 * 处理战场结束
	 */
	private void handleBattleStop(){
		battleStatus.set(BattleFieldRule.STATUS_INIT);
		Set<Long> playerIds = playerRoomMap.keySet();
		pushPlayerBattleInfo(playerIds, true);
		
		scheduler.scheduleWithDelay(new ScheduledTask() {
			
			public void run() {
				Set<Long> keySet = playerRoomMap.keySet();
				for(Long playerId : keySet){
					if( playerRoomMap.containsKey(playerId) ){
						ResultObject<ChangeScreenVo> resultObject = exitBattleField(playerId);
						Map<String, Object> resultMap = new HashMap<String, Object>(2);
						resultMap.put(ResponseKey.RESULT, resultObject.getResult() );
						resultMap.put("changeScreenVO", resultObject.getValue());
						sessionManager.write(playerId, Response.defaultResponse(Module.BATTLE_FIELD, BattleFieldCmd.EXIT_BATTLE_FIELD, resultMap) );
					}
				}
				
//				battleDate = null;
				battleStopDate = null;
			}
			
			public String getName() { return "强制乱武战场中的玩家出战场"; }
		}, TimeConstant.ONE_MINUTE_MILLISECOND);
	}

	/**
	 * 推送玩家战场信息
	 */
	private void pushPlayerBattleInfo(Collection<Long> playerIds) {
		pushPlayerBattleInfo(playerIds, false);
	}
	
	/**
	 * 推送玩家战场信息
	 */
	private void pushPlayerBattleInfo(Collection<Long> playerIds, boolean isBattleOver) {
		if(playerIds != null && playerIds.size() > 0){
			for(Long playerId : playerIds){
				PlayerBattleField playerBattleField = battleFieldManager.getPlayerBattleField(playerId);
				BattleFieldVO battleFieldVO = BattleFieldVO.valueOf(playerBattleField);
				battleFieldVO.setBattleOver(isBattleOver);
				
				sessionManager.write(playerId, Response.defaultResponse(Module.BATTLE_FIELD, BattleFieldCmd.PUSH_BATTLE_FIELD_INFO, battleFieldVO) );
			}
		}
	}
	
	
	public int enterBattleField(final long playerId, Map<String, Object> resultMap) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return BattleFieldConstant.PLAYER_NOT_FOUND;
		}
		PlayerBattle battle = userDomain.getBattle();
		if(battle.getLevel() < BattleFieldRule.BATTLE_FIELD_MIN_LEVEL ){
			return BattleFieldConstant.LEVEL_INVALID;
		}
		
		Player player = userDomain.getPlayer();
		Camp camp = player.getCamp();
		if(camp == null || camp == Camp.NONE){
			return BattleFieldConstant.MUST_HAD_CAMP;
		}
		
		if(battleStatus.get() != BattleFieldRule.STATUS_START){
			return BattleFieldConstant.BATTLE_FIELD_NOT_START_OR_OVER;
		}
		
		final PlayerBattleField playerBattleField = battleFieldManager.getPlayerBattleField(playerId);
		if(playerBattleField == null){
			return BattleFieldConstant.FAILURE;
		}
		
		if(campBattleFacade.isInCampBattle(userDomain)){
			return BattleFieldConstant.BATTLE_FIELD_STATUS_ERROR;
		}
		PlayerDungeon playerDungeon = dungeonFacade.getPlayerDungeon(playerId);
		if(playerDungeon != null && playerDungeon.isDungeonStatus()){
			return BattleFieldConstant.BATTLE_FIELD_STATUS_ERROR;
		}
		
		UserEscortTask userEscortTask = escortTaskManager.getEscortTask(battle);
		if(userEscortTask != null && !userEscortTask.isNoTask()){ //当玩家为运镖状态时,无需道具就能复活
			return BattleFieldConstant.BATTLE_FIELD_STATUS_ERROR;
		}
		
		//退出战场一定时间后才能再进
		long enterCDTime = playerBattleField.getEnterCDTime();
		if(enterCDTime > 0){
			long diffTime = enterCDTime - System.currentTimeMillis();
			if(diffTime > 0){
				resultMap.put(ResponseKey.COOL_TIME, diffTime / TimeConstant.ONE_SECOND_MILLISECOND  );
				return BattleFieldConstant.BATTLE_FIELD_ENTER_CDTIME;
			}
		}
		
		BattleCollectConfig battleCollectConfig = battleFieldService.getBattleCollectConfig(camp);
		if(battleCollectConfig == null){
			return BattleFieldConstant.BASEDATA_NOT_FOUND;
		}
		
		BattlePointConfig battlePointConfig = battleFieldService.getBattlePointConfig(0, camp);
		if(battlePointConfig == null){
			return BattleFieldConstant.BASEDATA_NOT_FOUND;
		}
		
		ChainLock lock = LockUtils.getLock(playerBattleField);
		try {
			lock.lock();
			if(playerBattleField.collectTaskVO == null){
				playerBattleField.collectTaskVO = new CollectTaskVO();
				playerBattleField.collectTaskVO.setBaseId( battleCollectConfig.getBaseId() );
				playerBattleField.collectTaskVO.setNpcId( battleCollectConfig.getNpcId() );
				playerBattleField.collectTaskVO.setTotalAmount( battleCollectConfig.getNum() );
			}
			
			Date playerBattleDate = playerBattleField.getBattleDate();
			if(playerBattleDate == null || !playerBattleDate.equals(this.battleDate) ){
				playerBattleField.reset();
				playerBattleField.setBattleDate(this.battleDate);
			}
			
		} finally {
			lock.unlock();
		}
		
		if(userDomain.getMapId() == CampBattleRule.CAMP_BATTLE_MAPID){
			campBattleFacade.existCampBattle(playerId);
		}
		
		BattleRoom battleRoom = selectBattleRoom(userDomain, playerBattleField);	//选择房间
		if(battleRoom == null){
			return BattleFieldConstant.FAILURE;
		}
		
		ChangeScreenVo changeScreenVo = mapFacade.leaveMap(userDomain, battleRoom.gameMap , battlePointConfig.getX(), battlePointConfig.getY() );
		if(changeScreenVo == null){
			return CampBattleConstant.FAILURE;
		}
		resultMap.put("changeScreenVO", changeScreenVo);
		
		return BattleFieldConstant.SUCCESS;
	}
	
	/**
	 * 选择并进入战场房间
	 * @param userDomain
	 * @return
	 */
	private BattleRoom selectBattleRoom(UserDomain userDomain, PlayerBattleField playerBattleField){
		Player player = userDomain.getPlayer();
		
		Long playerId = player.getId();
		Camp camp = player.getCamp();
		BattleRoom battleRoom = playerRoomMap.get(playerId);
		if(battleRoom == null){
			roomLock.lock();
			try {
				for(BattleRoom room : rooms){
					int players = room.players[camp.ordinal() - 1];
					if( players < BattleFieldRule.ROOM_MAX_CAMP_PLAYERS ){
						battleRoom = room;
						break;
					}
				}
				
				//没有创建新的战场房间
				if(battleRoom == null){
					battleRoom = createBattleRoom( BattleFieldRule.BATTLE_FIELD_MAPID, userDomain.getBranching() );
				}
				
				//添加成功
				if( playerRoomMap.putIfAbsent(playerId, battleRoom) == null ){
					++battleRoom.players[camp.ordinal() - 1];
				} else {
					battleRoom = playerRoomMap.get(playerId);
				}
				
			} catch (Exception ex) {
				logger.error("{}", ex);
				
			} finally {
				roomLock.unlock();
			}
		}
		BattleFieldLogger.logBattleInfo(this.battleDate, this.rooms, this.playerRoomMap );
		
		return battleRoom;
	}
	
	/**
	 * 创建新的战场房间
	 * @param mapId
	 * @param branching
	 * @return
	 */
	private BattleRoom createBattleRoom(int mapId, int branching){
		BattleRoom battleRoom = null;
		long temporaryGameMapId = gameMapManager.getTemporaryGameMapId();
		GameMap gameMap = gameMapManager.createTemporaryMap(mapId, temporaryGameMapId, branching );
		if(gameMap != null){
			List<MonsterConfig> monsterConfigs = resourceService.listByIndex(IndexName.MONSTER_MAPID, MonsterConfig.class, mapId);
			for(MonsterConfig monsterConfig : monsterConfigs){
				MonsterFightConfig monsterFight = resourceService.get(monsterConfig.getMonsterFightId(), MonsterFightConfig.class);
				int monsterConfigId = monsterConfig.getId();
				if (monsterFight == null) {
					logger.error("怪物的战斗信息基础表不存在,id:[{}]", monsterConfigId );
					continue;
				}
				monsterManager.addDungeonMonster(gameMap, monsterConfig, 0, branching);
			}
			
			battleRoom = BattleRoom.valueOf(gameMap);
			rooms.add( battleRoom );
		}
		return battleRoom;
	}
	
	/**
	 * 退出房间
	 * @param userDomain
	 * @return
	 */
	private BattleRoom exitBattleRoom(UserDomain userDomain){
		long playerId = userDomain.getId();
		Camp camp = userDomain.getPlayer().getCamp();
		BattleRoom battleRoom = playerRoomMap.remove(playerId);
		if(battleRoom != null){
			boolean clearMap = false;
			roomLock.lock();
			try {
				--battleRoom.players[camp.ordinal() - 1];
				
				//回收没有人的房间
				int total = 0;
				for(Camp cp : Camp.values()) {
					if(cp == Camp.NONE){
						continue;
					}
					total += battleRoom.players[ cp.ordinal() - 1 ];
				}
				if(total <= 0){
					clearMap = this.rooms.remove(battleRoom) && battleRoom.gameMap != null;
				}
				
			} finally {
				roomLock.unlock();
			}
			
			if(clearMap){
				battleRoom.gameMap.clear();
			}
			BattleFieldLogger.logBattleInfo(this.battleDate, this.rooms, this.playerRoomMap );
		}
		return battleRoom;
	}

	
	public void processKillPlayers(UserDomain attacker, UserDomain target) {
		if(this.battleStatus.get() != BattleFieldRule.STATUS_START){
			return ;
		}
		if(attacker.getMapId() != BattleFieldRule.BATTLE_FIELD_MAPID){
			return ;
		}
		Player attackerPlayer = attacker.getPlayer();
		Player targetPlayer = target.getPlayer();
		if(attackerPlayer.getCamp() == targetPlayer.getCamp()){
			return ;
		}
		
		PlayerBattleField playerBattleField = battleFieldManager.getPlayerBattleField( attacker.getPlayerId() );
		if(playerBattleField != null){
			int fightHonor = battleFieldService.getKillHonor();
			ChainLock lock = LockUtils.getLock(playerBattleField);
			try {
				lock.lock();
				playerBattleField.increaseFightHonor(fightHonor);
				playerBattleField.increaseKillPlayers();
			} finally {
				lock.unlock();
			}
			pushPlayerBattleInfo( Arrays.asList(attacker.getId()) );
		}
		
		PlayerBattleField targetPlayerBattleField = battleFieldManager.getPlayerBattleField( target.getId() );
		if(targetPlayerBattleField != null){
			int fightHonor = battleFieldService.getDeathHonor();
			ChainLock lock = LockUtils.getLock(targetPlayerBattleField);
			try {
				lock.lock();
				targetPlayerBattleField.increaseFightHonor(fightHonor);
				targetPlayerBattleField.increaseDeaths();
			} finally {
				lock.unlock();
			}
			pushPlayerBattleInfo( Arrays.asList(target.getId()) );
		}
		
	}

	
	public int processCollect(UserDomain userDomain, int npcId, int baseId) {
		if(this.battleStatus.get() != BattleFieldRule.STATUS_START){
			return FAILURE;
		}
		if(userDomain.getMapId() != BattleFieldRule.BATTLE_FIELD_MAPID){
			return FAILURE;
		}
		
		Player player = userDomain.getPlayer();
		BattleCollectConfig battleCollectConfig = battleFieldService.getBattleCollectConfig( player.getCamp() );
		if(battleCollectConfig == null){
			logger.error("乱武战场阵营[{}]采集配置不存在", player.getCamp() );
			return BASEDATA_NOT_FOUND;
		}
		if(battleCollectConfig.getBaseId() != baseId ){
			return FAILURE;
		}
		
		if( !checkNpcPosition(userDomain.getMotion(), npcId ) ){
			return BattleFieldConstant.POSITION_INVALID;	
		}
		
		Long playerId = player.getId();
		PlayerBattleField playerBattleField = battleFieldManager.getPlayerBattleField( playerId );
		if(playerBattleField == null){
			return BattleFieldConstant.PLAYER_NOT_FOUND;
		}
		
		if(playerBattleField.collectTaskVO == null || playerBattleField.collectTaskVO.getStatus() != TaskStatus.ACCEPTED){
			return BattleFieldConstant.COLLECT_NOT_ACCEPT;
		}
		
		int backpack = BackpackType.DEFAULT_BACKPACK;
		PropsStackResult stackResult = PropsHelper.calcPropsStack(playerId, backpack, baseId, 1, true);
		List<UserProps> newUserProps = stackResult.getNewUserProps();
		Map<Long, Integer> mergeProps = stackResult.getMergeProps();
		if(mergeProps.isEmpty() && newUserProps.isEmpty()) {
			return FAILURE;
		}
		
		if( checkUserPropsEnough(playerId, baseId, mergeProps, newUserProps, battleCollectConfig.getNum()) ){
			return BattleFieldConstant.COLLECT_COMPLETED;
		}
		
		if(!newUserProps.isEmpty()) {
			int currentBackSize = propsManager.getBackpackSize(playerId, backpack);
			ChainLock playerLock = LockUtils.getLock( player.getPackLock() );
			try {
				playerLock.lock();
				if(!player.canAddNew2Backpack(newUserProps.size() + currentBackSize, DEFAULT_BACKPACK)) {
					return BACKPACK_FULLED;
				}
				
				newUserProps = propsManager.createUserProps(newUserProps);
				propsManager.put2UserPropsIdsList(playerId, DEFAULT_BACKPACK, newUserProps);
			} catch (Exception e) {
				logger.error("角色: [{}] 采集物品异常:{}", playerId, e);
				return FAILURE;
			} finally {
				playerLock.unlock();
			}
		}
		playerBattleField.collectTaskVO.increaseAmount(1);
		
		Collection<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		if(!newUserProps.isEmpty()) {
			backpackEntries.addAll(newUserProps);
			GoodsLogger.goodsLogger(player, Source.PROPS_COLLECT, LoggerGoods.incomeProps(baseId, 1));
		}
		
		if(!mergeProps.isEmpty()) {
			Collection<UserProps> updateUserPropsList = propsManager.updateUserPropsList(mergeProps);
			backpackEntries.addAll(updateUserPropsList);
			GoodsLogger.goodsLogger(player, Source.PROPS_COLLECT, LoggerGoods.incomeProps(baseId, 1));
		}
		
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, backpackEntries);
		
		//推送采集任务给玩家
//		sessionManager.write(playerId, Response.defaultResponse(Module.BATTLE_FIELD, BattleFieldCmd.PUSH_COLLECT_TASK_CHANGE, playerBattleField.collectTaskVO) );
		
		return SUCCESS;
	}
	
	private boolean checkUserPropsEnough(long playerId, int propsId, Map<Long, Integer> mergeProps, List<UserProps> newUserProps, int totalCount){
		int amount = 0;
		if(mergeProps != null && mergeProps.size() > 0){
			for(Entry<Long, Integer> entry : mergeProps.entrySet() ){
				UserProps userProps = propsManager.getUserProps(entry.getKey());
				int count = userProps.getCount();
				if(count > 0 && userProps.getBaseId() == propsId){
					amount = amount + count + entry.getValue();
				}
			}
		}
		if(newUserProps != null && newUserProps.size() > 0){
			for(UserProps userProps : newUserProps){
				int count = userProps.getCount();
				if(count > 0 && userProps.getBaseId() == propsId){
					amount = amount + count;
				}
			}
		}
		return amount > totalCount;
	}
	
	private ResultObject<Map<Long, Integer>> checkUserPropsEnough(List<UserProps> userPropsList, int propsId, int totalCount){
		if(userPropsList != null){
			Map<Long, Integer> costMap = new HashMap<Long, Integer>();
			int remain = totalCount;
			for(UserProps userProps : userPropsList){
				int count = userProps.getCount();
				if(count > 0 && remain > 0){
					int cost = count > remain ? remain : count;
					remain = remain - cost;
					costMap.put(userProps.getId(), cost);
				}
				if(remain <= 0){
					return ResultObject.SUCCESS(costMap);
				}
			}
		}
		return ResultObject.ERROR(BattleFieldConstant.COLLECT_NOT_COMPLETE);
	}
	
	
	public int acceptCollectTask(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return BattleFieldConstant.PLAYER_NOT_FOUND;
		}
		PlayerBattle battle = userDomain.getBattle();
		int level = battle.getLevel();
		if(level < BattleFieldRule.BATTLE_FIELD_MIN_LEVEL ){
			return BattleFieldConstant.LEVEL_INVALID;
		}
		
		Player player = userDomain.getPlayer();
		Camp camp = player.getCamp();
		if(camp == null || camp == Camp.NONE){
			return BattleFieldConstant.MUST_HAD_CAMP;
		}
		
		PlayerBattleField playerBattleField = battleFieldManager.getPlayerBattleField(playerId);
		if(playerBattleField == null){
			return BattleFieldConstant.PLAYER_NOT_FOUND;
		}
		if(playerBattleField.collectTaskVO != null && playerBattleField.collectTaskVO.getStatus() != TaskStatus.UNACCEPT){
			return BattleFieldConstant.COLLECT_ACCEPTED;
		}
		BattleCollectConfig battleCollectConfig = battleFieldService.getBattleCollectConfig(camp);
		if(battleCollectConfig == null){
			return BattleFieldConstant.BASEDATA_NOT_FOUND;
		}
		
		if( !checkNpcPosition(userDomain.getMotion(), battleCollectConfig.getTaskNpc() ) ){
			return BattleFieldConstant.POSITION_INVALID;	
		}
		
		ChainLock lock = LockUtils.getLock(playerBattleField);
		try {
			lock.lock();
			if(playerBattleField.collectTaskVO == null){
				playerBattleField.collectTaskVO = new CollectTaskVO();
				playerBattleField.collectTaskVO.setBaseId( battleCollectConfig.getBaseId() );
				playerBattleField.collectTaskVO.setNpcId( battleCollectConfig.getNpcId() );
				playerBattleField.collectTaskVO.setAmount(0);
				playerBattleField.collectTaskVO.setTotalAmount( battleCollectConfig.getNum() );
			}
			playerBattleField.collectTaskVO.setStatus(TaskStatus.ACCEPTED);
			
		} finally {
			lock.unlock();
		}
		
		//推送采集任务给玩家
		sessionManager.write(playerId, Response.defaultResponse(Module.BATTLE_FIELD, BattleFieldCmd.PUSH_COLLECT_TASK_CHANGE, playerBattleField.collectTaskVO) );
		
		return BattleFieldConstant.SUCCESS;
	}

	private boolean checkNpcPosition(PlayerMotion playerMotion, int npcId){
		Npc npc = npcFacade.getNpc(npcId);
		return npc != null && playerMotion != null && MapUtils.checkPosScopeInfloat(playerMotion.getX(), playerMotion.getY(), npc.getBornX(), npc.getBornY(), 7);
	}
	
	
	public int rewardCollectTask(long playerId, long userPropsId) {
		if(battleStatus.get() != BattleFieldRule.STATUS_START){
			return BattleFieldConstant.BATTLE_FIELD_NOT_OVER;
		}
		
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return BattleFieldConstant.PLAYER_NOT_FOUND;
		}
		PlayerBattle battle = userDomain.getBattle();
		int level = battle.getLevel();
		if(level < BattleFieldRule.BATTLE_FIELD_MIN_LEVEL ){
			return BattleFieldConstant.LEVEL_INVALID;
		}
		
		Player player = userDomain.getPlayer();
		Camp camp = player.getCamp();
		if(camp == null || camp == Camp.NONE){
			return BattleFieldConstant.MUST_HAD_CAMP;
		}
		
		BattleCollectConfig battleCollectConfig = battleFieldService.getBattleCollectConfig(camp);
		if(battleCollectConfig == null){
			return BattleFieldConstant.BASEDATA_NOT_FOUND;
		}
		
		if( !checkNpcPosition(userDomain.getMotion(), battleCollectConfig.getTaskNpc() ) ){
			return BattleFieldConstant.POSITION_INVALID;	
		}
		
		List<UserProps> userPropsList = propsManager.listUserPropByBaseId(playerId, battleCollectConfig.getBaseId(), BackpackType.DEFAULT_BACKPACK);;
		if(userPropsList == null || userPropsList.isEmpty()){
			return BattleFieldConstant.ITEM_NOT_FOUND;
		}
		
		PlayerBattleField playerBattleField = battleFieldManager.getPlayerBattleField(playerId);
		if(playerBattleField == null){
			return BattleFieldConstant.PLAYER_NOT_FOUND;
		}
		
		if(playerBattleField.collectTaskVO == null){
			return BattleFieldConstant.COLLECT_NOT_ACCEPT;
		}
		
		int baseId = playerBattleField.collectTaskVO.getBaseId();
		if(baseId != userPropsList.get(0).getPropsConfig().getId()){
			return BattleFieldConstant.INPUT_VALUE_INVALID;
		}
		
		ResultObject<Map<Long, Integer>> resultObject = checkUserPropsEnough(userPropsList, baseId, battleCollectConfig.getNum());
		if(!resultObject.isOK()){
			return resultObject.getResult();
		}
		
		int collectHonor = battleCollectConfig.getHonor();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		List<UserProps> costUserPropsList = null;
		
		ChainLock lock = LockUtils.getLock(playerBattleField, player.getPackLock());
		try {
			lock.lock();
			resultObject = checkUserPropsEnough(userPropsList, baseId, battleCollectConfig.getNum());
			if(!resultObject.isOK()){
				return resultObject.getResult();
			}
			playerBattleField.increaseCollectHonor(collectHonor);
			playerBattleField.collectTaskVO.setAmount(0);
			playerBattleField.collectTaskVO.setStatus(TaskStatus.UNACCEPT);
		
			costUserPropsList = propsManager.costUserPropsList(resultObject.getValue());
		} finally {
			lock.unlock();
		}
		
		if(costUserPropsList != null && costUserPropsList.size() > 0){
			MessagePushHelper.pushUserProps2Client(playerId, backpack, false, costUserPropsList);
			
			Collection<GoodsVO> goodsVOs = GoodsVO.valuleOf(null, costUserPropsList, resultObject.getValue(), null);
			MessagePushHelper.pushGoodsCountChange2Client(playerId, goodsVOs);
			GoodsLogger.goodsLogger(player, Source.PROPS_COLLECT, LoggerGoods.incomeProps(baseId, 1));
		}
		//推送战场信息给玩家
		pushPlayerBattleInfo( Arrays.asList(playerId) );
		
		//推送采集任务给玩家
		sessionManager.write(playerId, Response.defaultResponse(Module.BATTLE_FIELD, BattleFieldCmd.PUSH_COLLECT_TASK_CHANGE, playerBattleField.collectTaskVO) );
		
		return BattleFieldConstant.SUCCESS;
	}

	
	public ResultObject<ChangeScreenVo> exitBattleField(final long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(BattleFieldConstant.PLAYER_NOT_FOUND);
		}
		PlayerBattle battle = userDomain.getBattle();
		if(battle.getLevel() < BattleFieldRule.BATTLE_FIELD_MIN_LEVEL ){
			return ResultObject.ERROR(BattleFieldConstant.LEVEL_INVALID);
		}
		
		Player player = userDomain.getPlayer();
		Camp camp = player.getCamp();
		if(camp == null || camp == Camp.NONE){
			return ResultObject.ERROR(BattleFieldConstant.MUST_HAD_CAMP);
		}
		
		PlayerBattleField playerBattleField = battleFieldManager.getPlayerBattleField(playerId);
		if(playerBattleField == null){
			return ResultObject.ERROR(BattleFieldConstant.PLAYER_NOT_FOUND);
		}
		
//		BattleRoom battleRoom = exitBattleRoom(userDomain);
//		if(battleRoom == null){
//			return ResultObject.ERROR(BattleFieldConstant.FAILURE);
//		}
		
		ChainLock lock = LockUtils.getLock(playerBattleField);
		try {
			lock.lock();
			playerBattleField.collectTaskVO = null;
			playerBattleField.setEnterCDTime( System.currentTimeMillis() + BattleFieldRule.BATTLE_FIELD_ENTER_CDTIME); 
		} finally {
			lock.unlock();
		}
		dbService.submitUpdate2Queue(playerBattleField);
		
		GameMap gameMap = gameMapManager.getGameMapById(MapRule.DEFAUL_REVIVE_MAPID, userDomain.getBranching());
		Point point = gameMap.getRandomCanStandPoint(MapRule.DEFAUL_REVIVE_X, MapRule.DEFAUL_REVIVE_Y, 20);
		int targetX = MapRule.DEFAUL_REVIVE_X, targetY = MapRule.DEFAUL_REVIVE_Y;
		if(point != null){
			targetX = point.getX();
			targetY = point.getY();
		}
		ChangeScreenVo changeScreenVo = mapFacade.leaveMap(userDomain, gameMap , targetX, targetY );
		if(changeScreenVo == null){
			return ResultObject.ERROR(CampBattleConstant.FAILURE);
		}
		exitBattleRoom(userDomain);
		
		final BattleCollectConfig battleCollectConfig = battleFieldService.getBattleCollectConfig( camp );
		if(battleCollectConfig != null){
			List<UserProps> userPropsList = propsManager.listUserPropByBaseId(playerId, battleCollectConfig.getBaseId(), BackpackType.DEFAULT_BACKPACK);
			if(userPropsList != null && userPropsList.size() > 0){
				Map<Long, Integer> updateUserItems = new HashMap<Long, Integer>( userPropsList.size() );
				List<UserProps> updateUserPropsList = null;
				ChainLock lock2 = LockUtils.getLock(player.getPackLock());
				lock2.lock();
				try {
					for(UserProps userProps: userPropsList){
						updateUserItems.put(userProps.getId(), -userProps.getCount());
					}
					updateUserPropsList = propsManager.updateUserPropsList(updateUserItems);
				} finally {
					lock2.unlock();
				}
				//推送物品消失
				MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, updateUserPropsList );
				MessagePushHelper.pushGoodsCountChange2Client(playerId, GoodsVO.valuleOf(null, updateUserPropsList, updateUserItems, null) );
			}
		} else {
			logger.error("乱武战场阵营[{}]采集配置不存在", camp );
		}
		
		
		return ResultObject.SUCCESS(changeScreenVo);
	}

	
	public int reward(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return BattleFieldConstant.PLAYER_NOT_FOUND;
		}
		PlayerBattle battle = userDomain.getBattle();
		int level = battle.getLevel();
		if(level < BattleFieldRule.BATTLE_FIELD_MIN_LEVEL ){
			return BattleFieldConstant.LEVEL_INVALID;
		}
		
		Player player = userDomain.getPlayer();
		Camp camp = player.getCamp();
		if(camp == null || camp == Camp.NONE){
			return BattleFieldConstant.MUST_HAD_CAMP;
		}
		
		if(battleStatus.get() != BattleFieldRule.STATUS_INIT){
			return BattleFieldConstant.BATTLE_FIELD_NOT_OVER;
		}
		
		PlayerBattleField playerBattleField = battleFieldManager.getPlayerBattleField(playerId);
		if(playerBattleField == null){
			return BattleFieldConstant.PLAYER_NOT_FOUND;
		}
		
		Date battleDate = playerBattleField.getBattleDate();
		if(battleDate == null){
			return BattleFieldConstant.NO_BATTLE_FIELD_RECORD;
		}
		
		Date rewardDate = playerBattleField.getRewardDate();
		if(rewardDate != null && battleDate.before(rewardDate) ){
			return BattleFieldConstant.REWARDED;
		}
		
		final int honor = playerBattleField.getFightHonor() + playerBattleField.getCollectHonor();
		BattleRewardsConfig battleRewardsConfig = battleFieldService.getBattleHonorRewards( honor );
		if(battleRewardsConfig == null){
			return BattleFieldConstant.EMPTY_REWARD;
		}
		int addExp = FormulaHelper.invoke(battleRewardsConfig.getExp(), level).intValue();
		
		ChainLock lock = LockUtils.getLock(playerBattleField, battle);
		try {
			lock.lock();
			rewardDate = playerBattleField.getRewardDate();
			if(rewardDate != null && battleDate.before(rewardDate) ){
				return BattleFieldConstant.REWARDED;
			}
			playerBattleField.setRewardDate(new Date());
			battle.increaseExp(addExp);
			if(addExp != 0) {
				ExpLogger.battleFieldExp(userDomain, Source.EXP_BATTLE_FIELD_REWARD, addExp);
			}
			
			BattleFieldLogger.log(player, battle, addExp, playerBattleField);
			playerBattleField.reset();
			
		} finally {
			lock.unlock();
		}
		dbService.submitUpdate2Queue(playerBattleField);
		
		//推送属性
		List<Long> playerIdList = Arrays.asList(player.getId());
		List<UnitId> unitIdList = Arrays.asList(UnitId.valueOf(player.getId(), ElementType.PLAYER));
		UserPushHelper.pushAttribute2AreaMember(player.getId(),playerIdList, unitIdList, AttributeKeys.EXP);
		
		return BattleFieldConstant.SUCCESS;
	}

	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		long playerId = messageInfo.getPlayerId();
		exitBattleField(playerId);
	}

	
	public void onLogoutEvent(UserDomain userDomain) {
		//战场结束刷新时退出战场
		if(this.battleStatus.get() == BattleFieldRule.STATUS_INIT){
			long playerId = userDomain.getPlayerId();
			if(playerRoomMap.containsKey(playerId)){
				exitBattleField(playerId);
			}
		}
	}

	
	public GameMap getBattleFieldGameMap(UserDomain userDomain) {
		long playerId = userDomain.getPlayerId();
		BattleRoom battleRoom = this.playerRoomMap.get(playerId);
		if(battleRoom != null){
			return battleRoom.gameMap;
		}
		return null;
	}

	
	public boolean isInBattleField(long playerId) {
		BattleRoom battleRoom = this.playerRoomMap.get(playerId);
		return battleRoom != null && battleRoom.gameMap != null;
	}

	
	public int battleRequestCmd(long playerId, int cmd, Map<String, Object> resultMap) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return BattleFieldConstant.PLAYER_NOT_FOUND;
		}
		if(cmd == 2){	//2-下次战场开启时间
			Date nextBattleTime = startTimeGenerator.next(new Date());
			resultMap.put("battleTime", nextBattleTime);
			return BattleFieldConstant.SUCCESS;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		int level = battle.getLevel();
		if(level < BattleFieldRule.BATTLE_FIELD_MIN_LEVEL ){
			return BattleFieldConstant.LEVEL_INVALID;
		}
		
		Player player = userDomain.getPlayer();
		Camp camp = player.getCamp();
		if(camp == null || camp == Camp.NONE){
			return BattleFieldConstant.MUST_HAD_CAMP;
		}
		
		int result = BattleFieldConstant.FAILURE;
		if(cmd == 1){	//1-本次战场结束时间
			if(this.battleStatus.get() != BattleFieldRule.STATUS_START){
				return BattleFieldConstant.BATTLE_FIELD_NOT_START_OR_OVER;
			}
			resultMap.put("battleEndTime", this.battleStopDate);
			result = BattleFieldConstant.SUCCESS;
			
		} else if(cmd == 3){
			if(this.battleStatus.get() != BattleFieldRule.STATUS_START || userDomain.getMapId() == BattleFieldRule.BATTLE_FIELD_MAPID){
				return BattleFieldConstant.SUCCESS;
			}
			if(camp != null && camp != Camp.NONE && battle.getLevel() >= BattleFieldRule.BATTLE_FIELD_MIN_LEVEL){
				sessionManager.write(playerId, Response.defaultResponse(Module.BATTLE_FIELD, BattleFieldCmd.PUSH_BATTLE_FIELD_CMD, 1) );
			}
			result = BattleFieldConstant.SUCCESS;
			
		} else if(cmd == 4){
			if(this.battleStatus.get() != BattleFieldRule.STATUS_START && camp != null && camp != Camp.NONE && battle.getLevel() >= BattleFieldRule.BATTLE_FIELD_MIN_LEVEL){
				PlayerBattleField playerBattleField = battleFieldManager.getPlayerBattleField(playerId);
				Date rewardDate = playerBattleField.getRewardDate();
				Date battleDate = playerBattleField.getBattleDate();
				result = battleDate != null && ( rewardDate == null || battleDate.before(rewardDate) ) ? 0 : 1;
			}
		}
		
		return result;
	}

	
	public void doEnterBattleField(UserDomain userDomain) {
		if(userDomain == null || userDomain.getMapId() != BattleFieldRule.BATTLE_FIELD_MAPID){
			return ;
		}
		final long playerId = userDomain.getPlayerId();
		
		scheduler.scheduleWithDelay(new ScheduledTask() {
			
			public void run() {
				try {
					PetDomain fightingPet = petManager.getFightingPet(playerId);
					if(fightingPet != null && fightingPet.getPet().isStatus(PetStatus.FIGHTING) ){
						ResultObject<Long> resultObject = petFacade.goBack(playerId);
						if(resultObject != null && resultObject.isOK() ){
							Map<String, Object> resultMap = new HashMap<String, Object>(2);
							resultMap.put(ResponseKey.RESULT, resultObject.getResult());
							resultMap.put(ResponseKey.PET_ID, resultObject.getValue());
							Response response = Response.defaultResponse(Module.PET, PetCmd.PET_BACK, resultMap);
							sessionManager.write(playerId, response);
						}
					}
					
				} catch (Exception e) {
					logger.error("{}", e);
				}
				
				//推送战场消息给玩家
				pushPlayerBattleInfo( Arrays.asList(playerId) );
				
				//推送采集任务给玩家
				final PlayerBattleField playerBattleField = battleFieldManager.getPlayerBattleField(playerId);
				if(playerBattleField != null){
					sessionManager.write(playerId, Response.defaultResponse(Module.BATTLE_FIELD, BattleFieldCmd.PUSH_COLLECT_TASK_CHANGE, playerBattleField.collectTaskVO) );
				}
			}
			
			public String getName() { return "进入乱武战场推送采集任务信息";}
		}, 2 * TimeConstant.ONE_SECOND_MILLISECOND);
	}
	
}
