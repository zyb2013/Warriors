package com.yayo.warriors.socket.handler.dungeon;

import static com.yayo.common.socket.type.ResponseCode.RESPONSE_CODE_ERROR;

import java.util.HashMap;
import java.util.Map;

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
import com.yayo.warriors.module.dungeon.constant.DungeonConstant;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.dungeon.facade.DungeonFacade;
import com.yayo.warriors.module.dungeon.vo.DungeonVo;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.vo.ChangeScreenVo;

import flex.messaging.io.amf.ASObject;

@Component
public class DungeonHandler extends BaseHandler {

	@Autowired
	private DungeonFacade dungeonFacade;
	
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	
	protected int getModule() {
		return Module.DUNGEON;
	}

	
	protected void inititialize() {
		putInvoker(DungeonCmd.ENTER_DUNGEON , new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				enterDungeon(session,request,response);
			}
		});
		
		putInvoker(DungeonCmd.EXIT_DUNGEON, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				exitDungeon(session,request,response);
			}
		});
		
		putInvoker(DungeonCmd.LOAD_USER_DUNGEON, new Invoker(){
			
			public void invoke(IoSession session, Request request, Response response) {
				loadPlayerDungeon(session,request,response);
			}
		});
		
		putInvoker(DungeonCmd.LOAD_USER_STORY, new Invoker(){
			
			public void invoke(IoSession session, Request request, Response response) {
				loadStoryDungeon(session,request,response);
			}
		});
		
		putInvoker(DungeonCmd.LOAD_DUNGEON, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadDungeonTime(session, request, response);
			}
		});
		
		putInvoker(DungeonCmd.REWARD_STORY_DUNGEON, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				rewardStory(session, request, response);
			}
		});
		
		putInvoker(DungeonCmd.VERIFY_STORY_DUNGEON, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				verifyStory(session, request, response);
			}
		});
	}
	
	/**
	 * 验证剧情副本
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void verifyStory(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		String data = "";
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.DATA)) {
				data = (String) aso.get(ResponseKey.DATA);
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<String> result = dungeonFacade.verifyStory(playerId, data);
		Map<String,Object> map = new HashMap<String, Object>(2);
		map.put(ResponseKey.RESULT, result.getResult());
		if(result.getResult() == DungeonConstant.SUCCESS){
			if(!result.getValue().isEmpty()){
				map.put(ResponseKey.DATA, result.getValue());
			}
		}
		
		response.setValue(map);
		session.write(response);
	}
	
	/**
	 * 奖励剧情副本
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void rewardStory(IoSession session, Request request, Response response){
		long playerId = sessionManager.getPlayerId(session);
		int dungeonBaseId = 0;
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.DUNGEON_BASE_ID)) {
				dungeonBaseId = ((Number) aso.get(ResponseKey.DUNGEON_BASE_ID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		int result = dungeonFacade.rewardStory(playerId, dungeonBaseId);
		if(result == DungeonConstant.SUCCESS){
			response.setValue(dungeonBaseId);
		}else{
			response.setValue(result);
		}
		
		session.write(response);
	}
	
	/**
	 * 加载副本时间
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void loadDungeonTime(IoSession session, Request request, Response response){
		long playerId = sessionManager.getPlayerId(session);
		ResultObject<DungeonVo> result = this.dungeonFacade.loadDungeon(playerId);
		if(result.getResult() == DungeonConstant.SUCCESS){
			response.setValue(result.getValue());
			session.write(response);
		}else{
			response.setValue(result.getResult());
			session.write(response);
		}
	}
	
	/**
	 * 进入副本
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void enterDungeon(IoSession session, Request request, Response response){
		long playerId = sessionManager.getPlayerId(session);
		
		int dungeonBaseId = 0; //副本ID
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.DUNGEON_BASE_ID)) {
				dungeonBaseId = ((Number) aso.get(ResponseKey.DUNGEON_BASE_ID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<ChangeScreenVo> result = this.dungeonFacade.enterDungeon(playerId, dungeonBaseId);
		int resultValue = result.getResult();
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("玩家[{}],进入[{}]副本,结果:[{}]",new Object[]{playerId,dungeonBaseId,resultValue});
		}
		if(resultValue != DungeonConstant.SUCCESS){
			Map<String,Object> map = new HashMap<String,Object>(1);
			map.put(ResponseKey.RESULT, resultValue);
			response.setValue(map);
			session.write(response);
		}else{
			ChangeScreenVo changeScreenVo = result.getValue();
			Map<String,Object> map = new HashMap<String,Object>(3);
			map.put(ResponseKey.RESULT, resultValue);
			map.put(ResponseKey.CHANGE_SCREEN, changeScreenVo);
			map.put(ResponseKey.DUNGEON_BASE_ID, dungeonBaseId);
			response.setValue(map);
			session.write(response);
		}
	}
	
	/**
	 * 退出副本
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void exitDungeon(IoSession session, Request request, Response response){
		long playerId = sessionManager.getPlayerId(session);
		
		ResultObject<ChangeScreenVo> result = this.dungeonFacade.exitDungeon(playerId);
		int resultValue = result.getResult();
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("玩家[{}],退出副本,结果:[{}]",playerId,resultValue);
		}
		
		if(resultValue != DungeonConstant.SUCCESS){
			Map<String,Object> map = new HashMap<String,Object>(1);
			map.put(ResponseKey.RESULT, resultValue);
			response.setValue(map);
			session.write(response);
		}else{
			ChangeScreenVo changeScreenVo = result.getValue();
			Map<String,Object> map = new HashMap<String,Object>(2);
			map.put(ResponseKey.RESULT, resultValue);
			map.put(ResponseKey.CHANGE_SCREEN, changeScreenVo);
			response.setValue(map);
			session.write(response);
		}
	}
	
	/**
	 * 加载玩家副本信息
	 * @param session   
	 * @param request
	 * @param response
	 */
	protected void loadPlayerDungeon(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		PlayerDungeon playerDungeon = this.dungeonFacade.getPlayerDungeon(playerId);
		if(playerDungeon == null){
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
	
		String dungeonInfo = playerDungeon.getData();
		Map<String,Object> map = new HashMap<String, Object>(2);
		map.put(ResponseKey.RESULT, DungeonConstant.SUCCESS);
		if(dungeonInfo != null){
			map.put(ResponseKey.DUNGEON_INFO, dungeonInfo);
		}
		
		response.setValue(map);
		session.write(response);
	}
	
	/**
	 * 加载玩家剧情副本信息
	 * @param session   
	 * @param request
	 * @param response
	 */
	protected void loadStoryDungeon(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		PlayerDungeon playerDungeon = this.dungeonFacade.getPlayerDungeon(playerId);
		if(playerDungeon == null){
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
	
		String storyInfo = playerDungeon.getStory();
		Map<String,Object> map = new HashMap<String, Object>(2);
		map.put(ResponseKey.RESULT, DungeonConstant.SUCCESS);
		if(storyInfo != null){
			map.put(ResponseKey.STORY_INFO, storyInfo);
		}
		
		response.setValue(map);
		session.write(response);
	}

}
