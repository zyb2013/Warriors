package com.yayo.warriors.module.alliance.manager;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.yayo.warriors.basedb.model.AllianceConfig;
import com.yayo.warriors.basedb.model.AllianceShopConfig;
import com.yayo.warriors.basedb.model.AllianceSkillConfig;
import com.yayo.warriors.module.alliance.entity.Alliance;
import com.yayo.warriors.module.alliance.entity.PlayerAlliance;
import com.yayo.warriors.module.alliance.types.Title;
import com.yayo.warriors.module.user.entity.PlayerBattle;

/**
 * 帮派Manager
 * @author liuyuhua
 */
public interface AllianceManager {
	
	
	/**
	 * 退出帮派
	 * @param playerAlliance   玩家的帮派对象
	 * @param battle           玩家战斗对象
	 * @return {@link Boolean} true 成功 false 失败
	 */
	boolean gquitAlliance(PlayerAlliance playerAlliance,PlayerBattle battle);
	
	/**
	 * 解散帮派
	 * @param playerAlliance    玩家的帮派对象
	 * @param {@link List}      帮派中的所有成员
	 */
	List<Long> disbandAlliance(PlayerAlliance playerAlliance);
	 
	 /**
	  * 获取所有帮派的ID集合
	  * @return
	  */
	 List<Long> getAllianceIds();
	
	/**
	 * 通过帮派ID获取帮派对象
	 * @param allianceId           帮派ID
	 * @return {@link Alliance}    帮派对象
	 */
	 Alliance getAlliance(long allianceId);
	
	/**
	 * 通过玩家ID获取帮派对象
	 * @param playerId             玩家的ID  
	 * @return {@link Alliance}    帮派对象
	 */
	 Alliance getAlliance4PlayerId(long playerId);
	 
	 /**
	  * 通过玩家战斗对象获取帮派对象
	  * @param battle              玩家战斗对象
	  * @return  {@link Alliance}  帮派对象
	  */
	 Alliance getAlliance4Battle(PlayerBattle battle);
	 
	 /**
	  * 通过帮派ID,获取帮派所有成员
	  * @param allianceId           帮派ID
	  * @param flush                是否刷新(成员排序)
	  * @return {@link Collection}  帮派成员集合
	  */
	 List<Long> getAllianceMembers(long allianceId,boolean flush);
	 
	 /**
	  * 获取所有帮派的名字
	  * @return {@link Collection}  帮派的名字
	  */
	 Map<String,Long> getAllianceNames();
	
	/**
	 * 获取玩家帮派对象
	 * @param playerId                玩家的ID
	 * @return {@link PlayerAlliance} 玩家帮派对象
	 */
	 PlayerAlliance getPlayerAlliance(PlayerBattle battle);
	 
	 /**
	  * 创建帮派
	  * @param playerAlliance         玩家帮派对象
	  * @param alliance               帮派对象
	  * @return {@link Boolean} true 创建成功 false 创建失败
	  */
	 boolean createAlliance(PlayerAlliance playerAlliance, Alliance alliance);
	 
	 
	 
	 /**
	  * 获取帮派基础配置
	  * @param level                   帮派等级
	  * @return {@link AllianceConfig} 帮派基础配合对象
	  */
	 AllianceConfig getAllianceConfig(int level);
	 
	 /**
	  * 获取帮派商店配置
	  * @param shopId                      商店编号
	  * @return {@link AllianceShopConfig} 帮派商店对象
	  */
	 AllianceShopConfig getAllianceShopConfig(int shopId);
	 
	 /**
	  * 获取帮派技能配置
	  * @param researchId                   帮派技能编号ID
	  * @return {@link AllianceSkillConfig} 帮派技能对象
	  */
	 AllianceSkillConfig getAllianceSkillConfig(int researchId);
	 
	 /**
	  * 任命玩家职位
	  * @param playerAlliance         玩家帮派对象
	  * @param targetAlliance         目标玩家帮派对象
	  * @param title                  职位
	  * @return {@link Boolean}       true 成功 false 反之
	  */
	 boolean appointTitle(PlayerAlliance playerAlliance,PlayerAlliance targetAlliance,Title title);
	 
	 /**
	  * 解雇帮派成员
	  * @param playerAlliance         玩家帮派对象
	  * @param targetAlliance         目标玩家帮派对象
	  * @param targetBattle           目标玩家战斗对象
	  * @return {@link Boolean}       true 成功 false 反之
	  */
	 boolean dismissMember(PlayerAlliance playerAlliance,PlayerAlliance targetAlliance,PlayerBattle targetBattle);
	 
	 /**
	  * 通过玩家的ID获取帮派所有成员
	  * @param playerId               玩家的ID
	  * @return {@link List}          成员ID列表集合
	  */
	 List<Long> getMembers4Player(long playerId);
	 
	 /**
	  * 加入帮派
	  * @param playerAlliance         玩家帮派对象
	  * @param alliance               帮派对象
	  * @return {@link Boolean}       true 成功 false 反之
	  */
	 boolean joinAlliance(PlayerAlliance playerAlliance,Alliance alliance);
	 
	 /**
	  * 帮派玩家总人数
	  * @param allianceId             帮派的ID
	  * @return {@link Integer}       成员数量
	  */
	 int sizeAllianceMembers(long allianceId);
	 

}
