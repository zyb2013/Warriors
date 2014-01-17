package com.yayo.warriors.module.user.dao.impl;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.yayo.common.db.dao.impl.CommonDaoImpl;
import com.yayo.warriors.module.user.dao.UserDao;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.model.InitCreateInfo;

/**
 * 用户DAO接口实现类
 * 
 * @author Hyint
 */
@Repository
public class UserDaoImpl extends CommonDaoImpl implements UserDao {

	private static final String SELECT = " SELECT " ;
	private static final String FROM = " FROM " ;
	private static final String LIMIT = " LIMIT ";
	private static final String WHERE = " WHERE ";
	private static final String AND = " AND ";
	private static final String BETWEEN = " BETWEEN " ;
	private static final String PLAYER_TABLE = "`player`" ;
	private static final String PLAYERBATTLE_TABLE = "`playerBattle`" ;
	
	/**
	 * 根据角色名查询角色ID
	 * 
	 * @param  playerName		角色名
	 * @return {@link Long}		角色ID
	 */
	
	public Long getPlayerId(String playerName) {
		Criteria criteria = createCriteria(Player.class);
		criteria.add(Restrictions.eq("name", playerName == null ? "" : playerName));
		criteria.setProjection(Projections.id());
		Long playerId = (Long) criteria.uniqueResult();
		return playerId == null ? 0L : playerId;
	}

	/**
	 * 根据帐号ID查询角色ID列表
	 * 
	 * @param  userName		帐号ID
	 * @param  getDeletable		是否要查询已删除的角色ID, true-获取已删除的, false-获取未删除的
	 * @return {@link List}		角色ID列表
	 */
	
	@SuppressWarnings("unchecked")
	public List<Long> listPlayerIdByUserName(String userName, boolean getDeletable) {
		Criteria criteria = createCriteria(Player.class);
		criteria.add(Restrictions.eq("userName", StringUtils.defaultIfBlank(userName, "")));
		criteria.add(Restrictions.eq("deletable", getDeletable));
		criteria.setProjection(Projections.id());
		return criteria.list();
	}
	
//	/**
//	 * 创建角色
//	 * 
//	 * @param player			角色对象
//	 * @param battle			角色战斗对象
//	 * @param motion			角色移动对象
//	 */
//	@SuppressWarnings("unchecked")
//	
//	public void createPlayerInfo(Player player, PlayerBattle battle, PlayerMotion motion) {
//		save(player);
//		battle.setId(player.getId());
//		motion.setId(player.getId());
//		save(battle, motion, UserSkill.valueOf(player.getId()), UserBuffer.valueOf(player.getId()));
//	}
//	
	/**
	 * 创建角色
	 * 
	 * @param initCreateInfo	初始化创建信息
	 */
	@SuppressWarnings("unchecked")
	
	public void createPlayerInfo(InitCreateInfo initCreateInfo) {
		this.save(initCreateInfo.getPlayer());
		initCreateInfo.updateInitPlayerId();
		this.save(initCreateInfo.getBattle(), 			initCreateInfo.getMeridian(), 			initCreateInfo.getCoolTime(),
				  initCreateInfo.getUserSkill(), 		initCreateInfo.getUserBuffer(), 		initCreateInfo.getPlayerVip(),
				  initCreateInfo.getPlayerTitle(), 		initCreateInfo.getTaskComplete(), 		initCreateInfo.getPlayerMotion(),
				  initCreateInfo.getPlayerDungeon(), 	initCreateInfo.getUserMortalBody());
	}

	@SuppressWarnings("unchecked")
	
	public List<Object[]> findPlayers(int paramType, String paramValue, int roleType , int levelBegin, int levelEnd, int pageSize, int currentPage) {
		StringBuilder sqlBuilder = new StringBuilder();
		
		sqlBuilder.append(SELECT)
		.append(PLAYER_TABLE+".`playerId`,"+PLAYER_TABLE+".`name`,"+PLAYER_TABLE+".`title`,"+PLAYER_TABLE+".`sex`,"+PLAYER_TABLE+".`serverId`,"+PLAYER_TABLE+".`userName`,"+PLAYER_TABLE+".`camp`,"+PLAYERBATTLE_TABLE+".`level`,"+PLAYER_TABLE+".`golden`,"+PLAYER_TABLE+".`silver`,"+PLAYER_TABLE+".`forbidLogin`,"+PLAYER_TABLE+".`forbidChat`")
		.append(FROM)
		.append(""+PLAYER_TABLE+","+PLAYERBATTLE_TABLE+"")
		.append(WHERE)
		.append(""+PLAYER_TABLE+".`playerId` = "+PLAYERBATTLE_TABLE+".`playerId`");
		if(StringUtils.isNotBlank(paramValue)){
			if(paramType==0){		//角色名
				sqlBuilder.append(AND).append(PLAYER_TABLE+".`name` like ").append("'%").append(paramValue).append("%'");
			}else if(paramType==1){	//用户名
				sqlBuilder.append(AND).append(PLAYER_TABLE+".`username` like ").append("'%").append(paramValue).append("%'");
			}else if(paramType==2){	//角色id
				sqlBuilder.append(AND).append(PLAYER_TABLE+".`playerId` = ").append(paramValue).append(" ");
			}
		}
		//1:被封号的,2:被禁言的 
		if(roleType == 2){
			sqlBuilder.append(AND).append(PLAYER_TABLE+".`forbidChat` != '' ");
		}
		if(roleType == 1){
			sqlBuilder.append(AND).append(PLAYER_TABLE+".`forbidLogin` != '' ");
		}
		if(levelBegin != -1 && -1 != levelBegin){
			sqlBuilder.append(AND).append(PLAYERBATTLE_TABLE+".`level` ").append(BETWEEN).append(levelBegin).append(AND).append(levelEnd);
		}
		sqlBuilder.append(LIMIT)
		.append(pageSize*(currentPage - 1))
		.append(",")
		.append(pageSize);
		return getSession().createSQLQuery(sqlBuilder.toString()).list();
	}

	
	public int findPlayersCount(int paramType, String paramValue, int levelBegin, int levelEnd) {
		StringBuilder sqlBuilder = new StringBuilder();
		
		sqlBuilder.append(SELECT)
		.append("count(*)")
		.append(FROM)
		.append(""+PLAYER_TABLE+","+PLAYERBATTLE_TABLE+"")
		.append(WHERE)
		.append(""+PLAYER_TABLE+".`playerId` = "+PLAYERBATTLE_TABLE+".`playerId`");
		if(StringUtils.isNotBlank(paramValue)){
			if(paramType==0){		//角色名
				sqlBuilder.append(AND).append(PLAYER_TABLE+".`name` like ").append("'%").append(paramValue).append("%'");
			}else if(paramType==1){	//用户名
				sqlBuilder.append(AND).append(PLAYER_TABLE+".`username` like ").append("'%").append(paramValue).append("%'");
			}else if(paramType==2){	//角色id
				sqlBuilder.append(AND).append(PLAYER_TABLE+".`playerId` = ").append(paramValue);
			}
		}
		if(levelBegin != -1 && -1 != levelBegin){
			sqlBuilder.append(AND).append(PLAYERBATTLE_TABLE+".`level` ").append(BETWEEN).append(levelBegin).append(AND).append(levelEnd);
		}
		
		Object o = getSession().createSQLQuery(sqlBuilder.toString()).list().get(0);
		return ((BigInteger)o).intValue();
	}

	
}
