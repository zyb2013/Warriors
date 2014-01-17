package com.yayo.warriors.module.onhook.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.train.TrainCmd;

/**
 * 修炼帮助类
 * 
 * @author huachaoping
 */
@Component
public class TrainHelper {
	
	@Autowired
	private SessionManager sessionManager;
	
	/**
	 * 申请双修
	 * 
	 * @param playerId          玩家ID
	 * @param targetId          目标ID
	 * @param applyName         申请者名字
	 */
	public void applyCouplesTrain(Player player, PlayerBattle battle, long targetId){
		Response response = Response.defaultResponse(Module.TRAIN, TrainCmd.APPLY_COUPLES_TRAIN);
		Map<String, Object> sender = new HashMap<String, Object>(4);
		sender.put(ResponseKey.PLAYER_ID, player.getId());
		sender.put(ResponseKey.PLAYER_NAME, player.getName());
		sender.put(ResponseKey.LEVEL, battle.getLevel());
		sender.put(ResponseKey.JOB, battle.getJob().ordinal());
		response.setValue(sender);
		sessionManager.write(targetId, response);
	}
	
	/**
	 * 推送双修处理结果(同意-true)
	 * 
	 * @param player            
	 * @param targetId          目标Id
	 * @param isAgree           是否同意
	 */
	public void processCouplesTrain(Player player, Long targetId, boolean isAgree) {
		Response response = Response.defaultResponse(Module.TRAIN, TrainCmd.COUPLES_TRAIN_PROCESS);
		Map<String, Object> sender = new HashMap<String, Object>(3);
		sender.put(ResponseKey.PLAYER_ID, player.getId());
		sender.put(ResponseKey.PLAYER_NAME, player.getName());
		sender.put(ResponseKey.TYPE, isAgree);
		response.setValue(sender);
		sessionManager.write(targetId, response);
	}
	
	/**
	 * 推送同屏玩家的打坐状态
	 * 
	 * @param playerId          玩家Id
	 * @param status            打坐状态
	 */
	public void pushScreenTrainMessage(long playerId, int status, Collection<Long> idList) {
		Response response = Response.defaultResponse(Module.TRAIN, TrainCmd.PUSH_SCREEN_TRAIN_MESSAGE);
		Map<String, Object> sender = new HashMap<String, Object>(2);
		sender.put(ResponseKey.PLAYER_ID, playerId);
		sender.put(ResponseKey.TYPE, status);
		response.setValue(sender);
		sessionManager.write(idList, response);
	}
	
	/**
	 * 推送玩家打坐所加经验和真气
	 * 
	 * @param playerId          玩家Id
	 * @param exp               所得经验
	 * @param gas               所得真气
	 */
	public void pushTrainAttrMessage(long playerId, int exp, int gas) {
		Response response = Response.defaultResponse(Module.TRAIN, TrainCmd.PUSH_TRAIN_ATTR_MESSAGE);
		Map<String, Object> sender = new HashMap<String, Object>(2);
		sender.put(ResponseKey.EXP, exp);
		sender.put(ResponseKey.GAS, gas);
		response.setValue(sender);
		sessionManager.write(playerId, response);
	}
	
	
	/**
	 * 推送取消双修
	 * 
	 * @param player           
	 * @param targetId
	 */
	public void pushCancelCoupleTrain(Player player, long targetId) {
		Response response = Response.defaultResponse(Module.TRAIN, TrainCmd.PUSH_CANCEL_COUPLE_TRAIN);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.PLAYER_ID, player.getId());
		resultMap.put(ResponseKey.PLAYER_NAME, player.getName());
		response.setValue(resultMap);
		sessionManager.write(targetId, response);
	}
	
	
	/**
	 * 推送玩家的方向
	 * 
	 * @param player
	 * @param direction
	 */
	public void pushPlayerDirection(Player player, int direction) {
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.PLAYER_ID, player.getId());
		resultMap.put(ResponseKey.DIRECTION, direction);
		Response response = Response.defaultResponse(Module.TRAIN, TrainCmd.PUSH_PLAYER_DIRECTION, resultMap);
		sessionManager.write(player.getId(), response);
	}
	
}
