package com.yayo.warriors.module.pet.rule;


import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.common.helper.FormulaHelper;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.type.FormulaKey;


@Component
public class PetExpCalc {
	@Autowired
	private UserManager userManager;
	@Autowired
	private DbService dbService;
	
	private static ObjectReference<PetExpCalc> ref = new ObjectReference<PetExpCalc>();
	
	@PostConstruct
	protected void init() {
		ref.set(this);
	}
	

	public static PetExpCalc getInstance() {
		return ref.get();
	}
	
	
	public static void caclePetExp(long playerId,PetBattle battle) {
		if(battle == null || !battle.isCheckingExp()){
			return;
		}
		
		ChainLock lock = LockUtils.getLock(battle);
		try {
			lock.lock();
			while(battle.getExp() != 0){
				long needExp = 0;
				int petLevel = battle.getLevel();
		    	if(petLevel < 30){
		    		needExp = FormulaHelper.invoke(FormulaKey.PET_LEVEL_UP_FORMULA_BEFORE, petLevel).longValue();
		    	}else{
		    		needExp = FormulaHelper.invoke(FormulaKey.PET_LEVEL_UP_FORMULA_AFTER, petLevel).longValue();
		    	}
				if(battle.getExp() >= needExp){
					battle.decreaseExp(needExp); 
					battle.increaseLevel(1);
					battle.setFlushable(Flushable.FLUSHABLE_LEVEL_UP);
				}else{
					break;
				}
			}
			
			battle.uncheckingExp();
		}finally{
			lock.unlock();
		}
		
		getInstance().dbService.submitUpdate2Queue(battle);
	}
	

}
