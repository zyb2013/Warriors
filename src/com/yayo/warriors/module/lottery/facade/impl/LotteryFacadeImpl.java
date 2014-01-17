package com.yayo.warriors.module.lottery.facade.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.warriors.basedb.model.BulletinConfig;
import com.yayo.warriors.basedb.model.EquipConfig;
import com.yayo.warriors.basedb.model.LotteryConfig;
import com.yayo.warriors.basedb.model.LotteryGridRateConfig;
import com.yayo.warriors.basedb.model.LotteryPropsRateConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.NoticePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.logger.helper.LoggerPropsHelper;
import com.yayo.warriors.module.logger.log.GoldLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Orient;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.lottery.constant.LotteryConstant;
import com.yayo.warriors.module.lottery.facade.LotteryFacade;
import com.yayo.warriors.module.lottery.vo.LotteryRewardVo;
import com.yayo.warriors.module.lottery.vo.LotteryVO;
import com.yayo.warriors.module.notice.rule.NoticeRule;
import com.yayo.warriors.module.notice.type.NoticeID;
import com.yayo.warriors.module.notice.type.NoticeType;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.module.props.model.PropsStackResult;
import com.yayo.warriors.module.props.rule.EquipHelper;
import com.yayo.warriors.module.props.rule.PropsHelper;
import com.yayo.warriors.module.server.listener.DataRemoveListener;
import com.yayo.warriors.module.server.listener.LogoutListener;
import com.yayo.warriors.module.task.facade.TaskFacade;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.type.GoodsType;
import com.yayo.warriors.type.IndexName;

@Component
public class LotteryFacadeImpl implements LotteryFacade, LogoutListener,DataRemoveListener {
	@Autowired
	private TaskFacade taskFacade;
	@Autowired
	private DbService cachedService ;
	@Autowired
	private UserManager userManager;
	@Autowired
	private ResourceService resourceService; 
	@Autowired
	private PropsManager propsManager ;
	@Autowired
	private VOFactory voFactory;
	
	/** 日志 */
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	/** 抽奖背包大小 */
	private final static int BACK_PACK_MAX_SIZE = 150 ;

	/** 全服抽奖历史大小  */
	private final static int GLOBAL_LOTTERY_HISTORY_SIZE = 20;
	
	/** 角色抽奖历史大小  */
	private final static int PLAYER_LOTTERY_HISTORY_SIZE = 50;
	
	/** 全服标识 */
	private final static long GLOBAL_FLAG = -1L;
	
