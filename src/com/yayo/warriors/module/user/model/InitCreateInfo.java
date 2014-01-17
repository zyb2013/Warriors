package com.yayo.warriors.module.user.model;

import com.yayo.warriors.module.buffer.entity.UserBuffer;
import com.yayo.warriors.module.cooltime.entity.UserCoolTime;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.meridian.entity.Meridian;
import com.yayo.warriors.module.mortal.entity.UserMortalBody;
import com.yayo.warriors.module.skill.entity.UserSkill;
import com.yayo.warriors.module.task.entity.TaskComplete;
import com.yayo.warriors.module.title.entity.PlayerTitle;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.entity.PlayerMotion;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.module.vip.entity.PlayerVip;

/**
 * 角色创建时需要初始化创建的信息
 * 
 * @author Hyint
 */
public class InitCreateInfo {

	/** 角色对象 */
	private Player player;
	
	/** 用户经脉对象 */
	private Meridian meridian;

	/** 角色战斗对象 */
	private PlayerBattle battle;
	
	/** 角色技能对象 */
	private UserSkill userSkill;

	/** 角色Buffer对象 */
	private UserBuffer userBuffer;
	
	/** 用户冷却时间 */
	private UserCoolTime coolTime;

	/** 角色移动对象 */
	private PlayerMotion playerMotion;
	
	/** 角色的地下城对象 */
	private PlayerDungeon playerDungeon;
	
	/** 用户肉身对象 */
	private UserMortalBody userMortalBody;

	/** 任务完成信息 */
	private TaskComplete taskComplete;
	
	/** 角色VIP对象 */
	private PlayerVip playerVip;
	
	/** 角色称号表 */
	private PlayerTitle playerTitle;
	
	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public Meridian getMeridian() {
		return meridian;
	}

	public void setMeridian(Meridian meridian) {
		this.meridian = meridian;
	}

	public PlayerBattle getBattle() {
		return battle;
	}

	public void setBattle(PlayerBattle battle) {
		this.battle = battle;
	}

	public UserSkill getUserSkill() {
		return userSkill;
	}

	public void setUserSkill(UserSkill userSkill) {
		this.userSkill = userSkill;
	}

	public UserBuffer getUserBuffer() {
		return userBuffer;
	}

	public void setUserBuffer(UserBuffer userBuffer) {
		this.userBuffer = userBuffer;
	}

	public UserCoolTime getCoolTime() {
		return coolTime;
	}

	public void setCoolTime(UserCoolTime coolTime) {
		this.coolTime = coolTime;
	}

	public PlayerMotion getPlayerMotion() {
		return playerMotion;
	}

	public void setPlayerMotion(PlayerMotion playerMotion) {
		this.playerMotion = playerMotion;
	}

	public PlayerDungeon getPlayerDungeon() {
		return playerDungeon;
	}

	public void setPlayerDungeon(PlayerDungeon playerDungeon) {
		this.playerDungeon = playerDungeon;
	}

	public UserMortalBody getUserMortalBody() {
		return userMortalBody;
	}

	public void setUserMortalBody(UserMortalBody userMortalBody) {
		this.userMortalBody = userMortalBody;
	}
	
	public long getPlayerId() {
		return this.player.getId();
	}
	
	public Camp getCamp() {
		return this.player.getCamp();
	}
	
	public TaskComplete getTaskComplete() {
		return taskComplete;
	}

	public void setTaskComplete(TaskComplete taskComplete) {
		this.taskComplete = taskComplete;
	}

	public PlayerVip getPlayerVip() {
		return playerVip;
	}

	public void setPlayerVip(PlayerVip playerVip) {
		this.playerVip = playerVip;
	}

	public PlayerTitle getPlayerTitle() {
		return playerTitle;
	}

	public void setPlayerTitle(PlayerTitle playerTitle) {
		this.playerTitle = playerTitle;
	}

	/**
	 * 更新初始化对象的 PlayerId
	 * 
	 * @param playerId		角色ID
	 */
	public void updateInitPlayerId() {
		this.battle.setId(this.player.getId());
		this.meridian.setId(this.player.getId());
		this.coolTime.setId(this.player.getId());
		this.userSkill.setId(this.player.getId());
		this.playerVip.setId(this.player.getId());
		this.userBuffer.setId(this.player.getId());
		this.playerTitle.setId(this.player.getId());
		this.taskComplete.setId(this.player.getId());
		this.playerMotion.setId(this.player.getId());
		this.playerDungeon.setId(this.player.getId());
		this.userMortalBody.setId(this.player.getId());
	}
	
}
