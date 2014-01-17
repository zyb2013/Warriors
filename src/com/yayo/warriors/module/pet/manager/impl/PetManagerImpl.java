package com.yayo.warriors.module.pet.manager.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.pet.constant.PetConstant.*;
import static com.yayo.warriors.module.user.type.AttributeKeys.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap.Builder;
import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.common.utility.DateUtil;
import com.yayo.warriors.basedb.model.PetConfig;
import com.yayo.warriors.basedb.model.PetMergedConfig;
import com.yayo.warriors.basedb.model.PetTrainConfig;
import com.yayo.warriors.common.helper.FormulaHelper;
import com.yayo.warriors.module.pet.constant.PetConstant;
import com.yayo.warriors.module.pet.dao.PetDao;
import com.yayo.warriors.module.pet.entity.Pet;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.pet.model.PetZoom;
import com.yayo.warriors.module.pet.rule.PetRule;
import com.yayo.warriors.module.pet.types.PetStatus;
import com.yayo.warriors.module.server.listener.DataRemoveListener;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.type.FormulaKey;

@Component
public class PetManagerImpl extends CachedServiceAdpter implements PetManager , DataRemoveListener {

	@Autowired
	private PetDao petDao;
	@Autowired
	private DbService dbService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private ResourceService resourceService;
	
	
	// LRU MAP Builder
	private static final Builder<Long, PetDomain> BUILDER = new ConcurrentLinkedHashMap.Builder<Long, PetDomain>();
	/** 家将对象域集合{家将的ID,家将域对象}*/
	private static final ConcurrentLinkedHashMap<Long, PetDomain> PETDOMAINMAP = BUILDER.maximumWeightedCapacity(12000).build();
	/** 玩家的家将信息{玩家的ID,家将的信息}*/
	private static final ConcurrentHashMap<Long, PetInfo> PETINFOMAP = new ConcurrentHashMap<Long, PetManagerImpl.PetInfo>();
	
	
	/**
	 * 获取家将对象
	 * @param id  家将的ID
	 * @return {@link Pet}家将对象
	 */
	private Pet getPet(long id){
		 return petDao.get(id, Pet.class);
	}
	
	/**
	 * 获取家将对象
	 * @param id  家将的ID
	 * @return {@link PetBattle} 家将战斗对象
	 */
	private PetBattle getBattle(long id){
		return petDao.get(id, PetBattle.class);
	}
	
	/**
	 * 获取家将的信息
	 * @param playerId 玩家的ID
	 * @return {@link PetInfo} 玩家家将信息
	 */
	private PetInfo getPetInfo(PlayerBattle playerBattle) {
		if(playerBattle == null) {
			return null;
		}
		
		long playerId = playerBattle.getId();
		PetInfo info = PETINFOMAP.get(playerId);
		if(info != null){
			return info;
		}
		List<Long> petIds = (List<Long>) petDao.getPlayerPetIds(playerId);
		if(petIds == null){
			petIds = new ArrayList<Long>();
		}
		
		long alivePetId = 0;
		for(Long petId : petIds){
			PetDomain petDomain = this.getPetDomain(petId, playerBattle);
			if(petDomain.getPet().isStatus(PetStatus.FIGHTING) || petDomain.getPet().isStatus(PetStatus.MERGED)){
				alivePetId = petDomain.getId();
			}
		}
		
		info = new PetInfo(playerId, new HashSet<Long>(petIds));
		if(alivePetId > 0){
			info.setAlivePetId(alivePetId);
		}
		
		PETINFOMAP.putIfAbsent(playerId,info);
		return PETINFOMAP.get(playerId);
	}
	
	
	
	public PetDomain getPetDomain(long petId) {
		if(petId <= 0L){
			return null;
		}
		
		PetDomain petDomain = PETDOMAINMAP.get(petId);
		if(petDomain != null){
			return petDomain;
		}
		
		Pet pet = this.getPet(petId);
		if(pet == null || pet.isStatus(PetStatus.DROP)){
			return null;
		}
		
		long playerId = pet.getPlayerId();
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return null;
		}
		
