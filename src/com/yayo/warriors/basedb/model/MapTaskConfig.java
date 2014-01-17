package com.yayo.warriors.basedb.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.task.model.RewardVO;
import com.yayo.warriors.module.task.model.TaskCondition;
import com.yayo.warriors.module.task.rule.TaskRule;
import com.yayo.warriors.module.task.type.EventStatus;
import com.yayo.warriors.module.task.type.EventType;

/**
 * 地图任务资源
 * 
 * @author Hyint
 */
@Resource
public class MapTaskConfig{
	
	/** 任务ID*/
	@Id
	private int id; 
	
	/** 任务名 */
	private String name;
	
	/** 接任务的最低等级 */
	private int minLevel;
	
	/** 接任务的最高等级 */
	private int maxLevel;
	
	/** 任务链 */
	private int chain;
	
	/** 下一个任务ID */
	private int nextId;
	
	/** 任务经验奖励 */
	private int exp;
	
	/** 任务游戏币奖励 */
	private int silver;
	
	/** 接任务的NPC */
	private int acceptNpc;
	
	/** 完成任务的NPC */
	private int completeNpc;
	
	/** 完成条件1. 格式: 条件类型_条件内容_完成条件的数量[_怪物ID_概率]| */
	private String addition1;
	 
	/** 是否可以放弃任务 */
	private boolean cancel;

	/** 道具奖励. 格式: 物品ID_物品数量_绑定状态(0-未绑定, 1-绑定)|物品ID_物品数量_绑定状态(0-未绑定, 1-绑定)| */
	private String itemRewards = "";
	
	@JsonIgnore
	private String taskEvents = null;
	/** 下个任务*/
	@JsonIgnore
	private MapTaskConfig nextTask = null;
	/** 任务奖励VO对象*/
	@JsonIgnore
	private List<RewardVO> rewardList = null;
	/** 使用完成按钮可以完成的事件类型 */
	@JsonIgnore
	private Set<Integer> useCompleteSet = null;
	/** 任务需要打怪的ID列表 */
	@JsonIgnore
	private Set<Integer> fightMonsterIds = null;
	/** 任务条件列表  */
	@JsonIgnore
	private List<TaskCondition> conditionList = null;
	/** 类型列表 */
	@JsonIgnore
	private Map<Integer, List<TaskCondition>> typeConditions = null;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public int getChain() {
		return chain;
	}

	public void setChain(int chain) {
		this.chain = chain;
	}

	public boolean isCancel() {
		return cancel;
	}

	public void setCancel(boolean cancel) {
		this.cancel = cancel;
	}

	public int getNextId() {
		return nextId;
	}

	public void setNextId(int nextId) {
		this.nextId = nextId;
	}

	public int getAcceptNpc() {
		return acceptNpc;
	}

	public void setAcceptNpc(int acceptNpc) {
		this.acceptNpc = acceptNpc;
	}

	public int getCompleteNpc() {
		return completeNpc;
	}

