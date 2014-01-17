package com.yayo.warriors.module.onlines.dao.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.yayo.common.db.dao.impl.CommonDaoImpl;
import com.yayo.warriors.module.onlines.dao.OnlineStatisticDao;
import com.yayo.warriors.module.onlines.entity.CampOnline;
import com.yayo.warriors.module.onlines.entity.OnlineStatistic;
import com.yayo.warriors.module.onlines.entity.RegisterDetailStatistic;
import com.yayo.warriors.module.onlines.entity.RegisterStatistic;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;

/**
 * 在线DAO实现类
 * 
 * @author Hyint
 */
@Repository
public class OnlineStatisticDaoImpl extends CommonDaoImpl implements OnlineStatisticDao {
	
	/**
	 * 列出在线统计对象列表
	 * 
	 * @param  recordDate				统计的日期. 格式: 年-月-日
	 * @param  recordTime				统计的时间. 格式: 时:分
	 * @return {@link List}				在线统计对象列表
	 */
	@SuppressWarnings("unchecked")
	
	public List<Long> listOnlineStatisticId(String recordDate, String recordTime) {
		Criteria criteria = createCriteria(OnlineStatistic.class);
		if(StringUtils.isNotBlank(recordDate)) {
			criteria.add(Restrictions.eq("recordDate", recordDate));
		}
		if(StringUtils.isNotBlank(recordTime)) {
			criteria.add(Restrictions.eq("recordTime", recordTime));
		}
		criteria.setProjection(Projections.id());
		return criteria.list();
	}

	
	public int getSinceMaxCount() {
		Criteria criteria = createCriteria(OnlineStatistic.class);
		criteria.setProjection(Projections.max("maxCount"));
		Integer maxCount = (Integer)criteria.uniqueResult();
		return maxCount == null ? 0 : maxCount;
	}

	@SuppressWarnings("unchecked")
	
	public List<Object> loadRegisterCount() {
		SQLQuery sqlQuery = getSession().createSQLQuery("select camp, count(playerId) as num from player group by camp");
		return sqlQuery.list();
	}

	@SuppressWarnings("unchecked")
	
	public List<Long> listRegisterStatisticId(String recordDate, String recordTime) {
		Criteria criteria = createCriteria(RegisterStatistic.class);
		if(StringUtils.isNotBlank(recordDate)) {
			criteria.add(Restrictions.eq("recordDate", recordDate));
		}
		if(StringUtils.isNotBlank(recordTime)) {
			criteria.add(Restrictions.eq("recordTime", recordTime));
		}
		criteria.setProjection(Projections.id());
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	
	public List<Long> listCampOnlineId(String recordDate, String recordTime) {
		Criteria criteria = createCriteria(CampOnline.class);
		if(StringUtils.isNotBlank(recordDate)) {
			criteria.add(Restrictions.eq("recordDate", recordDate));
		}
		if(StringUtils.isNotBlank(recordTime)) {
			criteria.add(Restrictions.eq("recordTime", recordTime));
		}
		criteria.setProjection(Projections.id());
		return criteria.list();
	}

	
	@SuppressWarnings("unchecked")
	public List<Integer> getAllPlayerLevel() {
		Criteria criteria = createCriteria(PlayerBattle.class);
		criteria.setProjection(Projections.property("level"));
		return criteria.list();
	}
	
	/** 统计登录次数 */
	private static final String SQL = "SELECT SUM(loginCount) FROM player WHERE loginCount > 0";
	
	/**
	 * 获取全服玩家总共登陆次数
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public long loadTotleLoginCount() {
		Session session = getSession();
		List<Object> result = session.createSQLQuery(SQL).list();
		if(result == null || result.isEmpty()) {
			return 0L;
		}
		Number number = (Number)result.get(0);
		return number == null ? 0L : number.longValue(); 
	}

	
	public int loadLoginCount4Time(Date startDate, Date endDate) {
		Criteria criteria = createCriteria(Player.class);
		criteria.add(Restrictions.between("loginTime", startDate, endDate));
		criteria.setProjection(Projections.count("id"));
		Integer uniqueResult = (Integer) criteria.uniqueResult();
		return uniqueResult == null ? 0 : uniqueResult;
	}

	
	public int loadCreateCount4Time(Date startDate, Date endDate) {
		Criteria criteria = createCriteria(Player.class);
		criteria.add(Restrictions.between("createTime", startDate, endDate));
		criteria.setProjection(Projections.count("id"));
		Integer uniqueResult = (Integer) criteria.uniqueResult();
		return uniqueResult == null ? 0 : uniqueResult;
	}

	
	@SuppressWarnings("unchecked")
	public List<RegisterDetailStatistic> loadRegisterDetailStatistics(
			Date startDate, Date endDate) {
		Criteria criteria = createCriteria(RegisterDetailStatistic.class);
		criteria.add(Restrictions.ge("startTime", startDate));
		criteria.add(Restrictions.le("endTime", endDate));
		return criteria.list();
	}
 
}
