package com.yayo.warriors.common.helper;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.push.Pusher;
import com.yayo.warriors.module.animal.facade.AnimalFacade;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.npc.facade.NpcFacade;
import com.yayo.warriors.module.npc.model.Npc;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.handler.map.MapCmd;
import com.yayo.warriors.socket.vo.ChangeScreenVo;
import com.yayo.warriors.type.ElementType;


/**
 * 地图信息主动发送类
 * @author liuyuhua
 */
@Component
public class MapPushHelper {
	@Autowired
	private Pusher pusher;
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private NpcFacade npcFacade;
	@Autowired
	private AnimalFacade animalFacade;
	
	private final Logger LOGGER = LoggerFactory.getLogger(MapPushHelper.class);
	
//	/**
//	 * 角色,离开屏幕可视范围 (通知其他角色离开视线)
//	 * @param playerId     角色ID
//	 * @param playerIdList 需要通知的其他角色ID
//	 */
//	public void playerLeaveViews(Long playerId, Collection<Long> playerIdList){
//		if(playerIdList != null && !playerIdList.isEmpty()){
//			Response response = Response.defaultResponse(Module.MAP, MapCmd.PUT_MAP_ANIMAL_REMOVE);
//			response.setValue(UnitId.valueOf(playerId, ElementType.PLAYER));
//			pusher.pushMessage(playerIdList, response);
//			if(LOGGER.isDebugEnabled()){
//				LOGGER.debug("RRRRRRRRRRRRRR玩家[{}]离开玩家[{}]的可视区域",playerId,playerIdList);
//			}
//		}
//	}
	
//	/**
//	 * 离开屏幕可视范围 (告诉'我',其他角色离开视线)
//	 * @param playerId     角色ID
//	 * @param playerIdList 需要通知的其他角色ID
//	 */
//	public void leavePlayerViews(Long playerId, Collection<Long> playerIdList){
//		if(playerIdList != null && !playerIdList.isEmpty()){
//			for(Long id : playerIdList){
//				Response response = Response.defaultResponse(Module.MAP, MapCmd.PUT_MAP_ANIMAL_REMOVE);
//				response.setValue(UnitId.valueOf(id, ElementType.PLAYER));
//				pusher.pushMessage(playerId, response);
//			}
//			if(LOGGER.isDebugEnabled()){
//				LOGGER.debug("RRRRRRRRRRRRRR玩家[{}],通知玩家[{}]的可视区域",playerIdList, playerId);
//			}
//			
//		}
//	}
	
//	/**
//	 * 获取可视区域角色
//	 * @param palyerId     角色ID(接受信息者)
//	 * @param playerVos    其他角色信息集合
//	 */
//	public void gainPlayerViews(Long playerId,Collection<Long> players) {
//		if(playerId != null && players != null && !players.isEmpty()){
//			for(Long id : players){
//				Map<String, Object> info = animalFacade.getAnimal(id, ElementType.PLAYER);
//				if(info != null){
//					Response response = Response.defaultResponse(Module.MAP, MapCmd.PUT_MAP_ANIMAL_ADD, info);
//					pusher.pushMessage(playerId, response);
//					if(LOGGER.isDebugEnabled()){
//						LOGGER.debug("ADDDDDDDDDDD玩家[{}],获取玩家 [{}]的可视区域",playerId, id);
//					}
//				}
//			}
//		}
//	}
	
	
//	/**
//	 * 怪物,离开屏幕可视范围 (通知其他角色离开视线)
//	 * @param monsterId    怪物ID
//	 * @param playerIds    需要通知的其他角色ID
//	 */
//	public void monsterLeaveViews(Long monsterId, Collection<Long> playerIds){
//		if(playerIds != null && !playerIds.isEmpty()){
//			Response response = Response.defaultResponse(Module.MAP, MapCmd.PUT_MAP_ANIMAL_REMOVE);
//			response.setValue(UnitId.valueOf(monsterId, ElementType.MONSTER));
//			pusher.pushMessage(playerIds, response);
//			if(LOGGER.isDebugEnabled()){
//				LOGGER.debug("怪物[{}],离开玩家[{}]的可视区域",monsterId,playerIds);
//			}
//		}
//	}
	
//	/**
//	 * 离开怪物可视区域,删除怪物
//	 * @param playerId     角色ID
//	 * @param monsterIds   怪物的ID集合
//	 */
//	public void leaveMonsterViews(Long playerId, Collection<Long> monsterIds){
//		if(monsterIds != null && !monsterIds.isEmpty()){
//			for(Long monsterId : monsterIds){
//				Response response = Response.defaultResponse(Module.MAP, MapCmd.PUT_MAP_ANIMAL_REMOVE);
//				response.setValue(UnitId.valueOf(monsterId, ElementType.MONSTER));
//				pusher.pushMessage(playerId, response);
//			}
//			if(LOGGER.isDebugEnabled()){
//				LOGGER.debug("RRRRRRRR玩家[{}]离开怪物[{}],",playerId,monsterIds);
//			}
//		}
//	}
	
