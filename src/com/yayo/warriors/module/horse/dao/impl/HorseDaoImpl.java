package com.yayo.warriors.module.horse.dao.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.springframework.stereotype.Repository;

import com.yayo.common.db.dao.impl.CommonDaoImpl;
import com.yayo.warriors.module.horse.dao.HorseDao;
import com.yayo.warriors.module.horse.entity.Horse;

/**
 * 坐骑数据访问层
 * @author liuyuhua
 */
@Repository
public class HorseDaoImpl  extends CommonDaoImpl implements HorseDao{

	
	public void createHorse(Horse horse) {
		if(horse != null){
			this.save(horse);
		}
	}

	
	@SuppressWarnings("unchecked")
	public List<Integer> getAllPlayerHorseLevel() {
		Criteria criteria = createCriteria(Horse.class);
		criteria.setProjection(Projections.property("level"));
		return criteria.list();
	}

}
