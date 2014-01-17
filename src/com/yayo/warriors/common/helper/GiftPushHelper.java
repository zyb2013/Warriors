package com.yayo.warriors.common.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.module.gift.entity.Gift;
import com.yayo.warriors.module.gift.vo.GiftVo;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.gift.GiftCmd;

/**
 * 礼包帮助类
 * 
 * @author huachaoping
 */
@Component
public class GiftPushHelper {

	@Autowired
	private SessionManager sessionManager;
	
	private static ObjectReference<GiftPushHelper> ref = new ObjectReference<GiftPushHelper>();
	@PostConstruct
	protected void init() {
		ref.set(this);
	}
	
	private static GiftPushHelper getInstance() {
		return ref.get();
	}
	
	
	/**
	 * 推送礼包
	 * 
	 * @param playerIds
	 * @param gift
	 */
	public static void pushGift2Client(Collection<Long> playerIds, Gift gift) {
		GiftVo giftVo = GiftVo.valueOf(gift, 0);
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.VALUES,  giftVo);
		Response response = Response.defaultResponse(Module.GIFT, GiftCmd.SEND_GIFT, resultMap);
		getInstance().sessionManager.write(playerIds, response);
	}
	
	
	/**
	 * 推送删除礼包
	 * 
	 * @param playerIds
	 * @param giftId
	 */
	public static void pushDelGift2Client(Collection<Long> playerIds, List<Integer> giftIds) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(ResponseKey.IDS, giftIds.toArray());
		Response response = Response.defaultResponse(Module.GIFT, GiftCmd.DEL_GIFT, resultMap);
		getInstance().sessionManager.write(playerIds, response);
	}
	
}
