package com.yayo.warriors.basedb.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;

/**
 * 道具炼化配置表格
 * 
 * @author Hyint
 */
@Resource
public class PropsArtificeConfig {

	@Id
	private int id;
	
	/** 合成需要的游戏币 */
	private int silver;
	
	/** 需要的道具信息 . 格式: 道具ID_道具数量|... */
	private String materials;

	/** 炼化出来的物品绑定状态 */
	private boolean binding;
	
	/** 材料缓存信息 */
	private volatile Map<Integer, Integer> materialCachhe = null;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSilver() {
		return silver;
	}

	public void setSilver(int silver) {
		this.silver = silver;
	}

	public String getMaterials() {
		return materials;
	}

	public void setMaterials(String materials) {
		this.materials = materials;
	}

	public Map<Integer, Integer> getMaterialCache() {
		if(this.materialCachhe != null) {
			return this.materialCachhe;
		}
		
		synchronized (this) {
			if(this.materialCachhe != null) {
				return this.materialCachhe;
			}
			
			this.materialCachhe = new HashMap<Integer, Integer>(1);
			List<String[]> arrays = Tools.delimiterString2Array(this.materials);
			if(arrays != null && !arrays.isEmpty()) {
				for (String[] array : arrays) {
					int itemId = Integer.valueOf(array[0]);
					int itemCount = Integer.valueOf(array[1]);
					this.materialCachhe.put(itemId, itemCount);
				}
			}
		}
		return this.materialCachhe;
	}
	
	/**
	 * 获得合成材料数量
	 * 
	 * @param  count			合成数量
	 * @return {@link Map}		返回需要的道具数量
	 */
	public Map<Integer, Integer> getSynthMaterialByCount(int count) {
		Map<Integer, Integer> currentCache = this.getMaterialCache();
		Map<Integer, Integer> newCountCache = new HashMap<Integer, Integer>(currentCache.size());
		for (Entry<Integer, Integer> entry : currentCache.entrySet()) {
			newCountCache.put(entry.getKey(), entry.getValue() * count);
		}
		return newCountCache;
	}
	
	public int getSynthCostSilver(int count) {
		return this.silver * count;
	}
	
	public boolean isBinding() {
		return binding;
	}

	public void setBinding(boolean binding) {
		this.binding = binding;
	}

	@Override
	public String toString() {
		return "PropsArtificeConfig [id=" + id + ", silver=" + silver + ", materials=" + materials + "]";
	}
}
