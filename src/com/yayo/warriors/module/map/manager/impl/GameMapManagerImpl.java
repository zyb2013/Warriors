package com.yayo.warriors.module.map.manager.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.yayo.common.basedb.ResourceReloadEvent;
import com.yayo.common.basedb.ResourceService;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.model.BigMapConfig;
import com.yayo.warriors.basedb.model.MapConfig;
import com.yayo.warriors.basedb.model.NpcConfig;
import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.battlefield.rule.BattleFieldRule;
import com.yayo.warriors.module.campbattle.rule.CampBattleRule;
import com.yayo.warriors.module.chat.facade.ChannelFacade;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.GameScreen;
import com.yayo.warriors.module.map.manager.GameMapManager;
import com.yayo.warriors.module.map.model.Position;
import com.yayo.warriors.module.map.types.MaskTypes;
import com.yayo.warriors.module.map.types.ScreenType;
import com.yayo.warriors.module.map.util.MapUtils;
import com.yayo.warriors.module.monster.facade.MonsterFacade;
import com.yayo.warriors.module.monster.facade.impl.MonsterFacadeImpl;
import com.yayo.warriors.module.npc.facade.NpcFacade;
import com.yayo.warriors.module.npc.facade.impl.NpcFacadeImpl;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.type.ElementType;
import com.yayo.warriors.type.IndexName;


@Service
public class GameMapManagerImpl implements GameMapManager, ApplicationListener<ResourceReloadEvent>{

	@Autowired
	private ResourceService baseService;
	@Autowired
	private ChannelFacade channelFacade;
	@Autowired
	private MonsterFacade monsterFacade;
	@Autowired
	private NpcFacade npcFacade ;
	
	private Map<Integer, GameMap> [] gameMapArraysMap;
	
	private Map<Long, GameMap>[] temporaryGameMapArraysMap;
	
	private static boolean finish ;
	
	private static final AtomicLong atomicLong = new AtomicLong(1000);
	
	public long getTemporaryGameMapId() {
		return atomicLong.getAndIncrement();
	}
	
	public GameMap getGameMapById(int mapId , int branch){
		return this.gameMapArraysMap[Math.max(branch - 1,0)].get(mapId);
	}
	
	public boolean isExistTemporaryMap(long tempId , int branch){
		return this.temporaryGameMapArraysMap[Math.max(branch - 1,0)].containsKey(tempId);
	}
	
	public GameMap createTemporaryMap(int mapId , long tempId , int branch){
		if(isExistTemporaryMap(tempId,branch)){
			return null ;
		}
		MapConfig config = baseService.get(mapId, MapConfig.class);
		BigMapConfig bigMapConfig = baseService.get(mapId, BigMapConfig.class);
		GameMap gameMap = new GameMap(config.getId(),tempId,config,bigMapConfig,branch,System.currentTimeMillis());
		temporaryGameMapArraysMap[Math.max(branch - 1,0)].put(tempId, gameMap);
		
		List<NpcConfig> list = baseService.listByIndex(IndexName.NPC_MAPID, NpcConfig.class, mapId);
		if(list != null){
			for (NpcConfig npcConfig : list) {
				Integer npcId = npcConfig.getId();
				UnitId unitId = UnitId.valueOf(npcId, ElementType.NPC);
				gameMap.enterMap(unitId, npcConfig.getBornX(), npcConfig.getBornY());
			}
		}
		return gameMap ;
	}
	
	public GameMap getTemporaryMap(long tempId , int branch){
		return temporaryGameMapArraysMap[Math.max(branch - 1,0)].get(tempId);
	}
	
	public void removeTemporaryMap(long tempId, int branch){
		GameMap gameMap = temporaryGameMapArraysMap[Math.max(branch - 1,0)].remove(tempId);
		if(gameMap != null){
		}
	}
	
	@SuppressWarnings("unchecked")
	
