package com.yayo.warriors.module.dungeon.dao.impl;

import com.yayo.common.db.dao.impl.CommonDaoImpl;
import com.yayo.warriors.module.dungeon.dao.DungeonDao;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;

/**
 * 副本数据访问层实现类
 * @author liuyuhua
 */
public class DungeonDaoImpl extends CommonDaoImpl implements DungeonDao {
	
	public void createPlayerDungeon(PlayerDungeon playerDungeon) {
		if(playerDungeon != null){
			this.save(playerDungeon);
		}
	}
}
