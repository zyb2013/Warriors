package com.yayo.warriors.module.treasure.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.scheduling.ScheduledTask;
import com.yayo.common.scheduling.Scheduler;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.push.Pusher;
import com.yayo.common.utility.EnumUtils;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.TimeConstant;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.adapter.TreasureService;
import com.yayo.warriors.basedb.model.BigMapConfig;
import com.yayo.warriors.basedb.model.BulletinConfig;
import com.yayo.warriors.basedb.model.DungeonConfig;
import com.yayo.warriors.basedb.model.MonsterConfig;
import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.basedb.model.TreasureConfig;
import com.yayo.warriors.basedb.model.TreasureEventConfig;
import com.yayo.warriors.basedb.model.TreasureMonsterConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.FormulaHelper;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.NoticePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.drop.manager.DropManager;
import com.yayo.warriors.module.drop.model.DropRewards;
import com.yayo.warriors.module.dungeon.constant.DungeonConstant;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.dungeon.facade.DungeonFacade;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.horse.rule.HorseRule;
import com.yayo.warriors.module.logger.helper.LoggerPropsHelper;
import com.yayo.warriors.module.logger.log.ExpLogger;
import com.yayo.warriors.module.logger.log.GoldLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.log.SilverLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Orient;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.GameScreen;
import com.yayo.warriors.module.map.manager.GameMapManager;
import com.yayo.warriors.module.map.util.MapUtils;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.manager.MonsterManager;
import com.yayo.warriors.module.notice.rule.NoticeRule;
import com.yayo.warriors.module.notice.type.NoticeID;
import com.yayo.warriors.module.notice.vo.NoticeVo;
import com.yayo.warriors.module.npc.facade.NpcFacade;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.module.props.model.PropsStackResult;
import com.yayo.warriors.module.props.rule.EquipHelper;
import com.yayo.warriors.module.props.rule.PropsHelper;
import com.yayo.warriors.module.props.type.PropsChildType;
import com.yayo.warriors.module.props.type.Quality;
import com.yayo.warriors.module.task.manager.EscortTaskManager;
import com.yayo.warriors.module.treasure.constant.TreasureConstant;
import com.yayo.warriors.module.treasure.entity.UserTreasure;
import com.yayo.warriors.module.treasure.facade.TreasureFacade;
import com.yayo.warriors.module.treasure.manager.TreasureManager;
import com.yayo.warriors.module.treasure.model.Treasure;
import com.yayo.warriors.module.treasure.rule.TreasureRule;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.entity.PlayerMotion;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.PlayerStateKey;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.dungeon.DungeonCmd;
import com.yayo.warriors.socket.vo.ChangeScreenVo;
import com.yayo.warriors.type.ElementType;
import com.yayo.warriors.type.FormulaKey;
import com.yayo.warriors.type.GoodsType;

@Component
public class TreasureFacadeImpl implements TreasureFacade {
	@Autowired
	private UserManager userManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private DbService dbService;
	@Autowired
	private TreasureService treasureService;
	@Autowired
	private TreasureManager treasureManager;
	@Autowired
	private MonsterManager monsterManager;
	@Autowired
	private GameMapManager gameMapManager;
	@Autowired
	private DungeonFacade dungeonFacade;
	@Autowired
	private Pusher pusher;
	@Autowired
	private Scheduler scheduler;
	@Autowired
	private EscortTaskManager escortTaskManager;
	@Autowired
	private DropManager dropManager;
	@Autowired
	private NpcFacade npcFacade;
	
