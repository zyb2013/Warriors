package com.yayo.warriors.module.battlefield.manager;

import com.yayo.warriors.module.battlefield.entity.PlayerBattleField;

/**
 * 乱舞战场
 * @author jonsai
 *
 */
public interface BattleFieldManager {

	/**
	 * 取得玩家的乱舞战场对象
	 * @param playerId
	 * @return
	 */
	PlayerBattleField getPlayerBattleField(Long playerId);
	
	
}
