package com.yayo.warriors.module.battlefield.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.yayo.common.db.model.BaseModel;
import com.yayo.warriors.module.battlefield.vo.CollectTaskVO;

@Entity
@Table(name = "playerBattleField")
public class PlayerBattleField extends BaseModel<Long> {
	private static final long serialVersionUID = -2224182593209019773L;
	
	/** 角色id */
	@Id
	@Column(name="playerId")
	private Long id;
	
	/** 战场开始时间(场次)  */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = true)
	private Date battleDate;
	
	/** 杀人数 */
	private int killPlayers;
	
	/** 死亡次数 */
	private int deaths;
	
	/** 战斗荣誉 */
	private int fightHonor;
	
	/** 采集荣誉 */
	private int collectHonor;
	
	/** 领奖励时间 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = true)
	private Date rewardDate;
	
	//--------------上次的战场信息---------------
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = true)
	private Date preBattleDate;

	/** 荣誉 */
	private int preTotalHonor;
	
	/** 采集任务vo */
	@Transient
	public CollectTaskVO collectTaskVO;
	
	/** 退出战场时间 */
	@Transient
	private long enterCDTime;
	
	//---------------------------------------------
	public static PlayerBattleField valueOf(long playerId){
		PlayerBattleField playerBattleField = new PlayerBattleField();
		playerBattleField.id = playerId;
		return playerBattleField;
	}
	
	public void reset(){
		this.preBattleDate = this.battleDate;
		this.preTotalHonor = this.collectHonor + this.fightHonor;
		
		this.collectHonor = 0;
		this.fightHonor = 0;
		this.deaths = 0;
		this.killPlayers = 0;
	}
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
	
	public int getKillPlayers() {
		return killPlayers;
	}

	public void setKillPlayers(int killPlayers) {
		this.killPlayers = killPlayers;
	}

	public int getDeaths() {
		return deaths;
	}

	public void setDeaths(int deaths) {
		this.deaths = deaths;
	}

	public Date getBattleDate() {
		return battleDate;
	}

	public void setBattleDate(Date battleDate) {
		this.battleDate = battleDate;
	}
	
	public Date getRewardDate() {
		return rewardDate;
	}

	public void setRewardDate(Date rewardDate) {
		this.rewardDate = rewardDate;
	}

	public Date getPreBattleDate() {
		return preBattleDate;
	}

	public void setPreBattleDate(Date preBattleDate) {
		this.preBattleDate = preBattleDate;
	}

	public int getFightHonor() {
		return fightHonor;
	}

	public void setFightHonor(int fightHonor) {
		this.fightHonor = fightHonor;
	}

	public int getCollectHonor() {
		return collectHonor;
	}

	public void setCollectHonor(int collectHonor) {
		this.collectHonor = collectHonor;
	}
	
	public void increaseCollectHonor(int collectHonor){
		this.collectHonor += collectHonor;
	}
	
	public void increaseFightHonor(int fightHonor) {
		this.fightHonor += fightHonor;
	}
	
	public void increaseKillPlayers() {
		this.killPlayers++;
	}
	
	public void increaseDeaths() {
		this.deaths++;
	}
	
	public long getEnterCDTime() {
		return enterCDTime;
	}

	public void setEnterCDTime(long enterCDTime) {
		this.enterCDTime = enterCDTime;
	}

	public int getPreTotalHonor() {
		return preTotalHonor;
	}

	public void setPreTotalHonor(int preTotalHonor) {
		this.preTotalHonor = preTotalHonor;
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
		PlayerBattleField other = (PlayerBattleField) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
