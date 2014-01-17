package com.yayo.warriors.module.pack.facade;

import com.yayo.warriors.module.pack.entity.Backpack;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.server.listener.LogoutListener;

/**
 * 背包位置保存接口
 * 
 * @author Hyint
 */
public interface BackpackFacade extends LogoutListener {

	/**
	 * 查询玩家背包或者仓库的装备或者背包位置
	 * 
	 * @param  playerId					角色ID
	 * @param  packageType				背包号.详细见:{@link BackpackType}
	 * @return {@link Backpack}			背包位置信息
	 */
	byte[] getPackagePosition(long playerId, int packageType);
	
	/**
	 * 临时保存背包信息(仅保存在内存中)
	 * 
	 * @param playerId					角色ID
	 * @param packageType				背包类型
	 * @param packagePosition			背包位置信息
	 */
	void saveTempPosition(long playerId, int packageType, byte[] packagePosition);
}
