package com.yayo.warriors.module.campbattle.facade;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.campbattle.constant.CampBattleConstant;
import com.yayo.warriors.module.campbattle.entity.CampBattleHistory;
import com.yayo.warriors.module.campbattle.entity.PlayerCampBattleHistory;
import com.yayo.warriors.module.campbattle.model.CampBattle;
import com.yayo.warriors.module.campbattle.type.CampTitle;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.socket.vo.ChangeScreenVo;

public interface CampBattleFacade {

	/**
	 * 报名加入战场
	 * @param playerId
	 * @return <=0:错误码	>0:可以报名时间
	 */
	long apply(long playerId);
	
	/**
	 * 取得报名状态
	 * @param 	playerId	角色id
	 * @return [Integer]  	<:错误码，0-未报名， 1-已经报名
	 */
	int getApplyStatus(long playerId);
	
	/**
	 * 取得玩家本阵营的报名分页列表
	 * 
	 * @param 	playerId				角色id {@link Long}
	 * @param 	pageSize				分页大小 {@link Integer}
	 * @param 	pageNow					当前分页 {@link Integer}
	 * @param 	resultMap				客户端返回map
	 * @return	{@link CampBattleConstant}
	 */
	int getApplyPlayers(Long playerId, int pageSize, int pageNow, Map<String, Object> resultMap);

	/**
	 * 调整报名玩家的优先权(位置)
	 * @param playerId			角色id
	 * @param targetId			目标角色id
	 * @param op				-1-向上置顶, 1-向下置底
	 * @param pageSize			分页大小
	 * @param pageNow			当前分页
	 * @param resultMap			客户端返回map
	 * @return					{@link CampBattleConstant}
	 */
	int adjustApplyPlayerPriority(Long playerId, long targetId, byte op,int pageSize, int pageNow, Map<String, Object> resultMap);
	
	/**
	 * 取得积分排行榜
	 * @param playerId			角色id
	 * @param pageSize			分页大小
	 * @param pageNow			当前分页
	 * @param resultMap			客户端返回map
	 * @return					{@link CampBattleConstant}
	 */
	int getPlayerScores(Long playerId, int pageSize, int pageNow, Map<String, Object> resultMap);
	
	/**
	 * 进入战场
	 * @param playerId			角色id
	 * @return					{@link CampBattleConstant}
	 */
	ResultObject<ChangeScreenVo> enterCampBattle(Long playerId);

	/**
	 * 退出战场
	 * @param playerId			角色id
	 * @return					{@link CampBattleConstant}
	 */
	ResultObject<ChangeScreenVo> existCampBattle(Long playerId);
	
	/**
	 * 领取俸禄（有官衔的才可以）
	 * @param playerId			角色id
	 * @return					{@link CampBattleConstant}
	 */
	int salary(Long playerId);
	
	/**
	 * 领取官衔时装奖励（有官衔的才可以）
	 * @param playerId			角色id
	 * @return					{@link CampBattleConstant}
	 */
	int suitReward(Long playerId);

	/**
	 * 取得阵营官衔玩家列表
	 * @param camp			阵营, 1-豪杰，2-侠客
	 * @param resultMap		返回客户端map
	 * @return				{@link CampBattleConstant}
	 */
	int getCampTitlePlayers(int camp, Map<String, Object> resultMap);
	
	/**
	 * 取得阵营历届阵营盟主
	 * @param camp			阵营, 1-豪杰，2-侠客
	 * @param pageSize		分页大小
	 * @param pageNow		当前分页
	 * @param resultMap		返回客户端map
	 * @return				{@link CampBattleConstant}
	 */
	int getCampLeader(int camp, int pageSize, int pageNow, Map<String, Object> resultMap);

	/**
	 * 取得阵营战场记录
	 * @param playerId		角色id
	 * @param camp			阵营, 1-豪杰，2-侠客
	 * @param pageSize		分页大小
	 * @param pageNow		当前分页
	 * @param resultMap		返回客户端map
	 * @return				{@link CampBattleConstant}
	 */
	int getCampBattleHistory(Long playerId, Date date, Camp camp, int pageSize, int pageNow, Map<String, Object> resultMap);
	

