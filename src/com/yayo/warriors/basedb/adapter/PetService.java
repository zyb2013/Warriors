package com.yayo.warriors.basedb.adapter;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceAdapter;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.model.PetConfig;
import com.yayo.warriors.basedb.model.PetEggConfig;
import com.yayo.warriors.basedb.model.PetQualityConfig;

/**
 * 家将适配器
 *  
 * @author hyint
 */
@Component
public class PetService extends ResourceAdapter {
	
	@Override
	public void initialize() {
		for (PetEggConfig petEgg : resourceService.listAll(PetEggConfig.class)) {
			int petConfigId = petEgg.getPetConfigId();
			petEgg.setPetConfig(resourceService.get(petConfigId, PetConfig.class));
		}
	}
	
	
	/**
	 * "开蛋"获取家将
	 * @param  dropNo 掉落编号
	 * @return {@link PetConfig} 家将配置 
	 */
	public PetConfig initEggPet(int dropNo){
		List<PetEggConfig> listConfig = this.resourceService.listByIndex("dropNo", PetEggConfig.class, dropNo);
		if(listConfig == null || listConfig.isEmpty()){
			return null;
		}
		
		PetEggConfig fullConfig = listConfig.get(0);
		if(fullConfig == null){
			return null;
		}
		
		int totalRate = 0;
		int fullValue = fullConfig.getFullRate();
		int ran = Tools.getRandomInteger(fullValue);
		
		for (PetEggConfig petEggProbability : listConfig) {
			totalRate += petEggProbability.getRate();
			if(ran < totalRate) {
				return petEggProbability.getPetConfig();
			}
		}
		return null;
	}
	
	/**
	 * 初始化家将品质
	 * @param aptitudeNo  编号
	 * @return {@link Integer} 品质
	 */
	public int initPetQuality(int aptitudeNo){
		List<PetQualityConfig> listConfig = this.resourceService.listByIndex("aptitudeNo", PetQualityConfig.class, aptitudeNo);
		if(listConfig == null || listConfig.isEmpty()){
			return 0;
		}
		
		PetQualityConfig fullConfig = listConfig.get(0);
		if(fullConfig == null){
			return 0;
		}
		
		Collections.shuffle(listConfig, new Random());
		
		int totalRate = 0;
		int fullValue = fullConfig.getFullRate();
		int ran = Tools.getRandomInteger(fullValue); //= (int)(Math.random() * fullValue); //
		ran = ran == 0 ? 1 : ran;
		for (PetQualityConfig petQualityConfig : listConfig) {
			totalRate += petQualityConfig.getRate();
			if(ran < totalRate) {
				return petQualityConfig.getRandomQuality();
			}
		}
		
		return 0;
	}

}
