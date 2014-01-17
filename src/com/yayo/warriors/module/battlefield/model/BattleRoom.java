package com.yayo.warriors.module.battlefield.model;


import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.user.type.Camp;

/**
 * 战场房间
 * @author jonsai
 *
 */
public class BattleRoom {
	/** 战场玩家数量 */
	public int[] players = null;
	
	/** 房间对应的游戏地图 */
	public GameMap gameMap = null;
	
	/**
	 * 构造一个战场房间
	 * @param gameMap
	 * @return
	 */
	public static BattleRoom valueOf(GameMap gameMap){
		BattleRoom battleRoom = new BattleRoom();
		battleRoom.gameMap = gameMap;
		battleRoom.players = new int[ Camp.values().length - 1 ];
		for(Camp camp : Camp.values() ){
			if(camp == Camp.NONE){
				continue;
			}
			battleRoom.players[camp.ordinal() - 1] = 0;
		}
		
		return battleRoom;
	}
}
