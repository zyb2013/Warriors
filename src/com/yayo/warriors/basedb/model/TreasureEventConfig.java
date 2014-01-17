package com.yayo.warriors.basedb.model;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.type.IndexName;

/**
 * 事件生成表
 * @author jonsai
 *
 */
@Resource
public class TreasureEventConfig {
	/** ID */
	@Id
	private int id;
	
	/** 藏宝ID */
	@Index(name = IndexName.TREASURE_EVENT_REWARDID_AND_DIG_PROPID_AND_NPCID, order = 0)
	private int rewardId;
	
	/** 铲子基础道具id */
	@Index(name = IndexName.TREASURE_EVENT_REWARDID_AND_DIG_PROPID_AND_NPCID, order = 1)
	private int propsId;
	
	/** 箱子id(npcId) */
	@Index(name = IndexName.TREASURE_EVENT_REWARDID_AND_DIG_PROPID_AND_NPCID, order = 2)
	private int type;
	
	/** 道具掉落编号 */
	private String itemDropNo;
	
	/** 怪物掉落编号 */
	private int monsterDropNo;
	
	/** 掉落编号 */
	@JsonIgnore
	private Set<Integer> itemDropNos = null;
	
	public Set<Integer> getItemDropNos() {
		if(itemDropNos == null && StringUtils.isNotBlank(this.itemDropNo) ){
			synchronized (this) {
				if(StringUtils.isNotBlank(this.itemDropNo)){
					if(itemDropNos == null){
						itemDropNos = new HashSet<Integer>(1);
					}
					String[] dropNos = this.itemDropNo.split(Splitable.ELEMENT_SPLIT);
					for(String str : dropNos){
						itemDropNos.add( Integer.valueOf(str) );
					}
				}
			}
		}
		return itemDropNos;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getItemDropNo() {
		return itemDropNo;
	}

	public void setItemDropNo(String itemDropNo) {
		this.itemDropNo = itemDropNo;
	}

	public int getMonsterDropNo() {
		return monsterDropNo;
	}

	public void setMonsterDropNo(int monsterDropNo) {
		this.monsterDropNo = monsterDropNo;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getRewardId() {
		return rewardId;
	}

	public void setRewardId(int rewardId) {
		this.rewardId = rewardId;
	}

	public int getPropsId() {
		return propsId;
	}

	public void setPropsId(int propsId) {
		this.propsId = propsId;
	}

	@Override
	public String toString() {
		return "TreasureEventConfig [id=" + id + ", rewardId=" + rewardId
				+ ", propsId=" + propsId + ", type=" + type + ", itemDropNo="
				+ itemDropNo + ", monsterDropNo=" + monsterDropNo + "]";
	}
	
	
}
