package com.yayo.warriors.module.pack.facade.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.warriors.module.pack.entity.Backpack;
import com.yayo.warriors.module.pack.facade.BackpackFacade;
import com.yayo.warriors.module.pack.manager.BackpackManager;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.user.model.UserDomain;


/**
 * 背包位置保存接口
 * 
 * @author Hyint
 */
@Component
public class BackpackFacadeImpl implements BackpackFacade {

	@Autowired
	private DbService dbService;
	@Autowired
	private BackpackManager backpackManager;
	
	
	
	public byte[] getPackagePosition(long playerId, int packageType) {
		byte[] packageInfo = null;
		Backpack backpack = backpackManager.getPackagePosition(playerId, packageType);
		if(backpack != null) {
			packageInfo = backpack.getPackageInfo();
		}
		return packageInfo;
	}


	/**
	 * 角色登出保存数据接口
	 * 
	 * @param userDomain	角色域模型
	 */
	@SuppressWarnings("unchecked")
	
	public void onLogoutEvent(UserDomain userDomain) {
		long playerId = userDomain.getPlayerId();
		List<Backpack> backpacks = new ArrayList<Backpack>();
		for (int packageType : BackpackType.CAN_SAVE_PACKAGES) {
			Backpack backpack = backpackManager.getPackagePosition(playerId, packageType);
			if(backpack != null) {
				backpacks.add(backpack);
			}
		}
		
		if(!backpacks.isEmpty()) {
			dbService.submitUpdate2Queue(backpacks);
		}
	}

	/**
	 * 临时保存背包信息(仅保存在内存中)
	 * 
	 * @param playerId						角色ID
	 * @param packageType					背包类型
	 * @param packagePosition				背包位置信息
	 */
	
	public void saveTempPosition(long playerId, int packageType, byte[] packagePosition) {
		Backpack position = backpackManager.getPackagePosition(playerId, packageType);
		if(position != null) {
			position.setPackageInfo(packagePosition);
		}
	}
}
