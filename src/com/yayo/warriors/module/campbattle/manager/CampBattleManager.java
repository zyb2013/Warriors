package com.yayo.warriors.module.campbattle.manager;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.yayo.warriors.module.campbattle.entity.CampBattleHistory;
import com.yayo.warriors.module.campbattle.entity.PlayerCampBattleHistory;
import com.yayo.warriors.module.campbattle.entity.PlayerCampBattleRecord;
import com.yayo.warriors.module.campbattle.model.CampBattle;
import com.yayo.warriors.module.campbattle.model.PlayerCampBattle;
import com.yayo.warriors.module.user.type.Camp;

/**
 * 阵营战业务接口
 * @author jonsai
 *
 */
public interface CampBattleManager {
	
	/**
	 * 取得玩家阵营战历史对象
	 * @param id
	 * @return
	 */
	PlayerCampBattleHistory getPlayerCampBattleHistory(long id);
	
	/**
	 * 取得玩家阵营战记录
	 * @param id
	 * @return
	 */
	PlayerCampBattleRecord getPlayerCampBattleRecord(long id);
	
	/**
	 * 取得阵营战历史对象
	 * @param id
	 * @return
	 */
	CampBattleHistory getCampBattleHistory(long id);
	
	/**
	 * 保存阵营战信息
	 * @param battleDate			阵营战场日期
	 * @param campBattles			阵营信息
	 * @param playerCampBattles		玩家阵营战信息
	 * @param playerCampBattleRecords玩家阵营战信息
	 */
	void saveCampBattleInfo(Date battleDate, Collection<CampBattle> campBattles, Collection<PlayerCampBattle> playerCampBattles, Collection<PlayerCampBattleRecord> playerCampBattleRecords);
	
	/**
	 * 取得阵营战历史记录
	 * @param date		日期
	 * @param camp		阵营, 0-全部， 1-豪杰， 2-侠客
	 * @return
	 */
	CampBattleHistory getCampBattleHistory(Date date, Camp camp);
	
	/**
	 * 取得玩家阵营战历史记录
	 * @param date		日期
	 * @param camp		阵营
	 * @param queryDB	是否查库
	 * @return
	 */
	List<Long> getPlayerCampBattleHistory(Date date, Camp camp, boolean queryDB);
	
	/**
	 * 取得角色总分id排行列表
	 * @param camp		阵营
	 * @return
	 */
	List<Long> getPlayerTotalScoreList(Camp camp);
	
	/**
	 * 取得阵营官衔列表
	 * @param camp
	 * @return
	 */
	List<Long> getCampTitlePlayers(Camp camp);
	
	/**
	 * 取得玩家阵营战历史记录
	 * @param playerId	角色id
	 * @param date		日期
	 * @param camp		阵营
	 * @return
	 */
	PlayerCampBattleHistory getPlayerCampBattleHistory(long playerId, Date date, Camp camp);
	
	/**
	 * 取得历届盟主
	 * @param	camp
	 * @return
	 */
	List<Long> getCampLeaderPlayerIds(Camp camp);
	
	/**
	 * 取得战场记录时间（倒序）, 最新的在前面
	 * @return
	 */
	List<Date> getCampBattleDates();
	
	/**
	 * 清空玩家的阵营战记录
	 */
	void clearPlayerCampBattleRecord();
	
	/**
	 * 清空玩家的阵营称号
	 */
	void clearCampTitles();
}
