package com.yayo.warriors.module.market.helper;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.module.market.entity.MarketItem;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.market.MarketCmd;

/**
 * 摆摊推送
 * 
 * @author huachaoping
 */
@Component
public class MarketHelper {
	
	@Autowired
	private SessionManager sessionManager;
	
	/**
	 * 历史记录推送
	 * 
	 * @param buyerName
	 * @param marketItem
	 */
	public void pushMarketSellMessage(String buyerName, MarketItem marketItem) {
		Response response = Response.defaultResponse(Module.MARKET, MarketCmd.PUSH_MARKET_SELL_HISTORY);
		Map<String, Object> resultMap = new HashMap<String, Object>(8);
		resultMap.put(ResponseKey.PLAYER_NAME, buyerName);
		resultMap.put(ResponseKey.MARKET_ITEM_ID, marketItem.getId());
		resultMap.put(ResponseKey.TYPE, marketItem.getType());
		resultMap.put(ResponseKey.BASEID, marketItem.getBaseId());
		resultMap.put(ResponseKey.TIME, System.currentTimeMillis());
		resultMap.put(ResponseKey.COUNT, marketItem.getSellCount());
		resultMap.put(ResponseKey.GOLDEN, marketItem.getSellGolden());
		resultMap.put(ResponseKey.SILVER, marketItem.getSellSilver());
		response.setValue(resultMap);
		sessionManager.write(marketItem.getPlayerId(), response);
	}
}
