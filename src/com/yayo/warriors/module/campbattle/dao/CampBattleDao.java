package com.yayo.warriors.module.campbattle.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.yayo.common.db.dao.CommonDao;
import com.yayo.warriors.module.campbattle.entity.CampBattleHistory;
import com.yayo.warriors.module.campbattle.entity.PlayerCampBattleHistory;
import com.yayo.warriors.module.campbattle.entity.PlayerCampBattleRecord;
import com.yayo.warriors.module.user.type.Camp;

/**
 * 数据访问定义
 * @author jonsai
 *
 */
public interface CampBattleDao extends CommonDao {

	/**
	 * 保存阵营战场记录
	 * @param campBattleHistorys
	 * @param playerCampBattleHistorys
	 * @param playerCampBattleRecords
	 */
	void saveCampBattleInfo(Collection<CampBattleHistory> campBattleHistorys, Collection<PlayerCampBattleHistory> playerCampBattleHistorys, Collection<PlayerCampBattleRecord> playerCampBattleRecords);
	
	/**
	 * 取得阵营战历史记录
	 * @param date
	 * @param camp
	 * @return
	 */
	CampBattleHistory getCampBattleHistory(Date date, Camp camp);

	/**
	 * 取得玩家阵营战历史记录
	 * @param date
	 * @param camp
	 * @return
	 */
	List<Long> getPlayerCampBattleHistory(Date date, Camp camp);
	
	/**
	 * 取得玩家阵营战积分列表
	 * @param date
	 * @param camp
	 * @return
	 */
	List<Long> getPlayerTotalScoreList(Camp camp);
	
	/**
	 * 取得玩家阵营战积分列表
	 * @param camp
	 * @return
	 */
	List<Long> getCampTitlePlayers(Camp camp);
	
	/**
	 * 取得战场记录时间（倒序）, 最新的在前面
	 * @return
	 */
	List<Date> getCampBattleDates();
	
	/**
	 * 取得历届盟主
	 * @param	camp
	 * @return
	 */
	List<Long> getCampLeaderPlayerIds(Camp camp);
	
	/**
	 * 清空玩家的阵营称号
	 */
	void clearCampTitle();
	
	/**
	 * 清空玩家的阵营战记录
	 */
	void clearPlayerCampBattleRecord();
}
