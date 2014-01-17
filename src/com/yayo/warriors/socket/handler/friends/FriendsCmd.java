package com.yayo.warriors.socket.handler.friends;

import com.yayo.warriors.module.friends.type.FriendType;


public interface FriendsCmd {

	int LOAD_LIST = 1;
	int ADD_FRIENDLY = 2;
	int DEL_FRIENDLY = 3;
	int ADD_BLACK = 4;
	int DEL_BLACK = 5;
	int ADD_NEAREST = 6;
	int DEL_NEAREST = 7;
	int SEARCH_NAME = 8;
	int FRIENDS_BLESS = 10;
	int GET_BLESS_EXP = 11;
	int LOAD_BLESS_EXP = 12;
	int LOAD_BLESS_STATE = 13;
	int COLLECT_FRIENDS = 14;
	int FRIENDS_PRESENT = 15;
	int FRIENDS_GREET = 16;
	int FRIENDS_DRINKED = 17;
	int LOAD_FRIENDS_WINE = 18;
	int ADD_FRIENDS = 19;
	int BATCH_FRIENDS_BLESS = 20;
	int LOAD_OTHERS_WINE = 21;
	int LOAD_GREET_HISTORY = 22;
	int PUT_APPLET_FRIEND = 100;
	int PUT_PLUS_FRIENDLY_VALUE = 101;
	int UPDATE_PLUS_HATRED_VALUE = 102;
	int PUSH_FRIEND_LEVEL_UP = 103;
	int PUSH_FRIEND_BLESS = 104;
	int PUSH_FRIEND_ONLINE_STATE = 105;
	int PUSH_FRIEND_PRESENT_WINE = 106;
	int PUSH_FRIEND_LEVEL_CHANGE = 107;
	int PUSH_FRIENDS_MARKET_STATE = 108;
	
}
