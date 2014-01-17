package com.yayo.warriors.module.trade.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.trade.rule.TradeRule;
import com.yayo.warriors.type.Currency;
import com.yayo.warriors.type.GoodsType;

/**
 * 玩家交易信息对象
 * @author huachaoping
 */
public class UserTrade implements Serializable {
	
	private static final long serialVersionUID = 6736439106069451924L;

	/** 玩家Id */
	private long playerId;
	
	/** 对方Id */
	private long targetId;
	
	/** 是否锁定(锁定-true) */
	private boolean lockProps;
	
	/** 是否确定交易 (确定-true) */
	private boolean click2Trade;
	
	/** 玩家的被邀请列表 [目标ID列表] */
	private final Set<Long> INVITE_SET = new HashSet<Long>(TradeRule.TRADE_LIMIT);
	
	/** [货币类型-数量] 两种货币 */
	private final Map<Integer, Long> MONEY_MAP = new HashMap<Integer, Long>(Currency.values().length);
	
	/** [物品索引-交易物品] */
	private final Map<Integer, TradeProps> TRADE_PROP_MAP = new HashMap<Integer, TradeProps>(TradeRule.TRADE_LIMIT);
	
	/**
	 * 构造函数
	 * @param playerId
	 * @return
	 */
	public static UserTrade valueOf(long playerId) {
		UserTrade userTrade = new UserTrade();
		userTrade.playerId  = playerId;
		return userTrade;
	}

	public Long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}

	public boolean isLockProps() {
		return lockProps;
	}

	public void setLockProps(boolean lockProps) {
		this.lockProps = lockProps;
	}

	public boolean isClick2Trade() {
		return click2Trade;
	}

	public void setClick2Trade(boolean click2Trade) {
		this.click2Trade = click2Trade;
	}
	
	// 获取交易目标
	public Long getTradeTarget() {
		return targetId;
	}
	
	// 加入玩家交易
	// true  - 加入成功
	// false - 加入失败
	public void addTrade(long targetId) {
		this.targetId = targetId;
	}
	
	public boolean isTraded() {
		return this.targetId > 0L;
	}
	
	// 取消交易, 移除目标, 清空缓存
	public void cancleTrade() {
		this.targetId = 0L;
		this.MONEY_MAP.clear();
		this.INVITE_SET.clear();
		this.TRADE_PROP_MAP.clear();
	}
	
	// 取得被邀请玩家列表
	public Set<Long> getBeInvitedPlayerIds() {
		return INVITE_SET;
	}
	
	// 移除邀请人
	public boolean removeBeInvitedPlayerId(long playerId) {
		return INVITE_SET.remove(playerId);
	}
	
	// 加入玩家的邀请列表
	public void add2InvitedSet(Long playerId) {
		this.INVITE_SET.add(playerId);
	}
	
	// 获得交易货币数量
	public long getCurrency(int currency) {
		Long count = MONEY_MAP.get(currency);
		return count == null ? 0 : count;
	}
	
	public void addCurrency(int currency, long count) {
		this.MONEY_MAP.put(currency, count);
	}
	
	// 增加交易物品, 当到达物品上限时不能加
	// true  - 增加成功
	// false - 增加失败
	public boolean addTradeProps(TradeProps tradeProps) {
		int maxIndex  = TRADE_PROP_MAP.keySet().size();
		maxIndex ++ ; 
		if (maxIndex > TradeRule.TRADE_LIMIT) {
			return false;
		}
		tradeProps.setIndex(maxIndex);
		TRADE_PROP_MAP.put(maxIndex, tradeProps);
		return true;
	}
	
	// 移除物品, 位置须重置
	public void removeTradeProps(int index) {
		this.TRADE_PROP_MAP.remove(index);
		for (int i = 1; i <= TradeRule.TRADE_LIMIT; i++) {
			TradeProps props = TRADE_PROP_MAP.get(i);
			int curIndex = i;
			if (props == null) {
				continue;
			}
			if (curIndex > index) {
				curIndex -- ;
				props.setIndex(curIndex);
				TRADE_PROP_MAP.put(curIndex, props);
			}
		}
		if (index != TRADE_PROP_MAP.size() + 1) {
			TRADE_PROP_MAP.remove(TRADE_PROP_MAP.size());
		}
	}
	
	// 移除交易物品
	public boolean removeProps(long goodsId, int goodsType, int count) {
		int index = -1;
		for (TradeProps props : TRADE_PROP_MAP.values()) {
			if (goodsId  ==  props.getUserPropId() 
			   && goodsType == props.getGoodType()
			   && count == props.getCount()) {
				index = props.getIndex();
				this.removeTradeProps(index);
				return true;
			}
		}
		return false;
	}
	
	// 按顺序获得玩家交易物品
	public List<TradeProps> getPropsList() {
		List<TradeProps> propsList = new ArrayList<TradeProps>();
		for (int i = 1; i <= TradeRule.TRADE_LIMIT; i++) {
			TradeProps props = TRADE_PROP_MAP.get(i);
			if (props == null) {
				break;
			}
			propsList.add(props);
		}
		return propsList;
	}

	
	// 验证同一件用户物品的交易数量
	public boolean validPropsCount(UserProps props, int addCount) {
		long userPropsId = props.getId();
		for (TradeProps tradeProps : TRADE_PROP_MAP.values()) {
			int type = tradeProps.getGoodType();
			if (type == GoodsType.PROPS && 
			    tradeProps.getUserPropId() == userPropsId) {
				int cacheCount = tradeProps.getCount();
				addCount += cacheCount;
			}
		}
		if (addCount > props.getCount()) {
			return false;
		}
		return true;
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
		if (!(obj instanceof UserTrade))
			return false;
		UserTrade other = (UserTrade) obj;
		return playerId == other.playerId;
	}

	public String toString() {
		return "UserTrade [playerId=" + playerId + ","  
	         + "isTrading=" + targetId + "," + "lockProps"
			 + lockProps + "click2Trade" + click2Trade + "]";
	}

}
