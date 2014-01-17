package com.yayo.warriors.module.logger.log;

import com.yayo.warriors.basedb.model.AllianceTaskConfig;
import com.yayo.warriors.basedb.model.CampTaskConfig;
import com.yayo.warriors.basedb.model.DungeonTaskConfig;
import com.yayo.warriors.basedb.model.EscortTaskConfig;
import com.yayo.warriors.basedb.model.MapTaskConfig;
import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.basedb.model.TaskConfig;
import com.yayo.warriors.module.logger.model.Term;
import com.yayo.warriors.module.logger.model.TraceLog;
import com.yayo.warriors.module.logger.type.LogType;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.type.TaskType;

/**
 * 经验日志. 记录玩家的经验获得日志
 * 
 * @author jonsai
 */
public class ExpLogger extends TraceLog {

	private static final LogType type = LogType.PLAYER_EXP; 

	/**
	 * 战斗获得经验
	 * 
	 * @param userDomain
	 * @param addExp
	 * @param monsterFight
	 */
	public static void fightExp(UserDomain userDomain, MonsterFightConfig monsterFight, int addExp) {
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		TraceLog.log(type, Term.valueOf(PLAYERID, userDomain.getPlayerId()),
		 		   		   Term.valueOf(PLAYER_NAME, player.getName()),
		 		   		   Term.valueOf(USER_NAME, player.getUserName()),
		 		   		   Term.valueOf(SOURCE, Source.EXP_FIGHT.getCode()),
		 		   		   Term.valueOf(ADDEXP, addExp),
		 		   		   Term.valueOf(EXP, battle.getExp()),
		 		   		   Term.valueOf(LEVEL, battle.getLevel()));
	}
	
	/**
	 * 主支线任务经验奖励
	 * 
	 * @param userDomain		用户域模型
	 * @param taskConfig		任务配置类
	 * @param addExp			获得的经验值
	 */
	public static void mainTaskExp(UserDomain userDomain, TaskConfig taskConfig, int addExp) {
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		TraceLog.log(type, Term.valueOf(ADDEXP, addExp),
						   Term.valueOf(EXP, battle.getExp()),
		 		   		   Term.valueOf(TASKID, taskConfig.getId()),
		 		   		   Term.valueOf(PLAYER_NAME, player.getName()),
		 		   		   Term.valueOf(TASKTYPE, taskConfig.getType()),
		 		   		   Term.valueOf(TASKNAME, taskConfig.getName()),
		 		   		   Term.valueOf(USER_NAME, player.getUserName()),
		 		   		   Term.valueOf(SOURCE, Source.EXP_TASK.getCode()),
		 		   		   Term.valueOf(PLAYERID, userDomain.getPlayerId()),
		 		   		   Term.valueOf(LEVEL, battle.getLevel()));
	}
	
	/**
	 * 副本任务经验奖励
	 * 
	 * @param userDomain		用户域模型
	 * @param taskConfig		任务配置类
	 * @param addExp			获得的经验值
	 */
	public static void dungeonTaskExp(UserDomain userDomain, DungeonTaskConfig taskConfig, int addExp) {
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		TraceLog.log(type,  Term.valueOf(ADDEXP, addExp),
							Term.valueOf(EXP, battle.getExp()),
							Term.valueOf(TASKID, taskConfig.getId()),
							Term.valueOf(PLAYER_NAME, player.getName()),
							Term.valueOf(TASKTYPE, TaskType.DUNGEON_TASK),
							Term.valueOf(TASKNAME, taskConfig.getName()),
							Term.valueOf(USER_NAME, player.getUserName()),
							Term.valueOf(SOURCE, Source.EXP_TASK.getCode()),
							Term.valueOf(PLAYERID, userDomain.getPlayerId()),
							Term.valueOf(LEVEL, battle.getLevel()));
	}

	/**
	 * 图环任务经验奖励
	 * 
	 * @param userDomain		用户域模型
	 * @param taskConfig		任务配置类
	 * @param addExp			获得的经验值
	 */
	public static void mapTaskExp(UserDomain userDomain, MapTaskConfig taskConfig, int addExp) {
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		TraceLog.log(type, Term.valueOf(ADDEXP, addExp),
						   Term.valueOf(EXP, battle.getExp()),
						   Term.valueOf(PLAYERID, userDomain.getPlayerId()),
						   Term.valueOf(PLAYER_NAME, player.getName()),
						   Term.valueOf(USER_NAME, player.getUserName()),
						   Term.valueOf(SOURCE, Source.EXP_TASK.getCode()),
						   Term.valueOf(TASKID, taskConfig.getId()),
						   Term.valueOf(TASKTYPE, TaskType.MAP_TASK),
						   Term.valueOf(TASKNAME, taskConfig.getName()),
						   Term.valueOf(LEVEL, battle.getLevel()));
	}
	
