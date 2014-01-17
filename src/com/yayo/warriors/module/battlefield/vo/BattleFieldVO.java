package com.yayo.warriors.module.battlefield.vo;

import java.io.Serializable;

import com.yayo.warriors.module.battlefield.entity.PlayerBattleField;

/**
 * 战场vo对象
 * @author jonsai
 *
 */
public class BattleFieldVO implements Serializable {
	private static final long serialVersionUID = 4674338592418644909L;

	/** 杀人数 */
	private int killPlayers;
	
	/** 死亡次数 */
	private int deaths;
	
	/** 战斗荣誉 */
	private int fightHonor;
	
	/** 采集荣誉 */
	private int collectHonor;

	/** 是否战场结束 */
	private boolean battleOver;

	/** 0-无意义，1-击杀，2-被杀 */
	private int type;
	
	/** 击杀/或角色名 */
	private String targetName;
	
	//----------------------------------------------------------
	/**
	 * 构造一个BattleFieldVO
	 * @param playerBattleField
	 * @return
	 */
	public static BattleFieldVO valueOf(PlayerBattleField playerBattleField){
		BattleFieldVO battleFieldVO = new BattleFieldVO();
		battleFieldVO.killPlayers = playerBattleField.getKillPlayers();
		battleFieldVO.deaths = playerBattleField.getDeaths();
		battleFieldVO.fightHonor = playerBattleField.getFightHonor();
		battleFieldVO.collectHonor = playerBattleField.getCollectHonor();
		return battleFieldVO;
	}
	
	public static BattleFieldVO valueOf(PlayerBattleField playerBattleField, int type, String targetName){
		BattleFieldVO battleFieldVO = valueOf(playerBattleField);
		battleFieldVO.type = type;
		battleFieldVO.targetName = targetName;
		return battleFieldVO;
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

	public void setCollectHonor(int collectHonor) {
		this.collectHonor = collectHonor;
	}

	public boolean isBattleOver() {
		return battleOver;
	}

	public void setBattleOver(boolean battleOver) {
		this.battleOver = battleOver;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
}
