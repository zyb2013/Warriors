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
import com.yayo.warriors.module.task.dao.TaskDao;
import com.yayo.warriors.module.task.entity.UserTask;


@Repository
public class TaskDaoImpl extends CommonDaoImpl implements TaskDao {

	
	@SuppressWarnings("unchecked")
	
	public List<Long> listUseraTaskId(long playerId) {
		Criteria critiria = createCriteria(UserTask.class);
		critiria.add(Restrictions.eq("playerId", playerId));
		critiria.setProjection(Projections.id());
		return critiria.list();
	}


	
	public Long getUserTaskId(long playerId, int chain) {
		Criteria critiria = createCriteria(UserTask.class);
		critiria.add(Restrictions.eq("playerId", playerId));
		critiria.add(Restrictions.eq("chain", chain));
		critiria.setProjection(Projections.id());
		Long userTaskId = (Long) critiria.uniqueResult();
		return userTaskId == null ? 0L : userTaskId;
	}

	
	
	public void createTask(UserTask userTask, Collection<UserProps> propsList) {
		this.save(userTask);
		if(propsList != null && !propsList.isEmpty()) {
			for (UserProps userProps : propsList) {
				this.save(userProps);
			}
		}
	}

	
	public void removeAll(long playerId) {
		Session session = getSession();
		String sql = "DELETE FROM userTask WHERE playerId =:id";
		Query createQuery = session.createSQLQuery(sql);
		createQuery.setParameter("id", playerId);
		createQuery.executeUpdate();
	}

	
	@SuppressWarnings("unchecked")
	
	public List<UserTask> listUserTask(int chain, int taskId) {
		Criteria critiria = createCriteria(UserTask.class);
		critiria.add(Restrictions.eq("chain", chain));
		critiria.add(Restrictions.eq("taskId", taskId));
		return critiria.list();
	}

	
	
	public void delete(Collection<UserTask> userTasks) {
		if(userTasks != null && !userTasks.isEmpty()) {
			hibernateTemplate.deleteAll(userTasks);
		}
	}
	
}
