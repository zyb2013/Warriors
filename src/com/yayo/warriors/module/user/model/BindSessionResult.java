package com.yayo.warriors.module.user.model;

import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerMotion;

/**
 * 绑定会话时的返回值
 * 
 * @author Hyint
 */
public class BindSessionResult {

	/** 地图ID */
	private int mapId;

	/** 角色 */
	private Player player;

	/** 据点X */
	private int positionX;
	
	/** 据点Y */
	private int positionY;
	
	/** 地下城ID */
	private long dungeonId;
	
	/** 地下城基础ID */
	private int dungeonBaseId;

	/** 角色属性参数 */
	private Object[] params;
	
	/** 角色属性参数对应的值 */
	private Object[] values;

	/** 分线号 */
	private int branching;
	
	public int getMapId() {
		return mapId;
	}

	public int getPositionX() {
		return positionX;
	}

	public int getPositionY() {
		return positionY;
	}

	public long getDungeonId() {
		return dungeonId;
	}

	public int getDungeonBaseId() {
		return dungeonBaseId;
	}

	public Object[] getParams() {
		return params;
	}

	public Object[] getValues() {
		return values;
	}
	
	public long getPlayerId() {
		return player.getId();
	}

	public int getBranching() {
		return branching;
	}

	public Player getPlayer() {
		return player;
	}

	/**
	 * 构建绑定Session返回值
	 * 
	 * @param  branching					分线号
	 * @param  player						角色对象
	 * @param  motion						角色的移动对象
	 * @param  playerDungeon				角色的地下城对象
	 * @param  params						属性参数数组
	 * @param  values						属性参数值数组
	 * @return {@link BindSessionResult}	绑定Session返回值对象
	 */
	public static BindSessionResult valueOf(int branching, Player player, PlayerMotion motion, PlayerDungeon playerDungeon, Object[] params, Object[] values) {
		BindSessionResult bindSessionResult = new BindSessionResult();
		bindSessionResult.params = params;
		bindSessionResult.values = values;
		bindSessionResult.player = player;
		bindSessionResult.branching = branching;
		bindSessionResult.mapId = motion.getMapId();
		bindSessionResult.positionX = motion.getX();
		bindSessionResult.positionY = motion.getY();
		if(playerDungeon != null) {
			bindSessionResult.dungeonId = playerDungeon.getDungeonId();
			bindSessionResult.dungeonBaseId = playerDungeon.getDungeonBaseId();
		}
		return bindSessionResult;
	}
	
}
