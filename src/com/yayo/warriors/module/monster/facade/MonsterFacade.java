package com.yayo.warriors.module.monster.facade;

import com.yayo.warriors.basedb.model.MonsterConfig;
import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.module.monster.domain.MonsterDomain;

/**
 * 怪物接口
 * @author liuyuhua
 *
 */
public interface MonsterFacade {
	
    /**
     * 随机获得一只怪物
     * 
     * @param  playerLevel					角色的等级
     * @return {@link MonsterConfig}		怪物信息
     */
    MonsterConfig getRandomMonsterFight(int playerLevel);

    /**
     * 查询怪物的域模型
     * 
     * @param  monsterId					怪物的ID
     * @return {@link MonsterDomain2}		用户域模型
     */
	MonsterDomain getMonsterDomain(Long monsterId);

	/**
	 * 怪物的战斗配置信息
	 * 
	 * @param  monsterFightId				怪物的战斗ID
	 * @return {@link MonsterFightConfig}	怪物的战斗对象
	 */
	MonsterFightConfig getMonsterFightConfig(int monsterFightId);

}
