package com.yayo.warriors.module.market.facade;

import java.util.Collection;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.market.entity.MarketItem;
import com.yayo.warriors.module.market.model.UserBooth;
import com.yayo.warriors.module.market.vo.UserBoothVO;
import com.yayo.warriors.module.server.listener.LoginListener;
import com.yayo.warriors.module.server.listener.LogoutListener;

/**
 * 摆摊
 * @author liuyuhua
 */
public interface MarketFacade extends LoginListener, LogoutListener {
	
	/**
	 * 加载玩家自己的摊位信息
	 * @param playerId       玩家的ID
	 * @return
	 */
	UserBooth loadBoothByPlayerId(long playerId);
	
	/**
	 * 摊位货品上架
	 * 
	 * @param  playerId    			玩家的ID
	 * @param  goodsId        		物品道具PK(唯一标识)
	 * @param  sellSilver    		售卖价格(银两)
	 * @param  sellGolden     		售卖价格(元宝)
	 * @return {@link ResultObject}	返回值对象
	 */
	ResultObject<MarketItem> putProps2Market(long playerId, long goodsId, long sellSilver, long sellGolden);
	
	/**
	 * 装备上架
	 * 
	 * @param  playerId       		玩家Id
	 * @param  goodsId          	装备主键
	 * @param  sellSilver        	银币价格
	 * @param  sellGolden         	金币价格
	 * @return {@link ResultObject}	返回值对象
	 */
	ResultObject<MarketItem> putEquip2Market(long playerId, long goodsId, long sellSilver, long sellGolden);

	/**
	 * 购买物品
	 * 
	 * @param  playerId      	 	玩家的ID
	 * @param  marketItemId        	购买摆摊物品的ID {@link MarketItem#getId()}
	 * @return {@link Integer}		购买的摊位物品信息
	 */
	int buyMarketItem(long playerId, long marketItemId);

	
	/**
	 * 摊位物品下架
	 * 
	 * @param  playerId      		玩家的ID
	 * @param  marketItemId         摊位物品ID
	 * @return {@link Integer}		返回值信息
	 */
	int removeMarketItem(long playerId, long marketItemId);
	 
	/**
	 * 更新玩家摊位顺序
	 * 注:玩家的摊位,以玩家的等级越高排位越前. 相同等级玩家,随机任意一个
	 *    
	 * @param playerId      		玩家的ID
	 * @param level         		玩家的等级
	 */
	void updateRank(long playerId, int level);

	/**
	 * 摆摊搜索
	 * 
	 * @param  keywords       		关键字
	 * @param  type           		搜索分类
	 * @return {@link Collection}	返回值
	 */
	Collection<UserBoothVO> searchPlayerBooth(String keywords, int type);
	
	/**
	 * 摆摊分类查看
	 * 
	 * @param  itemType       		物品类型
	 * @return {@link Collection}	用户摆摊信息返回值
	 */
	Collection<UserBoothVO> loadMarketByItemType(int itemType);
	
	
	/**
	 * 修改玩家摊位名字
	 * 
	 * @param playerId              玩家ID
	 * @param keywords              关键字
	 * @return {@link CommonConstant}
	 */
	int modifyBoothName(long playerId, String keywords);
	
}
