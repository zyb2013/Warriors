package com.yayo.warriors.module.chat.parser;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;

/**
 * 增加真气命令解析器
 * 
 * @author Hyint
 */
@Component
public class GasParser extends AbstractGMCommandParser {
	
	
	protected String getCommand() {
		return GmType.GAS;
	}

	
	public boolean execute(UserDomain userDomain, String[] elements) {
		int gasAttribute = AttributeKeys.GAS;
		long playerId = userDomain.getPlayerId();
		Integer addGas = Integer.valueOf(elements[2].trim());
		PlayerBattle playerBattle = userDomain.getBattle();
		playerBattle.increaseAttribute(gasAttribute, addGas);
		Collection<Long> playerIdList = Arrays.asList(playerId);
		List<UnitId> playerUnits = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, playerUnits, gasAttribute);
		dbService.submitUpdate2Queue(playerBattle);
		return true;
	}

}
