package com.yayo.warriors.module.friends.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.achieve.model.AchieveType.*;
import static com.yayo.warriors.module.achieve.model.FirstType.*;
import static com.yayo.warriors.module.friends.FriendRule.*;
import static com.yayo.warriors.module.friends.constant.FriendConstant.*;
import static com.yayo.warriors.module.pack.type.BackpackType.*;
import static com.yayo.warriors.type.FormulaKey.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.cache.CachedService;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.socket.SessionManager;
import com.yayo.common.utility.CollectionUtils;
import com.yayo.common.utility.EnumUtils;
import com.yayo.common.utility.TimeConstant;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.FormulaHelper;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.achieve.facade.AchieveFacade;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.friends.entity.Friend;
import com.yayo.warriors.module.friends.entity.FriendsTreasure;
import com.yayo.warriors.module.friends.facade.FriendsFacade;
import com.yayo.warriors.module.friends.helper.FriendHelper;
import com.yayo.warriors.module.friends.manager.FriendManager;
import com.yayo.warriors.module.friends.type.FriendType;
import com.yayo.warriors.module.logger.log.ExpLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.log.SilverLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.module.props.model.PropsStackResult;
import com.yayo.warriors.module.props.rule.PropsHelper;
import com.yayo.warriors.module.props.type.PropsChildType;
import com.yayo.warriors.module.server.listener.LogoutListener;
import com.yayo.warriors.module.task.facade.TaskFacade;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.entity.PlayerMotion;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.rule.AttributeRule;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Job;
import com.yayo.warriors.module.user.type.Sex;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.vo.FriendsSearchVo;
import com.yayo.warriors.type.ElementType;
import com.yayo.warriors.type.GoodsType;

/**
 * 好友实现类
 * 
 * @author liuyuhua
 */
@Component
public class FriendsFacadeImpl implements FriendsFacade, LogoutListener {

	@Autowired
	private DbService dbService;
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private TaskFacade taskFacade;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private FriendManager friendManager;
	@Autowired
	private CachedService cachedService;
	@Autowired
	private AchieveFacade achieveFacade;
	@Autowired
	private SessionManager sessionManager;
	
	/** 玩家ID的SubKey前缀 */
	private static final String MAP_ID = "MAPID_";
	/** 好友模块的HashKey */
	private static final String HASH_KEY = "FRIENDS_";
	/** 玩家的SubKey前缀 */
	private static final String PLAYER_NAME = "PLAYERNAME_";

	/**
	 * 获取所有好友信息
	 * @param playerId              好友所属玩家ID
	 * @param type                  好有类型
	 * @return {@link ResultObject} 返回值对象
	 */
	
	public ResultObject<Collection<Friend>> loadAllFriend(Long playerId, FriendType type) {
		if (type == null) {
			return ResultObject.ERROR(TYPE_NODATA); // 没有相关类型的数据
		}
		return ResultObject.SUCCESS(friendManager.getFirends(playerId, type));
	}

	
	
	public void onLoginEvent(UserDomain userDomain, int branching) {
		FriendHelper.pushFriendOnlineState(userDomain, true);		
	}

	/**
	 * 添加好友申请
	 * 
	 * @param playerId              玩家的ID
	 * @param targetId              目标玩家的ID
	 * @return {@link ResultObject} 返回值对象
	 */
	
