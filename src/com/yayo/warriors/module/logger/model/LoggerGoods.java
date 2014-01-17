package com.yayo.warriors.module.logger.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.yayo.common.utility.Splitable;
import com.yayo.warriors.module.logger.type.Orient;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.type.GoodsType;

/**
 * 物品收支情况
 * 
 * @author Hyint
 */
public class LoggerGoods {

	/** 收支情况. 0-收入, 1-支出*/
	private Orient orient;
	
	/** 物品的类型. 详细见: {@link GoodsType} */
	private int goodsType;

	/** 物品的自增ID. 收入则该值为0 */
	private long goodsId;
	
	/** 基础ID */
	private int baseId;
	
	/** 数量*/
	private int count;
	
	/** 是否自动购买, 0-手动购买, 1-自动购买 */
	private int auto = 0;
	
	/** 总金币价格*/
	private long totalGolden;
	
	/** 总礼金价格*/
	private long totalCoupon;

	/** 总游戏币价格*/
	private long totalSilver;

	/** 当前物品的详细信息 */
	private String info = null;
	
	public Orient getOrient() {
		return orient;
	}

	public int getGoodsType() {
		return goodsType;
	}

	public int getBaseId() {
		return baseId;
	}

	public int getCount() {
		return count;
	}
	
	public long getGoodsId() {
		return goodsId;
	}

	public int getAuto() {
		return auto;
	}

	public long getTotalGolden() {
		return totalGolden;
	}

	public long getTotalSilver() {
		return totalSilver;
	}

	public long getTotalCoupon() {
		return totalCoupon;
	}

