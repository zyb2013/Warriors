package com.yayo.warriors.basedb.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceAdapter;
import com.yayo.warriors.basedb.model.MeridianConfig;
import com.yayo.warriors.module.meridian.MeridianRule;
import com.yayo.warriors.module.meridian.type.MeridianStage;
import com.yayo.warriors.module.user.type.Job;

/**
 * 经脉点接口
 * 
 * @author huachaoping
 */
@Component
public class MeridianService extends ResourceAdapter {
	
	/** 总经脉点数 */
	private int merdianCount = 0;

	@Override
	public void initialize() {
		merdianCount = 0;
		for (MeridianConfig meridianConfig : resourceService.listAll(MeridianConfig.class)) {
			merdianCount++;
			int job = meridianConfig.getJob();
			int meridianType = meridianConfig.getMeridianType();
			String typeKey = getMeridianTypeKey(meridianType);
			String jobTypeKey = getMeridianJobTypeKey(meridianType, job);
			resourceService.addToIndex(typeKey, meridianConfig.getId(), MeridianConfig.class);
			resourceService.addToIndex(jobTypeKey, meridianConfig.getId(), MeridianConfig.class);
		}
	}

	
	/**
	 * 获得经脉类型Key			
	 * 
	 * @param  meridianType		经脉类型
	 * @param  job				职业类型
	 * @return {@link String}	经脉类型Key
	 */
	private String getMeridianJobTypeKey(int meridianType, int job) {
		return new StringBuffer().append("MERIDIAN_TYPE_").append(meridianType).append("_JOB_").append(job).toString();
	}
	
	/**
	 * 获得经脉类型Key			
	 * 
	 * @param  meridianType		经脉类型
	 * @return {@link String}	经脉类型Key
	 */
	private String getMeridianTypeKey(int meridianType) {
		return new StringBuffer().append("MERIDIAN_TYPE_").append(meridianType).toString();
	}
	
	/**
	 * 查询经脉
	 * 
	 * @param meridianType         经脉类型
	 * @return
	 */
	public List<MeridianConfig> listMeridianConfig(int meridianType){
		String key = getMeridianTypeKey(meridianType);
		return resourceService.listByIndex(key, MeridianConfig.class);
	}
	
	/**
	 * 查询经脉
	 * 
	 * @param  type					经脉类型
	 * @param  job					角色职业
	 * @return {@link List}			经脉点列表
	 */
	public List<MeridianConfig> listMeridianConfig(int meridianType, int job){
		String key = getMeridianJobTypeKey(meridianType, job);
		return resourceService.listByIndex(key, MeridianConfig.class);
	}

	/**
	 * 获得经脉配置信息
	 * 
	 * @param  meridianId				经脉ID
	 * @return {@link MeridianConfig}	经脉配置对象
	 */
	public MeridianConfig getMeridianConfig(int meridianId) {
		return resourceService.get(meridianId, MeridianConfig.class);
	}
	
	/**
	 * 查询经脉点总数量
	 * @return
	 */
	public int getMeridianConfigCount() {
		return merdianCount;
	}
	
	/**
	 * 获得阶段经脉点数量(不同职业拥有一份经脉数据, 每个职业有两个阶段的经脉{@link MeridianStage}, 两阶段的经脉数据一致)
	 * @return
	 */
	public int getStageCount() {
		return merdianCount / (Job.values().length - 1) / 2 ;
	}
	
	/**
	 * 获得每个类型经脉的数量
	 *
	 * @return {@link Map<Integer, Integer>}
	 */
	public Map<Integer, Integer> getTypeMeridianCount() {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i <= MeridianRule.TYPE_LIMIT; i++) {
			int count = this.listMeridianConfig(i).size() / (Job.values().length - 1);
			map.put(i, count);
		}
		return map;
	}
}
