package com.yayo.warriors.basedb.adapter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceAdapter;
import com.yayo.warriors.basedb.model.PropsGiftConfig;
import com.yayo.warriors.module.user.type.Job;
import com.yayo.warriors.type.IndexName;

/**
 * 
 * @author liuyuhua
 */
@Component
public class FastenGiftService extends ResourceAdapter{
	
	@Override
	public void initialize() {
	}
	
	/**
	 * 获取职业相关的礼包奖励配置
	 * @param job        职业
	 * @param giftNo     礼包ID
	 * @return
	 */
	public List<PropsGiftConfig> getGift(Job job,int giftNo){
		List<PropsGiftConfig> result = new ArrayList<PropsGiftConfig>();
		if(job != null && giftNo > 0){
			result.addAll(resourceService.listByIndex(IndexName.PROPS_GIFT_NO_JOB, PropsGiftConfig.class, giftNo, job.ordinal()));
			result.addAll(resourceService.listByIndex(IndexName.PROPS_GIFT_NO_JOB, PropsGiftConfig.class, giftNo, Job.COMMON.ordinal()));
		}
		return result;
	}
	
	

}
