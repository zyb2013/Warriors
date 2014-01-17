package com.yayo.warriors.socket.handler.team;

import java.util.Map;

import com.yayo.warriors.module.team.constant.TeamConstant;
import com.yayo.warriors.module.team.model.MemberVO;
import com.yayo.warriors.module.team.model.TeamVO;
import com.yayo.warriors.module.team.type.TeamReason;


public interface TeamCmd {
	int SEND_INVITE = 1;
	int PROCESS_INVITE = 2;
	int SWAPE_LEADER = 3;
	int LEAVE_TEAM = 4;
	int KICK_OUT_TEAM = 5;
	int DISBAND_TEAM = 6;
	int GET_TEAM_INFO = 7;
	int CREATE_TEAM = 8;
	int LIST_AREA_TEAM = 9;
	int LIST_AREA_MEMBERS = 10;
	int ONLINE_LOAD_TEAM = 11;
	int APPLY_TEAM = 12;
	int REPLY = 13;
	@Deprecated
	int MATCH_TEAM = 14;
	@Deprecated
	int CANCLE_MATCH = 15;
	int READY_ENTER = 16;
	int REJECT_ENTER = 17;
	int START_DUNGEON = 18;
	int TEAM_ENTER_DUNGEON = 19;
	int CANCLE_ENTER_DUNGEON = 20;
	int CHECK_PLAYER_TEAM = 21;
	int PUSH_INVITE_MESSAGE = 100;
	int PUSH_PROCESS_INVITE_MESSAGE = 101;
	int PUSH_MEMBER_JOIN_MESSAGE  = 102;
	int PUSH_MEMBER_LEAVE_MESSAGE = 103;
	int PUSH_MEMBER_SWAP_MESSAGE = 104;
	int PUSH_MEMBER_ATTRIBUTE_CHANGES = 105;
	int PUSH_MEMBER_DISBAND_MESSAGE = 106;
	int PUSH_MEMBER_KICKED_MESSAGE = 107;
	int PUSH_MATCH_SUCCESS_MESSAGE = 108;
	@Deprecated
	int PUSH_CANCAL_MATCH_MESSAGE = 109;
	int PUSH_READY_MESSAGE = 110;
	int PUSH_TEAMMEMBERS_DUNGEON = 111;
	int PUSH_TEAMMEMBER_LOGIN = 112;
	int PUSH_TEAMMEMBER_LOGOUT = 113;
	int PUSH_MEMBER_CHANGE_SCREEN = 114;
}
