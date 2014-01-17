package com.yayo.warriors.module.props.model;

import java.io.Serializable;

import com.yayo.common.utility.Splitable;

/**
 * 装备的孔属性
 * 
 * @author Hyint
 */
public class HoleInfo implements Serializable {
	private static final long serialVersionUID = 963942164978171969L;
	
	/** 孔下标 */
	private int index;

	/** 道具ID */
	private int itemId;

	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	
	@Override
	public String toString() {
		return index  + Splitable.ATTRIBUTE_SPLIT + itemId;
	}
	
	public static HoleInfo valueOf(int index, int itemId) {
		HoleInfo holeInfo = new HoleInfo();
		holeInfo.index = index;
		holeInfo.itemId = itemId;
		return holeInfo;
	}
	
}
