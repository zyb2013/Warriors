package com.yayo.warriors.module.props.model;

import java.io.Serializable;

/**
 * 装备耐久信息
 * 
 * @author Hyint
 */
public class EndureInfo implements Serializable {
	private static final long serialVersionUID = 1950494779975372313L;
	
	/** 用户装备ID */
	private long userEquipId;
	
	/** 背包号 */
	private int backpack;
	
	/** 装备的当前装备耐久 */
	private int currentEndure;

	public long getUserEquipId() {
		return userEquipId;
	}

	public void setUserEquipId(long userEquipId) {
		this.userEquipId = userEquipId;
	}

	public int getBackpack() {
		return backpack;
	}

	public void setBackpack(int backpack) {
		this.backpack = backpack;
	}

	public int getCurrentEndure() {
		return currentEndure;
	}

	public void setCurrentEndure(int currentEndure) {
		this.currentEndure = currentEndure;
	}
	
	/**
	 * 构建装备耐久详细信息
	 * 
	 * @param  userEquipId			装备ID
	 * @param  backpack				背包号
	 * @param  currentEndure		当前装备的耐久
	 * @return {@link EndureInfo}	装备耐久详细信息
	 */
	public static EndureInfo valueOf(long userEquipId, int backpack, int currentEndure) {
		EndureInfo endureInfo = new EndureInfo();
		endureInfo.backpack = backpack;
		endureInfo.userEquipId = userEquipId;
		endureInfo.currentEndure = currentEndure;
		return endureInfo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (userEquipId ^ (userEquipId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		
		EndureInfo other = (EndureInfo) obj;
		return userEquipId == other.userEquipId;
	}
	
}
