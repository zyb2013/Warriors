package com.yayo.warriors.module.mortal.facade;

import java.util.List;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.mortal.vo.MortalBodyVo;
import com.yayo.warriors.module.props.entity.BackpackEntry;

/**
 * 服务端接口
 * @author huachaoping
 *
 */
public interface MortalFacade {
	
	/**
	 * 肉身升级
	 * 
	 * @param playerId         角色ID
	 * @param mortalType       类型
	 * @param userItems        升级所需道具:用户道具ID_数量|...
	 * @param userPropsId      增加概率的用户道具ID
	 * @param useProps         是否使用增加概率道具
	 * @param autoCount        自动购买的数量
	 * @return {@link ResultObject}
	 */
	ResultObject<List<BackpackEntry>> mortalBodyLevelUp(long playerId, int type, String userItems, boolean useProps, long userPropsId, int autoCount);

	
	/**
	 * 获得所有加成属性
	 * @param playerId
	 * @return
	 */
	MortalBodyVo getAllAttribute(long playerId);
	
	
}
