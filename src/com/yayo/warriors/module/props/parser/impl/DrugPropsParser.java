package com.yayo.warriors.module.props.parser.impl;

import static com.yayo.warriors.constant.CommonConstant.*;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.DateUtil;
import com.yayo.warriors.basedb.model.CoolTimeConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.cooltime.entity.UserCoolTime;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.logger.log.ExpLogger;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.constant.PropsConstant;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.parser.AbstractEffectParser;
import com.yayo.warriors.module.props.type.PropsChildType;
import com.yayo.warriors.module.props.type.PropsType;
import com.yayo.warriors.module.task.manager.EscortTaskManager;
import com.yayo.warriors.module.treasure.constant.TreasureConstant;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Flushable;

/**
 * 药物解析器
 * 
 * @author Hyint
 */
@Component
public class DrugPropsParser extends AbstractEffectParser {
	@Autowired
	private MapFacade mapFacade;
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private DbService dbService;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private EscortTaskManager escortTaskManager;

	
	protected int getType() {
		return PropsType.DRUG_PROPS_TYPE;
	}

	/**
	 * 处理道具效果
	 * 
	 * @param userDomain		用户域模型对象
	 * @param userProps			用户道具对象
	 * @param count				使用数量
	 */
	
	public int effect(UserDomain userDomain, UserCoolTime userCoolTime, CoolTimeConfig coolTime, UserProps userProps, int count) {
		if(!userProps.validBackpack(BackpackType.DEFAULT_BACKPACK)) {
			return NOT_IN_BACKPACK;
		}
		
		PropsConfig propsConfig = userProps.getPropsConfig();
		if(propsConfig == null) {
			return ITEM_NOT_FOUND;
		} else if(propsConfig.getCdId() > 0 && coolTime == null){
			return BASEDATA_NOT_FOUND;
		}
		
//		PlayerBattle battle = userDomain.getBattle();
		switch (propsConfig.getChildType()) {
			case PropsChildType.EXP_ITEM:			return eatExpItem(userDomain, userCoolTime, coolTime, userProps, count); 		
			case PropsChildType.HP_DRUG_ITEM:		return eatHpDrug(userDomain, userCoolTime, coolTime, userProps, count); 			
			case PropsChildType.MP_DRUG_ITEM:		return eatMpDrug(userDomain, userCoolTime, coolTime, userProps, count); 		
			case PropsChildType.RESURRECT_ITEM:		return eatResurrectDrug(userDomain, userCoolTime, coolTime, userProps, count); 	
			case PropsChildType.HPBAG_DRUG_ITEM:	return eatHpBagDrug(userDomain, userCoolTime, coolTime, userProps, count); 		
			case PropsChildType.MPBAG_DRUG_ITEM:	return eatMpBagDrug(userDomain, userCoolTime, coolTime, userProps, count); 		
			case PropsChildType.PET_HPBAG_DRUG_ITEM:return eatPetHpBagDrug(userDomain, userCoolTime, coolTime, userProps, count); 		
			default:								return FAILURE;
		}
	}

