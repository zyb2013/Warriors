package com.yayo.warriors.module.skill.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.module.skill.model.SkillVO;

/**
 * 用户武功技能
 * 
 * @author Hyint
 */
@Entity
@Table(name="userSkill")
public class UserSkill extends BaseModel<Long> {
	private static final long serialVersionUID = 5399286062554496694L;
	
	/** 角色ID */
	@Id
	@Column(name="playerId")
	private Long id;
	
	/** 
	 * 主动技能 
	 * 
	 * <pre>格式: 技能ID_技能的等级(层数)_熟练度|技能ID_技能的等级(层数)_熟练度|...</pre>
	 */
	@Lob
	private String activeSkill = "";

	/** 
	 * 被动技能 
	 * 
	 * <pre>格式: 技能ID_技能的等级(层数)|技能ID_技能的等级(层数)|...</pre>
	 */
	@Lob
	private String passiveSkill = "";
	
	/** 主动技能等级和被动技能等级之和，主要用于武功等级排行 */
	private int skillLevels = 0;
	
	/** 主动技能集合 { 技能ID,  技能对象VO } */
	@Transient
	private transient volatile Map<Integer, SkillVO> activeSkillMap = null;

	/** 被动技能集合 { 技能ID,  技能对象VO } */
	@Transient
	private transient volatile Map<Integer, SkillVO> passiveSkillMap = null;
	
	/** 主动技能等级之和 */
	@Transient
	private transient volatile int activeSkillLevel = 0;
	
	/** 被动技能等级之和 */
	@Transient
	private transient volatile int passiveSkillLevel = 0;
	
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getActiveSkill() {
		return activeSkill;
	}

	public void setActiveSkill(String activeSkill) {
		this.activeSkillMap = null;
		this.activeSkill = activeSkill;
	}

	public int getSkillLevels() {
		return skillLevels;
	}

	public void setSkillLevels(int skillLevels) {
		this.skillLevels = skillLevels;
	}

	public String getPassiveSkill() {
		return passiveSkill;
	}

	public void setPassiveSkill(String passiveSkill) {
		this.passiveSkillMap = null;
		this.passiveSkill = passiveSkill;
	}

	/**
	 * 查询主动技能集合
	 * 
	 * @return	{@link Map}	
	 */
	public Map<Integer, SkillVO> getActiveSkillMap() {
		if(activeSkillMap != null){
			return activeSkillMap;
		}
		
		synchronized (this) {
			if(this.activeSkillMap != null) {
				return this.activeSkillMap;
			}
			
			int levels = 0;
			this.activeSkillMap = new HashMap<Integer, SkillVO>();
			if(!StringUtils.isBlank(this.activeSkill)) {
				String[] array = this.activeSkill.split(Splitable.ELEMENT_SPLIT);
				for (String element : array) {
					SkillVO skillVO = SkillVO.valueOf(element);
					if(skillVO != null) {
						this.activeSkillMap.put(skillVO.getId(), skillVO);
						levels += skillVO.getLevel(); 
					}
				}
			}
			this.activeSkillLevel = levels;
			this.skillLevels = this.activeSkillLevel + this.passiveSkillLevel;
		}
		
		return this.activeSkillMap;
	}

	/**
	 * 查询被动技能集合
	 * 
	 * @return	{@link Map}	
	 */
	public Map<Integer, SkillVO> getPassiveSkillMap() {
		if(passiveSkillMap != null){
			return passiveSkillMap;
		}
		
		synchronized (this) {
			if(this.passiveSkillMap != null) {
				return this.passiveSkillMap;
			}
			
			int levels = 0;
			this.passiveSkillMap = new HashMap<Integer, SkillVO>();
			if(!StringUtils.isBlank(this.passiveSkill)) {
				String[] array = this.passiveSkill.split(Splitable.ELEMENT_SPLIT);
				for (String element : array) {
					SkillVO skillVO = SkillVO.valueOf(element);
					if(skillVO != null) {
						this.passiveSkillMap.put(skillVO.getId(), skillVO);
						levels += skillVO.getLevel(); 
					}
				}
			}
			this.passiveSkillLevel = levels;
			this.skillLevels = this.activeSkillLevel + this.passiveSkillLevel;
		}
		
		return this.passiveSkillMap;
	}
	
	/**
	 * 查询技能VO
	 * 
	 * @param  skillId				技能ID
	 * @param  isActive				是否主动技能. true-主动技能, false-被动技能
	 * @return {@link SkillVO}		
	 */
	public SkillVO getSkillVO(int skillId, boolean isActive) {
		return isActive ? this.getActiveSkillMap().get(skillId) : this.getPassiveSkillMap().get(skillId);
	}
	
