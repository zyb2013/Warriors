package com.yayo.warriors.module.map.facade;

import java.util.Collection;

import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.cooltime.entity.UserCoolTime;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.socket.vo.ChangeScreenVo;
import com.yayo.warriors.type.ElementType;




public interface MapFacade {

	
	int enterScreen(Long playerId);

	
	int motion(Long playerId, int x, int y);

	
	boolean go(long playerId, int mapId, int x, int y,int distance);


	int motionPath(Long playerId, Object[] direction);

	
	boolean isStand(int mapId, int x, int y);
	
	
	boolean isChangeScreen(UserDomain userDomain);

	
	ResultObject<ChangeScreenVo> changeScreen(long playerId);
	
	
	Collection<Long> getScreenViews(Long playerId);
	
	
	void doLoginFilter(UserDomain userDomain,PlayerDungeon playerDungeon);


	
	public void changeMap(ISpire targetSpire,GameMap gameMap, int x ,int y);
	
	
	void changeMap(ISpire targetSpire, GameMap gameMap, int x, int y, ElementType... refTypes);
	
	
	ResultObject<ChangeScreenVo> campChangeScreen(long playerId);
	
	ChangeScreenVo leaveMap(UserDomain userDomain, GameMap targetGameMap, int targetX, int targetY);
	
	
	void skillChangeMap(ISpire target, GameMap gameMap, int targetX, int targetY);
	
	
	void checkPlayerDeadState(long playerId);
	
	
	int sendConveneInvite(UserDomain userDomain, UserCoolTime userCoolTime, PropsConfig propsConfig);
	
	
	int acceptConveneInvite(long playerId, long targetId, int type);
}
