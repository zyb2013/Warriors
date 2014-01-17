package com.yayo.warriors.module.chat.parser;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 清除背包命令解析器
 * 
 * @author Hyint
 */
@Component
public class ClearBagParser extends AbstractGMCommandParser {
	
	@Autowired
	private DbService dbService;
	@Autowired
	private PropsManager propsManager;
	
	
	protected String getCommand() {
		return GmType.CLEAR_BAG;
	}

	@SuppressWarnings("unchecked")
	
	public boolean execute(UserDomain userDomain, String[] elements) {
		long playerId = userDomain.getPlayerId();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		int dropBackpack = BackpackType.DROP_BACKPACK;
		List<UserProps> userPropsList = propsManager.listUserProps(playerId, backpack);
		List<UserEquip> userEquipList = propsManager.listUserEquip(playerId, backpack);
		ChainLock lock = LockUtils.getLock(userDomain.getPackLock());
		lock.lock();
		try {
			for (UserProps userProps : userPropsList) {
				userProps.setCount(0);
			}

			for (UserEquip userEquip : userEquipList) {
				userEquip.setCount(0);
			}

			dbService.submitUpdate2Queue(userPropsList, userEquipList);
			propsManager.put2UserEquipIdsList(playerId, dropBackpack, userEquipList);
			propsManager.removeFromEquipIdsList(playerId, backpack, userEquipList);
			propsManager.put2UserPropsIdsList(playerId, dropBackpack, userPropsList);
			propsManager.removeFromUserPropsIdsList(playerId, backpack, userPropsList);
		} finally {
			lock.unlock();
		}

		List<BackpackEntry> backpackEntrys = new ArrayList<BackpackEntry>();
		backpackEntrys.addAll(userEquipList);
		backpackEntrys.addAll(userPropsList);
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, backpackEntrys);
		return true;
	}

}
