package com.yayo.warriors.module.meridian.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.meridian.MeridianCmd;

@Component
public class MeridianHelper {
	
	@Autowired
	private SessionManager sessionManager;
	
	/**
	 * 推送区域玩家加经验
	 * 
	 * @param playerId             冲穴成功的玩家ID
	 * @param playerName           冲穴的玩家名字
	 * @param playerIdList         推送玩家列表
	 */
	public void pushPlayerAddExp(Long playerId, String playerName, Collection<Long> playerIdList){
		Response response = Response.defaultResponse(Module.MERIDIAN, MeridianCmd.PUSH_PLAYER_ADDEXP);
		Map<String, Object> sender = new HashMap<String, Object>(2);
		sender.put(ResponseKey.PLAYER_ID, playerId);
		sender.put(ResponseKey.PLAYER_NAME, playerName);
		response.setValue(sender);
		sessionManager.write(playerIdList, response);
	}
	
	/**
	 * 推送经脉成功的倒计时
	 * 
	 * @param playerId
	 * @param playerIdList
	 */
	public void pushMeridianTime(Long playerId, Collection<Long> playerIdList) {
		Response response = Response.defaultResponse(Module.MERIDIAN, MeridianCmd.PUSH_MERIDIAN_TIME);
		Map<String, Object> sender = new HashMap<String, Object>(1);
		sender.put(ResponseKey.PLAYER_ID, playerId);
		response.setValue(sender);
		sessionManager.write(playerIdList, response);
	}
}
