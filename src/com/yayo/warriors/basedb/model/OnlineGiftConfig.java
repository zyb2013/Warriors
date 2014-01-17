package com.yayo.warriors.basedb.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;

/**
 * 在线礼包数据
 * 
 * @author huachaoping
 */
@Resource
public class OnlineGiftConfig {
	
	/** 主键ID */
	@Id
	private int id;
	
	/** 倒计时长(分钟) */
	private int obtainTime;
	
	/** 道具奖励信息: 道具基础ID_数量|..  */
	private String giftProps;
	
	/**
	 *  礼包奖励{道具基础ID, 数量}
	 */
	@JsonIgnore
	private Map<Integer, Integer> giftMap = null;
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getObtainTime() {
		return obtainTime;
	}

	public void setObtainTime(int obtainTime) {
		this.obtainTime = obtainTime;
	}

	public String getGiftProps() {
		return giftProps;
	}

	public void setGiftProps(String giftProps) {
		this.giftProps = giftProps;
	} 
	
	
	public Map<Integer, Integer> getGiftMap() {
		if(this.giftMap != null) {
			return this.giftMap;
		}
		
		synchronized (this) {
			if(this.giftMap != null) {
				return this.giftMap;
			}
			
			this.giftMap = new HashMap<Integer, Integer>(1);
			List<String[]> arrays = Tools.delimiterString2Array(this.giftProps);
			if(arrays == null || arrays.isEmpty()) {
				return this.giftMap;
			}
			
			for (String[] array : arrays) {
				Integer propsId = Integer.valueOf(array[0]);
				Integer count = Integer.valueOf(array[1]);
				this.giftMap.put(propsId, count);
			}
			
		}
		return this.giftMap;
	}
	
}
