package com.yayo.warriors.module.dungeon.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.dungeon.constant.DungeonConstant.*;
import static com.yayo.warriors.module.pack.type.BackpackType.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.adapter.MonsterService;
import com.yayo.warriors.basedb.model.BulletinConfig;
import com.yayo.warriors.basedb.model.DungeonConfig;
import com.yayo.warriors.basedb.model.DungeonPoint;
import com.yayo.warriors.basedb.model.DungeonProps;
import com.yayo.warriors.basedb.model.DungeonStoryConfig;
import com.yayo.warriors.basedb.model.EquipConfig;
import com.yayo.warriors.basedb.model.MonsterDungeonConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.NoticePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.dungeon.facade.DungeonFacade;
import com.yayo.warriors.module.dungeon.manager.DungeonManager;
import com.yayo.warriors.module.dungeon.model.Dungeon;
import com.yayo.warriors.module.dungeon.rule.DungeonRule;
import com.yayo.warriors.module.dungeon.storyverify.SotryDungeonVerify;
import com.yayo.warriors.module.dungeon.types.DungeonType;
import com.yayo.warriors.module.dungeon.vo.DungeonVo;
import com.yayo.warriors.module.duntask.facade.DungeonTaskFacade;
import com.yayo.warriors.module.duntask.manager.DunTaskManager;
import com.yayo.warriors.module.logger.helper.LoggerPropsHelper;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Orient;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.map.manager.GameMapManager;
import com.yayo.warriors.module.map.rule.MapRule;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.manager.MonsterManager;
import com.yayo.warriors.module.notice.rule.NoticeRule;
import com.yayo.warriors.module.notice.type.NoticeID;
import com.yayo.warriors.module.notice.type.NoticeType;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.CreateResult;
import com.yayo.warriors.module.props.rule.EquipHelper;
import com.yayo.warriors.module.task.facade.TaskMainFacade;
import com.yayo.warriors.module.task.manager.EscortTaskManager;
import com.yayo.warriors.module.team.manager.TeamManager;
import com.yayo.warriors.module.team.model.Team;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.module.user.type.Job;
import com.yayo.warriors.socket.vo.ChangeScreenVo;
import com.yayo.warriors.type.ElementType;
import com.yayo.warriors.type.GoodsType;

/**
 * 副本实现类
 * @author liuyuhua
 */
@Component
public class DungeonFacadeImpl implements DungeonFacade {
	
	@Autowired
	private DbService dbService;
	@Autowired
	private DungeonManager dungeonManager;
	@Autowired
	private GameMapManager gameMapManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private MonsterManager monsterManager;
	@Autowired
	private MonsterService monsterDungeonService;
	@Autowired
	private TeamManager teamManager;
	@Autowired
	private DungeonTaskFacade dungeonTaskFacade;
	@Autowired
	private DunTaskManager dunTaskManager;
	@Autowired
	private TaskMainFacade taskMainFacade;
	@Autowired
	private PetManager petManager;
	@Autowired
	private MapFacade mapFacade;
	@Autowired
	private SotryDungeonVerify sotryDungeonVerify;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private EscortTaskManager escortTaskManager;
	
	
	/** 日志格式 */
	private final Logger logger = LoggerFactory.getLogger(getClass());

	
	public PlayerDungeon getPlayerDungeon(long playerId) {
		if(playerId == -1){
			return null;
		}
		return dungeonManager.getPlayerDungeon(playerId);
	}
	
	
	public ResultObject<DungeonVo> loadDungeon(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		PlayerDungeon playerDungeon = dungeonManager.getPlayerDungeon(battle);
		if(playerDungeon == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND); 
		}
		
		if(!playerDungeon.isDungeonStatus()){
			return ResultObject.ERROR(PLAYER_NOT_EXIST_DUNGEON); 
		}
		
