package com.yayo.warriors.basedb.model;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.module.lottery.vo.LotteryRewardVo;
import com.yayo.warriors.type.IndexName;

@Resource
public class LotteryPropsRateConfig {

	/** id */
	@Id
	private int id;
	/** 奖励道具 */
	private String props;
	/** 数量 */
	private int count;
	/** 是否绑定 */
	private boolean bangding;
	/** 随机id */
	@Index(name = IndexName.LOTTERY_PROPS_ROLL_ID)
	private int propsRollId;
	/** 概率 */
	private int rate;
	/** 概率满值 */
	private int fullRate;
	
	@JsonIgnore
	private List<LotteryRewardVo> lotteryRewardVos = null;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getProps() {
		return props;
	}
	public void setProps(String props) {
		this.props = props;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public boolean isBangding() {
		return bangding;
	}
	public void setBangding(boolean bangding) {
		this.bangding = bangding;
	}
	public int getPropsRollId() {
		return propsRollId;
	}
	public void setPropsRollId(int propsRollId) {
		this.propsRollId = propsRollId;
	}
	public int getRate() {
		return rate;
	}
	public void setRate(int rate) {
		this.rate = rate;
	}
	public int getFullRate() {
		return fullRate;
	}
	public void setFullRate(int fullRate) {
		this.fullRate = fullRate;
	}
	
	public List<LotteryRewardVo> obtainLotteryPropsList(){
		if(this.lotteryRewardVos == null){
			synchronized (this) {
				if(this.lotteryRewardVos == null){
					this.lotteryRewardVos = new ArrayList<LotteryRewardVo>();
					String[] pSwaps = this.props.split(Splitable.ELEMENT_SPLIT);
					for(String pSwap : pSwaps){
						String[] tempSwap = pSwap.split(Splitable.ATTRIBUTE_SPLIT);
						if(tempSwap.length != 2){
						}
						int propsId = Integer.parseInt(tempSwap[1]);
						int propsType = Integer.parseInt(tempSwap[0]);
						lotteryRewardVos.add(new LotteryRewardVo(propsId,propsType, this.count, bangding));
					}
				}
			}
		}
		return new ArrayList<LotteryRewardVo>(this.lotteryRewardVos);
	}
}
