package com.yayo.warriors.basedb.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceAdapter;
import com.yayo.warriors.basedb.model.MonsterConfig;
import com.yayo.warriors.basedb.model.MonsterDungeonConfig;
import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.type.IndexName;

/**
 * 副本怪物基础数据
 * @author liuyuhua
 */
@Component
public class MonsterService extends ResourceAdapter{
	
	/**
	 * 获取副本回合数怪物
	 * @param dungeonId   副本ID
	 * @param round       回合数
	 * @return {@link List<Integer>} 怪物ID集合
	 */
	public List<MonsterDungeonConfig> getMonsters4Round(int dungeonId,int round){
		List<MonsterDungeonConfig> configs = resourceService.listByIndex(IndexName.MONSTERDUNGEON_DUNGEONID_ROUND, MonsterDungeonConfig.class, dungeonId, round);
		List<MonsterDungeonConfig> monsters = null; //这里的设计特意让他返回空
		if(configs != null && !configs.isEmpty()){
			monsters = new ArrayList<MonsterDungeonConfig>();
			for(MonsterDungeonConfig config : configs){
				if(config.ranRate()){
					monsters.add(config);
				}
			}
		}
		return monsters;
	}
	
	@Override
	public void initialize() {
		Collection<MonsterConfig> monsterConfigs = resourceService.listAll(MonsterConfig.class);
		for (MonsterConfig monsterConfig : monsterConfigs) {
			int monsterFightId = monsterConfig.getMonsterFightId();
			MonsterFightConfig monsterFightConfig = resourceService.get(monsterFightId, MonsterFightConfig.class);
			monsterConfig.setMonsterFight(monsterFightConfig);
			if(monsterFightConfig == null){
				System.err.println(String.format("mapId:%d MonsterConfig:%d MonsterFightConfig:%d", monsterConfig.getMapId(), monsterConfig.getId(), monsterConfig.getMonsterFightId() ) );
			}
		}
		
		testDungeonMonster();
	}
	
	public List<MonsterConfig> listMonsterConfig(int monsterFightId) {
		return resourceService.listByIndex(MonsterConfig.IDX_NAME, MonsterConfig.class, monsterFightId);
	}
	
	private void testDungeonMonster(){
		Collection<MonsterDungeonConfig> dungeonConfigs = resourceService.listAll(MonsterDungeonConfig.class);
		for(MonsterDungeonConfig dungeonConfig : dungeonConfigs){
			MonsterFightConfig monsterFightConfig = resourceService.get(dungeonConfig.getMonsterFightId(), MonsterFightConfig.class);
			if(monsterFightConfig == null){
				System.err.println(String.format("DungeonId:%d MonsterDungeonConfig:%d MonsterFightConfig:%d", dungeonConfig.getDungeonId(), dungeonConfig.getId(), dungeonConfig.getMonsterFightId() ) );
			}
		}
	}
	
}
