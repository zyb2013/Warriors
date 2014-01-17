package com.yayo.warriors.module.logger.log;

import com.yayo.warriors.basedb.model.DungeonConfig;
import com.yayo.warriors.module.dungeon.model.Dungeon;
import com.yayo.warriors.module.logger.model.Term;
import com.yayo.warriors.module.logger.model.TraceLog;
import com.yayo.warriors.module.logger.type.LogType;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.type.AttributeKeys;

/**
 * 副本日志 
 * @author liuyuhua
 */
public class DungeonLogger extends TraceLog {
	

	public static void dungeonLogger(Player player,PlayerBattle battle,Dungeon dungeon){
		if(player == null || battle == null || dungeon == null){
			return;
		}
		DungeonConfig config = dungeon.getDungeonConfig();
		if(config == null){
			return;
		}
		
		 log(LogType.DUNGEON, Term.valueOf(SOURCE, Source.DUNGEON.getCode()),
				              Term.valueOf(PLAYERID, player.getId()),
				              Term.valueOf(PLAYER_NAME, player.getName()),
				              Term.valueOf(LEVEL, battle.getLevel()),
				              Term.valueOf(FIGHT_CAPACITY, battle.getAttribute(AttributeKeys.FIGHT_TOTAL_CAPACITY)),
				              Term.valueOf(DUNGEON_NAME, config.getName()),
				              Term.valueOf(BASE_ID, config.getId())
				 );
	}

}
