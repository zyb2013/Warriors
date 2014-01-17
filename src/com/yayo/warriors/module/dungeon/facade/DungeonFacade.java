package com.yayo.warriors.module.dungeon.facade;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.dungeon.constant.DungeonConstant;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.dungeon.vo.DungeonVo;
import com.yayo.warriors.socket.vo.ChangeScreenVo;

/**
 * 地下城(副本)
 * @author liuyuhua
 */
public interface DungeonFacade {
	
	/**
	 * 查询角色的副本对象
	 * @param  playerId 				角色的ID
	 * @return {@link PlayerDungeon} 	用户副本对象
	 */
	PlayerDungeon getPlayerDungeon(long playerId);
	
	/**
	 * 进入副本
	 * @param playerId                  角色的ID
	 * @param dungeonBaseId             副本原型ID
	 * @return {@link ResultObject<ChangeScreenVo>} 转场信息
	 */
	ResultObject<ChangeScreenVo> enterDungeon(long playerId,int dungeonBaseId);
	
	/**
	 * 退出副本
	 * @param playerId                  角色的ID
	 * @return {@link ResultObject<ChangeScreenVo>} 转场信息                     
	 */
	ResultObject<ChangeScreenVo> exitDungeon(long playerId);
	
	/**
	 * 退出副本
	 * @param playerId                  角色的ID
	 * @return {@link ResultObject<ChangeScreenVo>} 转场信息                     
	 */
	ResultObject<ChangeScreenVo> exitDungeon(long playerId, int mapId, int x, int y);
	
	/**
	 * 加载副本信息
	 * @param playerId                  角色的ID
	 * @return {@link DungeonVo}        副本信息对象
	 */
	ResultObject<DungeonVo> loadDungeon(long playerId);
	
	/**
	 * 删除(回收副本)
	 * @param dungeonId                 副本的ID
	 * @param branching                 分线
	 */
	void removeDungeon(long dungeonId,int branching);
	
	/**
	 * 领取剧情副本奖励
	 * @param playerId                  角色的ID
	 * @param dungeonBaseId             基础剧情副本ID
	 * @return {@link DungeonConstant}  副本模块公共常量
	 */
	int rewardStory(long playerId,int dungeonBaseId);

	/**
	 * 验证剧情副本
	 * @param playerId    玩家的ID
	 * @param storyIds    需要验证的剧情副本ID集合
	 * @return {@link String} 返回可以进入的ID {剧情副本ID_剧情副本ID} 
	 */
	public ResultObject<String> verifyStory(long playerId,String storyIds);

}
