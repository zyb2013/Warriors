package com.yayo.warriors.module.chat.parser;

import static com.yayo.warriors.module.pack.type.BackpackType.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.rule.EquipHelper;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 增加装备解析器
 * 
 * @author Hyint
 */
@Component
public class EquipParser extends AbstractGMCommandParser {
	
	@Autowired
	private PropsManager propsManager;
	
	
	protected String getCommand() {
		return GmType.EQUIP;
	}

	
	public boolean execute(UserDomain userDomain, String[] elements) {
		int backpack = DEFAULT_BACKPACK;
		Player player = userDomain.getPlayer();
		long playerId = userDomain.getPlayerId();
		Integer count = Integer.valueOf(elements[3].trim());
		Integer equipId = Integer.valueOf(elements[2].trim());
		List<UserEquip> userEquips = EquipHelper.newUserEquips(playerId, backpack, equipId, false, count);
		if(userEquips == null || userEquips.isEmpty()) {
			return false;
		}
		
		int currSize = propsManager.getBackpackSize(playerId, backpack);
		ChainLock lock = LockUtils.getLock(userDomain.getPackLock());
		try {
			lock.lock();
			if(!player.canAddNew2Backpack(currSize + count, backpack)) {
				return false;
			}
			
			userEquips = propsManager.createUserEquip(userEquips);
			propsManager.put2UserEquipIdsList(playerId, backpack, userEquips);
		} catch (Exception e) {
			LOGGER.error("{}", e);
			return false;
		} finally {
			lock.unlock();
		}
		
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, userEquips);
		return true;
	}

}
