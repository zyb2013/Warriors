package com.yayo.warriors.module.pack.rule;

import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.user.type.Job;

public class BackpackRule {

	/**
	 * 获得默认的背包位置信息
	 * 
	 * @param  job			角色的职业
	 * @param  backpack		背包号
	 * @return
	 */
	public static byte[] getDefaultPosition(Job job, int backpack) {
		if(backpack != BackpackType.USER_PANEL_BACKPACK) {
			return null;
		}

		switch (job) {
			case XINGXIU:	return new byte[]{ 0, 23, 48, 95, 50, 95, 49, 48, 48, 54, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124 };
			case XIAOYAO:	return new byte[]{ 0, 23, 48, 95, 50, 95, 49, 48, 48, 52, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124 };
			case TIANLONG:	return new byte[]{ 0, 23, 48, 95, 50, 95, 49, 48, 48, 48, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124 };
			case TIANSHAN:	return new byte[]{ 0, 23, 48, 95, 50, 95, 49, 48, 48, 53, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124 };
		}
		return null;
		//默认的背包位置. 物理攻击命令
	}
}
