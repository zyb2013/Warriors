package com.yayo.warriors.basedb.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.active.rule.ActiveLevelType;
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.module.task.model.RewardVO;
import com.yayo.warriors.type.GoodsType;

/**
 * 运营活动等级活动配置 
 * @author liuyuhua
 */
@Resource
public class ActiveOperatorLevelConfig {
	
	/** 增量ID*/
	@Id
	private int id;
	
	/** 活动基础ID,参考{@link ActiveOperatorConfig#getId()}*/
	private int activeBaseId;
	
	/** 排名类型,参考{@link ActiveLevelType}*/
	private int type;
	
	/** 条件要求*/
	private int condition;
	
	/** 奖励对象*/
	private int rewardObject;
	
	/** 装备奖励. 格式: 物品ID_物品数量_强化星级_是否绑定(0-未绑定, 1-绑定)|... */
	private String equips;
	
	/** 任务物品. 格式: 物品ID_物品数量|... */
	private String items;
	
	/** 绑定元宝奖励*/
	private int coupon;
	
	/** 经验奖励*/
	private int exp;
	
	/** 游戏币奖励*/
	private int silver;
	
	@JsonIgnore
	private transient String rewardFlag = null;
	
	/** 任务奖励VO对象*/
	@JsonIgnore
	private transient List<RewardVO> rewardList = null;
	
	/** 日志需要记录的,格式{ID_数量}*/
	@JsonIgnore
	private transient String logEquips = null;;
	
	/** 日志需要记录的,格式{ID_数量}*/
	@JsonIgnore
	private transient String logItems = null; 
	
	/** 物品奖励的内容*/
	@JsonIgnore
	private transient List<GoodsVO> goodsVos = null;
	
	/**
	 * 客户端显示得到的物品
	 * @return
	 */
	public List<GoodsVO> getClientShowGoods(){
		if(goodsVos != null){
			return goodsVos;
		}
		
		List<RewardVO> rewardVos = getRewardList();
		synchronized (this) {
			if(goodsVos != null){
				return goodsVos;
			}
			
			goodsVos = new ArrayList<GoodsVO>(2);
			if(rewardVos == null || rewardVos.isEmpty()){
				return goodsVos;
			}
			for(RewardVO reward : rewardVos){
				if(reward.getType() == GoodsType.PROPS){
					int baseId = reward.getBaseId();
					int goodsType = reward.getType();
					int num = reward.getCount();
					goodsVos.add(GoodsVO.valueOf(baseId, goodsType, num));
				}
			}
		}
		
		return goodsVos;
	}
	
	
	/**
	 * 获取奖励道具的日志(用于日志记录)
	 * @return
	 */
	public String getLogItems(){
		if(logItems != null){
			return logItems;
		}
		
		List<RewardVO> rewardVos = getRewardList();
		synchronized (this) {
			if(logItems != null){
				return logItems;
			}
			
			logItems = "";
			if(rewardVos.isEmpty()){
				return logItems;
			}
			
			//格式化...
			StringBuilder builder = new StringBuilder();
			for(RewardVO rewardVO : rewardVos){
				if(rewardVO.getType() == GoodsType.PROPS){
					builder.append(rewardVO.getBaseId()).append(Splitable.ATTRIBUTE_SPLIT).append(rewardVO.getCount()).append(Splitable.ELEMENT_DELIMITER);
				}
			}
			
			if(builder.length() > 0) {
				builder.deleteCharAt(builder.length() - 1);
			}
			
			logItems = builder.toString();
			return logItems;
		}
	}
	
	
	/**
	 * 获取奖励装备的日志(用于日志记录)
	 * @return
	 */
	public String getLogEquips(){
		if(logEquips != null){
			return logEquips;
		}
		
		List<RewardVO> rewardVos = getRewardList();
		synchronized (this) {
			if(logEquips != null){
				return logEquips;
			}
			
			logEquips = "";
			if(rewardVos.isEmpty()){
				return logEquips;
			}
			
			//格式化...
			StringBuilder builder = new StringBuilder();
			for(RewardVO rewardVO : rewardVos){
				if(rewardVO.getType() == GoodsType.EQUIP){
					builder.append(rewardVO.getBaseId()).append(Splitable.ATTRIBUTE_SPLIT).append(rewardVO.getCount()).append(Splitable.ELEMENT_DELIMITER);
				}
			}
			
			if(builder.length() > 0) {
				builder.deleteCharAt(builder.length() - 1);
			}
			
			logEquips = builder.toString();
			return logEquips;
		}
	}
	
