package com.yayo.warriors.module.active.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.springframework.stereotype.Repository;

import com.yayo.common.db.dao.impl.CommonDaoImpl;
import com.yayo.warriors.module.active.dao.ActiveOperatorDao;
import com.yayo.warriors.module.active.entity.OperatorActive;


/**
 * 在线活动管理类 
 * @author liuyuhua
 */
@Repository
public class ActiveOperatorDaoImpl extends CommonDaoImpl implements ActiveOperatorDao{

	
	public List<OperatorActive> getActives() {
		List<OperatorActive> result = new ArrayList<OperatorActive>(5);
		List<Long> ids = this.listActiveOperatorIds();
		if(ids == null || ids.isEmpty()){
			return result;
		}
		
		for(Long id : ids){
			OperatorActive active = this.get(id, OperatorActive.class);
			if(active != null){
				result.add(active);
			}
		}
		return result;
	}
	
	
	public boolean deleteActive(long id) {
		try {
			this.delete(id, OperatorActive.class);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * 获取运营活动列表缓存
	 * @return {@link List}  运营活动Id集合
	 */
	@SuppressWarnings({"unchecked"})
	private List<Long> listActiveOperatorIds(){
		Criteria criteria = createCriteria(OperatorActive.class);
		criteria.setProjection(Projections.id());
		return criteria.list();
	}

	
	public boolean saveActive(OperatorActive active) {
		try {
			this.save(active);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	
	public boolean updateActive(OperatorActive active) {
		try {
			this.update(active);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

}
