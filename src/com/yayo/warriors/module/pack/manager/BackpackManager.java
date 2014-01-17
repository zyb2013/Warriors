package com.yayo.warriors.module.pack.manager;

import com.yayo.warriors.module.pack.entity.Backpack;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.server.listener.DataRemoveListener;

/**
 * 背包管理接口
 * 
 * @author Hyint
 */
public interface BackpackManager extends DataRemoveListener {

	/**
	 * 查询玩家背包或者仓库的装备或者背包位置
	 * 
	 * @param  playerId					角色ID
	 * @param  packageType				背包号.详细见:{@link BackpackType}
	 * @return {@link Backpack}			背包位置信息
	 */
	Backpack getPackagePosition(long playerId, int packageType);
	
}
