package com.yayo.warriors.socket.handler.shop;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.handler.BaseHandler;
import com.yayo.common.socket.handler.Invoker;
import com.yayo.common.socket.message.Request;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.type.ResponseCode;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.module.shop.facade.ShopFacade;
import com.yayo.warriors.module.shop.model.MallProps;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.type.ElementType;

import flex.messaging.io.amf.ASObject;


@Component
public class ShopHandler extends BaseHandler {
	@Autowired
	private ShopFacade shopFacade;

	
	protected int getModule() {
		return Module.SHOP;
	}

	
	protected void inititialize() {
		
		putInvoker(ShopCmd.BUY_MALL_PROPS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				buyMallProps(session, request, response);
			}
		});

		putInvoker(ShopCmd.BUY_SHOP_PROPS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				buyShopPops(session, request, response);
			}
		});

		putInvoker(ShopCmd.LIST_MALL_SPECIALOFFER, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				listMallOffer(session, request, response);
			}
		});
		
		
		putInvoker(ShopCmd.BUY_MALL_OFFERS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				buyMallOffers(session, request, response);
			}
		});
	}

	public void buyMallProps(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int count = 0;
		int mallId = 0;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.COUNT)) {
				count = ((Number) aso.get(ResponseKey.COUNT)).intValue();
			}
			if (aso.containsKey(ResponseKey.MALLID)) {
				mallId = ((Number) aso.get(ResponseKey.MALLID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		ResultObject<List<BackpackEntry>> resultObject = shopFacade.buyPropsByMall(playerId, mallId, count);
		Map<String,Object> maps = new HashMap<String,Object>(1);
		maps.put(ResponseKey.RESULT, resultObject.getResult());
		response.setValue(maps);
		session.write(response);
		pushBackpackEntry2Client(playerId, resultObject.getValue(), count, AttributeKeys.GOLDEN, AttributeKeys.COUPON);
	}

	public void buyShopPops(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int count = 0;
		int shopId = 0;
		int npcId = 0;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.COUNT)) {
				count = ((Number) aso.get(ResponseKey.COUNT)).intValue();
			}
			if (aso.containsKey(ResponseKey.SHOPID)) {
				shopId = ((Number) aso.get(ResponseKey.SHOPID)).intValue();
			}
			if (aso.containsKey(ResponseKey.NPCID)) {
				npcId = ((Number) aso.get(ResponseKey.NPCID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		ResultObject<List<BackpackEntry>> resultObject = shopFacade.buyPropsByShop(playerId, shopId, count, npcId);
		response.setValue(resultObject.getResult());
		session.write(response);
		pushBackpackEntry2Client(playerId, resultObject.getValue(), count, AttributeKeys.SILVER);
	}

	private void pushBackpackEntry2Client(long playerId, List<BackpackEntry> backpackEntries, int count, Object...attributes) {
		if (backpackEntries == null || backpackEntries.isEmpty()) {
			return;
		}
		
		List<Long> players = Arrays.asList(playerId);
		List<UnitId> playerUnitIds = Arrays.asList(UnitId.valueOf(playerId, ElementType.PLAYER));
		MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, backpackEntries);
		if(attributes.length > 0) {
			UserPushHelper.pushAttribute2AreaMember(playerId, players, playerUnitIds, attributes);
		}
		
		BackpackEntry backpackEntry = backpackEntries.get(0);
		int baseId = backpackEntry.getBaseId();
		GoodsVO goodsVO = GoodsVO.valueOf(baseId, backpackEntry.getGoodsType(), count);
		MessagePushHelper.pushGoodsCountChange2Client(playerId, goodsVO);
	}

	public void listMallOffer(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		List<MallProps> propsList = shopFacade.listMallSpecialOffer(playerId);
		response.setValue(propsList.toArray());
		session.write(response);
	}
	
	
	public void buyMallOffers(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int mallId = 0;
		int count = 0;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.MALLID)) {
				mallId = ((Number) aso.get(ResponseKey.MALLID)).intValue();
			}
			if (aso.containsKey(ResponseKey.COUNT)) {
				count = ((Number) aso.get(ResponseKey.COUNT)).intValue();
			}
			
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		ResultObject<List<BackpackEntry>> resultObject = shopFacade.buySpecialMallProps(playerId, mallId, count);
		
		Map<String,Object> maps = new HashMap<String,Object>(1);
		maps.put(ResponseKey.RESULT, resultObject.getResult());
		response.setValue(maps);
		session.write(response);
		pushBackpackEntry2Client(playerId, resultObject.getValue(), count, AttributeKeys.GOLDEN);
	}
	
	
}
