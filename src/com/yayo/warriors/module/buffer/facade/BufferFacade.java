package com.yayo.warriors.module.buffer.facade;

import com.yayo.warriors.module.buffer.entity.UserBuffer;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.server.listener.LoginListener;

/**
 * Buffer 的接口
 *
 * @author Hyint
 */
public interface BufferFacade extends LoginListener {
	
	/** 
	 * 处理用户BUFF定时计算器
	 */
	void processBufferScheduling();
	
	/**
	 * 移除Buffer计时
	 * 
	 * @param unitId					战斗单位ID
	 */
	void removeBufferFromScheduler(UnitId unitId);
	
	/**
	 * 把角色的加入到检索队列中
	 * 
	 * @param playerId					角色ID
	 * @param resetAll					是否需要重置
	 */
	void addUserBufferQueue(long playerId, boolean resetAll);

	/**
	 * 把怪物的BUFF加入到检索队列中
	 * 
	 * @param monsterId					怪物ID
	 */
	void addMonsterBufferQueue(long monsterId);
	
	/**
	 * 查询角色的Buffer对象
	 * 
	 * @param  playerId					角色ID
	 * @return {@link UserBuffer}		用户BUFF对象
	 */
	UserBuffer getUserBuffer(long playerId);
	
//	/**
//	 * 取得技能效果值
//	 * @param playerId
//	 * @param effectType
//	 * @return
//	 */
//	double getBufferEffect(long playerId, SkillEffectType effectType);
}
