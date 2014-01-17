package com.yayo.warriors.module.trade.helper;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.module.trade.vo.TradeVo;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.trade.TradeCmd;

/**
 * 交易系统帮助类
 * @author huachaoping
 */
@Component
public class TradeHelper {
	
	@Autowired
	private SessionManager sessionManager;
	
	private static ObjectReference<TradeHelper> ref = new ObjectReference<TradeHelper>();
	
	@PostConstruct
	protected void init() {
		ref.set(this);
	}
	
	private static TradeHelper getInstance() {
		return ref.get();
	}
	
	/**
	 * 推送邀请信息
	 * @param playerId           邀请者Id
	 * @param targetId           被邀请者Id
	 * @param name               邀请者名字
	 */
	public static void pushInvitedMessage(long playerId, long targetId, String name) {
		Response response = Response.defaultResponse(Module.TRADE, TradeCmd.PUSH_INVITED_MESSAGE);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.PLAYER_ID, playerId);
		resultMap.put(ResponseKey.PLAYER_NAME, name);
		response.setValue(resultMap);
		getInstance().sessionManager.write(targetId, response);
	}
	
	/**
	 * 推送处理邀请信息
	 * @param playerId           被邀请者Id
	 * @param targetId           邀请者Id
	 * @param name               被邀请者的名字
	 * @param isAgree            是否同意交易      true - 同意交易  false - 拒绝交易
	 */
	public static void pushPlayerBeInvited(long playerId, long targetId, boolean isAgree, String name) {
		Response response = Response.defaultResponse(Module.TRADE, TradeCmd.PUSH_PROCESS_TRADE_MESSAGE);
		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		resultMap.put(ResponseKey.PLAYER_ID, playerId);
		resultMap.put(ResponseKey.PLAYER_NAME, name);
		resultMap.put(ResponseKey.TYPE, isAgree);
		response.setValue(resultMap);
		getInstance().sessionManager.write(targetId, response);
	}
	
	/**
	 * 推送交易物品信息给玩家
	 * @param playerId           玩家Id
	 * @param targetId           目标Id
	 * @param vo {@link TradeVo}
	 */
	public static void pushPlayerTradeProps(long playerId, long targetId, TradeVo vo) {
		Response response = Response.defaultResponse(Module.TRADE, TradeCmd.PUSH_PLAYER_TRADE_PROPS);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.PLAYER_ID, playerId);
		resultMap.put(ResponseKey.VALUES, vo);
		response.setValue(resultMap);
		getInstance().sessionManager.write(targetId, response);
	}
	
	/**
	 * 推送玩家锁定物品
	 * @param playerId           玩家Id
	 * @param targetId           目标Id
	 * @param lockProps          锁定
	 */
	public static void pushLockProps(long playerId, long targetId, boolean lockProps) {
		Response response = Response.defaultResponse(Module.TRADE, TradeCmd.PUSH_LOCK_TRADE_MESSAGE);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.PLAYER_ID, playerId);
		resultMap.put(ResponseKey.TYPE, lockProps);
		response.setValue(resultMap);
		getInstance().sessionManager.write(targetId, response);
	}
	
	/**
	 * 推送玩家取消交易
	 * @param targetId           目标Id
	 * @param reason             原因
	 * @param name               玩家名字
	 */
	public static void pushCancleTrade(long targetId, String name, int reason) {
		Response response = Response.defaultResponse(Module.TRADE, TradeCmd.PUSH_CANCLE_TRADE_MESSAGE);
	    Map<String, Object> resultMap = new HashMap<String, Object>(2);
	    resultMap.put(ResponseKey.PLAYER_NAME, name);
	    resultMap.put(ResponseKey.REASON, reason);
		response.setValue(resultMap);
		getInstance().sessionManager.write(targetId, response);
	}
	
	/**
	 * 推送玩家已点击交易
	 * @param playerId
	 * @param targetId
	 * @param click
	 */
	public static void pushClick2Trade(long playerId, long targetId, boolean click) {
		Response response = Response.defaultResponse(Module.TRADE, TradeCmd.PUSH_CLICK_TRADE_MESSAGE);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.PLAYER_ID, playerId);
		resultMap.put(ResponseKey.TYPE, click);
		response.setValue(resultMap);
		getInstance().sessionManager.write(targetId, response);
	}
}
