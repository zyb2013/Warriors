package com.yayo.warriors.module.title.rule;

import com.yayo.warriors.module.title.model.TitleType;

/**
 * 称号规则
 * 
 * @author huachaoping
 */
public class TitleRule {

	/** 等级相关称号 */
	public static TitleType[] referLevelTitleType = { TitleType.CHUSHEJIANGHU, 	TitleType.XIAOSHISHENSHOU,
												 	  TitleType.CHUXIANFENGMANG,	TitleType.ZANLUTOUJIAO,
												 	  TitleType.XIAOYOUMINGQI,	TitleType.ZHANGJIANJIANGHU,
												 	  TitleType.MINGSHENGHEQI,	TitleType.YANGMINGLIWAN,
												 	  TitleType.WEIMINGXIANHE,	TitleType.MINGCHUANTIANXIA}; 
	
	
	/** 经脉相关称号 */
	public static TitleType[] referMeridianTitleType = { TitleType.CHUKUIMENJING,    TitleType.LUEZHIYIER,
														 TitleType.CHUCHUMAOLU,   TitleType.YOUSUOXIAOCHENG,
														 TitleType.QINGCHEJIASHU, TitleType.DENGTANGRUSHI,
														 TitleType.CHULEIBACUI,   TitleType.YOUSUODACHENG };
		
	
}
