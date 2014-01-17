package com.yayo.warriors.basedb.model;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;

/**
 * 阵营任务配置对象
 * @author liuyuhua
 */
@Resource
public class CampTaskConfig {

	/** 任务的ID*/
	@Id
	private int id;
	
	/** 任务名 */
	private String name;
	
	/** 前置任务ID*/
	private int extaskId;

	/** 所属阵营*/
	private int camp;
	
	/** 任务事件 (格式:{任务类型_条件_数量|任务类型_条件_数量})*/
	private String addition1;
	
	/** 玩家最小等级限制*/
	private int minLevel;
	
	/** 玩家最大等级限制*/
	private int maxLevel;
	
	/** 任务奖励经验*/
	private int exp;
	
	/** 任务奖励铜币*/
	private int silver;
	
	@JsonIgnore
	private transient volatile List<String[]> eventslist = null;
	
	/** 任务中存在的时间类型*/
	@JsonIgnore
	private transient volatile List<Integer> eventTypes = null;
	
	
	/**
	 * 是否存在该类型事件
	 * @param type    事件类型
	 * @return true 存在 false 反之
	 */
	public boolean hasEventType(int type){
		if(eventTypes != null){
			return eventTypes.contains(type);
		}
		synchronized (this) {
			if(eventTypes != null){
				return eventTypes.contains(type);
			}
			eventTypes = new ArrayList<Integer>();
			List<String[]> events = this.getEventsList();
			if(events != null && !events.isEmpty()){
				for(String[] event : events){
					int eventType = Integer.parseInt(event[0]);
					if(!eventTypes.contains(eventType)){
						eventTypes.add(eventType);
					}
				}
			}
			return eventTypes.contains(type);
		}
	}
	
	
	/**
	 * 获取事件集合
	 * @return {@link List} 事件集合
	 */
	public List<String[]> getEventsList(){
		if(eventslist != null){
			return eventslist;
		}
		synchronized (this) {
			eventslist = new ArrayList<String[]>();
			if(addition1 == null || addition1.isEmpty()){
				return eventslist;
			}
			
			List<String[]> results = Tools.delimiterString2Array(addition1);
			for(String[] result : results){
				if(result.length >= 3){
					eventslist.add(result);
				}
			}
			return eventslist;
		}
	}
	

	//Getter and Setter
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public int getExtaskId() {
		return extaskId;
	}

	public void setExtaskId(int extaskId) {
		this.extaskId = extaskId;
	}

	public String getAddition1() {
		return addition1;
	}

	public void setAddition1(String addition1) {
		this.addition1 = addition1;
	}

	public int getCamp() {
		return camp;
	}

	public void setCamp(int camp) {
		this.camp = camp;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public int getSilver() {
		return silver;
	}

	public void setSilver(int silver) {
		this.silver = silver;
	}

	public int getMinLevel() {
		return minLevel;
	}

	public void setMinLevel(int minLevel) {
		this.minLevel = minLevel;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}

	@Override
	public String toString() {
		return "CampTaskConfig [id=" + id + ", extaskId=" + extaskId
				+ ", camp=" + camp + ", addition1=" + addition1 + ", minLevel="
				+ minLevel + ", maxLevel=" + maxLevel + ", exp=" + exp
				+ ", silver=" + silver + "]";
	}
	
}
