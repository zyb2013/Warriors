package com.yayo.warriors.socket.handler.alliance;


import com.yayo.warriors.module.alliance.entity.PlayerAlliance;


public interface AllianceCmd {
	
	int LOAD_PLAYER_ALLIANCE = 1;
	int LOAD_ALLIANCE = 2;
	int CREATE_ALLIANCE_USE_PROPS = 3;
	int SUBLIST_ALLIANCE = 4;
	int SUBLIST_MEMBERS  = 5;
	int RELEASE_NOTICE = 6;
	int JOIN_ALLIANCE = 7;
	int DISMISS_MEMBER = 8;
	int GQUIT_ALLIANCE = 9;   
	int DISBAND_ALLIANCE = 10;
	int VILIDA_ALLIANCE = 11;
	int EXAMINE_APPLY   = 12; 
	int DEVOLVE_MASTER  = 13;
	int CONFIRM_DEVOLVE = 14;
	int SUBLIST_APPLY   = 15;
	int APPOINT_TITLE   = 16;
	int INVITE_MEMBER   = 17;
	int CONFIRM_INVITE   = 18;
	int SEARCH_PLAYER    = 19;
	int CREATE_ALLIANCE = 20;
	int LEVELUP_BUILD    = 21;
	int DONATE_PROPS     = 22;
	int DONATE_SILVER    = 23;
	int VIEW_RECORD      = 24;
    int SHOPPING_ALLIANCE         = 25;         
    int DIVINE_ALLIANCE           = 26;
    int RESEARCH_SKILL            = 27;
    int STUDY_SKILL               = 28;
	int SUBLIST_TODAY_DONATE  = 29;
	int PUSH_DISMISS_MEMBER = 100;
	int PUSH_DISBAND_ALLIANCE = 101;
	int PUSH_JOIN_ALLIANCE = 102;
	int PUSH_JOIN_SUCCESS = 103;
	int PUSH_DEVOLVE_REJECT = 104;
	int PUSH_DEVOLVE_ACCEPT = 105;
	int PUSH_INVITE_MEMBER  = 106;
	int PUSH_INVITE_REJECT  = 107;
	int PUSH_DEVOLVE_NOTICE = 108;
	int PUSH_RESEARCH_SKILL = 109;
	int PUSH_LEVELUP_BUILD  = 110;

}
