package com.yayo.warriors.module.chat.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 增加经验命令解析器
 * 
 * @author Hyint
 */
@Component
public class ExpParser extends AbstractGMCommandParser {
	
	@Autowired
	private UserManager userManager;
	
	
	protected String getCommand() {
		return GmType.EXP;
	}

	
	public boolean execute(UserDomain userDomain, String[] elements) {
		long playerId = userDomain.getPlayerId();
		Integer addExp = Integer.valueOf(elements[2].trim());
		return userManager.addPlayerExp(playerId, addExp, true);
	}

}
