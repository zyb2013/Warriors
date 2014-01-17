package com.yayo.warriors.socket.handler.gift;


public interface GiftCmd {

	int OBTAIN_ONLINE_GIFT_PROPS = 70;
	@Deprecated
	int OPEN_ONLINE_GIFT = 73;
	int LOAD_GIFT_STATE = 74;
	int RECEIVE_CDKEY_GIFT = 75;
	int LOAD_EFFECT_GIFT = 76;
	int RECEIVE_EFFECT_GIFT = 77;
	int SEND_GIFT = 100;
	int DEL_GIFT = 101;
	
}
