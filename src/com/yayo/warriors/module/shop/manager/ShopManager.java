package com.yayo.warriors.module.shop.manager;

import java.util.Collection;
import java.util.List;

import com.yayo.warriors.basedb.model.MallActiveConfig;
import com.yayo.warriors.basedb.model.MallConfig;
import com.yayo.warriors.basedb.model.MallSpecialConfig;
import com.yayo.warriors.basedb.model.ShopConfig;
import com.yayo.warriors.module.shop.entity.LaveMallProp;
import com.yayo.warriors.module.shop.entity.PlayerBuyLimit;

public interface ShopManager {
	MallConfig getMallConfig(int mallId);
	MallSpecialConfig getMallSpecialConfig(int mallId);
	Collection<ShopConfig> getAllShopConfig();
	List<MallActiveConfig> getMallActiveConfigs(int isOpen);
	List<MallSpecialConfig> getSpecialMallConfigs(int activeId);
	ShopConfig getShopConfig(int shopId);
	LaveMallProp getLaveMallProp(int mallId);
	long findNpcByEquipOrProps(int type, int propsId, int screenType);
	
	
	MallActiveConfig getEffectActiveConfig();
	
	PlayerBuyLimit getPlayerBuyLimit(long playerId);
}
