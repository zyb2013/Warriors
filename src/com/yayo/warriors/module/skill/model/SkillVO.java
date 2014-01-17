package com.yayo.warriors.module.skill.model;

import static com.yayo.common.utility.Splitable.*;
import java.io.Serializable;

import org.apache.commons.lang.StringUtils;


/**
 * 技能VO(服务器端与客户端均公用此实体)
 * 
 * @author Hyint
 */
public class SkillVO implements Serializable {
	private static final long serialVersionUID = 3083566375689621722L;

	/** 技能ID */
	private int id;
	
	/** 技能等级 */
	private int level;
	
	/** 熟练度 */
	private int skilled;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void increaseLevel(int level) {
		this.level += level;
	}
	
	public void decreaseLevel(int level) {
		this.level = Math.max(0, this.level - level);
	}

	public int getSkilled() {
		return skilled;
	}

	public void setSkilled(int skilled) {
		this.skilled = skilled;
	}
	
	/**
	 * 构建技能VO
	 * 
	 * @param  skillId			技能ID
	 * @param  level			技能等级
	 * @return {@link SkillVO}	技能VO
	 */
	public static SkillVO valueOf(int skillId, int level) {
		SkillVO skillVO = new SkillVO();
		skillVO.id = skillId;
		skillVO.level = level;
		return skillVO;
	}

	/**
	 * 构建技能VO
	 * 
	 * @param  skillId			技能ID
	 * @param  level			技能等级
	 * @param  skilled			技能熟练度
	 * @return {@link SkillVO}	技能VO
	 */
	public static SkillVO valueOf(int skillId, int level, int skilled) {
		SkillVO skillVO = new SkillVO();
		skillVO.id = skillId;
		skillVO.level = level;
		skillVO.skilled = skilled;
		return skillVO;
	}
	
	/**
	 * 构建技能等级VO
	 * 
	 * @param  skill			技能等级字符串.格式:技能ID_技能等级
	 * @return {@link SkillVO}	技能VO
	 */
	public static SkillVO valueOf(String skill) {
		if(StringUtils.isBlank(skill)){
			return null;
		}
		try {
			String[] array = skill.split(ATTRIBUTE_SPLIT);
			int skillId = Integer.valueOf(array[0]);
			int level = Integer.valueOf(array[1]);
			int skilled = array.length >= 3 ? Integer.valueOf(array[2]) : 0;
			return valueOf(skillId, level, skilled);
		} catch (Exception e) {
			return null;
		}
	}

	
	@Override
	public String toString() {
		return id + ATTRIBUTE_SPLIT + level + (skilled > 0 ? ATTRIBUTE_SPLIT + skilled : "");
	}
}
