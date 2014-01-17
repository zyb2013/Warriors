
package com.yayo.warriors.module.map.facade.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.apache.mina.util.ConcurrentHashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.event.EventBus;
import com.yayo.common.scheduling.Scheduled;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.push.Pusher;
import com.yayo.common.utility.TimeConstant;
import com.yayo.warriors.basedb.model.CampChangePoint;
import com.yayo.warriors.basedb.model.CoolTimeConfig;
import com.yayo.warriors.basedb.model.MapConfig;
import com.yayo.warriors.basedb.model.MonsterConfig;
import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.DungeonPushHelper;
import com.yayo.warriors.common.helper.MapPushHelper;
import com.yayo.warriors.common.helper.TeamPushHelper;
import com.yayo.warriors.common.helper.WorldPusherHelper;
import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.event.EnterScreenEvent;
import com.yayo.warriors.event.EnterScreenReceiver;
import com.yayo.warriors.module.alliance.entity.PlayerAlliance;
import com.yayo.warriors.module.alliance.facade.AllianceFacade;
import com.yayo.warriors.module.alliance.manager.AllianceManager;
import com.yayo.warriors.module.battlefield.facade.BattleFieldFacade;
import com.yayo.warriors.module.buffer.entity.UserBuffer;
import com.yayo.warriors.module.campbattle.facade.CampBattleFacade;
import com.yayo.warriors.module.campbattle.rule.CampBattleRule;
import com.yayo.warriors.module.chat.facade.ChannelFacade;
import com.yayo.warriors.module.chat.model.Channel;
import com.yayo.warriors.module.chat.type.ChatChannel;
import com.yayo.warriors.module.cooltime.entity.UserCoolTime;
import com.yayo.warriors.module.cooltime.manager.CoolTimeManager;
import com.yayo.warriors.module.cooltime.model.CoolTime;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.dungeon.facade.DungeonFacade;
import com.yayo.warriors.module.dungeon.manager.DungeonManager;
import com.yayo.warriors.module.dungeon.model.Dungeon;
import com.yayo.warriors.module.fight.helper.BufferHelper;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.map.constant.MapConstant;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.GameScreen;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.map.manager.GameMapManager;
import com.yayo.warriors.module.map.model.ChangePoint;
import com.yayo.warriors.module.map.rule.MapRule;
import com.yayo.warriors.module.map.types.MaskTypes;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.manager.MonsterManager;
import com.yayo.warriors.module.monster.model.MonsterBattle;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.props.type.PropsChildType;
import com.yayo.warriors.module.server.listener.DataRemoveListener;
import com.yayo.warriors.module.server.listener.LogoutListener;
import com.yayo.warriors.module.task.manager.EscortTaskManager;
import com.yayo.warriors.module.team.facade.TeamFacade;
import com.yayo.warriors.module.team.model.Team;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.entity.PlayerMotion;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.model.UserDomain.SpireQueueType;
import com.yayo.warriors.module.user.rule.PlayerRule;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.map.MapCmd;
import com.yayo.warriors.socket.vo.ChangeScreenVo;
import com.yayo.warriors.type.ElementType;
import com.yayo.warriors.type.IndexName;
import com.yayo.warriors.util.GameConfig;


@Component
public class MapFacadeImpl implements MapFacade, MapConstant,LogoutListener, DataRemoveListener {
	
	@Autowired
	private Pusher pusher;
	@Autowired
	private EventBus eventBus;
	@Autowired
	private DbService dbService;
	@Autowired
	private PetManager petManager;
	@Autowired
	private UserManager userManager ;
	@Autowired
	private MapPushHelper mapHelper; 
	@Autowired
	private DungeonFacade dungeonFacade;
	@Autowired
	private DungeonManager dungeonManager;
	@Autowired
	private GameMapManager gameMapManager ;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private EscortTaskManager escortManager;
	@Autowired
	private WorldPusherHelper worldPusherHelper;
	@Autowired
	private DungeonPushHelper dungeonPushHelper;
	@Autowired
	private MonsterManager monsterManager;
	@Autowired
	private CampBattleFacade campBattleFacade;
	@Autowired
	private EnterScreenReceiver enterScreenReceiver;
	@Autowired
	private BattleFieldFacade battleFieldFacade;
	@Autowired
	private AllianceFacade allianceFacade;
	@Autowired
	private ChannelFacade channelFacade;
	@Autowired
	private TeamFacade teamFacade;
	@Autowired
	private AllianceManager allianceManager;
	@Autowired
	private CoolTimeManager coolTimeManager;
	
	private final ConcurrentMap<Long, GameMap> changeMapSpires = new ConcurrentHashMap<Long, GameMap>(5);
	private final ConcurrentMap<Integer, CopyOnWriteArrayList<GameMap> > tempGameMap = new ConcurrentHashMap<Integer, CopyOnWriteArrayList<GameMap>>();
	private final ConcurrentMap<Long, GameMap> playerTempMap = new ConcurrentHashMap<Long, GameMap>(5);
	private final ConcurrentMap<Long, ConveneInvite> playerConveneInvite = new ConcurrentHashMap<Long, ConveneInvite>(5);
	
	
	public int enterScreen(Long playerId) {
		try{
			UserDomain userDomain = userManager.getUserDomain(playerId);
			PlayerMotion playerMotion = userDomain.getMotion();
			Player player = userDomain.getPlayer() ;
			PlayerDungeon playerDungeon = dungeonFacade.getPlayerDungeon(playerId);
	
			GameMap gameMap = getGameMap(playerId, userDomain, playerMotion, playerDungeon, player.getBranching(), true);
			
			userDomain.changeMap(gameMap, playerMotion.getX(), playerMotion.getY() );
			
			PetDomain petDomain = petManager.getFightingPet(userDomain.getId());
			if(petDomain != null){
				petDomain.changeMap(gameMap, userDomain.getX(), userDomain.getY());
			}
			
			this.changeMap(userDomain, gameMap, userDomain.getX(), userDomain.getY());
			if(!gameMap.isPathPass( userDomain.getMotion().getX(),  userDomain.getMotion().getY())){
			}
			
			processEnterScreenEvent(userDomain, gameMap);
			
			campBattleFacade.doEnterCampBattle(userDomain);
			battleFieldFacade.doEnterBattleField(userDomain);
			
			dbService.submitUpdate2Queue(playerMotion);
			
			return SUCCESS;
		}catch(Exception e){
			return FAILURE;
		}

	}


