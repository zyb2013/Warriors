package com.yayo.warriors.module.market.entity;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import com.yayo.common.db.model.BaseModel;
import com.yayo.warriors.module.market.type.ItemType;
import com.yayo.warriors.module.market.type.MarketState;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;

/**
 * 摊位售卖的商品
 * @author liuyuhua
 */
@Entity
@Table(name="marketItem")
public class MarketItem extends BaseModel<Long> {
	private static final long serialVersionUID = -91359591972898709L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	/** 玩家商品的唯一标识ID*/
	private long goodsId;
	
	/** 商品所属玩家ID*/
	private long playerId;
	
	/** 商品类型*/
	@Enumerated
	private ItemType type;
	
	/** 原型基础数据类型*/
	private int baseId;
	
	/** 售卖的银两*/
	private long sellSilver;
	
	/** 售卖的元宝*/
	private long sellGolden;
	
	/** 道具售卖数量 */
	private int sellCount;
	
	@Enumerated
	private MarketState state = MarketState.NORMAL;
	
	public static MarketItem equip2MarketItem(long playerId, UserEquip userEquip, long sellSilver, long sellGolden) {
		MarketItem goods = new MarketItem();
		goods.playerId = playerId;
		goods.type = ItemType.EQUIP;
		goods.sellGolden = sellGolden;
		goods.sellSilver = sellSilver;
		goods.goodsId = userEquip.getId();
		goods.baseId = userEquip.getBaseId();
		goods.sellCount = userEquip.getCount();
		return goods;
	}

	public static MarketItem props2MarketItem(long playerId, UserProps props, long sellSilver, long sellGolden) {
		MarketItem goods = new MarketItem();
		goods.type = ItemType.PROPS;
		goods.goodsId = props.getId();
		goods.baseId = props.getBaseId();
		goods.playerId = playerId;
		goods.sellGolden = sellGolden;
		goods.sellSilver = sellSilver;
		goods.sellCount = props.getCount();
		return goods;
	}
	// Getter and Setter....
	
	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public long getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(long goodsId) {
		this.goodsId = goodsId;
	}

	public long getSellSilver() {
		return sellSilver;
	}

	public void setSellSilver(long sellSilver) {
		this.sellSilver = sellSilver;
	}

	public ItemType getType() {
		return type;
	}

	public void setType(ItemType type) {
		this.type = type;
	}

	public int getBaseId() {
		return baseId;
	}

	public void setBaseId(int baseId) {
		this.baseId = baseId;
	}
	
	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}
	
	public long getSellGolden() {
		return sellGolden;
	}

	public void setSellGolden(long sellGolden) {
		this.sellGolden = sellGolden;
	}

	public MarketState getState() {
		return state;
	}

	public boolean isMarketing() {
		return this.state == MarketState.NORMAL;
	}
	
	public void setState(MarketState state) {
		this.state = state;
	}

	public int getSellCount() {
		return sellCount;
	}

	public void setSellCount(int sellCount) {
		this.sellCount = sellCount;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof MarketItem))
			return false;
		MarketItem other = (MarketItem) obj;
		return id != null && other.id != null && id.equals(other.id);
	}

}
