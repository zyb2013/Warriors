package com.yayo.warriors.basedb.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceAdapter;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.model.MapConfig;
import com.yayo.warriors.basedb.model.TreasureConfig;
import com.yayo.warriors.basedb.model.TreasureEventConfig;
import com.yayo.warriors.basedb.model.TreasureMonsterConfig;
import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.manager.GameMapManager;
import com.yayo.warriors.module.map.types.ScreenType;
import com.yayo.warriors.module.treasure.rule.TreasureRule;
import com.yayo.warriors.type.IndexName;

@Component
public class TreasureService extends ResourceAdapter{
	@Autowired
	private GameMapManager gameMapManager;
	
	/** 日志 */
	private final Logger logger = LoggerFactory.getLogger(TreasureService.class);

	@Override
	public void initialize() {
		Collection<TreasureEventConfig> treasureEventConfigs = this.resourceService.listAll(TreasureEventConfig.class);
		for(TreasureEventConfig treasureEventConfig : treasureEventConfigs){
			this.resourceService.addToIndex(IndexName.TREASURE_EVENT_REWARDID_AND_DIG_PROPID, treasureEventConfig.getId(), TreasureEventConfig.class, treasureEventConfig.getRewardId(), treasureEventConfig.getPropsId());
		}
	}
	
	/**
	 * 刷新品质
	 * @param treasureId
	 * @return
	 */
	public int refreshQuality(int treasureId){
		TreasureConfig treasureConfig = randomTreasure(treasureId);
		return treasureConfig != null ? treasureConfig.getQuality() : 0;
	}
	
	/**
	 * 取得所有npcId
	 * @param rewardId
	 * @param digPropId
	 * @return
	 */
	public List<Integer> getAllNpcTypeIds(int rewardId, int digPropId){
		return this.resourceService.listIdByIndex(IndexName.TREASURE_EVENT_REWARDID_AND_DIG_PROPID, TreasureEventConfig.class, Integer.class, rewardId, digPropId);
	}
	
	/**
	 * 随机一个箱子
	 * @param rewardId
	 * @param digPropId
	 * @param openedBoxs
	 * @return
	 */
	public int randomBox(int rewardId, int digPropId, Collection<Integer> openedBoxs){
		List<Integer> allNpcIds = getAllNpcTypeIds(rewardId, digPropId);
		if(allNpcIds != null && openedBoxs != null){
			int size = allNpcIds.size();
			if(size > 0 && allNpcIds.size() > openedBoxs.size()){
				while(true){
					int id = allNpcIds.get( Tools.getRandomInteger(size) );
					TreasureEventConfig treasureEventConfig = this.resourceService.get(id, TreasureEventConfig.class);
					int npcId = treasureEventConfig.getType();
					if( !openedBoxs.contains(npcId) ){
						return npcId;
					}
				}
			}
		}
		return 0;
	}

	/**
	 * 随机一个藏宝图
	 * @param treasureId
	 * @return
	 */
	private TreasureConfig randomTreasure(int treasureId) {
		List<TreasureConfig> list = resourceService.listByIndex(IndexName.TREASURE_REWARDID, TreasureConfig.class, treasureId);
		if(list != null && list.size() > 0 ){
			int rndRate = 0;
			int rate = 0;
			for(TreasureConfig config : list){
				if(rndRate <= 0){
					rndRate = Tools.getRandomInteger(config.getFullRate());
				}
				rate += config.getRate();
				if(rndRate < rate ){
					return config;
				}
			}
			
		} else {
			logger.error("藏宝图id[{}]没有对应的品质刷新数据", treasureId);
		}
		return null;
	}
	
	/**
	 * 根据藏宝图生成一个事件
	 * @param rewardId			奖励id
	 * @param digPropsId		铲子id
	 * @param npcId				npc的Id
	 * @return
	 */
	public TreasureEventConfig getTreasureEvent(int rewardId, int digPropsId, int npcId){
		return resourceService.getByUnique(IndexName.TREASURE_EVENT_REWARDID_AND_DIG_PROPID_AND_NPCID, TreasureEventConfig.class, rewardId, digPropsId, npcId);
	}
	
	/**
	 * 取得
	 * @param rewardId
	 * @param quality
	 * @return
	 */
	public TreasureConfig getTreasureConfig(int rewardId, int quality){
		return resourceService.getByUnique(IndexName.TREASURE_REWARDID_QUALITY, TreasureConfig.class, rewardId, quality);
	}
	
