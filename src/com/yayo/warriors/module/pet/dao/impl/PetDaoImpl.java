package com.yayo.warriors.module.pet.dao.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import com.yayo.common.db.dao.impl.CommonDaoImpl;
import com.yayo.warriors.module.pet.dao.PetDao;
import com.yayo.warriors.module.pet.entity.Pet;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.pet.types.PetStatus;

@Service
public class PetDaoImpl extends CommonDaoImpl implements PetDao{

	public boolean createPetInfo(Pet pet, PetBattle battle) {
		if(pet != null && battle != null){
			try {
				save(pet);
				battle.setId(pet.getId());
				save(battle);
			} catch (Exception e) {
				return false;
			}
		}
		return true;
	}

	
	@SuppressWarnings("unchecked")
	public List<Long> getPlayerPetIds(long playerId) {
		Criteria criteria = createCriteria(Pet.class);
		criteria.add(Restrictions.eq("playerId", playerId));
		criteria.add(Restrictions.ne("status", PetStatus.DROP));
		criteria.add(Restrictions.ne("status", PetStatus.UNDRAW));
		criteria.setProjection(Projections.id());
		return criteria.list();
	}

	
	@SuppressWarnings("unchecked")
	public List<Integer> getPlayerAllIds(long playerId) {
		Criteria criteria = createCriteria(Pet.class);
		criteria.add(Restrictions.eq("playerId", playerId));
		criteria.setProjection(Projections.property("baseId"));
		return criteria.list();
	}

	
	@SuppressWarnings("unchecked")
	public List<Long> getPlayerUnDrawPet(long playerId) {
		Criteria criteria = createCriteria(Pet.class);
		criteria.add(Restrictions.eq("playerId", playerId));
		criteria.add(Restrictions.eq("status", PetStatus.UNDRAW));
		criteria.setProjection(Projections.id());
		return criteria.list();
	}

}
