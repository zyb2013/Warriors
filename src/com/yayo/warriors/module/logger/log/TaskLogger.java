package com.yayo.warriors.module.logger.log;

import org.apache.commons.lang.StringUtils;

import com.yayo.warriors.basedb.model.TaskConfig;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.model.Term;
import com.yayo.warriors.module.logger.model.TraceLog;
import com.yayo.warriors.module.logger.type.LogType;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.task.model.TaskRewardVO;
import com.yayo.warriors.module.user.entity.Player;

/**
 * 任务日志工具类
 * 
 * @author Hyint
 */
public class TaskLogger extends TraceLog {

	/**
	 * 任务操作记录
	 * 
	 * @param player		角色信息
	 * @param task			任务配置
	 * @param source		操作来源
	 * @param infos			详细物品收入信息
	 */
	public static void taskLogger(Player player, TaskConfig task, Source source, LoggerGoods...goodsInfo) {
		log(LogType.TASK, goodsInfo, Term.valueOf(PLAYERID, player.getId()),
							 	 	 Term.valueOf(TASKID, task.getId()),
							 	 	 Term.valueOf(TASKTYPE, task.getType()),
							 	 	 Term.valueOf(SOURCE, source.getCode()),
							 	 	 Term.valueOf(USER_NAME, player.getUserName()),
							 	 	 Term.valueOf(PLAYER_NAME, player.getName()),
							 	 	 Term.valueOf(TASKNAME, StringUtils.defaultIfBlank(task.getName(), "")));
	}

	/**
	 * 领取任务奖励
	 * 
	 * @param player		角色信息
	 * @param battle		角色战斗对象
	 * @param task			任务配置
	 * @param orient		物品收支情况
	 * @param goodsInfo		详细物品收入信息
	 */
	public static void rewardTask(Player player, TaskConfig task, TaskRewardVO rewardVO, LoggerGoods...goodsInfo) {
		log(LogType.TASK, goodsInfo, Term.valueOf(TASKID, task.getId()),
									 Term.valueOf(TASKTYPE, task.getType()),
								 	 Term.valueOf(PLAYERID, player.getId()),
								 	 Term.valueOf(USER_NAME, player.getUserName()),
								 	 Term.valueOf(PLAYER_NAME, player.getName()),
								 	 Term.valueOf("addExp", rewardVO.getAddExp()),
								 	 Term.valueOf("addSilver", rewardVO.getAddSilver()),
								 	 Term.valueOf(SOURCE, Source.REWARDS_TASK.getCode()),
								 	 Term.valueOf(TASKNAME, StringUtils.defaultIfBlank(task.getName(), "")));
	}
}