		Dungeon dungeon = dungeonManager.getDungeon(playerDungeon.getDungeonId());
		if(dungeon == null){
			return ResultObject.ERROR(PLAYER_NOT_EXIST_DUNGEON); 
		}
		return ResultObject.SUCCESS(DungeonVo.valueOf(dungeon));
	}

	
	public ResultObject<ChangeScreenVo> enterDungeon(long playerId,int dungeonBaseId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		DungeonConfig config = dungeonManager.getDungeonConfig(dungeonBaseId);
		if(config == null){
			logger.error("玩家[{}],创建[{}]副本,基础数据不存在.",playerId,dungeonBaseId);
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		
		if(!config.isOpen()){//判断副本是否开放
			return ResultObject.ERROR(DUNGEON_UNOPENED);
		}
		
		PlayerDungeon playerDungeon = dungeonManager.getPlayerDungeon(playerId);
		if(playerDungeon == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle.getLevel() < config.getLevelLimit()){
			return ResultObject.ERROR(ENTER_DUNGEON_LEVEL_LIMIT);
		}
		
		if(playerDungeon.getEnterDungeonTimes(dungeonBaseId) >= config.getEnterNum()){
			return ResultObject.ERROR(OVER_ENTER_DUNGEON_NUMBER);
		}
		
		if(escortTaskManager.isEscortStatus(battle)){ //押镖状态无法进入副本
			return ResultObject.ERROR(ESCORT_STATUS_CANT_ENTER);
		}
		
		if(playerDungeon.isDungeonStatus()){
			Dungeon dungeon = dungeonManager.getDungeon(playerDungeon.getDungeonId());
			if(dungeon != null){
				if(!dungeon.isComplete() && dungeon.getType() != DungeonType.HIGH_RICH){
					logger.error("玩家[{}],副本[{}],没有完成无法进入下一个副本",playerId,playerDungeon.getDungeonBaseId());
					return ResultObject.ERROR(CURRENT_DUNGEON_NOT_SUCCESS);
				}
				ChainLock lock = LockUtils.getLock(dungeon,playerDungeon);
				try {
					lock.lock();
					if(playerDungeon.getDungeonBaseId() == dungeonBaseId){
						return ResultObject.ERROR(CANT_ENTER_SAME_DUNGEON);
					}
					dungeon.addLeave(playerDungeon.getId());
				}finally{
					lock.unlock();
				}
			}
		}
		
		
		Team team = teamManager.getPlayerTeam(userDomain.getId());//队伍(组队)
		if(config.isStoryDungeon()){ //剧情副本判断
			if(team != null){
				//组队状态下无法进入剧情副本
				return ResultObject.ERROR(TEAM_MODEL_CANT_ENTER);
			}
			
			if(!playerDungeon.canEnterStory(dungeonBaseId)){
				return ResultObject.ERROR(STORY_CANT_ENTER);
			}
			
			DungeonStoryConfig storyConfig = dungeonManager.getDungeonStoryConfig(config.getId());
			if(!sotryDungeonVerify.verify(playerId, storyConfig.getStoryVerifies())){
				return ResultObject.ERROR(STORY_CANT_ENTER);
			}
		}
		
		ResultObject<ChangeScreenVo> result = null;
		if(config.isTreasure()){//是否藏宝图
			result = this.enterGeneral(userDomain, playerDungeon, config);
			
		} else {
			/** 判断是否满足进入条件*/
			int memberSize = 1; //自己也算一个
			if(team != null){
				memberSize += (team.getMembers().size() - 1);//不计算自己
			}
			/** end判断组队进入条件*/
			
			if(config.getMinNumLimit() > memberSize){
				return ResultObject.ERROR(MULTI_DUNGEON_TEAM_MIN_LIMIT);
			}
			if(config.getMaxNumLimit() < memberSize){
				return ResultObject.ERROR(OVER_DUNGEON_ENTER_PLAYER);
			}
			
			if(team == null){
				result = this.enterGeneral(userDomain, playerDungeon, config);
			}else{
				result = this.entryMulti(userDomain, playerDungeon, config);
			}
			
			if(result.getResult() == SUCCESS){ //进入成功,接受副本任务
				dungeonTaskFacade.accept(playerId, config.getEnterTaskList());
				taskMainFacade.updateCompleteInstanceTask(playerId, dungeonBaseId);
			}
			
		}
		
		return result;
	}
	
	
	public ResultObject<ChangeScreenVo> exitDungeon(long playerId) {
		return exitDungeon(playerId, 0, 0, 0);
	}
	
	
	public ResultObject<ChangeScreenVo> exitDungeon(long playerId, int mapId, int x, int y) {
		UserDomain userDomain = this.userManager.getUserDomain(playerId);
		PlayerDungeon playerDungeon = this.getPlayerDungeon(playerId);
		if(userDomain == null || playerDungeon == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		if(!playerDungeon.isDungeonStatus()){
			return ResultObject.ERROR(PLAYER_NOT_EXIST_DUNGEON);
		}
		long dungeonId = playerDungeon.getDungeonId(); //副本增量ID
		
		Dungeon dungeon = dungeonManager.getDungeon(dungeonId);
		if(dungeon == null){
			return ResultObject.ERROR(FAILURE);
		}
		
		DungeonConfig config = dungeon.getDungeonConfig();
		if(config == null){
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		
		boolean revive = false;
		if(battle.isDead()){
			revive = true;
		}
		
		ChangeScreenVo vo = null;
		GameMap gameMap = null;
		ChainLock lock = LockUtils.getLock(battle,dungeon,playerDungeon);
		try {
			lock.lock();
			battle.setHp(battle.getHpMax()); //退出副本HP置满
			playerDungeon.leaveDungeon();    //离开副本
			dungeon.addLeave(player.getId());//添加退出的玩家 
			DungeonPoint interruptPoint = null;
			if(mapId > 0 && x >= 0 && y >= 0){
				interruptPoint = new DungeonPoint();
				interruptPoint.setMapId(mapId);
				interruptPoint.setX(x);
				interruptPoint.setY(y);
			} else {//这里有点古怪，但是副本退出必须要随机NPC身边的10个点
				DungeonPoint configPoint = config.getInterruptPoint();
				gameMap =  this.gameMapManager.getGameMapById(configPoint.getMapId(), userDomain.getBranching());
				if(gameMap != null){
					Point point = gameMap.getRandomCanStandPoint(configPoint.getX(), configPoint.getY(), 10);
					if(point != null){
						interruptPoint = new DungeonPoint();
						interruptPoint.setMapId(configPoint.getMapId());
						interruptPoint.setX(point.getX());
						interruptPoint.setY(point.getY());
					}
				}else{
					interruptPoint = config.getInterruptPoint();
				}
			}
			
			gameMap = this.gameMapManager.getGameMapById(interruptPoint.getMapId(), userDomain.getBranching());
			if(gameMap == null){
				interruptPoint = new DungeonPoint();
				interruptPoint.setMapId(MapRule.DEFAUL_REVIVE_MAPID);
				interruptPoint.setX(MapRule.DEFAUL_REVIVE_X);
				interruptPoint.setY(MapRule.DEFAUL_REVIVE_Y);
				gameMap = this.gameMapManager.getGameMapById(interruptPoint.getMapId(), userDomain.getBranching());
			}
			vo = mapFacade.leaveMap(userDomain, gameMap, interruptPoint.getX(), interruptPoint.getY());
			
			//退出副本,家将也要退出
			PetDomain petDomain = petManager.getFightingPet(userDomain.getId());
			if(petDomain != null){
				petDomain.changeMap(gameMap, interruptPoint.getX(), interruptPoint.getY());
			}
			
			if(dungeon.filterPlayers().isEmpty()){ //副本里面没有玩家,直接回收副本
				this.removeDungeon(dungeonId,player.getBranching());
			}
			
		} catch (Exception e) {
			logger.error("玩家[{}],退出副本,异常:{}",playerId,e);
		}finally{
			lock.unlock();
		}
		
		dunTaskManager.removeAll(playerId); //删除玩家所有副本任务
		dbService.submitUpdate2Queue(playerDungeon);
		
		Collection<Long> viewPlayers = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
		if(revive){
			UserPushHelper.pushPlayerRecurrent(userDomain.getUnitId(), battle.getHp(), viewPlayers);
		}else{
			UserPushHelper.pushAttribute2AreaMember(playerId, Arrays.asList(playerId), Arrays.asList(userDomain.getUnitId()), AttributeKeys.HP,AttributeKeys.HP_MAX);
		}
		
		return ResultObject.SUCCESS(vo);
	}

	/**
	 * 进入多人副本
	 * @param userDomain      玩家域对象
	 * @param playerDungeon   玩家副本对象
	 * @param config          副本配置
	 * @return {@link ChangerScreenVo} 专场对象
	 */
	private ResultObject<ChangeScreenVo> entryMulti(UserDomain userDomain,PlayerDungeon playerDungeon,DungeonConfig config){
		Player player = userDomain.getPlayer();
		Team team = teamManager.getPlayerTeam(player.getId());
		if(team == null){
			return ResultObject.ERROR(MULTI_DUNGEON_TEAM_NOT_FOUND);
		}
		
	 	if(player.getId() == team.getLeaderId()){
	 		return this.enterGeneral(userDomain, playerDungeon, config); //如果是队长,直接创建副本
	 	}else{
	 		PlayerDungeon leaderDungeon = dungeonManager.getPlayerDungeon(team.getLeaderId());
	 		if(leaderDungeon == null){
	 			return ResultObject.ERROR(PLAYER_NOT_FOUND);
	 		}
	 		
			if(!leaderDungeon.isDungeonStatus()){
				return ResultObject.ERROR(MULTI_DUNGEON_LEADER_NOT_CREATE);
			}
			
			long dungeonId = leaderDungeon.getDungeonId();
			int dungeonBaseId = leaderDungeon.getDungeonBaseId();
			
			Dungeon dungeon = dungeonManager.getDungeon(dungeonId);
			if(dungeon == null){
				return ResultObject.ERROR(MULTI_DUNGEON_LEADER_NOT_CREATE);
			}
			
			if(dungeonBaseId != config.getId()){
				return ResultObject.ERROR(WAITTING_LEADER);
			}
			
			GameMap gameMap = gameMapManager.getTemporaryMap(dungeonId,player.getBranching());
			if(gameMap == null){
				return ResultObject.ERROR(MAP_NOT_FOUND);
			}
			
			int targetX = config.getEnterPoint().getX();
			int targetY = config.getEnterPoint().getY();
			ChangeScreenVo changeScreenVo = mapFacade.leaveMap(userDomain, gameMap, targetX, targetY);
			
			ChainLock lock = LockUtils.getLock(playerDungeon,dungeon);
			try {
				lock.lock();
				if(playerDungeon.getDungeonBaseId() == config.getId()){
					ResultObject.ERROR(CANT_ENTER_SAME_DUNGEON);
				}
				playerDungeon.enterDungeon(dungeonId, dungeonBaseId);
				//进入副本,家将也要进入
				PetDomain petDomain = petManager.getFightingPet(userDomain.getId());
				if(petDomain != null){
					petDomain.changeMap(gameMap, config.getEnterPoint().getX(), config.getEnterPoint().getY());
				}
				
				dungeon.addEntrant(player.getId());//增加进入副本的人数
			} catch (Exception e) {
				logger.error("玩家[{}],进入[{}]副本,异常{}",new Object[]{player.getId(),config.getId(),e});
				return ResultObject.ERROR(FAILURE);
			}finally{
				lock.unlock();
			}
			
			this.dbService.submitUpdate2Queue(playerDungeon);
			changeScreenVo.setDungeonId(dungeonId);
			changeScreenVo.setDungeonBaseId(dungeonBaseId);
			return ResultObject.SUCCESS(changeScreenVo);
	 	}
	}
	
	
	/**
	 * 进入(创建)一般副本
	 * @param userDomain      玩家域对象
	 * @param playerDungeon   玩家副本对象
	 * @param config          副本配置
	 * @return {@link ChangeScreenVo} 转场对象
	 */
	private ResultObject<ChangeScreenVo> enterGeneral(UserDomain userDomain,PlayerDungeon playerDungeon,DungeonConfig config){
		Player player = userDomain.getPlayer();
		long dungeonId = 0;//副本增量ID
		
		if(playerDungeon.isDungeonStatus()){ //防止千层塔隔层问题,当所有队员不在同一层时，无法进入下一层
			Team team = teamManager.getPlayerTeam(userDomain.getId());
			if(team != null){
				for(long memberId : team.getMembers()){
					PlayerDungeon memberDungeon = dungeonManager.getPlayerDungeon(memberId);
					if(memberDungeon.getDungeonId() != playerDungeon.getDungeonId()){
						return ResultObject.ERROR(TEAM_MEMBER_NOT_ALL_COMPLETE);
					}
				}
			}
		}
	
		ChangeScreenVo changeScreenVo = null;
		ChainLock lock = LockUtils.getLock(playerDungeon);
		try {
			lock.lock();
			if(playerDungeon.getDungeonBaseId() == config.getId()){
				ResultObject.ERROR(CANT_ENTER_SAME_DUNGEON);
			}
			Dungeon dungeon = dungeonManager.createDungeon(userDomain, config);
			dungeon.addEntrant(player.getId());//加入进入该副本的玩家
			dungeonId = dungeon.getDungeonId();
			GameMap gameMap = gameMapManager.createTemporaryMap(dungeon.getMapId(), dungeon.getDungeonId(),player.getBranching());
			if(gameMap == null){
				logger.error("玩家[{}]创建副本[{}],地图[{}]不存在",new Object[]{userDomain.getId(),dungeon.getDungeonId(),dungeon.getMapId()});
				return ResultObject.ERROR(MAP_NOT_FOUND);
			}
			
			int targetX = config.getEnterPoint().getX();
			int targetY = config.getEnterPoint().getY();
			changeScreenVo = mapFacade.leaveMap(userDomain, gameMap, targetX, targetY);
			if(changeScreenVo == null){
				return ResultObject.ERROR(DUNGEON_MAP_NOT_FOUND);
			}
			
			//进入副本,家将也要进入
			PetDomain petDomain = petManager.getFightingPet(userDomain.getId());
			if(petDomain != null){
				petDomain.changeMap(gameMap, config.getEnterPoint().getX(), config.getEnterPoint().getY());
			}
			
			List<MonsterDungeonConfig> monsterConfigs = monsterDungeonService.getMonsters4Round(dungeon.getBaseId(), DungeonRule.INIT_DUNGEON_ROUND);
			if(monsterConfigs != null){
				List<Long> monsterIds = new ArrayList<Long>();//怪物
				for(MonsterDungeonConfig monsterConfig : monsterConfigs){
					MonsterDomain monsterDomain = monsterManager.addDungeonMonster(gameMap, monsterConfig, dungeon.getDungeonId(), player.getBranching());
					if(monsterDomain != null){
						if( monsterDomain.getMonsterBattle().isDead() ){
							logger.error("增加副本新怪物时出错(HP为0), 怪物基础id:{} 怪物战斗属性id:{}", monsterConfig.getId(), monsterConfig.getMonsterFightId() );
							continue;
						}
						monsterIds.add(monsterDomain.getMonster().getId());
					}
				}
				dungeon.addDungeonMonster(DungeonRule.INIT_DUNGEON_ROUND, monsterIds);
			}
			playerDungeon.enterDungeon(dungeon.getDungeonId(), dungeon.getBaseId());
		} catch (Exception e) {
			logger.error("玩家[{}],创建[{}]副本,异常{}",new Object[]{player.getId(),config.getId(),e});
			return ResultObject.ERROR(FAILURE);
		}finally{
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(playerDungeon);
		changeScreenVo.setDungeonId(dungeonId);
		changeScreenVo.setDungeonBaseId(config.getId());
		return ResultObject.SUCCESS(changeScreenVo);
	}

	
	public void removeDungeon(long dungeonId,int branching) {
		try {
			dungeonManager.removeDungeon(dungeonId);
		} catch (Exception e) {
			logger.error("回收副本对象时出错：{}", e);
		}
		try {
			monsterManager.cleanDungeonMonster(dungeonId);
		} catch (Exception e) {
			logger.error("回收副本怪物时出错：{}", e);
		}
		try{
			gameMapManager.removeTemporaryMap(dungeonId, branching);
		} catch (Exception e) {
			logger.error("回收副本地图时出错：{}", e);
		}
	}
	
	
	public  ResultObject<String> verifyStory(long playerId, String storyIds) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(storyIds == null || storyIds.isEmpty()){
			return ResultObject.ERROR(FAILURE);
		}
		
		String[] dungeonStoryIds = storyIds.split(Tools.DELIMITER_INNER_ITEM);
		StringBuffer stringBuffer = new StringBuffer();
		
		for(String strId : dungeonStoryIds){
			DungeonStoryConfig config = dungeonManager.getDungeonStoryConfig(Integer.parseInt(strId));
			if(config == null){
				logger.error("玩家[{}],验证剧情副本[{}],基础数据不存在!",playerId,strId);
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			}
			
			if(sotryDungeonVerify.verify(playerId, config.getStoryVerifies())){
				stringBuffer.append(strId).append("_");
			}
		}
		return ResultObject.SUCCESS(stringBuffer.toString());
	}
	
	
	public int rewardStory(long playerId, int dungeonBaseId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return PLAYER_NOT_FOUND;
		}

		PlayerDungeon playerDungeon = dungeonManager.getPlayerDungeon(playerId);
		if (playerDungeon == null) {
			return PLAYER_NOT_FOUND;
		}

		DungeonConfig config = dungeonManager.getDungeonConfig(dungeonBaseId);
		if (config == null) {
			return BASEDATA_NOT_FOUND;
		}

		Fightable fightable = config.getRewardFightable();
		if (fightable == null) {
			return BASEDATA_NOT_FOUND;
		}

		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		ResultObject<CreateResult<UserProps, UserEquip>> initResult = initDungeonRewards(battle,
				config);
		if (initResult.getResult() < SUCCESS) {
			return initResult.getResult();
		}

		CreateResult<UserProps, UserEquip> itemRewards = null;
		CreateResult<UserProps, UserEquip> createResult = initResult.getValue();
		List<UserProps> userPropsList = createResult == null ? new ArrayList<UserProps>(0) : createResult.getCollections1();
		List<UserEquip> userEquipList = createResult == null ? new ArrayList<UserEquip>(0) : createResult.getCollections2();
		int backSize = propsManager.getBackpackSize(playerId, DEFAULT_BACKPACK);
		ChainLock lock = LockUtils.getLock(playerDungeon, battle, player.getPackLock());
		try {
			lock.lock();
			if (!playerDungeon.isRewardStory(dungeonBaseId)) {
				return STORY_REWARD_IS_FINISH;
			}

			if (createResult != null) {
				/* 首先要获的奖励物品和道具以后再开始判断背包和增加背包临时参量的值,否则容易出现异常返回 */
				int totalSize = backSize + userEquipList.size() + userPropsList.size();
				if (!player.canAddNew2Backpack(totalSize, DEFAULT_BACKPACK)) {
					return BACKPACK_FULLED;
				}

				itemRewards = propsManager.createUserEquipAndUserProps(userPropsList, userEquipList);
				propsManager.put2UserEquipIdsList(playerId, DEFAULT_BACKPACK, itemRewards.getCollections2());
				propsManager.put2UserPropsIdsList(playerId, DEFAULT_BACKPACK, itemRewards.getCollections1());
			}

			playerDungeon.finish4Story(dungeonBaseId); // 已经领取完成奖励
			if (doRewardAttribute(fightable, battle)) {
				dbService.submitUpdate2Queue(battle);
			}
			
			battle.setFlushable(Flushable.FLUSHABLE_NORMAL); // 刷新属性
		} catch (Exception e) {
			logger.error("玩家[{}],领取剧情副本[{}]奖励异常:e", new Object[] { playerId, dungeonBaseId, e });
			return FAILURE;
		} finally {
			lock.unlock();
		}

		dbService.updateEntityIntime(playerDungeon);
		if (itemRewards != null) {
			Collection<BackpackEntry> entries = new ArrayList<BackpackEntry>();
			if (itemRewards.getCollections1() != null && !itemRewards.getCollections1().isEmpty()) {
				entries.addAll(itemRewards.getCollections1());
			}
			if (itemRewards.getCollections2() != null && !itemRewards.getCollections2().isEmpty()) {
				entries.addAll(itemRewards.getCollections2());
			}
			MessagePushHelper.pushUserProps2Client(playerId, DEFAULT_BACKPACK, false, entries);
			
			//剧情副本奖励道具收入
			LoggerGoods[] loggerGoodsArray = LoggerPropsHelper.convertLoggerGoods(Orient.INCOME, userPropsList, userEquipList, null, null);
			GoodsLogger.goodsLogger(player, Source.COMPLETE_STORY_DUNGEON , loggerGoodsArray);
		}
		
		///发公告
		BulletinConfig bulletinConfig = NoticePushHelper.getConfig(NoticeID.COMPLETE_STORY_DUNGEON, BulletinConfig.class);
		if (bulletinConfig != null) {
			Map<String, Object> params = new HashMap<String, Object>(3);
			params.put(NoticeRule.playerId, playerId);
			params.put(NoticeRule.playerName, userDomain.getPlayer().getName());
			params.put(NoticeRule.dungeonId, dungeonBaseId);
			NoticePushHelper.pushNotice(NoticeID.COMPLETE_STORY_DUNGEON, NoticeType.HONOR, params, bulletinConfig.getPriority());
		}

		return SUCCESS;
	}

	/**
	 * 角色属性奖励
	 * 
	 * @param  fightable		战斗属性集合
	 * @param  battle			角色战斗对象
	 * @return {@link Boolean} 	true-属性发生改变, false-属性未发生改变
	 */
	private boolean doRewardAttribute(Fightable fightable, PlayerBattle battle) {
		if(fightable == null || fightable.isEmpty()) {
			return false;
		}
		
		/* 用于异常手动回滚 --- 考虑是否异常回滚*/
		/** 一级属性*/
		battle.setStrength(battle.getStrength() + fightable.getAttribute(AttributeKeys.STRENGTH));
		battle.setDexerity(battle.getDexerity() + fightable.getAttribute(AttributeKeys.DEXERITY));
		battle.setIntellect(battle.getIntellect() + fightable.getAttribute(AttributeKeys.INTELLECT));
		battle.setConstitution(battle.getConstitution() + fightable.getAttribute(AttributeKeys.CONSTITUTION));
		battle.setSpirituality(battle.getSpirituality() + fightable.getAttribute(AttributeKeys.SPIRITUALITY));
		
		/** 二级属性 */
		battle.setHit(battle.getHit() + fightable.getAttribute(AttributeKeys.HIT));
		battle.setDodge(battle.getDodge() + fightable.getAttribute(AttributeKeys.DODGE));
		battle.setMoveSpeed(battle.getMoveSpeed() + fightable.getAttribute(AttributeKeys.MOVE_SPEED));
		battle.setTheurgyAttack(battle.getTheurgyAttack() + fightable.getAttribute(AttributeKeys.THEURGY_ATTACK));
		battle.setTheurgyDefense(battle.getTheurgyDefense() + fightable.getAttribute(AttributeKeys.THEURGY_DEFENSE));
		battle.setTheurgyCritical(battle.getTheurgyCritical() + fightable.getAttribute(AttributeKeys.THEURGY_CRITICAL));
		battle.setPhysicalAttack(battle.getPhysicalAttack() + fightable.getAttribute(AttributeKeys.PHYSICAL_ATTACK));
		battle.setPhysicalDefense(battle.getPhysicalDefense() + fightable.getAttribute(AttributeKeys.PHYSICAL_DEFENSE));
		battle.setPhysicalCritical(battle.getPhysicalCritical() + fightable.getAttribute(AttributeKeys.PHYSICAL_CRITICAL));
		battle.setAddHpMax(battle.getAddHpMax() + fightable.getAttribute(AttributeKeys.HP_MAX));
		battle.setAddMpMax(battle.getAddMpMax() + fightable.getAttribute(AttributeKeys.MP_MAX));
		return true;
	}
	
	/**
	 * 初始化副本奖励信息
	 * 
	 * @param  player					角色对象
	 * @param  config					地下城配置对象
	 * @return {@link ResultObject}		返回值信息
	 */
	private ResultObject<CreateResult<UserProps, UserEquip>> initDungeonRewards(PlayerBattle battle, DungeonConfig config) {
		List<DungeonProps> dungeonPorpslist = config.getItemReaps();
		if(dungeonPorpslist == null || dungeonPorpslist.isEmpty()) {
			return ResultObject.SUCCESS();
		}
		
		long playerId = battle.getId();
		List<UserProps> propsList = new ArrayList<UserProps>(); 
		List<UserEquip> equipList = new ArrayList<UserEquip>();
		for (DungeonProps dungeonProps : dungeonPorpslist) {
			int baseId = dungeonProps.getPropsId();
			int goodsType = dungeonProps.getGoodsType();
			if (goodsType == GoodsType.PROPS) {
				PropsConfig propsConfig = propsManager.getPropsConfig(baseId);
				if (propsConfig == null) {
					return ResultObject.ERROR(BASEDATA_NOT_FOUND);
				}
				
				if(propsConfig.getJob() == Job.COMMON.ordinal() || propsConfig.getJob() == battle.getJob().ordinal()){
					int rewardCount = dungeonProps.getNumber();
					propsList.add(UserProps.valueOf(playerId, DEFAULT_BACKPACK, rewardCount, propsConfig, true));
				}
			} else if (goodsType == GoodsType.EQUIP) {
				EquipConfig equipConfig = propsManager.getEquipConfig(baseId);
				if (equipConfig == null) {
					return ResultObject.ERROR(BASEDATA_NOT_FOUND);
				}
				if (equipConfig.getJob() == battle.getJob().ordinal()) { // 装备需要区分职业
					equipList.add(EquipHelper.newUserEquip(playerId, equipConfig, DEFAULT_BACKPACK, true));
				}
			}
		}
		return ResultObject.SUCCESS(CreateResult.valueOf(propsList, equipList));
	}
}