	/**
	 * 领取阵营战场奖励
	 * @param 	playerId		角色id
	 * @param	date			日期, 为空表示领取上届的战斗奖励
	 * @return					{@link CampBattleConstant}
	 */
	int rewards(Long playerId, Date date);
	
	/**
	 * 取得玩家指定日期的战场记录
	 * @param date		日期
	 * @param playerId	角色id
	 * @return			{@link PlayerCampBattleHistory}
	 */
	PlayerCampBattleHistory getPlayerCampBattleHistory(Date date, long playerId);
	
	/**
	 * 取得角色阵营称号
	 * 
	 * @param  playerId				角色ID
	 * @param  campDate				阵营日期, 日期:null则取得上一次阵营战场的称号
	 * @return {@link CampTitle}	阵营称号
	 */
	CampTitle getCampBattleTitle(long playerId, Date campDate);
	
	/**
	 * 取得指定日期的阵营战场记录
	 * @param date		日期
	 * @param camp		角色id
	 * @return			{@link CampBattleHistory}
	 */
	CampBattleHistory getCampBattleHistory(Date date, Camp camp);
	
	/**
	 * 接收怪物掉血
	 * 
	 * @param monsterDomain	怪物域对象
	 * @param attacker			攻击者
	 * @param hurtHP			掉血值
	 */
	void processMonsterHurt(MonsterDomain monsterDomain, UnitId attacker, int hurtHP);
	
	/**
	 * 接收玩家被击杀
	 * @param attacker			攻击者
	 * @param target			被攻击者
	 */
	void processKillPlayers(UserDomain attacker, UserDomain target);
	
	/**
	 * 是否阵营战在进行中
	 * @return 					阵营战状态
	 */
	int getCampBattleStatus();
	
	/**
	 * 取得阵营战场游戏地图，(阵营战开启并且玩家在阵营战场里不为空，其它为空)
	 * @param 	userDomain
	 * @return
	 */
	GameMap getCampBattleGameMap(UserDomain userDomain);
	
	/**
	 * 是否在阵营战场中
	 * @param 	userDomain
	 * @return
	 */
	boolean isInCampBattle(UserDomain userDomain);
	
	/**
	 * 取得某个阵营已经占领的据点
	 * @param 	camp
	 * @return
	 */
	Collection<Integer> getOwnCampBattlePoints(Camp camp);
	
	/**
	 * 取得战场记录时间（倒序）, 最新的在前面
	 * @return
	 */
	List<Date> getCampBattleDates();
	
	/**
	 * 取得阵营据点的相关属性
	 * @param monsterDomain
	 * @return 为空表示忽略此返回
	 */
	Map<Integer, Object> getAttributesOfCampBattleMonster(MonsterDomain monsterDomain);
	
	/**
	 * 取得玩家是否有奖励状态
	 * @param playerId	角色id
	 * @param type		1-得分奖励  2-阵营官衔俸禄
	 * @param date		日期
	 * @return			-1-无奖励， 0-可以领，1-已经领取了
	 */
	int getRewardStat(Long playerId, int type, Date date);
	
	/**
	 * 阵营战请求
	 * @param playerId		角色id
	 * @param cmd			指令,		1-回本阵营
	 * @param resultMap		返回客户端map
	 * @return				{@link CampBattleConstant}
	 */
	int campBattleRequestCmd(Long playerId, int cmd, Map<String, Object> resultMap);
	
	/**
	 * 取得阵营战信息
	 * @param camp			阵营
	 * @return
	 */
	List<CampBattle> getCampBattles(Camp camp);
	
	/**
	 * 角色进入战场
	 * @param userDomain	用户域
	 */
	void doEnterCampBattle(UserDomain userDomain);
	
	/**
	 * 取得积分排行
	 * @param playerId		角色id
	 * @param pageNow		当前页
	 * @param pageSize		分页大小
	 * @param resultMap		返回map
	 * @return
	 */
	int getScoreRank(long playerId, int pageNow, int pageSize, Map<String, Object> resultMap);
	
	/**
	 * 取得本周角色积分信息
	 * @param playerId		角色id
	 * @param resultMap		返回map
	 * @return
	 */
	int getPlayerScoreInfo(long playerId, Map<String, Object> resultMap);
	
	/**
	 * 取得上届战场胜利的阵营
	 * @return
	 */
	Camp getWinCamp();
}
