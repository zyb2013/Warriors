package com.yayo.warriors.socket.handler.mortal;

import java.util.Arrays;
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
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.mortal.constant.MortalConstant;
import com.yayo.warriors.module.mortal.facade.MortalFacade;
import com.yayo.warriors.module.mortal.vo.MortalBodyVo;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.type.ElementType;

import flex.messaging.io.amf.ASObject;

@Component
public class MortalHandle extends BaseHandler {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private MortalFacade mortalFacade;
	
	
	protected int getModule() {
		return Module.MORTAL;
	}

	
	protected void inititialize() {
		
		putInvoker(MortalCmd.MORTALBODY_PROGRESS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadMortalProgress(session, request, response);
			}
		});
		
		putInvoker(MortalCmd.MORTALBODY_LEVELUP, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				mortalLevelUp(session, request, response);
			}
		});
		
	}
	
	protected void loadMortalProgress(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		MortalBodyVo vo = mortalFacade.getAllAttribute(playerId);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, MortalConstant.SUCCESS);
		resultMap.put(ResponseKey.PROGRESS, vo);
		response.setValue(resultMap);
		session.write(response);
	}

	protected void mortalLevelUp(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int type = -1;
		int autoCount = 0;
		long userPropsId = 0;
		String userItems = "";
		boolean useProps = false;
		
		ASObject aso = (ASObject) request.getValue();
		try {
			if (aso.containsKey(ResponseKey.TYPE)) {
				type = ((Number) aso.get(ResponseKey.TYPE)).intValue();
			}
			if (aso.containsKey(ResponseKey.AUTO_BUY_COUNT)) {
				autoCount = ((Number) aso.get(ResponseKey.AUTO_BUY_COUNT)).intValue();
			}
			if (aso.containsKey(ResponseKey.USER_PROPSID)) {
				userPropsId = ((Number) aso.get(ResponseKey.USER_PROPSID)).longValue();
			}
			if (aso.containsKey(ResponseKey.USER_PROPS)) {
				userItems = (String) aso.get(ResponseKey.USER_PROPS);
			}
			if (aso.containsKey(ResponseKey.USE_PROPS)) {
				useProps = (Boolean) aso.get(ResponseKey.USE_PROPS);
			}
		} catch (Exception e) {
			response.setValue(MortalConstant.FAILURE);
			session.write(response);
			return;
		}
		
		ResultObject<List<BackpackEntry>> result = mortalFacade.mortalBodyLevelUp(playerId, type, userItems, useProps, userPropsId, autoCount);
		response.setValue(result.getResult());
		session.write(response);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("角色 [{}] 肉身升级: 返回值: [{}]", playerId, result.getResult());
		}
		
		List<BackpackEntry> backpackEntries = result.getValue();
		if (backpackEntries != null && !backpackEntries.isEmpty()) {
			int backpack = BackpackType.DEFAULT_BACKPACK;
			MessagePushHelper.pushUserProps2Client(playerId, backpack, false, backpackEntries);
		}
		List<Long> receiver = Arrays.asList(playerId);
		List<UnitId> playerUnits = Arrays.asList(UnitId.valueOf(playerId, ElementType.PLAYER));
		UserPushHelper.pushAttribute2AreaMember(playerId, receiver, playerUnits, AttributeKeys.SILVER, AttributeKeys.GOLDEN);
	}
	
	
	
}
