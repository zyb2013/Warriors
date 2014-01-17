package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.type.IndexName;

/**
 * 基础道具对象
 * 
 * @author Hyint
 */
@Resource
public class PropsConfig extends GoodsParent {

	/** 基础道具ID */
	@Id
	private int id;

	/** 道具的子类型 */
	@Index(name = IndexName.PROPS_CHILDTYPE, order=0)
	private int childType;
	
	/** 最大堆叠数量 */
	private int maxAmount;
	
	/** 属性对应附加值. 技能等级也可以附加等级为2. */
	private double attrValue;
	
	/** 道具的CDID */
	private int cdId;
	
	/** 是否可以使用 */
	private boolean canUse = true;
	
	/** 是否可以丢弃 */
	private boolean canDrop = true;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getChildType() {
		return childType;
	}

	public void setChildType(int childType) {
		this.childType = childType;
	}

	public int getMaxAmount() {
		return maxAmount;
	}

	public void setMaxAmount(int maxAmount) {
		this.maxAmount = maxAmount;
	}

	public double getAttrValue() {
		return attrValue;
	}
	
	public int getAttrValueRound() {
		return (int) Math.round(this.attrValue);
	}
	
	public int getAttrValueInt() {
		return (int) this.attrValue;
	}

	public void setAttrValue(double attrValue) {
		this.attrValue = attrValue;
	}

	public int getCdId() {
		return cdId;
	}

	public void setCdId(int cdId) {
		this.cdId = cdId;
	}
	
	public boolean isCanUse() {
		return canUse;
	}

	public void setCanUse(boolean canUse) {
		this.canUse = canUse;
	}

	public boolean isCanDrop() {
		return canDrop;
	}

	public void setCanDrop(boolean canDrop) {
		this.canDrop = canDrop;
	}

	@Override
	public String toString() {
		return "PropsConfig [id=" + id + ", childType=" + childType + ", maxAmount=" + maxAmount
				+ ", attrValue=" + attrValue + ", cdId=" + cdId + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		PropsConfig other = (PropsConfig) obj;
		return id == other.id;
	}
 
	
}
