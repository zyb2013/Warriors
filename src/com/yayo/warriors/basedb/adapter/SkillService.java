package com.yayo.warriors.basedb.adapter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceAdapter;
import com.yayo.warriors.basedb.model.SkillConfig;
import com.yayo.warriors.basedb.model.SkillEffectConfig;
import com.yayo.warriors.basedb.model.SkillLearnConfig;
import com.yayo.warriors.type.IndexName;

/**
 * 技能配置服务器接口
 * 
 * @author Hyint
 */
@Component
public class SkillService extends ResourceAdapter {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public void initialize() {
		for (SkillConfig skillConfig : resourceService.listAll(SkillConfig.class)) {
			skillConfig.getFight2PlayerModeSet();
			this.convertSkillEffect(skillConfig);
			this.convertSkillLearns(skillConfig);
		}
	}
	
	/**
	 * 构建技能信息
	 * 
	 * @param skillId			技能ID
	 * @param skillConfig		技能配置信息
	 */
	private void convertSkillEffect(SkillConfig skillConfig) {
		List<SkillEffectConfig> skillEffects = this.listSkillEffectConfig(skillConfig.getId());
		Collections.sort(skillEffects);
		skillConfig.setSkillEffects(skillEffects);
		for (SkillEffectConfig skillEffect : skillEffects) {
			skillEffect.updateSkillConfig(skillConfig);
		}
	}

	/**
	 * 构建技能学习信息
	 * 
	 * @param skillConfig			技能配置表
	 */
	private void convertSkillLearns(SkillConfig skillConfig) {
		int skillId = skillConfig.getId();
		Map<Integer, SkillLearnConfig> skillLearnMap = new HashMap<Integer, SkillLearnConfig>(1);
		List<SkillLearnConfig> skillLearns = this.listSkillLearnConfig(skillId);
		if(skillLearns != null && !skillLearns.isEmpty()) {
			for (SkillLearnConfig skillLearn : skillLearns) {
				skillLearnMap.put(skillLearn.getLevel(), skillLearn);
			}
		}
		skillConfig.setSkillLearns(skillLearnMap);
	}
	
	/**
	 * 列出技能效果配置对象
	 * 
	 * @param  skillId				技能ID
	 * @return {@link List}			技能效果配置对象列表
	 */
	public List<SkillEffectConfig> listSkillEffectConfig(int skillId) {
		return resourceService.listByIndex(IndexName.SKILL_EFFECT_SKILLID, SkillEffectConfig.class, skillId);
	}
	
	/**
	 * 技能配置对象
	 * 
	 * @param  skillId				技能ID
	 * @return {@link List}			技能学习配置
	 */
	public List<SkillLearnConfig> listSkillLearnConfig(int skillId) {
		return resourceService.listByIndex(IndexName.SKILL_LEARN_SKILLID, SkillLearnConfig.class, skillId);
	}
	
	/**
	 * 获得效果ID列表.
	 * 
	 * @param  skillEffectTypes		技能效果类型可变参
	 * @return {@link Collection}	效果ID列表
	 */
	public Set<Integer> getEffectIdByBufferType(int...skillEffectTypes) {
		Set<Integer> set = new HashSet<Integer>(2);
		String indexName = IndexName.SKILLEFFECT_EFFECT_TYPE;
		Class<SkillEffectConfig> clazz = SkillEffectConfig.class;
		for (int effectType : skillEffectTypes) {
			try {
				List<Integer> bufferIdList = resourceService.listIdByIndex(indexName, clazz, Integer.class, effectType);
				if(bufferIdList != null && !bufferIdList.isEmpty()) {
					set.addAll(bufferIdList);
				}
			} catch (Exception e) {
				logger.debug("{}", e);
			}
		}
		return set;
	}
	
	/**
	 * 根据职业获得技能配置对象
	 * 
	 * @param  classify			技能分类
	 * @return {@link List}		技能列表
	 */
	public List<SkillConfig> getSkillByClassify(int classify) {
		return resourceService.listByIndex(IndexName.SKILL_CLASSIFY, SkillConfig.class, classify);
	}
	
	/**
	 * 查询技能基础对象
	 * 
	 * @param  skillId				技能ID
	 * @return {@link SkillConfig}	技能基础对象
	 */
	public SkillConfig getSkillConfig(int skillId) {
		return resourceService.get(skillId, SkillConfig.class);
	}
	
	/**
	 * 技能效果配置对象
	 * 
	 * @param  skillEffectId				技能效果配置对象ID
	 * @return {@link SkillEffectConfig}	技能效果配置对象 
	 */
	public SkillEffectConfig getSkillEffectConfig(int skillEffectId) {
		return resourceService.get(skillEffectId, SkillEffectConfig.class);
	}
}