	private GameMap getGameMap(Long playerId, UserDomain userDomain,
			PlayerMotion playerMotion, PlayerDungeon playerDungeon,
			final int branching, boolean createNewMap) {
		GameMap gameMap = null;
		if(playerDungeon.isDungeonStatus()){
			gameMap = gameMapManager.getTemporaryMap(playerDungeon.getDungeonId(),userDomain.getBranching());
		} else {
			gameMap = this.ifFullCreateNewMap(userDomain, branching, createNewMap);
		}
		if(gameMap == null){
			gameMap = gameMapManager.getGameMapById(MapRule.DEFAUL_REVIVE_MAPID, branching);
			playerMotion.changeMap(MapRule.DEFAUL_REVIVE_MAPID, MapRule.DEFAUL_REVIVE_X, MapRule.DEFAUL_REVIVE_Y);
			userDomain.changeMap(gameMap, playerMotion.getX(), playerMotion.getY());
		}
		return gameMap;
	}


	private void processEnterScreenEvent(UserDomain userDomain, GameMap gameMap) {
		enterScreenReceiver.changeFightMode(userDomain);
		eventBus.post(EnterScreenEvent.valueOf(userDomain,gameMap));
	}
	
	
	private GameMap ifFullCreateNewMap(UserDomain userDomain, int goMapId, int branch, boolean isCreate){
		GameMap gameMap = this.gameMapManager.getGameMapById(goMapId, userDomain.getBranching());
		PlayerBattle battle = userDomain.getBattle();
		long playerId = userDomain.getId();
		final Integer[] limitPlayer = GameConfig.getLimitPlayerMap(goMapId);
		if(gameMap != null && limitPlayer != null && limitPlayer.length >= 2){
			GameMap tmpGameMap = this.playerTempMap.get(playerId);
			if(tmpGameMap != null && goMapId == tmpGameMap.getMapId()){
				return tmpGameMap;
			}
			
			Team playerTeam = teamFacade.getPlayerTeam(playerId);
			if(playerTeam != null){
				ConcurrentHashSet<Long> members = playerTeam.getMembers();
				for(long memberId : members){
					if(memberId == playerId){
						continue;
					}
					UserDomain userDomain2 = userManager.getUserDomain(memberId);
					GameMap gameMap2 = userDomain2.getGameMap();
					if(gameMap2 != null && gameMap2.getMapId() == goMapId){
						this.playerTempMap.put(playerId, gameMap2);
						return gameMap2;
					}
				}
			}
			if(battle.getLevel() > limitPlayer[1]){	
				this.playerTempMap.put(playerId, gameMap);
				return gameMap; 
			}
			
			if(!isCreate){
				this.playerTempMap.put(playerId, gameMap);
				return gameMap; 
			}
			
			final int playerCounts = limitPlayer[0];
			if(gameMap != null && gameMap.getPlayerNums() >= playerCounts && !gameMap.inThisMap(userDomain) ){
				CopyOnWriteArrayList<GameMap> tempMapList = getTempMapList(goMapId);
				GameMap tempMap = null;
				try {
					int mapSize = tempMapList.size();
					for(GameMap map : tempMapList){
						if(map.getPlayerNums() < playerCounts){
							synchronized (map) {
								if(!map.isCleared() && goMapId == map.getMapId() ){
									tempMap = map;
									return tempMap;
								}
							}
						}
					}
					
					if(mapSize < tempMapList.size() ){		
						return ifFullCreateNewMap(userDomain, goMapId, branch, isCreate);
						
					} else {
						tempMap = createTempMap(branch, goMapId);
						return tempMap;
					}
					
				} catch (Exception e) {
				} finally {
					if(tempMap != null){
						this.playerTempMap.put(playerId, tempMap);
					}
				}
			}
			
		}
		GameMap campBattleGameMap = campBattleFacade.getCampBattleGameMap(userDomain);
		if(campBattleGameMap != null){
			return campBattleGameMap;
		}
		GameMap battleGameMap = battleFieldFacade.getBattleFieldGameMap(userDomain);
		if(battleGameMap != null){
			return battleGameMap;
		}
		return gameMap;
	}

	private CopyOnWriteArrayList<GameMap> getTempMapList(int goMapId) {
		CopyOnWriteArrayList<GameMap> tempMapList = tempGameMap.get(goMapId);
		if(tempMapList == null){
			tempGameMap.putIfAbsent(goMapId, new CopyOnWriteArrayList<GameMap>());
			tempMapList = tempGameMap.get(goMapId);
		}
		return tempMapList;
	}

	
	private GameMap createTempMap(int branch, final int mapId) {
		final long tempGameMapId = gameMapManager.getTemporaryGameMapId();
		GameMap tempMap = gameMapManager.createTemporaryMap(mapId, tempGameMapId, branch);
		CopyOnWriteArrayList<GameMap> tempMapList = getTempMapList(mapId);
		tempMapList.add(tempMap);
		
		List<MonsterConfig> monsterConfigs = resourceService.listByIndex(IndexName.MONSTER_MAPID, MonsterConfig.class, mapId);
		for(MonsterConfig monsterConfig : monsterConfigs){
			MonsterFightConfig monsterFight = resourceService.get(monsterConfig.getMonsterFightId(), MonsterFightConfig.class);
			if (monsterFight == null) {
				continue;
			}
			monsterManager.addDungeonMonster(tempMap, monsterConfig, 0, branch);
		}
		return tempMap;
	}
	