	/** 日志 */
	private final Logger log = LoggerFactory.getLogger(TreasureFacadeImpl.class);
	/** 藏宝怪缓存，key:怪物id value:队列id */
	private final ConcurrentMap<Long, TreasureMonster> treasulreMonsterMap = new ConcurrentHashMap<Long, TreasureMonster>(5);

	
	public int openTreansureProps(long playerId, long userPropsId, Map<String, Object> resultMap) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return TreasureConstant.PLAYER_NOT_FOUND;
		}
		
		UserProps userProps = propsManager.getUserProps(userPropsId);
		if(userProps == null){
			return TreasureConstant.ITEM_NOT_FOUND;
		} else if(userProps.getCount() <= 0){
			return TreasureConstant.ITEM_NOT_ENOUGH;
		} else if(userProps.getPlayerId() != playerId){
			return TreasureConstant.NO_RIGHT;
		} else if(userProps.isOutOfExpiration()){
			return TreasureConstant.TREASURE_TIMEOUT;
		}
		
		PropsConfig propsConfig = resourceService.get(userProps.getBaseId(), PropsConfig.class);
		if(propsConfig == null){
			return TreasureConstant.BASEDATA_NOT_FOUND;
		} else if(propsConfig.getChildType() != PropsChildType.TREASURE_PROPS_TYPE){
			return TreasureConstant.INPUT_VALUE_INVALID;
		}
		
		int branching = userDomain.getBranching();
		UserTreasure userTreasure = treasureManager.getUserTreasure(playerId);
		if(userTreasure == null){
			return TreasureConstant.PLAYER_NOT_FOUND;
		}
		Treasure treasure = null;
		ChainLock lock = LockUtils.getLock(userTreasure);
		try {
			lock.lock();
			treasure = userTreasure.getOpendTreasure(userPropsId);
			if(treasure == null){
				int[] mapPoint = treasureService.randomMapPoint( userDomain.getBattle().getLevel(), branching );
				if(mapPoint == null){
					return TreasureConstant.FAILURE;
				}
				treasure = userTreasure.addOpendTreasure(userPropsId, mapPoint[0], mapPoint[1], mapPoint[2]);
				dbService.submitUpdate2Queue(userTreasure);
			}
			treasure.setQuality(userProps.getQuality().ordinal());
			
		} finally {
			lock.unlock();
		}
		
		resultMap.put(ResponseKey.REWARD_ID, propsConfig.getAttrValueRound());
		resultMap.put(ResponseKey.INFO, treasure );
		
		return TreasureConstant.SUCCESS;
	}

	@SuppressWarnings("unchecked")
	
	public int refreshQuality(long playerId, long userPropsId, int quality, Map<String, Object> resultMap) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return TreasureConstant.PLAYER_NOT_FOUND;
		}
		
		UserTreasure userTreasure = treasureManager.getUserTreasure(playerId);
		if(userTreasure == null){
			return TreasureConstant.FAILURE;
		}
		Treasure treasure = userTreasure.getOpendTreasure(userPropsId);
		if(treasure == null){
			return TreasureConstant.TREASURE_NOT_OPENED;
		}
		
		UserProps userProps = propsManager.getUserProps(userPropsId);
		if(userProps == null){
			return TreasureConstant.ITEM_NOT_FOUND;
		} else if(userProps.getCount() <= 0){
			return TreasureConstant.ITEM_NOT_ENOUGH;
		} else if(userProps.getPlayerId() != playerId){
			return TreasureConstant.NO_RIGHT;
		}
		
		PropsConfig propsConfig = resourceService.get(userProps.getBaseId(), PropsConfig.class);
		if(propsConfig == null){
			return TreasureConstant.BASEDATA_NOT_FOUND;
		} else if(propsConfig.getChildType() != PropsChildType.TREASURE_PROPS_TYPE){
			return TreasureConstant.INPUT_VALUE_INVALID;
		}
		Quality propsQuality = userProps.getQuality();
		if(propsQuality.ordinal() >= Quality.PURPLE.ordinal() ){
			return TreasureConstant.TREASURE_CURR_QUALITY_NO_CHANGE;
		}else if(quality > 0 && propsQuality.ordinal() >= quality ){
			return TreasureConstant.TREASURE_CURR_QUALITY_NO_CHANGE;
		}
		
		Player player = userDomain.getPlayer();
		ChainLock lock = LockUtils.getLock(player, userDomain.getPackLock());
		try {
			lock.lock();
			if(propsQuality.ordinal() >= Quality.PURPLE.ordinal() ){
				return TreasureConstant.TREASURE_CURR_QUALITY_NO_CHANGE;
			} else if(quality > 0 && propsQuality.ordinal() >= quality ){
				return TreasureConstant.TREASURE_CURR_QUALITY_NO_CHANGE;
			}
			
			ResultObject<Integer> rt = refresh(player, userDomain.getBattle(), userProps, propsConfig, quality);
			if(!rt.isOK()){
				return rt.getResult();
			}
			Integer costSilver = rt.getValue();
			player.decreaseSilver(costSilver);
			
			dbService.submitUpdate2Queue(player, userProps);
			resultMap.put( ResponseKey.SILVER, costSilver );
			resultMap.put( ResponseKey.INFO, userProps.getQuality().ordinal() );
			
			//日志
			SilverLogger.outCome(Source.TREASURE_FRESH_QUALITY, costSilver, player);
		} finally {
			lock.unlock();
		}
		
		if(userProps.getQuality() == Quality.PURPLE){
			BulletinConfig config = resourceService.get(NoticeID.TREASURE_QUALITY, BulletinConfig.class);
			if (config != null) {
				Map<String, Object> params = new HashMap<String, Object>(2);
				params.put(NoticeRule.playerId, playerId);
				params.put(NoticeRule.playerName, player.getName());
				NoticeVo noticeVo = NoticeVo.valueOf(NoticeID.TREASURE_QUALITY, config.getPriority(), params);
				NoticePushHelper.pushNotice(noticeVo);
			}
		}
		
		//推送铜币
		List<Long> playerIdList = Arrays.asList(playerId);
		List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, AttributeKeys.SILVER);
		
		return TreasureConstant.SUCCESS;
	}
	
	/**
	 * 刷新藏宝图(用户道具)品质
	 * @param player
	 * @param userProps
	 * @param propsConfig
	 * @param quality
	 * @return
	 */
	private ResultObject<Integer> refresh(Player player, PlayerBattle playerBattle, UserProps userProps, PropsConfig propsConfig, int quality){
		int costSilver =  FormulaHelper.invoke(FormulaKey.TREASURE_REFRESH_QUALITY_COST_COIN, playerBattle.getLevel() ).intValue();
		int totalCostSilver = 0;
		while(true){
			if(player.getSilver() < totalCostSilver + costSilver ){
				return ResultObject.valueOf(totalCostSilver > 0 ? TreasureConstant.SUCCESS : TreasureConstant.SILVER_NOT_ENOUGH, totalCostSilver);
			}
			totalCostSilver += costSilver;
			int newQuality = treasureService.refreshQuality( propsConfig.getAttrValueRound() );
			userProps.setQuality( EnumUtils.getEnum(Quality.class, newQuality) );
			
			if(quality <= 0 || newQuality >= quality  || newQuality >= Quality.PURPLE.ordinal() ){	//达到此品质或铜币不退出
				break;
			}
			
		}
		
		return ResultObject.SUCCESS(totalCostSilver);
	}

	@SuppressWarnings("unchecked")
	
	public int digTreansure(final long playerId, long userPropsId, long digUserPropsId, Map<String, Object> resultMap) {
		if(userPropsId <= 0 || playerId <= 0){
			return TreasureConstant.INPUT_VALUE_INVALID;
		}
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return TreasureConstant.PLAYER_NOT_FOUND;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(escortTaskManager.isEscortStatus(battle)){
			return TreasureConstant.TREASURE_ESCORT_STATUS;
		}
		
		UserProps userProps = propsManager.getUserProps(userPropsId);
		if(userProps == null){
			return TreasureConstant.ITEM_NOT_FOUND;
		} else if(userProps.getCount() <= 0){
			return TreasureConstant.ITEM_NOT_ENOUGH;
		}
		
		PropsConfig propsConfig = resourceService.get(userProps.getBaseId(), PropsConfig.class);
		if(propsConfig == null){
			return TreasureConstant.BASEDATA_NOT_FOUND;
		} else if(propsConfig.getChildType() != PropsChildType.TREASURE_PROPS_TYPE){
			return TreasureConstant.INPUT_VALUE_INVALID;
		}
		
		UserProps digUserProps = propsManager.getUserProps(digUserPropsId);
		if(digUserProps == null){
			return TreasureConstant.ITEM_NOT_FOUND;
		} else if(digUserProps.getCount() <= 0){
			return TreasureConstant.ITEM_NOT_ENOUGH;
		} else if(digUserProps.getPlayerId() != playerId){
			return TreasureConstant.NO_RIGHT;
		}
		
		UserTreasure userTreasure = treasureManager.getUserTreasure(playerId);
		if(userTreasure == null){
			return TreasureConstant.FAILURE;
		}
		Treasure treasure = userTreasure.getOpendTreasure(userPropsId);
		if(treasure == null){
			return TreasureConstant.TREASURE_NOT_OPENED;
		}
		
		PlayerMotion motion = userDomain.getMotion();
		if(motion.getMapId() != treasure.getMapId() 
			|| !MapUtils.checkPosScopeInfloat(motion.getX(), motion.getY(), treasure.getX(), treasure.getY(), 2) ){
			return TreasureConstant.POINT_INVALID;
		}
		
		PropsConfig digPropsConfig = resourceService.get(digUserProps.getBaseId(), PropsConfig.class);
		if(digPropsConfig == null){
			return TreasureConstant.BASEDATA_NOT_FOUND;
		} else if(digPropsConfig.getChildType() != PropsChildType.TREASURE_DIG_PROPS_TYPE){
			return TreasureConstant.INPUT_VALUE_INVALID;
		}
		
		int rewardId = propsConfig.getAttrValueRound();
		TreasureConfig treasureConfig = treasureService.getTreasureConfig(rewardId, userProps.getQuality().ordinal() );
		if(treasureConfig == null){
			return TreasureConstant.BASEDATA_NOT_FOUND;
		}
		
		int dungeonBaseId = treasureConfig.getDungeonBaseId();
		ResultObject<ChangeScreenVo> result = null;
		int resultValue = TreasureConstant.FAILURE;
		Collection<BackpackEntry> backpackEntryList = new HashSet<BackpackEntry>(); 
		ChainLock lock = LockUtils.getLock( userTreasure, userDomain.getPackLock() );
		try {
			lock.lock();
			if(digUserProps.getCount() <= 0 || userProps.getCount() <= 0){
				return TreasureConstant.ITEM_NOT_ENOUGH;
			}
			
			result = dungeonFacade.enterDungeon(playerId, dungeonBaseId);
			if(!result.isOK()){
				if(resultValue == DungeonConstant.OVER_ENTER_DUNGEON_NUMBER){
					return TreasureConstant.TREASURE_MAX_COUNT_LIMIT;
				}
				return TreasureConstant.FAILURE;
			}

			resultValue = CommonConstant.SUCCESS;
			userTreasure.removeOpenedTreasure(userPropsId);
			
			//移除开启的藏宝图
			userTreasure.setUserPropsId( treasure.getUserPropsId() );
			userTreasure.setMapId( treasure.getMapId() );
			userTreasure.setX( treasure.getX() );
			userTreasure.setY( treasure.getY() );
			userTreasure.setStatus(1);
			userTreasure.setRewardId( rewardId );
			userTreasure.setPropsId( digPropsConfig.getId() );
			userTreasure.setQuality( userProps.getQuality().ordinal() );
			treasure.setQuality( userProps.getQuality().ordinal()  );
			
			//扣道具
			userProps.decreaseItemCount(1);
			digUserProps.decreaseItemCount(1);
			propsManager.removeUserPropsIfCountNotEnough(playerId, BackpackType.DEFAULT_BACKPACK, userProps, digUserProps);
			dbService.submitUpdate2Queue(userProps, digUserProps, userTreasure);
			
			backpackEntryList.add(userProps);
			backpackEntryList.add(digUserProps);
			
		} finally {
			lock.unlock();
		}
		
		if(backpackEntryList != null){
			MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, backpackEntryList );
		}
		
		if(resultValue == DungeonConstant.SUCCESS){
			ChangeScreenVo changeScreenVo = result.getValue();
			Map<String,Object> map = new HashMap<String,Object>(3);
			map.put(ResponseKey.RESULT, resultValue);
			map.put(ResponseKey.CHANGE_SCREEN, changeScreenVo);
			map.put(ResponseKey.DUNGEON_BASE_ID, dungeonBaseId);
			pusher.pushMessage(playerId, Response.defaultResponse(Module.DUNGEON, DungeonCmd.ENTER_DUNGEON, map) );
			
			DungeonConfig dungeonConfig =  resourceService.get(dungeonBaseId, DungeonConfig.class);
			ScheduledFuture<?> scheduleWithDelay = scheduler.scheduleWithDelay(new ScheduledTask() {
				
				public void run() {
					existTreansureMap(playerId);
				}
				
				public String getName() { return "藏宝图超时处理线程"; }
			}, dungeonConfig.getDungeonLiveDate() * TimeConstant.ONE_SECOND_MILLISECOND );
			
			userDomain.getPlayer().setAttribute(PlayerStateKey.TREASURE_EXIT_FUTURE, scheduleWithDelay);
		}
		
		return resultValue;
	}
	
	//物品奖励
	private ResultObject<List<BackpackEntry>> rewardGoods(UserDomain userDomain, Set<Integer> itemDropNos, int count, Map<String, GoodsVO> monsterVOs, boolean isFirstBox){
		Player player = userDomain.getPlayer();
		long playerId = userDomain.getPlayerId();
		
		List<DropRewards> dropRewards = new ArrayList<DropRewards>();
		for(int itemDropNo : itemDropNos){
			if(itemDropNo <= 0){
				continue;
			}
			List<DropRewards> list = dropManager.dropRewards(playerId, itemDropNo, count);
			if(list != null && list.size() > 0){
				dropRewards.addAll( list );
			}
		}
		
		int addSilver = 0;	//增加的铜币
		int addGolden = 0;	//增加的金币
		
		Map<Integer, int[]> dropProps = new HashMap<Integer, int[]>();
		List<UserProps> newUserProps = new ArrayList<UserProps>(1);
		Map<Long, Integer> mergeProps = new HashMap<Long, Integer>(1);
		List<UserEquip> userEquipsList = new ArrayList<UserEquip>(1);
		List<UserProps> updateUserPropsList = null;
		List<BackpackEntry> resultList = new ArrayList<BackpackEntry>();
		
		int rewardPackSize = 0; 
		int backpack = BackpackType.DEFAULT_BACKPACK;
		int usedSize = propsManager.getBackpackSize(playerId, backpack);
		ChainLock lock = LockUtils.getLock(player, player.getPackLock());
		try {
			lock.lock();
			for(DropRewards drs : dropRewards) {
				int baseId = drs.getBaseId();
				int amount = drs.getAmount();
				boolean binding = drs.isBinding();
				int type = drs.getType();
				if(type == GoodsType.PROPS) {
					int[] array = dropProps.get(baseId);
					array = array == null ? new int[2] : array;
					if(!binding) { //未绑定
						array[0] = array[0] + amount;
					} else {
						array[1] = array[1] + amount;
					}
					
					dropProps.put(baseId, array);
				} else if(type == GoodsType.EQUIP){
					userEquipsList.addAll(EquipHelper.newUserEquips(playerId, backpack, baseId, binding, amount));
				} else if(type == GoodsType.SILVER){
					addSilver += amount;
				} else if(type == GoodsType.GOLDEN){
					addGolden += amount;
				}
				
				if(type == GoodsType.PROPS || type == GoodsType.EQUIP){
					calcGoodsChange(monsterVOs, baseId, count, type);
				}
				
			}
			
			//第一个
			if(isFirstBox){
				int addCount = 1;
				calcGoodsChange(monsterVOs, HorseRule.HORSE_EXP_PROPS, addCount, GoodsType.PROPS);
				int[] values = dropProps.get(HorseRule.HORSE_EXP_PROPS);
				if(values == null){
					dropProps.put(HorseRule.HORSE_EXP_PROPS, new int[2]);
					values = dropProps.get(HorseRule.HORSE_EXP_PROPS);
				}
				values[1] += addCount;
			}
			
			for (Entry<Integer, int[]> entry : dropProps.entrySet()) {
				int[] value = entry.getValue();
				Integer propsId = entry.getKey();
				if(value[0] > 0) {
					PropsStackResult propsStack = PropsHelper.calcPropsStack(playerId, backpack, propsId, value[0], false);
					newUserProps.addAll(propsStack.getNewUserProps());
					mergeProps.putAll(propsStack.getMergeProps());
				}
				if(value[1] > 0) {
					PropsStackResult propsStack = PropsHelper.calcPropsStack(playerId, backpack, propsId, value[1], true);
					newUserProps.addAll(propsStack.getNewUserProps());
					mergeProps.putAll(propsStack.getMergeProps());
				}
			}
			
			rewardPackSize = newUserProps.size() + userEquipsList.size();
			if(!player.canAddNew2Backpack(rewardPackSize + usedSize, backpack)) {
				return ResultObject.ERROR(BACKPACK_FULLED);
			}
			
			updateUserPropsList = propsManager.updateUserPropsList(mergeProps);
			propsManager.createUserEquipAndUserProps(newUserProps, userEquipsList);
			propsManager.put2UserPropsIdsList(playerId, backpack, newUserProps);
			propsManager.put2UserEquipIdsList(playerId, backpack, userEquipsList);
			
			if(addSilver != 0){
				player.increaseSilver( addSilver );
			}
			if(addGolden != 0){
				player.increaseGolden( addGolden );
			}
			
			resultList.addAll(newUserProps);
			resultList.addAll(updateUserPropsList);
			resultList.addAll(userEquipsList);
			
		} catch (Exception e) {
			return ResultObject.ERROR(FAILURE);
		} finally {
			lock.unlock();
		}
		
		if(addGolden != 0 || addSilver != 0){
			if(addSilver != 0){
				SilverLogger.inCome(Source.TREASURE_PROPS, addSilver, player);
			}
			if(addGolden != 0){
				GoldLogger.inCome(Source.TREASURE_PROPS, addGolden, player);
			}
			
			//推送属性
			dbService.submitUpdate2Queue(player);
			List<Long> playerIdList = Arrays.asList(player.getId());
			List<UnitId> unitIdList = Arrays.asList(UnitId.valueOf(player.getId(), ElementType.PLAYER));
			UserPushHelper.pushAttribute2AreaMember(player.getId(),playerIdList, unitIdList, AttributeKeys.SILVER, AttributeKeys.GOLDEN);
		}
		
//		//物品日志
		LoggerGoods[] loggerGoodsArray = LoggerPropsHelper.convertLoggerGoods(Orient.INCOME, newUserProps, userEquipsList, mergeProps, updateUserPropsList);
		GoodsLogger.goodsLogger(userDomain.getPlayer(), Source.TREASURE_PROPS, loggerGoodsArray);
		
		return ResultObject.SUCCESS(resultList);
	}
	
	//怪物奖励
	private ResultObject<List<MonsterDomain>> rewardMonster(UserDomain userDomain, TreasureMonsterConfig treasureMonsterConfig, Map<String, Object> resultMap, Map<String, GoodsVO> monsterVOs){
		Player player = userDomain.getPlayer();
		List<MonsterDomain> monsterList = new ArrayList<MonsterDomain>();
		
		//怪物随机地图
		GameMap gameMap = userDomain.getGameMap();
		Point rndPoint = gameMapManager.randomPoint(gameMap);
		if(rndPoint == null){
			return ResultObject.ERROR(TreasureConstant.FAILURE);
		}
		Map<Integer, Integer> rewardMonster = treasureMonsterConfig.getRewardMonster();
		for(Entry<Integer, Integer> entry : rewardMonster.entrySet()){
			Integer monsterFightId = entry.getKey();
			MonsterFightConfig monsterFightConfig = resourceService.get(monsterFightId, MonsterFightConfig.class);
			
			int count = entry.getValue();
			int x = rndPoint.x, y = rndPoint.y;
			int n = rewardMonster.size() * count;
			
			for(int i=0; i < count; i++){
				MonsterConfig newMonsterConfig = new MonsterConfig();
				newMonsterConfig.setMonsterFightId(monsterFightId);
				newMonsterConfig.setMonsterFight(monsterFightConfig);
				
				if(n > 0){
					while(true){
						x = rndPoint.x + getRandomRange(n);
						y = rndPoint.y + getRandomRange(n);
						if(x > 0 && y > 0 && gameMap.isPathPass(x, y) ){
							GameScreen gameScreen = gameMap.getGameScreen(x, y);
							if(gameScreen != null && !gameScreen.hasSpireInThisPoint(x, y, ElementType.MONSTER,ElementType.PLAYER)){
								break;
							}
						}
					}
				}
				newMonsterConfig.setMapId(gameMap.getMapId());
				newMonsterConfig.setBornX(x);
				newMonsterConfig.setBornY(y);
				
				MonsterDomain monsterDomain = monsterManager.addDungeonMonster(gameMap, newMonsterConfig, 0L, player.getBranching() );
				if(monsterDomain != null){
					if( monsterDomain.getMonsterBattle().isDead() ){
						log.error("增加副本新怪物时出错(HP为0), 怪物基础id:{} 怪物战斗属性id:{}", newMonsterConfig.getId(), newMonsterConfig.getMonsterFightId() );
						continue;
					}
				}
				monsterList.add(monsterDomain);
				TreasureMonster e = new TreasureMonster(monsterDomain, userDomain);
				treasulreMonsterMap.put(monsterDomain.getId(), e);
				if(!resultMap.containsKey(ResponseKey.MAPID)){
					resultMap.put(ResponseKey.MAPID, monsterDomain.getMapId());
					resultMap.put(ResponseKey.X, monsterDomain.getX());
					resultMap.put(ResponseKey.Y, monsterDomain.getY());
				}
				if(log.isDebugEnabled()){
					log.debug("藏宝图刷新出怪物:MapID{}, X:{}-Y:{}", new Object[]{monsterDomain.getMapId(), monsterDomain.getX(), monsterDomain.getY()} );
				}
			}
			calcGoodsChange(monsterVOs, monsterFightId, count, 2);
		}
		return ResultObject.SUCCESS(monsterList);
	}

	private void calcGoodsChange(Map<String, GoodsVO> monsterVOs, int monsterFightId, int count, int type) {
		String goodsKey = goodsKey(type, monsterFightId );
		GoodsVO goodsVO = monsterVOs.get(goodsKey);
		if(goodsVO == null){
			goodsVO = new GoodsVO();
			goodsVO.setType(type);
			goodsVO.setBaseId(monsterFightId);
			monsterVOs.put(goodsKey, goodsVO);
		}
		goodsVO.setNum( goodsVO.getNum() + count);
	}
	
	private String goodsKey(int goodsType, int baseId){
		return new StringBuilder().append(goodsType).append(Splitable.ATTRIBUTE_SPLIT).append(baseId).toString();
	}

	/**
	 * 获取随机移动范围值
	 * @param patrolRange  范围值
	 * @return
	 */
	protected int getRandomRange(int patrolRange){
		int range = Tools.getRandomInteger(patrolRange * 2 + 1);
		return range - patrolRange;
	}
	
	/**
	 * 藏宝图怪物
	 * @author jonsai
	 *
	 */
	class TreasureMonster {
		/** 刷出怪物的玩家 */
		private MonsterDomain monsterDomain;
		/** 刷出怪物的玩家 */
		private UserDomain userDomain;
		
		public TreasureMonster(MonsterDomain monsterDomain, UserDomain userDomain){
			this.monsterDomain = monsterDomain;
			this.userDomain = userDomain;
		}
		
		public MonsterDomain getMonsterDomain() {
			return monsterDomain;
		}

		public UserDomain getUserDomain() {
			return userDomain;
		}
	}

	
	public boolean isTreasureMonster(long monsterId) {
		return monsterId > 0 ? treasulreMonsterMap.containsKey(monsterId) : false;
	}

	
	public int rewardMonsterExp(UserDomain attackUser, long userPetId, int totalFightExp, long monsterId) {
		if(totalFightExp <=0 ){
			return TreasureConstant.SUCCESS;
		}
		TreasureMonster delay = this.treasulreMonsterMap.remove(monsterId);
		if(delay == null){
			return TreasureConstant.FAILURE;
		}
		
		MonsterDomain monsterDomain = delay.getMonsterDomain();
		int monsterLevel = monsterDomain.getMonsterBattle().getLevel();
		int attackerLevel = attackUser.getBattle().getLevel();
		
		UserDomain userDomain = delay.getUserDomain();
		PlayerBattle playerBattle = userDomain.getBattle();
		
		int attackExp = FormulaHelper.invoke(FormulaKey.TREASURE_KILL_MONSTER_EXP, totalFightExp, monsterLevel, attackerLevel).intValue();
		int openExp = FormulaHelper.invoke(FormulaKey.TREASURE_FRESH_MONSTER_EXP, totalFightExp, monsterLevel, playerBattle.getLevel()).intValue();
		
		int totalExp = 0;
		if( attackUser.getPlayerId() == userDomain.getPlayerId() ){
			if(userManager.addPlayerExp(attackUser.getPlayerId(), attackExp, true)) {
				totalExp += attackExp;
			};
		} else {
			if(userManager.addPlayerExp(attackUser.getPlayerId(), attackExp, true)) {
				totalExp += attackExp;
			};
			if(userManager.addPlayerExp(userDomain.getPlayerId(), openExp, true)) {
				totalExp += openExp;
			};
		}
		
		if(totalExp != 0) {
			ExpLogger.fightExp(userDomain, monsterDomain.getMonsterFightConfig(), totalExp);
		}
		return TreasureConstant.SUCCESS;
	}

	
	public int rewardTreasure(final long playerId, int npcId, Map<String, Object> resultMap) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return TreasureConstant.PLAYER_NOT_FOUND;
		}
		UserTreasure userTreasure = treasureManager.getUserTreasure(playerId);
		if(userTreasure == null){
			return TreasureConstant.FAILURE;
		}else if(userTreasure.isEnterTreasureMap()){
			return TreasureConstant.TREASURE_NOT_IN_MAP;
		}
