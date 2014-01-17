package com.yayo.warriors.module.drop.facade;

import java.util.Collection;
import java.util.Map;

import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.user.model.UserDomain;


/**
 * 战斗奖励接口
 * 
 * @author Hyint
 */
public interface LootFacade {
	
	/**
	 * 角色进入场景, 只给当前场景中的掉落物品给客户端看
	 * 
	 * @param  userDomain			登陆场景的角色ID
	 * @param  gameMap				角色所在的地图对象
	 */
	void enterScreen(UserDomain userDomain, GameMap gameMap);

	/**
	 * 拾取掉落奖励
	 * 
	 * @param playerId			角色ID
	 * @param rewardId			奖励ID
	 * @return {@link Integer}	掉落模块返回值
	 */
	int pickupLootReward(long playerId, long rewardId);
	
	/**
	 * 战斗奖励接口
	 * 
	 * @param  userDomain		用户模型对象
	 * @param  playerIds		可以拾取该奖励的角色ID列表
	 * @param  monsterDomain	怪物的移动对象
	 * @param  dropInfo			掉落ID与次数信息
	 */
	void createFightLoot(UserDomain userDomain, Collection<Long> playerIds, MonsterDomain monsterDomain, Map<Integer, Integer> dropInfo);
}