	/**
	 * 随机藏宝图怪物
	 * @param monsterDropNo
	 * @return
	 */
	public TreasureMonsterConfig getTreasureMonsterConfig(int monsterDropNo){
		List<TreasureMonsterConfig> list = resourceService.listByIndex(IndexName.TREASURE_DROP_NO, TreasureMonsterConfig.class, monsterDropNo);
		if(list != null && list.size() > 0 ){
			int rndRate = 0;
			int rate = 0;
			for(TreasureMonsterConfig config : list){
				if(rndRate <= 0){
					rndRate = Tools.getRandomInteger(config.getFullRate());
				}
				rate += config.getRate();
				if(rndRate < rate ){
					return config;
				}
			}
			
		} else {
			logger.error("藏宝图怪物掉落id[{}]没有对应的数据", monsterDropNo);
		}
		return null;
	}
	
//	/**
//	 * 根据宝藏id得到一个物品奖励或怪物
//	 * @param treasureId
//	 * @return
//	 */
//	public <T extends RateConfig> RateConfig getRandomTreasureEvent(int rewardId, int quality, int digPropsId){
//		TreasureConfig treasureConfig = resourceService.getByUnique(IndexName.TREASURE_REWARDID_QUALITY, TreasureConfig.class, rewardId, quality);
//		if(treasureConfig == null){
//			return null;
//		}
//		TreasureEventConfig treasureEvent = randomTreasureEvent( treasureConfig.getId(), digPropsId);
//		return randomTreasureEvent(treasureEvent);
//	}
	
//	/**
//	 * 根据事件生成一个物品奖励或怪物
//	 * @return
//	 */
//	@SuppressWarnings("unchecked")
//	public <T extends RateConfig> RateConfig randomTreasureEvent(TreasureEventConfig treasureEventConfig){
//		if(treasureEventConfig == null){
//			return null;
//		}
//		int type = treasureEventConfig.getType();
//		Class<T> clazz = null;
//		if(type == 1){			//物品
//			clazz = (Class<T>) TreasureGoodsConfig.class;
//		} else if(type == 2){	//怪物
//			clazz = (Class<T>) TreasureMonsterConfig.class;
//		}
//		List<T> list = resourceService.listByIndex(IndexName.TREASURE_EVENT_ID, clazz, treasureEventConfig.getId() );
//		if(list != null && list.size() > 0){
//			int rndRate = 0;
//			int rate = 0;
//			for(T config : list){
//				if(rndRate <= 0){
//					rndRate = Tools.getRandomInteger(config.getFullRate());
//				}
//				rate += config.getRate();
//				if(rndRate < rate ){
//					return config;
//				}
//			}
//			
//		} else {
//			logger.error("藏宝图id[{}]没有对应的事件刷新数据", treasureEventConfig.getId() );
//		}
//		
//		return null;
//	}

	/**
	 * 根据角色等级随机生成一个地图坐标位置
	 * @param playerLevel
	 * @return int[地图id, x坐标, y坐标]
	 */
	public int[] randomMapPoint(int playerLevel, int branch){
 		ScreenType[] types = {ScreenType.FIELD};
		List<MapConfig> mapList = new ArrayList<MapConfig>();
		for(ScreenType type : types){
			List<MapConfig> list = resourceService.listByIndex(IndexName.MAP_SCREENTYPE, MapConfig.class, type.ordinal() );
			for(Iterator<MapConfig> iterator = list.iterator(); iterator.hasNext(); ){
				MapConfig mapConfig = iterator.next();
				int levelLimit = mapConfig.getLevelLimit();
				if(playerLevel < levelLimit || levelLimit < TreasureRule.TREASURE_MAP_LEVEL_LIMIT){
					iterator.remove();
				}
			}
			mapList.addAll(list);
		}
		
		if(mapList.size() > 0){
			MapConfig mapConfig = mapList.get( Tools.getRandomInteger(mapList.size()) );
			int mapId = mapConfig.getId();
			GameMap gameMap = gameMapManager.getGameMapById(mapId, branch);
			Point randomPoint = gameMapManager.randomPoint(gameMap);
			if(randomPoint != null){
				return new int[]{mapId, randomPoint.x , randomPoint.y};
			}
		}
		
		return null;
	}
}
