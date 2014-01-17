package com.yayo.warriors.module.campbattle.entity;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.module.campbattle.model.CampBattle;
import com.yayo.warriors.module.user.type.Camp;

/**
 * 阵营战场对象历史
 * @author jonsai
 *
 */
@Entity
@Table(name = "campBattleHistory")
public class CampBattleHistory extends BaseModel<Long> implements Comparable<CampBattleHistory>{
	private static final long serialVersionUID = -2131092784112237773L;
	
	/** 阵营 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	/** 阵营 */
	@Enumerated(EnumType.ORDINAL)
	private Camp camp;
	
	/** 角色id */
	@Temporal(TemporalType.TIMESTAMP)
	private Date battleDate;

	/** 击杀BOSS血量 */
	private int bossHurtHP;
	
	/** 击杀人头数 */
	private int killPlayers;

	/** 占领据点得分 */
	private int pointScores;
	
	/** 阵营战场分数 */
	private int scores;
	
	/** 是否胜利 */
	private boolean win = false;

	/** 是否击杀了对方boss	 */
	private boolean killBoss = false;
	
	/** 是否最后一场	 */
	private boolean last = false;
	
	/** 阵营占领的据点 */
	private String ownPoints = "";
	
	//-----------------------------------
	public static Collection<CampBattleHistory> valueOf(Collection<CampBattle> campBattles, Date battleDate) {
		List<CampBattleHistory> list = new ArrayList<CampBattleHistory>(campBattles.size());
		for(CampBattle campBattle : campBattles){
			CampBattleHistory history = new CampBattleHistory();
			history.battleDate = battleDate;
			history.camp = campBattle.getCamp();
			history.bossHurtHP = campBattle.getBossHurtHP();
			history.pointScores = campBattle.getPointScores();
			history.scores = campBattle.getTotalScores();
			history.killPlayers = campBattle.getKillPlayers();
			history.win = campBattle.isWin();
			history.last = campBattle.isLast();
			history.killBoss = campBattle.isKillBoss();
			
			CopyOnWriteArrayList<Integer> ownPointIds = campBattle.getOwnPointIds();
			StringBuilder sb = new StringBuilder();
			for(Integer monsterConfigId: ownPointIds){
				sb.append(Splitable.ELEMENT_DELIMITER).append( monsterConfigId );
			}
			if(sb.length() > 0){
				sb.deleteCharAt(0);
			}
			history.ownPoints = sb.toString();
			
			list.add(history);
		}
		
		return list;
	}
	
	public String getOwnPoints() {
		return ownPoints;
	}

	public void setOwnPoints(String ownPoints) {
		this.ownPoints = ownPoints;
	}

	public boolean isWin() {
		return win;
	}

	public void setWin(boolean win) {
		this.win = win;
	}

	public int getKillPlayers() {
		return killPlayers;
	}

	public void setKillPlayers(int killPlayers) {
		this.killPlayers = killPlayers;
	}

	
	public Long getId() {
		return id;
	}

	
	public void setId(Long id) {
		this.id = id;
	}

	public Camp getCamp() {
		return camp;
	}

	public void setCamp(Camp camp) {
		this.camp = camp;
	}
	
	public Date getBattleDate() {
		return battleDate;
	}

	public void setBattleDate(Date battleDate) {
		this.battleDate = battleDate;
	}

	public int getBossHurtHP() {
		return bossHurtHP;
	}

	public void setBossHurtHP(int bossHurtHP) {
		this.bossHurtHP = bossHurtHP;
	}

	public int getPointScores() {
		return pointScores;
	}

	public void setPointScores(int pointScores) {
		this.pointScores = pointScores;
	}

	public int getScores() {
		return scores;
	}

	public void setScores(int scores) {
		this.scores = scores;
	}

	public boolean isLast() {
		return last;
	}

	public void setLast(boolean last) {
		this.last = last;
	}

	public boolean isKillBoss() {
		return killBoss;
	}

	public void setKillBoss(boolean killBoss) {
		this.killBoss = killBoss;
	}

	
	public int compareTo(CampBattleHistory o2) {
		if(this.isWin() && !o2.isWin()){
			return -1;
		} else if(!o2.isWin() && o2.isWin() ){
			return 1;
		}
		if(this.getScores() > o2.getScores()){
			return -1;
		} else if(this.getScores() < o2.getScores()){
			return 1;
		}
		return 0;
	}

	
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((battleDate == null) ? 0 : battleDate.hashCode());
		result = prime * result + ((camp == null) ? 0 : camp.hashCode());
		return result;
	}

	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CampBattleHistory other = (CampBattleHistory) obj;
		if (battleDate == null) {
			if (other.battleDate != null)
				return false;
		} else if (!battleDate.equals(other.battleDate))
			return false;
		if (camp != other.camp)
			return false;
		return true;
	}
}
