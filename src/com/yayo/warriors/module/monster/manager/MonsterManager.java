package com.yayo.warriors.module.monster.manager;

import java.util.Map;

import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.entity.MonsterInfo;
import com.yayo.warriors.module.monster.model.IMonsterConfig;
import com.yayo.warriors.module.monster.model.Monster;
import com.yayo.warriors.module.monster.model.MonsterBattle;

public interface MonsterManager {

	/**
	 * 获取怪物域模型
	 * @return
	 */
	Map<Long, MonsterDomain> getMonsterDomainMap();

	/** 
	 * 获取副本怪物域模型
	 * @return
	 */
	Map<Long, MonsterDomain> getDungeonMonsterMap();

	/**
	 * 添加怪物
	 * @param map
	 * @param monster
	 * @param monsterConfig
	 * @param motion
	 * @param battle
	 */
	MonsterDomain addMonster(GameMap map, Monster monster, IMonsterConfig monsterConfig, MonsterBattle battle);

	/**
	 * 根据id 获取怪物模型 （遍历所有怪物）
	 * @param id
	 * @return
	 */
	MonsterDomain getMonsterDomain(Long id);

	/**
	 * 添加副本怪物
	 * @param map
	 * @param monsterConfig
	 * @param dungeonId
	 * @param branching
	 * @return
	 */
	MonsterDomain addDungeonMonster(GameMap map, IMonsterConfig monsterConfig, long dungeonId, int branching);

	/**
	 * 清除副本怪物
	 * @param dungeonId
	 */
	void cleanDungeonMonster(long dungeonId);

	/**
	 * 取得分线的怪物信息
	 * @param branch
	 * @return
	 */
	MonsterInfo getMonsterInfo(int branch);
}
