package com.yayo.warriors.module.logger.log;

import com.yayo.warriors.module.logger.model.Term;
import com.yayo.warriors.module.logger.model.TraceLog;
import com.yayo.warriors.module.logger.type.LogType;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.onlines.entity.RegisterStatistic;
import com.yayo.warriors.module.user.entity.Player;

/**
 * 角色注册统计日志. 记录玩家的名阵营各职业注册信息日志
 * 
 * @author jonsai
 */
public class RegStatiLogger extends TraceLog {

	private static final LogType type = LogType.PLAYER_REGISTER_STATISTIC; 

	/**
	 * 角色注册统计日志
	 * 
	 * @param playerId			角色ID
	 * @param rs				统计对象
	 */
	public static void regStati(RegisterStatistic rs) {
		TraceLog.log(type, Term.valueOf("campNone", rs.getCampNone()),
						   Term.valueOf("campKnife", rs.getCampKnife()),
						   Term.valueOf("campSword", rs.getCampSword()),
						   Term.valueOf("jobTianlong", rs.getJobTianlong()),
						   Term.valueOf("jobTianshan", rs.getJobTianshan()),
						   Term.valueOf("jobXingxiu", rs.getJobXingxiu()),
						   Term.valueOf("jobXiaoyao", rs.getJobXiaoyao()),
						   Term.valueOf("recordDate", rs.getRecordDate()),
						   Term.valueOf("recordTime", rs.getRecordTime())
						   );
	};

}
