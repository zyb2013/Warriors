package com.yayo.warriors.module.monster.model;

import com.yayo.common.utility.Splitable;
import com.yayo.warriors.basedb.model.SkillConfig;
import com.yayo.warriors.common.helper.MonsterHelper;

/**
 * 
 * @author haiming
 *
 */
public class SkillMonitor {

	/** 技能id */
	private int skillId ;
	/** 技能等级  */
	private int skillLevel ;
	/** hp血量百分比区间 */
	private int[] hpCondition = new int[2];
	/** 基础数据 */
	private SkillConfig skillConfig ;
	
	public SkillMonitor(){}
	
	public SkillMonitor(String skillInfo) {
		//解析1000_1_[100-1]
		String[] skillSpiteInfo = skillInfo.split(Splitable.ATTRIBUTE_SPLIT);
		if(skillSpiteInfo.length < 3) {
			return ;
		}
		this.skillId = Integer.parseInt(skillSpiteInfo[0]);
		this.skillLevel = Integer.parseInt(skillSpiteInfo[1]);
		
		//解析[100-1]
		String hpCon_tmp = skillSpiteInfo[2].replaceAll("\\[|\\]", "");
		
		//解析100-1
		String[] hpCon_tmps = hpCon_tmp.split("-");
		
		hpCondition[0] = Integer.parseInt(hpCon_tmps[0]);
		hpCondition[1] = Integer.parseInt(hpCon_tmps[1]);
		
		this.skillConfig = MonsterHelper.getSkillConfig(skillId);
	}

	public int getSkillId() {
		return this.skillId;
	}
	
	public int getSkillLevel(){
		return this.skillLevel ;
	}

	/**
	 * 是否符合条件
	 * @param currentHpPercent
	 * @return
	 */
	public boolean hitContidion(int currentHpPercent) {
		return (hpCondition[0] - currentHpPercent) * (hpCondition[1] - currentHpPercent) <= 0;
	}

	public SkillConfig getSkillConfig(){
		return this.skillConfig ;
	}
	
	public int getSkillAttackDistance(){
		if(this.skillConfig == null){
			return 1 ;
		}
		return this.skillConfig.getDistance() ;
	}
}
