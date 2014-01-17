package com.yayo.warriors.module.duntask.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.duntask.constant.DungeonTaskConstant.*;
import static com.yayo.warriors.module.pack.type.BackpackType.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.warriors.basedb.model.DungeonTaskConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.DunTaskPushHelper;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.dungeon.facade.DungeonFacade;
import com.yayo.warriors.module.duntask.facade.DungeonTaskFacade;
import com.yayo.warriors.module.duntask.manager.DunTaskManager;
import com.yayo.warriors.module.duntask.model.DunTask;
import com.yayo.warriors.module.duntask.types.TaskState;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.logger.helper.LoggerPropsHelper;
import com.yayo.warriors.module.logger.log.ExpLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.log.SilverLogger;
import com.yayo.warriors.module.logger.type.Orient;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.PropsStackResult;
import com.yayo.warriors.module.props.rule.PropsHelper;
import com.yayo.warriors.module.server.listener.DataRemoveListener;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.rule.AttributeRule;


/**
 * 副本任务接口实现类
 * @author liuyuhua
 */
@Component
public class DungeonTaskFacadeImpl implements DungeonTaskFacade , DataRemoveListener{
	
	@Autowired
	private DbService dbService;
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private UserManager userManager;
	@Autowired
	private DunTaskManager dunTaskManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private DungeonFacade dungeonFacade;
	@Autowired
	private DunTaskPushHelper dunTaskPushHelper;
	
	/** 日志格式 */
	private final Logger logger = LoggerFactory.getLogger(getClass());

	
	public void accept(long playerId, Collection<Integer> taskBaseIds) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			logger.error("接受副本任务,玩家[{}],接受副本任务[{}],玩家不存在",playerId,taskBaseIds);
			return;
		}
		
		if(taskBaseIds == null || taskBaseIds.isEmpty()){
			return;
		}
		
		List<DunTask> tasks = dunTaskManager.accpet(playerId, taskBaseIds);
		if(tasks != null){
			this.dunTaskPushHelper.acceptDunTask(playerId, tasks); //发送给客户端
		}
	}

	/**
	 * 领取任务奖励
	 * 
	 * @param  playerId        	玩家的ID
	 * @param  taskId          	副本任务的ID
	 * @return {@link Integer}	副本任务返回值
	 */
	@SuppressWarnings("unchecked")
	
	public int submit(long playerId, long taskId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return PLAYER_NOT_FOUND;
		}

		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		DunTask duntask = dunTaskManager.getDunTask(playerId, taskId);
		if (duntask == null) {
			return TASK_NOT_FOUND;
		} else if (duntask.getState() == TaskState.FINISHED) {
			return TASK_IS_REWARD;
		} else if (!duntask.isCompleted()) {
			return TASK_NOT_COMPLETE;
		}

		int taskBaseId = duntask.getBaseId();
		DungeonTaskConfig config = dunTaskManager.getDungeonTaskConfig(taskBaseId);
		if (config == null) {
			return FAILURE;
		}

		ResultObject<PropsStackResult> rewardResult = initDungeonRewards(battle, config);
		if (rewardResult.getResult() < SUCCESS) {
			return rewardResult.getResult();
		}

		int exp = config.getRewardExp();
		int gas = config.getRewardGas();
		int silver = config.getRewardSliver();
		PropsStackResult propsStackResult = rewardResult.getValue();
		List<UserProps> newUserProps = propsStackResult.getNewUserProps();
		Map<Long, Integer> mergePropsMap = propsStackResult.getMergeProps();
		int packSize = propsManager.getBackpackSize(playerId, DEFAULT_BACKPACK);
		List<UserProps> updateUserPropsList = null;
		Collection<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>(2);
		ChainLock lock = LockUtils.getLock(player.getPackLock(), duntask, player, battle);
		try {
			lock.lock();
			if (duntask.getState() == TaskState.FINISHED) {
				return TASK_IS_REWARD;
			} else if (!duntask.isCompleted()) {
				return TASK_NOT_COMPLETE;
			}

			if (!newUserProps.isEmpty()) {
				if (!player.canAddNew2Backpack(packSize + newUserProps.size(), DEFAULT_BACKPACK)) {
					return BACKPACK_FULLED;
				}
				newUserProps = propsManager.createUserProps(newUserProps);
				propsManager.put2UserPropsIdsList(playerId, DEFAULT_BACKPACK, newUserProps);
				backpackEntries.addAll(newUserProps);
			}

			if (!mergePropsMap.isEmpty()) {
				updateUserPropsList = propsManager.updateUserPropsList(mergePropsMap);
				backpackEntries.addAll(updateUserPropsList);
			}

			duntask.setState(TaskState.FINISHED);
			player.increaseSilver(silver);
			battle.increaseGas(gas);
			battle.increaseExp(exp);
			dbService.submitUpdate2Queue(player, battle);
			dunTaskManager.remove(playerId, taskId);
			if(exp != 0) { //记录经验日志
				ExpLogger.dungeonTaskExp(userDomain, config, exp);
			}
			
			if(silver != 0){
				SilverLogger.inCome(Source.REWARDS_TASK, silver, player);
			}
			
		} catch (Exception e) {
			return FAILURE;
		} finally {
			lock.unlock();
		}
		
		GoodsLogger.goodsLogger(player, Source.REWARDS_TASK, LoggerPropsHelper.convertLoggerGoods(Orient.INCOME, newUserProps, null, mergePropsMap, updateUserPropsList));

		// 推送属性
		List<Long> playerIdList = Arrays.asList(playerId);
		List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, AttributeRule.DUNGEON_TASK_EXP_ARR);
		MessagePushHelper.pushUserProps2Client(playerId, DEFAULT_BACKPACK, false, backpackEntries);
		return SUCCESS;
	}

	/**
	 * 初始化副本奖励信息
	 * 
	 * @param  player					角色对象
	 * @param  config					地下城配置对象
	 * @return {@link ResultObject}		返回值信息
	 */
	private ResultObject<PropsStackResult> initDungeonRewards(PlayerBattle battle, DungeonTaskConfig config) {
		Map<Integer, Integer> rewardItems = config.getRewardItems();
		PropsStackResult propsStackResult = PropsStackResult.valueOf();
		if(rewardItems == null || rewardItems.isEmpty()) {
			return ResultObject.SUCCESS(propsStackResult);
		}
		
		long playerId = battle.getId();
		for (Entry<Integer, Integer> entry : rewardItems.entrySet()) {
			int baseId = entry.getKey();
			int itemCount = entry.getValue();
			PropsConfig propsConfig = propsManager.getPropsConfig(baseId);
			if (propsConfig == null) {
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			}
			
			PropsStackResult propsStack = PropsHelper.calcPropsStack(playerId, 
									DEFAULT_BACKPACK, baseId, itemCount, true);
			propsStackResult.getMergeProps().putAll(propsStack.getMergeProps());
			propsStackResult.getNewUserProps().addAll(propsStack.getNewUserProps());
		}
		return ResultObject.SUCCESS(propsStackResult);
	}
	
	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		long playerId = messageInfo.getPlayerId();
		PlayerDungeon playerDungeon = dungeonFacade.getPlayerDungeon(playerId);
		if(playerDungeon != null){
			if(!playerDungeon.isDungeonStatus()){
				dunTaskManager.removeAll(playerId);
			}
		}
	}
	
