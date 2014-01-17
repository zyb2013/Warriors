package com.yayo.warriors.module.dungeon.types;

import com.yayo.warriors.module.pack.type.BackpackType;

/**
 * 剧情副本验证类型
 * @author liuyuhua
 */
public interface StoryVerifType {
	
	/**
	 * 等级类型
	 * <per>类型_等级_忽略</per>
	 */
	int LEVEL_TYPE = 0;
	
	/**
	 * 前置副本类型
	 * <per>类型_副本ID_忽略</per>
	 */
	int PRE_DUNGEON_TYPE = 1;
	
	/** VIP类型
	 * <per>类型_VIP等级_忽略</per>
	 * */
	int VIP_TYPE = 2;
	
	/** 道具类型(验证背包中的道具)
	 * <per>类型_指定道具_数量</per>
	 * */
	int PROPS_TYPE = 3;
	
	/** 家将类型
	 * <per>类型_指定家将(Id)_忽略</per>
	 * */
	int PET_TYPE   = 4;
	
	/** 家将品质类型
	 * <per>类型_是否指定家将(0指定,0<制定)_品质</per>
	 * */
	int PET_QUALITY_TYPE = 5; 
	
	/** 坐骑类型
	 * <per>类型_等级_忽略</per>
	 * */
	int HORSE_TYPE  = 6;
	
	/** 装备类型
	 * <per>类型_指定装备_位置(详细参考{@link BackpackType})</per>
	 * */
	int EQUIP_TYPE = 7;
	
	/** 装备强化类型(已经穿着装备)
	 *  <per>类型_(任意装备)星数_数量</per>
	 * */
	int EQUIP_STRENG_TYPE = 8;
	
	/**
	 * 装备品质类型(已经穿着装备)
	 * <per>类型_(任意装备)品质_数量</per>
	 */
	int EQUIP_QUALITY_TYPE = 9;
	
	/**
	 * 装备孔数(已经穿着装备)
	 * <per>类型_宝石的品质_数量</per>
	 */
	int EQUIP_HOLE_TYPE = 10;
	
}
