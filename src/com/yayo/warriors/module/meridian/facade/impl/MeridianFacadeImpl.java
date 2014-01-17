package com.yayo.warriors.module.meridian.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.meridian.constant.MeridianConstant.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.EnumUtils;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.adapter.MeridianService;
import com.yayo.warriors.basedb.model.BulletinConfig;
import com.yayo.warriors.basedb.model.MeridianConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.NoticePushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.logger.log.GoldLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.meridian.MeridianRule;
import com.yayo.warriors.module.meridian.constant.MeridianConstant;
import com.yayo.warriors.module.meridian.entity.Meridian;
import com.yayo.warriors.module.meridian.facade.MeridianFacade;
import com.yayo.warriors.module.meridian.helper.MeridianHelper;
import com.yayo.warriors.module.meridian.manager.MeridianManager;
import com.yayo.warriors.module.meridian.model.MeridianItemCheck;
import com.yayo.warriors.module.meridian.type.MeridianStage;
import com.yayo.warriors.module.meridian.type.MeridianType;
import com.yayo.warriors.module.meridian.vo.AttributeVo;
import com.yayo.warriors.module.notice.rule.NoticeRule;
import com.yayo.warriors.module.notice.type.NoticeID;
import com.yayo.warriors.module.notice.type.NoticeType;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.type.PropsChildType;
import com.yayo.warriors.module.task.facade.TaskFacade;
import com.yayo.warriors.module.title.facade.TitleFacade;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.type.ElementType;

/**
 * 经脉接口实现类
 * 
 * @author huachaoping
 */
@Component
public class MeridianFacadeImpl implements MeridianFacade {

	@Autowired
	private VOFactory voFactory;
	@Autowired
	private DbService dbService;
	@Autowired
	private TaskFacade taskFacade;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private MeridianHelper meridianHelper;
	@Autowired
	private MeridianManager meridianManager;
	@Autowired
	private MeridianService meridianService;
	@Autowired
	private TitleFacade titleFacade;
	
