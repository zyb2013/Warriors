package com.yayo.warriors.common.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.achieve.AchieveCmd;


@Component
public class AchievePushHelper {
	
	@Autowired
	private SessionManager sessionManager;
	
	private static ObjectReference<AchievePushHelper> ref = new ObjectReference<AchievePushHelper>();
	@PostConstruct
	protected void init() {
		ref.set(this);
	}
	
	private static AchievePushHelper getInstance() {
		return ref.get();
	}
	
	/**
	 * 推送成就
	 * 
	 * @param playerId
	 * @param achieveId
	 */
	public static void pushAchieved2Client(long playerId, int achieveId, Collection<Integer> ids) {
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.ID, achieveId);
		resultMap.put(ResponseKey.IDS, ids.toArray());       // 历史记录
		Response response = Response.defaultResponse(Module.ACHIEVE, AchieveCmd.PUSH_ACHIEVED, resultMap);
		getInstance().sessionManager.write(playerId, response);
	}
}
