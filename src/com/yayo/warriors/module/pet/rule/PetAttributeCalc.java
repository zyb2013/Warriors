package com.yayo.warriors.module.pet.rule;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.basedb.model.PetQualityAttributeConfig;
import com.yayo.warriors.basedb.model.PetSavvyAttributeConfig;
import com.yayo.warriors.common.helper.FormulaHelper;
import com.yayo.warriors.module.fight.type.FightCasting;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.pet.types.PetJob;
import com.yayo.warriors.module.user.model.ConcurrentFightable;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.type.FormulaKey;

@Component
public class PetAttributeCalc {
	
	@Autowired
	private ResourceService resourceService;
	
	
	public void calcAttribute(PetBattle petBattle) {
		if(petBattle == null){
			return;
		}
		
		if(!petBattle.isFlushable()){
			return;
		}

		long petId = petBattle.getId();
		int quality = petBattle.getQuality();
		int level = petBattle.getLevel();
		PetJob job = petBattle.getJob();
		if(job == null){
			return;
		}
		
		Fightable levelFightable = this.calcLevelFightable(level,quality,job);
		Fightable qualityFightable = this.calcQualityFightable(level, quality, job);
		ConcurrentFightable fightable = petBattle.getAttributes();
		
		if(fightable == null){
			return;
		}
		
		
		Map<Object, Integer> tmpAttr = new HashMap<Object, Integer>(10);
		int hit =        levelFightable.getAttribute(AttributeKeys.HIT) + 
                         qualityFightable.getAttribute(AttributeKeys.HIT);
		
		int dodge =      levelFightable.getAttribute(AttributeKeys.DODGE) + 
                         qualityFightable.getAttribute(AttributeKeys.DODGE);
		
		
		int theurgyattack = levelFightable.getAttribute(AttributeKeys.THEURGY_ATTACK) + 
                             qualityFightable.getAttribute(AttributeKeys.THEURGY_ATTACK);
		
				
	    int theurgydefense = levelFightable.getAttribute(AttributeKeys.THEURGY_DEFENSE) + 
                              qualityFightable.getAttribute(AttributeKeys.THEURGY_DEFENSE);
		
	    int theurgycritical =levelFightable.getAttribute(AttributeKeys.THEURGY_CRITICAL) + 
                              qualityFightable.getAttribute(AttributeKeys.THEURGY_CRITICAL);
	    
	    int physicalattack = levelFightable.getAttribute(AttributeKeys.PHYSICAL_ATTACK) + 
                              qualityFightable.getAttribute(AttributeKeys.PHYSICAL_ATTACK);		    
	    
	    
	    int physicaldefense = levelFightable.getAttribute(AttributeKeys.PHYSICAL_DEFENSE) + 
                               qualityFightable.getAttribute(AttributeKeys.PHYSICAL_DEFENSE);		
	    
	    int physicalcritical = levelFightable.getAttribute(AttributeKeys.PHYSICAL_CRITICAL) + 
                                qualityFightable.getAttribute(AttributeKeys.PHYSICAL_CRITICAL);	
	    
        int hp_max = levelFightable.getAttribute(AttributeKeys.HP_MAX) + 
                     qualityFightable.getAttribute(AttributeKeys.HP_MAX);	
        
        
        int mpMax = levelFightable.getAttribute(AttributeKeys.MP_MAX) + 
                    qualityFightable.getAttribute(AttributeKeys.MP_MAX);	
        
        tmpAttr.put(AttributeKeys.HP_MAX, hp_max);
        tmpAttr.put(AttributeKeys.MP, mpMax);
        tmpAttr.put(AttributeKeys.MP_MAX, mpMax);
        tmpAttr.put(AttributeKeys.PHYSICAL_ATTACK, physicalattack);
        tmpAttr.put(AttributeKeys.THEURGY_ATTACK, theurgyattack);
        tmpAttr.put(AttributeKeys.PHYSICAL_DEFENSE, physicaldefense);
        tmpAttr.put(AttributeKeys.THEURGY_DEFENSE, theurgydefense);
        tmpAttr.put(AttributeKeys.PHYSICAL_CRITICAL, physicalcritical);
        tmpAttr.put(AttributeKeys.THEURGY_CRITICAL, theurgycritical);
        tmpAttr.put(AttributeKeys.HIT, hit);
        tmpAttr.put(AttributeKeys.DODGE, dodge);
	    
	    if(petBattle.getFlushable() == Flushable.FLUSHABLE_LEVEL_UP){
		    tmpAttr.put(AttributeKeys.HP, hp_max);
	    }else{
	    	tmpAttr.put(AttributeKeys.HP, petBattle.getHp());
	    }
	    
	    int fighting = 0;
	    if(job.getCasting() == FightCasting.PHYSICAL){
	    	fighting = FormulaHelper.invoke(FormulaKey.PET_WAIGONG_FIGHTING, physicalattack, physicaldefense, physicalcritical, hit, dodge, hp_max, theurgydefense, mpMax).intValue();
	    } else if(job.getCasting() == FightCasting.THEURGY){
	    	fighting = FormulaHelper.invoke(FormulaKey.PET_NEIGONG_FIGHTING, theurgyattack, theurgydefense, theurgycritical, hit, dodge, hp_max, physicaldefense, mpMax).intValue();
	    }
	    tmpAttr.put(AttributeKeys.FIGHT_TOTAL_CAPACITY, fighting);
		/** end 计算家将属性*/
		
		ChainLock lock = LockUtils.getLock(petBattle);
		try {
			lock.lock();
			fightable.clear();
			fightable.addAll(tmpAttr);
			petBattle.setHp(tmpAttr.get(AttributeKeys.HP));
		    petBattle.setFighting(fighting);
			petBattle.setFlushable(Flushable.FLUSHABLE_NOT);
		} catch (Exception e) {
			petBattle.setFlushable(Flushable.FLUSHABLE_NORMAL);
		}finally{
			lock.unlock();
		}
		
	}
	
	