	private GameMap ifFullCreateNewMap(UserDomain userDomain, int branch, boolean isCreate){
		return ifFullCreateNewMap(userDomain, userDomain.getMapId(), branch, isCreate);
	}
	
	
	private void leaveBornMap(long playerId, GameMap gameMap){
		if(gameMap != null){
			this.playerTempMap.remove(playerId);
		}
	}
	
	@Scheduled(name = "", value = "0 30 */1 * * *")
	private void clearTeamMap(){
		int emptyMaps = 0;
		try {
			Collection<CopyOnWriteArrayList<GameMap>> list = this.tempGameMap.values();
			if(list == null || list.isEmpty()){
				return ;
			}
			for(CopyOnWriteArrayList<GameMap> tempMapList : list){
				if(tempMapList.size() > 2){			
					for(GameMap gameMap : tempMapList){
						if(gameMap.getPlayerNums() > 0){
							continue;
						}
						
						if(++emptyMaps > 2 && gameMap.getPlayerNums() <= 0){		
							synchronized (gameMap) {
								if(gameMap.getPlayerNums() <= 0){
									tempMapList.remove(gameMap);
									gameMap.clear();
								}
							}
						}
					}
					
				}
			}
			
		} catch (Exception e) {
		}
	}

	
	public boolean isChangeScreen(UserDomain userDomain) {
		if(changeMapSpires != null){
			return changeMapSpires.containsKey(userDomain.getId());
		}
		return false;
	}

	
	public int motion(Long playerId, int tx, int ty) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return PLAYER_NOT_FOUND;
		}

		try {
			PlayerMotion motion = userDomain.getMotion();
			UserBuffer userBuffer = userDomain.getUserBuffer();
			if (BufferHelper.isPlayerInImmobilize(userBuffer)) {
				mapHelper.coerceRetrun(playerId, motion.currentPath());
				return FAILURE;
			}
			
			if(this.changeMapSpires.containsKey(playerId)){
				return FAILURE;
			}

			PlayerBattle battle = userDomain.getBattle();
			if(battle.isDead()) {
				return PLAYER_DEADED;
			}
			
			int sx = motion.getX();
			int sy = motion.getY();
			if (motion.beyondTheVerificationScope(tx, ty, MapRule.MAX_POINT_SIZE)) {
				motion.clearPath(); 
				mapHelper.coerceRetrun(playerId, motion.currentPath());
				return CHANGEPOINT_XY_ERROR;
			}

			Player player = userDomain.getPlayer();
			PlayerDungeon playerDungeon = dungeonFacade.getPlayerDungeon(playerId);
			GameMap gameMap = getGameMap(playerId, userDomain, motion, playerDungeon, player.getBranching(), false);
			
			if (!gameMap.isPathPass(tx, ty)) {
				mapHelper.coerceRetrun(playerId, motion.currentPath());
				return MAP_NOTPASS;
			}
			
			List<Integer> path = motion.getPath();
			if(path.size() <= 0){	
				return FAILURE;
			}
			
			GameMap oldGameMap = userDomain.getGameMap();
			if(oldGameMap != gameMap){
				return FAILURE;
			}
			if(!gameMap.isPathPass( userDomain.getMotion().getX(),  userDomain.getMotion().getY())){
			}

			int oldX = sx;
			int oldY = sy;
			motion.walk(tx, ty);
			motion.removePoint();
			Map<ISpire, Set<ISpire>[]> spireMap = new HashMap<ISpire, Set<ISpire>[]>();

			GameScreen toGameScreen = gameMap.getGameScreen(tx, ty);
			if (userDomain.isChangeScreen(toGameScreen)) {
				userDomain.changeScreen(toGameScreen);
			}

			Collection<GameScreen> oldCanViewScreens = gameMap.calcViewScreen(oldX, oldY);
			Collection<GameScreen> newCanViewScreens = gameMap.calcViewScreenByScreen(userDomain);
			newCanViewScreens.removeAll(oldCanViewScreens);

			oldCanViewScreens.removeAll(gameMap.calcViewScreenByScreen(userDomain));
			Collection<ISpire> hideSpires = new HashSet<ISpire>( GameMap.getSpires( oldCanViewScreens, ElementType.PLAYER, ElementType.NPC, ElementType.MONSTER) );
			for (ISpire spire : hideSpires) {
				worldPusherHelper.put2SpireQueue(userDomain, spireMap, SpireQueueType.HIDE, spire);
				if (spire instanceof UserDomain) {
					worldPusherHelper.put2SpireQueue(spire, spireMap, SpireQueueType.HIDE, userDomain);
				}
			}

			Collection<ISpire> viewSpires = new HashSet<ISpire>( GameMap.getSpires( newCanViewScreens, ElementType.PLAYER, ElementType.MONSTER, ElementType.NPC) );
			for (ISpire spire : viewSpires) {
				if (spire.getType() == ElementType.NPC) {
					
				} else if (spire instanceof MonsterDomain) {
					MonsterDomain monsterAiDomain = (MonsterDomain) spire;
					MonsterBattle monsterBattle = monsterAiDomain.getMonsterBattle();
					if (monsterBattle.isDead() && monsterAiDomain.isRemoveCorpse()) { 
						continue;
					}
					worldPusherHelper.put2SpireQueue(userDomain, spireMap, SpireQueueType.MOTION, monsterAiDomain);

				} else if (spire instanceof UserDomain) {
					if (spire != userDomain) {
						worldPusherHelper.put2SpireQueue(spire, spireMap, SpireQueueType.VIEW, userDomain);
						worldPusherHelper.put2SpireQueue(spire, spireMap, SpireQueueType.MOTION, userDomain);
					}
					worldPusherHelper.put2SpireQueue(userDomain, spireMap, SpireQueueType.MOTION, spire);
				}
				worldPusherHelper.put2SpireQueue(userDomain, spireMap, SpireQueueType.VIEW, spire);
			}
			worldPusherHelper.pushSpireChange(spireMap);
		} catch (Exception e) {
		}
		return SUCCESS;
	}
	
	
	public boolean go(long playerId,int mapId, int x, int y,int distance) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		PlayerMotion motion = userDomain.getMotion();
		final int branch = userDomain.getBranching();
		if( this.changeMapSpires.containsKey(playerId) ){
			return false;
		}
		GameMap targetGameMap = this.ifFullCreateNewMap(userDomain, mapId, branch, true);
		if(targetGameMap == null){
			mapId = MapRule.DEFAUL_REVIVE_MAPID;
			x = MapRule.DEFAUL_REVIVE_X;
			y = MapRule.DEFAUL_REVIVE_Y;
			targetGameMap = gameMapManager.getGameMapById(mapId, branch);
			motion.changeMap(mapId, x, y);
		}
		
		if(targetGameMap == null || !targetGameMap.isPathPass(x,y)){
			return false;
		}
		
		if(distance > 0){
			Point point = targetGameMap.getRandomCanStandPoint(x, y, distance);
			if(point != null){
				x = point.x;
				y = point.y;
			}
		}
		
		motion.clearPath();
		ChangeScreenVo changeScreenVo = this.leaveMap(userDomain, targetGameMap, x, y);
		
		this.mapHelper.playerGo(playerId, changeScreenVo);
		
		return true;
	}

	
	public int motionPath(Long playerId, Object[] direction) {
		if (direction == null || direction.length <= 0) {
			return FAILURE;
		}

		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return PLAYER_NOT_FOUND;
		}

		try {
			PlayerBattle battle = userDomain.getBattle();
			if(battle.isDead()) {
				return PLAYER_DEADED;
			}
			
			UserBuffer userBuffer = userDomain.getUserBuffer();
			if (BufferHelper.isPlayerInImmobilize(userBuffer)) {
				return FAILURE;
			}
			
			if(this.changeMapSpires.containsKey(playerId)){
				return FAILURE;
			}

			Player player = userDomain.getPlayer();
			PlayerDungeon playerDungeon = dungeonFacade.getPlayerDungeon(playerId);
			PlayerMotion motion = userDomain.getMotion();
			String playerName = player.getName();
			GameMap gameMap = getGameMap(playerId, userDomain, motion, playerDungeon, player.getBranching(), false);

			
			if (direction.length >= 2) {
				int x = (Integer) direction[0];
				int y = (Integer) direction[1];
				if (!gameMap.isPathPass(x, y)) {
					mapHelper.coerceRetrun(playerId, motion.currentPath());
					return MAP_NOTPASS;
				}

			} else {
				return FAILURE;
			}
			
			GameMap oldGameMap = userDomain.getGameMap();
			if(oldGameMap != gameMap){
				return FAILURE;
			}
			Map<ISpire, Set<ISpire>[]> spireMap = new HashMap<ISpire, Set<ISpire>[]>();
			if(!gameMap.isPathPass( userDomain.getMotion().getX(),  userDomain.getMotion().getY())){
			}
			
			motion.addPath(direction); 
			Collection<ISpire> playerCollection = gameMap.getCanViewsSpireCollection(userDomain, ElementType.PLAYER);
			playerCollection.remove(userDomain);
			worldPusherHelper.put2SpireQueue(playerCollection, spireMap, userDomain, SpireQueueType.MOTION);
			
			worldPusherHelper.pushSpireChange(spireMap);

			return SUCCESS;
		} catch (Exception e) {
		}
		return FAILURE;
	}


	
	public boolean isStand(int mapId, int x, int y) {
		MapConfig mapConfig = resourceService.get(mapId, MapConfig.class);
		if(mapConfig != null){
			byte[][] mapMask = mapConfig.getMapMask();
			return x < mapMask.length && y < mapMask[0].length &&  mapMask[x][y] != MaskTypes.PATH_BARRIER;
		}
		return false;
	}

	
	public ResultObject<ChangeScreenVo> changeScreen(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		PlayerMotion motion = userDomain.getMotion();
		Player player = userDomain.getPlayer() ;
		PlayerDungeon playerDungeon = dungeonFacade.getPlayerDungeon(playerId);
		
		if(player == null || playerDungeon == null || motion == null){
			return ResultObject.ERROR(MapConstant.FAILURE);
		}
		
		if(changeMapSpires.containsKey(playerId)){			
			return ResultObject.ERROR(MapConstant.FAILURE);
		}
		
		if(playerDungeon.isDungeonStatus()){
			this.dungeonPushHelper.changeScreen(playerDungeon,motion);
			return ResultObject.ERROR(MAP_DUNGEON_CHANGEPOINT);
		}
		
		GameMap	gameMap = this.ifFullCreateNewMap(userDomain, player.getBranching(), false);
		if(gameMap == null){
			gameMap = gameMapManager.getGameMapById(MapRule.DEFAUL_REVIVE_MAPID, player.getBranching());
			motion.changeMap(MapRule.DEFAUL_REVIVE_MAPID, MapRule.DEFAUL_REVIVE_X, MapRule.DEFAUL_REVIVE_Y);
		}
		
		int x = motion.getX();
		int y = motion.getY(); 
		int mapId = motion.getMapId();
		
		userDomain.changeMap(gameMap, x, y);
		ChangePoint changePoint = gameMap.getChangePoint(x,y); 
		if (changePoint == null) {
			return ResultObject.ERROR(MapConstant.FAILURE);
		}
		
		if(gameMap.getMapId() == CampBattleRule.CAMP_BATTLE_MAPID && campBattleFacade.getCampBattleStatus() < CampBattleRule.STATUS_START ){
			return ResultObject.ERROR(MapConstant.CAMP_BATTLE_FIGHT_NOT_START);
		}
		
		PetDomain petDomain = petManager.getFightingPet(userDomain.getId());
		if(petDomain != null){
			petDomain.changeMap(gameMap, motion.getX(), motion.getX());
		}
		
		GameMap linkGameMap = null;
		if(changePoint.getLinkMapId() == CampBattleRule.CAMP_BATTLE_MAPID){
			linkGameMap = campBattleFacade.getCampBattleGameMap(userDomain);
		} else {
			linkGameMap = this.ifFullCreateNewMap(userDomain, changePoint.getLinkMapId(), player.getBranching(), true);
		}
		if(linkGameMap == null){
			linkGameMap = gameMapManager.getGameMapById(MapRule.DEFAUL_REVIVE_MAPID, player.getBranching());
			userDomain.changeMap(linkGameMap, MapRule.DEFAUL_REVIVE_X, MapRule.DEFAUL_REVIVE_Y);
		}
		if(userDomain.getBattle().getLevel() < linkGameMap.getLevelLimit()){ 
			return ResultObject.ERROR(LEVEL_LIMIT_CANT_ENTER);
		}
		
		motion.removePath();	
		ChangeScreenVo changeScreenVo = this.leaveMap(userDomain, linkGameMap, changePoint.getX(), changePoint.getY());
		
		return ResultObject.SUCCESS(changeScreenVo);
	}

	
	public Collection<Long> getScreenViews(Long playerId) {
		Collection<Long> playerIds = new HashSet<Long>();
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return playerIds;
		}
		
		playerIds.add(playerId);
		GameScreen currentScreen = userDomain.getCurrentScreen();
		if(currentScreen == null) {
			return playerIds;
		}
		
		GameMap gameMap = currentScreen.getGameMap();
		if(gameMap == null) {
			return playerIds;
		}
		
		ElementType elementType = ElementType.PLAYER;
		playerIds.addAll(gameMap.getCanViewsSpireIdCollection(userDomain, elementType));
		return playerIds;
	}

	
	public void doLoginFilter(UserDomain userDomain, PlayerDungeon playerDungeon) {
		if (userDomain == null || playerDungeon == null) {
			return;
		}

		PlayerMotion motion = userDomain.getMotion();
		long playerId = playerDungeon.getId();
		GameMap gameMap = null;
		if (playerDungeon.isDungeonStatus()) {
			Dungeon dungeon = this.dungeonManager.getDungeon(playerDungeon.getDungeonId());
			if (dungeon == null || dungeon.getLeaveIds().contains(playerId)) {
				playerDungeon.leaveDungeon();
				this.dbService.submitUpdate2Queue(playerDungeon);
				
			}
			if(gameMap == null){
				gameMap = gameMapManager.getTemporaryMap(playerDungeon.getDungeonId(),userDomain.getBranching());
			}
			
		} else {
			gameMap = this.ifFullCreateNewMap(userDomain, userDomain.getBranching(), true );
		}
		
		if(gameMap != null && !gameMap.isPathPass(motion.getX(), motion.getY()) && userDomain.getBattle().getLevel() < 12 ){
			gameMap = gameMapManager.getGameMapById(PlayerRule.INIT_MAP_ID, userDomain.getBranching());
			motion.changeMap(PlayerRule.INIT_MAP_ID, PlayerRule.INIT_POSITION_X, PlayerRule.INIT_POSITION_Y);
		}
		
		if (gameMap == null || !gameMap.isPathPass(motion.getX(), motion.getY()) ) {
			gameMap = gameMapManager.getGameMapById(MapRule.DEFAUL_REVIVE_MAPID, userDomain.getBranching());
			int x = MapRule.DEFAUL_REVIVE_X;
			int y = MapRule.DEFAUL_REVIVE_Y;
			Point point = gameMap.getRandomCanStandPoint(x, y, 10);
			if(point != null){
				x = point.x;
				y = point.y;
			}
			motion.changeMap(MapRule.DEFAUL_REVIVE_MAPID, x, y);
		} else {
			motion.changeMap(gameMap.getMapId(), motion.getX(), motion.getY());
		}
		
		changeMapSpires.put(playerId, gameMap);
	}

	
	public void onLogoutEvent(UserDomain userDomain) {
		long playerId = userDomain.getPlayerId();
		PlayerMotion motion = userDomain.getMotion();
		leaveMap(userDomain, null, motion.getX(), motion.getY());
		this.gameMapManager.removePlayer(userDomain);		
		
		PetDomain petDomain = petManager.getFightingPet(userDomain.getId());
		
		GameMap removeGameMap = changeMapSpires.remove(playerId);
		if(removeGameMap != null){
			Set<GameScreen> oldScreens = removeGameMap.calcViewScreen(userDomain.getX(), userDomain.getY());
			Set<ISpire> players = GameMap.getSpires(oldScreens, ElementType.PLAYER);
			Map<ISpire, Set<ISpire>[]> spireMap = new HashMap<ISpire, Set<ISpire>[]>( players.size() );
			worldPusherHelper.put2SpireQueue(players, spireMap, userDomain, SpireQueueType.HIDE);
			worldPusherHelper.put2SpireQueue(userDomain, spireMap, SpireQueueType.HIDE, players);
			worldPusherHelper.pushSpireChange(spireMap);
			
			removeGameMap.leaveMap(userDomain);
			if(petDomain != null){
				removeGameMap.leaveMap(petDomain);
			}
		}
		
		userDomain.leaveScreen();
		if(petDomain != null){
			petDomain.leaveScreen();
		}
	}
	
	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		long playerId = messageInfo.getPlayerId();
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ;
		}
		PlayerMotion motion = userDomain.getMotion();
		leaveMap(userDomain, null, motion.getX(), motion.getY());
	}
	
	public ChangeScreenVo leaveMap(UserDomain userDomain, GameMap targetGameMap, int targetX, int targetY){
		if(userDomain == null){
			return null;
		}
		
		Map<ISpire, Set<ISpire>[]> spireMap = new HashMap<ISpire, Set<ISpire>[]>();
		int x = userDomain.getX();
		int y = userDomain.getY();
		
		PetDomain petDomain = petManager.getFightingPet(userDomain.getId());
		GameMap gameMap = userDomain.getGameMap();
		if(gameMap != null){
			Set<GameScreen> oldScreens = gameMap.calcViewScreen(x, y);
			Set<ISpire> players = GameMap.getSpires(oldScreens, ElementType.PLAYER);
			worldPusherHelper.put2SpireQueue(players, spireMap, userDomain, SpireQueueType.HIDE);
			worldPusherHelper.put2SpireQueue(userDomain, spireMap, SpireQueueType.HIDE, players);
			
			gameMap.leaveMap(userDomain);
			if(petDomain != null){
				gameMap.leaveMap(petDomain);
			}
			
			this.leaveBornMap(userDomain.getId(), gameMap);
		}
		
		PlayerMotion motion = userDomain.getMotion();
		motion.clearPath();
		
		if(targetGameMap != null){
			userDomain.changeMap(targetGameMap, targetX, targetY);
			GameMap leaveMap = changeMapSpires.put(userDomain.getId(), targetGameMap);
			if(leaveMap != null){
				Set<GameScreen> oldScreens = leaveMap.calcViewScreen(userDomain.getX(), userDomain.getY());
				Set<ISpire> players = GameMap.getSpires(oldScreens, ElementType.PLAYER);
				worldPusherHelper.put2SpireQueue(players, spireMap, userDomain, SpireQueueType.HIDE);
				worldPusherHelper.put2SpireQueue(userDomain, spireMap, SpireQueueType.HIDE, players);
				
				leaveMap.leaveMap(userDomain);
				if(petDomain != null){
					gameMap.leaveMap(petDomain);
				}
			}
			
			processEnterScreenEvent(userDomain, targetGameMap);
			
			if(petDomain != null){ 
				petDomain.changeMap(targetGameMap, x, y);
			}
			
			if(!targetGameMap.isPathPass( userDomain.getMotion().getX(),  userDomain.getMotion().getY())){
			}
		}
		
		worldPusherHelper.pushSpireChange(spireMap);
		
		return ChangeScreenVo.valueOf(motion.getMapId(), motion.getX(), motion.getY());
		
	}

	
	public void changeMap(ISpire targetSpire,GameMap gameMap, int x ,int y){
		this.changeMap(targetSpire, gameMap, x, y, ElementType.NPC,ElementType.MONSTER);
	}

	
	public void changeMap(ISpire targetSpire,GameMap gameMap, int x ,int y, ElementType ...refTypes){
		if(targetSpire == null || gameMap == null){
			return ;
		}
		GameScreen currentGameScreen = targetSpire.getCurrentScreen() ;
		Collection<GameScreen> nextGameMapCanViewScreen = gameMap.calcViewScreen(x, y);
		GameMap currentGameMap = null ;
		if(currentGameScreen != null ){
			currentGameMap = currentGameScreen.getGameMap();
		}
		
		int oldX = targetSpire.getX();
		int oldY = targetSpire.getY();
		targetSpire.changeMap(gameMap, x, y);
		
		if( !(targetSpire instanceof UserDomain) ){
			return ;
		}
		
		
		refTypes = (ElementType[])ArrayUtils.add(refTypes, ElementType.PLAYER);
		Map<ISpire, Set<ISpire>[] > spireMap = new HashMap<ISpire, Set<ISpire>[] >();
		
		if(currentGameMap != null){
			Collection<GameScreen> gameScreenList = currentGameMap.calcViewScreen(oldX, oldY);
			Collection<ISpire> canViewSpireCollectiont = GameMap.getSpires(gameScreenList, refTypes);
			for(ISpire spire : canViewSpireCollectiont){
				if(spire instanceof UserDomain){
					worldPusherHelper.put2SpireQueue(spire, spireMap, SpireQueueType.HIDE, targetSpire);
				}
			}
			if(targetSpire instanceof UserDomain){
				worldPusherHelper.put2SpireQueue(targetSpire, spireMap, SpireQueueType.HIDE, canViewSpireCollectiont);
			}
		}
		
		Collection<ISpire> canViewSpireCollection = GameMap.getSpires(nextGameMapCanViewScreen, refTypes);
		if(targetSpire instanceof UserDomain){
			GameMap leaveMap = this.changeMapSpires.remove(targetSpire.getId());
			if(leaveMap != null && leaveMap != currentGameMap) {
				leaveMap.leaveMap(targetSpire);
				PetDomain petDomain = petManager.getFightingPet(targetSpire.getId());
				if(petDomain != null){
					leaveMap.leaveMap(petDomain);
				}
				
				Collection<GameScreen> gameScreenList = leaveMap.calcViewScreen(oldX, oldY);
				Collection<ISpire> canViewSpireCollectiont = GameMap.getSpires(gameScreenList, refTypes);
				for(ISpire spire : canViewSpireCollectiont){
					if(spire instanceof UserDomain){
						worldPusherHelper.put2SpireQueue(spire, spireMap, SpireQueueType.HIDE, targetSpire);
					}
				}
				if(targetSpire instanceof UserDomain){
					worldPusherHelper.put2SpireQueue(targetSpire, spireMap, SpireQueueType.HIDE, canViewSpireCollectiont);
				}
			}
			
			UserDomain userDomain = (UserDomain)targetSpire;
			if(currentGameMap == null){
				processEnterScreenEvent(userDomain, gameMap);
			}
			
			TeamPushHelper.pushMemberChangeScreen(userDomain.getId());
			
			for(Iterator<ISpire> iterator = canViewSpireCollection.iterator(); iterator.hasNext(); ){
				ISpire spire = iterator.next();
				if(spire instanceof UserDomain){
					worldPusherHelper.put2SpireQueue(spire, spireMap, SpireQueueType.VIEW, targetSpire);
				}
			}
			worldPusherHelper.put2SpireQueue(targetSpire, spireMap, SpireQueueType.VIEW, canViewSpireCollection);
			
			worldPusherHelper.pushSpireChange(spireMap);
		}
		
	}


	
	public ResultObject<ChangeScreenVo> campChangeScreen(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(escortManager.isEscortStatus(battle)){
			return ResultObject.ERROR(IS_ESCORT_STATUS);
		}
		
		Camp camp = userDomain.getPlayer().getCamp();
		if(camp == null || camp == Camp.NONE){
			return ResultObject.ERROR(NO_RIGHT);
		}
		CampChangePoint campChangePoint = null;
		List<CampChangePoint> changePoints = resourceService.listByIndex(IndexName.MAP_CAMP_CHANGE_POINT, CampChangePoint.class, camp.ordinal() );
		if(changePoints != null && changePoints.size() > 0){
			int currMapId = userDomain.getGameMap().getMapId();
			for(int index = 0; index < changePoints.size(); index++ ){
				CampChangePoint cp = changePoints.get(index);
				if(cp.getMapId() != currMapId){
					campChangePoint = cp;
					break;
				}
			}
			if(campChangePoint == null){	
				campChangePoint = changePoints.get(0);
			}
		}
		if(campChangePoint != null){
			PlayerMotion motion = userDomain.getMotion();
			int mapId = campChangePoint.getMapId();
			int x = campChangePoint.getX();
			int y = campChangePoint.getY();
			
			GameMap targetGameMap = gameMapManager.getGameMapById(mapId, userDomain.getBranching());
			if(targetGameMap == null || !targetGameMap.isPathPass(x,y)){
				return ResultObject.ERROR(MapConstant.MAP_NOTPASS);
			}
			if(userDomain.getBattle().getLevel() < targetGameMap.getLevelLimit()){
				return ResultObject.ERROR(LEVEL_LIMIT_CANT_ENTER);
			}
			
			motion.clearPath();
			changeMapSpires.put(playerId, targetGameMap);
			this.leaveMap(userDomain, targetGameMap, x, y);
			PetDomain petDomain = petManager.getFightingPet(userDomain.getId());
			if(petDomain != null){ 
				petDomain.changeMap(targetGameMap, x, y);
			}
			
			if(targetGameMap != null){
				processEnterScreenEvent(userDomain, targetGameMap);
			}
			
			ChangeScreenVo vo =  ChangeScreenVo.valueOf(motion.getMapId(), motion.getX(), motion.getY());
			return ResultObject.SUCCESS(vo);
		}
		return ResultObject.ERROR(BASEDATA_NOT_FOUND);
	}

	
	public void skillChangeMap(ISpire target, GameMap targetGameMap, int targetX, int targetY) {
		int x = target.getX();
		int y = target.getY();
		Set<GameScreen> newViewScreen = targetGameMap.calcViewScreen(targetX, targetY);
		Set<GameScreen> oldViewScreen = targetGameMap.calcViewScreen(x, y);
		Set<GameScreen> old2ViewScreen = new HashSet<GameScreen>(oldViewScreen);
		
		Map<ISpire, Set<ISpire>[]> spireMap = new HashMap<ISpire, Set<ISpire>[]>();
		oldViewScreen.removeAll(newViewScreen);
		Set<ISpire> hideSpires = GameMap.getSpires(oldViewScreen, ElementType.PLAYER);
		worldPusherHelper.put2SpireQueue(target, spireMap, SpireQueueType.HIDE, hideSpires);
		worldPusherHelper.put2SpireQueue(hideSpires, spireMap, target, SpireQueueType.HIDE);
		newViewScreen.removeAll( old2ViewScreen );
		Set<ISpire> viewSpires = GameMap.getSpires(newViewScreen, ElementType.PLAYER);
		worldPusherHelper.put2SpireQueue(target, spireMap, viewSpires, SpireQueueType.VIEW, SpireQueueType.MOTION);
		worldPusherHelper.put2SpireQueue(viewSpires, spireMap, target, SpireQueueType.VIEW, SpireQueueType.MOTION);
		target.changeMap(targetGameMap, targetX, targetY);
		worldPusherHelper.pushSpireChange(spireMap);
	}

	
	public void checkPlayerDeadState(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle == null || !battle.isDead()) {
			return;
		}
		
		GameMap gameMap = userDomain.getGameMap();
		if(gameMap == null) {
			return;
		}
		
		UnitId unitId = userDomain.getUnitId();
		Set<Long> playerIdList = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER, playerId);
		if(playerIdList != null && !playerIdList.isEmpty()) {
			pusher.pushMessage(playerIdList, Response.defaultResponse(Module.MAP, MapCmd.PUSH_DEAD_STATE, unitId));
		}
	}
	
	
	public int sendConveneInvite(UserDomain userDomain, UserCoolTime userCoolTime, PropsConfig propsConfig) {
		if(userDomain == null){
			return MapConstant.PLAYER_NOT_FOUND;
		}
		long playerId = userDomain.getPlayerId();
		
		if(userCoolTime == null || playerId != userCoolTime.getId()) {
			return BASEDATA_NOT_FOUND;
		}
		
		int coolTimeId = propsConfig.getCdId();
		CoolTimeConfig coolTime = null;
		if(coolTimeId > 0){
			coolTime = coolTimeManager.getCoolTimeConfig(coolTimeId);
			if(coolTime == null) {
				return BASEDATA_NOT_FOUND;
			} else if(userCoolTime.isCoolTiming(coolTimeId)) {
				return COOL_TIMING;
			}
		}
		
		PlayerDungeon playerDungeon = dungeonFacade.getPlayerDungeon(playerId);
		if(playerDungeon == null || playerDungeon.isDungeonStatus()){
			return MAP_NOTPASS;
		}
		
		PlayerBattle playerBattle = userDomain.getBattle();
		if(escortManager.isEscortStatus(playerBattle)){
			return MAP_NOTPASS;
		}
		if(campBattleFacade.isInCampBattle(userDomain)){
			return MAP_NOTPASS;
		}
		if(battleFieldFacade.isInBattleField(playerId)){
			return MAP_NOTPASS;
		}

		int type = 0;
		Player player = userDomain.getPlayer();
		Collection<Long> playerIds = null;
		PlayerAlliance playerAlliance = allianceFacade.getPlayerAlliance(playerId);
		Camp camp = player.getCamp();
		switch (propsConfig.getChildType()) {
		case PropsChildType.CONVENE_ALLIANCE:
			type = 1;
			if(playerAlliance == null){
				return FAILURE;
			}
			playerIds = allianceManager.getAllianceMembers(playerAlliance.getAllianceId(), false);
			break;
		case PropsChildType.CONVENE_CAMP:
			type = 2;
			if(camp == null || camp == Camp.NONE){
				return FAILURE;
			}
			playerIds = channelFacade.getChannelPlayers( Channel.valueOf(ChatChannel.CAMP_CHANNEL.ordinal(), camp.ordinal()) );
			break;
		case PropsChildType.CONVENE_TEAM:
			type = 3;
			Team playerTeam = teamFacade.getPlayerTeam(playerId);
			if(playerTeam == null){
				return FAILURE;
			}
			playerIds = playerTeam.getMembers();
			break;
		default:
			break;
		}
		
		if(playerIds != null && playerIds.size() > 0) {
			playerIds.remove(playerId);
			CoolTime coolTime2 = userCoolTime.getCoolTime(coolTimeId);
			Long endTime = coolTime2 != null ? coolTime2.getEndTime() : 30 * TimeConstant.ONE_SECOND_MILLISECOND;
			pushConveneInvite(playerIds, player, type, endTime );
			playerConveneInvite.put(playerId, ConveneInvite.valueOf(type, endTime));
		}
		
		return MapConstant.SUCCESS;
	}
	
	private void pushConveneInvite(Collection<Long> playerIds, Player player, int type, long endTime){
		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		resultMap.put(ResponseKey.TYPE, type);
		resultMap.put(ResponseKey.PLAYER_ID, player.getId());
		resultMap.put(ResponseKey.PLAYER_NAME, player.getName());
		resultMap.put(ResponseKey.TIME, endTime);
		Response response = Response.defaultResponse(Module.MAP, MapCmd.PUSH_CONVENE_INVITE, resultMap);
		pusher.pushMessage(playerIds, response);
	}

	
	public int acceptConveneInvite(long playerId, long targetId, int type) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return MapConstant.PLAYER_NOT_FOUND;
		}
		PlayerDungeon playerDungeon = dungeonFacade.getPlayerDungeon(playerId);
		if(playerDungeon == null || playerDungeon.isDungeonStatus()){
			return MAP_NOTPASS;
		}
		
		ConveneInvite conveneInvite = playerConveneInvite.get(targetId);
		if(conveneInvite == null || conveneInvite.timeOut()){
			return FAILURE;
		}
		
		PlayerBattle playerBattle = userDomain.getBattle();
		if(escortManager.isEscortStatus(playerBattle)){
			return MAP_NOTPASS;
		}
		if(campBattleFacade.isInCampBattle(userDomain)){
			return MAP_NOTPASS;
		}
		if(battleFieldFacade.isInBattleField(playerId)){
			return MAP_NOTPASS;
		}
		
		UserDomain targetDomain = userManager.getUserDomain(targetId);
		PlayerMotion targetMotion = targetDomain.getMotion();
		PlayerBattle targetBattle = targetDomain.getBattle();
		GameMap targetGameMap = targetDomain.getGameMap();
		if(targetBattle.getLevel() < targetGameMap.getLevelLimit()){
			return LEVEL_INVALID;
		}
		if(targetBattle.isDead()){
			return PLAYER_DEADED;
		}
		PlayerDungeon targetDungeon = dungeonFacade.getPlayerDungeon(targetId);
		if(targetDungeon == null || targetDungeon.isDungeonStatus()){
			return MAP_NOTPASS;
		}
		if(escortManager.isEscortStatus(targetBattle)){
			return MAP_NOTPASS;
		}
		if(campBattleFacade.isInCampBattle(targetDomain)){
			return MAP_NOTPASS;
		}
		if(battleFieldFacade.isInBattleField(targetId)){
			return MAP_NOTPASS;
		}
		
		int x = targetMotion.getX(), y = targetMotion.getY();
		Point point = targetGameMap.getRandomCanStandPoint(x, y, 6);
		if(point != null){
			x = point.x;
			y = point.y;
		}
		
		ChangeScreenVo changeScreenVo = this.leaveMap(userDomain, targetGameMap, x, y);
		this.playerTempMap.put(playerId, targetGameMap);
		this.mapHelper.playerGo(playerId, changeScreenVo);
		
		return SUCCESS;
	}
	
	static class ConveneInvite {
		int type;
		long time;
		
		public static ConveneInvite valueOf(int type, long time){
			ConveneInvite conveneInvite = new ConveneInvite();
			conveneInvite.type = type;
			conveneInvite.time = time;
			return conveneInvite;
		}
		
		public boolean timeOut(){
			return this.time > 0 && this.time < System.currentTimeMillis(); 
		}
	}
}
