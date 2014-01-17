package com.yayo.warriors.module.pack.manager.impl;

import static com.yayo.warriors.module.pack.type.BackpackType.*;

import java.io.Serializable;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.warriors.module.pack.entity.Backpack;
import com.yayo.warriors.module.pack.entity.Backpack.PK;
import com.yayo.warriors.module.pack.manager.BackpackManager;
import com.yayo.warriors.module.pack.rule.BackpackRule;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.Job;

/**
 * 背包管理接口
 * 
 * @author Hyint
 */
@Service
public class BackpackManagerImpl extends CachedServiceAdpter implements BackpackManager {
	@Autowired
	private UserManager userManager;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * 查询玩家背包或者仓库的装备或者背包位置
	 * 
	 * @param  playerId					角色ID
	 * @param  packageType				背包号.详细见:{@link BackpackType}
	 * @return {@link Backpack}	背包位置信息
	 */
	
	public Backpack getPackagePosition(long playerId, int packageType) {
		Backpack backpack = null;
		if(playerId > 0 && ArrayUtils.contains(CAN_SAVE_PACKAGES, packageType)) {
			backpack = this.get(PK.valueOf(playerId, packageType), Backpack.class);
		}
		return backpack;
	}

	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		for (int packageType : BackpackType.CAN_SAVE_PACKAGES) {
			removeEntityFromCache(PK.valueOf(messageInfo.getPlayerId(), packageType), Backpack.class);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	protected <T, K extends Serializable> T getEntityFromDB(K id, Class<T> clazz) {
		if(id != null && clazz == Backpack.class) {
			Backpack backpack = commonDao.get(id, Backpack.class);
			if(backpack != null) {
				return (T) backpack;
			}
			
			PK backpackId = (PK)id;
			UserDomain userDomain = userManager.getUserDomain(backpackId.getPlayerId());
			if(userDomain == null) {
				return null;
			}
			
			try {
				int type = backpackId.getPackageType();
				Job job = userDomain.getBattle().getJob();
				backpack = Backpack.valueOf(backpackId, BackpackRule.getDefaultPosition(job, type));
				commonDao.save(backpack);
				return (T) backpack;
			} catch (Exception e) {
				logger.error("创建位置信息:[{}] 出现异常: {}", id, e);
				return null;
			}
		}
		return super.getEntityFromDB(id, clazz);
	}

}
