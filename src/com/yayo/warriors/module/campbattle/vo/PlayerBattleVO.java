package com.yayo.warriors.module.campbattle.vo;

import java.io.Serializable;
import java.util.Date;

import com.yayo.warriors.module.campbattle.entity.PlayerCampBattleHistory;
import com.yayo.warriors.module.campbattle.model.PlayerCampBattle;
import com.yayo.warriors.module.campbattle.type.CampTitle;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.module.user.type.Job;

/**
 * 玩家战场信息VO
 * @author jonsai
 *
 */
public class PlayerBattleVO implements Serializable{
	private static final long serialVersionUID = 3712598031085512124L;
	
	/** 角色id */
	private long playerId;
	
	/** 角色名 */
	private String playerName;
	
	/** 击杀玩家数量 */
	private int killPlayers;
	
	/** 攻击BOSS得分  */
	private int hurtBossScores;
	
	/** 个人总得分  */
	private int totalScores;

	/** 阵营官衔  */
	private CampTitle campTitle;
	
	/** 阵营战场日期  */
	private Date battleDate;
	
	/** 角色等级  */
	private int level;
	
	/** 角色职业  */
	private Job job;
	
	/** 阵营  */
	private Camp camp;
	
	/** 本场排名 */
	private int rank;
	
	/** 帮派名 */
	private String allianceName;
	
	//-------------------------------
	/**
	 * 构造一个新的对象
	 * @param playerBattle
	 * @return
	 */
	public static PlayerBattleVO valueOf(PlayerCampBattle playerCampBattle, Date battleDate){
		PlayerBattleVO playerBattleVO = new PlayerBattleVO();
		synchronized (playerCampBattle) {
			playerBattleVO.playerId = playerCampBattle.getPlayerId();
			playerBattleVO.playerName = playerCampBattle.getPlayerName();
			playerBattleVO.killPlayers = playerCampBattle.getKillPlayers();
			playerBattleVO.totalScores = playerCampBattle.getTotalScores();
			playerBattleVO.battleDate = battleDate;
			playerBattleVO.hurtBossScores = playerCampBattle.getHurtBossScores();
//			playerBattleVO.campTitle = playerCampBattle.getCampTitle();
		}
		
		return playerBattleVO;
	}
	
	/**
	 * 构造一个新的对象
	 * @param playerBattle
	 * @return
	 */
	public static PlayerBattleVO valueOf(UserDomain userDomain, PlayerCampBattleHistory playerCampBattleHistory){
		PlayerBattleVO playerBattleVO = new PlayerBattleVO();
		playerBattleVO.playerId = playerCampBattleHistory.getPlayerId();
		playerBattleVO.playerName = userDomain != null ? userDomain.getPlayer().getName() : String.valueOf(playerBattleVO.playerId);
		playerBattleVO.killPlayers = playerCampBattleHistory.getKillPlayers();
		playerBattleVO.totalScores = playerCampBattleHistory.getScores();
		playerBattleVO.hurtBossScores = playerCampBattleHistory.getHurtBossScores();
		playerBattleVO.camp = playerCampBattleHistory.getCamp();
		playerBattleVO.campTitle = playerCampBattleHistory.getCampTitle();
		playerBattleVO.job = userDomain.getBattle().getJob();
		playerBattleVO.level = playerCampBattleHistory.getLevel();
		
		return playerBattleVO;
	}
	
	public String getAllianceName() {
		return allianceName;
	}

	public void setAllianceName(String allianceName) {
		this.allianceName = allianceName;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public int getKillPlayers() {
		return killPlayers;
	}

	public void setKillPlayers(int killPlayers) {
		this.killPlayers = killPlayers;
	}

	public int getHurtBossScores() {
		return hurtBossScores;
	}

	public void setHurtBossScores(int hurtBossScores) {
		this.hurtBossScores = hurtBossScores;
	}

	public int getTotalScores() {
		return totalScores;
	}

	public void setTotalScores(int totalScores) {
		this.totalScores = totalScores;
	}

	public CampTitle getCampTitle() {
		return campTitle;
	}

	public void setCampTitle(CampTitle campTitle) {
		this.campTitle = campTitle;
	}

	public Date getBattleDate() {
		return battleDate;
	}

	public void setBattleDate(Date battleDate) {
		this.battleDate = battleDate;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (playerId ^ (playerId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlayerBattleVO other = (PlayerBattleVO) obj;
		if (playerId != other.playerId)
			return false;
		return true;
	}
	
}
