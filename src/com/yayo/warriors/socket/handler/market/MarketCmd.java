package com.yayo.warriors.socket.handler.market;

import com.yayo.warriors.module.market.entity.MarketItem;
import com.yayo.warriors.module.market.type.SearchType;
public interface MarketCmd {
	
	int ADD_MARKET_PROPS = 1;
	int ADD_MARKET_EQUIP = 2;
	int BUY_MARKET_ITEM = 3;
	int REMOVE_MARKET_ITEM = 4;
	int SEARCH_MARKET = 5;
	int LOAD_SELF_MARKET = 6;
	int LOAD_ALL_MARKET = 7;
	int LOAD_OTHER_MARKET = 8;
	int MODIFY_BOOTH_NAME = 9;
	int PUSH_MARKET_SELL_HISTORY = 100;
	
}