	/**
	 * 是否已经学习这个技能
	 * 
	 * @param  skillId				技能ID
	 * @param  isActive				是否主动技能. true-主动技能, false-被动技能
	 * @return {@link Boolean}		true-已学习, false-未学习
	 */
	public boolean hasSkill(int skillId, boolean isActive) {
		return isActive ? getActiveSkillMap().containsKey(skillId) : getPassiveSkillMap().containsKey(skillId);
	}
	
	/**
	 * 查询技能的等级
	 * 
	 * @param  skillId				技能的等级
	 * @param  isActive				是否主动技能, true-主动技能, false-被动技能
	 * @return {@link Integer}		技能等级
	 */
	public int getSkillLevel(int skillId, boolean isActive) {
		SkillVO skillVO = isActive ? this.getActiveSkillMap().get(skillId) : this.getPassiveSkillMap().get(skillId);
		return skillVO == null ? 0 : skillVO.getLevel();
	}
	
	/**
	 * 增加等级
	 * 
	 * @param skillId				技能ID
	 * @param addLevel				增加的技能等级
	 * @param isActive				是否主动技能
	 */
	public void addSkill(int skillId, int addLevel, boolean isActive) {
		Map<Integer, SkillVO> maps = null;
		if(isActive) {
			maps = this.getActiveSkillMap();
		} else {
			maps = this.getPassiveSkillMap();
		}
		
		SkillVO skillVO = maps.get(skillId);
		if(skillVO == null) {
			skillVO = SkillVO.valueOf(skillId, 0);
			maps.put(skillId, skillVO);
		}
		skillVO.increaseLevel(addLevel);
	}
	
	/**
	 * 修改技能等级
	 * @param skillId
	 * @param level
	 * @param isActive
	 */
	public void updateSkillLevel(int skillId, int level, boolean isActive){
		Map<Integer, SkillVO> maps = null;
		if(isActive) {
			maps = this.getActiveSkillMap();
		} else {
			maps = this.getPassiveSkillMap();
		}
		
		SkillVO skillVO = maps.get(skillId);
		if(skillVO == null) {
			skillVO = SkillVO.valueOf(skillId, 0);
			maps.put(skillId, skillVO);
		}
		skillVO.setLevel(level);
	}

	/**
	 * 修改技能等级
	 * @param skillId
	 * @param level
	 * @param isActive
	 */
	public void removeSkill(int skillId, boolean isActive){
		Map<Integer, SkillVO> maps = null;
		if(isActive) {
			maps = this.getActiveSkillMap();
		} else {
			maps = this.getPassiveSkillMap();
		}
		maps.remove(skillId);
	}
	
	/**
	 * 更新技能集合
	 * 
	 * @param isActive
	 */
	public void updateUserSkillInfos(boolean isActive) {
		StringBuilder builder = new StringBuilder();
		Map<Integer, SkillVO> skillMap = isActive ? this.getActiveSkillMap() : this.getPassiveSkillMap();
		Collection<SkillVO> skillVOList = skillMap.values();
		int levels = 0;
		for (SkillVO skillVO : skillVOList) {
			builder.append(skillVO).append(Splitable.ELEMENT_DELIMITER);
			levels += skillVO.getLevel();
		}
		if(builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		if(isActive) {
			this.activeSkill = builder.toString();
			this.activeSkillLevel = levels;
		} else {
			this.passiveSkill = builder.toString();
			this.passiveSkillLevel = levels;
		}
		this.skillLevels = this.activeSkillLevel + this.passiveSkillLevel;
	}
	
	/**
	 * 查询技能VO列表
	 * 
	 * @param  isActive		是否主动技能. null-全部, true-主动技能, false-被动技能
	 * @return {@link List}	技能VO列表
	 */
	public List<SkillVO> getSkillVOList(Boolean isActive) {
		List<SkillVO> skillVOList = new ArrayList<SkillVO>();
		if(isActive == null) {
			skillVOList.addAll(this.getActiveSkillMap().values());
			skillVOList.addAll(this.getPassiveSkillMap().values());
		} else if(isActive) {
			skillVOList.addAll(this.getActiveSkillMap().values());
		} else {
			skillVOList.addAll(this.getPassiveSkillMap().values());
		}
		return skillVOList;
	}

	@Override
	public String toString() {
		return "UserSkill [id=" + id + ", activeSkill=" + activeSkill + ", passiveSkill=" + passiveSkill + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserSkill other = (UserSkill) obj;
		return id != null && other.id != null && id.equals(other.id);
	}
	
	/**
	 * 构建用户技能对象
	 * 
	 * @param  playerId				角色ID
	 * @return {@link UserSkill}	用户技能对象
	 */
	public static UserSkill valueOf(long playerId) {
		UserSkill userSkill = new UserSkill();
		userSkill.id = playerId;
		return userSkill;
	}
	
	/**
	 * 重置技能VO集合
	 */
	public void resetSkillVOMap() {
		this.activeSkillMap = null;
		this.passiveSkillMap = null;
	}
}
