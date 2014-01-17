package com.yayo.warriors.socket.handler.map;

import com.yayo.warriors.module.map.constant.MapConstant;
import com.yayo.warriors.socket.vo.ChangeScreenVo;


public interface MapCmd {

	int MAP_ENTERSCREEN = 1;
	int MAP_MOTION_PATH = 2;
	int MAP_PLAYER_MOTION = 3;
	int MAP_PLAYER_CHANGESCREEN = 4;
	int CAMP_CHANGESCREEN = 5;
	int CHECK_PLAYER_DEAD_STATE = 6;
	int ACCEPT_CONVENE_INVITE = 7;
	int PUT_MAP_COERCE_RETRUN = 100;
	int PUT_MAP_ANIMAL_ADD = 102;
	int PUT_MAP_ANIMAL_REMOVE = 103;
	int PUT_MAP_MOTION_PATH = 104;
	int PUT_MAP_PLAYER_GO = 105;
	int PUT_MAP_IPSIRE = 106 ;
	int PUSH_DEAD_STATE = 107;
	int PUSH_CONVENE_INVITE = 108;
	
}
