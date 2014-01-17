package com.yayo.warriors.basedb.model;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;

/**
 * 家将基础数据配置
 * @author liuyuhua
 */
@Resource
public class PetConfig {

	@Id
	private int id;
	
	/** 名字*/
	private String name;
	
	/** 模型(外观)*/
	private int model;
	
	/** 头像*/
	private int icon;
	
	/** 职业*/
	private int job;
	
	/** 是否名将*/
	private boolean famous;
	
	/** 基础配置技能 {技能ID_技能等级|技能ID_技能等级} 固定拥有*/
	private String skill;
	
	/** 公平竞争技能,随即 {技能ID_技能等级|技能ID_技能等级}*/
	private String ranSkill;
	
	/** 资质编号*/
	private int aptitudeNo;
	
	/** 解析 {@link PetConfig#ranSkill} 字段*/
	@JsonIgnore
	private Map<Integer,String> ranSkillMap = null;

	//Getter and Setter...
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getModel() {
		return model;
	}

	public void setModel(int model) {
		this.model = model;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public int getJob() {
		return job;
	}

	public void setJob(int job) {
		this.job = job;
	}

	public String getSkill() {
		Map<Integer,String> ranSkill = this.getRanSkillMap();
		if(ranSkill == null || ranSkill.isEmpty()){
			return skill;
		}
		int size = ranSkill.size();
		int no = Tools.getRandomInteger(size);
		String ranStr = ranSkill.get(no);
		if(ranStr != null){
			return skill + Splitable.ELEMENT_DELIMITER + ranStr;
		}
		return skill;
	}

	public void setSkill(String skill) {
		this.skill = skill;
	}

	public String getRanSkill() {
		return ranSkill;
	}

	public void setRanSkill(String ranSkill) {
		this.ranSkill = ranSkill;
	}
	
	public boolean isFamous() {
		return famous;
	}
	
	public void setFamous(boolean famous) {
		this.famous = famous;
	}
	
	public int getAptitudeNo() {
		return aptitudeNo;
	}

	public void setAptitudeNo(int aptitudeNo) {
		this.aptitudeNo = aptitudeNo;
	}

	/**
	 * 获取随即技能集合
	 * @return {@link Map} 随即技能对象
	 */
	private Map<Integer,String> getRanSkillMap(){
		if(this.ranSkillMap != null){
			return this.ranSkillMap;
		}
		
		synchronized (this) {
			if(this.ranSkillMap != null){
				return this.ranSkillMap;
			}
			
			this.ranSkillMap = new HashMap<Integer, String>(1);
			if(this.ranSkill == null){
				return this.ranSkillMap;
			}
			String[] skills = this.ranSkill.split(Splitable.ELEMENT_DELIMITER);
			int index = 0;
			for(String skill : skills){
				this.ranSkillMap.put(index, skill);
				index++;
			}
			return this.ranSkillMap;
		}
	}

	@Override
	public String toString() {
		return "PetConfig [id=" + id + ", name=" + name + ", model=" + model
				+ ", icon=" + icon + ", job=" + job + ", famous=" + famous
				+ ", skill=" + skill + ", ranSkill=" + ranSkill
				+ ", aptitudeNo=" + aptitudeNo + "]";
	}
}
