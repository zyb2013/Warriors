package com.yayo.warriors.module.chat.parser;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.rule.AttributeRule;
import com.yayo.warriors.module.user.rule.PlayerRule;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Flushable;

/**
 * 增加角色等级命令解析器
 * 
 * @author Hyint
 */
@Component
public class LevelParser extends AbstractGMCommandParser {
	
	@Autowired
	private MapFacade mapFacade;
	
	
	protected String getCommand() {
		return GmType.LEVEL;
	}

	
	public boolean execute(UserDomain userDomain, String[] elements) {
		PlayerBattle playerBattle = userDomain.getBattle();
		if(playerBattle == null) {
			return false;
		}
		
		int level = Math.abs(Integer.valueOf(elements[2].trim()));
		if (level > PlayerRule.getMaxPlayerLevel()) {
			level = PlayerRule.getMaxPlayerLevel();
		} else if (level <= 0) {
			level = PlayerRule.INIT_DEFAULT_LEVEL;
		} 
		
		playerBattle.setExp(0);
		long playerId = userDomain.getPlayerId();
		playerBattle.setAttribute(AttributeKeys.LEVEL, level);
		playerBattle.setFlushable(Flushable.FLUSHABLE_NORMAL);
		Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
		Object[] attrs = ArrayUtils.add(AttributeRule.AREA_MEMBER_VIEWS_PARAMS, AttributeKeys.LEVEL);
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, Arrays.asList(userDomain.getUnitId()), attrs);
		dbService.updateEntityIntime(playerBattle);
		return true;
	}

}
