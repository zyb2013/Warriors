package com.yayo.warriors.module.props.rule;

import static com.yayo.warriors.type.FormulaKey.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.TimeConstant;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.model.EquipStarConfig;
import com.yayo.warriors.common.helper.FormulaHelper;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.type.Quality;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.type.FormulaKey;

/**
 * 道具的规则类
 * 
 * @author Hyint
 */
public class PropsRule {
	
	/** 装备默认的神武ID */
	public static final int DEFAULT_SHENWU_ID = 1;
	/** 合成一个宝石需要的宝石数量 */
	public static final int SYNTH_STONE_COUNT = 3;
	/** 合成失败, 需要扣除的道具数量 */
	public static final int SYNTH_FAILURE_COUNT = 2;

	/** 合成技能书, 自动购买幸运道具和传输过来的道具总量 */
	public static final int MAX_LUCKY_ITEM_COUNT = 3;
	/** 装备升星最多可以使用的幸运晶数量 */
	public static final int ASCENT_EQUIP_STARLEVEL = 3;
	///** 装备最大的星级 */
	//public static final int MAX_EQUIP_STAR_LEVEL = 11;

	/** 装备升阶可以使用最多的保星石 */
	public static final int RANK_SAFE_STAR_COUNT = 1;
	/** 装备升阶可以使用最多的保魂石 */
	public static final int RANK_SAFE_ATTR_COUNT = 1;
	/** 装备可以进行精炼的附加属性条数 */
	public static final int REFINE_EQUIP_ADDITION_COUNT = 6;
	/** 角色死亡需要扣除的装备耐久度 */
	public static final int DEAD_DAMAGE_EQUIP_VALUE = 5;
	
	/** 开启家将槽所需要的扩展符基础道具ID*/
	public static final int OPEN_PET_SOLT_PROPS_BASE_ID = 120003;
	/** 洗练锁道具*/
	public static final int LOCK_PROPS_ID = 30016;
	/** 装备闪光的件数*/
	public static final int EQUIP_BLINK_COUNT = 13;
	/** 继承石ID */
	public static final int EXTEND_LEVEL_PROPSID = 120050;
	private static final Logger LOGGER = LoggerFactory.getLogger(PropsRule.class);
	
	/**
	 * 获得装备升星购买花费的金币
	 * 
	 * @param  autoBuyCount		自动购买的数量
	 * @return {@link Integer}	消耗的金币
	 */
	public static int getAscentStarBuyCostGold(int autoBuyCount) {
		return autoBuyCount * 5;
	}
	
	/**
	 * 获得合成舍利子需要的聚灵石数量
	 * 
	 * @param  bsLevel			当前道具的等级
	 * @return {@link Integer}	需要聚灵石的数量
	 */
	public static int getSynthSharipuNeedSpiritCount(int bsLevel) {
		return bsLevel + 1 <= 4 ? 1 : 2;
	}
	
	/**
	 * 检测合成舍利子, 是否成功
	 * 
	 * @param  luckyCount		幸运石的数量
	 * @param  bsLevel			宝石的等级
	 * @return {@link Boolean}	返回是否成功
	 */
	public static boolean isSynthSharipuSuccess(int luckyCount, int bsLevel) {
		int rate = 0;
		switch (bsLevel) {
			case 1:		rate = 600;		break;	//当前是1级, 合成2级
			case 2:		rate = 500;		break;	//当前是2级, 合成3级
			case 3:		rate = 400;		break;	//当前是3级, 合成4级
			case 4:		rate = 300;		break;	//当前是4级, 合成5级
			case 5:		rate = 100;		break;	//当前是5级, 合成6级
			case 6:		rate = 50;		break;	//当前是6级, 合成7级
			case 7:		rate = 50;		break;	//当前是7级, 合成8级
		}
		return Tools.getRandomInteger(AttributeKeys.RATE_BASE) < rate + (bsLevel * 50);
	}
	
 	/**
	 * 升阶自动购买升阶石的数据
	 * 
	 * @param  autoBuyCount			自动购买的数量
	 * @return {@link Integer}		购买消耗的金币
	 */
	public static int calcAscentRankAutoBuyCostGold(int autoBuyCount) {
		return 10 * autoBuyCount;
	}
	
	/**
	 * 护身符升阶, 自动购买聚灵珠, 需要元宝
	 * 
	 * @param  autoBuyCount			自动购买的数量
	 * @return {@link Integer}		购买消耗的金币
	 */
	public static int calcAscentAmuletRankAutoBuyCostGold(int autoBuyCount) {
		return 10 * autoBuyCount;
	}
	
	/**
	 * 是否升阶成功
	 * 
	 * @param  rankLevel			装备的阶级
	 * @return
	 */
	public static boolean isAscentRankSuccess(int rankLevel) {
		return true;
	}

	/**
	 * 护身符升阶是否升阶成功
	 * 
	 * @param  rankLevel			装备的阶级
	 * @return
	 */
	public static boolean isAscentAmuletRankSuccess(int rankLevel, int luckyCount) {
		int rate = 0;
		switch (rankLevel) {
			case 1:		rate = 600;		break;
			case 2:		rate = 500;		break;
			case 3:		rate = 400;		break;
			case 4:		rate = 300;		break;
			case 5:		rate = 100;		break;
			case 6:		rate = 50;		break;
			case 7:		rate = 50;		break;
		}
		return Tools.getRandomInteger(AttributeKeys.RATE_BASE) < rate + luckyCount * 5;
	}
	
	/**
	 * 获得合成舍利子需要的银币数量
	 * 
	 * @param  bsLevel			道具的等级
	 * @return {@link Integer}	需要扣除的货币
	 */
	public static int getSynthSharipuCostSilver(int bsLevel) {
		return bsLevel * 1000;
	}
	
