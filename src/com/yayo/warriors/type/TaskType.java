package com.yayo.warriors.type;

/**
 * 任务类型
 * 
 * @author Hyint
 */
public interface TaskType {

	/** 主线任务 */
	int MAIN_TASK = 1;

	/** 支线任务 */
	int BRANCH_TASK = 2;
	
	/** 日环任务 */
	int DAY_CIRCLE = 3;
	
	/** 循环( 图环)任务 */
	int MAP_TASK = 4;

	/** 阵营任务 */
	int CAMP_TASK = 5;

	/** 押镖任务 */
	int ESCORT_TASK = 6;

	/** 试炼任务 */
	int PRACTICE_TASK = 7;

	/** 帮派任务 */
	int ALLIANCE_TASK = 8;
	
	/** 副本任务 */
	int DUNGEON_TASK = 9;
}