	/**
	 * 吃复活药
	 * 
	 * @param  battle			角色战斗对象
	 * @param  userProps		用户物品对象
	 * @param  count			使用数量
	 * @param  props			道具数量
	 * @return {@link Integer}	道具模块返回值
	 */
	@SuppressWarnings("unchecked")
	private int eatResurrectDrug(UserDomain userDomain, UserCoolTime userCoolTime, CoolTimeConfig coolTime, UserProps userProps, int count) {
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		long playerId = battle.getId();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		if(!battle.isDead()) {
			return PLAYER_DEADED;
		} else if(userProps.getCount() < count) {
			return ITEM_NOT_ENOUGH;
		} else if(!userProps.validBackpack(backpack)) {
			return NOT_IN_BACKPACK;
		} else if(userProps.isTrading()) {
			return ITEM_CANNOT_USE;
		}
		
		ChainLock lock = LockUtils.getLock(player, battle, userDomain.getPackLock(), userCoolTime);
		try {
			lock.lock();
			if(!battle.isDead()) {
				return PLAYER_DEADED;
			} else if(userProps.getCount() < count) {
				return ITEM_NOT_ENOUGH;
			} else if(coolTime != null && userCoolTime.isCoolTiming(coolTime.getId())) {
				return COOL_TIMING;
			}
			
		    int backHp = (int)(battle.getHpMax() * 0.3f);//只恢复30%的血量
			battle.setHp(backHp);
			player.setReviveProteTime(DateUtil.getCurrentSecond() + 3);
			userProps.decreaseItemCount(count);
			if(coolTime != null){
				userCoolTime.addCoolTime(coolTime.getId(), coolTime.getCoolTime());
			}
			propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, userProps);
			dbService.submitUpdate2Queue(battle, userProps);
		} finally {
			lock.unlock();
		}
		
		Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, userProps);
		UserPushHelper.pushPlayerRecurrent(userDomain.getUnitId(), battle.getHp(), playerIdList);
		return SUCCESS;
	}
	
	/**
	 * 吃经验丹
	 * @param battle
	 * @param userProps
	 * @param count
	 * @param props
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private int eatExpItem(UserDomain userDomain, UserCoolTime userCoolTime, 
				CoolTimeConfig coolTime, UserProps userProps, int count) {
		PlayerBattle battle = userDomain.getBattle();
		long playerId = battle.getId();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		if(battle.isDead()) {
			return PLAYER_DEADED;
		} else if(userProps.getCount() < count) {
			return ITEM_NOT_ENOUGH;
		} else if(!userProps.validBackpack(backpack)) {
			return NOT_IN_BACKPACK;
		} else if(userProps.isTrading()) {
			return ITEM_CANNOT_USE;
		}
		
		PropsConfig props = userProps.getPropsConfig();
		ChainLock lock = LockUtils.getLock(battle, userDomain.getPackLock());
		try {
			lock.lock();
			if(battle.isDead()) {
				return PLAYER_DEADED;
			} else if(userProps.getCount() < count) {
				return ITEM_NOT_ENOUGH;
			} else if(coolTime != null && userCoolTime.isCoolTiming(coolTime.getId())) {
				return COOL_TIMING;
			}
			if(coolTime != null){
				userCoolTime.addCoolTime(coolTime.getId(), coolTime.getCoolTime());
			}
			
			int addExp = (int)Math.round(props.getAttrValue());
			battle.increaseExp( addExp );
			userProps.decreaseItemCount(count);
			propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, userProps);
			dbService.submitUpdate2Queue(battle, userProps);
			if(addExp != 0){ //记录经验日志
				ExpLogger.itemExp(userDomain, props, addExp);
			}
		} finally {
			lock.unlock();
		}
		
		Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, userProps);
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, Arrays.asList(userDomain.getUnitId()), AttributeKeys.LEVEL,  AttributeKeys.EXP);
		return SUCCESS;
	}
	
	/**
	 * 吃长生包(HPBag)
	 * @param battle
	 * @param userProps
	 * @param count
	 * @param props
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private int eatHpBagDrug(UserDomain userDomain, UserCoolTime userCoolTime,
				CoolTimeConfig coolTime, UserProps userProps, int count) {
		PlayerBattle battle = userDomain.getBattle();
		long playerId = battle.getId();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		if(battle.isDead()) {
			return PLAYER_DEADED;
		} else if(userProps.getCount() < count) {
			return ITEM_NOT_ENOUGH;
		} else if(!userProps.validBackpack(backpack)) {
			return NOT_IN_BACKPACK;
		} else if(userProps.isTrading()) {
			return ITEM_CANNOT_USE;
		}
		
		if(escortTaskManager.isEscortStatus(battle)){
			return TreasureConstant.TREASURE_ESCORT_STATUS;
		}
		
		PropsConfig props = userProps.getPropsConfig();
		ChainLock lock = LockUtils.getLock(battle, userDomain.getPackLock(), userCoolTime);
		try {
			lock.lock();
			if(battle.isDead()) {
				return PLAYER_DEADED;
			} else if(userProps.getCount() < count) {
				return ITEM_NOT_ENOUGH;
			} else if(coolTime != null && userCoolTime.isCoolTiming(coolTime.getId())) {
				return COOL_TIMING;
			} else if(battle.getHpBag() > 0){
				return PropsConstant.ONE_PORTABLE_BAG_USING;
			}
			
			if(coolTime != null){
				userCoolTime.addCoolTime(coolTime.getId(), coolTime.getCoolTime());
			}
			int actionValue = (int)Math.round(props.getAttrValue());
			battle.setHpBag( actionValue );
			battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
			userProps.decreaseItemCount(count);
			propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, userProps);
			dbService.submitUpdate2Queue(battle, userProps);
		} finally {
			lock.unlock();
		}
		
		Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, userProps);
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, Arrays.asList(userDomain.getUnitId()), AttributeKeys.HP_BAG,  AttributeKeys.HP);
		return SUCCESS;
	}
	
	/**
	 * 吃将•长生包(HPBag)
	 * @param battle
	 * @param userProps
	 * @param count
	 * @param props
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private int eatPetHpBagDrug(UserDomain userDomain, UserCoolTime userCoolTime,
			CoolTimeConfig coolTime, UserProps userProps, int count) {
		PlayerBattle battle = userDomain.getBattle();
		long playerId = battle.getId();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		if(battle.isDead()) {
			return PLAYER_DEADED;
		} else if(userProps.getCount() < count) {
			return ITEM_NOT_ENOUGH;
		} else if(!userProps.validBackpack(backpack)) {
			return NOT_IN_BACKPACK;
		} else if(userProps.isTrading()) {
			return ITEM_CANNOT_USE;
		}
		
		if(escortTaskManager.isEscortStatus(battle)){
			return PropsConstant.USEPORTABLEBAG_ESCORT_STATUS;
		}
		
		PropsConfig props = userProps.getPropsConfig();
		ChainLock lock = LockUtils.getLock(battle, userDomain.getPackLock(), userCoolTime);
		try {
			lock.lock();
			if(battle.isDead()) {
				return PLAYER_DEADED;
			} else if(userProps.getCount() < count) {
				return ITEM_NOT_ENOUGH;
			} else if(coolTime != null && userCoolTime.isCoolTiming(coolTime.getId())) {
				return COOL_TIMING;
			} else if(battle.getPetHpBag() > 0){
				return PropsConstant.ONE_PORTABLE_BAG_USING;
			}
			
			if(coolTime != null){
				userCoolTime.addCoolTime(coolTime.getId(), coolTime.getCoolTime());
			}
			battle.setPetHpBag( (int)Math.round(props.getAttrValue()) );
			userProps.decreaseItemCount(count);
			propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, userProps);
			dbService.submitUpdate2Queue(battle, userProps);
		} finally {
			lock.unlock();
		}
		
//		PetDomain petDomain = petManager.getGoFighting(playerId);
//		if(petDomain != null){
//			petDomain.getBattle().setFlushable(Flushable.FLUSHABLE_NORMAL);
//		}
		
		UnitId unitId = userDomain.getUnitId();
//		Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, userProps);
		UserPushHelper.pushAttribute2AreaMember(playerId, Arrays.asList(playerId), Arrays.asList(unitId), AttributeKeys.PET_HP_BAG);
		return SUCCESS;
	}
	
	/**
	 * 吃归内丹(MPBag)
	 * @param battle
	 * @param userProps
	 * @param count
	 * @param props
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private int eatMpBagDrug(UserDomain userDomain, UserCoolTime userCoolTime, CoolTimeConfig coolTime, UserProps userProps, int count) {
		PlayerBattle battle = userDomain.getBattle();
		long playerId = battle.getId();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		if(battle.isDead()) {
			return PLAYER_DEADED;
		} else if(userProps.getCount() < count) {
			return ITEM_NOT_ENOUGH;
		} else if(!userProps.validBackpack(backpack)) {
			return NOT_IN_BACKPACK;
		} else if(userProps.isTrading()) {
			return ITEM_CANNOT_USE;
		}
		
		if(escortTaskManager.isEscortStatus(battle)){
			return TreasureConstant.TREASURE_ESCORT_STATUS;
		}
		
		PropsConfig props = userProps.getPropsConfig();
		ChainLock lock = LockUtils.getLock(battle, userDomain.getPackLock(), userCoolTime);
		try {
			lock.lock();
			if(battle.isDead()) {
				return PLAYER_DEADED;
			} else if(userProps.getCount() < count) {
				return ITEM_NOT_ENOUGH;
			} else if(coolTime != null && userCoolTime.isCoolTiming(coolTime.getId())) {
				return COOL_TIMING;
			} else if(battle.getMpBag() > 0){
				return PropsConstant.ONE_PORTABLE_BAG_USING;
			}
			
			userProps.decreaseItemCount(count);
			if(coolTime != null){
				userCoolTime.addCoolTime(coolTime.getId(), coolTime.getCoolTime());
			}
			battle.setMpBag( (int)Math.round(props.getAttrValue()) );
			battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
			propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, userProps);
			dbService.submitUpdate2Queue(battle, userProps);
		} finally {
			lock.unlock();
		}
		
		Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, userProps);
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, Arrays.asList(userDomain.getUnitId()), AttributeKeys.MP_BAG,  AttributeKeys.MP);
		return SUCCESS;
	}
	
	/**
	 * 吃HP/MP/SP药物.
	 * 
	 * @param battle
	 * @param userProps
	 * @param count
	 * @param propsConfig
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private int eatHpDrug(UserDomain userDomain, UserCoolTime userCoolTime, CoolTimeConfig coolTime, UserProps userProps, int count) {
		PlayerBattle battle = userDomain.getBattle();
		if(battle.isDead()) {
			return PLAYER_DEADED;
		} else if(userProps.getCount() < count) {
			return ITEM_NOT_ENOUGH;
		} else if(!userProps.validBackpack(BackpackType.DEFAULT_BACKPACK)) {
			return NOT_IN_BACKPACK;
		} else if(userProps.isTrading()) {
			return ITEM_CANNOT_USE;
		}
		
		long playerId = battle.getId();
		PropsConfig propsConfig = userProps.getPropsConfig();
		int attrValue = (int)Math.round(propsConfig.getAttrValue());
		ChainLock lock = LockUtils.getLock(battle, userDomain.getPackLock(), userCoolTime);
		try {
			lock.lock();
			if(battle.isDead()) {
				return PLAYER_DEADED;
			} else if(userProps.getCount() < count) {
				return ITEM_NOT_ENOUGH;
			}
			
			int canAddHp = battle.getHpMax() - battle.getHp();
			if(canAddHp <= 0) {
				return ROLE_HP_FULLED;
			} 
			
			
			if(coolTime != null && userCoolTime.isCoolTiming(coolTime.getId())) {
				return COOL_TIMING;
			}
			
			
			battle.increaseHp(Math.max(0, Math.min(attrValue, canAddHp)));
			userProps.decreaseItemCount(count);
			propsManager.removeUserPropsIfCountNotEnough(playerId, BackpackType.DEFAULT_BACKPACK, userProps);
			dbService.submitUpdate2Queue(battle, userProps);
			if(coolTime != null){
				userCoolTime.addCoolTime(coolTime.getId(), coolTime.getCoolTime());
			}
		} finally {
			lock.unlock();
		}
		
		Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
		MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, userProps);
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, Arrays.asList(userDomain.getUnitId()), AttributeKeys.HP);
		return SUCCESS;
	}
	/**
	 * 吃MP药物.
	 * 
	 * @param battle
	 * @param userProps
	 * @param count
	 * @param propsConfig
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private int eatMpDrug(UserDomain userDomain, UserCoolTime userCoolTime, CoolTimeConfig coolTime, UserProps userProps, int count) {
		PlayerBattle battle = userDomain.getBattle();
		long playerId = battle.getId();
		long userItemId = userProps.getId();
		if(battle.isDead()) {
			return PLAYER_DEADED;
		} else if(userProps.getCount() < count) {
			return ITEM_NOT_ENOUGH;
		} else if(!userProps.validBackpack(BackpackType.DEFAULT_BACKPACK)) {
			return NOT_IN_BACKPACK;
		} else if(userProps.isTrading()) {
			return ITEM_CANNOT_USE;
		}
		
		PropsConfig props = userProps.getPropsConfig();
		int attrValue = (int)Math.round(props.getAttrValue());
		ChainLock lock = LockUtils.getLock(battle, userDomain.getPackLock(), userCoolTime);
		try {
			lock.lock();
			if(battle.isDead()) {
				return PLAYER_DEADED;
			} 
				
			if(userProps.getCount() < count) {
				return ITEM_NOT_ENOUGH;
			}
			
			int canAddMp = battle.getMpMax() - battle.getMp();
			if(canAddMp <= 0) {
				return ROLE_MP_FULLED;
			}
			
			if(coolTime != null && userCoolTime.isCoolTiming(coolTime.getId())) {
				return COOL_TIMING;
			}
			battle.increaseMp(Math.max(0, Math.min(attrValue, canAddMp)));
			userProps.decreaseItemCount(count);
			propsManager.removeUserPropsIfCountNotEnough(playerId, BackpackType.DEFAULT_BACKPACK, userProps);
			dbService.submitUpdate2Queue(battle, userProps);
			if(coolTime != null){
				userCoolTime.addCoolTime(coolTime.getId(), coolTime.getCoolTime());
			}
		} catch (Exception e) {
			LOGGER.error("角色:[{}] 使用用户道具:[{}] 异常", playerId, userItemId);
			LOGGER.error("{}", e);
			return FAILURE;
		} finally {
			lock.unlock();
		}
		
		Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, Arrays.asList(userDomain.getUnitId()), AttributeKeys.MP);
		MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, userProps);
		return SUCCESS;
	}
}
