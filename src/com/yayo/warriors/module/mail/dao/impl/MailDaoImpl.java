package com.yayo.warriors.module.mail.dao.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.yayo.common.db.dao.impl.CommonDaoImpl;
import com.yayo.warriors.module.mail.dao.MailDao;
import com.yayo.warriors.module.mail.entity.Mail;

/**
 * 邮件DAO
 * 
 * @author huachaoping
 */
@Repository
public class MailDaoImpl extends CommonDaoImpl implements MailDao {

	
	@SuppressWarnings("unchecked")
	
	public List<Long> listMailIds(int mailType) {
		Criteria criteria = createCriteria(Mail.class);
		criteria.add(Restrictions.eq("mailType", mailType));
		criteria.setProjection(Projections.id());
		return criteria.list();
	}

}
