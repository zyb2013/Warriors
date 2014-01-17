package com.yayo.warriors.module.logger.log;

import static com.yayo.common.utility.DatePattern.*;

import java.util.Date;

import com.yayo.common.utility.DateUtil;
import com.yayo.warriors.module.logger.LogKey;
import com.yayo.warriors.module.logger.model.Term;
import com.yayo.warriors.module.logger.model.TraceLog;
import com.yayo.warriors.module.logger.type.LogType;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerMotion;

/**
 * 等级日志. 记录玩家升级获得日志
 * 
 * @author jonsai
 */
public class PlayerLevelLogger extends TraceLog {

	private static final LogType type = LogType.PLAYER_LEVEL; 

	/**
	 * 角色经验变更
	 * 
	 * @param playerId			角色ID
	 * @param source			日志来源
	 * @param beforeLevel		增加前的经验
	 * @param level				增加后当前的等级
	 * @param date				升级时间
	 * @param player			角色对象
	 */
	public static void level(long playerId, Source source, int beforeLevel, int level, Date date, Player player, PlayerMotion playerMotion ) {
		TraceLog.log(type, Term.valueOf(PLAYERID, playerId),
				 		   Term.valueOf(PLAYER_NAME, player.getName()),
				 		   Term.valueOf(USER_NAME, player.getUserName()),
				 		   Term.valueOf(SOURCE, source.getCode()),
				 		   Term.valueOf(BEFORE_LEVEL, beforeLevel),
						   Term.valueOf(LEVEL, level),
						   Term.valueOf(MAP_ID, playerMotion.getMapId()),
						   Term.valueOf(LogKey.UPGRADE_TIME, DateUtil.date2String( date != null ? date : new Date() , PATTERN_YYYYMMDDHHMMSS))
						   );
	};
}