	/**
	 * 花元宝得到装备
	 * 
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @param  price				物品单价
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods incomeEquipByGolden(int baseId, int count, long price) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.totalGolden = price;
		goodInfo.orient = Orient.INCOME;
		goodInfo.goodsType = GoodsType.EQUIP;
		return goodInfo;
	}

	/**
	 * 花礼金得到装备
	 * 
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @param  price				物品单价
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods incomeEquipByCoupon(int baseId, int count, long price) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.totalCoupon = price;
		goodInfo.orient = Orient.INCOME;
		goodInfo.goodsType = GoodsType.EQUIP;
		return goodInfo;
	}

	/**
	 * 花铜币得到装备
	 * 
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @param  price				物品单价
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods incomeEquipBySilver(int baseId, int count, int price) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.totalSilver = price;
		goodInfo.orient = Orient.INCOME;
		goodInfo.goodsType = GoodsType.EQUIP;
		return goodInfo;
	}

	/**
	 * 花铜币得到装备
	 * 
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods incomeEquip(int baseId, int count) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.orient = Orient.INCOME;
		goodInfo.goodsType = GoodsType.EQUIP;
		return goodInfo;
	}

	/**
	 * 得到装备
	 * 
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @param  price				物品单价
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods incomeEquipByMoney(int baseId, int count, long golden, long silver) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.totalSilver = silver;
		goodInfo.totalGolden = golden;
		goodInfo.orient = Orient.INCOME;
		goodInfo.goodsType = GoodsType.EQUIP;
		return goodInfo;
	}
	
	
	/**
	 * 支出装备
	 * 
	 * @param  goodsId				物品自增ID
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods outcomeEquip(long goodsId, int baseId, int count) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.auto = 0;
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.goodsId = goodsId;
		goodInfo.orient = Orient.OUTCOME;
		goodInfo.goodsType = GoodsType.EQUIP;
		return goodInfo;
	}
	
	/**
	 * 支出装备
	 * 
	 * @param  goodsId				物品自增ID
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods outcomeEquipBySilver(long goodsId, int baseId, int count, long silver) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.auto = 0;
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.goodsId = goodsId;
		goodInfo.totalSilver = silver;
		goodInfo.orient = Orient.OUTCOME;
		goodInfo.goodsType = GoodsType.EQUIP;
		return goodInfo;
	}

	/**
	 * 自动购买支出装备
	 * 
	 * @param  goodsId				物品自增ID
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods outcomeEquipAutoBuySilver(long goodsId, int baseId, int count, long silver) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.auto = 1;
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.goodsId = goodsId;
		goodInfo.totalSilver = silver;
		goodInfo.orient = Orient.OUTCOME;
		goodInfo.goodsType = GoodsType.EQUIP;
		return goodInfo;
	}

	/**
	 * 支出装备
	 * 
	 * @param  goodsId				物品自增ID
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods outcomeEquipByGolden(long goodsId, int baseId, int count, long golden) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.auto = 0;
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.goodsId = goodsId;
		goodInfo.totalGolden = golden;
		goodInfo.orient = Orient.OUTCOME;
		goodInfo.goodsType = GoodsType.EQUIP;
		return goodInfo;
	}

	/**
	 * 支出装备
	 * 
	 * @param  goodsId				物品自增ID
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods outcomeEquipAutoBuyGolden(long goodsId, int baseId, int count, long golden) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.auto = 1;
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.goodsId = goodsId;
		goodInfo.totalGolden = golden;
		goodInfo.orient = Orient.OUTCOME;
		goodInfo.goodsType = GoodsType.EQUIP;
		return goodInfo;
	}
	
	/**
	 * 支出装备通过货币
	 * 
	 * @param  goodsId				物品自增ID
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods outcomeEquipByMoney(long goodsId, int baseId, int count, long golden, long silver) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.auto = 0;
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.goodsId = goodsId;
		goodInfo.totalSilver = silver;
		goodInfo.totalGolden = golden;
		goodInfo.orient = Orient.OUTCOME;
		goodInfo.goodsType = GoodsType.EQUIP;
		return goodInfo;
	}
	/**
	 * 支出装备通过货币
	 * 
	 * @param  goodsId				物品自增ID
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods outcomeEquipAutoBuyMoney(long goodsId, int baseId, int count, long golden, long silver) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.auto = 1;
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.goodsId = goodsId;
		goodInfo.totalSilver = silver;
		goodInfo.totalGolden = golden;
		goodInfo.orient = Orient.OUTCOME;
		goodInfo.goodsType = GoodsType.EQUIP;
		return goodInfo;
	}

	/**
	 * 得到道具
	 * 
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods incomeProps(int baseId, int count) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.orient = Orient.INCOME;
		goodInfo.goodsType = GoodsType.PROPS;
		return goodInfo;
	}

	/**
	 * 得到道具
	 * 
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods incomePropsBySilver(int baseId, int count, long silver) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.totalSilver = silver;
		goodInfo.orient = Orient.INCOME;
		goodInfo.goodsType = GoodsType.PROPS;
		return goodInfo;
	}

	/**
	 * 得到道具
	 * 
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods incomePropsByGolden(int baseId, int count, long golden) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.totalGolden = golden;
		goodInfo.orient = Orient.INCOME;
		goodInfo.goodsType = GoodsType.PROPS;
		return goodInfo;
	}
	
	/**
	 * 得到道具
	 * 
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @param  coupon				礼金价格
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods incomePropsByCoupon(int baseId, int count, long coupon) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.totalCoupon = coupon;
		goodInfo.orient = Orient.INCOME;
		goodInfo.goodsType = GoodsType.PROPS;
		return goodInfo;
	}
	
	/**
	 * 得到道具
	 * 
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods incomePropsByMoney(int baseId, int count, long golden, long silver) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.totalSilver = silver;
		goodInfo.totalGolden = golden;
		goodInfo.orient = Orient.INCOME;
		goodInfo.goodsType = GoodsType.PROPS;
		return goodInfo;
	}
	
	/**
	 * 支出道具
	 * 
	 * @param  goodsId				物品自增ID
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods outcomeProps(long goodsId, int baseId, int count) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.goodsId = goodsId;
		goodInfo.orient = Orient.OUTCOME;
		goodInfo.goodsType = GoodsType.PROPS;
		return goodInfo;
	}
	
	/**
	 * 支出道具
	 * 
	 * @param  goodsId				物品自增ID
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods outcomePropsByMoney(long goodsId, int baseId, int count, long golden, long silver) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.goodsId = goodsId;
		goodInfo.totalGolden = golden;
		goodInfo.totalSilver = silver;
		goodInfo.orient = Orient.OUTCOME;
		goodInfo.goodsType = GoodsType.PROPS;
		return goodInfo;
	}
	
	/**
	 * 支出道具
	 * 
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods outcomeProps(int baseId, int count) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.orient = Orient.OUTCOME;
		goodInfo.goodsType = GoodsType.PROPS;
		return goodInfo;
	}

	/**
	 * 自动购买道具支出
	 * 
	 * @param  goodsId				物品自增ID
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods outcomePropsAutoBuyGolden(int baseId, int count, long golden) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.auto = 1; 
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.totalGolden = golden;
		goodInfo.orient = Orient.OUTCOME;
		goodInfo.goodsType = GoodsType.PROPS;
		return goodInfo;
	}

	/**
	 * 自动购买道具支出
	 * 
	 * @param  goodsId				物品自增ID
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods outcomePropsAutoBuyMoney(int baseId, int count, long golden, long silver) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.auto = 1; 
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.totalGolden = golden;
		goodInfo.totalSilver = silver;
		goodInfo.orient = Orient.OUTCOME;
		goodInfo.goodsType = GoodsType.PROPS;
		return goodInfo;
	}

	/**
	 * 自动购买道具支出
	 * 
	 * @param  goodsId				物品自增ID
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods outcomePropsAutoBuySilver(int baseId, int count, long silver) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.auto = 1; 
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.totalSilver = silver;
		goodInfo.orient = Orient.OUTCOME;
		goodInfo.goodsType = GoodsType.PROPS;
		return goodInfo;
	}
	
	/**
	 * 装备收支... 
	 * 
	 * @param orient
	 * @param userEquips
	 * @return
	 */
	public static List<LoggerGoods> loggerEquip(Orient orient, Collection<UserEquip> userEquips){
		List<LoggerGoods> list = new ArrayList<LoggerGoods>();
		if(userEquips != null){
			for(UserEquip userEquip : userEquips){
				LoggerGoods goodInfo = new LoggerGoods();
				goodInfo.baseId = userEquip.getBaseId();
				goodInfo.count = 1;
				goodInfo.goodsId = userEquip.getId();
				goodInfo.goodsType = GoodsType.EQUIP;
				goodInfo.orient = orient;
				list.add(goodInfo);
			}
		}
		return list;
	}
	
	
	//-----------------------------------------------------------
	
	
	
	
	
	
	

