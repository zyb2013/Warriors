package com.yayo.warriors.socket.handler.campbattle;


import com.yayo.warriors.module.campbattle.constant.CampBattleConstant;
import com.yayo.warriors.module.campbattle.type.CampTitle;
import com.yayo.warriors.module.campbattle.vo.BattleInfoVO;
import com.yayo.warriors.module.campbattle.vo.CampBattleVO;
import com.yayo.warriors.module.campbattle.vo.PlayerBattleVO;
import com.yayo.warriors.socket.vo.ChangeScreenVo;


public interface CampBattleCmd {
	
	int APPLY = 1;
	int GET_APPLY_PLAYERS = 2;
	int ADJUST_APPLY_PLAYER_PRIORITY = 3;
	int ENTER_CAMPBATTLE = 4;
	int GET_PLAYER_SCORES = 5;
	int EXIST_CAMPBATTLE = 6;
	int REWARD_SALARY = 7;
	int GET_CAMP_TITLE_PLAYERS = 8;
	int GET_CAMP_LEADER = 9;
	int GET_CAMP_BATTLE_HISTORY = 10;
	int REWARDS = 11;
	int GET_APPLY_STATUS = 12;
	int GET_CAMP_BATTLE_STATUS = 13;
	int GET_CAMP_BATTLE_DATES = 14;
	int GET_REWARD_STATUS = 15;
	int CAMP_BATTLE_REQUEST_CMD = 16;
	int REWARD_SUIT = 17;
	int GET_PLAYER_SCORE_INFO = 18;
	int GET_SCORE_HISTORY = 19;
	int PUSH_CAMP_BATTLE_INFO = 101;
	int PUSH_CAMP_BATTLE_CMD = 102;
	int PUSH_POINT_ATTACKED = 103;
}
