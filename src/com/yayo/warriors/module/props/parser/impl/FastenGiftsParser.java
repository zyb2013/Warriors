package com.yayo.warriors.module.props.parser.impl;

import static com.yayo.warriors.constant.CommonConstant.*;

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
import com.yayo.warriors.basedb.adapter.FastenGiftService;
import com.yayo.warriors.basedb.model.CoolTimeConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.basedb.model.PropsGiftConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.cooltime.entity.UserCoolTime;
import com.yayo.warriors.module.drop.manager.DropManager;
import com.yayo.warriors.module.drop.model.DropRewards;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.logger.log.CouponLogger;
import com.yayo.warriors.module.logger.log.GoldLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.log.SilverLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Orient;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.module.props.model.PropsStackResult;
import com.yayo.warriors.module.props.parser.AbstractEffectParser;
import com.yayo.warriors.module.props.rule.EquipHelper;
import com.yayo.warriors.module.props.rule.PropsHelper;
import com.yayo.warriors.module.props.type.PropsChildType;
import com.yayo.warriors.module.props.type.PropsType;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.type.ElementType;
import com.yayo.warriors.type.GoodsType;

/**
 * 礼包类物品解析器
 * @author liuyuhua
 */
@Component
public class FastenGiftsParser extends AbstractEffectParser {

	@Autowired
	private PropsManager propsManager;
	@Autowired
	private FastenGiftService giftService;
	@Autowired
	private DbService dbService;
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private DropManager dropManager;
	
	
	protected int getType() {
		return PropsType.GIFTS_TYPE;
	}
	
	
	public int effect(UserDomain userDomain, UserCoolTime userCoolTime, CoolTimeConfig coolTime, UserProps userProps, int count) {
		if(!userProps.validBackpack(BackpackType.DEFAULT_BACKPACK)) {
			return NOT_IN_BACKPACK;
		}
		
		int baseId = userProps.getBaseId();
		PropsConfig propsConfig = propsManager.getPropsConfig(baseId);
		if(propsConfig == null) {
			return ITEM_NOT_FOUND;
		} else if(propsConfig.getCdId() > 0 && coolTime == null){
			return BASEDATA_NOT_FOUND;
		}
		
		int result = FAILURE;
		int childType = propsConfig.getChildType();
		switch (childType) {
		case PropsChildType.FASTEN_GIFI_TYPE:	result = fastenGifis(userDomain, userProps, count, propsConfig, userCoolTime, coolTime); 		break;
		case PropsChildType.RAND_GIFI_TYPE:	    result = randGifis(userDomain, userProps, count, propsConfig, userCoolTime, coolTime); 			break;
	    }
		
		return result;
	}
	