	/**
	 * 日环任务经验奖励
	 * 
	 * @param userDomain		用户域模型
	 * @param taskConfig		任务配置类
	 * @param addExp			获得的经验值
	 */
	public static void loopTaskExp(UserDomain userDomain, int addExp) {
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		TraceLog.log(type, Term.valueOf(ADDEXP, addExp),
						   Term.valueOf(EXP, battle.getExp()),
						   Term.valueOf(TASKTYPE, TaskType.DAY_CIRCLE),
						   Term.valueOf(PLAYER_NAME, player.getName()),
						   Term.valueOf(USER_NAME, player.getUserName()),
						   Term.valueOf(SOURCE, Source.EXP_TASK.getCode()),
						   Term.valueOf(PLAYERID, userDomain.getPlayerId()),
						   Term.valueOf(LEVEL, battle.getLevel()));
	}
	

	/**
	 * 阵营任务经验奖励
	 * 
	 * @param userDomain		用户域模型
	 * @param taskConfig		任务配置类
	 * @param addExp			获得的经验值
	 */
	public static void campTaskExp(UserDomain userDomain, CampTaskConfig taskConfig, int addExp) {
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		TraceLog.log(type,  Term.valueOf(ADDEXP, addExp),
							Term.valueOf(EXP, battle.getExp()),
							Term.valueOf(TASKID, taskConfig.getId()),
							Term.valueOf(TASKTYPE, TaskType.CAMP_TASK),
							Term.valueOf(PLAYER_NAME, player.getName()),
							Term.valueOf(TASKNAME, taskConfig.getName()),
							Term.valueOf(USER_NAME, player.getUserName()),
							Term.valueOf(SOURCE, Source.EXP_TASK.getCode()),
							Term.valueOf(PLAYERID, userDomain.getPlayerId()),
							Term.valueOf(LEVEL, battle.getLevel()));
	}

	/**
	 * 押镖任务经验奖励
	 * 
	 * @param userDomain		用户域模型
	 * @param taskConfig		任务配置类
	 * @param addExp			获得的经验值
	 */
	public static void escortTaskExp(UserDomain userDomain, EscortTaskConfig taskConfig, int addExp) {
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		TraceLog.log(type,  Term.valueOf(ADDEXP, addExp),
							Term.valueOf(EXP, battle.getExp()),
							Term.valueOf(TASKID, taskConfig.getId()),
							Term.valueOf(TASKTYPE, TaskType.CAMP_TASK),
							Term.valueOf(PLAYER_NAME, player.getName()),
							Term.valueOf(TASKNAME, taskConfig.getName()),
							Term.valueOf(USER_NAME, player.getUserName()),
							Term.valueOf(SOURCE, Source.EXP_TASK.getCode()),
							Term.valueOf(PLAYERID, userDomain.getPlayerId()),
							Term.valueOf(LEVEL, battle.getLevel()));
	}
	
	/**
	 * 试练任务经验奖励
	 * 
	 * @param userDomain		用户域模型
	 * @param taskConfig		任务配置类
	 * @param addExp			获得的经验值
	 */
	public static void practiceTaskExp(UserDomain userDomain, int addExp) {
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		TraceLog.log(type,  Term.valueOf(ADDEXP, addExp),
							Term.valueOf(EXP, battle.getExp()),
							Term.valueOf(PLAYER_NAME, player.getName()),
							Term.valueOf(USER_NAME, player.getUserName()),
							Term.valueOf(TASKTYPE, TaskType.PRACTICE_TASK),
							Term.valueOf(SOURCE, Source.EXP_TASK.getCode()),
							Term.valueOf(PLAYERID, userDomain.getPlayerId()),
							Term.valueOf(LEVEL, battle.getLevel()));
	}
	
	/**
	 * 试练任务经验奖励
	 * 
	 * @param userDomain		用户域模型
	 * @param taskConfig		任务配置类
	 * @param addExp			获得的经验值
	 */
	public static void allianceTaskExp(UserDomain userDomain, AllianceTaskConfig taskConfig, int addExp) {
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		TraceLog.log(type,  Term.valueOf(ADDEXP, addExp),
							Term.valueOf(EXP, battle.getExp()),
							Term.valueOf(TASKTYPE, TaskType.ALLIANCE_TASK),
							Term.valueOf(TASKID, taskConfig.getId()),
							Term.valueOf(TASKNAME, taskConfig.getName()),
							Term.valueOf(PLAYER_NAME, player.getName()),
							Term.valueOf(USER_NAME, player.getUserName()),
							Term.valueOf(SOURCE, Source.EXP_TASK.getCode()),
							Term.valueOf(PLAYERID, userDomain.getPlayerId()),
							Term.valueOf(LEVEL, battle.getLevel()));
	}
	
