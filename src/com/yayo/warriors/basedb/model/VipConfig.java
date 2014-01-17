package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;


@Resource
public class VipConfig {

	/** VIP等级*/
	@Id
	private int level;
	/** 称号编号*/
	private String vipName;
	/** 远程仓库, 是否开启: 0- 未开启, 1- 开启 */
	private int remoteStorage;
	/** 远程商店, 是否开启: 0- 未开启, 1- 开启 */
	private int remoteShop;
//	/** VIP挂机, 是否开启: 0- 未开启, 1- 开启 */
//	private int vipTrain;
	/** 每日免费使用飞鞋数量, -1代表无限次 */
	private int dailyUseFlyShoes;
	/** 每日领取坐骑幻化丹数量  */
	private int dailyHorseReward;
	/** 每日领取日环刷新符数量 */
	private int dailyTaskReward;
	/** 打怪经验加成 */
	private float monsterExpPercent;
	/** 打坐经验加成 */
	private float meditationExpPercent;
//	/** 家将成长、悟性培养几率加成 */
//	private float petCulturePercent;
	/** 领取VIP祝福经验, 是否开启: 0-未开启, 1- 开启  */
	private int blessExperience;
	/** 日环任务经验加成 */
	private float dailyTaskExperience;
	/** 护送任务经验加成 */
	private float escortTaskExperience;
	/** 阵营任务经验加成 */
	private float campTaskExperience;
	/** vip有效时间(毫秒) */
	private long vipOutOfDateMillis;
	/** 日环任务真气加成 */
	private float dailyTaskGasPercent;
	/** 打坐真气加成 */
	private float meditationGasPercent;
	/** 试炼任务经验加成 */
	private float practiceTaskExperience;
	/** VIP每日礼包(对应礼包ID) */
	private int dailyVipGift;

	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}

	public String getVipName() {
		return vipName;
	}

	public void setVipName(String vipName) {
		this.vipName = vipName;
	}

	public int getRemoteStorage() {
		return remoteStorage;
	}

	public void setRemoteStorage(int remoteStorage) {
		this.remoteStorage = remoteStorage;
	}

	public int getRemoteShop() {
		return remoteShop;
	}

	public void setRemoteShop(int remoteShop) {
		this.remoteShop = remoteShop;
	}

//	public int getVipTrain() {
//		return vipTrain;
//	}
//
//	public void setVipTrain(int vipTrain) {
//		this.vipTrain = vipTrain;
//	}
	
	public int getDailyVipGift() {
		return dailyVipGift;
	}

	public void setDailyVipGift(int dailyVipGift) {
		this.dailyVipGift = dailyVipGift;
	}

	public int getDailyUseFlyShoes() {
		return dailyUseFlyShoes;
	}

	public void setDailyUseFlyShoes(int dailyUseFlyShoes) {
		this.dailyUseFlyShoes = dailyUseFlyShoes;
	}

	public int getDailyHorseReward() {
		return dailyHorseReward;
	}

	public void setDailyHorseReward(int dailyHorseReward) {
		this.dailyHorseReward = dailyHorseReward;
	}

	public int getDailyTaskReward() {
		return dailyTaskReward;
	}

	public void setDailyTaskReward(int dailyTaskReward) {
		this.dailyTaskReward = dailyTaskReward;
	}

	public float getMonsterExpPercent() {
		return monsterExpPercent;
	}

	public void setMonsterExpPercent(float monsterExpPercent) {
		this.monsterExpPercent = monsterExpPercent;
	}

	public float getMeditationExpPercent() {
		return meditationExpPercent;
	}

	public void setMeditationExpPercent(float meditationExpPercent) {
		this.meditationExpPercent = meditationExpPercent;
	}

//	public float getPetCulturePercent() {
//		return petCulturePercent;
//	}
//
//	public void setPetCulturePercent(float petCulturePercent) {
//		this.petCulturePercent = petCulturePercent;
//	}

	public int getBlessExperience() {
		return blessExperience;
	}

	public void setBlessExperience(int blessExperience) {
		this.blessExperience = blessExperience;
	}

	public float getDailyTaskExperience() {
		return dailyTaskExperience;
	}

	public void setDailyTaskExperience(float dailyTaskExperience) {
		this.dailyTaskExperience = dailyTaskExperience;
	}

	public float getEscortTaskExperience() {
		return escortTaskExperience;
	}

	public void setEscortTaskExperience(float escortTaskExperience) {
		this.escortTaskExperience = escortTaskExperience;
	}

	public float getCampTaskExperience() {
		return campTaskExperience;
	}

	public void setCampTaskExperience(float campTaskExperience) {
		this.campTaskExperience = campTaskExperience;
	}

	public long getVipOutOfDateMillis() {
		return vipOutOfDateMillis;
	}

	public void setVipOutOfDateMillis(long vipOutOfDateMillis) {
		this.vipOutOfDateMillis = vipOutOfDateMillis;
	}

	public float getDailyTaskGasPercent() {
		return dailyTaskGasPercent;
	}

	public void setDailyTaskGasPercent(float dailyTaskGasPercent) {
		this.dailyTaskGasPercent = dailyTaskGasPercent;
	}

	public float getMeditationGasPercent() {
		return meditationGasPercent;
	}

	public void setMeditationGasPercent(float meditationGasPercent) {
		this.meditationGasPercent = meditationGasPercent;
	}

	public float getPracticeTaskExperience() {
		return practiceTaskExperience;
	}

	public void setPracticeTaskExperience(float practiceTaskExperience) {
		this.practiceTaskExperience = practiceTaskExperience;
	}

}
