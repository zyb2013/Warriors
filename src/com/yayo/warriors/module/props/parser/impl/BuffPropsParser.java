package com.yayo.warriors.module.props.parser.impl;

import static com.yayo.warriors.constant.CommonConstant.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.push.Pusher;
import com.yayo.warriors.basedb.model.CoolTimeConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.basedb.model.SkillEffectConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.buffer.entity.UserBuffer;
import com.yayo.warriors.module.buffer.model.Buffer;
import com.yayo.warriors.module.buffer.vo.BufferVO;
import com.yayo.warriors.module.cooltime.entity.UserCoolTime;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.constant.PropsConstant;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.parser.AbstractEffectParser;
import com.yayo.warriors.module.props.type.PropsChildType;
import com.yayo.warriors.module.props.type.PropsType;
import com.yayo.warriors.module.skill.type.SkillEffectType;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.handler.fight.FightCmd;
import com.yayo.warriors.type.ElementType;

/**
 * Buff效果类型道具的解析
 * @author jonsai
 *
 */
@Component
public class BuffPropsParser extends AbstractEffectParser {
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private DbService dbService;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private Pusher pusher;
	
	
	protected int getType() {
		return PropsType.BUFFER_EFFECT_TYPE;
	}

	
	public int effect(UserDomain userDomain, UserCoolTime userCoolTime, CoolTimeConfig coolTime, UserProps userProps, int count) {
		if(!userProps.validBackpack(BackpackType.DEFAULT_BACKPACK)) {
			return NOT_IN_BACKPACK;
		}
		
		PropsConfig propsConfig = userProps.getPropsConfig();
		if(propsConfig == null) {
			return ITEM_NOT_FOUND;
		} else if(propsConfig.getCdId() > 0 && coolTime == null){
			return BASEDATA_NOT_FOUND;
		} else if(userProps.isTrading()) {
			return ITEM_CANNOT_USE;
		}
		
		switch (propsConfig.getChildType()) {
			case PropsChildType.INC_BUFF_TYPE:	return useBuffProps(userDomain, userCoolTime, coolTime, userProps, count, propsConfig);
		}
		return PropsConstant.ITEM_CANNOT_USE;
	}
	
	/**
	 * 使用buff道具
	 * @param userDomain
	 * @param userCoolTime
	 * @param coolTime
	 * @param userProps
	 * @param count
	 * @param propsConfig
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private int useBuffProps(UserDomain userDomain, UserCoolTime userCoolTime, CoolTimeConfig coolTime, UserProps userProps, int count, PropsConfig propsConfig){
		PlayerBattle battle = userDomain.getBattle();
		long playerId = userDomain.getPlayerId();
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
		
		BufferVO[] bufferInfoArray = null;
		UserBuffer userBuffer = userDomain.getUserBuffer();
		ChainLock lock = LockUtils.getLock(battle, userDomain.getPackLock(), userCoolTime, userBuffer);
		try {
			lock.lock();
			if(userProps.isTrading()) {
				return ITEM_CANNOT_USE;
			} else if(userProps.getCount() < count){
				return ITEM_NOT_ENOUGH;
			} else if(coolTime != null && userCoolTime.isCoolTiming(coolTime.getId())) {
				return COOL_TIMING;
			}
			
			//处理buff效果
			ResultObject<Collection<BufferVO>> resultObject = processEffect(userBuffer, propsConfig);
			if(resultObject.getResult() != PropsConstant.SUCCESS){
				return resultObject.getResult();
			}
			
			if(coolTime != null){
				userCoolTime.addCoolTime(coolTime.getId(), coolTime.getCoolTime());
			}
			
			userProps.decreaseItemCount(count);
			userBuffer.updateItemBufferInfos(false);
			bufferInfoArray = resultObject.getValue().toArray(new BufferVO[resultObject.getValue().size()]);
			dbService.submitUpdate2Queue(userProps, userBuffer);
			propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, userProps);
			battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
		} finally {
			lock.unlock();
		}
		
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, userProps);
		Response response = Response.defaultResponse(Module.FIGHT, FightCmd.GET_USER_BUFF, bufferInfoArray);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("角色:[{}] 当前的BUFF信息:[{}] ", playerId, Arrays.toString(bufferInfoArray));
		}
		pusher.pushMessage(playerId, response);
		return PropsConstant.SUCCESS;
	}
	
	/**
	 * 处理buff效果(外部使用锁userBuffer)
	 * @param userDomain
	 * @param propsConfig
	 * @return
	 */
	private ResultObject<Collection<BufferVO>> processEffect(UserBuffer userBuffer, PropsConfig propsConfig){
		int skillEffectId = (int)Math.round(propsConfig.getAttrValue());
		SkillEffectConfig skillEffectConfig = resourceService.get(skillEffectId, SkillEffectConfig.class);
		if(skillEffectConfig == null){
			return ResultObject.ERROR(PropsConstant.BASEDATA_NOT_FOUND);
		}
		
		int type = skillEffectConfig.getEffectType();
		SkillEffectType effectType = SkillEffectType.getSkillEffectType(type);
		if(effectType == null) {
			return ResultObject.ERROR(PropsConstant.FAILURE);
		}
		
		Collection<BufferVO> bufferVOList = new ArrayList<BufferVO>(0);
		int rateValue = effectType.getRateValue();
		int actionValue = skillEffectConfig.calcSkillEffect(rateValue, 1).intValue();
		Map<Integer, Buffer> itemBufferInfos = userBuffer.getItemBufferInfos();
		for(int id : itemBufferInfos.keySet()){
			Buffer buffer = itemBufferInfos.get(id);
			SkillEffectConfig skillEffect = resourceService.get(id, SkillEffectConfig.class);
			if(skillEffect == null || buffer == null || buffer.isTimeOut() ) {
				continue;
			}
			
			if(skillEffect.getBuffType() == skillEffectConfig.getBuffType()){
				if(buffer.getDamage() > actionValue){
					return ResultObject.ERROR(PropsConstant.BUFF_ACTIONVALUE_LOW);
				}else {
					buffer.setEndTime(0);
					bufferVOList.add(BufferVO.valueOf(buffer));
				}
			}
		}
		
		long playerId = userBuffer.getId();
		long mil = System.currentTimeMillis();
		int cycle = skillEffectConfig.getCycle();
		int unitType = ElementType.PLAYER.ordinal();
		long endTime = mil + skillEffectConfig.getEffectTime();
		Buffer buffer = Buffer.valueOf(skillEffectId, 1, actionValue, cycle, mil, endTime, playerId, unitType );
		bufferVOList.add(BufferVO.valueOf(buffer));
		itemBufferInfos.put(skillEffectId, buffer);
		return ResultObject.SUCCESS(bufferVOList);
	}
}
