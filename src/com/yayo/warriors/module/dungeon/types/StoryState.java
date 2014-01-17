package com.yayo.warriors.module.dungeon.types;

/**
 * 剧情副本状态
 * @author liuyuhua
 */
public interface StoryState {
	
	/** 无状态
	 *  <per>剧情副本可以反复进入</per>*/
	int NONE = 0;
	
	/** 成功状态
	 *  <per>玩家可以领取奖励,并且无法再次进入该副本</per>*/
	int COMPLETE = 1;
	
	/**
	 *  完成状态
	 *  <per>玩家已经领取玩奖励,并且无法再次领取奖励及进入该副本</per>
	 * */
	int FINISH = 2;

}
