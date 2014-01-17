package com.yayo.warriors.basedb.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.task.model.RewardVO;
import com.yayo.warriors.type.IndexName;

/**
 * 充值奖励配置
 * 
 * @author Hyint
 */
@Resource
public class ChargeRewardConfig {
	
	/** 奖励ID */
	@Id
	private int id;
	
	@Index(name=IndexName.REWARDS_ID)
	private int rewardId;
	
	/** 奖励的经验 */
	private int exp;
	
	/** 可以领取的奖励次数 */
	private int times;
	
	/** 可以领取的奖励礼金 */
	private int coupon;

	/** 可以领取的奖励游戏币 */
	private int silver;
	
	/** 奖励的道具信息, 格式: 奖励的道具ID_奖励数量_绑定状态(0-不绑定, 1-绑定)| */
	private String items;
	
	/** 奖励的装备信息, 格式: 奖励的装备ID_奖励数量_绑定状态(0-不绑定, 1-绑定)_奖励的星级| */
	private String equips;

	/** 奖励列表 */
	@JsonIgnore
	private transient List<RewardVO> rewardList;
	
	/** 该奖励可以被哪些条件领取 */
	@JsonIgnore
	private transient Set<ChargeConditionConfig> conditionIds = new HashSet<ChargeConditionConfig>();
	
	public Set<ChargeConditionConfig> getConditions() {
		return conditionIds;
	}

	public List<RewardVO> getRewardVOList() {
		if(this.rewardList != null) {
			return this.rewardList;
		}

		synchronized (this) {
			if(this.rewardList != null) {
				return this.rewardList;
			}
			this.rewardList = new ArrayList<RewardVO>();
			constructEquipReward(this.rewardList);
			constructItemReward(this.rewardList);
		}
		return this.rewardList;
	}

	/**
	 * 构建装备奖励对象
	 * 	
	 * @param type				物品的类型
	 * @param rewardList		奖励列表
	 * @param cacheMap			缓存集合
	 */
	private void constructEquipReward(List<RewardVO> rewardList) {
		List<String[]> arrays = Tools.delimiterString2Array(this.equips);
		if(arrays == null || arrays.isEmpty()) {
			return;
		}
		
		for (String[] element : arrays) {
			int baseId = Integer.valueOf(element[0]);
			int count = Integer.valueOf(element[1]);
			int starLevel = Integer.valueOf(element[2]);
			boolean binding = element.length > 3 && Integer.valueOf(element[3]) > 0;
			this.rewardList.add(RewardVO.equip(baseId, count, starLevel, binding));
		}
	}

	
	/**
	 * 构建奖励对象
	 * 	
	 * @param type				物品的类型
	 * @param rewardList		奖励列表
	 * @param cacheMap			缓存集合
	 */
	private void constructItemReward(List<RewardVO> rewardList) {
		List<String[]> arrays = Tools.delimiterString2Array(this.items);
		if(arrays == null || arrays.isEmpty()) {
			return;
		}
		
		// {道具ID, [未绑定数量, 绑定数量] }
		Map<Integer, int[]> maps = new HashMap<Integer, int[]>(1);
		for (String[] element : arrays) {
			int baseId = Integer.valueOf(element[0]);
			int count = Integer.valueOf(element[1]);
			int[] cache = maps.get(baseId);
			if(cache == null) {
				cache = new int[2];
				maps.put(baseId, cache);
			}
			
			if(element.length < 2 || Integer.valueOf(element[2]) == 0) { //非绑定的
				cache[0] = cache[0] + count;
			} else { //绑定的
				cache[1] = cache[1] + count;
			}
		}
		
		for (Entry<Integer, int[]> entry : maps.entrySet()) {
			int itemId = entry.getKey();
			int[] itemCount = entry.getValue();
			if(itemCount[0] > 0) {
				this.rewardList.add(RewardVO.props(itemId, itemCount[0], false));
			}
			if(itemCount[1] > 0) {
				this.rewardList.add(RewardVO.props(itemId, itemCount[1], true));
			}
		}
	}
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public int getTimes() {
		return times;
	}

	public void setTimes(int times) {
		this.times = times;
	}

	public int getCoupon() {
		return coupon;
	}

	public void setCoupon(int coupon) {
		this.coupon = coupon;
	}

	public int getSilver() {
		return silver;
	}

	public void setSilver(int silver) {
		this.silver = silver;
	}

	public String getItems() {
		return items;
	}

	public void setItems(String items) {
		this.items = items;
	}

	public String getEquips() {
		return equips;
	}

	public void setEquips(String equips) {
		this.equips = equips;
	}

	public int getRewardId() {
		return rewardId;
	}

	public void setRewardId(int rewardId) {
		this.rewardId = rewardId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + rewardId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChargeRewardConfig other = (ChargeRewardConfig) obj;
		if (id != other.id)
			return false;
		if (rewardId != other.rewardId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ChargeRewardConfig [id=" + id + ", rewardId=" + rewardId + ", exp=" + exp
				+ ", times=" + times + ", coupon=" + coupon + ", silver=" + silver + ", items="
				+ items + ", equips=" + equips + "]";
	}
	
}
