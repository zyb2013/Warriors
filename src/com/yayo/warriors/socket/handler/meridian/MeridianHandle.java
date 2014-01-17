package com.yayo.warriors.socket.handler.meridian;

import static com.yayo.warriors.socket.handler.meridian.MeridianKey.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.meridian.constant.MeridianConstant;
import com.yayo.warriors.module.meridian.facade.MeridianFacade;
import com.yayo.warriors.module.meridian.vo.AttributeVo;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.type.ElementType;

import flex.messaging.io.amf.ASObject;

/**
 * @author huachaoping
 * 
 */
@Component
public class MeridianHandle extends BaseHandler {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private MeridianFacade meridianFacade;

	
	protected int getModule() {
		return Module.MERIDIAN;
	}

	
	protected void inititialize() {
		putInvoker(MeridianCmd.RUSH_MERIDIAN, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				rushMeridian(session, request, response);
			}

		});


		putInvoker(MeridianCmd.LOAD_ADDED_ATTR, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadMeridianAttr(session, request, response);
			}
		});
		
		putInvoker(MeridianCmd.BREAKTHROUGH, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				breakthrough(session, request, response);
			}
		});
		
		
		putInvoker(MeridianCmd.ADD_EXP, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				addExpByMeridian(session, request, response);
			}
		});
	}
	protected void rushMeridian(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int meridianId = 0;
		String userItems = "";
		int autoCount = 0;
		
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(MERIDIAN_ID)) {
				meridianId = ((Number) aso.get(MERIDIAN_ID)).intValue();
			}
			if (aso.containsKey(ResponseKey.USER_PROPSID)) {
				userItems = (String) aso.get(ResponseKey.USER_PROPSID); 
			}
			if (aso.containsKey(ResponseKey.AUTO_BUY_COUNT)) {
				autoCount = ((Number) aso.get(ResponseKey.AUTO_BUY_COUNT)).intValue();
			}
		} catch (Exception ex) {
			response.setValue(MeridianConstant.FAILURE);
			session.write(response);
			return;
		}
		
		ResultObject<Collection<BackpackEntry>> result = meridianFacade.rushMeridian(playerId, meridianId, userItems, autoCount);
		int resultValue = result.getResult();
		response.setValue(resultValue);
		session.write(response);
		
		Collection<BackpackEntry> backpackEntries = result.getValue();
		if (backpackEntries != null && !backpackEntries.isEmpty()) {
			int backpack = BackpackType.DEFAULT_BACKPACK;
			MessagePushHelper.pushUserProps2Client(playerId, backpack,  false, backpackEntries);
		}
		if(resultValue == MeridianConstant.RUSH_MERIDIAN_FAILURE || resultValue == MeridianConstant.SUCCESS){
			List<Long> receiver = Arrays.asList(playerId);
			List<UnitId> playerUnits = Arrays.asList(UnitId.valueOf(playerId, ElementType.PLAYER));
			UserPushHelper.pushAttribute2AreaMember(playerId, receiver, playerUnits, AttributeKeys.GOLDEN, AttributeKeys.GAS);
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("脉点Id: [{}], 返回值: [{}]", meridianId, resultValue);
			}
		}
	}

	
	
	protected void loadMeridianAttr(IoSession session, Request request,Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		AttributeVo attributeVO = meridianFacade.loadMeridianAttr(playerId);
		response.setValue(attributeVO);
		session.write(response);
	}
	
	protected void breakthrough(IoSession session, Request request, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		
		String userItems = "";
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.USER_PROPSID)) {
				userItems = (String) aso.get(ResponseKey.USER_PROPSID);
			}
		} catch (Exception e) {
			response.setValue(MeridianConstant.FAILURE);
			session.write(response);
			return;
		}
		
		int result = meridianFacade.breakthrough(playerId, userItems);
		
		response.setValue(result);
		session.write(response);
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("突破瓶颈, 返回值: [{}]", result);
		}
	}
	protected void addExpByMeridian(IoSession session, Request request, Response response) {
	}
}
