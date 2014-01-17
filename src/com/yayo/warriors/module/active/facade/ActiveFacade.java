package com.yayo.warriors.module.active.facade;

import java.util.List;
import java.util.Map;

import com.yayo.warriors.module.active.vo.ActiveBossRefreshVO;
import com.yayo.warriors.module.active.vo.ActiveDungeonVO;
import com.yayo.warriors.module.active.vo.ActiveTaskVO;

/**
 * 活动接口
 * 
 * @author huachaoping
 */
public interface ActiveFacade {

	/**
	 * Boss刷新活动 
	 * 
	 * @param playerId
	 * @return {@link ActiveBossRefreshVO}
	 */
	List<ActiveBossRefreshVO> monsterRefreshActive(long playerId);
	
	/**
	 * 每日副本活动
	 * 
	 * @param playerId
	 * @return {@link ActiveDungeonVO}
	 */
	List<ActiveDungeonVO> dailyDungeonActive(long playerId);
	
	/**
	 * 每日任务活动
	 * 
	 * @param playerId
	 * @return {@link Map}
	 */
	List<ActiveTaskVO> dailyTaskActive(long playerId);

	
}
