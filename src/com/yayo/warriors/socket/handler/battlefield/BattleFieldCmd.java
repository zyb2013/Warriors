package com.yayo.warriors.socket.handler.battlefield;

import com.yayo.warriors.module.battlefield.constant.BattleFieldConstant;
import com.yayo.warriors.module.battlefield.vo.BattleFieldVO;
import com.yayo.warriors.module.battlefield.vo.CollectTaskVO;
import com.yayo.warriors.module.campbattle.constant.CampBattleConstant;
import com.yayo.warriors.module.props.entity.UserProps;


public interface BattleFieldCmd {
	
	int ENTER_BATTLEFIELD = 1;
	int EXIT_BATTLE_FIELD = 2;
	int REWARD = 3;
	int ACCEPT_COLLECT_TASK = 4;
	int REWARD_COLLECT_TASK = 5;
	int BATTLE_REQUEST_CMD = 6;
	int PUSH_BATTLE_FIELD_INFO = 101;
	int PUSH_COLLECT_TASK_CHANGE = 102;
	int PUSH_BATTLE_FIELD_CMD = 103;
}
