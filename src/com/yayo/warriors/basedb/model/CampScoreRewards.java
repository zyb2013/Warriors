package com.yayo.warriors.basedb.model;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;

/**
 * 阵营胜利积分奖励
 * @author jonsai
 *
 */
@Resource
public class CampScoreRewards {
	
	/** id */
	@Id
	private int id;
	
	/** 最小得分 */
	private int minScore;
	
	/** 最大得分 */
	private int maxScore;
	
	/** 得分礼包 */
	private int scoreGift;
	
	/** 经验 */
	private String exp;
	
	/** 铜币 */
	private String silver;
//	
//	/** 胜利礼包 */
//	private int successGift;
	
	@JsonIgnore
	private Map<Integer, Integer> propsRewards = null;
	
	/**
	 * 判断奖励是否为空
	 * @return
	 */
	public boolean isEmpty(){
		return StringUtils.isBlank(exp) && StringUtils.isBlank(silver) && scoreGift <= 0;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getMinScore() {
		return minScore;
	}

	public void setMinScore(int minScore) {
		this.minScore = minScore;
	}

	public int getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(int maxScore) {
		this.maxScore = maxScore;
	}

	public int getScoreGift() {
		return scoreGift;
	}

	public void setScoreGift(int scoreGift) {
		this.scoreGift = scoreGift;
	}

	public String getExp() {
		return exp;
	}

	public void setExp(String exp) {
		this.exp = exp;
	}

	public String getSilver() {
		return silver;
	}

	public void setSilver(String silver) {
		this.silver = silver;
	}

//	public int getSuccessGift() {
//		return successGift;
//	}
//
//	public void setSuccessGift(int successGift) {
//		this.successGift = successGift;
//	}
}
