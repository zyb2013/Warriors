package com.yayo.warriors.module.campbattle.vo;

import java.io.Serializable;
import java.util.concurrent.CopyOnWriteArrayList;


import com.yayo.warriors.module.campbattle.entity.CampBattleHistory;
import com.yayo.warriors.module.campbattle.model.CampBattle;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.user.type.Camp;


/**
 * 阵营战场对象
 * @author jonsai
 *
 */
public class CampBattleVO implements Serializable{
	private static final long serialVersionUID = 7642343388234100292L;

	/** 阵营 */
	private Camp camp;

	/** BOSS血量 */
	private int bossHP;
	
	/** 阵营战场分数 */
	private int scores;
	
	/** 是否胜利 */
	private boolean win;
	
	/** 是否击杀了对方boss */
	private boolean killBoss = false;
	
	/** 已经占领的据点 */
	private Object[] ownPoints = null;
	
	/**
	 * 构造一个对象
	 * @param camp
	 * @return
	 */
	public static CampBattleVO valueOf(Camp camp){
		CampBattleVO campBattle = new CampBattleVO();
		campBattle.camp = camp;
		return campBattle;
	}
	
	/**
	 * 
	 * @param campBattle
	 * @return
	 */
	public static CampBattleVO valueOf(CampBattle campBattle){
		CampBattleVO campBattleVO = new CampBattleVO();
		synchronized (campBattle) {
			campBattleVO.camp = campBattle.getCamp();
			MonsterDomain monsterAiDomain = campBattle.getMonsterAiDomain();
			campBattleVO.bossHP = monsterAiDomain.getMonsterBattle().getHp();
			campBattleVO.scores = campBattle.getTotalScores();
			CopyOnWriteArrayList<Integer> ownPointIdList = campBattle.getOwnPointIds();
			campBattleVO.ownPoints = ownPointIdList.toArray();
			campBattleVO.win = campBattle.isWin();
			campBattleVO.killBoss = campBattle.isKillBoss();
		}
		
		return campBattleVO;
	}
	
	/**
	 * 
	 * @param campBattleHistory
	 * @return
	 */
	public static CampBattleVO valueOf(CampBattleHistory campBattleHistory){
		CampBattleVO campBattleVO = new CampBattleVO();
		campBattleVO.camp = campBattleHistory.getCamp();
		campBattleVO.scores = campBattleHistory.getScores();
		campBattleVO.win = campBattleHistory.isWin();
		campBattleVO.killBoss = campBattleHistory.isKillBoss();
		return campBattleVO;
	}
	
	
	public Camp getCamp() {
		return camp;
	}

	public void setCamp(Camp camp) {
		this.camp = camp;
	}

	public int getScores() {
		return scores;
	}

	public void setScores(int scores) {
		this.scores = scores;
	}

	public int getBossHP() {
		return bossHP;
	}

	public void setBossHP(int bossHP) {
		this.bossHP = bossHP;
	}

	public Object[] getOwnPoints() {
		return ownPoints;
	}

	public void setOwnPoints(Object[] ownPoints) {
		this.ownPoints = ownPoints;
	}

	public boolean isWin() {
		return win;
	}

	public void setWin(boolean win) {
		this.win = win;
	}

	public boolean isKillBoss() {
		return killBoss;
	}

	public void setKillBoss(boolean killBoss) {
		this.killBoss = killBoss;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((camp == null) ? 0 : camp.hashCode());
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
		CampBattleVO other = (CampBattleVO) obj;
		if (camp == null) {
			if (other.camp != null)
				return false;
		} else if (!camp.equals(other.camp))
			return false;
		return true;
	}
	
}
