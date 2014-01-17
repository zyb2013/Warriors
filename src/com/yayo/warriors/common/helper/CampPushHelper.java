package com.yayo.warriors.common.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.push.Pusher;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.camp.CampCmd;

/**
 * 阵营推送类 
 * @author liuyuhua
 */
@Component
public class CampPushHelper {
	
	@Autowired
	private Pusher pusher;
	
	/**
	 * 通知加入阵营
	 * @param playerIds     需要通知的玩家集合
	 * @param playerId      玩家的ID
	 * @param camp          阵营的ID
	 */
	public void pushJoinCamp(Collection<Long> playerIds,long playerId,int camp){
		if(playerIds != null){
			Response response = Response.defaultResponse(Module.CAMP, CampCmd.PUSH_JOIN_CAMP);
			Map<String,Object> result = new HashMap<String,Object>(2);
			result.put(ResponseKey.PLAYER_ID, playerId);
			result.put(ResponseKey.CAMP, camp);
			response.setValue(result);
			pusher.pushMessage(playerIds, response);
		}
	}

}
