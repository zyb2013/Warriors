package com.yayo.warriors.module.active.facade.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.model.ActiveOperatorConfig;
import com.yayo.warriors.basedb.model.ActiveOperatorExChangeConfig;
import com.yayo.warriors.basedb.model.ActiveOperatorLevelConfig;
import com.yayo.warriors.basedb.model.ActiveOperatorRankConfig;
import com.yayo.warriors.basedb.model.EquipConfig;
import com.yayo.warriors.basedb.model.HorseConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.NoticePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.active.entity.OperatorActive;
import com.yayo.warriors.module.active.entity.PlayerActive;
import com.yayo.warriors.module.active.facade.ActiveOperatorFacade;
import com.yayo.warriors.module.active.manager.ActiveOperatorManager;
import com.yayo.warriors.module.active.rule.ActiveLevelType;
import com.yayo.warriors.module.active.rule.ActiveOperatorType;
import com.yayo.warriors.module.active.rule.ActiveRankType;
import com.yayo.warriors.module.active.rule.ActiveRewardObject;
import com.yayo.warriors.module.active.rule.ActiveRewardStatus;
import com.yayo.warriors.module.active.verify.ActiveLevelService;
import com.yayo.warriors.module.active.verify.ActiveRankService;
import com.yayo.warriors.module.active.vo.OperatorActiveVo;
import com.yayo.warriors.module.alliance.entity.Alliance;
import com.yayo.warriors.module.alliance.manager.AllianceManager;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.horse.entity.Horse;
import com.yayo.warriors.module.horse.manager.HorseManager;
import com.yayo.warriors.module.logger.log.ActiveLogger;
import com.yayo.warriors.module.logger.log.CouponLogger;
import com.yayo.warriors.module.logger.log.ExpLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.log.SilverLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.notice.rule.NoticeRule;
import com.yayo.warriors.module.notice.type.NoticeID;
import com.yayo.warriors.module.notice.type.NoticeType;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.CreateResult;
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.module.props.model.PropsStackResult;
import com.yayo.warriors.module.props.rule.EquipHelper;
import com.yayo.warriors.module.props.rule.PropsHelper;
import com.yayo.warriors.module.task.model.RewardVO;
import com.yayo.warriors.module.task.model.TaskRewardVO;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.rule.AttributeRule;
import com.yayo.warriors.module.user.type.Job;
import com.yayo.warriors.type.GoodsType;

import static com.yayo.warriors.constant.CommonConstant.BACKPACK_FULLED;
import static com.yayo.warriors.constant.CommonConstant.BACKPACK_INVALID;
import static com.yayo.warriors.constant.CommonConstant.BASEDATA_NOT_FOUND;
import static com.yayo.warriors.constant.CommonConstant.INPUT_VALUE_INVALID;
import static com.yayo.warriors.constant.CommonConstant.ITEM_CANNOT_USE;
import static com.yayo.warriors.constant.CommonConstant.ITEM_NOT_ENOUGH;
import static com.yayo.warriors.module.active.constant.ActiveConstant.*;
import static com.yayo.warriors.module.pack.type.BackpackType.DEFAULT_BACKPACK;

/**
 * 在线活动 
 * @author liuyuhua
 */
@Component
public class ActiveOperatorFacadeImpl implements ActiveOperatorFacade{
	
	@Autowired
	private ActiveOperatorManager activeOperatorManager;
	@Autowired
	private ActiveLevelService activeLevelService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private ActiveRankService activeRankService;
	@Autowired
	private DbService dbService;
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private AllianceManager allianceManager;
	@Autowired
	private HorseManager horseManager;

	
	
	/** 管理后台调用*/
	
	public int createOrUpdateActive(List<OperatorActiveVo> vos) {
		if(vos == null || vos.isEmpty()){
			return INPUT_VALUE_INVALID;
		}
		
		boolean exist = false;//是否不存在基础配置
		List<OperatorActive> actives = new ArrayList<OperatorActive>(5);
		for(OperatorActiveVo vo : vos){
			ActiveOperatorConfig config = activeOperatorManager.getActiveOperatorConfig(vo.getActiveBaseId());
			if(config == null){
				exist = true;
			}
			actives.add(OperatorActive.valueOf(vo, config));
		}
		
		if(exist){
			return BASEDATA_NOT_FOUND;
		}
		
		boolean result = activeOperatorManager.createOrUpdateActive(actives);
		return result == true ? SUCCESS : FAILURE;
	}

    /** 管理后台调用*/
	
