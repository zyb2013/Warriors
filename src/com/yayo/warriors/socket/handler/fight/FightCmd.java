package com.yayo.warriors.socket.handler.fight;


public interface FightCmd {
	int ACTIVE_FIGHT = 1;
	int JUMP_COMPLETE = 2;
	int OTHER_FIGHT = 3;
	int GET_USER_BUFF = 4;
	int NOTICE_BUFF_TIMEOUT = 5;
	int PUSH_FIGHT_REPORT = 101;
	int PUSH_DOT_DAMAGE = 102;
	int PUSH_BUFFER_CLEAR = 103;
}
