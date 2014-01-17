package com.yayo.warriors.basedb.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceAdapter;
import com.yayo.warriors.basedb.model.ChargeConditionConfig;
import com.yayo.warriors.basedb.model.ChargeConfig;
import com.yayo.warriors.basedb.model.ChargeRewardConfig;
import com.yayo.warriors.type.IndexName;

/**
 * 充值礼包服务接口
 * 
 * @author Hyint
 */
@Component
public class ChargeGiftService extends ResourceAdapter {

	/** {礼包的充值类型, {礼包的类别1, 礼包的类别2 }}*/
	private Map<Integer, Collection<Integer>> chargeTypeMap = new HashMap<Integer, Collection<Integer>>();
	
	@Override
	public void initialize() {
		chargeTypeMap.clear();
		Class<ChargeConfig> rechargeClazz = ChargeConfig.class;
		Class<ChargeRewardConfig> rewardClazz = ChargeRewardConfig.class;
		Class<ChargeConditionConfig> conditionClazz = ChargeConditionConfig.class;
		for (ChargeConditionConfig rechargeCondition : resourceService.listAll(conditionClazz)) {
			Set<ChargeRewardConfig> rechargeRewards = new HashSet<ChargeRewardConfig>();
			for (Integer rewardId : rechargeCondition.getRewardIds()) {
				rechargeRewards.addAll(resourceService.listByIndex(IndexName.REWARDS_ID, rewardClazz, rewardId));
			}
			for (ChargeRewardConfig chargeReward : rechargeRewards) {
				chargeReward.getConditions().add(rechargeCondition);
			}
			rechargeCondition.setRechargeRewards(new ArrayList<ChargeRewardConfig>(rechargeRewards));
		}
		
		String index = IndexName.RECHARGE_SERIAL;
		Map<Integer, Integer> cycleMap = new HashMap<Integer, Integer>();
		for (ChargeConfig rechargeConfig : resourceService.listAll(rechargeClazz)) {
			List<ChargeConditionConfig> conditions = resourceService.listByIndex(index, conditionClazz, rechargeConfig.getSerial());
			Collections.sort(conditions);
			rechargeConfig.setConditions(conditions);
			
			int type = rechargeConfig.getType();
			Collection<Integer> collection = chargeTypeMap.get(type);
			if(collection == null) {
				collection = new HashSet<Integer>();
				chargeTypeMap.put(type, collection);
			}

			collection.add(rechargeConfig.getGiftType());
			int cycle = rechargeConfig.getCycle();
			Integer cache = cycleMap.get(rechargeConfig.getGiftType());
			cycleMap.put(rechargeConfig.getGiftType(), (cache == null ? 0 : cache) + cycle);
		}
		
		for (Entry<Integer, Integer> entry : cycleMap.entrySet()) {
			Integer giftType = entry.getKey();
			Integer maxCycle = entry.getValue();
			if(giftType != null && maxCycle != null) {
				for (ChargeConfig chargeConfig : listChargeConfig(giftType, true)) {
					chargeConfig.setTotalCycle(Math.max(chargeConfig.getCycle(), maxCycle));
				}
			}
		}
		
	}
	
	/**
	 * 查询对象
	 * 
	 * @param id
	 * @param clazz
	 * @return
	 */
	public <T> T get(Object id, Class<T> clazz) {
		return resourceService.get(id, clazz);
	}

	/**
	 * 列出对应的所有类
	 * 
	 * @param clazz
	 * @return
	 */
	public <T> Collection<T> list(Class<T> clazz) {
		return resourceService.listAll(clazz);
	}
	
	/**
	 * 根据礼包的充值类型取得类型列表
	 * 
	 * @param  type					礼包的充值类型
	 * @return {@link Collection}	礼包的类型列表
	 */
	public Collection<Integer> listGiftTypeByType(int type) {
		Collection<Integer> collection = chargeTypeMap.get(type); 
		if(collection == null) {
			return Collections.emptySet();
		}
		return collection;
	}
	
	/**
	 * 列出充值礼包配置
	 * 
	 * @param  giftType				礼包类型
	 * @return {@link Collection}	礼包配置对象
	 */
	public List<ChargeConfig> listChargeConfig(int giftType, boolean sort) {
		List<ChargeConfig> chargeConfigs = resourceService.listByIndex(IndexName.GIVE_CHARGE_TYPE, ChargeConfig.class, giftType);
		if(sort) {
			Collections.sort(chargeConfigs);	//按照出现顺序排序
		}
		return chargeConfigs;
	}
}
