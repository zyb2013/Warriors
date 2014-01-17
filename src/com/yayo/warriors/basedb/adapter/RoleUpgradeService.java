package com.yayo.warriors.basedb.adapter;


import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceAdapter;
import com.yayo.warriors.basedb.model.RoleUpgradeConfig;
import com.yayo.warriors.type.IndexName;

/**
 * 角色升级配置服务接口
 * 
 * @author Hyint
 */
@Component
public class RoleUpgradeService extends ResourceAdapter {
	
	@Override
	public void initialize() {
	}

	/**
	 * 角色升级配置对象
	 * 
	 * @param  job							角色的职业
	 * @param  level						角色的等级
	 * @return {@link RoleUpgradeConfig}	角色升级配置对象
	 */
	public RoleUpgradeConfig getRoleUpgradeConfig(int job, int level) {
		return resourceService.getByUnique(IndexName.USER_JOB_LEVEL, RoleUpgradeConfig.class, job, level);
	}
 
}
