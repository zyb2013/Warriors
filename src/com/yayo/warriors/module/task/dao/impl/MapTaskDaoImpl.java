package com.yayo.warriors.module.task.dao.impl;

import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.yayo.common.db.dao.impl.CommonDaoImpl;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.task.dao.MapTaskDao;
import com.yayo.warriors.module.task.entity.UserMapTask;


@Repository
public class MapTaskDaoImpl extends CommonDaoImpl implements MapTaskDao {


	@SuppressWarnings("unchecked")
	
	public List<Long> listUserMapTaskId(long playerId) {
		Criteria critiria = createCriteria(UserMapTask.class);
		critiria.add(Restrictions.eq("playerId", playerId));
		critiria.setProjection(Projections.id());
		return critiria.list();
	}

	
	public Long getUserMapTask(long playerId, int chain) {
		Criteria critiria = createCriteria(UserMapTask.class);
		critiria.add(Restrictions.eq("playerId", playerId));
		critiria.add(Restrictions.eq("chain", chain));
		critiria.setProjection(Projections.id());
		Long userTaskId = (Long) critiria.uniqueResult();
		return userTaskId == null ? 0L : userTaskId;
	}

	
	public void createMapTask(UserMapTask userMapTask, Collection<UserProps> propsList) {
		this.save(userMapTask);
		if(propsList != null && !propsList.isEmpty()) {
			for (UserProps userProps : propsList) {
				this.save(userProps);
			}
		}
	}

	
	public void removeAll(long playerId) {
		Session session = getSession();
		String sql = "DELETE FROM userMapTask WHERE playerId =:id";
		Query createQuery = session.createSQLQuery(sql);
		createQuery.setParameter("id", playerId);
		createQuery.executeUpdate();
	}
}
