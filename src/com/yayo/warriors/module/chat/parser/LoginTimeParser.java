package com.yayo.warriors.module.chat.parser;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.TimeConstant;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.model.UserDomain;


@Component
public class LoginTimeParser extends AbstractGMCommandParser {

	
	
	public boolean execute(UserDomain userDomain, String[] elements) {
		Player player = userDomain.getPlayer();
		int onlineMin = Integer.valueOf(elements[2].trim());      // 在线时长
		
		long loginTime = player.getLoginTime().getTime();
		long onlineMillis = onlineMin * TimeConstant.ONE_MINUTE_MILLISECOND;
		
		long changeTime = loginTime - onlineMillis;               // 改变登录时间
		
		ChainLock lock = LockUtils.getLock(player);
		try {
			lock.lock();
			player.setLoginTime(new Date(changeTime));
			dbService.submitUpdate2Queue(player);
		} finally {
			lock.unlock();
		}
		
		return true;
	}

	
	
	protected String getCommand() {
		return GmType.LOGIN_TIME;
	}

}
