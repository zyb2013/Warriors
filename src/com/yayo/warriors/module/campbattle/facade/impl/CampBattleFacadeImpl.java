package com.yayo.warriors.module.campbattle.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.cache.CachedService;
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
import com.yayo.common.socket.push.Pusher;
import com.yayo.common.thread.NamedThreadFactory;
import com.yayo.common.utility.DateUtil;
import com.yayo.common.utility.EnumUtils;
import com.yayo.common.utility.TimeConstant;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.adapter.CampBattleService;
import com.yayo.warriors.basedb.model.BulletinConfig;
import com.yayo.warriors.basedb.model.CampPointConfig;
import com.yayo.warriors.basedb.model.CampScoreRewards;
import com.yayo.warriors.basedb.model.CampTitleRewards;
import com.yayo.warriors.basedb.model.EquipConfig;
import com.yayo.warriors.basedb.model.MonsterConfig;
import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.CampBattlePushHelper;
import com.yayo.warriors.common.helper.FormulaHelper;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.NoticePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.alliance.entity.Alliance;
import com.yayo.warriors.module.alliance.manager.AllianceManager;
import com.yayo.warriors.module.battlefield.facade.BattleFieldFacade;
import com.yayo.warriors.module.battlefield.rule.BattleFieldRule;
import com.yayo.warriors.module.campbattle.constant.CampBattleConstant;
import com.yayo.warriors.module.campbattle.entity.CampBattleHistory;
import com.yayo.warriors.module.campbattle.entity.PlayerCampBattleHistory;
import com.yayo.warriors.module.campbattle.entity.PlayerCampBattleRecord;
import com.yayo.warriors.module.campbattle.facade.CampBattleFacade;
import com.yayo.warriors.module.campbattle.manager.CampBattleManager;
import com.yayo.warriors.module.campbattle.manager.impl.CampBattleManagerImpl;
import com.yayo.warriors.module.campbattle.model.CampBattle;
import com.yayo.warriors.module.campbattle.model.PlayerCampBattle;
import com.yayo.warriors.module.campbattle.rule.CampBattleRule;
import com.yayo.warriors.module.campbattle.type.CampTitle;
import com.yayo.warriors.module.campbattle.type.PlayerCampBattleStatus;
import com.yayo.warriors.module.campbattle.vo.ApplyVO;
import com.yayo.warriors.module.campbattle.vo.BattleInfoVO;
import com.yayo.warriors.module.campbattle.vo.CampBattleVO;
import com.yayo.warriors.module.campbattle.vo.PlayerBattleVO;
import com.yayo.warriors.module.campbattle.vo.PlayerScoreVO;
import com.yayo.warriors.module.chat.facade.ChannelFacade;
import com.yayo.warriors.module.chat.model.Channel;
import com.yayo.warriors.module.chat.type.ChatChannel;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.dungeon.facade.DungeonFacade;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.logger.helper.LoggerPropsHelper;
import com.yayo.warriors.module.logger.log.ExpLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.log.SilverLogger;
import com.yayo.warriors.module.logger.type.Orient;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.map.manager.GameMapManager;
import com.yayo.warriors.module.map.model.ChangePoint;
import com.yayo.warriors.module.map.rule.MapRule;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.manager.MonsterManager;
import com.yayo.warriors.module.monster.model.IMonsterConfig;
import com.yayo.warriors.module.monster.model.MonsterBattle;
import com.yayo.warriors.module.monster.type.MonsterType;
import com.yayo.warriors.module.notice.rule.NoticeRule;
import com.yayo.warriors.module.notice.type.NoticeID;
import com.yayo.warriors.module.notice.type.NoticeType;
import com.yayo.warriors.module.notice.vo.NoticeVo;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.pet.facade.PetFacade;
import com.yayo.warriors.module.pet.helper.PetHelper;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.pet.types.PetStatus;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.module.props.rule.EquipHelper;
import com.yayo.warriors.module.rank.manager.RankManager;
import com.yayo.warriors.module.server.listener.DataRemoveListener;
import com.yayo.warriors.module.syscfg.manager.SystemConfigManager;
import com.yayo.warriors.module.task.entity.UserEscortTask;
import com.yayo.warriors.module.task.manager.EscortTaskManager;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.entity.PlayerMotion;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.pet.PetCmd;
import com.yayo.warriors.socket.vo.ChangeScreenVo;
import com.yayo.warriors.type.ElementType;
import com.yayo.warriors.type.FormulaKey;
import com.yayo.warriors.type.IndexName;
import com.yayo.warriors.util.GameConfig;

@Service
public class CampBattleFacadeImpl implements CampBattleFacade, DataRemoveListener, ApplicationListener<ContextRefreshedEvent> {
	@Autowired
	private UserManager userManager;
	@Autowired
	private AllianceManager allianceManager;
	@Autowired
	private MapFacade mapFacade;
	@Autowired
	private CampBattleManager campBattleManager;
	@Autowired
	private Scheduler scheduler;
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private GameMapManager gameMapManager;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private CampBattleService campBattleService;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private DbService dbService;
	@Autowired
	private MonsterManager monsterManager;
	@Autowired
	private CachedService cachedService;
	@Autowired
	private ChannelFacade channelFacade;
	@Autowired
	private SystemConfigManager systemConfigManager;
	@Autowired
	private PetFacade petFacade;
	@Autowired
	private Pusher pusher;
	@Autowired
	private PetManager petManager;
	@Autowired
	private RankManager rankManager;
	@Autowired
	private BattleFieldFacade battleFieldFacade;
	@Autowired 
	private DungeonFacade dungeonFacade;
	@Autowired
	private EscortTaskManager escortTaskManager;
	
	@Autowired
	@Qualifier("CAMPBATTLE_APPLY_START_TIME")
	private String CAMPBATTLE_APPLYSTARTTIME;	//阵营战开始报名时间
	
	@Autowired
	@Qualifier("CAMPBATTLE_START_TIME")
	private String CAMPBATTLE_STARTTIME;		//阵营战开始时间
	
	@Autowired
	@Qualifier("CAMP_BATTLE_SCORE_THRESHOLD")
	private Integer CAMP_BATTLE_SCORE_THRESHOLD = 1000;	//上届阵营战分差
	
	@Autowired(required = false)
	@Qualifier("BATTLEFIELD_MAX_CAMP_PLAYERS")
	public Integer BATTLEFIELD_MAX_CAMP_PLAYERS = 30;	//阵营战场(每个)阵营最大的人数
	
	@Autowired
	@Qualifier("BATTLEFIELD_OPEN")
	public Boolean BATTLEFIELD_OPEN = false;	//阵营战场是否开启
	
	/** 日志 */
	private final Logger logger = LoggerFactory.getLogger( getClass() );
	
	/** 阵营战状态  0-初始状态，1-系统准备报名中	2-报名开始， 3-报名者优先进入阵营战场， 4-阵营战开始，5-计算战场结果， 6-阵营战结束*/
	private AtomicInteger campBattleStatus = new AtomicInteger();
	
	/** 阵营战线程组 */
	NamedThreadFactory factory = new NamedThreadFactory(new ThreadGroup("阵营战"), "战场消息推送线程");
	
	/** 阵营战信息对象 */
	private Map<Camp, CampBattle> campBattles = null;
	
	/** 阵营战游戏地图 */
	private ConcurrentMap<Integer, GameMap> campBattleGameMaps = new ConcurrentHashMap<Integer, GameMap>(1);
	
	/** 玩家的阵营战场对象 */
	private ConcurrentMap<Long, PlayerCampBattle> playerCampBattleMap = new ConcurrentHashMap<Long, PlayerCampBattle>(5);
	
	/** 没有被占领的据点id列表  */
	private CopyOnWriteArrayList<Integer> noCampPointId = new CopyOnWriteArrayList<Integer>();
	
	/** 据点怪列表  */
	private ConcurrentMap<Integer, MonsterDomain> pointMonsterMap = new ConcurrentHashMap<Integer, MonsterDomain>(1);

	/** 阵营战结束future */
	private ScheduledFuture<?> campBattleStopFuture = null;
	
	/** 本次阵营战日期时间  */
	private Date campBattleDate = null;
	
	/** 本次阵营战结束时间  */
	private Date campBattleEndTime = null;
	
	/** 上届的阵营战日期时间  */
	private Date preCampBattleDate = null;
	
	/** 阵营战分支  */
	private int globleBranch = 1;
	
	/** 是否需要推送 */
	private AtomicBoolean pushFlag = new AtomicBoolean();
	
	/** 公告缓存 */
	private ConcurrentMap<Integer, Object> noticeMap = new ConcurrentHashMap<Integer, Object>();
	
	private CronSequenceGenerator applyTimeGenerator = null;
	private CronSequenceGenerator startTimeGenerator = null;
	private CopyOnWriteArrayList<Date> roundBattleDates = new CopyOnWriteArrayList<Date>();
	private int round = 0;				//本周第几场
	private boolean last = false;		//本场战场是否是最后一场(本周)
	
	
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if(applyTimeGenerator == null){
			applyTimeGenerator = new CronSequenceGenerator(CAMPBATTLE_APPLYSTARTTIME, TimeZone.getDefault() );
		}
		if(startTimeGenerator == null){
			startTimeGenerator = new CronSequenceGenerator(CAMPBATTLE_STARTTIME, TimeZone.getDefault() );
		}
		if(BATTLEFIELD_OPEN == null || !BATTLEFIELD_OPEN){
			return ;
		}
		
		if(campBattleStatus.get() != CampBattleRule.STATUS_INIT){
			return ;
		}
		
		fillRoundBattleDates();
		/** corn表达式生成解析器 */
		Date now = new Date();
		Date firstApplyDate = applyTimeGenerator.next(now);		//启服后第一个报名开始时间
		Date firstStartTimeDate = startTimeGenerator.next(now);	//启服后第一个战场开始时间
		
		List<Date> campBattleDates = campBattleManager.getCampBattleDates();
		this.preCampBattleDate = campBattleDates != null && campBattleDates.size() > 0 ? campBattleDates.get(0) : null;	//上次阵营战时间
		if( firstStartTimeDate.before(firstApplyDate) ){		//调度报名时间过了, 主动调用报名方法
			scheduleStartApplyCampBattle();
		}
		
		int index = this.roundBattleDates.indexOf(firstStartTimeDate);
		this.round =  index < 0 ? this.roundBattleDates.size() : index;
		this.last = this.round >= this.roundBattleDates.size();
		
