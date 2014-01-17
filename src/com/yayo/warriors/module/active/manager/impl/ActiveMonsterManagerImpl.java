package com.yayo.warriors.module.active.manager.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.warriors.basedb.model.ActiveMonsterConfig;
import com.yayo.warriors.basedb.model.MonsterConfig;
import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.common.helper.MonsterHelper;
import com.yayo.warriors.common.helper.NoticePushHelper;
import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.active.manager.ActiveMonsterManager;
import com.yayo.warriors.module.active.model.ActiveLive;
import com.yayo.warriors.module.active.rule.ActiveOnlineType;
import com.yayo.warriors.module.chat.facade.ChannelFacade;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.map.manager.GameMapManager;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.manager.MonsterManager;
import com.yayo.warriors.module.monster.model.MonsterBattle;
import com.yayo.warriors.module.notice.rule.NoticeRule;
import com.yayo.warriors.module.notice.type.NoticeID;
import com.yayo.warriors.module.notice.type.NoticeType;
import com.yayo.warriors.type.ElementType;
import com.yayo.warriors.type.IndexName;

/**
 * 在线活动
 * 
 * @author liuyuhua
 */
@Component
public class ActiveMonsterManagerImpl implements ActiveMonsterManager {

	@Autowired
	private MonsterManager monsterManager;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private ChannelFacade channelFacade;
	@Autowired
	private GameMapManager gameMapManager;

	/** 经验玩法怪物集合 */
	private Map<Long, ActiveLive> MONSTER_EXP_RULES = Collections.synchronizedMap(new HashMap<Long, ActiveLive>(5));

	/** 围城玩法怪物集合 */
	private Set<MonsterDomain> MONSTER_WRAP_RULES = Collections.synchronizedSet(new HashSet<MonsterDomain>(10));
	private volatile int currentRound = 0;       //围城玩法当前回合数
	private volatile boolean finalRound = false; //标记是否最后一波 
	private volatile long roundStartTime = 0;    //当前回合开始时间

	/** 怪物活动,经验怪物ID定义 */
	private final long MONSTER_EXP_DUNGEON_ID = 1;
	/** 怪物活动,围城怪物ID定义 */
	private final long MONSTER_WRAP_DUNGEON_ID = 2;

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	
	
	public void clearMonsterWrapRule() {
		if(currentRound == 0){
			return;
		}
		
		for(MonsterDomain monsterDomain : MONSTER_WRAP_RULES){
			GameMap gameMap = monsterDomain.getGameMap();
			if(gameMap == null){
				continue;
			}
			
			Set<ISpire> players = gameMap.getCanViewsSpireCollection(monsterDomain, ElementType.PLAYER);
			MonsterHelper.removeMoster(monsterDomain, players);
			gameMap.leaveMap(monsterDomain);
		}
		
		currentRound = 0;
		finalRound = false;
		roundStartTime = 0;
		MONSTER_EXP_RULES.clear();//清理缓存的内容
		monsterManager.cleanDungeonMonster(MONSTER_WRAP_DUNGEON_ID); //清理副本的怪物
	}
	