	public ResultObject<Friend> addFriend(Long playerId, Long targetId) {
		if (playerId.equals(targetId)) {
			return ResultObject.ERROR(TARGET_IS_SELF);
		}
		
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		if (player == null || battle == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(!userManager.isOnline(targetId)){
			return ResultObject.ERROR(TARGET_NOT_ONLINE);
		}
		
		if(friendManager.isFriend(playerId, targetId, FriendType.FRIENDLY)){
			return ResultObject.ERROR(TARGET_IN_FRIENDLY); 
		}
		
		if(friendManager.isFriend(playerId, targetId, FriendType.BLACK)){
			return ResultObject.ERROR(TARGET_IN_BLACK);
		}
		
		// 超过添加上限
		if (friendManager.size4Type(playerId, FriendType.FRIENDLY) >= LIMIT) {
			return ResultObject.ERROR(ERROR_LIMIT);
		}
		
		Friend tfriend = friendManager.getPlayerFriend(targetId, playerId);
		Friend friend = null;
		if (tfriend != null) {
			friendManager.resetKillMonsterCount(playerId, targetId);
			friend = Friend.valueOf(playerId, targetId, tfriend.getValue(), FriendType.FRIENDLY);
		} else {
			friend = Friend.valueOf(playerId, targetId, FriendType.FRIENDLY);
		}
		
		friendManager.createFriend(playerId,friend);
		
		if(friend == null){
			return ResultObject.ERROR(FAILURE);
		}
		
		if (friendManager.size4Type(targetId, FriendType.FRIENDLY) < LIMIT) {
			if (!friendManager.isFriend(targetId, playerId, FriendType.FRIENDLY)) {
				int roleJob = battle.getJob().ordinal();
				FriendHelper.applyFriend(targetId, player.getId(), roleJob, battle.getLevel(), player.getName());
			}
		}
		
		taskFacade.updateAddFriendTask(playerId);
		friendManager.removeFriendFocusCache(targetId);        // 清除通用缓存
		achieveFacade.firstAchieved(playerId, FIRST_ACHIEVE, FIRST_ADD_FRIEND);
		return ResultObject.SUCCESS(friend);
	}

	/**
	 * 删除好友
	 * @param playerId              玩家的ID
	 * @param targetId              目标玩家的ID
	 * @return {@link ResultObject} 返回值对象
	 */
	
	public ResultObject<Long> deleteFriend(Long playerId,Long targetId,FriendType type) {
		if (playerId.equals(targetId)) {
			return ResultObject.ERROR(TARGET_IS_SELF);
		}
		
		if(type == null){
			return ResultObject.ERROR(TYPE_NODATA);
		}
		
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(!friendManager.isFriend(playerId, targetId, type)){
			return ResultObject.ERROR(TARGET_NOT_FRIENDLY);
		}
		
		friendManager.removeFriend(playerId, targetId, type);
		friendManager.removeFriendFocusCache(targetId);
		return ResultObject.SUCCESS(targetId);
	}

	/**
	 * 添加黑名单
	 * @param playerId              好友所属玩家Id
	 * @param targetId              目标好友Id
	 * @return {@link ResultObject} 返回值对象
	 */
	
	public ResultObject<Friend> addBlack(Long playerId, Long targetId) {
		if (playerId.equals(targetId)) {
			return ResultObject.ERROR(TARGET_IS_SELF);
		}
		
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if (!userManager.isOnline(targetId)) {
			return ResultObject.ERROR(TARGET_NOT_ONLINE);
		}
		
		if(friendManager.isFriend(playerId, targetId, FriendType.BLACK)){
			return ResultObject.ERROR(TARGET_IN_BLACK); 
		}
		
		if (friendManager.size4Type(playerId, FriendType.BLACK) >= LIMIT) {
			return ResultObject.ERROR(ERROR_LIMIT);
		}
		
		if (friendManager.isFriend(playerId, targetId, FriendType.FRIENDLY)) {
			friendManager.removeFriend(playerId, targetId, FriendType.FRIENDLY);
		}
		if (friendManager.isFriend(playerId, targetId, FriendType.NEAREST)) {
			friendManager.removeFriend(playerId, targetId, FriendType.NEAREST);
		}
		
		Friend friend = Friend.valueOf(playerId, targetId, FriendType.BLACK);
		friendManager.createFriend(playerId, friend);
		
		if(friend == null){
			return ResultObject.ERROR(FAILURE);
		}
		
		return ResultObject.SUCCESS(friend);
	}


	/**
	 * 添加最近联系人
	 * 
	 * @param playerId              好友所属玩家ID
	 * @param targetId              目标好友ID
	 * @return {@link ResultObject} 返回值对象
	 */
	
	public ResultObject<Friend> addNearest(Long playerId, Long targetId) {
		if (playerId.equals(targetId)) {
			return ResultObject.ERROR(TARGET_IS_SELF);
		}
		
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(!userManager.isOnline(targetId)){
			return ResultObject.ERROR(TARGET_NOT_ONLINE);
		}
		
		if(friendManager.isFriend(playerId, targetId, FriendType.NEAREST)){
			return ResultObject.ERROR(TARGET_IN_NEAREST); 
		}
		
		if (friendManager.size4Type(playerId,FriendType.NEAREST) >= LIMIT) {
			return ResultObject.ERROR(ERROR_LIMIT);
		}
		
		Friend friend = Friend.valueOf(playerId, targetId,FriendType.NEAREST);
		friendManager.createFriend(playerId,friend);
		if(friend == null) {
			return ResultObject.ERROR(FAILURE);
		}
		
		return ResultObject.SUCCESS(friend);
	}

	
	/***
	 * 查找玩家 名字
	 * @return Collection<FriendsSearchVo> - {玩家名字,玩家ID}
	 */
	
	public ResultObject<Collection<FriendsSearchVo>> searchPlayerName(String keywords, long playerId) {
		String subKey = this.getPlayerNameSubKey(keywords); // 构建二级缓存关键字
		Collection<FriendsSearchVo> result = this.getCommonCache1min(subKey);

		if (null != result) {
			return ResultObject.SUCCESS(result);
		}

		result = this.search(keywords, playerId);

		// 查无此关键字,将直接返回
		if (result.isEmpty()) {
			return ResultObject.ERROR(SEARCH_TARGET_NOT_FOUNT);
		}

		this.putCommonCache1min(subKey, result); // 放入二级缓存

		return ResultObject.SUCCESS(result);
	}

	/**
	 * 搜索在线玩家关键字
	 * 
	 * @param keywords             关键字
	 * @return {@link Collection} {玩家名字}
	 */
	private Collection<FriendsSearchVo> search(String keywords, long playerId) {
		Collection<Long> ids = sessionManager.getOnlinePlayerIdList(); // 获取所有在线玩家IDS
		Collection<FriendsSearchVo> result = new ArrayList<FriendsSearchVo>();
		for (Long id : ids) {
			if (id == null || id == playerId) {
				continue;
			}
			
			UserDomain userDomain = userManager.getUserDomain(id);
			if(userDomain == null){
				continue;
			}
			
			Player player = userDomain.getPlayer();
			PlayerBattle battle = userDomain.getBattle();

			Job playerJob = battle.getJob();
			int playerLevel = battle.getLevel();
			String playerName = player.getName();
			boolean isFriend = friendManager.isFriend(playerId, id, FriendType.FRIENDLY);
			if (playerName.contains(keywords)) {
				result.add(FriendsSearchVo.valueOf(id, playerName, playerLevel, playerJob, isFriend));
			}
		}
		return result;
	}

	/**
	 * 好友的SubKey
	 * 
	 * @param playerName      角色名字
	 * @return {@link String} 角色名SubKey
	 */
	private String getPlayerNameSubKey(String playerName) {
		return HASH_KEY + PLAYER_NAME + (playerName == null ? "" : playerName);
	}

//	private String getMapPlayerIdSubKey(Long mapId) {
//		return HASH_KEY + MAP_ID + mapId;
//	}

	/**
	 * 放入公共缓存(公共缓存设置为1分钟)
	 */
	private void putCommonCache1min(String subKey, Object obj) {
		cachedService.put2CommonHashCache(HASH_KEY, subKey, obj, TimeConstant.ONE_MINUTE_MILLISECOND);
	}

	/**
	 * 获取公共缓存中的对象
	 * 
	 * @param subKey          好友的SubKey
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Collection<FriendsSearchVo> getCommonCache1min(String subKey) { 
		return (Collection<FriendsSearchVo>) cachedService.getFromCommonCache(HASH_KEY, subKey);
	}

//	@SuppressWarnings("unchecked")
//	private Collection<Long> getPlayerIdsCommonCache(String subKey) {
//		return (Collection<Long>) cachedService.getFromCommonCache(HASH_KEY, subKey);
//	}

	/**
	 * 获取该场景地图上玩家ID
	 * @param mapId
	 * @return
	 */
	@Deprecated
	public ResultObject<Long> getRandomPlayerId(Long playerId, Integer sex) {
//		UserDomain userDomain = userManager.getUserDomain(playerId);
//		if(userDomain == null){
//			return ResultObject.ERROR(PLAYER_NOT_FOUND);
//		}
//		
//		PlayerMotion playMotion = userDomain.getMotion();
//		Long mapId =  (long) playMotion.getMapId();
//		GameMap gameMap = userDomain.getCurrentScreen().getGameMap(); 
//		Collection<Long> playerIds = gameMap.getAllSpireIdCollection(ElementType.PLAYER);
//		if (playerIds.isEmpty()) {
//			return ResultObject.ERROR(PLAYERS_NOT_NEAR);
//		}
//		
//		String subKey = this.getMapPlayerIdSubKey(mapId);
//		Collection<Long> selectPlayerIds = this.getPlayerIdsCommonCache(subKey);
//		
//		Sex sextype = EnumUtils.getEnum(Sex.class, sex);// sex 0或1
//		if(sextype == null && sex != 2){
//			return ResultObject.ERROR(SEX_NOT_FOUND);
//		}
//		
//		if(selectPlayerIds == null){
//			selectPlayerIds = new ArrayList<Long>();
//			if (sex == Sex.FEMALE.ordinal() || sex == Sex.MALE.ordinal()) {
//			for (Long eachPlayerId : playerIds) {
//				UserDomain eacheUserDomain = userManager.getUserDomain(eachPlayerId);
//				if(eacheUserDomain == null){
//					continue;
//				}
//				int cacheSex = eacheUserDomain.getPlayer().getSex().ordinal();
//				if (cacheSex == sex) {
//					selectPlayerIds.add(eachPlayerId);
//				}
//			}
//			} else if (sex == 2) {
//				selectPlayerIds.addAll(playerIds);
//			}
//			this.putCommonCache1min(subKey, selectPlayerIds);
//		}
//		
//		selectPlayerIds.remove(playerId);                               // 如果存在自己的ID, 则移除
//		
//	    //玩家数据
//		List<Long> playerIdsData = new ArrayList<Long>();
//		for(long selPlayerIds : selectPlayerIds){
//			
//			if(!friendManager.isFriend(playerId, selPlayerIds, FriendType.FRIENDLY) &&
//			   !friendManager.isFriend(playerId, selPlayerIds, FriendType.BLACK)){
//				playerIdsData.add(selPlayerIds);
//			}		
//		}
//		
//		// 附近没有非好友玩家
//		if (playerIdsData.isEmpty()) {
//			return ResultObject.ERROR(PLAYERS_ARE_FRIEND);
//		}
//		
//		// 随机抽取玩家ID
//		Long targetPlayerId = playerIdsData.get(Tools.getRandomInteger(playerIdsData.size()));
//		return ResultObject.SUCCESS(targetPlayerId);
		return ResultObject.ERROR(FAILURE);
	}

	/**
	 * 查询祝福瓶实体
	 * @param playerId          玩家Id
	 * @return
	 */
	public FriendsTreasure getFriendsBless(long playerId) {
		FriendsTreasure treasure = friendManager.getFriendsTreasure(playerId);
		if (treasure != null) {
			if (treasure.clearData()) {
				dbService.submitUpdate2Queue(treasure);
			}
		}
		return treasure;
	}
	
	
	/**
	 * 获取祝福瓶一键征友的状态
	 * @param playerId
	 * @return {@link Boolean}
	 */
	
	public Map<String, Object> friendsCollected(long playerId) {
		Map<String, Object> result = new HashMap<String, Object>(3);
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain != null) {
			int paramLevel = 0;
			PlayerBattle battle = userDomain.getBattle();
			FriendsTreasure treasure = getFriendsBless(playerId);
			Set<Integer> set = treasure.getParamsSet();
			if (!set.isEmpty()) {
				paramLevel = Collections.max(set);
			}
			int param = alterParam(paramLevel);
			result.put(ResponseKey.RECEIVE_BLESS, treasure.isReward());
			result.put(ResponseKey.LEVEL_BEGIN, param);
			result.put(ResponseKey.IS_COLLECT, !(battle.getLevel() >= param));
		}
		return result;
	}
	
	
	
