package com.yayo.warriors.module.campbattle.dao.impl;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.yayo.common.db.dao.impl.CommonDaoImpl;
import com.yayo.common.utility.DateUtil;
import com.yayo.warriors.module.campbattle.dao.CampBattleDao;
import com.yayo.warriors.module.campbattle.entity.CampBattleHistory;
import com.yayo.warriors.module.campbattle.entity.PlayerCampBattleHistory;
import com.yayo.warriors.module.campbattle.entity.PlayerCampBattleRecord;
import com.yayo.warriors.module.campbattle.rule.CampBattleRule;
import com.yayo.warriors.module.campbattle.type.CampTitle;
import com.yayo.warriors.module.user.type.Camp;

@Repository
public class CampBattleDaoImpl extends CommonDaoImpl implements CampBattleDao {

	
	public void saveCampBattleInfo(Collection<CampBattleHistory> campBattles, Collection<PlayerCampBattleHistory> playerCampBattles, Collection<PlayerCampBattleRecord> playerCampBattleRecords) {
		if(campBattles != null){
			this.save( campBattles.toArray() );
		}
		
		if(playerCampBattles != null && playerCampBattles.size() > 0){
			this.save( playerCampBattles.toArray() );
		}
		
		if(playerCampBattleRecords != null && playerCampBattleRecords.size() > 0){
			this.update( playerCampBattleRecords );
		}
	}

	@SuppressWarnings("unchecked")
	
	public CampBattleHistory getCampBattleHistory(Date date, Camp camp) {
		Criteria criteria = getSession().createCriteria(CampBattleHistory.class);
		criteria.add(Restrictions.eq("battleDate", date));
		if(camp != Camp.NONE){
			criteria.add(Restrictions.eq("camp", camp));
		}
		criteria.addOrder(Order.desc("scores"))
				.addOrder(Order.desc("bossHurtHP"));
		List<CampBattleHistory> list = (List<CampBattleHistory>)criteria.list();
		return list != null && list.size() > 0 ? list.get(0) : null;
	}

	@SuppressWarnings("unchecked")
	
	public List<Long> getPlayerCampBattleHistory(Date date, Camp camp) {
		Criteria criteria = getSession().createCriteria(PlayerCampBattleHistory.class);
		criteria.add(Restrictions.eq("battleDate", date));
		if(camp != Camp.NONE){
			criteria.add(Restrictions.eq("camp", camp));
		}
		criteria.addOrder(Order.desc("scores"))
				.addOrder(Order.desc("killPlayers"))
				.addOrder(Order.desc("bossHurtHP"))
				.setProjection(Projections.id());
		return (List<Long>) criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	
	public List<Long> getPlayerTotalScoreList(Camp camp) {
		Criteria criteria = getSession().createCriteria(PlayerCampBattleRecord.class);
		if(camp != Camp.NONE){
			criteria.add(Restrictions.eq("camp", camp));
		}
		
		//本周内有进入过阵营战场
		Date firstTime = DateUtil.firstTimeOfWeek(Calendar.MONDAY, null);
		criteria.add(Restrictions.isNotNull("battleDate"));
		criteria.add(Restrictions.ge("battleDate", firstTime));
		
		criteria.addOrder(Order.desc("totalScore"))
				.addOrder(Order.asc("joins"))
				.addOrder(Order.asc("id"))
				.setProjection(Projections.id());
		return (List<Long>) criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	
	public List<Long> getCampTitlePlayers(Camp camp) {
		Criteria criteria = getSession().createCriteria(PlayerCampBattleRecord.class);
		if(camp != Camp.NONE){
			criteria.add(Restrictions.eq("camp", camp));
		}
		
		criteria.add(Restrictions.ne("campTitle", Camp.NONE));
		criteria.addOrder(Order.asc("campTitle"))
				.addOrder(Order.asc("id"))
				.setProjection(Projections.id());
		return (List<Long>) criteria.list();
	}

	@SuppressWarnings("unchecked")
	
	public List<Date> getCampBattleDates() {
		Criteria criteria = getSession().createCriteria(CampBattleHistory.class);
		criteria.addOrder(Order.desc("battleDate"))
				.setProjection(Projections.distinct(Projections.property("battleDate")))
				.setMaxResults(CampBattleRule.CAMP_BATTLE_RECORD_FETCH_COUNT);
		
		return (List<Date>) criteria.list();
	}

	@SuppressWarnings("unchecked")
	
	public List<Long> getCampLeaderPlayerIds(Camp camp) {
		Criteria criteria = getSession().createCriteria(PlayerCampBattleHistory.class);
		if(camp != Camp.NONE){
			criteria.add(Restrictions.eq("camp", camp));
		}
		criteria.add(Restrictions.eq("last", true))
				.add(Restrictions.eq("campTitle", CampTitle.LEADER))
				.addOrder(Order.desc("id"))
				.setProjection(Projections.id());
		return (List<Long>) criteria.list();
	}

	
	public void clearPlayerCampBattleRecord() {
		Query query = getSession().createQuery("update PlayerCampBattleRecord set joins = 0, totalScore = 0 ");
		query.executeUpdate();
	}
	
	
	public void clearCampTitle() {
		Query query = getSession().createQuery("update PlayerCampBattleRecord set campTitle = :campTitle, suitReward = null");
		query.setInteger("campTitle", CampTitle.NONE.ordinal());
		query.executeUpdate();
	}
	
}
