package com.yayo.warriors.socket.handler.fight;

import static com.yayo.common.socket.type.ResponseCode.*;

import java.util.Arrays;
import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.handler.BaseHandler;
import com.yayo.common.socket.handler.Invoker;
import com.yayo.common.socket.message.Request;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.FightPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.buffer.facade.BufferFacade;
import com.yayo.warriors.module.buffer.vo.BufferVO;
import com.yayo.warriors.module.fight.facade.FightFacade;
import com.yayo.warriors.module.fight.model.FightEvent;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;

import flex.messaging.io.amf.ASObject;

@Component
public class FightHandler extends BaseHandler {
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private FightFacade fightFacade;
	@Autowired
	private BufferFacade bufferFacade;
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	
	protected int getModule() {
		return Module.FIGHT;
	}

	
	protected void inititialize() {
		putInvoker(FightCmd.ACTIVE_FIGHT,  new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				activeFight(session, request, response);				
			}
		});
		
		putInvoker(FightCmd.JUMP_COMPLETE,  new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				jumpComplete(session, request, response);				
			}
		});

		putInvoker(FightCmd.OTHER_FIGHT,  new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				otherFight(session, request, response);				
			}
		});
		
		putInvoker(FightCmd.GET_USER_BUFF,  new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				getUserBuffer(session, request, response);				
			}
		});
		
		putInvoker(FightCmd.NOTICE_BUFF_TIMEOUT,  new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				noticeBuffTimeout(session, request, response);				
			}
		});
	}

	protected void noticeBuffTimeout(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		bufferFacade.getUserBuffer(playerId);
	}
	protected void getUserBuffer(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		List<BufferVO> bufferInfoList = voFactory.getUserBufferVO(playerId);
		Object[] bufferInfoArray = bufferInfoList.toArray();
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("角色:[{}] 当前的BUFF信息:[{}] ", playerId, Arrays.toString(bufferInfoArray));
		}
		response.setValue(bufferInfoArray);
		session.write(response);
	}
	
	protected void otherFight(IoSession session, Request request, Response response) {
		int skillId = -1;
		int positionX = -1;
		int positionY = -1;
		long targetId = 0L;
		long attackId = 0L;
		int targetType = -1;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.X)) {
				positionX = ((Number) aso.get(ResponseKey.X)).intValue();
			}
			if(aso.containsKey(ResponseKey.Y)) {
				positionY = ((Number) aso.get(ResponseKey.Y)).intValue();
			}
			if(aso.containsKey(ResponseKey.ATTACK_ID)) {
				attackId = ((Number) aso.get(ResponseKey.ATTACK_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.SKILL_ID)) {
				skillId = ((Number) aso.get(ResponseKey.SKILL_ID)).intValue();
			}
			if(aso.containsKey(ResponseKey.TARGET_TYPE)) {
				targetType = ((Number) aso.get(ResponseKey.TARGET_TYPE)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<FightEvent> fightResult = fightFacade.userPetFight(playerId, attackId, targetId, targetType, skillId, positionX, positionY);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("家将:[{}] 发起使用技能:[{}] 返回值: [{}] ", new Object[] { attackId, skillId, fightResult.getResult() });
			LOGGER.debug("家将:[{}] 攻击战报:[{}] ", attackId, fightResult.getValue());
		}
		
		response.setValue(fightResult.getResult());
		session.write(response);
		FightPushHelper.pushReport2Client(fightResult.getValue());
	}

	protected void jumpComplete(IoSession session, Request request, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		fightFacade.updatePlayerJumpInfo(playerId);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("------------------> 玩家:[{}] 主动通知服务器跳跃落地. <--------------------", playerId);
		}
	}

	public void activeFight(IoSession session, Request request, Response response) {
		int skillId = -1;
		int unitType = -1;
		long targetId = 0L;
		int x = -1;
		int y = -1;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.TYPE)) {
				unitType = ((Number) aso.get(ResponseKey.TYPE)).intValue();
			}
			if(aso.containsKey(ResponseKey.SKILL_ID)) {
				skillId = ((Number) aso.get(ResponseKey.SKILL_ID)).intValue();
			}
			if(aso.containsKey(ResponseKey.X)) {
				x = ((Number) aso.get(ResponseKey.X)).intValue();
			}
			if(aso.containsKey(ResponseKey.Y)) {
				y = ((Number) aso.get(ResponseKey.Y)).intValue();
			}
			if(aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<FightEvent> fightResult = fightFacade.playerFight(playerId, targetId, unitType, skillId, x, y);
		int result = fightResult.getResult();
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("角色:[{}] 发起使用技能:[{}] 返回值: [{}] ", new Object[] { playerId, skillId, result });
		}
		response.setValue(result);
		session.write(response);
		FightPushHelper.pushReport2Client(fightResult.getValue());
	}
}
