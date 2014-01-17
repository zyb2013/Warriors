package com.yayo.warriors.module.chat.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.module.alliance.entity.Alliance;
import com.yayo.warriors.module.alliance.entity.PlayerAlliance;
import com.yayo.warriors.module.alliance.manager.AllianceManager;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 增加玩家帮派贡献值 
 * @author liuyuhua
 */
@Component
public class PlayerDonateParser extends AbstractGMCommandParser {

	@Autowired
	private AllianceManager allianceManager;
	
	
	public boolean execute(UserDomain userDomain, String[] elements) {
		int donate = Integer.valueOf(elements[2].trim());
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null || !playerAlliance.isExistAlliance()){
			return false;
		}
		Alliance alliance = allianceManager.getAlliance(playerAlliance.getAllianceId());
		if(alliance == null || alliance.isDrop()){
			return false;
		}
		
	    ChainLock lock = LockUtils.getLock(playerAlliance);
	    try {
	    	lock.lock();
	    	playerAlliance.increaseDonate(donate);
	    	playerAlliance.increaseHisDonate(donate);
		}finally{
			lock.unlock();
		}
	    
	    
		return true;
	}

	
	protected String getCommand() {
		return GmType.PLAYER_DONATE;
	}

}
