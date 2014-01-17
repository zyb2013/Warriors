package com.yayo.warriors.basedb.adapter;

import static com.yayo.warriors.type.IndexName.*;

import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceAdapter;
import com.yayo.warriors.basedb.model.MortalAddedConfig;
import com.yayo.warriors.basedb.model.MortalBodyConfig;

/**
 * 基础服务类
 * @author huachaoping
 *
 */
@Component
public class MortalBodyConfigService extends ResourceAdapter {
	
	@Override
	public void initialize() {
		
	}
	
	
	/**
	 * 查询基础肉身数据
	 * 
	 * @param roleJob
	 * @param type
	 * @return {@link MortalBodyConfig}
	 */
	public MortalBodyConfig getMorbodyConfig(int roleJob, int type, int level) {
		return resourceService.getByUnique(MORTAL_JOB_TYPE_LEVEL, MortalBodyConfig.class, roleJob, type, level);	
	}
	
	/** 
	 * 获得加成基础数据
	 * 
	 * @param roleJob
	 * @param level
	 * @return {@link MortalAddedConfig}
	 */
	public MortalAddedConfig getMortalAddedConfig(int roleJob, int level) {
		return resourceService.getByUnique(MORTAL_JOB_LEVEL, MortalAddedConfig.class, roleJob, level);
	}

}
