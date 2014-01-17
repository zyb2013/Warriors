package com.yayo.warriors.module.chat.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.friends.FriendRule;
import com.yayo.warriors.module.friends.entity.FriendsTreasure;
import com.yayo.warriors.module.friends.facade.FriendsFacade;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 好友酒坛GM
 * 
 * @author huachaoping
 */
@Component
public class FriendsParser extends AbstractGMCommandParser {

	@Autowired
	private DbService dbService;
	@Autowired
	private FriendsFacade friendsFacade;
	
	
	public boolean execute(UserDomain userDomain, String[] elements) {
		int wine = Integer.valueOf(elements[2].trim());
		if (wine > FriendRule.WINE_LIMIT) {
			wine = FriendRule.WINE_LIMIT;
		}
		
		long playerId = userDomain.getId();
		FriendsTreasure treasure = friendsFacade.getFriendsBless(playerId);
		
		ChainLock lock = LockUtils.getLock(treasure);
		try {
			lock.lock();
			treasure.setWineMeasure(wine);
		} finally {
			lock.unlock();
		}
		dbService.submitUpdate2Queue(treasure);
		return true;
	}

	
	
	protected String getCommand() {
		return GmType.WINE_JAR;
	}

}
