package com.yayo.warriors.module.chat.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.module.achieve.entity.UserAchieve;
import com.yayo.warriors.module.achieve.manager.AchieveManager;
import com.yayo.warriors.module.achieve.model.AchieveType;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.user.model.UserDomain;

@Component
public class KillMonsterParser extends AbstractGMCommandParser {

	@Autowired
	private DbService dbService;
	@Autowired
	private AchieveManager achieveManager;
	
	
	public boolean execute(UserDomain userDomain, String[] elements) {
		long playerId = userDomain.getPlayerId();
		int count = Integer.valueOf(elements[2].trim());    // 需要修改的杀怪数量
		
		int achieveType = AchieveType.MONSTER_ACHIEVE.ordinal();
		
		UserAchieve userAchieve = achieveManager.getUserAchieve(playerId);
		if (userAchieve == null) {
			return false;
		}
		
		ChainLock lock = LockUtils.getLock(userAchieve);
		try {
			lock.lock();
			userAchieve.put2AchieveMap(achieveType, count);
			userAchieve.updateAchieved();
			dbService.submitUpdate2Queue(userAchieve);
		} finally {
			lock.unlock();
		}
		
		return true;
	}

	
	
	protected String getCommand() {
		return GmType.KILL_MONSTER;
	}

}
