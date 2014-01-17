package com.yayo.warriors.module.cooltime.manager;

import com.yayo.warriors.basedb.model.CoolTimeConfig;
import com.yayo.warriors.module.cooltime.entity.UserCoolTime;
import com.yayo.warriors.module.cooltime.model.PetCoolTime;
import com.yayo.warriors.module.server.listener.DataRemoveListener;
import com.yayo.warriors.module.server.listener.LogoutListener;


/**
 * 冷却时间管理接口
 *  
 * @author Hyint
 */
public interface CoolTimeManager extends DataRemoveListener, LogoutListener  {
	
	/**
	 * 查询基础冷却时间对象
	 * 
	 * @param  coolTimeId				冷却时间ID
	 * @return {@link CoolTimeConfig}	冷却时间对象
	 */
	CoolTimeConfig getCoolTimeConfig(int coolTimeId);
	
	/**
	 * 查询角色CD时间
	 * 
	 * @param  playerId					角色ID
	 * @return {@link UserCoolTime}		用户CD时间对象
	 */
	UserCoolTime getUserCoolTime(long playerId);
	
	/**
	 * 获得召唤兽冷却时间
	 * 
	 * @param  userPetId				召唤兽ID
	 * @return {@link PetCoolTime}		召唤兽冷却时间
	 */
	PetCoolTime getPetCoolTime(long userPetId);
	
	/** 
	 * 更新角色CD时间
	 * 
	 * @param  playerId					角色ID
	 * @param  coolTimeId				冷却时间ID
	 * @param  coolTime					冷却时间
	 */
	void updateUserCooleTime(long playerId, int coolTimeId, int len);
}
