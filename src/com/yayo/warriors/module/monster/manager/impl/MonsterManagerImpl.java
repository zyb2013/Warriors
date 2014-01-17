package com.yayo.warriors.module.monster.manager.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.common.helper.MonsterHelper;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.monster.action.MonsterAction.MonsterFutrue;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.entity.MonsterInfo;
import com.yayo.warriors.module.monster.manager.MonsterManager;
import com.yayo.warriors.module.monster.model.IMonsterConfig;
import com.yayo.warriors.module.monster.model.Monster;
import com.yayo.warriors.module.monster.model.MonsterBattle;
import com.yayo.warriors.type.ElementType;

@Service
public class MonsterManagerImpl extends CachedServiceAdpter  implements MonsterManager {
	@Autowired
	private ResourceService resourceService;  //源数据
	
	/** 日志 */
	private final Logger logger = LoggerFactory.getLogger(getClass());
	/** 怪物保存器    K(野外怪编号) V怪物*/
	private final Map<Long,MonsterDomain> monsterDomainMap = new HashMap<Long,MonsterDomain>(20);
	/** 怪物保存器    K(副本怪编号) V怪物*/
	private final Map<Long,MonsterDomain> tempMonsterMap = new ConcurrentHashMap<Long,MonsterDomain>(20);
	private final ConcurrentMap<Long, List<MonsterDomain>> dungeonMonsterMap = new ConcurrentHashMap<Long, List<MonsterDomain>>(5);
	
	/**
	 * 回收副本怪物
	 * @param dungeonId
	 */
	
	public void cleanDungeonMonster(long dungeonId){
		Collection<MonsterDomain> domainCollection = this.dungeonMonsterMap.remove(dungeonId);
		if(domainCollection == null){
			return ;
		}
		
		for(MonsterDomain domain : domainCollection){
			MonsterDomain monsterDomain = tempMonsterMap.remove(domain.getId());
			try {
				if(monsterDomain != null){
					monsterDomain.setStopRun(true);
					MonsterFutrue monsterFutrue = monsterDomain.getMonsterFutrue();
					if(monsterFutrue != null){
						try {
							Future<Boolean> future = monsterFutrue.getFuture();
							if(future != null){
								future.cancel(true);
							}
						} catch (Exception ex){
							logger.error("{}", ex);
						}
					}
					monsterDomain.dispose();
					monsterDomain = null;
				}
				
			} catch (Exception ex){
				logger.error("{}", ex);
			}
		}
		
	}
	
	
	public Map<Long, MonsterDomain> getMonsterDomainMap() {
		return this.monsterDomainMap;
	}

	
	public Map<Long, MonsterDomain> getDungeonMonsterMap() {
		return tempMonsterMap;
	}

	
	public MonsterDomain getMonsterDomain(Long id) {
		MonsterDomain domain = this.tempMonsterMap.get(id);
		if(domain == null){
			return this.monsterDomainMap.get(id);
		}
		return domain;
	}

	/**
	 * 录入需要AI处理的怪物
	 * @param map
	 * @param monster
	 * @param monsterConfig
	 * @param battle 
	 * @param motion2 
	 */
	
	public MonsterDomain addMonster(GameMap map, Monster monster, IMonsterConfig monsterConfig,MonsterBattle battle) {
		MonsterDomain monsterDomain = new MonsterDomain(monster,map,monsterConfig,battle);
		if(monsterDomain != null){
			MonsterDomain exist = monsterDomainMap.put(monster.getId(), monsterDomain);
			if(exist != null){
				logger.error("地图[{}]基础怪物[{}]重复。。。。。。", exist.getMapId(), exist.getMonster().getBaseId() );
			}
			if(!map.isPathPass(monsterConfig.getBornX(), monsterConfig.getBornY())){
				logger.error("怪物[{}]放在了地图[{}]不可行动区域,",  monsterConfig.getId(), map.getMapId());
			}
		}
		return monsterDomain ;
	}
	
	/**
	 * 创建副本怪物
	 * @param map
	 * @param monster
	 * @param monsterConfig
	 */
	
	public MonsterDomain addDungeonMonster(GameMap map,IMonsterConfig monsterConfig , long dungeonId, int branching) {
		MonsterFightConfig monsterFightConfig = resourceService.get(monsterConfig.getMonsterFightId(), MonsterFightConfig.class);
		Monster monster = Monster.valueOf(branching, dungeonId, monsterFightConfig);
		MonsterDomain monsterAiDomain = null ;
		try{
			int bornX = monsterConfig.getBornX();
			int bornY = monsterConfig.getBornY();
			if(!map.isPathPass(bornX, bornY)){
				logger.error("怪物[{}]放在了地图[{}]不可行动区域,",  monsterConfig.getId(), map.getMapId());
				return null ;
			}
			
			long monsterId = monster.getId();
			monsterAiDomain = new MonsterDomain(monster,map,monsterConfig, MonsterBattle.valueOf(monsterId, monsterFightConfig));
			if(monsterAiDomain != null){
				monsterAiDomain.getMonsterBattle(); //为了防止并发引起的怪物空血的问题.
				Collection<ISpire> spireCollection = map.getCanViewsSpireCollection(bornX,bornY, ElementType.PLAYER);
				if(!spireCollection.isEmpty()){
					MonsterHelper.refreshMonster(monsterAiDomain,spireCollection);
				}
				
				map.enterMap(monsterAiDomain);
				tempMonsterMap.put(monsterId, monsterAiDomain);
				if(dungeonId != 0){
					List<MonsterDomain> list = dungeonMonsterMap.get(dungeonId);
					if(list == null){
						dungeonMonsterMap.putIfAbsent(dungeonId, new CopyOnWriteArrayList<MonsterDomain>());
						list = dungeonMonsterMap.get(dungeonId);
					}
					list.add(monsterAiDomain);
				}
			}
			
		} catch(Exception e) {
			logger.error("创建怪物对象时报错：{}", e);
		}
		return monsterAiDomain ;
	}

	
	public MonsterInfo getMonsterInfo(int branch) {
		return this.get(branch, MonsterInfo.class);
	}

	@SuppressWarnings("unchecked")
	
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(id != null && clazz == MonsterInfo.class){
			Integer branch = (Integer)id;
			MonsterInfo monsterInfo = commonDao.get(branch, MonsterInfo.class);
			if(monsterInfo == null){
				monsterInfo = new MonsterInfo();
				monsterInfo.setId(branch);
				commonDao.save(monsterInfo);
			}
			return (T) monsterInfo;
		}
		return super.getEntityFromDB(id, clazz);
	}
	
}
