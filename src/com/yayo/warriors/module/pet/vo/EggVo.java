package com.yayo.warriors.module.pet.vo;

import java.io.Serializable;

public class EggVo implements Serializable{
	private static final long serialVersionUID = 1L;

	private long id;
	
	private int baseId;
	
	private int quality;
	
	private String skill;
	
	private boolean specify;
	
	public static EggVo valueOf(long id,int baseId,int quality,String skill,boolean specify){
		EggVo vo = new EggVo();
		vo.id = id;
		vo.baseId = baseId;
		vo.quality = quality;
		vo.skill = skill;
		vo.specify = specify;
		return vo;
	}
	
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public int getBaseId() {
		return baseId;
	}

	public void setBaseId(int baseId) {
		this.baseId = baseId;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public String getSkill() {
		return skill;
	}

	public void setSkill(String skill) {
		this.skill = skill;
	}

	public boolean isSpecify() {
		return specify;
	}

	public void setSpecify(boolean specify) {
		this.specify = specify;
	}

	@Override
	public String toString() {
		return "EggVo [id=" + id + ", baseId=" + baseId + ", quality="
				+ quality + ", skill=" + skill + ", specify=" + specify + "]";
	}

}
