package com.yayo.warriors.module.logger.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Orient;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.type.GoodsType;

public class LoggerPropsHelper {

	/**
	 * 
	 * @param orient
	 * @param newProps
	 * @param newEquips
	 * @param updateProps
	 * @param updatePropsList
	 * @return
	 */
	public static LoggerGoods[] convertLoggerGoods(Orient orient, Collection<UserProps> newProps, Collection<UserEquip> 
			newEquips, Map<Long, Integer> updateProps, Collection<UserProps> updatePropsList) {
		return convertLoggerGoods(orient, newProps, newEquips, updateProps, updatePropsList, 0, -1, 0);
	}

	/**
	 * 
	 * @param orient
	 * @param newPropsList
	 * @param newEquipList
	 * @param updatePropsMap
	 * @param updatePropsList
	 * @param autoBuyCount
	 * @param itemId
	 * @return
	 */
	public static LoggerGoods[] convertLoggerGoods(Orient orient, Collection<UserProps> newPropsList, 
		Collection<UserEquip> newEquipList, Map<Long, Integer> updatePropsMap, Collection<UserProps> 
		updatePropsList, int autoBuyCount, int itemId, long autoBuyCost) {
		
		List<LoggerGoods> list = new ArrayList<LoggerGoods>();
		Map<Integer, Integer> propsMap = new HashMap<Integer, Integer>(2);
		if(newPropsList != null) {
			for(BackpackEntry goods : newPropsList){
				Integer num = propsMap.get(goods.getBaseId());
				propsMap.put(goods.getBaseId(), num != null ? num + goods.getCount() : goods.getCount() );
			}
		}
		
		if(newEquipList != null){
			for(BackpackEntry goods : newEquipList ){
				list.add( LoggerGoods.changedGoods(orient, GoodsType.EQUIP, goods.getBaseId(), goods.getCount()) );
			}
		}
		
		if(updatePropsList != null){
			for(BackpackEntry goods : updatePropsList ){
				Integer num = updatePropsMap.get(goods.getId());
				if(num == null) {
					continue;
				}
				
				Integer count = propsMap.get(goods.getBaseId());
				count = count == null ? 0 : count;
				propsMap.put(goods.getBaseId(), num + count);
				
			}
		}
		
		for(Entry<Integer, Integer> goods : propsMap.entrySet()){
			list.add(LoggerGoods.changedGoods(orient, GoodsType.PROPS, goods.getKey(), goods.getValue()));
		}
		
		if(autoBuyCount > 0) {
			list.add(LoggerGoods.outcomePropsAutoBuyGolden(itemId, autoBuyCount, autoBuyCost));
		}
		
		return list.toArray(new LoggerGoods[list.size()]);
	}
}