	/**
	 * 训练增加经验
	 * 
	 * @param  userDomain		用户域模型
	 * @param  addExp			增加的经验
	 */
	public static void trainExp(UserDomain userDomain, int addExp) {
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		TraceLog.log(type,  Term.valueOf(ADDEXP, addExp),
							Term.valueOf(EXP, battle.getExp()),
							Term.valueOf(PLAYER_NAME, player.getName()),
							Term.valueOf(USER_NAME, player.getUserName()),
							Term.valueOf(PLAYERID, userDomain.getPlayerId()),
							Term.valueOf(SOURCE, Source.EXP_TRAINING.getCode()),
							Term.valueOf(LEVEL, battle.getLevel()));
	}

	/**
	 * 使用道具增加经验
	 * 
	 * @param  userDomain		用户域模型
	 * @param  propsConfig		道具配置对象
	 * @param  addExp			增加的经验
	 */
	public static void itemExp(UserDomain userDomain, PropsConfig propsConfig, int addExp) {
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		TraceLog.log(type,  Term.valueOf(ADDEXP, addExp),
						    Term.valueOf(EXP, battle.getExp()),
						    Term.valueOf(ITEM_ID, propsConfig.getId()),
						    Term.valueOf(PLAYER_NAME, player.getName()),
						    Term.valueOf(USER_NAME, player.getUserName()),
						    Term.valueOf(ITEM_NAME, propsConfig.getName()),
						    Term.valueOf(SOURCE, Source.EXP_PROPS.getCode()),
						    Term.valueOf(PLAYERID, userDomain.getPlayerId()),
							Term.valueOf(LEVEL, battle.getLevel()));
	}

	/**
	 * 活动增加增加经验
	 * 
	 * @param  userDomain		用户域模型
	 * @param  propsConfig		道具配置对象
	 * @param  addExp			增加的经验
	 */
	public static void activeExp(UserDomain userDomain, Source source, String activeName, int addExp) {
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		TraceLog.log(type,  Term.valueOf(ADDEXP, addExp),
							Term.valueOf(EXP, battle.getExp()),
							Term.valueOf(ACTIVE_NAME, activeName),
							Term.valueOf(SOURCE, source.getCode()),
							Term.valueOf(PLAYER_NAME, player.getName()),
							Term.valueOf(USER_NAME, player.getUserName()),
							Term.valueOf(PLAYERID, userDomain.getPlayerId()),
							Term.valueOf(LEVEL, battle.getLevel()));
	}
	
	/**
	 * 战场增加经验
	 * 
	 * @param  userDomain		用户域模型
	 * @param  propsConfig		道具配置对象
	 * @param  addExp			增加的经验
	 */
	public static void battleFieldExp(UserDomain userDomain, Source source, int addExp) {
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		TraceLog.log(type,  Term.valueOf(ADDEXP, addExp),
							Term.valueOf(EXP, battle.getExp()),
							Term.valueOf(SOURCE, source.getCode()),
							Term.valueOf(PLAYER_NAME, player.getName()),
							Term.valueOf(USER_NAME, player.getUserName()),
							Term.valueOf(PLAYERID, userDomain.getPlayerId()),
							Term.valueOf(LEVEL, battle.getLevel()));
	}
	
	/**
	 * 好友祝福瓶增加经验
	 * 
	 * @param  userDomain		用户域模型
	 * @param  addExp			增加的经验
	 */
	public static void friendFieldExp(UserDomain userDomain, int addExp) {
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		TraceLog.log(type,  Term.valueOf(ADDEXP, addExp),
							Term.valueOf(EXP, battle.getExp()),
							Term.valueOf(PLAYER_NAME, player.getName()),
							Term.valueOf(USER_NAME, player.getUserName()),
							Term.valueOf(PLAYERID, userDomain.getPlayerId()),
							Term.valueOf(SOURCE, Source.EXP_REWARD_BLESS.getCode()),
							Term.valueOf(LEVEL, battle.getLevel()));
	}

	/**
	 * 其他增加经验
	 * 
	 * @param  userDomain		用户域模型
	 * @param  addExp			增加的经验
	 */
	public static void expReward(UserDomain userDomain, Source source, int addExp) {
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		TraceLog.log(type,  Term.valueOf(ADDEXP, addExp),
							Term.valueOf(EXP, battle.getExp()),
							Term.valueOf(SOURCE, source.getCode()),
							Term.valueOf(PLAYER_NAME, player.getName()),
							Term.valueOf(USER_NAME, player.getUserName()),
							Term.valueOf(PLAYERID, userDomain.getPlayerId()),
							Term.valueOf(LEVEL, battle.getLevel()));
	}
	
}
