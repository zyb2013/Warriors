package com.yayo.warriors.module.active.dao;

import java.util.List;

import com.yayo.common.db.dao.CommonDao;
import com.yayo.warriors.module.active.entity.OperatorActive;

/**
 * 运营活动Dao 
 * @author liuyuhua
 */
public interface ActiveOperatorDao extends CommonDao{
	
	/**
	 * 保存运营活动
	 * @param active            运营活动
	 * @return {@link Boolean}  true 成功 , false 失败
	 */
	boolean saveActive(OperatorActive active);
	
	/**
	 * 更新运营活动
	 * @param active            运营活动
	 * @return {@link Boolean}  true 成功 , false 失败
	 */
	boolean updateActive(OperatorActive active);
	
	
	/**
	 * 获取运营活动集合
	 * @return {@link List}       运营活动集合
	 */
	List<OperatorActive> getActives();
	
	/**
	 * 删除运营活动对象
	 * @param id                   运营活动ID
	 * @return {@link Boolean}     true 成功 , false 失败
	 */
	boolean deleteActive(long id);

}
