package com.yayo.warriors.module.task.facade.Impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.pack.type.BackpackType.*;
import static com.yayo.warriors.module.task.constant.TaskConstant.*;
import static com.yayo.warriors.module.vip.model.VipFunction.*;

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
import com.yayo.common.utility.EnumUtils;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.adapter.OnlineActiveService;
import com.yayo.warriors.basedb.model.BulletinConfig;
import com.yayo.warriors.basedb.model.EscortTaskConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.NoticePushHelper;
import com.yayo.warriors.common.helper.TaskPushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.horse.entity.Horse;
import com.yayo.warriors.module.horse.manager.HorseManager;
import com.yayo.warriors.module.logger.helper.LoggerPropsHelper;
import com.yayo.warriors.module.logger.log.ExpLogger;
import com.yayo.warriors.module.logger.log.GoldLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Orient;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.facade.MonsterFacade;
import com.yayo.warriors.module.notice.rule.NoticeRule;
import com.yayo.warriors.module.notice.type.NoticeID;
import com.yayo.warriors.module.notice.type.NoticeType;
import com.yayo.warriors.module.npc.facade.NpcFacade;
import com.yayo.warriors.module.npc.model.Npc;
import com.yayo.warriors.module.onhook.facade.TrainFacade;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.module.props.type.PropsChildType;
import com.yayo.warriors.module.props.type.Quality;
import com.yayo.warriors.module.task.entity.UserEscortTask;
import com.yayo.warriors.module.task.facade.AllianceTaskFacade;
import com.yayo.warriors.module.task.facade.EscortTaskFacade;
import com.yayo.warriors.module.task.manager.EscortTaskManager;
import com.yayo.warriors.module.task.model.RewardVO;
import com.yayo.warriors.module.task.rule.EscortTaskRule;
import com.yayo.warriors.module.task.rule.TaskRule;
import com.yayo.warriors.module.task.type.TaskStatus;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.module.vip.manager.VipManager;
import com.yayo.warriors.module.vip.model.VipDomain;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.type.ElementType;
import com.yayo.warriors.type.GoodsType;

@Component
public class EscortTaskFacadeImpl implements EscortTaskFacade {
	
	@Autowired
	private NpcFacade npcFacade;
	@Autowired
	private DbService dbService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private EscortTaskManager escortManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	MonsterFacade monsterFacade;
	@Autowired
	HorseManager horseManager;
	@Autowired
	AllianceTaskFacade allianceTaskFacade;
	@Autowired
	private VipManager vipManager;
	@Autowired
	private TrainFacade trainFacade;
	@Autowired
	private OnlineActiveService onlineActiveService;
	
	
	
