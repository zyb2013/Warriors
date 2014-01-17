package com.yayo.warriors.basedb.model;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;

/**
 * 套表基础表
 * 
 * @author Hyint
 */
@Resource
public class SuitConfig {
	public static final String SUIT_CONFIG_ID = "SUIT_CONFIG_ID";
	
	@Id
	private int id;
	
	/** 套装ID */
	@Index(name = SUIT_CONFIG_ID, order = 0)
	private int suitId;
	
	/** 激活条件 */
	private int condition;
	
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

	@JsonIgnore
	private Map<Integer, Integer> attributes = null;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSuitId() {
		return suitId;
	}

	public void setSuitId(int suitId) {
		this.suitId = suitId;
	}

	public int getCondition() {
		return condition;
	}

	public void setCondition(int condition) {
		this.condition = condition;
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
		return attributes;
	}

	public void setAttributes(Map<Integer, Integer> attributes) {
		this.attributes = attributes;
	}

	@Override
	public String toString() {
		return "SuitConfig [id=" + id + ", suitId=" + suitId + ", condition=" + condition + ", attribute1=" 
				+ attribute1 + ", attrValue1=" + attrValue1 + ", attribute2=" + attribute2 + ", attrValue2=" 
				+ attrValue2 + ", attribute3=" + attribute3 + ", attrValue3=" + attrValue3 + "]";
	}
	
	
}