	/**
	 * 在新的一波怪物出来的时候
	 * 需要清空之前的一波怪物
	 */
	private void clearBeforeWrapRound(){
		if(MONSTER_WRAP_RULES.isEmpty()){
			return;
		}
		
		for(MonsterDomain monsterDomain : MONSTER_WRAP_RULES){
			MonsterBattle battle = monsterDomain.getMonsterBattle();
			if(battle.isDead()){
				continue;
			}
			
			GameMap gameMap = monsterDomain.getGameMap();
			if(gameMap == null){
				continue;
			}
			Set<ISpire> players = gameMap.getCanViewsSpireCollection(monsterDomain, ElementType.PLAYER);
			MonsterHelper.removeMoster(monsterDomain, players);
		}
		
		monsterManager.cleanDungeonMonster(MONSTER_WRAP_DUNGEON_ID); //清理副本的怪物
	}
	
	
	public void activeMonsterWrapRule() {
		if(finalRound){
			return;
		}
		
		int round = currentRound + 1;
		List<ActiveMonsterConfig> configs = listWarpActive(round);
		if(configs == null || configs.isEmpty()){
			finalRound = true;
			return;
		}
		
		List<ActiveMonsterConfig> monsterConfigs = new ArrayList<ActiveMonsterConfig>(2);//需要增加的怪物ID
		long currentTime = System.currentTimeMillis();
		for(ActiveMonsterConfig config : configs){
			if(currentTime - roundStartTime < config.getRefuTime()){
				continue;
			}
			
			monsterConfigs.add(config);//本回合需要出生的怪物
		}
		
		if(!monsterConfigs.isEmpty()){
			clearBeforeWrapRound();//在新的一波怪出来之前,需要清空之前的一波怪物
			for(ActiveMonsterConfig config : monsterConfigs){
				for(int i = 0 ; i < config.getRefuCount() ; i++){
					List<MonsterDomain> lists = this.createMonsterWarp(config);
					if(lists == null || lists.isEmpty()){
						continue;
					}
					
					for(MonsterDomain monsterDomain : lists){
						MONSTER_WRAP_RULES.add(monsterDomain);
					}
				}
			}
			
			currentRound += 1;
			roundStartTime = currentTime;
			Map<String, Object> resultMap = new HashMap<String, Object>(1);//公告需要推送世界
			resultMap.put(NoticeRule.number, currentRound);
			NoticePushHelper.pushNotice(NoticeID.ACTIVE_MONSTER_WARP, NoticeType.HONOR, resultMap, 1);   // 推送世界荣誉公告
		}
	}
	
	
	public void activeMonsterExpRule() {
		if(MONSTER_EXP_RULES.isEmpty()) {
			List<ActiveMonsterConfig> configs = this.listExpActive();
			for(ActiveMonsterConfig config : configs){
				List<MonsterDomain> monsters = this.createMonsterExp(config);
				if(monsters == null || monsters.isEmpty()){
					continue;
				}
				
				for(MonsterDomain monsterDomain : monsters){
					MONSTER_EXP_RULES.put(monsterDomain.getId(), ActiveLive.valueOf(monsterDomain,config));
				}
			}
			
			Map<String, Object> resultMap = new HashMap<String, Object>(1);//公告需要推送世界
			NoticePushHelper.pushNotice(NoticeID.ACTIVE_MONSTER_EXP, NoticeType.HONOR, resultMap, 1);   // 推送世界荣誉公告
		}else{
			this.updateMonsterExpRule();
		}
	}
	
	/**
	 * 更新怪物经验 
	 * <per>要补充说明想法</per>
	 * 
	 * @return {@link boolean} true 已经更新 false 没有更新
	 */
	private void updateMonsterExpRule() {
		if(MONSTER_EXP_RULES.isEmpty()){
			return;
		}
		
		int reviveCount = 0;//用于表示公告发送
		
		for(Entry<Long, ActiveLive> entry : MONSTER_EXP_RULES.entrySet()){
			ActiveLive activeLive = entry.getValue();
			if(activeLive == null){
				continue;
			}
			
			MonsterDomain monsterDomain = (MonsterDomain)activeLive.getMonsterDomain();
			if(monsterDomain == null){
				continue;
			}
			
			MonsterBattle battle = monsterDomain.getMonsterBattle();
			if(!battle.isDead()){
				continue;
			}
			
			GameMap gameMap = monsterDomain.getGameMap();
			if(gameMap == null){
				continue;
			}
			
			if(!activeLive.canRefresh()){
				continue;
			}
			
			Set<ISpire> players = gameMap.getCanViewsSpireCollection(monsterDomain, ElementType.PLAYER);
			MonsterHelper.removeMoster(monsterDomain, players);
			
			int mapId = activeLive.getActiveMonsterConfig().randomMapId();
			GameMap targetGameMap = gameMapManager.getGameMapById(mapId, monsterDomain.getBranching());
			if(targetGameMap == null){
				continue;
			}
			Point point = gameMapManager.randomPoint(targetGameMap);
			MonsterConfig config = (MonsterConfig)monsterDomain.getMonsterConfig();
			config.setBornX(point.x);
			config.setBornY(point.y);
			monsterDomain.changeMap(targetGameMap, point.x, point.y);
			monsterDomain.resurrection(true);
			monsterDomain.setStopRun(false);
			players = targetGameMap.getCanViewsSpireCollection(monsterDomain, ElementType.PLAYER);
			MonsterHelper.refreshMonster(monsterDomain, players);
			
			reviveCount += 1;
		}
		
		if(reviveCount == MONSTER_EXP_RULES.size()){
			Map<String, Object> resultMap = new HashMap<String, Object>(1);//公告需要推送世界
			NoticePushHelper.pushNotice(NoticeID.ACTIVE_MONSTER_EXP, NoticeType.HONOR, resultMap, 1);   // 推送世界荣誉公告
		}
	}
	
