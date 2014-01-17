package com.yayo.warriors.module.vip.manager.impl;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap.Builder;
import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.warriors.basedb.model.VipConfig;
import com.yayo.warriors.module.vip.dao.VipDao;
import com.yayo.warriors.module.vip.entity.PlayerVip;
import com.yayo.warriors.module.vip.manager.VipManager;
import com.yayo.warriors.module.vip.model.VipDomain;

/**
 * VIP 管理接口实现类
 *  
 * @author Hyint
 */
@Service
public class VipManagerImpl extends CachedServiceAdpter implements VipManager {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private VipDao vipDao;
	@Autowired
	private ResourceService resourceService;
	
	
	private static final Builder<Long, VipDomain> BUILDER = new ConcurrentLinkedHashMap.Builder<Long, VipDomain>();
	//VIP域模型集合
	private static final ConcurrentLinkedHashMap<Long, VipDomain> VIP_DOMAINS = BUILDER.maximumWeightedCapacity(5000).build();


	/**
	 * 获得VIP管理接口
	 * 
	 * @param  playerId				角色ID
	 * @return {@link PlayerVip}	角色VIP对象
	 */
	
	public PlayerVip getPlayerVip(long playerId) {
		PlayerVip playerVip = null;
		if(playerId > 0L) {
			playerVip = this.get(playerId, PlayerVip.class);
		}
		return playerVip;
	}

	
	/**
	 * 用户VIP域
	 * 
	 * @param  playerId            角色ID
	 * @return {@link VipDomain}   VIP域
	 */
	
	public VipDomain getVip(long playerId) {
		VipDomain vipDomain = VIP_DOMAINS.get(playerId);
		if (vipDomain != null) {
			return vipDomain;
		}
		
		PlayerVip vip = this.getPlayerVip(playerId);
		if (vip == null) {
			return null;
		}
		
		VipConfig vipConfig = resourceService.get(vip.getVipLevel(), VipConfig.class);
		VIP_DOMAINS.putIfAbsent(playerId, VipDomain.valueOf(vip, vipConfig));
		return VIP_DOMAINS.get(playerId);
	}
	
	
	/**
	 * 加入缓存
	 * 
	 * @param playerId              角色ID
	 * @param vipDomain             VIP域
	 */
	
	public void put2VipCache(long playerId, VipDomain vipDomain) {
		VIP_DOMAINS.put(playerId, vipDomain);
	}
	
	
	/**
	 * VIP福利信息
	 * 
	 * @param vipLevel              主键ID
	 * @return {@link VipConfig}    VIP基础信息
	 */
	
	public VipConfig getVipConfig(int vipLevel) {
		return resourceService.get(vipLevel, VipConfig.class);
	}
	
	
	
	
	@SuppressWarnings("unchecked")
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(id != null && clazz == PlayerVip.class) {
			PlayerVip playerVip = vipDao.get(id, PlayerVip.class);
			if(playerVip == null) {
				try {
					playerVip = PlayerVip.valueOf((Long) id);
					vipDao.save(playerVip);
				} catch (Exception e) {
					playerVip = null;
					logger.error("角色:[{}] 创建VIP信息异常:{}", id, e);
				}
			}
			return (T) playerVip;
		}
		return super.getEntityFromDB(id, clazz);
	}


	
	/**
	 * 清实体缓存
	 * 
	 * @param playerId     
	 */
	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		VIP_DOMAINS.remove(messageInfo.getPlayerId());
		this.removeEntityFromCache(messageInfo.getPlayerId(), PlayerVip.class);
	}


}
