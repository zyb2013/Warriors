package com.yayo.warriors.module.horse.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.horse.constant.HorseConstant.*;
import static com.yayo.warriors.module.pack.type.BackpackType.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.model.BulletinConfig;
import com.yayo.warriors.basedb.model.HorseConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.HorsePushHelper;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.NoticePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.horse.entity.Horse;
import com.yayo.warriors.module.horse.facade.HorseFacade;
import com.yayo.warriors.module.horse.manager.HorseManager;
import com.yayo.warriors.module.horse.rule.HorseRule;
import com.yayo.warriors.module.horse.vo.HorseVo;
import com.yayo.warriors.module.logger.log.GoldLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.log.HorseLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.notice.rule.NoticeRule;
import com.yayo.warriors.module.notice.type.NoticeID;
import com.yayo.warriors.module.notice.type.NoticeType;
import com.yayo.warriors.module.onhook.facade.TrainFacade;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.type.PropsChildType;
import com.yayo.warriors.module.task.facade.TaskFacade;
import com.yayo.warriors.module.task.manager.EscortTaskManager;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.socket.ResponseKey;

/**
 * 坐骑接口实现类
 * @author liuyuhua
 */
@Component
public class HorseFacadeImpl implements HorseFacade {
	@Autowired
	private DbService dbService;
	@Autowired
	private TaskFacade taskFacade;
	@Autowired
	private UserManager userManager;
	@Autowired
	private HorseManager horseManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private HorsePushHelper horsePushHelper; 
	@Autowired
	private EscortTaskManager escortTaskManager;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private TrainFacade trainFacade;
	
	/** 日志格式 */
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * 是否骑乘
	 * 
	 * @param  playerId  		玩家的ID
	 * @return {@link Boolean} 	true-骑乘, false-下马
	 */
	
	public boolean isRide(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return false;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		Horse horse = horseManager.getHorse(battle);
		return horse != null && horse.isRiding();
	}

	/**
	 * 坐骑的外观
	 * @param  playerId  		玩家的ID
	 * @return {@link Integer} 	坐骑的外观
	 */
	
	public int getHorseMount(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return 0;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		Horse horse = horseManager.getHorse(battle);
		return horse == null ? 0 : horse.getModel();
	}
	
	/**
	 * 获取玩家速度
	 * 
	 * @param  playerId   		玩家的ID
	 * @return {@link Integer} 	玩家的速度
	 */
	
	public int getPlayerSpeed(long playerId) {
		UserDomain userDomain = this.userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return 0;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle == null) {
			return 0;
		}
		
