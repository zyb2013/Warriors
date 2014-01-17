package com.yayo.warriors.socket.handler.alliance;

import java.util.Collection;
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
import com.yayo.warriors.module.alliance.constant.AllianceConstant;
import com.yayo.warriors.module.alliance.entity.Alliance;
import com.yayo.warriors.module.alliance.entity.PlayerAlliance;
import com.yayo.warriors.module.alliance.facade.AllianceBuildFacade;
import com.yayo.warriors.module.alliance.facade.AllianceFacade;
import com.yayo.warriors.module.alliance.model.DonateRecord;
import com.yayo.warriors.module.alliance.vo.AllianceVo;
import com.yayo.warriors.module.alliance.vo.ApplyVo;
import com.yayo.warriors.module.alliance.vo.MemberVo;
import com.yayo.warriors.module.search.facade.SearchFacade;
import com.yayo.warriors.module.search.vo.CommonSearchVo;
import com.yayo.warriors.module.task.facade.AllianceTaskFacade;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;

import flex.messaging.io.amf.ASObject;


@Component
public class AllianceHandler extends BaseHandler {

	@Autowired
	private AllianceFacade allianceFacade;
	@Autowired
	private SearchFacade searchFacade;
	@Autowired
	private AllianceBuildFacade allianceBuildFacade;
	@Autowired
	private AllianceTaskFacade allianceTaskFacade;
	
	
	
