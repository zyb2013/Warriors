package com.yayo.warriors.module.pet.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.pet.rule.PetRule;
import com.yayo.warriors.module.pet.types.PetStatus;

@Entity
@Table(name = "userPet")
public class Pet extends BaseModel<Long> {
	private static final long serialVersionUID = -6920147837496653589L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "petId")
	private Long id;
	
	private long playerId;
	
	private int baseId;
	
    private int model;
    
    private int icon;
    
    private String name;
    
	private int energy;
	
	private int slot;
	
	@Lob
	private String skill = "";
	
	private int status = PetStatus.ACTIVE;
	
	private long startTraingTime = 0;
	
	private long totleTraingTime = 0;
	
	@Transient
	private transient volatile Map<Integer,Integer> skillMap = null;
	
	@Transient
	private transient boolean specify = false;
    
    public static Pet valueOf(long playerId,String name,int baseId,int modelId,int icon,int energy,int slot,String skill){
    	Pet pet = new Pet();
    	pet.slot = slot;
    	pet.name     = name;
    	pet.icon     = icon;
    	pet.skill = skill;
    	pet.model    = modelId;
    	pet.baseId   = baseId;
    	pet.energy   = energy;
    	pet.status = PetStatus.ACTIVE;
    	pet.playerId = playerId;
    	return pet;
    }
    
    public boolean isOverTraing(){
    	return this.totleTraingTime >= 28800;
    }
    
    public boolean isTraing(){
    	return this.startTraingTime > 0;
    }
	
	public boolean isFighting(){
		return this.status == PetStatus.FIGHTING;
	}
	
	public boolean isStatus(int status){
		return this.status == status;
	}
    
    public void decreaseEnergy(int energy){
    	this.energy = this.energy - energy;
    	this.energy = this.energy <= 0 ? 0 : this.energy;
    }
    
    public void increaseEnergy(int energy){
    	this.energy = this.energy + energy;
    	this.energy = this.energy >= PetRule.INIT_PET_ENERGY ? PetRule.INIT_PET_ENERGY : this.energy;
    }
    
	
	public Map<Integer,Integer> getSkillMap() {
		if(skillMap != null){
			return this.skillMap;
		}
		
		synchronized (this) {
			if(skillMap != null){
				return skillMap;
			}
			
			this.skillMap = new HashMap<Integer, Integer>();
			List<String[]> arrays = Tools.delimiterString2Array(this.skill);
			if(arrays == null || arrays.isEmpty()) {
				return this.skillMap;
			}
			
			for (String[] element : arrays) {
				if(element.length >= 2) {
					int skillId = Integer.parseInt(element[0]); 		
					int skillLevel = Integer.parseInt(element[1]); 		
					this.skillMap.put(skillId, skillLevel);
				}
			}
		}
		return this.skillMap;
	}
	
	public void putSkillMap(int skillId,int skillLevel) {
		this.getSkillMap().put(skillId, skillLevel);
		this.serialSkillData(); 
	}
	
	public boolean hasSkill(int skillId) {
		boolean result = false;
		Map<Integer, Integer> maps = this.getSkillMap();
		if(maps.containsKey(skillId)) {
			Integer lv = maps.get(skillId);
			result = lv != null && lv > 0;
		}
		return result;
	}
	protected void serialSkillData() {
		StringBuffer buffer = new StringBuffer();
		Map<Integer, Integer> map = this.getSkillMap();
		for (Entry<Integer, Integer> entry : map.entrySet()) {
			buffer.append(entry.getKey()).append(Splitable.ATTRIBUTE_SPLIT);
			buffer.append(entry.getValue()).append(Splitable.ELEMENT_DELIMITER);
		}

		if(buffer.length() > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		this.skill = buffer.toString();
	}
	
    
    

	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public int getBaseId() {
		return baseId;
	}

	public void setBaseId(int baseId) {
		this.baseId = baseId;
	}

	public int getModel() {
		return model;
	}

	public void setModel(int modelId) {
		this.model = modelId;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public int getEnergy() {
		return energy;
	}

	public void setEnergy(int energy) {
		this.energy = energy;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSlot() {
		return slot;
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}

	public String getSkill() {
		return skill;
	}

	public void setSkill(String skill) {
		this.skill = skill;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public boolean isSpecify() {
		return specify;
	}

	public void setSpecify(boolean specify) {
		this.specify = specify;
	}

	public long getStartTraingTime() {
		return startTraingTime;
	}

	public void setStartTraingTime(long startTraingTime) {
		this.startTraingTime = startTraingTime;
	}

	public long getTotleTraingTime() {
		return totleTraingTime;
	}

	public void setTotleTraingTime(long totleTraingTime) {
		this.totleTraingTime = totleTraingTime;
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
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pet other = (Pet) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}


}
