package com.yayo.warriors.module.vip.manager;

import com.yayo.warriors.basedb.model.VipConfig;
import com.yayo.warriors.module.server.listener.DataRemoveListener;
import com.yayo.warriors.module.vip.entity.PlayerVip;
import com.yayo.warriors.module.vip.model.VipDomain;

/**
 * VIP 管理接口
 * 
 * @author Hyint
 */
public interface VipManager extends DataRemoveListener {
	
	/**
	 * 获得VIP管理接口(自用)
	 * 
	 * @param  playerId				角色ID
	 * @return {@link PlayerVip}	角色VIP对象
	 */
	PlayerVip getPlayerVip(long playerId);
	
	
	/**
	 * VIP福利信息(自用)
	 * 
	 * @param vipLevel              VIP等级
	 * @return {@link VipConfig}    VIP基础信息
	 */
	VipConfig getVipConfig(int vipLevel);
	
	
	/**
	 * VIP域(对外提供)
	 * 
	 * @param  playerId             角色ID
	 * @return {@link VipDomain}    VIP域
	 */
	VipDomain getVip(long playerId);
	
	
	/**
	 * 加入缓存
	 * 
	 * @param playerId              角色ID
	 * @param vipDomain             VIP域
	 */
	void put2VipCache(long playerId, VipDomain vipDomain);
	
	
}
