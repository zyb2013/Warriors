package com.yayo.warriors.module.chat.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.PropsStackResult;
import com.yayo.warriors.module.props.rule.PropsHelper;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 增加道具命令解析器
 * 
 * @author Hyint
 */
@Component
public class ItemParser extends AbstractGMCommandParser {
	@Autowired
	private PropsManager propsManager;
	
	
	protected String getCommand() {
		return GmType.ITEM;
	}

	
	public boolean execute(UserDomain userDomain, String[] elements) {
		Integer propsId = Integer.valueOf(elements[2].trim());
		Integer count = Integer.valueOf(elements[3].trim());
		PropsConfig props = propsManager.getPropsConfig(propsId);
		if(props == null || count <= 0) {
			return false;
		}
		
		Player player = userDomain.getPlayer();
		long playerId = userDomain.getPlayerId();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		PropsStackResult stackResult = PropsHelper.calcPropsStack(playerId, backpack, propsId, count, false);
		List<UserProps> newPropsList = stackResult.getNewUserProps();
		int currBackSize = propsManager.getBackpackSize(playerId, backpack);
		ChainLock lock = LockUtils.getLock(userDomain.getPackLock());
		try {
			lock.lock();
			if(!newPropsList.isEmpty()) {
				if(!player.canAddNew2Backpack(currBackSize + newPropsList.size(), backpack)) {
					return false;
				}
				newPropsList = propsManager.createUserProps(newPropsList);
				propsManager.put2UserPropsIdsList(playerId, backpack, newPropsList);
			}
		} catch (Exception e) {
			LOGGER.error("角色: [{}] 增加道具异常:{}", playerId, e);
			return false;
		} finally {
			lock.unlock();
		}
		
		List<BackpackEntry> backpackEntrys = new ArrayList<BackpackEntry>();
		if(!newPropsList.isEmpty()) {
			backpackEntrys.addAll(newPropsList);
		}
		
		Map<Long, Integer> mergeProps = stackResult.getMergeProps();
		if(!mergeProps.isEmpty()) {
			List<UserProps> updateProps = propsManager.updateUserPropsList(mergeProps);
			backpackEntrys.addAll(updateProps);
		}
		
		if(!backpackEntrys.isEmpty()) {
			MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, backpackEntrys);
		}
		return true;
	}

}
