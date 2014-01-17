package com.yayo.warriors.basedb.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.module.user.type.Job;
import com.yayo.warriors.type.GoodsType;

/**
 * 基础商店表格
 * 
 * @author Hyint
 */
@Resource
public class ShopConfig {

	/** 商店ID */
	@Id
	private int id;
	
	/** 出售的道具ID */
	private int propsId;
	
	/** 详细见: {@link GoodsType} */
	private int goodsType;
	
	/**
	 * 道具所属职业
	 * @see Job
	 */
	private int roleJob;

	/** 出售该物品的NPCID. 格式: npcId_npcId_... */
	private String npcIds;
	
	/** 购买该物品时, 是否绑定 */
	private boolean binding;
	
	/**
	 * 可以出售该物品的NPCID列表
	 */
	@JsonIgnore
	private List<Integer> npcIdList = null;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNpcIds() {
		return npcIds;
	}

	public void setNpcIds(String npcIds) {
		this.npcIds = npcIds;
	}

	public int getPropsId() {
		return propsId;
	}

	public void setPropsId(int propsId) {
		this.propsId = propsId;
	}

	public int getGoodsType() {
		return goodsType;
	}

	public void setGoodsType(int goodsType) {
		this.goodsType = goodsType;
	}

	public boolean isBinding() {
		return binding;
	}

	public void setBinding(boolean binding) {
		this.binding = binding;
	}
	
	public int getRoleJob() {
		return roleJob;
	}

	public void setRoleJob(int roleJob) {
		this.roleJob = roleJob;
	}

	/**
	 * 解析可以出售该物品的NPCID列表
	 * @return
	 */
	public List<Integer> getNpcIdList() {
		if(this.npcIdList != null) {
			return this.npcIdList;
		}
		
		synchronized (this) {
			if(this.npcIdList != null) {
				return this.npcIdList;
			}
			this.npcIdList = new ArrayList<Integer>();
			if(StringUtils.isBlank(this.npcIds)) {
				return this.npcIdList;
			}
			String[] split = this.npcIds.split(Splitable.ATTRIBUTE_SPLIT);
			for (String element : split) {
				if(StringUtils.isBlank(element)) {
					continue;
				}
				
				int npcId = Integer.valueOf(element);
				if(!npcIdList.contains(npcId)) {
					this.npcIdList.add(npcId);
				}
			}
		}
		return this.npcIdList;
	}

	@Override
	public String toString() {
		return "ShopConfig [id=" + id + ", propsId=" + propsId + ", goodsType=" + goodsType
				+ ", roleJob=" + roleJob + ", npcIds=" + npcIds + ", binding=" + binding
				+ ", npcIdList=" + npcIdList + "]";
	}
	
}
