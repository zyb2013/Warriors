package com.yayo.warriors.module.chat.parser;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;

/**
 * 增加MP命令解析器
 * 
 * @author Hyint
 */
@Component
public class MpParser extends AbstractGMCommandParser {
	
	@Autowired
	private MapFacade mapFacade;
	
	
	protected String getCommand() {
		return GmType.MP;
	}

	
	public boolean execute(UserDomain userDomain, String[] elements) {
		PlayerBattle playerBattle = userDomain.getBattle();
		if(playerBattle == null) {
			return false;
		}
		
		int mpAttribute = AttributeKeys.MP;
		long playerId = userDomain.getPlayerId();
		Integer addMp = Integer.valueOf(elements[2].trim());
		playerBattle.increaseAttribute(mpAttribute, addMp);
		Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
		List<UnitId> playerUnits = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, playerUnits, mpAttribute);
		dbService.submitUpdate2Queue(playerBattle);
		return true;
	}

}