	private Fightable calcQualityFightable(int level,int quality,PetJob job){
		Fightable fightable = new Fightable();
		PetQualityAttributeConfig qualityConfig = this.getPetQualityAttributeConfig(job.ordinal());
		if(qualityConfig == null){
			return fightable;
		}
		
		int hp = FormulaHelper.invoke(FormulaKey.PET_QUALITY_ATTRIBUTE_FORMULA, qualityConfig.getHp(),quality).intValue();
		int mp = FormulaHelper.invoke(FormulaKey.PET_QUALITY_ATTRIBUTE_FORMULA, qualityConfig.getMp(),quality).intValue();
		int physicalattack = FormulaHelper.invoke(FormulaKey.PET_QUALITY_ATTRIBUTE_FORMULA, qualityConfig.getPhysicalattack(),quality).intValue();
		int theurgyattack = FormulaHelper.invoke(FormulaKey.PET_QUALITY_ATTRIBUTE_FORMULA, qualityConfig.getTheurgyattack(),quality).intValue();
		int physicaldefense = FormulaHelper.invoke(FormulaKey.PET_QUALITY_ATTRIBUTE_FORMULA, qualityConfig.getPhysicaldefense(),quality).intValue();
		int theurgydefense = FormulaHelper.invoke(FormulaKey.PET_QUALITY_ATTRIBUTE_FORMULA, qualityConfig.getTheurgydefense(),quality).intValue();
		int physicalcritical = FormulaHelper.invoke(FormulaKey.PET_QUALITY_ATTRIBUTE_FORMULA, qualityConfig.getPhysicalcritical(),quality).intValue();
		int theurgycritical = FormulaHelper.invoke(FormulaKey.PET_QUALITY_ATTRIBUTE_FORMULA, qualityConfig.getTheurgycritical(),quality).intValue();
		int hit = FormulaHelper.invoke(FormulaKey.PET_QUALITY_ATTRIBUTE_FORMULA, qualityConfig.getHit(),quality).intValue();
		int dodge = FormulaHelper.invoke(FormulaKey.PET_QUALITY_ATTRIBUTE_FORMULA, qualityConfig.getDodge(),quality).intValue();
		
		fightable.put(AttributeKeys.HP, hp);
		fightable.put(AttributeKeys.HP_MAX, hp);
		fightable.put(AttributeKeys.MP, mp);
		fightable.put(AttributeKeys.MP_MAX, mp);
		fightable.put(AttributeKeys.PHYSICAL_ATTACK, physicalattack);
		fightable.put(AttributeKeys.THEURGY_ATTACK, theurgyattack);
		fightable.put(AttributeKeys.PHYSICAL_DEFENSE, physicaldefense);
		fightable.put(AttributeKeys.THEURGY_DEFENSE, theurgydefense);
		fightable.put(AttributeKeys.PHYSICAL_CRITICAL, physicalcritical);
		fightable.put(AttributeKeys.THEURGY_CRITICAL, theurgycritical);
		fightable.put(AttributeKeys.HIT, hit);
		fightable.put(AttributeKeys.DODGE, dodge);
		return fightable;
	}

