package com.yayo.warriors.module.chat.parser;

import org.springframework.stereotype.Component;

import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.model.UserDomain;

@Component
public class LoginDaysParser extends AbstractGMCommandParser {

	
	
	public boolean execute(UserDomain userDomain, String[] elements) {
		Player player = userDomain.getPlayer();
		int addDays = Integer.valueOf(elements[2].trim());
		
		ChainLock lock = LockUtils.getLock(player);
		try {
			lock.lock();
			player.setLoginDays(addDays);
			dbService.submitUpdate2Queue(player);
		} finally {
			lock.unlock();
		}
		
		return true;
	}

	
	
	protected String getCommand() {
		return GmType.LOGIN_DAYS;
	}

}
