package com.yayo.warriors.module.skill.facade;

import java.util.List;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.skill.model.SkillVO;

/**
 * 用户技能ID
 * 
 * @author Hyint
 */
public interface SkillFacade {
	
	/**
	 * 列出用户技能VO列表
	 * 
	 * @param  playerId						角色ID
	 * @return {@link List<SkillVO>}		技能VO列表	
	 */
	List<SkillVO> listUserSkillVO(long playerId);
	
	/**
	 * 学习或者升级技能等级
	 * 
	 * @param  playerId						角色ID
	 * @param  userItemId					用户道具ID
	 * @param  skillId						技能ID
	 * @return {@link ResultObject}			用户技能返回值
	 */
	ResultObject<SkillVO> learnUserSkill(long playerId, long userItemId, int skillId);
}
