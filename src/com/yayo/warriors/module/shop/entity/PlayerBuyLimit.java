package com.yayo.warriors.module.shop.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.Tools;

@Entity
@Table(name="playerBuyLimit")
public class PlayerBuyLimit extends BaseModel<Long> {

	private static final long serialVersionUID = 7424551290201072318L;

	@Id
	@Column(name="playerId")
	private Long id;
	
	@Lob
	private String goodsBuyCount = "";
	
	@Transient
	private transient Map<Integer, Integer> buyCountMap = null;
	
	
	public static PlayerBuyLimit valueOf(long playerId) {
		PlayerBuyLimit limit = new PlayerBuyLimit();
		limit.id = playerId;
		return limit;
	}
	
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long playerId) {
		this.id = playerId;
	}

	public String getGoodsBuyCount() {
		return goodsBuyCount;
	}

	public void setGoodsBuyCount(String goodsBuyCount) {
		this.goodsBuyCount = goodsBuyCount;
	}
	
	
	public Map<Integer, Integer> getMallBuyCountMap() {
		if (this.buyCountMap != null) {
			return this.buyCountMap;
		}
		
		synchronized (this) {
			if (this.buyCountMap != null) {
				return this.buyCountMap;
			}
			
			this.buyCountMap = new HashMap<Integer, Integer>();
			if (StringUtils.isBlank(this.goodsBuyCount)) {
				return this.buyCountMap;
			}
			List<String[]> arrays = Tools.delimiterString2Array(this.goodsBuyCount);
			for (String[] element : arrays) {
				this.buyCountMap.put(Integer.valueOf(element[0]), Integer.valueOf(element[1]));
			}
		}
		return this.buyCountMap;
	}
	
	
	public void updatePlayerGoodsCount() {
		Map<Integer, Integer> map = this.getMallBuyCountMap();
		List<String[]> subArray = new ArrayList<String[]>();
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			String key = String.valueOf(entry.getKey());
			String value = String.valueOf(entry.getValue());
			subArray.add(new String[] {key, value});
		}
		this.goodsBuyCount = Tools.listArray2DelimiterString(subArray);
	}
	
	
	public int getCountById(int baseId) {
		Map<Integer, Integer> map = this.getMallBuyCountMap();
		return map.get(baseId) == null ? 0 : map.get(baseId);
	}
	
	
	public void put2BuyCountMap(int baseId, int count) {
		Map<Integer, Integer> map = this.getMallBuyCountMap();
		Integer buyCount = map.get(baseId);
		if (buyCount == null) {
			buyCount = Integer.valueOf(0);
		}
		map.put(baseId, buyCount + count);
	}

}
