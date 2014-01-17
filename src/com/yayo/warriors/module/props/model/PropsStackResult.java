package com.yayo.warriors.module.props.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yayo.warriors.module.props.entity.UserProps;

/**
 * 道具堆叠结果.
 * <pre>
 * 堆叠是会根据当前背包的详细情况, 计算出该道具是需要新创建, 还是堆叠到背包已存在的物品上.
 * 当背包中没有该物品, 则需要新创建一堆物品, 此时: {@link PropsStackResult#getNewUserProps()} 会有一个物品
 * 当背包中有该物品, 则需要对当前已存在的物品上增加数量, 此时: {@link PropsStackResult#getMergeProps()} 会增加数量
 * </pre>
 * 
 * @author Hyint
 */
public class PropsStackResult {

	/** 新建的用户道具列表 */
	private List<UserProps> newUserProps = new ArrayList<UserProps>(0);

	/** 合并的物品信息. { 用户道具ID, 叠加的数量 } */
	private Map<Long, Integer> mergeProps = new HashMap<Long, Integer>(0);

	public List<UserProps> getNewUserProps() {
		return newUserProps;
	}

	public Map<Long, Integer> getMergeProps() {
		return mergeProps;
	}
	
	/**
	 * 构建道具返回值
	 * 
	 * @return	{@link PropsStackResult} 	道具返回值	
	 */
	public static PropsStackResult valueOf() {
		return new PropsStackResult();
	}
	
	public UserProps[] getNewPropsArray() {
		return this.newUserProps.toArray(new UserProps[newUserProps.size()]);
	}
}
