package com.yayo.warriors.module.pet.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.yayo.warriors.module.pet.entity.Pet;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.pet.vo.EggVo;


public class PetZoom implements Serializable{
	private static final long serialVersionUID = 1139931098741247343L;
	
	private long playerId;

	private Map<Long,PetDomain> petDomains = new HashMap<Long, PetDomain>(2);

	public static PetZoom valueOf(long playerId){
		PetZoom zoom = new PetZoom();
		zoom.playerId = playerId;
		return zoom;
	}
	
	public void addPetDomain(PetDomain petDomain){
		if(petDomain != null){
			petDomain.getBattle();
			this.petDomains.put(petDomain.getId(), petDomain);
		}
	}
	public PetDomain removePetDomain(long petId){
		return this.petDomains.remove(petId);
	}
	
	public int size(){
		return this.petDomains.size();
	}
	
	public List<EggVo> getEggVo(){
		int size = this.size();
		if(size <= 0){
			return new ArrayList<EggVo>(0);
		}
		
		List<EggVo> vos = new ArrayList<EggVo>();
		Iterator<Entry<Long,PetDomain>> iterator = petDomains.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<Long,PetDomain> entry = iterator.next();
			PetDomain petDomain = entry.getValue();
			if(petDomain == null){
				continue;
			}
			Pet pet = petDomain.getPet();
			PetBattle petBattle = petDomain.getBattle();
			if(pet == null || petBattle == null){
				continue;
			}
			
			long petId = pet.getId();
			int baseId = pet.getBaseId();
			int quality = petBattle.getQuality();
			String skill = pet.getSkill();
			boolean specify = pet.isSpecify();
			EggVo eggVo = EggVo.valueOf(petId, baseId, quality, skill, specify);
			vos.add(eggVo);
		}
		
		return vos;
	} 
	
	
	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public Map<Long, PetDomain> getPetDomains() {
		return petDomains;
	}

	public void setPetDomains(Map<Long, PetDomain> petDomains) {
		this.petDomains = petDomains;
	}

	@Override
	public String toString() {
		return "PetZoom [playerId=" + playerId + ", petDomains=" + petDomains
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (playerId ^ (playerId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PetZoom other = (PetZoom) obj;
		if (playerId != other.playerId)
			return false;
		return true;
	}
}