		//需要重置玩家的战场记录
		if(this.round <= 0){
			clearPlayerCampBattleRecord();
		}
	}
	
	/**
	 * 是否开启阵营战
	 * @return
	 */
	private boolean isOpenCampBattle(){
		Date firstOpenTime = systemConfigManager.getFirstOpenTime();
		if(firstOpenTime != null){
			Calendar calendar = Calendar.getInstance();
			Calendar first = Calendar.getInstance();
			first.setTime(firstOpenTime);
			return calendar.get(Calendar.YEAR) >= first.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) > first.get(Calendar.DAY_OF_YEAR);
		}
		return true;
	}
	
	/**
	 * 刷新一周的战场时间列表
	 */
	@Scheduled(type = ValueType.BEANNAME, value = "", defaultValue = "0 0 0 * * 1", name = "CAMP_BATTLE_REFRESH_WEEK_DATES")
	private void clearPlayerCampBattleRecord(){
		//刷新本周所有战场时间
		fillRoundBattleDates();
		
		//重置所有玩家的战场记录(包括参加次数，总得分)
		this.campBattleManager.clearPlayerCampBattleRecord();
		cachedService.removeFromCommonCache(CampBattleManagerImpl.PLAYER_CAMP_BATTLE_SCORE_PREFIX);
		
		//重置所有在线玩家的阵营称号
		Camp[] camps = Camp.values();
		for(Camp camp : camps){
			if(camp == Camp.NONE){
				continue;
			}
			Channel channels = Channel.valueOf(ChatChannel.CAMP_CHANNEL.ordinal(), camp.ordinal());
			Collection<Long> players = channelFacade.getChannelPlayers(channels);
			for(long playerId : players){
				UserDomain userDomain = userManager.getUserDomain(playerId);
				if(userDomain == null){
					continue ;
				}
				PlayerBattle playerBattle = userDomain.getBattle();
				if(playerBattle.getLevel() < CampBattleRule.APPLY_MIN_LEVEL){
					continue ;
				}
				
				PlayerCampBattleRecord playerCampBattleRecord = campBattleManager.getPlayerCampBattleRecord(playerId);
				ChainLock lock = LockUtils.getLock(playerCampBattleRecord);
				try {
					lock.lock();
					playerCampBattleRecord.setJoins(0);
					playerCampBattleRecord.setTotalScore(0);
				} finally {
					lock.unlock();
				}
				dbService.submitUpdate2Queue(playerCampBattleRecord);
			}
		}
		
	}
	
	/**
	 * 刷新本周所有战场时间
	 */
	private void fillRoundBattleDates(){
		synchronized (this.roundBattleDates) {
			this.round = 0;
			this.roundBattleDates.clear();
			Date firstOpenTime = systemConfigManager.getFirstOpenTime();
			Date date = DateUtil.firstTimeOfWeek(Calendar.MONDAY, null);
			while(true){
				date = startTimeGenerator.next(date);
				if( date.before(firstOpenTime) ){
					continue ;
				}
				if( !DateUtil.isSameWeek(date, Calendar.MONDAY) ){
					break;
				}
				this.roundBattleDates.add(date);
			};
		}
	}

	/** 开始本次阵营战  */
	@Scheduled(name="开始报名参加阵营战", type=ValueType.BEANNAME, value = "CAMPBATTLE_APPLY_START_TIME")
	private synchronized void scheduleStartApplyCampBattle(){
		if(BATTLEFIELD_OPEN == null || !BATTLEFIELD_OPEN){
			return ;
		}
		if(!isOpenCampBattle()){
			return ;
		}
		if(!campBattleStatus.compareAndSet( CampBattleRule.STATUS_INIT, CampBattleRule.STATUS_SYS_APPLY) ){	//系统报名了
			return ;
		}
		playerCampBattleMap.clear();
		
		//战场报名前几名优先进入阵营战场
		Date now = new Date();
		Date startTimeDate = startTimeGenerator.next(now);	//本次战场开始时间
		this.campBattleDate = startTimeDate;
		Calendar cal = Calendar.getInstance();
		cal.setTime(startTimeDate);
		cal.add(Calendar.MINUTE, GameConfig.getCampBattleTimeout() );
		this.campBattleEndTime = cal.getTime();
		
		List<Date> campBattleDates = this.getCampBattleDates();
		this.preCampBattleDate = campBattleDates != null && campBattleDates.size() > 0 ? campBattleDates.get(0) : null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startTimeDate);
		calendar.add(Calendar.MINUTE, -CampBattleRule.PRE_MIN_ENTER_BATTLE );
		Date preEnterTime = calendar.getTime();
		if(!this.roundBattleDates.contains(startTimeDate)){
			this.clearPlayerCampBattleRecord();
		}
		
		if(this.round <= 0 ){
			this.fillRoundBattleDates();
			int index = this.roundBattleDates.indexOf(startTimeDate);
			this.round =  index < 0 ? this.roundBattleDates.size() : index + 1;
		}
		this.last = this.round >= this.roundBattleDates.size();
		
		List<CampBattleHistory> hisList = null;
		Map<Camp, CampBattle> campBattleMap = new HashMap<Camp, CampBattle>(2);
		for(Camp camp : Camp.values()){
			List<Integer> campPointIds = campBattleService.getCampPointIds(camp);
			if(camp == Camp.NONE){
				noCampPointId.addAll(campPointIds);
				continue;
			}
			
			CampBattle campBattle = CampBattle.valueOf(camp, this.last);
			CopyOnWriteArrayList<Integer> ownPointIds = campBattle.getOwnPointIds();
			ownPointIds.addAll(campPointIds);
			
			//取得上届的阵营官衔,并自动创建玩家战场信息对象
			if(this.preCampBattleDate != null){
				List<PlayerCampBattleRecord> campTitlePlayers = this.getCampTitlePlayers(camp);
				if(campTitlePlayers != null){
					for(PlayerCampBattleRecord record : campTitlePlayers){
						Long playerId = record.getId();
						UserDomain userDomain = userManager.getUserDomain(playerId);
						playerCampBattleMap.put(playerId, PlayerCampBattle.valueOf(userDomain, record.getCampTitle(), last ));
					}
				}
			}
			
			campBattleMap.put(camp, campBattle);
			
			if(this.preCampBattleDate != null){
				hisList = new ArrayList<CampBattleHistory>();
				CampBattleHistory campBattleHistory = getCampBattleHistory(this.preCampBattleDate, camp);
				if(campBattleHistory != null){
					hisList.add( campBattleHistory );
				}
			}
			
		}
		
		campBattles = campBattleMap;
		
		Camp weakCamp = null;
		if(hisList != null && hisList.size() > 0){
			Collections.sort(hisList);
			CampBattleHistory winCampHis = hisList.get(0);
			CampBattleHistory weakCampHis = hisList.get(hisList.size() - 1);
			weakCamp = winCampHis.getScores() - weakCampHis.getScores() > this.CAMP_BATTLE_SCORE_THRESHOLD ? weakCampHis.getCamp() : null;
		}
		
		//创建阵营战场地图
		createCampBattleMap(globleBranch, weakCamp);
		
		//可能要发一些公告给玩家
		campBattleStatus.compareAndSet( CampBattleRule.STATUS_SYS_APPLY, CampBattleRule.STATUS_APPLY);
		//推送报名开始
		sendBattleCmd(4);
		
		if(preEnterTime.after(now)){
			scheduler.schedule(new ScheduledTask() {
				
				public void run() {
					applerEnterCampBattle();
				}
				
				public String getName() {
					return "战场报名前几名优先进入阵营战场";
				}
			}, preEnterTime);
			
		} else {
			applerEnterCampBattle();
		}
		
	}
	
	/**
	 * 创建阵营战游戏地图
	 * @param branch
	 */
	private void createCampBattleMap(int branch, Camp weakCamp){
		final long tempGameMapId = gameMapManager.getTemporaryGameMapId();
		int mapId = CampBattleRule.CAMP_BATTLE_MAPID;
		GameMap tempMap = gameMapManager.createTemporaryMap(mapId, tempGameMapId, branch);
		
		Set<Integer> campPointIds = campBattleService.getCampPointIds();
		List<MonsterConfig> monsterConfigs = resourceService.listByIndex(IndexName.MONSTER_MAPID, MonsterConfig.class, mapId);
		for(MonsterConfig monsterConfig : monsterConfigs){
			MonsterFightConfig monsterFight = resourceService.get(monsterConfig.getMonsterFightId(), MonsterFightConfig.class);
			int monsterConfigId = monsterConfig.getId();
			if (monsterFight == null) {
				logger.error("怪物的战斗信息基础表不存在,id:[{}]", monsterConfigId );
				continue;
			}
			
			byte monsterType = 0;
			Camp camp = EnumUtils.getEnum(Camp.class, monsterFight.getMonsterCamp() );
			if( campPointIds.contains(monsterConfigId) ){
				monsterType = 1;
			} else if(monsterFight.isBoss()){	//是BOSS
				monsterType = 2;
			}  else if(monsterFight.getMonsterType() == MonsterType.ELITE.getValue()){	//是精英
				monsterType = 4;
			} else if( this.preCampBattleDate != null){	//普通怪
				if(weakCamp != null && camp == weakCamp){
					monsterType = 3;
				} else {
					continue;
				}
			}
			
			MonsterDomain monsterAiDomain = (MonsterDomain)monsterManager.addDungeonMonster(tempMap, monsterConfig, 0, branch);
			if(monsterAiDomain != null){
				if(monsterType == 1){
					Camp monsterCamp = campBattleService.getInitCampByMonsterConfigId(monsterConfigId);
					monsterAiDomain.setMonsterCamp(monsterCamp);
					pointMonsterMap.put(monsterConfigId, monsterAiDomain);
					
				} else if(monsterType == 2){
					CampBattle campBattle = getCampBattle(camp);
					if(campBattle != null){
						campBattle.setMonsterAiDomain(monsterAiDomain);
					}
					
				}
			}
		}
		campBattleGameMaps.put(branch, tempMap);
	}
	
	/**
	 * 处理报名的先进
	 */
	private void applerEnterCampBattle(){
		campBattleStatus.set(CampBattleRule.STATUS_APPLYER_ENTER);
//		campBattleStatus.compareAndSet( CampBattleRule.STATUS_APPLY, CampBattleRule.STATUS_APPLYER_ENTER);
		
		Collection<CampBattle> campBattles = this.campBattles.values();
		if(campBattles != null){
			List<Long> playerIds = new LinkedList<Long>();
			for(CampBattle campBattle : campBattles){
				List<UserDomain> applyPlayers = campBattle.getApplyPlayers();
				sortApplyPlayers(applyPlayers);
				synchronized (applyPlayers) {
					List<UserDomain> pageResult = Tools.pageResult(applyPlayers, 0, BATTLEFIELD_MAX_CAMP_PLAYERS);
					if(pageResult != null){
						for(UserDomain userDomain : pageResult){
							playerIds.add( userDomain.getPlayerId() );
						}
					}
				}
				
			}
			CampBattlePushHelper.pushCampBattleCmd(playerIds, 1);
		}
		
	}
	
	/** 开始本次阵营战  */
	@Scheduled(name="开始阵营战", type=ValueType.BEANNAME, value = "CAMPBATTLE_START_TIME")
	protected synchronized void scheduleStartCampBattle(){
		if(BATTLEFIELD_OPEN == null || !BATTLEFIELD_OPEN){
			return ;
		}
		if(!isOpenCampBattle()){
			return ;
		}
		//没有调度报名流程时
		if(this.campBattleStatus.get() == CampBattleRule.STATUS_INIT){
			scheduleStartApplyCampBattle();
		}
		if( campBattleStatus.compareAndSet( CampBattleRule.STATUS_APPLYER_ENTER, CampBattleRule.STATUS_START) ){
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			this.campBattleDate = calendar.getTime();
			this.round =  this.roundBattleDates.indexOf(this.campBattleDate ) + 1;
			if(this.round <= 0 ){
				this.fillRoundBattleDates();
				int index = this.roundBattleDates.indexOf( this.campBattleDate );
				this.round =  index < 0 ? this.roundBattleDates.size() : index + 1;
			}
			this.last = this.round >= this.roundBattleDates.size();
					
			//结束阵营战
			calendar.add(Calendar.MINUTE, GameConfig.getCampBattleTimeout() );
			final Date stopTime = calendar.getTime();
			this.campBattleEndTime = stopTime;
			campBattleStopFuture = scheduler.schedule(new ScheduledTask() {
				
				public void run() {
					if(campBattleStatus.compareAndSet(CampBattleRule.STATUS_START, CampBattleRule.STATUS_SYS_CALC)){
						handleStopCampBattle();
					}
				}
				
				public String getName() { return "阵营战结束线程"; }
			}, stopTime );
			
			//发布一些公告等
			Thread newThread = factory.newThread(HANDLER_BATTLE_INFO_TASK);
			newThread.start();
			
			//人数不够时邀请加入,	推送已经在战场中玩家转场
			sendBattleCmd(2);
		}
	}
	
	private void sendBattleCmd(int cmd){
		Collection<CampBattle> campBattles = this.campBattles.values();
		if(campBattles != null){
			for(CampBattle campBattle : campBattles){
				Camp camp = campBattle.getCamp();
				if(camp == null || camp == Camp.NONE){
					continue;
				}
				
				Set<PlayerCampBattle> playersCampBattles = campBattle.getPlayers();
				Channel channels = Channel.valueOf(ChatChannel.CAMP_CHANNEL.ordinal(), camp.ordinal());
				Collection<Long> players = channelFacade.getChannelPlayers(channels);
				Collection<Long> playerIds = new HashSet<Long>();
				if(players != null){
					for(Long playerId : players){
						UserDomain userDomain = userManager.getUserDomain(playerId);
						if( userDomain == null ){
							continue;
						}
						PlayerBattle battle = userDomain.getBattle();
						if(battle.getLevel() < CampBattleRule.APPLY_MIN_LEVEL ){
							continue;
						}
						if(this.playerCampBattleMap.containsKey(playerId)){
							continue;
						}
						if(cmd == 2){
							if(playersCampBattles.size() < CampBattleRule.NEED_INVITE_MIN_CAMP_PLAYERS ){
								playerIds.add(playerId);
								continue;
							}
						} else if(cmd == 4){
							PlayerCampBattleRecord playerCampBattleRecord = this.campBattleManager.getPlayerCampBattleRecord(playerId);
							if(playerCampBattleRecord == null || playerCampBattleRecord.getCampTitle() != CampTitle.NONE){
								continue;
							}
						}
					}
				}
				
				CampBattlePushHelper.pushCampBattleCmd(playerIds, cmd);
			}
		}
		
	}
	
	/**
	 * 处理阵营战结束
	 */
	private synchronized void handleStopCampBattle(){
		if(campBattles == null || this.campBattleDate == null){
			return ;
		}
		if(this.campBattleStatus.get() != CampBattleRule.STATUS_SYS_CALC){
			return ;
		}
	
		List<CampBattle> campBattleList = new ArrayList<CampBattle>( this.campBattles.values() );
		Collections.sort(campBattleList);
		if(campBattleList != null && campBattleList.size() > 0){
			CampBattle campBattle = campBattleList.get(0);
			synchronized (campBattle) {
				campBattle.setWin(true);
			}
			
			//推送胜利消息
			CampBattlePushHelper.pushBattleInfo(this.campBattles.values(), noCampPointId, true);
		}
		
		Collection<PlayerCampBattleRecord> playerCampBattleRecords = new ArrayList<PlayerCampBattleRecord>();
		Collection<PlayerCampBattle> savePlayers = this.playerCampBattleMap.values();
		for(PlayerCampBattle playerCampBattle : savePlayers){
			if(playerCampBattle.getStatus().ordinal() < PlayerCampBattleStatus.ENTER.ordinal()){
				this.playerCampBattleMap.remove(playerCampBattle.getPlayerId());
				continue ;
			}
			//没有锁同步
			PlayerCampBattleRecord playerCampBattleRecord = this.campBattleManager.getPlayerCampBattleRecord( playerCampBattle.getPlayerId() );
			playerCampBattleRecord.increaseTotalScore( playerCampBattle.getTotalScores() );
			playerCampBattleRecords.add( playerCampBattleRecord );
		}
		
		try {
			//计算阵营称号
			this.calcCampTitles(campBattleList, playerCampBattleRecords);
		} catch (Exception ex){
			logger.error("{}", ex);
		}
		
		try {
			//保存战场数据，一切都成为历史
			this.campBattleManager.saveCampBattleInfo( this.campBattleDate, campBattles.values(), savePlayers, playerCampBattleRecords);
		} catch (Exception ex){
			logger.error("{}", ex);
		}
		
		List<Date> campBattleDates = this.campBattleManager.getCampBattleDates();
		if(campBattleDates != null){
			synchronized (campBattleDates) {
				campBattleDates.add(0, this.campBattleDate);
				if(campBattleDates.size() > CampBattleRule.CAMP_BATTLE_RECORD_FETCH_COUNT){
					campBattleDates.remove( campBattleDates.size() -1 );	//删除最后一个
				}
			}
		}
		cachedService.removeFromCommonCache(CampBattleManagerImpl.CAMP_BATTLE_PREFIX);
		cachedService.removeFromCommonCache(CampBattleManagerImpl.PLAYER_CAMP_BATTLE_PREFIX);
		cachedService.removeFromCommonCache(CampBattleManagerImpl.CAMP_BATTLE_LEADER_IDS);
//		cachedService.removeFromCommonCache(CampBattleManagerImpl.PLAYER_CAMP_BATTLE_TITLE_PREFIX);
		
//		this.preCampBattleDate = this.campBattleDate;
//		this.campBattleStopFuture = null;
//		this.campBattleDate = null;
		
		//等待其他玩家退出
		scheduler.scheduleWithDelay(new ScheduledTask() {
			
			public void run() {
				clearBattle();
			}
			
			public String getName() { return "阵营战结束"; }
		}, CampBattleRule.CLEAR_MAP_CD_TIME);
		
		campBattleStatus.set(CampBattleRule.STATUS_INIT);
	}
	
	/**
	 * 计算阵营称号
	 */
	public synchronized void calcCampTitles(List<CampBattle> campBattleList, Collection<PlayerCampBattleRecord> playerCampBattleRecords){
		if( this.last ){
			try {
				//重置所有在线玩家的阵营称号
				this.campBattleManager.clearCampTitles();
			} catch (Exception e){
				logger.error("{}", e);
			}
		}
		
		Set<Long> playerIds = new HashSet<Long>();
//		cachedService.removeFromCommonCache(CampBattleManagerImpl.PLAYER_CAMP_BATTLE_SCORE_PREFIX);
		NoticeVo[] noticeVos = new NoticeVo[Camp.values().length - 1];
		for(CampBattle campBattle : campBattleList){
			Camp camp = campBattle.getCamp();
			List<Long> playerTotalScoreList = this.campBattleManager.getPlayerTotalScoreList(camp);
			//排序
			sortPlayerTotalScoreList(playerTotalScoreList);
			
			if( !this.last ){	//不是最后一场
				continue ;
			}
			campBattle.setLast(true);
			
			//上场有称号的玩家
			List<Long> preCampTitlePlayers = this.campBattleManager.getCampTitlePlayers(camp);
			if(preCampTitlePlayers != null){
				playerIds.addAll( preCampTitlePlayers );
			}
			
			//重置所有在线玩家的阵营称号,  不需要入库，已经刷库了
			Channel channels = Channel.valueOf(ChatChannel.CAMP_CHANNEL.ordinal(), camp.ordinal());
			Collection<Long> players = channelFacade.getChannelPlayers(channels);
			for(long playerId : players){
				UserDomain userDomain = userManager.getUserDomain(playerId);
				if(userDomain == null){
					continue ;
				}
				PlayerBattle playerBattle = userDomain.getBattle();
				if(playerBattle.getLevel() < CampBattleRule.APPLY_MIN_LEVEL){
					continue ;
				}
				
				PlayerCampBattleRecord playerCampBattleRecord = campBattleManager.getPlayerCampBattleRecord(playerId);
				ChainLock lock = LockUtils.getLock(playerCampBattleRecord);
				try {
					lock.lock();
					playerCampBattleRecord.setCampTitle(CampTitle.NONE);
					playerCampBattleRecord.setSuitReward(null);
				} finally {
					lock.unlock();
				}
			}
			
			List<Long> newCampTitlePlayers = new ArrayList<Long>(5);
			CampTitle[] campTitles = CampTitle.values();
			for(int i = 0; i < campTitles.length; i++ ){
				CampTitle campTitle = campTitles[i];
				if(campTitle == CampTitle.NONE){
					continue ;
				}
				if(i > playerTotalScoreList.size()){
					continue ;
				}
				Long playerId = playerTotalScoreList.get(i-1);
				PlayerCampBattleRecord playerCampBattleRecord = campBattleManager.getPlayerCampBattleRecord(playerId);
				ChainLock lock = LockUtils.getLock(playerCampBattleRecord);
				try {
					lock.lock();
					playerCampBattleRecord.setCampTitle( campTitle );
					playerCampBattleRecord.setSuitReward(null);
					playerIds.add(playerId);
				} finally {
					lock.unlock();
				}
				PlayerCampBattle playerCampBattle = this.playerCampBattleMap.get(playerId);
				if(playerCampBattle != null) {
					playerCampBattle.setCampTitle(campTitle);
					playerCampBattle.setLast(true);
				}
				newCampTitlePlayers.add(playerId);
				dbService.updateEntityIntime(playerCampBattleRecord);
			}
			
			synchronized (preCampTitlePlayers) {
				preCampTitlePlayers.clear();
				preCampTitlePlayers.addAll(newCampTitlePlayers);
			}
			
			//构造新盟主公告
			int noticeId = 0; 
			if(camp == Camp.KNIFE_CAMP){
				noticeId = NoticeID.KNIFE_CAMP_LEADER;
			} else if(camp == Camp.SWORD_CAMP){
				noticeId = NoticeID.SWORD_CAMP_LEADER;
			}
			if(noticeId > 0) {
				Map<String, Object> params = new HashMap<String, Object>(1);
				Long playerId = playerTotalScoreList.get(0);
				UserDomain userDomain = userManager.getUserDomain(playerId);
				params.put( NoticeRule.playerName, userDomain.getPlayer().getName() );
				noticeVos[camp.ordinal() - 1] = NoticeVo.valueOf(noticeId, NoticeType.HONOR, params, 1) ;
			}
		}
		
		if( playerIds.size() > 0 ){
			//推送阵营战官衔更新
			rankManager.freshRankTitle(playerIds);
			//推送新阵营盟主公告
			NoticePushHelper.pushNotice(sessionManager.getOnlinePlayerIdList(), noticeVos );
		}
	}

	Comparator<Long> battleRecordComparator = new Comparator<Long>() {
		
		public int compare(Long id1, Long id2) {
			PlayerCampBattleRecord o1 = campBattleManager.getPlayerCampBattleRecord(id1);
			PlayerCampBattleRecord o2 = campBattleManager.getPlayerCampBattleRecord(id2);
			int totalScore1 = o1.getTotalScore();
			int totalScore2 = o2.getTotalScore();
			if(totalScore1 > totalScore2){
				return -1;
			} else if(totalScore1 < totalScore2){
				return 1;
			}
			
//			UserDomain userDomain1 = userManager.getUserDomain(o1.getId());
//			UserDomain userDomain2 = userManager.getUserDomain(o2.getId());
//			int fightCapacity1 = userDomain1.getBattle().getAttribute(AttributeKeys.PLAYER_FIGHT_TOTAL_CAPACITY);
//			int fightCapacity2 = userDomain2.getBattle().getAttribute(AttributeKeys.PLAYER_FIGHT_TOTAL_CAPACITY);
//			if(fightCapacity1 > fightCapacity2){
//				return -1;
//			} else if(fightCapacity1 < fightCapacity2){
//				return 1;
//			}
			
			int joins1 = o1.getJoins();
			int joins2 = o2.getJoins();
			if(joins1 < joins2){
				return -1;
			} else if(joins1 > joins2){
				return 1;
			}
			
			if(id1 < id2){
				return -1;
			} else if(id1 > id2){
				return 1;
			}
			return 0;
		}
	};

	private void clearBattle(){
//		campBattleStatus.set(CampBattleRule.STATUS_INIT);
		
//		强制玩家退出阵营战场
		Collection<PlayerCampBattle> players = this.playerCampBattleMap.values();
		CampBattlePushHelper.pushForseLeaveCampBattle(players);
		
		this.campBattleEndTime = null;
		this.campBattles = null;
		this.playerCampBattleMap.clear();
		this.noCampPointId.clear();
		Collection<GameMap> gameMaps = this.campBattleGameMaps.values();
		for(GameMap gameMap : gameMaps){
			gameMap.clear();
		}
		this.campBattleGameMaps.clear();
		this.noticeMap.clear();
	}

	
	public long apply(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return CampBattleConstant.PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		Camp camp = player.getCamp();
		if(camp == null || camp == Camp.NONE){
			return CampBattleConstant.MUST_HAD_CAMP;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle.getLevel() < CampBattleRule.APPLY_MIN_LEVEL){
			return CampBattleConstant.JOIN_LEVEL_LIMIT;
		}
		
		CampBattle campBattle = getCampBattle(camp);
		if(campBattle == null){
			Date nextApplyDate = applyTimeGenerator.next(new Date());
			return nextApplyDate.getTime();
		}
		
		if(campBattleStatus.get() < CampBattleRule.STATUS_APPLY){
			return CampBattleConstant.BATTLE_NOT_START;
		}
		if(campBattleStatus.get() > CampBattleRule.STATUS_APPLYER_ENTER){
			return CampBattleConstant.APPLY_IS_DONE;
		}
		
		PlayerCampBattle playerCampBattle = playerCampBattleMap.get(playerId);
		if(playerCampBattle != null && playerCampBattle.getPreCampTitle() != CampTitle.NONE){
			return CampBattleConstant.HAD_CAMP_TITLE;
		}
		
		PlayerCampBattle exist = playerCampBattleMap.putIfAbsent(playerId, PlayerCampBattle.valueOf(userDomain, CampTitle.NONE, this.last) );
		List<UserDomain> applyPlayers = campBattle.getApplyPlayers();
		if(exist == null){
			synchronized (applyPlayers) {
				applyPlayers.add(userDomain);
			}
			sortApplyPlayers(applyPlayers);
		} else {
			return CampBattleConstant.HAD_APPLY_JOIN_BATTLE;
		}
		
		return CampBattleConstant.SUCCESS;
	}
	
	
	public int getApplyStatus(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return CampBattleConstant.PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		Camp camp = player.getCamp();
		if(camp == null || camp == Camp.NONE){
			return CampBattleConstant.MUST_HAD_CAMP;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle.getLevel() < CampBattleRule.APPLY_MIN_LEVEL){
			return CampBattleConstant.JOIN_LEVEL_LIMIT;
		}
		
		if(campBattleStatus.get() < CampBattleRule.STATUS_APPLY){
			return CampBattleConstant.BATTLE_NOT_START;
		}
		if(campBattleStatus.get() >= CampBattleRule.STATUS_SYS_CALC){
			return CampBattleConstant.BATTLE_IS_OVER;
		}
		
		CampBattle campBattle = getCampBattle(camp);
		if(campBattle == null){
			return CampBattleConstant.BATTLE_NOT_START;
		}
		
		PlayerCampBattle playerCampBattle = playerCampBattleMap.get(playerId);
		if(playerCampBattle != null && playerCampBattle.getPreCampTitle() != CampTitle.NONE){
			return CampBattleConstant.HAD_CAMP_TITLE;
		}
		
		List<UserDomain> applyPlayers = campBattle.getApplyPlayers();
		synchronized (applyPlayers) {
			return applyPlayers.indexOf(userDomain) > -1 ? 1 : 0;
		}
	}

	/**
	 * 取得阵营战信息对象
	 * @param camp
	 * @return
	 */
	private CampBattle getCampBattle(Camp camp){
		return campBattles != null && camp != Camp.NONE ? campBattles.get( camp ): null;
	}

	
	public Collection<Integer> getOwnCampBattlePoints(Camp camp) {
		CampBattle campBattle = getCampBattle(camp);
		if(campBattle != null){
			return new ArrayList<Integer>( campBattle.getOwnPointIds() );
		}
		return null;
	}

	
	public int getApplyPlayers(Long playerId, int pageSize, int pageNow, Map<String, Object> resultMap) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return CampBattleConstant.PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		Camp camp = player.getCamp();
		if(camp == null || camp == Camp.NONE){
			return CampBattleConstant.MUST_HAD_CAMP;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle.getLevel() < CampBattleRule.APPLY_MIN_LEVEL){
			return CampBattleConstant.JOIN_LEVEL_LIMIT;
		}
		
		if(campBattleStatus.get() < CampBattleRule.STATUS_APPLY){
			return CampBattleConstant.BATTLE_NOT_START;
		}
		
		if(campBattleStatus.get() == CampBattleRule.STATUS_SYS_CALC){
			return CampBattleConstant.BATTLE_IS_OVER;
		}
		
		CampBattle campBattle = getCampBattle(camp);
		if(campBattle == null){
			return CampBattleConstant.BATTLE_NOT_START;
		}
		
		if(pageNow <= 0){
			pageNow = 1;
		}
		
		List<UserDomain> applyPlayers = campBattle.getApplyPlayers();
		List<UserDomain> sortApplyPlayers = sortApplyPlayers(applyPlayers);
		
		int pageCount = (sortApplyPlayers.size() + pageSize - 1) / pageSize ;
		//分页数
		resultMap.put(ResponseKey.COUNT, pageCount );
		if(pageNow > pageCount){
			pageNow = pageCount;
		}
		
		List<UserDomain> pageResult = Tools.pageResult(sortApplyPlayers, (pageNow - 1) * pageSize, pageSize);
		if(pageResult != null && pageResult.size() > 0){
			List<ApplyVO> list = new ArrayList<ApplyVO>( pageResult.size() );
//			if(hasNewApply.get()){
//				Collections.sort(pageResult, applyPlayersComparator());
//			}
			for(UserDomain pDomain : pageResult){
				long pId = pDomain.getId();
				ApplyVO applyVO = new ApplyVO();
				if(pDomain != null){
					PlayerBattle pBattle = pDomain.getBattle();
					Player pPlayer = pDomain.getPlayer();
					applyVO.setPlayerId(pId);
					applyVO.setPlayerName(pPlayer.getName());
					applyVO.setLevel(pBattle.getLevel());
					applyVO.setJob(pBattle.getJob() );
					applyVO.setFightCapacity(pBattle.getAttribute(AttributeKeys.PLAYER_FIGHT_TOTAL_CAPACITY));
					
					Alliance alliance = allianceManager.getAlliance4PlayerId(pId);
					if(alliance != null){
						applyVO.setAllianceName(alliance.getName());
					}
					
					list.add(applyVO);
				}
			}
			resultMap.put(ResponseKey.VALUES, list.toArray());
		}
		
		resultMap.put(ResponseKey.PAGE_SIZE, pageSize );
		resultMap.put(ResponseKey.PAGE_NOW, applyPlayers.isEmpty() ? 0 : pageNow );
		
		return CampBattleConstant.SUCCESS;
	}

	/**
	 * 申请都排序
	 * @param applyPlayers
	 */
	private List<UserDomain> sortApplyPlayers(List<UserDomain> applyPlayers) {
		synchronized (applyPlayers) {
			Collections.sort(applyPlayers, applyPlayersComparator());
			return new ArrayList<UserDomain>(applyPlayers);
		}
	}
	
	private void sortPlayerTotalScoreList(List<Long> ids) {
			synchronized (ids) {
				List<Long> list = new ArrayList<Long>(ids);
				Collections.sort(list, battleRecordComparator);
				ids.clear();
				ids.addAll(list);
			}
	}

	private Comparator<UserDomain> applyPlayersComparator() {
		return new Comparator<UserDomain>() {
			
			public int compare(UserDomain o1, UserDomain o2) {
				int attribute = o1.getBattle().getAttribute(AttributeKeys.PLAYER_FIGHT_TOTAL_CAPACITY);
				int attribute2 = o2.getBattle().getAttribute(AttributeKeys.PLAYER_FIGHT_TOTAL_CAPACITY);
				if(attribute > attribute2){
					return -1;
				} else if(attribute < attribute2){
					return 1;
				}
				long id = o1.getId();
				long id2 = o2.getId();
				if(id < id2){
					return -1;
				} else if(id > id2){
					return 1;
				}
				return 0;
			}
		};
	}

	
	public int adjustApplyPlayerPriority(Long playerId, long targetId, byte op, int pageSize, int pageNow, Map<String, Object> resultMap) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return CampBattleConstant.PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		Camp camp = player.getCamp();
		if(camp == null || camp == Camp.NONE){
			return CampBattleConstant.MUST_HAD_CAMP;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle.getLevel() < CampBattleRule.APPLY_MIN_LEVEL){
			return CampBattleConstant.JOIN_LEVEL_LIMIT;
		}
		
		UserDomain targetUserDomain = userManager.getUserDomain(targetId);
		if(targetUserDomain == null){
			return CampBattleConstant.PLAYER_NOT_FOUND;
		}
		
		if(campBattleStatus.get() < CampBattleRule.STATUS_APPLY){
			return CampBattleConstant.BATTLE_NOT_START;
		} else if(campBattleStatus.get() == CampBattleRule.STATUS_APPLYER_ENTER){
			return CampBattleConstant.SOME_BODY_ENTER;
		}

		//先屏蔽掉， 方便测试，提交时再取消注释
		PlayerCampBattle playerCampBattle = this.playerCampBattleMap.get(playerId);
		if(playerCampBattle == null || playerCampBattle.getPreCampTitle() != CampTitle.LEADER){
			return CampBattleConstant.NO_RIGHT;
		}
		
		PlayerCampBattle targetPlayerCampBattle = this.playerCampBattleMap.get(targetId);
		if(targetPlayerCampBattle == null){
			return CampBattleConstant.PLAYER_NOT_FOUND;
		}