	/**
	 * 获得合成宝石需要的真气值
	 * 
	 * @param  synthCount		合成的宝石个数
	 * @return {@link Integer}	需要的真气值
	 */
	public static int getSynthStoneNeedGas(int synthCount) {
		return 1;
	}
	
	/**
	 * 获得护身符装备升阶需要消耗的聚灵珠和游戏币数量
	 * 
	 * @param  rankLevel		装备的阶级
	 * @return int[]			{ 消耗的升阶石数量, 消耗的游戏币数量}
	 */
	public static int[] getAscentAmuletNeedRankItemCount(int rankLevel) {
		switch (rankLevel) {
			case 1:		return new int[] { 1000 , 1 };
			case 2:		return new int[] { 2000 , 2 };
			case 3:		return new int[] { 3000 , 3 };
			case 4:		return new int[] { 4000 , 4 };
			default:	return null;
		}
	}
	
	/**
	 * 获得合成技能书需要的真气值
	 * 
	 * @param  synthCount		合成的技能书个数
	 * @return {@link Integer}	需要的真气值
	 */
	public static int getSynthSkillBookNeedGas(int synthCount) {
		return 1;
	}

	/**
	 * 合成技能书, 自动购买幸运石需要的元宝
	 * 
	 * @param  autoBuyCount		自动购买的数量
	 * @return {@link Integer}	需要的元宝值
	 */
	public static int getSynthSkillBooKNeedGold(int autoBuyCount) {
		return 10 * autoBuyCount;
	}
	
	/**
	 * 宝石合成需要扣除原料的数量
	 * 
	 * @param  synthSuccess 	合成是否成功
	 * @return {@link Integer}	返回合成道具需要扣除的数量
	 */
	public static int getSynthStoneCost(boolean synthSuccess) {
		return synthSuccess ? SYNTH_STONE_COUNT : SYNTH_FAILURE_COUNT;
	}
	
	/**
	 * 计算物品的过期时间
	 * 
	 * @param  vilidTime		有效时间
	 * @return
	 */
	public static Date calcPropsExpirateTime(int vilidTime) {
		Date expirateTime = null;
		if(vilidTime > 0) {
			expirateTime = new Date(System.currentTimeMillis() + vilidTime * TimeConstant.ONE_MINUTE_MILLISECOND);
		}
		return expirateTime;
	}
	
	/**
	 * 构建洗练保护下标为Set
	 * 
	 * @param  playerId				角色ID
	 * @param  safeIndex			保护下标字符串. 保护下标1_保护下标2_...
	 * @return {@link Set}			下标列表
	 */
	public static Set<Integer> splitSafeIndex2Set(long playerId, String safeIndex) {
		Set<Integer> indexSet = new HashSet<Integer>();
		if(StringUtils.isBlank(safeIndex)) {
			return indexSet;
		}
		
		String[] array = safeIndex.split(Splitable.ATTRIBUTE_SPLIT);
		for (String element : array) {
			try {
				Integer index = Integer.valueOf(element);
				if(index != null && index > 0 && index <= 8) {
					indexSet.add(index);
				}
			} catch (Exception e) {
				LOGGER.error("角色:[{}] 洗练装备保护下标: {} 解析异常.", playerId,  element);
				LOGGER.error("{}", e);
			}
		}
		return indexSet;
	}
	
	/**
	 * 获得装备精炼需要消耗的游戏币
	 * 
	 * @param  equipLevel		装备的等级
	 * @return {@link Integer}	需要消耗的游戏币
	 */
	public static int getRefineEquipCostSilver(int equipLevel) {
		return 1000 + equipLevel * 1000;
	}
	
	/**
	 * 扩展背包所需道具数量
	 * 
	 * @param page              当前背包最大页数
	 * @param backpack          背包号
	 * @return {@link Integer}  需要的道具数量
	 */
	public static int getExpandBackpackItemCount(int page, int backpack) {
		switch(backpack) {
			case BackpackType.DEFAULT_BACKPACK: return FormulaHelper.invoke(BACKPACK_OPEN_FORMULA, page).intValue();
			case BackpackType.STORAGE_BACKPACK: return FormulaHelper.invoke(STORAGE_OPEN_FORMULA, page).intValue();
		}
		return -1;
	}
	
	/** 
	 * 扩展背包自动购买道具所需金币
	 * 
	 * @param autoBuyCount      自动购买数量
	 * @return {@link Integer}  扣除的金币
	 */
	public static int getExpandBackAutoBuyCostGold(int autoBuyCount) {
		return autoBuyCount * 30;
	}
	
	/**
	 * 获得继承星级需要消耗的游戏币
	 * 
	 * @param  price			装备的价格
	 * @param  maxStar			最大星级		
	 * @return {@link Integer}	需要消耗的游戏币
	 */
	public static int getExtendStarCost(int price, int maxStar) {
		int totalPrice = 0;
		int formulaId = FormulaKey.EQUIP_EXTENDS_LEVEL_FORMULA;
		for (int star = 0; star < maxStar; star++) {
			EquipStarConfig equipStar = EquipHelper.getEquipStarConfig(star);
			if(equipStar != null) {
				totalPrice += FormulaHelper.invoke(formulaId, equipStar.getRate(), price, equipStar.getSilver()).intValue();
			}
		}
		return totalPrice;
	}
	
	/**
	 * 获得装备洗练道具ID
	 * 
	 * @param  quality			装备的品阶
	 * @return {@link Integer}	道具ID
	 */
	public static int getPolishEquipItemId(Quality quality) {
		switch (quality) {
			case GREEN:		return 30012;
			case BLUE:		return 30019;
			case PURPLE:	return 30020;
			case ORANGE:	return 30021;
		}
		return -1;
	}
}
