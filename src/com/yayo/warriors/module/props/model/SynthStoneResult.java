package com.yayo.warriors.module.props.model;

import java.util.Collection;

import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.props.entity.UserProps;

/**
 * 合成宝石返回值
 * 
 * @author Hyint
 */
public class SynthStoneResult {

	/** 返回值 */
	private int result;
	
	/** 合成成功的数量 */
	private int successCount;
	
	/** 合成失败的数量 */
	private int failureCount;
	
	/** 用户道具列表 */
	private Collection<UserProps> userPropsList;
	
	public int getResult() {
		return result;
	}

	public int getSuccessCount() {
		return successCount;
	}

	public int getFailureCount() {
		return failureCount;
	}

	public Collection<UserProps> getUserPropsList() {
		return userPropsList;
	}

	/**
	 * 构建合成宝石返回值
	 * 
	 * @param  result					返回值
	 * @return {@link SynthStoneResult}	合成宝石返回值
	 */
	public static SynthStoneResult ERROR(int result) {
		SynthStoneResult stoneResult = new SynthStoneResult();
		stoneResult.result = result;
		return stoneResult;
	}
	
	/**
	 * 构建合成宝石返回值
	 * 
	 * @param  successCount				成功的数量
	 * @param  failureCount				失败的数量
	 * @param  userPropsList			用户道具列表
	 * @return {@link SynthStoneResult}	合成宝石返回值
	 */
	public static SynthStoneResult SUCCESS(int successCount, 
		int failureCount, Collection<UserProps> userPropsList) {
		SynthStoneResult stoneResult = new SynthStoneResult();
		stoneResult.successCount = successCount;
		stoneResult.failureCount = failureCount;
		stoneResult.userPropsList = userPropsList;
		stoneResult.result = CommonConstant.SUCCESS;
		return stoneResult;
	}
	
}
