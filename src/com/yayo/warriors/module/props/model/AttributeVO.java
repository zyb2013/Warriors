package com.yayo.warriors.module.props.model;

import java.io.Serializable;

import com.yayo.common.utility.Splitable;

/**
 * 属性封装对象
 * 
 * @author Hyint
 */
public class AttributeVO implements Serializable, Comparable<AttributeVO> {
	private static final long serialVersionUID = -466339487111685302L;

	/** 属性下标. 属性下标从1开始 */
	private int id;

	/** 附加属性值 */
	private int attribute;

	/** 附加属性值 */
	private int attrValue;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getAttribute() {
		return attribute;
	}

	public void setAttribute(int attribute) {
		this.attribute = attribute;
	}

	public int getAttrValue() {
		return attrValue;
	}

	public void setAttrValue(int attrValue) {
		this.attrValue = attrValue;
	}

	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		AttributeVO other = (AttributeVO) obj;
		return id == other.id;
	}
	
	
	public static AttributeVO valueOf(int id, int attribute, int attrValue) {
		AttributeVO attributeVO = new AttributeVO();
		attributeVO.id = id;
		attributeVO.attribute = attribute;
		attributeVO.attrValue = attrValue;
		return attributeVO;
	}

	
	public String toString() {
		return id + Splitable.ATTRIBUTE_SPLIT + attribute + Splitable.ATTRIBUTE_SPLIT + attrValue;
	}

	
	public int compareTo(AttributeVO o) {
		return o == null || this.id < o.getId() ? -1 : (this.id == o.getId() ? 0 : 1);
	}
	
	
}
