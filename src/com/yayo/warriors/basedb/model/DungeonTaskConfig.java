package com.yayo.warriors.basedb.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;

@Resource
public class DungeonTaskConfig {
	
	@Id
	private int id;
	
	/** 任务名字*/
	private String name;
	
	/** 任务事件 (格式:{任务类型_条件_数量|任务类型_条件_数量})*/
	private String events;
	
	/** 任务道具奖励*/
	private String rewardPorps;
	
	/** 任务奖励经验*/
	private int rewardExp;
	
	/** 任务奖励银币*/
	private int rewardSliver;
	
	/** 任务奖励真气*/
	private int rewardGas;
	
	/** 配置*/
	@JsonIgnore
	private Collection<DungeonTaskEventConfig> eventConfigs;
	
	/** 奖励的道具*/
	@JsonIgnore
	private Map<Integer, Integer> rewardItems;
//	private Collection<DungeonTaskProps> rewardItems;
	/**
	 * 获取任务奖励道具
	 * <per>解析{@link DungeonTaskConfig#rewardPorps}</per>
	 * @return
	 */
	public Map<Integer, Integer> getRewardItems(){
		if(rewardItems != null){
			return this.rewardItems;
		}
		
		synchronized (this) {
			if(rewardItems != null){
				return this.rewardItems;
			}
			
			rewardItems = new HashMap<Integer, Integer>(1);
			if(StringUtils.isBlank(rewardPorps)){
				return rewardItems;
			}
			
			List<String[]> rewards = Tools.delimiterString2Array(rewardPorps);
			for(String[] reward : rewards){
				if(reward.length < 2){
					continue;
				}
				
				int propsId = Integer.parseInt(reward[0]);
				int count = Integer.parseInt(reward[1]);
				Integer cacheCount = this.rewardItems.get(propsId);
				cacheCount = cacheCount == null ? 0 : cacheCount;
				int totalCount = cacheCount + count;
				if(totalCount <= 0) {
					this.rewardItems.remove(propsId);
				} else {
					this.rewardItems.put(propsId, totalCount);
				}
			}
		}
		return this.rewardItems;
	}
	
	
	/**
	 * 获取任务事件 
	 * <per>解析{@link DungeonTaskConfig#eventConfigs}</per>
	 * @return
	 */
	public Collection<DungeonTaskEventConfig> getEventConfig(){
		if(eventConfigs != null){
			return this.eventConfigs;
		}
		
		synchronized (this) {
			if(eventConfigs != null){
				return this.eventConfigs;
			}
			
			eventConfigs = new ArrayList<DungeonTaskEventConfig>();
			if(events == null){
				return eventConfigs;
			}
			
			List<String[]> str_events = Tools.delimiterString2Array(events);
			if(str_events == null || str_events.isEmpty()){
				return eventConfigs;
			}
			int index = 1;
			for(String[] event : str_events){
				if(event.length < 3){
					continue;
				}
				int type = Integer.parseInt(event[0]);
				int condition = Integer.parseInt(event[1]);
				int completeCount = Integer.parseInt(event[2]);
				DungeonTaskEventConfig eventConfig = DungeonTaskEventConfig.valueOf(index, type, completeCount,condition);
				eventConfigs.add(eventConfig);
				index++;
			}
			
			return this.eventConfigs;
		}
	}
	

	//Getter and Setter....
	
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

	public String getEvents() {
		return events;
	}

	public void setEvents(String events) {
		this.events = events;
	}

	public String getRewardPorps() {
		return rewardPorps;
	}

	public void setRewardPorps(String rewardPorps) {
		this.rewardPorps = rewardPorps;
	}

	public int getRewardExp() {
		return rewardExp;
	}

	public void setRewardExp(int rewardExp) {
		this.rewardExp = rewardExp;
	}

	public int getRewardSliver() {
		return rewardSliver;
	}

	public void setRewardSliver(int rewardSliver) {
		this.rewardSliver = rewardSliver;
	}

	public int getRewardGas() {
		return rewardGas;
	}

	public void setRewardGas(int rewardGas) {
		this.rewardGas = rewardGas;
	}

	@Override
	public String toString() {
		return "DungeonTaskConfig [id=" + id + ", name=" + name + ", events="
				+ events + ", rewardPorps=" + rewardPorps + ", rewardExp="
				+ rewardExp + ", rewardSliver=" + rewardSliver + ", rewardGas="
				+ rewardGas + "]";
	}
}
