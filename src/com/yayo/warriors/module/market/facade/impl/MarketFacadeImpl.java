package com.yayo.warriors.module.market.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.market.constant.MarketConstant.*;
import static com.yayo.warriors.module.market.rule.MarketRule.*;
import static com.yayo.warriors.module.pack.type.BackpackType.*;
import static com.yayo.warriors.module.user.type.AttributeKeys.*;
import static com.yayo.warriors.module.achieve.model.AchieveType.FIRST_ACHIEVE;
import static com.yayo.warriors.module.achieve.model.FirstType.FIRST_MARKET;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.cache.CachedService;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.EnumUtils;
import com.yayo.warriors.basedb.model.EquipConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.achieve.facade.AchieveFacade;
import com.yayo.warriors.module.friends.helper.FriendHelper;
import com.yayo.warriors.module.logger.log.GoldLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.log.SilverLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.market.entity.MarketItem;
import com.yayo.warriors.module.market.facade.MarketFacade;
import com.yayo.warriors.module.market.helper.MarketHelper;
import com.yayo.warriors.module.market.manager.MarketManager;
import com.yayo.warriors.module.market.model.Sell;
import com.yayo.warriors.module.market.model.UserBooth;
import com.yayo.warriors.module.market.type.ItemType;
import com.yayo.warriors.module.market.type.MarketState;
import com.yayo.warriors.module.market.type.SearchType;
import com.yayo.warriors.module.market.util.SellPage;
import com.yayo.warriors.module.market.util.SellShelf;
import com.yayo.warriors.module.market.vo.UserBoothVO;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.type.Quality;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.socket.handler.friends.FriendsHandler;

/**
 * 这个业务比较特殊
 * 1.当玩家上线的时候,随身摊位数据也要马上加载和上线
 * 2.当玩家下线的时候,随身摊位数据也要马上下线,清空数据
 * @author liuyuhua
 *
 */
@Component
public class MarketFacadeImpl implements MarketFacade {
	/** 日志 */
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired
	private SellPage page;
	@Autowired
	private SellShelf shelf;
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private DbService dbService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private CachedService cachedService;
	@Autowired
	private MarketManager marketManager;
	@Autowired
	private MarketHelper marketHelper;
	@Autowired
	private AchieveFacade achieveFacade;
	
	/** 摆摊交易对象锁 */
	private static final Object MARKET_OBJECT_LOCK = new Object();
	
	
	
	public void onLogoutEvent(UserDomain userDomain) {
		this.shelfDown(userDomain.getPlayerId());
	}
	
	/**
	 * 加载玩家自己的摊位信息
	 * 
	 * @param  playerId       		玩家的ID
	 * @return {@link UserBooth}	玩家的摊位信息
	 */
	
	public UserBooth loadBoothByPlayerId(long playerId) {
		return marketManager.getUserBooth(playerId, true);
	}
	
	/**
	 * 摊位货品上架
	 * 
	 * @param  playerId    			玩家的ID
	 * @param  goodsId        		物品道具PK(唯一标识)
	 * @param  sellSiliver    		售卖价格(银两)
	 * @param  sellGolden     		售卖价格(元宝)
	 * @return {@link ResultObject}	返回值对象
	 */
	
