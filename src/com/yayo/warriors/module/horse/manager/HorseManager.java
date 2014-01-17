package com.yayo.warriors.module.horse.manager;

import java.util.Map;
import com.yayo.warriors.basedb.model.HorseConfig;
import com.yayo.warriors.module.horse.entity.Horse;
import com.yayo.warriors.module.user.entity.PlayerBattle;

/**
 * 坐骑管理接口 
 * @author liuyuhua
 */
public interface HorseManager {
	
	/**
	 * 获取坐骑对象
	 * @param playerId 玩家的ID
	 * @return {@link Horse} 坐骑对象
	 */
	Horse getHorse(long playerId);
	
	/**
	 * 获取坐骑对象
	 * @param battle   玩家战斗对象
	 * @return {@link Horse} 坐骑对象
	 */
	Horse getHorse(PlayerBattle battle);
	
//	/**
//	 * 获取坐骑对象
//	 * @param playerId 玩家的ID
//	 * @return {@link Horse} 坐骑对象
//	 */
//	Horse getHorse(long playerId, boolean checkLevel);
	
	/**
	 * 获取坐骑基础数据配置对象
	 * @param level   坐骑等级
	 * @return {@link HorseConfig} 坐骑基础数据配置
	 */
	HorseConfig getHorseConfig(int level);
	
	/**
	 * 管理后台获得全服坐骑等级统计
	 * @return {@link Map<Integer,Integer>} {类型,数量} 统计结果
	 */
	Map<Integer,Integer>  getAdminAllHorseLevel();
}
