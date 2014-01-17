
package com.yayo.warriors.module.user.facade;

import org.apache.mina.core.session.IoSession;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.campbattle.constant.CampBattleConstant;
import com.yayo.warriors.module.server.listener.LoginListener;
import com.yayo.warriors.module.user.constant.UserConstant;
import com.yayo.warriors.module.user.entity.DailyRecord;
import com.yayo.warriors.module.user.model.BindSessionResult;
import com.yayo.warriors.module.user.model.LoginResult;
import com.yayo.warriors.module.user.model.LoginWrapper;
import com.yayo.warriors.module.user.type.FightMode;

/**
 * 用户接口
 * 
 * @author Hyint
 */
public interface UserFacade extends LoginListener {
	
	/**
	 * 查询登陆返回值
	 * 
	 * @param  userName					用户帐号信息
	 * @return {@link LoginResult}		返回值对象, 内容: {@link LoginResult}
	 */
	LoginResult getLoginResult(String userName);
	
	/**
	 * 绑定新的连接
	 * 
	 * @param  session				socket session
	 * @param  serialNum			密钥SN
	 * @param  clientIp				客户端IP
	 * @return {@link ResultObject}	返回值信息
	 */
	ResultObject<BindSessionResult> bindNewSession(IoSession session, String serialNum, String clientIp);
	
	/**
	 * 创建角色对象
	 * 
	 * @param  userName				用户名
	 * @param  password				密     码
	 * @param  playerName			角色名
	 * @param  job					角色职业
	 * @param  sex					角色性别
	 * @param  icon					角色图标
	 * @return {@link Integer}		返回值对象
	 */
	int createPlayer(String userName, String password, String playerName, int job, int sex, int icon);

	/**
	 * 登陆选择角色
	 * 
	 * @param  userName				账号名
	 * @param  password				密码
	 * @param  playerId				角色ID
	 * @param  branching			分线ID 
	 * @return {@link ResultObject} 返回值对象
	 */
	ResultObject<String> selectPlayer(String userName, String password, long playerId, int branching, String clientIp);

	/**
	 * 查询登陆封装对象
	 * 
	 * @param  serialNum				随机SN
	 * @return {@link LoginWrapper}		登陆封装对象
	 */
	LoginWrapper getLoginWrapper(String serialNum);

	/**
	 * 移除登陆封装对象
	 * 
	 * @param  serialNum				随机SN
	 * @return {@link LoginWrapper}		登陆封装对象
	 */
	void removeLoginWrapper(String serialNum);
	
	/**
	 * 把登陆序号封装类设置到缓存中
	 * 
	 * @param  playerId 				角色ID
	 * @param  branching				分线号
	 * @param  address					地址信息
	 * @return {@link String}			Key
	 */
	String putSerialNum2Cache(long playerId, int branching, String address);
	
	/***
	 * 更新角色的战斗模式
	 * 
	 * @param  playerId					角色ID
	 * @param  fightMode				战斗模式. 详细见:{@link FightMode}
	 * @param  byClient					是否通过客户端更新属性
	 * @return {@link ResultObject}		返回值		
	 */
	ResultObject<Integer> updateFightMode(long playerId, int fightMode, boolean byClient);

	/**
	 * 保存用户新手引导步骤
	 * @param	playerId				角色id
	 * @param	stepId					新手引导步骤id
	 * @return  Integer					{@link UserConstant}					
	 */
	int saveGuideStep(long playerId, int stepId);
	
	//----------------------------------------------------------------------

	/**
	 * 获得角色的属性
	 * 
	 * @param  playerId					角色属性
	 * @param  params					Key参数
	 * @return {@link Object[]}			返回值参数			
	 */
	Object[] getPlayerAttribute(long playerId, Object[] params);
	
	/**
	 * 回城复活
	 * 
	 * @param  playerId                  角色ID
	 * @return {@link UserConstant}      用户模块返回值
	 */
	int backRevive(long playerId);
	
	/**
	 * 原地复活
	 * 
	 * @param playerId                  角色ID
	 * @param propsId                   物品的ID
	 * @return {@link UserConstant}     用户模块返回值
	 */
	int propsRevive(long playerId,long propsId); 
	
	/**
	 * 阵营战场复活
	 * @param playerId
	 * @return					{@link CampBattleConstant}
	 */
	int campBattleRevive(long playerId);
	
	/**
	 * 阵营战场复活
	 * @param playerId
	 * @return					{@link CampBattleConstant}
	 */
	int battleFieldRevive(long playerId);
	
	/**
	 * 领取教学信息奖励
	 * 
	 * @param  playerId					角色ID
	 * @param  rewardId					奖励ID
	 * @return {@link Integer}			用户模块返回值
	 */
	int receiveGuideRewards(long playerId, int rewardId);
	
	
	/**
	 * 保存玩家防沉迷信息
	 * 
	 * @param playerId                  角色ID
	 * @param state                     状态
	 * @return {@link CommonConstant}   返回值
	 */
	int saveAdultMessage(long playerId, int state);
	
	/**
	 * 保存玩家时装是否显示
	 * 
	 * @param playerId                  角色ID
	 * @param state                     状态
	 * @return  {@link UserConstant}    返回值
	 */
	int saveFashionShow(long playerId,boolean state);
	
	/**
	 * 更新玩家的级别
	 * 
	 * @param  playerId					角色ID
	 * @param  type						级别类型						
	 * @return {@link Integer}			返回值
	 */
	int updatePlayerCapacity(long playerId, int type);
	
	/**
	 * 角色日常记录
	 * @param playerId
	 * @return
	 */
	DailyRecord getDailyRecord(long playerId);
}
