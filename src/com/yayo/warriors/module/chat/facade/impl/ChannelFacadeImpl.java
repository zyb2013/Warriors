package com.yayo.warriors.module.chat.facade.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.scheduling.Scheduled;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.module.chat.facade.ChannelFacade;
import com.yayo.warriors.module.chat.model.Channel;
import com.yayo.warriors.module.chat.type.ChatChannel;
import com.yayo.warriors.module.server.listener.LogoutListener;
import com.yayo.warriors.module.syscfg.entity.SystemConfig;
import com.yayo.warriors.module.syscfg.manager.SystemConfigManager;
import com.yayo.warriors.module.syscfg.type.ConfigType;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.vo.BranchingVO;
import com.yayo.warriors.util.GameConfig;

/**
 * 频道接口实现类
 * 
 * @author Hyint
 */
@Component
public class ChannelFacadeImpl implements ChannelFacade, LogoutListener, ApplicationListener<ContextRefreshedEvent> {

	/** 当前服务器的分线状态 */
	private CopyOnWriteArrayList<Integer> TOTAL_BRANCHINGS = null;
	/** 写锁 */
	private final Lock WRITE_LOCK = new ReentrantReadWriteLock().writeLock();
	//日志对象
	private static final Logger LOGGER = LoggerFactory.getLogger(ChannelFacadeImpl.class);
	/** 频道的玩家集合 */
	private ConcurrentHashMap<String, ConcurrentHashSet<Long>> CHANNEL_PLAYERS = new ConcurrentHashMap<String, ConcurrentHashSet<Long>>(3);

	@Autowired
	private SystemConfigManager configManager;
	
	
	public void onLogoutEvent(UserDomain userDomain) {
		long playerId = userDomain.getPlayerId();
		for (String channelKey : CHANNEL_PLAYERS.keySet()) {
			this.leftChannel(playerId, channelKey);
		}
	}

	
	public void onApplicationEvent(ContextRefreshedEvent event) {
		for (Integer branching : getCurrentBranching()) {
			this.getBranchingPlayers(branching);
		}
	}
	
	/**
	 * 进入频道
	 * 
	 * @param playerId		角色ID
	 * @param branching		分线号
	 * @param channels		频道数组
	 */
	
	public void enterChannel(Player player, Channel...channels) {
		for (Channel channel : channels) {
			Collection<Long> channelPlayers = getChannelPlayers(channel);
			if(channelPlayers == null) {
				continue;
			}
		
			channelPlayers.add(player.getId());
			if(channel.getChannel() == ChatChannel.WORLD_CHANNEL.ordinal()) { //分线频道, 则需要去处理离开分线
				updatePlayerBranching(player, channel.getSubChannel());
			}
		}
	}

