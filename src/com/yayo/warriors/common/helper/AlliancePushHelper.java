package com.yayo.warriors.common.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.push.Pusher;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.alliance.AllianceCmd;

@Component
public class AlliancePushHelper {
	
	@Autowired
	private Pusher pusher;
	
	/**
	 * 通知玩家被提出帮派
	 * @param playerId     通知被提出的玩家
	 */
	public void pushDismissMember(long playerId) {
		Response response = Response.defaultResponse(Module.ALLIANCE, AllianceCmd.PUSH_DISMISS_MEMBER);
		response.setValue(new Object());
		pusher.pushMessage(playerId, response);
	}
	
	/**
	 * 通知玩家帮派被解散
	 * @param playerIds     需要通知的玩家集合
	 */
	public void pushDisbandAlliace(Collection<Long> playerIds) {
		Response response = Response.defaultResponse(Module.ALLIANCE, AllianceCmd.PUSH_DISBAND_ALLIANCE);
		response.setValue(new Object());
		pusher.pushMessage(playerIds, response);
	}
	
	/**
	 * 通知帮派中的成员,‘谁’加入了帮派
	 * @param playerIds    需要通知的玩家集合
	 * @param playerId     新加入帮派者
	 * @param name         刚加入者的名字
	 * @param masterName   帮主的名字     
	 * @param masterId     帮主的ID
	 * @param number       当前帮派中的人数
	 */
	public void pushJoinAlliance(Collection<Long> playerIds, long playerId, String name,String masterName,long masterId,int number) {
		Response response = Response.defaultResponse(Module.ALLIANCE, AllianceCmd.PUSH_JOIN_ALLIANCE);
		Map<String,Object> map = new HashMap<String,Object>(5);
		map.put(ResponseKey.NAME, name);
		map.put(ResponseKey.PLAYER_ID, playerId);
		map.put(ResponseKey.MASTER_NAME, masterName);
		map.put(ResponseKey.MASTER_ID, masterId);
		map.put(ResponseKey.NUMBER, number);
		response.setValue(map);
		pusher.pushMessage(playerIds, response);
	}
	
	/**
	 * 通知玩家加入帮派成功
	 * @param playerId     玩家的ID
	 * @param allianceId   帮派的ID
	 */
	public void pushJoinSuccess(long playerId,long allianceId) {
		Response response = Response.defaultResponse(Module.ALLIANCE, AllianceCmd.PUSH_JOIN_SUCCESS);
		response.setValue(allianceId);
		pusher.pushMessage(playerId, response);
	}
	
	/**
	 * 通知帮主,‘我’不接受帮主转移
	 * @param playerId    玩家的ID(帮主的ID)
	 * @param name        拒绝人的名字
	 */
	public void pushDevolveReject(long playerId,String name) {
		Response response = Response.defaultResponse(Module.ALLIANCE, AllianceCmd.PUSH_DEVOLVE_REJECT);
		response.setValue(name);
		pusher.pushMessage(playerId, response);
	}
	
	/**
	 * 通知帮派中所有玩家 新帮主上任
	 * @param playerIds      全体帮员
	 * @param name           新帮主的名字
	 * @param playerId       新帮主的ID
	 */
	public void pushDevolveAccept(Collection<Long> playerIds,String name , long playerId) {
		Response response = Response.defaultResponse(Module.ALLIANCE, AllianceCmd.PUSH_DEVOLVE_ACCEPT);
		Map<String,Object> map = new HashMap<String,Object>(2);
		map.put(ResponseKey.NAME, name);
		map.put(ResponseKey.PLAYER_ID, playerId);
		response.setValue(map);
		pusher.pushMessage(playerIds, response);
	}
	
	/**
	 * 通知玩家被邀请加入帮派
	 * @param playerId        被邀请者的ID
	 * @param inviterId       邀请者的ID
	 * @param allianceId      帮派的ID
	 * @param level           帮派等级
	 * @param allianceName    帮派的名字
	 * @param name            邀请人的名字
	 */
	public void pushInviteMember(long playerId,long inviterId,long allianceId,int level,String allianceName,String name){
		Response response = Response.defaultResponse(Module.ALLIANCE, AllianceCmd.PUSH_INVITE_MEMBER);
		Map<String,Object> map = new HashMap<String,Object>(5);
		map.put(ResponseKey.ALLIANCE_ID, allianceId);
		map.put(ResponseKey.INVITE, inviterId);
		map.put(ResponseKey.LEVEL, level);
		map.put(ResponseKey.ALLIANCE_NAME, allianceName);
		map.put(ResponseKey.NAME, name);
		response.setValue(map);
		pusher.pushMessage(playerId, response);
	}
	
	/**
	 * 玩家拒绝邀请加入帮派
	 * @param playerId       玩家(邀请者)的ID
	 * @param name           玩家(拒绝者)的名字
	 */
	public void pushInviteReject(long playerId,String name){
		Response response = Response.defaultResponse(Module.ALLIANCE, AllianceCmd.PUSH_INVITE_REJECT);
		response.setValue(name);
		pusher.pushMessage(playerId, response);
	} 
	
	/**
	 * 告知玩家,转让帮主给'你'
	 * @param playerId       玩家(转让授予人)的ID
	 * @param name           玩家(原帮主名字)的名字
	 */
	public void pushDevolveNotice(long playerId,String name){
		Response response = Response.defaultResponse(Module.ALLIANCE, AllianceCmd.PUSH_DEVOLVE_NOTICE);
		response.setValue(name);
		pusher.pushMessage(playerId, response);
	}
	
	/**
	 * 帮派研究技能(通知所有玩家)
	 * @param playerIds   通知的所有玩家
	 * @param skills      当前技能集合
	 * @param silver      帮派当前的铜币
	 */
	public void pushResearchSkill(Collection<Long> playerIds,String skills,long silver){
		Response response = Response.defaultResponse(Module.ALLIANCE, AllianceCmd.PUSH_RESEARCH_SKILL);
		Map<String, Object> resultObject = new HashMap<String, Object>(2);
		resultObject.put(ResponseKey.SKILLS, skills);
		resultObject.put(ResponseKey.SILVER, silver);
		response.setValue(resultObject);
		pusher.pushMessage(playerIds, response);
	}
	
	/**
	 * 帮派升级
	 * @param playerIds       通知的所有玩家
	 * @param silver          帮派当前的铜币
	 * @param tokenPropsCount 帮派当前的令牌数
	 * @param type            升级建筑的类型
	 * @param level           建筑当前的等级
	 */
	public void pushBuildLevel(Collection<Long> playerIds,long silver,int tokenPropsCount,int type,int level){
		Response response = Response.defaultResponse(Module.ALLIANCE, AllianceCmd.PUSH_LEVELUP_BUILD);
		Map<String, Object> resultObject = new HashMap<String, Object>(4);
		resultObject.put(ResponseKey.TOKEN_PROPS_COUNT, tokenPropsCount);
		resultObject.put(ResponseKey.SILVER, silver);
		resultObject.put(ResponseKey.TYPE, type);
		resultObject.put(ResponseKey.LEVEL, level);
		response.setValue(resultObject);
		pusher.pushMessage(playerIds, response);
		
	}

}
