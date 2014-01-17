package com.yayo.warriors.module.user.dao;

import java.util.List;

import com.yayo.common.db.dao.CommonDao;
import com.yayo.warriors.module.user.model.InitCreateInfo;

/***
 * 用户DAO接口
 * 
 * @author Hyint
 */
public interface UserDao extends CommonDao {
	
	/**
	 * 根据角色名查询角色ID
	 * 
	 * @param  playerName		角色名
	 * @return {@link Long}		角色ID
	 */
	Long getPlayerId(String playerName);

	/**
	 * 根据帐号ID查询角色ID列表
	 * 
	 * @param  username			帐号名
	 * @param  getDeletable		是否要查询已删除的角色ID, true-获取已删除的, false-获取未删除的
	 * @return {@link List}		角色ID列表
	 */
	List<Long> listPlayerIdByUserName(String userName, boolean getDeletable);

	/**
	 * 创建角色
	 * 
	 * @param initCreateInfo	初始化创建信息
	 */
	void createPlayerInfo(InitCreateInfo initCreateInfo);
	
	/**
	 * 查询角色
	 * @param paramType			查询类型,	0-角色名, 1-用户名  2-角色id
	 * @param paramValue		查询值
	 * @param type				1:被封号的,2:被禁言的 
	 * @param levelBegin		开始等级
	 * @param levelEnd			结束等级
	 * @param pageSize			分页大小
	 * @param currentPage		当前页
	 * @return
	 */
	List<Object[]> findPlayers(int paramType, String paramValue, int type, int levelBegin,	 int levelEnd, int pageSize, int currentPage);

	/**
	 * 查询角色
	 * @param paramType			查询类型,	0-角色名, 1-用户名  2-角色id
	 * @param paramValue		查询值
	 * @param levelBegin		开始等级
	 * @param levelEnd			结束等级
	 * @return
	 */
	int findPlayersCount(int paramType, String paramValue, int levelBegin, int levelEnd);
}