//
//		CampBattle campBattle = getCampBattle(camp);
//		CopyOnWriteArrayList<UserDomain> applyPlayers = campBattle.getApplyPlayers();
		if(op == -1){			//置顶
//			if( applyPlayers.remove(userDomain) ){		//找到目标
//				applyPlayers.add(0, userDomain);
//			} else {
//				return CampBattleConstant.PLAYER_NOT_FOUND;
//			}
			
		} else if(op == 1){		//置底
//			synchronized (applyPlayers) {
//				if( applyPlayers.remove(userDomain) ){	//找到目标
//					applyPlayers.add(userDomain);
//				} else {
//					return CampBattleConstant.PLAYER_NOT_FOUND;
//				}
//			}
			
		} else {
			return CampBattleConstant.INPUT_VALUE_INVALID;
		}
		
		return getApplyPlayers(playerId, pageSize, pageNow, resultMap);
	}

	
	public int getPlayerScores(Long playerId, int pageSize, int pageNow, Map<String, Object> resultMap) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return CampBattleConstant.PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		Camp camp = player.getCamp();
		if(camp == null || camp == Camp.NONE){
			return CampBattleConstant.MUST_HAD_CAMP;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle.getLevel() < CampBattleRule.APPLY_MIN_LEVEL){
			return CampBattleConstant.JOIN_LEVEL_LIMIT;
		}
		
		CampBattle campBattle = getCampBattle(camp);
		if(campBattle == null){
			return CampBattleConstant.BATTLE_NOT_START;
		}
		
		PlayerCampBattle playerCampBattle = playerCampBattleMap.get(playerId);
		if(playerCampBattle == null){
			return CampBattleConstant.NOT_IN_CAMP_BATTLE;
		}
		
		if(pageNow <= 0){
			pageNow = 1;
		}
		
		int totalScore = 0;
		Set<PlayerCampBattle> players = campBattle.getPlayers();
		List<PlayerCampBattle> list = null;
		synchronized (players) {
			list = new ArrayList<PlayerCampBattle>( players );
			totalScore = playerCampBattle.getTotalScores();
		}
		
		int pageCount = (list.size() + pageSize - 1) / pageSize ;
		//分页数
		resultMap.put(ResponseKey.COUNT, pageCount );
		if(pageNow > pageCount){
			pageNow = pageCount;
		}
		
		//排序
		Collections.sort(list);
		
		List<PlayerCampBattle> pageResult = Tools.pageResult(list, (pageNow - 1) * pageSize, pageSize);
		if(pageResult != null && pageResult.size() > 0){
			List<PlayerBattleVO> resultList = new ArrayList<PlayerBattleVO>( pageResult.size() );
			for(PlayerCampBattle pCampBattle : pageResult){
				PlayerBattleVO playerBattleVO = null;
				Long pId = pCampBattle.getPlayerId();
				UserDomain pDomain = userManager.getUserDomain( pId );
				if(pDomain != null){
					playerBattleVO = PlayerBattleVO.valueOf(pCampBattle, this.campBattleDate);
					resultList.add(playerBattleVO);
				}
			}
			resultMap.put(ResponseKey.VALUES, resultList.toArray() );
		}
		
		int index = list.indexOf(playerCampBattle);
		
		resultMap.put("rank", index + 1);
		resultMap.put("scores", totalScore );
		resultMap.put(ResponseKey.PAGE_SIZE, pageSize);
		resultMap.put(ResponseKey.PAGE_NOW, list.isEmpty() ? 0 : pageNow );
		
		return CampBattleConstant.SUCCESS;
	}

	
	public ResultObject<ChangeScreenVo> enterCampBattle(final Long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(CampBattleConstant.PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		Camp camp = player.getCamp();
		if(camp == null || camp == Camp.NONE){
			return ResultObject.ERROR(CampBattleConstant.MUST_HAD_CAMP);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle.getLevel() < CampBattleRule.APPLY_MIN_LEVEL){
			return ResultObject.ERROR(CampBattleConstant.JOIN_LEVEL_LIMIT);
		}
		
		CampBattle campBattle = getCampBattle(camp);
		if(this.campBattleStatus.get() >= CampBattleRule.STATUS_SYS_CALC){
			return ResultObject.ERROR(CampBattleConstant.BATTLE_IS_OVER); 
		}
		if(this.campBattleStatus.get() <= CampBattleRule.STATUS_APPLY){
			return ResultObject.ERROR(CampBattleConstant.BATTLE_NOT_START); 
		}
		PlayerCampBattle playerCampBattle = this.playerCampBattleMap.get(playerId);
		if(this.campBattleStatus.get() == CampBattleRule.STATUS_APPLYER_ENTER){
			if(playerCampBattle != null && playerCampBattle.getPreCampTitle() == CampTitle.NONE){
				List<UserDomain> applyPlayers = campBattle.getApplyPlayers();
				List<UserDomain> sortApplyPlayers = this.sortApplyPlayers(applyPlayers);
				List<Long> campTitlePlayers = campBattleManager.getCampTitlePlayers(camp);
				int num = campTitlePlayers != null ? BATTLEFIELD_MAX_CAMP_PLAYERS - campTitlePlayers.size() : BATTLEFIELD_MAX_CAMP_PLAYERS;
				if(sortApplyPlayers.indexOf(userDomain) >= num){
					return ResultObject.ERROR(CampBattleConstant.HAVE_NOT_PRIORITY_ENTER);
				}
			}
		}
		
		CampPointConfig campPointConfig = resourceService.getByUnique(IndexName.CAMP_TYPE_POINT, CampPointConfig.class, 0, 0, camp.ordinal() );
		if(campPointConfig == null){
			return ResultObject.ERROR(CampBattleConstant.BASEDATA_NOT_FOUND);
		}
		
		GameMap targetGameMap = this.campBattleGameMaps.get(this.globleBranch);
		if(targetGameMap == null){
			return ResultObject.ERROR(CampBattleConstant.FAILURE);
		}
		
		PlayerDungeon playerDungeon = dungeonFacade.getPlayerDungeon(playerId);
		if(playerDungeon != null && playerDungeon.isDungeonStatus()){
			return ResultObject.ERROR(CampBattleConstant.FAILURE);
		}
		
		UserEscortTask userEscortTask = escortTaskManager.getEscortTask(battle);
		if(userEscortTask != null && !userEscortTask.isNoTask()){ //当玩家为运镖状态时,无需道具就能复活
			return ResultObject.ERROR(CampBattleConstant.FAILURE);
		}
		
		if(battleFieldFacade.isInBattleField(playerId)){
			return ResultObject.ERROR(CampBattleConstant.FAILURE);
		}
		
//		if(this.campBattleStatus.get() == CampBattleRule.STATUS_START){
			Set<PlayerCampBattle> players = campBattle.getPlayers();
			synchronized (players) {
				if(playerCampBattle == null || !players.contains(playerCampBattle) ){
					if(players.size() >= BATTLEFIELD_MAX_CAMP_PLAYERS){
						return ResultObject.ERROR(CampBattleConstant.BATTLE_PLAYER_FULLED);
					}
					if(playerCampBattle == null){
						playerCampBattleMap.putIfAbsent(playerId, PlayerCampBattle.valueOf(userDomain, CampTitle.NONE, this.last) );
						playerCampBattle = this.playerCampBattleMap.get(playerId);
					}
					players.add(playerCampBattle);
				}
				playerCampBattle.setStatus(PlayerCampBattleStatus.ENTER);
			}
//		}
		if(userDomain.getMapId() == BattleFieldRule.BATTLE_FIELD_MAPID){
			battleFieldFacade.exitBattleField(playerId);
		}
		
		PlayerCampBattleRecord playerCampBattleRecord = this.campBattleManager.getPlayerCampBattleRecord(playerId);
		Date battleDate = this.campBattleDate;
		if(battleDate == null){
			battleDate = this.round > 0 && this.round <= this.roundBattleDates.size() ? this.roundBattleDates.get(this.round - 1) : null;
		}
		if(battleDate == null){
			return ResultObject.ERROR(CampBattleConstant.BATTLE_NOT_START);
		}
		if( !battleDate.equals(playerCampBattleRecord.getBattleDate()) ){
			ChainLock lock = LockUtils.getLock(playerCampBattleRecord);
			try {
				lock.lock();
				int joins = playerCampBattleRecord.getJoins();
				playerCampBattleRecord.setBattleDate(battleDate);
				++joins;
				if(joins > this.roundBattleDates.size() && this.roundBattleDates.size() > 0){
					joins = this.roundBattleDates.size();
				}
				playerCampBattleRecord.setJoins(joins);
			} finally {
				lock.unlock();
			}
			dbService.submitUpdate2Queue(playerCampBattleRecord);
		}
		
		List<Long> playerTotalScoreList = this.campBattleManager.getPlayerTotalScoreList(camp);
		if( !playerTotalScoreList.contains(playerId) ){
			playerTotalScoreList.add(playerId);
		}
		
		ChangeScreenVo changeScreenVo = mapFacade.leaveMap(userDomain, targetGameMap, campPointConfig.getX(), campPointConfig.getY() );
		if(changeScreenVo == null){
			return ResultObject.ERROR(CampBattleConstant.FAILURE);
		}
		
		return ResultObject.SUCCESS(changeScreenVo);
	}

	
	public ResultObject<ChangeScreenVo> existCampBattle(Long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(CampBattleConstant.PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		Camp camp = player.getCamp();
		if(camp == null || camp == Camp.NONE){
			return ResultObject.ERROR(CampBattleConstant.MUST_HAD_CAMP);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle.getLevel() < CampBattleRule.APPLY_MIN_LEVEL){
			return ResultObject.ERROR(CampBattleConstant.JOIN_LEVEL_LIMIT);
		}
		
		PlayerCampBattle playerCampBattle = this.playerCampBattleMap.get(playerId);
		if(playerCampBattle == null){
			return ResultObject.ERROR(CampBattleConstant.FAILURE);
		}
		
//		if(this.campBattleStatus.get() < CampBattleRule.STATUS_APPLYER_ENTER){
//			return ResultObject.ERROR(CampBattleConstant.FAILURE); 
//		}

		GameMap targetGameMap = gameMapManager.getGameMapById(MapRule.DEFAUL_REVIVE_MAPID, userDomain.getBranching() );
		if(targetGameMap == null){
			return ResultObject.ERROR(CampBattleConstant.FAILURE);
		}
		
		Point point = targetGameMap.getRandomCanStandPoint(MapRule.DEFAUL_REVIVE_X, MapRule.DEFAUL_REVIVE_Y, 15);
		int targetX = MapRule.DEFAUL_REVIVE_X, targetY = MapRule.DEFAUL_REVIVE_Y;
		if(point != null){
			targetX = point.getX();
			targetY = point.getY();
		}
		
		ChangeScreenVo changeScreenVo = mapFacade.leaveMap(userDomain, targetGameMap, targetX, targetY);
		if(changeScreenVo == null){
			return ResultObject.ERROR(CampBattleConstant.FAILURE);
		}
		
		//不在战场里
		if(this.getCampBattleStatus() != CampBattleRule.STATUS_SYS_CALC){
//			synchronized (playerCampBattle) {
//				playerCampBattle.reset();
//			}
			
			CampBattle campBattle = getCampBattle(camp);
			if(campBattle != null){
				Set<PlayerCampBattle> players = campBattle.getPlayers();
				synchronized (players) {
					players.remove(playerCampBattle);
				}
			}
		}
		
		return ResultObject.SUCCESS(changeScreenVo);
	}

	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		long playerId = messageInfo.getPlayerId();
		if( !sessionManager.isOnline(playerId) ){
			//退出的战场信息
			existCampBattle(playerId);
		}
	}
	
	
	
	
	public CampTitle getCampBattleTitle(long playerId, Date campDate) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null ){
			return CampTitle.NONE;
		}
		Player player = userDomain.getPlayer();
		Camp camp = player.getCamp();
		if(camp == null || camp == Camp.NONE){
			return CampTitle.NONE;
		}
		if(userDomain.getBattle().getLevel() < CampBattleRule.APPLY_MIN_LEVEL){
			return CampTitle.NONE;
		}
		PlayerCampBattleRecord playerCampBattleRecord = campBattleManager.getPlayerCampBattleRecord(playerId);
		if(playerCampBattleRecord == null){
			return CampTitle.NONE;
		}
		return playerCampBattleRecord.getCampTitle();
	}

	
	public PlayerCampBattleHistory getPlayerCampBattleHistory(Date date, long playerId) {
		if(date == null){
			date = this.preCampBattleDate;
		}
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return null;
		}
		
		Player player = userDomain.getPlayer();
		Camp camp = player.getCamp();
		if(date == null || camp == null || camp == Camp.NONE){
			return null;
		}
		
		return this.campBattleManager.getPlayerCampBattleHistory(playerId, date, camp);
	}

	
	public CampBattleHistory getCampBattleHistory(Date date, Camp camp) {
		if(date == null || camp == null || camp == Camp.NONE){
			return null;
		}
		return campBattleManager.getCampBattleHistory(date, camp);
	}

	@SuppressWarnings("unchecked")
	
	public int salary(Long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return CampBattleConstant.PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		Camp camp = player.getCamp();
		if(camp == null || camp == Camp.NONE){
			return CampBattleConstant.MUST_HAD_CAMP;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle.getLevel() < CampBattleRule.APPLY_MIN_LEVEL){
			return CampBattleConstant.JOIN_LEVEL_LIMIT;
		}
		
		PlayerCampBattleRecord playerCampBattleRecord = this.campBattleManager.getPlayerCampBattleRecord(playerId);
		if(playerCampBattleRecord == null){
			return CampBattleConstant.PLAYER_NOT_FOUND;
		}
		
		CampTitle campTitle = playerCampBattleRecord.getCampTitle();
		if(campTitle == null || campTitle == CampTitle.NONE){
			return CampBattleConstant.CAMPTITLE_LIMIT;
		}
		
//		if(this.campBattleStatus.get() != CampBattleRule.STATUS_INIT  && campBattleDate != null && DateUtil.isToday(this.campBattleDate) ){
//			return CampBattleConstant.CAMP_BATTLE_NOT_OVER;
//		}
		
		Date salaryReward = playerCampBattleRecord.getSalaryReward();
		if(salaryReward != null && DateUtil.isToday(salaryReward) ){
			return CampBattleConstant.SALARY_REWARDED;
		}
		
		CampTitleRewards campBattleTitleRewards = campBattleService.getCampBattleTitleRewards(camp, playerCampBattleRecord.getCampTitle() );
		if(campBattleTitleRewards == null){
			return CampBattleConstant.BASEDATA_NOT_FOUND;
		}
		
		List<UserProps> userPropsList = new ArrayList<UserProps>(1);
		ChainLock lock = LockUtils.getLock(playerCampBattleRecord, player, battle );
		try {
			lock.lock();
			salaryReward = playerCampBattleRecord.getSalaryReward();
			if(salaryReward != null && DateUtil.isToday(salaryReward) ){
				return CampBattleConstant.SALARY_REWARDED;
			}

			int backpack = BackpackType.DEFAULT_BACKPACK;
			Map<Integer, Integer> propsRewards = campBattleTitleRewards.getPropsRewards();
			if(propsRewards != null ){
				for(Entry<Integer, Integer> entry : propsRewards.entrySet() ){
					UserProps userProps =  UserProps.valueOf(playerId, entry.getKey(), entry.getValue(), backpack, null, true);
					userPropsList.add(userProps);
				}
			}
			
			int currentBackSize = propsManager.getBackpackSize(playerId, backpack);
			if(!player.canAddNew2Backpack(currentBackSize + userPropsList.size(), backpack)) {
				return BACKPACK_FULLED;
			}
			playerCampBattleRecord.setSalaryReward(new Date());
			
			propsManager.createUserProps(userPropsList);
			
			if(userPropsList != null && userPropsList.size() > 0){
				propsManager.put2UserPropsIdsList(playerId, backpack, userPropsList);
			}
			
			int addSilver = FormulaHelper.invoke(campBattleTitleRewards.getSilver(), battle.getLevel()).intValue();
			player.increaseSilver( addSilver );
			
			int addExp = FormulaHelper.invoke(campBattleTitleRewards.getExp(), battle.getLevel()).intValue();
			battle.increaseExp( addExp );
			
			dbService.submitUpdate2Queue(player, battle, playerCampBattleRecord);
			
			if(addSilver != 0){
				SilverLogger.inCome(Source.CAMP_BATTLE_TITLE_SALARY, addSilver, player);
			}
			if(addExp != 0){
				ExpLogger.battleFieldExp(userDomain, Source.CAMP_BATTLE_TITLE_SALARY, addExp);
			}
			GoodsLogger.goodsLogger(player, Source.CAMP_BATTLE_TITLE_SALARY, LoggerPropsHelper.convertLoggerGoods(Orient.INCOME, userPropsList, null, null, null));
			
		} finally {
			lock.unlock();
		}
		
		//物品奖励
		MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, userPropsList);
		
		//推送属性
		List<Long> playerIdList = Arrays.asList(player.getId());
		List<UnitId> unitIdList = Arrays.asList(UnitId.valueOf(player.getId(), ElementType.PLAYER));
		UserPushHelper.pushAttribute2AreaMember(player.getId(),playerIdList, unitIdList, AttributeKeys.SILVER, AttributeKeys.EXP);
		
		MessagePushHelper.pushGoodsCountChange2Client(playerId, GoodsVO.valuleOf(userPropsList, null, null, null) );
		
		return CampBattleConstant.SUCCESS;
	}
	
	
	public int suitReward(Long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return CampBattleConstant.PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		Camp camp = player.getCamp();
		if(camp == null || camp == Camp.NONE){
			return CampBattleConstant.MUST_HAD_CAMP;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle.getLevel() < CampBattleRule.APPLY_MIN_LEVEL){
			return CampBattleConstant.JOIN_LEVEL_LIMIT;
		}
		
		PlayerCampBattleRecord playerCampBattleRecord = this.campBattleManager.getPlayerCampBattleRecord(playerId);
		if(playerCampBattleRecord == null){
			return CampBattleConstant.CAMP_BATTLE_HISTORY_NOT_FOUND;
		}
		
		CampTitle campTitle = playerCampBattleRecord.getCampTitle();
		if(campTitle == null || campTitle == CampTitle.NONE){
			return CampBattleConstant.CAMPTITLE_LIMIT;
		}
		
//		if(this.campBattleStatus.get() != CampBattleRule.STATUS_INIT  && campBattleDate != null && DateUtil.isToday(this.campBattleDate) ){
//			return CampBattleConstant.CAMP_BATTLE_NOT_OVER;
//		}
		
		if(playerCampBattleRecord.getSuitReward() != null ){
			return CampBattleConstant.SUIT_REWARDED;
		}
		
		CampTitleRewards campBattleTitleRewards = campBattleService.getCampBattleTitleRewards(camp, campTitle );
		if(campBattleTitleRewards == null){
			return CampBattleConstant.BASEDATA_NOT_FOUND;
		}
		
		List<UserEquip> userEquips = new ArrayList<UserEquip>(1);
		ChainLock lock = LockUtils.getLock(playerCampBattleRecord, player, battle );
		try {
			lock.lock();
			if(playerCampBattleRecord.getSuitReward() != null ){
				return CampBattleConstant.SUIT_REWARDED;
			}
			
			int backpack = BackpackType.DEFAULT_BACKPACK;
			Integer[] suitRewards = campBattleTitleRewards.getSuitRewards();
			if(suitRewards != null){
				Calendar cal = Calendar.getInstance();
				cal.setTime( startTimeGenerator.next(new Date()) );
				int week = cal.get(Calendar.WEEK_OF_YEAR);
				while(true){
					Calendar calendar = Calendar.getInstance();
					calendar.setTime( startTimeGenerator.next(cal.getTime()) );
					if( week != calendar.get(Calendar.WEEK_OF_YEAR) ){
						break;
					}
					cal = calendar;
				};
				
				cal.add(Calendar.MINUTE, GameConfig.getCampBattleTimeout() );
				Date expirationDate = cal.getTime();
				
				for(Integer suit : suitRewards){
					EquipConfig equipConfig = resourceService.get(suit, EquipConfig.class);
					if(equipConfig != null && equipConfig.getSex() == player.getSex().ordinal() ){
						UserEquip userEquip = EquipHelper.newUserEquip(playerId, equipConfig, backpack, true);
						userEquip.setExpiration(expirationDate);
						userEquips.add(userEquip);
						break;
					}
				}
			}
			
			int currentBackSize = propsManager.getBackpackSize(playerId, backpack);
			if(!player.canAddNew2Backpack(currentBackSize + userEquips.size(), backpack)) {
				return BACKPACK_FULLED;
			}
			
			propsManager.createUserEquip(userEquips);
			if(userEquips != null && userEquips.size() > 0){
				propsManager.put2UserEquipIdsList(playerId, backpack, userEquips);
			}
			
			playerCampBattleRecord.setSuitReward(new Date());
			dbService.submitUpdate2Queue(playerCampBattleRecord);
			
			GoodsLogger.goodsLogger(player, Source.CAMP_BATTLE_TITLE_SALARY, LoggerPropsHelper.convertLoggerGoods(Orient.INCOME, null, userEquips, null, null));
		} finally {
			lock.unlock();
		}
		
		//物品奖励
		MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, userEquips);
		MessagePushHelper.pushGoodsCountChange2Client(playerId, GoodsVO.valuleOf(null, null, null, userEquips) );
		
		return CampBattleConstant.SUCCESS;
	}

	
	public int getCampTitlePlayers(int campValue, Map<String, Object> resultMap) {
		Camp camp = EnumUtils.getEnum(Camp.class, campValue);
		if(camp == null || camp == Camp.NONE){
			return CampBattleConstant.MUST_HAD_CAMP;
		}
		
		List<PlayerCampBattleRecord> campTitlePlayers = getCampTitlePlayers(camp);
		if(campTitlePlayers != null){
			List<PlayerBattleVO> vos = new ArrayList<PlayerBattleVO>( campTitlePlayers.size() );
			for(PlayerCampBattleRecord record: campTitlePlayers){
				long playerId = record.getId();
				UserDomain userDomain = userManager.getUserDomain(playerId);
				PlayerBattleVO vo = new PlayerBattleVO();
				vo.setPlayerId(playerId);
				vo.setPlayerName(userDomain.getPlayer().getName());
				vo.setCampTitle(record.getCampTitle());
				Alliance alliance = allianceManager.getAlliance4PlayerId(playerId);
				if(alliance != null){
					vo.setAllianceName(alliance.getName());
				}
				vos.add( vo );
			}
			resultMap.put(ResponseKey.VALUES, vos.toArray() );
		}
		
		return CampBattleConstant.SUCCESS;
	}
	
	/**
	 * 取得本周有官衔的角色信息
	 * @param camp
	 * @param date
	 * @return
	 */
	public List<PlayerCampBattleRecord> getCampTitlePlayers(Camp camp) {
		if(camp == null || camp == Camp.NONE){
			return null;
		}
		List<Long> playerIds = campBattleManager.getCampTitlePlayers(camp);
		if(playerIds != null ){
			List<PlayerCampBattleRecord> list = new ArrayList<PlayerCampBattleRecord>( CampTitle.values().length );
			for(Long pId : playerIds){
				PlayerCampBattleRecord record = campBattleManager.getPlayerCampBattleRecord( pId );
				if(record != null){
					CampTitle campTitle = record.getCampTitle();
					if(campTitle != null && campTitle != CampTitle.NONE){
						list.add( record );
					}
				}
			}
			return list;
		}
		
		return null;
	}

	
	public int getCampLeader(int campValue, int pageSize, int pageNow, Map<String, Object> resultMap) {
		Camp camp = EnumUtils.getEnum(Camp.class, campValue);
		if(camp == null || camp == Camp.NONE){
			return CampBattleConstant.MUST_HAD_CAMP;
		}
		
		if(pageNow <= 0){
			pageNow = 1;
		}
		
		List<Long> leaderPlayerIds = campBattleManager.getCampLeaderPlayerIds(camp);
		List<Long> leaderIds = null;
		synchronized (leaderPlayerIds) {
			leaderIds = new ArrayList<Long>(leaderPlayerIds); 
		}
		int pageCount = (leaderIds.size() + pageSize - 1) / pageSize ;
		//分页数
		resultMap.put(ResponseKey.COUNT, pageCount );
		if(pageNow > pageCount){
			pageNow = pageCount;
		}
		
		List<Long> pageResult = Tools.pageResult(leaderIds, (pageNow - 1) * pageSize, pageSize);
		if(pageResult != null && pageResult.size() > 0){
			List<PlayerBattleVO> list = new ArrayList<PlayerBattleVO>( pageResult.size() );
			for(long hisId : pageResult){
				PlayerBattleVO vo = new PlayerBattleVO();
				PlayerCampBattleHistory his = campBattleManager.getPlayerCampBattleHistory(hisId);
				Long playerId = his.getPlayerId();
				UserDomain pDomain = userManager.getUserDomain(playerId);
				if(pDomain != null){
					Player pPlayer = pDomain.getPlayer();
					vo.setPlayerId(playerId);
					vo.setPlayerName(pPlayer.getName());
					vo.setLevel( his.getLevel() );
					 
					list.add( vo);
				}
				vo.setCampTitle(his.getCampTitle());
				vo.setBattleDate(his.getBattleDate());
			}
			resultMap.put(ResponseKey.VALUES, list.toArray());
		}
		
		resultMap.put(ResponseKey.PAGE_SIZE, pageSize );
		resultMap.put(ResponseKey.PAGE_NOW, leaderIds.isEmpty() ? 0 : pageNow );
		
		return CampBattleConstant.SUCCESS;
	}

	
	public int getCampBattleHistory(Long playerId, Date date, Camp camp, int pageSize, int pageNow, Map<String, Object> resultMap) {
		if(date == null){
			return CampBattleConstant.INPUT_VALUE_INVALID; 
		}
		
		if(camp == null){
			camp = Camp.NONE;
		}
		
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return CampBattleConstant.PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();   
		Camp playerCamp = player.getCamp();
		if(playerCamp == null || playerCamp == Camp.NONE){
			return CampBattleConstant.MUST_HAD_CAMP;
		}
		
		CampBattleHistory campBattleHistory1 = campBattleManager.getCampBattleHistory(date, Camp.KNIFE_CAMP);
		if(campBattleHistory1 == null){
			return CampBattleConstant.INPUT_VALUE_INVALID;
		}
		
		CampBattleHistory campBattleHistory2 = campBattleManager.getCampBattleHistory(date, Camp.SWORD_CAMP);
		if(campBattleHistory2 == null){
			return CampBattleConstant.INPUT_VALUE_INVALID;
		}
		BattleInfoVO battleInfoVO = new BattleInfoVO();
		CampBattleVO[] campBattleVOs = {CampBattleVO.valueOf(campBattleHistory1), CampBattleVO.valueOf(campBattleHistory2)};
		battleInfoVO.setCampBattleVOs(campBattleVOs);
		
		PlayerCampBattleHistory playerCampBattleHistory = getPlayerCampBattleHistory(date, playerId);
		if(playerCampBattleHistory != null){
			PlayerBattleVO playerBattleVO = PlayerBattleVO.valueOf(userDomain, playerCampBattleHistory);
			List<Long> pIds = campBattleManager.getPlayerCampBattleHistory(date, playerCamp, true);
			if(pIds != null){
				playerBattleVO.setRank( pIds.indexOf(playerCampBattleHistory.getId()) + 1 );
			}
			battleInfoVO.setPlayerBattleVO(playerBattleVO);
		}
		
		if(pageNow <= 0){
			pageNow = 1;
		}
		List<Long> pHisIds = campBattleManager.getPlayerCampBattleHistory(date, camp, true);
		if(pHisIds != null){
			int pageCount = (pHisIds.size() + pageSize - 1) / pageSize ;
			//分页数
			resultMap.put(ResponseKey.COUNT, pageCount );
			if(pageNow > pageCount){
				pageNow = pageCount;
			}
			
			List<PlayerBattleVO> vos = new ArrayList<PlayerBattleVO>();
			List<Long> pageResult = Tools.pageResult(pHisIds, (pageNow - 1) * pageSize, pageSize);
			if(pageResult != null && pageResult.size() > 0){
				for(Long hisId : pageResult){
					PlayerCampBattleHistory pHis = campBattleManager.getPlayerCampBattleHistory(hisId);
					if(pHis != null){
						UserDomain user = userManager.getUserDomain(pHis.getPlayerId());
						vos.add(PlayerBattleVO.valueOf(user, pHis));
					}
				}
			}
			resultMap.put(ResponseKey.VALUES, vos.toArray() );
		}
		resultMap.put("battleInfoVO", battleInfoVO);
		
		resultMap.put(ResponseKey.PAGE_SIZE, pageSize);
		resultMap.put(ResponseKey.PAGE_NOW, pageNow);
		
		return CampBattleConstant.SUCCESS;
	}

	@SuppressWarnings("unchecked")
	
	public int rewards(Long playerId, Date date) {
		if(date == null && this.preCampBattleDate == null){
			return CampBattleConstant.CAMP_BATTLE_HISTORY_NOT_FOUND;
		}
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return CampBattleConstant.PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		Camp camp = player.getCamp();
		if(camp == null || camp == Camp.NONE){
			return CampBattleConstant.MUST_HAD_CAMP;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle.getLevel() < CampBattleRule.REWARD_MIN_LEVEL){
			return CampBattleConstant.LEVEL_INVALID;
		}
		
		if(this.preCampBattleDate == null){
			List<Date> campBattleDates = this.getCampBattleDates();
			this.preCampBattleDate = campBattleDates != null && campBattleDates.size() > 0 ? campBattleDates.get(0) : null;
		}
		date = date == null ? this.preCampBattleDate : date;
		CampBattleHistory campBattleHistory = campBattleManager.getCampBattleHistory(date, camp);
		if(campBattleHistory == null){
			return CampBattleConstant.CAMP_BATTLE_HISTORY_NOT_FOUND;
		}
		
		PlayerCampBattleHistory playerCampBattleHistory = campBattleManager.getPlayerCampBattleHistory(playerId, date, camp);
		
		PlayerCampBattleRecord playerCampBattleRecord = campBattleManager.getPlayerCampBattleRecord(playerId);
		if(playerCampBattleRecord == null){
			return CampBattleConstant.PLAYER_NOT_FOUND;
		}
		if(playerCampBattleRecord.isRewardCampBattle(date)){
			return CampBattleConstant.HAD_REWARD;
		}
		
		CampScoreRewards campScoreRewards = campBattleService.getCampBattleScoreRewards( playerCampBattleHistory == null ? 0 : playerCampBattleHistory.getScores() );
		if(campScoreRewards != null && !campScoreRewards.isEmpty() ){
			List<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
			List<UserProps> userPropsList = new ArrayList<UserProps>( 1 );
			int backpack = BackpackType.DEFAULT_BACKPACK;
			int baseId = campScoreRewards.getScoreGift();
			PropsConfig propsConfig = resourceService.get(baseId, PropsConfig.class);
			if(propsConfig == null){
				logger.error("阵营战奖励，道具不存在:{}", baseId);
				return CampBattleConstant.BASEDATA_NOT_FOUND;
			}
			UserProps userProps = UserProps.valueOf(playerId, baseId, 1, backpack, null, true);
			userPropsList.add(userProps);
			
			ChainLock lock = LockUtils.getLock( player.getPackLock(), playerCampBattleRecord );
			try {
				lock.lock();
				if(playerCampBattleRecord.isRewardCampBattle(date)){
					return CampBattleConstant.HAD_REWARD;
				}
				int currentBackSize = propsManager.getBackpackSize(playerId, backpack);
				if(!player.canAddNew2Backpack(currentBackSize + userPropsList.size(), backpack)) {
					return BACKPACK_FULLED;
				}
				playerCampBattleRecord.add2CampBattleRewards(date);
				
				List<UserProps> newUserProps = propsManager.createUserProps(userPropsList);
				propsManager.put2UserPropsIdsList(playerId, backpack, newUserProps);
				
				int addSilver = FormulaHelper.invoke(campScoreRewards.getSilver(), battle.getLevel(), battle.getLevel()).intValue();
				player.increaseSilver( addSilver );
				
				int addExp = FormulaHelper.invoke(campScoreRewards.getExp(), battle.getLevel(), battle.getLevel()).intValue();
				battle.increaseExp( addExp );
				
				if(addSilver != 0){
					SilverLogger.inCome(Source.CAMP_BATTLE_SCORE_WIN_REWARD, addSilver, player);
				}
				if(addExp != 0){
					ExpLogger.battleFieldExp(userDomain, Source.CAMP_BATTLE_SCORE_WIN_REWARD, addExp);
				}
				
				backpackEntries.addAll( newUserProps );
				GoodsLogger.goodsLogger(player, Source.CAMP_BATTLE_SCORE_WIN_REWARD, LoggerPropsHelper.convertLoggerGoods(Orient.INCOME, newUserProps, null, null, null));
				
			} finally {
				lock.unlock();
			}
			dbService.submitUpdate2Queue(playerCampBattleRecord, player, battle);
			
			//奖励
			MessagePushHelper.pushUserProps2Client(playerId, backpack, false, backpackEntries);
			
			//推送属性
			List<Long> playerIdList = Arrays.asList(player.getId());
			List<UnitId> unitIdList = Arrays.asList(UnitId.valueOf(player.getId(), ElementType.PLAYER));
			UserPushHelper.pushAttribute2AreaMember(player.getId(),playerIdList, unitIdList, AttributeKeys.SILVER, AttributeKeys.EXP);
			
			MessagePushHelper.pushGoodsCountChange2Client(playerId, GoodsVO.valuleOf(userPropsList, null, null, null) );
			
		} else {
			return CampBattleConstant.NO_RIGHT;
		}
		
		return CampBattleConstant.SUCCESS;
	}

	
	public int getCampBattleStatus() {
		return this.campBattleStatus.get();
	}

	
	public void processMonsterHurt(MonsterDomain monsterDomain, UnitId attacker, int hurtHP) {
		if(monsterDomain == null || attacker == null || hurtHP < 0){
			return ;
		}
		
		final MonsterDomain monsterAiDomain = (MonsterDomain)monsterDomain;
		GameMap gameMap = monsterAiDomain.getGameMap();
		if(gameMap == null || gameMap.getMapId() != CampBattleRule.CAMP_BATTLE_MAPID){
			return ;
		}
		
		final MonsterBattle monsterBattle = monsterAiDomain.getMonsterBattle();
		UserDomain userDomain = null;
		if(attacker.getType() == ElementType.PLAYER){
			userDomain = userManager.getUserDomain(attacker.getId());
		} else if(attacker.getType() == ElementType.PET){
			userDomain = PetHelper.getUserDomain(attacker.getId());
		} else { return ; }
		
		if(this.campBattleStatus.get() != CampBattleRule.STATUS_START){
			return ;
		}
		
		Player player = userDomain.getPlayer();
		Camp playerCamp = player.getCamp();
		CampBattle campBattle = this.getCampBattle(playerCamp);
		if(campBattle == null){
			return ;
		}
		
		final Set<Integer> campPointIds = campBattleService.getCampPointIds();
		final Integer monsterConfigId = monsterAiDomain.getMonsterConfig().getId();
		Camp targetCamp = Camp.values()[ monsterAiDomain.getMonsterCamp() ];
		CampBattle targetCampBattle = this.getCampBattle( targetCamp );
		
		PlayerCampBattle playerCampBattle = this.playerCampBattleMap.get( userDomain.getId() );
		if(playerCampBattle == null){
			return ;
		}
		MonsterFightConfig monsterFightConfig = monsterAiDomain.getMonsterFightConfig();
		
		final boolean boss = targetCampBattle != null && targetCampBattle.getMonsterAiDomain() == monsterAiDomain;
		boolean isWin = false;
		if(boss && monsterBattle.isDead()){
			if(this.campBattleStatus.compareAndSet(CampBattleRule.STATUS_START, CampBattleRule.STATUS_SYS_CALC) ){
//				if(this.campBattleStopFuture != null){
//					this.campBattleStopFuture.cancel(false);
//				}
				isWin = true;
			}
		}
		
		final boolean isPoint = campPointIds.contains(monsterConfigId);
		final boolean hasScores = monsterFightConfig.getMonsterType() == MonsterType.ELITE.getValue() ;
		final boolean isDead = monsterBattle.isDead();
		if(boss){	//是阵营BOSS
			synchronized (playerCampBattle) {
				playerCampBattle.increaseBossHurtHP(hurtHP);
				try {
					Number number = FormulaHelper.invoke(FormulaKey.CAMP_BATTLE_HURT_BOSS_SCORE, playerCampBattle.getBossHurtHP() );
					playerCampBattle.setHurtBossScores(number.intValue());
					
				} catch (Exception e) {
					logger.error("计算阵营战场玩家得分出错：{}", e);
					logger.error("{}", e);
				}
			}
			
			synchronized (campBattle) {
				if(isWin){
					campBattle.setKillBoss(true);
					campBattle.setWin(true);
				}
				campBattle.increaseBossHurtHP(hurtHP);
				try {
					Number number = FormulaHelper.invoke(FormulaKey.CAMP_BATTLE_CAMP_HURT_BOSS_SCORE, campBattle.getBossHurtHP() );
					campBattle.setHurtBossScores(number.intValue());
				} catch (Exception e) {
					logger.error("计算阵营战场阵营得分出错：{}", e);
					logger.error("{}", e);
				}
			}
			pushFlag.compareAndSet(false, true);
			
			pushBossNotice(monsterBattle, monsterConfigId, targetCamp, monsterFightConfig);
			
		} else  if( isPoint ){	//据点
			handlePointInfo(monsterAiDomain, monsterBattle, player, playerCamp, monsterConfigId, targetCamp, monsterFightConfig);
			
		} else if( hasScores ){	//精英怪
			synchronized (playerCampBattle) {
				playerCampBattle.increaseBossHurtHP(hurtHP);
				try {
					Number number = FormulaHelper.invoke(FormulaKey.CAMP_BATTLE_HURT_BOSS_SCORE, playerCampBattle.getBossHurtHP() );
					playerCampBattle.setHurtBossScores(number.intValue());
					
				} catch (Exception e) {
					logger.error("计算阵营战场玩家得分出错：{}", e);
					logger.error("{}", e);
				}
			}
			
			synchronized (campBattle) {
				campBattle.increaseBossHurtHP(hurtHP);
				try {
					Number number = FormulaHelper.invoke(FormulaKey.CAMP_BATTLE_CAMP_HURT_BOSS_SCORE, campBattle.getBossHurtHP() );
					campBattle.setHurtBossScores(number.intValue());
				} catch (Exception e) {
					logger.error("计算阵营战场阵营得分出错：{}", e);
					logger.error("{}", e);
				}
			}
			pushFlag.compareAndSet(false, true);
		}
		
		//处理据点
		handlePointFlash(monsterBattle, isDead, playerCamp, monsterConfigId, targetCamp, boss, isPoint);
		
		//处理阵营战场结束
		if(this.campBattleStatus.get() == CampBattleRule.STATUS_SYS_CALC){
			if( this.campBattleStopFuture.cancel(false) ){
				this.handleStopCampBattle();
			}
		}
		
	}

	/**
	 * 处理闪烁
	 * @param monsterBattle
	 * @param isDead
	 * @param playerCamp
	 * @param monsterConfigId
	 * @param targetCamp
	 * @param boss
	 * @param isPoint
	 */
	@SuppressWarnings("unchecked")
	private void handlePointFlash(final MonsterBattle monsterBattle, final boolean isDead,
			Camp playerCamp, final Integer monsterConfigId, Camp targetCamp,
			final boolean boss, final boolean isPoint) {
		if(isPoint || boss){
			if(targetCamp == null || targetCamp == Camp.NONE){
				return ;
			}
			int noticeId = -1;
			//处理据点被攻击
			ConcurrentMap<Integer, PointInfo> attackedPoints = (ConcurrentMap<Integer, PointInfo>) this.noticeMap.get(noticeId);
			if(attackedPoints == null) {
				this.noticeMap.putIfAbsent(noticeId, new ConcurrentHashMap<Integer, PointInfo>(9) );
				attackedPoints = (ConcurrentMap<Integer, PointInfo>) noticeMap.get(noticeId);
			}
			PointInfo pointInfo = attackedPoints.get(monsterConfigId);
			if( isDead ){		//死亡
				if(pointInfo != null){
					synchronized (pointInfo) {
						pointInfo.preAttackedTime = 0;
					}
				}
				
			} else if(targetCamp != null || targetCamp != Camp.NONE){
				if(pointInfo == null){
					pointInfo = new PointInfo();
					pointInfo.monsterConfigId = monsterConfigId;
					attackedPoints.putIfAbsent(monsterConfigId, pointInfo );
					pointInfo = attackedPoints.get(monsterConfigId);
				}
				
				boolean needPush = false;
				synchronized (pointInfo) {
					if(!pointInfo.attacked){
						pointInfo.attacked = true;
						pointInfo.pointCamp = targetCamp;
						needPush = true;
					}
					pointInfo.preAttackedTime = System.currentTimeMillis(); 
				}
				if(needPush){
					CampBattlePushHelper.pushPointAttacked(monsterConfigId, targetCamp, true);
					if(boss){
						Map<String, Object> params = new HashMap<String, Object>(2);
						params.put(NoticeRule.campName, targetCamp.getName());
						params.put(NoticeRule.monsterBaseId, monsterBattle.getMonsterFight().getName() );
						CampBattlePushHelper.pushNotice(NoticeID.CAMP_BATTLE_BOSS_HURTED, targetCamp, params);
					}
				}
				
			}
		}
	}
	
	

	private void handlePointInfo(final MonsterDomain monsterAiDomain,
			final MonsterBattle monsterBattle, Player player, Camp playerCamp,
			final Integer monsterConfigId, Camp targetCamp,
			MonsterFightConfig monsterFightConfig) {
		
		if(monsterBattle.isDead()){
			processPointObtain(monsterAiDomain, monsterFightConfig, player, monsterBattle, playerCamp, targetCamp, monsterConfigId);
			pushFlag.compareAndSet(false, true);
			
		} else if(targetCamp != null && targetCamp != Camp.NONE){
			pushCampBattlePointInfo(monsterBattle, player, playerCamp, monsterConfigId, targetCamp, monsterFightConfig);
		}
		
	}

	/**
	 * 推送boss公告
	 * @param monsterBattle
	 * @param monsterConfigId
	 * @param targetCamp
	 * @param monsterFightConfig
	 */
	private void pushBossNotice(final MonsterBattle monsterBattle,
			final Integer monsterConfigId, Camp targetCamp,
			MonsterFightConfig monsterFightConfig) {
		
		//推送BOSS血低公告
		int precent = monsterBattle.getMonsterHpPercent();
		Integer preHPPrecent = (Integer)this.noticeMap.put(NoticeID.CAMP_BATTLE_BOSS_HP_LOW, precent);
		if(preHPPrecent == null || preHPPrecent > precent) {
			BulletinConfig bulletinConfig = NoticePushHelper.getConfig(NoticeID.CAMP_BATTLE_BOSS_HP_LOW, BulletinConfig.class);
			if(bulletinConfig != null){
				Set<Integer> conditions = bulletinConfig.getConditions();
				if(conditions.contains(precent)){
					Map<String, Object> params = new HashMap<String, Object>(3);
					params.put(NoticeRule.campName, targetCamp.getName() );
					params.put(NoticeRule.monsterBaseId, monsterFightConfig.getName() );
					params.put(NoticeRule.number, precent );
					CampBattlePushHelper.pushNotice(NoticeID.CAMP_BATTLE_BOSS_HP_LOW, targetCamp, params);
				}
				
			} else {
				logger.error("公告基础数据不存在:{}", NoticeID.CAMP_BATTLE_BOSS_HP_LOW);
			}
			
		}
	}

	/**
	 * 推送战场结束时间公告
	 * @param campBattleEndTime
	 * @return
	 */
	public void pushCampBattleTimeNotice(Date campBattleEndTime){
		if(campBattleEndTime == null){
			return ;
		}
		int mins = (int)( ( campBattleEndTime.getTime() - System.currentTimeMillis() ) / TimeConstant.ONE_MINUTE_MILLISECOND );
		Integer preMins = (Integer) noticeMap.putIfAbsent(NoticeID.CAMP_BATTLE_TIME_REMAIN, mins);
		if(mins <= 0) {
			return ;
		}
		
		if(preMins == null || preMins != mins){
			BulletinConfig bulletinConfig = NoticePushHelper.getConfig(NoticeID.CAMP_BATTLE_TIME_REMAIN, BulletinConfig.class);
			if(bulletinConfig != null){
				Set<Integer> conditions = bulletinConfig.getConditions();
				if( conditions.contains(mins) ){
					preMins = (Integer) noticeMap.put(NoticeID.CAMP_BATTLE_TIME_REMAIN, mins);
					if(preMins != mins){
						Map<String, Object> params = new HashMap<String, Object>(4);
						params.put(NoticeRule.number, mins );
						CampBattlePushHelper.pushNotice( bulletinConfig.getId(), Camp.NONE, params );
					}
				}
			} else {
				logger.error("公告基础数据不存在:{}", NoticeID.CAMP_BATTLE_TIME_REMAIN);
			}
		}
			
	}
	
	/**
	 * 推送阵营据点公告
	 * @param monsterBattle
	 * @param player
	 * @param playerCamp
	 * @param monsterConfigId
	 * @param targetCamp
	 * @param monsterFightConfig
	 */
	@SuppressWarnings("unchecked")
	private void pushCampBattlePointInfo(final MonsterBattle monsterBattle,
			Player player, Camp playerCamp, final Integer monsterConfigId,
			Camp targetCamp, MonsterFightConfig monsterFightConfig) {
		//目标阵营据点被攻击
		Set<Integer> points = (Set<Integer>)this.noticeMap.get(NoticeID.CAMP_BATTLE_POINT_ATTACKED);
		if(points == null) {
			this.noticeMap.putIfAbsent(NoticeID.CAMP_BATTLE_POINT_ATTACKED, Collections.synchronizedSet( new HashSet<Integer>(7)) );
			points = (Set<Integer>)noticeMap.get(NoticeID.CAMP_BATTLE_POINT_ATTACKED);
		}
		boolean isPush = false;
		synchronized (points) {
			if( points.contains(monsterConfigId) ){
				isPush = true;
			}
		}
		if(isPush){
			Map<String, Object> params = new HashMap<String, Object>(3);
			params.put(NoticeRule.monsterBaseId, monsterFightConfig.getName() );
			params.put(NoticeRule.campName, playerCamp.getName() );
			params.put(NoticeRule.playerName, player.getName() );
			CampBattlePushHelper.pushNotice(NoticeID.CAMP_BATTLE_POINT_ATTACKED, targetCamp, params);
		}
		
		//推送据点血hp百分比公告
		int precent = monsterBattle.getMonsterHpPercent();
		Map<Integer, Integer> pointHpMap = (Map<Integer, Integer>) this.noticeMap.get(NoticeID.CAMP_BATTLE_POINT_HP_LOW);
		if(pointHpMap == null) {
			this.noticeMap.putIfAbsent(NoticeID.CAMP_BATTLE_POINT_HP_LOW, new ConcurrentHashMap<Integer, Integer>(7) );
			pointHpMap = (Map<Integer, Integer>)this.noticeMap.get(NoticeID.CAMP_BATTLE_POINT_HP_LOW);
		}
		Integer preHPPrecent = pointHpMap.put(monsterConfigId, precent);
		if(preHPPrecent == null || preHPPrecent > precent) {
			BulletinConfig bulletinConfig = NoticePushHelper.getConfig(NoticeID.CAMP_BATTLE_POINT_HP_LOW, BulletinConfig.class);
			if(bulletinConfig != null){
				if( bulletinConfig.getConditions().contains(precent) ){
					Map<String, Object> params = new HashMap<String, Object>(3);
					params.put(NoticeRule.campName, targetCamp.getName() );
					params.put(NoticeRule.monsterBaseId, monsterFightConfig.getName() );
					params.put(NoticeRule.playerName, player.getName() );
					params.put(NoticeRule.number, precent );
					CampBattlePushHelper.pushNotice(NoticeID.CAMP_BATTLE_POINT_HP_LOW, targetCamp, params);
				}
				
			} else {
				logger.error("公告基础数据不存在:{}", NoticeID.CAMP_BATTLE_POINT_HP_LOW);
			}
		}
	
	}

	/**
	 * 处理据点被击破
	 * @param monsterAiDomain
	 * @param monsterFightConfig
	 * @param player
	 * @param monsterBattle
	 * @param playerCamp
	 * @param targetCamp
	 * @param monsterConfigId
	 */
	private void processPointObtain(final MonsterDomain monsterAiDomain, final MonsterFightConfig monsterFightConfig, final Player player,
			final MonsterBattle monsterBattle, Camp playerCamp, Camp targetCamp,
			final Integer monsterConfigId) {
		  
		synchronized (this) {
			boolean isNoCampPoint = this.noCampPointId.contains(monsterConfigId);	//是中立
			if(isNoCampPoint){
				this.noCampPointId.remove(monsterConfigId);
				monsterAiDomain.setMonsterCamp(playerCamp);
			} else {
				this.noCampPointId.add(monsterConfigId);
				monsterAiDomain.setMonsterCamp(Camp.NONE);
			}
			Collection<CampBattle> campBattles = this.campBattles.values();
			for(CampBattle cb : campBattles){
				CopyOnWriteArrayList<Integer> ownPointIds = cb.getOwnPointIds();
				if(isNoCampPoint){	//跟玩家同一个阵营
					if(cb.getCamp() == playerCamp && !ownPointIds.contains(monsterConfigId) ) {
						ownPointIds.add(monsterConfigId);
					}
					
				} else {
					ownPointIds.remove(monsterConfigId);
				}
			}
		}
		
		final int reviveTime = monsterBattle.getMonsterFight().getReviveTime();
		if(reviveTime > 0){
			scheduler.scheduleWithDelay(new ScheduledTask() {
				
				public void run() {
					resurrectionPoint(monsterAiDomain, monsterBattle);
				}
				
				public String getName() { return "阵营战据点复活"; }
			}, reviveTime * TimeConstant.ONE_SECOND_MILLISECOND);
			
		} else {
			resurrectionPoint(monsterAiDomain, monsterBattle);
		}
		
		//占据据点公告
		Map<String, Object> params = new HashMap<String, Object>(3);
		params.put(NoticeRule.monsterBaseId, monsterFightConfig.getName() );
		params.put(NoticeRule.campName, playerCamp.getName() );
		params.put(NoticeRule.playerName, player.getName() );
		CampBattlePushHelper.pushNotice(NoticeID.CAMP_BATTLE_POINT_CAPTURE, targetCamp, params);
		
		//全部据点被占据公告
		CampBattle campBattle = getCampBattle(playerCamp);
		CopyOnWriteArrayList<Integer> ownPointIds = campBattle.getOwnPointIds();
		if(ownPointIds.size() >= campBattleService.getCampPointIds().size() ){
			Map<String, Object> params2 = new HashMap<String, Object>(2);
			params2.put(NoticeRule.campName, playerCamp.getName() );
			params2.put(NoticeRule.playerName, player.getName() );
			CampBattlePushHelper.pushNotice(NoticeID.CAMP_BATTLE_POINT_ALL_LOST, Camp.NONE, params2);
		}
	}
	
	/**
	 * 据点复活处理
	 * @param monsterAiDomain
	 * @param monsterBattle
	 */
	private void resurrectionPoint( final MonsterDomain monsterAiDomain, final MonsterBattle monsterBattle) {
		synchronized (monsterAiDomain) {
			if(monsterBattle.isDead()){
				monsterAiDomain.resurrection(true);
			}
		}
		CampBattlePushHelper.pushCampPointChange(monsterAiDomain);
	}
	
	public Map<Integer, Object> getAttributesOfCampBattleMonster(MonsterDomain monsterDomain) {
		if(this.campBattleStatus.get() == CampBattleRule.STATUS_START){
			IMonsterConfig monsterConfig = monsterDomain.getMonsterConfig();
			int monsterConfigId = monsterConfig.getId();
//			Camp camp = Camp.NONE;
//			if( !this.noCampPointId.contains(monsterConfigId) ){
//				CampBattle[] campBattles = this.campBattles;
//				for(CampBattle campBattle : campBattles){
//					MonsterAiDomain bossMonster = campBattle.getMonsterAiDomain();
//					if(campBattle.getOwnPointIds().contains(monsterConfigId) || bossMonster != null && bossMonster.getMonsterConfig().getId() == monsterConfigId  ){
//						camp = campBattle.getCamp();
//						break;
//					}
//				}
//			}
			
			Set<Integer> campPointIds = campBattleService.getCampPointIds();
			if( campPointIds.contains(monsterConfigId) ){
				if( !this.noCampPointId.contains(monsterConfigId) ){
					Map<Integer, Object> attrs = new HashMap<Integer, Object>(1);
					attrs.put(AttributeKeys.CLOTHING, campBattleService.getPointModule( monsterDomain.getMonsterCamp() ) );
					return attrs;
				}
			}
		}
		return null;
	}

	
	public void processKillPlayers(UserDomain attacker, UserDomain target) {
		if(attacker == null || target == null){
			return ;
		}
		
		if(this.campBattleStatus.get() != CampBattleRule.STATUS_START){
			return ;
		}
		
		PlayerCampBattle playerCampBattle = this.playerCampBattleMap.get( attacker.getId() );
		if(playerCampBattle != null){
			CampBattle campBattle = this.getCampBattle(playerCampBattle.getCamp());
			int killPlayers = 0;
			synchronized (playerCampBattle) {
				playerCampBattle.increaseKillPlayers(1);
				killPlayers = playerCampBattle.getKillPlayers();
				try {
					Number number = FormulaHelper.invoke(FormulaKey.CAMP_BATTLE_KILL_PLAYER_SCORE, killPlayers );
					playerCampBattle.setScores(number.intValue());
					pushFlag.compareAndSet(false, true);
				} catch (Exception e) {
					logger.error("计算阵营战场杀人得分出错：{}", e);
				}
			}
			
			if(campBattle != null){
				synchronized (campBattle) {
					campBattle.increaseKillPlayers(1);
					try {
						Number number = FormulaHelper.invoke(FormulaKey.CAMP_BATTLE_KILL_PLAYER_SCORE, campBattle.getKillPlayers() );
						campBattle.setScores(number.intValue());
						pushFlag.compareAndSet(false, true);
					} catch (Exception e) {
						logger.error("计算阵营战场杀人得分出错：{}", e);
					}
				}
			}
			
			//推送杀人公告
			CampBattlePushHelper.pushKillPlayerNotice(attacker.getPlayer(), target.getPlayer(), killPlayers, this.noticeMap);
		}
		
	}
	
	/** 等待锁对象 */
	private final ReentrantLock takeLock = new ReentrantLock();
	private final Condition notEmpty = takeLock.newCondition();
	public final Runnable HANDLER_BATTLE_INFO_TASK = new Runnable() {
		
		public void run() {
			while (true) {
				try {
//					CampBattle[] cbs = campBattles;
					int status = campBattleStatus.get();
					if(status != CampBattleRule.STATUS_START){	//战场结束，以后不推送了
//						if(cbs != null){
//							CampBattlePushHelper.pushBattleInfo(cbs, noCampPointId);	//最后再推送一次
//						}
						break;
					}
					
					//推送战场消息
					if(pushFlag.compareAndSet(true, false)){
						CampBattlePushHelper.pushBattleInfo(campBattles.values(), noCampPointId);
					}
					
					//推送时间公告
					try {
						pushCampBattleTimeNotice(campBattleEndTime);
					} catch (Exception e) {
						logger.error("推送战斗时间公告时出错:{}", e);
						logger.error("{}", e);
					}
					
					try {
						int noticeId = -1;
						@SuppressWarnings("unchecked")
						ConcurrentMap<Integer, PointInfo> attackedPoints = (ConcurrentMap<Integer, PointInfo>)noticeMap.get(noticeId);
						if(attackedPoints != null && attackedPoints.size() > 0){
							for(Entry<Integer, PointInfo> entry : attackedPoints.entrySet()){
								Integer monsterConfigId = entry.getKey();
								PointInfo pointInfo = entry.getValue();
								boolean needPush = false;
								synchronized (pointInfo) {
									needPush = pointInfo.isLeaveFightOverTime();
									if(needPush){
										pointInfo.attacked = false;
										pointInfo.preAttackedTime = 0;
									}
								}
								if( needPush ){
									CampBattlePushHelper.pushPointAttacked(monsterConfigId, pointInfo.pointCamp, false);
								}
							}
						}
						
					} catch (Exception e) {
						logger.error("推送据点闪现时出错：{}", e);
						logger.error("{}", e);
					}
					
					takeLock.lockInterruptibly();
					try {
						notEmpty.await(CampBattleRule.BATTLE_INFO_PUSH_TIMEUNIT, TimeUnit.MILLISECONDS);
			        } finally {
			            takeLock.unlock();
			        }
					
				} catch (Exception ex) {
					logger.error("推送战场信息时出错:{}", ex);
					logger.error("{}", ex);
				}
			}
		}
	};

	
	public GameMap getCampBattleGameMap(UserDomain userDomain) {
		if(userDomain != null /** && this.campBattleStatus.get() >= CampBattleRule.STATUS_APPLYER_ENTER*/ ){
			Player player = userDomain.getPlayer();
			Camp camp = player.getCamp();
			if(camp == null || camp == Camp.NONE){
				return null;
			}
			if(userDomain.getBattle().getLevel() < CampBattleRule.APPLY_MIN_LEVEL){
				return null;
			}
			PlayerMotion motion = userDomain.getMotion();
			if(motion.getMapId() != CampBattleRule.CAMP_BATTLE_MAPID){
				return null;
			}
			return userDomain.getGameMap();
//			CampBattle campBattle = this.getCampBattle(camp);
//			if(campBattle != null){
//				Long playerId = player.getId();
//				PlayerCampBattle playerCampBattle = this.playerCampBattleMap.get(playerId);
//				if(playerCampBattle != null){
//					Set<PlayerCampBattle> players = campBattle.getPlayers();
//					if( players.contains(playerCampBattle) ){
//						return this.campBattleGameMaps.get(globleBranch);
//					}
//				}
//			}
		}
		return null;
	}

	
	public boolean isInCampBattle(UserDomain userDomain) {
		return getCampBattleGameMap(userDomain) != null;
	}

	
	public List<Date> getCampBattleDates() {
		return campBattleManager.getCampBattleDates();
	}

	
	public int getRewardStat(Long playerId, int type, Date date) {
		if(date == null && this.preCampBattleDate == null){
			return CampBattleConstant.FAILURE;
		}
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return CampBattleConstant.FAILURE;
		}
		
		Player player = userDomain.getPlayer();
		Camp camp = player.getCamp();
		if(camp == null || camp == Camp.NONE){
			return CampBattleConstant.FAILURE;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle.getLevel() < CampBattleRule.REWARD_MIN_LEVEL){
			return CampBattleConstant.FAILURE;
		}
		
		date = date == null ? this.preCampBattleDate : date;
		PlayerCampBattleRecord playerCampBattleRecord = campBattleManager.getPlayerCampBattleRecord(playerId);
		if(playerCampBattleRecord == null){
			return CampBattleConstant.FAILURE;
		}
		if(type == 1 && playerCampBattleRecord.isRewardCampBattle(date)){
			return 1;
		}
		if(type == 3 && playerCampBattleRecord.getSuitReward() != null){
			return 1;
		}
		
		PlayerCampBattleHistory playerCampBattleHistory = campBattleManager.getPlayerCampBattleHistory(playerId, date, camp);
		ChainLock lock = playerCampBattleHistory == null ? LockUtils.getLock(playerCampBattleRecord) : LockUtils.getLock(playerCampBattleRecord, playerCampBattleHistory);
		try {
			lock.lock();
			if(type == 1){			//积分奖励
				if(playerCampBattleRecord.isRewardCampBattle(date)){
					return 1;
				}
				
				CampScoreRewards campScoreRewards = campBattleService.getCampBattleScoreRewards(playerCampBattleHistory == null ? 0 : playerCampBattleHistory.getScores() );
				return ( campScoreRewards == null || campScoreRewards.isEmpty() ) ? CampBattleConstant.FAILURE : 0;
				
			} else if(type == 2){	//官衔俸禄
				CampTitle campTitle = playerCampBattleRecord.getCampTitle();
				if(campTitle == null || campTitle == CampTitle.NONE){
					return CampBattleConstant.FAILURE;
				}
				Date rewardDate = playerCampBattleRecord.getSalaryReward();
				return rewardDate != null && DateUtil.isToday(date) ? 1 : CampBattleConstant.SUCCESS;
				
			} else if(type == 3){	//官衔时装
				CampTitle campTitle = playerCampBattleRecord.getCampTitle();
				if(campTitle == null || campTitle == CampTitle.NONE){
					return CampBattleConstant.FAILURE;
				}
				return playerCampBattleRecord.getSuitReward() != null ? 1 : CampBattleConstant.SUCCESS;
				
			}
			
		} finally {
			lock.unlock();
		}
		
		return CampBattleConstant.FAILURE;
	}

	
	public int campBattleRequestCmd(Long playerId, int cmd, Map<String, Object> resultMap) {
		if(cmd == 2){//请求下次战场开启时间
			Date nextBattleTime = startTimeGenerator.next(new Date());	//下次战场开战时间
			resultMap.put("battleTime", nextBattleTime);
			return CampBattleConstant.SUCCESS;
		}
		
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return CampBattleConstant.PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		Camp camp = player.getCamp();
		if(camp == null || camp == Camp.NONE){
			return CampBattleConstant.MUST_HAD_CAMP;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle.getLevel() < CampBattleRule.APPLY_MIN_LEVEL){
			return CampBattleConstant.JOIN_LEVEL_LIMIT;
		}
		
		GameMap gameMap = getCampBattleGameMap(userDomain);
		if(gameMap == null){
			return CampBattleConstant.FAILURE;
		}
		
		PlayerCampBattle playerCampBattle = this.playerCampBattleMap.get(playerId);
		if(playerCampBattle == null){
			return CampBattleConstant.NOT_IN_CAMP_BATTLE;
		}
		
		int result = CampBattleConstant.FAILURE;
		if(cmd == 1){	//回城
			result = backCampPoint(playerId, resultMap, camp, gameMap, playerCampBattle);
			
		} else if(cmd == 3){//本次战场结束时间
//			if(this.campBattleStatus.get() < CampBattleRule.STATUS_START){
//				return CampBattleConstant.BATTLE_NOT_START;
//			}
			
			if(this.campBattleStatus.get() == CampBattleRule.STATUS_SYS_CALC){
				return CampBattleConstant.BATTLE_IS_OVER;
			}
			
			Date stopTime = this.campBattleEndTime;
			if(stopTime == null && campBattleStopFuture != null){
				long delay = campBattleStopFuture.getDelay(TimeUnit.MILLISECONDS) + 10;
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.MILLISECOND, (int)delay);
				stopTime = calendar.getTime();
				this.campBattleEndTime = stopTime;
			}
			resultMap.put("battleEndTime", stopTime);
			result = CampBattleConstant.SUCCESS;
			
		} else if(cmd == 4){
			if( gameMap == userDomain.getGameMap() && gotoDefaultCampPoint(playerId, camp, gameMap) ){	//在阵营战地图中能强制转场
				result = CampBattleConstant.SUCCESS;
			}
		}
		
		return result;
	}

	/**
	 * 回阵营据点
	 * @param playerId
	 * @param resultMap
	 * @param camp
	 * @param gameMap
	 * @param playerCampBattle
	 * @return
	 */
	private int backCampPoint(Long playerId, Map<String, Object> resultMap, Camp camp, GameMap gameMap, PlayerCampBattle playerCampBattle) {
		if(this.campBattleStatus.get() < CampBattleRule.STATUS_START){
			return CampBattleConstant.BATTLE_NOT_START;
		}
		synchronized (playerCampBattle) {
			long preBackTime = playerCampBattle.getPreBackTime();
			long currTime = System.currentTimeMillis();
			if(preBackTime > 0 && preBackTime > currTime ){
				resultMap.put(ResponseKey.COOL_TIME, preBackTime);
				return CampBattleConstant.COOL_TIMING;
				
			}
			playerCampBattle.setPreBackTime( currTime + CampBattleRule.BACK_CD_TIME );
			resultMap.put(ResponseKey.COOL_TIME, playerCampBattle.getPreBackTime() );
		}
		
		final boolean result = gotoDefaultCampPoint(playerId, camp, gameMap);
		return result ? CampBattleConstant.SUCCESS : CampBattleConstant.FAILURE;
	}
	
	/**
	 * 回到阵营默认战场点
	 * @param playerId
	 * @param camp
	 * @param gameMap
	 * @return
	 */
	private boolean gotoDefaultCampPoint(Long playerId, Camp camp, GameMap gameMap){
		if(gameMap == null){
			return false;
		}
		final CampPointConfig campPointConfig = resourceService.getByUnique(IndexName.CAMP_TYPE_POINT, CampPointConfig.class, 0, 0, camp.ordinal() );
		if(campPointConfig == null){
			return false;
		}
		int mapId = gameMap.getMapId(), targetX = campPointConfig.getX(), targetY = campPointConfig.getY();
		ChangePoint nearestChangePoint = gameMap.getNearestChangePoint(campPointConfig.getX(), campPointConfig.getY());
		if(nearestChangePoint != null){
			mapId = nearestChangePoint.getLinkMapId();
			targetX = nearestChangePoint.getX();
			targetY = nearestChangePoint.getY();
		}
		return mapFacade.go(playerId, mapId, targetX, targetY, 8);
	}

	
	public List<CampBattle> getCampBattles(Camp camp) {
		if( this.campBattles != null && camp != null){
			if(camp == Camp.NONE){
				return new ArrayList<CampBattle>( campBattles.values() );
			} else {
				return Arrays.asList( campBattles.get(camp) );
			}
			
		}
		return null;
	}
	
	class PointInfo{
		/** 据点id */
		private int monsterConfigId;
		/** 据点所属的阵营 */
		private Camp pointCamp = Camp.NONE;
		/** 是否标识为攻击 */
		private boolean attacked;
		/** 上次攻击时间 */
		private long preAttackedTime;
		
		public boolean isLeaveFightOverTime(){
			return attacked && preAttackedTime + CampBattleRule.CAMP_POINT_LEAVE_FIGHT_TIME < System.currentTimeMillis();
		}

		
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + monsterConfigId;
			return result;
		}

		
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PointInfo other = (PointInfo) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (monsterConfigId != other.monsterConfigId)
				return false;
			return true;
		}

		private CampBattleFacadeImpl getOuterType() {
			return CampBattleFacadeImpl.this;
		}
		
	}
	
	public int getScoreRank(long playerId, int pageNow, int pageSize, Map<String, Object> resultMap) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return CampBattleConstant.PLAYER_NOT_FOUND;
		}
		Player player = userDomain.getPlayer();
		Camp camp = player.getCamp();
		if(camp == null || camp == Camp.NONE){
			return  CampBattleConstant.MUST_HAD_CAMP;
		}
		
		PlayerBattle playerBattle = userDomain.getBattle();
		if(playerBattle.getLevel() < CampBattleRule.APPLY_MIN_LEVEL){
			return CampBattleConstant.LEVEL_INVALID;
		}
		
		if(pageNow <= 0){
			pageNow = 1;
		}
		
		if(this.campBattleStatus.get() >= CampBattleRule.STATUS_START){
			return CampBattleConstant.CAMP_BATTLE_NOT_OVER; 
		}
		
		List<Long> playerTotalScoreList = this.campBattleManager.getPlayerTotalScoreList(camp);
		if(this.campBattleStatus.get() >= CampBattleRule.STATUS_SYS_CALC){
			sortPlayerTotalScoreList(playerTotalScoreList);
		}
