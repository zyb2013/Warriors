package com.yayo.warriors.module.mortal.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.mortal.constant.MortalConstant.*;
import static com.yayo.warriors.module.mortal.rule.MortalRule.*;
import static com.yayo.warriors.module.pack.type.BackpackType.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.EnumUtils;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.model.MortalBodyConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.NoticePushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.logger.log.GoldLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.log.SilverLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.mortal.entity.UserMortalBody;
import com.yayo.warriors.module.mortal.facade.MortalFacade;
import com.yayo.warriors.module.mortal.helper.MortalPushHelper;
import com.yayo.warriors.module.mortal.manager.MortalManager;
import com.yayo.warriors.module.mortal.type.MortalBodyType;
import com.yayo.warriors.module.mortal.vo.MortalBodyVo;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.type.PropsChildType;
import com.yayo.warriors.module.task.facade.TaskFacade;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Flushable;

/**
 * 肉身系统接口实现类
 * 
 * @author huachaoping
 */
@Component
public class MortalFacadeImpl implements MortalFacade {

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
	private MortalManager mortalManager;
	
	/**
	 * 肉身升级
	 * 
	 * @param  playerId         角色ID
	 * @param  mortalType       类型
	 * @param  userItems        升级所需道具:用户道具ID_数量|...
	 * @param  useProps         是否使用加概率道具
	 * @param  userPropsId      增加概率的用户道具ID
	 * @param  autoCount        自动购买的数量
	 * @return {@link ResultObject}
	 */
	@SuppressWarnings("unchecked")
	
