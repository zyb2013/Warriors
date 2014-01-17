package com.yayo.warriors.common.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.props.model.EndureInfo;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.props.PropsCmd;
import com.yayo.warriors.type.ElementType;

/**
 * 装备推送帮助类
 * 
 * @author Hyint
 */
@Component
public class EquipPushHelper {
	private static final ObjectReference<EquipPushHelper> ref = new ObjectReference<EquipPushHelper>();
	@Autowired
	private UserManager userManager;
	@Autowired
	private SessionManager sessionManager;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EquipPushHelper.class);
	
	@PostConstruct
	protected void init() {
		ref.set(this);
	}
	
	private static EquipPushHelper getInstance() {
		return ref.get();
	}
	
	/**
	 * 推送换装属性变化
	 * 
	 * @param userDomain		角色(发生换装的角色)
	 * @param playerIds			接受信息的角色ID列表
	 * @param attributes		需要推送的属性值
	 */
	public static void pushDressAttributeChanges(UserDomain userDomain, Object...attributes) {
		if(userDomain == null) {
			return;
		}
		
		GameMap gameMap = userDomain.getGameMap();
		if(gameMap == null) {
			return;
		}
		
		Set<Long> playerIds = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
		if(playerIds == null || playerIds.isEmpty()) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("推送角色上下装, 接收者为空, 不推送");
			}
			return;
		}
		
		long playerId = userDomain.getPlayerId();
		Object[] values = getInstance().userManager.getPlayerAttributes(playerId, attributes);
		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		resultMap.put(ResponseKey.VALUES, values);
		resultMap.put(ResponseKey.PARAMS, attributes);
		resultMap.put(ResponseKey.UNITID, UnitId.valueOf(playerId, ElementType.PLAYER));
		getInstance().sessionManager.write(playerIds, Response.defaultResponse(Module.PROPS, PropsCmd.PUSH_DRESS_ATTRCHANGE, resultMap));
		if(LOGGER.isDebugEnabled()) { 
			LOGGER.debug("角色:[{}] 换装后. 推送属性:[{}] , 属性值:[{}]", new Object[] { playerId, attributes, values });
		}
	}

	
	/**
	 * 推送换装属性变化
	 * 
	 * @param playerId			角色ID(发生换装的角色)
	 * @param endureInfo		装备的耐久属性
	 */
	public static void pushEquipEndureDamageInfo(long playerId, Collection<EndureInfo> endureInfo) {
		if(endureInfo != null && !endureInfo.isEmpty()) {
			Response response = Response.defaultResponse(Module.PROPS, PropsCmd.PUSH_EQUIP_DAMAGE_INFO, endureInfo.toArray());
			getInstance().sessionManager.write(playerId, response);
		}
	}
}
