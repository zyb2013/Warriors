package com.yayo.warriors.basedb.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;

@Resource
public class TitleDictionary {

	@Id
	private int id ;
	
	/** 公告ID, 获得称号推送荣誉公告 */
	private int noticeId ;
	
	/** 附加信息   AttributeKeys_添加百分比   1000_500(力量添加5%),1001_1000(敏捷添加10%)*/
	private String additional ;
	
	@JsonIgnore
	private Map<Integer, Integer> additionalCacheMap ;
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getAdditional() {
		return additional;
	}

	public void setAdditional(String additional) {
		this.additional = additional;
	}
	
	public int getNoticeId() {
		return noticeId;
	}

	public void setNoticeId(int noticeId) {
		this.noticeId = noticeId;
	}

	public Integer obtainValue(int attr){
		return getAdditionMap().get(attr);
	}
	
	private Map<Integer, Integer> getAdditionMap() {
		if(this.additionalCacheMap != null) {
			return this.additionalCacheMap;
		}
		
		synchronized (this) {
			if(this.additionalCacheMap != null) {
				return this.additionalCacheMap;
			}
			
			this.additionalCacheMap = new HashMap<Integer, Integer>(1);
			List<String[]> arrays = Tools.delimiterString2Array(this.additional);
			if(arrays == null || arrays.isEmpty()) {
				return this.additionalCacheMap;
			}
			
			for (String[] elements : arrays) {
				int key = Integer.valueOf(elements[0].trim());
				int value = Integer.valueOf(elements[1].trim());
				this.additionalCacheMap.put(key, value);
			}
		}
		return this.additionalCacheMap;
	}
}
