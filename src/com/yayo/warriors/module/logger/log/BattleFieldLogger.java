package com.yayo.warriors.module.logger.log;

import java.util.Date;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.yayo.common.utility.DatePattern;
import com.yayo.common.utility.DateUtil;
import com.yayo.warriors.module.battlefield.entity.PlayerBattleField;
import com.yayo.warriors.module.battlefield.model.BattleRoom;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.model.Term;
import com.yayo.warriors.module.logger.model.TraceLog;
import com.yayo.warriors.module.logger.type.LogType;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;

/**
 * 乱武战场日志文件
 * 
 * @author jonsai
 */
public class BattleFieldLogger extends TraceLog {

	private static final LogType type = LogType.BATTLE_FILED; 
	
	/**
	 * 记录角色战场奖励状态
	 * @param player
	 * @param playerBattle
	 * @param addExp
	 * @param playerBattleField
	 * @param goodsInfos
	 */
	public static void log(Player player, PlayerBattle playerBattle, int addExp,PlayerBattleField playerBattleField, LoggerGoods...goodsInfos) {
		TraceLog.log(type, goodsInfos, Term.valueOf(TYPE, 0),
									   Term.valueOf(PLAYERID, player.getId()),
									   Term.valueOf(PLAYER_NAME, player.getName()),
									   Term.valueOf(USER_NAME, player.getUserName()),
									   Term.valueOf(SOURCE, Source.EXP_BATTLE_FIELD_REWARD.getCode()),
									   Term.valueOf(KILLP_LAYERS, playerBattleField.getKillPlayers()),
									   Term.valueOf(DEATHS, playerBattleField.getDeaths()),
									   Term.valueOf(FIGHT_HONOR, playerBattleField.getFightHonor()),
									   Term.valueOf(COLLECT_HONOR, playerBattleField.getCollectHonor()),
									   Term.valueOf(ADDEXP, addExp),
									   Term.valueOf(EXP, playerBattle.getExp()),
									   Term.valueOf(LEVEL, playerBattle.getLevel())
									   );
	};
	
	/**
	 * 记录战场信息
	 * @param room
	 */
	public static void logBattleInfo(Date battleDate, CopyOnWriteArrayList<BattleRoom> rooms, ConcurrentMap<Long, BattleRoom> playerRoomMap) {
		if(battleDate == null || rooms == null || playerRoomMap == null){
			return ;
		}
		TraceLog.log(type, Term.valueOf(TYPE, 1), Term.valueOf(BATTLE_FIELD_DATE, DateUtil.date2String(battleDate, DatePattern.PATTERN_YYYYMMDDHHMM)), Term.valueOf(ROOM_NUM, rooms.size()), Term.valueOf(PLAYER_NUM, playerRoomMap.size()) );
	};

}
