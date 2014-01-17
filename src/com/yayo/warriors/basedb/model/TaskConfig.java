package com.yayo.warriors.basedb.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
import com.yayo.warriors.module.user.type.Camp;

/**
 * 任务资源
 * 
 * @author Hyint
 */
@Resource
public class TaskConfig{
	
	/** 任务ID*/
	@Id
	private int id; 
	
	/** 任务类型, 1-主线,2-支线,3-日常 */
	private int type;
	
	/** 任务名 */
	private String name;
	
	/** 接任务的最低等级 */
	private int minLevel;
	
	/** 接任务的最高等级 */
	private int maxLevel;
	
	/** 任务链 */
	private int chain;
	
	/** 下一个任务ID */
	private String nextId;
	
	/** 接任务的NPC */
	private int acceptNpc;
	
	/** 完成任务的NPC */
	private int completeNpc;
	
	/** 完成条件1. 格式: 条件类型_条件内容_完成条件的数量[_怪物ID_概率]| */
	private String addition1;
	
	/** 完成条件2. 格式: 条件类型_条件内容_完成条件的数量[_怪物ID_概率]| */
	private String addition2;
	
	/** 完成条件3. 格式: 条件类型_条件内容_完成条件的数量[_怪物ID_概率]| */
	private String addition3;
	
	/** 完成条件4. 格式: 条件类型_条件内容_完成条件的数量[_怪物ID_概率]| */
	private String addition4;

	/** 是否可以放弃任务 */
	private boolean cancel;

	/** 接任务的阵营需求*/
	private int camp;
	
	/** 任务经验奖励 */
	private int exp;
	
	/** 任务游戏币奖励 */
	private int silver;
	
	/** 真气奖励 */
	private int gas;
	
	/** 道具奖励. 格式: 物品ID_物品数量_绑定状态(0-未绑定, 1-绑定)|... */
	private String itemRewards = "";

	/** 装备奖励. 格式: 物品ID_物品数量_强化星级_是否绑定(0-未绑定, 1-绑定)|... */
	private String equipRewards = "";

	/** 任务物品. 格式: 物品ID_物品数量|... */
	private String questItems = "";
	
	@JsonIgnore
	private String taskEvents = null;
	
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
	/** 任务物品列表. { 道具ID, 道具数量 } */
	@JsonIgnore
	private Map<Integer, Integer> questItemMap = null;
	/** 类型列表 */
	@JsonIgnore
	private Map<Integer, List<TaskCondition>> typeConditions = null;
	/** 下个任务*/
	@JsonIgnore
	private List<TaskConfig> nextTasks = new LinkedList<TaskConfig>();
	
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

	public String getNextId() {
		return nextId;
	}