	/**
	 * 固定礼包
	 * @param player         玩家的对象
	 * @param userProps      道具对象
	 * @param count          数量
	 * @param propConfig     道具配置
	 * @return {@link CommonConstant} 道具模块公共返回常量
	 */
	private int fastenGifis(UserDomain userDomain,UserProps userProps,int count,PropsConfig propConfig, UserCoolTime userCoolTime, CoolTimeConfig coolTime){
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		long playerId = player.getId();
		int giftNo = (int)Math.round(propConfig.getAttrValue());
		List<PropsGiftConfig> configs = giftService.getGift(battle.getJob(), giftNo);
		if(configs == null){
			return ITEM_NOT_FOUND;
		}
		
		int rewardPackSize = 0;
		int packSize = propsManager.getBackpackSize(playerId, BackpackType.DEFAULT_BACKPACK);
		ChainLock lock = LockUtils.getLock(player.getPackLock(), userCoolTime);
		lock.lock();
		try {
		    if(userProps.getCount() < count) {
				return ITEM_NOT_ENOUGH;
			} else if(coolTime != null && userCoolTime.isCoolTiming(coolTime.getId())) {
				return COOL_TIMING;
			}
		    
		    if(coolTime != null){
		    	userCoolTime.addCoolTime(coolTime.getId(), coolTime.getCoolTime());
		    }
		    
		    List<UserProps> newUserProps = new ArrayList<UserProps>(0);
			List<UserEquip> newEquipList = new ArrayList<UserEquip>(0);
			Map<Long, Integer> mergeProps = new HashMap<Long, Integer>(0);
			ResultObject<Integer[]> rewards = calcRewards(player, configs, newUserProps, mergeProps, newEquipList);
			if(rewards.getResult() != SUCCESS){
				return rewards.getResult();
			}
			int addSilver = rewards.getValue()[0];
			int addGolden = rewards.getValue()[1];
			int addCoupon = rewards.getValue()[2];
			
			rewardPackSize = newUserProps.size() + newEquipList.size();
			if(!player.canAddNew2Backpack(packSize + rewardPackSize, BackpackType.DEFAULT_BACKPACK)) {
				return BACKPACK_FULLED;
			}
			
			Integer mergeCount = mergeProps.get(userProps.getId());
			mergeProps.put(userProps.getId(), mergeCount != null ?  mergeCount - count : -count );
			List<UserProps> updateUserPropsList = this.rewardsPorps(player, newUserProps, mergeProps); 		//道具奖励
			this.rewardsEquips(player, newEquipList); 					//装备奖励
			this.rewardsSliverAndGolden(player, addSilver, addGolden, addCoupon); 	//不需要暂用背包,就是奖励货币
			
			List<LoggerGoods> loggerGoods = new ArrayList<LoggerGoods>();
			if(newUserProps.size() > 0){
				loggerGoods.addAll( LoggerGoods.incomeProps(newUserProps) );
			}
			if(newEquipList.size() > 0){
				loggerGoods.addAll( LoggerGoods.loggerEquip(Orient.INCOME, newEquipList) );
			}
			
			if(updateUserPropsList != null && updateUserPropsList.size() > 0){
				Integer var = mergeProps.get(userProps.getId());
				if(var > 0){
					mergeProps.put(userProps.getId(), var + count );
				} else {
					mergeProps.remove(userProps.getId());
				}
				loggerGoods.addAll( LoggerGoods.loggerProps(Orient.INCOME, mergeProps, updateUserPropsList) );
				loggerGoods.add( LoggerGoods.outcomeProps(userProps.getId(), userProps.getBaseId(), count));
			}
			
			
			LoggerGoods[] loggerGoodsArray = loggerGoods.toArray(new LoggerGoods[loggerGoods.size()]);
			GoodsLogger.goodsLogger(player, Source.FASTEN_GIFT, loggerGoodsArray);
			if(addSilver != 0){
				SilverLogger.inCome(Source.FASTEN_GIFT, addSilver, player, loggerGoodsArray);
			}
			if(addGolden != 0){
				GoldLogger.inCome(Source.FASTEN_GIFT, addGolden, player, loggerGoodsArray);
			}
			if(addCoupon != 0){
				CouponLogger.inCome(Source.FASTEN_GIFT, addCoupon, player, loggerGoodsArray);
			}
		} catch (Exception e) {
			userProps.increaseItemCount(count);
			dbService.submitUpdate2Queue(userProps);
			return FAILURE;
		} finally{
			lock.unlock();
		}
		return SUCCESS;
	}
	
