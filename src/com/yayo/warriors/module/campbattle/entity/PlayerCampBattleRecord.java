package com.yayo.warriors.module.campbattle.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.DatePattern;
import com.yayo.common.utility.DateUtil;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.module.campbattle.rule.CampBattleRule;
import com.yayo.warriors.module.campbattle.type.CampTitle;
import com.yayo.warriors.module.user.type.Camp;

/**
 * 角色战场记录（不是历史）
 * @author jonsai
 *
 */
@Entity
@Table(name = "playerCampBattleRecord")
public class PlayerCampBattleRecord extends BaseModel<Long> {
	private static final long serialVersionUID = -3810864778147650657L;
	
	/** 角色id */
	@Id
	@Column(name = "playerId")
	private Long id;
	
	/** 阵营 */
	@Enumerated
	private Camp camp = Camp.NONE;
	
	/** 参加的场数 */
	private int joins = 0;
	
	/** 几场后最终总得分 */
	private int totalScore;
	
	/** 最近进的一场战场时间 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date battleDate;
	
	/** 阵营称号 */
	@Enumerated
	private CampTitle campTitle = CampTitle.NONE;
	
	/** 领官衔俸禄奖励时间 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date salaryReward;
	
	/** 领官衔时装奖励时间 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date suitReward;

	/** 领取的阵营战场胜利奖励的时间, 格式：yyyyMMddHHmm|... */
	@Lob
	private String campBattleReward = "";
	
	@Transient
	private Set<Date> campBattleRewards = null;
	
	private Set<Date> getCampBattleRewards() {
		if(campBattleRewards == null){
			synchronized (this) {
				if(campBattleRewards == null){
					campBattleRewards = Collections.synchronizedSet( new HashSet<Date>(1) );
					if( StringUtils.isNotBlank(this.campBattleReward) ){
						String[] dates = this.campBattleReward.split(Splitable.ELEMENT_SPLIT);
						if(dates != null && dates.length > 0){
							for(String date : dates){
								Date string2Date = DateUtil.string2Date(date, DatePattern.PATTERN_YYYYMMDDHHMM);
								if(string2Date != null){
									campBattleRewards.add( string2Date );
								}
							}
						}
					}
				}
			}
		}
		return campBattleRewards;
	}
	
	private void refreshCampBattleRewards(){
		if(campBattleRewards != null){
			StringBuilder sb = new StringBuilder();
			synchronized (campBattleRewards) {
				for(Date date : campBattleRewards){
					String date2String = DateUtil.date2String(date, DatePattern.PATTERN_YYYYMMDDHHMM);
					sb.append(Splitable.ELEMENT_DELIMITER).append(date2String);
				}
			}
			if(sb.length() > 0){
				sb.deleteCharAt(0);
			}
			this.campBattleReward = sb.toString();
		}
	}
	
	/**
	 * 是否已经领奖
	 * @param campBattleDate
	 * @return
	 */
	public boolean isRewardCampBattle(Date campBattleDate){
		if(campBattleDate != null){
			Set<Date> campBattleRewards = getCampBattleRewards();
			synchronized (campBattleRewards) {
				return campBattleRewards.contains( campBattleDate );
			}
		}
		return false;
	}
	
	/**
	 * 添加到已经领取阵营奖励
	 * @param campBattleDate
	 * @return
	 */
	public boolean add2CampBattleRewards(Date campBattleDate){
		if(campBattleDate != null){
			Set<Date> campBattleRewards = getCampBattleRewards();
			synchronized (campBattleRewards) {
				boolean result = campBattleRewards.add( campBattleDate );
				if(campBattleRewards.size() > CampBattleRule.CAMP_BATTLE_RECORD_FETCH_COUNT){
					List<Date> list = new ArrayList<Date>( campBattleRewards.size() );
					Collections.sort(list);
					for(Date date : list){
						if(campBattleRewards.size() <= CampBattleRule.CAMP_BATTLE_RECORD_FETCH_COUNT){
							continue;
						}
						campBattleRewards.remove( date );
					}
				}
				refreshCampBattleRewards();
				return result;
			}
		}
		return false;
	}
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
	
	public Date getBattleDate() {
		return battleDate;
	}

	public void setBattleDate(Date battleDate) {
		this.battleDate = battleDate;
	}

	public CampTitle getCampTitle() {
		return campTitle;
	}

	public void setCampTitle(CampTitle campTitle) {
		this.campTitle = campTitle;
	}

	public Date getSuitReward() {
		return suitReward;
	}

	public void setSuitReward(Date suitReward) {
		this.suitReward = suitReward;
	}
	
	public Date getSalaryReward() {
		return salaryReward;
	}

	public void setSalaryReward(Date salaryReward) {
		this.salaryReward = salaryReward;
	}

	public int getJoins() {
		return joins;
	}

	public void setJoins(int joins) {
		this.joins = joins;
	}

	public String getCampBattleReward() {
		return campBattleReward;
	}

	public void setCampBattleReward(String campBattleReward) {
		this.campBattleReward = campBattleReward;
	}

	public int getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(int totalScore) {
		this.totalScore = totalScore;
	}
	
	public void increaseTotalScore(int totalScore) {
		this.totalScore += totalScore;
	}

	public Camp getCamp() {
		return camp;
	}

	public void setCamp(Camp camp) {
		this.camp = camp;
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
		if (getClass() != obj.getClass())
			return false;
		PlayerCampBattleRecord other = (PlayerCampBattleRecord) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
