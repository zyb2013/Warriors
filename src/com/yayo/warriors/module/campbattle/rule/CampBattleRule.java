package com.yayo.warriors.module.campbattle.rule;

import com.yayo.common.utility.TimeConstant;

/**
 * 阵营战场规则类
 * @author jonsai
 *
 */
public abstract class CampBattleRule {
	
	/** 0-初始状态 */
	public static final int STATUS_INIT = 0;
	/** 1-系统准备报名中	 */
	public static final int STATUS_SYS_APPLY = 1;
	/** 2-报名开始 */
	public static final int STATUS_APPLY = 2;
	/** 3-报名者优先进入阵营战场 */
	public static final int STATUS_APPLYER_ENTER = 3;
	/** 4-阵营战开始 */
	public static final int STATUS_START = 4;
	/** 5-计算阵营战结果 */
	public static final int STATUS_SYS_CALC = 5;
	
	/** 报名等级条件 */
	public static int APPLY_MIN_LEVEL = 40;
	
	/** 领奖等级条件 */
	public static int REWARD_MIN_LEVEL = 30;
	
	/** 阵营参与人数小于此人数时需要发邀请  */
	public static int NEED_INVITE_MIN_CAMP_PLAYERS = 20;
	
	/** 阵营战场地图id */
	public static int CAMP_BATTLE_MAPID = 8001;
	
	/** 提交几分钟进入战场 */
	public static int PRE_MIN_ENTER_BATTLE = 5;
	
	/** 阵营战场信息推送的间隔(单位:毫秒), 默认:500毫秒 */
	public static int BATTLE_INFO_PUSH_TIMEUNIT = 500;
	
	/** 阵营记录时间列表大小:10场 */
	public static int CAMP_BATTLE_RECORD_FETCH_COUNT = 10; 
	
	/** 回阵营CD时间 */
	public static int BACK_CD_TIME = 5 * TimeConstant.ONE_MINUTE_MILLISECOND;
	
	/** 强制玩家退出副本CD时间 */
	public static int CLEAR_MAP_CD_TIME = 1 * TimeConstant.ONE_MINUTE_MILLISECOND;
	
	/** 据点脱离战斗时间 */
	public static int CAMP_POINT_LEAVE_FIGHT_TIME = 5 * TimeConstant.ONE_SECOND_MILLISECOND;
	
	/** 战场结束后开启庆功宴的时间 */
	public static int DINNER_DELAY_TIME = 10 * TimeConstant.ONE_MINUTE_MILLISECOND;
}
