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
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.module.task.model.RewardVO;
import com.yayo.warriors.type.GoodsType;

/**
 * 运营活动兑换类型配置 
 * @author liuyuhua
 */
@Resource
public class ActiveOperatorExChangeConfig {
	
	/** 增量ID*/
	@Id
	private int id;
	
	/** 活动ID*/
	private int activeBaseId;
	
	/** 需要物品*/
	private String needItems;
	
	/** 兑换上限*/
	private int limitTimes;
	
	/** 装备奖励*/
	private String equips;
	
	/** 道具奖励*/
	private String items;
	
	/** 绑定元宝奖励*/
	private int coupon;
	
	/** 经验奖励*/
	private int exp;
	
	/** 铜币奖励*/
	private int silver;

	/** 解析所需要的物品数量 {@link ActiveOperatorExChangeConfig#items}*/
	@JsonIgnore
	private transient Map<Integer,Integer> needItemMap = null;
	
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
	 * 兑换数量是否有限制
	 * @return true 无限制 false 有限制
	 */
	public boolean isUnLimit(){
		return limitTimes <= 0 ? true : false; 
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
	 * 获取所需物品的数量
	 * @return
	 */
	public Map<Integer,Integer> getNeedItemMap(){
		if(needItemMap != null){
			return needItemMap;
		}
		
		synchronized (this) {
			if(needItemMap != null){
				return needItemMap;
			}
			
			needItemMap = new HashMap<Integer, Integer>(2);
			if(needItems == null || needItems.isEmpty()){
				return needItemMap;
			}
			
			List<String[]> delimits = Tools.delimiterString2Array(needItems);
			for(String[] delimit : delimits){
				if(delimit.length < 2){
					continue;
				}
				int propsBaseId = Integer.parseInt(delimit[0]);
				int count = Integer.parseInt(delimit[1]);
				needItemMap.put(propsBaseId, count);
			}
			
			return needItemMap;
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

	public String getNeedItems() {
		return needItems;
	}

	public void setNeedItems(String needItems) {
		this.needItems = needItems;
	}

	public int getLimitTimes() {
		return limitTimes;
	}

	public void setLimitTimes(int limitTimes) {
		this.limitTimes = limitTimes;
	}

	public String getEquips() {
		return equips;
	}

	public void setEquips(String equips) {
		this.equips = equips;
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

	@Override
	public String toString() {
		return "ActiveOperatorExChangeConfig [id=" + id + ", activeBaseId="
				+ activeBaseId + ", needItems=" + needItems + ", limitTimes="
				+ limitTimes + ", equips=" + equips + ", items=" + items
				+ ", coupon=" + coupon + ", exp=" + exp + ", silver=" + silver
				+ "]";
	}
}