	/**
//	 * 离开怪物可视区域,删除怪物
//	 * @param playerId     角色ID
//	 * @param monsterId   怪物的ID
//	 */
//	public void leaveMonsterViews(Long playerId, Long monsterId) {
//		Response response = Response.defaultResponse(Module.MAP,MapCmd.PUT_MAP_ANIMAL_REMOVE);
//		response.setValue(UnitId.valueOf(monsterId, ElementType.MONSTER));
//		pusher.pushMessage(playerId, response);
//		if (LOGGER.isDebugEnabled()) {
//			LOGGER.debug("RRRRRRRR玩家[{}]离开怪物[{}],", playerId, monsterId);
//		}
//	}
	
	
//	/**
//	 * 获取可视区域怪物
//	 * @param playerId    角色ID(接受信息者)
//	 * @param monsters    怪物信息
//	 */
//	public void gainMonsterViews(Long playerId, Collection<Long> monsterIds){
//		if(playerId != null && monsterIds != null && !monsterIds.isEmpty()){
//			for(Long id : monsterIds){
//				Map<String,Object> info = this.animalFacade.getAnimal(id, ElementType.MONSTER);
//				if(info != null){
//					Response response = Response.defaultResponse(Module.MAP, MapCmd.PUT_MAP_ANIMAL_ADD, info);
//					pusher.pushMessage(playerId, response);
//				}
//			}
//			if(LOGGER.isDebugEnabled()){
//				LOGGER.debug("AAAAAAAAAAA玩家[{}]新增怪物[{}],",playerId,monsterIds);
//			}
//		}
//	}
	
//	/**
//	 * 获取可视区域怪物
//	 * @param playerId    角色ID(接受信息者)
//	 * @param monster    怪物信息
//	 */
//	public void gainMonsterViews(Long playerId, Long monsterId) {
//		Map<String, Object> info = this.animalFacade.getAnimal(monsterId,ElementType.MONSTER);
//		if (info != null) {
//			Response response = Response.defaultResponse(Module.MAP,
//					MapCmd.PUT_MAP_ANIMAL_ADD, info);
//			pusher.pushMessage(playerId, response);
//		}
//		if (LOGGER.isDebugEnabled()) {
//			LOGGER.debug("AAAAAAAAAAA玩家[{}]新增怪物[{}],", playerId, monsterId);
//		}
//	}
	
	
//	/**
//	 * 获取可视区域的NPC
//	 * @param playerId    角色ID(接受信息者)
//	 * @param npcs        NPC的ID
//	 */
//	public void gainNpcViews(Long playerId,Collection<Long> npcs){
//		if(playerId != null && npcs != null && !npcs.isEmpty()){
//			for(Long npcId : npcs){
//				Map<String,Object> info = this.animalFacade.getAnimal(npcId, ElementType.NPC);
//				if(info != null){
//					Response response = Response.defaultResponse(Module.MAP, MapCmd.PUT_MAP_ANIMAL_ADD, info);
//					pusher.pushMessage(playerId, response);
//				}
//			}
//			
//			if(LOGGER.isDebugEnabled()){
//				LOGGER.debug("AAAAAAAAAAA玩家[{}]新增NPC[{}],",playerId,npcs);
//			}
//			
//			
//		}
//	}
	
//	/**
//	 * 离开NPC可视区域(删除NPC)
//	 * @param playerId   角色ID(接受信息者) 
//	 * @param npcs       NPC的ID
//	 */
//	public void leaveNpcViews(Long playerId,Collection<Long> npcs){
//		if(playerId != null && npcs != null && !npcs.isEmpty()){
//			for(Long npcId : npcs){
//				Npc npc = npcFacade.getNpc(npcId.intValue());
//				if(npc == null){
//					continue;
//				}
//				
//				/**
//				 * NP这个地方做特性变化
//				 */
//				if(npc.getElementType() == ElementType.NPC){
//					this.leave4Npc(playerId, npc);
//				}else{
//					this.leave4OrtherNpc(playerId, npc);
//				}
//				
//			}
//			if(LOGGER.isDebugEnabled()){
//				LOGGER.debug("RRRRRRR玩家[{}]删除NPC[{}],",playerId,npcs);
//			}
//		}
//	}
	
	
//	/**
//	 * 离开NPC可视区域
//	 * (注:NPC比较特殊,因为涉及到客户端需要用来寻路定位,所以发送的都是BaseId)
//	 * @param playerId    玩家的ID
//	 * @param npc         NPC实体
//	 */
//	private void leave4Npc(long playerId, Npc npc){
//		Response response = Response.defaultResponse(Module.MAP, MapCmd.PUT_MAP_ANIMAL_REMOVE);
//		response.setValue(UnitId.valueOf(npc.getBaseId().longValue(), npc.getElementType()));
//		pusher.pushMessage(playerId, response);
//	}
	
//	/**
//	 * 离开转场点,采集点,等NPC的可视区域
//	 * @param playerId    玩家的ID
//	 * @param npc         NPC实体
//	 */
//	private void leave4OrtherNpc(long playerId, Npc npc){
//		Response response = Response.defaultResponse(Module.MAP, MapCmd.PUT_MAP_ANIMAL_REMOVE);
//		response.setValue(UnitId.valueOf(npc.getId(), npc.getElementType()));
//		pusher.pushMessage(playerId, response);
//	}
	
//	/**
//	 * 角色,告知其他角色,'我'进入'你们'的可视区域
//	 * @param playerId     角色ID
//	 * @param playerVos    其他角色信息集合
//	 */
//	public void intoPlayerViews(Long playerId,Collection<Long> players){
//		if(playerId != null && players != null && !players.isEmpty()){
//			Map<String,Object> info = this.animalFacade.getAnimal(playerId, ElementType.PLAYER);
//			if(info != null) {
//				Response response = Response.defaultResponse(Module.MAP, MapCmd.PUT_MAP_ANIMAL_ADD, info);
//				pusher.pushMessage(players, response);
//				if(LOGGER.isDebugEnabled()){
//					LOGGER.debug(" ADDDDDDDD 玩家[{}]进入了[{}]可视区域", playerId, players);
//				}
//				
//			}
//		}
//	}
	
	
//	/**
//	 * 怪物,告知其他角色,'我'进入'你们'的可视区域
//	 * @param playerId     角色ID
//	 * @param playerVos    其他角色信息集合
//	 */
//	public void intoMonsterViews(Long monsterId, Collection<Long> players){
//		if(players != null && players != null && !players.isEmpty()){
//			Map<String,Object> info = this.animalFacade.getAnimal(monsterId, ElementType.MONSTER);
//			if(info != null){
//				pusher.pushMessage(players, Response.defaultResponse(Module.MAP, MapCmd.PUT_MAP_ANIMAL_ADD, info));
//			}
//		}
//	}
	
	
	
