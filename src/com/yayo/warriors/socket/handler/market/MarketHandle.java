package com.yayo.warriors.socket.handler.market;

import static com.yayo.common.socket.type.ResponseCode.*;
import static com.yayo.warriors.constant.CommonConstant.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.handler.BaseHandler;
import com.yayo.common.socket.handler.Invoker;
import com.yayo.common.socket.message.Request;
import com.yayo.common.socket.message.Response;
import com.yayo.common.utility.CollectionUtils;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.market.constant.MarketConstant;
import com.yayo.warriors.module.market.entity.MarketItem;
import com.yayo.warriors.module.market.facade.MarketFacade;
import com.yayo.warriors.module.market.model.UserBooth;
import com.yayo.warriors.module.market.vo.UserBoothVO;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;

import flex.messaging.io.amf.ASObject;

@Component
public class MarketHandle extends BaseHandler {
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private MarketFacade marketFacade;
	
	
	protected int getModule() {
		return Module.MARKET;
	}

	
	protected void inititialize() {
		putInvoker(MarketCmd.ADD_MARKET_PROPS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				addMarketProps(session, request, response);
			}
		});

		putInvoker(MarketCmd.ADD_MARKET_EQUIP, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				addMarketEquip(session, request, response);
			}
		});
		
		putInvoker(MarketCmd.BUY_MARKET_ITEM, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				buyMarketProps(session, request, response);
			}
		});
		
		putInvoker(MarketCmd.REMOVE_MARKET_ITEM, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				removeMarketProps(session, request, response);
			}
		});
		
		putInvoker(MarketCmd.SEARCH_MARKET, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				searchMarketProps(session, request, response);
			}
		});
		
		putInvoker(MarketCmd.LOAD_SELF_MARKET, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadSelfMarket(session, request, response);
			}
		});
		
		putInvoker(MarketCmd.LOAD_ALL_MARKET, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadAllMarket(session, request, response);
			}
		});
		
		putInvoker(MarketCmd.LOAD_OTHER_MARKET, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadOtherMarket(session, request, response);
			}
		});
		
		putInvoker(MarketCmd.MODIFY_BOOTH_NAME, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				modifyBoothName(session, request, response);
			}
		});
		
	}
	
	
	protected void addMarketProps(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long userPropsId = 0L;
		long siliver     = 0L;
		long golden      = 0L;
		
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.USER_PROPSID)) {
				userPropsId = ((Number) aso.get(ResponseKey.USER_PROPSID)).longValue();
			}
			if (aso.containsKey(ResponseKey.SILVER)) {
				siliver = ((Number) aso.get(ResponseKey.SILVER)).longValue();
			}
			if (aso.containsKey(ResponseKey.GOLDEN)) {
				golden = ((Number) aso.get(ResponseKey.GOLDEN)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<MarketItem> result =  marketFacade.putProps2Market(playerId, userPropsId, siliver, golden);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, result.getResult());
		
		if (result.getResult() == MarketConstant.SUCCESS) {
			resultMap.put(ResponseKey.VALUES, result.getValue());
		}
		
		response.setValue(resultMap);
		session.write(response);
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("摆摊物品上架, 返回值: [{}]", result.getResult());
		}
	}
	
	protected void addMarketEquip(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long userEquipId = 0L;
		long siliver     = 0L;
		long golden      = 0L;
		
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.USER_EQUIP_ID)) {
				userEquipId = ((Number) aso.get(ResponseKey.USER_EQUIP_ID)).longValue();
			}
			if (aso.containsKey(ResponseKey.SILVER)) {
				siliver = ((Number) aso.get(ResponseKey.SILVER)).longValue();
			}
			if (aso.containsKey(ResponseKey.GOLDEN)) {
				golden = ((Number) aso.get(ResponseKey.GOLDEN)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<MarketItem> result = marketFacade.putEquip2Market(playerId, userEquipId, siliver, golden);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, result.getResult());
		
		if (result.getResult() == MarketConstant.SUCCESS) {
			resultMap.put(ResponseKey.VALUES, result.getValue());
		}
		
		response.setValue(resultMap);
		session.write(response);
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("摆摊装备上架, 返回值: [{}]", result.getResult());
		}
	}
	
	protected void removeMarketProps(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long itemId = 0L;
		
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.MARKET_ITEM_ID)) {
				itemId = ((Number) aso.get(ResponseKey.MARKET_ITEM_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = marketFacade.removeMarketItem(playerId, itemId);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, result);
		resultMap.put(ResponseKey.MARKET_ITEM_ID, itemId);
		
		response.setValue(resultMap);
		session.write(response);
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("摆摊物品下架, 返回值: [{}]", result);
		}
	}
	protected void buyMarketProps(IoSession session, Request request, Response response) {
		long itemId = 0L;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.MARKET_ITEM_ID)) {
				itemId = ((Number) aso.get(ResponseKey.MARKET_ITEM_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = marketFacade.buyMarketItem(playerId, itemId);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, result);
		resultMap.put(ResponseKey.MARKET_ITEM_ID, itemId);
		
		response.setValue(resultMap);
		session.write(response);
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("购买摆摊物品, 返回值: [{}]", result);
		}
	}
	
	protected void searchMarketProps(IoSession session, Request request, Response response) {
		String keywords = null;
		int searchType = 0;
		int start = 0;
		int pageSize = 0;
		
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.KEYWORDS)) {
				keywords = (String) aso.get(ResponseKey.KEYWORDS);
			}
			if (aso.containsKey(ResponseKey.TYPE)) {
				searchType = ((Number) aso.get(ResponseKey.TYPE)).intValue();
			}
			if (aso.containsKey(ResponseKey.START)) {
				start = ((Number) aso.get(ResponseKey.START)).intValue();
			}
			if (aso.containsKey(ResponseKey.PAGE_SIZE)) {
				pageSize = ((Number) aso.get(ResponseKey.PAGE_SIZE)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		Collection<UserBoothVO> voList = new ArrayList<UserBoothVO>();
		Collection<UserBoothVO> resultList = marketFacade.searchPlayerBooth(keywords, searchType);
		if (!resultList.isEmpty()) {
			voList = CollectionUtils.subListCopy((List<UserBoothVO>) resultList, start, pageSize);
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		resultMap.put(ResponseKey.START, start);
		resultMap.put(ResponseKey.TOTAL, resultList.size());
		resultMap.put(ResponseKey.VALUES, voList.toArray());
		
		response.setValue(resultMap);
		session.write(response);
	}
	
	
	protected void loadOtherMarket(IoSession session, Request request, Response response) {
		long playerId = 0L;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.PLAYER_ID)) {
				playerId = ((Number) aso.get(ResponseKey.PLAYER_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		UserBooth userBooth = marketFacade.loadBoothByPlayerId(playerId);
		
		response.setValue(userBooth != null ? UserBoothVO.valueOf(userBooth) : PLAYER_NOT_FOUND);
		session.write(response);
	}
	
	
	protected void loadSelfMarket(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		UserBooth userBooth = marketFacade.loadBoothByPlayerId(playerId);
		response.setValue(UserBoothVO.valueOf(userBooth));
		session.write(response);
	}
	
	
	protected void loadAllMarket(IoSession session, Request request, Response response) {
		int type = -1;
		int start = 0;
		int pageSize = 0;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TYPE)) {
				type = ((Number) aso.get(ResponseKey.TYPE)).intValue();
			}
			if (aso.containsKey(ResponseKey.START)) {
				start = ((Number) aso.get(ResponseKey.START)).intValue();
			}
			if (aso.containsKey(ResponseKey.PAGE_SIZE)) {
				pageSize = ((Number) aso.get(ResponseKey.PAGE_SIZE)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		Collection<UserBoothVO> voList = new ArrayList<UserBoothVO>();
		Collection<UserBoothVO> result = marketFacade.loadMarketByItemType(type);
		if (!result.isEmpty()) {
			voList = CollectionUtils.subListCopy((List<UserBoothVO>) result, start, pageSize);
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		resultMap.put(ResponseKey.START, start);
		resultMap.put(ResponseKey.TOTAL, result.size());
		resultMap.put(ResponseKey.VALUES, voList.toArray());
		
		response.setValue(resultMap);
		session.write(response);
	}
	
	protected void modifyBoothName(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		String keywords = "";
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.KEYWORDS)) {
				keywords = (String) aso.get(ResponseKey.KEYWORDS);
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = marketFacade.modifyBoothName(playerId, keywords);
		response.setValue(result);
		session.write(response);
		
	}
}
