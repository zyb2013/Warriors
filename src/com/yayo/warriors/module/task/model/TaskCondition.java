package com.yayo.warriors.module.task.model;

import org.apache.commons.lang.StringUtils;

import com.yayo.common.utility.Splitable;
import com.yayo.warriors.basedb.model.LoopTaskConfig;
import com.yayo.warriors.module.task.type.EventType;
public class TaskCondition {

	private int type;
	
	private int condition;
	
	private int amount;
	
	private String taskParams;

	public int getType() {
		return type;
	}

	public int getCondition() {
		return condition;
	}

	public int getAmount() {
		return amount;
	}

	public static TaskCondition valueOf(String condition) {
		if(StringUtils.isBlank(condition)) {
			return null;
		}
		String[] array = condition.split(Splitable.ATTRIBUTE_SPLIT);
		TaskCondition taskCondition = new TaskCondition();
		taskCondition.type = Integer.valueOf(array[0].trim());
		taskCondition.condition = Integer.valueOf(array[1].trim());
		taskCondition.amount = Integer.valueOf(array[2].trim());
		return taskCondition;
	}
	
	/**
	 * @param type 任务类型
	 * @param condition 条件
	 * @param amount 数量
	 * @return
	 */
	public static TaskCondition valueOf(int type, int condition, int amount) {
		TaskCondition conditions = new TaskCondition();
		conditions.type = type;
		conditions.condition = condition;
		conditions.amount = amount;
		return conditions;
		
	}
	
	/**
	 * 创建任务条件
	 * 
	 * @param  task						任务对象
	 * @param  condition				任务条件
	 * @param  amount					完成条件的数量
	 * @param  params					任务参数
	 * @return {@link TaskCondition}	任务条件对象
	 */
	public static TaskCondition valueOf(LoopTaskConfig task, int condition, int amount, String params){
		TaskCondition conditions = new TaskCondition();
		conditions.amount = amount;
		conditions.type = task.getType();
		conditions.condition = condition;
		conditions.taskParams = params ;
		return conditions;
	}

	/** 
	 * 创建任务条件
	 * 
	 * @param  taskType					任务类型
	 * @param  condition				条件
	 * @param  amount					数量
	 * @param  params					玩家战斗信息
	 * @return {@link TaskCondition}	任务条件对象
	 */
	public static TaskCondition valueOf(int taskType, int condition, int amount, String params){
		TaskCondition conditions = new TaskCondition();
		conditions.amount = amount;
		conditions.type = taskType;
		conditions.condition = condition;
		conditions.taskParams = params ;
		return conditions;
	}
	
	
	@Override
	public String toString() {
		return "TaskCondition [type=" + type + ", condition=" + condition 
				+ ", amount=" + amount + ", taskParams=" + taskParams + "]";
	}

	public String getTaskParams() {
		return this.taskParams;
	}


}
