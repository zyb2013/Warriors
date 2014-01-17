package com.yayo.warriors.module.gift.dao;

import java.util.List;

import com.yayo.common.db.dao.CommonDao;

/**
 * 礼包DAO
 * 
 * @author huachaoping
 */
public interface GiftDao extends CommonDao{
	
	/**
	 * 根据礼包类型获取礼包主键列表
	 * 
	 * @param giftType            礼包类型
	 * @return {@link List}       主键列表
	 */
	List<Integer> getGiftByType(int giftType);
	
}
