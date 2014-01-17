package com.yayo.warriors.module.chat.parser;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.message.Response;
import com.yayo.common.utility.EnumUtils;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.meridian.entity.Meridian;
import com.yayo.warriors.module.meridian.facade.MeridianFacade;
import com.yayo.warriors.module.meridian.manager.MeridianManager;
import com.yayo.warriors.module.meridian.type.MeridianType;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.rule.AttributeRule;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.handler.meridian.MeridianCmd;
import com.yayo.warriors.type.ElementType;

/**
 * 经脉命令解析器
 * 
 * @author Hyint
 */
@Component
public class MeridianParser extends AbstractGMCommandParser {
	
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private MeridianFacade meridianFacade;
	@Autowired
	private MeridianManager meridianManager;

	
	protected String getCommand() {
		return GmType.MERIDIAN;
	}

	
	public boolean execute(UserDomain userDomain, String[] elements) {
		long playerId = userDomain.getPlayerId();
		int type = Math.abs(Integer.valueOf(elements[2].trim()));
		MeridianType meridianType = EnumUtils.getEnum(MeridianType.class, type);
		if(meridianType == null) {
			return false;
		}
		
		Meridian meridian = meridianManager.getMeridian(playerId);
		if(meridian == null) {
			return false;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		int playerJob = battle.getJob().ordinal();
		Collection<Integer> meridianPoints = meridianManager.getMerdianConfigByType(playerJob, type);
		meridian.getMeridiansSet().addAll(meridianPoints);
		meridian.updateMeridianSet();
		dbService.submitUpdate2Queue(meridian);
		
		//推送玩家经脉信息
		Response response = Response.defaultResponse(Module.MERIDIAN, MeridianCmd.LOAD_ADDED_ATTR);
		response.setValue(meridianFacade.loadMeridianAttr(playerId) );
		sessionManager.write(playerId, response);
		
		//推送玩家属性的改变给周围的玩家
		GameMap gameMap = userDomain.getGameMap();
		Object[] attributes = AttributeRule.AREA_MEMBER_VIEWS_PARAMS;
		List<UnitId> unitIds = Arrays.asList(userDomain.getUnitId());
		Collection<Long> playerIds = gameMap.getAllSpireIdCollection(ElementType.PLAYER);
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIds, unitIds, attributes);
		attributes = meridianManager.getMeridianAttrKeyByType(playerJob, type);	//推送玩家属性改变
		UserPushHelper.pushAttribute2AreaMember(playerId, Arrays.asList(playerId), unitIds, attributes);
		return true;
	}

}
