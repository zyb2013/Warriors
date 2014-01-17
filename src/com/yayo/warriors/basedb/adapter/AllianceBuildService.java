package com.yayo.warriors.basedb.adapter;

import java.util.List;

import org.springframework.stereotype.Component;
import com.yayo.common.basedb.ResourceAdapter;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.model.AllianceBuildConfig;
import com.yayo.warriors.basedb.model.AllianceDivineConfig;
import com.yayo.warriors.basedb.model.AllianceSkillConfig;
import com.yayo.warriors.type.IndexName;

/**
 * 帮派建筑物适配器 
 * @author liuyuhua
 */
@Component
public class AllianceBuildService extends ResourceAdapter {
	
	@Override
	public void initialize() {
	}
	
	
	/**
	 * 获取等到奖励的 道具基础ID
	 * @return
	 */
	public AllianceDivineConfig getDivineAlliance(){
		List<AllianceDivineConfig> configs = (List<AllianceDivineConfig>) resourceService.listAll(AllianceDivineConfig.class);
		if(configs == null || configs.isEmpty()){
			return null;
		}
		
		AllianceDivineConfig allianceDivineConfig = configs.get(0);
		if(allianceDivineConfig == null){
			return null;
		}
		
		int totalRate = 0;
		int fullValue = allianceDivineConfig.getFullRate();
		int ran = Tools.getRandomInteger(fullValue);
		for (AllianceDivineConfig config : configs) {
			totalRate += config.getRate();
			if(ran < totalRate) {
				return config;
			}
		}
		
		return null;
	}
	
	
	/**
	 * 获取帮派配置对象
	 * @param buildType                     建筑物类型
	 * @param level                         等级
	 * @return {@link AllianceBuildConfig}  帮派配置对象
	 */
	public AllianceBuildConfig getAllianceBuildConfig(int buildType,int level){
		return resourceService.getByUnique(IndexName.ALLIANCEBUILD_TYPE_LEVEL, AllianceBuildConfig.class, buildType, level);
	}
	
	/**
	 * 获取帮派技能配置对象
	 * @param skillId                      技能ID
	 * @param skillLevel                   技能等级
	 * @return {@link AllianceSkillConfig} 帮派技能配置对象
	 */
	public AllianceSkillConfig getAllianceSkillConfig(int skillId,int skillLevel){
		return resourceService.getByUnique(IndexName.ALLIANCE_SKILL_LEVEL, AllianceSkillConfig.class, skillId, skillLevel);
	}

}