	public void onApplicationEvent(ResourceReloadEvent reloadEvent) {
		List<MapConfig> list = (List<MapConfig>) baseService.listAll(MapConfig.class);
		
		CopyOnWriteArrayList<Integer> branchs = channelFacade.getCurrentBranching();
		if(gameMapArraysMap == null ){
			gameMapArraysMap = new HashMap[ branchs.size() ] ;
		}
		
		if(temporaryGameMapArraysMap == null){
			temporaryGameMapArraysMap = new ConcurrentHashMap[branchs.size()];
			for(int i = 0 ; i < temporaryGameMapArraysMap.length ; i ++){
				temporaryGameMapArraysMap[i] = new ConcurrentHashMap<Long, GameMap>(5);
			}
		}
		
		for(MapConfig config : list){
			byte[] mapArrayData = config.getFormatMapData();
			int col = config.getCol(); 
			int row = config.getRow();
			byte[][] mapMask = buildMapMask(mapArrayData, col, row);
			config.setMapMask( mapMask );
			config.setMapdata(null);	
			config.setChangePoint(null);	
			
			BigMapConfig  bigMapConfig = baseService.get(config.getId(), BigMapConfig.class);
			if(bigMapConfig != null){
				for(int x = 0; x < mapMask.length; x++){
					for(int y = 0; y < mapMask[x].length; y++){
						Position position = MapUtils.getTilePositionToStage(x, y , bigMapConfig);
						if(position == null || position.getX() < 0 || position.getY() < 0 || position.getX() > config.getWidth() || position.getY() > config.getHeight()){
							mapMask[x][y] = MaskTypes.PATH_BARRIER;
						}
					}
				}
			} 
			
			if(config.getScreenType() == ScreenType.DUNGEON.ordinal()){
				continue ;
			}
			
			if(config.getId() == CampBattleRule.CAMP_BATTLE_MAPID || config.getId() == BattleFieldRule.BATTLE_FIELD_MAPID){
				continue;
			}
			
			for(int branching : branchs ){
				GameMap gameMap = new GameMap(config.getId(),0,config,bigMapConfig,branching,System.currentTimeMillis());
				Map<Integer, GameMap> map = gameMapArraysMap[branching - 1] ;
				if(map == null){
					map = new HashMap<Integer, GameMap>( branchs.size() );
				}
				map.put(gameMap.getMapId(), gameMap);
				gameMapArraysMap[branching - 1] = map ;
			}
		}
		
		((MonsterFacadeImpl)(monsterFacade)).initMonster();
		((NpcFacadeImpl)npcFacade).initNPC();
		finish = true ;
	}
	

	public void removePlayer(UserDomain userDomain){
		GameScreen gameScreen = userDomain.getCurrentScreen();
		if(gameScreen != null){
			gameScreen.leaveScreen(userDomain);
		}
	}
	
	
	private static byte[][] buildMapMask(byte[] mapdata,int col,int row) {
		byte[][] data = new byte[row][col];
		for(int i  = 0 ; i < row  ; i++) {
			for(int j = 0 ; j < col ; j++){
				data[i][j] = mapdata[i * row +j];
			}
		}
		return data;
	}

	@SuppressWarnings("static-access")
	
	public boolean isFinish(){
		return this.finish ;
	}

	
	public Point randomPoint(GameMap gameMap) {
		MapConfig mapConfig = gameMap.getMapConfig();
		int row = mapConfig.getWidth(); 
		int col = mapConfig.getHeight(); 
		int x = 0, y = 0;
		BigMapConfig bigMapConfig = gameMap.getBigMapConfig();
		while(true){
			x = Tools.getRandomInteger(row);
			y = Tools.getRandomInteger(col);
			Position position = MapUtils.getTileStageToPosition(x, y, bigMapConfig);
			x = position.getX();
			y = position.getY();
			if(x > 0 && y >0 && gameMap.isPathPass(x, y)){
				break;
			}
		}
		return new Point(x, y);
	}
}
