package com.yayo.warriors.module.recharge.model;

import java.util.LinkedList;
import java.util.List;

import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.model.GoodsVO;

/**
 * 礼包奖励
 * 
 * @author Hyint
 */
public class GiftReward {

	/** 增加的经验 */
	private int addExp;

	/** 增加的绑定元宝 */
	private int addCoupon;
	
	/** 增加的绑定游戏币 */
	private int addSilver;
	
	/** 增加的装备 */
	private List<UserEquip> userEquips = new LinkedList<UserEquip>();
	
	/** 增加的道具 */
	private List<UserProps> userProps = new LinkedList<UserProps>();

	/** 获得的物品VO */
	private List<GoodsVO> goodVOList = new LinkedList<GoodsVO>();
	
	/** 日志集合 */
	private List<LoggerGoods> loggerGoods = new LinkedList<LoggerGoods>();
	
	public int getAddExp() {
		return addExp;
	}

	public void setAddExp(int addExp) {
		this.addExp = addExp;
	}

	public int getAddCoupon() {
		return addCoupon;
	}

	public void setAddCoupon(int addCoupon) {
		this.addCoupon = addCoupon;
	}

	public int getAddSilver() {
		return addSilver;
	}

	public void setAddSilver(int addSilver) {
		this.addSilver = addSilver;
	}

	public List<UserEquip> getUserEquips() {
		return userEquips;
	}

	public void setUserEquips(List<UserEquip> userEquips) {
		this.userEquips = userEquips;
	}

	public List<UserProps> getUserProps() {
		return userProps;
	}

	public void setUserProps(List<UserProps> userProps) {
		this.userProps = userProps;
	}

	public List<GoodsVO> getGoodVOList() {
		return goodVOList;
	}

	public void setGoodVOList(List<GoodsVO> goodVOList) {
		this.goodVOList = goodVOList;
	}

	public List<LoggerGoods> getLoggerGoods() {
		return loggerGoods;
	}

	public void setLoggerGoods(List<LoggerGoods> loggerGoods) {
		this.loggerGoods = loggerGoods;
	}
	
	public LoggerGoods[] getLoggerGoodsArray() {
		return loggerGoods.toArray(new LoggerGoods[loggerGoods.size()]);
	}
}
