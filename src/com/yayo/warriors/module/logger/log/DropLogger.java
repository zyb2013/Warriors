package com.yayo.warriors.module.logger.log;

import com.yayo.warriors.module.drop.model.LootWrapper;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.model.Term;
import com.yayo.warriors.module.logger.model.TraceLog;
import com.yayo.warriors.module.logger.type.LogType;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.type.Currency;

/**
 * 掉落日志
 * 
 * @author huachaoping
 */
public class DropLogger extends TraceLog {
	
	
	/**
	 * 拾取货币日志
	 * 
	 * @param player
	 * @param source
	 * @param currency
	 * @param lootWrapper
	 */
	public static void pickupMoneyLogger(Player player, Source source, Currency currency, LootWrapper lootWrapper) {
		if(currency == Currency.GOLDEN) {
			log(LogType.DROP, Term.moneyIncomeTerm(),
							  Term.valueOf(SOURCE, source.getCode()),
							  Term.valueOf(PLAYERID, player.getId()),
							  Term.valueOf(PLAYER_NAME, player.getName()),
							  Term.valueOf(USER_NAME, player.getUserName()),
							  Term.valueOf(GOLDEN, lootWrapper.getAmount()),
							  Term.valueOf(MAP_ID, lootWrapper.getMapId()),
							  Term.valueOf(MONSTER_NAME, lootWrapper.getMonsterName()));
		} else if(currency == Currency.SILVER) {
			log(LogType.DROP, Term.moneyIncomeTerm(),
					  		  Term.valueOf(SOURCE, source.getCode()),
					  		  Term.valueOf(PLAYERID, player.getId()),
					  		  Term.valueOf(PLAYER_NAME, player.getName()),
					  		  Term.valueOf(USER_NAME, player.getUserName()),
					  		  Term.valueOf(SILVER, lootWrapper.getAmount()),
					  		  Term.valueOf(MAP_ID, lootWrapper.getMapId()),
					  		  Term.valueOf(MONSTER_NAME, lootWrapper.getMonsterName()));
		}
		
	}
	
	
	/**
	 * 拾取物品日志
	 * 
	 * @param player
	 * @param source
	 * @param lootWrapper
	 * @param goodsInfos
	 */
	public static void pickupPropsLogger(Player player, Source source, LootWrapper lootWrapper, LoggerGoods... goodsInfos) {
		log(LogType.DROP, goodsInfos, Term.valueOf(SOURCE, source.getCode()),
		  		  		  			  Term.valueOf(PLAYERID, player.getId()),
		  		  		  			  Term.valueOf(PLAYER_NAME, player.getName()),
		  		  		  			  Term.valueOf(USER_NAME, player.getUserName()),
		  		  		  			  Term.valueOf(MAP_ID, lootWrapper.getMapId()),
		  		  		  			  Term.valueOf(MONSTER_NAME, lootWrapper.getMonsterName()));
	}
	
}