	public void setNextId(String nextId) {
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

	public int getGas() {
		return gas;
	}

	public void setGas(int gas) {
		this.gas = gas;
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

	public String getAddition2() {
		return addition2;
	}

	public void setAddition2(String addition2) {
		this.addition2 = addition2;
	}

	public String getAddition3() {
		return addition3;
	}

	public void setAddition3(String addition3) {
		this.addition3 = addition3;
	}

	public String getAddition4() {
		return addition4;
	}

	public void setAddition4(String addition4) {
		this.addition4 = addition4;
	}

	public String getEquipRewards() {
		return equipRewards;
	}

	public void setEquipRewards(String equipRewards) {
		this.equipRewards = equipRewards;
	}

	public String getItemRewards() {
		return itemRewards;
	}

	public void setItemRewards(String itemRewards) {
		this.itemRewards = itemRewards;
	}

	public String getQuestItems() {
		return questItems;
	}

	public void setQuestItems(String questItems) {
		this.questItems = questItems;
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
			this.add2Condition(conditionList, TaskCondition.valueOf(this.addition2));
			this.add2Condition(conditionList, TaskCondition.valueOf(this.addition3));
			this.add2Condition(conditionList, TaskCondition.valueOf(this.addition4));
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
		return getConditionTypes().contains(type);
	}
	
	public Set<Integer> getConditionTypes() {
		if(this.typeConditions == null) {
			this.getTaskConditions();
		}
		return typeConditions.keySet();
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
			this.constructItemReward(rewardList);
			this.constructEquipReward(rewardList);
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
	private void constructItemReward(List<RewardVO> rewardList) {
		List<String[]> arrays = Tools.delimiterString2Array(this.itemRewards);
		if(arrays == null || arrays.isEmpty()) {
			return;
		}
		
		// {道具ID, [未绑定数量, 绑定数量] }
		Map<Integer, int[]> maps = new HashMap<Integer, int[]>(1);
		for (String[] element : arrays) {
			int baseId = Integer.valueOf(element[0]);
			int count = Integer.valueOf(element[1]);
			int[] cache = maps.get(baseId);
			if(cache == null) {
				cache = new int[2];
				maps.put(baseId, cache);
			}
			
			if(element.length < 2 || Integer.valueOf(element[2]) == 0) { //非绑定的
				cache[0] = cache[0] + count;
			} else { //绑定的
				cache[1] = cache[1] + count;
			}
		}
		
		for (Entry<Integer, int[]> entry : maps.entrySet()) {
			int itemId = entry.getKey();
			int[] itemCount = entry.getValue();
			if(itemCount[0] > 0) {
				this.rewardList.add(RewardVO.props(itemId, itemCount[0], false));
			}
			if(itemCount[1] > 0) {
				this.rewardList.add(RewardVO.props(itemId, itemCount[1], true));
			}
		}
	}

	/**
	 * 构建装备奖励对象
	 * 	
	 * @param type				物品的类型
	 * @param rewardList		奖励列表
	 * @param cacheMap			缓存集合
	 */
	private void constructEquipReward(List<RewardVO> rewardList) {
		List<String[]> arrays = Tools.delimiterString2Array(this.equipRewards);
		if(arrays == null || arrays.isEmpty()) {
			return;
		}
		
		for (String[] element : arrays) {
			int baseId = Integer.valueOf(element[0]);
			int count = Integer.valueOf(element[1]);
			int starLevel = Integer.valueOf(element[2]);
			boolean binding = element.length > 3 && Integer.valueOf(element[3]) > 0;
			this.rewardList.add(RewardVO.equip(baseId, count, starLevel, binding));
		}
	}

	/**
	 * 获得任务道具列表 
	 * 
	 * @return {@link List}		任务道具列表
	 */
	public Map<Integer, Integer> getQuestItemMap() {
		if(this.questItemMap != null) {
			return this.questItemMap;
		}
		
		synchronized (this) {
			if(this.questItemMap != null) {
				return this.questItemMap;
			}
			
			this.questItemMap = new HashMap<Integer, Integer>(1);
			List<String[]> arrays = Tools.delimiterString2Array(this.questItems);
			if(arrays == null || arrays.isEmpty()) {
				return this.questItemMap;
			}
			
			for (String[] element : arrays) {
				Integer itemId = Integer.valueOf(element[0]);
				Integer amount = Integer.valueOf(element[1]);
				Integer cache = this.questItemMap.get(itemId);
				cache = cache == null ? 0 : cache;
				this.questItemMap.put(itemId, cache + amount);
			}
		}
		return this.questItemMap;
	}

	public boolean isMatchingLevel(int playerLevel) {
		return playerLevel >= minLevel && playerLevel <= maxLevel;
	}
	
	public boolean isCampConfirm(Camp camp) {
		return this.camp <= 0 || this.camp == camp.ordinal();
		
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
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCamp() {
		return camp;
	}

	public void setCamp(int camp) {
		this.camp = camp;
	}
	
	public void addNextTaskConfig(TaskConfig taskConfig) {
		if(taskConfig != null && !this.nextTasks.add(taskConfig)) {
			this.nextTasks.add(taskConfig);
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		
		TaskConfig other = (TaskConfig) obj;
		return id == other.id;
	}

	public List<TaskConfig> getNextTasks() {
		return nextTasks;
	}

	@Override
	public String toString() {
		return "TaskConfig [id=" + id + ", type=" + type + ", name=" + name + ", minLevel="
				+ minLevel + ", maxLevel=" + maxLevel + ", chain=" + chain + ", nextId=" + nextId
				+ ", acceptNpc=" + acceptNpc + ", completeNpc=" + completeNpc + ", addition1="
				+ addition1 + ", addition2=" + addition2 + ", addition3=" + addition3
				+ ", addition4=" + addition4 + ", cancel=" + cancel + ", camp=" + camp + ", exp="
				+ exp + ", silver=" + silver + ", gas=" + gas + ", itemRewards=" + itemRewards
				+ ", equipRewards=" + equipRewards + ", questItems=" + questItems + ", taskEvents="
				+ taskEvents + ", nextTasks=" + nextTasks + ", rewardList=" + rewardList
				+ ", useCompleteSet=" + useCompleteSet + ", fightMonsterIds=" + fightMonsterIds
				+ ", conditionList=" + conditionList + ", questItemMap=" + questItemMap
				+ ", typeConditions=" + typeConditions + "]";
	}
}
