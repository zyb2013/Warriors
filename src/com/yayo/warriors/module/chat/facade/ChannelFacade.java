package com.yayo.warriors.module.chat.facade;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.yayo.warriors.module.chat.model.Channel;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.vo.BranchingVO;

/**
 * 聊天频道信息
 * 
 * @author Hyint
 */
public interface ChannelFacade {

	/**
	 * 进入频道
	 * 
	 * @param player		角色ID
	 * @param channels		频道数组
	 */
	void enterChannel(Player player, Channel...channels);

	/**
	 * 离开频道
	 * 
	 * @param  playerId			角色ID
	 * @param  channels			频道列表
	 */
	void leftChannel(long playerId, Channel...channels);
	
	/**
	 * 分线列表
	 * 
	 * @return
	 */
	CopyOnWriteArrayList<Integer> getCurrentBranching();
	
	/**
	 * 增加分线数量
	 * @param 	amount
	 * @return	当前的分线数量
	 */
	int addBranching(int amount);
	
	/**
	 * 验证是否能登录该分线
	 * 
	 * @param  branching			分线号
	 * @return {@link Boolean}		是否可以登录
	 */
	boolean validateLogin(int branching);
	
	/**
	 * 获得分线当前在线信息
	 * 
	 * @return {@link Collection}	分线VO列表
	 */
	Collection<BranchingVO> getBranchingOnlineInfo();

	/**
	 * 获得分线的角色ID列表
	 * 
	 * @param  branching			分线号
	 * @return {@link Collection}	角色ID列表
	 */
	Collection<Long> getBranchingPlayers(int branching);
	
	/**
	 * 获得分线的在线人数信息
	 * 
	 * @param  branching			分线号
	 * @return {@link Map}			{分线号, 总人数}
	 */
	Map<Integer, Integer> getBranchingOnlinePlayerCount();

	/**
	 * 取得频道中的角色ID列表
	 * 
	 * @param  channel				频道
	 * @return {@link Collection}	角色ID列表
	 */
	Collection<Long> getChannelPlayers(Channel channel);
	
}
