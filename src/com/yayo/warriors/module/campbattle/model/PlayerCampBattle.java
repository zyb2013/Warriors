package com.yayo.warriors.module.campbattle.model;


import com.yayo.warriors.module.campbattle.type.CampTitle;
import com.yayo.warriors.module.campbattle.type.PlayerCampBattleStatus;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.Camp;

/**
 * 角色阵营战场信息
 * @author jonsai
 *
 */
public class PlayerCampBattle implements Comparable<PlayerCampBattle>{
	
	/** 角色id */
	private long playerId;
	
	/** 角色玩家 */
	private String playerName;
	
	/** 角色等级 */
	private int level;
	
	/** 角色阵营 */
	private Camp camp;
	
	/** 击杀BOSS血量 */
	private int bossHurtHP;

	/** 击杀玩数量 */
	private int killPlayers;
	
	/** 杀人得分 */
	private int scores;
	
	/** 伤害BOSS得分 */
	private int hurtBossScores;
	
	/** 阵营官衔 */
	private CampTitle campTitle = CampTitle.NONE;
	
	/** 本阵营本次排名 */
	private int rank;
	
	/** 玩家阵营战状态 */
	private PlayerCampBattleStatus status = PlayerCampBattleStatus.NONE;
	
	/** 上届的阵营官衔 */
	private CampTitle preCampTitle = CampTitle.NONE;
	
	//----------------------
	/** 上次点击回阵营时间 */
	private long preBackTime = 0;

	/** 最后一场(本周) */
	private boolean last;
	
	/** 总分 */
	private int totalScores = 0;
	
	/**
	 * 构造一个对象
	 * @param player
	 * @param preCampTitle
	 * @return
	 */
	public static PlayerCampBattle valueOf(UserDomain userDomain, CampTitle preCampTitle, boolean last){
		PlayerCampBattle playerCampBattle = new PlayerCampBattle();
		Player player = userDomain.getPlayer();
		playerCampBattle.playerId = player.getId();
		playerCampBattle.playerName = player.getName();
		playerCampBattle.camp = player.getCamp();
		playerCampBattle.preCampTitle = preCampTitle;
		playerCampBattle.level = userDomain.getBattle().getLevel();
		playerCampBattle.last = last;
		
		return playerCampBattle;
	}
	
	/**
	 * 重置玩家战场记录
	 */
	public void reset(){
		this.bossHurtHP = 0;
		this.killPlayers = 0;
		this.totalScores = 0;
		this.scores = 0;
		this.hurtBossScores = 0;
		this.campTitle = CampTitle.NONE;
		this.rank = 0;
		this.status = PlayerCampBattleStatus.MIDWAY;
		this.preBackTime = 0;
	}
	
	public long getPreBackTime() {
		return preBackTime;
	}

	public void setPreBackTime(long preBackTime) {
		this.preBackTime = preBackTime;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public Camp getCamp() {
		return camp;
	}

	public void setCamp(Camp camp) {
		this.camp = camp;
	}

	public CampTitle getPreCampTitle() {
		return preCampTitle;
	}

	public void setPreCampTitle(CampTitle preCampTitle) {
		this.preCampTitle = preCampTitle;
	}

	public PlayerCampBattleStatus getStatus() {
		return status;
	}

	public void setStatus(PlayerCampBattleStatus status) {
		this.status = status;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
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

	public int getKillPlayers() {
		return killPlayers;
	}

	public void setKillPlayers(int killPlayers) {
		this.killPlayers = killPlayers;
	}
	
	public void increaseKillPlayers(int killPlayers){
		this.killPlayers += killPlayers;
	}

	public int getScores() {
		return scores;
	}

	public void setScores(int scores) {
		this.scores = scores;
		this.totalScores = this.hurtBossScores + this.scores;
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
		this.totalScores = this.hurtBossScores + this.scores;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public int getTotalScores() {
		return totalScores;
	}

	public void setTotalScores(int totalScores) {
		this.totalScores = totalScores;
	}

	public boolean isLast() {
		return last;
	}

	public void setLast(boolean last) {
		this.last = last;
	}

	
	public int compareTo(PlayerCampBattle o2) {
		if(this.totalScores > o2.getTotalScores()){
			return -1;
		} else if(this.totalScores < o2.getTotalScores()){
			return 1;
		} else if(this.killPlayers > o2.getKillPlayers()){
			return -1;
		} else if(this.killPlayers  < o2.getKillPlayers()){
			return 1;
		} else if(this.bossHurtHP > o2.getBossHurtHP()){
			return -1;
		} else if(this.bossHurtHP < o2.getBossHurtHP()){
			return 1;
		}
		return 0;
	}

	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (playerId ^ (playerId >>> 32));
		return result;
	}

	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlayerCampBattle other = (PlayerCampBattle) obj;
		if (playerId != other.playerId)
			return false;
		return true;
	}
	
}
