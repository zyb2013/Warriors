package com.yayo.warriors.basedb.model;

import static com.yayo.warriors.type.IndexName.*;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Splitable;

/**
 * 肉身附加基础类
 * 
 * @author huachaoping
 */
@Resource
public class MortalAddedConfig {

	/** 基础Id */
	@Id
	private int id;
	
	/** 所属职业 */
	@Index(name=MORTAL_JOB_LEVEL, order=0)
	private int roleJob;
	
	/** 肉身类型数量 */
	private int mortalTypeCount;
	
	/** 属性加成所需肉身等级 */
	@Index(name=MORTAL_JOB_LEVEL, order=1)
	private int mortalLevel;
	
	/** 属性加成类型 */
	private String addedAttributes;
	
	/** 属性加成值 */
	private String addedAttrValues;
	
	
	/** 加成属性KEY */
	private int[] attributes = null;
	
	/** 加成属性值 */
	private int[] values = null;
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRoleJob() {
		return roleJob;
	}

	public void setRoleJob(int roleJob) {
		this.roleJob = roleJob;
	}

	public int getMortalTypeCount() {
		return mortalTypeCount;
	}

	public void setMortalTypeCount(int mortalTypeCount) {
		this.mortalTypeCount = mortalTypeCount;
	}

	public int getMortalLevel() {
		return mortalLevel;
	}

	public void setMortalLevel(int mortalLevel) {
		this.mortalLevel = mortalLevel;
	}

	public String getAddedAttributes() {
		return addedAttributes;
	}

	public void setAddedAttributes(String addedAttributes) {
		this.addedAttributes = addedAttributes;
	}

	public String getAddedAttrValues() {
		return addedAttrValues;
	}

	public void setAddedAttrValues(String addedAttrValues) {
		this.addedAttrValues = addedAttrValues;
	}

	
	public int[] getAttributes() {
		if (this.attributes != null) {
			return attributes;
		}
		
		String[] arrays = this.addedAttributes.split(Splitable.ELEMENT_SPLIT);
		this.attributes = new int[arrays.length];
		for (int i = 0; i <= arrays.length - 1; i++) {
			this.attributes[i] = Integer.valueOf(arrays[i]); 
		}
		return this.attributes;
	}
	
	
	public int[] getValues() {
		if (this.values != null) {
			return this.values;
		}
		
		String[] arrays = this.addedAttrValues.split(Splitable.ELEMENT_SPLIT);
		this.values = new int[arrays.length];
		for (int i = 0; i <= arrays.length - 1; i++) {
			this.values[i] = Integer.valueOf(arrays[i]);
		}
		return this.values;
	}
}