	/**
	 * 强制把角色 拉回某个坐标点
	 * @param playerId  角色ID
	 * @param xy  {@link int[2]{x,y}} XY坐标      
	 */
	public void coerceRetrun(Long playerId,int[] xy){
		Response response = Response.defaultResponse(Module.MAP, MapCmd.PUT_MAP_COERCE_RETRUN, xy);
		pusher.pushMessage(playerId, response);
	}
	
//	/**
//	 * 角色移动路径
//	 * @param playerId    角色ID
//	 * @param direction   移动路径集合
//	 * @param playerIdList  需要通知的角色
//	 */
//	public void playerMotionPath(Long playerId,Collection<Integer> direction,Collection<Long> playerIdList){
//		if(direction != null && !direction.isEmpty()){
//			Response response = Response.defaultResponse(Module.MAP, MapCmd.PUT_MAP_MOTION_PATH);
//			response.setValue(voFactory.getPlayerPathVo(playerId, direction.toArray(new Integer[0])));
//			pusher.pushMessage(playerIdList, response);
//		}
//	}
	
//	/**
//	 * 怪物移动路径
//	 * @param monsterId      怪物ID
//	 * @param direction      移动路径集合
//	 * @param collection   需要通知的角色
//	 */
//	public void monsterMotionPath(Long monsterId,Integer[] direction,Collection<ISpire> collection){
//		if(direction != null && direction.length > 0){
////			Response response = Response.defaultResponse(Module.MAP, MapCmd.PUT_MAP_MOTION_PATH);
////			response.setValue(voFactory.getMonsterPathVo(monsterId,direction));
////			pusher.pushMessage(collection, response);
//			
//		}
//	}
	
	/**
	 * 玩家使用小飞鞋
	 * @param playerId       玩家的ID
	 * @param vo             转场Vo
	 */
	public void playerGo(Long playerId,ChangeScreenVo vo){
		if(playerId != null && vo != null){
			Response response = Response.defaultResponse(Module.MAP, MapCmd.PUT_MAP_PLAYER_GO);
			response.setValue(vo);
			pusher.pushMessage(playerId, response);
		}
	}

}
