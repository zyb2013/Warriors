package com.yayo.warriors.module.campbattle.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.yayo.common.db.model.BaseModel;
import com.yayo.warriors.module.campbattle.model.PlayerCampBattle;
import com.yayo.warriors.module.campbattle.type.CampTitle;
import com.yayo.warriors.module.campbattle.type.PlayerCampBattleStatus;
import com.yayo.warriors.module.user.type.Camp;


/**
 * 角色阵营战场信息
 * @author jonsai
 *
 */
@Entity
@Table(name = "playerCampBattleHistory")
public class PlayerCampBattleHistory extends BaseModel<Long> {
	private static final long serialVersionUID = -1610440747408490775L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	/** 战场时间 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date battleDate;
	
	/** 角色id */
	private long playerId;
	
	/** 角色阵营 */
	private Camp camp;
	
	/** 角色等级 */
	private int level;
	
	/** 击杀BOSS血量 */
	private int bossHurtHP;

	/** 击杀玩数量 */
	private int killPlayers;
	
	/** 个人战场总得分 */
	private int scores;
	
	/** 伤害BOSS得分 */
	private int hurtBossScores;
	
	/** 阵营官衔 */
	@Enumerated(EnumType.ORDINAL)
	private CampTitle campTitle;
	
	/** 状态 */
	@Enumerated(EnumType.ORDINAL)
	private PlayerCampBattleStatus status;
	
	/** 是否本周最后一场 */
	private boolean last;
	
	/** 本阵营本次排名 */
	@Transient
	private int rank;
	
	/**
	 * 构造玩家战场历史对象
	 * @param playerCampBattle
	 * @param battleDate
	 * @return
	 */
	public static PlayerCampBattleHistory valueOf(PlayerCampBattle playerCampBattle, Date battleDate){
		PlayerCampBattleHistory history = new PlayerCampBattleHistory();
		history.battleDate = battleDate;
		history.playerId = playerCampBattle.getPlayerId();
		history.bossHurtHP = playerCampBattle.getBossHurtHP();
		history.killPlayers = playerCampBattle.getKillPlayers();
		history.scores = playerCampBattle.getTotalScores();
		history.hurtBossScores = playerCampBattle.getHurtBossScores();
		history.campTitle = playerCampBattle.getCampTitle();
		history.status = playerCampBattle.getStatus();
		history.camp = playerCampBattle.getCamp();
		history.level = playerCampBattle.getLevel();
		history.last = playerCampBattle.isLast();
		
		return history;
	}
	
	/**
	 * 构造玩家战场历史对象
	 * @param playerCampBattles
	 * @param battleDate
	 * @return
	 */
	public static List<PlayerCampBattleHistory> valueOf(Collection<PlayerCampBattle> playerCampBattles, Date battleDate){
		List<PlayerCampBattleHistory> list = null;
		if(playerCampBattles != null && battleDate != null){
			list = new ArrayList<PlayerCampBattleHistory>( playerCampBattles.size() );
			for(PlayerCampBattle playerCampBattle : playerCampBattles){
				list.add( PlayerCampBattleHistory.valueOf(playerCampBattle, battleDate) );
			}
		}
		return list;
	}
	
	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public PlayerCampBattleStatus getStatus() {
		return status;
	}

	public void setStatus(PlayerCampBattleStatus status) {
		this.status = status;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
	
	public Camp getCamp() {
		return camp;
	}

	public void setCamp(Camp camp) {
		this.camp = camp;
	}

	public int getBossHurtHP() {
		return bossHurtHP;
	}

	public void setBossHurtHP(int bossHurtHP) {
		this.bossHurtHP = bossHurtHP;
	}

	public int getKillPlayers() {
		return killPlayers;
	}

	public void setKillPlayers(int killPlayers) {
		this.killPlayers = killPlayers;
	}

	public int getScores() {
		return scores;
	}

	public void setScores(int scores) {
		this.scores = scores;
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

	public int getHurtBossScores() {
		return hurtBossScores;
	}

	public void setHurtBossScores(int hurtBossScores) {
		this.hurtBossScores = hurtBossScores;
	}

	public boolean isLast() {
		return last;
	}

	public void setLast(boolean last) {
		this.last = last;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((battleDate == null) ? 0 : battleDate.hashCode());
		result = prime * result + (int) (playerId ^ (playerId >>> 32));
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
		PlayerCampBattleHistory other = (PlayerCampBattleHistory) obj;
		if (battleDate == null) {
			if (other.battleDate != null)
				return false;
		} else if (!battleDate.equals(other.battleDate))
			return false;
		if (playerId != other.playerId)
			return false;
		return true;
	}
	
}
