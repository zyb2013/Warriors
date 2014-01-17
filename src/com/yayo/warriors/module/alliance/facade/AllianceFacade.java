package com.yayo.warriors.module.alliance.facade;

import java.util.List;
import java.util.Map;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.alliance.entity.Alliance;
import com.yayo.warriors.module.alliance.entity.PlayerAlliance;
import com.yayo.warriors.module.alliance.vo.AllianceVo;
import com.yayo.warriors.module.alliance.vo.ApplyVo;
import com.yayo.warriors.module.alliance.vo.MemberVo;

/**
 * 帮派基础玩法 Facade接口 
 * @author liuyuhua
 */
public interface AllianceFacade {

	/**
	 * 加入帮派
	 * @param allianceId         帮派的ID
	 * @param playerId           玩家角色ID
	 * @return {@link Integer}   帮派公共常量
	 */
	int joinAlliance(long allianceId,long playerId);
	
	
	/**
	 * 获取玩家帮派对象
	 * @param playerId                 玩家的ID
	 * @return {@link PlayerAlliance}  玩家帮派对象             
	 */
	 PlayerAlliance getPlayerAlliance(long playerId);
	 
	 /**
	  * 获取帮派
	  * @param playerId               玩家的ID
	  * @return {@link Alliacne}      帮派对象
	  */
	 Alliance getAlliance(long playerId);
	 
	 /**
	  * 发布公告
	  * @param playerId              玩家的ID
	  * @param content               内容
	  * @return {@link Integer}      帮派公共常量
	  */
	 int releaseNotice(long playerId,String content);
	 
	 /**
	 * 创建帮派(需要使用帮派令)
	 * @param playerId              玩家的ID
	 * @param propsId               道具的ID
	 * @param name                  帮派名字
	 * @return {@link Alliance}     帮派对象
	 */
	 ResultObject<Alliance> createAllianceUseProps(long playerId, long propsId, String name);
	 
	 /**
	  * 创建帮派
	  * @param playerId             玩家的ID
	  * @param name                 帮派名字
	  * @return {@link Alliance}    帮派对象
	  */
	 ResultObject<Alliance> createAlliance(long playerId, String name);
	 
	 /**
	  * 分页查询帮派列表
	  * @param playerId             玩家的ID
	  * @param start                起始页
	  * @param count                总条数
	  * @return {@link List}        帮派显示集合
	  */
	 List<AllianceVo> sublistAlliances(long playerId,int start, int count);
	 
	 /**
	  * 分页查询帮派玩家列表
	  * @param playerId             玩家的ID
	  * @param start                起始页
	  * @param count                总条数
	  * @return {@link List}        帮派成员显示集合
	  */
	 List<MemberVo> sublistMembers(long playerId,int start,int count);
	 
	 /**
	  * 分页查询申请加入帮派玩家列表
	  * @param playerId             玩家的ID
	  * @param start                起始页
	  * @param count                总条数
	  * @return {@link List}        帮派成员显示集合
	  */
	 List<ApplyVo> sublistApplys(long playerId,int start,int count);
	 
	 /**
	  * 帮派的总数 
	  * @return {@link Integer}     帮派的总数
	  */
	 int sizeAlliances();
	 
	 /**
	  * 帮派成员总数
	  * @param allianceId           帮派的ID
	  * @return {@link Integer}     帮派成员总数
	  */
	 int sizeMembers4Alliance(long allianceId);
	 
	 /**
	  * 获取申请加入帮派人员总数
	  * @param allianceId           帮派的ID
	  * @return  {@link Integer}    申请者总数
	  */
	 int sizeApply4Alliance(long allianceId);
	 
	 
	 /**
	  * 解雇帮员
	  * @param playerId             玩家的ID
	  * @param targetId             目标玩家的ID
	  * @return {@link Integer}     帮派公共常量
	  */
	 int dismissMember(long playerId,long targetId);
	 
	 /**
	  * 退出帮派
	  * @param playerId             玩家的ID
	  * @return {@link Integer}     帮派公共常量
	  */
	 int gquitAlliance(long playerId);
	 
	 /**
	  * 解散帮派
	  * @param playerId            玩家的ID
	  * @return {@link Integer}    帮派公共常量
	  */
	 int disbandAlliance(long playerId);
	 

	 /**
	  * 设置帮派验证状态
	  * @param playerId            玩家的ID
	  * @param state               状态
	  * @return {@link Integer}    帮派公共常量
	  */
	 int vilidaAlliance(long playerId,int state);
	 
	 /**
	  * 审批帮员
	  * @param playerId            玩家的ID
	  * @param applyId             申请者ID
	  * @param agree               是否同意
	  * @return {@link Integer}    帮派公共常量
	  */
	 int examineApply(long playerId,long applyId,boolean agree);
	 
	 /**
	  * 转移帮主
	  * @param playerId            玩家的ID
	  * @param targetId            目标玩家的ID
	  * @return {@link Integer}    帮派公共常量
	  */
	 int devolveMaster(long playerId,long targetId);
	 
	 /**
	  * 确认转换帮主
	  * @param playerId            玩家的ID
	  * @param confirm             确认是否
	  * @return {@link Integer}    帮派公共常量
	  */
	 int confirmDevolve(long playerId, boolean confirm);
	 
	 
	 /**
	  * 任命
	  * @param playerId            玩家的ID
	  * @param targetId            目标玩家的ID
	  * @param title               职位
	  * @return {@link Integer}    返回值
	  */
	 ResultObject<Map<String,Object>> appointTitle(long playerId,long targetId,int title);
	 
	 /**
	  * 主动邀请玩家加入帮派
	  * @param playerId            玩家的ID
	  * @param targetId            目标玩家的ID
	  * @return {@link Integer}    帮派公共常量
	  */
	 int inviteMember(long playerId,long targetId);
	 
	 /**
	  * 确认邀请
	  * @param playerId            玩家的ID
	  * @param inviterId           邀请者ID
	  * @param allianceId          帮派的ID
	  * @return {@link Integer}    帮派公共常量
	  */
	 int confirmInvite(long playerId,long inviterId,long allianceId,boolean confirm);
	 
	 
	
}
