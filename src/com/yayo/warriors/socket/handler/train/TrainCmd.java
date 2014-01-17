package com.yayo.warriors.socket.handler.train;

import com.yayo.warriors.module.onhook.constant.TrainConstant;
import com.yayo.warriors.module.onhook.vo.TrainVo;
import com.yayo.warriors.module.search.vo.CommonSearchVo;

public interface TrainCmd {
	
	int LOAD_TRAIN_INFO = 1;
	int RECEIVE_TRAIN_REWARD = 2;
	int START_TRAIN = 3;
	int START_SINGLE_TRAIN = 4;
	int RECEIVE_AWARD = 5;
	int CANCEL_SINGLE_TRAIN = 6;
	int INVITE_COUPLE_TRAIN = 7;
	int REJECT_COUPLE_TRAIN = 8;
	int ACCEPT_COUPLE_TRAIN = 9;
	int SEARCH_COUPLE_PLAYER = 10;
	int SAVE_DIRECTION = 11;
	int APPLY_COUPLES_TRAIN = 100;
	int COUPLES_TRAIN_PROCESS = 101;
	int PUSH_SCREEN_TRAIN_MESSAGE = 102;
	int PUSH_TRAIN_ATTR_MESSAGE = 103;
	int PUSH_CANCEL_COUPLE_TRAIN = 104;
	int PUSH_PLAYER_DIRECTION = 105;
}
