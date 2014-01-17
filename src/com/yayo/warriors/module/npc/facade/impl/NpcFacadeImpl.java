package com.yayo.warriors.module.npc.facade.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.scheduling.Scheduled;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.adapter.NpcService;
import com.yayo.warriors.basedb.model.MapConfig;
import com.yayo.warriors.basedb.model.NpcConfig;
import com.yayo.warriors.common.helper.WorldPusherHelper;
import com.yayo.warriors.module.chat.facade.ChannelFacade;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.GameScreen;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.map.manager.GameMapManager;
import com.yayo.warriors.module.npc.facade.NpcFacade;
import com.yayo.warriors.module.npc.model.Npc;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.type.ElementType;
import com.yayo.warriors.type.IndexName;
import com.yayo.warriors.module.map.types.ScreenType;

@Component
public class NpcFacadeImpl implements NpcFacade {

	@Autowired
	private ResourceService baseService; // 源数据
	@Autowired
	private NpcService npcService;
	@Autowired
	private ChannelFacade channelFacade ;
	@Autowired
	private WorldPusherHelper worldPusherHelper;
	@Autowired
	private GameMapManager gameMapManager;
	@Autowired
	private ResourceService resourceService;
	
	/**NPC存储*/
	private Map<Integer,Npc> npcs = null; 
	/** 日志 */
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	/** */
	private static Set<Npc> handleNpcSet;
	
	
	
	
	@SuppressWarnings("unused")
	@Scheduled(name="NPC_GATHER_REBIRTH", value="*/1 * * * * *")
	private void processNpcSchedule(){
		if(handleNpcSet == null || handleNpcSet.isEmpty()){
			return ;
		}
		List<Npc> npcList = null;
		synchronized (handleNpcSet) {
			npcList =  new ArrayList<Npc>(handleNpcSet);
		}
		
		for(Npc npc : npcList){
			Collection<GameMap> gameMaps = npc.getTimeOutGameMap(handleNpcSet);
			if(gameMaps != null && gameMaps.size() > 0){
				for(GameMap gameMap : gameMaps){
					handleCollect(gameMap, npc, true, 0);
				}
			}
		}
		
	}
	
	
	public Npc getNpc(int npcId) {
		return npcs.get(npcId);
	}
	