	/**
	 * 道具收入
	 * @param orient
	 * @param userEquips
	 * @return
	 */
	public static List<LoggerGoods> incomeProps(Collection<UserProps> userPropsList){
		List<LoggerGoods> list = new ArrayList<LoggerGoods>();
		if(userPropsList != null){
			for(UserProps userProps : userPropsList){
				LoggerGoods goodInfo = new LoggerGoods();
				goodInfo.orient = Orient.INCOME;
				goodInfo.baseId = userProps.getBaseId();
				goodInfo.count = userProps.getCount(); 
				goodInfo.goodsId = userProps.getId();
				goodInfo.goodsType = GoodsType.PROPS;
				list.add(goodInfo);
			}
		}
		return list;
	}
	

	/**
	 * 物品收支
	 * @param orient
	 * @param userEquips
	 * @return
	 */
	public static List<LoggerGoods> loggerProps(Orient orient, Map<Long, Integer> updateMap, Collection<UserProps> userPropsList){
		List<LoggerGoods> list = new ArrayList<LoggerGoods>();
		if(userPropsList != null){
			for(UserProps userProps : userPropsList){
				LoggerGoods goodInfo = new LoggerGoods();
				goodInfo.baseId = userProps.getBaseId();
				goodInfo.count = updateMap.containsKey( userProps.getId() )? updateMap.get(userProps.getId()) : 0;
				goodInfo.goodsId = userProps.getId();
				goodInfo.goodsType = GoodsType.PROPS;
				goodInfo.orient = orient;
				list.add(goodInfo);
			}
		}
		return list;
	}
	
	/**
	 * 物品收支
	 * @param orient
	 * @param userEquips
	 * @return
	 */
	public static List<LoggerGoods> updateProps(Map<Long, Integer> updateMap, Collection<UserProps> userPropsList){
		List<LoggerGoods> list = new ArrayList<LoggerGoods>();
		if(userPropsList != null){
			for(UserProps userProps : userPropsList){
				LoggerGoods goodInfo = new LoggerGoods();
				Integer count = updateMap.get(userProps.getId());
				if(count == null){
					continue;
				}
				goodInfo.baseId = userProps.getBaseId();
				goodInfo.count = Math.abs(count);
				goodInfo.goodsId = userProps.getId();
				goodInfo.goodsType = GoodsType.PROPS;
				goodInfo.orient = count > 0 ? Orient.INCOME : Orient.OUTCOME;
				list.add(goodInfo);
			}
		}
		return list;
	}
	
	/**
	 * 变更道具
	 * 
	 * @param  orient				收支情况
	 * @param  goodsType			物品类型
	 * @param  baseId				基础装备ID
	 * @param  count				得到的数量
	 * @return {@link LoggerGoods}	物品得失信息
	 */
	public static LoggerGoods changedGoods(Orient orient, int goodsType, int baseId, int count) {
		LoggerGoods goodInfo = new LoggerGoods();
		goodInfo.count = count;
		goodInfo.baseId = baseId;
		goodInfo.orient = orient;
		goodInfo.goodsType = goodsType;
		return goodInfo;
	}
	
	

	@Override
	public String toString() {
		if(this.info == null) {
			this.info = new StringBuilder().append(orient.getCode()).append(Splitable.ATTRIBUTE_SPLIT)
										   .append(goodsType).append(Splitable.ATTRIBUTE_SPLIT)
										   .append(goodsId).append(Splitable.ATTRIBUTE_SPLIT)
										   .append(baseId).append(Splitable.ATTRIBUTE_SPLIT)
										   .append(count).append(Splitable.ATTRIBUTE_SPLIT)
										   .append(auto).append(Splitable.ATTRIBUTE_SPLIT)
										   .append(totalGolden).append(Splitable.ATTRIBUTE_SPLIT)
										   .append(totalSilver).append(Splitable.ELEMENT_DELIMITER).toString();
		}
		return this.info;
	}
}
