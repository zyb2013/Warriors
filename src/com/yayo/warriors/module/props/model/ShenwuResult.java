package com.yayo.warriors.module.props.model;

import java.util.Collection;

import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserEquip;

/**
 * 神武返回值
 * 
 * @author Hyint
 */
public class ShenwuResult {

	private int result;

	private UserEquip userEquip;

	private Collection<BackpackEntry> backpackEntries;

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public UserEquip getUserEquip() {
		return userEquip;
	}

	public void setUserEquip(UserEquip userEquip) {
		this.userEquip = userEquip;
	}

	public Collection<BackpackEntry> getBackpackEntries() {
		return backpackEntries;
	}

	public void setBackpackEntries(Collection<BackpackEntry> backpackEntries) {
		this.backpackEntries = backpackEntries;
	}

	private ShenwuResult() {
	}

	public static ShenwuResult ERROR(int result) {
		ShenwuResult shenwuResult = new ShenwuResult();
		shenwuResult.result = result;
		return shenwuResult;
	}

	public static ShenwuResult SUCCESS(UserEquip userEquip, Collection<BackpackEntry> backpackEntries) {
		ShenwuResult shenwuResult = new ShenwuResult();
		shenwuResult.userEquip = userEquip;
		shenwuResult.result = CommonConstant.SUCCESS;
		shenwuResult.backpackEntries = backpackEntries;
		return shenwuResult;
	}
}
