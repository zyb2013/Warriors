package com.yayo.warriors.module.market.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.yayo.warriors.module.market.entity.MarketItem;

/**
 * 摊位
 * @author liuyuhua
 */
public class UserBooth implements Serializable{
	private static final long serialVersionUID = -1095723945803321074L;
	
	/** 玩家的ID*/
	private long playerId;
	
	/** 玩家名字*/
	private String name;
	
	/** 摆摊名字 */
	private String boothName;
	
	/** 商品集合*/
	private Map<Long, MarketItem> goodsMap;
	
	/**
	 * 构造函数
	 * @param playerId    玩家的ID
	 * @param name        摊位名字
	 * @return
	 */
	public static UserBooth valueOf(Long playerId, String name, Map<Long, MarketItem> goodsMap){
		UserBooth booth = new UserBooth();
		booth.name = name;
		booth.playerId = playerId;
		booth.boothName = name + "的摊位";
		booth.goodsMap = goodsMap;
		if(goodsMap == null) {
			booth.goodsMap = new HashMap<Long, MarketItem>();
		}
		return booth;
	}
	
	/**
	 * 添加需要售卖的商品
	 * @param goods
	 */
	public void addGoods(MarketItem item){
		this.goodsMap.put(item.getId(), item);
	}
	
	/**
	 * 更具Id来获取商品
	 * @param itemId   商品ID
	 * @return
	 */
	public MarketItem getItemById(long itemId){
		return this.goodsMap.get(itemId);
	}
	
	/**
	 * 摆摊物品是否为空
	 * @return
	 */
	public boolean isEmpty() {
		return goodsMap.isEmpty();
	}
	
	//Getter and Setter....

	public Long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}

	public Collection<MarketItem> getGoodslist() {
		return goodsMap.values();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBoothName() {
		return boothName;
	}

	public void setBoothName(String boothName) {
		this.boothName = boothName;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (playerId ^ (playerId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof UserBooth))
			return false;
		UserBooth other = (UserBooth) obj;
		if (playerId != other.playerId)
			return false;
		return true;
	}

	/**
	 * 是否有该交易物品
	 * 
	 * @param  marketItemId
	 * @return
	 */
	public boolean contains(long marketItemId) {
		return goodsMap.containsKey(marketItemId);
	}
	
	public synchronized void removeMarketItem(long marketItemId) {
		goodsMap.remove(marketItemId);
	}
	
	public synchronized int size() {
		return goodsMap.size();
	}

}
