package com.yayo.warriors.module.horse.manager.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.warriors.basedb.model.HorseConfig;
import com.yayo.warriors.module.horse.dao.HorseDao;
import com.yayo.warriors.module.horse.entity.Horse;
import com.yayo.warriors.module.horse.manager.HorseManager;
import com.yayo.warriors.module.horse.rule.HorseRule;
import com.yayo.warriors.module.server.listener.DataRemoveListener;
import com.yayo.warriors.module.task.facade.TaskFacade;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.module.user.type.Job;
import com.yayo.warriors.type.IndexName;

@Service
public class HorseManagerImpl extends CachedServiceAdpter implements HorseManager, DataRemoveListener {
	@Autowired
	private HorseDao horseDao;
	@Autowired
	private DbService dbService;
	@Autowired
	private TaskFacade taskFacade;
	@Autowired
	private UserManager userManager;
	@Autowired
	private ResourceService resourceService;
	
	/** 日志格式 */
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	
	
	public Horse getHorse(PlayerBattle battle) {
		if(battle == null || battle.getLevel() < HorseRule.MIN_WINUP_LEVEL){
			return null;
		}
		
		Horse horse = this.get(battle.getId(), Horse.class);
		this.checkHorseLevel(horse);				//计算坐骑升级情况
		this.flushableAttribute(horse,battle); 			//刷新坐骑属性
		return horse;
	}
	
//	
//	public Horse getHorse(long playerId, boolean checkLevel) {
//		if(playerId <= 0L) {
//			return null;
//		}
//		
//		if(checkLevel) {
//			UserDomain userDomain = userManager.getUserDomain(playerId);
//			if(userDomain == null) {
//				return null;
//			}
//			
//			PlayerBattle battle = userDomain.getBattle();
//			if(battle.getLevel() < HorseRule.MIN_WINUP_LEVEL){
//				return null;
//			}
//		}
//		
//		Horse horse = this.get(playerId, Horse.class);
//		//this.checkHorseLevel(horse);				//计算坐骑升级情况  现在采用 自定义物品幻化 所以改变一下 liuyuhua
//		this.flushableAttribute(horse); 			//刷新坐骑属性
//		return horse;
//	}
	
	/**
	 * 计算坐骑的等级
	 * 
	 * @param  horse		坐骑对象
	 */
	private void checkHorseLevel(Horse horse) {
		if(horse == null) {
			return;
		}
		
		HorseConfig horseConfig = this.getHorseConfig(horse.getLevel());
		if(horseConfig == null || horse.getExp() < horseConfig.getLevelupExp()) { //经验不足, 直接返回
			return;
		}
		
		ChainLock lock = LockUtils.getLock(horse);
		try {
			lock.lock();
			horseConfig = this.getHorseConfig(horse.getLevel());
			if(horseConfig == null) {
				return;
			} else if(horse.getExp() < horseConfig.getLevelupExp()) { //经验不足, 直接返回
				return;
			}
			
			horse.setExp(0);
			horse.setLevel( horse.getLevel() + 1);
			horseConfig = this.getHorseConfig(horse.getLevel());
			if (horseConfig != null) {
				horse.setModel(horseConfig.getModel());
			}
		} finally {
			lock.unlock();
		}
		
		long playerId = horse.getId();
		dbService.submitUpdate2Queue(horse);
		taskFacade.updateHorseLevelTask(playerId);
		userManager.updateFlushable(playerId, Flushable.FLUSHABLE_NORMAL);
	}
	
