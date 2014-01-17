package com.yayo.warriors.module.title.manager.impl;

import java.io.Serializable;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.warriors.basedb.model.TitleDictionary;
import com.yayo.warriors.module.title.entity.PlayerTitle;
import com.yayo.warriors.module.title.manager.TitleManager;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 称号Manager接口
 * 
 * @author Hyint
 */
@Service
public class TitleManagerImpl extends CachedServiceAdpter implements TitleManager {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private ResourceService resourceService;
	
	
	
	public PlayerTitle getUserTitle(long playerId) {
		PlayerTitle playerTitle = null;
		UserDomain domain = userManager.getUserDomain(playerId);
		if (domain != null) {
			playerTitle = this.get(playerId, PlayerTitle.class);
		}
		return playerTitle;
	}
	
	
	
	public TitleDictionary getTitleConfig(int titleId) {
		return resourceService.get(titleId, TitleDictionary.class);
	}

	/**
	 * 列出称号列表
	 * 
	 * @return {@link Collection}	称号配置列表
	 */
	
	public Collection<TitleDictionary> listAllTitleConfig() {
		return resourceService.listAll(TitleDictionary.class);
	}

	
	@SuppressWarnings("unchecked")
	
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(id != null && clazz == PlayerTitle.class) {
			PlayerTitle title = commonDao.get(id, PlayerTitle.class);
			if(title != null) {
				return (T) title;
			}
			
			try {
				title = PlayerTitle.valueOf((Long) id);
				commonDao.save(title);
				return (T) title;
			} catch (Exception e) {
				logger.error("角色: [{}] 创建称号实体异常:{}", id, e);
				return null;
			}
		}
		return super.getEntityFromDB(id, clazz);
	}

	
	/**
	 * 清数据
	 * 
	 * @param playerId
	 */
	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		this.removeEntityFromCache(messageInfo.getPlayerId(), PlayerTitle.class);
	}

	
}
