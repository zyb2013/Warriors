package com.yayo.warriors.module.logger.log;

import java.util.Date;

import com.yayo.common.utility.DatePattern;
import com.yayo.common.utility.DateUtil;
import com.yayo.warriors.module.logger.model.Term;
import com.yayo.warriors.module.logger.model.TraceLog;
import com.yayo.warriors.module.logger.type.LogType;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;

/**
 * 角色登录日志
 * 
 * 格式: 
 * <pre>
 * time  			: 当前服务器时间
 * source  			: 登录类型
 * ip    			: 客户机的IP
 * hp	 			: 角色的当前HP量
 * mp	 			: 角色的当前MP量
 * gas				: 角色的当前Gas量
 * exp	 			: 角色的当前经验值
 * hpBag			: 角色的HP便携包值
 * mpBag			: 角色的MP便携包值
 * level			: 角色的等级
 * hpMax			: 角色的HPMax值
 * mpMax			: 角色的MPMax值
 * gasMax			: 角色的GasMax值
 * silver			: 角色的游戏币剩余量
 * golden			: 角色的金币剩余量
 * playerId			: 角色的ID
 * branching		: 角色所在的分线
 * fightMode		: 战斗模式.(0-和平)
 * loginDays		: 连续登录天数
 * loginTime	   	: 角色登录日期
 * logoutTime		: 角色登出时间
 * loginCount		: 登录次数
 * onlineTimes		: 角色当前在线时间
 * userName			: 角色账号
 * playerName		: 角色名
 * createTime    	: 角色创建时间
 * </pre>
 * 
 * @author Hyint
 */
public class LoginLogger extends TraceLog {

	/**
	 * 角色登录日志
	 * 
	 * @param player			角色对象
	 * @param battle			角色战斗属性
	 * @param source			日志来源
	 * @param clientIp			登录的IP信息
	 */
	public static void login(Player player, PlayerBattle battle, String clientIp, int branching) {
		Date logoutTime = player.getLogoutTime();
		String datePattern = DatePattern.PATTERN_YYYYMMDDHHMMSS;
		Date loginTime = player.getLoginTime();
		TraceLog.log(LogType.LOGIN, Term.valueOf(IP, clientIp),
									Term.valueOf(HP, battle.getHp()),
									Term.valueOf(MP, battle.getMp()),
									Term.valueOf(GAS, battle.getGas()),
									Term.valueOf(EXP, battle.getExp()),
									Term.valueOf(BRANCHING, branching),
									Term.valueOf(LEVEL, battle.getLevel()),
									Term.valueOf(HPBAG, battle.getHpBag()),
									Term.valueOf(MPBAG, battle.getMpBag()),
									Term.valueOf(HPMAX, battle.getHpMax()),
									Term.valueOf(MPMAX, battle.getMpMax()),
									Term.valueOf(GASMAX, battle.getGasMax()),
									Term.valueOf(SILVER, player.getSilver()),
									Term.valueOf(GOLDEN, player.getGolden()),
									Term.valueOf(PLAYERID, player.getId()),
									Term.valueOf(PLAYER_NAME, player.getName()),
									Term.valueOf(LOGINDAYS, player.getLoginDays()),
									Term.valueOf(LOGINCOUNT, player.getLoginCount()),
									Term.valueOf(ONLINETIMES, player.getOnlineTimes()),
									Term.valueOf(SOURCE, Source.PLAYER_LOGIN.getCode()),
									Term.valueOf(FIGHT_MODE, battle.getMode().ordinal()),
									Term.valueOf(LOGINTIME, DateUtil.date2String(loginTime, datePattern)),
									Term.valueOf(LOGOUTTIME, DateUtil.date2String(logoutTime, datePattern)),
									Term.valueOf(USER_NAME, player.getUserName()),
									Term.valueOf(CREATE_TIME, DateUtil.date2String(player.getCreateTime(), datePattern)));
	};

	/**
	 * 角色登出日志
	 * 
	 * @param player			角色对象
	 * @param battle			角色战斗属性
	 * @param clientIp			登出的IP信息
	 * @param branching			分线号
	 */
	public static void logout(Player player, PlayerBattle battle, String clientIp, int branching) {
		Date logoutTime = player.getLogoutTime();
		String datePattern = DatePattern.PATTERN_YYYYMMDDHHMMSS;
		Date loginTime = player.getLoginTime();
		TraceLog.log(LogType.LOGIN, Term.valueOf(IP, clientIp),
									Term.valueOf(HP, battle.getHp()),
									Term.valueOf(MP, battle.getMp()),
									Term.valueOf(GAS, battle.getGas()),
									Term.valueOf(EXP, battle.getExp()),
									Term.valueOf(BRANCHING, branching),
									Term.valueOf(LEVEL, battle.getLevel()),
									Term.valueOf(PLAYERID, player.getId()),
									Term.valueOf(HPBAG, battle.getHpBag()),
									Term.valueOf(MPBAG, battle.getMpBag()),
									Term.valueOf(HPMAX, battle.getHpMax()),
									Term.valueOf(MPMAX, battle.getMpMax()),
									Term.valueOf(GASMAX, battle.getGasMax()),
									Term.valueOf(SILVER, player.getSilver()),
									Term.valueOf(GOLDEN, player.getGolden()),
									Term.valueOf(PLAYER_NAME, player.getName()),
									Term.valueOf(LOGINDAYS, player.getLoginDays()),
									Term.valueOf(LOGINCOUNT, player.getLoginCount()),
									Term.valueOf(ONLINETIMES, player.getOnlineTimes()),
									Term.valueOf(FIGHT_MODE, battle.getMode().ordinal()),
									Term.valueOf(SOURCE, Source.PLAYER_LOGOUT.getCode()),
									Term.valueOf(LOGINTIME, DateUtil.date2String(loginTime, datePattern)),
									Term.valueOf(LOGOUTTIME, DateUtil.date2String(logoutTime, datePattern)),
									Term.valueOf(USER_NAME, player.getUserName()),
									Term.valueOf(CREATE_TIME, DateUtil.date2String(player.getCreateTime(), datePattern))
									);
	}; 
}
