package com.yayo.warriors.common.helper;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.admin.vo.PlayerVipVO;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.vip.VipCmd;

/**
 * VIP推送对象
 * 
 * @author Hyint
 */
@Component
public class VipPushHelper {

	@Autowired
	private SessionManager sessionManager;
	
	private static ObjectReference<VipPushHelper> ref = new ObjectReference<VipPushHelper>();
	
	@PostConstruct
	protected void init() {
		ref.set(this);
	}
	
	
	private static VipPushHelper getInstance() {
		return ref.get();
	}
	
	/**
	 * 推送VIP对象到客户端
	 * 
	 * @param  playerId			角色ID
	 * @param  playerVipVO		角色VIPVO对象
	 */
	public static void pushVipVO2Client(long playerId, PlayerVipVO playerVipVO) {
		if(playerVipVO != null) {
			Map<String, Object> resultMap = new HashMap<String, Object>(2);
			resultMap.put(ResponseKey.RESULT, CommonConstant.SUCCESS);
			resultMap.put(ResponseKey.VALUES, playerVipVO);
			Response response = Response.defaultResponse(Module.VIP, VipCmd.LOAD_VIP, resultMap);
			getInstance().sessionManager.write(playerId, response);
		}
	}
	
	
	/**
	 * 推送VIP玩家续费
	 * 
	 * @param playerId
	 */
	public static void pushVipAttention(long playerId) {
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.TIME, 0);
		Response response = Response.defaultResponse(Module.VIP, VipCmd.PUSH_VIP_ATTENTION, resultMap);
		getInstance().sessionManager.write(playerId, response);
	}
	
}
