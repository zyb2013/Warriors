package com.yayo.warriors.module.campbattle.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;


import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.Camp;


/**
 * 阵营战场对象
 * @author jonsai
 *
 */
public class CampBattle implements Serializable, Comparable<CampBattle>{
	private static final long serialVersionUID = 7642343388234100292L;

	/** 阵营 */
	private Camp camp;

	/** 击杀BOSS血量 */
	private int bossHurtHP;
	
	/** 击杀人头数 */
	private int killPlayers;

	/** 占领据点得分 */
	private int pointScores;
	
	/** 杀人分数 */
	private int scores;

	/** 是否胜利 */
	private boolean win = false;
	
	/** 是否击杀了对方boss */
	private boolean killBoss = false;
	
	/** 开始计算阵营据点得分的时间  */
	private long startCalcPointScore = 0;
	
	/** 伤害BOSS得分 */
	private int hurtBossScores;

	/** 总分 */
	private int totalScores = 0;

	/** 是否最后一场	 */
	private boolean last = false;

	/** 申请加入的玩家 */
	private transient volatile List<UserDomain> applyPlayers = Collections.synchronizedList( new ArrayList<UserDomain>() );
	
	/** 阵营BOSS */
	private transient MonsterDomain monsterAiDomain = null;
	
	/** 已经占领的据点 */
	private transient CopyOnWriteArrayList<Integer> ownPointIds = new CopyOnWriteArrayList<Integer>();
	
	/** 进入战场的本阵营玩家 */
	private transient volatile Set<PlayerCampBattle> players = Collections.synchronizedSet( new HashSet<PlayerCampBattle>() );
	
	/**
	 * 构造一个对象
	 * @param camp
	 * @param last
	 * @return
	 */
	public static CampBattle valueOf(Camp camp, boolean last){
		CampBattle campBattle = new CampBattle();
		campBattle.camp = camp;
		campBattle.last = last;
		return campBattle;
	}
	
	/**
	 * 检查并设置计算阵营据点得分
	 * @param pointCount
	 */
	public void checkAndSetStartCalcPointScore(int pointCount){
		if( this.ownPointIds.size() > pointCount ){
			if(this.startCalcPointScore == 0){
				this.startCalcPointScore = System.currentTimeMillis();
			}
			
		} else {
			this.startCalcPointScore = 0;
		}
		
	}
	
	public int getKillPlayers() {
		return killPlayers;
	}

	public void setKillPlayers(int killPlayers) {
		this.killPlayers = killPlayers;
	}
	
	public void increaseKillPlayers(int killPlayers){
		this.killPlayers += killPlayers;
	}

	public MonsterDomain getMonsterAiDomain() {
		return monsterAiDomain;
	}

	public Set<PlayerCampBattle> getPlayers() {
		return players;
	}

	public void setPlayers(Set<PlayerCampBattle> players) {
		this.players = players;
	}

	public void setMonsterAiDomain(MonsterDomain monsterAiDomain) {
		this.monsterAiDomain = monsterAiDomain;
	}

	public CopyOnWriteArrayList<Integer> getOwnPointIds() {
		return ownPointIds;
	}

	public void setOwnPointIds(CopyOnWriteArrayList<Integer> ownPointIds) {
		this.ownPointIds = ownPointIds;
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
	
	public void increaseBossHurtHP(int bossHurtHP){
		this.bossHurtHP += bossHurtHP;
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
		this.totalScores = this.hurtBossScores + this.scores;
	}
	
	public int getHurtBossScores() {
		return hurtBossScores;
	}

	public void setHurtBossScores(int hurtBossScores) {
		this.hurtBossScores = hurtBossScores;
		this.totalScores = this.hurtBossScores + this.scores;
	}

	public int getTotalScores() {
		return totalScores;
	}

	public void setTotalScores(int totalScores) {
		this.totalScores = totalScores;
	}

	public boolean isWin() {
		return win;
	}

	public void setWin(boolean win) {
		this.win = win;
	}

	public List<UserDomain> getApplyPlayers() {
		return applyPlayers;
	}

	public void setApplyPlayers(List<UserDomain> applyPlayers) {
		this.applyPlayers = applyPlayers;
	}
	
	public long getStartCalcPointScore() {
		return startCalcPointScore;
	}

	public void setStartCalcPointScore(long startCalcPointScore) {
		this.startCalcPointScore = startCalcPointScore;
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

	
	public int compareTo(CampBattle o2) {
		CampBattle o1 = this;
		boolean win = o1.isWin();
		boolean win2 = o2.isWin();
		if(win && !win2){
			return -1;
		}else if(!win && win2){
			return 1;
		}
		int totalScores = o1.getTotalScores();
		int totalScores2 = o2.getTotalScores();
		if(totalScores > totalScores2){
			return -1;
		} else if(totalScores < totalScores2){
			return 1;
		} 
		int hurtBossScores = o1.getHurtBossScores();
		int hurtBossScores2 = o2.getHurtBossScores();
		if(hurtBossScores > hurtBossScores2){
			return -1;
		} else if(hurtBossScores < hurtBossScores2){
			return 1;
		}
		int killPlayers = o1.getKillPlayers();
		int killPlayers2 = o2.getKillPlayers();
		if(killPlayers > killPlayers2){
			return -1;
		} else if(killPlayers < killPlayers2){
			return 1;
		}
		return 0;
	}

	
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
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
		CampBattle other = (CampBattle) obj;
		if (camp == null) {
			if (other.camp != null)
				return false;
		} else if (!camp.equals(other.camp))
			return false;
		return true;
	}
	
}
