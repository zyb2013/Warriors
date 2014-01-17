package com.yayo.warriors.module.flyshoes.facade.impl;


import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.flyshoes.constant.FlyShoesConstant.*;
import static com.yayo.warriors.module.pack.type.BackpackType.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.dungeon.manager.DungeonManager;
import com.yayo.warriors.module.flyshoes.facade.FlyShoesFacade;
import com.yayo.warriors.module.flyshoes.rule.FlyShoesRule;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.map.manager.GameMapManager;
import com.yayo.warriors.module.map.types.ScreenType;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.type.PropsChildType;
import com.yayo.warriors.module.task.manager.EscortTaskManager;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.vip.manager.VipManager;
import com.yayo.warriors.module.vip.model.VipDomain;
import com.yayo.warriors.module.vip.model.VipFunction;


@Component
public class FlyShoesFacadeImpl implements FlyShoesFacade {

	@Autowired
	private DungeonManager dungeonManager;
	@Autowired
	private MapFacade mapFacade;
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private DbService dbService;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private GameMapManager gameMapManager;
	@Autowired
	private VipManager vipManager;
	@Autowired
	private EscortTaskManager escortManager;
	
	
	
	public int useFlyShoes(long playerId, long propsId,int mapId, int x, int y) {
		UserDomain userDomain = this.userManager.getUserDomain(playerId);
		PlayerDungeon playerDungeon = dungeonManager.getPlayerDungeon(playerId);
		if(userDomain == null || playerDungeon == null){
			return PLAYER_NOT_FOUND;
		}
		
		GameMap beforeMap = userDomain.getGameMap();//玩家没有飞之前的地图
		if(beforeMap != null){
			if(beforeMap.getScreenType() == ScreenType.CAMP.ordinal()){
				return CAMP_MAP_CANT_USE;
			}
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle playerBattle = userDomain.getBattle();
		if(playerBattle.isDead()){
			return PLAYER_DEADED;
		}
		
		
		if(playerDungeon.isDungeonStatus()){
			return IN_DUNGEON_CANT_USE;
		}
		
		if(escortManager.isEscortStatus(playerBattle)){
			return ESCORT_STATUS_CANT_USE;
		}
		
		GameMap gameMap = gameMapManager.getGameMapById(mapId, userDomain.getBranching());
		if(gameMap == null){
			return MAP_NOT_FOUND;
		}
		
		if(gameMap.getScreenType() == ScreenType.CAMP.ordinal()){
			return CAMP_MAP_CANT_USE;
		}
		
		if(playerBattle.getLevel() < gameMap.getLevelLimit()){
			return LEVEL_LIMIT_CANT_ENTER;
		}
		
		
		VipDomain vipDomain = vipManager.getVip(playerId);
		if(vipDomain != null && vipDomain.isVip()){
			ChainLock lock = LockUtils.getLock(vipDomain.getPlayerVip());
			try {
				lock.lock();
				int count = vipDomain.intValue(VipFunction.FlyingShoesRemainTimes);
				if(count == -1){ 
					if(!mapFacade.go(playerId, mapId, x, y,5)){
						return USE_FLYSHOES_FAIL;
					}
					return SUCCESS;
				}else if(count > 0){
					if(!mapFacade.go(playerId, mapId, x, y,5)){ 
						return USE_FLYSHOES_FAIL;
					}
					vipDomain.alterNum(1, VipFunction.FlyingShoes);
					dbService.submitUpdate2Queue(vipDomain.getPlayerVip());
					return SUCCESS;
				}
			}finally{
				lock.unlock();
			}
		}
		
		
		int costCount = FlyShoesRule.USE_FLY_SHOES_PROPS_COUNT;
		UserProps userProps = propsManager.getUserProps(propsId);
		if (userProps == null) {
			return ITEM_NOT_ENOUGH;
		} else if (userProps.getPlayerId() != playerId) {
			return ITEM_NOT_ENOUGH;
		} else if (!userProps.validBackpack(DEFAULT_BACKPACK)) {
			return BACKPACK_INVALID;
		} else if (userProps.getCount() < costCount) {
			return ITEM_NOT_ENOUGH;
		} else if(userProps.isTrading()) {
			return ITEM_CANNOT_USE;
		}

		int baseId = userProps.getBaseId();
		PropsConfig propsConfig = userProps.getPropsConfig();
		if (propsConfig == null) {
			return BASEDATA_NOT_FOUND;
		} else if (propsConfig.getChildType() != PropsChildType.FLY_SHOES_TYPE) {
			return BELONGS_INVALID;
		}
		
		ChainLock lock = LockUtils.getLock(userDomain.getPackLock());
		try {
			lock.lock();
			if (userProps.getPlayerId() != playerId) {
				return ITEM_NOT_ENOUGH;
			} else if (!userProps.validBackpack(DEFAULT_BACKPACK)) {
				return BACKPACK_INVALID;
			} else if (userProps.getCount() < costCount) {
				return ITEM_NOT_ENOUGH;
			}
			
			if(!mapFacade.go(playerId, mapId, x, y,3)){ 
				return USE_FLYSHOES_FAIL;
			}
			
			userProps.decreaseItemCount(costCount);
			propsManager.removeUserPropsIfCountNotEnough(userProps);
			
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(userProps);
		MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, userProps);
		GoodsLogger.goodsLogger(player, Source.PROPS_USE_PROPS, LoggerGoods.outcomeProps(propsId, baseId, costCount));
		return SUCCESS;
	}

}
