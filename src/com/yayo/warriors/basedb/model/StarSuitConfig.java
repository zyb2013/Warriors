package com.yayo.warriors.basedb.model;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;

/**
 * 装备星级套装配置
 * 
 * @author Hyint
 */
@Resource
public class StarSuitConfig {
	
	/** 自增ID*/
	@Id
	private int id;
	
	/** 附加属性1 */
	private int attribute1;

	/** 附加属性值1 */
	private int attrValue1;
	
	/** 附加属性2 */
	private int attribute2;
	
	/** 附加属性值2 */
	private int attrValue2;

	/** 附加属性3 */
	private int attribute3;
	
	/** 附加属性值3 */
	private int attrValue3;

	/** 附加属性4 */
	private int attribute4;
	
	/** 附加属性值4 */
	private int attrValue4;
	
	/** 附加属性5 */
	private int attribute5;
	
	/** 附加属性值5 */
	private int attrValue5;

	/** 附加属性值集合*/
	@JsonIgnore
	private Map<Integer, Integer> attributes = null;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getAttribute1() {
		return attribute1;
	}

	public void setAttribute1(int attribute1) {
		this.attribute1 = attribute1;
	}

	public int getAttrValue1() {
		return attrValue1;
	}

	public void setAttrValue1(int attrValue1) {
		this.attrValue1 = attrValue1;
	}

	public int getAttribute2() {
		return attribute2;
	}

	public void setAttribute2(int attribute2) {
		this.attribute2 = attribute2;
	}

	public int getAttrValue2() {
		return attrValue2;
	}

	public void setAttrValue2(int attrValue2) {
		this.attrValue2 = attrValue2;
	}

	public int getAttribute3() {
		return attribute3;
	}

	public void setAttribute3(int attribute3) {
		this.attribute3 = attribute3;
	}

	public int getAttrValue3() {
		return attrValue3;
	}

	public void setAttrValue3(int attrValue3) {
		this.attrValue3 = attrValue3;
	}

	public Map<Integer, Integer> getAttributes() {
		if(this.attributes != null) {
			return this.attributes;
		}
		
		synchronized (this) {
			if(this.attributes != null) {
				return this.attributes;
			}
			this.attributes = new HashMap<Integer, Integer>(5);
			this.addAttribute2Map(this.attribute1, this.attrValue1, this.attributes);
			this.addAttribute2Map(this.attribute2, this.attrValue2, this.attributes);
			this.addAttribute2Map(this.attribute3, this.attrValue3, this.attributes);
			this.addAttribute2Map(this.attribute4, this.attrValue4, this.attributes);
			this.addAttribute2Map(this.attribute5, this.attrValue5, this.attributes);
		}
		return attributes;
	}
	
	/**
	 * 叠加属性集合中的值
	 * 
	 * @param attribute
	 * @param attrValue
	 * @param attributes
	 */
	private void addAttribute2Map(int attribute, int attrValue, Map<Integer, Integer> attributes) {
		if(attribute != 0 && attrValue != 0) {
			Integer cacheValue = attributes.get(attribute);
			cacheValue = cacheValue == null ? 0 : cacheValue;
			attributes.put(attribute, cacheValue + attrValue);
		}
	}

	public void setAttributes(Map<Integer, Integer> attributes) {
		this.attributes = attributes;
	}

	public int getAttribute4() {
		return attribute4;
	}

	public void setAttribute4(int attribute4) {
		this.attribute4 = attribute4;
	}

	public int getAttrValue4() {
		return attrValue4;
	}

	public void setAttrValue4(int attrValue4) {
		this.attrValue4 = attrValue4;
	}

	public int getAttribute5() {
		return attribute5;
	}

	public void setAttribute5(int attribute5) {
		this.attribute5 = attribute5;
	}

	public int getAttrValue5() {
		return attrValue5;
	}

	public void setAttrValue5(int attrValue5) {
		this.attrValue5 = attrValue5;
	}

	@Override
	public String toString() {
		return "StarSuitConfig [id=" + id + ", attribute1=" + attribute1 + ", attrValue1="
				+ attrValue1 + ", attribute2=" + attribute2 + ", attrValue2=" + attrValue2
				+ ", attribute3=" + attribute3 + ", attrValue3=" + attrValue3 + ", attribute4="
				+ attribute4 + ", attrValue4=" + attrValue4 + ", attribute5=" + attribute5
				+ ", attrValue5=" + attrValue5 + ", attributes=" + attributes + "]";
	}
}