	/**
	 * 奖励装备
	 * @param player     玩家的对象
	 * @param configs    配置集合
	 */
	private void rewardsEquips(Player player, Collection<UserEquip> equipList){
		if(!equipList.isEmpty()){
			Long playerId = player.getId();
			propsManager.createUserEquip(equipList);
//			propsManager.removeUserEquipIdList(playerId, BackpackType.DEFAULT_BACKPACK);
			propsManager.put2UserEquipIdsList(playerId, BackpackType.DEFAULT_BACKPACK, equipList);
			MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, equipList);
			
			Collection<GoodsVO> goodsVOs = GoodsVO.valuleOf(null, null, null, equipList);
			MessagePushHelper.pushGoodsCountChange2Client(playerId, goodsVOs);
		}
	}
	
	/**
	 * 计算箱子奖励
	 * @param player
	 * @param configs
	 * @param newUserProps
	 * @param mergeProps
	 * @param newEquipList
	 * @return
	 */
	private ResultObject<Integer[]> calcRewards(Player player,List<PropsGiftConfig> configs, List<UserProps> newUserProps, Map<Long, Integer> mergeProps, List<UserEquip> newEquipList){
		if(player == null || configs == null){
			return ResultObject.ERROR(FAILURE);
		}
		
		Long playerId = player.getId();
		Map<Integer, Integer> newPropsMap = new HashMap<Integer, Integer>();
		int addSilver = 0, addGolden = 0, addCoupon = 0;
		for(PropsGiftConfig config : configs) {
			List<String[]> rewardGoods = config.getRewardGoods();
			if(rewardGoods != null){
				for(String[] goods : rewardGoods) {
					int baseId = Integer.parseInt(goods[0]);
					int count  = Integer.parseInt(goods[1]);
					if(config.getGiftType() == GoodsType.PROPS){
						int num = newPropsMap.containsKey(baseId) ? newPropsMap.get(baseId) : 0;
						newPropsMap.put(baseId, num + count * config.getNumber());		//可能有多组
						
					} else if(config.getGiftType() == GoodsType.EQUIP){
						for(int i = 0; i < count * config.getNumber(); i++ ){
							UserEquip userEquip = EquipHelper.newUserEquip(playerId, BackpackType.DEFAULT_BACKPACK, baseId, true);
							if(userEquip != null){
								newEquipList.add(userEquip);
							} else {
								LOGGER.error("装备奖励不存在，id:{}", baseId);
							}
						}
					} 
				}
				
			} else if(config.getGiftType() == GoodsType.SILVER){
				addSilver += config.getNumber();
			} else if(config.getGiftType() == GoodsType.GOLDEN){
				addGolden += config.getNumber();
			} else if(config.getGiftType() == GoodsType.COUPON){
				addCoupon += config.getNumber();
			}
			
		}
		
		for(Integer propsId : newPropsMap.keySet()){
			Integer count = newPropsMap.get(propsId);
			PropsConfig propsConfig = propsManager.getPropsConfig(propsId);
			if (propsConfig == null) {
				LOGGER.error("基础道具不存在,id:{}", propsId);
				continue;
			}
			PropsStackResult propsStack = PropsHelper.calcPropsStack(playerId, BackpackType.DEFAULT_BACKPACK, propsId, count, true);
			newUserProps.addAll(propsStack.getNewUserProps());
			mergeProps.putAll(propsStack.getMergeProps());
		}
		
		return ResultObject.SUCCESS(new Integer[]{addSilver, addGolden, addCoupon});
	}
	
	/**
	 * 奖励道具
	 * @param player    玩家的ID
	 * @param configs   配置集合
	 */
	private List<UserProps> rewardsPorps(Player player, List<UserProps> newUserProps, Map<Long, Integer> mergeProps){
		if(newUserProps.isEmpty() && mergeProps.isEmpty() ){
			return null;
		}
		
		Long playerId = player.getId();
		List<BackpackEntry> backpacks = new ArrayList<BackpackEntry>();
		List<UserProps> updateUserPropsList = propsManager.updateUserPropsList(mergeProps);
		propsManager.createUserProps(newUserProps);
		propsManager.put2UserPropsIdsList(playerId, BackpackType.DEFAULT_BACKPACK, newUserProps);
		
		backpacks.addAll(newUserProps);
		backpacks.addAll(updateUserPropsList);
		MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, backpacks);
		
		Collection<GoodsVO> goodsVOs = GoodsVO.valuleOf(newUserProps, updateUserPropsList, mergeProps, null);
		MessagePushHelper.pushGoodsCountChange2Client(playerId, goodsVOs);
		
		return updateUserPropsList;
	}
	
	/**
	 * 奖励铜币
	 * @param configs  配置集合
	 */
	private void rewardsSliverAndGolden(Player player, int silver, int golden, int coupon){
		player.increaseSilver( silver );
		player.increaseGolden( golden );
		player.increaseCoupon( coupon );
		dbService.submitUpdate2Queue(player);
		
		//推送属性
		List<Long> playerIdList = Arrays.asList(player.getId());
		List<UnitId> unitIdList = Arrays.asList(UnitId.valueOf(player.getId(), ElementType.PLAYER));
		UserPushHelper.pushAttribute2AreaMember(player.getId(),playerIdList, unitIdList, AttributeKeys.SILVER, AttributeKeys.GOLDEN, AttributeKeys.COUPON);
	}
	
	
	/**
	 * 随机礼包
	 * @param player         玩家的对象
	 * @param userProps      道具对象
	 * @param count
	 * @param propConfig
	 * @return
	 */
	private int randGifis(UserDomain userDomain,UserProps userProps,int count,PropsConfig propConfig, UserCoolTime userCoolTime, CoolTimeConfig coolTime){
		int rewardNo = (int)Math.round(propConfig.getAttrValue());
		long playerId = userDomain.getPlayerId();
		Player player = userDomain.getPlayer();
		
		List<DropRewards> dropRewards = dropManager.dropRewards(playerId, rewardNo, count);
		if(dropRewards != null){
			int addSilver = 0;	//增加的铜币
			int addGolden = 0;	//增加的金币
			
			int rewardPackSize = 0; 
			int backpack = BackpackType.DEFAULT_BACKPACK;
			int usedSize = propsManager.getBackpackSize(playerId, backpack);
			ChainLock lock = LockUtils.getLock(player.getPackLock(), userCoolTime);
			lock.lock();
			try {
				if(userProps.getCount() < count) {
					return ITEM_NOT_ENOUGH;
				} else if(coolTime != null && userCoolTime.isCoolTiming(coolTime.getId())) {
					return COOL_TIMING;
				}
			    
			    if(coolTime != null){
			    	userCoolTime.addCoolTime(coolTime.getId(), coolTime.getCoolTime());
			    }
			    
			    Map<Integer, int[]> dropProps = new HashMap<Integer, int[]>();
				List<UserProps> newUserProps = new ArrayList<UserProps>(1);
				Map<Long, Integer> mergeProps = new HashMap<Long, Integer>(1);
				List<UserEquip> userEquipsList = new ArrayList<UserEquip>(1);
				
				//TODO 这个问题明天要重点看下. 2012年7月13日23:14:03, 尹优源
				for(DropRewards drs : dropRewards) {
					int baseId = drs.getBaseId();
					int amount = drs.getAmount();
					boolean binding = drs.isBinding();
					if(drs.getType() == GoodsType.PROPS) {
						int[] array = dropProps.get(baseId);
						array = array == null ? new int[2] : array;
						if(!binding) { //未绑定
							array[0] = array[0] + amount;
						} else {
							array[1] = array[1] + amount;
						}
						
						dropProps.put(baseId, array);
					} else if(drs.getType() == GoodsType.EQUIP){
						userEquipsList.addAll(EquipHelper.newUserEquips(playerId, backpack, baseId, binding, amount));
					} else if(drs.getType() == GoodsType.SILVER){
						addSilver += amount;
					} else if(drs.getType() == GoodsType.GOLDEN){
						addGolden += amount;
					}
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
					return BACKPACK_FULLED;
				}
				mergeProps.put(userProps.getId(), -count);
				List<UserProps> updateUserPropsList = rewardsPorps(player, newUserProps, mergeProps);
				rewardsEquips(player, userEquipsList);
				rewardsSliverAndGolden(player, addSilver, addGolden, 0);
				
				List<LoggerGoods> loggerGoods = new ArrayList<LoggerGoods>();
				if(addSilver != 0){
					SilverLogger.inCome(Source.RAND_GIFT, addSilver, player);
				}
				if(addGolden != 0){
					GoldLogger.inCome(Source.RAND_GIFT, addGolden, player);
				}
				if(newUserProps.size() > 0){
					loggerGoods.addAll( LoggerGoods.incomeProps(newUserProps) );
				}
				if(userEquipsList.size() > 0){
					loggerGoods.addAll( LoggerGoods.loggerEquip(Orient.INCOME, userEquipsList) );
				}
				if(updateUserPropsList != null && updateUserPropsList.size() > 0){
					Integer var = mergeProps.get(userProps.getId());
					if(var > 0){
						mergeProps.put(userProps.getId(), var + count );
					} else {
						mergeProps.remove(userProps.getId());
					}
					loggerGoods.addAll(LoggerGoods.loggerProps(Orient.INCOME, mergeProps, updateUserPropsList));
					loggerGoods.add(LoggerGoods.outcomeProps(userProps.getId(), userProps.getBaseId(), count));
				}
				GoodsLogger.goodsLogger(player, Source.RAND_GIFT, loggerGoods.toArray(new LoggerGoods[loggerGoods.size()]));
				
			} catch (Exception e) {
				userProps.increaseItemCount(count);
				dbService.submitUpdate2Queue(userProps);
				return FAILURE;
			} finally {
				lock.unlock();
			}
			return SUCCESS;
			
		} else {
			LOGGER.error("空的随机掉落奖励,playerId:{}, userPropsId:{}, propConfigId:{}, count:{}", new Object[]{playerId, userProps.getBaseId(), userProps.getId(), count});
		}
		return FAILURE;
	}

}