	/**
	 * 好友祝福(祝福瓶)
	 * @param playerId         发送祝福的玩家ID
	 * @param targetId         目标祝福的玩家ID
	 * @return
	 */
	
	public int getBless(long playerId, long targetId) {
		UserDomain userDomain   = userManager.getUserDomain(playerId);
		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(userDomain == null || targetDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if (battle == null) {
			return PLAYER_NOT_FOUND;
		}
		
		if (blessLevelLimit(battle.getLevel())) {
			return ERROR_LIMIT;
		}
		
		if(!friendManager.isFriend(playerId, targetId, FriendType.FRIENDLY)){
			return TARGET_NOT_FRIENDLY;
		}
		
		FriendsTreasure treasure = getFriendsBless(playerId);
		if (treasure.getBlessExp() >= BLESS_EXP_LIMIT) {
			return ERROR_LIMIT;
		} else if (treasure.isReward()) {
			return FAILURE; 
		}
		
		ChainLock lock = LockUtils.getLock(treasure);		
		try {
			lock.lock();
			if (treasure.getBlessExp() + BLESS_EXP < BLESS_EXP_LIMIT) {
				treasure.addExp(BLESS_EXP);
			} else {
				treasure.setBlessExp(BLESS_EXP_LIMIT);
			}
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(treasure);
		
		List<Long> playerIds = Arrays.asList(targetId);
		FriendHelper.pushFriendBless(playerId, userDomain.getPlayer().getName(), playerIds);
		return SUCCESS;
	}

	
	/**
	 * 领取祝福瓶经验
	 * @param playerId         玩家ID
	 * @return
	 */
	@SuppressWarnings("unchecked")
	
	public int rewardBlessExp(long playerId) {
	    UserDomain userDomain = userManager.getUserDomain(playerId);
	    if(userDomain == null){
	    	return PLAYER_NOT_FOUND;
	    }
		
		PlayerBattle battle = userDomain.getBattle();
		if (battle == null) {
			return PLAYER_NOT_FOUND;
		} else if (battle.getLevel() < LEVEL_LIMIT) {
			return LEVEL_LIMIT;
		}
		
		FriendsTreasure treasure = this.getFriendsBless(playerId);
		if(treasure == null){
			return PLAYER_NOT_FOUND;
		}
		
		ChainLock lock = LockUtils.getLock(treasure, battle);
		int blessExp = 0;
		try {
			lock.lock();
			if(treasure.isReward()) {
				return FAILURE; 
			}
			
			blessExp = treasure.getBlessExp();
			treasure.useExp(blessExp);
			treasure.setReward(true);
			battle.increaseExp(blessExp);
			if(blessExp != 0){ //记录经验日志
				ExpLogger.friendFieldExp(userDomain, blessExp);
			}
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(treasure, battle);
		List<Long> receiver = Arrays.asList(playerId);
		UserPushHelper.pushAttribute2AreaMember(playerId, receiver, Arrays.asList(userDomain.getUnitId()), AttributeRule.PLAYER_EXP);
		return SUCCESS; 
	}

	
	/**
	 * 一键征友, 服务器随机12名玩家 
	 * @param playerId          玩家ID
	 * @return {@link FriendsSearchVo}
	 */
	
	public Collection<FriendsSearchVo> listRandomPlayer(Long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return Collections.emptyList();
		}
		
		int paramLevel = 0;
		FriendsTreasure treasure = getFriendsBless(playerId);
		Set<Integer> paramsSet = treasure.getParamsSet();
		if (!paramsSet.isEmpty()) {
			paramLevel = Collections.max(paramsSet);
		}
		
		PlayerBattle userBattle = userDomain.getBattle();
		int param = alterParam(paramLevel);
		if (userBattle.getLevel() < param) {
			return Collections.emptyList();
		}
		
		Set<Long> onlinePlayerIds = sessionManager.getOnlinePlayerIdList();
		List<Long> idList = new ArrayList<Long>(onlinePlayerIds);
		Collection<Long> friendIds = friendManager.getfriendIds(playerId);
		idList.remove(playerId);                                              // 移除自己
		if (friendIds != null) {
			idList.remove(playerId);
			idList.removeAll(friendIds);
		}
		
		ChainLock lock = LockUtils.getLock(treasure);
		try {
			lock.lock();
			paramsSet.add(param);
			treasure.updateParamsSet(paramsSet);
		} finally {
			lock.unlock();
		}
		
		int temp = idList.size() > COUNT ? COUNT : idList.size();              // 征集12个玩家
		Collection<FriendsSearchVo> voList = new ArrayList<FriendsSearchVo>();
		List<Long> chooseList = CollectionUtils.subListCopy(idList, 0, temp);
		for (Long id : chooseList) {
			UserDomain domain = userManager.getUserDomain(id);
			if (domain != null) {
				Player player = domain.getPlayer();
				PlayerBattle battle = domain.getBattle();
				
				Job clazz = battle.getJob();
				int iconId = player.getIcon();
				String name = player.getName();
				FriendsSearchVo vo = FriendsSearchVo.valueOf(id, name, clazz, iconId);
				voList.add(vo);
			}
		}
		
		dbService.submitUpdate2Queue(treasure);
		return voList;	
	}
	
	
	/**
	 * 好友赠酒(增加好友度)
	 * 
	 * @param targetId                  目标ID 
	 * @param userPropsId               用户道具ID
	 * @return {@link CommonConstant}
	 */ 
	
	public long friendsPresentWine(long playerId, long targetId, String userItems) {
		if (playerId == targetId) {
			return TARGET_IS_SELF;
		}
		
		UserDomain playerDomain = userManager.getUserDomain(playerId);
		if (playerDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if (targetDomain == null) {
			return TARGET_NOT_FOUND;
		}
		
		if (!userManager.isOnline(targetId)) {
			return PLAYER_OFF_LINE;
		}
		
		if (!friendManager.isFriend(playerId, targetId, FriendType.FRIENDLY)) {
			return TARGET_NOT_FRIENDLY;
		}
		
		int baseId = 0;
		int totalCount = 0;
		List<LoggerGoods> goodsLoggers = new ArrayList<LoggerGoods>(2);
		Map<Long, Integer> userBackItems = this.spliteUserItems(userItems);
		for (Map.Entry<Long, Integer> entry : userBackItems.entrySet()) {
			long userPropsId = entry.getKey();
			int propsCount = entry.getValue();
			
			if (propsCount <= 0) {
				return INPUT_VALUE_INVALID;
			}
			UserProps userProps = propsManager.getUserProps(userPropsId);
			if (userProps == null) {
				return ITEM_NOT_ENOUGH;
			} else if (userProps.getPlayerId() != playerId) {
				return BELONGS_INVALID;
			} else if (userProps.getBackpack() != DEFAULT_BACKPACK) {
				return NOT_IN_BACKPACK;
			} else if (userProps.getCount() < propsCount) {
				return ITEM_NOT_ENOUGH;
			} else if(userProps.isTrading()) {
				return ITEM_CANNOT_USE;
			}
			
			int childType = PropsChildType.RESURRECT_ITEM;
			PropsConfig propsConfig = userProps.getPropsConfig();
			if (propsConfig.getChildType() != childType) {
				return TYPE_INVALID;
			}
			
			totalCount += propsCount;
			baseId = propsConfig.getId();
			goodsLoggers.add(LoggerGoods.outcomeProps(userPropsId, propsConfig.getId(), propsCount));
		}
		
		PropsStackResult stack = PropsHelper.calcPropsStack(targetId, DEFAULT_BACKPACK, baseId, totalCount, true);
		Map<Long, Integer> mergeProps = stack.getMergeProps();		// 堆叠的道具
		List<UserProps> newUserProps = stack.getNewUserProps();		// 新创建的道具
		
		int playerBackSize = propsManager.getBackpackSize(targetId, DEFAULT_BACKPACK);
		ChainLock lock = LockUtils.getLock(targetDomain.getPackLock());
		try {
			lock.lock();
			int needSize = playerBackSize + newUserProps.size();
			if (!targetDomain.getPlayer().canAddNew2Backpack(needSize, DEFAULT_BACKPACK)) {
				return TARGET_BACK_FULL;
			}
			
			if (!newUserProps.isEmpty()) {
				propsManager.createUserProps(newUserProps);
				propsManager.put2UserPropsIdsList(targetId, DEFAULT_BACKPACK, newUserProps);
			}
		} finally {
			lock.unlock();
		}
		
		List<BackpackEntry> targetEntries = new ArrayList<BackpackEntry>();
		List<UserProps> updateUserPropsList = propsManager.updateUserPropsList(mergeProps);
		if(!mergeProps.isEmpty()) {
			targetEntries.addAll(voFactory.getUserPropsEntries(updateUserPropsList));
		}
		if(!newUserProps.isEmpty()) {
			targetEntries.addAll(voFactory.getUserPropsEntries(newUserProps));
		}
		
		List<UserProps> costUserPropsList = propsManager.costUserPropsList(userBackItems);
//		List<BackpackEntry> playerEntries = voFactory.getUserPropsEntries(costUserPropsList);
		
		MessagePushHelper.pushUserProps2Client(playerId, DEFAULT_BACKPACK, false, costUserPropsList);
		MessagePushHelper.pushUserProps2Client(targetId, DEFAULT_BACKPACK, false, targetEntries);
		
		GoodsVO playerGoodsVO = GoodsVO.valueOf(baseId, GoodsType.PROPS, -totalCount);
		Collection<GoodsVO> targetGoodsVO = GoodsVO.valuleOf(newUserProps, updateUserPropsList, mergeProps, null);
		
		MessagePushHelper.pushGoodsCountChange2Client(playerId, playerGoodsVO);
		MessagePushHelper.pushGoodsCountChange2Client(targetId, targetGoodsVO);
		
		Friend pfriend = friendManager.raiseFriendly(playerId, targetId, totalCount);                 // 双方增加好友度, 赠酒数量就是好友度值
		Friend tfriend = friendManager.raiseFriendly(targetId, playerId, totalCount);           
	
		FriendHelper.plusFriendValue(playerDomain.getPlayer(), targetId, tfriend, totalCount);
		
		LoggerGoods targetGoods = LoggerGoods.incomeProps(baseId, totalCount);                     // 得到物品
		LoggerGoods[] playerGoods = goodsLoggers.toArray(new LoggerGoods[goodsLoggers.size()]);    // 失去物品
		
		GoodsLogger.goodsLogger(playerDomain.getPlayer(), Source.FRIENDS_PRESENT, playerGoods);
		GoodsLogger.goodsLogger(targetDomain.getPlayer(), Source.FRIENDS_PRESENT, targetGoods);
		return pfriend.getId();
	}
	
	
	/**
	 * 好友敬酒
	 * 
	 * @param playerId                  
	 * @param targetId
	 * @return {@link CommonConstant}
	 */
	
	public int greetFriends(long playerId, long targetId) {
		UserDomain pDomain = userManager.getUserDomain(playerId);
		if (pDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		UserDomain tDomain = userManager.getUserDomain(targetId);
		if (tDomain == null) {
			return TARGET_NOT_FOUND;
		}
		
		PlayerBattle pBattle = pDomain.getBattle();
		PlayerBattle tBattle = tDomain.getBattle();
		
		if (pBattle.getLevel() < GREET_LIMIT) {
			return LEVEL_INVALID;
		} else if (tBattle.getLevel() < GREET_LIMIT) {
			return LEVEL_INVALID;
		}
		
		FriendsTreasure pTreasure = friendManager.getFriendsTreasure(playerId);
		FriendsTreasure tTreasure = friendManager.getFriendsTreasure(targetId);
		
		int addExp = 0;
		if (pTreasure.getGreetFriends().size() < 200) {       // 敬酒前200个好友加敬酒经验
			addExp = FormulaHelper.invoke(GREET_FRIENDS_EXP_REWARD, pBattle.getLevel()).intValue();
		}
		
		ChainLock lock = LockUtils.getLock(pTreasure, tTreasure);
		try {
			lock.lock();
			if (pTreasure.getGreetFriends().contains(targetId)) {
				return GREETED_FRIEND;
			}
			
			if (tTreasure.getWineMeasure() >= WINE_LIMIT) {
				return WINE_FULL;
			}
			
			tTreasure.addWine(10);                     		// 每敬酒一次增加10点酒量
			pBattle.increaseExp(addExp);
			pTreasure.addGreetFriends(targetId);
			pTreasure.updateGreetFriends(targetId);
			tTreasure.put2HistoryMap(playerId);
			dbService.submitUpdate2Queue(pTreasure, tTreasure);
		} finally {
			lock.unlock();
		}
		
		if (userManager.isOnline(targetId)) {
			FriendHelper.pushFriendGreetWine(playerId, targetId);
		}
		
		if (addExp > 0) {
			ExpLogger.expReward(pDomain, Source.FRIENDS_GREET, addExp);
			List<Long> receiver = Arrays.asList(playerId);
			List<UnitId> playerUnits = Arrays.asList(pDomain.getUnitId());
			UserPushHelper.pushAttribute2AreaMember(playerId, receiver, playerUnits, AttributeRule.PLAYER_EXP);
		}
		
		return SUCCESS;
	}
	
	
	/**
	 * 喝酒领奖
	 * 
	 * @param  playerId
	 * @return {@link CommonConstant}
	 */
	
	public int drinkWine(long playerId) {
		UserDomain domain = userManager.getUserDomain(playerId);
		if (domain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		PlayerBattle battle = domain.getBattle();
		FriendsTreasure treasure = friendManager.getFriendsTreasure(playerId);
		
		if (battle.getLevel() < GREET_LIMIT) {
			return LEVEL_INVALID;
		}
		
		if (treasure.getWineMeasure() <= 0) {
			return FAILURE;
		}
		
		int addExp = FormulaHelper.invoke(FRIEND_WINE_EXP_REWARD, battle.getLevel(), treasure.getWineMeasure()).intValue();
		
		ChainLock lock = LockUtils.getLock(battle, treasure);
		try {
			lock.lock();
			if (treasure.isDrinked()) {
				return WINE_DRINKED;
			}
			
			if (treasure.getWineMeasure() <= 0) {
				return FAILURE;
			}
			
			treasure.setDrinked(true);
			treasure.setWineMeasure(0);
			battle.increaseExp(addExp);
			dbService.submitUpdate2Queue(treasure);
		} finally {
			lock.unlock();
		}
		
		ExpLogger.expReward(domain, Source.FRIENDS_DRINK_WINE, addExp);
		List<Long> receiver = Arrays.asList(playerId);
		List<UnitId> playerUnits = Arrays.asList(domain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, receiver, playerUnits, AttributeRule.PLAYER_EXP);
		return SUCCESS;
	}
	
	
	/**
	 * 一键祝福好友
	 * 
	 * @param playerId
	 * @param playerIds
	 * @return
	 */
	
	public int blessFriends(long playerId, List<Long> playerIds) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		if (playerIds == null || playerIds.isEmpty()) {
			return FAILURE;
		}
		
		FriendsTreasure treasure = friendManager.getFriendsTreasure(playerId);
		
		ChainLock lock = LockUtils.getLock(treasure);
		try {
			lock.lock();
			int addExp = BLESS_EXP * playerIds.size();
			if (treasure.getBlessExp() + addExp >= BLESS_EXP_LIMIT) {
				treasure.setBlessExp(BLESS_EXP_LIMIT);
			} else {
				treasure.addExp(addExp);
			}
			dbService.submitUpdate2Queue(treasure);
		} finally {
			lock.unlock();
		}
		
		Player player = userDomain.getPlayer();
		FriendHelper.pushFriendBless(playerId, player.getName(), playerIds);
		return SUCCESS;
	}
	
	
	
	
	public boolean isGreet(long playerId, long targetId) {
		FriendsTreasure treasure = getFriendsBless(playerId);
		return treasure.getGreetFriends().contains(targetId);
	}
	
	
	/** 
	 * 好友下线处理
	 * @param playerId
	 */
	
	public void onLogoutEvent(UserDomain userDomain) {
		FriendHelper.pushFriendOnlineState(userDomain, false);
	}

	
	/**
	 * 截取用户道具信息字符串
	 * @param userItems
	 * @return {@link Map}
	 */
	private Map<Long, Integer> spliteUserItems(String userItems) {
		Map<Long, Integer> maps = new HashMap<Long, Integer>();
		List<String[]> arrays = Tools.delimiterString2Array(userItems);
		if (arrays != null && !arrays.isEmpty()) {
			for (String[] array : arrays) {
				Long userItemId = Long.valueOf(array[0]);
				Integer count = Integer.valueOf(array[1]);
				if(userItemId == null || count == null || count < 0) {
					continue;
				}
				
				Integer cacheCount = maps.get(userItemId);
				cacheCount = cacheCount == null ? 0 : cacheCount;
				maps.put(userItemId, count + cacheCount);
			}
		}
		return maps;
	}


	
	
}