	private Fightable calcLevelFightable(int level,int quality,PetJob job){
		Fightable fightable = new Fightable();
		PetSavvyAttributeConfig config = this.getPetSavvyAttributeConfig(job.ordinal());
		if(config == null){
			return fightable;
		}
		
		int hp = FormulaHelper.invoke(FormulaKey.PET_LEVEL_ATTRIBUTE_FORMULA, config.getHp(),level,quality).intValue();
		int mp = FormulaHelper.invoke(FormulaKey.PET_LEVEL_ATTRIBUTE_FORMULA, config.getMp(),level,quality).intValue();
		int physicalattack = FormulaHelper.invoke(FormulaKey.PET_LEVEL_ATTRIBUTE_FORMULA, config.getPhysicalattack(),level,quality).intValue();
		int theurgyattack = FormulaHelper.invoke(FormulaKey.PET_LEVEL_ATTRIBUTE_FORMULA, config.getTheurgyattack(),level,quality).intValue();
		int physicaldefense = FormulaHelper.invoke(FormulaKey.PET_LEVEL_ATTRIBUTE_FORMULA, config.getPhysicaldefense(),level,quality).intValue();
		int theurgydefense = FormulaHelper.invoke(FormulaKey.PET_LEVEL_ATTRIBUTE_FORMULA, config.getTheurgydefense(),level,quality).intValue();
		int physicalcritical = FormulaHelper.invoke(FormulaKey.PET_LEVEL_ATTRIBUTE_FORMULA, config.getPhysicalcritical(),level,quality).intValue();
		int theurgycritical = FormulaHelper.invoke(FormulaKey.PET_LEVEL_ATTRIBUTE_FORMULA, config.getTheurgycritical(),level,quality).intValue();
		int hit = FormulaHelper.invoke(FormulaKey.PET_LEVEL_ATTRIBUTE_FORMULA, config.getHit(),level,quality).intValue();
		int dodge = FormulaHelper.invoke(FormulaKey.PET_LEVEL_ATTRIBUTE_FORMULA, config.getDodge(),level,quality).intValue();
		
		fightable.put(AttributeKeys.HP, hp);
		fightable.put(AttributeKeys.HP_MAX, hp);
		fightable.put(AttributeKeys.MP, mp);
		fightable.put(AttributeKeys.MP_MAX, mp);
		fightable.put(AttributeKeys.PHYSICAL_ATTACK, physicalattack);
		fightable.put(AttributeKeys.THEURGY_ATTACK, theurgyattack);
		fightable.put(AttributeKeys.PHYSICAL_DEFENSE, physicaldefense);
		fightable.put(AttributeKeys.THEURGY_DEFENSE, theurgydefense);
		fightable.put(AttributeKeys.PHYSICAL_CRITICAL, physicalcritical);
		fightable.put(AttributeKeys.THEURGY_CRITICAL, theurgycritical);
		fightable.put(AttributeKeys.HIT, hit);
		fightable.put(AttributeKeys.DODGE, dodge);
		return fightable;
	} 
	

	private PetQualityAttributeConfig getPetQualityAttributeConfig(int job) {
		 return this.resourceService.get(job,PetQualityAttributeConfig.class);
	}
	

	private PetSavvyAttributeConfig getPetSavvyAttributeConfig(int job) {
		return this.resourceService.get(job, PetSavvyAttributeConfig.class);
	}

}