	public ResultObject<List<BackpackEntry>> mortalBodyLevelUp(long playerId, int type, String userItems,
			                                        boolean useProps, long userPropsId, int autoCount) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		} 
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		int userLevel = battle.getLevel();
		int roleJob = battle.getJob().ordinal();
		if (userLevel < USER_LEVEL) {
			return ResultObject.ERROR(LEVEL_INVALID);
		}
		
		MortalBodyType mortalType = EnumUtils.getEnum(MortalBodyType.class, type);
		if (mortalType == null) {
			return ResultObject.ERROR(TYPE_INVALID);
		}
		
		int requiredCount = 0;
		List<LoggerGoods> goodsInfos = new ArrayList<LoggerGoods>();
		Map<Long, Integer> userBackItems = spliteUserItems(userItems);
		for (Map.Entry<Long, Integer> entry : userBackItems.entrySet()) {
			long propsId    = entry.getKey();
			int  propsCount = entry.getValue();
			if (propsCount <= 0) {
				return ResultObject.ERROR(INPUT_VALUE_INVALID);
			}
			
			UserProps userProps = propsManager.getUserProps(propsId);
			if (userProps == null) {
				return ResultObject.ERROR(ITEM_NOT_FOUND);
			} else if (userProps.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			} else if (userProps.getBackpack() != DEFAULT_BACKPACK) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if (userProps.getCount() < propsCount) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			}
			
			int childType = PropsChildType.MORTAL_LEVEL_UP_PROPS_TYPE;
			int baseId = userProps.getBaseId();
			PropsConfig config = propsManager.getPropsConfig(baseId);
			if (config == null) {
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			} else if (config.getChildType() != childType) {
				return ResultObject.ERROR(TYPE_INVALID);
			}
			requiredCount += propsCount;
			goodsInfos.add(LoggerGoods.outcomeProps(propsId, baseId, propsCount));
		}
		
		UserProps userProps = null;
		if (useProps) {                                         // 验证加概率道具
			userProps = propsManager.getUserProps(userPropsId);
			if (userProps == null) {
				return ResultObject.ERROR(ITEM_NOT_FOUND);
			} else if (userProps.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			} else if (userProps.getBackpack() != DEFAULT_BACKPACK) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if (userProps.getCount() < 1) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			}
			int childType = PropsChildType.MORTAL_PERFECT_PROPS_TYPE;
			int baseId = userProps.getBaseId();
			PropsConfig config = propsManager.getPropsConfig(baseId);
			if (config == null) {
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			} else if (config.getChildType() != childType) {
				return ResultObject.ERROR(TYPE_INVALID);
			}
			goodsInfos.add(LoggerGoods.outcomeProps(userPropsId, baseId, 1));
		} 

		UserMortalBody mortal = mortalManager.getUserMortalBody(battle);
		int level = mortal.getMortalLevel(type);
		if (level >= MAX_LEVEL) {
			return ResultObject.ERROR(LEVEL_FULLED);
		} 
		
		level ++;                                                                          // 升级后等级        
		MortalBodyConfig config = mortalManager.getMorbodyConfig(roleJob, type, level);    // 升级成功的肉身配置
		if (config == null) {
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		} else if (config.getRequiredCount() != requiredCount + autoCount) {
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		} 
		
		int requiredBaseId = config.getRequiredPropsId();
		PropsConfig propsConfig = propsManager.getPropsConfig(requiredBaseId);
		if (propsConfig == null) {
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		int autoBuyCost = calAutoBuyCount(autoCount, propsConfig.getMallPrice());
		
		ChainLock lock = LockUtils.getLock(player, mortal, player.getPackLock());
		int result = SUCCESS;
		boolean clearCache = false;
		try {
			lock.lock();
			if (player.getSilver() <= config.getRequiredMoney()) {
				return ResultObject.ERROR(SILVER_NOT_ENOUGH);
			}
			if (player.getGolden() < autoBuyCost) {
				return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
			}
			int curProbability = config.getProbability();
			int maxProbability = config.getMaxProbability();
			int probability = useProps ? maxProbability : curProbability;
			if (!successRatio(probability, maxProbability)) {
				result = LEVEL_UP_FAILURE;
			} else {
				mortal.putMortalLevel(type, level);
				mortal.updateMortalBodyMap();
			}
			if (userProps != null) {
				userProps.decreaseItemCount(1);                 // 炼骨丹道具只需一个
				clearCache = userProps.getCount() <= 0;
				dbService.submitUpdate2Queue(userProps);
			}
			
			player.decreaseGolden(autoBuyCost);             // 扣除自动购买元宝
			player.decreaseSilver(config.getRequiredMoney());   // 扣除升级铜币
			battle.setFlushable(Flushable.FLUSHABLE_NORMAL); 
			dbService.submitUpdate2Queue(player, mortal);
			if (clearCache) {
				propsManager.put2UserPropsIdsList(playerId, DROP_BACKPACK, userProps);
				propsManager.removeFromUserPropsIdsList(playerId, DEFAULT_BACKPACK, userProps);
			}
		} finally {
			lock.unlock();
		}
		
		List<UserProps> costUserPropsList = propsManager.costUserPropsList(userBackItems);
		
		List<BackpackEntry> backpackEntries = voFactory.getUserPropsEntries(costUserPropsList);
		BackpackEntry backpackEntry = voFactory.getUserPropsEntry(userProps);
		if (userProps != null) {
			backpackEntries.add(backpackEntry);
		}
		if (result == SUCCESS) {
			NoticePushHelper.pushNoticeMessage2Client(player, battle, mortal, level);
			pushChangeMessage2Client(playerId, mortalType, level, config);
		}
		
		taskFacade.updateMortalLevelUpTask(playerId);
		
		if (autoCount > 0) {
			goodsInfos.add(LoggerGoods.outcomePropsAutoBuyGolden(requiredBaseId, autoCount, autoBuyCost));
			GoldLogger.outCome(Source.MORTAL_LEVELUP, autoBuyCost, player, goodsInfos.toArray(new LoggerGoods[goodsInfos.size()]));
		}
		if (goodsInfos.size() > 0) {
			GoodsLogger.goodsLogger(player, Source.MORTAL_LEVELUP, goodsInfos.toArray(new LoggerGoods[goodsInfos.size()]));
		}
		SilverLogger.outCome(Source.MORTAL_LEVELUP, config.getRequiredMoney(), player, goodsInfos.toArray(new LoggerGoods[goodsInfos.size()]));         // 银币日志
		return ResultObject.valueOf(result, backpackEntries);
	}

	/**
	 * 获得所有加成属性
	 * @param playerId
	 * @return {@link MortalBodyVo}
	 */
	
	public MortalBodyVo getAllAttribute(long playerId) {
		MortalBodyVo mortalbodyVo = new MortalBodyVo();
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return mortalbodyVo;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		Fightable fightable = mortalManager.getAttributeValue(battle);
		Map<Object, Integer> attributeMap = fightable.getAttributes();
		if (attributeMap.isEmpty()) {
			return mortalbodyVo;
		}
		
		List<Object> attributes = Arrays.asList(mortalbodyVo.getAttributes());
		Object[] values = mortalbodyVo.getValues();
		
		for (Map.Entry<Object, Integer> entry : attributeMap.entrySet()) {
			Integer attribute = (Integer) entry.getKey();
			Integer attrValue = entry.getValue();
			if (attrValue == null || attribute == null) {
				continue;
			}
			switch (attribute) {
				case AttributeKeys.HIT:  			  values[attributes.indexOf(AttributeKeys.HIT)] = attrValue; 			   break;
				case AttributeKeys.DODGE: 			  values[attributes.indexOf(AttributeKeys.DODGE)] = attrValue;             break;
				case AttributeKeys.HP_MAX:            values[attributes.indexOf(AttributeKeys.HP_MAX)] = attrValue;            break;
				case AttributeKeys.MP_MAX:            values[attributes.indexOf(AttributeKeys.MP_MAX)] = attrValue;            break;
				case AttributeKeys.THEURGY_ATTACK: 	  values[attributes.indexOf(AttributeKeys.THEURGY_ATTACK)] = attrValue;    break;
				case AttributeKeys.PHYSICAL_ATTACK:   values[attributes.indexOf(AttributeKeys.PHYSICAL_ATTACK)] = attrValue;   break;
				case AttributeKeys.THEURGY_DEFENSE:   values[attributes.indexOf(AttributeKeys.THEURGY_DEFENSE)] = attrValue;   break;
				case AttributeKeys.PHYSICAL_DEFENSE:  values[attributes.indexOf(AttributeKeys.PHYSICAL_DEFENSE)] = attrValue;  break;
				case AttributeKeys.THEURGY_CRITICAL:  values[attributes.indexOf(AttributeKeys.THEURGY_CRITICAL)] = attrValue;  break;
				case AttributeKeys.PHYSICAL_CRITICAL: values[attributes.indexOf(AttributeKeys.PHYSICAL_CRITICAL)] = attrValue; break;
			}
		}
		
		
		UserMortalBody body = mortalManager.getUserMortalBody(battle);
		Map<Integer, Integer> levelMap = body.getMortalBodyMap();
		if (levelMap == null || levelMap.isEmpty()) {
			return mortalbodyVo;
		}
		for (Map.Entry<Integer, Integer> entry : levelMap.entrySet()) {
			Integer type  = entry.getKey();
			Integer level = entry.getValue();
			if (type == null || level == null) {
				continue;
			}
			MortalBodyType mortalType = EnumUtils.getEnum(MortalBodyType.class, type);
			if (mortalType == null) {
				continue;
			}
			switch (mortalType) {
				case WUZHIJI:     mortalbodyVo.setMortalBody3(level); break;
				case XUELUNHUI:	  mortalbodyVo.setMortalBody4(level); break;
				case DUOZAOHUA:   mortalbodyVo.setMortalBody7(level); break;
				case WOSHENGSI:   mortalbodyVo.setMortalBody5(level); break;
				case QIEYINYANG:  mortalbodyVo.setMortalBody0(level); break;
				case POCANGQIONG: mortalbodyVo.setMortalBody2(level); break;
				case DONGQIANKUN: mortalbodyVo.setMortalBody1(level); break;
				case ZHUANNIEPAN: mortalbodyVo.setMortalBody6(level); break;
			}
		}
		return mortalbodyVo;
	}

	
	/**
	 * 截取用户道具信息字符串
	 * @param userItems
	 * @return {@link Map}
	 */
	private Map<Long, Integer> spliteUserItems(String userItems) {
		Map<Long, Integer> maps = new HashMap<Long, Integer>();
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
	
	
	
	/**
	 * 推送属性变化给客户端
	 * @param playerId
	 * @param mortalType
	 * @param level
	 * @param config
	 */
	private void pushChangeMessage2Client(long playerId, MortalBodyType mortalType, int level, MortalBodyConfig config) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return;
		}
		
		PlayerBattle playerBattle = userDomain.getBattle();
		int[] attributes = ArrayUtils.addAll(config.getAttributes(), CHANGE_PARAM);                   // 变化的属性类型
		int[] values = new int[attributes.length];   	                                            	// 变化的属性值  
		Fightable fightable = mortalManager.getAttributeValue(playerBattle);
		Map<Object, Integer> attributeMap = fightable.getAttributes();
		for (int i = 0; i <= attributes.length - 1; i++) {
			values[i] = attributeMap.get(attributes[i]) == null ? 0 : attributeMap.get(attributes[i]);
		}
		MortalPushHelper.pushChangeAttr2Client(playerId, mortalType.ordinal(), level, attributes, values);
	}
}