	public int deleteActive(List<Long> ids) {
		if(ids == null || ids.isEmpty()){
			return INPUT_VALUE_INVALID;
		}
		List<Long> result = activeOperatorManager.deleteActive(ids);
		return result.size() > 0 ? SUCCESS : FAILURE;
	}

	
	public List<OperatorActive> sublistActive(int start, int count) {
		List<OperatorActive> actives = activeOperatorManager.getClientShowActives();
		List<OperatorActive> result = Tools.pageResult(actives,(start * count), count);
		if(result == null){
			result = new ArrayList<OperatorActive>(0);
		}
		return result;
	}

	
	@SuppressWarnings("unchecked")
	public int rewardRankActive(long playerId, long aliveActiveId, int activeId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		PlayerActive playerActive = activeOperatorManager.getPlayerActive(battle);
		if(playerActive == null){
			return PLAYER_NOT_FOUND;
		}
		
		OperatorActive active = activeOperatorManager.getOperatorActive(aliveActiveId);
		if(active == null || !active.isOpened()){
			return ACTIVE_NOT_FOUND;
		}
		
		long currentTime = System.currentTimeMillis();
		if(currentTime < active.getStartTime() || currentTime > active.getLostTime()){//不可以小于运营开始时间,不可以大于活动领奖结束时间
		   return ACTIVE_NOT_FOUND;
		}
		
	    ActiveOperatorConfig config = active.getOperatorConfig();
	    if(config == null){
	    	return BASEDATA_NOT_FOUND;
	    }
	    
	    if(config.getType() != ActiveOperatorType.RANKING){
	    	return ACTIVE_NOT_FOUND;
	    }
	    
	    ActiveOperatorRankConfig rankConfig = activeOperatorManager.getActiveOperatorRankConfig(activeId);
	    if(rankConfig == null){
	    	return BASEDATA_NOT_FOUND;
	    }
	    
	    if(config.getId() != rankConfig.getActiveBaseId()){//rankConfig的基础类型必须是 运营活动的配置的 子类型
	    	return BASEDATA_CONFIG_ERROR;
	    }
	    
	    int activeBaseId = rankConfig.getActiveBaseId();
	    if(playerActive.getRankActives().contains(activeBaseId)){//判断是否领取过奖励
	    	return ACTIVE_REWARED;
	    }
	    
	    if(!activeRankService.isCondition(userDomain, rankConfig, active)){//重要,检测是否达成领取条件
	    	return ACTIVE_CONDITION_ENOUGH;
	    }
	    
		int backpack = BackpackType.DEFAULT_BACKPACK;
		TaskRewardVO taskRewardVO = constructRankReward(player, battle, rankConfig);
		int currentBackSize = propsManager.getBackpackSize(playerId, backpack);
		Collection<UserProps> newPropsList = taskRewardVO.getNewPropsList();
		Collection<UserEquip> newUserEquipList = taskRewardVO.getNewUserEquipList();
		
		ChainLock lock = LockUtils.getLock(player,battle,player.getPackLock(),playerActive);
		try {
			lock.lock();
		    if(playerActive.getRankActives().contains(activeBaseId)){
		    	return ACTIVE_REWARED;
		    }
			
			if(!newPropsList.isEmpty() || !newUserEquipList.isEmpty()) {
				int totalSize = currentBackSize + newPropsList.size() + newUserEquipList.size();
				if(!player.canAddNew2Backpack(totalSize, DEFAULT_BACKPACK)) {
					return BACKPACK_FULLED;
				}
				
				CreateResult<UserProps, UserEquip> cache = propsManager.createUserEquipAndUserProps(newPropsList, newUserEquipList);
				newPropsList = cache.getCollections1();
				newUserEquipList = cache.getCollections2();
				propsManager.put2UserPropsIdsList(playerId, backpack, newPropsList);
				propsManager.put2UserEquipIdsList(playerId, backpack, newUserEquipList);
			}
			
			battle.increaseExp(taskRewardVO.getAddExp());
			player.increaseSilver(taskRewardVO.getAddSilver());
			player.increaseCoupon(taskRewardVO.getAddCoupon());
			if(taskRewardVO.getAddExp() != 0) {//记录经验日志
				ExpLogger.activeExp(userDomain, Source.ACTIVE_OPERATOR, active.getTitle(), taskRewardVO.getAddExp());
			}
			if(taskRewardVO.getAddSilver() != 0){
				SilverLogger.inCome(Source.ACTIVE_OPERATOR, taskRewardVO.getAddSilver(), player);
			}
			
			if(taskRewardVO.getAddCoupon() != 0) {
				CouponLogger.inCome(Source.ACTIVE_OPERATOR, taskRewardVO.getAddCoupon(), player);
			}
			playerActive.addRankActives(activeBaseId);//加入已领取列表
			
		}finally{
			lock.unlock();
		}
		
		dbService.updateEntityIntime(player, battle, playerActive);
		
		List<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		Map<Long, Integer> mergePropMap = taskRewardVO.getMergePropMap();
		if(mergePropMap != null && !mergePropMap.isEmpty()) {
			Collection<UserProps> mergeList = propsManager.updateUserPropsList(mergePropMap);
			backpackEntries.addAll(mergeList);
		}
		
		if(!newPropsList.isEmpty()) {
			backpackEntries.addAll(newPropsList);
		}

		List<GoodsVO> goodsVos = new ArrayList<GoodsVO>(2);//发布给客户端的新增内容
		String logEquip = "";//新增的装备日志
		if(!newUserEquipList.isEmpty()) {
			backpackEntries.addAll(newUserEquipList);
			StringBuilder builder = new StringBuilder();
			for(UserEquip userEquip : newUserEquipList){//只需要添加新增的装备,物品已经在配置地方弄了
				int equipBaseId = userEquip.getBaseId();
				int count = userEquip.getCount();
				goodsVos.add(GoodsVO.valueOf(equipBaseId, GoodsType.EQUIP, count));
				builder.append(equipBaseId).append(Splitable.ATTRIBUTE_SPLIT).append(count).append(Splitable.ELEMENT_DELIMITER);
			}
			if(builder.length() > 0) {
				builder.deleteCharAt(builder.length() - 1);
			}
			logEquip = builder.toString();
		}
		
		List<Long> playerIdList = Arrays.asList(playerId);
		List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, AttributeRule.ACTIVE_PLAYER_ATTR);
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, backpackEntries);
		
		goodsVos.addAll(rankConfig.getClientShowGoods());//需要广播的内容
		MessagePushHelper.pushGoodsCountChange2Client(playerId, goodsVos);
		
		LoggerGoods[] goodsRewardInfo = taskRewardVO.getGoodsRewardInfo();
		if(goodsRewardInfo != null){
			GoodsLogger.goodsLogger(player, Source.ACTIVE_OPERATOR, goodsRewardInfo);//物品收入
		}
		
		int type = config.getType();
		String activeName = active.getTitle();
		ActiveLogger.activeLogger(player, battle, activeName, type, activeBaseId, 
				                  activeId, taskRewardVO.getAddCoupon(),
				                  taskRewardVO.getAddSilver(), taskRewardVO.getAddExp(),
				                  rankConfig.getLogItems(), logEquip); //日志记录
	    
		this.notice4Rank(player, rankConfig);//排行领取公告
		return SUCCESS;
	}
	
	
	@SuppressWarnings("unchecked")
	
	public int rewardLevelActive(long playerId, long aliveActiveId, int activeId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		PlayerActive playerActive = activeOperatorManager.getPlayerActive(battle);
		Alliance alliance = allianceManager.getAlliance4Battle(battle);
		
		if(playerActive == null){
			return PLAYER_NOT_FOUND;
		}
		
		OperatorActive active = activeOperatorManager.getOperatorActive(aliveActiveId);
		if(active == null || !active.isOpened()){
			return ACTIVE_NOT_FOUND;
		}
		
		long currentTime = System.currentTimeMillis();
		if(currentTime < active.getStartTime() || currentTime > active.getLostTime()){//不可以小于运营开始时间,不可以大于活动领奖结束时间
		   return ACTIVE_NOT_FOUND;
		}
		
	    ActiveOperatorConfig config = active.getOperatorConfig();
	    if(config == null){
	    	return BASEDATA_NOT_FOUND;
	    }
	    
	    if(config.getType() != ActiveOperatorType.LEVEL){
	    	return ACTIVE_NOT_FOUND;
	    }
	    
	    ActiveOperatorLevelConfig levelConfig = activeOperatorManager.getActiveOperatorLevelConfig(activeId);
	    if(levelConfig == null){
	    	return BASEDATA_NOT_FOUND;
	    }
	    
	    if(config.getId() != levelConfig.getActiveBaseId()){//rankConfig的基础类型必须是 运营活动的配置的 子类型
	    	return BASEDATA_CONFIG_ERROR;
	    }
	    
	    String rewardFlag = levelConfig.getRewardFlag();
	    if(playerActive.getLevelActives().contains(rewardFlag)){//判断是否领取过奖励
	    	return ACTIVE_REWARED;
	    }
	    
	    if(!activeLevelService.isCondition(userDomain, levelConfig)){
	    	return ACTIVE_CONDITION_ENOUGH;
	    }
	    
	    if(levelConfig.getRewardObject() == ActiveRewardObject.ALLIANCE_MASTER){//专门为奖励目标为帮主类型的处理,因为帮主奖励只能够领取一份
	    	if(alliance == null){
	    		return ACTIVE_CONDITION_ENOUGH;
	    	}
	    	
	    	if(alliance.getLevelActives().contains(rewardFlag)){
	    		return ACTIVE_REWARED;
	    	}
	    }
		
		int backpack = BackpackType.DEFAULT_BACKPACK;
		TaskRewardVO taskRewardVO = constructLevelReward(player, battle, levelConfig);
		int currentBackSize = propsManager.getBackpackSize(playerId, backpack);
		Collection<UserProps> newPropsList = taskRewardVO.getNewPropsList();
		Collection<UserEquip> newUserEquipList = taskRewardVO.getNewUserEquipList();
		
		ChainLock lock = LockUtils.getLock(player,battle,player.getPackLock(),playerActive);
		try {
			lock.lock();
		    if(playerActive.getLevelActives().contains(rewardFlag)){//判断是否领取过奖励
		    	return ACTIVE_REWARED;
		    }
		    
		    if(levelConfig.getRewardObject() == ActiveRewardObject.ALLIANCE_MASTER){//专门为奖励目标为帮主类型的处理,因为帮主奖励只能够领取一份
		    	if(alliance == null){
		    		return ACTIVE_CONDITION_ENOUGH;
		    	}
		    	
		    	if(alliance.getLevelActives().contains(rewardFlag)){
		    		return ACTIVE_REWARED;
		    	}
		    }
			
			if(!newPropsList.isEmpty() || !newUserEquipList.isEmpty()) {
				int totalSize = currentBackSize + newPropsList.size() + newUserEquipList.size();
				if(!player.canAddNew2Backpack(totalSize, DEFAULT_BACKPACK)) {
					return BACKPACK_FULLED;
				}
				
				CreateResult<UserProps, UserEquip> cache = propsManager.createUserEquipAndUserProps(newPropsList, newUserEquipList);
				newPropsList = cache.getCollections1();
				newUserEquipList = cache.getCollections2();
				propsManager.put2UserPropsIdsList(playerId, backpack, newPropsList);
				propsManager.put2UserEquipIdsList(playerId, backpack, newUserEquipList);
			}
			
			battle.increaseExp(taskRewardVO.getAddExp());
			player.increaseSilver(taskRewardVO.getAddSilver());
			player.increaseCoupon(taskRewardVO.getAddCoupon());
			if(taskRewardVO.getAddExp() != 0) {//记录经验日志
				ExpLogger.activeExp(userDomain, Source.ACTIVE_OPERATOR, active.getTitle(), taskRewardVO.getAddExp());
			}
			if(taskRewardVO.getAddSilver() != 0){
				SilverLogger.inCome(Source.ACTIVE_OPERATOR, taskRewardVO.getAddSilver(), player);
			}
			
			if(taskRewardVO.getAddCoupon() != 0) {
				CouponLogger.inCome(Source.ACTIVE_OPERATOR, taskRewardVO.getAddCoupon(), player);
			}
						
			///特殊处理关于帮派的奖励
			if(levelConfig.getRewardObject() == ActiveRewardObject.ALLIANCE_MASTER){
				alliance.addLevelActives(rewardFlag);
			}
			
			playerActive.addLevelActives(rewardFlag);//加入已领取列表
			
		}finally{
			lock.unlock();
		}
		
		 if(levelConfig.getRewardObject() == ActiveRewardObject.ALLIANCE_MASTER){
			 dbService.updateEntityIntime(player, battle, playerActive, alliance);
		 }else{
			 dbService.updateEntityIntime(player, battle, playerActive);
		 }
		
		List<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		Map<Long, Integer> mergePropMap = taskRewardVO.getMergePropMap();
		if(mergePropMap != null && !mergePropMap.isEmpty()) {
			Collection<UserProps> mergeList = propsManager.updateUserPropsList(mergePropMap);
			backpackEntries.addAll(mergeList);
		}
		
		if(!newPropsList.isEmpty()) {
			backpackEntries.addAll(newPropsList);
		}

		List<GoodsVO> goodsVos = new ArrayList<GoodsVO>(2);//发布给客户端的新增内容
		String logEquip = "";//新增的装备日志
		if(!newUserEquipList.isEmpty()) {
			backpackEntries.addAll(voFactory.getUserEquipEntries(newUserEquipList));
			StringBuilder builder = new StringBuilder();
			for(UserEquip userEquip : newUserEquipList){//只需要添加新增的装备,物品已经在配置地方弄了
				int equipBaseId = userEquip.getBaseId();
				int count = userEquip.getCount();
				goodsVos.add(GoodsVO.valueOf(equipBaseId, GoodsType.EQUIP, count));
				builder.append(equipBaseId).append(Splitable.ATTRIBUTE_SPLIT).append(count).append(Splitable.ELEMENT_DELIMITER);
			}
			if(builder.length() > 0) {
				builder.deleteCharAt(builder.length() - 1);
			}
			logEquip = builder.toString();
		}
		
		List<Long> playerIdList = Arrays.asList(playerId);
		List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, AttributeRule.ACTIVE_PLAYER_ATTR);
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, backpackEntries);
		
		goodsVos.addAll(levelConfig.getClientShowGoods());
		MessagePushHelper.pushGoodsCountChange2Client(playerId, goodsVos);
		
		LoggerGoods[] goodsRewardInfo = taskRewardVO.getGoodsRewardInfo();
		if(goodsRewardInfo != null){
			GoodsLogger.goodsLogger(player, Source.ACTIVE_OPERATOR, goodsRewardInfo);//物品收入
		}
		
		int type = config.getType();
		String activeName = active.getTitle();
		int activeBaseId = active.getActiveBaseId();
		ActiveLogger.activeLogger(player, battle, activeName, type,activeBaseId, 
				                  activeId, taskRewardVO.getAddCoupon(),
				                  taskRewardVO.getAddSilver(), taskRewardVO.getAddExp(),
				                  levelConfig.getLogItems(), logEquip); //日志记录
		
		this.notice4Level(player, battle, levelConfig);//公告
		return SUCCESS;
	}
	
	
	/**
	 * 排行公告
	 * @param player    玩家对象
	 * @param config    排行配置对象
	 */
	private void notice4Rank(Player player, ActiveOperatorRankConfig config){
		if(player == null || config == null){
			return;
		}
		
		HashMap<String, Object> paramsMap = new HashMap<String, Object>(2); 
		int condition = config.getRanking();
		String name = player.getName();
		paramsMap.put(NoticeRule.number, condition);
		paramsMap.put(NoticeRule.playerName, name);
		if(config.getType() == ActiveRankType.ROLE_TYPE){
			NoticePushHelper.pushNotice(NoticeID.ACTIVE_OPERATOR_RANK_ROLELEVEL, NoticeType.HONOR, paramsMap, 1);
		}else if(config.getType() == ActiveRankType.ROLE_FIGHT_TYPE){
			NoticePushHelper.pushNotice(NoticeID.ACTIVE_OPERATOR_RANK_FIGHT, NoticeType.HONOR, paramsMap, 1);
		}else if(config.getType() == ActiveRankType.PET_FIGHT_TYPE){
			NoticePushHelper.pushNotice(NoticeID.ACTIVE_OPERATOR_RANK_PET, NoticeType.HONOR, paramsMap, 1);
		}
	}
	

	/**
	 * 等级公告
	 * @param player     玩家对象
	 * @param battle     玩家战斗对象
	 * @param config     运营活动等级类型配置
	 */
	private void notice4Level(Player player, PlayerBattle battle, ActiveOperatorLevelConfig config){
		if(player == null ||  battle == null || config == null){
			return;
		}
		
		int condition = config.getCondition(); //条件
		HashMap<String, Object> paramsMap = new HashMap<String, Object>(2); 
		paramsMap.put(NoticeRule.number, condition);
		
	    if(config.getType() == ActiveLevelType.HORSE_LEVEL){
			Horse horse = horseManager.getHorse(battle);
			if(horse == null){
				return;
			}
			HorseConfig horseConfig = horseManager.getHorseConfig(horse.getLevel());
			if(horseConfig == null){
				return;
			}
			paramsMap.put(NoticeRule.playerName, player.getName());
			paramsMap.put(NoticeRule.horse, horseConfig.getName());
			NoticePushHelper.pushNotice(NoticeID.ACTIVE_OPERATOR_LEVEL_HORSE, NoticeType.HONOR, paramsMap, 1);
		}
	}
	
	
	
	
	
	@SuppressWarnings("unchecked")
	public ResultObject<Integer> rewardExChange(long playerId, long aliveActiveId, int activeId, String userItems) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		PlayerActive playerActive = activeOperatorManager.getPlayerActive(battle);
		if(playerActive == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		OperatorActive active = activeOperatorManager.getOperatorActive(aliveActiveId);
		if(active == null || !active.isOpened()){
			return ResultObject.ERROR(ACTIVE_NOT_FOUND);
		}
		
		long currentTime = System.currentTimeMillis();
		if(currentTime < active.getStartTime() || currentTime > active.getLostTime()){//不可以小于运营开始时间,不可以大于活动领奖结束时间
		   return ResultObject.ERROR(ACTIVE_NOT_FOUND);
		}
		
	    ActiveOperatorConfig config = active.getOperatorConfig();
	    if(config == null){
	    	return ResultObject.ERROR(BASEDATA_NOT_FOUND);
	    }
	    
	    if(config.getType() != ActiveOperatorType.EXCHANGE){
	    	return ResultObject.ERROR(ACTIVE_NOT_FOUND);
	    }
	    
	    ActiveOperatorExChangeConfig exChangeConfig = activeOperatorManager.getActiveOperatorExChangeConfig(activeId);
	    if(exChangeConfig == null){
	    	return ResultObject.ERROR(BASEDATA_NOT_FOUND);
	    }
	    
	    if(config.getId() != exChangeConfig.getActiveBaseId()){//exChangeConfig的基础类型必须是 运营活动的配置的 子类型
	    	return ResultObject.ERROR(BASEDATA_CONFIG_ERROR);
	    }
	    
	    int exChangeBaseId = exChangeConfig.getId();
	    Integer exChangeCount = playerActive.getExChange().get(exChangeBaseId);
	    if(!exChangeConfig.isUnLimit()){
		    if(exChangeCount != null && exChangeCount >= exChangeConfig.getLimitTimes()){
		    	return ResultObject.ERROR(ACTIVE_OVER_EXCHANGE_LIMIT);
		    }
	    }
	    
		Map<Integer,Integer> needItem = exChangeConfig.getNeedItemMap();
		if(needItem == null || needItem.isEmpty()){
			return ResultObject.ERROR(BASEDATA_CONFIG_ERROR);
		}
	    
	    Map<Integer,Integer> statisticsItems = new HashMap<Integer, Integer>(2);//统计玩家数量
	    Map<Long, Integer> costUserItems = this.spliteUserItems(userItems);//解析客户端发送过来的数据
	    if(costUserItems == null || costUserItems.isEmpty()){
	    	return ResultObject.ERROR(INPUT_VALUE_INVALID);
	    }
	    
		for (Map.Entry<Long, Integer> entry : costUserItems.entrySet()) {
			long userPropsId = entry.getKey();
			int propsCount   = entry.getValue();
			if (propsCount <= 0) {
				return ResultObject.ERROR(INPUT_VALUE_INVALID);
			}
		
			UserProps userProps = propsManager.getUserProps(userPropsId);
			if (userProps == null) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			}

			if (userProps.getPlayerId() != playerId) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			} else if (!userProps.validBackpack(DEFAULT_BACKPACK)) {
				return ResultObject.ERROR(BACKPACK_INVALID);
			} else if (userProps.getCount() < propsCount) {
				return ResultObject.ERROR(INPUT_VALUE_INVALID);
			} else if(userProps.isTrading()) {
				return ResultObject.ERROR(ITEM_CANNOT_USE);
			} 
			
			PropsConfig propsConfig = userProps.getPropsConfig();
			if (propsConfig == null) {
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			}
			
			int propsBaseId = propsConfig.getId();
			
			Integer count = statisticsItems.get(propsBaseId); //统计数量
			if(count == null){
				statisticsItems.put(propsBaseId, propsCount);
			}else{
				count += propsCount;
				statisticsItems.put(propsBaseId, count);
			}
		}
	    
		List<GoodsVO> goodsVos = new ArrayList<GoodsVO>(4);//推送客户端扣减物品
	    for(Entry<Integer, Integer> entry : needItem.entrySet()) { //判断兑换道具数量是否满足
	    	int propsBaseId = entry.getKey();
	    	int count = entry.getValue();
	    	if(statisticsItems.get(propsBaseId) == null || statisticsItems.get(propsBaseId) != count){
	    		return ResultObject.ERROR(INPUT_VALUE_INVALID);
	    	}
	    	
	    	goodsVos.add(GoodsVO.valueOf(propsBaseId, GoodsType.PROPS, (count * -1)));
	    }
	    
		int backpack = BackpackType.DEFAULT_BACKPACK;
		TaskRewardVO taskRewardVO = constructExChangeReward(player, battle, exChangeConfig);
		int currentBackSize = propsManager.getBackpackSize(playerId, backpack);
		Collection<UserProps> newPropsList = taskRewardVO.getNewPropsList();
		Collection<UserEquip> newUserEquipList = taskRewardVO.getNewUserEquipList();
		if(!newPropsList.isEmpty() || !newUserEquipList.isEmpty()) {//判断背包格子
			int totalSize = currentBackSize + newPropsList.size() + newUserEquipList.size();
			if(!player.canAddNew2Backpack(totalSize, DEFAULT_BACKPACK)) {
				return ResultObject.ERROR(BACKPACK_FULLED);
			}
		}
		
		List<UserProps> userPropslist = propsManager.costUserPropsList(costUserItems);
		if(userPropslist == null || userPropslist.isEmpty()){
			return ResultObject.ERROR(FAILURE);
		}
		
		ChainLock lock = LockUtils.getLock(player,battle,player.getPackLock(),playerActive);
		try {
			lock.lock();
		    if(!exChangeConfig.isUnLimit()){ //没有限制
		    	exChangeCount = playerActive.getExChange().get(exChangeBaseId);
			    if(exChangeCount != null && exChangeCount >= exChangeConfig.getLimitTimes()){
			    	return ResultObject.ERROR(ACTIVE_OVER_EXCHANGE_LIMIT);
			    }
		    }
			
			if(!newPropsList.isEmpty() || !newUserEquipList.isEmpty()) {
				int totalSize = currentBackSize + newPropsList.size() + newUserEquipList.size();
				if(!player.canAddNew2Backpack(totalSize, DEFAULT_BACKPACK)) {
					return ResultObject.ERROR(BACKPACK_FULLED);
				}
				
				CreateResult<UserProps, UserEquip> cache = propsManager.createUserEquipAndUserProps(newPropsList, newUserEquipList);
				newPropsList = cache.getCollections1();
				newUserEquipList = cache.getCollections2();
				propsManager.put2UserPropsIdsList(playerId, backpack, newPropsList);
				propsManager.put2UserEquipIdsList(playerId, backpack, newUserEquipList);
			}
			
			battle.increaseExp(taskRewardVO.getAddExp());
			player.increaseSilver(taskRewardVO.getAddSilver());
			player.increaseCoupon(taskRewardVO.getAddCoupon());
			if(taskRewardVO.getAddExp() != 0) {//记录经验日志
				ExpLogger.activeExp(userDomain, Source.ACTIVE_OPERATOR, active.getTitle(), taskRewardVO.getAddExp());
			}
			if(taskRewardVO.getAddSilver() != 0){
				SilverLogger.inCome(Source.ACTIVE_OPERATOR, taskRewardVO.getAddSilver(), player);
			}
			
			if(!exChangeConfig.isUnLimit()){ //有限制才需要入库
				playerActive.addExChange(exChangeBaseId);//加入已领取列表
			}
			
		}finally{
			lock.unlock();
		}
		
		
		dbService.updateEntityIntime(player, battle, playerActive);
		
		List<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		Map<Long, Integer> mergePropMap = taskRewardVO.getMergePropMap();
		if(mergePropMap != null && !mergePropMap.isEmpty()) {
			Collection<UserProps> mergeList = propsManager.updateUserPropsList(mergePropMap);
			backpackEntries.addAll(mergeList);
		}
		
		if(!newPropsList.isEmpty()) {
			backpackEntries.addAll(newPropsList);
		}

		String logEquip = "";//新增的装备日志
		if(!newUserEquipList.isEmpty()) {
			backpackEntries.addAll(voFactory.getUserEquipEntries(newUserEquipList));
			StringBuilder builder = new StringBuilder();
			for(UserEquip userEquip : newUserEquipList){//只需要添加新增的装备,物品已经在配置地方弄了
				int equipBaseId = userEquip.getBaseId();
				int count = userEquip.getCount();
				goodsVos.add(GoodsVO.valueOf(equipBaseId, GoodsType.EQUIP, count));
				builder.append(equipBaseId).append(Splitable.ATTRIBUTE_SPLIT).append(count).append(Splitable.ELEMENT_DELIMITER);
			}
			if(builder.length() > 0) {
				builder.deleteCharAt(builder.length() - 1);
			}
			logEquip = builder.toString();
		}
		
		if(!userPropslist.isEmpty()){
			backpackEntries.addAll(voFactory.getUserPropsEntries(userPropslist));
		}
		
		List<Long> playerIdList = Arrays.asList(playerId);
		List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, AttributeRule.ACTIVE_PLAYER_ATTR);
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, backpackEntries);
		
		goodsVos.addAll(exChangeConfig.getClientShowGoods());
		MessagePushHelper.pushGoodsCountChange2Client(playerId,goodsVos);
		
		LoggerGoods[] goodsRewardInfo = taskRewardVO.getGoodsRewardInfo();
		if(goodsRewardInfo != null){
			GoodsLogger.goodsLogger(player, Source.ACTIVE_OPERATOR, goodsRewardInfo);//物品收入
		}
		
		int type = config.getType();
		String activeName = active.getTitle();
		int activeBaseId = active.getActiveBaseId();
		
		if(taskRewardVO.getAddCoupon() != 0) {
			CouponLogger.inCome(Source.ACTIVE_OPERATOR, taskRewardVO.getAddCoupon(), player);
		}
		
		ActiveLogger.activeLogger(player, battle, activeName, type, activeBaseId, 
				                  activeId, taskRewardVO.getAddCoupon(),
				                  taskRewardVO.getAddSilver(), taskRewardVO.getAddExp(),
				                  exChangeConfig.getLogItems(), logEquip); //日志记录
		
		Integer count = playerActive.getExChange().get(exChangeBaseId);
		count = count == null ? 0 : count;
		
		return ResultObject.SUCCESS(count);
	}
	
	
	/**
	 * 构建冲级奖励信息
	 * @param  battle					战斗对象
	 * @param  config				        配置信息
	 * @return {@link TaskRewardVO}		任务VO对象
	 */
	private TaskRewardVO constructLevelReward(Player player, PlayerBattle battle, ActiveOperatorLevelConfig config) {
		TaskRewardVO taskRewardVO = new TaskRewardVO();
		List<RewardVO> rewardList = config.getRewardList();
		taskRewardVO.increaseExp(player.calcIndulgeProfit(config.getExp()));
		taskRewardVO.increaseSilver(player.calcIndulgeProfit(config.getSilver()));
		taskRewardVO.increaseCoupon(config.getCoupon());
		if(rewardList != null && !rewardList.isEmpty() && player.isGoodsReward()) {
			for (RewardVO rewardVO : rewardList) {
				int type = rewardVO.getType();
				int count = rewardVO.getCount();
				int baseId = rewardVO.getBaseId();
				boolean binding = rewardVO.isBinding();
				int starLevel = rewardVO.getStarLevel();
				if(count > 0 && type == GoodsType.EQUIP) {
					this.processEquipReward(battle, baseId, count, starLevel, binding, taskRewardVO);
				} else if(count > 0 && type == GoodsType.PROPS) {
					this.processPropsReward(battle, baseId, count, binding, taskRewardVO);
				}
			}
		}
		return taskRewardVO;
	}
	
	/**
	 * 构建兑换奖励信息
	 * @param  battle					战斗对象
	 * @param  config				        配置信息
	 * @return {@link TaskRewardVO}		任务VO对象
	 */
	private TaskRewardVO constructExChangeReward(Player player, PlayerBattle battle, ActiveOperatorExChangeConfig config) {
		TaskRewardVO taskRewardVO = new TaskRewardVO();
		List<RewardVO> rewardList = config.getRewardList();
		taskRewardVO.increaseExp(player.calcIndulgeProfit(config.getExp()));
		taskRewardVO.increaseSilver(player.calcIndulgeProfit(config.getSilver()));
		taskRewardVO.increaseCoupon(config.getCoupon());
		if(rewardList != null && !rewardList.isEmpty() && player.isGoodsReward()) {
			for (RewardVO rewardVO : rewardList) {
				int type = rewardVO.getType();
				int count = rewardVO.getCount();
				int baseId = rewardVO.getBaseId();
				boolean binding = rewardVO.isBinding();
				int starLevel = rewardVO.getStarLevel();
				if(count > 0 && type == GoodsType.EQUIP) {
					this.processEquipReward(battle, baseId, count, starLevel, binding, taskRewardVO);
				} else if(count > 0 && type == GoodsType.PROPS) {
					this.processPropsReward(battle, baseId, count, binding, taskRewardVO);
				}
			}
		}
		return taskRewardVO;
	}
	
	/**
	 * 构建排行奖励信息
	 * @param  battle					战斗对象
	 * @param  config				        配置信息
	 * @return {@link TaskRewardVO}		任务VO对象
	 */
	private TaskRewardVO constructRankReward(Player player, PlayerBattle battle, ActiveOperatorRankConfig config) {
		TaskRewardVO taskRewardVO = new TaskRewardVO();
		List<RewardVO> rewardList = config.getRewardList();
		taskRewardVO.increaseExp(player.calcIndulgeProfit(config.getExp()));
		taskRewardVO.increaseSilver(player.calcIndulgeProfit(config.getSilver()));
		taskRewardVO.increaseCoupon(config.getCoupon());
		if(rewardList != null && !rewardList.isEmpty() && player.isGoodsReward()) {
			for (RewardVO rewardVO : rewardList) {
				int type = rewardVO.getType();
				int count = rewardVO.getCount();
				int baseId = rewardVO.getBaseId();
				boolean binding = rewardVO.isBinding();
				int starLevel = rewardVO.getStarLevel();
				if(count > 0 && type == GoodsType.EQUIP) {
					this.processEquipReward(battle, baseId, count, starLevel, binding, taskRewardVO);
				} else if(count > 0 && type == GoodsType.PROPS) {
					this.processPropsReward(battle, baseId, count, binding, taskRewardVO);
				}
			}
		}
		return taskRewardVO;
	}
	
	/**
	 * 处理装备奖励
	 * 
	 * @param battle			角色战斗对象
	 * @param equipId			基础装备ID
	 * @param count				获得的数量	
	 * @param taskRewardVO		任务奖励VO
	 */
	private void processEquipReward(PlayerBattle battle, int equipId, int count, int starLevel, boolean binding, TaskRewardVO taskRewardVO) {
		long playerId = battle.getId();
		int playerJob = battle.getJob().ordinal();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		EquipConfig equipConfig = propsManager.getEquipConfig(equipId);
		if(equipConfig == null) {
			return;
		} 

		if(equipConfig.getJob() == Job.COMMON.ordinal() || equipConfig.getJob() == playerJob) {
			for (int i = 0; i < count; i++) {
				taskRewardVO.addNewEquip(EquipHelper.newUserEquip2Star(playerId, backpack, equipId, binding, starLevel));
			}
		}
	}

	/**
	 * 处理道具奖励
	 * 
	 * @param battle			角色战斗对象
	 * @param propsId			基础道具ID
	 * @param count				获得的数量
	 * @param binding			绑定状态
	 * @param taskRewardVO		任务奖励VO
	 */
	private void processPropsReward(PlayerBattle battle, int propsId, int count, boolean binding, TaskRewardVO taskRewardVO) {
		Job playerJob = battle.getJob();
		long playerId = battle.getId();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		PropsConfig propsConfig = propsManager.getPropsConfig(propsId);
		if(propsConfig != null && (propsConfig.getJob() == Job.COMMON.ordinal() || propsConfig.getJob() == playerJob.ordinal())) {
			PropsStackResult stackResult = PropsHelper.calcPropsStack(playerId, backpack, propsId, count, binding);
			taskRewardVO.addMergeProps(stackResult.getMergeProps());
			taskRewardVO.addNewPropsList(stackResult.getNewUserProps());
		}
	}


	
	
	/**
	 * 截取出字符串.
	 * @param userItems		用户道具信息. 格式: 用户道具ID_数量|用户道具ID_数量|...
	 * @return
	 */
	private Map<Long, Integer> spliteUserItems(String userItems) {
		Map<Long, Integer> maps = new HashMap<Long, Integer>();
		if(userItems == null || userItems.isEmpty()){
			return maps;
		}
		List<String[]> arrays = Tools.delimiterString2Array(userItems);
		if(arrays != null && !arrays.isEmpty()) {
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

	
	public String clientRewardActiveVrifi(long playerId, long aliveActiveId,int type, String activeIds) {
		String resultObject = "";
		if(activeIds == null || activeIds.isEmpty()){
			return resultObject;
		}
		
		OperatorActive operatorActive = activeOperatorManager.getOperatorActive(aliveActiveId);
		if(operatorActive == null){
			return resultObject;
		}
		
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return resultObject;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		PlayerActive playerActive = activeOperatorManager.getPlayerActive(battle);
		if(playerActive == null){
			return resultObject;
		}
		
		String[] splitActiveIds = activeIds.split(Splitable.ATTRIBUTE_SPLIT);
		if(splitActiveIds == null ||  splitActiveIds.length <= 0){
			return resultObject;
		}
		
		Map<Integer,Object> resultMap = new HashMap<Integer, Object>(2);//返回结果集合
		for(String strId : splitActiveIds){
			int activeId = Integer.parseInt(strId);
			if(type == ActiveOperatorType.EXCHANGE){
				Integer count = playerActive.getExChange().get(activeId);
				if(count == null){
					count = 0;
				}
				
				resultMap.put(activeId, count);
			}
			
			if(type == ActiveOperatorType.LEVEL){ //判断冲级类型是否可以领取奖励
				ActiveOperatorLevelConfig levelConfig = activeOperatorManager.getActiveOperatorLevelConfig(activeId);
				if(levelConfig == null){
					resultMap.put(activeId, ActiveRewardStatus.UNRESERVED);
				}else{
					
					if(levelConfig.getRewardObject() == ActiveRewardObject.ALLIANCE_MASTER){///特殊处理关于帮派的奖励
				    	Alliance alliance = allianceManager.getAlliance4Battle(battle);
				    	if(alliance == null || alliance.getLevelActives().contains(levelConfig.getRewardFlag())){
				    	     resultMap.put(activeId, ActiveRewardStatus.UNRESERVED);
				    	     continue;
				    	}
					}
						
				    if(playerActive.getLevelActives().contains(levelConfig.getRewardFlag())){//判断是否领取过奖励
				    	resultMap.put(activeId, ActiveRewardStatus.REWARDED);
				    }else{
				    	int condition = activeLevelService.isCondition(userDomain, levelConfig) == true ? ActiveRewardStatus.RESERVED : ActiveRewardStatus.UNRESERVED;
				    	resultMap.put(activeId, condition);
				    }
				    
				}
			}
			
			if(type == ActiveOperatorType.RANKING){ //排行类型是否可以领取
				ActiveOperatorRankConfig rankConfig = activeOperatorManager.getActiveOperatorRankConfig(activeId);
				if(rankConfig == null){
					resultMap.put(activeId, ActiveRewardStatus.UNRESERVED);
				}else{
					int activeBaseId = rankConfig.getActiveBaseId();//排行活动是只要领取过一次无论任何名次，下次都无法再次领取
					if(playerActive.getRankActives().contains(activeBaseId)){ //判断是否领取过
						resultMap.put(activeId, ActiveRewardStatus.REWARDED);
					}else{
				    	int condition = activeRankService.isCondition(userDomain, rankConfig, operatorActive) == true ? ActiveRewardStatus.RESERVED : ActiveRewardStatus.UNRESERVED;
				    	resultMap.put(activeId, condition);
					}
				}
			}
		}
		
		if(!resultMap.isEmpty()){
			//格式化...
			StringBuilder builder = new StringBuilder();
			for(Entry<Integer, Object> entry : resultMap.entrySet()){
				int activeId = entry.getKey();
				Object value = entry.getValue();
				builder.append(activeId).append(Splitable.ATTRIBUTE_SPLIT).append(value).append(Splitable.ELEMENT_DELIMITER);
			}
			
			if(builder.length() > 0) {
				builder.deleteCharAt(builder.length() - 1);
			}
			
			resultObject = builder.toString();
		}
		
		
		return resultObject;
	}

}
