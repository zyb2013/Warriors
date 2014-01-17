package com.yayo.warriors.module.props.manager.impl;

import static com.yayo.warriors.module.pack.type.BackpackType.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.mina.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.cache.CachedService;
import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.common.utility.DateUtil;
import com.yayo.warriors.basedb.model.EquipConfig;
import com.yayo.warriors.basedb.model.EquipRankConfig;
import com.yayo.warriors.basedb.model.EquipStarConfig;
import com.yayo.warriors.basedb.model.PropsArtificeConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.basedb.model.PropsSynthConfig;
import com.yayo.warriors.basedb.model.WashRuleConfig;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.logger.log.GoodsMoveLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Orient;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.constant.PropsConstant;
import com.yayo.warriors.module.props.dao.UserEquipDao;
import com.yayo.warriors.module.props.dao.UserPropsDao;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.CreateResult;
import com.yayo.warriors.module.props.rule.EquipHelper;
import com.yayo.warriors.module.props.type.PropsChildType;
import com.yayo.warriors.module.props.type.Quality;
import com.yayo.warriors.module.server.listener.DataRemoveListener;
import com.yayo.warriors.module.treasure.rule.TreasureRule;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.Player.PackLock;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.type.GoodsType;


/**
 * 用户道具Manager接口
 * 
 * @author Hyint
 */
@Component
public class PropsManagerImpl extends CachedServiceAdpter implements PropsManager, DataRemoveListener {
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private DbService dbService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserEquipDao userEquipDao;
	@Autowired
	private UserPropsDao userPropsDao;
	@Autowired
	private CachedService cachedService;
	@Autowired
	private ResourceService resourceService;

