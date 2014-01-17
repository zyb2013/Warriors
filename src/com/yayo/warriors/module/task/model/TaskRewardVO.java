package com.yayo.warriors.module.task.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;

public class TaskRewardVO {

	private int addExp = 0;
	
	private int addGas = 0;
	
	private int addSilver = 0;
	
	private int addCoupon = 0;

	private LoggerGoods[] goodsRewardInfo;
	
	private Map<Long, Integer> mergePropMap = new HashMap<Long, Integer>(0);
	
	private Collection<UserProps> newPropsList = new ArrayList<UserProps>(0);
	
	private Collection<UserEquip> newUserEquipList = new ArrayList<UserEquip>(0);

	public int getAddExp() {
		return addExp;
	}

	public void setAddExp(int addExp) {
		this.addExp = addExp;
	}

	public int getAddSilver() {
		return addSilver;
	}

	public void increaseSilver(int addSilver) {
		this.addSilver += addSilver;
	}

	public void increaseExp(int addExp) {
		this.addExp += addExp;
	}

	public Map<Long, Integer> getMergePropMap() {
		return mergePropMap;
	}
	
	public LoggerGoods[] getGoodsRewardInfo() {
		return goodsRewardInfo;
	}

	public void addMergeProps(Map<Long, Integer> addMergePropsMap) {
		if(addMergePropsMap == null || addMergePropsMap.isEmpty()) {
			return;
		}
		
		for (Entry<Long, Integer> entry : addMergePropsMap.entrySet()) {
			Long userItemId = entry.getKey();
			Integer amount = entry.getValue();
			if(userItemId == null || amount == null || amount <= 0) {
				continue;
			}
			Integer cache = mergePropMap.get(userItemId);
			cache = cache == null ? 0 : cache;
			mergePropMap.put(userItemId, cache + amount);
		}
	}

	public Collection<UserProps> getNewPropsList() {
		return newPropsList;
	}
	
	public void addNewPropsList(Collection<UserProps> userPropsList) {
		if(userPropsList != null && !userPropsList.isEmpty()) {
			this.newPropsList.addAll(userPropsList);
		}
	}

	public Collection<UserEquip> getNewUserEquipList() {
		return newUserEquipList;
	}
	
	public void addNewEquip(UserEquip userEquip) {
		if(userEquip != null) {
			this.newUserEquipList.add(userEquip);
		}
	}

	public int getAddGas() {
		return addGas;
	}

	public void setAddGas(int addGas) {
		this.addGas = addGas;
	}
	
	public void increaseGas(int addGas) {
		this.addGas += addGas;
	}

	public int getAddCoupon() {
		return addCoupon;
	}

	public void setAddCoupon(int addCoupon) {
		this.addCoupon = addCoupon;
	}
	
	public void increaseCoupon(int addCoupon) {
		this.addCoupon += addCoupon;
	}
}
