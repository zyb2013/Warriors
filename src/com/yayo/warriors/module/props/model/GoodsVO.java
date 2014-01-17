package com.yayo.warriors.module.props.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.yayo.warriors.module.logger.type.Orient;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.type.GoodsType;

/**
 *	物品获得/消耗推送VO
 */
public class GoodsVO implements Serializable {
	private static final long serialVersionUID = 4121084087106456970L;

	/** 基础物品id */
	private int baseId;
	
	/** 物品类型 	0-道具, 1-装备  2-怪物*/
	private int type = 0;
	
	/** 数量, 说是:>0-获得   <0-失去 */
	private int num;
	
	/**
	 * 物品VO构造
	 * @param baseId
	 * @param goodsType
	 * @param num
	 * @return
	 */
	public static GoodsVO valueOf(int baseId, int goodsType, int num){
		return valueOf(num > 0 ? Orient.INCOME : Orient.OUTCOME, baseId, goodsType, num);
	}
	
	/**
	 * 
	 * @param baseId
	 * @param goodsType
	 * @param num
	 * @return
	 */
	public static GoodsVO valueOf(Orient orient, int baseId, int goodsType, int num){
		GoodsVO vo = new GoodsVO();
		vo.baseId = baseId;
		vo.type = goodsType;
		vo.num = getOrientValue(orient, num);
		return vo;
	}
	
	/**
	 * 
	 * @param newUserProps
	 * @param updateUserPropsList
	 * @param mergeProps
	 * @param userEquips
	 * @return
	 */
	public static Collection<GoodsVO> valuleOf(Collection<UserProps> newUserProps, Collection<UserProps> updateUserPropsList, Map<Long, Integer> mergeProps, Collection<UserEquip> userEquips	){
		return valuleOf(null, newUserProps, updateUserPropsList, mergeProps, userEquips);
	}
	
	/**
	 * 
	 * @param newUserProps
	 * @param updateUserPropsList
	 * @param mergeProps
	 * @param userEquips
	 * @return
	 */
	public static Collection<GoodsVO> valuleOf(Orient orient, Collection<UserProps> newUserProps, Collection<UserProps> updateUserPropsList, Map<Long, Integer> mergeProps, Collection<UserEquip> userEquips	){
		Map<Integer, GoodsVO> map = new HashMap<Integer, GoodsVO>(0);
		if(newUserProps != null){
			for(UserProps userProps : newUserProps){
				GoodsVO goodsVO = map.get(userProps.getBaseId());
				if(goodsVO == null){
					goodsVO = GoodsVO.valueOf(userProps.getBaseId(), GoodsType.PROPS, getOrientValue(orient, userProps.getCount() ) );
					map.put(userProps.getBaseId(), goodsVO);
				} else{
					goodsVO.setNum( getOrientValue(orient, goodsVO.getNum() + userProps.getCount() ) );
				}
			}
		}
		
		if(mergeProps != null){
			for(UserProps userProps : updateUserPropsList){
				GoodsVO goodsVO = map.get(userProps.getBaseId());
				Integer updateValue = mergeProps.get(userProps.getId());
				if(updateValue == null){
					continue;
				}
				if(goodsVO == null){
					goodsVO = GoodsVO.valueOf(userProps.getBaseId(), GoodsType.PROPS, getOrientValue(orient, updateValue) );
					map.put(userProps.getBaseId(), goodsVO);
				} else{
					goodsVO.setNum( getOrientValue(orient, goodsVO.getNum() + updateValue) );
				}
			}
		}
		
		if(userEquips != null){
			for(UserEquip userEquip : userEquips){
				GoodsVO goodsVO = map.get(userEquip.getBaseId());
				if(goodsVO == null){
					goodsVO = GoodsVO.valueOf(userEquip.getBaseId(), GoodsType.EQUIP, getOrientValue(orient, userEquip.getCount()) );
					map.put(userEquip.getBaseId(), goodsVO);
				} else{
					goodsVO.setNum( getOrientValue(orient, goodsVO.getNum() + userEquip.getCount()) );
				}
			}
		}
		
		return map.values();
	}
	
	private static int getOrientValue(Orient orient, int count){
		if(orient != null){
			count = orient == Orient.INCOME ? count : -Math.abs(count);
		}
		return count;
	}

	public int getBaseId() {
		return baseId;
	}

	public void setBaseId(int baseId) {
		this.baseId = baseId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + baseId;
		result = prime * result + type;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof GoodsVO))
			return false;
		GoodsVO other = (GoodsVO) obj;
		if (baseId != other.baseId)
			return false;
		if (type != other.type)
			return false;
		return true;
	}
	
}
