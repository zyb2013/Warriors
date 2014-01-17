package com.yayo.warriors.basedb.model;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.InitializeBean;
import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.NumberUtil;
import com.yayo.warriors.type.IndexName;

/**
 * 怪物生成表
 * @author jonsai
 *
 */
@Resource
public class TreasureMonsterConfig extends RateConfig implements InitializeBean{
	/** id */
	@Id
	private int id;
	
	/** 事件ID(怪物掉落id) */
	@Index(name = IndexName.TREASURE_DROP_NO)
	private int dropNo;
	
	/** 怪物ID */
	private String monsterId;
	
	/** 公告id */
	private int noticeID;

	@JsonIgnore
	private Map<Integer, Integer> rewardMonster = null;
	
	//-----------------------------------------------
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getDropNo() {
		return dropNo;
	}

	public void setDropNo(int dropNo) {
		this.dropNo = dropNo;
	}

	public String getMonsterId() {
		return monsterId;
	}

	public void setMonsterId(String monsterId) {
		this.monsterId = monsterId;
	}

	public Map<Integer, Integer> getRewardMonster() {
		return rewardMonster;
	}
	
	public int getNoticeID() {
		return noticeID;
	}

	public void setNoticeID(int noticeID) {
		this.noticeID = noticeID;
	}

	
	public void afterPropertiesSet() {
		if(StringUtils.isNotBlank(this.monsterId)){
			rewardMonster = NumberUtil.delimiterString2Map2(this.monsterId, Integer.class, Integer.class);
		}
	}
	
}
