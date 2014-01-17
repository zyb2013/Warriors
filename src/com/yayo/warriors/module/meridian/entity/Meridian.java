package com.yayo.warriors.module.meridian.entity;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.DateUtil;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.module.meridian.MeridianRule;

/**
 * 玩家经脉实体
 * @author huachaoping 
 *
 */
@Entity
@Table(name="meridian")
public class Meridian extends BaseModel<Long>{

	private static final long serialVersionUID = -5703930453750138399L;

	/** 主键id */
	@Id
	@Column(name="playerId")
	private Long id;

	/**
	 * 已经学习的经脉点数。
	 * 
	 * 格式: 经脉ID_经脉ID...
	 */
	@Lob
	private String meridians = "";
	
	/** 已获得经验*/
	private int acquiredExp;
	
	/** 剩余次数(分享经验次数)*/
	private int laveTimes;
	
	/** 更新剩余次数时间 */
	private Date updateTime;
	
	/** 是否通过小周天 */
	private boolean stagePass;
	
	/** 经脉点数，参考开启的经脉id，主要用于经脉排行榜 */
	private Integer meridianIds = 0;
	
	@Transient
	private transient Set<Integer> meridiansSet = null;
	
	@Transient
	private transient Map<Integer, Integer> meridianAttributes = null;
	
	@Transient
	private transient Map<Integer, Collection<Integer>> meridianTypes = null;
	
		
	public Map<Integer, Integer> getMeridianAttributes() {
		return meridianAttributes;
	}
	
	public boolean isAttributeEmpty() {
		return this.meridianAttributes == null || this.meridianAttributes.isEmpty();
	}
	
	public void setMeridianAttributes(Map<Integer, Integer> meridianAttributes) {
		this.meridianAttributes = meridianAttributes;
	}
	
	public Map<Integer, Collection<Integer>> getMeridianTypes() {
		return meridianTypes;
	}

	public void setMeridianTypes(Map<Integer, Collection<Integer>> meridianTypes) {
		this.meridianTypes = meridianTypes;
	}
	
	public void resetMedianTypes() {
		this.meridianTypes = null;
		this.meridianAttributes = null;
	}
	
	public void resetMeridianSet() {
		this.meridiansSet = null;
	}
	
	/**
	 * 取出经脉点列表
	 * 
	 * @return {@link Set}	经脉ID列表
	 */
	public Set<Integer> getMeridiansSet() {
		if(this.meridiansSet != null) {
			return this.meridiansSet;
		}
		
		synchronized (this) {
			if(this.meridiansSet != null) {
				return this.meridiansSet;
			}
			this.meridiansSet = new HashSet<Integer>();
			if(StringUtils.isBlank(this.meridians)) {
				return this.meridiansSet;
			}

			String[] array = this.meridians.split(Splitable.ATTRIBUTE_SPLIT);
			for (String element : array) {
				this.meridiansSet.add(Integer.valueOf(element));
			}
			this.meridianIds = meridiansSet.size();
		}
		return this.meridiansSet;
	}
	
	
	public void updateMeridianSet() {
		StringBuilder builder = new StringBuilder();
		Set<Integer> set = this.getMeridiansSet();
		for (Integer meridianId : set) {
			builder.append(meridianId).append(Splitable.ATTRIBUTE_SPLIT);
		}
		if(builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		this.meridians = builder.toString();
		this.meridianIds = set.size();
		resetMedianTypes();
	}
	
	/**
	 * 构造函数
	 * 
	 * @param  playerId			玩家id
	 * @return {@link Meridian}	经脉对象
	 */
	public static Meridian valueOf(long playerId) {
		Meridian playerMeridian = new Meridian();
		playerMeridian.id = playerId;
		playerMeridian.acquiredExp = 0;
		playerMeridian.stagePass = false;
		playerMeridian.laveTimes = MeridianRule.TIMES;
		return playerMeridian;
	}

	public boolean isStagePass() {
		return stagePass;
	}

	public void setStagePass(boolean stagePass) {
		this.stagePass = stagePass;
	}

	public int getAcquiredExp() {
		return acquiredExp;
	}

	public void increaseAcquiredExp(int exp) {
		this.acquiredExp += exp;
	}

	public int getLaveTimes() {
		return laveTimes;
	}

	public void setMeridians(String meridians) {
		this.meridiansSet = null;
		this.meridians = meridians;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMeridians() {
		return meridians;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void decreaseTimes() {
		this.laveTimes--;
	}
	
	// 更新次数
	public void updateTimes(){
		this.updateTime = DateUtil.getNextDay0AM(new Date());
		this.laveTimes  = MeridianRule.TIMES;
	}
	
	public Integer getMeridianIds() {
		return meridianIds;
	}

	public void setMeridianIds(Integer meridianIds) {
		this.meridianIds = meridianIds;
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
		if (this == obj) {
			return true;
		} else if (!super.equals(obj)) {
			return false;
		} else if (!(obj instanceof Meridian)) {
			return false;
		}
		
		Meridian other = (Meridian) obj;
		return id != null && other.id != null && id.equals(other.id);
	}

	@Override
	public String toString() {
		return "Meridian [id=" + id + ", meridians=" + meridians + "]";
	}

}
