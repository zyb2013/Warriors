package com.yayo.warriors.basedb.adapter;

import static com.yayo.warriors.type.IndexName.*;

import java.util.List;

import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceAdapter;
import com.yayo.warriors.basedb.model.AchieveConfig;

/**
 * 成就基础服务类
 * 
 * @author huachaoping
 */
@Component
public class AchieveService extends ResourceAdapter {

	@Override
	public void initialize() {

	}

	
	/**
	 * 查询成就基础数据
	 * 
	 * @param  achieveId                主键
	 * @return {@link AchieveConfig}    基础数据
 	 */
	public AchieveConfig getAchieveConfig(int achieveId) {
		return resourceService.get(achieveId, AchieveConfig.class);
	}
	
	
	/**
	 * 查询成就基础数据
	 * 
	 * @param achieveType               类型   
	 * @return {@link List<AchieveConfig>}
	 */
	public List<AchieveConfig> listAchieveConfigs(int achieveType) {
		return resourceService.listByIndex(ACHIEVE_TYPE, AchieveConfig.class, achieveType);
	}
	
	
	/**
	 * 查询成就基础数据
	 * 
	 * @param achieveType               类型   
	 * @return {@link List<AchieveConfig>}
	 */
	public List<Integer> listAchieveConfigIds(int achieveType) {
		return resourceService.listIdByIndex(ACHIEVE_TYPE, AchieveConfig.class, Integer.class, achieveType);
	}
	
}
