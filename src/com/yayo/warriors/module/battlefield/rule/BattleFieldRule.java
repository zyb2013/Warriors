package com.yayo.warriors.module.battlefield.rule;

import com.yayo.common.utility.TimeConstant;


/**
 * 每日战场规则定义
 * @author jonsai
 *
 */
public abstract class BattleFieldRule {
	
	/** 0-初始(或结束)状态 */
	public static final int STATUS_INIT = 0;
	/** 1-阵营战开始 */
	public static final int STATUS_START = 1;

	
	/** 每个房间最大的人数 */
	public final static int ROOM_MAX_PLAYERS = 60;
	
	/** 每个房间最大的各阵营人数 */
	public final static int ROOM_MAX_CAMP_PLAYERS = 20;
	
	/** 乱武战场场次时间格式 */
	public final static String BATTLE_TIME_FORMATE = "yyyy-MM-dd HH:mm:00";
	
	/** 乱武战场地图id */
	public final static int BATTLE_FIELD_MAPID = 8002;
	
	/** 乱武战场最小进入等级 */
	public final static int BATTLE_FIELD_MIN_LEVEL = 35;
	
	/** 退出后再进入乱武战场地图时间限制(毫秒),默认:5分钟 */
	public final static int BATTLE_FIELD_ENTER_CDTIME = 5 * TimeConstant.ONE_MINUTE_MILLISECOND;
}
