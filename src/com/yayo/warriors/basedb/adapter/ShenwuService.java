package com.yayo.warriors.basedb.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceAdapter;
import com.yayo.warriors.basedb.model.ShenwuAttributeConfig;
import com.yayo.warriors.basedb.model.ShenwuConfig;
import com.yayo.warriors.type.IndexName;

/**
 * 神武接口
 * 
 * @author Hyint
 */
@Component
public class ShenwuService extends ResourceAdapter {

	@Override
	public void initialize() {
		
	}
	
	/**
	 * 获得神武配置
	 * 
	 * @param  shenwuId				神武ID
	 * @param  equipType			装备类型
	 * @return {@link ShenwuConfig}	神武配置
	 */
	public ShenwuConfig getShenwuConfig(int shenwuId, int equipType) {
		return resourceService.getByUnique(IndexName.EQUIP_SHENWUID_CONFIG, ShenwuConfig.class, shenwuId, equipType);
	}

	/**
	 * 获得神武属性配置
	 * 
	 * @param  shenwuId				神武ID
	 * @param  equipType			装备类型
	 * @param  job					装备需求的职业
	 * @return {@link ShenwuConfig}	神武配置
	 */
	public List<ShenwuAttributeConfig> listShenwuAttribute(int shenwuId, int equipType, int job) {
		return resourceService.listByIndex(IndexName.EQUIP_SHENWUID_ATTRIBUTE, ShenwuAttributeConfig.class, shenwuId, equipType, job);
	}

	/**
	 * 获得神武属性配置
	 * 
	 * @param  shenwuId				神武ID
	 * @param  equipType			装备类型
	 * @param  job					装备需求的职业
	 * @param  attribute			属性类型
	 * @return {@link ShenwuConfig}	神武配置
	 */
	public ShenwuAttributeConfig getShenwuAttribute(int shenwuId, int equipType, int job, int attribute) {
		List<ShenwuAttributeConfig> shenwuAttributes = listShenwuAttribute(shenwuId, equipType, job);
		if(shenwuAttributes != null && !shenwuAttributes.isEmpty()) {
			for (ShenwuAttributeConfig shenwuAttribute : shenwuAttributes) {
				if(shenwuAttribute.getAttribute() == attribute) {
					return shenwuAttribute;
				}
			}
		}
		return null;
	}
}
