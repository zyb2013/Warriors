package com.yayo.warriors.module.dungeon.dao;

import com.yayo.common.db.dao.CommonDao;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;

/**
 * 副本数据访问层 
 * @author liuyuhua
 */
public interface DungeonDao extends CommonDao{

	/**
	 * 创建玩家副本对象
	 * @param playerDungeon 玩家副本对象
	 */
	void createPlayerDungeon(PlayerDungeon playerDungeon);
	
}
