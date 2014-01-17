package com.yayo.warriors.module.shop.facade.impl;

import static com.yayo.warriors.module.pack.type.BackpackType.*;
import static com.yayo.warriors.module.shop.constant.ShopConstant.*;
import static com.yayo.warriors.module.vip.model.VipFunction.*;
import static com.yayo.warriors.module.achieve.model.AchieveType.FIRST_ACHIEVE;
import static com.yayo.warriors.module.achieve.model.FirstType.FIRST_MALL_BUY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.DatePattern;
import com.yayo.common.utility.DateUtil;
import com.yayo.common.utility.EnumUtils;
import com.yayo.warriors.basedb.model.EquipConfig;
import com.yayo.warriors.basedb.model.MallActiveConfig;
import com.yayo.warriors.basedb.model.MallConfig;
import com.yayo.warriors.basedb.model.MallSpecialConfig;
import com.yayo.warriors.basedb.model.NpcConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.basedb.model.ShopConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.achieve.facade.AchieveFacade;
import com.yayo.warriors.module.logger.log.CouponLogger;
import com.yayo.warriors.module.logger.log.GoldLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.log.SilverLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.PropsStackResult;
import com.yayo.warriors.module.props.rule.EquipHelper;
import com.yayo.warriors.module.props.rule.PropsHelper;
import com.yayo.warriors.module.shop.entity.LaveMallProp;
import com.yayo.warriors.module.shop.entity.PlayerBuyLimit;
import com.yayo.warriors.module.shop.facade.ShopFacade;
import com.yayo.warriors.module.shop.manager.ShopManager;
import com.yayo.warriors.module.shop.model.MallPrice;
import com.yayo.warriors.module.shop.model.MallProps;
import com.yayo.warriors.module.shop.rule.ShopRule;
import com.yayo.warriors.module.shop.type.MallType;
import com.yayo.warriors.module.shop.type.MallType.PriceChecker;
import com.yayo.warriors.module.task.facade.TaskFacade;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.entity.PlayerMotion;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.vip.manager.VipManager;
import com.yayo.warriors.module.vip.model.VipDomain;
import com.yayo.warriors.type.Currency;
import com.yayo.warriors.type.GoodsType;
import com.yayo.warriors.util.GameConfig;

@Component
public class ShopFacadeImpl implements ShopFacade {

	@Autowired
	private DbService dbService;
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private TaskFacade taskFacade;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private VipManager vipManager;
	@Autowired
	private ShopManager shopManager;
	@Autowired
	private AchieveFacade achieveFacade;
	@Autowired
	private ResourceService resourceService ;
	
	
	public ResultObject<List<BackpackEntry>> buyPropsByShop(long playerId, int shopId, int count, int npcId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		if (count <= 0 || count > ShopRule.getMaxCount()) {
			return ResultObject.ERROR(BUY_COUNT_INVALID);
		}

		ShopConfig shopConfig = shopManager.getShopConfig(shopId);
		if (shopConfig == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		}

		int propsType = shopConfig.getGoodsType();

		VipDomain vipDomain = vipManager.getVip(playerId);
		if (vipDomain.isVip() && vipDomain.booleanValue(OpenRemoteShop)) {
			return propsType == GoodsType.EQUIP  
				  ? buyShopEquip(player, shopConfig, count)
				  : buyShopProps(player, shopConfig, count);
		}
		
		if(!shopConfig.getNpcIdList().contains(npcId)){
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		NpcConfig npcConfig = resourceService.get(npcId, NpcConfig.class);
		if(userDomain.getMapId() != npcConfig.getMapId()){
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		PlayerMotion motion = userDomain.getMotion();
		if (motion == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}

		if (propsType == GoodsType.PROPS) {
			return buyShopProps(player, shopConfig, count);
		} else {
			return buyShopEquip(player, shopConfig, count);
		}
	}

	
	public ResultObject<List<BackpackEntry>> buyPropsByMall(long playerId, int mallId, int count) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}

		MallConfig mallConfig = shopManager.getMallConfig(mallId);
		if (mallConfig == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if (count <= 0) {
			return ResultObject.ERROR(BUY_COUNT_INVALID);
		}
		
		if (mallConfig.getGoodsType() == GoodsType.PROPS) {
			return buyMallUnlimitProps(userDomain, mallConfig, count);
		} else {
			return buyMallUnlimitEquip(userDomain, mallConfig, count);
		}
		
	}

	public ResultObject<List<BackpackEntry>> buySpecialMallProps(long playerId, int mallId, int count) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}

