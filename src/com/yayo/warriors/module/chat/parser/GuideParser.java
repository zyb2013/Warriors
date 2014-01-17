package com.yayo.warriors.module.chat.parser;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 修改引导步骤
 * 
 * @author Hyint
 */
@Component
public class GuideParser extends AbstractGMCommandParser {

	
	public boolean execute(UserDomain userDomain, String[] elements) {
		int targetGuids = Integer.valueOf(elements[2].trim());
		Player player = userDomain.getPlayer();
	    ChainLock lock = LockUtils.getLock(player);
	    try {
	    	lock.lock();
	    	Set<Integer> guids = new HashSet<Integer>();
	    	for (int index = 0; index < targetGuids; index++) {
	    		guids.add(index);
			}
	    	player.getGuides().clear();
	    	player.getGuides().addAll(guids);
	    	player.saveGuides();
		} finally {
			lock.unlock();
		}
	    
	    dbService.submitUpdate2Queue(player);
		return true;
	}

	
	protected String getCommand() {
		return GmType.GUIDE;
	}

}
