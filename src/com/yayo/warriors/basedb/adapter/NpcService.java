package com.yayo.warriors.basedb.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceAdapter;
import com.yayo.warriors.basedb.model.NpcConfig;
import com.yayo.warriors.type.IndexName;

/**
 * 任务服务适配器
 * 
 * @author Hyint
 */
@Component
public class NpcService extends ResourceAdapter {
	
	@Override
	public void initialize() {
	}
	
	/**
	 * 根据地图获取NpcConfig
	 * @param mapId    地图ID
	 * @return
	 */
	public List<NpcConfig> listNpcConfig(int mapId){
		return resourceService.listByIndex(IndexName.NPC_MAPID, NpcConfig.class, mapId);
	}
	
	public List<NpcConfig> listNpcConfig(int screenType, int elementType) {
		return resourceService.listByIndex(IndexName.NPC_SCREEN_TYPE, NpcConfig.class, elementType, screenType);
	}
}