	protected int getModule() {
		return Module.ALLIANCE;
	}

	
	protected void inititialize() {
		
		putInvoker(AllianceCmd.LOAD_PLAYER_ALLIANCE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadPlayerAlliance(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.LOAD_ALLIANCE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadAlliance(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.CREATE_ALLIANCE_USE_PROPS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				createAllianceUseProps(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.SUBLIST_ALLIANCE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				sublistAlliance(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.SUBLIST_MEMBERS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				sublistMembers(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.RELEASE_NOTICE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				releaseNotice(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.JOIN_ALLIANCE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				joinAlliance(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.DISMISS_MEMBER , new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				dismissMember(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.GQUIT_ALLIANCE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				gquitAlliance(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.DISBAND_ALLIANCE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				disbandAlliance(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.VILIDA_ALLIANCE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				vilidaAlliance(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.EXAMINE_APPLY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				examineApply(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.DEVOLVE_MASTER, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				devolveMaster(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.CONFIRM_DEVOLVE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				confirmDevolve(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.SUBLIST_APPLY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				sublistApplys(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.APPOINT_TITLE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				appointTitle(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.INVITE_MEMBER, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				inviteMember(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.CONFIRM_INVITE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				confirmInvite(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.SEARCH_PLAYER, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				searchPlayer(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.LEVELUP_BUILD, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				levelBuild(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.DONATE_SILVER, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				donateSilver(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.DONATE_PROPS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				donateProps(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.VIEW_RECORD, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				viewRecord(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.SHOPPING_ALLIANCE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				shoppingAlliance(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.DIVINE_ALLIANCE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				divineAlliance(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.RESEARCH_SKILL, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				researchSkill(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.STUDY_SKILL, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				studySkill(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.SUBLIST_TODAY_DONATE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				sublistDonateRecord(session, request, response);
			}
		});
		
		putInvoker(AllianceCmd.CREATE_ALLIANCE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				createAlliance(session, request, response);
			}
		});
	}
	
	
	
	
	protected void studySkill(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		int researchId = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.RESEARCH_ID)) {
				researchId = ((Number)aso.get(ResponseKey.RESEARCH_ID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<PlayerAlliance> result = allianceBuildFacade.studySkill(playerId, researchId);
		HashMap<String, Object> resultObject = new HashMap<String, Object>(2);
		resultObject.put(ResponseKey.RESULT, result.getResult());
		if(result.getResult() == AllianceConstant.SUCCESS){
			resultObject.put(ResponseKey.PLAYER_ALLIANCE, result.getValue());
		}
		
		response.setValue(resultObject);
		session.write(response);
	}
	
	
	
	protected void researchSkill(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		int researchId = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.RESEARCH_ID)) {
				researchId = ((Number)aso.get(ResponseKey.RESEARCH_ID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Alliance> result = allianceBuildFacade.researchSkill(playerId, researchId);
		HashMap<String, Object> resultObject = new HashMap<String, Object>(2);
		resultObject.put(ResponseKey.RESULT, result.getResult());
		response.setValue(resultObject);
		session.write(response);
	}
	
	
	protected void divineAlliance(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		
		ResultObject<Map<String,Object>> result = allianceBuildFacade.divineAlliance(playerId);
		if(result.getResult() == AllianceConstant.SUCCESS){
			response.setValue(result.getValue());
			session.write(response);
		}else{
			HashMap<String, Object> resultObject = new HashMap<String, Object>(1);
			resultObject.put(ResponseKey.RESULT, result.getResult());
			response.setValue(resultObject);
			session.write(response);
		}
	}
	
	
	
	protected void shoppingAlliance(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		int shopId = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.SHOPID)) {
				shopId = ((Number)aso.get(ResponseKey.SHOPID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<PlayerAlliance> resultObject = allianceBuildFacade.shoppingAlliance(playerId, shopId);
		HashMap<String,Object> result = new HashMap<String,Object>(2);
		result.put(ResponseKey.RESULT, resultObject.getResult());
		if(resultObject.getResult() == AllianceConstant.SUCCESS){
			PlayerAlliance playerAlliance = resultObject.getValue();
			if(playerAlliance != null){
				result.put(ResponseKey.DONATE, playerAlliance.getDonate());
			}
		}
		
		response.setValue(result);
		session.write(response);
	}
	
	
	protected void viewRecord(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		int pageStart = 0;
		int pageSize  = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PAGE_START)) {
				pageStart = ((Number)aso.get(ResponseKey.PAGE_START)).intValue();
			}
			if(aso.containsKey(ResponseKey.PAGE_SIZE)) {
				pageSize = ((Number)aso.get(ResponseKey.PAGE_SIZE)).intValue();
			}
			
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int startIndex = pageStart - 1;
		startIndex = startIndex < 0 ? 0 : startIndex; 
		Map<String,Object> result = allianceBuildFacade.sublistRecord(playerId, startIndex, pageSize);
		result.put(ResponseKey.PAGE_START, pageStart);
		response.setValue(result);
		session.write(response);
	}
	
	
	protected void levelBuild(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		int type = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.TYPE)) {
				type = ((Number)aso.get(ResponseKey.TYPE)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		int result = allianceBuildFacade.levelupBuild(playerId, type);
		HashMap<String, Object> resultObject = new HashMap<String, Object>(1);
		resultObject.put(ResponseKey.RESULT, result);
		response.setValue(resultObject);
		session.write(response);
	}
	
	
	protected void donateProps(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		String props = "";
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PROPS_ID)) {
				props = (String)aso.get(ResponseKey.PROPS_ID);
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Map<String,Object>> result = allianceBuildFacade.donateProps(playerId, props);
		if(result.getResult() == AllianceConstant.SUCCESS){
			response.setValue(result.getValue());
			session.write(response);
		}else{
			HashMap<String, Object> resultObject = new HashMap<String, Object>(1);
			resultObject.put(ResponseKey.RESULT, result.getResult());
			response.setValue(resultObject);
			session.write(response);
		}
	}
	
	
	protected void donateSilver(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		int silver = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.SILVER)) {
				silver = ((Number)aso.get(ResponseKey.SILVER)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Map<String,Object>> result = allianceBuildFacade.donateSilver(playerId, silver);
		if(result.getResult() == AllianceConstant.SUCCESS){
			response.setValue(result.getValue());
			session.write(response);
		}else{
			HashMap<String, Object> resultObject = new HashMap<String, Object>(1);
			resultObject.put(ResponseKey.RESULT, result.getResult());
			response.setValue(resultObject);
			session.write(response);
		}
	}
	
	
	protected void searchPlayer(IoSession session, Request request, Response response) {
		String name = "";
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.NAME)) {
				name = (String)aso.get(ResponseKey.NAME);
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		Collection<CommonSearchVo> result = searchFacade.searchPlayerName(name);
		response.setValue(result.toArray());
		session.write(response);
	}
	
	
	protected void appointTitle(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		long targetId = 0;
		int title = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number)aso.get(ResponseKey.TARGET_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.TITLE)) {
				title = ((Number)aso.get(ResponseKey.TITLE)).intValue();
			}
			
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Map<String,Object>> result = allianceFacade.appointTitle(playerId, targetId, title);
		if(result.getResult() == AllianceConstant.SUCCESS){
			response.setValue(result.getValue());
			session.write(response);
		}else{
			Map<String, Object> map = new HashMap<String, Object>(1);
			map.put(ResponseKey.RESULT, result.getResult());
			response.setValue(map);
			session.write(response);
		}
		
	}
	
	
	protected void confirmInvite(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		long allianceId = 0;
		long inviterId  = 0;
		boolean confirm = false;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.CONFIRM)) {
				confirm = (Boolean)aso.get(ResponseKey.CONFIRM);
			}
			if(aso.containsKey(ResponseKey.ALLIANCE_ID)) {
				allianceId = ((Number)aso.get(ResponseKey.ALLIANCE_ID)).longValue();
			}
			
			if(aso.containsKey(ResponseKey.INVITE)) {
				inviterId = ((Number)aso.get(ResponseKey.INVITE)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
				
		int result = allianceFacade.confirmInvite(playerId, inviterId, allianceId, confirm);
		
		response.setValue(result);
		session.write(response);
	}

	
	protected void inviteMember(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		long targetId = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number)aso.get(ResponseKey.TARGET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		int result = allianceFacade.inviteMember(playerId, targetId);
		
		response.setValue(result);
		session.write(response);
		
	}
	
	
	protected void confirmDevolve(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		boolean confirm = false;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.CONFIRM)) {
				confirm = (Boolean)aso.get(ResponseKey.CONFIRM);
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
				
		int result = allianceFacade.confirmDevolve(playerId, confirm);
		response.setValue(result);
		session.write(response);
	}
	
	
	protected void devolveMaster(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		long targetId = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number)aso.get(ResponseKey.TARGET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		int result = allianceFacade.devolveMaster(playerId, targetId);
		response.setValue(result);
		session.write(response);
	}
	
	
	protected void examineApply(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		long applyId = 0;
		boolean agree = false;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.APPLY_ID)) {
				applyId = ((Number)aso.get(ResponseKey.APPLY_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.AGREE)) {
				agree = (Boolean)aso.get(ResponseKey.AGREE);
			}
			
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
				
		int result = allianceFacade.examineApply(playerId, applyId, agree);
		response.setValue(result);
		session.write(response);
	}
	
	
	protected void vilidaAlliance(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		int state = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.STATE)) {
				state = ((Number)aso.get(ResponseKey.STATE)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = allianceFacade.vilidaAlliance(playerId, state);
		response.setValue(result);
		session.write(response);
	}
	
	
	protected void disbandAlliance(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		int result = allianceFacade.disbandAlliance(playerId);
		response.setValue(result);
		session.write(response);
	}
	
	
	protected void gquitAlliance(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		int result = allianceFacade.gquitAlliance(playerId);
		response.setValue(result);
		session.write(response);
	}
	
	
	protected void dismissMember(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		long targetId = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number)aso.get(ResponseKey.TARGET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = allianceFacade.dismissMember(playerId, targetId);
		response.setValue(result);
		session.write(response);
	}
	
	
	protected void joinAlliance(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		long allianceId = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.ALLIANCE_ID)) {
				allianceId = ((Number)aso.get(ResponseKey.ALLIANCE_ID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = allianceFacade.joinAlliance(allianceId, playerId);
		response.setValue(result);
		session.write(response);
	}
	
	
	protected void releaseNotice(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		String content = "";
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.CONTENT)) {
				content = (String) aso.get(ResponseKey.CONTENT);
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = allianceFacade.releaseNotice(playerId, content);
		response.setValue(result);
		session.write(response);
	}
	
	
	protected void sublistDonateRecord(IoSession session, Request request, Response response){
		long playerId = this.sessionManager.getPlayerId(session);
		int pageStart = 0;
		int pageSize  = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PAGE_START)) {
				pageStart = ((Number)aso.get(ResponseKey.PAGE_START)).intValue();
			}
			if(aso.containsKey(ResponseKey.PAGE_SIZE)) {
				pageSize = ((Number)aso.get(ResponseKey.PAGE_SIZE)).intValue();
			}
			
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int startIndex = pageStart - 1;
		startIndex = startIndex < 0 ? 0 : startIndex; 
		
		List<DonateRecord> donateRecords = allianceBuildFacade.sublistDonateRecords(playerId, startIndex, pageSize);
		if(donateRecords != null){
			int number = allianceBuildFacade.sizeDonateRecord4Alliance(playerId);
			Map<String,Object> result = new HashMap<String, Object>(3);
			result.put(ResponseKey.DATA, donateRecords.toArray());
			result.put(ResponseKey.PAGE_START, pageStart);
			result.put(ResponseKey.NUMBER, number);
			response.setValue(result);
			session.write(response);
		}
		
	}
	
	
	protected void sublistMembers(IoSession session, Request request, Response response){
		long playerId = this.sessionManager.getPlayerId(session);
		int pageStart = 0;
		int pageSize  = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PAGE_START)) {
				pageStart = ((Number)aso.get(ResponseKey.PAGE_START)).intValue();
			}
			if(aso.containsKey(ResponseKey.PAGE_SIZE)) {
				pageSize = ((Number)aso.get(ResponseKey.PAGE_SIZE)).intValue();
			}
			
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int startIndex = pageStart - 1;
		startIndex = startIndex < 0 ? 0 : startIndex; 
		
		List<MemberVo> memberVos = allianceFacade.sublistMembers(playerId, startIndex, pageSize);
		if(memberVos != null){
			PlayerAlliance playerAlliance = allianceFacade.getPlayerAlliance(playerId);
			int number = 0;
			if(playerAlliance != null){
				number = allianceFacade.sizeMembers4Alliance(playerAlliance.getAllianceId());
			}
			Map<String,Object> result = new HashMap<String, Object>(3);
			result.put(ResponseKey.DATA, memberVos.toArray());
			result.put(ResponseKey.PAGE_START, pageStart);
			result.put(ResponseKey.NUMBER, number);
			response.setValue(result);
			session.write(response);
			
		}else{
			response.setValue(AllianceConstant.FAILURE);
			session.write(response);
		}
		
		
		
	}
	
	
	protected void sublistApplys(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		int pageStart = 0;
		int pageSize  = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PAGE_START)) {
				pageStart = ((Number)aso.get(ResponseKey.PAGE_START)).intValue();
			}
			if(aso.containsKey(ResponseKey.PAGE_SIZE)) {
				pageSize = ((Number)aso.get(ResponseKey.PAGE_SIZE)).intValue();
			}
			
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int startIndex = pageStart - 1;
		startIndex = startIndex < 0 ? 0 : startIndex; 
		
		List<ApplyVo> applyVos = allianceFacade.sublistApplys(playerId, startIndex, pageSize);
		if(applyVos != null){
			PlayerAlliance playerAlliance = allianceFacade.getPlayerAlliance(playerId);
			int number = allianceFacade.sizeApply4Alliance(playerAlliance.getAllianceId());
			Map<String,Object> result = new HashMap<String, Object>(3);
			result.put(ResponseKey.DATA, applyVos.toArray());
			result.put(ResponseKey.PAGE_START, pageStart);
			result.put(ResponseKey.NUMBER, number);
			response.setValue(result);
			session.write(response);
		}else{
			response.setValue(AllianceConstant.FAILURE);
			session.write(response);
		}
		
	}
	
	
	protected void sublistAlliance(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		int pageStart = 0;
		int pageSize  = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PAGE_START)) {
				pageStart = ((Number)aso.get(ResponseKey.PAGE_START)).intValue();
			}
			if(aso.containsKey(ResponseKey.PAGE_SIZE)) {
				pageSize = ((Number)aso.get(ResponseKey.PAGE_SIZE)).intValue();
			}
			
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int startIndex = pageStart - 1;
		startIndex = startIndex < 0 ? 0 : startIndex; 
		
		List<AllianceVo> allianceVos = allianceFacade.sublistAlliances(playerId, startIndex, pageSize);
		if(allianceVos != null){
			int number = allianceFacade.sizeAlliances();
			Map<String,Object> result = new HashMap<String, Object>(3);
			result.put(ResponseKey.DATA, allianceVos.toArray());
			result.put(ResponseKey.PAGE_START, pageStart);
			result.put(ResponseKey.NUMBER, number);
			response.setValue(result);
			session.write(response);
		}else{
			response.setValue(AllianceConstant.FAILURE);
			session.write(response);
		}

	}
	
	
	
	protected void createAlliance(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		String name = "";
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.NAME)) {
				name = (String) aso.get(ResponseKey.NAME);
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Alliance> result = allianceFacade.createAlliance(playerId, name);
		HashMap<String, Object> resultObject = new HashMap<String, Object>(3);
		resultObject.put(ResponseKey.RESULT, result.getResult());
		if(result.getResult() == AllianceConstant.SUCCESS){
			resultObject.put(ResponseKey.ALLIANCE, result.getValue());
			int number = allianceFacade.sizeMembers4Alliance(result.getValue().getId());
			resultObject.put(ResponseKey.NUMBER, number);
			PlayerAlliance playerAlliance = allianceFacade.getPlayerAlliance(playerId);
			if(playerAlliance != null){
				resultObject.put(ResponseKey.PLAYER_ALLIANCE, playerAlliance);
			}
			
			response.setValue(resultObject);
			session.write(response);
		}else{
			response.setValue(resultObject);
			session.write(response);
		}
		
	}
	
	
	protected void createAllianceUseProps(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		String name = "";
		long propsId = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.NAME)) {
				name = (String) aso.get(ResponseKey.NAME);
			}
			if(aso.containsKey(ResponseKey.PROPS_ID)) {
				propsId = ((Number)aso.get(ResponseKey.PROPS_ID)).longValue();
			}
			
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Alliance> result = allianceFacade.createAllianceUseProps(playerId, propsId, name);
		HashMap<String, Object> resultObject = new HashMap<String, Object>(3);
		resultObject.put(ResponseKey.RESULT, result.getResult());
		if(result.getResult() == AllianceConstant.SUCCESS){
			resultObject.put(ResponseKey.ALLIANCE, result.getValue());
			int number = allianceFacade.sizeMembers4Alliance(result.getValue().getId());
			resultObject.put(ResponseKey.NUMBER, number);
			PlayerAlliance playerAlliance = allianceFacade.getPlayerAlliance(playerId);
			if(playerAlliance != null){
				resultObject.put(ResponseKey.PLAYER_ALLIANCE, playerAlliance);
			}
			
			response.setValue(resultObject);
			session.write(response);
		}else{
			response.setValue(resultObject);
			session.write(response);
		}
		
	}
	
	
	protected void loadAlliance(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		Alliance alliance = allianceFacade.getAlliance(playerId);
		PlayerAlliance playerAlliance = allianceFacade.getPlayerAlliance(playerId);
		
		if(alliance != null && playerAlliance != null){
			HashMap<String,Object> result = new HashMap<String, Object>(5);
			int number = allianceFacade.sizeMembers4Alliance(alliance.getId());
			String progress = allianceTaskFacade.getUserAllianceTaskProgress(playerId);
			result.put(ResponseKey.RESULT, AllianceConstant.SUCCESS);
			result.put(ResponseKey.ALLIANCE, alliance);
			result.put(ResponseKey.PLAYER_ALLIANCE, playerAlliance);
			result.put(ResponseKey.NUMBER, number);
			result.put(ResponseKey.PROGRESS, progress);
			response.setValue(result);
			session.write(response);
		}else{
			HashMap<String,Object> result = new HashMap<String, Object>(1);
			result.put(ResponseKey.RESULT, AllianceConstant.ALLIANCE_NOT_FOUND);
			response.setValue(result);
			session.write(response);
		}
	}
	
	
	protected void loadPlayerAlliance(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		PlayerAlliance playerAlliance = allianceFacade.getPlayerAlliance(playerId);
		if(playerAlliance != null){
			response.setValue(playerAlliance);
		}else{
			response.setValue(AllianceConstant.FAILURE);
		}
		session.write(response);
	}

}
