package com.yayo.warriors.basedb.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceAdapter;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.model.LoopQualityConfig;
import com.yayo.warriors.basedb.model.LoopRewardConfig;
import com.yayo.warriors.basedb.model.LoopTaskConfig;
import com.yayo.warriors.basedb.model.MapTaskConfig;
import com.yayo.warriors.basedb.model.MonsterConfig;
import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.basedb.model.PracticeRewardConfig;
import com.yayo.warriors.basedb.model.TaskConfig;
import com.yayo.warriors.basedb.model.TaskMonsterConfig;
import com.yayo.warriors.module.props.type.Quality;
import com.yayo.warriors.type.IndexName;

/**
 * 任务服务适配器
 * 
 * @author Hyint
 */
@Component
public class TaskService extends ResourceAdapter {
	
	@Autowired
	private MonsterService monsterService;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	private List<LoopRewardConfig> LOOP_TASK_REWARD = new LinkedList<LoopRewardConfig>();
	private List<PracticeRewardConfig> PRACTICE_TASK_REWARDS = new LinkedList<PracticeRewardConfig>();
	
	
	@Override
	public void initialize() {
		LOOP_TASK_REWARD.clear();
		PRACTICE_TASK_REWARDS.clear();
		Collection<LoopRewardConfig> loopTaskRewards = resourceService.listAll(LoopRewardConfig.class);
		for (LoopRewardConfig loopReward : loopTaskRewards) {
			String propsId = loopReward.getPropsId();
			if(!StringUtils.isBlank(propsId) && loopReward.canReward()) {
				LOOP_TASK_REWARD.add(loopReward);
			}
		}

		Collection<PracticeRewardConfig> practiceRewards = resourceService.listAll(PracticeRewardConfig.class);
		for (PracticeRewardConfig practiceReward : practiceRewards) {
			if(practiceReward.canReward()) {
				PRACTICE_TASK_REWARDS.add(practiceReward);
			}
		}
		
		String simpleName = TaskConfig.class.getSimpleName();
		for (TaskConfig taskConfig : resourceService.listAll(TaskConfig.class)) {
			String nextIds = taskConfig.getNextId();
			if(StringUtils.isBlank(nextIds)) {
				continue;
			}

			int taskId = taskConfig.getId();
			for (String nextTaskId : nextIds.split(Splitable.ATTRIBUTE_SPLIT)) {
				if(!StringUtils.isBlank(nextTaskId)) {
					Integer nextId = Integer.valueOf(nextTaskId);
					taskConfig.addNextTaskConfig(this.get(nextId, TaskConfig.class));
					resourceService.addToIndex(consPreviousIdKey(simpleName, nextId), taskId, TaskConfig.class);
				}
			}
		}

		String mapConfigName = MapTaskConfig.class.getSimpleName();
		for (MapTaskConfig mapTaskConfig : resourceService.listAll(MapTaskConfig.class)) {
			int taskId = mapTaskConfig.getId();
			int nextTaskIdId = mapTaskConfig.getNextId();
			mapTaskConfig.setNextTask(this.get(nextTaskIdId, MapTaskConfig.class));
			resourceService.addToIndex(consPreviousIdKey(mapConfigName, nextTaskIdId), taskId, MapTaskConfig.class);
		}
		
		for (TaskMonsterConfig taskMonster : resourceService.listAll(TaskMonsterConfig.class)) {
			int monsterFightId = taskMonster.getId();
			MonsterFightConfig monsterFight = resourceService.get(monsterFightId, MonsterFightConfig.class);
			if(monsterFight == null) {
				logger.error("任务随机怪物表(TaskMonsterConfig): [{}] 对应的怪物战斗对象不存在", monsterFightId);
				continue;
			}
			
			List<MonsterConfig> monsterConfigs = monsterService.listMonsterConfig(monsterFightId);
			if(monsterConfigs == null || monsterConfigs.isEmpty()) {
				logger.error("任务随机怪物表(TaskMonsterConfig): [{}] 地图上没有怪物.. ", monsterFightId);
				continue;
			}
			
			int level = monsterFight.getLevel();
			taskMonster.getMonsters().addAll(monsterConfigs);
			resourceService.addToIndex(IndexName.TASK_MONSTER_ID, monsterFightId, TaskMonsterConfig.class, level);
		}
	}
	
