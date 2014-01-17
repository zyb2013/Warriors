package com.yayo.warriors.module.chat.parser;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.rank.manager.RankManager;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 刷新排行榜命令解析器
 * 
 * @author jonsai
 */
@Component
public class RankParser extends AbstractGMCommandParser {
	
	@Autowired
	private RankManager rankManager;
	@Autowired
	private SessionManager sessionManager;
	
	
	protected String getCommand() {
		return GmType.RANK;
	}

	
	public boolean execute(UserDomain userDomain, String[] elements) {
		if(userDomain == null) {
			return false;
		}
		rankManager.refreshAllRank();
		return true;
	}

}
