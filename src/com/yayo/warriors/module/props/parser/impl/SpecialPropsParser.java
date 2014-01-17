package com.yayo.warriors.module.props.parser.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.vip.model.VipFunction.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.push.Pusher;
import com.yayo.warriors.basedb.model.CoolTimeConfig;
import com.yayo.warriors.basedb.model.HorseConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.basedb.model.VipConfig;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.common.helper.VipPushHelper;
import com.yayo.warriors.module.alliance.facade.AllianceFacade;
import com.yayo.warriors.module.alliance.manager.AllianceManager;
import com.yayo.warriors.module.chat.facade.ChannelFacade;
import com.yayo.warriors.module.cooltime.entity.UserCoolTime;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.horse.entity.Horse;
import com.yayo.warriors.module.horse.facade.HorseFacade;
import com.yayo.warriors.module.horse.manager.HorseManager;
import com.yayo.warriors.module.horse.vo.HorseVo;
import com.yayo.warriors.module.logger.log.CouponLogger;
import com.yayo.warriors.module.logger.log.SilverLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.parser.AbstractEffectParser;
import com.yayo.warriors.module.props.type.PropsChildType;
import com.yayo.warriors.module.props.type.PropsType;
import com.yayo.warriors.module.team.facade.TeamFacade;
import com.yayo.warriors.module.treasure.constant.TreasureConstant;
import com.yayo.warriors.module.treasure.facade.TreasureFacade;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.module.vip.entity.PlayerVip;
import com.yayo.warriors.module.vip.manager.VipManager;
import com.yayo.warriors.module.vip.model.VipDomain;
import com.yayo.warriors.module.vip.rule.VipRule;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.horse.HorseCmd;
import com.yayo.warriors.socket.handler.treasure.TreasureCmd;


@Component
public class SpecialPropsParser extends AbstractEffectParser {