	/**
	 * 获得任务怪物列表
	 * 
	 * @param  level		角色等级 
	 * @return {@link List}	怪物配置列表
	 */
	public List<TaskMonsterConfig> getTaskMonsterConfigs(int level) { 
		return resourceService.listByIndex(IndexName.TASK_MONSTER_ID, TaskMonsterConfig.class, level);
	}
	
	/**
	 * 构建上一个IDKey
	 * 
	 * @param  taskId			任务ID
	 * @return {@link String}	任务Key
	 */
	private String consPreviousIdKey(String clazzName, int taskId) {
		return new StringBuffer().append("NEXT_TASK_ID").append(taskId).toString();
	}
	
	/**
	 * 获得任务的上一个任务
	 * 
	 * @param  taskId			任务ID
	 * @return {@link List}		任务对象
	 */
	public List<TaskConfig> getPreviousTask(int taskId) {
		return resourceService.listByIndex(consPreviousIdKey(TaskConfig.class.getSimpleName(), taskId), TaskConfig.class);
	}
	
	/**
	 * 获得任务的上一个地图任务对象
	 * 
	 * @param  taskId					任务ID
	 * @return {@link MapTaskConfig}	任务对象
	 */
	public MapTaskConfig getPreviousMapTask(int taskId) {
		return resourceService.getByUnique(consPreviousIdKey(MapTaskConfig.class.getSimpleName(), taskId), MapTaskConfig.class);
	}
	
	/**
	 * 列出可以领取的试炼奖励配置信息
	 *  
	 * @return {@link Collection}		试炼奖励配置信息列表
	 */
	public Collection<PracticeRewardConfig> listCanPracticeRewardConfig() {
		return PRACTICE_TASK_REWARDS;
	}
	
	/**
	 * 列出可以领取的日环奖励配置信息
	 *  
	 * @return {@link Collection}	日环奖励配置信息列表
	 */
	public Collection<LoopRewardConfig> listCanLoopRewardConfig() {
		return LOOP_TASK_REWARD;
	}
	
	public <T> T get(Object id, Class<T> clazz) {
		return resourceService.get(id, clazz);
	}
	
	/**
	 * 获得日环任务随机品质
	 * @param currentQuality 
	 * 
	 * @return {@link Integer}	品质对象
	 */
	public int getRandomQuality() {
		Collection<LoopQualityConfig> list = resourceService.listAll(LoopQualityConfig.class);
		if(list == null || list.isEmpty()) {
			return Quality.WHITE.ordinal();
		}

		List<LoopQualityConfig> qualities = new ArrayList<LoopQualityConfig>(list);
		LoopQualityConfig quality = qualities.get(0);
		if(quality == null) {
			return Quality.WHITE.ordinal();
		}
		
		int total = 0;
		Collections.shuffle(qualities);
		int random = Tools.getRandomInteger(quality.getMaxRate()) + 1;
		for (LoopQualityConfig qualityConfig : qualities) {
			total += qualityConfig.getRate();
			if(random <= total) {
				return qualityConfig.getId();
			}
		}
		return Quality.WHITE.ordinal();
	}

	/**
	 * 随机构建日环任务
	 * 
	 * @return {@link LoopTaskConfig}	日环任务对象
	 */
	public LoopTaskConfig obtainLoopTaskByRandom() {
		int maxRate = 100;
		List<LoopTaskConfig> loopTaskList = new ArrayList<LoopTaskConfig>();
		Collection<LoopTaskConfig> loopTasks = resourceService.listAll(LoopTaskConfig.class);
		if(loopTasks != null && !loopTasks.isEmpty()) {
			loopTaskList.addAll(loopTasks);
			Collections.shuffle(loopTaskList);
			maxRate = loopTaskList.get(0).getMaxRate();
		}
		
		int total = 0;
		int rdn = Tools.getRandomInteger(maxRate) + 1;
		for (LoopTaskConfig loopTask : loopTaskList) {
			int rate = loopTask.getRate();
			if(rate <= 0) {
				continue;
			} 
			
			total += rate;
			if(rdn <= total) {
				return loopTask;
			}
		}
		return null;
	}
	
}
