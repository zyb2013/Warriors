package com.yayo.warriors.module.gift.rule;

import com.yayo.warriors.module.gift.constant.GiftConstant;

/**
 * 礼包规则
 * 
 * @author huachaoping
 */
public class GiftRule {
	
	/** 在线礼包开启等级 */
	public final static int OPEN_LEVEL = 11;
	
	/** CDKEY 字符位 */
	public final static int CDKEY_BIT[] = {10, 16, 36};
	
	
	/**
	 * 获取礼包服对应的错误返回值
	 * 
	 * @param  result          礼包服返回值
	 * @return {@link Integer}
	 */
	public final static int getCDKeyResult(int result) {
		switch (result) {
			case -2:     return GiftConstant.GIFT_CDKEY_USED;
			case -3:	 return GiftConstant.CDKEY_OUT_OF_DATE;
			case -6:     return GiftConstant.GIFT_CDKEY_NOT_FOUND;
		}
		return GiftConstant.FAILURE;
	}
	
}