	public ResultObject<MarketItem> putProps2Market(long playerId, long goodsId, long sellSiliver, long sellGolden) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		UserBooth userBooth = marketManager.getUserBooth(playerId, true);
		if(userBooth == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(userBooth.size() >= MARKET_MAX) {
			return ResultObject.ERROR(MARKET_LIMIT);
		}
		
		UserProps userProps = propsManager.getUserProps(goodsId);
		if (userProps == null || userProps.getPlayerId() != playerId) {
			return ResultObject.ERROR(ITEM_NOT_PLAYER);
		} else if (!userProps.validBackpack(DEFAULT_BACKPACK)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if (userProps.isBinding()) {
			return ResultObject.ERROR(ITEM_PROPS_BINDING);
		} else if(userProps.getCount() <= 0) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(userProps.isTrading()) {
			return ResultObject.ERROR(ITEM_CANNOT_USE);
		}
		 
		PropsConfig propsConfig = userProps.getPropsConfig();
		if(propsConfig == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		}
		
		MarketItem marketItem = MarketItem.props2MarketItem(playerId, userProps, sellSiliver, sellGolden);
		ChainLock lock = LockUtils.getLock(MARKET_OBJECT_LOCK, userDomain.getPackLock(), userBooth);
		try {
			lock.lock();
			if (userProps.getPlayerId() != playerId) {
				return ResultObject.ERROR(ITEM_NOT_PLAYER);
			} else if (!userProps.validBackpack(DEFAULT_BACKPACK)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if (userProps.isBinding()) {
				return ResultObject.ERROR(ITEM_PROPS_BINDING);
			} else if(userProps.getCount() <= 0) {
				return ResultObject.ERROR(ITEM_NOT_FOUND);
			}
			marketManager.createMarket(marketItem);
			userProps.updateBackpackAndPut2Market();
			dbService.submitUpdate2Queue(userProps);
			propsManager.changeUserPropsBackpack(playerId, DEFAULT_BACKPACK, MARKET_BACKPACK, userProps);
			userBooth.addGoods(marketItem); 
		} catch (Exception e) {
			LOGGER.error("摆摊物品上架: {}", e);
			return ResultObject.ERROR(FAILURE);
		} finally {
			lock.unlock();
		}
		
		int baseId = marketItem.getBaseId();
		ItemType type = marketItem.getType();
		int propsIndex = userProps.getIndex();
		Quality quality = userProps.getQuality();
		String propsName = propsConfig.getName();
		long marketPlayerId = marketItem.getPlayerId();
		shelf.enterShelf(marketPlayerId, type, baseId, propsName);              //商品上架
		page.addNode(marketPlayerId, userDomain.getBattle().getLevel());
		BackpackEntry sourceEntry = BackpackEntry.valueProps(goodsId, baseId, 0, DEFAULT_BACKPACK, quality, propsIndex, userProps.isBinding());
		MessagePushHelper.pushUserProps2Client(playerId, DEFAULT_BACKPACK, false, sourceEntry, userProps);
		
		marketManager.removeMarketCommonCache(playerId, type);					//处理出售者的摆摊缓存
		
		if (userBooth.size() == 1) {                                            // 当摊位有1件物品时, 刷新好友摊位
			FriendHelper.pushFriendsMarketState(marketPlayerId, true);
		}
		
		return ResultObject.SUCCESS(marketItem);
	}
	
	/**
	 * 装备上架
	 * 
	 * @param  playerId       		玩家Id
	 * @param  goodsId          	装备主键
	 * @param  sellSilver        	银币价格
	 * @param  sellGolden         	金币价格
	 * @return {@link ResultObject}	返回值对象
	 */
	
	public ResultObject<MarketItem> putEquip2Market(long playerId, long goodsId, long sellSilver, long sellGolden) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		UserBooth userBooth = marketManager.getUserBooth(playerId, true);
		if(userBooth == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if (userBooth.size() >= MARKET_MAX) {
			return ResultObject.ERROR(MARKET_LIMIT);
		}
		
		int backpack = BackpackType.DEFAULT_BACKPACK;
		UserEquip userEquip = propsManager.getUserEquip(goodsId);
		if (userEquip == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if (userEquip.getPlayerId() != playerId) {
			return ResultObject.ERROR(ITEM_NOT_PLAYER);
		} else if (!userEquip.validBackpack(backpack)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if (userEquip.isBinding()) {
			return ResultObject.ERROR(ITEM_PROPS_BINDING);
		} else if(userEquip.isTrading()) {
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		}
		
		EquipConfig equipConfig = userEquip.getEquipConfig();
		if (equipConfig == null) {
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		
		MarketItem marketItem = MarketItem.equip2MarketItem(playerId, userEquip, sellSilver, sellGolden);
		ChainLock lock = LockUtils.getLock(MARKET_OBJECT_LOCK, userDomain.getPackLock(), userBooth);
		try {
			lock.lock();
			marketManager.createMarket(marketItem);
			userEquip.updateBackpackAndPut2Market();
			dbService.submitUpdate2Queue(userEquip);
			propsManager.changeUserEquipBackpack(playerId, DEFAULT_BACKPACK, MARKET_BACKPACK, userEquip);
			userBooth.addGoods(marketItem); 
		} catch (Exception e) {
			LOGGER.error("{}", e);
			return ResultObject.ERROR(FAILURE);
		} finally {
			lock.unlock();
		}
		
		int baseId = marketItem.getBaseId();
		ItemType type = marketItem.getType();
		int equipIndex = userEquip.getIndex();
		Quality quality = userEquip.getQuality();
		String propsName = equipConfig.getName();
		long marketPlayerId = marketItem.getPlayerId();
		shelf.enterShelf(marketPlayerId, type, baseId, propsName);              // 商品上架
		page.addNode(marketPlayerId, userDomain.getBattle().getLevel());
		BackpackEntry sourceEntry = BackpackEntry.valueEquipEmpty(goodsId, baseId, DEFAULT_BACKPACK, quality, equipIndex, userEquip.isBinding());
		MessagePushHelper.pushUserProps2Client(playerId, DEFAULT_BACKPACK, false, sourceEntry, userEquip);
		
		marketManager.removeMarketCommonCache(playerId, type);					// 处理出售者的摆摊缓存
		
		if (userBooth.size() == 1) {                                            // 当摊位有1件物品时, 刷新好友摊位
			FriendHelper.pushFriendsMarketState(marketPlayerId, true);
		}
		
		return ResultObject.SUCCESS(marketItem);
	}

	
	
	public void onLoginEvent(UserDomain userDomain, int branching) {
		this.shelfDown(userDomain.getPlayerId());
		if (marketManager.isMarket(userDomain.getPlayerId())) {
			this.shelfUp(userDomain.getPlayerId());
		}
	}

	/**
	 * 购买物品
	 * 
	 * @param  playerId      	 	玩家的ID
	 * @param  marketItemId        	购买摆摊物品的ID {@link MarketItem#getId()}
	 * @return {@link Integer}		购买的摊位物品信息
	 */
	
	public int buyMarketItem(long playerId, long marketItemId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND; 
		}
		
		MarketItem marketItem = marketManager.getMarketItem(marketItemId);
		if(marketItem == null) {
			return MARKET_ITEM_NOT_FOUND;
		}
		
		long targetId = marketItem.getPlayerId();
		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(targetDomain == null) {
			return TARGET_NOT_FOUND; 
		} else if(!userManager.isOnline(targetId)) {
			return TARGET_OFF_LINE;
		}
		
		UserBooth targetBooth = marketManager.getUserBooth(targetId, false);
		if(targetBooth == null) {
			return MARKET_ITEM_NOT_FOUND;
		} else if(!targetBooth.contains(marketItemId)) {
			return MARKET_ITEM_NOT_FOUND;
		}
		
		ItemType type = marketItem.getType();
		if(type == null) {
			return FAILURE;
		}
		
		if(type == ItemType.EQUIP) {
			return buyEquipItem(userDomain, targetDomain, targetBooth, marketItem);
		} else if(type == ItemType.PROPS) {
			return buyPropsItem(userDomain, targetDomain, targetBooth, marketItem);
		}
		return FAILURE;
	}

	/**
	 * 购买道具信息
	 * 
	 * @param  buyer				购买者
	 * @param  seller				出售者
	 * @param  targetBooth			出售者的摊位
	 * @param  marketItem			摊位道具信息
	 * @return {@link Integer}		返回值信息
	 */
	@SuppressWarnings("unchecked")
	private int buyPropsItem(UserDomain buyer, UserDomain seller, UserBooth targetBooth, MarketItem marketItem) {
		long buyerId = buyer.getPlayerId();
		long sellerId = seller.getPlayerId();
		long userPropsId = marketItem.getGoodsId();
		UserProps userProps = propsManager.getUserProps(userPropsId);
		if (userProps == null) {
			return ITEM_NOT_FOUND;
		} else if(!userProps.validBackpack(MARKET_BACKPACK)) {
			return NOT_IN_BACKPACK;
		} else if (userProps.getPlayerId() != sellerId) {
			return BELONGS_INVALID;
		} else if(!marketItem.isMarketing()) {
			return MARKET_ITEM_NOT_FOUND;
		}
		
		int count = userProps.getCount();
		int baseId = userProps.getBaseId();
		PropsConfig propsConfig = propsManager.getPropsConfig(baseId);
		if (propsConfig == null) {
			return ITEM_NOT_FOUND;
		}
		
		Player buyPlayer = buyer.getPlayer();
		Player sellPlayer = seller.getPlayer();
		long sellGolden = marketItem.getSellGolden();
		long sellSilver = marketItem.getSellSilver();
		int buyerBackSize = propsManager.getBackpackSize(buyerId, DEFAULT_BACKPACK);
		ChainLock lock = LockUtils.getLock(buyPlayer, sellPlayer, sellPlayer.getPackLock(), marketItem);
		try {
			lock.lock();
			if(!userProps.validBackpack(MARKET_BACKPACK)) {
				return NOT_IN_BACKPACK;
			} else if (userProps.getPlayerId() != sellerId) {
				return BELONGS_INVALID;
			} else if(!marketItem.isMarketing()) {
				return MARKET_ITEM_NOT_FOUND;
			} else if(buyPlayer.getSilver() < sellSilver) {
				return SILVER_NOT_ENOUGH;
			} else if(buyPlayer.getGolden() < sellGolden) {
				return GOLDEN_NOT_ENOUGH;
			}

			if(!buyPlayer.canAddNew2Backpack(buyerBackSize + 1, DEFAULT_BACKPACK)) {
				return BACKPACK_FULLED;
			}
			
			userProps.setPlayerId(buyerId);
			buyPlayer.decreaseGolden(sellGolden);
			buyPlayer.decreaseSilver(sellSilver);
			sellPlayer.increaseGolden(sellGolden);
			sellPlayer.increaseSilver(sellSilver);
			userProps.setBackpack(DEFAULT_BACKPACK);
			marketItem.setState(MarketState.SELLED);
			dbService.updateEntityIntime(buyPlayer, sellPlayer, userProps, marketItem);             //摆摊信息即时入库
			
			targetBooth.removeMarketItem(marketItem.getId());					                    //从摆摊列表中移除
			marketManager.removeMarketCommonCache(sellerId, marketItem.getType());					//处理出售者的摆摊缓存
			propsManager.put2UserPropsIdsList(buyerId, DEFAULT_BACKPACK, userProps);
			propsManager.removeFromUserPropsIdsList(sellerId, MARKET_BACKPACK, userProps);
			
		} finally {
			lock.unlock();
		}
		
		LoggerGoods incomeProps = LoggerGoods.incomePropsByMoney(baseId, count, sellGolden, sellSilver);
		LoggerGoods outcomeProps = LoggerGoods.outcomePropsByMoney(userPropsId, baseId, count, sellGolden, sellSilver);
		if(sellGolden != 0) {
			GoldLogger.inCome(Source.MARKET_SOLD, sellGolden, sellPlayer, outcomeProps);
			GoldLogger.outCome(Source.MARKET_BUYS, sellGolden, buyPlayer, incomeProps);
		}

		if(sellSilver != 0) {
			SilverLogger.inCome(Source.MARKET_SOLD, sellSilver, sellPlayer, outcomeProps);
			SilverLogger.outCome(Source.MARKET_BUYS, sellSilver, buyPlayer, incomeProps);
		}
		
		GoodsLogger.goodsLogger(buyPlayer, Source.MARKET_BUYS, incomeProps);
		GoodsLogger.goodsLogger(sellPlayer, Source.MARKET_SOLD, outcomeProps);
		
		MessagePushHelper.pushUserProps2Client(buyerId, DEFAULT_BACKPACK, false, userProps);
		UserPushHelper.pushAttribute2AreaMember(buyerId, Arrays.asList(buyerId), Arrays.asList(buyer.getUnitId()), SILVER, GOLDEN);
		UserPushHelper.pushAttribute2AreaMember(sellerId, Arrays.asList(sellerId), Arrays.asList(seller.getUnitId()), SILVER, GOLDEN);
		marketHelper.pushMarketSellMessage(buyPlayer.getName(), marketItem);
		shelf.removeGoods(sellerId, ItemType.PROPS, baseId, propsConfig.getName());              // 移除玩家货架物品
		
		achieveFacade.firstAchieved(buyerId, FIRST_ACHIEVE, FIRST_MARKET);
		achieveFacade.firstAchieved(sellerId, FIRST_ACHIEVE, FIRST_MARKET);
		return SUCCESS;
	}
	
	/**
	 * 购买摊位上的道具信息
	 * 
	 * @param  buyer				购买者
	 * @param  seller				出售者
	 * @param  targetBooth			出售者的摊位
	 * @param  marketItem			摊位道具信息
	 * @return {@link Integer}		返回值信息
	 */
	@SuppressWarnings("unchecked")
	private int buyEquipItem(UserDomain buyer, UserDomain seller, UserBooth targetBooth, MarketItem marketItem) {
		long buyerId = buyer.getPlayerId();
		long sellerId = seller.getPlayerId();
		long userEquipId = marketItem.getGoodsId();
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if (userEquip == null) {
			return ITEM_NOT_FOUND;
		} else if(!userEquip.validBackpack(MARKET_BACKPACK)) {
			return NOT_IN_BACKPACK;
		} else if (userEquip.getPlayerId() != sellerId) {
			return BELONGS_INVALID;
		} else if(!marketItem.isMarketing()) {
			return MARKET_ITEM_NOT_FOUND;
		}
		
		int baseId = userEquip.getBaseId();
		EquipConfig equipConfig = propsManager.getEquipConfig(baseId);
		if (equipConfig == null) {
			return ITEM_NOT_FOUND;
		}
		
		Player buyPlayer = buyer.getPlayer();
		Player sellPlayer = seller.getPlayer();
		long sellGolden = marketItem.getSellGolden();
		long sellSilver = marketItem.getSellSilver();
		int buyerBackSize = propsManager.getBackpackSize(buyerId, DEFAULT_BACKPACK);
		ChainLock lock = LockUtils.getLock(buyPlayer, sellPlayer, sellPlayer.getPackLock(), marketItem);
		try {
			lock.lock();
			if(!userEquip.validBackpack(MARKET_BACKPACK)) {
				return NOT_IN_BACKPACK;
			} else if (userEquip.getPlayerId() != sellerId) {
				return BELONGS_INVALID;
			} else if(!marketItem.isMarketing()) {
				return MARKET_ITEM_NOT_FOUND;
			}
			
			if(buyPlayer.getSilver() < sellSilver) {
				return SILVER_NOT_ENOUGH;
			}

			if(buyPlayer.getGolden() < sellGolden) {
				return GOLDEN_NOT_ENOUGH;
			}
			
			if(!buyPlayer.canAddNew2Backpack(buyerBackSize + 1, DEFAULT_BACKPACK)) {
				return BACKPACK_FULLED;
			}
			
			userEquip.setPlayerId(buyerId);
			buyPlayer.decreaseGolden(sellGolden);
			buyPlayer.decreaseSilver(sellSilver);
			sellPlayer.increaseGolden(sellGolden);
			sellPlayer.increaseSilver(sellSilver);
			userEquip.setBackpack(DEFAULT_BACKPACK);
			marketItem.setState(MarketState.SELLED);
			dbService.updateEntityIntime(buyPlayer, sellPlayer, userEquip, marketItem);
			
			targetBooth.removeMarketItem(marketItem.getId());					                    //从摆摊列表中移除
			marketManager.removeMarketCommonCache(sellerId, marketItem.getType());					//处理出售者的摆摊缓存
			propsManager.put2UserEquipIdsList(buyerId, DEFAULT_BACKPACK, userEquip);
			propsManager.removeFromEquipIdsList(sellerId, MARKET_BACKPACK, userEquip);
		} finally {
			lock.unlock();
		}
		

		LoggerGoods incomeEquip = LoggerGoods.incomeEquipByMoney(baseId, 1, sellGolden, sellSilver);
		LoggerGoods outcomeEquip = LoggerGoods.outcomeEquipByMoney(userEquipId, baseId, 1, sellGolden, sellSilver);
		if(sellGolden != 0) {
			GoldLogger.inCome(Source.MARKET_SOLD, sellGolden, sellPlayer, outcomeEquip);
			GoldLogger.outCome(Source.MARKET_BUYS, sellGolden, buyPlayer, incomeEquip);
		}

		if(sellSilver != 0) {
			SilverLogger.inCome(Source.MARKET_SOLD, sellSilver, sellPlayer, outcomeEquip);
			SilverLogger.outCome(Source.MARKET_BUYS, sellSilver, buyPlayer, incomeEquip);
		}
		
		GoodsLogger.goodsLogger(buyPlayer, Source.MARKET_BUYS, incomeEquip);
		GoodsLogger.goodsLogger(sellPlayer, Source.MARKET_SOLD, outcomeEquip);
		
		MessagePushHelper.pushUserProps2Client(buyerId, DEFAULT_BACKPACK, false, userEquip);
		UserPushHelper.pushAttribute2AreaMember(buyerId, Arrays.asList(buyerId), Arrays.asList(buyer.getUnitId()), SILVER, GOLDEN);
		UserPushHelper.pushAttribute2AreaMember(sellerId, Arrays.asList(sellerId), Arrays.asList(seller.getUnitId()), SILVER, GOLDEN);
		marketHelper.pushMarketSellMessage(buyPlayer.getName(), marketItem);
		shelf.removeGoods(sellerId, ItemType.EQUIP, baseId, equipConfig.getName());
		
		achieveFacade.firstAchieved(buyerId, FIRST_ACHIEVE, FIRST_MARKET);
		achieveFacade.firstAchieved(sellerId, FIRST_ACHIEVE, FIRST_MARKET);
		return SUCCESS;
	
	}
	
	
	/**
	 * 摊位物品下架
	 * 
	 * @param  playerId       	玩家的ID
	 * @param  itemId        	 摊位物品ID
	 * @return {@link Integer}	返回值
	 */
	
	public int removeMarketItem(long playerId, long marketItemId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		UserBooth userBooth = marketManager.getUserBooth(playerId, true);
		if(userBooth == null || !userBooth.contains(marketItemId)) {
			return ITEM_NOT_FOUND;
		}
		
		MarketItem marketItem = userBooth.getItemById(marketItemId);
		if (marketItem == null || !marketItem.isMarketing()) {
			return ITEM_NOT_FOUND;
		}
		
		ItemType itemType = marketItem.getType();
		if (itemType == ItemType.PROPS) {
			return removeSellProps(userDomain, userBooth, marketItem);
		} else if (itemType == ItemType.EQUIP) {
			return removeSellEquip(userDomain, userBooth, marketItem);
		}
		return FAILURE;
	}
	
	
	public void updateRank(long playerId, int level) {
		page.update(playerId, level);
	}
	
	/**
	 * 清空销售货架(下架)
	 * 
	 * @param playerId  玩家的ID
	 */
	private void shelfDown(long playerId){
		page.remove(playerId); 						//清理分页
		shelf.clearPlayerShelf(playerId);   		//清理货架
		marketManager.removeMarketCache(playerId);  //清理缓存
	}
	
	/**
	 * 移除出售的道具物品信息
	 * 
	 * @param   userDomain			用户域模型
	 * @param   userBooth			用户摊位信息
	 * @param   marketItem			交易物品信息
	 * @return {@link Integer}		返回值信息
	 */
	private int removeSellProps(UserDomain userDomain, UserBooth userBooth, MarketItem marketItem) {
		long playerId = userDomain.getPlayerId();
		long userPropsId = marketItem.getGoodsId();
		UserProps userProps = propsManager.getUserProps(userPropsId);
		if (userProps == null) {
			return ITEM_NOT_FOUND;
		} else if(!userProps.validBackpack(MARKET_BACKPACK)) {
			return NOT_IN_BACKPACK;
		} else if (userProps.getPlayerId() != playerId) {
			return BELONGS_INVALID;
		} else if(!marketItem.isMarketing()) {
			return MARKET_ITEM_NOT_FOUND;
		}
		
		PropsConfig propsConfig = userProps.getPropsConfig();
		if(propsConfig == null) {
			return ITEM_NOT_FOUND;
		}
		
		long marketItemId = marketItem.getId();
		Player player = userDomain.getPlayer();
		int backSize = propsManager.getBackpackSize(playerId, DEFAULT_BACKPACK);
		ChainLock lock = LockUtils.getLock(player, player.getPackLock(), userBooth, marketItem);
		try {
			lock.lock();
			if(!userProps.validBackpack(MARKET_BACKPACK)) {
				return NOT_IN_BACKPACK;
			} else if (userProps.getPlayerId() != playerId) {
				return BELONGS_INVALID;
			} else if(!marketItem.isMarketing()) {
				return MARKET_ITEM_NOT_FOUND;
			} else if(!userBooth.contains(marketItemId)) {
				return MARKET_ITEM_NOT_FOUND;
			}
			
			if(!player.canAddNew2Backpack(backSize + 1, DEFAULT_BACKPACK)) {
				return BACKPACK_FULLED;
			}
			
			marketItem.setState(MarketState.REMOVE);
			userProps.setBackpack(DEFAULT_BACKPACK);
			userBooth.removeMarketItem(marketItemId);
			dbService.submitUpdate2Queue(userProps);
			dbService.updateEntityIntime(marketItem);           // 摆摊状态改变,　即时入库
			propsManager.changeUserPropsBackpack(playerId, MARKET_BACKPACK, DEFAULT_BACKPACK, userProps);
		} finally {
			lock.unlock();
		}
		
		if (userBooth.isEmpty()) {
			page.remove(playerId);
			FriendHelper.pushFriendsMarketState(playerId, false);
		}
		int baseId = marketItem.getBaseId();
		String propsName = propsConfig.getName();
		shelf.removeGoods(playerId, ItemType.PROPS, baseId, propsName);
		marketManager.removeMarketCommonCache(playerId, marketItem.getType());
		MessagePushHelper.pushUserProps2Client(playerId, DEFAULT_BACKPACK, false, userProps);
		return SUCCESS;
	}
	
	/**
	 * 移除出售的装备物品信息
	 * 
	 * @param  userDomain			用户域模型
	 * @param  userBooth			用户摊位信息
	 * @param  marketItem			交易物品信息
	 * @return {@link Integer}		返回值信息
	 */
	private int removeSellEquip(UserDomain userDomain, UserBooth userBooth, MarketItem marketItem) {
		long playerId = userDomain.getPlayerId();
		long userEquipId = marketItem.getGoodsId();
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if (userEquip == null) {
			return ITEM_NOT_FOUND;
		} else if(!userEquip.validBackpack(MARKET_BACKPACK)) {
			return NOT_IN_BACKPACK;
		} else if (userEquip.getPlayerId() != playerId) {
			return BELONGS_INVALID;
		} else if(!marketItem.isMarketing()) {
			return MARKET_ITEM_NOT_FOUND;
		}
		
		EquipConfig equipConfig = userEquip.getEquipConfig();
		if(equipConfig == null) {
			return EQUIP_NOT_FOUND;
		}
		
		long marketItemId = marketItem.getId();
		Player player = userDomain.getPlayer();
		int backSize = propsManager.getBackpackSize(playerId, DEFAULT_BACKPACK);
		ChainLock lock = LockUtils.getLock(player, player.getPackLock(), userBooth, marketItem);
		try {
			lock.lock();
			if(!userEquip.validBackpack(MARKET_BACKPACK)) {
				return NOT_IN_BACKPACK;
			} else if (userEquip.getPlayerId() != playerId) {
				return BELONGS_INVALID;
			} else if(!marketItem.isMarketing()) {
				return MARKET_ITEM_NOT_FOUND;
			} else if(!userBooth.contains(marketItemId)) {
				return MARKET_ITEM_NOT_FOUND;
			}
			
			if(!player.canAddNew2Backpack(backSize + 1, DEFAULT_BACKPACK)) {
				return BACKPACK_FULLED;
			}
			
			marketItem.setState(MarketState.REMOVE);
			userEquip.setBackpack(DEFAULT_BACKPACK);
			userBooth.removeMarketItem(marketItemId);
			dbService.submitUpdate2Queue(userEquip);
			dbService.updateEntityIntime(marketItem);           // 摆摊状态改变,　即时入库
			propsManager.changeUserEquipBackpack(playerId, MARKET_BACKPACK, DEFAULT_BACKPACK, userEquip);
		} finally {
			lock.unlock();
		}
		
		if (userBooth.isEmpty()) {
			page.remove(playerId);
			FriendHelper.pushFriendsMarketState(playerId, false);
		}
		int baseId = marketItem.getBaseId();
		String equipName = equipConfig.getName();
		shelf.removeGoods(playerId, ItemType.EQUIP, baseId, equipName);
		marketManager.removeMarketCommonCache(playerId, marketItem.getType());
		MessagePushHelper.pushUserProps2Client(playerId, DEFAULT_BACKPACK, false, userEquip);
		return SUCCESS;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	

	
	/**
	 * 摊位货品上架
	 * 
	 * @param booth     玩家的摊位
	 */
	private void shelfUp(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return;
		}
		
		UserBooth booth = marketManager.getUserBooth(playerId, true);
		if (booth == null) {
			return;
		}
		
		for(MarketItem goods : booth.getGoodslist()) {
			int baseId = goods.getBaseId();
			ItemType type = goods.getType();
			if (type == ItemType.PROPS) {
				PropsConfig config = propsManager.getPropsConfig(baseId);
				if (config != null) {
					shelf.enterShelf(playerId, type, baseId, config.getName());
				}
			} else if (type == ItemType.EQUIP) {
				EquipConfig config = propsManager.getEquipConfig(baseId);
				if (config != null) {
					shelf.enterShelf(playerId, type, baseId, config.getName());
				}
			}
		}
		
		PlayerBattle battle = userDomain.getBattle();
		page.addNode(playerId, battle.getLevel());
	}
	

	/**
	 * 摆摊分类查看
	 * 
	 * @param  itemType       		物品类型
	 * @return {@link Collection}	用户摆摊信息返回值
	 */
	
	public Collection<UserBoothVO> loadMarketByItemType(int itemType) {
		ItemType type = EnumUtils.getEnum(ItemType.class, itemType);
		if (type == null) {
			return Collections.emptyList();
		}
		
		if (type == ItemType.PROPS) {
			Set<Long> marketOwnerIds = marketManager.listMarketOwnerIds(type);
			return getUserBoothVO(marketOwnerIds);
		} else if (type == ItemType.EQUIP) {
			Set<Long> marketOwnerIds = marketManager.listMarketOwnerIds(type);
			return getUserBoothVO(marketOwnerIds);
		} else {
			Collection<Long> marketOwnerIds = page.getPlayerId4Sort();
			return getUserBoothVO(marketOwnerIds); 
		}
	}
	
	
	/**
	 * 玩家名字Subkey
	 * 
	 * @param  playerName		角色名
	 * @return {@link String}	SubKey
	 */
	private String getSellerSubKey(String playerName) {
		return new StringBuilder().append(HASH_KEY).append(PLAYER_NAME).append(playerName == null ? "" : playerName).toString();
	}
	
	@SuppressWarnings("unchecked")
	private List<Long> searchByPlayerNameIdList(String keywords) {
		String subKey = getSellerSubKey(keywords);
		List<Long> playerIds = (List<Long>)cachedService.getFromCommonCache(HASH_KEY, subKey); 
		if(playerIds != null) {
			return playerIds;
		}
		
		playerIds = new ArrayList<Long>();
		Set<Long> marketPlayerIds = marketManager.getAllMarketPlayerIds();
		for (Long playerId : marketPlayerIds) {
			if (!userManager.isOnline(playerId)) {
				continue;
			}
			
			UserDomain userDomain = userManager.getUserDomain(playerId);
			if(userDomain == null) {
				continue;
			}
			
			Player player = userDomain.getPlayer();
			String playerName = player.getName();
			if (!playerName.contains(keywords)) {
				continue;
			}
			
			UserBooth userBooth = marketManager.getUserBooth(playerId, false);
			if (userBooth == null || userBooth.isEmpty()) {
				continue;
			}
			
			if(!playerIds.contains(playerId)) {
				playerIds.add(playerId);
			}
		}
		cachedService.put2CommonHashCache(HASH_KEY, subKey, playerIds, SEARCH_CACHE);
		return playerIds;
	}
	
	/**
	 * 通过道具名查询玩家信息
	 * 
	 * @param keywords
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Set<Long> searchPropsNamePlayerIds(String keywords) {
		String subKey = getPropsNameSubKey(keywords);
		Set<Long> playerIds = (Set<Long>) cachedService.getFromCommonCache(HASH_KEY, subKey);
		if (playerIds != null) {
			return playerIds;
		}
		
		playerIds = new HashSet<Long>();
		for (Sell sell : shelf.getAllSells()) {
			String propsName = sell.getGoodsName();
			if (!propsName.contains(keywords)) {
				continue;
			}
			
			for (Long playerId : shelf.getSellPlayers(sell)) {
				playerIds.add(playerId);
			}
			cachedService.put2CommonHashCache(HASH_KEY, subKey, playerIds, SEARCH_CACHE);
		}
		
		return playerIds;
	}
	
	/**
	 * 摆摊搜索
	 * 
	 * @param  keywords       		关键字
	 * @param  type           		搜索分类
	 * @return {@link Collection}	返回值
	 */
	
	public Collection<UserBoothVO> searchPlayerBooth(String keywords, int type) {
		Collection<Long> playerIds = null;
		if (!StringUtils.isBlank(keywords)) {
			if (type == SearchType.PLAYER_NAME) {
				playerIds = searchByPlayerNameIdList(keywords);
			} else if (type == SearchType.PROPS_NAME) {
				playerIds = searchPropsNamePlayerIds(keywords);
			}
		}
		return getUserBoothVO(playerIds);
	}

	/**
	 * 查询用户摆摊VO信息
	 * 
	 * @param playerIds		角色ID列表
	 * @return
	 */
	private Collection<UserBoothVO> getUserBoothVO(Collection<Long> playerIds) {
		if(playerIds == null || playerIds.isEmpty()) {
			return Collections.emptyList();
		}
		
		Collection<UserBoothVO> userBooths = new ArrayList<UserBoothVO>();
		for (Long playerId : playerIds) {
			if(!userManager.isOnline(playerId)) {
				continue;
			}
			
			UserBooth userBooth = marketManager.getUserBooth(playerId, false);
			if(userBooth != null && !userBooth.isEmpty()) {
				String name  = userBooth.getName();
				String bName = userBooth.getBoothName();
				userBooths.add(UserBoothVO.valueOf(playerId, name, bName));
			}
		}
		return userBooths;
	}
	
	/**
	 * 物品名称Subkey
	 * 
	 * @param propsName
	 * @return
	 */
	private String getPropsNameSubKey(String propsName) {
		return new StringBuilder().append(HASH_KEY).append(PROPS_NAME).append(propsName == null ? "" : propsName).toString();
	}

	
	/**
	 * 修改玩家摊位名字
	 * 
	 * @param playerId              玩家ID
	 * @param keywords              关键字
	 * @return {@link CommonConstant}
	 */
	
	public int modifyBoothName(long playerId, String keywords) {
		UserDomain domain = userManager.getUserDomain(playerId);
		if (domain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		UserBooth userBooth = marketManager.getUserBooth(playerId, false);
		if (!userBooth.isEmpty() && !StringUtils.isBlank(keywords)) {
			userBooth.setBoothName(keywords);
			return SUCCESS;
		}
		
		return FAILURE;
	}
}