		MallSpecialConfig specialConfig = shopManager.getMallSpecialConfig(mallId);
		if (specialConfig == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if (count <= 0) {
			return ResultObject.ERROR(BUY_COUNT_INVALID);
		}
		
		if (specialConfig.getGoodsType() == GoodsType.PROPS) {
			return buyMallLimitProps(userDomain, mallId, count);
		} else {
			return buyMallLimitEquip(userDomain, mallId, count);
		}
	}
	
	@SuppressWarnings("unchecked")
	private ResultObject<List<BackpackEntry>> buyMallLimitProps(UserDomain userDomain, int mallId, int count)  {
		PlayerBuyLimit playerLimit = shopManager.getPlayerBuyLimit(userDomain.getId());
		if (playerLimit == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		MallSpecialConfig specialConfig = shopManager.getMallSpecialConfig(mallId);
		if(specialConfig == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(specialConfig.getBuyCountLimit() < count) {
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		} else if (battle.getLevel() < specialConfig.getBuyLevel()) {
			return ResultObject.ERROR(LEVEL_INVALID);
		}
		
		MallActiveConfig activeConfig = shopManager.getEffectActiveConfig();
		if (activeConfig.getId() != specialConfig.getActiveId()) {
			return ResultObject.ERROR(ACTIVE_NOT_OPEN);
		}
		
		LaveMallProp laveMallProp = shopManager.getLaveMallProp(mallId);
		if(laveMallProp == null) {
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		} else if (laveMallProp.getCount() + count > specialConfig.getTotalCount()) {
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		}
		
		long playerId = userDomain.getPlayerId();
		Player player = userDomain.getPlayer();

		int propsId = specialConfig.getPropsId();
		PropsConfig propsConfig = propsManager.getPropsConfig(propsId);
		if (propsConfig == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		}

		int goldenPrice = specialConfig.getSpecialPrice();

		int backpack = DEFAULT_BACKPACK;
		int costPrice = goldenPrice * count;
		PropsStackResult propsStack = PropsHelper.calcPropsStack(playerId, backpack, propsId, count, true);  
		Map<Long, Integer> mergeProps = propsStack.getMergeProps();
		List<UserProps> newUserProps = propsStack.getNewUserProps();
		int currBackSize = propsManager.getBackpackSize(playerId, backpack);
		ChainLock lock = LockUtils.getLock(player, player.getPackLock(), laveMallProp, playerLimit);
		try {
			lock.lock();
			if (!player.canAddNew2Backpack(currBackSize + newUserProps.size(), backpack)) {
				return ResultObject.ERROR(BACKPACK_FULLED);
			}

			if (battle.getLevel() < specialConfig.getBuyLevel()) {
				return ResultObject.ERROR(LEVEL_INVALID);
			}
			 
			if (player.getGolden() < costPrice) {
				return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
			}
			
			if (laveMallProp.getCount() + count > specialConfig.getTotalCount()) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			}
			
			if (playerLimit.getCountById(mallId) + count > specialConfig.getBuyCountLimit()) {
				return ResultObject.ERROR(BUY_COUNT_LIMIT);
			}
			
			if(!newUserProps.isEmpty()) {
				propsManager.createUserProps(newUserProps);
				propsManager.put2UserPropsIdsList(playerId, backpack, newUserProps);
			}
			
			laveMallProp.addCount(count);
			player.decreaseGolden(costPrice);
			playerLimit.put2BuyCountMap(mallId, count);
			playerLimit.updatePlayerGoodsCount();
		} catch (Exception e) {
			return ResultObject.ERROR(FAILURE);
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(player, laveMallProp, playerLimit);
		List<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		if(!newUserProps.isEmpty()) {
			backpackEntries.addAll(voFactory.getUserPropsEntries(newUserProps));
		}

		if(!mergeProps.isEmpty()) {
			List<UserProps> updatePropsList = propsManager.updateUserPropsList(mergeProps);
			backpackEntries.addAll(voFactory.getUserPropsEntries(updatePropsList));
		}
		
		LoggerGoods loggerGoods = LoggerGoods.incomePropsByGolden(propsId, count, costPrice);
		GoodsLogger.goodsLogger(player, Source.BUY_MALL_OFFER, loggerGoods);
		if(goldenPrice > 0) {
			GoldLogger.outCome(Source.BUY_MALL_OFFER, costPrice, player, loggerGoods);
		}
		
		return ResultObject.SUCCESS(backpackEntries);
	}

	
	@SuppressWarnings("unchecked")
	private ResultObject<List<BackpackEntry>> buyMallLimitEquip(UserDomain userDomain, int mallId, int count) {
		PlayerBuyLimit playerLimit = shopManager.getPlayerBuyLimit(userDomain.getId());
		if (playerLimit == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		MallSpecialConfig specialConfig = shopManager.getMallSpecialConfig(mallId);
		if(specialConfig == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(specialConfig.getBuyCountLimit() < count) {
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		} else if (battle.getLevel() < specialConfig.getBuyLevel()) {
			return ResultObject.ERROR(LEVEL_INVALID);
		}
		
		MallActiveConfig activeConfig = shopManager.getEffectActiveConfig();
		if (activeConfig.getId() != specialConfig.getActiveId()) {
			return ResultObject.ERROR(ACTIVE_NOT_OPEN);
		}
		
		LaveMallProp laveMallProp = shopManager.getLaveMallProp(mallId);
		if(laveMallProp == null) {
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		} else if (laveMallProp.getCount() + count > specialConfig.getTotalCount()) {
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		}
		
		long playerId = userDomain.getPlayerId();
		Player player = userDomain.getPlayer();

		int equipId = specialConfig.getPropsId();
		EquipConfig equipConfig = propsManager.getEquipConfig(equipId);
		if (equipConfig == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		}

		int goldenPrice = specialConfig.getSpecialPrice();
		
		int backpack = DEFAULT_BACKPACK;
		int costPrice = goldenPrice * count;
		List<UserEquip> equipList = EquipHelper.newUserEquips(playerId, backpack, equipId, true, count);
		int currBackSize = propsManager.getBackpackSize(playerId, backpack);
		
		ChainLock lock = LockUtils.getLock(player, player.getPackLock(), laveMallProp, playerLimit);
		try {
			lock.lock();
			if (!player.canAddNew2Backpack(currBackSize + equipList.size(), backpack)) {
				return ResultObject.ERROR(BACKPACK_FULLED);
			}
			
			if (battle.getLevel() < specialConfig.getBuyLevel()) {
				return ResultObject.ERROR(LEVEL_INVALID);
			}
			 
			if (player.getGolden() < costPrice) {
				return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
			}
			
			if (laveMallProp.getCount() + count > specialConfig.getTotalCount()) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			}
			
			if (playerLimit.getCountById(mallId) + count > specialConfig.getBuyCountLimit()) {
				return ResultObject.ERROR(BUY_COUNT_LIMIT);
			}
			
			if(!equipList.isEmpty()) {
				propsManager.createUserEquip(equipList);
				propsManager.put2UserEquipIdsList(playerId, backpack, equipList);
			}
			
			laveMallProp.addCount(count);
			player.decreaseGolden(costPrice);
			playerLimit.put2BuyCountMap(mallId, count);
			playerLimit.updatePlayerGoodsCount();
			dbService.submitUpdate2Queue(player, laveMallProp, playerLimit);
		} catch (Exception ex) { 
			return ResultObject.ERROR(FAILURE);
	    } finally {
			lock.unlock();
		}
		
		List<BackpackEntry> entries = voFactory.getUserEquipEntries(equipList);
		
		LoggerGoods goodsInfos = LoggerGoods.incomeEquipByGolden(equipId, count, costPrice);
		if (costPrice > 0) {
			GoldLogger.outCome(Source.BUY_MALL_OFFER, costPrice, player, goodsInfos);
		}
		
		GoodsLogger.goodsLogger(player, Source.BUY_MALL_OFFER, goodsInfos);
		
		return ResultObject.SUCCESS(entries);
	}
	
	
	private ResultObject<List<BackpackEntry>> buyMallUnlimitProps(UserDomain userDomain, MallConfig mallConfig, int count) {
		int propsId = mallConfig.getPropsId();
		PropsConfig propsConfig = propsManager.getPropsConfig(propsId);
		if (propsConfig == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		}

		int propsMallType = mallConfig.getMallType();
		MallType mallType = EnumUtils.getEnum(MallType.class, propsMallType);
		if(mallType == null) {
			return ResultObject.ERROR(TYPE_INVALID);
		}
		
		long costGoldenPrice = 0L;
		long costCouponPrice = 0L;
		int backpack = DEFAULT_BACKPACK;
		Player player = userDomain.getPlayer();
		long playerId = userDomain.getPlayerId();
		VipDomain vipDomain = vipManager.getVip(playerId);				
		PriceChecker priceChecker = mallType.getPriceChecker();
		MallPrice mallPrice = priceChecker.calcPrice(vipDomain, propsConfig, count);
		PropsStackResult propsStack = PropsHelper.calcPropsStack(playerId, backpack, propsId, count, mallPrice.isBinding());
		Map<Long, Integer> mergeProps = propsStack.getMergeProps();
		List<UserProps> newUserProps = propsStack.getNewUserProps();
		int currBackSize = propsManager.getBackpackSize(playerId, backpack);
		List<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		ChainLock lock = LockUtils.getLock(player, player.getPackLock());
		try {
			lock.lock();
			if (!player.canAddNew2Backpack(currBackSize + newUserProps.size(), backpack)) {
				return ResultObject.ERROR(BACKPACK_FULLED);
			}
			
			if(mallPrice.getMallPrice() <= 0) {
				return ResultObject.ERROR(FAILURE);
			}
			if(mallPrice.getCurrency() == null) {
				return ResultObject.ERROR(TYPE_INVALID);
			} else if(mallPrice.getCurrency() == Currency.GOLDEN) {
				if (player.getGolden() < mallPrice.getMallPrice()) {
					return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
				}
				costGoldenPrice = mallPrice.getMallPrice();
			} else if(mallPrice.getCurrency() == Currency.COUPON) {
				if (player.getCoupon() < mallPrice.getMallPrice()) {
					return ResultObject.ERROR(COUPON_NOT_ENOUGH);
				}
				costCouponPrice = mallPrice.getMallPrice();
			} else {
				return ResultObject.ERROR(TYPE_INVALID);
			}
			
			if(!newUserProps.isEmpty()) {
				propsManager.createUserProps(newUserProps);
				propsManager.put2UserPropsIdsList(playerId, backpack, newUserProps);
			}
			
			player.decreaseGolden(costGoldenPrice);
			player.decreaseCoupon(costCouponPrice);
			if(!mergeProps.isEmpty()) {
				List<UserProps> updatePropsList = propsManager.updateUserPropsList(mergeProps);
				backpackEntries.addAll(voFactory.getUserPropsEntries(updatePropsList));
			}
		} catch (Exception e) {
			return ResultObject.ERROR(FAILURE);
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(player);
		if(!newUserProps.isEmpty()) {
			backpackEntries.addAll(voFactory.getUserPropsEntries(newUserProps));
		}
		
		taskFacade.updateBuyMallItemTask(playerId, propsId, count);
		
		LoggerGoods incomeProps = null;
		if(mallPrice.getCurrency() == Currency.GOLDEN) {
			incomeProps = LoggerGoods.incomePropsByCoupon(propsId, count, costCouponPrice);
		} else if(mallPrice.getCurrency() == Currency.GOLDEN) {
			incomeProps = LoggerGoods.incomePropsByGolden(propsId, count, costGoldenPrice);
		}
		 
		if(costGoldenPrice != 0) {
			GoldLogger.outCome(Source.BUY_MALL_PROPS, costGoldenPrice, player, incomeProps);
		}
		if(costCouponPrice != 0) {
			CouponLogger.outCome(Source.BUY_MALL_PROPS, costCouponPrice, player, incomeProps);
		}
		
		GoodsLogger.goodsLogger(player, Source.BUY_MALL_PROPS, incomeProps);
		
		achieveFacade.firstAchieved(playerId, FIRST_ACHIEVE, FIRST_MALL_BUY);      
		return ResultObject.SUCCESS(backpackEntries);
	}
	
	private ResultObject<List<BackpackEntry>> buyMallUnlimitEquip(UserDomain userDomain, MallConfig mallConfig, int count) {
		int equipId = mallConfig.getPropsId();
		EquipConfig equipConfig = propsManager.getEquipConfig(equipId);
		if (equipConfig == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		}

		int propsMallType = mallConfig.getMallType();
		MallType mallType = EnumUtils.getEnum(MallType.class, propsMallType);
		if(mallType == null) {
			return ResultObject.ERROR(TYPE_INVALID);
		}
		
		long costGoldenPrice = 0L;
		long costCouponPrice = 0L;
		int backpack = DEFAULT_BACKPACK;
		Player player = userDomain.getPlayer();
		long playerId = userDomain.getPlayerId();
		VipDomain vipDomain = vipManager.getVip(playerId);				
		PriceChecker priceChecker = mallType.getPriceChecker();
		MallPrice mallPrice = priceChecker.calcPrice(vipDomain, equipConfig, count);
		List<UserEquip> equipList = EquipHelper.newUserEquips(playerId, backpack, equipId, false, count);
		if (equipList.size() != count) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int currBackSize = propsManager.getBackpackSize(playerId, backpack);
		ChainLock lock = LockUtils.getLock(player, player.getPackLock());
		try {
			lock.lock();
			if(mallPrice.getMallPrice() <= 0) {
				return ResultObject.ERROR(FAILURE);
			}
			
			if(mallPrice.getCurrency() == null) {
				return ResultObject.ERROR(TYPE_INVALID);
			} else if(mallPrice.getCurrency() == Currency.GOLDEN) {
				if (player.getGolden() < mallPrice.getMallPrice()) {
					return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
				}
				costGoldenPrice = mallPrice.getMallPrice();
			} else if(mallPrice.getCurrency() == Currency.COUPON) {
				if (player.getCoupon() < mallPrice.getMallPrice()) {
					return ResultObject.ERROR(COUPON_NOT_ENOUGH);
				}
				costCouponPrice = mallPrice.getMallPrice();
			} else {
				return ResultObject.ERROR(TYPE_INVALID);
			}
			
			if (!player.canAddNew2Backpack(currBackSize + count, backpack)) {
				return ResultObject.ERROR(BACKPACK_FULLED);
			}
			
			player.decreaseGolden(costGoldenPrice);
			player.decreaseCoupon(costCouponPrice);
			propsManager.createUserEquip(equipList);
			propsManager.put2UserEquipIdsList(playerId, backpack, equipList);
		} finally {
			lock.unlock();
		}
		
		List<BackpackEntry> entries = voFactory.getUserEquipEntries(equipList);
		
		LoggerGoods incomeEquip = null;
		if(mallPrice.getCurrency() == Currency.GOLDEN) {
			incomeEquip = LoggerGoods.incomeEquipByCoupon(equipId, count, costCouponPrice);
		} else if(mallPrice.getCurrency() == Currency.GOLDEN) {
			incomeEquip = LoggerGoods.incomeEquipByGolden(equipId, count, costGoldenPrice);
		}
		 
		if(costGoldenPrice != 0) {
			GoldLogger.outCome(Source.BUY_MALL_PROPS, costGoldenPrice, player, incomeEquip);
		}
		if(costCouponPrice != 0) {
			CouponLogger.outCome(Source.BUY_MALL_PROPS, costGoldenPrice, player, incomeEquip);
		}
		
		GoodsLogger.goodsLogger(player, Source.BUY_MALL_PROPS, incomeEquip);
		achieveFacade.firstAchieved(playerId, FIRST_ACHIEVE, FIRST_MALL_BUY);      
		return ResultObject.SUCCESS(entries);
	}
	
	

	private ResultObject<List<BackpackEntry>> buyShopProps(Player player, ShopConfig shopConfig, int count) {
		int shopConfigId = shopConfig.getId();
		int propsId = shopConfig.getPropsId();
		PropsConfig propsConfig = propsManager.getPropsConfig(propsId);
		if (propsConfig == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		}

		long playerId = player.getId();
		int backpack = DEFAULT_BACKPACK;
		boolean binding = shopConfig.isBinding();
		int totalCostSilver = propsConfig.getShopSilverPrice(count);
		PropsStackResult propsStack = PropsHelper.calcPropsStack(playerId, backpack, propsId, count, binding);
		int currBackSize = propsManager.getBackpackSize(playerId, backpack);
		Map<Long, Integer> mergeProps = propsStack.getMergeProps();
		List<UserProps> newUserProps = propsStack.getNewUserProps();
		ChainLock lock = LockUtils.getLock(player, player.getPackLock());
		try {
			lock.lock();
			if (player.getSilver() < totalCostSilver) {
				return ResultObject.ERROR(SILVER_NOT_ENOUGH);
			}

			if(!newUserProps.isEmpty()) {
				if (!player.canAddNew2Backpack(currBackSize + newUserProps.size(), backpack)) {
					return ResultObject.ERROR(BACKPACK_FULLED);
				}
				propsManager.createUserProps(newUserProps);
				propsManager.put2UserPropsIdsList(playerId, backpack, newUserProps);
			}
			
			player.decreaseSilver(totalCostSilver);
		} catch (Exception e) {
			return ResultObject.ERROR(FAILURE);
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(player);
		List<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		if(!newUserProps.isEmpty()) {
			backpackEntries.addAll(voFactory.getUserPropsEntries(newUserProps));
		}
		
		if(!mergeProps.isEmpty()) {
			List<UserProps> mergePropsList = propsManager.updateUserPropsList(mergeProps);
			backpackEntries.addAll(voFactory.getUserPropsEntries(mergePropsList));
		}
		
		LoggerGoods incomeProps = LoggerGoods.incomePropsBySilver(propsId, count, totalCostSilver);
		SilverLogger.outCome(Source.BUY_SHOP_PROPS, totalCostSilver, player, incomeProps);
		GoodsLogger.goodsLogger(player, Source.BUY_SHOP_PROPS, incomeProps);
		return ResultObject.SUCCESS(backpackEntries);
	}


	private ResultObject<List<BackpackEntry>> buyShopEquip(Player player, ShopConfig shopConfig, int count) {
		int shopConfigId = shopConfig.getId();
		int propsId = shopConfig.getPropsId();
		EquipConfig equipConfig = propsManager.getEquipConfig(propsId);
		if (equipConfig == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		}

		long playerId = player.getId();
		int backpack = DEFAULT_BACKPACK;
		boolean binding = shopConfig.isBinding();
		List<UserEquip> equipList = EquipHelper.newUserEquips(playerId, backpack, propsId, binding, count);
		if (equipList.size() != count) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int totalCostSilver = equipConfig.getShopSilverPrice(count);
		int currBackSize = propsManager.getBackpackSize(playerId, backpack);
		ChainLock lock = LockUtils.getLock(player, player.getPackLock());
		try {
			lock.lock();
			int needSize = equipList.size();
			if (!player.canAddNew2Backpack(currBackSize + needSize, backpack)) {
				return ResultObject.ERROR(BACKPACK_FULLED);
			} else if (player.getSilver() < totalCostSilver) {
				return ResultObject.ERROR(SILVER_NOT_ENOUGH);
			}
			
			propsManager.createUserEquip(equipList);
			propsManager.put2UserEquipIdsList(playerId, backpack, equipList);
			player.decreaseSilver(totalCostSilver);
		} catch (Exception e) {
			return ResultObject.ERROR(FAILURE);
		} finally {
			lock.unlock();
		}
		
		List<BackpackEntry> backpackEntries = voFactory.getUserEquipEntries(equipList);
		LoggerGoods incomeEquip = LoggerGoods.incomeEquipBySilver(propsId, count, totalCostSilver);
		GoodsLogger.goodsLogger(player, Source.BUY_SHOP_PROPS, incomeEquip);
		if(totalCostSilver != 0) {
			SilverLogger.outCome(Source.BUY_SHOP_PROPS, totalCostSilver, player, incomeEquip);
		}
		return ResultObject.SUCCESS(backpackEntries);
	}

	
	public int findNpcByEquipOrProps(int type , int id){
		Collection<ShopConfig> shopConfigList = shopManager.getAllShopConfig();
		for(ShopConfig shopConfig : shopConfigList){
			if(shopConfig.getGoodsType() == type && id == shopConfig.getPropsId()){
				return Integer.parseInt(shopConfig.getNpcIds());
			}
		}
		return -1 ;
	}


	public List<MallProps> listMallSpecialOffer(long playerId) {
		PlayerBuyLimit limit = shopManager.getPlayerBuyLimit(playerId);
		if (limit == null) {
			return Collections.emptyList();
		}
		
		MallActiveConfig config = shopManager.getEffectActiveConfig();
		if (config == null) {
			return Collections.emptyList();
		}
		
		int lastDay = config.getLastDay();
		String openTime = GameConfig.getFirstServerOpenTime();
	    Date openDate = DateUtil.string2Date(openTime, DatePattern.PATTERN_YYYY_MM_DD);
	    Date endDate = DateUtil.changeDateTime(openDate, lastDay, 0, 0, 0);
	    
		List<MallSpecialConfig> specialConfigs = shopManager.getSpecialMallConfigs(config.getId());
		
		List<MallProps> mallPropsList = new ArrayList<MallProps>();
		for (MallSpecialConfig specialConfig : specialConfigs) {
			int count = limit.getCountById(specialConfig.getId());
			LaveMallProp prop = shopManager.getLaveMallProp(specialConfig.getId());
			if (prop == null) {
				continue;
			}
			MallProps mallProps = MallProps.valueOf(specialConfig.getId(), endDate.getTime(), count, prop.getCount()); 
			mallPropsList.add(mallProps);
		}
		return mallPropsList;
	}
}
