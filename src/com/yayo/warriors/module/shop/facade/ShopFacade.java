package com.yayo.warriors.module.shop.facade;

import java.util.List;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.shop.model.MallProps;

public interface ShopFacade {
	ResultObject<List<BackpackEntry>> buyPropsByShop(long playerId, int shopId, int count, int npcId);
	
	ResultObject<List<BackpackEntry>> buyPropsByMall(long playerId, int mallId, int count);
	
	ResultObject<List<BackpackEntry>> buySpecialMallProps(long playerId, int mallId, int count);
	List<MallProps> listMallSpecialOffer(long playerId);
}