	/**
	 * 刷新坐骑属性
	 * @param horse      坐骑的对象
	 * @param battle     角色战斗对象
	 */
	private void flushableAttribute(Horse horse,PlayerBattle battle) {
		if (horse == null || !horse.isFlushable()) {
			return;
		}

		HorseConfig config = this.getHorseConfig(horse.getLevel());
		if (config == null) {
			return;
		}
		
		int hp = config.getHp(); //生命
		int mp = config.getMp();//内力
		int physicalAttack = config.getPhysicalAttack();//外攻(物攻)
		int theurgyAttack = config.getTheurgyAttack();//内攻(法攻)
		int physicalDefense = config.getPhysicalDefense();//外防(物防)
	    int theurgyDefense = config.getTheurgyDefense();//内防(法防)
		int physicalCritical = config.getPhysicalCritical();//外暴(物暴)
		int theurgyCritical = config.getTheurgyCritical();//内暴(法暴)
		int hit = config.getHit();//命中
		int dodge = config.getDodge();//闪避
		
		Fightable fightable = new Fightable();
		int speed = this.getSpeed4Model(horse.getModel());
		fightable.set(AttributeKeys.MOVE_SPEED, speed);
		
		fightable.set(AttributeKeys.HP_MAX, hp);
		fightable.set(AttributeKeys.MP_MAX, mp);
		fightable.set(AttributeKeys.PHYSICAL_DEFENSE, physicalDefense);
		fightable.set(AttributeKeys.THEURGY_DEFENSE, theurgyDefense);
		fightable.set(AttributeKeys.HIT, hit);
		fightable.set(AttributeKeys.DODGE, dodge);
		if(battle.getJob() == Job.XIAOYAO || battle.getJob() == Job.XINGXIU){ //根据职业不同增做特殊变化
			fightable.set(AttributeKeys.THEURGY_ATTACK, theurgyAttack);
			fightable.set(AttributeKeys.THEURGY_CRITICAL, theurgyCritical);
		}
		
		if(battle.getJob() == Job.TIANLONG || battle.getJob() == Job.TIANSHAN){ //根据职业不同增做特殊变化
			fightable.set(AttributeKeys.PHYSICAL_ATTACK, physicalAttack);
			fightable.set(AttributeKeys.PHYSICAL_CRITICAL, physicalCritical);
		}

		ChainLock lock = LockUtils.getLock(horse);
		try {
			lock.lock();
			Fightable horseAttribute = horse.getAttributes();
			horseAttribute.clear();
			horseAttribute.putAll(fightable);
			horse.setFlushable(Flushable.FLUSHABLE_NOT);
		} catch (Exception e) {
			LOGGER.error("坐骑ID[{}],刷新坐骑属性异常:{}", e);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * 通过模型获取 坐骑的速度
	 * @param model 模型的ID 
	 * @return {@link Integer} 速度
	 */
	private int getSpeed4Model(int model){
		HorseConfig config = resourceService.getByUnique(IndexName.HORSE_MODEL, HorseConfig.class, model);
		if(config == null){
			return 0;
		}
		return config.getSpeed();
	}
	
	
	
	@SuppressWarnings("unchecked")
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(id != null && clazz == Horse.class) {
			Horse horse = horseDao.get(id, Horse.class);
			if(horse == null){
				try {
					HorseConfig config = this.getHorseConfig(HorseRule.INIT_HORSE_LEVEL);
					horse = HorseRule.createHorse((Long)id, config);
					horseDao.save(horse);
				} catch (Exception e) {
					horse = null;
					LOGGER.error("角色:[{}] 创建坐骑信息异常:{}", id, e);
				}
			}
			return (T) horse;
		}
		return super.getEntityFromDB(id, clazz);
	}

	
	public HorseConfig getHorseConfig(int level) {
		return resourceService.get(level, HorseConfig.class);
	}
	
	

	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		this.removeEntityFromCache(messageInfo.getPlayerId(), Horse.class);
	}

	
	public Map<Integer, Integer> getAdminAllHorseLevel() {
		List<Integer> horseLevels = horseDao.getAllPlayerHorseLevel();
		Map<Integer,Integer> resultObject = new HashMap<Integer, Integer>(10);
		if(horseLevels == null || horseLevels.isEmpty()){
			return resultObject;
		}
		
		int oneCount = 0;
		int twoCount = 0;
		int threeCount = 0;
		int fourCount = 0;
		int fiveCount = 0;
		int sixCount = 0;
		int sevenCount = 0;
		int eightCount = 0;
		int nightCount = 0;
		int tenCount = 0;
		for(int level : horseLevels){
			if(level >= 0 && level <= 10){
				oneCount += 1;
			}else if(level > 10 && level <= 20){
				twoCount += 1;
			}else if(level > 20 && level <= 30){
				threeCount += 1;
			}else if(level > 30 && level <= 40){
				fourCount += 1;
			}else if(level > 40 && level <= 50){
				fiveCount += 1;
			}else if(level > 50 && level <= 60){
				sixCount += 1;
			}else if(level > 60 && level <= 70){
				sevenCount += 1;
			}else if(level > 70 && level <= 80){
				eightCount += 1;
			}else if(level > 80 && level <= 90){
				nightCount += 1;
			}else if(level > 90 && level <= 100){
				tenCount += 1;
			}
		}
		
		resultObject.put(1, oneCount);
		resultObject.put(2, twoCount);
		resultObject.put(3, threeCount);
		resultObject.put(4, fourCount);
		resultObject.put(5, fiveCount);
		resultObject.put(6, sixCount);
		resultObject.put(7, sevenCount);
		resultObject.put(8, eightCount);
		resultObject.put(9, nightCount);
		resultObject.put(10, tenCount);
		
		
		
		return resultObject;
	}

	
	public Horse getHorse(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return null;
		}
		PlayerBattle battle = userDomain.getBattle();
		return this.getHorse(battle);
	}
	

}