	/**
	 * 离开分线
	 * 
	 * @param playerId		角色ID
	 * @param channelKey	频道信息
	 */
	private void leftChannel(long playerId, String channelKey) {
		ConcurrentHashSet<Long> playerIds = CHANNEL_PLAYERS.get(channelKey);
		if(playerIds != null) {
			playerIds.remove(playerId);
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("角色:[{}] 离开了频道: [{}] ", playerId, channelKey);
			}
		}
	}

	
	
	public void leftChannel(long playerId, Channel... channels) {
		for (Channel channel : channels) {
			leftChannel(playerId, channel.toString());
		}
	}

	/**
	 * 更新角色的分线信息
	 * 
	 * @param player
	 * @param branching
	 */
	private void updatePlayerBranching(Player player, int branching) {
		ChainLock lock = LockUtils.getLock(player);
		try {
			lock.lock();
			player.setBranching(branching);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * 获得当前可以登录的分线信息
	 * 
	 * @return	{@link Set}		分线列表
	 */
	
	public CopyOnWriteArrayList<Integer> getCurrentBranching() {
		if(TOTAL_BRANCHINGS != null) {
			return this.TOTAL_BRANCHINGS;
		}
		
		try {
			WRITE_LOCK.lock();
			if(TOTAL_BRANCHINGS != null) {
				return TOTAL_BRANCHINGS;
			}
			
			TOTAL_BRANCHINGS = new CopyOnWriteArrayList<Integer>();
			String branchings = getServerBranchings();
			if(StringUtils.isBlank(branchings)) {
				return TOTAL_BRANCHINGS;
			}
			
			String[] arrays = branchings.split(Splitable.ATTRIBUTE_SPLIT);
			for (String element : arrays) {
				if(!StringUtils.isBlank(element)) {
					TOTAL_BRANCHINGS.add(Integer.valueOf(element));
				}
			}
		} catch(Exception e) {
			LOGGER.error("{}", e);
		} finally {
			WRITE_LOCK.unlock();
		}
		return TOTAL_BRANCHINGS;
	}
	
	
	public synchronized int addBranching(int amount) {
		CopyOnWriteArrayList<Integer> currentBranching = getCurrentBranching();
		int curSize = currentBranching.size();
		if(amount > curSize){
			WRITE_LOCK.lock();
			try {
				while (curSize < amount){
					currentBranching.add( ++curSize );
				}
				
			} finally {
				WRITE_LOCK.unlock();
			}
		}
		return currentBranching.size();
	}

	/**
	 * 获得服务器分线信息
	 * 
	 * @return {@link String}		服务器分线信息
	 */
	private String getServerBranchings() {
		SystemConfig systemConfig = configManager.getSystemConfig(ConfigType.BRANCHING);
		return systemConfig == null ? "" : systemConfig.getInfo();
	}

	/**
	 * 验证是否能登录该分线
	 * 
	 * @param  branching			分线号
	 * @return {@link Boolean}		是否可以登录
	 */
	
	public boolean validateLogin(int branching) {
		boolean canLogin = false;
		if(this.getCurrentBranching().contains(branching)) {
			Collection<Long> members = this.getBranchingPlayers(branching);
			canLogin = (members == null ? 0 : members.size()) < GameConfig.getMaxBranchingMembers();
		}
		return canLogin;
	}

	
	public Collection<BranchingVO> getBranchingOnlineInfo() {
		Collection<BranchingVO> branchingVOSet = new HashSet<BranchingVO>();
		for (Integer branching : this.getCurrentBranching()) {
			Collection<Long> channelPlayers = getBranchingPlayers(branching);
			branchingVOSet.add(BranchingVO.valueOf(branching, channelPlayers == null ? 0 : channelPlayers.size()));
			
		}
		return branchingVOSet;
	}

	
	public Collection<Long> getBranchingPlayers(int branching) {
		return this.getChannelPlayers(Channel.valueOf(ChatChannel.WORLD_CHANNEL.ordinal(), branching));
	}

	/**
	 * 获得分线的在线人数信息
	 * 
	 * @param  branching			分线号
	 * @return {@link Map}			{分线号, 总人数}
	 */
	
	public Map<Integer, Integer> getBranchingOnlinePlayerCount() {
		CopyOnWriteArrayList<Integer> currentBranching = this.getCurrentBranching();
		Map<Integer, Integer> branchingMaps = new HashMap<Integer, Integer>(currentBranching.size());
		for (Integer branching : currentBranching) {
			Collection<Long> channelPlayers = getBranchingPlayers(branching);
			branchingMaps.put(branching, channelPlayers == null ? 0 : channelPlayers.size());
		}
		return branchingMaps;
	}
	
	/**
	 * 取得频道中的角色ID列表
	 * 
	 * @param  channel				频道
	 * @return {@link Collection}	角色ID列表
	 */
	
	public Collection<Long> getChannelPlayers(Channel channel) {
		String channelKey = channel.toString();
		ConcurrentHashSet<Long> playerIds = CHANNEL_PLAYERS.get(channelKey);
		if(playerIds != null) {
			return playerIds;
		}
		
		if(channel.getChannel() == ChatChannel.WORLD_CHANNEL.ordinal()) {
			if(!this.getCurrentBranching().contains(channel.getSubChannel())) {
				return playerIds;
			}
		} 

		CHANNEL_PLAYERS.putIfAbsent(channelKey, new ConcurrentHashSet<Long>());
		playerIds = CHANNEL_PLAYERS.get(channelKey);
		return playerIds;
	}

	
	/**
	 * 定时处理分线信息
	 */
	@Scheduled(name = "定时处理分线信息", value = "0 2/15 * * * ?")
	protected void scheduleToProcessBranching() {
		CopyOnWriteArrayList<Integer> currentBranchings = this.getCurrentBranching();
		Set<Entry<String, ConcurrentHashSet<Long>>> entrySet = CHANNEL_PLAYERS.entrySet();
		for (Iterator<Entry<String, ConcurrentHashSet<Long>>> it = entrySet.iterator(); it.hasNext();) {
			Entry<String, ConcurrentHashSet<Long>> entry = it.next();
			String channelKey = entry.getKey();
			ConcurrentHashSet<Long> channelPlayers = entry.getValue();
			if(StringUtils.isBlank(channelKey) || channelPlayers == null) {
				it.remove();
				continue;
			} 

			Channel channel = Channel.valueOf(channelKey);
			if(channel == null) {
				it.remove();
				continue;
			}
			
			int chatChannel = channel.getChannel();
			if(chatChannel != ChatChannel.WORLD_CHANNEL.ordinal()) {
				continue;
			}
			
			int branching = channel.getSubChannel();
			if(!currentBranchings.contains(branching) && channelPlayers.isEmpty()) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("定时删除分线:[{}] ", branching);
				}
				it.remove();
			}
			
		}
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("定时删除分线执行器执行..... ");
		}
	}
}
