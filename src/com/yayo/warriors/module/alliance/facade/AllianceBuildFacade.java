package com.yayo.warriors.module.alliance.facade;

import java.util.List;
import java.util.Map;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.alliance.entity.Alliance;
import com.yayo.warriors.module.alliance.entity.PlayerAlliance;
import com.yayo.warriors.module.alliance.model.DonateRecord;

/**
 * 帮派建筑类玩法接口 
 * @author liuyuhua
 */
public interface AllianceBuildFacade {
	
	/**
	 * 捐献(游戏币)
	 * @param playerId                     玩家的ID
	 * @param silver                       捐献的铜币 (游戏币)
	 * @return {@link Map}                 返回值  
	 */
	 ResultObject<Map<String,Object>>  donateSilver(long playerId,int silver);
	
	/**
	 * 捐献(物品)
	 * @param playerId                      玩家的ID
	 * @param props                         物品集合           
	 * @return  {@link Map<String,Object>}  返回值 
	 */
	ResultObject<Map<String,Object>> donateProps(long playerId,String props);
	
	/**
	 * 升级帮派建筑
	 * @param playerId                      玩家的ID
	 * @param type                          建筑的类型
	 * @return {@link Integer}              帮派公共返回常量
	 */
	int levelupBuild(long playerId,int type);
	
	/**
	 * 查看捐献/消耗记录
	 * @param playerId                       玩家的ID
	 * @param start                          起始页
	 * @param count                          总条数
	 * @return {
	 *  data     {@link Record[]}            记录数据
	 *  number   {@link Integer}             记录总数   
	 *  pageStart{@link Integer}             其实页 
	 * }                  
	 */
	Map<String,Object> sublistRecord(long playerId,int start,int count);
	
	
	/**
	 * 购买商店里面的商品
	 * @param playerId                 玩家的ID
	 * @param shopId                   商品的ID
	 * @return {@link PlayerAlliance}  玩家帮派对象
	 */
	ResultObject<PlayerAlliance> shoppingAlliance(long playerId,int shopId);
	
	/**
	 * 占卜(抽筋)
	 * @param playerId                      玩家的ID
	 * @return {@link <Map<String,Object>}  返回值 
	 */
	ResultObject<Map<String,Object>> divineAlliance(long playerId);
	
	/**
	 * 研究技能
	 * @param playerId                 玩家的ID
	 * @param researchId               帮派技能配置ID
	 * @return {@link Alliance}        帮派对象
	 */
	ResultObject<Alliance> researchSkill(long playerId,int researchId);
	
	/**
	 * 学习技能
	 * @param playerId                 玩家的ID
	 * @param researchId               帮派技能配置ID
	 * @return {@link PlayerAlliance}  玩家帮派对象
	 */
	ResultObject<PlayerAlliance> studySkill(long playerId,int researchId);
	
	 /**
	  * 分页查询帮派玩家今日捐献值排行
	  * @param playerId               玩家的ID
	  * @param start                  起始页
	  * @param count                  总条数
	  * @return {@link List}          捐献值得集合
	  */
	 List<DonateRecord> sublistDonateRecords(long playerId,int start,int count);
	 
	 /**
	  * 帮派捐献记录数
	  * @param playerId               玩家的ID
	  * @return {@link Integer}       数量
	  */
	 int sizeDonateRecord4Alliance(long playerId);
	 

}