//	/**
//	 * 
//	 * @param playerId
//	 * @param rewardItems
//	 */
//	private void rewardsGoods(long playerId,Collection<DungeonTaskProps> rewardItems){
//		if(rewardItems != null && !rewardItems.isEmpty()){
//			for(DungeonTaskProps taskPros : rewardItems){
//				int count = taskPros.getCount();
//				int propsId = taskPros.getPropsId();
//				ResultObject<Collection<BackpackEntry>> result = taskRewardsProps(playerId, propsId, count);
//				if(result.getResult() == SUCCESS){
//					MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, result.getValue());
//				}
//			}
//		}
//	}
//	
//	/**
//	 * 奖励物品
//	 * @param playerId
//	 * @param propsId
//	 * @param count
//	 * @return
//	 */
//	private ResultObject<Collection<BackpackEntry>> taskRewardsProps(long playerId,int propsId, int count) {
//		PropsConfig propsConfig = propsManager.getPropsConfig(propsId);
//		if (propsConfig == null) {
//			return ResultObject.ERROR(ITEM_NOT_FOUND);
//		}
//		int maxAmount = propsConfig.getMaxAmount();
//		PropsStackResult propsStack = PropsHelper.calcPropsStack(playerId,DEFAULT_BACKPACK, propsId, count, true);
//		Map<Long, Integer> mergeProps = propsStack.getMergeProps();
//		List<UserProps> newUserProps = propsStack.getNewUserProps();
//		Collection<BackpackEntry> saveUserProps = saveUserProps(playerId, maxAmount, newUserProps,mergeProps);
//		return ResultObject.SUCCESS(saveUserProps);
//	}
	
//	/**
//	 * 保存购买的物品信息
//	 * @param playerId      角色ID
//	 * @param maxCount      物品的最大堆叠
//	 * @param newUserProps  新创建的道具列表
//	 * @param mergeProps    合并的道具列表
//	 * @return {@link List} 背包实体列表
//	 */
//	private List<BackpackEntry> saveUserProps(long playerId, int maxCount,
//			List<UserProps> newUserProps, Map<Long, Integer> mergeProps) {
//		List<BackpackEntry> backpackEntry = new ArrayList<BackpackEntry>();
//		//TODO 这里需要重新查看一下
//		if (newUserProps != null && !newUserProps.isEmpty()) {
//			propsManager.createUserProps(newUserProps);
//			propsManager.put2UserPropsIdsList(playerId, DEFAULT_BACKPACK, newUserProps);
//			backpackEntry.addAll(voFactory.getUserPropsEntries(newUserProps));
////			propsManager.removeUserPropsIdList(playerId, DEFAULT_BACKPACK);
//		}
//
//		if (mergeProps != null && !mergeProps.isEmpty()) {
//			for (Entry<Long, Integer> entry : mergeProps.entrySet()) {
//				Long userPropsId = entry.getKey();
//				Integer addPropsCount = entry.getValue();
//				if (userPropsId == null || addPropsCount == null) {
//					continue;
//				}
//
//				UserProps userProps = propsManager.getUserProps(userPropsId);
//				ChainLock lock = LockUtils.getLock(userProps);
//				try {
//					lock.lock();
//					int currentCount = userProps.getCount();
//					userProps.setCount(Math.min(maxCount, currentCount + addPropsCount));
//				} finally {
//					lock.unlock();
//				}
//
//				this.dbService.submitUpdate2Queue(userProps);
//				backpackEntry.add(voFactory.getUserPropsEntry(userProps));
//			}
//		}
//		return backpackEntry;
//	}

	
	public Collection<DunTask> getAllDunTask(long playerId) {
		return dunTaskManager.getAllDunTask(playerId);
	}

}
