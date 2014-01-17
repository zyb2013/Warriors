package com.yayo.warriors.socket.handler.skill;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.socket.handler.BaseHandler;
import com.yayo.common.socket.handler.Invoker;
import com.yayo.common.socket.message.Request;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.type.ResponseCode;
import com.yayo.warriors.basedb.model.SkillConfig;
import com.yayo.warriors.basedb.model.SkillLearnConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.props.constant.PropsConstant;
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.module.skill.facade.SkillFacade;
import com.yayo.warriors.module.skill.model.SkillVO;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.type.GoodsType;

import flex.messaging.io.amf.ASObject;

@Component
public class SkillHandler extends BaseHandler{
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private SkillFacade skillFacade;
	@Autowired
	private ResourceService resourceService;
	
	
	protected int getModule() {
		return Module.SKILL;
	}
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	
	protected void inititialize() {
		putInvoker(SkillCmd.QUERY_SKILL, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				querySkill(session, request, response);
			}
		});
		
		putInvoker(SkillCmd.SKILL_LEVELUP, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				skillLevelUp(session, request, response);
			}
		});
		
	}
	public void querySkill(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<SkillVO> skillVOList = skillFacade.listUserSkillVO(playerId);
		resultMap.put(ResponseKey.SKILL_VO, skillVOList.toArray());
		resultMap.put(ResponseKey.COOL_TIME, voFactory.getCdTime(playerId, skillVOList));
		response.setValue(resultMap);
		session.write(response);
	}
	
	public void skillLevelUp(IoSession session, Request request, Response response) {
		int skillId = 0;
		long userItemId = 0L;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.SKILL_ID)) {
				skillId = ((Number)aso.get(ResponseKey.SKILL_ID)).intValue(); 
			}
			if(aso.containsKey(ResponseKey.USER_PROPSID)) {
				userItemId = ((Number)aso.get(ResponseKey.USER_PROPSID)).longValue(); 
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		ResultObject<SkillVO> resultObject = skillFacade.learnUserSkill(playerId, userItemId, skillId);
		int result = resultObject.getResult();
		SkillVO skillVO = resultObject.getValue();
		resultMap.put(ResponseKey.RESULT, result);
		if(skillVO != null) {
			resultMap.put(ResponseKey.SKILL_VO, skillVO);
		}
		if(result == PropsConstant.SUCCESS){
			SkillConfig skillConfig = resourceService.get(skillId, SkillConfig.class);
			SkillLearnConfig skillLearn = skillConfig.getLearnSkill(skillVO.getLevel());
			if(skillLearn.isNeedItem()){
				MessagePushHelper.pushGoodsCountChange2Client(playerId, GoodsVO.valueOf(skillLearn.getItemId(), GoodsType.PROPS, -1));
			}
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("角色: {} 用户道具ID:[{}] 技能ID:[{}] ", new Object[] { playerId, userItemId, skillId });
			logger.debug("{}", resultMap);
		}
		response.setValue(resultMap);
		session.write(response);
	}
}
