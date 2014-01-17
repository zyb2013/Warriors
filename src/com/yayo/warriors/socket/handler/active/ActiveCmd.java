package com.yayo.warriors.socket.handler.active;

import com.yayo.warriors.module.active.vo.ActiveBossRefreshVO;
import com.yayo.warriors.module.active.vo.ActiveDungeonVO;


public interface ActiveCmd {

	int BOSS_REFRESH_ACTIVE = 1;
	int DAILY_DUNGEON_ACTIVE = 2;
	int DAILY_TASK_ACTIVE = 3;
	int SUBLIS_OPERATOR_ACTIVE = 10;
	int REWARD_RANK_ACTIVE = 11;
	int REWARD_LEVEL_ACTIVE = 12;
	int REWARD_EXCHANGE_ACTIVE = 13;
	int CLIENT_REWARD_VIRIFI = 14;
}
