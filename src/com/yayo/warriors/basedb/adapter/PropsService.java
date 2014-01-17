package com.yayo.warriors.basedb.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceAdapter;
import com.yayo.warriors.basedb.model.PropsArtificeConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.module.props.type.PropsChildType;
import com.yayo.warriors.type.IndexName;

/**
 * 道具配置接口
 * 
 * @author Hyint
 */
@Component
public class PropsService extends ResourceAdapter {
	
	@Override
	public void initialize() {
		Collection<PropsArtificeConfig> artifices = resourceService.listAll(PropsArtificeConfig.class);
		for (PropsArtificeConfig propsArtifice : artifices) {
			propsArtifice.getMaterialCache();
		}
	}

	public <T> T get(int id, Class<T> clazz) {
		return resourceService.get(id, clazz);
	}
	
	/**
	 * 列出基础道具对象列表
	 * 
	 * @return {@link List}		用户道具列表
	 */
	public List<PropsConfig> listPropsConfig() {
		List<PropsConfig> propsConfigs = new ArrayList<PropsConfig>(5);
		propsConfigs.addAll(resourceService.listByIndex(IndexName.PROPS_CHILDTYPE, PropsConfig.class, PropsChildType.HP_DRUG_ITEM));
		propsConfigs.addAll(resourceService.listByIndex(IndexName.PROPS_CHILDTYPE, PropsConfig.class, PropsChildType.MP_DRUG_ITEM));
		return propsConfigs;
	}
	
	/**
	 * 通过道具子类型得到CDid
	 * @param childType		{@link PropsChildType}
	 * @return
	 */
	public int getCdIdByPropChildType(int propsChildType){
		PropsConfig propsConfig = resourceService.getByUnique(IndexName.PROPS_CHILDTYPE, PropsConfig.class, propsChildType);
		if(propsConfig != null) {
			return propsConfig.getCdId();
		}
		return 0;
	}
	
	
	
}
