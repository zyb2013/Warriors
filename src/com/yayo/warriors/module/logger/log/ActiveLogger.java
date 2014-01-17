package com.yayo.warriors.module.logger.log;
import com.yayo.warriors.module.logger.model.Term;
import com.yayo.warriors.module.logger.model.TraceLog;
import com.yayo.warriors.module.logger.type.LogType;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;

/**
 * 活动日志
 * @author liuyuhua
 */
public class ActiveLogger extends TraceLog {

	/**
	 * 活动日志
	 * @param player        玩家对象
	 * @param petBattle     玩家战斗对象
	 * @param activeName    活动名字
	 * @param activeId      基础活动ID
	 * @param rewardId      活动奖励ID
	 * @param coupon        奖励绑定元宝
	 * @param silver        奖励铜币
	 * @param exp           奖励经验
	 * @param props         奖励物品
	 * @param equip         奖励装备
	 */
	public static void activeLogger(Player player,PlayerBattle battle,String activeName,int type,int activeId,int rewardId,int coupon,int silver,int exp,String props,String equip){
		 log(LogType.ACTIVE, Term.valueOf(SOURCE, Source.ACTIVE_OPERATOR.getCode()),
				     Term.valueOf(PLAYERID, player.getId()),
				     Term.valueOf(PLAYER_NAME, player.getName()),
				     Term.valueOf(TYPE, type),
				     Term.valueOf(LEVEL, battle.getLevel()),
				     Term.valueOf(ACTIVE_NAME, activeName),
				     Term.valueOf(ACTIVE_ID, activeId),
				     Term.valueOf(REWARD_ID, rewardId),
				     Term.valueOf(COUPON, coupon),
				     Term.valueOf(SILVER, silver),
				     Term.valueOf(EXP, exp),
				     Term.valueOf(BASE_USER_PROPS, props),
				     Term.valueOf(BASE_USER_EQUIP, equip)
				    );
	}

}
