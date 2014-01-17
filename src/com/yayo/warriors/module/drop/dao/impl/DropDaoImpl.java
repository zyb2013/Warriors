package com.yayo.warriors.module.drop.dao.impl;

import java.util.Collection;

import org.springframework.stereotype.Repository;

import com.yayo.common.db.dao.impl.CommonDaoImpl;
import com.yayo.warriors.module.drop.dao.DropDao;
import com.yayo.warriors.module.drop.entity.DropRecord;

/**
 * 掉落记录DAO对象
 * 
 * @author Hyint
 */
@Repository
public class DropDaoImpl extends CommonDaoImpl implements DropDao {
	
	
	public void updateDropRecords(Collection<DropRecord> dropRecords) {
		if(dropRecords != null && !dropRecords.isEmpty()) {
			for (DropRecord dropRecord : dropRecords) {
				if(dropRecord != null) {
					this.update(dropRecord);
				}
			}
		}
	}

}