	/** 玩家抽奖记录 , key:>0表示玩家,-1表示全服 */
	private final ConcurrentHashMap<Long, CopyOnWriteArrayList<LotteryVO>> cacheRewardMap = new ConcurrentHashMap<Long, CopyOnWriteArrayList<LotteryVO>>(5);
	private final ConcurrentMap<Long, Boolean> playerCacheMap = new ConcurrentHashMap<Long, Boolean>();
	
	
	public int doLottery(Long playerId , int lotteryId, boolean autoBuy, Map<String,Object> resultMap) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return LotteryConstant.PLAYER_NOT_FOUND;
		}
		
		LotteryConfig lotteryConfig = resourceService.get(lotteryId, LotteryConfig.class);
		if(lotteryConfig == null){
			return LotteryConstant.BASEDATA_NOT_FOUND;
		}
		
		PlayerBattle playerBattle = userDomain.getBattle();
		//等级不够
		if(!lotteryConfig.isLevelToLottery(playerBattle.getLevel())){
			return LotteryConstant.LEVEL_INVALID;
		}
		Player player = userDomain.getPlayer() ;
		
		int currStoreSize = propsManager.getBackpackSize(playerId, BackpackType.LOTTERY_BACKPACK);
		if(currStoreSize >= BACK_PACK_MAX_SIZE ){
			return LotteryConstant.LOTTERY_BACKPACK_CAPACITY_LACK;
		}
		
		int propsId = lotteryConfig.getPropsId();
		PropsConfig propsConfig = resourceService.get(propsId, PropsConfig.class);
		if(propsConfig == null){
			return LotteryConstant.BASEDATA_NOT_FOUND;
		}
		
		Map<Long, Integer> costUserItems = new HashMap<Long, Integer>(2);
		
		Map<Integer, Integer[]> newPropsMap = new HashMap<Integer, Integer[]>();
		List<UserEquip> newUserEquips = new ArrayList<UserEquip>();
		Map<Long, Integer> mergeProps = new HashMap<Long, Integer>();
		List<UserProps> newUserPropsList = new ArrayList<UserProps>();
		List<BackpackEntry> resultList = new ArrayList<BackpackEntry>();
		List<UserProps> updateUserPropsList = null;
		
		List<UserProps> costUserPropsList = null;
		
		int totalUseGold = 0;
		ChainLock lock = LockUtils.getLock(player, userDomain.getPackLock());
		try {
			lock.lock();
	 		//等级不够
			if(!lotteryConfig.isLevelToLottery(playerBattle.getLevel())){
				return LotteryConstant.LEVEL_INVALID;
			}
			
			ResultObject<Integer> resultObject = calcCostProps(player, propsConfig, lotteryConfig.getNum(), autoBuy, costUserItems);
			if( !resultObject.isOK()){
				return resultObject.getResult();
			}
			totalUseGold = resultObject.getValue();
			
			//抽奖（仅计算）
			calcLottery(playerId, lotteryConfig, newPropsMap, newUserEquips, mergeProps, newUserPropsList);
			
			if(currStoreSize + newUserEquips.size() + newUserPropsList.size() > BACK_PACK_MAX_SIZE){
				return LotteryConstant.LOTTERY_BACKPACK_CAPACITY_LACK;
			}
			
			//入库
			propsManager.createUserEquipAndUserProps(newUserPropsList, newUserEquips);
			updateUserPropsList = propsManager.updateUserPropsList(mergeProps);
			
			costUserPropsList = propsManager.costUserPropsList(costUserItems);
			resultList.addAll(newUserEquips);
			resultList.addAll(newUserPropsList);
			resultList.addAll(updateUserPropsList);
			
			//入缓存
			propsManager.put2UserEquipIdsList(playerId, BackpackType.LOTTERY_BACKPACK, newUserEquips);
			propsManager.put2UserPropsIdsList(playerId, BackpackType.LOTTERY_BACKPACK, newUserPropsList);
			
			player.decreaseGolden(totalUseGold);
			
		} finally {
			lock.unlock();
		}
		this.playerCacheMap.clear();
		
		LotteryVO vo = handlerRewards(player, lotteryId, newPropsMap, newUserEquips);
		
		recordHistory(userDomain, vo);		//记录抽奖历史
		resultMap.put(ResponseKey.DATA, vo);
		
		LoggerGoods[] loggerGoodsArray = LoggerPropsHelper.convertLoggerGoods(Orient.INCOME, newUserPropsList, newUserEquips, mergeProps, updateUserPropsList);
		if(totalUseGold != 0){
			cachedService.submitUpdate2Queue(player);
			List<Long> playerIdList = Arrays.asList(playerId);
			List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
			UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, AttributeKeys.GOLDEN);
			
			LoggerGoods loggerGoods = LoggerGoods.outcomePropsAutoBuyGolden(propsId, totalUseGold / propsConfig.getMallPrice(), totalUseGold);
			GoldLogger.outCome(Source.PROPS_DO_LOTTERY, totalUseGold, player, loggerGoods);	//金币消耗日志
			loggerGoodsArray = (LoggerGoods[])ArrayUtils.add(loggerGoodsArray, loggerGoods);
		}
		
		
		MessagePushHelper.pushUserProps2Client(playerId, BackpackType.LOTTERY_BACKPACK, false, resultList);
		if(costUserPropsList != null && costUserPropsList.size() > 0){
			MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, voFactory.getUserPropsEntries(costUserPropsList) );
			
			Collection<GoodsVO> goodsVOs = GoodsVO.valuleOf(Orient.OUTCOME, null, costUserPropsList, costUserItems, null);
			MessagePushHelper.pushGoodsCountChange2Client(playerId, goodsVOs);
			
			LoggerGoods[] costLoggerGoods = LoggerPropsHelper.convertLoggerGoods(Orient.OUTCOME, null, null, costUserItems, costUserPropsList);
			loggerGoodsArray = (LoggerGoods[])ArrayUtils.addAll(loggerGoodsArray, costLoggerGoods);
		}
		
		GoodsLogger.goodsLogger(player, Source.PROPS_DO_LOTTERY, loggerGoodsArray);
		
		taskFacade.updateLettoryTask(playerId, lotteryConfig.getTimes());
		return LotteryConstant.SUCCESS ;
	}
	
	/**
	 * 计算物品消耗和金币消耗
	 * @param playerId
	 * @param costMap
	 * @return
	 */
	private ResultObject<Integer> calcCostProps(Player player, PropsConfig propsConfig, int num, boolean autoBuy, Map<Long, Integer> costMap){
		if(num <= 0){
			return ResultObject.SUCCESS(0);
		}
		Long playerId = player.getId();
		List<UserProps> userPropsList = propsManager.listUserPropByBaseId(playerId, propsConfig.getId(), BackpackType.DEFAULT_BACKPACK);
		int costItems = num;
		if(userPropsList != null && userPropsList.size() > 0){
			for(UserProps userProps : userPropsList){
				int count = userProps.getCount();
				if(count <= 0){
					continue;
				}
				int realCost = Math.min(costItems, count);
				costItems -= realCost;
				costMap.put(userProps.getId(), realCost);
				if(costItems <= 0){
					return ResultObject.SUCCESS(0);
				}
			}
		}
		
		if(autoBuy && costItems > 0){
			int costGolden = propsConfig.getMallPriceByCount(costItems);
			if(player.getGolden() < costGolden){
				return ResultObject.ERROR(LotteryConstant.GOLDEN_NOT_ENOUGH);
			}
			return ResultObject.SUCCESS(costGolden);
		}
		
		return ResultObject.ERROR(LotteryConstant.ITEM_NOT_ENOUGH);
	}

	/**
	 * 处理抽奖奖励
	 * @param player
	 * @param newPropsMap
	 * @param newUserEquips
	 * @return
	 */
	private LotteryVO handlerRewards(Player player, int lotteryId, Map<Integer, Integer[]> newPropsMap, List<UserEquip> newUserEquips) {
		Long playerId = player.getId();
		String playerName = player.getName();
		List<LotteryRewardVo> values = new ArrayList<LotteryRewardVo>( newPropsMap.size() + newUserEquips.size() );
		Set<Integer> keySet = newPropsMap.keySet();
		for(int propId :keySet){
			PropsConfig propsConfig = resourceService.get(propId, PropsConfig.class);
			
			BulletinConfig config = resourceService.get(NoticeID.LOTTERY_PROPS, BulletinConfig.class);
			if (config != null && config.getConditions().contains(propId)) {
				HashMap<String, Object> paramsMap = new HashMap<String, Object>();
				paramsMap.put(NoticeRule.props, propsConfig.getName());
				paramsMap.put(NoticeRule.playerName, playerName);
				NoticePushHelper.pushNotice(NoticeID.LOTTERY_PROPS, NoticeType.HONOR, paramsMap, config.getPriority());
			}
			
			Integer[] counts = newPropsMap.get(propId);
			if(counts[0] > 0){
				LotteryRewardVo rVO = new LotteryRewardVo(propId, GoodsType.PROPS, counts[0], false);
				values.add(rVO);
			}
			if(counts[1] > 0){
				LotteryRewardVo rVO = new LotteryRewardVo(propId, GoodsType.PROPS, counts[1], true);
				values.add(rVO);
			}
		}
		
		for(UserEquip userEquip :newUserEquips){
			EquipConfig equipConfig = resourceService.get(userEquip.getBaseId(), EquipConfig.class);
			
			BulletinConfig config = resourceService.get(NoticeID.LOTTERY_EQUIP, BulletinConfig.class);
			HashMap<String, Object> paramsMap = new HashMap<String, Object>(3);
			paramsMap.put(NoticeRule.equipId, equipConfig.getName());
			paramsMap.put(NoticeRule.playerName, playerName);
			paramsMap.put(NoticeRule.playerId, playerId);
			if (config != null) NoticePushHelper.pushNotice(NoticeID.LOTTERY_EQUIP, NoticeType.HONOR, paramsMap, config.getPriority());
			
			LotteryRewardVo rVO = new LotteryRewardVo(userEquip.getBaseId(), GoodsType.EQUIP, userEquip.getCount(), userEquip.isBinding());
			values.add(rVO);
		}
		
		return LotteryVO.valueOf(playerId, playerName, lotteryId,values);
	}

	/**
	 * 抽奖
	 * @param playerId
	 * @param lotteryConfig
	 * @param newPropsMap
	 * @param newUserEquips
	 * @param mergeProps
	 * @param newUserPropsList
	 */
	private void calcLottery(Long playerId, LotteryConfig lotteryConfig,
			Map<Integer, Integer[]> newPropsMap, List<UserEquip> newUserEquips,
			Map<Long, Integer> mergeProps, List<UserProps> newUserPropsList) {
		
		List<LotteryGridRateConfig> grids = resourceService.listByIndex(IndexName.LOTTERYGRIDRATE_CONFIGID, LotteryGridRateConfig.class, lotteryConfig.getId());
		//一次或多次抽奖
		for(int i = 0 ; i < lotteryConfig.getTimes() ; i ++){
			LotteryGridRateConfig gridRateConfig = lotteryConfig.rollGird(grids);
			List<LotteryPropsRateConfig> propsRolls = resourceService.listByIndex(IndexName.LOTTERY_PROPS_ROLL_ID, LotteryPropsRateConfig.class, gridRateConfig.getPropsRollId() );
			Collections.shuffle(propsRolls);
			LotteryPropsRateConfig propsRateConfig = gridRateConfig.rollProps(propsRolls);
			
			List<LotteryRewardVo> lotteryRewardVos = propsRateConfig.obtainLotteryPropsList();
			for(LotteryRewardVo lotteryRewardVo : lotteryRewardVos ){
				int propId = lotteryRewardVo.getPropId();
				if(lotteryRewardVo.propsType()){	//道具
					Integer[] hasNum = newPropsMap.get(propId);
					if(hasNum == null){
						hasNum = new Integer[]{0, 0};
					}
					hasNum[lotteryRewardVo.isBanding() ? 1 : 0] += lotteryRewardVo.getNum();
					newPropsMap.put(propId, hasNum);
					
				} else {	//装备
					UserEquip userEquip = EquipHelper.newUserEquip(playerId, BackpackType.LOTTERY_BACKPACK, propId, lotteryRewardVo.isBanding() );
					if(newUserEquips != null){
						newUserEquips.add(userEquip);
					}
					else {
						log.error("基础装备不存在,id:[{}]", propId );
					}
				}
				
			}
		}
		
		//合并道具
		for(Integer propsId : newPropsMap.keySet()){
			Integer[] counts = newPropsMap.get(propsId);
			PropsConfig propsConfig = propsManager.getPropsConfig(propsId);
			if (propsConfig == null) {
				log.error("基础道具不存在,id:{}", propsId);
				continue;
			}
			if(counts[0] > 0){
				PropsStackResult propsStack = PropsHelper.calcPropsStack(playerId, BackpackType.LOTTERY_BACKPACK, propsId, counts[0], false);
				newUserPropsList.addAll(propsStack.getNewUserProps());
				mergeProps.putAll(propsStack.getMergeProps());
			}
			if(counts[1] > 0){
				PropsStackResult propsStack = PropsHelper.calcPropsStack(playerId, BackpackType.LOTTERY_BACKPACK, propsId, counts[1], true);
				newUserPropsList.addAll(propsStack.getNewUserProps());
				mergeProps.putAll(propsStack.getMergeProps());
			}
		}
	}
	
	/**
	 * 记录抽奖历史
	 * @param userDomain
	 * @param rewardResults
	 */
	private void recordHistory(UserDomain userDomain, LotteryVO lotteryVO){
		long playerId = userDomain.getPlayerId();
		//玩家的抽奖历史
		CopyOnWriteArrayList<LotteryVO> playerHistory = cacheRewardMap.get(playerId);
		if(playerHistory == null){
			playerHistory = new CopyOnWriteArrayList<LotteryVO>();
			cacheRewardMap.putIfAbsent(playerId, playerHistory);
			playerHistory = cacheRewardMap.get(playerId);
		}
		synchronized (playerHistory) {
			playerHistory.add(lotteryVO);
			if(playerHistory.size() > PLAYER_LOTTERY_HISTORY_SIZE){
				playerHistory.remove(0);
			}
		}
		
		//全服的抽奖历史
		CopyOnWriteArrayList<LotteryVO> globleHistory = cacheRewardMap.get( GLOBAL_FLAG );
		if(globleHistory == null){
			globleHistory = new CopyOnWriteArrayList<LotteryVO>();
			cacheRewardMap.putIfAbsent( GLOBAL_FLAG, globleHistory);
			globleHistory = cacheRewardMap.get( GLOBAL_FLAG );
		}
		synchronized (globleHistory) {
			globleHistory.add(lotteryVO);
			if(globleHistory.size() > GLOBAL_LOTTERY_HISTORY_SIZE){
				globleHistory.remove(0);
			}
		}
		
	}
	
	
	public Map<String,Object> lotteryCacheMsg(long playerId, boolean force){
		Map<String,Object> result = new HashMap<String, Object>(2);
		if( force || !this.playerCacheMap.containsKey(playerId) ) {
			this.playerCacheMap.put(playerId, true);
			CopyOnWriteArrayList<LotteryVO> myLottery = cacheRewardMap.get(playerId);
			if(myLottery != null){
				result.put("myLotteryInfo", myLottery.toArray());
			}
			
			CopyOnWriteArrayList<LotteryVO> globleLottery = cacheRewardMap.get( GLOBAL_FLAG );
			if(globleLottery != null){
				result.put("allLotteryInfo", globleLottery.toArray());
			}
//			log.error("玩家:[{}]来取抽奖历史记录", playerId);
		}
		
		return result ;
	}
	
	
	public int checkoutAllFromLotteryStorage(long playerId){
		int backpack = BackpackType.DEFAULT_BACKPACK;
		int lotteryBackpack = BackpackType.LOTTERY_BACKPACK;
		List<UserProps> userPropsList = propsManager.listUserProps(playerId, lotteryBackpack);
		List<UserEquip> userEquipList = propsManager.listUserEquip(playerId, lotteryBackpack);
		return propsManager.transferPackage(playerId, userPropsList, userEquipList, lotteryBackpack, backpack);
	}
	
	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		this.cacheRewardMap.remove(messageInfo.getPlayerId());	//清除玩家的抽奖历史
	}

	
	public void onLogoutEvent(UserDomain userDomain) {
		if(userDomain != null){
			this.playerCacheMap.remove(userDomain.getPlayerId());
		}
	}
	
}
