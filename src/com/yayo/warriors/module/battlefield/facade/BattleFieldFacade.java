package com.yayo.warriors.module.battlefield.facade;


import java.util.Map;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.battlefield.constant.BattleFieldConstant;
import com.yayo.warriors.module.battlefield.vo.CollectTaskVO;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.socket.vo.ChangeScreenVo;

/**
 * 乱武战场定义
 * @author jonsai
 *
 */
public interface BattleFieldFacade {
	
	/**
	 * 进入战场
	 * @param playerId			角色id
	 * @return
	 */
	int enterBattleField(long playerId, Map<String, Object> resultMap);
	
	/**
	 * 取得角色
	 * @param userDomain		角色域对象  {@link UserDomain}
	 * @return
	 */
	GameMap getBattleFieldGameMap(UserDomain userDomain);
	
	/**
	 * 判断是否在战场中
	 * @param playerId
	 * @return
	 */
	boolean isInBattleField(long playerId);
	
	/**
	 * 接收玩家被击杀事件
	 * @param attacker			攻击者 {@link UserDomain}
	 * @param target			被攻击者 {@link UserDomain}
	 */
	void processKillPlayers(UserDomain attacker, UserDomain target);
	
	/**
	 * 接收采集事件
	 * @param 	attacker		攻击者 {@link UserDomain}
	 * @param 	npcId			npc id
	 * @param 	baseId			物品id
	 */
	int processCollect(UserDomain attacker, int npcId, int baseId);
	
	/**
	 * 退出战场
	 * @param	playerId		角色id
	 * @return
	 */
	ResultObject<ChangeScreenVo> exitBattleField(long playerId);
	
	/**
	 * 领取目标奖励
	 * @param	playerId		角色id
	 * @return					错误码返回值 {@link BattleFieldConstant}
	 */
	int reward(long playerId);
	
	/**
	 * 接采集任务
	 * @param	playerId		角色id
	 * @return
	 */
	int acceptCollectTask(long playerId);
	
	/**
	 * 领取战场采集奖励
	 * @param playerId			角色id
	 * @param	userPropsId		用来交任务的用户道具id {@link UserProps#id}
	 * @return					错误码返回值 {@link BattleFieldConstant}
	 */
	int rewardCollectTask(long playerId, long userPropsId);

	/**
	 * 处理战场请求
	 * @param playerId			角色id
	 * @param cmd				请求指令
	 * @param resultMap			返回map
	 * @return					错误码返回值 {@link BattleFieldConstant}
	 */
	int battleRequestCmd(long playerId, int cmd, Map<String, Object> resultMap);
	
	/**
	 * 角色进入乱武战场
	 * @param userDomain
	 */
	void doEnterBattleField(UserDomain userDomain);
}
