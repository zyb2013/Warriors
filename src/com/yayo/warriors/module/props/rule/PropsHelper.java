package com.yayo.warriors.module.props.rule;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.PropsStackResult;

/**
 * 道具帮助类
 * 
 * @author Hyint
 */
@Component
public class PropsHelper {
	
	@Autowired
	private PropsManager propsManager;

	private static ObjectReference<PropsHelper> ref = new ObjectReference<PropsHelper>();
	
	@PostConstruct
	void init(){
		ref.set(this);
	}
	
	public static PropsHelper getInstance() {
		return ref.get();
	}
	
	/**
	 * <pre>
	 * 计算道具的堆叠信息接口. <br>
	 * 
	 * 该接口主要是计算需要放入的道具, 是否可以堆叠到背包中已存在的物品上, 但是不会提供入库. 具体怎么入库, 由调用放自己来做逻辑校验.
	 * </pre>
	 * 
	 * <li>如果背包中有该物品, 则可以先堆叠满, 再创建新的一堆</li>
	 * <li>如果背包中没有该物品, 则直接创建新的一堆</li>
	 * 
	 * @param  playerId					角色ID
	 * @param  backpack					背包号
	 * @param  propsId					道具ID
	 * @param  count					堆叠数量
	 * @param  binding					绑定信息
	 * @return {@link PropsStackResult}	道具堆叠信息
	 */
	public static PropsStackResult calcPropsStack(long playerId, 
		int backpack, int propsId, int count, boolean binding) {
		PropsStackResult propsStackResult = PropsStackResult.valueOf();
		PropsConfig props = getInstance().propsManager.getPropsConfig(propsId);
		if(props == null || count <= 0) {
			return propsStackResult;
		}
		
		
		//当前的
		int currCount = count;
		int maxAmount = props.getMaxAmount();
		//时效性
		Date expirateDate = props.getExpirateDate(false);
		List<UserProps> currBackProps = getInstance().propsManager.listUserPropByBaseId(playerId, propsId, backpack);
		if(currBackProps != null && !currBackProps.isEmpty()) {
			for (UserProps userProps : currBackProps) {
				if(currCount <= 0 || !userProps.isExpirationEquals(expirateDate) || userProps.isBinding() != binding) {
					continue;
				}
				
				Long userPropsId = userProps.getId();
				Integer cacheCount = propsStackResult.getMergeProps().get(userPropsId);
				cacheCount = cacheCount == null ? 0 : cacheCount;
				int currPropsCount = userProps.getCount();
				
				//可以增加的数量
				int canAddCount = maxAmount - currPropsCount - cacheCount;
				if(canAddCount <= 0) {
					continue;
				}
				
				int addCount = Math.min(canAddCount, currCount);
				propsStackResult.getMergeProps().put(userPropsId, cacheCount + addCount);
				currCount -= addCount;
			}
		}

		int maxStack = (int) Math.ceil(currCount / (double)maxAmount);
		List<UserProps> newUserProps = propsStackResult.getNewUserProps();
		for (int currStack = 0; currStack < maxStack; currStack++) {
			int currAddCount = Math.min(currCount, maxAmount);
			if(currAddCount > 0) {
				currCount -= currAddCount;
				newUserProps.add(UserProps.valueOf(playerId, propsId, currAddCount, backpack, expirateDate, binding));
			}
		}
		return propsStackResult;
	}
	
	/**
	 * 新创建道具列表
	 * 
	 * @param  playerId			角色ID
	 * @param  backpack			背包号
	 * @param  propsId			基础ID
	 * @param  count			道具数量
	 * @param  binding			绑定状态
	 * @return {@link List}		用户道具列表
	 */
	public static List<UserProps> newUserProps(long playerId, int backpack, int propsId, int count, boolean binding) {
		PropsConfig props = getInstance().propsManager.getPropsConfig(propsId);
		if(props == null || count <= 0) {
			return Collections.emptyList();
		}
		
		int currCount = count;								//当前的数量
		int maxAmount = props.getMaxAmount();				//道具的最大堆叠数量
		Date expirateDate = props.getExpirateDate(false);	//道具的时效性
		int maxStack = (int) Math.ceil(currCount / (double)maxAmount);
		List<UserProps> newUserProps = new LinkedList<UserProps>();
		for (int currStack = 0; currStack < maxStack; currStack++) {
			int currAddCount = Math.min(currCount, maxAmount);
			if(currAddCount > 0) {
				currCount -= currAddCount;
				newUserProps.add(UserProps.valueOf(playerId, propsId, currAddCount, backpack, expirateDate, binding));
			}
		}
		return newUserProps;
	}
}