	/** 日志 */
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());


	
//	/**
//	 * 按类型查看经脉
//	 * 
//	 * @param playerId        玩家ID
//	 * @param meridianType    经脉类型
//	 */
//	
//	public Collection<Integer> loadCurrentMeridian(Long playerId, int meridianType) {
//		Meridian meridian = meridianManager.getMeridian(playerId);
//		if(meridian == null) {
//			return Collections.emptyList();
//		}
//		
//		Map<Integer, Collection<Integer>> meridianTypes = meridian.getMeridianTypes();
//		if(meridianTypes == null) {
//			return Collections.emptyList();
//		}
//		
//		Collection<Integer> typeMeridianIds = meridianTypes.get(meridianType);
//		if(typeMeridianIds == null) {
//			return Collections.emptyList();
//		}
//		return typeMeridianIds;
//	}

	/**
	 * 经脉属性总和 	
	 * 
	 * @param playerId        玩家ID
	 */
	
	public AttributeVo loadMeridianAttr(Long playerId) {
		AttributeVo attributeVo = new AttributeVo();
		Meridian meridian = meridianManager.getMeridian(playerId);
		if(meridian != null) {
			attributeVo.setPassStage(meridian.isStagePass());
			attributeVo.setLaveTimes(meridian.getLaveTimes());
			attributeVo.setRequiredExp(meridian.getAcquiredExp());
			this.calculateMeridianAttributes(meridian, attributeVo);
			this.calculateMeridianTypeCounts(meridian, attributeVo);
		}
		return attributeVo;
	}
	
	/**
	 * 计算每条脉点亮数量
	 * 
	 * @param meridian
	 * @param attributeVo
	 */
	private void calculateMeridianTypeCounts(Meridian meridian, AttributeVo attributeVo) {
		Map<Integer, Collection<Integer>> meridianTypes = meridian.getMeridianTypes();
		if (meridianTypes == null || meridianTypes.isEmpty()) {
			return;
		}
		for (Entry<Integer, Collection<Integer>> entry : meridianTypes.entrySet()) {
			Integer attribute = entry.getKey();
			Collection<Integer> collection = entry.getValue();
			if(attribute == null || collection == null) {
				continue;
			}

			MeridianType meridianType = EnumUtils.getEnum(MeridianType.class, attribute);
			if(meridianType == null) {
				continue;
			}
			
			int size = collection.size();
			switch (meridianType) {
				case YANGWEI_MERIDIAN:	attributeVo.setSmallMeridian1(size); 	break;	//阳维脉
				case YINWEI_MERIDIAN:	attributeVo.setSmallMeridian2(size);	break;	//阴维脉
				case YANGQIAO_MERIDIAN:	attributeVo.setSmallMeridian3(size);	break;
				case YINQIAO_MERIDIAN: 	attributeVo.setSmallMeridian4(size);	break;
				case DAI_MERIDIAN:		attributeVo.setSmallMeridian5(size);	break;
				case CHONG_MERIDIAN:	attributeVo.setSmallMeridian6(size);	break;
				case JEN_MERIDIAN:		attributeVo.setSmallMeridian7(size);	break;
				case GOVERNOR_MERIDIAN:	attributeVo.setSmallMeridian8(size);	break;
				
				case BIG_YANGWEI:		attributeVo.setBigMeridian1(size);	    break;
				case BIG_YINWEI:		attributeVo.setBigMeridian2(size);		break;
				case BIG_YANGQIAO:		attributeVo.setBigMeridian3(size);	    break;
				case BIG_YINQIAO:		attributeVo.setBigMeridian4(size);	    break;
				case BIG_DAI:			attributeVo.setBigMeridian5(size);		break;
				case BIG_CHONG:			attributeVo.setBigMeridian6(size);		break;
				case BIG_JEN:			attributeVo.setBigMeridian7(size);		break;
				case BIG_GOVERNOR:		attributeVo.setBigMeridian8(size);	    break;
			}
		}
	}
	
	/**
	 * 计算经脉已加成属性值
	 * 
	 * @param meridian
	 * @param attributeVo
	 */
	private void calculateMeridianAttributes(Meridian meridian, AttributeVo attributeVo) {
		Map<Integer, Integer> meridianAttributes = meridian.getMeridianAttributes();
		if (meridianAttributes == null) {
			return;
		}
		
		List<Object> attributes = Arrays.asList(attributeVo.getAttributes());
		Object[] values = attributeVo.getValues();
		
		for (Entry<Integer, Integer> entry : meridianAttributes.entrySet()) {
			Integer attribute = entry.getKey();
			Integer attrValue = entry.getValue();
			if(attribute == null || attrValue == null) {
				continue;
			}
			
			switch (attribute) {
			    case AttributeKeys.HIT:  values[attributes.indexOf(AttributeKeys.HIT)] = attrValue;				               break;
				case AttributeKeys.DODGE:  values[attributes.indexOf(AttributeKeys.DODGE)] = attrValue;			               break;
				case AttributeKeys.HP_MAX:  values[attributes.indexOf(AttributeKeys.HP_MAX)] = attrValue;			           break;
				case AttributeKeys.MP_MAX:  values[attributes.indexOf(AttributeKeys.MP_MAX)] = attrValue;			           break;
				case AttributeKeys.THEURGY_ATTACK:  values[attributes.indexOf(AttributeKeys.THEURGY_ATTACK)] = attrValue;      break;
				case AttributeKeys.THEURGY_DEFENSE:  values[attributes.indexOf(AttributeKeys.THEURGY_DEFENSE)] = attrValue;	   break;
				case AttributeKeys.PHYSICAL_ATTACK:  values[attributes.indexOf(AttributeKeys.PHYSICAL_ATTACK)] = attrValue;	   break;
				case AttributeKeys.THEURGY_CRITICAL:  values[attributes.indexOf(AttributeKeys.THEURGY_CRITICAL)] = attrValue;  break;
				case AttributeKeys.PHYSICAL_DEFENSE:  values[attributes.indexOf(AttributeKeys.PHYSICAL_DEFENSE)] = attrValue;  break;
				case AttributeKeys.PHYSICAL_CRITICAL:  values[attributes.indexOf(AttributeKeys.PHYSICAL_CRITICAL)] = attrValue;break;
			}
		}
		
		attributeVo.setAttributes(attributeVo.getAttributes());
		attributeVo.setValues(values);
	}

	/**
	 * 运气冲穴
	 * 
	 * @param playerId          玩家ID
	 * @param meridianId        经脉点ID
	 * @param userItems         用户道具id_数量|...
	 * @return {@link ResultObject}
	 */
	@SuppressWarnings("unchecked")
	
	public ResultObject<Collection<BackpackEntry>> rushMeridian(Long playerId, int meridianId, String userItems, int autoCount) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle playerBattle = userDomain.getBattle();
		if (playerBattle.getLevel() < MeridianRule.OPEN_LEVEL) {
			return ResultObject.ERROR(LEVEL_INVALID);
		} else if (playerBattle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		} 
		
		Meridian meridian = meridianManager.getMeridian(playerId);
		if(meridian == null) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int stage = this.validateMeridianStage(playerId);
		MeridianConfig meridianConfig = meridianManager.getMeridianConfig(meridianId);
		if (meridianConfig == null) {
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		} else if (playerBattle.getJob().ordinal() != meridianConfig.getJob()) {
			return ResultObject.ERROR(PLAYER_CLAZZ_INVALID);
		} else if (meridianConfig.getMeridianStage() != stage) {
			return ResultObject.ERROR(TYPE_INVALID);
		}
		
		// 脉点验证
		int chain = meridianConfig.getChain();
		Set<Integer> meridiansSet = meridian.getMeridiansSet();
		if (chain != 0 && !meridiansSet.contains(chain) || meridiansSet.contains(meridianId)) {
			return ResultObject.ERROR(MERIDIAN_POINT_INVALID);
		}
		
		//冲穴道具验证
		int needItemCount = MeridianRule.LUCKITEM;
		int propsChildType = PropsChildType.MERIDIAN_LUCK_PROPS_TYPE;
		int requiredBaseId = meridianConfig.getRequiredPropsChildType();
		if (requiredBaseId > 0) { //大周天
			PropsConfig propsConfig = propsManager.getPropsConfig(requiredBaseId);
			if (propsConfig == null) {
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			}
			propsChildType = propsConfig.getChildType();    
		}
		
		ResultObject<MeridianItemCheck> validResult = validateProps(playerId, propsChildType, userItems);
		if (validResult.getResult() < SUCCESS) {
			return ResultObject.ERROR(validResult.getResult());
		}
		
		MeridianItemCheck meridianItemCheck = validResult.getValue();
		if(meridianItemCheck == null) {
			return ResultObject.ERROR(LACK_CONDITION);
		} 
		
		Map<Long, Integer> userItemCounts = meridianItemCheck.getUserItems();
		int totalCount = meridianItemCheck.getTotalCount();
		
		int autoCost    = 0;
		int probability = 0;
		if (propsChildType == PropsChildType.MERIDIAN_LUCK_PROPS_TYPE) {
			if (totalCount > needItemCount) {
				return ResultObject.ERROR(COUNT_LIMIT);
			}
			int addedPercent = 0;
			int luckItemBaseId = meridianItemCheck.getBaseId();
			if (luckItemBaseId != 0) {
				PropsConfig config = propsManager.getPropsConfig(luckItemBaseId);
				addedPercent = totalCount * config.getAttrValueInt();
			}
			probability = addedPercent + meridianConfig.getProbability();
		} else {
			if (totalCount + autoCount != meridianConfig.getRequiredCount()) {
				return ResultObject.ERROR(COUNT_LIMIT);
			}
			
			PropsConfig config = propsManager.getPropsConfig(requiredBaseId);
			if (config == null) {
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			}
			autoCost    = autoCount * config.getMallPrice();
			probability = meridianConfig.getProbability();
			meridianItemCheck.addLogGoods(requiredBaseId, autoCount, autoCost);
		}
		
		int result = SUCCESS;
		ChainLock lock = LockUtils.getLock(player, playerBattle, meridian);
		try {
			lock.lock();
			if(player.getGolden() < autoCost){
				return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
			}
			
			if (playerBattle.getGas() < meridianConfig.getRequiredGas()) {
				return ResultObject.ERROR(GAS_NOT_ENOUGH);
			}
			
			
			// 冲脉机率
			if (!MeridianRule.successRatio(probability)) {
				result = RUSH_MERIDIAN_FAILURE;
			} else {
				meridiansSet = meridian.getMeridiansSet();
				if (chain != 0 && !meridiansSet.contains(chain) || meridiansSet.contains(meridianId)) {
					return ResultObject.ERROR(MERIDIAN_POINT_INVALID);
				}
				meridiansSet.add(meridianId);
				meridian.updateMeridianSet();
				playerBattle.setFlushable(Flushable.FLUSHABLE_NORMAL);
			}
			
			player.decreaseGolden(autoCost);
			playerBattle.decreaseGas(meridianConfig.getRequiredGas());
			dbService.submitUpdate2Queue(meridian, playerBattle, player);
		} finally {
			lock.unlock();
		}
		
		taskFacade.updateRushMeridianTask(playerId);
		List<UserProps> costUserPropsList = propsManager.costUserPropsList(userItemCounts);
		Collection<BackpackEntry> backpackEntries = voFactory.getUserPropsEntries(costUserPropsList);
		
		List<LoggerGoods> logGoods = meridianItemCheck.getLogGoods();
		LoggerGoods[] goodsInfo = logGoods.toArray(new LoggerGoods[logGoods.size()]);
		if (goodsInfo.length > 0) {
			GoodsLogger.goodsLogger(player, Source.LIGHT_MERIDIAN, goodsInfo);
		}
		
		if(autoCost > 0) {
			GoldLogger.outCome(Source.LIGHT_MERIDIAN, autoCost, player, goodsInfo);
		}
		
		if (result == SUCCESS) {
			GameMap gameMap = userDomain.getCurrentScreen().getGameMap();
			Collection<Long> playerIds = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER, playerId);
			playerIds.remove(playerId);                                                           // 不推给自己  
			meridianHelper.pushMeridianTime(playerId, playerIds);                                 // 推送同屏玩家显示倒计时
			passTypeMeidians(player, playerBattle.getJob().ordinal(), meridianConfig.getMeridianType());
		}		
		return ResultObject.valueOf(result, backpackEntries);
	}

	
	/**
	 * 突破瓶颈
	 * 
	 * @param playerId          玩家ID
	 * @param userItems         格式: 用户道具id_数量|...
	 * @return {@link MeridianConstant}
	 */
	
	@SuppressWarnings("unchecked")
	public int breakthrough(Long playerId, String userItems) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		PlayerBattle playerBattle = userDomain.getBattle();
		if (playerBattle.getGas() < MeridianRule.BREAK_VALUE) {
			return GAS_NOT_ENOUGH;
		}
		
		Meridian meridian = meridianManager.getMeridian(playerId);
		if(meridian == null) {
			return FAILURE;
		}
		
		int stage = this.validateMeridianStage(playerId);
		if (stage != MeridianStage.XIAOZHOUTIAN.ordinal()) {
			return LACK_CONDITION;
		}
		
		int childType = PropsChildType.MERIDIAN_DRAGON_PROPS_TYPE;
		ResultObject<MeridianItemCheck> result = this.validateProps(playerId, childType, userItems);
		if (result.getResult() != SUCCESS) {
			return result.getResult();
		}
		
		MeridianItemCheck check = result.getValue();
		if (check == null) {
			return LACK_CONDITION;
		}
		
		int requiredCount = check.getTotalCount();
		if (requiredCount != MeridianRule.COUNT) {
			return COUNT_LIMIT;
		}
		
		Map<Long, Integer> userItemCount = check.getUserItems();
		ChainLock lock = LockUtils.getLock(playerBattle, meridian);
		try {
			lock.lock();
			meridian.setStagePass(true);
			playerBattle.decreaseGas(MeridianRule.BREAK_VALUE);
		} catch (Exception e) {
			LOGGER.error("{}", e);
			meridian.setStagePass(false);
			playerBattle.increaseGas(MeridianRule.BREAK_VALUE);
			return FAILURE;
		} finally {
			lock.unlock();
		}
		
		List<LoggerGoods> logGoods = check.getLogGoods();
		dbService.submitUpdate2Queue(playerBattle, meridian);
		List<UserProps> costUserPropsList = propsManager.costUserPropsList(userItemCount);
		propsManager.removeUserPropsIfCountNotEnough(playerId, BackpackType.DEFAULT_BACKPACK, costUserPropsList);
		MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, costUserPropsList);
		LoggerGoods[] logGoodsArray = logGoods.toArray(new LoggerGoods[logGoods.size()]);