	public ResultObject<UserEscortTask> loadUserEscortTask(long playerId,boolean flushable) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		PlayerBattle battle = userDomain.getBattle();
		UserEscortTask userEscortTask = escortManager.getEscortTask(battle);
		if(userEscortTask == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		int quality = escortManager.getRandomEscortTaskQuality();
		ChainLock lock = LockUtils.getLock(userEscortTask);
			try {
				lock.lock();
				userEscortTask.updateEscortTimes(quality);
			}finally{
				lock.unlock();
			}
		dbService.submitUpdate2Queue(userEscortTask);
		
		return ResultObject.SUCCESS(userEscortTask);
	}

	
	public ResultObject<UserEscortTask> giveup(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		PlayerBattle battle = userDomain.getBattle();
		UserEscortTask userEscortTask = escortManager.getEscortTask(battle);
		if(userEscortTask == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(!userEscortTask.canGiveup()){
			return ResultObject.ERROR(TASK_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		int quality = escortManager.getRandomEscortTaskQuality();
		ChainLock lock = LockUtils.getLock(userEscortTask,battle,player);
		try {
			lock.lock();
			if(!userEscortTask.canGiveup()){
				return ResultObject.ERROR(TASK_NOT_FOUND);
			}
			userEscortTask.giveupTask(quality);
			battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
			player.setProtection(false);
		}finally{
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(userEscortTask);
		this.pushEscortRiding(userDomain, userEscortTask);
		return ResultObject.SUCCESS(userEscortTask);
	}
	
	
	@SuppressWarnings("unchecked")
	public ResultObject<UserEscortTask> accept(long playerId,int taskId,long propsId,boolean autoBuy) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(trainFacade.isTrainStatus(playerId) > 0){
			return ResultObject.ERROR(TRAIN_STATE);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		UserEscortTask userEscortTask = escortManager.getEscortTask(battle);
		if(userEscortTask == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		EscortTaskConfig config = escortManager.getEscortTaskConfig(taskId);
		if(config == null){
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle playerBattle = userDomain.getBattle();
		if(playerBattle.getLevel() > config.getMaxLevel() || playerBattle.getLevel() < config.getMinLevel()){
			return ResultObject.ERROR(LEVEL_INVALID);
		}
		
		if(config.getCamp() != 0){
			if(config.getCamp() != player.getCamp().ordinal()){
				return ResultObject.ERROR(TASK_CAMP_ERR);
			}
		}
		
		Npc npc = npcFacade.getNpc(config.getAcceptNpc());
		if(npc == null || npc.getMapId() != userDomain.getMapId()){
			return ResultObject.ERROR(MAP_POSITION_ERR);
		}
		
		
		int quality = escortManager.getRandomEscortTaskQuality();
		
		int protectionCount = EscortTaskRule.USE_ESCORT_PROTECTION_COUNT;
		if(autoBuy){ 
			int protectionPropsid = EscortTaskRule.ESCORT_PROTECTION_PROPSID;
			PropsConfig propsConfig = propsManager.getPropsConfig(protectionPropsid);
			if(propsConfig == null){
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			}
			
			int needGolden = propsConfig.getMallPriceByCount(protectionCount);
			ChainLock lock = LockUtils.getLock(player.getPackLock(),userEscortTask,battle);
			try {
				lock.lock();
				if(player.getGolden() < needGolden){
					return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
				}
				
				userEscortTask.updateEscortTimes(quality); 
				if(!userEscortTask.canAccept()){
					return ResultObject.ERROR(DUPLICATE_ACCEPT_TASK);
				}
				
				if(userEscortTask.getActionTimes() >= EscortTaskRule.MAX_ACCPET_TIMES_DAY){
					return ResultObject.ERROR(MAX_COUNT_INVALID);
				}
				
				player.decreaseGolden(needGolden);
				userEscortTask.acceptTask(battle.getLevel(),taskId,config.getLimitTime());
				userEscortTask.setProtection(true);
				battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
			}finally{
				lock.unlock();
			}
			
			LoggerGoods loggerGoods = LoggerGoods.outcomePropsAutoBuyGolden( protectionPropsid, protectionCount, needGolden);
			GoldLogger.outCome(Source.ESCORT_PROTECTION, needGolden, player, loggerGoods);
			dbService.submitUpdate2Queue(userEscortTask,player);
			List<Long> playerIdList = Arrays.asList(playerId); 
			List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
			UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList,AttributeKeys.GOLDEN);
		}
		
		
		if(propsId > 0) { 
			UserProps userProps = propsManager.getUserProps(propsId);
			if (userProps == null) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			}

			if (userProps.getPlayerId() != playerId) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			} else if (!userProps.validBackpack(DEFAULT_BACKPACK)) {
				return ResultObject.ERROR(BACKPACK_INVALID);
			} else if (userProps.getCount() < protectionCount) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			} else if(userProps.isTrading()) {
				return ResultObject.ERROR(ITEM_CANNOT_USE);
			}
			
			PropsConfig propsConfig = userProps.getPropsConfig();
			if (propsConfig == null) {
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			} else if (propsConfig.getChildType() != PropsChildType.ESCORT_PROTECTION_TYPE) {
				return ResultObject.ERROR(BELONGS_INVALID);
			}
			
			ChainLock lock = LockUtils.getLock(player.getPackLock(),userEscortTask);
			try {
				lock.lock();
				if (userProps.getCount() < protectionCount) {
					return ResultObject.ERROR(ITEM_NOT_ENOUGH);
				}
				
				userEscortTask.updateEscortTimes(quality); 
				if(!userEscortTask.canAccept()){
					return ResultObject.ERROR(DUPLICATE_ACCEPT_TASK);
				}
				
				if(userEscortTask.getActionTimes() >= EscortTaskRule.MAX_ACCPET_TIMES_DAY){
					return ResultObject.ERROR(MAX_COUNT_INVALID);
				}
				
				userProps.decreaseItemCount(protectionCount);
				if(userProps.getCount() <= 0){
					propsManager.put2UserPropsIdsList(playerId, BackpackType.DROP_BACKPACK, userProps);
					propsManager.removeFromUserPropsIdsList(playerId, userProps.getBackpack(), userProps);
				}
				userEscortTask.acceptTask(battle.getLevel(),taskId,config.getLimitTime());
				userEscortTask.setProtection(true);
				battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
			}finally{
				lock.unlock();
			}
			
			GoodsLogger.goodsLogger(player, Source.ESCORT_PROTECTION, LoggerGoods.outcomeProps(propsId, propsConfig.getId(), protectionCount));
			MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, userProps); 
			dbService.submitUpdate2Queue(userProps,userEscortTask);
		}
		
		
		if(propsId == 0 && autoBuy == false){ 
			ChainLock lock = LockUtils.getLock(userEscortTask);
			try {
				lock.lock();
				userEscortTask.updateEscortTimes(quality); 
				if(!userEscortTask.canAccept()){
					return ResultObject.ERROR(DUPLICATE_ACCEPT_TASK);
				}
				if(userEscortTask.getActionTimes() >= EscortTaskRule.MAX_ACCPET_TIMES_DAY){
					return ResultObject.ERROR(MAX_COUNT_INVALID);
				}
				
				userEscortTask.acceptTask(battle.getLevel(),taskId,config.getLimitTime());
				userEscortTask.setProtection(false);
				battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
			}finally{
				lock.unlock();
			}
			
			dbService.submitUpdate2Queue(userEscortTask);
		}
		
		this.pushEscortRiding(userDomain, userEscortTask);
		return ResultObject.SUCCESS(userEscortTask);
	}
	
	/**
	 * 推送押镖任务骑乘
	 * @param userDomain         玩家的域对象
	 * @param userEscortTask     押镖对象
	 */
	private void pushEscortRiding(UserDomain userDomain,UserEscortTask userEscortTask){
		if(userEscortTask == null || userDomain == null){
			return;
		}
		
		long playerId = userDomain.getId();
		PlayerBattle battle = userDomain.getBattle();
		GameMap gameMap = userDomain.getGameMap();
		if(gameMap != null){
			Collection<Long> playerlist = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
			boolean riding = false; 
			int falg = 0;          
			int mount = 0;          
			Horse horse = horseManager.getHorse(battle);
			if(horse != null && horse.isRiding()){
				falg = 1;      
				riding = true; 
				mount = horse.getModel();
			}
			
			if(escortManager.isEscortStatus(battle)){
				falg = 2;      
				riding = true; 
				mount = escortManager.getEscortMount(battle); 
			}
			
			int speed = escortManager.getMoveSpeed(battle) + battle.getAttribute(AttributeKeys.MOVE_SPEED);
			TaskPushHelper.pushEscortRiding(playerId, mount, speed, riding, falg, playerlist);
		}
	}


	
	public ResultObject<UserEscortTask> updateEscortTask(long playerId,int npcId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		PlayerBattle battle = userDomain.getBattle();
		UserEscortTask userEscortTask = escortManager.getEscortTask(battle);
		if(userEscortTask == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(userEscortTask.getStatus() == TaskStatus.UNACCEPT){
			return ResultObject.ERROR(TASK_NOT_FOUND);
		}
		
		if(userEscortTask.getStatus() == TaskStatus.COMPLETED){
			return ResultObject.ERROR(TASK_COMPLETED);
		}
		
		if(userEscortTask.getStatus() == TaskStatus.FAILED){
			return ResultObject.ERROR(TASK_FAILED);
		}
		
		EscortTaskConfig config = escortManager.getEscortTaskConfig(userEscortTask.getTaskId());
		if(config == null){
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		
		if(npcId != config.getCompleteNpc()){
			return ResultObject.ERROR(TASK_CONDITION_ERR);
		}
		
		Npc npc = npcFacade.getNpc(config.getCompleteNpc());
		if(npc == null || npc.getMapId() != userDomain.getMapId()){
			return ResultObject.ERROR(MAP_POSITION_ERR);
		}
		
		ChainLock lock = LockUtils.getLock(userEscortTask);
		try {
			lock.lock();
			if(userEscortTask.getStatus() == TaskStatus.UNACCEPT){
				return ResultObject.ERROR(TASK_NOT_FOUND);
			}
			
			if(userEscortTask.getStatus() == TaskStatus.COMPLETED){
				return ResultObject.ERROR(TASK_COMPLETED);
			}
			
			if(userEscortTask.getStatus() == TaskStatus.FAILED){
				return ResultObject.ERROR(TASK_FAILED);
			}
			
			if(userEscortTask.isTimeOut()){ 
				userEscortTask.failed();
			}else{
				userEscortTask.complete();
			}
			
		}finally{
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(userEscortTask);
		return ResultObject.SUCCESS(userEscortTask);
	}
	
	
	public void updateMonsterPlunderEscort(long monsterId, long targetId) {
		UserDomain userDomain = userManager.getUserDomain(targetId);
		MonsterDomain monsterDomain = monsterFacade.getMonsterDomain(monsterId);
		if(userDomain == null || monsterDomain == null){
			return;
		}
		
		if(userDomain.getBattle().getLevel() < EscortTaskRule.MIN_ACCEPT_ESCORT_LEVEL){
			return;
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		UserEscortTask userEscortTask = escortManager.getEscortTask(battle);
		if(userEscortTask != null && !userEscortTask.isNoTask()){
			ChainLock lock = LockUtils.getLock(userEscortTask,player);
			try {
				lock.lock();
				player.setProtection(true);
				userEscortTask.setPlunderTimes(userEscortTask.getPlunderTimes() + 1); 
			}finally{
				lock.unlock();
			}
			dbService.submitUpdate2Queue(userEscortTask);
			TaskPushHelper.pushEscortPlunder(userDomain.getId(), targetId , monsterDomain.getMonster().getName(),ElementType.MONSTER);
		}
	}

	
	@SuppressWarnings("unchecked")
	public void updatePlayerPlunderEscort(long playerId, long targetId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(userDomain == null || targetDomain == null){
			return;
		}
		
		
		Player player = userDomain.getPlayer();
		PlayerBattle playerBattle = userDomain.getBattle();
		
		
		Player targetPlayer = targetDomain.getPlayer();
		PlayerBattle targetBattle = targetDomain.getBattle();
		UserEscortTask targetEscortTask = escortManager.getEscortTask(targetBattle);
		
		if(targetEscortTask != null && !targetEscortTask.isNoTask()){
			UserEscortTask userEscortTask = escortManager.getEscortTask(playerBattle);
			if(userEscortTask == null){
				
				ChainLock lock = LockUtils.getLock(targetEscortTask,targetPlayer);
				try {
					lock.lock();
					targetPlayer.setProtection(true);
					targetEscortTask.setPlunderTimes(targetEscortTask.getPlunderTimes() + 1); 
				}finally{
					lock.unlock();
				}
				
			}else{
				
				int rewardExp = 0;
				EscortTaskConfig config = escortManager.getEscortTaskConfig(targetEscortTask.getTaskId());
				if(config != null){
				    int escortExp = config.getExpValue(targetEscortTask.getAcceptLevel(), targetEscortTask.getQuality()); 
				    rewardExp = escortManager.caclPlunderEscortExp(escortExp);
				    rewardExp = rewardExp < 0 ? 0 : rewardExp;
				}
				
				ChainLock lock = LockUtils.getLock(targetEscortTask,userEscortTask,playerBattle,targetPlayer);
				try {
					lock.lock();
					targetPlayer.setProtection(true);
					targetEscortTask.setPlunderTimes(targetEscortTask.getPlunderTimes() + 1);
					userEscortTask.setBeplunderTimes(userEscortTask.getBeplunderTimes() + 1); 
					if(player.getCamp() != targetPlayer.getCamp()) { 
						if(userEscortTask.getBeplunderTimes() < EscortTaskRule.MAX_BEPLUNDER_TIMES){
							playerBattle.increaseExp(rewardExp); 
						}
					}
				}finally{
					lock.unlock();
				}
			}
			
			TaskPushHelper.pushEscortPlunder(targetPlayer.getId(), player.getId(), player.getName(),ElementType.PLAYER);
			dbService.submitUpdate2Queue(targetEscortTask,userEscortTask,playerBattle);
			allianceTaskFacade.updatePlunderEscort(playerId);
			
			if (player.getCamp() != targetPlayer.getCamp()) { 
				List<Long> playerIdList = Arrays.asList(player.getId());
				List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
				UserPushHelper.pushAttribute2AreaMember(player.getId(), playerIdList, unitIdList, AttributeKeys.EXP);
			
			
				BulletinConfig bulletinConfig = NoticePushHelper.getConfig(NoticeID.PLUNDER_ESCORT, BulletinConfig.class);
				if (bulletinConfig != null) {
					Map<String, Object> params = new HashMap<String, Object>(5);
					params.put(NoticeRule.playerId, playerId);
					params.put(NoticeRule.playerName, userDomain.getPlayer().getName());
					params.put(NoticeRule.targetName, targetDomain.getPlayer().getName());
					params.put(NoticeRule.campName, player.getCamp().getName());
					params.put(NoticeRule.campName2, targetPlayer.getCamp().getName());
					NoticePushHelper.pushCampNotice(userDomain.getPlayer().getCamp(), NoticeID.PLUNDER_ESCORT, NoticeType.HONOR, params, bulletinConfig.getPriority());
				}
			} 
		}
	}

	
	
	public ResultObject<Map<String,Object>> refreshTaskQuality(long playerId, int targetQuality, int refreshCount, boolean autoBuyBook) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		
		UserEscortTask userEscortTask = escortManager.getEscortTask(battle);
		if(userEscortTask == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(!userEscortTask.isNoTask()){
			return ResultObject.ERROR(TASK_ACCEPTED_CANT_REFRESH_QUALITY);
		}
		
		Quality quality = EnumUtils.getEnum(Quality.class, targetQuality);
		if(quality == null || refreshCount <= 0) {
			return ResultObject.ERROR(QUALITY_IS_THE_SAME);
		}
		
		if(userEscortTask.getQuality() >= targetQuality) {
			Map<String,Object> result = new HashMap<String, Object>();
			result.put(ResponseKey.ESCORTTASK, userEscortTask);
			result.put(ResponseKey.USE_BOOKS, 0);
			result.put(ResponseKey.USE_GOLD, 0);
			return ResultObject.SUCCESS(result);
		}
		
		int backpack = BackpackType.DEFAULT_BACKPACK;
		int propsId =  EscortTaskRule.REFRESH_QUALITY_ITEMID;
		PropsConfig propsConfig = propsManager.getPropsConfig(propsId);
		if(propsConfig == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		}
		
		int mallPrice = propsConfig.getMallPrice();
		List<UserProps> propsList = propsManager.listUserPropByBaseId(playerId, propsId, backpack);
		if(!autoBuyBook && (propsList == null || propsList.isEmpty())) { 
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		}
		
		int useBooks = 0;
		int useGolden = 0;
		int autoBuyBooksCount = 0;
		int result = SUCCESS;
		boolean updateEntity = false;
		Map<Long, UserProps> userPropsMap = new HashMap<Long, UserProps>();
		OUTTER_BREAK: for (int index = 0; index < refreshCount; index++) {
			boolean isPayToRandom = false;
			INNER_BREAK: for (UserProps userProps : propsList) {
				if(userProps.getCount() <= 0) {
					continue;
				}
				
				ChainLock lock = LockUtils.getLock(player.getPackLock());
				try {
					lock.lock();
					if(userProps.getCount() <= 0) {
						continue;
					}
					
					useBooks += 1;
					updateEntity = true;
					isPayToRandom = true;
					userProps.decreaseItemCount(1);
					userPropsMap.put(userProps.getId(), userProps);
					propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, userProps);
					break INNER_BREAK;
				} finally {
					lock.unlock();
				}
			}
			
			if(!isPayToRandom) { 
				if(player.getGolden() < mallPrice) {
					result = GOLDEN_NOT_ENOUGH;
					break OUTTER_BREAK;
				}
				
				ChainLock lock = LockUtils.getLock(player);
				try {
					lock.lock();
					if(player.getGolden() < mallPrice) {
						result = GOLDEN_NOT_ENOUGH;
						break OUTTER_BREAK;
					}
					
					updateEntity = true;
					isPayToRandom = true;
					useGolden += mallPrice;
					autoBuyBooksCount += 1;
					player.decreaseGolden(mallPrice);
				} finally {
					lock.unlock();
				}
			}
			
			if(isPayToRandom) {
				result = SUCCESS;
				updateEntity = true;
				userEscortTask.setQuality(escortManager.getRandomEscortTaskQuality());
				if(userEscortTask.getQuality() >= targetQuality) {
					BulletinConfig config = NoticePushHelper.getConfig(NoticeID.ESCORT_TASK_REFRESH, BulletinConfig.class);
					if (config != null) {
						TaskRule.pushTaskQualityNotice(config, userEscortTask.getQuality(), player);
					}
					break OUTTER_BREAK;
				}
			}
		}
		
		if(updateEntity) {
			Collection<UserProps> values = userPropsMap.values();
			dbService.submitUpdate2Queue(player, values, userEscortTask);
			List<LoggerGoods> logGoodsInfos = new ArrayList<LoggerGoods>(3);
			if(!values.isEmpty()) {
				MessagePushHelper.pushUserProps2Client(playerId, DEFAULT_BACKPACK, false, values);
				for(UserProps userProps : values){
					logGoodsInfos.add(LoggerGoods.outcomeProps(userProps.getId(), userProps.getBaseId(), userProps.getCount()));
				}
			}

			
			if(useGolden != 0) {
				List<UnitId> unitIds = Arrays.asList(userDomain.getUnitId());
				UserPushHelper.pushAttribute2AreaMember(playerId, Arrays.asList(playerId), unitIds, AttributeKeys.GOLDEN);
				logGoodsInfos.add(LoggerGoods.outcomePropsAutoBuyGolden(EscortTaskRule.REFRESH_QUALITY_ITEMID, autoBuyBooksCount, useGolden));
			}
			
			LoggerGoods[] loggerGoodsArray = logGoodsInfos.toArray(new LoggerGoods[logGoodsInfos.size()]);
			if(loggerGoodsArray.length > 0) {
				GoodsLogger.goodsLogger(player, Source.ESCORT_REFRESH_QUALITY, loggerGoodsArray);//道具消耗日志
			}
			
			if(useGolden != 0) {
				GoldLogger.outCome(Source.ESCORT_REFRESH_QUALITY, useGolden, player, loggerGoodsArray);//金币消耗日志
			}
		}
		
		Map<String,Object> resultMap = new HashMap<String, Object>(4);
		resultMap.put(ResponseKey.RESULT, result);
		resultMap.put(ResponseKey.ESCORTTASK, userEscortTask);
		resultMap.put(ResponseKey.USE_BOOKS, useBooks);
		resultMap.put(ResponseKey.USE_GOLD, useGolden);
		
		return ResultObject.SUCCESS(resultMap);
	}

	@SuppressWarnings("unchecked")
	
	public ResultObject<UserEscortTask> completeAndreward(long playerId, int npcId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
			
		PlayerBattle battle = userDomain.getBattle();
		UserEscortTask userEscortTask = escortManager.getEscortTask(battle);
		if(userEscortTask == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(userEscortTask.getStatus() == TaskStatus.UNACCEPT){
			return ResultObject.ERROR(TASK_NOT_FOUND);
		}
		
		if(userEscortTask.getStatus() != TaskStatus.COMPLETED){
			return ResultObject.ERROR(TASK_IS_OVER_TIME);
		}
		
		if(userEscortTask.isTimeOut()){
			return ResultObject.ERROR(TASK_IS_OVER_TIME);
		}
		
		EscortTaskConfig config = escortManager.getEscortTaskConfig(userEscortTask.getTaskId());
		if(config == null){
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		
		if(config.getCompleteNpc() != npcId){
			return ResultObject.ERROR(COMPLETE_ESCORT_TASK_ERR);
		}
		
		Npc npc = npcFacade.getNpc(config.getCompleteNpc());
		if(npc == null || npc.getMapId() != userDomain.getMapId()){
			return ResultObject.ERROR(MAP_POSITION_ERR);
		}
		
		VipDomain vipDomain = vipManager.getVip(playerId);      
		
		List<UserProps> userPropslist = null;
		Player player = userDomain.getPlayer();
		PlayerBattle playerBattle = userDomain.getBattle();
		List<UserProps> newPropslist = this.rewardProps(playerId, config);
		int rewardExp = 0;
		int exp = config.getExpValue(userEscortTask.getAcceptLevel(), userEscortTask.getQuality()); 
		float trainProfit = onlineActiveService.getEscortProfit(userDomain);
		int vipAddExp = vipDomain.calsVipExperience(exp, EscortTaskExperience);   					
		if(userEscortTask.isplunder()){
			if(userEscortTask.isProtection()){ 
				rewardExp = escortManager.caclEscortProtectPlunder(exp); 
			}else{
				rewardExp = escortManager.caclEscortUnprotectPlunder(exp);
			}
		}else{
			if(userEscortTask.isProtection()){
				rewardExp = escortManager.caclEscortProtectUnplunder(exp);
			}else{
				rewardExp = exp;
			}
		}
		
		int activeAwardExp = (int)(exp * trainProfit);
		rewardExp += vipAddExp;
		rewardExp += activeAwardExp;
		
		int quality = escortManager.getRandomEscortTaskQuality();
		ChainLock lock = LockUtils.getLock(userEscortTask,player,player.getPackLock(),playerBattle);
		try {
			lock.lock();
			if(userEscortTask.getStatus() != TaskStatus.COMPLETED){
				return ResultObject.ERROR(TASK_IS_OVER_TIME);
			}
			
			if(newPropslist != null && !newPropslist.isEmpty()){
				int packSize = propsManager.getBackpackSize(playerId, BackpackType.DEFAULT_BACKPACK);
				if((packSize + newPropslist.size()) > player.getMaxBackSize()){
					return ResultObject.ERROR(BACKPACK_FULLED);
				}
				userPropslist = propsManager.createUserProps(newPropslist);
				propsManager.put2UserPropsIdsList(playerId, BackpackType.DEFAULT_BACKPACK, userPropslist);
			}
			
			player.setProtection(false);
			playerBattle.increaseExp(rewardExp);
			playerBattle.setFlushable(Flushable.FLUSHABLE_NORMAL);
			userEscortTask.reastTask(quality);
		}finally{
			lock.unlock();
		}
		
		if(exp != 0){
			ExpLogger.escortTaskExp(userDomain, config, exp);
		}
		
		this.pushEscortRiding(userDomain, userEscortTask);
		dbService.submitUpdate2Queue(userEscortTask,playerBattle);
		if(userPropslist != null && !userPropslist.isEmpty()){
			MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, userPropslist);
			MessagePushHelper.pushGoodsCountChange2Client(playerId, GoodsVO.valuleOf( userPropslist, null, null, null));
			
			LoggerGoods[] loggerGoodsArray = LoggerPropsHelper.convertLoggerGoods(Orient.INCOME, userPropslist, null, null, null);
			GoodsLogger.goodsLogger(player, Source.COMPLETE_ESCORT_TASK, loggerGoodsArray);
		}
		List<Long> playerIdList = Arrays.asList(playerId);
		List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, AttributeKeys.EXP);
		return ResultObject.SUCCESS(userEscortTask);
	}
	
	
	/**
	 * 需要奖励的道具
	 * @param playerId        玩家的ID
	 * @param rewardConfig    奖励道具配置
	 * @return {@link List}   奖励的道具集合
	 */
    private List<UserProps> rewardProps(long playerId, EscortTaskConfig taskConfig) {
    	List<UserProps> propsList = new ArrayList<UserProps>(2);
    	List<RewardVO> itemRewardMap = taskConfig.getItemRewardMap();
    	if(itemRewardMap == null || itemRewardMap.isEmpty()){
    		return propsList;
    	}
    	
    	for (RewardVO rewardVO : itemRewardMap) {
    		int baseId = rewardVO.getBaseId();
    		int count = rewardVO.getCount();
    		boolean binding = rewardVO.isBinding();
			PropsConfig propsConfig = propsManager.getPropsConfig(baseId);
			if(propsConfig != null){
				propsList.add(UserProps.valueOf(playerId, baseId, count, BackpackType.DEFAULT_BACKPACK, null, binding));
			}
    	}
    	return propsList;
    }
    
	
	@SuppressWarnings("unchecked")
	public ResultObject<UserEscortTask> refreshRandQuality(long playerId, String userItem, int autoBuyCount) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		UserEscortTask userEscortTask = escortManager.getEscortTask(battle);
		if(userEscortTask == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(userEscortTask.getQuality() == Quality.ORANGE.ordinal()){
			return ResultObject.ERROR(QUALITY_IS_THE_SAME);
		}
		
		int goodsItemBaseId = 0;
		int totleCount = 0;
		int needGolden = 0;
		List<LoggerGoods> logGoodsInfos = new ArrayList<LoggerGoods>(3);
		
		Map<Long,Integer> costMapList = null;
		if(userItem != null && !userItem.isEmpty()){
			costMapList = this.spliteUserItems(userItem);
			for(Entry<Long, Integer> entry : costMapList.entrySet()){
				long propsId = entry.getKey();
				int count = entry.getValue();
				
				UserProps userProps = propsManager.getUserProps(propsId);
				if (userProps == null) {
					return ResultObject.ERROR(ITEM_NOT_ENOUGH);
				}

				if (userProps.getPlayerId() != playerId) {
					return ResultObject.ERROR(ITEM_NOT_ENOUGH);
				} else if (!userProps.validBackpack(DEFAULT_BACKPACK)) {
					return ResultObject.ERROR(BACKPACK_INVALID);
				} else if (userProps.getCount() < count) {
					return ResultObject.ERROR(ITEM_NOT_ENOUGH);
				} else if(userProps.isTrading()) {
					return ResultObject.ERROR(ITEM_CANNOT_USE);
				}

				PropsConfig propsConfig = userProps.getPropsConfig();
				if (propsConfig == null) {
					return ResultObject.ERROR(BASEDATA_NOT_FOUND);
				} else if (propsConfig.getChildType() != PropsChildType.ESCORT_REFRESH_TYPE) {
					return ResultObject.ERROR(BELONGS_INVALID);
				}
				
				totleCount += count;
				goodsItemBaseId = propsConfig.getId();
				logGoodsInfos.add(LoggerGoods.outcomeProps(propsId, propsConfig.getId(), count));
			}
		}
		
		if(autoBuyCount > 0){
			PropsConfig propsConfig = propsManager.getPropsConfig(EscortTaskRule.REFRESH_QUALITY_ITEMID);
			if (propsConfig == null) {
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			}
			needGolden = autoBuyCount * propsConfig.getMallPrice();
			if(player.getGolden() < needGolden){
				return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
			}
			
			totleCount += autoBuyCount;
		}
		
		if(totleCount != EscortTaskRule.REFRESH_RAND_QUALITY_PROPS_COUNT){
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		int quality = escortManager.getRandomEscortTaskQuality();
		
		List<UserProps> userPropslist = null;
		if(costMapList != null && !costMapList.isEmpty()){
			userPropslist = propsManager.costUserPropsList(costMapList);
		}
		ChainLock lock = LockUtils.getLock(player,userEscortTask);
		try {
			lock.lock();
			if(userEscortTask.getQuality() == Quality.ORANGE.ordinal()){
				return ResultObject.ERROR(QUALITY_IS_THE_SAME);
			}
			
			if(player.getGolden() < needGolden){
				return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
			}
			
			player.decreaseGolden(needGolden);
			userEscortTask.setQuality(quality);
		}finally{
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(userEscortTask,player);
		
		if(userPropslist != null && !userPropslist.isEmpty()) {
			MessagePushHelper.pushUserProps2Client(playerId, DEFAULT_BACKPACK, false, userPropslist);
			MessagePushHelper.pushGoodsCountChange2Client(playerId, GoodsVO.valueOf(goodsItemBaseId, GoodsType.PROPS, (totleCount * -1)));//显示扣减
		}
		
		if(needGolden > 0) { 
			List<Long> playerIdList = Arrays.asList(playerId);
			List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
			UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, AttributeKeys.GOLDEN);
			logGoodsInfos.add(LoggerGoods.outcomePropsAutoBuyGolden(EscortTaskRule.REFRESH_QUALITY_ITEMID, autoBuyCount, needGolden));//自动购买的数量
			GoldLogger.outCome(Source.ESCORT_REFRESH_QUALITY, needGolden, player, logGoodsInfos.toArray(new LoggerGoods[logGoodsInfos.size()]));//金币日志
		}
		
		if(!logGoodsInfos.isEmpty()){
			GoodsLogger.goodsLogger(player, Source.HORSE_LEVEL_UP, logGoodsInfos.toArray(new LoggerGoods[logGoodsInfos.size()]));
		}
		
		if(quality == Quality.ORANGE.ordinal()){
			HashMap<String, Object> paramsMap = new HashMap<String, Object>(2); 
			paramsMap.put(NoticeRule.campName, player.getCamp().getName());
			paramsMap.put(NoticeRule.playerName, player.getName());
			NoticePushHelper.pushNotice(NoticeID.ESCORT_TASK_REFRESH_2, NoticeType.HONOR, paramsMap, 1);
		}
		
		return ResultObject.SUCCESS(userEscortTask);
	}

	
	@SuppressWarnings("unchecked")
	public ResultObject<UserEscortTask> refreshOrange(long playerId,String userItem, int autoBuyCount) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		UserEscortTask userEscortTask = escortManager.getEscortTask(battle);
		if(userEscortTask == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(userEscortTask.getQuality() == Quality.ORANGE.ordinal()){
			return ResultObject.ERROR(QUALITY_IS_THE_SAME);
		}
		
		int goodsItemBaseId = 0;
		int totleCount = 0;
		int needGolden = 0;
		List<LoggerGoods> logGoodsInfos = new ArrayList<LoggerGoods>(3);
		Map<Long,Integer> costMapList = null;
		
		if(userItem != null && !userItem.isEmpty()){
			costMapList = this.spliteUserItems(userItem);
			for(Entry<Long, Integer> entry : costMapList.entrySet()){
				long propsId = entry.getKey();
				int count = entry.getValue();
				
				UserProps userProps = propsManager.getUserProps(propsId);
				if (userProps == null) {
					return ResultObject.ERROR(ITEM_NOT_ENOUGH);
				}

				if (userProps.getPlayerId() != playerId) {
					return ResultObject.ERROR(ITEM_NOT_ENOUGH);
				} else if (!userProps.validBackpack(DEFAULT_BACKPACK)) {
					return ResultObject.ERROR(BACKPACK_INVALID);
				} else if (userProps.getCount() < count) {
					return ResultObject.ERROR(ITEM_NOT_ENOUGH);
				} else if(userProps.isTrading()) {
					return ResultObject.ERROR(ITEM_CANNOT_USE);
				}

				PropsConfig propsConfig = userProps.getPropsConfig();
				if (propsConfig == null) {
					return ResultObject.ERROR(BASEDATA_NOT_FOUND);
				} else if (propsConfig.getChildType() != PropsChildType.ESCORT_REFRESH_TYPE) {
					return ResultObject.ERROR(BELONGS_INVALID);
				}
				
				goodsItemBaseId = propsConfig.getId();
				totleCount += count;
				logGoodsInfos.add(LoggerGoods.outcomeProps(propsId, propsConfig.getId(), count));
			}
		}
		
		if(autoBuyCount > 0){
			PropsConfig propsConfig = propsManager.getPropsConfig(EscortTaskRule.REFRESH_QUALITY_ITEMID);
			if (propsConfig == null) {
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			}
			needGolden = autoBuyCount * propsConfig.getMallPrice();
			if(player.getGolden() < needGolden){
				return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
			}
			
			totleCount += autoBuyCount;
		}
		
		if(totleCount != EscortTaskRule.REFRESH_ORANGE_QUALITY_PROPS_COUNT){
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		List<UserProps> userPropslist = null;
		if(costMapList != null && !costMapList.isEmpty()){
			userPropslist = propsManager.costUserPropsList(costMapList);
		}
		ChainLock lock = LockUtils.getLock(player,userEscortTask);
		try {
			lock.lock();
			if(userEscortTask.getQuality() == Quality.ORANGE.ordinal()){
				return ResultObject.ERROR(QUALITY_IS_THE_SAME);
			}
			
			if(player.getGolden() < needGolden){
				return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
			}
			
			player.decreaseGolden(needGolden);
			userEscortTask.setQuality(Quality.ORANGE.ordinal());
		}finally{
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(userEscortTask,player);
		
		if(userPropslist != null && !userPropslist.isEmpty()) {
			MessagePushHelper.pushUserProps2Client(playerId, DEFAULT_BACKPACK, false, userPropslist);
			MessagePushHelper.pushGoodsCountChange2Client(playerId, GoodsVO.valueOf(goodsItemBaseId, GoodsType.PROPS, (totleCount * -1)));//显示扣减
		}
		
		if(needGolden > 0) { 
			List<Long> playerIdList = Arrays.asList(playerId);
			List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
			UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, AttributeKeys.GOLDEN);
			logGoodsInfos.add(LoggerGoods.outcomePropsAutoBuyGolden(EscortTaskRule.REFRESH_QUALITY_ITEMID, autoBuyCount, needGolden));//自动购买的数量
			GoldLogger.outCome(Source.ESCORT_REFRESH_QUALITY, needGolden, player, logGoodsInfos.toArray(new LoggerGoods[logGoodsInfos.size()]));//金币日志
		}
		
		if(!logGoodsInfos.isEmpty()){
			GoodsLogger.goodsLogger(player, Source.HORSE_LEVEL_UP, logGoodsInfos.toArray(new LoggerGoods[logGoodsInfos.size()]));
		}
		
		
		HashMap<String, Object> paramsMap = new HashMap<String, Object>(2); 
		paramsMap.put(NoticeRule.campName, player.getCamp().getName());
		paramsMap.put(NoticeRule.playerName, player.getName());
		NoticePushHelper.pushNotice(NoticeID.ESCORT_TASK_REFRESH_2, NoticeType.HONOR, paramsMap, 1);
		
		return ResultObject.SUCCESS(userEscortTask);
	}
	
	
	
	/**
	 * 截取出字符串.
	 * 
	 * @param userItems		用户道具信息. 格式: 用户道具ID_数量|用户道具ID_数量|...
	 * @return
	 */
	private Map<Long, Integer> spliteUserItems(String userItems) {
		Map<Long, Integer> maps = new HashMap<Long, Integer>(2);
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

}