	@Autowired
	private PropsManager propsManager;
	@Autowired
	private DbService dbService;
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private TreasureFacade treasureFacade;
	@Autowired
	private Pusher pusher;
	@Autowired
	private VipManager vipManager;
	@Autowired
	private MapFacade mapFacade;
	@Autowired
	private HorseFacade horseFacade;
	@Autowired
	private HorseManager horseManager;
	@Autowired
	private AllianceFacade allianceFacade;
	@Autowired
	private ChannelFacade channelFacade;
	@Autowired
	private TeamFacade teamFacade;
	@Autowired
	private AllianceManager allianceManager;
	
	
	protected int getType() {
		return PropsType.OTHER_PROPS_TYPE;
	}
	
	
	public int effect(UserDomain userDomain, UserCoolTime userCoolTime, CoolTimeConfig coolTime, UserProps userProps, int count) {
		if(!userProps.validBackpack(BackpackType.DEFAULT_BACKPACK)) {
			return NOT_IN_BACKPACK;
		} else if(userProps.getPlayerId() != userDomain.getId()) {
			return BELONGS_INVALID;
		} else if(userProps.isOutOfExpiration()) {
			return OUT_OF_EXPIRATION;
		} 
		
		int baseId = userProps.getBaseId();
		PropsConfig propsConfig = propsManager.getPropsConfig(baseId);
		if(propsConfig == null) {
			return ITEM_NOT_FOUND;
		} else if(!propsConfig.isCanUse()){
			return ITEM_CANNOT_USE;
		} else if(propsConfig.getCdId() > 0 && coolTime == null){
			return BASEDATA_NOT_FOUND;
		}
		
		int result = FAILURE;
		int childType = propsConfig.getChildType();
		switch (childType) {
			case PropsChildType.SILVER_PROPS_TYPE   : result = useSilverItem(userDomain, userProps, count, propsConfig, userCoolTime, coolTime);	break;
			case PropsChildType.COUPON_PROPS_TYPE   : result = useCouponItem(userDomain, userProps, count, propsConfig, userCoolTime, coolTime);	break;
			case PropsChildType.VIP_CARD_PROPS_TYPE : result = useVipCard(userDomain, userProps, count, propsConfig, userCoolTime, coolTime);		break;
			case PropsChildType.TREASURE_PROPS_TYPE : result = useTreasureProps(userDomain, userProps, count, propsConfig, userCoolTime, coolTime);	break;
			case PropsChildType.HORSE_EVOLVE_TYPE : result = useHorseEvolveItem(userDomain, userProps, count, propsConfig, userCoolTime, coolTime);	break;
			case PropsChildType.CONVENE_ALLIANCE : result = useConveneItem(userDomain, userProps, count, propsConfig, userCoolTime, coolTime);		break;
			case PropsChildType.CONVENE_CAMP : result = useConveneItem(userDomain, userProps, count, propsConfig, userCoolTime, coolTime);			break;
			case PropsChildType.CONVENE_TEAM : result = useConveneItem(userDomain, userProps, count, propsConfig, userCoolTime, coolTime);			break;
	    }
		
		return result;
	}
	
	
	@SuppressWarnings("unchecked")
	private int useSilverItem(UserDomain userDomain, UserProps userProps, int count, PropsConfig propsConfig, UserCoolTime userCoolTime, CoolTimeConfig coolTime) {
		long playerId = userDomain.getPlayerId();
		PlayerBattle battle = userDomain.getBattle();
		Player player = userDomain.getPlayer();
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
		
		ChainLock lock = LockUtils.getLock(player, battle, userDomain.getPackLock(), userCoolTime);
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
			player.increaseSilver( propsConfig.getAttrValueRound() );
			
			userProps.decreaseItemCount(count);
			dbService.submitUpdate2Queue(player, userProps);
			propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, userProps);
		} finally {
			lock.unlock();
		}
		
		LoggerGoods outcomeProps = LoggerGoods.outcomeProps(userProps.getId(), userProps.getBaseId(), count);
		SilverLogger.inCome(Source.PROPS_USE_PROPS, propsConfig.getAttrValueRound(), player, outcomeProps );
		
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, userProps);
		UserPushHelper.pushAttribute2AreaMember(playerId, Arrays.asList(playerId), Arrays.asList(userDomain.getUnitId()), AttributeKeys.SILVER);
		return SUCCESS;
	}
	

	@SuppressWarnings("unchecked")
	private int useHorseEvolveItem(UserDomain userDomain, UserProps userProps, int count, PropsConfig propsConfig, UserCoolTime userCoolTime, CoolTimeConfig coolTime) {
		long playerId = userDomain.getPlayerId();
		PlayerBattle battle = userDomain.getBattle();
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
		
		Horse horse = horseManager.getHorse(battle);
		if(horse == null){
			return LEVEL_INVALID;	
		}
		
		int newLevel = propsConfig.getAttrValueRound();
		HorseConfig nextConfig = this.horseManager.getHorseConfig(newLevel);
		if (nextConfig == null) {
			LOGGER.error("玩家[{}],坐骑直接升级,坐骑配置[{}]不存在", playerId, newLevel);
			return BASEDATA_NOT_FOUND;
		}
		
		int horseLevel = horse.getLevel();
		if(horseLevel >= newLevel || horseLevel + 10 < newLevel ){
			return LEVEL_INVALID;
		}
		
		ChainLock lock = LockUtils.getLock(horse, userDomain.getPackLock(), userCoolTime);
		try {
			lock.lock();
			if(battle.isDead()) {
				return PLAYER_DEADED;
			} else if(userProps.getCount() < count) {
				return ITEM_NOT_ENOUGH;
			} else if(coolTime != null && userCoolTime.isCoolTiming(coolTime.getId())) {
				return COOL_TIMING;
			}
			
			if(horseLevel >= newLevel || horseLevel + 10 < newLevel ){
				return LEVEL_INVALID;
			}
			
			if(coolTime != null){
				userCoolTime.addCoolTime(coolTime.getId(), coolTime.getCoolTime());
			}

			horse.setLevel(newLevel);
			horse.addHorseMount(nextConfig.getModel());
			battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
			horse.setExp(0);
			
			userProps.decreaseItemCount(count);
			dbService.submitUpdate2Queue(horse, userProps);
			propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, userProps);
			
		} finally {
			lock.unlock();
		}
		
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, userProps);
		
		horse = horseManager.getHorse(battle);
		Response response = Response.defaultResponse(Module.HORSE, HorseCmd.DEFIND_PROPS_FANCY);
		Map<String,Object> map = new HashMap<String,Object>(2);
		map.put(ResponseKey.RESULT, SUCCESS);
		map.put(ResponseKey.HORSE, HorseVo.valueOf(horse) );
		response.setValue(map);
		pusher.pushMessage(playerId, response);
		
		return SUCCESS;
	}

	private int useConveneItem(UserDomain userDomain, UserProps userProps, int count, PropsConfig propsConfig, UserCoolTime userCoolTime, CoolTimeConfig coolTime) {
		long playerId = userDomain.getPlayerId();
		PlayerBattle battle = userDomain.getBattle();
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
		
		ChainLock lock = LockUtils.getLock(userDomain.getPackLock(), userCoolTime);
		try {
			lock.lock();
			if(battle.isDead()) {
				return PLAYER_DEADED;
			} else if(userProps.getCount() < count) {
				return ITEM_NOT_ENOUGH;
			} else if(coolTime != null && userCoolTime.isCoolTiming(coolTime.getId())) {
				return COOL_TIMING;
			}
			int result = mapFacade.sendConveneInvite(userDomain, userCoolTime, propsConfig);
			if(result != SUCCESS){
				return result;
			}
			
			if(coolTime != null){
				userCoolTime.addCoolTime(coolTime.getId(), coolTime.getCoolTime());
			}
			
			userProps.decreaseItemCount(count);
			dbService.submitUpdate2Queue(userProps);
			propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, userProps);
		} finally {
			lock.unlock();
		}

		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, userProps);
		
		return SUCCESS;
	}
	

	@SuppressWarnings("unchecked")
	private int useCouponItem(UserDomain userDomain, UserProps userProps, int count, PropsConfig propsConfig, UserCoolTime userCoolTime, CoolTimeConfig coolTime) {
		long playerId = userDomain.getPlayerId();
		PlayerBattle battle = userDomain.getBattle();
		Player player = userDomain.getPlayer();
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
		
		ChainLock lock = LockUtils.getLock(player, battle, userDomain.getPackLock(), userCoolTime);
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
			player.increaseCoupon( propsConfig.getAttrValueRound() );
			
			userProps.decreaseItemCount(count);
			dbService.submitUpdate2Queue(player, userProps);
			propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, userProps);
		} finally {
			lock.unlock();
		}
		
		LoggerGoods outcomeProps = LoggerGoods.outcomeProps(userProps.getId(), userProps.getBaseId(), count);
		CouponLogger.inCome(Source.PROPS_USE_PROPS, propsConfig.getAttrValueRound(), player, outcomeProps );
		if(propsConfig.getAttrValueRound() != 0) {
			CouponLogger.inCome(Source.PROPS_USE_PROPS, propsConfig.getAttrValueRound(), player, outcomeProps);
		}
		
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, userProps);
		UserPushHelper.pushAttribute2AreaMember(playerId, Arrays.asList(playerId), Arrays.asList(userDomain.getUnitId()), AttributeKeys.COUPON);
		return SUCCESS;
	}
	

	private int useTreasureProps(UserDomain userDomain, UserProps userProps, int count, PropsConfig propsConfig, UserCoolTime userCoolTime, CoolTimeConfig coolTime) {
		long playerId = userDomain.getPlayerId();
		PlayerBattle battle = userDomain.getBattle();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		if(battle.isDead()) {
			return PLAYER_DEADED;
		} else if(userProps.getCount() < count) {
			return ITEM_NOT_ENOUGH;
		} else if(!userProps.validBackpack(backpack)) {
			return NOT_IN_BACKPACK;
		} else if(userProps.isTrading()) {
			return ITEM_CANNOT_USE;
		} else if(userProps.isOutOfExpiration()){
			return TreasureConstant.TREASURE_TIMEOUT;
		}
		
		ChainLock lock = LockUtils.getLock(userDomain.getPackLock(), userCoolTime);
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
		} finally {
			lock.unlock();
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		int result = treasureFacade.openTreansureProps(playerId, userProps.getId(), resultMap);
		resultMap.put(ResponseKey.RESULT, result);
		Response response = Response.defaultResponse(Module.TREASURE, TreasureCmd.PUSH_TREANSURE_PROPS, resultMap);
		pusher.pushMessage(playerId, response);
		
		return SUCCESS;
	}
	

	@SuppressWarnings("unchecked")
	private int useVipCard(UserDomain userDomain, UserProps userProps, int count, PropsConfig propsConfig, UserCoolTime userCoolTime, CoolTimeConfig coolTime) {
		long playerId = userDomain.getPlayerId();
		PlayerBattle battle = userDomain.getBattle();
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
		
		PlayerVip playerVip = vipManager.getPlayerVip(userDomain.getId());
		int propsVipLevel = (int) Math.round(propsConfig.getAttrValue());
		if (propsVipLevel < playerVip.getVipLevel()) {
			return LEVEL_INVALID;
		}
		
		VipConfig vipConfig = vipManager.getVipConfig(propsVipLevel);
		if (vipConfig == null) {
			return BASEDATA_NOT_FOUND;
		}
		
		VipDomain vipDomain = VipDomain.valueOf(playerVip, vipConfig);     // 构建VIP域
		
		ChainLock lock = LockUtils.getLock(playerVip, userDomain.getPackLock(), userCoolTime);
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
			
			long vipOutOfTime = vipDomain.longValue(VipOutOfDateTime);     // 获取VIP时长
			playerVip.alterEndTime(propsVipLevel, vipOutOfTime);                
			playerVip.initVipParams();
			
			userProps.decreaseItemCount(count);
			dbService.submitUpdate2Queue(userProps, playerVip);
			vipManager.put2VipCache(userDomain.getId(), vipDomain);
			propsManager.removeUserPropsIfCountNotEnough(playerId, BackpackType.DEFAULT_BACKPACK, userProps);
		} finally {
			lock.unlock();
		}
		
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, userProps);
		
		List<UnitId> unitIds = Arrays.asList(userDomain.getUnitId());
		Collection<Long> playerIdList = mapFacade.getScreenViews(userDomain.getId());
		UserPushHelper.pushAttribute2AreaMember(userDomain.getId(), playerIdList, unitIds, AttributeKeys.VIP_INFO);
		
		VipRule.pushVipNotice(userDomain.getPlayer(), vipConfig);       // 推送VIP公告
		return SUCCESS;
	}

}
