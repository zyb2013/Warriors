package com.yayo.warriors.socket.handler.shop;

import com.yayo.warriors.module.shop.constant.ShopConstant;



public interface ShopCmd {
	int BUY_SHOP_PROPS = 1;
	int BUY_MALL_PROPS = 2;
	@Deprecated
	int LIST_NPC_PROPS = 3;
	@Deprecated
	int LIST_MALL_PROPS = 4;
	int LIST_MALL_SPECIALOFFER = 5;
	int BUY_MALL_OFFERS = 6;
}
