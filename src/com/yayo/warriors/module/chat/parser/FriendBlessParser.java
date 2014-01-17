package com.yayo.warriors.module.chat.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.friends.FriendRule;
import com.yayo.warriors.module.friends.entity.FriendsTreasure;
import com.yayo.warriors.module.friends.facade.FriendsFacade;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.handler.friends.FriendsCmd;

/**
 * 好友祝福
 * 
 * @author huachaoping
 */
@Component
public class FriendBlessParser extends AbstractGMCommandParser {
	
	@Autowired
	private FriendsFacade friendsFacade;
	@Autowired
	private SessionManager sessionManager;
	
	
	public boolean execute(UserDomain userDomain, String[] elements) {
		long playerId = userDomain.getPlayerId();
		int blessValue = Integer.valueOf(elements[2].trim());
		
		FriendsTreasure treasure = friendsFacade.getFriendsBless(playerId);
		if (treasure == null) {
			return false;
		}
		
		int addExp = 0;
		ChainLock lock = LockUtils.getLock(treasure);
		try {
			lock.lock();
			addExp = Math.abs(blessValue) >= FriendRule.BLESS_EXP_LIMIT 
				   ? FriendRule.BLESS_EXP_LIMIT : Math.abs(blessValue);
			treasure.setBlessExp(addExp);
			dbService.submitUpdate2Queue(treasure);
		} finally {
			lock.unlock();
		}
		
		Response response = Response.defaultResponse(Module.FRIENDS, FriendsCmd.LOAD_BLESS_EXP, addExp);
		sessionManager.write(playerId, response);
		return true;
	}

	
	
	protected String getCommand() {
		return GmType.BLESS;
	}

}
