package com.yayo.warriors.common.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.push.Pusher;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.user.rule.AttributeRule;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.pet.PetCmd;

/**
 * 家将推送
 * @author liuyuhua
 */
@Component
public class PetPushHelper {
	@Autowired
	private Pusher pusher;
	@Autowired
	private PetManager petManager;
	
	/**
	 * 家将出战
	 * @param viewPlayers     可视区域内所有玩家(包括自己)
	 * @param petId           家将的ID
	 * @param x               家将的X坐标
	 * @param y               家将的Y坐标
	 */
	public void petGoFighting(long playerId,long petId,int x,int y,Collection<Long> viewPlayers){
		if(viewPlayers != null){
			Response response = Response.defaultResponse(Module.PET, PetCmd.PUSH_PET_GO_FIGHTING);
			Object[] pet_params = AttributeRule.PET_PARAMS;
			Object[] pet_values = petManager.getPetAttributes(playerId, petId, pet_params);
			Map<String,Object> result = new HashMap<String,Object>(6);
			result.put(ResponseKey.PLAYER_ID, playerId);
			result.put(ResponseKey.PET_ID, petId);
			result.put(ResponseKey.X, x);
			result.put(ResponseKey.Y, y);
			result.put(ResponseKey.PET_PARAMS, pet_params);
			result.put(ResponseKey.PET_VALUES, pet_values);
			response.setValue(result);
			this.pusher.pushMessage(viewPlayers, response);
		}
	}
	
	
	/**
	 * 召回(休息)家将
	 * @param playerId       玩家的ID
	 * @param viewPlayers    可视区域内玩家
	 */
	public void petBack(long playerId, Collection<Long> viewPlayers){
		if(viewPlayers != null){
			Response response = Response.defaultResponse(Module.PET, PetCmd.PUSH_PET_BACK);
			response.setValue(playerId);
			this.pusher.pushMessage(viewPlayers, response);
		}
	}
	
	/**
	 * 家将真传(契合)
	 * @param playerId       玩家的ID
	 * @param petId          家将的ID
	 * @param baseId         家将基础ID
	 * @param quality        家将的品质
	 * @param viewPlayers    可视区域内玩家
	 */
	public void petMerged(long playerId, long petId, int baseId, int quality, Collection<Long> viewPlayers){
		if(viewPlayers != null){
			Map<String,Object> resultObject = new HashMap<String, Object>(4);
			resultObject.put(ResponseKey.PLAYER_ID, playerId);
			resultObject.put(ResponseKey.PET_ID,  petId);
			resultObject.put(ResponseKey.BASEID,  baseId);
			resultObject.put(ResponseKey.QUALITY, quality);
			Response response = Response.defaultResponse(Module.PET, PetCmd.PUSH_PET_MERGED, resultObject);
			this.pusher.pushMessage(viewPlayers, response);
		}
	}
	
	
	
	/**
	 * 推送家将属性
	 * @param playerIds   命令接受者
	 * @param owner       家将拥有者
	 * @param petId       家将的ID
	 * @param param       参数
	 */
	public void pushPetAttribute(Collection<Long> playerIds, long owner, long petId, Object[] params) {
		if (playerIds != null) {
			Response response = Response.defaultResponse(Module.PET, PetCmd.PUSH_PET_ATTRIBUTE);
			Object[] values = petManager.getPetAttributes(owner, petId, params);
			Map<String, Object> result = new HashMap<String, Object>(4);
			result.put(ResponseKey.PLAYER_ID, owner);
			result.put(ResponseKey.PET_ID, petId);
			result.put(ResponseKey.PARAMS, params);
			result.put(ResponseKey.VALUES, values);
			response.setValue(result);
			this.pusher.pushMessage(playerIds, response);
		}
	}
	
	/**
	 * 推送家将升级(只针对出战家将)
	 * <per>
	 * 该命令只是用来告知其他玩家家将升级，客户端需要升级特效
	 * </per>
	 * @param playerIds      命令接受者
	 * @param owner          家将拥有者
	 * @param petId          家将的ID
	 */
	public void pushPetLevelUp(Collection<Long> playerIds,long owner,long petId){
		if(playerIds != null && !playerIds.isEmpty()){
			Response response = Response.defaultResponse(Module.PET, PetCmd.PUSH_PET_LEVEL_UP);
			Map<String, Object> resultObject = new HashMap<String, Object>(2);
			resultObject.put(ResponseKey.PLAYER_ID, owner);
			resultObject.put(ResponseKey.PET_ID, petId);
			response.setValue(resultObject);
			this.pusher.pushMessage(playerIds, response);
		}
	}

}