//		else if(userTreasure.getStatus() > 1){		//已经领过奖了
//			return TreasureConstant.TREASURE_HAD_REWARD;
//		}
		TreasureConfig treasureConfig = treasureService.getTreasureConfig(userTreasure.getRewardId(), userTreasure.getQuality() );
		if(treasureConfig == null){
			return TreasureConstant.BASEDATA_NOT_FOUND;
		}
		Set<Integer> openedBoxs = userTreasure.getOpenedBoxs();
		if(openedBoxs.size() >= treasureConfig.getMaxOpen()){
			return TreasureConstant.TREASURE_MAX_OPEN_BOX_LIMIT;
		}
		
		Map<String, GoodsVO> propsVOs = null;
		Map<String, GoodsVO> monsterVOs = null;
		List<NoticeVo> noticeVOs = null;
		Collection<BackpackEntry> backpackEntryList = new HashSet<BackpackEntry>(); 
		ChainLock lock = LockUtils.getLock( userTreasure, userDomain.getPackLock() );
		try {
			lock.lock();
			int randomBox = treasureService.randomBox(treasureConfig.getId(), userTreasure.getPropsId(), openedBoxs);
			TreasureEventConfig treasureEvent = treasureService.getTreasureEvent(treasureConfig.getId(), userTreasure.getPropsId(), randomBox);
			if(treasureEvent == null){
				return TreasureConstant.BASEDATA_CONFIG_ERROR;
			}
//			System.err.println(String.format("TreasureEventConfig:%s", treasureEvent.toString()));
//			if(openedBoxs.contains(npcId)){
//				return TreasureConstant.TREASURE_HAD_REWARD;
//			}
			if(openedBoxs.size() >= treasureConfig.getMaxOpen()){
				return TreasureConstant.TREASURE_MAX_OPEN_BOX_LIMIT;
			}
			
			Set<Integer> itemDropNos = treasureEvent.getItemDropNos();
			if(itemDropNos != null && itemDropNos.size() > 0){
				propsVOs = new HashMap<String, GoodsVO>();
				ResultObject<List<BackpackEntry>> rewardGoods = rewardGoods(userDomain, itemDropNos, 1, propsVOs, openedBoxs.isEmpty() && userTreasure.getPropsId() == TreasureRule.GOLDEN_DIG_PROPS );
				if(!rewardGoods.isOK()){
					return rewardGoods.getResult();
				}
				resultMap.put(ResponseKey.TYPE, 0);
				resultMap.put(ResponseKey.INFO, propsVOs.values().toArray());
				if( rewardGoods.getValue() != null){
					backpackEntryList.addAll( rewardGoods.getValue() );
				}
			}
			
			int monsterDropNo = treasureEvent.getMonsterDropNo();
			if(monsterDropNo > 0){
				TreasureMonsterConfig monsterConfig = treasureService.getTreasureMonsterConfig(monsterDropNo);
				monsterVOs = new HashMap<String, GoodsVO>();
				ResultObject<List<MonsterDomain>> rewardMonster = rewardMonster(userDomain, monsterConfig, resultMap, monsterVOs);
				if(!rewardMonster.isOK()){
					return rewardMonster.getResult();
				}
				resultMap.put(ResponseKey.TYPE, 1);
				resultMap.put(ResponseKey.INFO, monsterVOs.values().toArray() );
				int noticeID = monsterConfig.getNoticeID();
				if(noticeID > 0){
					BulletinConfig bulletinConfig = resourceService.get(noticeID, BulletinConfig.class);
					if(bulletinConfig != null){
						noticeVOs = new ArrayList<NoticeVo>(); 
						List<MonsterDomain> monsterDomains = rewardMonster.getValue();
						final String playerName = userDomain.getPlayer().getName();
						Set<Integer> noticeMonsterIds = new HashSet<Integer>(1);
						for(MonsterDomain monsterDomain : monsterDomains){
	//						{playerName}在挖宝过程中不小心触动了机关释放了{monsterBaseId}
	//						{monsterBaseId}出现在{map}，请各位大虾速去支援
							MonsterFightConfig monsterFight = monsterDomain.getMonsterBattle().getMonsterFight();
							if(noticeMonsterIds.add(monsterFight.getId())){
								BigMapConfig bigMapConfig = resourceService.get(monsterDomain.getMapId(), BigMapConfig.class );
								final String mapName = bigMapConfig != null ? bigMapConfig.getName() : String.valueOf(monsterDomain.getMapId());
								
								Map<String, Object> params = new HashMap<String, Object>(5);
								params.put(NoticeRule.playerName, playerName);
								params.put(NoticeRule.monsterBaseId, monsterFight.getName());
								params.put(NoticeRule.map, mapName);
								params.put(NoticeRule.x, monsterDomain.getX());
								params.put(NoticeRule.y, monsterDomain.getY());
								noticeVOs.add(NoticeVo.valueOf(noticeID, bulletinConfig.getPriority(), params));
								
							}
						}
						
					}
					
				}
			}
			userTreasure.setStatus(randomBox);
			openedBoxs.add(randomBox);
			userTreasure.flushOpenedBoxs();
			
		} finally {
			lock.unlock();
		}
		dbService.submitUpdate2Queue(userTreasure);
		
		PlayerDungeon playerDungeon = dungeonFacade.getPlayerDungeon(playerId);
		GameMap gameMap = gameMapManager.getTemporaryMap(playerDungeon.getDungeonId(),userDomain.getBranching());
		if(gameMap != null){
			DungeonConfig dungeonConfig =  resourceService.get(playerDungeon.getDungeonBaseId(), DungeonConfig.class);
			if(dungeonConfig != null){
				npcFacade.hideNpc(gameMap, npcId, dungeonConfig.getDungeonLiveDate() * TimeConstant.ONE_SECOND_MILLISECOND + TimeConstant.ONE_HOUR_MILLISECOND);
			}
		}
		
		if(backpackEntryList != null){
			MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, backpackEntryList );
		}
		if(noticeVOs != null){
			for(NoticeVo vo : noticeVOs){
				NoticePushHelper.pushNotice(vo);
			}
		}
		
		return TreasureConstant.SUCCESS;
	}

	
	public int existTreansureMap(long playerId) {
		UserTreasure userTreasure = treasureManager.getUserTreasure(playerId);
		if(userTreasure == null){
			return TreasureConstant.FAILURE;
		}
		
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return TreasureConstant.PLAYER_NOT_FOUND;
		}
		int mapId = userTreasure.getMapId();
		int x = userTreasure.getX();
		int y = userTreasure.getY();
		ChainLock lock = LockUtils.getLock(userTreasure);
		try {
			lock.lock();
			userTreasure.reset();
			dbService.submitUpdate2Queue(userTreasure);
		} finally {
			lock.unlock();
		}
		ResultObject<ChangeScreenVo> result = dungeonFacade.exitDungeon(playerId, mapId, x, y);
		int resultValue = result.getResult();
		if(resultValue == DungeonConstant.SUCCESS){
			ChangeScreenVo changeScreenVo = result.getValue();
			Map<String,Object> map = new HashMap<String,Object>(2);
			map.put(ResponseKey.RESULT, resultValue);
			map.put(ResponseKey.CHANGE_SCREEN, changeScreenVo);
			pusher.pushMessage(playerId, Response.defaultResponse(Module.DUNGEON, DungeonCmd.EXIT_DUNGEON, map) );
			Player player = userDomain.getPlayer();
			ScheduledFuture<?> scheduledFuture = player.getAttribute(PlayerStateKey.TREASURE_EXIT_FUTURE, ScheduledFuture.class);
			if(scheduledFuture != null){
				scheduledFuture.cancel(false);
			}
		}
		return resultValue;
	}

	
	public int dropUserTreasure(UserDomain userDomain, long dropUserPropsId) {
		long playerId = userDomain.getPlayerId();
		UserTreasure userTreasure = treasureManager.getUserTreasure(playerId);
		if(userTreasure != null){
			ChainLock lock = LockUtils.getLock(userTreasure);
			try {
				lock.lock();
//				long userPropsId = userTreasure.getUserPropsId();
//				if(dropUserPropsId > 0 && dropUserPropsId == userPropsId){
//					userTreasure.reset();
//				}
				userTreasure.removeOpenedTreasure(dropUserPropsId);
			} finally {
				lock.unlock();
			}
			dbService.submitUpdate2Queue(userTreasure);
		}
		return TreasureConstant.SUCCESS;
	}
	
}
