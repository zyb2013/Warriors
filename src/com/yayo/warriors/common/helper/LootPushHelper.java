package com.yayo.warriors.common.helper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.push.Pusher;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.module.drop.model.LootWrapper;
import com.yayo.warriors.module.drop.vo.DropRewardVO;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.GameScreen;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.handler.drop.LootCmd;

/**
 * 掉落推送帮助类
 * 
 * @author Hyint
 */
@Component
public class LootPushHelper {

	private static Logger LOGGER = LoggerFactory.getLogger(LootPushHelper.class);
	private static ObjectReference<LootPushHelper> ref = new ObjectReference<LootPushHelper>();
	
	@Autowired
	private Pusher pusher;
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private MapFacade mapFacade;
	@Autowired
	private UserManager userManager;
	
	@PostConstruct
	protected void init() {
		ref.set(this);
	}
	
	/**
	 * 获得掉落推送类的实例
	 * 
	 * @return {@link LootPushHelper}
	 */
	private static LootPushHelper getInstance() {
		return ref.get();
	}
	
	/**
	 * 推送掉落奖励信息给客户端
	 * 
	 * @param  playerId				角色ID		
	 * @param  mapId				地图ID
	 * @param  branching			所在的分线
	 * @param  dungeonId            所在副本的ID
	 * @param  playerIds			角色ID列表
	 * @param  fightRewards			战斗奖励信息
	 * @param  positionId			所在的据点信息
	 */
	public static void pushNewFightReward2Client(UserDomain userDomain, Collection<Long> playerIds, Collection<LootWrapper> fightRewards) {
		if(fightRewards == null || fightRewards.isEmpty()) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("推送战斗掉落奖励, 奖励为空");
			}
			return;
		}

		long playerId = userDomain.getPlayerId();
		Collection<Long> screeenViews = getInstance().mapFacade.getScreenViews(playerId);
		Set<Long> totalWritePlayerIds = new HashSet<Long>(screeenViews);
		totalWritePlayerIds.addAll(canPush2SharePlayerIds(userDomain, playerIds));
		pushEnterScreenReward(playerId, totalWritePlayerIds, fightRewards);
	}
	
	/**
	 * 可以推送的玩家ID列表
	 * 
	 * @param userDomain
	 * @param playerIds
	 * @return
	 */
	private static Collection<Long> canPush2SharePlayerIds(UserDomain userDomain, Collection<Long> playerIds) {
		if(playerIds == null || playerIds.isEmpty()) {
			return Collections.emptySet();
		}
		
		GameScreen currentScreen = userDomain.getCurrentScreen();
		if(currentScreen == null) {
			return Collections.emptySet();
		}
		
		GameMap gameMap = currentScreen.getGameMap();
		if(gameMap == null) {
			return Collections.emptySet();
		}
		
		Set<Long> totalPlayers = new HashSet<Long>();
		for (Long sharePlayerId : playerIds) {
			if(sharePlayerId == userDomain.getPlayerId()) {
				totalPlayers.add(sharePlayerId);
				continue;
			} 

			UserDomain memberDomain = getInstance().userManager.getUserDomain(sharePlayerId);
			if(memberDomain == null) {
				continue;
			}
			
			GameScreen memberScreen = memberDomain.getCurrentScreen();
			if(memberScreen == null) {
				continue;
			}
			
			GameMap memberGameMap = memberScreen.getGameMap();
			if(memberGameMap == null) {
				continue;
			}
			
			if(memberGameMap.equals(gameMap)) {
				totalPlayers.add(sharePlayerId);
			}
		}
		return totalPlayers;
	}
	
	/**
	 * 推送角色进入场景获取奖励信息
	 * 
	 * @param  playerIdList		角色ID列表
	 * @param  fightRewards		战斗奖励对象
	 */
	public static void pushEnterScreenReward(long playerId, Collection<Long> playerIdList, Collection<LootWrapper> fightRewards) {
		if(playerIdList == null || playerIdList.isEmpty() || fightRewards == null || fightRewards.isEmpty()) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("角色:[{}] 进入场景获取掉落:[{}] ", playerIdList, fightRewards);
			}
			return;
		}
		
		List<DropRewardVO> dropRewardList = getInstance().voFactory.listDropRewardVO(fightRewards);
		if(dropRewardList == null || dropRewardList.size() <= 0) {
			LOGGER.debug("角色:[{}] 构建后的奖励为空，不推送奖励信息", playerIdList);
			return;
		}
		
		Object[] dropRewardArray = dropRewardList.toArray();
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("当前时间:[{}]", System.currentTimeMillis());
			LOGGER.debug("推送掉落信息:[{}] 给玩家:[{}] ", Arrays.toString(dropRewardArray), playerIdList);
		}
		
		Response response = Response.defaultResponse(Module.LOOT, LootCmd.PUSH_LOOT_REWARD, dropRewardArray );
		getInstance().pusher.pushMessage(playerIdList, response);
	}
	
	/**
	 * 玩家拾取奖励物品成功后,推送给其他玩家,删除奖励原件
	 * 
	 * @param playerId 			角色ID
	 * @param playerIdList     	角色ID列表
	 * @param dropwardId       	战斗奖励对象
	 */
	public static void pushRemoveReward(long playerId, Collection<Long> playerIdList, long dropwardId){
		if(playerIdList == null || playerIdList.isEmpty()) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("角色:[{}]",playerIdList);
			}
			return;
		}
		
		if(dropwardId < 0){
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("奖励ID[{}]错误",dropwardId);
			}
			return;
		}
		
		Response response = Response.defaultResponse(Module.LOOT, LootCmd.PUSH_LOOT_REWARD_REMOVE, dropwardId);
		getInstance().pusher.pushMessage(playerIdList, response);
		
	}
}
