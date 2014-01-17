package com.yayo.warriors.common.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.push.Pusher;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.horse.HorseCmd;

/**
 * 坐骑帮助推送类
 * @author liuyuhua
 */
@Component
public class HorsePushHelper {
	@Autowired
	private Pusher pusher;
	@Autowired
	private MapFacade mapFacade;
	
	/**
	 * 玩家上下坐骑
	 * @param playerId   玩家的ID
	 * @param model      坐骑模型
	 */
	public void riding(UserDomain userDomain, int model) {
		if(userDomain == null) {
			return;
		}
		
		long playerId = userDomain.getPlayerId();
		PlayerBattle playerBattle = userDomain.getBattle();
		Collection<Long> playerIds = mapFacade.getScreenViews(playerId);
		playerIds.remove(playerId);
		
		int speed = playerBattle.getAttribute(AttributeKeys.MOVE_SPEED); //速度
		Response response = Response.defaultResponse(Module.HORSE, HorseCmd.PUSH_RIDING_HORSE);
		Map<String,Object> result = new HashMap<String, Object>(3);
		result.put(ResponseKey.PLAYER_ID, playerId);
		result.put(ResponseKey.MOUNT, model);
		result.put(ResponseKey.SPEED, speed);
		response.setValue(result);
		pusher.pushMessage(playerIds, response);
	}

}
