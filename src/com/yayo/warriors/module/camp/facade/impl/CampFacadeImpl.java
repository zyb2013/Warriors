package com.yayo.warriors.module.camp.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.camp.constant.CampConstant.*;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.EnumUtils;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.common.helper.CampPushHelper;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.camp.facade.CampFacade;
import com.yayo.warriors.module.camp.rule.CampRule;
import com.yayo.warriors.module.chat.facade.ChannelFacade;
import com.yayo.warriors.module.chat.model.Channel;
import com.yayo.warriors.module.chat.type.ChatChannel;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.onlines.manager.OnlineStatisticManager;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.module.task.facade.TaskMainFacade;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.type.ElementType;
import com.yayo.warriors.type.GoodsType;

/**
 * 阵营
 * @author liuiyuhua
 */
@Component
public class CampFacadeImpl implements CampFacade {
	
	@Autowired
	private DbService dbService;
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private ChannelFacade channelFacade;
	@Autowired
	private TaskMainFacade taskMainFacade;
	@Autowired
	private CampPushHelper campPushHelper;
	@Autowired
	private OnlineStatisticManager onlineStatisticManager;
	
	private final Logger LOGGER = LoggerFactory.getLogger(CampFacadeImpl.class);
	
	
	public int joinCamp(long playerId, int campValue) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return PLAYER_NOT_FOUND;
		}

		Player player = userDomain.getPlayer();
		if (player.getCamp() != Camp.NONE) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("玩家[{}],已经加入了[{}],阵营,无法再次加入其他阵营.", playerId, player.getCamp());
			}
			return PLAYER_CAMP_EXIST;
		}

		Camp camp = EnumUtils.getEnum(Camp.class, campValue);
		if (camp == null) {
			LOGGER.error("玩家[{}],所选择加入的阵营[{}]为空.", playerId, campValue);
			return CHOOSE_CAMP_NOT_EXIST;
		}

		if(camp == Camp.NONE) { //选择听天由命
			return ramdomSelectCamp(userDomain);
		} else { //自己决定了选择的阵营
			return handleSelectCamp(userDomain, camp);
		}
	}
	
	/**
	 * 手动选择阵营
	 * 
	 * @param  userDomain		用户域模型
	 * @param  camp 			选择的阵营
	 * @return
	 */
	private int handleSelectCamp(UserDomain userDomain, Camp camp) {
		Camp oldCamp = Camp.NONE;
		long playerId = userDomain.getPlayerId();
		Player player = userDomain.getPlayer();
		ChainLock lock = LockUtils.getLock(player);
		try {
			lock.lock();
			oldCamp = player.getCamp();
			if (oldCamp != Camp.NONE) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("玩家[{}],已经加入了[{}],阵营,无法再次加入其他阵营.", playerId, player.getCamp());
				}
				return PLAYER_CAMP_EXIST;
			}
			player.setCamp(camp);
			dbService.submitUpdate2Queue(player);
		} finally {
			lock.unlock();
		}

		// 更新数据库
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("玩家[{}],成功加入[{}]阵营.", playerId, camp);
		}
		
		taskMainFacade.updateSelectCampTask(playerId);
		channelFacade.leftChannel(playerId, Channel.valueOf(ChatChannel.CAMP_CHANNEL.ordinal(), oldCamp.ordinal()));
		channelFacade.enterChannel(player, Channel.valueOf(ChatChannel.CAMP_CHANNEL.ordinal(), camp.ordinal()));
		GameMap gameMap = userDomain.getGameMap();
		if(gameMap != null){
			Collection<Long> playerIds = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
			campPushHelper.pushJoinCamp(playerIds, playerId, camp.ordinal());
		}
		
		//记录阵营注册人数
		onlineStatisticManager.addCampOnline(camp); 		//选择的阵营在线数
		onlineStatisticManager.subCampOnline(Camp.NONE);	//阵营在线
		onlineStatisticManager.addCampRegisterRecord(camp);	//增加注册阵营数
		return camp.ordinal();
	}
	
	/**
	 * 随机选择最小的阵营
	 * 
	 * @param  userDomain		用户域模型
	 * @return {@link Integer}	返回值信息
	 */
	private int ramdomSelectCamp(UserDomain userDomain) {
		Camp oldCamp = Camp.NONE;
		UserProps userProps = null;
		int baseId = CampRule.GIFT_ITEM_ID;
		PropsConfig props = propsManager.getPropsConfig(CampRule.GIFT_ITEM_ID);
		if(props == null) {
			return ITEM_NOT_FOUND;
		}
		
		int count = 1;
		Player player = userDomain.getPlayer();
		long playerId = userDomain.getPlayerId();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		int knifeCount = onlineStatisticManager.getCampPlayerCount(Camp.KNIFE_CAMP);
		int swordCampCount = onlineStatisticManager.getCampPlayerCount(Camp.SWORD_CAMP);
		Camp camp = knifeCount >= swordCampCount ? Camp.SWORD_CAMP : Camp.KNIFE_CAMP;
		int currBackSize = propsManager.getBackpackSize(playerId, backpack);
		ChainLock lock = LockUtils.getLock(player, player.getPackLock());
		try {
			lock.lock();
			oldCamp = player.getCamp();
			if (oldCamp != Camp.NONE) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("玩家[{}],已经加入了[{}],阵营,无法再次加入其他阵营.", playerId, player.getCamp());
				}
				return PLAYER_CAMP_EXIST;
			}
			
			if(!player.canAddNew2Backpack(currBackSize + 1, backpack)) {
				return BACKPACK_FULLED;
			}
			
			userProps = UserProps.valueOf(playerId, backpack, count, props, true);
			propsManager.createUserProps(userProps);
			propsManager.put2UserPropsIdsList(playerId, backpack, userProps);
			
			player.setCamp(camp);
			dbService.submitUpdate2Queue(player);
		} catch (Exception e) {
			LOGGER.error("{}", e);
			return FAILURE;
		} finally {
			lock.unlock();
		}

		// 更新数据库
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("玩家[{}],成功加入[{}]阵营.", playerId, camp);
		}
		
		GameMap gameMap = userDomain.getGameMap();
		taskMainFacade.updateSelectCampTask(playerId);
		channelFacade.leftChannel(playerId, Channel.valueOf(ChatChannel.CAMP_CHANNEL.ordinal(), oldCamp.ordinal()));
		channelFacade.enterChannel(player, Channel.valueOf(ChatChannel.CAMP_CHANNEL.ordinal(), camp.ordinal()));
		if(gameMap != null){
			Collection<Long> playerIds = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
			campPushHelper.pushJoinCamp(playerIds, playerId, camp.ordinal());
		}
		
		if(userProps != null) {
			MessagePushHelper.pushUserProps2Client(playerId, backpack, false, userProps);
			GoodsLogger.goodsLogger(player, Source.PLAYER_SELECT_CAMP, LoggerGoods.incomeProps(baseId, count));
			MessagePushHelper.pushGoodsCountChange2Client(playerId, GoodsVO.valueOf(baseId, GoodsType.PROPS, 1));
		}
		
		//记录阵营注册人数
		onlineStatisticManager.addCampOnline(camp); 		//选择的阵营在线数
		onlineStatisticManager.subCampOnline(Camp.NONE);	//阵营在线
		onlineStatisticManager.addCampRegisterRecord(camp);	//增加注册阵营数
		return camp.ordinal();
	}
}
