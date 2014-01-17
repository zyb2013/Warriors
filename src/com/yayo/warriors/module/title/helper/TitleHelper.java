package com.yayo.warriors.module.title.helper;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.title.TitleCmd;

/**
 * 称号帮助类
 * 
 * @author huachaoping
 */
@Component
public class TitleHelper {
	
	@Autowired
	private SessionManager sessionManager;
	
	/**
	 * 推送玩家获得称号
	 * 
	 * @param playerId    玩家ID
	 * @param titleId     称号ID
	 */
	public void pushObtainTitle(long playerId, int titleId) {
		Response response = Response.defaultResponse(Module.TITLE, TitleCmd.PUSH_OBTAIN_TITLE);
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.TITLE_ID, titleId);
		response.setValue(resultMap);
		sessionManager.write(playerId, response);
	}
}
