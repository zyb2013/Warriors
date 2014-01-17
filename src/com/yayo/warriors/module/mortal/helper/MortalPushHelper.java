package com.yayo.warriors.module.mortal.helper;

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
import com.yayo.warriors.socket.handler.mortal.MortalCmd;

/**
 * 肉身系统推送类
 * 
 * @author huachaoping
 */
@Component
public class MortalPushHelper {

	@Autowired
	private SessionManager sessionManager;
	
	private static ObjectReference<MortalPushHelper> ref = new ObjectReference<MortalPushHelper>();
	
	@PostConstruct
	protected void init() {
		ref.set(this);
	}
	
	private static MortalPushHelper getInstance() {
		return ref.get();
	}
	
	/**
	 * 推送肉身升级信息给客户端
	 * @param playerId        角色ID
	 * @param mortalType      类型
	 * @param level           肉身等级
	 * @param attributes      属性类型数组
	 * @param values          属性值数组
	 */
	public static void pushChangeAttr2Client(long playerId, int mortalType, int level, int[] attributes, int[] values) {
		Response response = Response.defaultResponse(Module.MORTAL, MortalCmd.PUSH_CHANGE_ATTR_MESSAGE);
		Map<String, Object> resultMap = new HashMap<String, Object>(4);
		resultMap.put(ResponseKey.TYPE, mortalType);
		resultMap.put(ResponseKey.LEVEL, level);
		resultMap.put(ResponseKey.PARAMS, attributes);
		resultMap.put(ResponseKey.VALUES, values);
		response.setValue(resultMap);
		getInstance().sessionManager.write(playerId, response);
	}
}