	/**
	 * 创建 围城活动玩法怪物
	 * @param config                 活动怪物配置对象
	 * @param type                   类型
	 * @return {@link MonsterDomain} 怪物域对象
	 */
	private List<MonsterDomain> createMonsterWarp(ActiveMonsterConfig config) {
		if (config == null) {
			return null;
		}

		List<MonsterDomain> lists = new ArrayList<MonsterDomain>(1);
		CopyOnWriteArrayList<Integer> branchSize = channelFacade.getCurrentBranching();
		for (int branching : branchSize) {
			int monsterFightId = config.getMonsterId();// 怪物ID
			int mapId = config.randomMapId();// 随机地图ID
			MonsterFightConfig monsterFightConfig = resourceService.get(monsterFightId, MonsterFightConfig.class);
			MonsterConfig newMonsterConfig = new MonsterConfig();
			newMonsterConfig.setMonsterFightId(monsterFightId);
			newMonsterConfig.setMonsterFight(monsterFightConfig);
			GameMap gameMap = gameMapManager.getGameMapById(mapId, branching);
			if (gameMap == null) {
				logger.error("在线活动,创建经验怪物,分线[{}],地图:[{}]不存在.", branching, mapId);
				continue;
			}

			Point point = gameMap.getRandomCanStandPoint(config.getX(), config.getY(), 40);
			newMonsterConfig.setMapId(gameMap.getMapId());
			newMonsterConfig.setBornX(point.x);
			newMonsterConfig.setBornY(point.y);
			MonsterDomain monsterDomain = monsterManager.addDungeonMonster(gameMap, newMonsterConfig, MONSTER_WRAP_DUNGEON_ID , branching);
			if (monsterDomain != null) {
				if (monsterDomain.getMonsterBattle().isDead()) {
					logger.error("在线活动,创建经验怪物时出错(HP为0), 怪物基础id:{} 怪物战斗属性id:{}",newMonsterConfig.getId(),newMonsterConfig.getMonsterFightId());
					continue;
				}
			}
			lists.add(monsterDomain);
		}
		
		return lists;
	}
	
	
	/**
	 * 创建 经验活动玩法怪物
	 * @param config                 活动怪物配置对象
	 * @param type                   类型
	 * @return {@link MonsterDomain} 怪物域对象
	 */
	private List<MonsterDomain> createMonsterExp(ActiveMonsterConfig config) {
		if (config == null) {
			return null;
		}

		List<MonsterDomain> lists = new ArrayList<MonsterDomain>(1);
		CopyOnWriteArrayList<Integer> branchSize = channelFacade.getCurrentBranching();
		for (int branching : branchSize) {
			int monsterFightId = config.getMonsterId();// 怪物ID
			int mapId = config.randomMapId();// 随机地图ID
			MonsterFightConfig monsterFightConfig = resourceService.get(monsterFightId, MonsterFightConfig.class);
			MonsterConfig newMonsterConfig = new MonsterConfig();
			newMonsterConfig.setMonsterFightId(monsterFightId);
			newMonsterConfig.setMonsterFight(monsterFightConfig);
			GameMap gameMap = gameMapManager.getGameMapById(mapId, branching);
			if (gameMap == null) {
				logger.error("在线活动,创建经验怪物,分线[{}],地图:[{}]不存在.", branching, mapId);
				continue;
			}

			Point point = gameMapManager.randomPoint(gameMap);
			newMonsterConfig.setMapId(gameMap.getMapId());
			newMonsterConfig.setBornX(point.x);
			newMonsterConfig.setBornY(point.y);
			MonsterDomain monsterDomain = monsterManager.addDungeonMonster(gameMap, newMonsterConfig, MONSTER_EXP_DUNGEON_ID , branching);
			if (monsterDomain != null) {
				if (monsterDomain.getMonsterBattle().isDead()) {
					logger.error("在线活动,创建经验怪物时出错(HP为0), 怪物基础id:{} 怪物战斗属性id:{}",
							newMonsterConfig.getId(),
							newMonsterConfig.getMonsterFightId());
					continue;
				}
			}
			lists.add(monsterDomain);
		}
		
		return lists;
	}
	
	/**
	 * 获取围城类型的怪物玩法配置
	 * @param currentRound     当前回合数
	 * @return
	 */
	private List<ActiveMonsterConfig> listWarpActive(int currentRound) {
		List<ActiveMonsterConfig> configs = resourceService.listByIndex(
				IndexName.ACTIVE_MONSTER_RULE, ActiveMonsterConfig.class,
				ActiveOnlineType.MONSTER_WARP_RULE_TYPE, currentRound);

		return configs;
	}

	/**
	 * 获取经验类型的怪物玩法配置
	 * @return
	 */
	private List<ActiveMonsterConfig> listExpActive() {
		List<ActiveMonsterConfig> configs = resourceService.listByIndex(
				IndexName.ACTIVE_MONSTER_RULE, ActiveMonsterConfig.class,
				ActiveOnlineType.MONSTER_EXP_RULE_TYPE, 1);

		return configs;
	}

}
