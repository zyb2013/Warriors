package com.yayo.warriors.module.props.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;

/**
 * 装备分解对象
 * 
 * @author Hyint
 */
public class ResolveVO {

	/** 合成需要扣除的游戏币 */
	private int costSilver;
	
	/** 新创建的用户道具信息 */
	private List<UserProps> addNewUserProps = new ArrayList<UserProps>(0);

	/** 堆叠的用户道具集合  */
	private Map<Long, Integer> addMoreUserProps = new HashMap<Long, Integer>(0);

	/** {装备ID, 装备对象} */
	private Map<Long, UserEquip> cacheEquips = new HashMap<Long, UserEquip>(0);
	
	/** 分解得到的用户道具信息 */
	private Map<Integer, Integer> resolveItems = new HashMap<Integer, Integer>(0);
	
	public void putCacheEquip(UserEquip userEquip) {
		if (userEquip != null) {
			this.cacheEquips.put(userEquip.getId(), userEquip);
		}
	}
	
	public Map<Long, UserEquip> getCacheEquips() {
		return this.cacheEquips;
	}
	
	public UserEquip getCacheUserEquip(long userEquipId) {
		return this.cacheEquips.get(userEquipId);
	}
	
	public int getResolveItemCount(int propsId) {
		Integer count = resolveItems.get(propsId);
		return count == null ? 0 : count;
	}

	public Map<Integer, Integer> getResolveItems() {
		return resolveItems;
	}

	public void addResolveItems(int propsId, int addCount) {
		Integer count = resolveItems.get(propsId);
		count = count == null ? 0 : count;
		resolveItems.put(propsId, addCount + count);
	}

	public List<UserProps> getAddNewUserProps() {
		return addNewUserProps;
	}

	public void setAddNewUserProps(List<UserProps> addNewUserProps) {
		this.addNewUserProps = addNewUserProps;
	}

	public Map<Long, Integer> getAddMoreUserProps() {
		return addMoreUserProps;
	}

	public void setAddMoreUserProps(Map<Long, Integer> addMoreUserProps) {
		this.addMoreUserProps = addMoreUserProps;
	}

	public int getCostSilver() {
		return costSilver;
	}

	public void addCostSilver(int costSilver) {
		this.costSilver += costSilver;
	}
	
	
}
