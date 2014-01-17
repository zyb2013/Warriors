package com.yayo.warriors.module.dungeon.manager;

import com.yayo.warriors.basedb.model.DungeonConfig;
import com.yayo.warriors.basedb.model.DungeonStoryConfig;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.dungeon.model.Dungeon;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 副本管理层接口 
 * @author liuyuhua
 */
public interface DungeonManager {
	
	/**
	 * 副本玩法
	 */
	void processDungeonRule(); 
	
	
	/**
	 * 玩家副本对象
	 * @param battle   玩家战斗对象
	 * @return {@link PlayerDungeon} 玩家副本对象
	 */
	PlayerDungeon getPlayerDungeon(PlayerBattle battle);
	
	
	/**
	 * 玩家副本对象
	 * @param playerId
	 * @return {@link PlayerDungeon} 玩家副本对象
	 */
	PlayerDungeon getPlayerDungeon(long playerId);
	
	/**
	 * 获取副本基础配置对象
	 * @param baseId  副本基础数据ID
	 * @return {@link DungeonConfig}
	 */
	DungeonConfig getDungeonConfig(int baseId);
	
	/**
	 * 获取剧情副本基础配置对象
	 * @param baseId   副本基础数据ID
	 * @return {@link DungeonStoryConfig}
	 */
	DungeonStoryConfig getDungeonStoryConfig(int baseId);
	
	/**
	 * 创建副本
	 * @param userDomain       玩家域对象
	 * @param config           副本基础配置
	 * @return {@link Dungeon} 副本对象
	 */
	Dungeon createDungeon(UserDomain userDomain,DungeonConfig config);
	
	/**
	 * 回收副本
	 * @param dungeonId       副本的ID
	 */
	void removeDungeon(long dungeonId);
	
	/**
	 * 获取副本对象
	 * @param dungeonId       副本增量ID
	 * @return {@link Dungeon} 副本对象
	 */
	Dungeon getDungeon(long dungeonId);

	/**
	 * 杀死副本怪物
	 * @param dungeonId       副本增量ID
	 * @param monsterId       怪物的ID
	 * @param exp             杀死怪物后获得的经验
	 */
	void killDungeonMonster(long dungeonId, long monsterId,int exp);
	
	/**
	 * 是否可以进入副本(判断进入次数)
	 * @param playerId        玩家的ID
	 * @param dungeonBaseId   副本的ID
	 * @return {@link Boolean} true 可以进入 false不可以进入
	 */
	boolean canEnterDungeon(long playerId,int dungeonBaseId);
	
	/**
	 * 验证剧情副本
	 * @param playerId         玩家的ID
	 * @param stroyDungeonId   剧情副本ID        
	 * @return {@link Boolean} true 可以进入 false不可以进入
	 */
	boolean verifyStoryDungeon(long playerId,int stroyDungeonId);

}