		return battle.getAttribute(AttributeKeys.MOVE_SPEED);
	}
	
	
	public ResultObject<Integer> winupHorse(long playerId, int mount) {
		UserDomain userDomain = this.userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(userDomain.getBattle().getLevel() < HorseRule.MIN_WINUP_LEVEL){
			return ResultObject.ERROR(WINUP_LEVEL_LIMIT);
		}
		
		if(trainFacade.isTrainStatus(playerId) > 0){
			return ResultObject.ERROR(TRAIN_STATE);
		}

		PlayerBattle battle = userDomain.getBattle();
		Horse horse = horseManager.getHorse(battle);
		if (horse == null) {
			return ResultObject.ERROR(HORSE_NOT_FOUND);
		} else if (horse.isRiding()) {
			return ResultObject.ERROR(IS_WINUP_HORSE);
		}

		int level = horse.getLevel();
		if (mount == 0) {	// 如果传过来的值为0,模型将不存在, 直接赋予最新坐骑外观
			HorseConfig config = this.horseManager.getHorseConfig(level);
			if (config == null) {
				return ResultObject.ERROR(CAN_USER_MOUNT);
			}
			mount = config.getModel();
		} else {
			
			if (!horse.canUseModel(mount)) {
				return ResultObject.ERROR(CAN_USER_MOUNT);
			}
		}
		
		Fightable beforable = battle.getAndCopyAttributes(); //之前的信息,用户发送推送
		ChainLock lock = LockUtils.getLock(horse, battle);
		try {
			lock.lock();
			horse.onRiding();
			horse.setModel(mount);
			horse.setFlushable(Flushable.FLUSHABLE_NORMAL);
			battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
		} finally {
			lock.unlock();
		}
		
		
		if(escortTaskManager.isEscortStatus(battle)){ //当不在押镖状态下的时候才需要发送坐骑外观
			mount = escortTaskManager.getEscortMount(battle);//坐骑外观
		}else{ 
			this.horsePushHelper.riding(userDomain, mount); // 上下坐骑主动推送命令
		}
		
		UserPushHelper.pushPlayerAttributeChange(beforable, userDomain); //客户端推送数值飘动
		return ResultObject.SUCCESS(mount);
	}

	/**
	 * 角色下马
	 * 
	 * @param  playerId  		玩家的ID
	 * @return {@link Integer} 	坐骑公共返回常量
	 */
	
	public ResultObject<Horse> dismountHorse(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(escortTaskManager.isEscortStatus(battle)){ //在押镖状态下,无法下坐骑
			return ResultObject.ERROR(IS_ESCORT_STATUS);
		}
		
		Horse horse = horseManager.getHorse(battle);
		if(horse == null){
			return ResultObject.ERROR(HORSE_NOT_FOUND);
		} else if(!horse.isRiding()){
			return ResultObject.ERROR(IS_DISMOUNT_HORSE);
		}
		
		Fightable beforable = battle.getAndCopyAttributes(); //之前的信息,用户发送推送
		ChainLock lock = LockUtils.getLock(horse, battle);
		try {
			lock.lock();
			horse.offRiding();
			horse.setFlushable(Flushable.FLUSHABLE_NORMAL);
			battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
		} finally {
			lock.unlock();
		}
		

		this.horsePushHelper.riding(userDomain, horse.getModel());//上下坐骑主动推送命令
		UserPushHelper.pushPlayerAttributeChange(beforable, userDomain); //客户端推送数值飘动
		return ResultObject.SUCCESS(horse);
	}

	
	/**
	 * 查询坐骑VO对象
	 * 
	 * @param  playerId 		角色ID
	 * @return {@link HorseVo}	坐骑的VO对象
	 */
	
	public HorseVo getHorseVO(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return null;
		}
		PlayerBattle battle = userDomain.getBattle();
		Horse horse = horseManager.getHorse(battle);
		if(horse == null){
			return null;
		}
		return HorseVo.valueOf(horse);
	}
	
	

	
	public ResultObject<Map<String,Object>> viewHorse(long targetId) {
		UserDomain userDomain = userManager.getUserDomain(targetId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		PlayerBattle battle = userDomain.getBattle();
		Horse horse = horseManager.getHorse(battle);
		if(horse == null){
			return ResultObject.ERROR(HORSE_NOT_FOUND);
		}
		
		Map<String,Object> result = new HashMap<String, Object>(2);
		result.put(ResponseKey.NAME, userDomain.getPlayer().getName());
		result.put(ResponseKey.LEVEL, horse.getLevel());
		result.put(ResponseKey.JOB, battle.getJob().ordinal());
		return ResultObject.SUCCESS(result);
	}
	
	
	@SuppressWarnings("unchecked")
	public ResultObject<HorseVo> definePropsFancy(long playerId,String userItems,int autoBuyCount){
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(userItems == null && autoBuyCount <= 0){
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		Horse horse = this.horseManager.getHorse(battle);
		if(horse == null){
			return ResultObject.ERROR(HORSE_NOT_FOUND);
		}
		
		if(horse.getLevel() >= HorseRule.MAX_HORSE_LEVEL){
			return ResultObject.ERROR(OVER_LEVEL_LIMIT);
		}
		
		if (battle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		} 
		
		int totleupExp = 0; 			// 总共增加的经验值
		int minRateCount = 0; 			// 小暴次数
		int maxRateCount = 0; 			// 大暴次数
		int fancyCount = 0;             // 坐骑幻化次数
		int logUsePropsCount = 0;       // 日志消耗的个数(日志记录) 
		int beforeLevel = (horse.getLevel() - 1) / 10 * 10; //坐骑升级之前的等级
		int needGolden = 0;             //幻化坐骑需要消耗的元宝
		int logBeforeHorseLevel = horse.getLevel();//升级之前的坐骑等级(日志记录)
		
		List<LoggerGoods> logGoodsInfos = new ArrayList<LoggerGoods>(3);//日志道具(日志记录)
		Map<Long, Integer> costUserItems = this.spliteUserItems(userItems);//解析客户端发送过来的数据
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
			} else if (propsConfig.getChildType() != PropsChildType.HORSE_FANCY_MEDICINE_TYPE) {
				return ResultObject.ERROR(BELONGS_INVALID);
			}
			
			logGoodsInfos.add(LoggerGoods.outcomeProps(userPropsId, propsConfig.getId(), propsCount));
			logUsePropsCount += propsCount;//日志记录数量
			fancyCount += propsCount;//坐骑需要幻化的次数
		}
		
		if(autoBuyCount > 0){
			PropsConfig propsConfig = propsManager.getPropsConfig(HorseRule.HORSE_EXP_PROPS);
			if(propsConfig == null){
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			}
			needGolden = propsConfig.getMallPrice() * autoBuyCount;
			if(player.getGolden() < needGolden){
				return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
			}
			
			fancyCount += autoBuyCount;//增加坐骑幻化次数
		}
		
		
		int horseLevel = horse.getLevel(); //用于计算的等级
		int horseExp = horse.getExp(); //坐骑能够增加多少经验
		Set<Integer> mountModels = new HashSet<Integer>(3);//可以拥有的坐骑皮肤
		
		for(int i = 0 ; i < fancyCount ; i++){ //预计算坐骑等级和经验
			if(horseLevel >= HorseRule.MAX_HORSE_LEVEL){
				break;
			}
			
			HorseConfig horseConfig = this.horseManager.getHorseConfig(horseLevel);
			if(horseConfig == null){
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			}
			
			int addExp =  horseConfig.getPropsExp(); //获得的经验值
			int probability = horseConfig.calcProbability();
			if (probability == 1) { // 小暴率,经验乘以系数
				minRateCount += 1;
				addExp *= HorseRule.MIN_RATE_VALUE;
			} else if (probability == 2) { // 大暴率,直接升级
				addExp = 0;
				horseLevel += 1;
				maxRateCount += 1;
				battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
			}
			
			totleupExp += addExp;//本次总共获得多少经验
			horseExp += addExp;//累计坐骑经验
			
			int[] data = caclHorseExp(horseLevel, horseExp, horseConfig);
			if(data != null){
				horseLevel = data[0];
				horseExp = data[1];
				
				horseConfig = this.horseManager.getHorseConfig(horseLevel); //更新新的皮肤
				if(horseConfig != null){
					mountModels.add(horseConfig.getModel()); //添加皮肤
				}
			}
			
		}
		
		List<UserProps> userPropslist = null;
		ChainLock lock = LockUtils.getLock(player,player.getPackLock(),horse);
		try {
			lock.lock();
			if(player.getGolden() < needGolden){
				return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
			}
			
			if(costUserItems != null && !costUserItems.isEmpty()){
				userPropslist = this.costUserPropsList(costUserItems);
				if(userPropslist == null || userPropslist.isEmpty()){
					return ResultObject.ERROR(ITEM_NOT_ENOUGH);
				}
			}
			
			player.decreaseGolden(needGolden);
			horse.setExp(horseExp);
			horse.setLevel(horseLevel);
			horse.addHorseMounts(mountModels);
			horse.setFlushable(Flushable.FLUSHABLE_NORMAL);
			battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
		}finally{
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(horse,player);//提交更新
		taskFacade.updateHorseLevelTask(playerId); //更新坐骑等级任务
		if(userPropslist != null && !userPropslist.isEmpty()){
			MessagePushHelper.pushUserProps2Client(playerId, DEFAULT_BACKPACK, false, userPropslist);//发送物品更新
		}
		
		if(needGolden > 0){//推送金币
			List<Long> playerIdList = Arrays.asList(playerId);
			List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
			UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, AttributeKeys.GOLDEN);
			logGoodsInfos.add(LoggerGoods.outcomePropsAutoBuyGolden(HorseRule.HORSE_EXP_PROPS, autoBuyCount, needGolden));//自动购买的数量
			GoldLogger.outCome(Source.HORSE_LEVEL_UP, needGolden, player, logGoodsInfos.toArray(new LoggerGoods[logGoodsInfos.size()]));//金币日志
		}
		
		if(!logGoodsInfos.isEmpty()){//道具日志
			GoodsLogger.goodsLogger(player, Source.HORSE_LEVEL_UP, logGoodsInfos.toArray(new LoggerGoods[logGoodsInfos.size()]));
		}
		
		int logAfterHorseLevel = horse.getLevel();//升级之后的坐骑等级(日志记录)
		HorseLogger.horseLevelup(player, logBeforeHorseLevel, logAfterHorseLevel, needGolden, HorseRule.HORSE_EXP_PROPS, logUsePropsCount, logGoodsInfos.toArray(new LoggerGoods[logGoodsInfos.size()]));//坐骑日志
		
		int afterlevel = (horse.getLevel() - 1) / 10 * 10;  //坐骑升级之后的等级, 修改了下 --- 超平..
		BulletinConfig config = resourceService.get(NoticeID.HORSE_LEVEL_UP, BulletinConfig.class);
		if (config != null) {
			if (config.getConditions().contains(horseLevel) && afterlevel - beforeLevel == 10) {
				HashMap<String, Object> paramsMap = new HashMap<String, Object>(3); 
				paramsMap.put(NoticeRule.playerId, playerId);
				paramsMap.put(NoticeRule.playerName, userDomain.getPlayer().getName());
				paramsMap.put(NoticeRule.horse, this.horseManager.getHorseConfig(horse.getLevel()).getName());
				NoticePushHelper.pushNotice(NoticeID.HORSE_LEVEL_UP, NoticeType.HONOR, paramsMap, config.getPriority());
			}
		}
		
		return ResultObject.SUCCESS(HorseVo.valueOf(horse, totleupExp, minRateCount, maxRateCount));
	}
	

	
	/**
	 * 计算坐骑经验
	 * @param horseLevel    坐骑当前等级
	 * @param horseExp      坐骑当前经验
	 * @param config        坐骑配置对象
	 * @return {@link Integer[]} [0]:坐骑等级 [1]:坐骑经验
	 */
	private int[] caclHorseExp(int horseLevel,int horseExp,HorseConfig config){
		if(config == null){
			return null;
		}
		
		if(horseExp < config.getLevelupExp()){
			return null;
		}
		
		horseLevel += 1;
		horseExp -= config.getLevelupExp();
		horseExp = horseExp < 0 ? 0 : horseExp;
		return new int[]{horseLevel,horseExp};
	}
	
	/**
	 * 统一扣除道具接口(注意:内部无锁)
	 * @param costUserItems 道具集合
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<UserProps> costUserPropsList(Map<Long, Integer> costUserItems) {
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
			
			UserProps userProps = propsManager.getUserProps(userItemId);
			if(userProps == null) {
				continue;
			}
			
			if(userProps.getCount() < itemCount){
				return null;
			}
			
			userProps.decreaseItemCount(itemCount);
			propsManager.removeUserPropsIfCountNotEnough(userProps);
			userPropsList.add(userProps);
		}
		
		if(!userPropsList.isEmpty()) {
			dbService.submitUpdate2Queue(userPropsList);
		}
		
		return userPropsList;
	} 
	
	
	/**
	 * 截取出字符串.
	 * 
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
}
