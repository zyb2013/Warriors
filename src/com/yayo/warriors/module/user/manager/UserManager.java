package com.yayo.warriors.module.user.manager;

import java.util.List;

import com.yayo.warriors.basedb.model.RevivePointConfig;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.server.listener.DataRemoveListener;
import com.yayo.warriors.module.server.listener.LogoutListener;
import com.yayo.warriors.module.user.entity.DailyRecord;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.model.InitCreateInfo;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 用户管理接口
 * 
 * @author Hyint
 */
public interface UserManager extends DataRemoveListener, LogoutListener {

	/**
	 * 验证角色是否在线
	 * 
	 * @param  playerId					角色ID
	 * @return {@link Boolean}			true-角色在线, false-角色不在线
	 */
	boolean isOnline(long playerId);
	
	/**
	 * 更新角色信息. 对于一些依赖数据库事务的操作
	 * 
	 * @param player					角色信息
	 */
	void updatePlayer(Player player);
	
	/**
	 * 查询用户的域模型对象
	 * 
	 * @param  playerId				角色ID
	 * @return {@link UserDomain}	用户域模型对象
	 */
	UserDomain getUserDomain(long playerId);
	
	/**
	 * 保存角色的登录状态
	 * 
	 * @param player				角色对象
	 */
	void savePlayerLoginState(Player player);
	
	/**
	 * 根据角色名查询角色对象
	 * 
	 * @param  playerName			角色名
	 * @return {@link Player}		角色对象
	 */
	Player getPlayer(String playerName);
	
	/**
	 * 根据角色名查询角色ID
	 * 
	 * @param  playerName			角色名
	 * @return {@link Long}			角色ID对象
	 */
	long getPlayerId(String playerName);
	
	/**
	 * 根据用户ID查询角色角色ID列表
	 * 
	 * @param  userName 				用户账号名
	 * @return {@link List} 			角色ID列表
	 */
	List<Long> listPlayerIdByUserName(String userName);
	
	/**
	 * 移除用户名通用缓存
	 * 
	 * @param userName					用户名
	 */
	void addPlayerId2UserNameCache(String userName, long playerId);
	
	
	/**
	 * 移除角色名通用缓存
	 * 
	 * @param playerName				角色名
	 */
	void addPlayerId2PlayerNameCache(String playerName, long playerId);
	
	/**
	 * 查询角色的属性值
	 * 
	 * @param  spire					角色ID
	 * @param  params					参数对象
	 * @return Object[]					返回的属性参数
	 */
	Object[] getPlayerAttributes(ISpire spire, Object...params);
	
	/**
	 * 查询角色的属性值
	 * 
	 * @param  playerId					角色ID
	 * @param  params					参数对象
	 * @return Object[]					返回的属性参数
	 */
	Object[] getPlayerAttributes(long playerId, Object...params);

	/**
	 * 创建角色对象. 
	 * 
	 * @param initCreateInfo			角色创建信息
	 */
	InitCreateInfo createPlayer(InitCreateInfo initCreateInfo);
	
	/**
	 * 增加角色的经验(该接口不记录日志. 调用方自己记录日志)
	 * 
	 * @param  playerId					角色ID
	 * @param  addExp					增加的经验值
	 * @param  update					是否需要更新入库
	 * @return {@link Boolean}			是否增加经验成功
	 */
	boolean addPlayerExp(long playerId, long addExp, boolean update);
	
	/** 
	 * 查询角色
	 * @param paramType					查询类型,	0-角色名, 1-用户名  2-角色id
	 * @param paramValue				查询值
	 * @param levelBegin				开始等级
	 * @param levelEnd					结束等级
	 * @return							玩家数量
	 */
	int findPlayersCount(int paramType, String paramValue, int levelBegin, int levelEnd);

	/**
	 * 更新角色的刷新状态
	 * 
	 * @param  playerId					角色ID
	 * @param  flushable				角色的刷新状态
	 */
	void updateFlushable(long playerId, int flushable);

	/**
	 * 查询角色
	 * @param paramType					查询类型,	0-角色名, 1-用户名  2-角色id
	 * @param paramValue				查询值
	 * @param type						0:全部, 1:被封号的,2:被禁言的 
	 * @param levelBegin				开始等级
	 * @param levelEnd					结束等级
	 * @param pageSize					分页大小
	 * @param currentPage				当前页
	 * @return
	 */
	List<Object[]> findPlayers(int paramType, String paramValue, int type, int levelBegin, int levelEnd, int pageSize, int currentPage);
	
	/**
	 * 获取复活点配置
	 * @param mapId    地图ID
	 * @return {@link RevivePointConfig} 复活点配置对象
	 */
	RevivePointConfig getRevivePointConfig(int mapId);
	
	/**
	 * 角色日常记录
	 * @param playerId
	 * @return
	 */
	DailyRecord getDailyRecord(long playerId);
}