//		int rank = playerTotalScoreList.indexOf(playerId) + 1;
		int pageCount = (playerTotalScoreList.size() + pageSize - 1) / pageSize ;
		//分页数
		if(pageNow > pageCount){
			pageNow = pageCount;
		}
		//分页开始列
		int startIndex = pageSize * (pageNow - 1);
		List<Long> pageResult = Tools.pageResult(playerTotalScoreList, startIndex, pageSize);
		List<PlayerScoreVO> list = new ArrayList<PlayerScoreVO>();
		if(pageResult != null){
			for(long id : pageResult){
				PlayerScoreVO vo = new PlayerScoreVO();
				UserDomain uDomain = userManager.getUserDomain(id);
				if(uDomain != null){
					Player player2 = uDomain.getPlayer();
					vo.setPlayerId(id);
					vo.setPlayerName(player2.getName());
				}
				
				PlayerCampBattleRecord playerCampBattleRecord = this.campBattleManager.getPlayerCampBattleRecord(id);
				if(playerCampBattleRecord != null){
					vo.setJoins(playerCampBattleRecord.getJoins());
					vo.setTotalScore(playerCampBattleRecord.getTotalScore());
				}
				list.add(vo);
			}
		}
		
		resultMap.put("round", this.round);
		resultMap.put("totalRound", this.roundBattleDates.size());
		
		resultMap.put(ResponseKey.PAGE_NOW, pageNow);
		resultMap.put(ResponseKey.PAGE_SIZE, pageSize);
		resultMap.put(ResponseKey.COUNT, pageCount);
		resultMap.put(ResponseKey.VALUES, list.toArray());
		
		return CampBattleConstant.SUCCESS;
	}

	
	public int getPlayerScoreInfo(long playerId, Map<String, Object> resultMap) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return CampBattleConstant.PLAYER_NOT_FOUND;
		}
		Player player = userDomain.getPlayer();
		Camp camp = player.getCamp();
		if(camp == null || camp == Camp.NONE){
			return  CampBattleConstant.MUST_HAD_CAMP;
		}
		
		PlayerBattle playerBattle = userDomain.getBattle();
		if(playerBattle.getLevel() < CampBattleRule.APPLY_MIN_LEVEL){
			return CampBattleConstant.LEVEL_INVALID;
		}
		
		List<PlayerBattleVO> battleHis = new ArrayList<PlayerBattleVO>();
		for(Date battleDate : this.roundBattleDates){
			PlayerBattleVO vo = null;
			PlayerCampBattleHistory playerCampBattleHistory = this.campBattleManager.getPlayerCampBattleHistory(playerId, battleDate, camp);
			if(playerCampBattleHistory != null){
				vo = PlayerBattleVO.valueOf(userDomain, playerCampBattleHistory);
			} else {
				vo = new PlayerBattleVO();
			}
			vo.setBattleDate(battleDate);
			battleHis.add( vo );
		}
		
		PlayerScoreVO playerScoreVO = new PlayerScoreVO();
		playerScoreVO.setCamp(camp);
		
		//第几名
		List<Long> playerTotalScoreList = this.campBattleManager.getPlayerTotalScoreList(camp);
		int rank = playerTotalScoreList.indexOf(playerId) + 1;
		playerScoreVO.setRank(rank);
		
		PlayerCampBattleRecord playerCampBattleRecord = this.campBattleManager.getPlayerCampBattleRecord(playerId);
		playerScoreVO.setCampTitle(playerCampBattleRecord.getCampTitle());
		playerScoreVO.setTotalScore(playerCampBattleRecord.getTotalScore());
		playerScoreVO.setJoins(playerCampBattleRecord.getJoins());
		playerScoreVO.setSalaryReward(playerCampBattleRecord.getSalaryReward());
		playerScoreVO.setSuitReward(playerCampBattleRecord.getSuitReward());
		
		resultMap.put("playerScoreVO", playerScoreVO);
		resultMap.put("battleHis", battleHis.toArray());
		
		return CampBattleConstant.SUCCESS;
	}

	
	public void doEnterCampBattle(final UserDomain userDomain) {
		if(userDomain == null || userDomain.getMapId() != CampBattleRule.CAMP_BATTLE_MAPID){
			return ;
		}
		
		scheduler.scheduleWithDelay(new ScheduledTask() {
			
			public void run() {
				long playerId = userDomain.getPlayerId();
				try {
					PetDomain fightingPet = petManager.getFightingPet(playerId);
					if(fightingPet != null && fightingPet.getPet().isStatus(PetStatus.FIGHTING) ){
						ResultObject<Long> resultObject = petFacade.goBack(playerId);
						if(resultObject != null && resultObject.isOK() ){
							Map<String, Object> resultMap = new HashMap<String, Object>(2);
							resultMap.put(ResponseKey.RESULT, resultObject.getResult());
							resultMap.put(ResponseKey.PET_ID, resultObject.getValue());
							Response response = Response.defaultResponse(Module.PET, PetCmd.PET_BACK, resultMap);
							pusher.pushMessage(playerId, response);
						}
					}
					
				} catch (Exception e) {
					logger.error("{}", e);
				}
				
				try {
					PlayerCampBattle playerCampBattle = playerCampBattleMap.get(playerId);
					if(playerCampBattle != null){
						CampBattlePushHelper.pushBattleInfo(campBattles.values(), noCampPointId, playerCampBattle);
					}
				} catch (Exception e) {
					logger.error("{}", e);
				}
			}
			
			public String getName() { return "角色进入场景推送回收家将及战场信息"; }
			
		}, 2000);
	}

	
	public Camp getWinCamp() {
		if(this.preCampBattleDate == null){
			List<Date> campBattleDates = this.getCampBattleDates();
			this.preCampBattleDate = campBattleDates != null && campBattleDates.size() > 0 ? campBattleDates.get(0) : null;
		}
		Date date = this.preCampBattleDate;
		if(date == null){
			return Camp.NONE;
		}
		
		Camp[] camps = Camp.values();
		for(Camp camp : camps){
			if(camp == Camp.NONE){
				continue;
			}
			CampBattleHistory campBattleHistory = this.getCampBattleHistory(date, camp);
			if(campBattleHistory != null && campBattleHistory.isWin()){
				return camp;
			}
		}
		return Camp.NONE;
	}
}
