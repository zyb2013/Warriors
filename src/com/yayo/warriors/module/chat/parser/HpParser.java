package com.yayo.warriors.module.chat.parser;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;

/**
 * 增加角色HP命令解析器
 * 
 * @author Hyint
 */
@Component
public class HpParser extends AbstractGMCommandParser {
	
	@Autowired
	private MapFacade mapFacade;
	
	
	protected String getCommand() {
		return GmType.HP;
	}

	
	public boolean execute(UserDomain userDomain, String[] elements) {
		int hpAttribute = AttributeKeys.HP;
		long playerId = userDomain.getPlayerId();
		Integer addHp = Integer.valueOf(elements[2].trim());
		boolean isDead = userDomain.getBattle().isDead(); 
		userDomain.getBattle().increaseAttribute(hpAttribute, addHp);
		Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, Arrays.asList(userDomain.getUnitId()), hpAttribute);
		dbService.submitUpdate2Queue(userDomain.getBattle());
		if(isDead) {
			UserPushHelper.pushPlayerRecurrent(userDomain.getUnitId(), userDomain.getBattle().getHp(), playerIdList);
		}
		return true;
	}

}
