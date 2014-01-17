package com.yayo.warriors.common.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.push.Pusher;
import com.yayo.warriors.basedb.model.DungeonConfig;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.dungeon.manager.DungeonManager;
import com.yayo.warriors.module.dungeon.model.Dungeon;
import com.yayo.warriors.module.dungeon.types.DungeonType;
import com.yayo.warriors.module.user.entity.PlayerMotion;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.dungeon.DungeonCmd;

@Component
public class DungeonPushHelper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DungeonPushHelper.class);
	@Autowired
	private Pusher pusher;
	@Autowired
	private DungeonManager dungeonManger;
	
	/**
	 * 通知玩家副本统计信息
	 * @param playerIds   玩家的集合
	 * @param msg         需要发送的信息
	 */
	public void pushDungeonStatistics(Collection<Long> playerIds,Map<String,Object> msg){
		if(playerIds != null && !playerIds.isEmpty() && msg != null && !msg.isEmpty()){
			Response response = Response.defaultResponse(Module.DUNGEON, DungeonCmd.PUT_DUNGEON_STATISTICS, msg);
			pusher.pushMessage(playerIds, response);
		}
	}
	
	/**
	 * 通知玩家第N波即将开始
	 * @param dungeon
	 */
	public void pushNoticeProgress(Dungeon dungeon){
		if(dungeon == null){
			return;
		}
		Collection<Long> players = dungeon.filterPlayers();
		
		if(players == null || players.isEmpty()){
			return;
		}
		
		DungeonConfig config = dungeon.getDungeonConfig();
		if(config == null){
			return ;
		}
		
		boolean falg = false;
		int round =	dungeon.getRoundCount() + 1;
		if(round > config.getTotleRoundCount()){
			return;
		}
		
		if(config.getTotleRoundCount() == round){
			falg = true;
		}
		Response response = Response.defaultResponse(Module.DUNGEON, DungeonCmd.PUT_NOTICE_PROGRESS);
		Map<String,Object> result = new HashMap<String,Object>(2);
		result.put(ResponseKey.ROUND, round);
		result.put(ResponseKey.FALG, falg);
		response.setValue(result);
		pusher.pushMessage(players, response);
	}
	
	
	
	/**
	 * 在副本状态下转场
	 * @param playerDungeon  玩家的副本对象
	 * @param motion         玩家的行走对象
	 */
	public void changeScreen(PlayerDungeon playerDungeon,PlayerMotion motion) {
		if(playerDungeon == null){
			return;
		}
		
		if(!playerDungeon.isDungeonStatus()){
			return;
		}
		
		long playerId = playerDungeon.getId();
		Dungeon dungeon = this.dungeonManger.getDungeon(playerDungeon.getDungeonId());
		DungeonConfig dungeonConfig = dungeon.getDungeonConfig();
		if(dungeonConfig == null){
			return;
		}
		
		if(dungeonConfig.getType() == DungeonType.HIGH_RICH){ //高富帅类型副本特殊处理
			Integer highBaseId = dungeonConfig.getHighRichs(motion.getX(), motion.getY()); //高富帅副本基础ID
			if(highBaseId != null){
				this.pushCoerceInDungeon(playerId, highBaseId); 
			}else{
				this.pushCoerceOutDungeon(playerId);
			}
		}else{
			/* 如果存在下一个节点,将发送强制进入命令,否则将突出*/
			if(dungeonConfig.hasChainId()){
				this.pushCoerceInDungeon(playerId, dungeonConfig.getChainId()); 
			}else{
				this.pushCoerceOutDungeon(playerId);
			}
		}
	}
	
	/**
	 * 通知玩家离开副本
	 * @param playerIds  玩家的ID集合
	 */
	public void pushCoerceleave(Collection<Long> playerIds) {
		for(long playerId : playerIds){
			PlayerDungeon playerDungeon = this.dungeonManger.getPlayerDungeon(playerId);
			if(playerDungeon == null){
				continue;
			}
			
			if(playerDungeon.isDungeonStatus()){
				this.pushCoerceOutDungeon(playerId);
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("通知[{}]玩家,强制退出副本.",playerId);
				}
			}
		}
	}
	
	
	/**
	 * 通知玩家进入副本
	 * @param playerId        玩家的ID
	 * @param dungeonBaseId   副本的ID
	 */
	private void pushCoerceInDungeon(long playerId,int dungeonBaseId) {
		Response response = Response.defaultResponse(Module.DUNGEON, DungeonCmd.PUT_COERCE_IN_DUNGEON);
		response.setValue(dungeonBaseId);
		pusher.pushMessage(playerId, response);
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("玩家[{}],主动通知进入副本[{}]",playerId,dungeonBaseId);
		}
	}
	
	/**
	 * 通知玩家进入副本
	 * @param playerId       玩家的ID
	 */
	private void pushCoerceOutDungeon(long playerId) {
		Response response = Response.defaultResponse(Module.DUNGEON, DungeonCmd.PUT_COERCE_OUT_DUNGEON);
		response.setValue(new Object());
		pusher.pushMessage(playerId, response);
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("玩家[{}],主动通知退出副本[{}]",playerId);
		}
	}
	
	
	/**
	 * 通知副本中的玩家,副本成功完成
	 * @param playerIds      玩家的ID集合
	 * @param dungeonBaseId  副本基础ID
	 */
	public void pushDungeonComplete(Collection<Long> playerIds,int dungeonBaseId) {
		if(playerIds != null){
			Response response = Response.defaultResponse(Module.DUNGEON,DungeonCmd.PUT_DUNGEON_COMPLETE);
			response.setValue(dungeonBaseId);
			pusher.pushMessage(playerIds, response);
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("通知玩家[{}],副本成功完成.",playerIds);
			}
		}
	}
	
	/**
	 * 通知副本中的玩家,副本失败
	 * @param playerIds      玩家的ID集合
	 */
	public void pushDungeonFail(Collection<Long> playerIds) {
		if(playerIds != null){
			Response response = Response.defaultResponse(Module.DUNGEON,DungeonCmd.PUT_DUNGEON_FAIL);
			response.setValue(new Object());
			pusher.pushMessage(playerIds, response);
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("通知玩家[{}],副本失败.",playerIds);
			}
		}
	}
}