	/**
	 * 获得任务奖励列表
	 * @return 奖励VO信息
	 */
	public List<RewardVO> getRewardList() {
		if(this.rewardList != null) {
			return this.rewardList;
		}
		
		synchronized (this) {
			if(this.rewardList != null) {
				return this.rewardList;
			}
			this.rewardList = new ArrayList<RewardVO>();
			this.constructItemReward(rewardList);
			this.constructEquipReward(rewardList);
		}
		return this.rewardList;
	}
	
	/**
	 * 构建奖励对象
	 * 	
	 * @param type				物品的类型
	 * @param rewardList		奖励列表
	 * @param cacheMap			缓存集合
	 */
	private void constructItemReward(List<RewardVO> rewardList) {
		List<String[]> arrays = Tools.delimiterString2Array(this.items);
		if(arrays == null || arrays.isEmpty()) {
			return;
		}
		
		// {道具ID, [未绑定数量, 绑定数量] }
		Map<Integer, int[]> maps = new HashMap<Integer, int[]>(1);
		for (String[] element : arrays) {
			int baseId = Integer.valueOf(element[0]);
			int count = Integer.valueOf(element[1]);
			int[] cache = maps.get(baseId);
			if(cache == null) {
				cache = new int[2];
				maps.put(baseId, cache);
			}
			
			if(element.length < 2 || Integer.valueOf(element[2]) == 0) { //非绑定的
				cache[0] = cache[0] + count;
			} else { //绑定的
				cache[1] = cache[1] + count;
			}
		}
		
		for (Entry<Integer, int[]> entry : maps.entrySet()) {
			int itemId = entry.getKey();
			int[] itemCount = entry.getValue();
			if(itemCount[0] > 0) {
				this.rewardList.add(RewardVO.props(itemId, itemCount[0], false));
			}
			if(itemCount[1] > 0) {
				this.rewardList.add(RewardVO.props(itemId, itemCount[1], true));
			}
		}
	}
	
	/**
	 * 构建装备奖励对象
	 * 	
	 * @param type				物品的类型
	 * @param rewardList		奖励列表
	 * @param cacheMap			缓存集合
	 */
	private void constructEquipReward(List<RewardVO> rewardList) {
		List<String[]> arrays = Tools.delimiterString2Array(this.equips);
		if(arrays == null || arrays.isEmpty()) {
			return;
		}
		
		for (String[] element : arrays) {
			int baseId = Integer.valueOf(element[0]);
			int count = Integer.valueOf(element[1]);
			int starLevel = Integer.valueOf(element[2]);
			boolean binding = element.length > 3 && Integer.valueOf(element[3]) > 0;
			this.rewardList.add(RewardVO.equip(baseId, count, starLevel, binding));
		}
	}
	
	/**
	 * 奖励领取标识
	 * @return {@link String} 标识
	 */
	public String getRewardFlag(){
		if(rewardFlag != null){
			return rewardFlag;
		}
		
		synchronized (this) {
			if(rewardFlag != null){
				return rewardFlag;
			}
			
			rewardFlag = activeBaseId + Splitable.ATTRIBUTE_SPLIT + condition;
			return rewardFlag;
		}
	}
	
	//Getter and Setter...
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getActiveBaseId() {
		return activeBaseId;
	}

	public void setActiveBaseId(int activeBaseId) {
		this.activeBaseId = activeBaseId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCondition() {
		return condition;
	}

	public void setCondition(int condition) {
		this.condition = condition;
	}

	public int getRewardObject() {
		return rewardObject;
	}

	public void setRewardObject(int rewardObject) {
		this.rewardObject = rewardObject;
	}

	public String getItems() {
		return items;
	}

	public void setItems(String items) {
		this.items = items;
	}

	public int getCoupon() {
		return coupon;
	}

	public void setCoupon(int coupon) {
		this.coupon = coupon;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public int getSilver() {
		return silver;
	}

	public void setSilver(int silver) {
		this.silver = silver;
	}

	public String getEquips() {
		return equips;
	}

	public void setEquips(String equips) {
		this.equips = equips;
	}

	@Override
	public String toString() {
		return "ActiveOperatorLevelConfig [id=" + id + ", activeBaseId="
				+ activeBaseId + ", type=" + type + ", condition=" + condition
				+ ", rewardObject=" + rewardObject + ", equips=" + equips
				+ ", items=" + items + ", coupon=" + coupon + ", exp=" + exp
				+ ", silver=" + silver + "]";
	}

}
