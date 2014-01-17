package com.yayo.warriors.module.treasure.manager;


import com.yayo.warriors.module.server.listener.DataRemoveListener;
import com.yayo.warriors.module.treasure.entity.UserTreasure;

public interface TreasureManager extends DataRemoveListener{
	
	/**
	 * 取得用户藏宝图对象
	 * @param userPropsId
	 * @return
	 */
	UserTreasure getUserTreasure(long playerId);
	
}
