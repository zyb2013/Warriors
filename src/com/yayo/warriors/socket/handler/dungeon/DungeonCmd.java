package com.yayo.warriors.socket.handler.dungeon;

import com.yayo.warriors.module.dungeon.constant.DungeonConstant;
import com.yayo.warriors.module.dungeon.vo.DungeonVo;
import com.yayo.warriors.socket.vo.ChangeScreenVo;

public interface DungeonCmd {
	int ENTER_DUNGEON  = 1; 
	int EXIT_DUNGEON   = 2;
	int LOAD_USER_DUNGEON   = 3;  
	int LOAD_DUNGEON = 4;
	int LOAD_USER_STORY = 5;
	int REWARD_STORY_DUNGEON = 6;
	int VERIFY_STORY_DUNGEON = 7;
	int PUT_COERCE_IN_DUNGEON = 100;
	int PUT_COERCE_OUT_DUNGEON = 101;
	int PUT_DUNGEON_COMPLETE = 102;
	int PUT_DUNGEON_FAIL = 103;
	int PUT_NOTICE_PROGRESS = 104;
	int PUT_DUNGEON_STATISTICS = 105;
}