		return getPetDomain(petId, userDomain.getBattle());
	}

	/**
	 * 取得家将域模型
	 * 
	 * @param  petId			家将ID
	 * @param  playerBattle		角色战斗对象
	 * @return
	 */
	private PetDomain getPetDomain(long petId, PlayerBattle playerBattle) {
		if(petId <= 0L){
			return null;
		}
		
		PetDomain petDomain = PETDOMAINMAP.get(petId);
		if(petDomain != null){
			return petDomain;
		}
		
		Pet pet = this.getPet(petId);
		if(pet == null || pet.isStatus(PetStatus.DROP)){
			return null;
		}
		
		PetBattle battle = this.getBattle(petId);
		if(battle == null){
			return null;
		}
		
		if(pet.isTraing()) { //计算家将的修炼经验值
			this.calcTraingPet(pet, battle, playerBattle);
		}
		
		PETDOMAINMAP.putIfAbsent(petId, PetDomain.valueOf(pet, battle));
		return PETDOMAINMAP.get(petId);
	}
	
	/**
	 * 计算在修炼状态的家将经验值
	 * @param pet              家将对象
	 * @param petBattle        家将战斗对象
	 * @param playerBattle     玩家战斗对象
	 */
	private void calcTraingPet(Pet pet, PetBattle petBattle, PlayerBattle playerBattle){
		if(playerBattle == null || pet == null || !pet.isTraing()){
			return;
		}
		
		ChainLock lock = LockUtils.getLock(pet,petBattle);
	    try {
	    	lock.lock();
			if(pet.isOverTraing()){//超过了时间
				pet.setTotleTraingTime(0);
				pet.setStartTraingTime(0);
				dbService.submitUpdate2Queue(pet);//保存一次
				return;
			}
			
			long currentTime = System.currentTimeMillis();//当前时间
			long timeSecond = currentTime - pet.getStartTraingTime(); //得到本次修炼时间(单位:毫秒)
			long timeMinute = timeSecond / 60000; //得到本次计算出来的分钟数
	    	if(timeMinute == 0){
	    		return;//未到达修炼时间
	    	}
	    	
	        long totleTime = (28800L - pet.getTotleTraingTime())/60;//可用的总时间
	        timeMinute = totleTime - timeMinute < 0 ? timeMinute - totleTime : timeMinute;//计算可用时间 
	    	pet.setStartTraingTime(currentTime);//设置新开始的时间
	    	pet.setTotleTraingTime(pet.getTotleTraingTime() + timeSecond);//设置本轮训练用了多少时间
	    	
	    	int playerLevel = playerBattle.getLevel();
	    	int exp = 0;
	    	if(petBattle.getLevel() < 30){
	    		exp = FormulaHelper.invoke(FormulaKey.PET_TRAING_EXP_BEFORE,playerLevel,timeMinute).intValue();
	    	}else{
	    		exp = FormulaHelper.invoke(FormulaKey.PET_TRAING_EXP_AFTER,playerLevel,timeMinute).intValue();
	    	}
	    	
	    	exp = exp < 0 ? 0 : exp;//过滤零值操作
			petBattle.increaseExp(exp);
			while(petBattle.getExp() != 0){
				long needExp = 0;
				int petLevel = petBattle.getLevel();
		    	if(petBattle.getLevel() < 30){
		    		needExp = FormulaHelper.invoke(FormulaKey.PET_LEVEL_UP_FORMULA_BEFORE, petLevel).longValue();
		    	}else{
		    		needExp = FormulaHelper.invoke(FormulaKey.PET_LEVEL_UP_FORMULA_AFTER, petLevel).longValue();
		    	}
				
				if(petBattle.getExp() >= needExp){
					if(petBattle.getLevel() >= playerBattle.getLevel()){ //不可以超过玩家的最大等级
						break;
					}
					petBattle.decreaseExp(needExp); //扣减需要升级的经验值
					petBattle.increaseLevel(1);
					petBattle.setFlushable(Flushable.FLUSHABLE_NORMAL);
				}else{
					break;
				}
			}
			
			if(pet.isOverTraing() || petBattle.getLevel() == playerLevel){//超过最修炼时间,自动将其停止
				pet.setTotleTraingTime(0);
				pet.setStartTraingTime(0);
			}
			
		}finally{
			lock.unlock();
		}
		
	    dbService.submitUpdate2Queue(pet);//保存一次
	}

	
	public List<PetDomain> getPetDomains(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return Collections.emptyList();
		}
		
		PetInfo info = this.getPetInfo(userDomain.getBattle());
		if(info == null){
			return Collections.emptyList();
		}
		
		List<PetDomain> petDomains = new ArrayList<PetDomain>();
		Collection<Long> petIds = info.getPetIds();
		if(petIds == null){
			return petDomains;
		}
		
		for(long petId : petIds){
			PetDomain petDomain = this.getPetDomain(petId);
			if(petDomain != null){
				petDomains.add(petDomain);
			}
		}
		return petDomains;
	}

	
	public PetDomain getFightingPet(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return null;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(userDomain == null || battle.getLevel() < PetRule.USE_PET_MIX_LEVEL){
			return null;
		}
		
		PetInfo info = this.getPetInfo(userDomain.getBattle());
		if(info == null){
			return null;
		}
		
		PetDomain petDomain = this.getPetDomain(info.getAlivePetId());
		if(petDomain == null || !petDomain.getPet().isStatus(PetStatus.FIGHTING)){
			return null;
		}
		
		return petDomain;
	}
	
	
	/**
	 * 家将出战
	 * @param userDomain   玩家域对象
	 * @param petDomain    需要出战的家将域对象
	 * @return {@link Integer} 返回错误码
	 */
	
	public int goFighting(UserDomain userDomain, PetDomain petDomain){
		if(userDomain == null || petDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		long petId = petDomain.getId();
		Pet pet = petDomain.getPet();
		PetBattle petBattle = petDomain.getBattle();
		PetInfo info = this.getPetInfo(userDomain.getBattle());
		if(info == null){
			return PetConstant.PET_NOT_FOUND;
		}

		if(!petBattle.canFight()){
			return PET_CD_ERROR;
		}
		
	    if(petBattle.isDeath()){
	    	return PET_GOFIGHTING_FAILURE;
	    }
	    
		
	    PetDomain beforeDomain = this.getPetDomain(info.getAlivePetId());
	    if(beforeDomain != null){ //之前已经有家将出战,所以要置换状态
	    	if(petDomain.getId() == beforeDomain.getId()){
	    		return PET_GOFIGHTING_FAILURE;
	    	}
	    	
	    	Pet beforPet = beforeDomain.getPet();//之前的家将
	    	ChainLock lock = LockUtils.getLock(info,beforeDomain,petDomain);
	    	try {
				lock.lock();
				if(info.getAlivePetId() == petId){
					return PET_GOFIGHTING_FAILURE;
				}
				
				info.setAlivePetId(petId);
				pet.setStatus(PetStatus.FIGHTING);
				beforPet.setStatus(PetStatus.ACTIVE);
	    	}finally{
				lock.unlock();
			}
	    	
	    	dbService.submitUpdate2Queue(beforPet,pet); //更新
	    	
	    }else{ //没有家将出战,无需置换状态
	    	ChainLock lock = LockUtils.getLock(info,petDomain);
	    	try {
				lock.lock();
				if(info.getAlivePetId() == petId){
					return PET_GOFIGHTING_FAILURE;
				}
				info.setAlivePetId(petId);
				pet.setStatus(PetStatus.FIGHTING);
	    	}finally{
				lock.unlock();
			}
	    	
	    	dbService.submitUpdate2Queue(pet); //更新
	    }
		
		return SUCCESS;
	}
	
	
	public boolean isPlayerPet(long playerId, long petId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return false;
		}
		
		PetInfo info = this.getPetInfo(userDomain.getBattle());
		if(info == null){
			return false;
		}

		return info.isExistPetId(petId);
	}

	
	public PetDomain goBack(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return null;
		}
		PetInfo info = this.getPetInfo(userDomain.getBattle());
		if(!info.isAlivePet()){
			return null;
		}
		
		
		PetDomain petDomain = this.getPetDomain(info.getAlivePetId());
		if(petDomain == null){
			return null;
		}
		
		Pet pet = petDomain.getPet();
		PetBattle petBattle = petDomain.getBattle();
		PlayerBattle playerBattle = userDomain.getBattle();
	   ChainLock lock = LockUtils.getLock(info,petDomain,playerBattle);
	   try {
		   lock.lock();
		   if(!info.isAlivePet()){
				return null;
		   }
		   if(pet.isStatus(PetStatus.MERGED)){ //如果在真传状态,需要刷新人物角色属性
				pet.setStatus(PetStatus.ACTIVE);
				playerBattle.setFlushable(Flushable.FLUSHABLE_NORMAL);
		   }else{
				pet.setStatus(PetStatus.ACTIVE);
		   }
			
		   info.goBack();
		   petBattle.goBack();//家将回收
	   }finally{
		   lock.unlock();
	   }
		
		dbService.submitUpdate2Queue(pet);
		return petDomain;
	}

	
	public boolean disbandPet(long playerId, long petId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		PetInfo info = this.getPetInfo(userDomain.getBattle());
		if(info == null){
			return false;
		}
		
		PetDomain petDomain = this.getPetDomain(petId);
		if(petDomain == null){
			return false;
		}
		
		Pet pet = petDomain.getPet();
		if(pet.isStatus(PetStatus.DROP) || !pet.isStatus(PetStatus.ACTIVE)){
			return false;
		}
		
		ChainLock lock = LockUtils.getLock(info,petDomain);
		try {
			lock.lock();
			if(pet.getStatus() == PetStatus.DROP){
				return false;
			}
			pet.setStatus(PetStatus.DROP);
			info.removePetId(petId);
		}finally{
			lock.unlock();
		}
		
		this.dbService.updateEntityIntime(pet); //提交更新
	    return true;
	}


	
	public Object[] getPetAttributes(long playerId, long petId, Object... params) {
		UserDomain userDomain = this.userManager.getUserDomain(playerId);
		Object[] values = new Object[params.length];
		if(userDomain == null){
			return values;
		}
		
		PetDomain petDomain = this.getPetDomain(petId);
		if(petDomain == null){
			return values;
		}
		
		Pet pet = petDomain.getPet();
		PetBattle battle = petDomain.getBattle();
		
		for(int index = 0 ; index < params.length; index++){
			Integer attribute = (Integer) params[index];
			if(attribute == null) {
				continue;
			}
			
			switch(attribute){
			   case NAME:                   values[index] = pet.getName();                                    break;
			   case ICON:                   values[index] = pet.getIcon();                                    break;
			   case BASE_ID:                values[index] = pet.getBaseId();                                  break;
			   case CLOTHING:               values[index] = pet.getModel();                                   break;
			   case JOB:                    values[index] = battle.getJob();                                  break;
			   case LEVEL:                  values[index] = battle.getLevel();                                break;
			   case PET_SAVVY:              values[index] = battle.getSavvy();                                break;
			   case PET_ENERGY:             values[index] = pet.getEnergy();                                  break;
			   case PET_ENERGY_MAX:         values[index] = PetRule.INIT_PET_ENERGY;                          break;
			   case PET_STATUS:             values[index] = pet.getStatus();                                  break;
			   case PET_SKILL:              values[index] = pet.getSkill();                                   break;
			   case PET_MERGED_LEVEL:       values[index] = battle.getMergedLevel();                          break;
			   case PET_MERGED_BLESS:       battle.checkOverMerged(); values[index] = battle.getMergedBless();break;
			   case PET_START_TRAING_TIME:  values[index] = pet.getStartTraingTime();                         break;
			   case TOTLE_TRAING_TIME:      values[index] = pet.getTotleTraingTime();                         break;
			   
			   case QUALITY:                values[index] = battle.getQuality();                              break;
			   
			   case HP:			            values[index] = battle.getHp();	                 		   	      break;
			   case HP_MAX:		            values[index] = battle.getAttribute(attribute);				      break;
			   
			   case PIERCE:			    	values[index] = battle.getAttribute(attribute);				      break;
			   case BLOCK:					values[index] = battle.getAttribute(attribute);				      break;	
			   case DODGE:					values[index] = battle.getAttribute(attribute);				      break;
			   case HIT:					values[index] = battle.getAttribute(attribute);				      break;
			   case HP_BAG:			    	values[index] = battle.getAttribute(attribute);				      break;

			   case EXP:                    values[index] = battle.getExp();                                  break;
			   case EXP_MAX:				values[index] = battle.getAttribute(attribute);				      break;
			   case RAPIDLY:				values[index] = battle.getAttribute(attribute);				      break;
			   case STRENGTH:				values[index] = battle.getAttribute(attribute);				      break;
			   case DEXERITY:				values[index] = battle.getAttribute(attribute);				      break;
			   case DUCTILITY:				values[index] = battle.getAttribute(attribute);				      break;
			   case INTELLECT:				values[index] = battle.getAttribute(attribute);				      break;
			   case MOVE_SPEED:			    values[index] = battle.getAttribute(attribute);				      break;
			   case CONSTITUTION:			values[index] = battle.getAttribute(attribute);				      break;
			   case SPIRITUALITY:			values[index] = battle.getAttribute(attribute);				      break;
			   case THEURGY_ATTACK:		    values[index] = battle.getAttribute(attribute);				      break;
			   case THEURGY_DEFENSE:		values[index] = battle.getAttribute(attribute);				      break;
			   case THEURGY_CRITICAL:		values[index] = battle.getAttribute(attribute);				      break;
			   case PHYSICAL_ATTACK:		values[index] = battle.getAttribute(attribute);				      break;
			   case PHYSICAL_DEFENSE:		values[index] = battle.getAttribute(attribute);				      break;
			   case PHYSICAL_CRITICAL:		values[index] = battle.getAttribute(attribute);				      break;
			   case PET_FIGHTINT_CAPACITY:  values[index] = battle.getFighting();                             break;
			   default:values[index] = 0;	break;
			}
		}
		return values;
	}

	
	@SuppressWarnings("unchecked")
	public void onDataRemoveEvent(MessageInfo messageInfo){
		long playerId = messageInfo.getPlayerId();
		PetInfo petInfo = PETINFOMAP.remove(playerId);
		if(petInfo == null) {
			return;
		}
		
		final List<Pet> pets = new ArrayList<Pet>();
		final List<PetBattle> petBattles = new ArrayList<PetBattle>();
		for(long petId : petInfo.getPetIds()){
			PetDomain petDomain = PETDOMAINMAP.get(petId);
			if(petDomain != null) {
				pets.add(petDomain.getPet());
				petBattles.add(petDomain.getBattle());
			}
			PETDOMAINMAP.remove(petId);
		}
		dbService.submitUpdate2Queue(pets.toArray(new Pet[pets.size()]),petBattles.toArray(new PetBattle[petBattles.size()]));
	}
	


	
	public boolean checkFighting(long playerId, long petId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return false;
		}
		
		PetInfo info = this.getPetInfo(userDomain.getBattle());
		if(info == null){
			return false;
		}
		
		if(!info.isAlivePet()){
			return false;
		}
		
		return info.getAlivePetId() == petId;
	}

	
	public PetDomain caclPetEnergy(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return null;
		}
		
		PetDomain petDomain = this.getFightingPet(playerId);
		if(petDomain == null){
			return null;
		}
		PetInfo info = this.getPetInfo(userDomain.getBattle());
		if(info == null){
			return null;
		}
		
		long lastTime = info.getEnergyCaclTime(); //最后一次计算时间
		long currentTime = DateUtil.getCurrentSecond();//当前时间
		long resultTime = currentTime - lastTime; //时间差
		int caclEnergy = ((int)Math.ceil(resultTime / (double)PetRule.CACL_PET_ENERGY_TIME)) * PetRule.DECREASE_PET_ENERGY;
		caclEnergy = Math.max(caclEnergy, 1);
		
		Pet pet = petDomain.getPet();
		ChainLock lock = LockUtils.getLock(info,pet);
		try {
			lock.lock();
			info.setEnergyCaclTime(currentTime);
			pet.decreaseEnergy(caclEnergy);
		} finally {
			lock.unlock();
		}
		
		this.dbService.submitUpdate2Queue(pet);
		return petDomain;
	}

	
	public Set<Long> getPlayerPetIds(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return null;
		}
		
		PetInfo info = this.getPetInfo(userDomain.getBattle());
		if(info == null){
			return new HashSet<Long>(0);
		}
		
		return info.getPetIds();
	}

	
	public List<Integer> getFamousPetIds(long playerId) {
		List<Integer> result = this.petDao.getPlayerAllIds(playerId);
		return result;
	}

	
	public boolean remove(long playerId, long petId) {
		if(!this.isPlayerPet(playerId, petId)){
			return false;
		}
		
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return false;
		}
		
		
		PetInfo info = this.getPetInfo(userDomain.getBattle());
		if(info == null){
			return false;
		}
		
		ChainLock lock = LockUtils.getLock(info);
		try {
			lock.lock();
			info.removePetId(petId);
		}finally{
			lock.unlock();
		}
		PETDOMAINMAP.remove(petId);
		return true;
	}
	
	
	public PetConfig getPetConfig(int petBaseId) {
		PetConfig petConfig = this.resourceService.get(petBaseId, PetConfig.class);
		return petConfig;
	}


	
 	public PetTrainConfig getPetTrainConfig(int level){
 		return this.resourceService.get(level, PetTrainConfig.class);
 	}
	
	
	public PetMergedConfig getPetMergedConfig(int level) {
		return this.resourceService.get(level, PetMergedConfig.class);
	}

	
	public PetDomain createUnDrawPetDomain(Pet pet, PetBattle battle) {
		if(pet == null || battle == null){
			return null;
		}
		if(!petDao.createPetInfo(pet, battle)){
			return null;
		}
		
		return PetDomain.valueOf(pet, battle);
	}
	
	
	
	public PetDomain createPetDomain(Pet pet, PetBattle battle) {
		if(pet == null || battle == null){
			return null;
		}
		
		if(!petDao.createPetInfo(pet, battle)){
			return null;
		}
		
		long playerId = pet.getPlayerId();
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return null;
		}
		
		
		PetInfo info = this.getPetInfo(userDomain.getBattle());
		if(info != null){
			info.addPetId(pet.getId());
		}
		
		PETDOMAINMAP.putIfAbsent(pet.getId(), PetDomain.valueOf(pet, battle));
		return PETDOMAINMAP.get(pet.getId());
	}
	
	
	/**
	 * 玩家家将信息类
	 * @author liuyuhua
	 */
	class PetInfo{
		/** 玩家的ID*/
		private long playerId;
		/** 活跃中的家将*/
		private long alivePetId;
		/** 宠物出战最后一次精力结算时间 (单位:秒)*/
		private long energyCaclTime;
		/** 玩家所有的家将ID集合*/
		private Set<Long> petIds;
		
		/**
		 * 删除集合中的家将ID
		 * @param petId  家将ID
		 */
		public void removePetId(long petId){
			this.petIds.remove(petId);
		}
		
		/**
		 * 构造函数
		 * @param playerId   玩家的ID
		 * @param petIds     玩家所有的家将ID集合
		 */
		public PetInfo(long playerId,Set<Long> petIds){
			this.playerId = playerId;
			this.petIds = petIds;
		}
		
		/**
		 * 是否存在家将ID
		 * @return true 存在 ,false 不存在
		 */
		public boolean isExistPetId(long petId){
			return petIds.contains(petId);
		}
		
		/**
		 * 是否有家将出战
		 * @return true 有出战家将,false没有出战家将
		 */
		public boolean isAlivePet(){
			return this.alivePetId > 0;
		}
		
		/**
		 * 回收家将
		 */
		public void goBack(){
			this.alivePetId = 0;
			this.energyCaclTime = 0;
		}
		
		/**
		 * 添加角色家将ID
		 * @param petId     家将的ID
		 */
		public void addPetId(long petId){
			this.petIds.add(petId);
		}
		
		//Getter and Setter....
		public long getPlayerId() {
			return playerId;
		}
		public void setPlayerId(long playerId) {
			this.playerId = playerId;
		}
		public long getAlivePetId() {
			return alivePetId;
		}
		public void setAlivePetId(long alivePetId) {
			this.alivePetId = alivePetId;
		}
		public long getEnergyCaclTime() {
			return energyCaclTime;
		}
		public void setEnergyCaclTime(long energyCaclTime) {
			this.energyCaclTime = energyCaclTime;
		}
		
		public Set<Long> getPetIds() {
			return petIds;
		}
		
		public void setPetIds(Set<Long> petIds) {
			this.petIds = petIds;
		}
		
		private PetManagerImpl getOuterType() {
			return PetManagerImpl.this;
		}
		
		
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + (int) (playerId ^ (playerId >>> 32));
			return result;
		}
		
		
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PetInfo other = (PetInfo) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (playerId != other.playerId)
				return false;
			return true;
		}

		
		public String toString() {
			return "PetInfo [playerId=" + playerId + ", alivePetId="
					+ alivePetId + ", energyCaclTime=" + energyCaclTime
					+ ", petIds=" + petIds + "]";
		}

	}
	

	
	public int getUserPetMerged(PlayerBattle battle) {
		if(battle == null || battle.getLevel() < PetRule.USE_PET_MIX_LEVEL){
			return -1;
		}
		PetDomain petDomain = this.getPetMerged(battle);
		if(petDomain == null){
			return -1;
		}
		return petDomain.getBattle().getQuality();
	}
	

	
	public PetDomain getPetMerged(PlayerBattle playerBattle) {
		if(playerBattle == null){
			return null;
		}
		
		PetInfo info = this.getPetInfo(playerBattle);
		PetDomain petDomain = this.getPetDomain(info.getAlivePetId(), playerBattle);
		if(petDomain == null){
			return null;
		}
		
		Pet pet = petDomain.getPet();
		if(!pet.isStatus(PetStatus.MERGED)){
			return null;
		}
		return petDomain;
	}
	
	
	public int mergedPet(UserDomain userDomain, PetDomain petDomain){
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		if(petDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		long petId = petDomain.getId();
		PlayerBattle battle = userDomain.getBattle();
		PetInfo info = this.getPetInfo(battle);
		if(petDomain.getPlayerId() != userDomain.getId()){
			return PLAYER_NOT_FOUND;
		}
		
		PetDomain beforePetDomain = this.getPetDomain(info.getAlivePetId());//是否有活跃家将
		if(beforePetDomain != null && beforePetDomain.getId() != petId){
			Pet pet = petDomain.getPet();
			Pet beforePet = beforePetDomain.getPet();
			ChainLock lock = LockUtils.getLock(petDomain,beforePetDomain,info, battle);
			try {
				lock.lock();
				beforePet.setStatus(PetStatus.ACTIVE);
				pet.setStatus(PetStatus.MERGED);
				battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
				info.setAlivePetId(petId);
			}finally{
				lock.unlock();
			}
			
			dbService.submitUpdate2Queue(pet,beforePet);
		}else{
			Pet pet = petDomain.getPet();
			ChainLock lock = LockUtils.getLock(petDomain,info, battle);
			try {
				lock.lock();
				info.setAlivePetId(petId);
				pet.setStatus(PetStatus.MERGED);
				battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
			}finally{
				lock.unlock();
			}
			
			dbService.submitUpdate2Queue(pet);
		}
		
		return SUCCESS;
	}
	
	
	

	
	public PetDomain mergedPet(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return null;
		}
		
		PlayerBattle playerBattle = userDomain.getBattle();
		PetDomain petDomaind = this.getFightingPet(playerId);
		if(petDomaind == null){
			return null;
		}
		
		Pet pet = petDomaind.getPet();
		if(pet.isStatus(PetStatus.MERGED)){
			return petDomaind;
		}
		
		ChainLock lock = LockUtils.getLock(petDomaind,playerBattle);
		try {
			lock.lock();
			pet.setStatus(PetStatus.MERGED);
			playerBattle.setFlushable(Flushable.FLUSHABLE_NORMAL);
		}finally{
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(pet);
		return petDomaind;
	}

	 /**
     * 获取真传的附件属性值
     * 
     * @param  playerBattle 	   角色战斗对象
     * @return {@link Fightable} 属性值得 
     */ 
	
	public Fightable getMergedAttribute(PlayerBattle playerBattle) {
		Fightable fightable = new Fightable();
		PetDomain petDomain = this.getPetMerged(playerBattle);
		if(petDomain == null){
			return fightable;
		}
		
		PetBattle petBattle = petDomain.getBattle();
		PetMergedConfig config = this.getPetMergedConfig(petBattle.getMergedLevel());
		if(config == null){
			return fightable;
		}
		
		int strength = (int) ((int)petBattle.getAttribute(AttributeKeys.STRENGTH) * config.getMergedValue());
		int dexerity = (int) ((int)petBattle.getAttribute(AttributeKeys.DEXERITY) * config.getMergedValue());
		int intellect = (int) ((int)petBattle.getAttribute(AttributeKeys.INTELLECT) * config.getMergedValue());
		int constitution = (int) ((int)petBattle.getAttribute(AttributeKeys.CONSTITUTION) * config.getMergedValue());
		int spirituality = (int) ((int)petBattle.getAttribute(AttributeKeys.SPIRITUALITY) * config.getMergedValue());
		int hit = (int) ((int)petBattle.getAttribute(AttributeKeys.HIT) * config.getMergedValue());
		int dodge = (int) ((int)petBattle.getAttribute(AttributeKeys.DODGE) * config.getMergedValue());
		int theurgyattack = (int) ((int)petBattle.getAttribute(AttributeKeys.THEURGY_ATTACK) * config.getMergedValue());
		int theurgydefense = (int) ((int)petBattle.getAttribute(AttributeKeys.THEURGY_DEFENSE) * config.getMergedValue());
		int theurgycritical = (int) ((int)petBattle.getAttribute(AttributeKeys.THEURGY_CRITICAL) * config.getMergedValue());
		int physicalattack = (int) ((int)petBattle.getAttribute(AttributeKeys.PHYSICAL_ATTACK) * config.getMergedValue());
		int physicaldefense = (int) ((int)petBattle.getAttribute(AttributeKeys.PHYSICAL_DEFENSE) * config.getMergedValue());
		int physicalcritical = (int) ((int)petBattle.getAttribute(AttributeKeys.PHYSICAL_CRITICAL) * config.getMergedValue());
		int hpmax = (int) ((int)petBattle.getAttribute(AttributeKeys.HP_MAX) * config.getMergedValue());
		
	    fightable.put(AttributeKeys.STRENGTH, strength);
	    fightable.put(AttributeKeys.DEXERITY, dexerity);
	    fightable.put(AttributeKeys.INTELLECT, intellect);
	    fightable.put(AttributeKeys.CONSTITUTION, constitution);
	    fightable.put(AttributeKeys.SPIRITUALITY, spirituality);
	    fightable.put(AttributeKeys.HIT, hit);
	    fightable.put(AttributeKeys.DODGE, dodge);
	    fightable.put(AttributeKeys.THEURGY_ATTACK, theurgyattack);
	    fightable.put(AttributeKeys.THEURGY_DEFENSE, theurgydefense);
	    fightable.put(AttributeKeys.THEURGY_CRITICAL, theurgycritical);
	    fightable.put(AttributeKeys.PHYSICAL_ATTACK, physicalattack);
	    fightable.put(AttributeKeys.PHYSICAL_DEFENSE, physicaldefense);
	    fightable.put(AttributeKeys.PHYSICAL_CRITICAL, physicalcritical);
	    fightable.put(AttributeKeys.HP_MAX, hpmax);
		return fightable;
	}

	
	public PetZoom getPetZooms(long playerId) {
		PetZoom zoom = PetZoom.valueOf(playerId);
		Collection<Long> petIds = petDao.getPlayerUnDrawPet(playerId);
		if(petIds != null && !petIds.isEmpty()){
			for(long petId : petIds){
				PetDomain petDomain = getPetDomain(petId);
				if(petDomain != null){
					zoom.addPetDomain(petDomain);
				}
			}
		}
		return zoom;
	}

	
	public void addPetInfo(PetDomain petDomain) {
		if(petDomain == null){
			return;
		}
		long playerId = petDomain.getPlayerId();
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return;
		}
		
		PetInfo petInfo = this.getPetInfo(userDomain.getBattle());
		if(petInfo != null){
			petInfo.addPetId(petDomain.getId());
			PETDOMAINMAP.putIfAbsent(petDomain.getId(), petDomain);
		}
	}
}
