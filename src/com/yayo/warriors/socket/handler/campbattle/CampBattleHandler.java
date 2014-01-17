package com.yayo.warriors.socket.handler.campbattle;

import java.util.Date;
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
import com.yayo.common.utility.EnumUtils;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.campbattle.constant.CampBattleConstant;
import com.yayo.warriors.module.campbattle.facade.CampBattleFacade;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.vo.ChangeScreenVo;

import flex.messaging.io.amf.ASObject;


@Component
public class CampBattleHandler extends BaseHandler {
	@Autowired
	private CampBattleFacade campBattleFacade;
	

	
	protected int getModule() {
		return Module.CAMP_BATTLE;
	}

	
	protected void inititialize() {
		putInvoker(CampBattleCmd.APPLY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				apply(session, request, response);
			}
		});
		
		putInvoker(CampBattleCmd.GET_APPLY_PLAYERS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				getApplyPlayers(session, request, response);
			}
		});
		
		putInvoker(CampBattleCmd.ADJUST_APPLY_PLAYER_PRIORITY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
//				--屏蔽调整报名位置功能 2012-7-28
				adjustApplyPlayerPriority(session, request, response);
			}
		});
		
		putInvoker(CampBattleCmd.ENTER_CAMPBATTLE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				enterCampBattle(session, response);
			}
		});
		
		putInvoker(CampBattleCmd.GET_PLAYER_SCORES, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				getPlayerScores(session, request, response);
			}
		});
		
		putInvoker(CampBattleCmd.EXIST_CAMPBATTLE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				existCampBattle(session, response);
			}
		});
		
		putInvoker(CampBattleCmd.REWARD_SALARY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				rewardSalary(session, response);
			}
		});
		
		putInvoker(CampBattleCmd.GET_CAMP_TITLE_PLAYERS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				getCampTitlePlayers(session, request, response);
			}
		});
		
		putInvoker(CampBattleCmd.GET_CAMP_LEADER, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				getCampLeader(session, request, response);
			}
		});
		
		putInvoker(CampBattleCmd.GET_CAMP_BATTLE_HISTORY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				getCampBattleHistory(session, request, response);
			}
		});
		
		putInvoker(CampBattleCmd.REWARDS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				rewards(session, request, response);
			}
		});
		
		putInvoker(CampBattleCmd.GET_APPLY_STATUS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				getApplyStatus(session, response);
			}
		});
		
		putInvoker(CampBattleCmd.GET_CAMP_BATTLE_STATUS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				getCampBattleStatus(session, response);
			}
		});
		
		putInvoker(CampBattleCmd.GET_CAMP_BATTLE_DATES, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				List<Date> campBattleDates = campBattleFacade.getCampBattleDates();
				response.setValue( campBattleDates != null && campBattleDates.size() > 0 ? campBattleDates.toArray() : new Date[0]);
				session.write(response);
			}
		});
		
		putInvoker(CampBattleCmd.CAMP_BATTLE_REQUEST_CMD, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				campBattleRequestCmd(session, request, response);
			}
		});
		
		putInvoker(CampBattleCmd.REWARD_SUIT, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				rewardSuit(session, response);
			}
		});
		
		putInvoker(CampBattleCmd.GET_PLAYER_SCORE_INFO, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				Long playerId = sessionManager.getPlayerId(session);
				Map<String, Object> resultMap = new HashMap<String, Object>(4);
				int result = CampBattleConstant.FAILURE;
				if(playerId != null && playerId > 0){
					result = campBattleFacade.getPlayerScoreInfo(playerId, resultMap);
				}
				
				resultMap.put(ResponseKey.RESULT, result);
				response.setValue(resultMap);
				
				session.write(response);
			}
		});
		
		putInvoker(CampBattleCmd.GET_SCORE_HISTORY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				Map<String, Object> resultMap = new HashMap<String, Object>(4);
				ASObject aso = (ASObject)request.getValue();
				int result = CampBattleConstant.FAILURE;
				if(aso != null){
					Long playerId = sessionManager.getPlayerId(session);
					int pageNow = aso.containsKey(ResponseKey.PAGE_NOW) ? ((Number)aso.get(ResponseKey.PAGE_NOW)).intValue() : 0;
					int pageSize = aso.containsKey(ResponseKey.PAGE_SIZE) ? ((Number)aso.get(ResponseKey.PAGE_SIZE)).intValue() : 0;
					result = campBattleFacade.getScoreRank(playerId, pageNow, pageSize, resultMap);
				}
				
				resultMap.put(ResponseKey.RESULT, result);
				response.setValue(resultMap);
				
				session.write(response);
			}
		});
		
	}
	
	


	private void apply(IoSession session, Request request, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		long result = CampBattleConstant.FAILURE;
		if(playerId != null){
			result = campBattleFacade.apply(playerId);
		}
		response.setValue(result);
		
		session.write(response);
	}


	private void getApplyPlayers(IoSession session, Request request, Response response) {
		Map<String, Object> resultMap = new HashMap<String, Object>(4);
		ASObject aso = (ASObject)request.getValue();
		int result = CampBattleConstant.FAILURE;
		if(aso != null){
			Long playerId = sessionManager.getPlayerId(session);
			int pageSize = aso.containsKey(ResponseKey.PAGE_SIZE) ? ((Number)aso.get(ResponseKey.PAGE_SIZE)).intValue() : 10;
			int pageNow = aso.containsKey(ResponseKey.PAGE_NOW) ? ((Number)aso.get(ResponseKey.PAGE_NOW)).intValue() : 1;
			result = campBattleFacade.getApplyPlayers(playerId, pageSize, pageNow, resultMap);
		}
		
		resultMap.put(ResponseKey.RESULT, result);
		response.setValue(resultMap);
		
		session.write(response);
	}
	
	
	private void adjustApplyPlayerPriority(IoSession session, Request request, Response response) {
		Map<String, Object> resultMap = new HashMap<String, Object>(4);
		ASObject aso = (ASObject)request.getValue();
		int result = CampBattleConstant.FAILURE;
		if(aso != null){
			Long playerId = sessionManager.getPlayerId(session);
			long targetId = aso.containsKey(ResponseKey.PLAYER_ID) ? ((Number)aso.get(ResponseKey.PLAYER_ID)).longValue() : -1;
			int pageSize = aso.containsKey(ResponseKey.PAGE_SIZE) ? ((Number)aso.get(ResponseKey.PAGE_SIZE)).intValue() : 10;
			int pageNow = aso.containsKey(ResponseKey.PAGE_NOW) ? ((Number)aso.get(ResponseKey.PAGE_NOW)).intValue() : 1;
			byte op = aso.containsKey("op") ? ((Number)aso.get("op")).byteValue() : 0;
			result = campBattleFacade.adjustApplyPlayerPriority(playerId, targetId, op, pageSize, pageNow, resultMap);
		}
		
		resultMap.put(ResponseKey.RESULT, result);
		response.setValue(resultMap);
		
		session.write(response);
	}


	private void enterCampBattle(IoSession session, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		ResultObject<ChangeScreenVo> resultObj = campBattleFacade.enterCampBattle(playerId);
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		if(resultObj != null){
			resultMap.put(ResponseKey.RESULT, resultObj.getResult() );
			resultMap.put("changeScreenVO", resultObj.getValue() );
		}
		response.setValue(resultMap);
		
		session.write(response);
	}

	
	private void getPlayerScores(IoSession session, Request request, Response response) {
		Map<String, Object> resultMap = new HashMap<String, Object>(4);
		ASObject aso = (ASObject)request.getValue();
		int result = CampBattleConstant.FAILURE;
		if(aso != null){
			Long playerId = sessionManager.getPlayerId(session);
			int pageSize = aso.containsKey(ResponseKey.PAGE_SIZE) ? ((Number)aso.get(ResponseKey.PAGE_SIZE)).intValue() : 10;
			int pageNow = aso.containsKey(ResponseKey.PAGE_NOW) ? ((Number)aso.get(ResponseKey.PAGE_NOW)).intValue() : 1;
			
			result = campBattleFacade.getPlayerScores(playerId, pageSize, pageNow, resultMap);
		}
		
		resultMap.put(ResponseKey.RESULT, result);
		response.setValue(resultMap);
		
		session.write(response);
	}


	private void existCampBattle(IoSession session, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		ResultObject<ChangeScreenVo> resultObj = campBattleFacade.existCampBattle(playerId);
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		if(resultObj != null){
			resultMap.put(ResponseKey.RESULT, resultObj.getResult() );
			resultMap.put("changeScreenVO", resultObj.getValue() );
		}
		response.setValue(resultMap);
		
		session.write(response);
	}

	
	private void rewardSalary(IoSession session, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		int result = CampBattleConstant.FAILURE;
		if(playerId != null){
			result = campBattleFacade.salary(playerId);
		}
		
		response.setValue(result);
		
		session.write(response);
	}

	
	private void getCampTitlePlayers(IoSession session, Request request, Response response) {
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		ASObject aso = (ASObject)request.getValue();
		int result = CampBattleConstant.FAILURE;
		if(aso != null){
			Long playerId = sessionManager.getPlayerId(session);
			int camp = aso.containsKey(ResponseKey.CAMP) ? ((Number)aso.get(ResponseKey.CAMP)).intValue() : 0;
			result = campBattleFacade.getCampTitlePlayers(camp, resultMap);
			if(playerId != null){
				int rewardStat = campBattleFacade.getRewardStat(playerId, 2, null);
				resultMap.put("rewardStat", rewardStat);
			}
		}
		
		resultMap.put(ResponseKey.RESULT, result);
		response.setValue(resultMap);
		
		session.write(response);
	}


	private void getCampLeader(IoSession session, Request request, Response response) {
		Map<String, Object> resultMap = new HashMap<String, Object>(4);
		ASObject aso = (ASObject)request.getValue();
		int result = CampBattleConstant.FAILURE;
		if(aso != null){
			int pageSize = aso.containsKey(ResponseKey.PAGE_SIZE) ? ((Number)aso.get(ResponseKey.PAGE_SIZE)).intValue() : 10;
			int pageNow = aso.containsKey(ResponseKey.PAGE_NOW) ? ((Number)aso.get(ResponseKey.PAGE_NOW)).intValue() : 1;
			int camp = aso.containsKey(ResponseKey.CAMP) ? ((Number)aso.get(ResponseKey.CAMP)).intValue() : 0;
			result = campBattleFacade.getCampLeader(camp, pageSize, pageNow, resultMap);
		}
		
		resultMap.put(ResponseKey.RESULT, result);
		response.setValue(resultMap);
		
		session.write(response);
	}

	
	private void getCampBattleHistory(IoSession session, Request request, Response response) {
		Map<String, Object> resultMap = new HashMap<String, Object>(6);
		ASObject aso = (ASObject)request.getValue();
		int result = CampBattleConstant.FAILURE;
		if(aso != null){
			Long playerId = sessionManager.getPlayerId(session);
			Date date = (Date)aso.get("date");
			int campValue = aso.containsKey(ResponseKey.CAMP) ? ((Number)aso.get(ResponseKey.CAMP)).intValue() : 0;
			int pageSize = aso.containsKey(ResponseKey.PAGE_SIZE) ? ((Number)aso.get(ResponseKey.PAGE_SIZE)).intValue() : 10;
			int pageNow = aso.containsKey(ResponseKey.PAGE_NOW) ? ((Number)aso.get(ResponseKey.PAGE_NOW)).intValue() : 1;
			Camp camp = EnumUtils.getEnum(Camp.class, campValue);
			result = campBattleFacade.getCampBattleHistory(playerId, date, camp, pageSize, pageNow, resultMap);
			if(playerId != null){
				int rewardStat = campBattleFacade.getRewardStat(playerId, 1, date);
				resultMap.put("rewardStat", rewardStat);
			}
		}
		
		resultMap.put(ResponseKey.RESULT, result);
		response.setValue(resultMap);
		
		session.write(response);
	}

	private void rewards(IoSession session, Request request, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		ASObject aso = (ASObject)request.getValue();
		int result = CampBattleConstant.FAILURE;
		if(playerId != null){
			Date date = (Date)aso.get("date");
			result = campBattleFacade.rewards(playerId, date);
		}
		
		response.setValue(result);
		
		session.write(response);
	}


	private void getApplyStatus(IoSession session, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		int result = campBattleFacade.getApplyStatus(playerId);
		
		response.setValue(result);
		
		session.write(response);
	}

	private void getCampBattleStatus(IoSession session, Response response) {
		int campBattleStatus = campBattleFacade.getCampBattleStatus();
		response.setValue(campBattleStatus);
		session.write(response);
	}

	private void campBattleRequestCmd(IoSession session, Request request, Response response) {
		Map<String, Object> resultMap = new HashMap<String, Object>(4);
		ASObject aso = (ASObject)request.getValue();
		int result = CampBattleConstant.FAILURE;
		if(aso != null){
			Long playerId = sessionManager.getPlayerId(session);
			int type = aso.containsKey(ResponseKey.TYPE) ? ((Number)aso.get(ResponseKey.TYPE)).intValue() : 0;
			result = campBattleFacade.campBattleRequestCmd(playerId, type, resultMap);
			resultMap.put(ResponseKey.TYPE, type);
		}
		resultMap.put(ResponseKey.RESULT, result);
		response.setValue(resultMap);
		
		session.write(response);
	}
	private void rewardSuit(IoSession session, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		int result = CampBattleConstant.FAILURE;
		if(playerId != null){
			result = campBattleFacade.suitReward(playerId);
		}
		
		response.setValue(result);
		session.write(response);
	}
	

}