//		MeridianLogger.meridianStageUp(playerId, stage, MeridianStage.DAZHOUTIAN.ordinal(), logGoodsArray);
		GoodsLogger.goodsLogger(userDomain.getPlayer(), Source.MERIDIAN_STAGEUP, logGoodsArray);              // 物品日志
		pushNotice2Client(userDomain.getPlayer());
		return SUCCESS;
	}
	
	/**
	 * 冲穴道具验证
	 * 
	 * @param  playerId              	角色ID
	 * @param  childType                道具子类型
	 * @param  userItems             	格式:用户道具ID_数量|.....
	 * @return {@link ResultObject}		返回值对象
	 */
	private ResultObject<MeridianItemCheck> validateProps(Long playerId, int childType, String userItems) {
		List<String[]> arrays = Tools.delimiterString2Array(userItems);
		MeridianItemCheck itemCheck = new MeridianItemCheck();
		if(arrays == null || arrays.isEmpty()) {
			return ResultObject.SUCCESS(itemCheck);
		}
		
		int backpack = BackpackType.DEFAULT_BACKPACK;
		for (String[] array : arrays) {
			Long userPropsId = Long.valueOf(array[0]);
			Integer itemCount = Integer.valueOf(array[1]);
			if(userPropsId == null || itemCount == null) {
				continue;
			}
			
			UserProps userProps = propsManager.getUserProps(userPropsId);
			if(userProps == null) {
				return ResultObject.ERROR(ITEM_NOT_FOUND);
			} else if(!userProps.validBackpack(backpack)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if(userProps.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			}  else if(userProps.isTrading()) {
				return ResultObject.ERROR(ITEM_CANNOT_USE);
			}
			
			int baseId = userProps.getBaseId();
			PropsConfig propsConfig = propsManager.getPropsConfig(baseId);
			if(propsConfig == null) {
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			}
			
			if(propsConfig.getChildType() != childType) {
				return ResultObject.ERROR(TYPE_INVALID);
			}
			
			int cacheCount = itemCheck.getItemCount(userPropsId);
			if (userProps.getCount() < cacheCount + itemCount) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			}
			
			itemCheck.setBaseId(baseId);
			itemCheck.addItemCount(userPropsId, itemCount);
			itemCheck.addLogGoods(userPropsId, baseId, itemCount);
		}
		return ResultObject.SUCCESS(itemCheck);
	}
	
	/**
	 * 阶段验证
	 * 
	 * @param playerId
	 * @return {@link MeridianConstant}
	 */
	
	public int validateMeridianStage(Long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		if (player == null) {
			return PLAYER_NOT_FOUND;
		}

		Meridian meridian = meridianManager.getMeridian(playerId);
		if(meridian == null) {
			return FAILURE;
		}
		
		int stage = MeridianStage.XIAOZHOUTIAN.ordinal();
		int totalCount = meridianService.getStageCount();
		int currCount = meridian.getMeridiansSet().size();
		if (currCount <= totalCount && !meridian.isStagePass()) {
			return stage;
		}
		return MeridianStage.DAZHOUTIAN.ordinal();
	}
	 
	/**
	 * 范围内玩家吸纳经验
	 * 
	 * @param playerId
	 * @return {@link MeridianConstant}
	 */
	
	public int addPlayersExp(long playerId) {
//		UserDomain userDomain = userManager.getUserDomain(playerId);
//		if (userDomain == null) {
//			return PLAYER_NOT_FOUND;
//		}
//		
//		GameMap gameMap = userDomain.getCurrentScreen().getGameMap();
//		Collection<Long> playerIds = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
//		if (playerIds == null) {
//			return SUCCESS;
//		}
//		if (playerIds.isEmpty()) {
//			return SUCCESS;
//		}
//		
//		Set<Long> postPlayerIds = new HashSet<Long>();
//		for (Long memberId : playerIds) {
//			Meridian meridian = meridianManager.getMeridian(memberId);
//			if(playerId != memberId && meridian == null || meridian.getLaveTimes() <= 0) {
//				continue;
//			}
//			
//			UserDomain targetDomain = userManager.getUserDomain(memberId);
//			if(targetDomain == null) {
//				continue;
//			}
//			PlayerBattle battle = targetDomain.getBattle();
//			if (battle.isDeath()) {
//				continue;
//			}
//			
//			ChainLock lock = LockUtils.getLock(battle, meridian);
//			try {
//				lock.lock();
//				if (playerId != memberId && meridian.getLaveTimes() <= 0) {
//					continue;
//				}
//				
//				int awardExp = playerId != memberId
//						? FormulaHelper.invoke(PLAYER_ADD_EXP_FORMULA, battle.getLevel()).intValue()
//						: FormulaHelper.invoke(MERIDIAN_GET_EXP_FORMULA, userDomain.getBattle().getLevel()).intValue();
//				
//				int beforeExp = battle.getExp();
//				
//				battle.increaseExp(awardExp);
//				postPlayerIds.add(memberId);
//				meridian.increaseAcquiredExp(awardExp);  
//				if (playerId != memberId) {
//					meridian.decreaseTimes();
//				}
//				if(awardExp != 0) {                          // 经验日志
//					ExpLogger.exp(targetDomain.getId(), Source.EXP_MERIDIAN_RANGE_ADD, beforeExp, awardExp, battle.getExp(), battle.getLevel(), targetDomain.getPlayer());
//				}
//			} finally {
//				lock.unlock();
//			}
//			dbService.submitUpdate2Queue(meridian);
//			List<Long> receiver = Arrays.asList(memberId);
//			List<UnitId> playerUnits = Arrays.asList(UnitId.valueOf(memberId, ElementType.PLAYER));
//			UserPushHelper.pushAttribute2AreaMember(memberId, receiver, playerUnits, AttributeRule.PLAYER_EXP);
//		}
//		
//		postPlayerIds.add(playerId);
//		meridianHelper.pushPlayerAddExp(playerId, userDomain.getPlayer().getName(), postPlayerIds);
		return SUCCESS;     // TODO 策划需求, 暂时屏蔽
	}
	
	/**
	 * 经脉荣誉公告
	 * @param player        角色对象
	 */
	private void pushNotice2Client(Player player) {
		BulletinConfig config = NoticePushHelper.getConfig(NoticeID.MERIDIAN_STAGE_UP, BulletinConfig.class);
		if (config != null) {
			Map<String, Object> paramsMap = new HashMap<String, Object>(3);
			paramsMap.put(NoticeRule.playerId, player.getId());
			paramsMap.put(NoticeRule.playerName, player.getName());
			paramsMap.put(NoticeRule.meridians, "大周天");
			NoticePushHelper.pushNotice(NoticeID.MERIDIAN_STAGE_UP, NoticeType.HONOR, paramsMap, config.getPriority());
		}
	}

	
	/**
	 * 经脉达成条件(称号, 公告)
	 * 
	 * @param meridian
	 * @param roleJob
	 */
	private void passTypeMeidians(Player player, int roleJob, int meridianType) {
		Meridian meridian = meridianManager.getMeridian(player.getId());
		MeridianType typeEnum = EnumUtils.getEnum(MeridianType.class, meridianType);
		Map<Integer, Collection<Integer>> typePoints = meridian.getMeridianTypes();
		BulletinConfig config = NoticePushHelper.getConfig(NoticeID.MERIDIAN_TYPE_FULL, BulletinConfig.class);
		int passType = 0;                       // 贯通经脉数(一条脉)
		for (int i = 0; i <= MeridianRule.TYPE_LIMIT; i++) {
			List<Integer> points = meridianManager.getMerdianConfigByType(roleJob, i);
			Collection<Integer> passPoints = typePoints.get(i);
			if (passPoints != null && passPoints.containsAll(points)) {
				if (meridianType == i && config != null) {
					Map<String, Object> paramsMap = new HashMap<String, Object>(3);
					paramsMap.put(NoticeRule.playerId, player.getId());
					paramsMap.put(NoticeRule.playerName, player.getName());
					paramsMap.put(NoticeRule.meridianType, typeEnum.getName());
					NoticePushHelper.pushNotice(NoticeID.MERIDIAN_TYPE_FULL, NoticeType.HONOR, paramsMap, config.getPriority());
				}
				passType ++;
			}
		}
		if (passType != 0) titleFacade.obtainNewTitleRelationMeridian(player.getId(), passType);
	}
	
	
}
