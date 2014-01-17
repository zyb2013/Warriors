package com.yayo.warriors.module.props.entity;

import javax.persistence.Column;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.commons.lang.ArrayUtils;

import com.yayo.common.db.model.BaseModel;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.type.Quality;
import com.yayo.warriors.type.GoodsType;

/**
 * 抽象的物品对象
 * 
 * @author Hyint
 * @param <PK>
 */
@MappedSuperclass
public class BackpackEntry extends BaseModel<Long> {
	private static final long serialVersionUID = -204808597413950011L;
	
	/** 物品ID,自增 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	/** 基础ID */
	private int baseId;
	
	/** 装备/道具的数量 */
	private int count;
	
	/** 物品在背包的位置 */
	@Column(name="positionId")
	private int index = -1;

	/** 装备/道具所在的背包 */
	private int backpack;
	
	/** 装备/道具的绑定状态 */
	private boolean binding = false;
	
	/** 装备的品质, 决定装备的颜色 */
	@Enumerated
	private Quality quality = Quality.WHITE;
	
	/** 是否正在交易中, true-交易中, false-不在交易中 */
	@Transient
	private transient volatile boolean trading = false;
	
	/** 物品类型. 道具/装备 */
	@Transient
	protected transient int goodsType = -1;
	
	public int getBaseId() {
		return baseId;
	}

	public void setBaseId(int baseId) {
		this.baseId = baseId;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
 
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getGoodsType() {
		return goodsType;
	}

	public void setGoodsType(int goodsType) {
		this.goodsType = goodsType;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getBackpack() {
		return backpack;
	}

	public void setBackpack(int backpack) {
		this.backpack = backpack;
	}
	
	public void updateBackpackAndPut2Market() {
		this.index = -1;
		this.backpack = BackpackType.MARKET_BACKPACK;
	}
	
	public boolean validBackpack(int...backpack) {
		return ArrayUtils.contains(backpack, this.backpack);
	}
	
	public boolean isTrading() {
		return trading;
	}

	public void setTrading(boolean trading) {
		this.trading = trading;
	}

	public Quality getQuality() {
		return quality;
	}

	public void setQuality(Quality quality) {
		this.quality = quality;
	}
	
	public boolean isBinding() {
		return binding;
	}

	public void setBinding(boolean binding) {
		this.binding = binding;
	}

	/**
	 * 构建装备实体对象
	 * 
	 * @param  userEquipId				用户装备ID
	 * @param  baseId					基础装备ID
	 * @param  count					装备的数量
	 * @param  backpack					所在的背包
	 * @param  quality					装备的品质
	 * @param  index					装备所在背包的位置
	 * @param  binding					绑定状态
	 * @return {@link BackpackEntry}	用户背包实体对象
	 */
	public static BackpackEntry valueEquip(long userEquipId, int baseId, int count, int backpack, Quality quality, int index, boolean binding) {
		BackpackEntry backpackEntry = new BackpackEntry();
		backpackEntry.count = count;
		backpackEntry.index = index;
		backpackEntry.baseId = baseId;
		backpackEntry.id = userEquipId;
		backpackEntry.quality = quality;
		backpackEntry.binding = binding;
		backpackEntry.backpack = backpack;
		backpackEntry.setGoodsType(GoodsType.EQUIP);
		return backpackEntry;
	}

	/**
	 * 构建装备实体对象
	 * 
	 * @param  userEquipId				用户装备ID
	 * @param  baseId					基础装备ID
	 * @param  count					装备的数量
	 * @param  backpack					所在的背包
	 * @param  quality					装备的品质
	 * @param  index					装备所在背包的位置
	 * @param  binding					绑定状态
	 * @return {@link BackpackEntry}	用户背包实体对象
	 */
	public static BackpackEntry valueEquipEmpty(long userEquipId, int baseId, int backpack, Quality quality, int index, boolean binding) {
		BackpackEntry backpackEntry = new BackpackEntry();
		backpackEntry.count = 0;
		backpackEntry.index = index;
		backpackEntry.baseId = baseId;
		backpackEntry.id = userEquipId;
		backpackEntry.binding = binding;
		backpackEntry.quality = quality;
		backpackEntry.backpack = backpack;
		backpackEntry.setGoodsType(GoodsType.EQUIP);
		return backpackEntry;
	}

	/**
	 * 构建装备实体对象
	 * 
	 * @param  userEquipId				用户道具ID
	 * @param  baseId					基础道具ID
	 * @param  count					道具的数量
	 * @param  backpack					所在的背包
	 * @param  quality					装备的品质
	 * @param  index					道具所在背包的位置
	 * @param  binding					绑定状态
	 * @return {@link BackpackEntry}	用户背包实体对象
	 */
	public static BackpackEntry valueProps(long userPropsId, int baseId, int count, int backpack, Quality quality, int index, boolean binding) {
		BackpackEntry backpackEntry = new BackpackEntry();
		backpackEntry.count = count;
		backpackEntry.index = index;
		backpackEntry.baseId = baseId;
		backpackEntry.id = userPropsId;
		backpackEntry.quality = quality;
		backpackEntry.binding = binding;
		backpackEntry.backpack = backpack;
		backpackEntry.setGoodsType(GoodsType.PROPS);
		return backpackEntry;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + baseId;
		result = prime * result + this.getGoodsType();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!super.equals(obj)) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		
		BackpackEntry other = (BackpackEntry) obj;
		return id != null && other.id != null 
			&& id.equals(other.id) && baseId == other.baseId 
			&& this.getGoodsType() == other.getGoodsType();
	}

	@Override
	public String toString() {
		return "BackpackEntry [id=" + id + ", baseId=" + baseId + ", count=" + count + ", index="
				+ index + ", backpack=" + backpack + ", binding=" + binding + ", quality="
				+ quality + "]";
	}
}