	public void initNPC(){
		List<NpcConfig> list = (List<NpcConfig>) baseService.listAll(NpcConfig.class);
		if(list == null || list.isEmpty()){
			LOGGER.error("NPC数据为空");
			return;
		}
		
		if(npcs == null){
			npcs = new HashMap<Integer , Npc>( list.size() );
			handleNpcSet = Collections.synchronizedSet( new HashSet<Npc>(5) );
		}
		
		CopyOnWriteArrayList<Integer> branchs = channelFacade.getCurrentBranching();
		for (NpcConfig config : list) {
			if (config.getMapId() == null) {
				LOGGER.error("构建NPC数据,基础NPC:{},地图ID为空,请检查配置", config.getId());
				continue;
			}
			Npc npc = Npc.valueOf(config);
			Npc exist = this.npcs.put(npc.getId(), npc);
			if(exist != null){
				LOGGER.error("重复npc1[id:{}, mapId:{}],npc2[id:{}, mapId:{}],请检查配置",new Object[]{config.getId(), config.getMapId(), exist.getId(), exist.getMapId()});
			}
			
			for (int branching : branchs) {
				GameMap gameMap = gameMapManager.getGameMapById(npc.getMapId(), branching);
				if (gameMap == null) {
					continue;
				}
				UnitId unitId = UnitId.valueOf(config.getId(), ElementType.NPC);
				gameMap.enterMap(unitId, config.getBornX(), config.getBornY());
			}
		}
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("NPC数据构建完毕,一共存在[{}]个NPC",this.npcs.size());
		}
		
	}
	
	
	public Npc getRandomCollect(int level) {
		List<NpcConfig> allGather = new ArrayList<NpcConfig>();//所有采集物
		List<MapConfig> mapList = resourceService.listByIndex(IndexName.MAP_SCREENTYPE, MapConfig.class, ScreenType.FIELD.ordinal());
		if(mapList != null && !mapList.isEmpty()){
			HashSet<Integer> mapIdList = new HashSet<Integer>();
			for(MapConfig config : mapList){
				if(config == null){
					continue;
				}
				if(level >= config.getLevelLimit()){
					mapIdList.add(config.getId());
				}
			}
			
			if(!mapIdList.isEmpty()){
				for(int mapId : mapIdList){
					List<NpcConfig> npcList = resourceService.listByIndex(IndexName.NPC_MAPID, NpcConfig.class, mapId);
					if(npcList != null && !npcList.isEmpty()){
						for(NpcConfig config : npcList){
							if(config.getElementType() == ElementType.NPC_GATHER.ordinal()){
								allGather.add(config);
							}
						}
					}
				}
			}
		}
		
	
		NpcConfig npcConfig = allGather.get(Tools.getRandomInteger(allGather.size()));
		return this.getNpc(npcConfig.getId());
	}
	
	
	
	public Npc getRandomNpcByLevel(int level) {
		List<NpcConfig> allNpcList = new ArrayList<NpcConfig>();//所有NPC
		List<NpcConfig> castleList = npcService.listNpcConfig(ScreenType.CASTLE.ordinal(), ElementType.NPC.ordinal());
		if(castleList != null && !castleList.isEmpty()){
			allNpcList.addAll(castleList);
		}
		List<MapConfig> mapList = resourceService.listByIndex(IndexName.MAP_SCREENTYPE, MapConfig.class, ScreenType.FIELD.ordinal());
		if(mapList != null && !mapList.isEmpty()){
			HashSet<Integer> mapIdList = new HashSet<Integer>();
			for(MapConfig config : mapList){
				if(config == null){
					continue;
				}
				if(level >= config.getLevelLimit()){
					mapIdList.add(config.getId());
				}
			}
			
			if(!mapIdList.isEmpty()){
				for(int mapId : mapIdList){
					List<NpcConfig> result = resourceService.listByIndex(IndexName.NPC_MAPID, NpcConfig.class, mapId);
					if(result != null && !result.isEmpty()){
						for(NpcConfig config : result){
							if(config.getElementType() == ElementType.NPC.ordinal() && config.getScreenType() == ScreenType.FIELD.ordinal()){
								allNpcList.add(config);
							}
						}
					}
				}
			}
		}
		
		NpcConfig npcConfig = allNpcList.get(Tools.getRandomInteger(allNpcList.size()));
		return this.getNpc(npcConfig.getId());
	}
	
	
	public Npc getRandomNpcByScreenType(int screenType){
		List<NpcConfig> npcList = npcService.listNpcConfig(screenType, ElementType.NPC.ordinal());
		if(npcList == null || npcList.isEmpty()) {
			return null;
		}
		
		NpcConfig npcConfig = npcList.get(Tools.getRandomInteger(npcList.size()));
		return this.getNpc(npcConfig.getId());
	}

	
	public void handleCollect(GameMap gameMap, Npc npc) {
		handleCollect(gameMap, npc, false, npc.getRebirthTime() );
	}
	
	/**
	 * 处理采集显示和隐藏
	 * @param gameMap
	 * @param npc
	 * @param view
	 * @param hideTime, 隐藏时间 (毫秒)
	 */
	public void handleCollect(GameMap gameMap, Npc npc, boolean view, int hideTime) {
		if(npc == null || gameMap == null){
			return ;
		}
		int x = npc.getBornX();
		int y = npc.getBornY();
		GameScreen gameScreen = gameMap.getGameScreen(x, y);
		if(gameScreen == null){
			return ;
		}
		
		Collection<ISpire> spireCollection = gameScreen.getSpireCollection(ElementType.NPC);
		ISpire target = null;
		int npcId = npc.getId();
		for(ISpire npcSpire :spireCollection){
			if(npcSpire.getId() == npcId){
				target = npcSpire;
				break;
			}
		}
		if(target == null ){
			return ;
		}
		
		Set<ISpire> spires = gameMap.getCanViewsSpireCollection(x, y, ElementType.PLAYER);
		if(spires != null){
			for(ISpire spire: spires){
				UserDomain userDomain = (UserDomain)spire;
				if(view){
					npc.view(gameMap);
					userDomain.putCanViewSpire(target);
				} else if(hideTime > 0){
					npc.hide(gameMap, hideTime);
					synchronized (handleNpcSet) {
						handleNpcSet.add(npc);
					}
					userDomain.putHideSpire(target);
				}
			}
			worldPusherHelper.putMessage2Queue(spires);
		}
	}

	
	public void hideNpc(GameMap gameMap, int npcId, int hideTime) {
		Npc npc = getNpc(npcId);
		if(npc == null){
			return ;
		}
		handleCollect(gameMap, npc, false, hideTime);
	}
	
}