	public void setCompleteNpc(int completeNpc) {
		this.completeNpc = completeNpc;
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

	public String getAddition1() {
		return addition1;
	}

	public void setAddition1(String addition1) {
		this.addition1 = addition1;
	}


	public String getItemRewards() {
		return itemRewards;
	}

	public void setItemRewards(String itemRewards) {
		this.itemRewards = itemRewards;
	}

	public MapTaskConfig getNextTask() {
		return nextTask;
	}

	public void setNextTask(MapTaskConfig nextTask) {
		this.nextTask = nextTask;
	}

	public List<TaskCondition> getTaskConditions() {
		if(this.conditionList != null) {
			return this.conditionList;
		}
		
		synchronized (this) {
			if(this.conditionList != null) {
				return this.conditionList;
			}
			
			this.useCompleteSet = new HashSet<Integer>();
			this.fightMonsterIds = new HashSet<Integer>();
			this.conditionList = new ArrayList<TaskCondition>();
			this.typeConditions = new HashMap<Integer, List<TaskCondition>>();
			this.add2Condition(conditionList, TaskCondition.valueOf(this.addition1));
		}
		return this.conditionList;
	}
	
	/**
	 * 增加的条件列表中
	 * 
	 * @param conditions		完成任务的条件列表
	 * @param condition			任务条件
	 */
	private void add2Condition(List<TaskCondition> conditions, TaskCondition condition) {
		if(condition != null) {
			conditions.add(condition);
			int conditionType = condition.getType();
			if(TaskRule.validateEventType(conditionType)) {
				this.useCompleteSet.add(conditionType);
			}
			
			if(condition.getType() == EventType.KILLS) {
				fightMonsterIds.add(condition.getCondition());
			} 

			List<TaskCondition> list = typeConditions.get(conditionType);
			if(list == null) {
				list = new ArrayList<TaskCondition>();
				this.typeConditions.put(conditionType, list);
			}
			list.add(condition);
		}
	}
	
	public Set<Integer> getFightMonsterIds() {
		if(this.fightMonsterIds == null) {
			this.getTaskConditions();
		}
		return this.fightMonsterIds;
	}
	
	/**
	 * 根据事件类型获得任务条件列表
	 * 
	 * @param  type				事件类型
	 * @return {@link List}		任务条件列表
	 */
	public List<TaskCondition> getTaskConditionByType(int type) {
		if(this.typeConditions == null) {
			this.getTaskConditions();
		}
		return this.typeConditions.get(type);
	}
	
	public boolean hasEventType(int type) {
		if(this.typeConditions == null) {
			this.getTaskConditions();
		}
		return this.typeConditions.containsKey(type);
	}
	
	/**
	 * 获得可以使用完成按钮的类型列表
	 * 
	 * @return
	 */
	public Set<Integer> getUseCompleteTypes() {
		if(this.useCompleteSet == null) {
			getTaskConditions();
		}
		return useCompleteSet;
	}

	/**
	 * 获得任务奖励列表
	 * 
	 * @return 奖励VO信息
	 */
	public List<RewardVO> getRewardList() {
		if(this.rewardList != null) {
			return this.rewardList;
		}
		
		synchronized (this) {
			if(this.rewardList != null) {
				return this.rewardList;
			}
			this.rewardList = new ArrayList<RewardVO>();
			this.constructReward(rewardList, castReward2Map(this.itemRewards));
		}
		return this.rewardList;
	}

	/**
	 * 构建奖励对象
	 * 	
	 * @param type				物品的类型
	 * @param rewardList		奖励列表
	 * @param cacheMap			缓存集合
	 */
	private void constructReward(List<RewardVO> rewardList, Map<Integer, int[]> cacheMap) {
		if(cacheMap != null && !cacheMap.isEmpty()) {
			for (Entry<Integer, int[]> entry : cacheMap.entrySet()) {
				Integer baseId = entry.getKey();
				int[] value = entry.getValue();
				if(value[0] > 0) {
					this.rewardList.add(RewardVO.props(baseId, value[0], false));
				}
				if(value[1] > 0) {
					this.rewardList.add(RewardVO.props(baseId, value[1], true));
				}
			}
		}
	}

	/**
	 * 构建唯一的奖励信息
	 * 
	 * @param  arrays		分解字符串的信息
	 * @return {@link Map}	返回值集合
	 */
	private Map<Integer, int[]> castReward2Map(String rewardsInfo) {
		List<String[]> arrays = Tools.delimiterString2Array(rewardsInfo);
		if(arrays == null || arrays.isEmpty()) {
			return null;
		}
		
		Map<Integer, int[]> maps = new HashMap<Integer, int[]>(1);
		for (String[] element : arrays) {
			int baseId = Integer.valueOf(element[0]);
			int count = Integer.valueOf(element[1]);
			int[] array = maps.get(baseId);
			array = array == null ? new int[2] : array;
			if(Integer.valueOf(element[2]) < 1){ //未绑定的
				array[0] = array[0] + count;
			} else {
				array[1] = array[1] + count;
			}
			maps.put(baseId, array);
		}
		
		return maps;
	}
	
	public boolean isMatchingLevel(int playerLevel) {
		return playerLevel >= minLevel && playerLevel <= maxLevel;
	}
	
	public String getTaskEvents() {
		if(taskEvents != null) {
			return taskEvents;
		}
		
		List<TaskCondition> conditions = this.getTaskConditions();
		synchronized (this) {
			if(taskEvents != null) {
				return taskEvents;
			}
			
			StringBuilder builder = new StringBuilder();
			if(conditions != null && !conditions.isEmpty()) {
				for (TaskCondition taskCondition : conditions) {
					builder.append(taskCondition.getType()).append(Splitable.ATTRIBUTE_SPLIT)
						   .append(taskCondition.getCondition()).append(Splitable.ATTRIBUTE_SPLIT)
						   .append(taskCondition.getAmount()).append(Splitable.ATTRIBUTE_SPLIT)
						   .append(taskCondition.getAmount()).append(Splitable.ATTRIBUTE_SPLIT)
						   .append(EventStatus.PROGRESS.ordinal()).append(Splitable.ELEMENT_DELIMITER);
				}
				
				if(builder.length() > 0) {
					builder.deleteCharAt(builder.length() - 1);
				}
			}
			this.taskEvents = builder.toString();
		}
		return this.taskEvents;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "MapTaskConfig [id=" + id + ", name=" + name + ", minLevel=" + minLevel
				+ ", maxLevel=" + maxLevel + ", chain=" + chain + ", nextId=" + nextId
				+ ", acceptNpc=" + acceptNpc + ", completeNpc=" + completeNpc + ", addition1="
				+ addition1 + ", cancel=" + cancel + ", itemRewards=" + itemRewards + ", exp="
				+ exp + ", silver=" + silver + "]";
	}
}
