package com.yayo.warriors.basedb.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;

/**
 * 好友加成配置
 * 
 * @author huachaoping
 */
@Resource
public class FriendAddedConfig {
	
	/** 主键 */
	@Id
	private int id;
	
	/** 好友度区间 */
	private int minFriendlyValue;
	
	/** 好友度区间 */
	private int maxFriendlyValue;
	
	/** 加成属性: 属性KEY_值|... */
	private String attributes;
	
	@JsonIgnore
	private Map<Integer, Integer> attributeMap = null;
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getMinFriendlyValue() {
		return minFriendlyValue;
	}

	public void setMinFriendlyValue(int minFriendlyValue) {
		this.minFriendlyValue = minFriendlyValue;
	}

	public int getMaxFriendlyValue() {
		return maxFriendlyValue;
	}

	public void setMaxFriendlyValue(int maxFriendlyValue) {
		this.maxFriendlyValue = maxFriendlyValue;
	}

	public String getAttributes() {
		return attributes;
	}

	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}
	
	/**
	 * 是否有好友加成
	 * @param friendlyValue
	 * @return {@link Boolean}
	 */
	public boolean isFriendlyAdded(int friendlyValue) {
		return friendlyValue >= minFriendlyValue && friendlyValue <= maxFriendlyValue;
	}
	
	
	
	public Map<Integer, Integer> getAttributeMap() {
		if(this.attributeMap != null) {
			return this.attributeMap;
		}
		
		synchronized (this) {
			if(this.attributeMap != null) {
				return this.attributeMap;
			}
			
			this.attributeMap = new HashMap<Integer, Integer>(1);
			List<String[]> arrays = Tools.delimiterString2Array(this.attributes);
			if(arrays == null || arrays.isEmpty()) {
				return this.attributeMap;
			}
			
			for (String[] array : arrays) {
				Integer attribute = Integer.valueOf(array[0]);
				Integer attrValue = Integer.valueOf(array[1]);
				this.attributeMap.put(attribute, attrValue);
			}
			
		}
		return this.attributeMap;
	}
	
}
