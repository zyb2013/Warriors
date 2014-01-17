package com.yayo.warriors.socket.handler.props;

import java.util.Map;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.constant.PropsConstant;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.model.UserEquipVO;
import com.yayo.warriors.type.GoodsType;


public interface PropsCmd {
	
	int GET_PACKAGE_INFO = 1;
	int UPDATE_PACKAGE_INFO = 2;
	int CHECK_ROLE_PACKINFO = 4;
	int GET_USER_PROPS = 5;
	int USE_USERPROPS = 6;
	int DROP_USERPROPS = 7;
	int SELL_USERPROPS = 8;
	int MERGE_USERPROPS = 9;
	int SPLITE_USERPROPS = 10;
	int SYNTH_EXCHANGE_STONE = 11;
	int SYNTH_SHARIPU_ITEM = 13;
	int ASCENT_EQUIP_STARLEVEL = 14;
	int ASCENT_EQUIP_RANKLEVEL = 15;
	int EMENDATION_EQUIP_ATTRIBUTE = 16;
	int POLISHED_EQUIP_ATTRIBUTE = 17;
	int RECAST_EQUIP_ATTRIBUTE = 18;
	int ENCHANGE_EQUIP_STONE = 19;
	int REMOVE_EQUIP_STONE = 20;
	int SELECT_WASH_EQUIP_ATTRIBUTE = 21;
	int REFINING_EQUIP_ATTRIBUTE = 22; 
	int ARTIFICE_PROPS = 23;
	int RESOLVE_USER_EQUIP = 24;
	int CALC_REPARE_COSTSILVER = 25;
	int REPARE_USER_EQUIP = 26;
	int EQUIP_STARLEVEL_EXTENDS = 27;
	int UPDATE_ENTRY_POSITION = 30;
	int AUTO_ADJUST_BACKPACK = 31;
	int ROLE_DRESS_EQUIP = 50;
	int QUERY_USER_EQUIP = 51;
	int LIST_USER_EQUIP = 52;
	int LIST_USER_PROPS = 53;
	int QUERY_USER_EQUIP_ARRAY = 54;
	int PUT_2_STORAGE = 60;
	int CHECKOUT_FROM_STORAGE = 61;
	int SWAP_GOODS_BACKPACK = 62;
	int EXPAND_BACKPACK = 63;
	int CHECKOUT_PROPS_FROM_STORAGE = 64;
	int USE_PORTABLE_BAG = 71;
	
	int BATCH_BUY_AND_USE_PROPS = 72;
	int SHENWU_TEMPO_FORGE = 80;
	int SHENWU_ATTRIBUTE_FORGE = 81;
	int PUSH_DRESS_ATTRCHANGE = 100;
	int PUSH_EQUIP_DAMAGE_INFO = 101;
	int PUSH_GOODS_CHANGE = 102;
}
