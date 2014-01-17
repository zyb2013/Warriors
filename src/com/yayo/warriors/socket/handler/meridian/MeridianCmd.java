package com.yayo.warriors.socket.handler.meridian;

import com.yayo.warriors.module.meridian.constant.MeridianConstant;

public interface MeridianCmd {
	
	int RUSH_MERIDIAN = 1;
	
	
	@Deprecated
	int LOAD_MERIDIAN = 2;
	
	int LOAD_ADDED_ATTR = 3;
	
	int BREAKTHROUGH = 4;
	
	
	@Deprecated
	int STAGE_VALIDATE = 5;
	
	int ADD_EXP = 6;
	
	int PUSH_PLAYER_ADDEXP = 100;
	int PUSH_MERIDIAN_TIME = 101;
	
}
