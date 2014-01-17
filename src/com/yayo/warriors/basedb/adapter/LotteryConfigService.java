package com.yayo.warriors.basedb.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceAdapter;
import com.yayo.warriors.basedb.model.EquipConfig;
import com.yayo.warriors.basedb.model.LotteryPropsRateConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.module.lottery.vo.LotteryRewardVo;
import com.yayo.warriors.type.GoodsType;

@Component
public class LotteryConfigService extends ResourceAdapter{
	
	/** 日志 */
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void initialize() {
		Class<LotteryPropsRateConfig> clazz = LotteryPropsRateConfig.class;
		for(LotteryPropsRateConfig propsRate : resourceService.listAll(clazz)) {
			for(LotteryRewardVo rewardVO : propsRate.obtainLotteryPropsList()){
				int baseId = rewardVO.getPropId();
				int propsType = rewardVO.getPropsType();
				if(propsType == GoodsType.EQUIP){
					EquipConfig equipConfig = resourceService.get(baseId, EquipConfig.class);
					if(equipConfig == null){
						logger.error("基础装备不存在,id:[{}]", baseId);
					}
				} else if(propsType == GoodsType.PROPS){
					PropsConfig propsConfig = resourceService.get(baseId, PropsConfig.class);
					if(propsConfig == null){
						logger.error("基础道具不存在,id:[{}]", baseId);
					}
				}
			}
		}
	}

}
