package com.yayo.warriors.module.rank.vo;

import java.io.Serializable;

import com.yayo.warriors.module.rank.type.RankType;

/**
 * 排行榜组合信息VO 
 * @author jonsai
 * 
 */
public class RankVO implements Serializable {
	private static final long serialVersionUID = 3377240584930963528L;
	
	/** 当前分页 */
	private int pageNow = 0;

	/** 分页数量 */
	private int pageCount = 0;
	
	/** 排行榜类型  {@link RankType} */
	private int rankType;
	
	/** 排行榜列表信息{@link RankInfoVO} */
	private Object[] values = null;
	
	/** 角色排行榜是, >0:玩家的排行, <=0:没有入榜 */
	private int rank = 0;
	
	/** 搜索的角色名,用于回传给客户端 */
	private String targetPlayerName = "";
	
	//-----------------------领域方法----------------------------------------------
	/**
	 * 构造一个RankVO
	 * @param values
	 * @return
	 */
	public static RankVO valueOf(RankType rankType){
		RankVO vo = new RankVO();
		vo.rankType = rankType.ordinal();
		return vo;
	}
	
	public int getPageNow() {
		return pageNow;
	}

	public void setPageNow(int pageNow) {
		this.pageNow = pageNow;
	}

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public Object[] getValues() {
		return values;
	}

	public void setValues(Object[] values) {
		this.values = values;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getRankType() {
		return rankType;
	}

	public void setRankType(int rankType) {
		this.rankType = rankType;
	}

	public String getTargetPlayerName() {
		return targetPlayerName;
	}

	public void setTargetPlayerName(String targetPlayerName) {
		this.targetPlayerName = targetPlayerName;
	}
	
}
