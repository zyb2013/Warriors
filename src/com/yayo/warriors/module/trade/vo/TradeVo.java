package com.yayo.warriors.module.trade.vo;

import java.io.Serializable;
import java.util.List;

import com.yayo.warriors.module.trade.model.TradeProps;
import com.yayo.warriors.module.trade.model.UserTrade;
import com.yayo.warriors.type.Currency;

/**
 * 交易物品VO
 * @author huachaoping
 */
public class TradeVo implements Serializable {

	private static final long serialVersionUID = 8210425132695317796L;
	
	/** 玩家Id */
	private long playerId;
	
	/** 元宝数量 */
	private long goldenCount;
	
	/** 铜币数量 */
	private long siliverCount;
	
	/** 交易物品数组 */
	private TradeProps[] props;
	
	private TradeVo() {
	}
	
	/** 构造函数 */
	public static TradeVo valueOf(long playerId, UserTrade userTrade) {
		TradeVo vo = new TradeVo();
		vo.playerId = playerId;
		List<TradeProps> propsList = userTrade.getPropsList();
		vo.props = propsList.toArray(new TradeProps[propsList.size()]);
		vo.goldenCount = userTrade.getCurrency(Currency.GOLDEN.ordinal());
		vo.siliverCount = userTrade.getCurrency(Currency.SILVER.ordinal());
		return vo;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public TradeProps[] getProps() {
		return props;
	}

	public void setProps(TradeProps[] props) {
		this.props = props;
	}

	public long getGoldenCount() {
		return goldenCount;
	}

	public void setGoldenCount(long goldenCount) {
		this.goldenCount = goldenCount;
	}

	public long getSiliverCount() {
		return siliverCount;
	}

	public void setSiliverCount(long siliverCount) {
		this.siliverCount = siliverCount;
	}
	
}
