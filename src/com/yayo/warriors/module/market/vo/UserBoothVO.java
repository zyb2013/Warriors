package com.yayo.warriors.module.market.vo;

import java.io.Serializable;
import java.util.Collection;

import com.yayo.warriors.module.market.entity.MarketItem;
import com.yayo.warriors.module.market.model.UserBooth;

/**
 * 玩家摊位VO
 * 
 * @author huachaoping
 */
public class UserBoothVO implements Serializable {
	
	private static final long serialVersionUID = -8579922869493051655L;

	/** 摊位所属玩家Id */
	private long playerId;
	
	/** 玩家名字 */
	private String playerName;
	
	/** 摊位名字 */
	private String boothName;
	
	/** 摆摊物品列表 */
	private MarketItem[] itemList;

	/**
	 * 构造函数
	 * @param userBooth     玩家摊位信息
	 * @return
	 */
	public static UserBoothVO valueOf(UserBooth userBooth) {
		UserBoothVO vo = new UserBoothVO();
		vo.playerId    = userBooth.getPlayerId();
		vo.playerName  = userBooth.getName();
		vo.boothName   = userBooth.getBoothName();
		Collection<MarketItem> goodslist = userBooth.getGoodslist();
		vo.itemList    = goodslist.toArray(new MarketItem[goodslist.size()]);
		return vo;
	}
	

	public static UserBoothVO valueOf(long playerId, String playerName, String boothName) {
		UserBoothVO vo = new UserBoothVO();
		vo.playerId    = playerId;
		vo.playerName  = playerName;
		vo.boothName   = boothName;
		return vo;
	}
	
	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public String getBoothName() {
		return boothName;
	}

	public void setBoothName(String boothName) {
		this.boothName = boothName;
	}

	public MarketItem[] getItemList() {
		return itemList;
	}

	public void setItemList(MarketItem[] itemList) {
		this.itemList = itemList;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
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
		if (!(obj instanceof UserBoothVO))
			return false;
		UserBoothVO other = (UserBoothVO) obj;
		if (playerId != other.playerId)
			return false;
		return true;
	}
	
}
