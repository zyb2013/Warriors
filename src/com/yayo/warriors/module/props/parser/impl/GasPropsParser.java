package com.yayo.warriors.module.props.parser.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.props.constant.PropsConstant.*;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.basedb.model.CoolTimeConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.cooltime.entity.UserCoolTime;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.parser.AbstractEffectParser;
import com.yayo.warriors.module.props.type.PropsChildType;
import com.yayo.warriors.module.props.type.PropsType;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;

@Component
public class GasPropsParser extends AbstractEffectParser{

	@Autowired
	private DbService dbService;
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private PropsManager propsManager;
	
	
	protected int getType() {
		return PropsType.PLAYER_TRAIN_PROPS_TYPE;
	}
	
	@SuppressWarnings("unchecked")
	
	public int effect(UserDomain userDomain, UserCoolTime userCoolTime, CoolTimeConfig coolTime, UserProps userProps, int count) {
		PropsConfig propsConfig = propsManager.getPropsConfig( userProps.getBaseId() );
		if(propsConfig == null){
			return ITEM_NOT_FOUND;
		}
		
		if(propsConfig.getChildType() != PropsChildType.GAS_PROPS_TYPE){
			return ITEM_CANNOT_USE;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle.getGas() >= battle.getGasMax()){
			return GAS_IS_FULL;
		}

		long playerId = userDomain.getPlayerId();
		/* 扣减物品*/
		ChainLock lock = LockUtils.getLock(userDomain.getPackLock(), battle, userCoolTime);
		try {
			lock.lock();
		    if(userProps.getCount() < count) {
				return ITEM_NOT_ENOUGH;
			} else if(battle.getGas() >= battle.getGasMax()){
				return GAS_IS_FULL;
			} else if(coolTime != null && userCoolTime.isCoolTiming(coolTime.getId())) {
				return COOL_TIMING;
			}
		    
		    userProps.decreaseItemCount(count);
		    if(coolTime != null){
		    	userCoolTime.addCoolTime(coolTime.getId(), coolTime.getCoolTime());
		    }
			battle.increaseGas((int)Math.round(propsConfig.getAttrValue()));
			dbService.submitUpdate2Queue(battle, userProps);
			propsManager.removeUserPropsIfCountNotEnough(playerId, userProps.getBackpack(), userProps);
		} finally {
			lock.unlock();
		}
		
		//推送道具变更
		List<Long> playerIdList = Arrays.asList(playerId);
		List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, AttributeKeys.GAS, AttributeKeys.GAS_MAX);
		MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, userProps);
		return SUCCESS;
	}

}