	/** 主模块 */
	public static final String PREFIX = "ITEMS_";
	/** 角色ID */
	public static final String PLAYERID = "PLAYERID_";
	/** 背包号 */
	public static final String BACKPACK = "BACKPACK_";
	/** 道具Key */
	public static final String ITEM_PREFIX = "USERITEM_";
	/** 装备Key*/
	private static final String EQUIP_PREFIX = "USEREQUIP_";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PropsManagerImpl.class);
	
	
	public EquipConfig getEquipConfig(int equipId) {
		return resourceService.get(equipId, EquipConfig.class);
	}

	
	public EquipStarConfig getEquipStarConfig(int star) {
		return resourceService.get(star, EquipStarConfig.class);
	}

	
	public PropsArtificeConfig getPropsArtifice(int propsId) {
		return resourceService.get(propsId, PropsArtificeConfig.class);
	}

	
	
	public WashRuleConfig getWashRuleConfig(int additionNum) {
		return resourceService.get(additionNum, WashRuleConfig.class);
	}

	/**
	 * 获得道具合成配置信息
	 * 
	 * @param  propsId						基础道具ID
	 * @return {@link PropsSynthConfig}		道具合成配置信息
	 */
	
	public PropsSynthConfig getPropsSynthConfig(int propsId) {
		return resourceService.get(propsId, PropsSynthConfig.class);
	}

	
	public PropsConfig getPropsConfig(int baseId) {
		return resourceService.get(baseId, PropsConfig.class);
	}

	
	public EquipRankConfig getEquipRankConfig(int equipId) {
		return resourceService.get(equipId, EquipRankConfig.class);
	}

	
	public UserProps getUserProps(long userPropsId) {
		return this.get(userPropsId, UserProps.class);
	}

	
	public UserEquip getUserEquip(long userEquipId) {
		return this.get(userEquipId, UserEquip.class);
	}
	
	@SuppressWarnings("unchecked")
	
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(clazz != null && clazz == UserEquip.class) {
			UserEquip userEquip = commonDao.get((Long)id, UserEquip.class);
			if(userEquip == null || userEquip.getCount() <= 0) {
				return (T) userEquip;
			} 

			boolean updateEntity = false;
			EquipConfig equipConfig = userEquip.getEquipConfig();
			if(StringUtils.isBlank(userEquip.getAttributes())) {
				updateEntity = true;
				EquipHelper.refreshEquipStarAttributes(userEquip);
			}
			
			if(EquipHelper.fillShenwuAttributeToEquip(userEquip, equipConfig)) {
				updateEntity = true;
			}
			
			if(updateEntity) {
				dbService.submitUpdate2Queue(userEquip);
			}
			return (T) userEquip;
		}
		return super.getEntityFromDB(id, clazz);
	}

	/** 
	 * 获得背包的 HashKey
	 * 
	 * @param  playerId			角色ID
	 * @return {@link String}	道具模块的HashKey
	 */
	private String getHashKey(long playerId) {
		return new StringBuffer().append(PREFIX).append(PLAYERID).append(playerId).toString();
	}

	/**
	 * 获得装备背包附属键
	 * 
	 * @param  playerId			角色ID
	 * @return {@link String}	背包附属的子健
	 */
	private String getEquipBackpackSubKey(int backpack) {
		return new StringBuffer().append(EQUIP_PREFIX).append(BACKPACK).append(backpack).toString();
	}

	/**
	 * 获得道具背包附属键
	 * 
	 * @param  playerId			角色ID
	 * @return {@link String}	背包附属的子健
	 */
	private String getPropsBackpackSubKey(int backpack) {
		return new StringBuffer().append(ITEM_PREFIX).append(BACKPACK).append(backpack).toString();
	}
	
	
	public List<UserEquip> listUserEquip(long playerId, int backpack) {
		return getUserEquipByIdList(getUserEquipIdList(playerId, backpack));
	}

	
	public List<UserEquip> getUserEquipByIdList(Collection<Long> userEquipIdList) {
		return this.getEntityFromIdList(userEquipIdList, UserEquip.class);
	}

	
	public void changeUserPropsBackpack(long playerId, int sourceBackpack,  int targetBackpack, Collection<UserProps> userPropsList) {
		if(userPropsList != null && userPropsList.size() > 0) {
			Set<Long> sourceIds = getUserPropsIdList(playerId, sourceBackpack);
			Set<Long> targetIds = getUserPropsIdList(playerId, targetBackpack);
			for(UserProps userProps : userPropsList){
				sourceIds.remove(userProps.getId());
				if(userProps.getCount() > 0){
					targetIds.add(userProps.getId());
				}
			}
		}
	}
	
	
	public void changeUserPropsBackpack(long playerId, int sourceBackpack,  int targetBackpack, UserProps... userPropsList) {
		if(userPropsList != null && userPropsList.length > 0) {
			Set<Long> sourceIds = getUserPropsIdList(playerId, sourceBackpack);
			Set<Long> targetIds = getUserPropsIdList(playerId, targetBackpack);
			for(UserProps userProps : userPropsList){
				sourceIds.remove(userProps.getId());
				if(userProps.getCount() > 0){
					targetIds.add(userProps.getId());
				}
			}
		}
	}

	
	public void createPlayerPack(Player player) {
		if(player == null || player.getId() == null){
			return ;
		}
		String hashKey = getHashKey(player.getId());
		int[] backpacks = {DEFAULT_BACKPACK, STORAGE_BACKPACK, DRESSED_BACKPACK, LOTTERY_BACKPACK};
		for(int backpack : backpacks){
			String propSubKey = getPropsBackpackSubKey(backpack);
			cachedService.put2CommonHashCacheIfAbsent(hashKey, propSubKey, new ConcurrentHashSet<Long>() );
			
			String equipSubKey = getEquipBackpackSubKey(backpack);
			cachedService.put2CommonHashCacheIfAbsent(hashKey, equipSubKey, new ConcurrentHashSet<Long>() );
		}
		
	}

	@SuppressWarnings("unchecked")
	
	public Set<Long> getUserPropsIdList(long playerId, int backpack) {
		String hashKey = getHashKey(playerId);
		String subKey = getPropsBackpackSubKey(backpack);
		Set<Long> idList = (Set<Long>) cachedService.getFromCommonCache(hashKey, subKey);
		if(idList != null || !ArrayUtils.contains(QUERY_BACKPACKS, backpack)) {
			return idList;
		}
		
		UserDomain userDomain = userManager.getUserDomain(playerId);
		ChainLock lock = LockUtils.getLock(userDomain.getPackLock());
		try {
			lock.lock();
			idList = (Set<Long>) cachedService.getFromCommonCache(hashKey, subKey);
			if(idList != null) {
				return idList;
			}
			
			Set<Long> newUserItemIdList = new ConcurrentHashSet<Long>();
			List<Long> userItemIdList = userPropsDao.getUserPropsIdList(playerId, backpack);
			if(userItemIdList == null || userItemIdList.isEmpty()) {
				cachedService.put2CommonHashCache(hashKey, subKey, newUserItemIdList);
				return newUserItemIdList;
			}

			for (Long userPropsId : userItemIdList) {
				UserProps userProps = this.getUserProps(userPropsId);
				if(userProps == null || userProps.getBackpack() != backpack) {
					continue;
				}

				if(ArrayUtils.contains(VALID_COUNT_BACKPACKS, backpack) && userProps.getCount() <= 0) {
					continue;
				}
				newUserItemIdList.add(userPropsId);
			}
			cachedService.put2CommonHashCache(hashKey, subKey, newUserItemIdList);
			return newUserItemIdList;
		} catch (Exception e) {
			LOGGER.error("查询角色:[{}] 背包:[{}] 的道具ID列表计算出异常", new Object[] { playerId, backpack, e });
			return null;
		} finally {
			lock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	
	public Set<Long> getUserEquipIdList(long playerId, int backpack) {
		String hashKey = getHashKey(playerId);
		String subKey = getEquipBackpackSubKey(backpack);
		Set<Long> idList = (Set<Long>) cachedService.getFromCommonCache(hashKey, subKey);
		if(idList != null || !ArrayUtils.contains(BackpackType.QUERY_BACKPACKS, backpack)) {
			return idList;
		}
		
		UserDomain userDomain = userManager.getUserDomain(playerId);
		ChainLock lock = LockUtils.getLock(userDomain.getPackLock());
		try {
			lock.lock();
			idList = (Set<Long>) cachedService.getFromCommonCache(hashKey, subKey);
			if(idList != null) {
				return idList;
			}
			
			Set<Long> newUserItemIdList = new ConcurrentHashSet<Long>();
			List<Long> userItemIdList = userEquipDao.getUserEquipIdList(playerId, backpack);
			if(userItemIdList == null || userItemIdList.isEmpty()) {
				cachedService.put2CommonHashCache(hashKey, subKey, newUserItemIdList);
				return newUserItemIdList;
			}

			for (Long userEquipId : userItemIdList) {
				UserEquip userEquip = this.getUserEquip(userEquipId);
				if(userEquip != null && userEquip.validBackpack(backpack)) {
					newUserItemIdList.add(userEquipId);
				}
			}
			cachedService.put2CommonHashCache(hashKey, subKey, newUserItemIdList);
			return newUserItemIdList;
		} catch (Exception e) {
			LOGGER.error("查询角色:[{}] 背包:[{}] 的道具ID列表计算出异常", new Object[] { playerId, backpack, e });
			return null;
		} finally {
			lock.unlock();
		}
	}

	
	public void put2UserEquipIdsList(long playerId, int backpack, Collection<UserEquip> userEquipList) {
		if(userEquipList != null && userEquipList.size() > 0){
			Set<Long> userEquipIdList = getUserEquipIdList(playerId, backpack);
			for(UserEquip userEquip : userEquipList){
				if(userEquip.getCount() > 0 || backpack == BackpackType.DROP_BACKPACK || backpack == BackpackType.SOLD_BACKPACK){
					userEquipIdList.add(userEquip.getId());
				}
			}
		}
	}
	
	
	public void put2UserEquipIdsList(long playerId, int backpack, UserEquip... userEquipList) {
		if(userEquipList != null && userEquipList.length > 0){
			Set<Long> userEquipIdList = getUserEquipIdList(playerId, backpack);
			for(UserEquip userEquip : userEquipList){
				if(userEquip.getCount() > 0 || backpack == BackpackType.DROP_BACKPACK || backpack == BackpackType.SOLD_BACKPACK){
					userEquipIdList.add(userEquip.getId());
				}
			}
		}
	}

	
	public void removeFromEquipIdsList(long playerId, int backpack, Collection<UserEquip> userEquipList) {
		if(userEquipList != null && userEquipList.size() > 0){
			Set<Long> userEquipIdList = getUserEquipIdList(playerId, backpack);
			for(UserEquip userEquip : userEquipList){
				userEquipIdList.remove(userEquip.getId());
			}
		}
	}

	
	public void removeFromEquipIdsList(long playerId, int backpack, UserEquip... userEquips) {
		if(userEquips != null && userEquips.length > 0){
			Set<Long> userEquipIdList = getUserEquipIdList(playerId, backpack);
			for(UserEquip userEquip : userEquips){
				userEquipIdList.remove(userEquip.getId());
			}
		}
	}

	
	public List<UserProps> listUserProps(long playerId, int backpack) {
		Set<Long> idList = getUserPropsIdList(playerId, backpack);
		return this.getEntityFromIdList(idList, UserProps.class);
	}

	
	public List<UserProps> listUserPropByBaseId(long playerId, int propsId, int backpack) {
		List<UserProps> userPropsList = new ArrayList<UserProps>();
		List<UserProps> cacheUserPropsList = this.listUserProps(playerId, backpack);
		if(cacheUserPropsList == null || cacheUserPropsList.isEmpty()) {
			return userPropsList;
		}
	
		for (UserProps userProps : cacheUserPropsList) {
			if(userProps != null && userProps.getBaseId() == propsId) {
				userPropsList.add(userProps);
			}
		}
		return userPropsList;
	}

	
	public List<UserEquip> listUserEquipByBaseId(long playerId, int equipId, int backpack) {
		List<UserEquip> userEquipList = new ArrayList<UserEquip>();
		List<UserEquip> cacheUserEquipList = this.listUserEquip(playerId, backpack);
		if(cacheUserEquipList == null || cacheUserEquipList.isEmpty()) {
			return userEquipList;
		}
	
		for (UserEquip userEquip : cacheUserEquipList) {
			if(userEquip != null && userEquip.getBaseId() == equipId) {
				userEquipList.add(userEquip);
			}
		}
		return userEquipList;
	}

	
	public List<UserProps> getUserPropsByIdList(Collection<Long> userItemIdList) {
		return getEntityFromIdList(userItemIdList, UserProps.class);
	}

	
	public void changeUserEquipBackpack(long playerId, int sourceBackpack,  int targetBackpack, Collection<UserEquip> userEquipList){
		if(userEquipList != null && userEquipList.size() > 0){
			Set<Long> sourceIds = getUserEquipIdList(playerId, sourceBackpack);
			Set<Long> targetIds = getUserEquipIdList(playerId, targetBackpack);
			for(UserEquip userEquip : userEquipList){
				sourceIds.remove(userEquip.getId());
				if(userEquip.getCount() > 0){
					targetIds.add(userEquip.getId());
				}
			}
		}
	}
	
	
	public void changeUserEquipBackpack(long playerId, int sourceBackpack,  int targetBackpack, UserEquip... userEquipList){
		if(userEquipList != null && userEquipList.length > 0){
			Set<Long> sourceIds = getUserEquipIdList(playerId, sourceBackpack);
			Set<Long> targetIds = getUserEquipIdList(playerId, targetBackpack);
			for(UserEquip userEquip : userEquipList){
				sourceIds.remove(userEquip.getId());
				if(userEquip.getCount() > 0){
					targetIds.add(userEquip.getId());
				}
			}
		}
	}

	
	public void put2UserPropsIdsList(long playerId, int backpack, Collection<UserProps> userPropsList) {
		if(userPropsList != null && userPropsList.size() > 0){
			Set<Long> userPropsIdList = getUserPropsIdList(playerId, backpack);
			for(UserProps userProps : userPropsList){
				if(userProps.getCount() > 0 || backpack == BackpackType.DROP_BACKPACK || backpack == BackpackType.SOLD_BACKPACK){
					userPropsIdList.add(userProps.getId());
				}
			}
		}
	}
	
	
	public void put2UserPropsIdsList(long playerId, int backpack, UserProps... userPropsList) {
		if(userPropsList != null && userPropsList.length > 0){
			Set<Long> userPropsIdList = getUserPropsIdList(playerId, backpack);
			for(UserProps userProps : userPropsList){
				if(userProps.getCount() > 0 || backpack == BackpackType.DROP_BACKPACK || backpack == BackpackType.SOLD_BACKPACK){
					userPropsIdList.add(userProps.getId());
				}
			}
		}
	}

	
	public void removeFromUserPropsIdsList(long playerId, int backpack, Collection<UserProps> userPropsList) {
		if(userPropsList != null && userPropsList.size() > 0){
			Set<Long> userPropsIdList = getUserPropsIdList(playerId, backpack);
			for(UserProps userProps : userPropsList){
				userPropsIdList.remove(userProps.getId());
			}
		}
	}

	
	public void removeFromUserPropsIdsList(long playerId, int backpack, UserProps... userPropsList) {
		if(userPropsList != null && userPropsList.length > 0){
			Set<Long> userPropsIdList = getUserPropsIdList(playerId, backpack);
			for(UserProps userProps : userPropsList){
				userPropsIdList.remove(userProps.getId());
			}
		}
	}

	
	public void removeUserPropsIfCountNotEnough(long playerId, int backpack, Collection<UserProps> userPropsList) {
		if(userPropsList != null && userPropsList.size() > 0){
			Set<Long> userPropsIdList = getUserPropsIdList(playerId, backpack);
			for(UserProps userProps : userPropsList){
				if(userProps.getCount() <= 0){
					userPropsIdList.remove(userProps.getId());
					put2UserPropsIdsList(userProps.getPlayerId(), BackpackType.DROP_BACKPACK, userProps);
				}
			}
		}
	}
	
	

	
	public void removeUserPropsIfCountNotEnough(Collection<UserProps> userPropsList) {
		if(userPropsList != null && userPropsList.size() > 0){
			for(UserProps userProps : userPropsList){
				Set<Long> userPropsIdList = getUserPropsIdList(userProps.getPlayerId(), userProps.getBackpack());
				if(userProps.getCount() <= 0){
					userPropsIdList.remove(userProps.getId());
					put2UserPropsIdsList(userProps.getPlayerId(), BackpackType.DROP_BACKPACK, userProps);
				}
			}
		}
	}

	
	public void removeUserPropsIfCountNotEnough(UserProps... userPropsList) {
		if(userPropsList != null && userPropsList.length > 0){
			for(UserProps userProps : userPropsList){
				Set<Long> userPropsIdList = getUserPropsIdList(userProps.getPlayerId(), userProps.getBackpack());
				if(userProps.getCount() <= 0){
					userPropsIdList.remove(userProps.getId());
					put2UserPropsIdsList(userProps.getPlayerId(), BackpackType.DROP_BACKPACK, userProps);
				}
			}
		}
	}

	
	public void removeUserPropsIfCountNotEnough(long playerId, int backpack, UserProps... userPropsList) {
		if(userPropsList != null && userPropsList.length > 0){
			Set<Long> userPropsIdList = getUserPropsIdList(playerId, backpack);
			for(UserProps userProps : userPropsList){
				if(userProps.getCount() <= 0){
					userPropsIdList.remove(userProps.getId());
					put2UserPropsIdsList(userProps.getPlayerId(), BackpackType.DROP_BACKPACK, userProps);
				}
			}
		}
	}

	
	public int getBackpackSize(long playerId, int backpack) {
		int backpackSize = 0;
		Set<Long> userEquipIdList = this.getUserEquipIdList(playerId, backpack);
		Set<Long> userPropsIdList = this.getUserPropsIdList(playerId, backpack);
		backpackSize += (userEquipIdList == null ? 0 : userEquipIdList.size());
		backpackSize += (userPropsIdList == null ? 0 : userPropsIdList.size());
		return backpackSize;
	}

	
	public UserProps createUserProps(UserProps userProps) {
		checkAndAddExpiration(userProps);
		userPropsDao.createUserProps(userProps);
		List<UserProps> list = put2EntityCache(userProps);
		return list != null && list.size() > 0 ? list.get(0) : null;
	}

	
	public List<UserProps> createUserProps(Collection<UserProps> userPropsList) {
		checkAndAddExpiration(userPropsList);
		userPropsDao.createUserProps(userPropsList);
		return put2EntityCache(userPropsList);
	}

	
	public List<UserProps> spliteUserProps(UserProps createProps, UserProps updateProps) {
		userPropsDao.spliteUserProps(createProps, updateProps);
		return put2EntityCache(createProps, updateProps);
	}
 
	@SuppressWarnings("unchecked")
	
	public List<UserProps> costUserPropsList(Map<Long, Integer> costUserItems) {
		List<UserProps> userPropsList = new ArrayList<UserProps>(0);
		if(costUserItems == null || costUserItems.isEmpty()) {
			return userPropsList;
		}
		
		for (Entry<Long, Integer> entry : costUserItems.entrySet()) {
			Long userItemId = entry.getKey();
			Integer itemCount = entry.getValue();
			if(userItemId == null || itemCount == null) {
				continue;
			}
			
			UserProps userProps = this.getUserProps(userItemId);
			if(userProps == null) {
				continue;
			}
			
			UserDomain userDomain = userManager.getUserDomain(userProps.getPlayerId());
			ChainLock lock = LockUtils.getLock(userDomain.getPackLock());
			try {
				lock.lock();
				userProps.decreaseItemCount(itemCount);
				this.removeUserPropsIfCountNotEnough(userProps);
			} finally {
				lock.unlock();
			}
			userPropsList.add(userProps);
		}
		
		if(!userPropsList.isEmpty()) {
			dbService.submitUpdate2Queue(userPropsList);
		}
		return userPropsList;
	} 

	
	public List<UserProps> updateUserPropsList(Map<Long, Integer> updateUserItems) {
		List<UserProps> userPropsList = new ArrayList<UserProps>(updateUserItems == null ? 0 : updateUserItems.size());
		if(updateUserItems == null || updateUserItems.isEmpty()) {
			return userPropsList;
		}
		
		for (Entry<Long, Integer> entry : updateUserItems.entrySet()) {
			Long userItemId = entry.getKey();
			Integer itemCount = entry.getValue();
			if(userItemId == null || itemCount == null) {
				continue;
			}
			
			UserProps userProps = this.getUserProps(userItemId);
			if(userProps == null) {
				continue;
			}
			
			PackLock packLock = userManager.getUserDomain(userProps.getPlayerId()).getPackLock();
			ChainLock lock = LockUtils.getLock(packLock);
			try {
				lock.lock();
				userProps.increaseItemCount(itemCount);
				dbService.submitUpdate2Queue(userProps);
				removeUserPropsIfCountNotEnough(userProps);
			} finally {
				lock.unlock();
			}
			userPropsList.add(userProps);
		}
		
		return userPropsList;
	}

	
	public List<UserEquip> createUserEquip(UserEquip... userEquips) {
		userPropsDao.createUserEquip(userEquips);
		return put2EntityCache(userEquips);
	}

	
	public List<UserEquip> createUserEquip(Collection<UserEquip> userEquips) {
		userPropsDao.createUserEquip(userEquips);
		return put2EntityCache(userEquips);
	}

	
	public CreateResult<UserProps,UserEquip>  createUserEquipAndUserProps(Collection<UserProps> userPropsList, Collection<UserEquip> userEquips) {
		checkAndAddExpiration(userPropsList);
		userPropsDao.createUserEquipAndProps(userEquips, userPropsList);
		List<UserEquip> equipCaches = put2EntityCache(userEquips);
		List<UserProps> propsCaches = put2EntityCache(userPropsList);
		return CreateResult.valueOf(propsCaches, equipCaches);
		
	}

	@SuppressWarnings("unchecked")
	
	public int transferPackage(long playerId, List<UserProps> userPropsList, List<UserEquip> userEquipList, int sourcePackType, int targetPackType) {
		if(userPropsList.isEmpty() && userEquipList.isEmpty()){
			return PropsConstant.INPUT_VALUE_INVALID;
		}
		
		if(sourcePackType != BackpackType.LOTTERY_BACKPACK || targetPackType != BackpackType.DEFAULT_BACKPACK ){
			return PropsConstant.INPUT_VALUE_INVALID;
		} else if(sourcePackType == targetPackType) {
			return CommonConstant.INPUT_VALUE_INVALID;
		}
		
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return PropsConstant.PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		int usedSize = getBackpackSize(playerId, targetPackType);
		if(!player.canAddNew2Backpack(usedSize, targetPackType)) {
			return PropsConstant.BACKPACK_FULLED;
		}
		
		List<UserProps> updatePropsList = new LinkedList<UserProps>();						//更新的道具列表
		List<UserEquip> updateEquipsList = new LinkedList<UserEquip>();						//更新的装备列表
		List<LoggerGoods> loggerGoodsList = new LinkedList<LoggerGoods>();					//日志信息
		List<BackpackEntry> addBackpackEntries = new LinkedList<BackpackEntry>();			//获得的物品
		List<BackpackEntry> deleteBackpackEntries = new LinkedList<BackpackEntry>();		//移除的物品
		ChainLock lock = LockUtils.getLock(player.getPackLock());
		try {
			lock.lock();
			int remainBackSize = 0;
			int maxBackSize = player.getMaxBackSize(targetPackType);
			usedSize = getBackpackSize(playerId, targetPackType);
			if((remainBackSize = maxBackSize - usedSize) <= 0) { //格子不够了, 告知没有格子了
				return CommonConstant.BACKPACK_FULLED;
			}
			
			if(userPropsList != null && !userPropsList.isEmpty() && remainBackSize > 0) {
				for (UserProps userProps : userPropsList) {
					if(remainBackSize <= 0) {
						break;
					}
					
					if(userProps.getBackpack() != sourcePackType) {
						continue;
					}
					
					remainBackSize --;
					BackpackEntry deleteEntry = new BackpackEntry();
					deleteEntry.setId(userProps.getId());
					deleteEntry.setBackpack(sourcePackType);
					deleteEntry.setGoodsType(GoodsType.PROPS);
					deleteEntry.setBaseId(userProps.getBaseId());
					deleteEntry.setIndex(userProps.getIndex());
					deleteEntry.setBinding(userProps.isBinding());
					deleteEntry.setQuality(userProps.getQuality());
					deleteBackpackEntries.add(deleteEntry);	//把需要删除的实体加入列表
					
					userProps.setIndex(-1);
					userProps.setBackpack(targetPackType);
					updatePropsList.add(userProps);
					addBackpackEntries.add(voFactory.getUserPropsEntry(userProps));
				}
			}
			 
			
			if(userEquipList != null && !userEquipList.isEmpty() && remainBackSize > 0){
				for(UserEquip userEquip: userEquipList){
					if(remainBackSize <= 0) {
						break;
					}
					
					if(userEquip.getBackpack() != sourcePackType) {
						continue;
					}
					
					remainBackSize --;
					BackpackEntry deleteEntry = new BackpackEntry();
					deleteEntry.setId(userEquip.getId());
					deleteEntry.setGoodsType(GoodsType.EQUIP);
					deleteEntry.setBackpack(sourcePackType);
					deleteEntry.setBaseId(userEquip.getBaseId());
					deleteEntry.setIndex(userEquip.getIndex());
					deleteEntry.setBinding(userEquip.isBinding());
					deleteEntry.setQuality(userEquip.getQuality());
					deleteBackpackEntries.add(deleteEntry);			//把需要删除的实体加入列表
					
					userEquip.setIndex(-1);
					userEquip.setBackpack(targetPackType);
					updateEquipsList.add(userEquip);
					addBackpackEntries.add(voFactory.getUserEquipEntry(userEquip));
				}
			}
			
			if(!updateEquipsList.isEmpty() || !updatePropsList.isEmpty()) {
				dbService.submitUpdate2Queue(updateEquipsList, updatePropsList);
			}
			
			//移除缓存
			this.changeUserEquipBackpack(playerId, sourcePackType, targetPackType, updateEquipsList);
			this.changeUserPropsBackpack(playerId, sourcePackType, targetPackType, updatePropsList);
			loggerGoodsList.addAll( LoggerGoods.incomeProps(userPropsList) );
			loggerGoodsList.addAll( LoggerGoods.loggerEquip(Orient.INCOME, userEquipList) );
		} finally {
			lock.unlock();
		}
		
		if(loggerGoodsList != null && loggerGoodsList.size() > 0){
			GoodsMoveLogger.log(player, sourcePackType, targetPackType, loggerGoodsList.toArray( new LoggerGoods[loggerGoodsList.size()] ) );
		}
 
		MessagePushHelper.pushUserProps2Client(playerId, sourcePackType, false, deleteBackpackEntries);
		MessagePushHelper.pushUserProps2Client(playerId, targetPackType, false, addBackpackEntries);	//推送背包物品
		return CommonConstant.SUCCESS;
	}

	@SuppressWarnings("unchecked")
	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		long playerId = messageInfo.getPlayerId();
		for(int backpack : BackpackType.TOTAL_BACKPACKS){
			if(backpack != BackpackType.DROP_BACKPACK || backpack != BackpackType.SOLD_BACKPACK){
				String hashKey = getHashKey(playerId);
				String subKeyProps = getPropsBackpackSubKey(backpack);
				Collection<Long> propsIds = (Collection<Long>) cachedService.getFromCommonCache(hashKey, subKeyProps);
				if(propsIds != null) {
					this.removeEntityFromCache(propsIds, UserProps.class);
				}
				
				String subKeyEquip = getEquipBackpackSubKey(backpack);
				Collection<Long> equipIds = (Collection<Long>) cachedService.getFromCommonCache(hashKey, subKeyEquip);
				if(equipIds != null) {
					this.removeEntityFromCache(equipIds, UserEquip.class);
				}
				
			} else{
				Set<Long> userPropsIdList = this.getUserPropsIdList(playerId, backpack);
				this.removeEntityFromCache(userPropsIdList, UserProps.class);
				Set<Long> userEquipIdList = this.getUserEquipIdList(playerId, backpack);
				this.removeEntityFromCache(userEquipIdList, UserEquip.class);
			}
		}
		
		/** 移除角色道具列表缓存，   参考PropsManagerImpl.getHashKey()方法*/
		String playerItemHashKey = getHashKey(playerId);
		this.cachedService.removeFromCommonCache(playerItemHashKey);
		
	}
	
	/**
	 * 检查是否需要增加道具有效期
	 * @param userProps
	 * @param propsConfig
	 * @return
	 */
	private void checkAndAddExpiration(UserProps... userPropsList){
		if(userPropsList != null){
			for(UserProps userProps : userPropsList){
				addPropsExpiration(userProps);
			}
		}
	}
	private void checkAndAddExpiration(Collection<UserProps> userPropsList){
		if(userPropsList != null){
			for(UserProps userProps : userPropsList){
				addPropsExpiration(userProps);
			}
		}
	}
	
	private void addPropsExpiration(UserProps userProps){
		PropsConfig propsConfig = resourceService.get(userProps.getBaseId(), PropsConfig.class);
		switch (propsConfig.getChildType()) {
		case PropsChildType.TREASURE_PROPS_TYPE:
			userProps.setQuality(Quality.GREEN);
			userProps.setExpiration( DateUtil.changeDateTime(new Date(), 1, TreasureRule.TREASURE_TIMEOUT_HOUR, 0, 0) );
			if(userProps.isOutOfExpiration()){	//怎么那么快就过期了？
				userProps.setExpiration( DateUtil.changeDateTime(new Date(), 1, TreasureRule.TREASURE_TIMEOUT_HOUR, 0, 0) );
			}
			break;
		default:
			break;
		}
	}
	
}
