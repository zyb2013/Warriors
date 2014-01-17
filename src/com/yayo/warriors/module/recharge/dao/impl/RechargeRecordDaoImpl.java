package com.yayo.warriors.module.recharge.dao.impl;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.yayo.common.db.dao.impl.CommonDaoImpl;
import com.yayo.warriors.module.recharge.dao.RechargeRecordDao;
import com.yayo.warriors.module.recharge.entity.RechargeRecord;

/**
 * 充值DAO实现类
 * 
 * @author Hyint
 */
@Repository
public class RechargeRecordDaoImpl extends CommonDaoImpl implements RechargeRecordDao {

	@SuppressWarnings("unchecked")
	
	public List<Long> getRechargeRecordIds(long playerId, Date startTime, Date endTime) {
		Criteria criteria = createCriteria(RechargeRecord.class);
		criteria.add(Restrictions.eq("playerId", playerId));
		if(startTime != null) {
			criteria.add(Restrictions.ge("recordTime", RechargeRecord.toRecordDate(startTime)));
		}
		if(endTime != null) {
			criteria.add(Restrictions.le("recordTime", RechargeRecord.toRecordDate(endTime)));
		}
 		criteria.setProjection(Projections.id());
		return criteria.list();
	}

	
	public Long getRechargeRecord(long playerId, Date recordDate) {
		Criteria criteria = createCriteria(RechargeRecord.class);
		criteria.add(Restrictions.eq("playerId", playerId));
		criteria.add(Restrictions.eq("recordTime", RechargeRecord.toRecordDate(recordDate)));
 		criteria.setProjection(Projections.id());
		Long uniqueResult = (Long) criteria.uniqueResult();
		return uniqueResult == null ? 0L : uniqueResult;
	}
	
	
}
