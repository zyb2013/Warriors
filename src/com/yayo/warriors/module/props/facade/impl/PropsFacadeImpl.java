package com.yayo.warriors.module.props.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.achieve.model.AchieveType.*;
import static com.yayo.warriors.module.achieve.model.FirstType.*;
import static com.yayo.warriors.module.pack.type.BackpackType.*;
import static com.yayo.warriors.module.props.constant.PropsConstant.*;
import static com.yayo.warriors.module.user.type.AttributeKeys.*;
import static com.yayo.warriors.type.FormulaKey.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.DateUtil;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.adapter.EquipService;
import com.yayo.warriors.basedb.adapter.ShenwuService;
import com.yayo.warriors.basedb.model.BulletinConfig;
import com.yayo.warriors.basedb.model.CoolTimeConfig;
import com.yayo.warriors.basedb.model.EquipBreakConfig;
import com.yayo.warriors.basedb.model.EquipConfig;
import com.yayo.warriors.basedb.model.EquipRankConfig;
import com.yayo.warriors.basedb.model.EquipStarConfig;
import com.yayo.warriors.basedb.model.MallConfig;
import com.yayo.warriors.basedb.model.PropsArtificeConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.basedb.model.PropsSynthConfig;
import com.yayo.warriors.basedb.model.ShenwuAttributeConfig;
import com.yayo.warriors.basedb.model.ShenwuConfig;
import com.yayo.warriors.basedb.model.WashAttributeConfig;
import com.yayo.warriors.basedb.model.WashRuleConfig;
import com.yayo.warriors.basedb.model.WashTypeConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.EquipPushHelper;
import com.yayo.warriors.common.helper.FormulaHelper;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.NoticePushHelper;
import com.yayo.warriors.common.helper.PetPushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.achieve.facade.AchieveFacade;
import com.yayo.warriors.module.buffer.entity.UserBuffer;
import com.yayo.warriors.module.cooltime.entity.UserCoolTime;
import com.yayo.warriors.module.cooltime.manager.CoolTimeManager;
import com.yayo.warriors.module.fight.helper.BufferHelper;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.logger.helper.LoggerPropsHelper;
import com.yayo.warriors.module.logger.log.GoldLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.log.GoodsMoveLogger;
import com.yayo.warriors.module.logger.log.SilverLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Orient;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.map.types.ItemLimitTypes;
import com.yayo.warriors.module.notice.rule.NoticeRule;
import com.yayo.warriors.module.notice.type.NoticeID;
import com.yayo.warriors.module.notice.type.NoticeType;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.pet.rule.PetAttributeRule;
import com.yayo.warriors.module.props.constant.PropsConstant;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.facade.PropsFacade;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.ArtificeVO;
import com.yayo.warriors.module.props.model.AttributeVO;
import com.yayo.warriors.module.props.model.EndureInfo;
import com.yayo.warriors.module.props.model.EquipEnchangeInfo;
import com.yayo.warriors.module.props.model.GoodsPosition;
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.module.props.model.HoleInfo;
import com.yayo.warriors.module.props.model.PropsStackResult;
import com.yayo.warriors.module.props.model.ResolveVO;
import com.yayo.warriors.module.props.model.ShenwuResult;
import com.yayo.warriors.module.props.model.SynthObject;
import com.yayo.warriors.module.props.model.SynthResult;
import com.yayo.warriors.module.props.model.SynthStoneResult;
import com.yayo.warriors.module.props.model.WashAttributeVO;
import com.yayo.warriors.module.props.parser.PropsParser;
import com.yayo.warriors.module.props.parser.PropsParserContext;
import com.yayo.warriors.module.props.rule.EquipHelper;
import com.yayo.warriors.module.props.rule.PropsHelper;
import com.yayo.warriors.module.props.rule.PropsRule;
import com.yayo.warriors.module.props.type.BlinkType;
import com.yayo.warriors.module.props.type.EquipType;
import com.yayo.warriors.module.props.type.PropsChildType;
import com.yayo.warriors.module.props.type.PropsType;
import com.yayo.warriors.module.props.type.Quality;
import com.yayo.warriors.module.server.listener.LogoutListener;
import com.yayo.warriors.module.shop.facade.ShopFacade;
import com.yayo.warriors.module.task.facade.TaskFacade;
import com.yayo.warriors.module.task.facade.TaskMainFacade;
import com.yayo.warriors.module.task.manager.EscortTaskManager;
import com.yayo.warriors.module.trade.manager.TradeManager;
import com.yayo.warriors.module.treasure.facade.TreasureFacade;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.Player.PackLock;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.rule.AttributeRule;
import com.yayo.warriors.module.user.rule.PlayerRule;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.module.user.type.PortableType;
import com.yayo.warriors.type.ElementType;
import com.yayo.warriors.type.FormulaKey;
import com.yayo.warriors.type.GoodsType;
import com.yayo.warriors.type.IndexName;

/**
 * 道具接口实现类
 * 
 * @author Hyint
 */
@Component
public class PropsFacadeImpl implements PropsFacade, LogoutListener {
	@Autowired
	private MapFacade mapFacade;
	@Autowired
	private DbService dbService;
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private ShopFacade shopFacade;
	@Autowired
	private TaskFacade taskFacade;
	@Autowired
	private PetManager petManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private TradeManager tradeManager;
	@Autowired
	private EquipService equipService;
	@Autowired
	private ShenwuService shenwuService;
	@Autowired
	private PetPushHelper petPushHelper;
	@Autowired
	private TreasureFacade treasureFacade;
	@Autowired
	private CoolTimeManager coolTimeManager;
	@Autowired
	private TaskMainFacade taskEntityFacade;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private EscortTaskManager escortTaskManager;
	@Autowired
	private PropsParserContext itemParserContext;
	@Autowired
	private AchieveFacade achieveFacade;
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	/** 洗练属性上下文 */
	private final ConcurrentHashMap<Long, WashAttributeVO> WASH_ATTRIBUTE_CONTEXT = new ConcurrentHashMap<Long, WashAttributeVO>(5);
	
	/** 不用推送使用的道具子类型 */
	private final int[] notPushUsePropsType = {PropsChildType.TREASURE_PROPS_TYPE, PropsChildType.FASTEN_GIFI_TYPE, PropsChildType.RAND_GIFI_TYPE};
	
	
	public List<UserProps> queryUserProps(Object[] userPropsIds) {
		List<UserProps> userPropsList = new ArrayList<UserProps>();
		if(userPropsIds == null || userPropsIds.length <= 0) {
			return userPropsList;
		}
		
		for (Object userPropId : userPropsIds) {
			UserProps userProps = propsManager.getUserProps(((Number) userPropId).longValue());
			if (userProps != null) {
				userPropsList.add(userProps);
			}
		}
		return userPropsList;
	}

	/**
	 * 列出背包中的装备实体信息
	 * 
	 * @param  playerId		角色ID
	 * @param  backpack		背包号
	 * @return {@link List}	背包实体信息
	 */
	
	public List<BackpackEntry> listEquipBackpackEntry(long playerId, int backpack) {
		List<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		List<UserEquip> userEquips = propsManager.listUserEquip(playerId, backpack);
		if(userEquips != null && !userEquips.isEmpty()) {
			backpackEntries.addAll(voFactory.getUserEquipEntries(userEquips));
		}
		
		UserDomain userDomain = userManager.getUserDomain(playerId);
		this.equipBlink(userDomain);//获取装备的时候,计算一下玩家身上的闪光效果
		return backpackEntries;
	}

	/**
	 * 列出背包中的道具实体信息
	 * 
	 * @param  playerId		角色ID
	 * @param  backpack		背包号
	 * @return {@link List}	背包实体信息
	 */
	
	public List<BackpackEntry> listPropsBackpackEntry(long playerId, int backpack) {
		List<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		List<UserProps> userPropsList = propsManager.listUserProps(playerId, backpack);
		if(userPropsList != null && !userPropsList.isEmpty()) {
			backpackEntries.addAll(userPropsList);
		}
		return backpackEntries;
	}

	/**
	 * 丢弃用户装备
	 * 
	 * @param playerId				
	 * @param userEquipId
	 * @return
	 */
	
	public ResultObject<BackpackEntry> dropUserEquip(long playerId, long userEquipId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if(userEquip == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		}

		int backpack = userEquip.getBackpack();
		if(!userEquip.validBackpack(CAN_DROP_PACKAGES)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userEquip.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userEquip.isTrading()) {
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		}
		
		int index = userEquip.getIndex();
		int baseId = userEquip.getBaseId();
		Quality quality = userEquip.getQuality();
		ChainLock lock = LockUtils.getLock(userDomain.getPackLock());
		try {
			lock.lock();
			if(!userEquip.validBackpack(CAN_DROP_PACKAGES)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if(userEquip.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			}
			
			userEquip.setCount(0);
			userEquip.setDiscardTime(new Date());
			userEquip.setBackpack(DROP_BACKPACK);
			propsManager.put2UserEquipIdsList(playerId, DROP_BACKPACK, userEquip);
			propsManager.removeFromEquipIdsList(playerId, backpack, userEquip);
		} finally {
			lock.unlock();
		}

		dbService.submitUpdate2Queue(userEquip);
		GoodsLogger.goodsLogger(userDomain.getPlayer(), Source.PROPS_DROP_EQUIP, LoggerGoods.outcomeEquip(userEquipId, baseId, 1));
		return ResultObject.SUCCESS(BackpackEntry.valueEquip(userEquipId, baseId, 1, backpack, quality, index, userEquip.isBinding()));
	}

	/**
	 * 出售用户装备
	 * 
	 * @param  playerId				角色ID
	 * @param  userEquipId			用户装备ID
	 * @return {@link Integer}		用户装备ID
	 */
	@SuppressWarnings("unchecked")
	
	public ResultObject<BackpackEntry> sellUserEquip(long playerId, long userEquipId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if(userEquip == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if(userEquip.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userEquip.isTrading()) {
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		} 

		int backpack = userEquip.getBackpack();
		if(!userEquip.validBackpack(CAN_SOLD_BACKPACKS)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		}
		
		int baseId = userEquip.getBaseId();
		EquipConfig equipConfig = propsManager.getEquipConfig(baseId);
		if(equipConfig == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if(!equipConfig.getCanSales()) {
			return ResultObject.ERROR(CANNOT_SELLS);
		}
		
		Player player = userDomain.getPlayer();
		int silverPrice = equipConfig.getSellSilverPrice();
		ChainLock lock = LockUtils.getLock(player, userDomain.getPackLock());
		try {
			lock.lock();
			if (userEquip.getCount() <= 0) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if(!userEquip.validBackpack(CAN_SOLD_BACKPACKS)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if(userEquip.isTrading()) {
				return ResultObject.ERROR(EQUIP_CANNOT_USE);
			}
			
			userEquip.setCount(0);
			userEquip.setDiscardTime(new Date());
			userEquip.setBackpack(SOLD_BACKPACK);
			player.increaseSilver(silverPrice);
			propsManager.put2UserEquipIdsList(playerId, SOLD_BACKPACK, userEquip);
			propsManager.removeFromEquipIdsList(playerId, backpack, userEquip);
		} finally {
			lock.unlock();
		}
		
		int index = userEquip.getIndex();
		Quality quality = userEquip.getQuality();
		dbService.submitUpdate2Queue(player, userEquip);
		
		LoggerGoods outcomeEquip = LoggerGoods.outcomeEquip(userEquipId, baseId, 1);
		if(silverPrice != 0) {
			SilverLogger.inCome(Source.PROPS_SELL_USEREQUIP, silverPrice, player, outcomeEquip);
		}
		
		GoodsVO goodsVO = GoodsVO.valueOf(baseId, GoodsType.EQUIP, -1);
		MessagePushHelper.pushGoodsCountChange2Client(playerId, goodsVO);
		GoodsLogger.goodsLogger(userDomain.getPlayer(), Source.PROPS_SELL_USEREQUIP, outcomeEquip);
		return ResultObject.SUCCESS(BackpackEntry.valueEquipEmpty(userEquipId, baseId, backpack, quality, index, userEquip.isBinding()));
	}

	/**
	 * 角色上装.
	 * 
	 * @param  playerId				角色ID
	 * @param  userEquipId			用户装备ID
	 * @param  targetEquipId 		被替换下的装备ID
	 * @return {@link ResultObject}	穿着装备的返回值. [原来在身上的装备, 原来在背包中的装备]
	 */
	@SuppressWarnings("unchecked")
	
	public ResultObject<BackpackEntry[]> dressUserEquip(long playerId, long userEquipId, long targetEquipId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		} 

		PlayerBattle battle = userDomain.getBattle();
		if(battle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		}
		
		Player player = userDomain.getPlayer();
		// 需要验证角色的状态
		int backpack = BackpackType.DEFAULT_BACKPACK;
		int dressBackpack = BackpackType.DRESSED_BACKPACK;
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if(userEquip == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if(!userEquip.validBackpack(backpack)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userEquip.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userEquip.isOutOfExpiration()) {
			return ResultObject.ERROR(OUT_OF_EXPIRATION);
		} else if(userEquip.isTrading()) { //是否交易中
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		}
		
		int equipId = userEquip.getBaseId();
		EquipConfig equipConfig = propsManager.getEquipConfig(equipId);
		if(equipConfig == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if(equipConfig.getLevel() > battle.getLevel()) {
			return ResultObject.ERROR(LEVEL_INVALID);
		} else if(!equipConfig.isJobType(battle.getJob())) {
			return ResultObject.ERROR(PLAYER_CLAZZ_INVALID);
		} else if(!equipConfig.isSexFit(battle.getJob().getSex())) {
			return ResultObject.ERROR(SEX_INVALID);
		}
		
		UserEquip dressedEquip = null;
		int willDressType = equipConfig.getPropsType();
		if(targetEquipId > 0L) { //需要替换的装备, 这件装备就在身上了
			dressedEquip = propsManager.getUserEquip(targetEquipId);
			if(dressedEquip == null) {
				return ResultObject.ERROR(EQUIP_NOT_FOUND);
			} else if(!dressedEquip.validBackpack(DRESSED_BACKPACK)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if(dressedEquip.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			}
			
			int targetId = dressedEquip.getBaseId();
			EquipConfig targetConfig = propsManager.getEquipConfig(targetId);
			if(targetConfig == null) {
				return ResultObject.ERROR(EQUIP_NOT_FOUND);
			} else if(targetConfig.getPropsType() != willDressType) {
				return ResultObject.ERROR(TYPE_INVALID);
			}
		} else {
			dressedEquip = getUserEquipByType(playerId, dressBackpack, willDressType);
		}
		
		//穿在身上的装备
		long dressEquipId = -1L;					//穿着的装备ID
		int userEquipIndex = -1;					//装备当前在背包中的下标
		int dressEquipIndex = -1;					//穿在身上我物品ID
		Fightable beforable = battle.getAndCopyAttributes();
		ChainLock lock = LockUtils.getLock(battle, player.getPackLock(), player);
		try {
			lock.lock();
			if(!userEquip.validBackpack(backpack)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if(userEquip.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			} else if(userEquip.isOutOfExpiration()) {
				return ResultObject.ERROR(OUT_OF_EXPIRATION);
			} else if(userEquip.isTrading()) {
				return ResultObject.ERROR(EQUIP_CANNOT_USE);
			}
			
			userEquip.setBinding(true);
			userEquip.updateExpiration();
			userEquipIndex = userEquip.getIndex();
			userEquip.setBackpack(DRESSED_BACKPACK);
			if(dressedEquip != null) {
				dressEquipId = dressedEquip.getId();
				dressEquipIndex = dressedEquip.getIndex();
				dressedEquip.setIndex(userEquipIndex);
				if(dressedEquip.getEquipConfig().isFaction()) {
					player.setFashionShow(false);
				}
				
				dressedEquip.setBackpack(DEFAULT_BACKPACK);
				battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
				dbService.submitUpdate2Queue(userEquip, dressedEquip);
				propsManager.changeUserEquipBackpack(playerId, DRESSED_BACKPACK, DEFAULT_BACKPACK, dressedEquip);
				propsManager.changeUserEquipBackpack(playerId, DEFAULT_BACKPACK, DRESSED_BACKPACK, userEquip);
			} else {
				if(equipConfig.isFaction()) {
					player.setFashionShow(true);
				}
				
				userEquip.setIndex(dressEquipIndex);
				dbService.submitUpdate2Queue(userEquip, player);
				battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
				propsManager.changeUserEquipBackpack(playerId, DEFAULT_BACKPACK, DRESSED_BACKPACK, userEquip);
			}
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("背包中装备位置:[{}] 身上位置:[{}]", userEquipIndex, dressEquipIndex);
			}
		} finally {
			lock.unlock();
		}
		
		BackpackEntry backEqiupEntry = voFactory.getUserEquipEntry(userEquip);
		BackpackEntry dressEqiupEntry = voFactory.getUserEquipEntry(dressedEquip); 
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("角色:[{}] 穿上装备:[{}] 成功, 换下装备:[{}]. 装备类型:[{}] .", new Object[] { playerId, userEquipId, dressEquipId, willDressType });
		}
		UserPushHelper.pushPlayerAttributeChange(beforable, userDomain);
		EquipPushHelper.pushDressAttributeChanges(userDomain, AttributeRule.DRESS_ATTRIBUTE_CHANGES);
		this.equipBlink(userDomain); //装备闪光效果
		return ResultObject.SUCCESS(new BackpackEntry[] { dressEqiupEntry, backEqiupEntry });
	}
	
	/**
	 * 根据装备类型取得指定的装备
	 * 
	 * @param  playerId				角色ID
	 * @param  backpack				背包号
	 * @param  equipType			装备类型		
	 * @return {@link UserEquip}	用户装备类型
	 */
	private UserEquip getUserEquipByType(long playerId, int backpack, int equipType) {
		List<UserEquip> dressEquips = new ArrayList<UserEquip>();
		List<UserEquip> userEquipList = propsManager.listUserEquip(playerId, backpack);
		if(userEquipList != null && !userEquipList.isEmpty()) {
			for (UserEquip userEquip : userEquipList) {
				int baseId = userEquip.getBaseId();
				EquipConfig base = propsManager.getEquipConfig(baseId);
				if(base.getPropsType() != equipType) {
					continue;
				} 

				if(equipType != EquipType.RING_TYPE) {
					return userEquip;
				}
				dressEquips.add(userEquip);
			}
		}
		
		if(dressEquips.size() < 2) {
			return null;
		}
		
		int randomIndex = Tools.getRandomInteger(dressEquips.size());
		return dressEquips.get(randomIndex);
	}

	/**
	 * 角色脱下装备
	 * 
	 * @param  playerId				角色ID
	 * @param  userEquipId			用户装备ID
	 * @param  index				背包的索引号
	 * @return {@link ResultObject}	穿着装备的返回值. [原来在身上的装备, 原来在背包中的装备]
	 */
	@SuppressWarnings("unchecked")
	
	public ResultObject<BackpackEntry[]> undressUserEquip(long playerId, long userEquipId, int index) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		} 
		
		//验证角色的状态
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		if(player == null || battle == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if(userEquip == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if(!userEquip.validBackpack(DRESSED_BACKPACK)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userEquip.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userEquip.isTrading()) {
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		}
		
		int equipId = userEquip.getBaseId();
		EquipConfig equipConfig = propsManager.getEquipConfig(equipId);
		if(equipConfig == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		}
		
		Fightable beforable = battle.getAndCopyAttributes();
		int currBackSize = propsManager.getBackpackSize(playerId, DEFAULT_BACKPACK);
		ChainLock lock = LockUtils.getLock(player.getPackLock(), player, battle);
		try {
			lock.lock();
			if(!player.canAddNew2Backpack(currBackSize + 1, DEFAULT_BACKPACK)) {
				return ResultObject.ERROR(BACKPACK_FULLED);
			}
			
			if(!userEquip.validBackpack(DRESSED_BACKPACK)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if(userEquip.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			}
			
			
			userEquip.setBackpack(DEFAULT_BACKPACK);
			userEquip.setIndex(index < 0 ? -1 : index);
			if(equipConfig.isFaction()) {
				player.setFashionShow(false);
			}

			dbService.submitUpdate2Queue(userEquip, player);
			battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
			propsManager.changeUserEquipBackpack(playerId, DRESSED_BACKPACK, DEFAULT_BACKPACK, userEquip);
		} finally {
			lock.unlock();
		}
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("角色:[{}] 脱下装备:[{}], 装备类型:[{}] 成功.", new Object[]{ playerId, userEquipId, equipConfig.getPropsType() });
		}
		
		//由于没办法, 换装这种不频繁的操作, 需要立即入库. 改变的背包号的都应该立即入库
		UserPushHelper.pushPlayerAttributeChange(beforable, userDomain);
		EquipPushHelper.pushDressAttributeChanges(userDomain, AttributeRule.DRESS_ATTRIBUTE_CHANGES);
		BackpackEntry dressEqiupEntry = voFactory.getUserEquipEntry(userEquip);
		this.equipBlink(userDomain); //装备闪光效果
		return ResultObject.SUCCESS(new BackpackEntry[] { dressEqiupEntry, null});
	}
	
	/**
	 * 损坏角色身上装备的耐久值
	 * 
	 * @param playerId				角色ID
	 * @param damageEndure			损坏装备的耐久值
	 */
	@SuppressWarnings("unchecked")
	
	public void damageUserEquipEndure(long playerId, int damageEndure) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return;
		}
		
		List<UserEquip> userEquips = propsManager.listUserEquip(playerId, DRESSED_BACKPACK);
		if(userEquips == null || userEquips.isEmpty()) {
			return;
		}
		
		List<UserEquip> damageEquips = new ArrayList<UserEquip>();
		Collection<EndureInfo> endureInfos = new HashSet<EndureInfo>();
		boolean reflushable = false;
		final PackLock packLock = userDomain.getPackLock();
		for (UserEquip userEquip : userEquips) {
			EquipConfig equipConfig = userEquip.getEquipConfig();
			if(userEquip.getCurrentEndure() <= 0 || equipConfig.isFaction()) { //没有耐久, 或者时装, 则不扣除耐久
				continue;
			}
			
			ChainLock lock = LockUtils.getLock(packLock);
			try {
				lock.lock();
				int currentEndure = userEquip.getCurrentEndure();
				int costEndure = currentEndure < damageEndure ? currentEndure : damageEndure;
				if(costEndure <= 0) {
					continue;
				}
				
				damageEquips.add(userEquip);
				Long userEquipId = userEquip.getId();
				userEquip.setCurrentEndure(Math.max(0, currentEndure - costEndure));
				reflushable = userEquip.getCurrentEndure() <= 0 ? true : reflushable;
				endureInfos.add(EndureInfo.valueOf(userEquipId, DRESSED_BACKPACK, userEquip.getCurrentEndure()));
			} finally {
				lock.unlock();
			}
		}
		
		if(!damageEquips.isEmpty()) {
			dbService.submitUpdate2Queue(damageEquips);
		}
		
		if(reflushable) {
			userDomain.updateFlushable(true, Flushable.FLUSHABLE_NORMAL);
		}
		
		if(!endureInfos.isEmpty()) {
			EquipPushHelper.pushEquipEndureDamageInfo(playerId, endureInfos);
		}
	}

	/**
	 * 使用用户道具
	 * 
	 * @param  playerId				角色ID
	 * @param  userItemId			用户道具ID
	 * @param  count				数量
	 * @return {@link ResultObject}	用户道具模块返回值
	 */
	
	public int useProps(long playerId, long userItemId, int count) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND; 
		}
		
		//验证角色的定身效果
		PlayerBattle playerBattle = userDomain.getBattle();
		UserBuffer userBuffer = userDomain.getUserBuffer();
		if(BufferHelper.isPlayerInImmobilize(userBuffer)) {
			return FAILURE;
		}
		
		UserProps userItem = propsManager.getUserProps(userItemId);
		int backpack = BackpackType.DEFAULT_BACKPACK;
		if(userItem == null || !userItem.validBackpack(backpack)) {
			return ITEM_NOT_FOUND;
		} else if(userItem.getPlayerId() != playerId) {
			return BELONGS_INVALID;
		} else if(userItem.isOutOfExpiration()) {
			return OUT_OF_EXPIRATION;
		} else if(userItem.getCount() <= 0) {
			return ITEM_NOT_ENOUGH;
		} else if(userItem.isTrading()) {
			return ITEM_CANNOT_USE;
		}
		
		int itemId = userItem.getBaseId();
		PropsConfig propsConfig = propsManager.getPropsConfig(itemId);
		if(propsConfig == null) {
			return ITEM_NOT_FOUND;
		} else if(playerBattle.getLevel() < propsConfig.getLevel()){
			return LEVEL_INVALID;
		} else if(!propsConfig.isCanUse()){
			return ITEM_CANNOT_USE;
		}
		
		UserCoolTime userCoolTime = coolTimeManager.getUserCoolTime(playerId);
		if(userCoolTime == null) {
			return BASEDATA_NOT_FOUND;
		}
		
		int coolTimeId = propsConfig.getCdId();
		CoolTimeConfig coolTime = null;
		if(coolTimeId > 0){
			coolTime = coolTimeManager.getCoolTimeConfig(coolTimeId);
			if(coolTime == null) {
				LOGGER.error("道具:[{}] CDID:[{}] 基础数据不存在", propsConfig.getId(), coolTimeId);
				return BASEDATA_NOT_FOUND;
			} else if(userCoolTime.isCoolTiming(coolTimeId)) {
				return COOL_TIMING;
			}
		}
		int type = propsConfig.getPropsType();
		PropsParser parser = itemParserContext.getParser(type);
		if(parser == null) {
			return ITEM_CANNOT_USE;
		}
		
		// 调用效果处理器处理
		int result = parser.effect(userDomain, userCoolTime, coolTime, userItem, count);
		if(userItem.getCount() <= 0) { //这里是角色默认背包, 如果使用完则清除
			propsManager.put2UserPropsIdsList(playerId, BackpackType.DROP_BACKPACK, userItem);
			propsManager.removeFromUserPropsIdsList(playerId, backpack, userItem);
		}
		
		Player player = userDomain.getPlayer();
		if(result == PropsConstant.SUCCESS && !ArrayUtils.contains(notPushUsePropsType, propsConfig.getChildType()) ){
			GoodsLogger.goodsLogger(player, Source.PROPS_USE_PROPS, LoggerGoods.outcomeProps(userItemId, itemId, count));
			MessagePushHelper.pushGoodsCountChange2Client(playerId, GoodsVO.valueOf(itemId, GoodsType.PROPS, -1));
		}
		return result;
	}

	/** 
	 * 丢弃用户道具
	 * 
	 * @param  playerId				用户ID
	 * @param  userPropsId			用户道具ID
	 * @return {@link Integer}		用户道具模块返回值
	 */
	
	public ResultObject<BackpackEntry> dropUserProps(long playerId, long userPropsId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND); 
		}
		
		Player player = userDomain.getPlayer();
		if(player == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		UserProps userProps = propsManager.getUserProps(userPropsId);
		if(userProps == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(userProps.getCount() <= 0) {
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		} else if(userProps.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userProps.isTrading()) {
			return ResultObject.ERROR(ITEM_CANNOT_USE);
		}

		int backpack = userProps.getBackpack();
		if(!userProps.validBackpack(CAN_DROP_PACKAGES)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		}
		
		PropsConfig propsConfig = resourceService.get(userProps.getBaseId(), PropsConfig.class);
		if(propsConfig == null){
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		} else if(!propsConfig.isCanDrop()){
			return ResultObject.ERROR(PROPS_CAN_NOT_DROP);
		}
		
		int index = userProps.getIndex();
		int baseId = userProps.getBaseId();
		int propsCount = userProps.getCount();
		Quality quality = userProps.getQuality();
		ChainLock lock = LockUtils.getLock(player.getPackLock());
		try {
			lock.lock();
			if (!userProps.validBackpack(CAN_DROP_PACKAGES)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if (userProps.getCount() <= 0) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			} else if(userProps.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			} 
			userProps.setCount(0);
			userProps.setBackpack(DROP_BACKPACK);
			propsManager.put2UserPropsIdsList(playerId, DROP_BACKPACK, userProps);
			propsManager.removeFromUserPropsIdsList(playerId, backpack, userProps);
		} finally {
			lock.unlock();
		}
		dbService.submitUpdate2Queue(userProps);
		treasureFacade.dropUserTreasure(userDomain, userPropsId);
		GoodsLogger.goodsLogger(player, Source.PROPS_DROP_PROP, LoggerGoods.outcomeProps(userPropsId, baseId, propsCount));
		return ResultObject.SUCCESS(BackpackEntry.valueProps(userPropsId, baseId, propsCount, backpack, quality, index, userProps.isBinding()));
	}

	/**
	 * 出售用户道具
	 * 
	 * @param  playerId				用户ID
	 * @param  userPropsId			用户道具ID
	 * @return {@link Integer}		用户道具模块返回值
	 */
	@SuppressWarnings("unchecked")
	
	public ResultObject<BackpackEntry> sellUserProps(long playerId, long userPropsId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND); 
		}
		
		Player player = userDomain.getPlayer();
		if(player == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		UserProps userProps = propsManager.getUserProps(userPropsId);
		if(userProps == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(userProps.getCount() <= 0) {
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		} else if(userProps.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userProps.isTrading()) {
			return ResultObject.ERROR(ITEM_CANNOT_USE);
		} 

		int backpack = userProps.getBackpack();
		if(!userProps.validBackpack(CAN_SOLD_BACKPACKS)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		}
		
		int baseId = userProps.getBaseId();
		int propsCount = userProps.getCount();
		PropsConfig propsConfig = propsManager.getPropsConfig(baseId);
		if(propsConfig == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(!propsConfig.getCanSales()) {
			return ResultObject.ERROR(CANNOT_SELLS);
		}
		
		int itemCount = 0;
		int incomeSilver = 0;
		int silverPrice = propsConfig.getSellSilverPrice();
		ChainLock lock = LockUtils.getLock(player, userDomain.getPackLock());
		try {
			lock.lock();
			itemCount = userProps.getCount();
			if (itemCount <= 0) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			} else if(!userProps.validBackpack(CAN_SOLD_BACKPACKS)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			}
			
			userProps.setCount(0);
			userProps.setBackpack(SOLD_BACKPACK);
			incomeSilver = itemCount * silverPrice;
			player.increaseSilver(incomeSilver);
			dbService.submitUpdate2Queue(player, userProps);
			
			propsManager.put2UserPropsIdsList(playerId, BackpackType.SOLD_BACKPACK, userProps);
			propsManager.removeFromUserPropsIdsList(playerId, backpack, userProps);
		} finally {
			lock.unlock();
		}
		
		int index = userProps.getIndex();
		Quality quality = userProps.getQuality();
		LoggerGoods outcomeProps = LoggerGoods.outcomeProps(userPropsId, baseId, propsCount);
		GoodsLogger.goodsLogger(player, Source.PROPS_SELL_USERPROPS, outcomeProps);
		if(incomeSilver != 0) {
			SilverLogger.inCome(Source.PROPS_SELL_USERPROPS, incomeSilver, player, outcomeProps);
		}
		
		GoodsVO goodsVO = GoodsVO.valueOf(baseId, GoodsType.PROPS, -propsCount);
		MessagePushHelper.pushGoodsCountChange2Client(playerId, goodsVO);
		return ResultObject.SUCCESS(BackpackEntry.valueProps(userPropsId, baseId, 0, backpack, quality, index, userProps.isBinding()));
	}

	/**
	 * 更新背包位置
	 * 
	 * @param playerId				角色ID
	 * @param info					背包位置信息.格式:[[物品ID, 物品类型, 物品位置], [物品ID, 物品类型, 物品位置]...]
	 */
	@SuppressWarnings("unchecked")
	
	public void updateBackpackEntryPosition(long playerId, Object[] info) {
		if(info == null || info.length <= 0) {
			return;
		}
		
		List<UserEquip> userEquipList = new ArrayList<UserEquip>();
		List<UserProps> userPropsList = new ArrayList<UserProps>();
		for (Object element : info) {
			if(element == null || element.getClass() != Object[].class) {
				continue;
			}
			
			Object[] array = (Object[])element;
			if(array == null || array.length < 3) {
				continue;
			}
			
			Long goodsId = ((Number)array[0]).longValue();
			Integer type = ((Number)array[1]).intValue();
			Integer index = ((Number)array[2]).intValue();
			if(goodsId == null || type == null || index == null) {
				continue;
			}
			
			if(type == GoodsType.PROPS) {
				this.updateUserPropsPosition(playerId, goodsId, index, userPropsList);
			} else if(type == GoodsType.EQUIP) {
				this.updateUserEquipsPosition(playerId, goodsId, index, userEquipList);
			}
		}
		
		if(!userEquipList.isEmpty() || !userPropsList.isEmpty()) {
			dbService.submitUpdate2Queue(userEquipList, userPropsList);
		}
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("角色:[{}] 保存背包位置成功", playerId);
		}
	}
	
	/**
	 * 更新用户道具位置信息
	 * 
	 * @param playerId
	 * @param userPropsId
	 * @param index
	 * @return
	 */
	private boolean updateUserPropsPosition(long playerId, long userPropsId, int index, List<UserProps> userPropsList) {
		UserProps userProps = propsManager.getUserProps(userPropsId);
		if(userProps == null) {
			return false;
		} else if(userProps.getCount() <= 0) {
			return false;
		} else if(userProps.getPlayerId() != playerId) {
			return false;
		} else if(!userProps.validBackpack(SAVE_POSITION_BACKPACKS)) {
			return false;
		} else if(userProps.getIndex() == index) {
			return false;
		}
		
		userProps.setIndex(index < 0 ? -1 : index);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("角色:[{}] 更新道具:[{}] 位置为: [{}]", new Object[] { playerId,  userPropsId, index });
		}
		userPropsList.add(userProps);
		return true;
	}
	
	/**
	 * 更新用户装备数据
	 * 
	 * @param  playerId			角色ID
	 * @param  userEquipId		用户装备ID
	 * @param  index			物品下标
	 * @return {@link Boolean}	
	 */
	private boolean updateUserEquipsPosition(long playerId, long userEquipId, int index, List<UserEquip> userEquips) {
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if(userEquip == null) {
			return false;
		} else if(userEquip.getCount() <= 0) {
			return false;
		} else if(userEquip.getIndex() == index) {
			return false;
		} else if(userEquip.getPlayerId() != playerId) {
			return false;
		} else if(!userEquip.validBackpack(SAVE_POSITION_BACKPACKS)) {
			return false;
		}
		
		userEquip.setIndex(index < 0 ? -1 : index);
		userEquips.add(userEquip);
		return true;
	}
	
	/** 
	 * 使用HP/MP/家将HP便携包
	 * 
	 * @param  playerId				角色ID
	 * @param  types				补血类型. 详细见: {@link PortableType}
	 * @param  needPost				是否需要推送
	 * @return {@link Integer}		是否使用成功
	 */
	
	public int usePortableBag(long playerId, boolean needPost, PortableType...types) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		if(!userDomain.canUsePropsInMap(ItemLimitTypes.HPMP_BAG)){
			return MAP_LIMIT_CAN_USE_HPMP_BAG;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle == null) {
			return PLAYER_NOT_FOUND;
		} else if(types.length <= 0) {
			return FAILURE;
		}
		
		UserCoolTime userCoolTime = coolTimeManager.getUserCoolTime(playerId);
		if(userCoolTime == null) {
			return BASEDATA_NOT_FOUND;
		}

		if(escortTaskManager.isEscortStatus(battle)){
			return PropsConstant.USEPORTABLEBAG_ESCORT_STATUS;
		}
		
		boolean usePetHPBag = ArrayUtils.contains(types, PortableType.PET_HPBAG);	//使用家将便携包
		Set<Object> locks = new HashSet<Object>(2);
		locks.add(battle);
		PetDomain petDomain = null; 
		if(usePetHPBag){
			petDomain = petManager.getFightingPet(playerId);
			if(petDomain != null){
				locks.add(petDomain.getBattle());
			}
		}
		int result = FAILURE;
		Set<Integer> attributes = new HashSet<Integer>();
		ChainLock lock = LockUtils.getLock(locks.toArray());
		try {
			lock.lock();
			if(battle.isDead()){
				return PropsConstant.PLAYER_DEADED;
			}
			
			for (PortableType type : types) {
				if(type == PortableType.HPBAG) {
					if(checkCdTime(userCoolTime, PlayerRule.PLAYER_HP_PORTABLEBAG_CD_ID) != SUCCESS){
						continue;
					} else if(battle.getHpBag() <= 0){
						result = PropsConstant.HP_BAG_NOT_ENOUGH;
						continue;
					}
					int hp = battle.getHp();
					int hpBag = battle.getHpBag();
					int hpMax = battle.getHpMax();
					int addHp = hpBag > (hpMax - hp) ? (hpMax - hp) : hpBag;
					if(addHp > 0) {
						battle.increaseHp(addHp);
						battle.decreaseHpBag(addHp);
						attributes.add(HP);
						attributes.add(HP_BAG);
					}
				} else if(type == PortableType.MPBAG) {
					if(checkCdTime(userCoolTime, PlayerRule.PLAYER_MP_PORTABLEBAG_CD_ID) != SUCCESS){
						continue;
					} else if(battle.getMpBag() <= 0){
						result = PropsConstant.MP_BAG_NOT_ENOUGH;
						continue;
					}
					int mp = battle.getMp();
					int mpBag = battle.getMpBag();
					int mpMax = battle.getMpMax();
					int addMp = mpBag > (mpMax - mp) ? (mpMax - mp) : mpBag;
					if(addMp > 0) {
						battle.increaseMp(addMp);
						battle.decreaseMpBag(addMp);
						attributes.add(MP);
						attributes.add(MP_BAG);
					}
				} else if(type == PortableType.PET_HPBAG) {
					if(checkCdTime(userCoolTime, PlayerRule.PET_HP_PORTABLEBAG_CD_ID) != SUCCESS){
						continue;
					} else if(battle.getPetHpBag() <= 0){
						result = PropsConstant.PET_HP_BAG_NOT_ENOUGH;
						continue;
					}
					
					if(petDomain != null){
						PetBattle petBattle = petDomain.getBattle();
						int petHpBag = battle.getPetHpBag();
						int hp = petBattle.getHp();
						int hpMax = petBattle.getHpMax();
						int addHp = petHpBag > (hpMax - hp) ? (hpMax - hp) : petHpBag;
						if(addHp > 0) {
							petBattle.increaseHp(addHp);
							battle.decreasePetHpBag(addHp);
							attributes.add(PET_HP_BAG);
						}
					} else {
						return FAILURE;
					}
				}
				result =  SUCCESS;
			}
		} catch (Exception e) {
			LOGGER.error("{}", e);
		} finally {
			lock.unlock();
		}
		
		if(needPost && result == SUCCESS) {
			dbService.submitUpdate2Queue(battle);
			List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
			Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
			UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, attributes.toArray());
			if(petDomain != null){
				this.petPushHelper.pushPetAttribute(Arrays.asList(playerId), playerId, petDomain.getBattle().getId(), PetAttributeRule.PET_HP);
			}
		}
		return result;
	}
	
	
	public int batchBuyOrUseProps(long playerId, int propsId, int count, boolean isBuy, boolean isUse) {
		if(!isBuy && !isUse){
			return INPUT_VALUE_INVALID;
		}
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		PropsConfig propsConfig = propsManager.getPropsConfig(propsId);
		if(propsConfig == null){
			return BASEDATA_NOT_FOUND;
		}
		
		if(count <= 0){
			return INPUT_VALUE_INVALID;
		}
		
		//购买
		if(isBuy){
			MallConfig mallConfig = resourceService.getByUnique(IndexName.MALL_PROPSID, MallConfig.class, propsId);
			if(mallConfig == null){
				return BASEDATA_NOT_FOUND;
			}
			ResultObject<List<BackpackEntry>> resultObject = shopFacade.buyPropsByMall(playerId, mallConfig.getId(), count);
			if(resultObject.getResult() != SUCCESS){
				return resultObject.getResult();
			}
			
			List<Long> players = Arrays.asList(playerId);
			List<UnitId> playerUnitIds = Arrays.asList(userDomain.getUnitId());
			UserPushHelper.pushAttribute2AreaMember(playerId, players, playerUnitIds, AttributeKeys.GOLDEN, AttributeKeys.COUPON);
			MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, resultObject.getValue());
			BackpackEntry backpackEntry = resultObject.getValue().get(0);
			GoodsVO goodsVO = GoodsVO.valueOf(backpackEntry.getBaseId(), backpackEntry.getGoodsType(), count);
			MessagePushHelper.pushGoodsCountChange2Client(playerId, goodsVO);
		}
		
		//使用道具
		if(isUse){
			List<UserProps> userPropsList = propsManager.listUserPropByBaseId(playerId, propsId, BackpackType.DEFAULT_BACKPACK);
			int needCount = 1;
			for(UserProps userProps : userPropsList){
				int costCount = Math.min(needCount, userProps.getCount() );
				int result = useProps(playerId, userProps.getId(), costCount);
				if(result != SUCCESS){
					return result;
				}
				needCount -= costCount;
				if(needCount <= 0 ){
					break;
				}
			}
		}
		
		return SUCCESS;
	}

	/**
	 * 检查道具cd时间
	 * @param userCoolTime
	 * @param propsChildType
	 * @return
	 */
	private int checkCdTime(UserCoolTime userCoolTime, int coolTimeId){
//		int coolTimeId = propsService.getCdIdByPropChildType(propsChildType);
		if(coolTimeId > 0){
			CoolTimeConfig coolTime = coolTimeManager.getCoolTimeConfig(coolTimeId);
			if(coolTime == null) {
				LOGGER.error("CDID:[{}] 基础数据不存在", coolTimeId);
				return ITEM_NOT_FOUND;
			} else if(userCoolTime.isCoolTiming(coolTimeId)) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("CDID:[{}] 冷却中", coolTimeId);
				}
				return ITEM_NOT_FOUND;
			}
			userCoolTime.addCoolTime(coolTimeId, coolTime.getCoolTime());
//			userCoolTime.addCoolTime(coolTimeId, 1000);		//测试用
		}
		return SUCCESS;
	}

	/** 
	 * 整理背包位置
	 * 
	 * @param  playerId					角色ID
	 * @param  backpack					背包号
	 * @return {@link Integer}			道具模块返回值
	 */
	@SuppressWarnings("unchecked")
	
	public int settleBackpackPos(long playerId, int backpack) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		if(player == null) {
			return PLAYER_NOT_FOUND;
		}

		if(!ArrayUtils.contains(CAN_SETTLE_BACKPACKS, backpack)) {
			return BACKPACK_INVALID;
		}
		
		// 增加交易状态中的操作限制
		if (tradeManager.isTradeState(playerId)) {
			return FAILURE;
		}
		
		ChainLock lock = LockUtils.getLock(player.getPackLock());
		Map<Long, Integer> equipIndexMap = new HashMap<Long, Integer>(1);
		Map<Long, Integer> propsIndexMap = new HashMap<Long, Integer>(1);
		List<GoodsPosition> goodsPositions = new ArrayList<GoodsPosition>(1);
		List<BackpackEntry> backpackEntrys = new ArrayList<BackpackEntry>(1);
		try {
			lock.lock();
			List<UserEquip> userEquipList = propsManager.listUserEquip(playerId, backpack);
			List<UserProps> userPropsList = this.getAndMergeProps(playerId, backpack);
			if((userPropsList == null || userPropsList.isEmpty()) 
					&& (userEquipList == null || userEquipList.isEmpty())) {
				return FAILURE;
			}
			if(userPropsList != null && !userPropsList.isEmpty()) {
				for (UserProps userProps : userPropsList) {
					long goodsId = userProps.getId();
					int index = userProps.getIndex();
					int baseId = userProps.getBaseId();
					int goodsType = userProps.getGoodsType();
					PropsConfig propsConfig = propsManager.getPropsConfig(baseId);
					int backpackSort = propsConfig.getPackSort();
					propsIndexMap.put(goodsId, index);
					goodsPositions.add(GoodsPosition.valueOf(goodsId, goodsType, backpackSort));
				}
			}
			
			if(userEquipList != null && !userEquipList.isEmpty()) {
				for (UserEquip userEquip : userEquipList) {
					int index = userEquip.getIndex();
					int baseId = userEquip.getBaseId();
					long goodsId = userEquip.getId();
					int goodsType = userEquip.getGoodsType();
					EquipConfig equipConfig = propsManager.getEquipConfig(baseId);
					int backpackSort = equipConfig.getPackSort();
					equipIndexMap.put(goodsId, index);
					goodsPositions.add(GoodsPosition.valueOf(goodsId, goodsType, backpackSort));
				}
			}
				
			int index = 0;
			Collections.sort(goodsPositions);
			List<UserProps> savePropsList = new ArrayList<UserProps>();
			List<UserEquip> saveEquipList = new ArrayList<UserEquip>();
			
			for (GoodsPosition goodsPosition : goodsPositions) {
				long goodsId = goodsPosition.getId();
				int goodsType = goodsPosition.getGoodsType();
				if(goodsType == GoodsType.PROPS) {
					UserProps userProps = propsManager.getUserProps(goodsId);
					if(userProps != null) {
						userProps.setIndex(index);
						index++;
						backpackEntrys.add(voFactory.getUserPropsEntry(userProps));
						Integer oldIndex = propsIndexMap.get(goodsId);
						if(oldIndex == null || oldIndex != userProps.getIndex()) {
							savePropsList.add(userProps);
						}
					}
				} else if(goodsType == GoodsType.EQUIP) {
					UserEquip userEquip = propsManager.getUserEquip(goodsId);
					if(userEquip != null) {
						userEquip.setIndex(index);
						index++;
						backpackEntrys.add(voFactory.getUserEquipEntry(userEquip));
						Integer oldIndex = equipIndexMap.get(goodsId);
						if(oldIndex == null || oldIndex != userEquip.getIndex()) {
							saveEquipList.add(userEquip);
						}
					}
				}
			}
			
			if(!savePropsList.isEmpty() || !saveEquipList.isEmpty()) {
				dbService.submitUpdate2Queue(saveEquipList, savePropsList);
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("整理背包, 用户装备列表入库列表: {}", Arrays.toString(saveEquipList.toArray(new UserEquip[0])));
					LOGGER.debug("整理背包, 用户道具列表入库列表: {}", Arrays.toString(savePropsList.toArray(new UserProps[0])));
				}
			}
				
		} finally {
			lock.unlock();
		}
		
		if(backpackEntrys != null && backpackEntrys.size() > 0) {
			MessagePushHelper.pushUserProps2Client(playerId, backpack, true, backpackEntrys);
		}
		return SUCCESS;
	}
	
	/**
	 * 查询并且合并用户道具
	 * 
	 * @param  playerId		角色ID
	 * @param  backpack		背包号
	 * @return {@link List}	物品信息
	 */
	@SuppressWarnings("unchecked")
	private List<UserProps> getAndMergeProps(long playerId, int backpack) {
		//最大数量集合 [ 基础道具ID, 最大的道具数量 ] 
		Map<Integer, Integer> maxCounterMap = new HashMap<Integer, Integer>(3);
		//当前的道具数量: [ 用户道具ID, 用户道具当前数量 ]
		Map<Long, Integer> currentItemCounter = new HashMap<Long, Integer>(3);
		//相同类型的用户道具列表. { 相同类型的字符串, 物品列表 } 
		Map<String, List<UserProps>> propsMaps = new HashMap<String, List<UserProps>>(3);
		List<UserProps> userPropsList = propsManager.listUserProps(playerId, backpack);
		if(userPropsList == null || userPropsList.isEmpty()) {
			return userPropsList;
		}
		
		for (UserProps userProp : userPropsList) {
			int baseId = userProp.getBaseId();
			boolean binding = userProp.isBinding();
			Date expirate = userProp.getExpiration();
			String hashKey = toBackpackKey(baseId, binding, expirate);
			List<UserProps> list = propsMaps.get(hashKey);
			if(list == null) {
				list = new ArrayList<UserProps>();
				propsMaps.put(hashKey, list);
			}
			list.add(userProp);
			if(!maxCounterMap.containsKey(baseId)) {
				PropsConfig propsConfig = propsManager.getPropsConfig(baseId);
				maxCounterMap.put(baseId, propsConfig.getMaxAmount());
			}
		}
		
		List<Long> currProcItemIdList = new ArrayList<Long>();	//当前背包位置中, 已经处理的用户道具ID
		for (List<UserProps> sameProps : propsMaps.values()) {
			currProcItemIdList.clear();
			for (UserProps sameItem : sameProps) {
				long userItemId = sameItem.getId();
				int currCount = sameItem.getCount();
				int baseId = sameItem.getBaseId();
				//TODO 这里要测试
				if(!sameItem.isTrading() && !currProcItemIdList.contains(userItemId)){
					currProcItemIdList.add(userItemId);
				}
				
				currentItemCounter.put(userItemId, currCount);
				int maxCount = maxCounterMap.get(baseId);
				for (long sameItemId : currProcItemIdList) {
					if(sameItemId == userItemId) {
						continue;
					}
				
					if(currCount <= 0) {
						break;
					}
					
					Integer cacheCount = currentItemCounter.get(sameItemId);
					cacheCount = cacheCount == null ? 0 : cacheCount;
					int canAddCount = Math.max(0, maxCount - cacheCount);
					int addCount = Math.min(canAddCount, currCount);
					if(addCount > 0) {
						currentItemCounter.put(sameItemId, cacheCount + addCount);
						currCount = Math.max(0, currCount - addCount);
					}
				}
				currentItemCounter.put(userItemId, currCount);
			}
		}
		
		List<UserProps> userPropList = new ArrayList<UserProps>();
		UserDomain userDomain = userManager.getUserDomain(playerId);
		ChainLock lock = LockUtils.getLock(userDomain.getPackLock());
		try {
			lock.lock();
			for (Entry<Long, Integer> entry : currentItemCounter.entrySet()) {
				Long propsId = entry.getKey();
				if(propsId == null) {
					continue;
				}
				
				UserProps userProps = propsManager.getUserProps(propsId);
				if(userProps == null) {
					continue;
				}
				
				Integer count = entry.getValue();
				count = count == null ? 0 : count;
				if(count == userProps.getCount()) {
					continue;
				}
				
				if(count == userProps.getCount()) {
					continue;
				}
				userProps.setCount(count);
				userPropList.add(userProps);
				
				if(!userPropList.isEmpty()) {
					propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, userPropsList);
					dbService.submitUpdate2Queue(userPropsList);
				}
			}
		} finally {
			lock.unlock();
		}
		
		return propsManager.listUserProps(playerId, backpack);
	}

	/**
	 * 构建背包Key
	 * 
	 * @param  baseId			基础ID
	 * @param  binding			绑定状态
	 * @param  expirate			效果时间
	 * @return {@link String}	背包相同的物品Key
	 */
	private String toBackpackKey (int baseId, boolean binding, Date expirate) {
		Long expirateTime = expirate == null ? 0 : DateUtil.toSecond(expirate.getTime());
		return new StringBuffer().append("BASEID_").append(baseId)
								.append("_BINDING_").append(binding)
								.append("_EXPIRATE_").append(expirateTime).toString();
	}
	
	/**
	 * 合并用户道具
	 * 
	 * @param  playerId					角色ID
	 * @param  addPropsId				数量会增加的道具ID
	 * @param  costPropsId				数量会减少的道具ID
	 * @return {@link Integer}			返回值
	 */
	
	public int mergeUserProps(long playerId, long addPropsId, long costPropsId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		UserProps addProps = propsManager.getUserProps(addPropsId);
		if(addProps == null) {
			return ITEM_NOT_FOUND;
		} else if(addProps.isTrading()) {
			return ITEM_CANNOT_USE;
		}
		
		int addBaseId = addProps.getBaseId();
		PropsConfig addPropsConfig = addProps.getPropsConfig();
		if(addPropsConfig == null) {
			return ITEM_NOT_FOUND;
		}
		
		boolean addBinding = addProps.isBinding();
		int maxAmount = addPropsConfig.getMaxAmount();
		Date addExpiration = addProps.getExpiration();
		if(!ArrayUtils.contains(MERGE_PROPS_BACKPACKS, addProps.getBackpack())) {
			return NOT_IN_BACKPACK;
		} else if(addProps.getCount() <= 0) {
			return ITEM_NOT_FOUND;
		} else if(addProps.getCount() >= maxAmount) {
			return ITEM_AMOUNT_FULLED;
		}
		
		UserProps costProps = propsManager.getUserProps(costPropsId);
		if(costProps == null) {
			return ITEM_NOT_FOUND;
		} else if(addProps.isTrading()) {
			return ITEM_CANNOT_USE;
		} else if(addProps.getBackpack() != costProps.getBackpack()) {
			return BACKPACK_INVALID;
		}

		int costBaseId = costProps.getBaseId();
		boolean costBinding = costProps.isBinding();
		PropsConfig costPropsConfig = costProps.getPropsConfig();
		if(costPropsConfig == null) {
			return ITEM_NOT_FOUND;
		}
		
		if(costBaseId != addBaseId || addBinding != costBinding) {
			return TYPE_INVALID;
		} else if(!costProps.isSameExpiration(addExpiration)) {
			return TYPE_INVALID;
		}
		
		int canCostCount = 0;
		int backpack = addProps.getBackpack();
		ChainLock lock = LockUtils.getLock(userDomain.getPackLock());
		try {
			lock.lock();
			int sourceCount = addProps.getCount();
			int targetCount = costProps.getCount();
			if(sourceCount <= 0 || targetCount <= 0) {
				return ITEM_NOT_ENOUGH;
			} else if(addProps.getPlayerId() != playerId) {
				return BELONGS_INVALID;
			} else if(costProps.getPlayerId() != playerId) {
				return BELONGS_INVALID;
			} else if(sourceCount >= maxAmount) {
				return ITEM_AMOUNT_FULLED;
			} else if(addProps.getBackpack() != costProps.getBackpack()) {
				return BACKPACK_INVALID;
			}
			
			int addCount = maxAmount - sourceCount;
			int canAddCount = Math.max(0, addCount);
			canCostCount = Math.min(canAddCount, targetCount);
			if(canAddCount <= 0 || canCostCount <= 0) {
				return FAILURE;
			}

			addProps.increaseItemCount(canCostCount);
			costProps.decreaseItemCount(canCostCount);
			if(addProps.getCount() > 0){
				propsManager.put2UserPropsIdsList(playerId, backpack, addProps);
			}
			propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, costProps);
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(addProps, costProps);
		GoodsLogger.goodsLogger(userDomain.getPlayer(), Source.PROPS_MERGE_USERPROPS, LoggerGoods.incomeProps(addProps.getBaseId(), canCostCount));
		GoodsLogger.goodsLogger(userDomain.getPlayer(), Source.PROPS_MERGE_USERPROPS, LoggerGoods.outcomeProps(costProps.getId(), costProps.getBaseId(), canCostCount));
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, addProps, costProps);
		return SUCCESS;
	}

	/**
	 * 拆分用户道具
	 * 
	 * @param  playerId					角色ID
	 * @param  userPropsId				需要拆分的用户道具ID
	 * @param  count					需要拆分的用户道具数量
	 * @return {@link ResultObject}		返回值对象信息	
	 */
	
	public int spliteUserProps(long playerId, long userPropsId, int count) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		} else if(count <= 0) {
			return INPUT_VALUE_INVALID;
		}
		
		
		Player player = userDomain.getPlayer();
		if(player == null) {
			return PLAYER_NOT_FOUND;
		}
		
		UserProps userProps = propsManager.getUserProps(userPropsId);
		if(userProps == null) {
			return ITEM_NOT_FOUND;
		} else if(userProps.getPlayerId() != playerId) {
			return BELONGS_INVALID;
		} else if(userProps.getCount() <= count) {
			return ITEM_NOT_ENOUGH;
		} else if(userProps.isTrading()) {
			return ITEM_CANNOT_USE;
		}

		int backpack = userProps.getBackpack();
		if(!ArrayUtils.contains(SPLIT_PROPS_BACKPACKS, backpack)) {
			return NOT_IN_BACKPACK;
		}
		
		int baseId = userProps.getBaseId();
		PropsConfig addPropsConfig = propsManager.getPropsConfig(baseId);
		if(addPropsConfig == null) {
			return ITEM_NOT_FOUND;
		}
		
		int addCount = 0;
		UserProps newUserProps = null;
		ChainLock lock = LockUtils.getLock(player.getPackLock());
		try {
			lock.lock();
			int currBackSize = propsManager.getBackpackSize(playerId, backpack);
			if(!player.canAddNew2Backpack(currBackSize + 1, backpack)) {
				return BACKPACK_FULLED;
			} else if(userProps.getCount() <= count) {
				return ITEM_NOT_ENOUGH;
			}
			
			addCount = count;
			userProps.decreaseItemCount(addCount);
			boolean binding = userProps.isBinding();
			Date expiration = userProps.getExpiration();
			newUserProps = UserProps.valueOf(playerId, baseId, count, backpack, expiration, binding);
			propsManager.spliteUserProps(newUserProps, userProps);
			
			propsManager.put2UserPropsIdsList(playerId, backpack, newUserProps);
			propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, userProps);
		} catch (Exception e) {
			if(newUserProps != null){
				userProps.increaseItemCount(addCount);
			}
			LOGGER.error("角色:[{}] 拆分道具异常:{}", playerId, e);
			return FAILURE;
		} finally {
			lock.unlock();
		}
		
		GoodsLogger.goodsLogger(userDomain.getPlayer(), Source.PROPS_SPLITE_USERPROPS, LoggerGoods.incomeProps(userProps.getBaseId(), addCount));
		GoodsLogger.goodsLogger(userDomain.getPlayer(), Source.PROPS_SPLITE_USERPROPS, LoggerGoods.outcomeProps(userProps.getId(), userProps.getBaseId(), addCount));
		
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, userProps, newUserProps);
		return SUCCESS;
	}

//	/**
//	 * 创建用户道具列表
//	 * 
//	 * @param userPropsList				用户道具列表
//	 */
//	
//	public Collection<UserProps> createUserPropsList(UserProps... userProps) {
//		Collection<UserProps> propsList = new ArrayList<UserProps>();
//		for (UserProps props : userProps) {
//			if(props != null) {
//				cachedService.saveEntityIntime(props);
//				removeUserPropsIdList(props.getPlayerId(), props.getBackpack());
//				propsList.add(props);
//			}
//		}
//		return propsList;
//	}

	/**
	 * 合成宝石道具. 因为策划需求, 所有的宝石合成都合成已绑定的
	 * 
	 * @param  playerId					角色ID
	 * @param  bindItems				绑定的用户道具
	 * @param  unBindItems				未绑定的用户道具
	 * @return {@link SynthStoneResult}	返回值信息对象		
	 */
	
	public SynthStoneResult synthStoneItem(long playerId, String bindItems, String unBindItems) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return SynthStoneResult.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerBattle playerBattle = userDomain.getBattle();
		if(playerBattle.isDead()) {
			return SynthStoneResult.ERROR(PLAYER_DEADED);
		}
		
		SynthObject synthObject = this.checkSynthItem(playerId, bindItems, unBindItems);
		int result = synthObject.getResult();
		int baseItemId = synthObject.getBaseItemId();
		int totalCount = synthObject.getTotalCount();
		
		Map<Long, Integer> bindingItems = synthObject.getBindingItems();
		if(result != SUCCESS) {
			return SynthStoneResult.ERROR(result);
		}
		
		//总输入数量为0, 或者总数和合成数量相与不为零, 表示不合法
		if(totalCount <= 0 || totalCount % PropsRule.SYNTH_STONE_COUNT != 0) {
			return SynthStoneResult.ERROR(INPUT_VALUE_INVALID);
		}
		
		//没有输入任何一个道具来合成, 所以不能合成
		if(bindingItems.isEmpty()) {
			return SynthStoneResult.ERROR(INPUT_VALUE_INVALID);
		}
		
		//基础道具
		PropsConfig propsConfig = propsManager.getPropsConfig(baseItemId);
		if(propsConfig == null) {
			return SynthStoneResult.ERROR(ITEM_NOT_FOUND);
		} 

		PropsSynthConfig propsSynth = propsManager.getPropsSynthConfig(baseItemId);
		if(propsSynth == null) {
			return SynthStoneResult.ERROR(TYPE_INVALID);
		}
		
		//道具类型
		PropsConfig nextProps = propsManager.getPropsConfig(propsSynth.getNextId());
		if(nextProps == null) {
			return SynthStoneResult.ERROR(ITEM_NOT_FOUND);
		} 
		
		int nextPropsId = nextProps.getId();												//下一等级的道具ID
		int backpack = BackpackType.DEFAULT_BACKPACK;										//背包号
		int canSynthCount = totalCount / PropsRule.SYNTH_STONE_COUNT;						//总共可以合成的宝石数量
		int needSilver = propsSynth.getTotalSynthSilver(canSynthCount);						//需要的游戏币
		SynthResult synthResult = this.checkSynthResult(synthObject, propsSynth);			//合成结果
		int newBindingCount = synthResult.getNewBindingCount();								//可以合成新的绑定道具数量
		PropsStackResult bindingResult = PropsHelper.calcPropsStack(playerId, backpack, nextPropsId, newBindingCount, true);
		
		int rollbackSilver = 0;
		List<LoggerGoods> loggerGoodsList = new ArrayList<LoggerGoods>();
		Player player = userDomain.getPlayer();
		List<UserProps> totalNews = bindingResult.getNewUserProps();
		Map<Long, Integer> totalUpdates = bindingResult.getMergeProps();
		Map<Long, Integer> costUserItems = synthResult.getCostUserItems();
		int currBackSize = propsManager.getBackpackSize(playerId, backpack);			//当前已用背包格子数
		ChainLock lock = LockUtils.getLock(player.getPackLock());
		try {
			lock.lock();
			if(player.getSilver() < needSilver) {
				return SynthStoneResult.ERROR(SILVER_NOT_ENOUGH);
			} else if(!player.canAddNew2Backpack(totalNews.size() + currBackSize, backpack)) {
				return SynthStoneResult.ERROR(BACKPACK_FULLED);
			}
			
			totalNews = propsManager.createUserProps(totalNews);
			propsManager.put2UserPropsIdsList(playerId, backpack, totalNews);
			
			loggerGoodsList.addAll( LoggerGoods.incomeProps(totalNews) );
			
			rollbackSilver = needSilver;
			player.decreaseSilver(rollbackSilver);
		} catch (Exception e) {
			player.increaseSilver(rollbackSilver);
			return SynthStoneResult.ERROR(FAILURE);
		} finally {
			lock.unlock();
		}
		
		if(synthResult.getSuccessCount() > 0) {
//          发公告, 超平增加 ---- 2012-6-7
			BulletinConfig bulletinConfig = resourceService.get(NoticeID.PROPS_SYNTH_STONE, BulletinConfig.class);
			if (bulletinConfig != null) {
				Set<Integer> conditionSet = bulletinConfig.getConditions();
				if (conditionSet.contains(nextPropsId)) {
					Map<String, Object> paramsMap = new HashMap<String, Object>(3);
					paramsMap.put(NoticeRule.playerId, playerId);
					paramsMap.put(NoticeRule.playerName, player.getName());
					paramsMap.put(NoticeRule.props, nextProps.getName());
					NoticePushHelper.pushNotice(NoticeID.PROPS_SYNTH_STONE, NoticeType.HONOR, paramsMap, bulletinConfig.getPriority());
				}
			}
		}
		
		dbService.submitUpdate2Queue(player);
		Collection<UserProps> totals = new ArrayList<UserProps>(totalNews);
		
		List<UserProps> updateUserPropsList = propsManager.updateUserPropsList(totalUpdates);
		totals.addAll(updateUserPropsList);
		loggerGoodsList.addAll( LoggerGoods.updateProps(totalUpdates, updateUserPropsList) );
		
		List<UserProps> costUserPropsList = propsManager.costUserPropsList(costUserItems);
		totals.addAll(costUserPropsList);
		loggerGoodsList.addAll( LoggerGoods.loggerProps(Orient.OUTCOME, costUserItems, costUserPropsList) );
		
		LoggerGoods[] loggerGoods = loggerGoodsList.toArray( new LoggerGoods[loggerGoodsList.size()] );
		SilverLogger.outCome(Source.PROPS_SYNTH_STONEITEM, rollbackSilver, player, loggerGoods);
		GoodsLogger.goodsLogger(player, Source.PROPS_SYNTH_STONEITEM, loggerGoods);
		
		achieveFacade.firstAchieved(playerId, FIRST_ACHIEVE, FIRST_SYNTH);             // 第一次合成物品成就
		return SynthStoneResult.SUCCESS(synthResult.getSuccessCount(), synthResult.getFailureCount(), totals);
	}
	
//	/**
//	 * 合成宝石道具
//	 * 
//	 * @param  playerId					角色ID
//	 * @param  bindItems				绑定的用户道具
//	 * @param  unBindItems				未绑定的用户道具
//	 * @return {@link SynthStoneResult}	返回值信息对象		
//	 */
//	
//	public SynthStoneResult synthStoneItem(long playerId, String bindItems, String unBindItems) {
//		UserDomain userDomain = userManager.getUserDomain(playerId);
//		if(userDomain == null) {
//			return SynthStoneResult.ERROR(PLAYER_NOT_FOUND);
//		}
//		
//		PlayerBattle playerBattle = userDomain.getBattle();
//		if(playerBattle.isDeath()) {
//			return SynthStoneResult.ERROR(PLAYER_DEADED);
//		}
//		
//		SynthObject synthObject = this.checkSynthItem(playerId, bindItems, unBindItems);
//		int result = synthObject.getResult();
//		int baseItemId = synthObject.getBaseItemId();
//		int totalCount = synthObject.getTotalCount();
//		Map<Long, Integer> bindingItems = synthObject.getBindingItems();
//		Map<Long, Integer> unBindingItems = synthObject.getUnbindingItems();
//		if(result != SUCCESS) {
//			return SynthStoneResult.ERROR(result);
//		}
//		
//		//总输入数量为0, 或者总数和合成数量相与不为零, 表示不合法
//		if(totalCount <= 0 || totalCount % PropsRule.SYNTH_STONE_COUNT != 0) {
//			return SynthStoneResult.ERROR(INPUT_VALUE_INVALID);
//		}
//		
//		//没有输入任何一个道具来合成, 所以不能合成
//		if(bindingItems.isEmpty() && unBindingItems.isEmpty()) {
//			return SynthStoneResult.ERROR(INPUT_VALUE_INVALID);
//		}
//		
//		//基础道具
//		PropsConfig propsConfig = propsManager.getPropsConfig(baseItemId);
//		if(propsConfig == null) {
//			return SynthStoneResult.ERROR(ITEM_NOT_FOUND);
//		} 
//		
//		PropsSynthConfig propsSynth = propsManager.getPropsSynthConfig(baseItemId);
//		if(propsSynth == null) {
//			return SynthStoneResult.ERROR(TYPE_INVALID);
//		}
//		
//		//道具类型
//		PropsConfig nextProps = propsManager.getPropsConfig(propsSynth.getNextId());
//		if(nextProps == null) {
//			return SynthStoneResult.ERROR(ITEM_NOT_FOUND);
//		} 
//		
//		int nextPropsId = nextProps.getId();												//下一等级的道具ID
//		int backpack = BackpackType.DEFAULT_BACKPACK;										//背包号
//		int canSynthCount = totalCount / PropsRule.SYNTH_STONE_COUNT;						//总共可以合成的宝石数量
//		int needSilver = propsSynth.getTotalSynthSilver(canSynthCount);						//需要的游戏币
//		SynthResult synthResult = this.checkSynthResult(synthObject, propsSynth);			//合成结果
//		
//		int newBindingCount = synthResult.getNewBindingCount();								//可以合成新的绑定道具数量
//		int newUnBindingCount = synthResult.getNewUnBindingCount();							//可以合成新的未绑定的道具数量
//		PropsStackResult bindingResult = PropsHelper.calcPropsStack(playerId, backpack, nextPropsId, newBindingCount, true);
//		PropsStackResult unBindingResult = PropsHelper.calcPropsStack(playerId, backpack, nextPropsId, newUnBindingCount, false);
//		
//		int rollbackSilver = 0;
//		Player player = userDomain.getPlayer();
//		List<UserProps> totalNews = new ArrayList<UserProps>();
//		Map<Long, Integer> totalUpdates = new HashMap<Long, Integer>();
//		Map<Long, Integer> costUserItems = synthResult.getCostUserItems();
//		totalUpdates.putAll(bindingResult.getMergeProps());
//		totalUpdates.putAll(unBindingResult.getMergeProps());
//		
//		totalNews.addAll(bindingResult.getNewUserProps());
//		totalNews.addAll(unBindingResult.getNewUserProps());
//		int currBackSize = propsManager.getBackpackSize(playerId, backpack);			//当前已用背包格子数
//		ChainLock lock = LockUtils.getLock(player.getPackLock());
//		try {
//			lock.lock();
//			if(player.getSilver() < needSilver) {
//				return SynthStoneResult.ERROR(GAS_NOT_ENOUGH);
//			} else if(!player.canAddNew2Backpack(totalNews.size() + currBackSize, backpack)) {
//				return SynthStoneResult.ERROR(BACKPACK_FULLED);
//			}
//			
//			totalNews = propsManager.createUserProps(totalNews);
//			propsManager.put2UserPropsIdsList(playerId, backpack, totalNews);
//			
//			rollbackSilver = needSilver;
//			player.decreaseSilver(rollbackSilver);
//		} catch (Exception e) {
//			player.increaseSilver(rollbackSilver);
//			return SynthStoneResult.ERROR(FAILURE);
//		} finally {
//			lock.unlock();
//		}
//		
//		if(synthResult.getSuccessCount() > 0) {
////			if (nextProps.getChildType() == PropsChildType.EQUIP_ASCENT_STAR_TYPE){
////				List<PropsConfig> list = resourceService.listByIndex(IndexName.PROPS_CHILDTYPE, PropsConfig.class, PropsChildType.EQUIP_ASCENT_STAR_TYPE);
////				int index = list.indexOf(nextProps);
////				if(index > 6){
////					Map<String, Object> paramsMap = new HashMap<String, Object>(2);
////					paramsMap.put(ResponseKey.PLAYER_NAME, player.getName());
////					paramsMap.put(ResponseKey.PROPS_ID, nextProps.getName());
////					NoticePushHelper.pushNotice(NoticeID.PROPS_SYNTH_7, NoticeType.HONOR, paramsMap);
////				}
////			}
//			
////          发公告, 超平增加 ---- 2012-6-7
//			BulletinConfig bulletinConfig = resourceService.get(NoticeID.PROPS_SYNTH_STONE, BulletinConfig.class);
//			if (bulletinConfig != null) {
//				Set<Integer> conditionSet = bulletinConfig.getConditions();
//				if (conditionSet.contains(nextPropsId)) {
//					Map<String, Object> paramsMap = new HashMap<String, Object>(2);
//					paramsMap.put(NoticeRule.playerName, player.getName());
//					paramsMap.put(NoticeRule.props, nextProps.getName());
//					NoticePushHelper.pushNotice(NoticeID.PROPS_SYNTH_STONE, NoticeType.HONOR, paramsMap);
//				}
//			}
//		}
//		
//		dbService.submitUpdate2Queue(player);
//		Collection<UserProps> totals = new ArrayList<UserProps>(totalNews);
//		totals.addAll(propsManager.updateUserPropsList(totalUpdates));
//		totals.addAll(propsManager.costUserPropsList(costUserItems));
////		GoodsLogger.goodsLogger(player, Source.PROPS_SYNTH_STONEITEM, LoggerGoods.incomeProps(baseId, count));
//		return SynthStoneResult.SUCCESS(synthResult.getSuccessCount(), synthResult.getFailureCount(), totals);
//	}
	
 
	/**
	 * 合成舍利子
	 * 
	 * @param  playerId					角色ID
	 * @param  userPropsId				用户道具ID(舍利子, 主材料)
	 * @param  targetPropsId			用户道具ID(舍利子, 辅材料)
	 * @param  userItems				聚灵珠用户道具. 格式: 用户道具ID_数量|...
	 * @param  luckItems				幸运石用户道具. 格式: 用户道具ID_数量|...
	 * @param  autoBuyCount				自动购买聚灵珠的数量
	 * @return {@link ResultObject}		返回值对象
	 */
	
	public ResultObject<Collection<UserProps>> synthSharipuDiverse(long playerId, long userPropsId,
						long targetPropsId, String userItems, String luckItems, int autoBuyCount) {
		return ResultObject.ERROR(FAILURE);
	}

	/**
	 * 合成舍利子
	 * 
	 * @param  playerId					角色ID
	 * @param  userPropsId				用户道具ID(舍利子, 主材料)
	 * @param  userItems				聚灵珠用户道具. 格式: 用户道具ID_数量|...
	 * @param  luckItems				幸运石用户道具. 格式: 用户道具ID_数量|...
	 * @param  autoBuyCount				自动购买聚灵珠的数量
	 * @return {@link ResultObject}		返回值对象
	 */
	
	public ResultObject<Collection<UserProps>> synthSharipuSameItem(long playerId, 
			long userPropsId, String userItems, String luckItems, int autoBuyCount) {
 		return ResultObject.ERROR(FAILURE);
	}
	
	/**
	 * 装备升星(装备强化)
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquipId				用户装备ID
	 * @param  userProps				金刚石用户道具.. 格式: 用户道具ID_数量|...
	 * @param  luckyUserItems			幸运石用户道具.. 格式: 用户道具ID_数量|...
	 * @param  userPropsId				用户保护符道具ID
	 * @param  autoBuyCount				自动购买金刚石的数量
	 * @return {@link ResultObject}		返回值信息
	 */
	@SuppressWarnings("unchecked")
	
	public ResultObject<Collection<BackpackEntry>> ascentEquipStar(long playerId, long userEquipId, 
					String userProps, String luckyUserItems, long userPropsId, int autoBuyCount) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		if(player == null || battle == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		} else if(battle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		} else if(autoBuyCount < 0) {
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if(userEquip == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if(!userEquip.validBackpack(CAN_FORGE_BACKPACK)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userEquip.getCount() <= 0) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if(userEquip.isTrading()) {
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		}
		
		int equipBackpack = userEquip.getBackpack();
		EquipConfig equipConfig = userEquip.getEquipConfig();
		if(equipConfig == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if(!equipConfig.canForge()) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int starLevel = userEquip.getStarLevel();
		EquipStarConfig starConfig = propsManager.getEquipStarConfig(starLevel);
		if(starConfig == null) {
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}

		//金刚石的子类型
		int itemId = starConfig.getItemId();
		SynthObject userItemObject = this.checkUserItemCountByItemId(playerId, userProps, itemId);
		if(userItemObject.getResult() < SUCCESS) {
			return ResultObject.ERROR(userItemObject.getResult());
		}
		
		int needPropsCount = starConfig.getItemCount();		//需要的道具数量
		int diamonCount = userItemObject.getBindingCount();	//当前已使用的宝石数量
		PropsConfig propsConfig = propsManager.getPropsConfig(itemId);
		if(propsConfig == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(autoBuyCount + diamonCount != needPropsCount) {
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		}
		
		//要么你不传, 要传你就传正确的
		SynthObject luckyResult = this.checkUserItemCount(playerId, luckyUserItems, PropsChildType.LUCKY_PROPS_TYPE);
		if(luckyResult.getResult() != SUCCESS) {
			return ResultObject.ERROR(luckyResult.getResult());
		}
		
		//使用超过最大幸运晶的使用数量
		int luckyCount = luckyResult.getBindingCount();
		if(luckyCount > PropsRule.ASCENT_EQUIP_STARLEVEL) {
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		//装备强化保护道具类型
		int childType = PropsChildType.EQUIP_STAR_SAFE_TYPE;	
		ResultObject<UserProps> safeResultObject = checkAscentStarSafeProps(playerId, userPropsId, childType);
		if(safeResultObject.getResult() < SUCCESS) {
			return ResultObject.ERROR(safeResultObject.getResult());
		}
		
		boolean isAscentStarSuccess = false;
		boolean needPersistUserEquip = false;
		double silverRate = starConfig.getSilver();				//需要的游戏币数量
		int baseSilver = equipConfig.getSilverPrice();			//装备基础价格
		UserProps safeUserProps = safeResultObject.getValue();	//保护道具对象
		boolean isSafeEquipStartLevel = safeUserProps != null;
		Fightable beforeAttribute = battle.getAndCopyAttributes();
		int costGold = PropsRule.getAscentStarBuyCostGold(autoBuyCount);
		int costSilver = FormulaHelper.invoke(ASCENT_STAR_COSTSILVER, baseSilver, silverRate).intValue();
		ChainLock lock = LockUtils.getLock(player, player.getPackLock());
		try {
			lock.lock();
			starLevel = userEquip.getStarLevel();
			//	if(starLevel >= PropsRule.MAX_EQUIP_STAR_LEVEL) {
			//		return ResultObject.ERROR(LEVEL_INVALID);
			//	}
			
			starConfig = propsManager.getEquipStarConfig(starLevel);
			if(starConfig == null) {
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			}
			
			int baseItemId = starConfig.getItemId();		//需要的基础道具ID
			needPropsCount = starConfig.getItemCount();		//需要的道具数量
			diamonCount = userItemObject.getBindingCount();	//当前已使用的宝石数量
			if(propsConfig.getId() != baseItemId) {
				return ResultObject.ERROR(LEVEL_INVALID);
			} else if(autoBuyCount + diamonCount != needPropsCount) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			}
			
			if(player.getSilver() < costSilver) {
				return ResultObject.ERROR(SILVER_NOT_ENOUGH);
			} else if(player.getGolden() < costGold) {
				return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
			}
			
			if(safeUserProps != null) {
				if(safeUserProps.getCount() <= 0) {
					return ResultObject.ERROR(ITEM_NOT_ENOUGH);
				}
				safeUserProps.decreaseItemCount(1);
			}
			
			isAscentStarSuccess = starConfig.isAscentStarSuccess(luckyCount);   
			if(isAscentStarSuccess) {									//是否升星成功
				needPersistUserEquip = true;
				userEquip.increaseStarLevel(1);
			} else if(!isSafeEquipStartLevel) {
				needPersistUserEquip = true;
				userEquip.setStarLevel(starConfig.getFailureStar());	//升星失败后, 将回多少星
			}
			
			userEquip.setBinding(true);
			player.decreaseGolden(costGold);
			player.decreaseSilver(costSilver);
			if(needPersistUserEquip) {
				EquipHelper.refreshEquipStarAttributes(userEquip);
			}
			
			if(needPersistUserEquip) {
				if(safeUserProps != null) {
					dbService.submitUpdate2Queue(player, userEquip, safeUserProps);
					propsManager.removeUserPropsIfCountNotEnough(safeUserProps);
				} else {
					dbService.submitUpdate2Queue(player, userEquip);
				}
			} else {
				if(safeUserProps != null) {
					dbService.submitUpdate2Queue(player, safeUserProps);
					propsManager.removeUserPropsIfCountNotEnough(safeUserProps);
				} else {
					dbService.submitUpdate2Queue(player);
				}
			}
		} finally {
			lock.unlock();
		}
		
		int newEquipLevel = userEquip.getStarLevel();
		taskEntityFacade.updateEquipAscentStarTask(playerId, newEquipLevel);
		Collection<BackpackEntry> backpackEntries = new HashSet<BackpackEntry>();
		backpackEntries.add(voFactory.getUserEquipEntry(userEquip));
		if(newEquipLevel >= 7 && isAscentStarSuccess) {
			BulletinConfig config = resourceService.get(NoticeID.EQUIP_STAR_UP, BulletinConfig.class);
			if (config != null) {
				Map<String, Object> noticeMap = new HashMap<String, Object>(4);
				noticeMap.put(NoticeRule.playerName, player.getName());
				noticeMap.put(NoticeRule.equipId, userEquip.getEquipConfig().getName());
				noticeMap.put(NoticeRule.level, newEquipLevel);
				noticeMap.put(NoticeRule.playerId, playerId);
				NoticePushHelper.pushNotice(NoticeID.EQUIP_STAR_UP, NoticeType.HONOR, noticeMap, config.getPriority());
			}
		} 
		
		Map<Long, Integer> updatePropsMap = new HashMap<Long, Integer>();
		updatePropsMap.putAll(luckyResult.getBindingItems());
		updatePropsMap.putAll(userItemObject.getBindingItems());
		List<UserProps> diamonPropsList = propsManager.costUserPropsList(updatePropsMap);
		backpackEntries.addAll(voFactory.getUserPropsEntries(diamonPropsList));
		if(safeUserProps != null) {
			backpackEntries.add(voFactory.getUserPropsEntry(safeUserProps));
		}
		
		List<LoggerGoods> goodsLogList = new ArrayList<LoggerGoods>(diamonPropsList.size());
		for(UserProps props : diamonPropsList){
			Long costUserPropsId = props.getId();
			int baseId = props.getBaseId();
			Integer count = updatePropsMap.get(costUserPropsId);
			if(count != null && count != 0) {
				goodsLogList.add(LoggerGoods.outcomeProps(costUserPropsId, baseId, count));
			}
		}
		
		if(autoBuyCount > 0) {
			goodsLogList.add(LoggerGoods.outcomePropsAutoBuyGolden(itemId, autoBuyCount, costGold));
		}
		
		LoggerGoods[] goodsLoggerArray = goodsLogList.toArray(new LoggerGoods[goodsLogList.size()]);
		if(goodsLoggerArray.length > 0) {
			GoodsLogger.goodsLogger(player, Source.PROPS_ASCENT_EQUIPSTAR, goodsLoggerArray);
		}
		if(costSilver != 0){
			SilverLogger.outCome(Source.PROPS_ASCENT_EQUIPSTAR, costSilver, player, goodsLoggerArray);
		}
		if(costGold != 0) {
			GoldLogger.outCome(Source.PROPS_ASCENT_EQUIPSTAR, costGold, player, goodsLoggerArray);
		}
		
		this.forgePlayerFlushable(userDomain, beforeAttribute, equipBackpack, true);
		achieveFacade.firstAchieved(playerId, FIRST_ACHIEVE, FIRST_EQUIP_STAR_UP);
		return ResultObject.SUCCESS(backpackEntries);
	}
	
	/**
	 * 打造刷新角色属性
	 * 
	 * @param userDomain	角色域模型
	 * @param beforable		计算之前的属性
	 * @param backpack		装备的背包号
	 */
	private void forgePlayerFlushable(UserDomain userDomain, Fightable beforable, int backpack, boolean pushMySelf) {
		try {
			long playerId = userDomain.getPlayerId();
			GameMap gameMap = userDomain.getGameMap();
			List<UnitId> unitIds = Arrays.asList(userDomain.getUnitId());
			if(ArrayUtils.contains(FLUSHABLE_BACKPACK, backpack)) { //需要刷新的背包号
				userDomain.updateFlushable(true, Flushable.FLUSHABLE_NORMAL);
				UserPushHelper.pushPlayerAttributeChange(beforable, userDomain);
				Set<Long> playerIds = gameMap.getAllSpireIdCollection(ElementType.PLAYER);
				playerIds.remove(playerId);
				UserPushHelper.pushAttribute2AreaMember(playerId, playerIds, unitIds, AttributeRule.AREA_MEMBER_VIEWS_PARAMS);
				UserPushHelper.pushAttribute2AreaMember(playerId, Arrays.asList(playerId), unitIds, AttributeRule.PUSH_MONEY_AND_HPMP_AREA);
			} else {
				if(pushMySelf) {
					UserPushHelper.pushAttribute2AreaMember(playerId, Arrays.asList(playerId), unitIds, AttributeKeys.SILVER, AttributeKeys.GOLDEN);
				}
			}
		} catch (Exception e) {
			LOGGER.error("{}", e);
		}
	}
	
	/**
	 * 检测装备升星保护符类型
	 * 
	 * @param  playerId				角色ID
	 * @param  userPropsId			用户道具ID
	 * @param  childType			用户道具的子类型
	 * @return {@link ResultObject}	返回值对象
 	 */
	private ResultObject<UserProps> checkAscentStarSafeProps(long playerId, long userPropsId, int childType) {
		if(userPropsId <= 0L) {
			return ResultObject.SUCCESS();
		}
		
		UserProps userSafeProps = propsManager.getUserProps(userPropsId);
		if(userSafeProps == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(!userSafeProps.validBackpack(DEFAULT_BACKPACK)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userSafeProps.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userSafeProps.getCount() <= 0) {
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		} else if(userSafeProps.isOutOfExpiration()) {
			return ResultObject.ERROR(OUT_OF_EXPIRATION);
		} else if(userSafeProps.isTrading()) {
			return ResultObject.ERROR(ITEM_CANNOT_USE);
		}
		
		int baseId = userSafeProps.getBaseId();
		PropsConfig propsConfig = propsManager.getPropsConfig(baseId);
		if(propsConfig == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(propsConfig.getChildType() != childType) {
			return ResultObject.ERROR(TYPE_INVALID);
		}
		return ResultObject.SUCCESS(userSafeProps);
	}
	
	/**
	 * 装备升阶. 
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquipId				用户装备ID
	 * @param  userProps				升阶石信息. 	    格式: 用户道具ID_用户道具数量|..
	 * @param  safeUserProps			保护符数量. 	    格式: 用户道具ID_用户道具数量|..
	 * @param  luckyUserProps			幸运晶道具信息. 格式: 用户道具ID_用户道具数量|..
	 * @param  autoBuyCount				自动购买升阶石的数量
	 * @return {@link ResultObject}		返回值信息
	 */
	@SuppressWarnings("unchecked")
	
	public ResultObject<Collection<BackpackEntry>> ascentEquipRank(long playerId, long userEquipId, 
				String userProps, String safeUserProps, String luckyUserProps, int autoBuyCount) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		if(battle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		} else if(autoBuyCount < 0) {
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		int backpack = BackpackType.DEFAULT_BACKPACK;
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if(userEquip == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if(!userEquip.validBackpack(backpack)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userEquip.getCount() <= 0) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if(userEquip.isTrading()) {
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		}
		
		int oldIndex = userEquip.getIndex();
		int equipId = userEquip.getBaseId();
		Quality oldQuality = userEquip.getQuality();
		EquipConfig currRankEquip = userEquip.getEquipConfig();
		if(currRankEquip == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if(!currRankEquip.canForge()) {
			return ResultObject.ERROR(FAILURE);
		} else if(currRankEquip.getLevel() < 40) {
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		}

		//验证是否还有下一阶装备
		EquipRankConfig equipRank = propsManager.getEquipRankConfig(equipId);
		if(equipRank == null) {
			return ResultObject.ERROR(LEVEL_INVALID);
		}
		
		//下一级的装备显示ID
		EquipConfig nextRankEquip = equipRank.getNextEquip();
		if(nextRankEquip == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		}
		
		//需要的道具ID
		int itemId = equipRank.getItemId();
		PropsConfig propsConfig = propsManager.getPropsConfig(itemId);
		if(propsConfig == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		}
		
		//升阶石的道具子类型
		SynthObject rankItemObject = this.checkUserItemCountByItemId(playerId, userProps, itemId);
		if(rankItemObject.getResult() < SUCCESS) {
			return ResultObject.ERROR(rankItemObject.getResult());
		}

		//需要升阶石的数量
		int needRankItemCount = equipRank.getItemCount();
		int rankItemCount = rankItemObject.getBindingCount();
		if(rankItemCount + autoBuyCount != needRankItemCount) {
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		//保护水晶的子类型
		int safePropsType = PropsChildType.EQUIP_RANK_SAFE_TYPE;
		SynthObject safePropsObject = this.checkUserItemCount(playerId, safeUserProps, safePropsType);
		if(safePropsObject.getResult() != SUCCESS) {
			return ResultObject.ERROR(safePropsObject.getResult());
		} else if(safePropsObject.getBindingCount() > PropsRule.RANK_SAFE_STAR_COUNT) {
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		//保护水晶的子类型. TODO 这里等下要看
		int safeAttrType = PropsChildType.LUCKY_PROPS_TYPE;
		SynthObject luckyObject = this.checkUserItemCount(playerId, luckyUserProps, safeAttrType);
		if(luckyObject.getResult() != SUCCESS) {
			return ResultObject.ERROR(luckyObject.getResult());
		} 

		int bindingCount = luckyObject.getBindingCount();
		if(bindingCount > PropsRule.MAX_LUCKY_ITEM_COUNT) {
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		boolean isAscentSuccess = false;
		double costSilverRate = equipRank.getSilver();							//装备游戏币价格概率
		int baseSilver = currRankEquip.getSilverPrice();						//当前装备的价格
		int costGolden = propsConfig.getMallPriceByCount(autoBuyCount);			//自动购买消耗的金币数量
		boolean hasSafeItems = !safePropsObject.getBindingItems().isEmpty();	//是否有升星石
		int costSilver = FormulaHelper.invoke(EQUIP_ASCENT_RANK_COSTSILVER, baseSilver, costSilverRate).intValue();
		ChainLock lock = LockUtils.getLock(player, player.getPackLock());
		try {
			lock.lock();
			if(player.getSilver() < costSilver) {
				return ResultObject.ERROR(SILVER_NOT_ENOUGH);
			} else if(player.getGolden() < costGolden) {
				return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
			} else if(userEquip.getBaseId() == nextRankEquip.getId()) {
				return ResultObject.ERROR(FAILURE);
			}
			
			userEquip.setBinding(true);
			player.decreaseGolden(costGolden);
			player.decreaseSilver(costSilver);
			isAscentSuccess = EquipHelper.refreshEquipRankAttributes(userEquip, equipRank, hasSafeItems, bindingCount);
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(player, userEquip);
		Map<Long, Integer> totalCostPropsMap = new HashMap<Long, Integer>(3);
		totalCostPropsMap.putAll(luckyObject.getBindingItems());
		totalCostPropsMap.putAll(rankItemObject.getBindingItems());
		totalCostPropsMap.putAll(safePropsObject.getBindingItems());
		
		Collection<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		List<UserProps> costUserPropsList = propsManager.costUserPropsList(totalCostPropsMap);
		backpackEntries.add(BackpackEntry.valueEquipEmpty(userEquipId, equipId, backpack, oldQuality, oldIndex, userEquip.isBinding()));
		backpackEntries.add(userEquip);
		if(costUserPropsList != null && !costUserPropsList.isEmpty()) {
			propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, costUserPropsList);
			backpackEntries.addAll(costUserPropsList);
		}
		
		LoggerGoods[] loggerGoodsArray = LoggerPropsHelper.convertLoggerGoods(Orient.OUTCOME, 
				null, null, totalCostPropsMap, costUserPropsList, autoBuyCount, itemId, costGolden);
		GoodsLogger.goodsLogger(player, Source.PROPS_ASCENT_EQUIPRANK, loggerGoodsArray);
		if(costSilver != 0) {
			SilverLogger.outCome(Source.PROPS_ASCENT_EQUIPRANK, costSilver, player, loggerGoodsArray);
		}
		if(costGolden != 0){
			GoldLogger.outCome(Source.PROPS_ASCENT_EQUIPRANK, costGolden, player, loggerGoodsArray);
		}
		return ResultObject.valueOf(isAscentSuccess ? SUCCESS : ASCENT_RANK_FAILURE, backpackEntries);
	}

	/**
	 * 装备镶嵌
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquipId				用户装备ID
	 * @param  enchangeInfos			镶嵌下标1_镶嵌的用户道具ID1|镶嵌下标2_镶嵌的用户道具ID2|
	 * @return {@link ResultObject}		返回值信息
	 */
	@SuppressWarnings("unchecked")
	
	public ResultObject<Collection<BackpackEntry>> enchangeEquip(long playerId, long userEquipId, String enchangeInfos) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		if(player == null || battle == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		} else if(battle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		}
		
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if(userEquip == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if(!userEquip.validBackpack(CAN_FORGE_BACKPACK)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userEquip.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userEquip.isTrading()) {
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		}
		
		EquipConfig equipConfig = userEquip.getEquipConfig();
		if(equipConfig == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if(!equipConfig.canForge()) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int type = PropsType.ENCHANGE_PROPS_TYPE;
		EquipEnchangeInfo enchangeResult = checkEnchanges(userEquip, equipConfig, enchangeInfos, type);
		if(enchangeResult.getResult() != SUCCESS) {
			return ResultObject.ERROR(enchangeResult.getResult());
		}
		
		int maxHole = equipConfig.getMaxHole();
		int silver = enchangeResult.getSilver();
		int equipBackpack = userEquip.getBackpack();
		Fightable beforable = battle.getAndCopyAttributes();
		ChainLock lock = LockUtils.getLock(player, player.getPackLock());
		try {
			lock.lock();
			if(player.getSilver() < silver) {
				return ResultObject.ERROR(SILVER_NOT_ENOUGH);
			}
			
			Map<Integer, Integer> enchangeHoles = enchangeResult.getEnchangeHoles();
			Map<Integer, HoleInfo> holeInfos = userEquip.getHoleInfos();
			for (Integer index : enchangeHoles.keySet()) {	//先检测是否已经镶嵌了道具, 如果镶嵌了, 则直接返回错误码
				HoleInfo holeInfo = holeInfos.get(index);
				if(holeInfo == null || holeInfo.getItemId() > 0) {
					return ResultObject.ERROR(FAILURE);
				}
			}
			
			for (Entry<Integer, Integer> entry : enchangeHoles.entrySet()) {
				Integer index = entry.getKey();
				Integer itemId = entry.getValue();
				HoleInfo holeInfo = holeInfos.get(index);
				if(holeInfo != null) {
					holeInfo.setItemId(itemId);
				}
			}

			userEquip.setBinding(true);
			player.decreaseSilver(silver);
			EquipHelper.refreshHoleIndex(userEquip, maxHole);
			dbService.submitUpdate2Queue(player, userEquip);
		} finally {
			lock.unlock();
		}
		
		Collection<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		backpackEntries.add(voFactory.getUserEquipEntry(userEquip));
		Map<Long, Integer> enchangeItems = enchangeResult.getEnchangeItems();
		List<UserProps> costUserPropsList = propsManager.costUserPropsList(enchangeItems);
		if(costUserPropsList != null && !costUserPropsList.isEmpty()) {
			backpackEntries.addAll(voFactory.getUserPropsEntries(costUserPropsList));
		}
		
		LoggerGoods[] loggerGoods = LoggerPropsHelper.convertLoggerGoods(Orient.OUTCOME, null, null, enchangeItems, costUserPropsList);
		GoodsLogger.goodsLogger(player, Source.PROPS_ENCHANGE_EQUIP, loggerGoods);
		if(silver != 0) {
			SilverLogger.outCome(Source.PROPS_ENCHANGE_EQUIP, silver, player, loggerGoods);
		}

		forgePlayerFlushable(userDomain, beforable, equipBackpack, true);
		taskFacade.updateEnchanceEquipTask(playerId);
		achieveFacade.firstAchieved(playerId, FIRST_ACHIEVE, FIRST_EQUIP_ENCHANGE);
		return ResultObject.SUCCESS(backpackEntries);
	}
	
	/**
	 * 洗练角色装备的属性值
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquipId				用户装备ID
	 * @param  userProps				精炼石道具ID信息.(格式: 用户道具ID_使用数量|...)
	 * @param  autoBuyCount				自动购买的数量
	 * @param  safeIndex				保护的下标集合(保护下标_保护下标...)
	 * @param  lockProps				洗练锁道具信息(格式: 用户道具ID1_使用数量1|...)
	 * @return {@link ResultObject}		返回值对象
	 */
	
	public ResultObject<Collection<BackpackEntry>> polishedEquipAdditions(long playerId, long userEquipId, 
									String userProps, int autoBuyCount, String safeIndex, String lockProps) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		if(battle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		} else if(autoBuyCount < 0) {
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if(userEquip == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		} else if(!userEquip.validBackpack(CAN_FORGE_BACKPACK)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userEquip.getCount() <= 0) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userEquip.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userEquip.isTrading()) {
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		} else if(userEquip.getQuality() == Quality.WHITE) {
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		}
		
		//附加属性的条数
		int additionSize = userEquip.getAdditionAttributeMap().size();	
		WashRuleConfig washRule = propsManager.getWashRuleConfig(additionSize);
		if(washRule == null) {
			return ResultObject.ERROR(FAILURE);
		}

		EquipConfig equipConfig = userEquip.getEquipConfig();
		if(equipConfig == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if(!equipConfig.canForge()) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int itemId = PropsRule.getPolishEquipItemId(userEquip.getQuality());
		PropsConfig propsConfig = propsManager.getPropsConfig(itemId);
		if(propsConfig == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		}
		
		//精炼石道具ID验证
		SynthObject userPropsObject = this.checkUserItemCountByItemId(playerId, userProps, itemId);
		if(userPropsObject.getResult() < SUCCESS) {
			return ResultObject.ERROR(userPropsObject.getResult());
		} 

		int needItemCount = washRule.getItemCount();			//需要的保护符数量
		int propsCount = userPropsObject.getBindingCount();		//当前保护符数量
		if(propsCount + autoBuyCount != needItemCount) {		
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		//保护的下标数量与需要的道具不合法, 直接返回错误码
		int lockPropsId = PropsRule.LOCK_PROPS_ID;
		double costSilverRate = washRule.getSilver();								//需要的游戏币数量概率
		int baseSilver = equipConfig.getSilverPrice();								//装备的游戏币价格
		Set<Integer> safeIndexs = PropsRule.splitSafeIndex2Set(playerId, safeIndex);//保护的下标列表
		SynthObject lockPropsObject = this.checkUserItemCountByItemId(playerId, lockProps, lockPropsId);
		if(lockPropsObject.getResult() < SUCCESS) {
			return ResultObject.ERROR(lockPropsObject.getResult());
		} 
		
		int needLockCount = safeIndexs.size();
		int lockCount = lockPropsObject.getBindingCount();		//当前保护符数量
		if(needLockCount != lockCount) {		
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		int totalCostGolden = propsConfig.getMallPriceByCount(autoBuyCount);		//自动购买的金币数量
		int costSilver = FormulaHelper.invoke(EQUIP_POLISH_COSTSILVER, baseSilver, costSilverRate).intValue();
		ChainLock lock = LockUtils.getLock(player, player.getPackLock());
		try {
			lock.lock();
			if(player.getSilver() < costSilver) {
				return ResultObject.ERROR(SILVER_NOT_ENOUGH);
			} else if(player.getGolden() < totalCostGolden) {
				return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
			}
			userEquip.setBinding(true);
			player.decreaseSilver(costSilver);
			player.decreaseGolden(totalCostGolden);
		} finally {
			lock.unlock();
		}

		dbService.submitUpdate2Queue(player);
		List<LoggerGoods> loggerGoods = new ArrayList<LoggerGoods>();
		Map<Long, Integer> bindingItems = userPropsObject.getBindingItems();
		Collection<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		List<UserProps> costUserPropsList = propsManager.costUserPropsList(bindingItems);
		if(costUserPropsList != null && !costUserPropsList.isEmpty()) {
			backpackEntries.addAll(voFactory.getUserPropsEntries(costUserPropsList));
			for(UserProps up : costUserPropsList){
				int baseId = up.getBaseId();
				long userPropsId = up.getId();
				Integer count = bindingItems.get(userPropsId);
				if(count != null && count != 0) {
					loggerGoods.add(LoggerGoods.outcomeProps(userPropsId, baseId, Math.abs(count)));
				}
			}
		}
		
		Map<Long, Integer> lockBindingItems = lockPropsObject.getBindingItems();
		List<UserProps> costLockPropsList = propsManager.costUserPropsList(lockBindingItems);
		if(costLockPropsList != null && !costLockPropsList.isEmpty()) {
			backpackEntries.addAll(voFactory.getUserPropsEntries(costLockPropsList));
			for(UserProps up : costLockPropsList){
				int baseId = up.getBaseId();
				long userPropsId = up.getId();
				Integer count = lockBindingItems.get(userPropsId);
				if(count != null && count != 0) {
					loggerGoods.add(LoggerGoods.outcomeProps(userPropsId, baseId, Math.abs(count)));
				}
			}
		}
		
		if(autoBuyCount > 0) {
			loggerGoods.add(LoggerGoods.outcomeEquipAutoBuyGolden(-1L, itemId, autoBuyCount, totalCostGolden));
		}
		
		//附加新属性.
		if(washRule.isWashSuccess()) { //洗练是否成功. 处理增加属性
			this.refreshEquipSuccessAddition(userEquip, equipConfig, washRule, safeIndexs);
		} else {
			this.refreshEquipFailureAddition(userEquip, equipConfig, washRule, safeIndexs);
		}
		
		taskEntityFacade.updateEquipPolishTask(playerId);
		LoggerGoods[] loggerGoodsArray = loggerGoods.toArray(new LoggerGoods[loggerGoods.size()]);
		if(loggerGoodsArray.length > 0) {
			GoodsLogger.goodsLogger(player, Source.PROPS_POLISHED_EQUIP_ADDITIONS, loggerGoodsArray);
		}
		if(costSilver != 0) {
			SilverLogger.outCome(Source.PROPS_POLISHED_EQUIP_ADDITIONS, costSilver, player, loggerGoodsArray);
		}
		if(totalCostGolden != 0) {
			GoldLogger.outCome(Source.PROPS_POLISHED_EQUIP_ADDITIONS, totalCostGolden, player, loggerGoodsArray);
		}
		
		achieveFacade.firstAchieved(playerId, FIRST_ACHIEVE, FIRST_EQUIP_POLISHED);
		return ResultObject.SUCCESS(backpackEntries);
	}
	
	/**
	 * 设置洗练装备的附加属性到缓存中
	 * 
	 * @param playerId		角色ID
	 * @param attributeVO	洗练VO对象
	 */
	private void putWashEquipAddition(long playerId, WashAttributeVO attributeVO) {
		this.WASH_ATTRIBUTE_CONTEXT.put(playerId, attributeVO);
	}
	
	/**
	 * 选择洗练装备的附加属性
	 * 
	 * @param  playerId					角色ID
	 * @return {@link ResultObject}		返回值对象			
	 */
	
	public int selectPolishedEquipAddition(long playerId, boolean select) {
		WashAttributeVO attributeVO = this.removeWashAttributeVO(playerId);
		if(attributeVO == null) {
			return WASH_ATTRIBUTE_NOT_FOUND;
		} else if(!select) {	//如果不是选择, 则直接返回成功
			return SUCCESS;
		}
		
		long userEquipId = attributeVO.getUserEquipId();
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if(userEquip == null) {
			putWashEquipAddition(playerId, attributeVO);
			return EQUIP_NOT_FOUND;
		} else if(!userEquip.validBackpack(CAN_FORGE_BACKPACK)) {
			putWashEquipAddition(playerId, attributeVO);
			return NOT_IN_BACKPACK;
		} else if(userEquip.getPlayerId() != playerId) {
			putWashEquipAddition(playerId, attributeVO);
			return BELONGS_INVALID;
		} else if(userEquip.getCount() <= 0) {
			putWashEquipAddition(playerId, attributeVO);
			return EQUIP_NOT_FOUND;
		} else if(userEquip.isTrading()) {
			putWashEquipAddition(playerId, attributeVO);
			return EQUIP_CANNOT_USE;
		}
		
		int equipBackpack = userEquip.getBackpack();
		UserDomain userDomain = userManager.getUserDomain(playerId);
		Fightable beforable = userDomain.getBattle().getAndCopyAttributes();
		ChainLock lock = LockUtils.getLock(userDomain.getPackLock());
		try {
			lock.lock();
			userEquip.getAdditionAttributeMap().clear();
			Map<Integer, AttributeVO> attributes = attributeVO.getAttributes();
			if(attributes != null && !attributes.isEmpty()) {
				EquipHelper.rebuildIndex(attributes);
				userEquip.getAdditionAttributeMap().putAll(attributes);
			}
			userEquip.updateAdditionAttributeMap();
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(userEquip);
		forgePlayerFlushable(userDomain, beforable, equipBackpack, false);
		return SUCCESS;
	}

	/**
	 * 结算装备的属性
	 * 
	 * @param  userEquip			用户装备对象
	 * @param  equipConfig			基础装备对象
	 * @param  washRule				洗练规则对象
	 * @param  safeIndexs			保护的下标列表
	 */
	private void refreshEquipSuccessAddition(UserEquip userEquip, EquipConfig equipConfig, WashRuleConfig washRule, Set<Integer> safeIndexs) {
		int count = 0;
		Long userEquipId = userEquip.getId();
		long playerId = userEquip.getPlayerId();
		int equipLevel = equipConfig.getLevel();
		int maxAddition = equipConfig.getMaxAddition();
		int quality = userEquip.getQuality().ordinal();
		int successAddition = washRule.getSuccessAddition();
		List<Integer> additionList = equipConfig.getAdditionList();	//装备的附加属性列表
		Map<Integer, AttributeVO> equipAttributeMap = userEquip.getAdditionAttributeMap();
		Map<Integer, AttributeVO> additionMap = new HashMap<Integer, AttributeVO>(equipAttributeMap); //装备的附加属性集合
		while(++count < 20 && successAddition > additionMap.size() && additionMap.size() < maxAddition) {
			WashAttributeConfig washAttribute = EquipHelper.getRandomWashAttribute(equipLevel, quality, additionList);
			if(washAttribute == null) {
				continue;
			}
			
			int addAttrValue = washAttribute.getRandomAddValue();
			if(addAttrValue <= 0) {
				continue;
			}
			
			int attribute = washAttribute.getAttribute();
			int newIndex = additionMap.size() + 1;
			additionMap.put(newIndex, AttributeVO.valueOf(newIndex, attribute, addAttrValue));
		}
		
		Map<Integer, AttributeVO> cacheAdditions = washRandomAttribute(quality, equipConfig, safeIndexs, additionMap);
		this.putWashEquipAddition(playerId, WashAttributeVO.valueOf(userEquipId, cacheAdditions));
	}

	/**
	 * 洗练生成随机属性
	 * 
	 * @param  equipLevel		装备等级
	 * @param  safeIndexs		保护下标	
	 * @param  additionMap		附加属性集合信息
	 * @return {@link Map}		附加属性集合
	 */
	private Map<Integer, AttributeVO> washRandomAttribute(int quality, EquipConfig equipConfig, Set<Integer> safeIndexs, Map<Integer, AttributeVO> additionMap) {
		int equipLevel = equipConfig.getLevel();
		List<Integer> additionList = equipConfig.getAdditionList();
		Map<Integer, AttributeVO> cacheAdditions = new HashMap<Integer, AttributeVO>();	//这个集合是缓存起来的
		for (AttributeVO attributeVO : additionMap.values()) {
			int index = attributeVO.getId();
			if(safeIndexs != null && safeIndexs.contains(index)) { //该下标被保护了, 需要保护属性和保护值
				cacheAdditions.put(index, attributeVO);
				continue;
			} 
			
			int newAttribute = attributeVO.getAttribute();
			if(additionList != null && !additionList.isEmpty()) {
				int additionListSize = additionList.size();
				int randomAddition = additionList.get(Tools.getRandomInteger(additionListSize));
				WashTypeConfig washType = equipService.getRandomAdditionWashType(randomAddition);
				newAttribute = washType != null ? washType.getAttribute() : newAttribute;
			}
			
			WashAttributeConfig attributeConfig = equipService.getWashAttributeConfig(equipLevel, newAttribute, quality);
			if(attributeConfig == null) {
				cacheAdditions.put(index, attributeVO);
				continue;
			}
			
			int randomAddValue = attributeConfig.getRandomAddValue();
			if(randomAddValue > 0) {
				cacheAdditions.put(index, AttributeVO.valueOf(index, newAttribute, randomAddValue));
			}
		}
		return cacheAdditions;
	}
	
	/**
	 * 结算装备的属性
	 * 
	 * @param  userEquip			用户装备对象
	 * @param  equipConfig			基础装备对象
	 * @param  washRule				洗练规则对象
	 * @param  safeIndexs			保护的下标列表
	 */
	private void refreshEquipFailureAddition(UserEquip userEquip, EquipConfig equipConfig, WashRuleConfig washRule, Set<Integer> safeIndexs) {
		int count = 0;
		long userEquipId = userEquip.getId();
		long playerId = userEquip.getPlayerId();
		int quality = userEquip.getQuality().ordinal();
		int minAddition = equipConfig.getMinAddition();
		int failureAddition = washRule.getFailureAddition();
		Map<Integer, AttributeVO> additionMap = new HashMap<Integer, AttributeVO>(userEquip.getAdditionAttributeMap()); 
		Map<Integer, AttributeVO> cacheAdditions = washRandomAttribute(quality, equipConfig, safeIndexs, additionMap);
		while(++count < 30 && failureAddition < additionMap.size() && additionMap.size() > minAddition) {
			Set<Integer> currentIndexes = new TreeSet<Integer>(additionMap.keySet());
			if(safeIndexs != null && !safeIndexs.isEmpty()) {
				Set<Integer> index = new HashSet<Integer>(currentIndexes);
				index.retainAll(safeIndexs);
				currentIndexes.removeAll(index);
			}
			
			if(currentIndexes.isEmpty()) {
				break;
			}
			
			List<Integer> indexList = new ArrayList<Integer>(currentIndexes);
			Integer removeIndex = indexList.get(indexList.size() - 1);
			additionMap.remove(removeIndex);
			cacheAdditions.remove(removeIndex);
		}
		this.putWashEquipAddition(playerId, WashAttributeVO.valueOf(userEquipId, cacheAdditions));
	}
	
	
	public void onLogoutEvent(UserDomain userDomain) {
		WASH_ATTRIBUTE_CONTEXT.remove(userDomain.getPlayerId());	//移除洗练属性
	}

	/**
	 * 移除洗练属性VO对象
	 * 
	 * @param  playerId					角色ID
	 */
	
	public WashAttributeVO removeWashAttributeVO(long playerId) {
		return WASH_ATTRIBUTE_CONTEXT.remove(playerId);
	}


	/**
	 * 获得洗练属性VO对象
	 * 
	 * @param  playerId					角色ID
	 * @return {@link WashAttributeVO}	洗练属性VO对象
	 */
	
	public WashAttributeVO getWashAttributeVO(long playerId) {
		return WASH_ATTRIBUTE_CONTEXT.get(playerId);
	}

	
	public ResultObject<Collection<BackpackEntry>> emendationEquipAttribute(long playerId,
			long userEquipId, String userProps, String luckyProps, int autoBuyCount) {
		return ResultObject.ERROR(FAILURE);
	}

	/**
	 * 提升装备的附加属性.
	 * 
	 * <strong>注意: 该方法没用</strong>
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquipId				用户装备ID
	 * @param  userProps				玄晶石...	格式: 用户道具ID_数量|..
	 * @param  luckyProps				幸运石信息.	格式: 用户道具ID_数量|..
	 * @param  autoBuyCount				自动购买玄晶石的数量
	 * @param  upgradeParams			需要洗点的信息. 
	 * @return {@link ResultObject}		用户道具模块返回值
	 */
	
	public ResultObject<Collection<BackpackEntry>> recastEquipAttribute(long playerId, long userEquipId, 
			String userProps, String luckyProps, int autoBuyCount, Set<Integer> upgradeParams) {
		int backpack = BackpackType.DEFAULT_BACKPACK;
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if(userEquip == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if(!userEquip.validBackpack(backpack)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userEquip.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userEquip.isTrading()) {
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		}
		return ResultObject.ERROR(FAILURE);
	}

	/**
	 * 检测装备镶嵌
	 * 
	 * @param  userEquip					用户装备对象
	 * @param  equipConfig					基础装备对象
	 * @param  enchangeInfos				镶嵌信息字符串
	 * @param  childType					需要验证的子类型
	 * @return {@link EquipEnchangeInfo}	镶嵌信息返回值	
	 */
	private EquipEnchangeInfo checkEnchanges(UserEquip userEquip, EquipConfig equipConfig, String enchangeInfos, int type) {
		long playerId = userEquip.getPlayerId();
		EquipEnchangeInfo equipEnchangeInfos = new EquipEnchangeInfo();
		List<String[]> arrays = Tools.delimiterString2Array(enchangeInfos);
		if(arrays == null || arrays.isEmpty()) {
			return equipEnchangeInfos.updateResult(INPUT_VALUE_INVALID);
		}
		
		int maxHole = equipConfig.getMaxHole();
		int silverPrice = equipConfig.getSilverPrice();
		List<Integer> embadeChildTypeList = equipConfig.getCanEmbadeList();
		for (String[] element : arrays) {
			Integer index = Integer.valueOf(element[0]);
			Long userItemId = Long.valueOf(element[1]);
			if(index == null || userItemId == null) {
				return equipEnchangeInfos.updateResult(INPUT_VALUE_INVALID);
			} else if(index <= 0 || index > maxHole) {
				return equipEnchangeInfos.updateResult(INPUT_VALUE_INVALID);
			}
			
			UserProps userProps = propsManager.getUserProps(userItemId);
			if(userProps == null) {
				return equipEnchangeInfos.updateResult(ITEM_NOT_FOUND);
			} else if(!userProps.validBackpack(BackpackType.DEFAULT_BACKPACK)) {
				return equipEnchangeInfos.updateResult(NOT_IN_BACKPACK);
			} else if(userProps.getPlayerId() != playerId) {
				return equipEnchangeInfos.updateResult(BELONGS_INVALID);
			} else if(userProps.isOutOfExpiration()) {
				return equipEnchangeInfos.updateResult(OUT_OF_EXPIRATION);
			} else if(userProps.isTrading()) {
				return equipEnchangeInfos.updateResult(ITEM_CANNOT_USE);
			}
			
			int baseId = userProps.getBaseId();
			PropsConfig props = propsManager.getPropsConfig(baseId);
			if(props == null) {
				return equipEnchangeInfos.updateResult(ITEM_NOT_FOUND);
			}
			
			int propsType = props.getPropsType();
			int propsChildType = props.getChildType();
			if(propsType != type || !embadeChildTypeList.contains(propsChildType)) {
				return equipEnchangeInfos.updateResult(TYPE_INVALID);
			}
			
			Map<Integer, HoleInfo> holeInfos = userEquip.getHoleInfos();
			HoleInfo holeInfo = holeInfos.get(index);
			if(holeInfo == null || holeInfo.getItemId() > 0) {
				return equipEnchangeInfos.updateResult(ENCHANGE_FAILURE);
			} else if(equipEnchangeInfos.hasHoleInfo(index)) {
				return equipEnchangeInfos.updateResult(ENCHANGE_FAILURE);
			}
			
			int cacheItemCount = equipEnchangeInfos.getEnchangeItemCount(userItemId);
			if(userProps.getCount() - cacheItemCount <= 0) {
				return equipEnchangeInfos.updateResult(ITEM_NOT_ENOUGH);
			}
			
			equipEnchangeInfos.addHoleInfo(index, baseId);
			equipEnchangeInfos.addEnchangeItemCount(userItemId, 1);
			equipEnchangeInfos.addSilver(FormulaHelper.invoke(EQUIP_ENCHANGE_COSTSILVER, silverPrice).intValue());
		}
		return equipEnchangeInfos.updateResult(SUCCESS);
	}
	
//	/**
//	 * 镶嵌护身符
//	 * 
//	 * @param  playerId				角色ID
//	 * @param  userEquip			用户装备
//	 * @param  equipConfig			基础装备信息
//	 * @param  userProps			用户道具信息
//	 * @param  index				镶嵌下标
//	 * @return
//	 */
//	private ResultObject<Collection<BackpackEntry>> enchangeAmuletEquip(long playerId, 
//				UserEquip userEquip, EquipConfig equipConfig, String enchangeInfos) {
//		int type = PropsType.SHARIPU_PROPS_TYPE;
//		EquipEnchangeInfo enchangeResult = checkEnchanges(userEquip, equipConfig, enchangeInfos, type);
//		if(enchangeResult.getResult() != SUCCESS) {
//			return ResultObject.ERROR(enchangeResult.getResult());
//		}
//		
//		int maxHole = equipConfig.getMaxHole();
//		ChainLock lock = LockUtils.getLock(userEquip);
//		try {
//			lock.lock();
//			Map<Integer, Integer> enchangeHoles = enchangeResult.getEnchangeHoles();
//			Map<Integer, HoleInfo> holeInfos = userEquip.getHoleInfos();
//			for (Integer index : enchangeHoles.keySet()) {	//先检测是否已经镶嵌了道具, 如果镶嵌了, 则直接返回错误码
//				HoleInfo holeInfo = holeInfos.get(index);
//				if(holeInfo == null || holeInfo.getItemId() > 0) {
//					return ResultObject.ERROR(FAILURE);
//				}
//			}
//			
//			for (Entry<Integer, Integer> entry : enchangeHoles.entrySet()) {
//				Integer index = entry.getKey();
//				Integer itemId = entry.getValue();
//				if(index == null || itemId == null) {
//					continue;
//				}
//				
//				HoleInfo holeInfo = holeInfos.get(index);
//				if(holeInfo != null) {
//					holeInfo.setItemId(itemId);
//				}
//			}
//
//			EquipHelper.refreshHoleIndex(userEquip, maxHole);
//		} finally {
//			lock.unlock();
//		}
//		
//		cachedService.submitUpdated2Queue(userEquip.getId(), UserEquip.class);
//		Collection<BackpackEntry> backpackEntries = new HashSet<BackpackEntry>();
//		backpackEntries.add(voFactory.getUserEquipEntry(userEquip));
//		Map<Long, Integer> enchangeItems = enchangeResult.getEnchangeItems();
//		for (Entry<Long, Integer> entry : enchangeItems.entrySet()) {
//			Long userItemId = entry.getKey();
//			Integer costCount = entry.getValue();
//			UserProps userProps = this.getUserProps(userItemId);
//			if(userProps == null) {
//				continue;
//			}
//
//			boolean clearCache = false;
//			ChainLock propsLocks = LockUtils.getLock(userProps);
//			try {
//				propsLocks.lock();
//				userProps.decreaseItemCount(costCount);
//				clearCache = userProps.getCount() <= 0;
//			} finally {
//				propsLocks.unlock();
//			}
//			
//			backpackEntries.add(voFactory.getUserPropsEntry(userProps));
//			cachedService.submitUpdated2Queue(userItemId, UserProps.class);
//			if(clearCache) {
//				this.removeUserPropsIdList(playerId, DEFAULT_BACKPACK);
//			}
//		}
//		
//		return ResultObject.SUCCESS(backpackEntries);
//	}

	/**
	 * 用户道具炼化
	 * 
	 * @param  playerId					角色ID
	 * @param  propsId					道具ID
	 * @param  count					道具数量
	 * @param  userProps				用户道具信息. 格式: 用户道具ID1_道具数量1|用户道具ID2_道具数量2|...
	 * @return {@link ResultObject}		用户道具模块返回值
	 */
	
	public ResultObject<Collection<BackpackEntry>> aritificeProps(long playerId, int propsId, int count, String userProps) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(count <= 0) {
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		PropsConfig propsConfig = propsManager.getPropsConfig(propsId);
		if(propsConfig == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		}
		
		PropsArtificeConfig artificeConfig = propsManager.getPropsArtifice(propsId);
		if(artificeConfig == null) {
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		
		Map<Integer, Integer> materials = artificeConfig.getSynthMaterialByCount(count);
		ResultObject<ArtificeVO> artificeResult = this.checkArtificeProps(playerId, userProps, materials);
		if(artificeResult.getResult() < SUCCESS) {
			return ResultObject.ERROR(artificeResult.getResult());
		}
		
		ArtificeVO artificeVO = artificeResult.getValue();
		if(artificeVO == null) {
			return ResultObject.ERROR(FAILURE);
		}
		
		Player player = userDomain.getPlayer();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		ChainLock lock = LockUtils.getLock(player.getPackLock());
		int costSilver = artificeConfig.getSynthCostSilver(count);
		int currentBackSize = propsManager.getBackpackSize(playerId, backpack);
		PropsStackResult stackResult = PropsHelper.calcPropsStack(playerId, backpack, propsId, count, true);
		List<UserProps> newUserProps = stackResult.getNewUserProps();
		try {
			lock.lock();
			if(player.getSilver() < costSilver) {
				return ResultObject.ERROR(SILVER_NOT_ENOUGH);
			} 

			if(!newUserProps.isEmpty()) {
				if(!player.canAddNew2Backpack(newUserProps.size() + currentBackSize, backpack)) {
					return ResultObject.ERROR(BACKPACK_FULLED);
				}
				newUserProps = propsManager.createUserProps(newUserProps);
				propsManager.put2UserPropsIdsList(playerId, backpack, newUserProps);
			}
			
			player.decreaseSilver(costSilver);
		} catch (Exception e) {
			LOGGER.error("{}", e);
			return ResultObject.ERROR(FAILURE);
		} finally {
			lock.unlock();
		}

		dbService.submitUpdate2Queue(player);
		List<LoggerGoods> changeGoods = new ArrayList<LoggerGoods>(2);
		Collection<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		Collection<UserProps> updateUserPropsList = propsManager.updateUserPropsList(stackResult.getMergeProps());
		List<UserProps> costUserPropsList = propsManager.costUserPropsList(artificeVO.getCostUserProps());
		if(updateUserPropsList != null && !updateUserPropsList.isEmpty()) {
			backpackEntries.addAll(updateUserPropsList);
		}
		
		if(newUserProps != null && !newUserProps.isEmpty()) {
			backpackEntries.addAll(newUserProps);
			for(UserProps up : newUserProps){
				changeGoods.add(LoggerGoods.incomeProps(up.getBaseId(), up.getCount()));
			}
		}
		
		if(costUserPropsList != null && !costUserPropsList.isEmpty()) {
			backpackEntries.addAll(costUserPropsList);
			Map<Long, Integer> costUserProps = artificeVO.getCostUserProps();
			for(UserProps up : costUserPropsList){
				int baseId = up.getBaseId();
				long userPropsId = up.getId();
				Integer costCount = costUserProps.get(userPropsId);
				if(costCount != null && costCount != 0) {
					changeGoods.add(LoggerGoods.outcomeProps(userPropsId, baseId, Math.abs(costCount)));
				}
			}
		}
		
		// 发公告, 超平增 ---- 2012.6.7
		BulletinConfig bulletinConfig = resourceService.get(NoticeID.PROPS_SYNTH, BulletinConfig.class);
		if (bulletinConfig != null) {
			Set<Integer> conditionSet = bulletinConfig.getConditions();
			if (conditionSet.contains(propsId)) {
				Map<String, Object> paramsMap = new HashMap<String, Object>(3);
				paramsMap.put(NoticeRule.playerId, playerId);
				paramsMap.put(NoticeRule.playerName, player.getName());
				paramsMap.put(NoticeRule.props, propsConfig.getName());
				NoticePushHelper.pushNotice(NoticeID.PROPS_SYNTH, NoticeType.HONOR, paramsMap, bulletinConfig.getPriority());
			}
		}
		
		
		LoggerGoods[] array = changeGoods.toArray(new LoggerGoods[changeGoods.size()]);
		if(array.length > 0) {
			GoodsLogger.goodsLogger(player, Source.PROPS_ARITIFICE_PROPS, array);
		}
		if(costSilver != 0) {
			SilverLogger.outCome(Source.PROPS_ARITIFICE_PROPS, costSilver, player, array);
		}
		
		achieveFacade.firstAchieved(playerId, FIRST_ACHIEVE, FIRST_SYNTH);
		return ResultObject.SUCCESS(backpackEntries);
	}

	/**
	 * 用户装备分解.
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquips				用户装备信息. 格式: 用户装备ID_用户装备ID_...
	 * @return {@link ResultObject}		用户道具模块返回值
	 */
	
	public ResultObject<Collection<BackpackEntry>> resolveUserEquips(long playerId, String userEquips) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		if(battle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		}
		
		ResultObject<ResolveVO> resolveResult = checkResolveUserEquips(playerId, userEquips);
		if(resolveResult.getResult() < SUCCESS) {
			return ResultObject.ERROR(resolveResult.getResult());
		}
		
		ResolveVO resolveVO = resolveResult.getValue();
		if(resolveVO == null) {
			return ResultObject.ERROR(FAILURE);
		}
		
		Map<Long, UserEquip> cacheEquips = resolveVO.getCacheEquips();
		if(cacheEquips.isEmpty()) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int costSilver = resolveVO.getCostSilver();
		List<UserProps> addNewUserPropsList = resolveVO.getAddNewUserProps();
		Map<Long, Integer> addMoreUserProps = resolveVO.getAddMoreUserProps();
		int currentBackSize = propsManager.getBackpackSize(playerId, DEFAULT_BACKPACK);
		ChainLock lock = LockUtils.getLock(player.getPackLock());
		try {
			lock.lock();
			if(player.getSilver() < costSilver) {
				return ResultObject.ERROR(SILVER_NOT_ENOUGH);
			}
			
			if(!addNewUserPropsList.isEmpty()) {
				int needBackSize = addNewUserPropsList.size();
				if(!player.canAddNew2Backpack(needBackSize + currentBackSize, DEFAULT_BACKPACK)) {
					return ResultObject.ERROR(BACKPACK_FULLED);
				} 
				addNewUserPropsList = propsManager.createUserProps(addNewUserPropsList);
				propsManager.put2UserPropsIdsList(playerId, DEFAULT_BACKPACK, addNewUserPropsList);
			}
			
			player.decreaseSilver(costSilver);
		} catch (Exception e) {
			LOGGER.error("用户装备:{} 分解异常:{}", playerId, e);
			return ResultObject.ERROR(FAILURE);
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(player);
		Collection<LoggerGoods> loggerGoodsList = new ArrayList<LoggerGoods>();
		Collection<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		backpackEntries.addAll(removeUserEquips(playerId, cacheEquips.values()));
		if(!addMoreUserProps.isEmpty()) {
			List<UserProps> updateUserPropsList = propsManager.updateUserPropsList(addMoreUserProps);
			backpackEntries.addAll(voFactory.getUserPropsEntries(updateUserPropsList));
		}
		
		if(!addNewUserPropsList.isEmpty()) {
			backpackEntries.addAll(voFactory.getUserPropsEntries(addNewUserPropsList));
			for(UserProps up : addNewUserPropsList){
				loggerGoodsList.add(LoggerGoods.incomeProps(up.getBaseId(), up.getCount()) );
			}
		}
		
		//分解得到的道具信息
		if(!resolveVO.getResolveItems().isEmpty()) {
			for (Entry<Integer, Integer> entry : resolveVO.getResolveItems().entrySet()) {
				loggerGoodsList.add(LoggerGoods.incomeProps(entry.getKey(), entry.getValue()));
			}
 		}
				
		for(long userEquipId : cacheEquips.keySet()){
			UserEquip userEquip = cacheEquips.get(userEquipId);
			loggerGoodsList.add(LoggerGoods.outcomeEquip(userEquip.getId(), userEquip.getBaseId(), 1));
		}

		LoggerGoods[] goodsLoggerArray = loggerGoodsList.toArray(new LoggerGoods[loggerGoodsList.size()]);
		if(goodsLoggerArray.length > 0) {
			GoodsLogger.goodsLogger(player, Source.PROPS_RESOLVE_USEREQUIP, goodsLoggerArray);
		}
		if(costSilver != 0) {
			SilverLogger.outCome(Source.PROPS_RESOLVE_USEREQUIP, costSilver, player, goodsLoggerArray);
		}
		
		achieveFacade.firstAchieved(playerId, FIRST_ACHIEVE, FIRST_EQUIP_RESOLVE);
		return ResultObject.SUCCESS(backpackEntries);
	}

	/**
	 * 移除用户装备列表
	 * 
	 * @param  userEquipList		用户装备列表
	 * @return {@link Collection}	用户背包实体列表
	 */
	@SuppressWarnings("unchecked")
	private Collection<BackpackEntry> removeUserEquips(long playerId, Collection<UserEquip> userEquipList) {
		Collection<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		if(userEquipList != null && !userEquipList.isEmpty()) {
			UserDomain userDomain = userManager.getUserDomain(playerId);
			ChainLock lock = LockUtils.getLock(userDomain.getPackLock());
			try {
				lock.lock();
				for (UserEquip userEquip : userEquipList) {
					userEquip.setCount(0);
					int backpack = userEquip.getBackpack();
					userEquip.setBackpack(DROP_BACKPACK);
					int index = userEquip.getIndex();
					int baseId = userEquip.getBaseId();
					long userEquipId = userEquip.getId();
					Quality quality = userEquip.getQuality();
					backpackEntries.add(BackpackEntry.valueEquipEmpty(userEquipId, baseId, backpack, quality, index, userEquip.isBinding()));
					propsManager.removeFromEquipIdsList(playerId, backpack, userEquip);
					propsManager.put2UserEquipIdsList(playerId, DROP_BACKPACK, userEquip);
				}
			} finally {
				lock.unlock();
			}
			dbService.submitUpdate2Queue(userEquipList);
		}
		return backpackEntries;
	}
	
	/**
	 * 检测分解用户装备信息
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquips				用户装备ID列表
	 * @return {@link ResultObject}		用户装备返回值
	 */
	private ResultObject<ResolveVO> checkResolveUserEquips(long playerId, String userEquips) {
		if(StringUtils.isBlank(userEquips)) {
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		ResolveVO resolveVO = new ResolveVO();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		String[] arrays = userEquips.split(Splitable.ATTRIBUTE_SPLIT);
		for (String element : arrays) {
			if(StringUtils.isBlank(element)) {
				continue;
			}
			
			long userEquipId = Long.valueOf(element.trim());
			UserEquip userEquip = propsManager.getUserEquip(userEquipId);
			if(userEquip == null || userEquip.getCount() <= 0) {
				return ResultObject.ERROR(EQUIP_NOT_FOUND);
			} else if(!userEquip.validBackpack(backpack)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if(userEquip.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			} else if(userEquip.isOutOfExpiration()) {
				return ResultObject.ERROR(OUT_OF_EXPIRATION);
			} else if(userEquip.getQuality() == Quality.WHITE) {
				return ResultObject.ERROR(INPUT_VALUE_INVALID);
			} else if(userEquip.isTrading()) {
				return ResultObject.ERROR(EQUIP_CANNOT_USE);
			}
			
			UserEquip cacheUserEquip = resolveVO.getCacheUserEquip(userEquipId);
			if(cacheUserEquip != null) {	//有重复的用户装备信息.
				return ResultObject.ERROR(INPUT_VALUE_INVALID);
			}
			this.refreshResolve(resolveVO, userEquip);
		}
		
		Map<Integer, Integer> resolveItems = resolveVO.getResolveItems();
		for (Entry<Integer, Integer> entry : resolveItems.entrySet()) {
			PropsStackResult stackResult = PropsHelper.calcPropsStack(playerId, backpack, entry.getKey(), entry.getValue(), true);
			resolveVO.getAddMoreUserProps().putAll(stackResult.getMergeProps());
			resolveVO.getAddNewUserProps().addAll(stackResult.getNewUserProps());
		}
		return ResultObject.SUCCESS(resolveVO);
	}
	
	/**
	 * 刷新星级装备分解
	 * 	
	 * @param resolveVO		分解属性VO
	 * @param userEquip		基础装备VO
	 */
	private void refreshResolve(ResolveVO resolveVO, UserEquip userEquip) {
		resolveVO.putCacheEquip(userEquip);
		EquipConfig equip = userEquip.getEquipConfig();
		EquipBreakConfig equipBreakConfig = equipService.getEquipResolveByIndex(equip.getLevel(), userEquip.getQuality().ordinal());
		int randomItemCount1 = equipBreakConfig.getRandomItemCount1();
		int randomItemCount2 = equipBreakConfig.getRandomItemCount2();
		if(equipBreakConfig.getPropsConfig1() != null && randomItemCount1 > 0) {
			resolveVO.addResolveItems(equipBreakConfig.getItemId1(), randomItemCount1);
		}
		if(equipBreakConfig.getPropsConfig2() != null && randomItemCount2 > 0) {
			resolveVO.addResolveItems(equipBreakConfig.getItemId2(), randomItemCount2);
		}
		int silverPrice = equip.getSilverPrice();
		int calcCostSilver = equipBreakConfig.calcCostSilver(silverPrice);
		resolveVO.addCostSilver(calcCostSilver);
	}
	
	/**
	 * 检查用户道具炼化信息
	 * 
	 * @param  playerId				角色ID
	 * @param  userPropsStr			用户道具字符串. 格式: 用户道具ID_用户道具数量|...
	 * @param  materials			可以使用的道具集合
	 * @return {@link ResultObject}	返回值信息
	 */
	private ResultObject<ArtificeVO> checkArtificeProps(long playerId, 
				String userPropsStr, Map<Integer, Integer> materials) {
		List<String[]> arrays = Tools.delimiterString2Array(userPropsStr);
		if(arrays == null || arrays.isEmpty()) {
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		ArtificeVO artificeVO = ArtificeVO.valueOf(materials);
		for (String[] array : arrays) {
			Long userPropsId = Long.valueOf(array[0]);
			Integer itemCount = Integer.valueOf(array[1]);
			if(userPropsId == null || itemCount == null || itemCount <= 0) {
				return ResultObject.ERROR(INPUT_VALUE_INVALID);
			}
			
			int backpack = BackpackType.DEFAULT_BACKPACK;
			UserProps userProps = propsManager.getUserProps(userPropsId);
			if(userProps == null) {
				return ResultObject.ERROR(ITEM_NOT_FOUND);
			} else if(!userProps.validBackpack(backpack)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if(userProps.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			} else if(userProps.isOutOfExpiration()) {
				return ResultObject.ERROR(OUT_OF_EXPIRATION);
			} else if(userProps.isTrading()) {
				return ResultObject.ERROR(ITEM_CANNOT_USE);
			}
			
			int cacheCount = artificeVO.getCostUserPropsCount(userPropsId);
			if(userProps.getCount() - cacheCount < itemCount) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			}
			
			int baseId = userProps.getBaseId();
			PropsConfig propsConfig = propsManager.getPropsConfig(baseId);
			if(propsConfig == null) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			}
			
			//不能使用该道具进行合成
			if(!materials.containsKey(baseId)) {
				return ResultObject.ERROR(INPUT_VALUE_INVALID);
			}
			
			Integer materialCount = materials.get(baseId);
			if(materialCount == null) {
				materials.remove(baseId);
				continue;
			}
			
			//不需要再使用这个多的道具了
			if(materialCount < itemCount) {
				return ResultObject.ERROR(INPUT_VALUE_INVALID);
			}
			
			materialCount -= itemCount;
			materials.put(baseId, materialCount);
			artificeVO.addCostUserProps(userPropsId, itemCount);
			if(materialCount <= 0) {
				materials.remove(baseId);
			}
		}
		
		if(!materials.isEmpty()) {
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		}
		return ResultObject.SUCCESS(artificeVO);
	}
	
	/**
	 * 移除装备镶嵌的宝石
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquipId				用户道具ID
	 * @param  index					移除的下标
	 * @return {@link ResultObject}		返回值信息
	 */
	@SuppressWarnings("unchecked")
	
	public ResultObject<Collection<BackpackEntry>> removeEquipEnchange(long playerId, long userEquipId, int index) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		if(player == null || battle == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		} else if(battle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		} else if(index <= 0) {
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if(userEquip == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if(!userEquip.validBackpack(CAN_FORGE_BACKPACK)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userEquip.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userEquip.isTrading()) {
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		} 
		
		Map<Integer, HoleInfo> holeInfos = userEquip.getHoleInfos();
		HoleInfo holeInfo = holeInfos.get(index);
		if(holeInfo == null || holeInfo.getItemId() <= 0) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int propsId = holeInfo.getItemId();
		PropsConfig props = propsManager.getPropsConfig(propsId);
		if(props == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		}
		
		int equipId = userEquip.getBaseId();
		EquipConfig equipConfig = propsManager.getEquipConfig(equipId);
		if(equipConfig == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		}
		
		int equipBackpack = userEquip.getBackpack();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		int baseSilver = equipConfig.getSilverPrice();
		Fightable beforable = battle.getAndCopyAttributes();
		int costSilver = FormulaHelper.invoke(EQUIP_ENCHANGE_COSTSILVER, baseSilver).intValue();
		PropsStackResult stackResult = PropsHelper.calcPropsStack(playerId, backpack, propsId, 1, true);
		int currBackpackSize = propsManager.getBackpackSize(playerId, backpack);
		ChainLock lock = LockUtils.getLock(player.getPackLock());
		List<UserProps> newPropsList = stackResult.getNewUserProps();
		try {
			lock.lock();
			if(player.getSilver() < costSilver) {
				return ResultObject.ERROR(SILVER_NOT_ENOUGH);
			}
			
			if(!userEquip.validBackpack(CAN_FORGE_BACKPACK)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if(userEquip.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			} 
			
			holeInfo = userEquip.getHoleInfos().get(index);
			if(holeInfo == null) {
				return ResultObject.ERROR(FAILURE);
			} else if(holeInfo.getItemId() != propsId) {
				return ResultObject.ERROR(FAILURE);
			}
			
			if(!newPropsList.isEmpty()) {
				if(!player.canAddNew2Backpack(newPropsList.size() + currBackpackSize, backpack)) {
					return ResultObject.ERROR(BACKPACK_FULLED);
				}
				newPropsList = propsManager.createUserProps(newPropsList);
				propsManager.put2UserPropsIdsList(playerId, backpack, newPropsList);
			}
			
			holeInfo.setItemId(-1);
			player.decreaseSilver(costSilver);
			EquipHelper.refreshHoleIndex(userEquip, equipConfig.getMaxHole());
		} catch (Exception e) {
			LOGGER.error("{}", e);
			return ResultObject.ERROR(FAILURE);
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(player, userEquip);
		Map<Long, Integer> mergeProps = stackResult.getMergeProps();
		Collection<BackpackEntry> backpackEntries = new HashSet<BackpackEntry>();
		Collection<UserProps> updateUserPropsList = propsManager.updateUserPropsList(mergeProps);
		List<LoggerGoods> loggerGoods = new ArrayList<LoggerGoods>() ;

		//更新的道具列表, 异步更新
		if(!updateUserPropsList.isEmpty()) {
			backpackEntries.addAll(voFactory.getUserPropsEntries(updateUserPropsList));
			loggerGoods.addAll(LoggerGoods.updateProps(mergeProps, updateUserPropsList));
		}
		
		//如果有, 则表示已经入库完成, 需要返回给客户端
		if(!newPropsList.isEmpty()) {
			backpackEntries.addAll(voFactory.getUserPropsEntries(newPropsList));
			loggerGoods.addAll(LoggerGoods.incomeProps(newPropsList));
		}
		
		LoggerGoods[] loggerGoodsArray = loggerGoods.toArray(new LoggerGoods[loggerGoods.size()]);
		GoodsLogger.goodsLogger(player, Source.PROPS_REMOVE_EQUIP_ENCHANGE, loggerGoodsArray );
		
		if(costSilver != 0){
			SilverLogger.outCome(Source.PROPS_REMOVE_EQUIP_ENCHANGE, costSilver, player, loggerGoodsArray);
		}
		
		backpackEntries.add(voFactory.getUserEquipEntry(userEquip));
		
		forgePlayerFlushable(userDomain, beforable, equipBackpack, true);
		return ResultObject.SUCCESS(backpackEntries);
	}

	/**
	 * 检测幸运石的数量
	 * 
	 * @param  playerId			检查幸运石的角色ID
	 * @param  userProps		幸运石的道具信息
	 * @return
	 */
	private SynthObject checkUserItemCount(long playerId, String userProps, int childType) {
		SynthObject synthObject = new SynthObject();
		Map<Long, Integer> spliteUserItems = this.spliteUserItems(userProps);
		for (Entry<Long, Integer> lockyEntry : spliteUserItems.entrySet()) {
			long userItemId = lockyEntry.getKey();
			int userItemCount = lockyEntry.getValue();
			UserProps userItem = propsManager.getUserProps(userItemId);
			if(userItem == null) {
				return synthObject.updateResult(ITEM_NOT_FOUND);
			} 
			
			if(!userItem.validBackpack(DEFAULT_BACKPACK)) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("用户道具:[{}] 背包号:[{}] 不能使用", userItemId, userItem.getBackpack());
				}
				return synthObject.updateResult(NOT_IN_BACKPACK);
			}
			
			if(userItem.getPlayerId() != playerId) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("用户道具:[{}] 不是玩家:[{}] 不能合成", userItemId, playerId);
				}
				return synthObject.updateResult(BELONGS_INVALID);
			}
			
			if(userItemCount <= 0) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("扣除的用户道具:[{}] 数量:[{}] 不合法, 不能当保护水晶", userItemId, userItemCount);
				}
				return synthObject.updateResult(INPUT_VALUE_INVALID);
			} 
			
			if(userItem.isTrading()) {
				return synthObject.updateResult(ITEM_CANNOT_USE);
			}

			int cacheCount = synthObject.getBindingMapCount(userItemId);
			if(userItem.getCount() - cacheCount < userItemCount) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("用户道具:[{}] 数量不足, 不能参与合成", userItemId);
				}
				return synthObject.updateResult(ITEM_NOT_ENOUGH);
			}
			
			int currBaseId = userItem.getBaseId();
			int baseId = synthObject.getBaseItemId();
			if(baseId <= 0) {
				synthObject.setBaseItemId(currBaseId);
			}
			
			if(synthObject.getBaseItemId() != currBaseId) {
				return synthObject.updateResult(FAILURE);
			}
			
			PropsConfig base = userItem.getPropsConfig();
			if(base == null) {
				return synthObject.updateResult(ITEM_NOT_FOUND);
			}
			
			if(base.getChildType() != childType) {
				return synthObject.updateResult(TYPE_INVALID);
			}
			synthObject.addBindingCount(userItemId, userItemCount);
		}
		return synthObject;
	}

	/**
	 * 检测幸运石的数量
	 * 
	 * @param  playerId			检查幸运石的角色ID
	 * @param  userProps		幸运石的道具信息
	 * @return
	 */
	private SynthObject checkUserItemCountByItemId(long playerId, String userProps, int itemId) {
		SynthObject synthObject = new SynthObject();
		Map<Long, Integer> spliteUserItems = this.spliteUserItems(userProps);
		for (Entry<Long, Integer> lockyEntry : spliteUserItems.entrySet()) {
			long userItemId = lockyEntry.getKey();
			int userItemCount = lockyEntry.getValue();
			UserProps userItem = propsManager.getUserProps(userItemId);
			if(userItem == null) {
				return synthObject.updateResult(ITEM_NOT_FOUND);
			} else if(!userItem.validBackpack(DEFAULT_BACKPACK)) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("用户道具:[{}] 背包号:[{}] 不能使用", userItemId, userItem.getBackpack());
				}
				return synthObject.updateResult(NOT_IN_BACKPACK);
			} else if(userItem.getPlayerId() != playerId) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("用户道具:[{}] 不是玩家:[{}] 不能合成", userItemId, playerId);
				}
				return synthObject.updateResult(BELONGS_INVALID);
			} else if(userItemCount <= 0) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("扣除的用户道具:[{}] 数量:[{}] 不合法, 不能当保护水晶", userItemId, userItemCount);
				}
				return synthObject.updateResult(INPUT_VALUE_INVALID);
			} else if(userItem.isTrading()) {
				return synthObject.updateResult(ITEM_CANNOT_USE);
			} else if(userItem.getBaseId() != itemId) {
				return synthObject.updateResult(TYPE_INVALID);
			}
			
			int cacheCount = synthObject.getBindingMapCount(userItemId);
			if(userItem.getCount() - cacheCount < userItemCount) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("用户道具:[{}] 数量不足, 不能参与合成", userItemId);
				}
				return synthObject.updateResult(ITEM_NOT_ENOUGH);
			}
			
			synthObject.setBaseItemId(itemId);
			synthObject.addBindingCount(userItemId, userItemCount);
		}
		return synthObject;
	}
	
	/**
	 * 精炼用户装备对象
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquipId				用户装备ID
	 * @param  userProps				用户道具信息. 格式: 用户道具ID_数量|...
	 * @return {@link ResultObject}		精炼用户装备返回值
	 */
	@SuppressWarnings("unchecked")
	
	public ResultObject<Collection<BackpackEntry>> refineEquipAttribute(long playerId, long userEquipId, String userProps) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		if (player == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if (battle == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		} else if (battle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		}
		
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if(userEquip == null || userEquip.getCount() <= 0) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if(!userEquip.validBackpack(CAN_FORGE_BACKPACK)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userEquip.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userEquip.isTrading()) {
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		} 

		int equipBackpack = userEquip.getBackpack();
		Map<Integer, AttributeVO> additionMap = userEquip.getAdditionAttributeMap();
		if(additionMap.size() != PropsRule.REFINE_EQUIP_ADDITION_COUNT) {
			return ResultObject.ERROR(TYPE_INVALID);
		}
		
		int equipId = userEquip.getBaseId();
		EquipConfig equipConfig = propsManager.getEquipConfig(equipId);
		if(equipConfig == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if(!equipConfig.canForge()) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int childType = PropsChildType.EQUIP_REFINING_TYPE;
		SynthObject checkResult = this.checkUserItemCount(playerId, userProps, childType);
		if(checkResult.getResult() < SUCCESS) {
			return ResultObject.ERROR(checkResult.getResult());
		}
		
		int equipLevel = equipConfig.getLevel();
		Fightable beforable = battle.getAndCopyAttributes();
		int costSilver = PropsRule.getRefineEquipCostSilver(equipLevel);
		ChainLock lock = LockUtils.getLock(player, player.getPackLock());
		try {
			lock.lock();
			if(player.getSilver() < costSilver) {
				return ResultObject.ERROR(SILVER_NOT_ENOUGH);
			}
			
			additionMap = userEquip.getAdditionAttributeMap();
			if(additionMap.size() != PropsRule.REFINE_EQUIP_ADDITION_COUNT) {
				return ResultObject.ERROR(TYPE_INVALID);
			}
			
			userEquip.setBinding(true);
			player.decreaseSilver(costSilver);
			EquipHelper.refreshRefineEquipAddition(userEquip, equipConfig);
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(player, userEquip);
		forgePlayerFlushable(userDomain, beforable, equipBackpack, true);
		Collection<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		backpackEntries.add(userEquip);
		List<LoggerGoods> loggerGoods = new ArrayList<LoggerGoods>();
		Map<Long, Integer> bindingItems = checkResult.getBindingItems();
		if(bindingItems != null && !bindingItems.isEmpty()) {
			List<UserProps> costUserPropsList = propsManager.costUserPropsList(bindingItems);
			backpackEntries.addAll(costUserPropsList);
			for(UserProps up : costUserPropsList){
				int count = bindingItems.get(up.getId());
				loggerGoods.add(LoggerGoods.outcomeProps(up.getId(), up.getBaseId(), count));
			}
		}
		
		if(userEquip.getAdditionAttributeMap().size() == 8){
			BulletinConfig config = resourceService.get(NoticeID.EQUIP_ATTR_UP, BulletinConfig.class);
			if (config != null) {
				Map<String, Object> noticeMap = new HashMap<String, Object>(4);
				noticeMap.put(NoticeRule.playerId, playerId);
				noticeMap.put(NoticeRule.playerName, player.getName());
				noticeMap.put(NoticeRule.equipId, userEquip.getEquipConfig().getName());
				noticeMap.put(NoticeRule.number, 8);
				NoticePushHelper.pushNotice(NoticeID.EQUIP_ATTR_UP, NoticeType.HONOR, noticeMap, config.getPriority());
			}
		}
		
		LoggerGoods[] loggerGoodsArray = loggerGoods.toArray(new LoggerGoods[loggerGoods.size()]);
		if(loggerGoodsArray.length > 0) {
			GoodsLogger.goodsLogger(player, Source.PROPS_REFINE_EQUIP_ATTRIBUTE, loggerGoodsArray); 
		}
		if(costSilver != 0) {
			SilverLogger.outCome(Source.PROPS_REFINE_EQUIP_ATTRIBUTE, costSilver, player, loggerGoodsArray);
		}
		return ResultObject.SUCCESS(backpackEntries);
	}

	/**
	 * 检查可以合成的数量, 和扣除的数量
	 * 
	 * @param  synthObject				合成道具信息
	 * @param  synthStone				是否合成宝石
	 * @return {@link SynthResult}		合成道具返回值
	 */
	private SynthResult checkSynthResult(SynthObject synthObject, PropsSynthConfig propsSynth) {
		SynthResult synthResult = new SynthResult();
		int totalBindingCount = synthObject.getBindingCount();					//扣除的绑定道具数量
		int synthBindCount = totalBindingCount / PropsRule.SYNTH_STONE_COUNT;	//合成未绑定道具的数量
		synthResult.setSynchTotalCount(synthBindCount);							//设置总共合成的数量
		while(synthBindCount > 0) {												//合成未绑定的道具
			boolean isSynthSuccess = propsSynth.isSynthSuccess();				//是否合成成功
			int costCount = PropsRule.getSynthStoneCost(isSynthSuccess);		//扣除的道具数量
			costCount = synthBindItems(synthResult, synthObject, costCount);	//扣除绑定的用户道具数量
			synthBindCount--;													//不管成功不成功, 先扣减
			if(isSynthSuccess) {
				synthResult.addSuccessCount(1);
				synthResult.addNewBindingCount(1);
			}
		}
		return synthResult;
	}
//	/**
//	 * 检查可以合成的数量, 和扣除的数量
//	 * 
//	 * @param  synthObject				合成道具信息
//	 * @param  synthStone				是否合成宝石
//	 * @return {@link SynthResult}		合成道具返回值
//	 */
//	private SynthResult checkSynthResult(SynthObject synthObject, PropsSynthConfig propsSynth) {
//		SynthResult synthResult = new SynthResult();
//		int bindingCount = synthObject.getBindingCount();						//扣除的绑定道具数量
//		int unBindingCount = synthObject.getUnBindingCount();					//扣除的非绑定数量
//		int synthUnBindCount = unBindingCount / PropsRule.SYNTH_STONE_COUNT;	//合成未绑定道具的数量
//		int lessUnBindCount = unBindingCount % PropsRule.SYNTH_STONE_COUNT;		//剩下的未绑定的道具数量
//		int totalBindingCount = bindingCount + lessUnBindCount;					//总共的可以合成绑定的道具数量
//		int synthBindCount = totalBindingCount / PropsRule.SYNTH_STONE_COUNT;	//合成未绑定道具的数量
//		synthResult.setSynchTotalCount(synthBindCount + synthUnBindCount);		//设置总共合成的数量
//		while(synthUnBindCount > 0) {											//优先合成未绑定的道具
//			boolean isSynthSuccess = propsSynth.isSynthSuccess();				//是否合成成功
//			int costCount = PropsRule.getSynthStoneCost(isSynthSuccess);		//扣除的道具数量
//			costCount = synthUnbindItems(synthResult, synthObject, costCount);
//			synthUnBindCount--;													//不管成功不成功, 先扣减
//			if(isSynthSuccess) {
//				synthResult.addSuccessCount(1);
//				synthResult.addNewUnBindingCount(1);
//			}
//		}
//		
//		while(synthBindCount > 0) {												//合成未绑定的道具
//			boolean isSynthSuccess = propsSynth.isSynthSuccess();				//是否合成成功
//			int costCount = PropsRule.getSynthStoneCost(isSynthSuccess);		//扣除的道具数量
//			costCount = synthUnbindItems(synthResult, synthObject, costCount);	//扣除未绑定的道具数量
//			costCount = synthBindItems(synthResult, synthObject, costCount);	//扣除绑定的用户道具数量
//			synthBindCount--;													//不管成功不成功, 先扣减
//			if(isSynthSuccess) {
//				synthResult.addSuccessCount(1);
//				synthResult.addNewBindingCount(1);
//			}
//		}
//		
//		return synthResult;
//	}
//	/**
//	 * 合成未绑定的用户道具
//	 * 
//	 * @param  synthResult			合成道具返回值
//	 * @param  synthObject			合成道具的封装对象
//	 * @param  costCount			需要的数量
//	 * @return {@link Integer}		剩余的道具数量
//	 */
//	private int synthUnbindItems(SynthResult synthResult, SynthObject synthObject, int costCount) {
//		int currCostCount = costCount;												//获得扣减的道具数量
//		Map<Long, Integer> unbindingUserItems = synthObject.getUnbindingItems();	//未绑定的用户道具集合
//		Set<Long> unbindIdList = new HashSet<Long>(unbindingUserItems.keySet());
//		for (Long userPropsId : unbindIdList) {
//			if(userPropsId == null || !unbindIdList.contains(userPropsId)) {
//				continue;
//			}
//			
//			Integer userItemCount = unbindingUserItems.get(userPropsId);
//			if(userItemCount == null || userItemCount <= 0) {
//				unbindingUserItems.remove(userPropsId);
//				continue;
//			}
//			
//			int canSubCount = Math.min(currCostCount, userItemCount);				//当前还可以扣除的道具数量
//			
//			currCostCount -= canSubCount;											//扣除道具数量
//			userItemCount -= canSubCount;											//扣除用户道具的数量
//			synthResult.addCostUserItemCount(userPropsId, canSubCount);				//增加道具扣除数量
//			unbindingUserItems.put(userPropsId, userItemCount);
//			if(userItemCount <= 0) {
//				unbindingUserItems.remove(userPropsId);
//			}
//			
//			if(currCostCount <= 0) {
//				break;
//			}
//		}
//		return currCostCount;
//	}

	/**
	 * 合成未绑定的用户道具
	 * 
	 * @param  synthResult			合成道具返回值
	 * @param  synthObject			合成道具的封装对象
	 * @param  costCount			需要的数量
	 * @return {@link Integer}		剩余的道具数量
	 */
	private int synthBindItems(SynthResult synthResult, SynthObject synthObject, int costCount) {
		int currCostCount = costCount;												//获得扣减的道具数量
		Map<Long, Integer> bindingUserItems = synthObject.getBindingItems();
		Set<Long> unbindIdList = new HashSet<Long>(bindingUserItems.keySet());
		for (Long userPropsId : unbindIdList) {
			if(userPropsId == null || !unbindIdList.contains(userPropsId)) {
				continue;
			}
			
			Integer userItemCount = bindingUserItems.get(userPropsId);
			if(userItemCount == null || userItemCount <= 0) {
				bindingUserItems.remove(userPropsId);
				continue;
			}

			int canSubCount = Math.min(currCostCount, userItemCount);				//当前还可以扣除的道具数量
			
			currCostCount -= canSubCount;											//扣除道具数量
			userItemCount -= canSubCount;											//扣除用户道具的数量
			synthResult.addCostUserItemCount(userPropsId, canSubCount);				//增加道具扣除数量
			bindingUserItems.put(userPropsId, userItemCount);
			if(userItemCount <= 0) {
				bindingUserItems.remove(userPropsId);
			}
			
			if(currCostCount <= 0) {
				break;
			}
		}
		return currCostCount;
	}

	/**
	 * 检查合成道具数量
	 * 
	 * @param  playerId					角色ID
	 * @param  bindUserItems			绑定的用户道具
	 * @param  unBindUserItems			未绑定的用户道具
	 * @return {@link SynthObject}		道具合成对象
	 */
	private SynthObject checkSynthItem(long playerId, String bindUserItems, String unBindUserItems) {
		SynthObject synthObject = new SynthObject();
		synthObject = synthResultValidate(playerId, spliteUserItems(bindUserItems), synthObject);
		if(synthObject.getResult() != SUCCESS) {
			return synthObject;
		}
		return synthResultValidate(playerId, spliteUserItems(unBindUserItems), synthObject);
	}
	
	/**
	 * 合成校验
	 * 
	 * @param  playerId				角色ID
	 * @param  userItems			道具列表. 格式: 用户道具ID_用户道具数量|....
	 * @param  synthObject			合成返回值
	 * @return {@link SynthObject}	合成返回值对象
	 */
	private SynthObject synthResultValidate(long playerId, Map<Long, Integer> userItems, SynthObject synthObject) {
		for (Entry<Long, Integer> entry : userItems.entrySet()) {
			long userItemId = entry.getKey();
			int userItemCount = entry.getValue();
			UserProps userProps = propsManager.getUserProps(userItemId);
			if(userProps == null) {
				return synthObject.updateResult(ITEM_NOT_FOUND);
			} 
			
			int backpack = userProps.getBackpack();
			if(backpack != DEFAULT_BACKPACK) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("用户道具:[{}] 背包号:[{}] 不能合成", userItemId, backpack);
				}
				return synthObject.updateResult(NOT_IN_BACKPACK);
			}
			
			long ownerId = userProps.getPlayerId();
			if(ownerId != playerId) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("用户道具:[{}] 不是玩家:[{}] 不能合成", userItemId, playerId);
				}
				return synthObject.updateResult(BELONGS_INVALID);
			}
			
			if(userItemCount <= 0) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("扣除的用户道具:[{}] 数量:[{}] 不合法, 不能合成", userItemId, userItemCount);
				}
				return synthObject.updateResult(INPUT_VALUE_INVALID);
			} else if(userProps.getCount() < userItemCount) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("用户道具:[{}] 数量不足, 不能参与合成", userItemId);
				}
				return synthObject.updateResult(ITEM_NOT_ENOUGH);
			} else if(userProps.isTrading()) {
				return synthObject.updateResult(ITEM_CANNOT_USE);
			}
			
			int baseId = synthObject.getBaseItemId();
			int currBaseId = userProps.getBaseId();
			if(baseId == -1) {
				synthObject.setBaseItemId(currBaseId);
			}
			if(synthObject.getBaseItemId() != currBaseId) {
				return synthObject.updateResult(FAILURE);
			}
			synthObject.addBindingCount(userItemId, userItemCount);
		}
		return synthObject.updateResult(SUCCESS);
	}
	
	/**
	 * 截取出字符串.
	 * 
	 * @param userItems		用户道具信息. 格式: 用户道具ID_数量|用户道具ID_数量|...
	 * @return
	 */
	private Map<Long, Integer> spliteUserItems(String userItems) {
		Map<Long, Integer> maps = new HashMap<Long, Integer>(2);
		List<String[]> arrays = Tools.delimiterString2Array(userItems);
		if(arrays != null && !arrays.isEmpty()) {
			for (String[] array : arrays) {
				Long userItemId = Long.valueOf(array[0]);
				Integer count = Integer.valueOf(array[1]);
				if(userItemId == null || count == null || count < 0) {
					continue;
				}
				
				Integer cacheCount = maps.get(userItemId);
				cacheCount = cacheCount == null ? 0 : cacheCount;
				maps.put(userItemId, count + cacheCount);
			}
		}
		return maps;
	}

	/**
	 * 把物品放入仓库中
	 * 
	 * @param  playerId					角色ID
	 * @param  goodsId					移动的用户道具/用户装备ID
	 * @param  goodsType				移动的物品类型
	 * @param  userPropsId				需要堆叠的物品ID.
	 * @param  index					物品的下标
	 * @return {@link ResultObject}		返回值封装对象
	 */
	
	public ResultObject<Collection<BackpackEntry>> put2Storage(long playerId, 
				long goodsId, int goodsType, long userPropsId, int index) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		if (player == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if (battle == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		} else if (battle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		}
		
		if(goodsType == GoodsType.EQUIP) {
			return putEquip2Storate(player, goodsId, index);
		} else if(goodsType == GoodsType.PROPS && userPropsId <= 0L) {
			return putProps2StorateNoStack(player, goodsId, index);
		} else if(goodsType == GoodsType.PROPS && userPropsId > 0L) {
			return putProps2StorateWithStack(player, goodsId, userPropsId);
		}
		return ResultObject.ERROR(TYPE_INVALID);
	}

	/**
	 * 把装备放入仓库中
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquipId				用户装备ID
	 * @return {@link ResultObject}		返回值对象
	 */
	private ResultObject<Collection<BackpackEntry>> putEquip2Storate(Player player, long userEquipId, int index) {
		long playerId = player.getId();
		int backpack = DEFAULT_BACKPACK;
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if(userEquip == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if(!userEquip.validBackpack(backpack)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userEquip.getCount() <= 0) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userEquip.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userEquip.isTrading()) {
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		}
		
		int oldPosition = userEquip.getIndex();
		int currStoreSize = propsManager.getBackpackSize(playerId, STORAGE_BACKPACK);
		ChainLock lock = LockUtils.getLock(player, player.getPackLock());
		try {
			lock.lock();
			if(!userEquip.validBackpack(backpack)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if(userEquip.getCount() <= 0) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			}
			
			int maxStorageSize = player.getMaxStoreSize();
			if(currStoreSize >= maxStorageSize) {
				return ResultObject.ERROR(BACKPACK_FULLED);
			}
			
			userEquip.setIndex(index);
			userEquip.setBackpack(STORAGE_BACKPACK);
			dbService.submitUpdate2Queue(userEquip);
			propsManager.changeUserEquipBackpack(playerId, backpack, STORAGE_BACKPACK, Arrays.asList(userEquip) );
		} finally {
			lock.unlock();
		}
		
		GoodsMoveLogger.log(player, backpack, STORAGE_BACKPACK, LoggerGoods.outcomeEquip(userEquipId, userEquip.getBaseId(), userEquip.getCount() ) );
		
		int baseId = userEquip.getBaseId();
		Quality quality = userEquip.getQuality();
		Collection<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		backpackEntries.add(BackpackEntry.valueEquipEmpty(userEquipId, baseId, backpack, quality, oldPosition, userEquip.isBinding()));
		backpackEntries.add(userEquip);
		return ResultObject.SUCCESS(backpackEntries);
	}

	/**
	 * 把装备从仓库中取出
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquipId				用户装备ID
	 * @param backpack 
	 * @return {@link ResultObject}		返回值对象
	 */
	private ResultObject<Collection<BackpackEntry>> checkEquipFromStorate(Player player, long userEquipId, int index, int backpack) {
		long playerId = player.getId();
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if(userEquip == null || userEquip.getCount() <= 0) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if(!userEquip.validBackpack(backpack)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userEquip.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userEquip.isTrading()) {
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		}
		
		int baseId = userEquip.getBaseId();
		int targetBackpack = DEFAULT_BACKPACK;
		int oldPosition = userEquip.getIndex();
		Quality quality = userEquip.getQuality();
		int currStoreSize = propsManager.getBackpackSize(playerId, targetBackpack);
		ChainLock lock = LockUtils.getLock(player.getPackLock());
		try {
			lock.lock();
			if(!userEquip.validBackpack(backpack)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if(userEquip.getCount() <= 0) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if(userEquip.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			} else if(userEquip.isTrading()) {
				return ResultObject.ERROR(EQUIP_CANNOT_USE);
			}
			
			if(!player.canAddNew2Backpack(currStoreSize + 1, DEFAULT_BACKPACK)) {
				return ResultObject.ERROR(BACKPACK_FULLED);
			}
			
			userEquip.setIndex(index);
			userEquip.setBackpack(targetBackpack);
			dbService.submitUpdate2Queue(userEquip);
			propsManager.changeUserEquipBackpack(playerId, backpack, targetBackpack, userEquip);
		} finally {
			lock.unlock();
		}

		GoodsMoveLogger.log(player, backpack, targetBackpack, LoggerGoods.outcomeEquip(userEquipId, baseId, userEquip.getCount() ) );
		
		Collection<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		backpackEntries.add(BackpackEntry.valueEquipEmpty(userEquipId, baseId, backpack, quality, oldPosition, userEquip.isBinding()));
		backpackEntries.add(voFactory.getUserEquipEntry(userEquip));
		return ResultObject.SUCCESS(backpackEntries);
	}
	
	/**
	 * 把用户道具放入仓库中
	 * 
	 * @param  player				角色对象
	 * @param  userPropsId			用户道具ID
	 * @param  targetPropsId		被堆叠的用户道具ID
	 * @param  amount				取出的数量
	 * @return {@link ResultObject}	用户模块返回值
	 */
	private ResultObject<Collection<BackpackEntry>> checkPropsFromStorateStack(Player player,
			long userPropsId, long targetPropsId, int amount , int backpack) {
		long playerId = player.getId();
		int targetPack = DEFAULT_BACKPACK;
		UserProps userProps = propsManager.getUserProps(userPropsId);
		if(userProps == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(userProps.getCount() <= 0) {
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		} else if(!userProps.validBackpack(backpack)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userProps.isTrading()) {
			return ResultObject.ERROR(ITEM_CANNOT_USE);
		} else if(userProps.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(amount <= 0) {
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		UserProps targetProps = propsManager.getUserProps(targetPropsId);
		if(targetProps == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(targetProps.getCount() <= 0) {
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		} else if(!targetProps.validBackpack(targetPack)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(targetProps.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		}
		
		int sourceBaseId = userProps.getBaseId();
		int targetBaseId = targetProps.getBaseId();
		if(sourceBaseId != targetBaseId) {
			return ResultObject.ERROR(TYPE_INVALID);
		} else if(!userProps.isSameExpiration(targetProps.getExpiration())) {
			return ResultObject.ERROR(TYPE_INVALID);
		}
		
		PropsConfig base = propsManager.getPropsConfig(sourceBaseId);
		if(base == null) {
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		
		int maxAmount = base.getMaxAmount();
		ChainLock lock = LockUtils.getLock(userProps, targetProps);
		try {
			lock.lock();
			int sourceCount = userProps.getCount();
			int targetCount = userProps.getCount();
			if(sourceCount < amount || targetCount <= 0) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			} else if(!userProps.validBackpack(backpack) || !targetProps.validBackpack(targetPack)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if(userProps.getPlayerId() != playerId || targetProps.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			} 
			
			if(targetCount + amount > maxAmount) {
				return ResultObject.ERROR(MAX_STACK_INVALID);
			}
			
			userProps.decreaseItemCount(amount);
			targetProps.increaseItemCount(amount);
			propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, userProps);
		} finally {
			lock.unlock();
		}
		
		GoodsMoveLogger.log(player, backpack, targetPack, LoggerGoods.outcomeProps(userPropsId, userProps.getBaseId(), amount ) );
		
		dbService.submitUpdate2Queue(userProps, targetProps);
		
		Collection<BackpackEntry> entries = voFactory.getUserPropsEntries(userProps, targetProps);
		return ResultObject.SUCCESS(entries);
	}
	
	/**
	 * 把用户道具放入仓库中
	 * 
	 * @param  player				角色对象
	 * @param  userPropsId			用户道具ID
	 * @param  targetPropsId		被堆叠的用户道具ID
	 * @return {@link ResultObject}	用户模块返回值
	 */
	private ResultObject<Collection<BackpackEntry>> putProps2StorateWithStack(Player player,
			long userPropsId, long targetPropsId) {
		long playerId = player.getId();
		int backpack = DEFAULT_BACKPACK;
		int targetPack = STORAGE_BACKPACK;
		UserProps userProps = propsManager.getUserProps(userPropsId);
		if(userProps == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(userProps.getCount() <= 0) {
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		} else if(!userProps.validBackpack(backpack)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userProps.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userProps.isTrading()) {
			return ResultObject.ERROR(ITEM_CANNOT_USE);
		}
		
		UserProps targetProps = propsManager.getUserProps(targetPropsId);
		if(targetProps == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(targetProps.getCount() <= 0) {
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		} else if(!targetProps.validBackpack(targetPack)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(targetProps.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(targetProps.isTrading()) {
			return ResultObject.ERROR(ITEM_CANNOT_USE);
		}
		
		int sourceBaseId = userProps.getBaseId();
		int targetBaseId = targetProps.getBaseId();
		if(sourceBaseId != targetBaseId) {
			return ResultObject.ERROR(TYPE_INVALID);
		} else if(!userProps.isSameExpiration(targetProps.getExpiration())) {
			return ResultObject.ERROR(TYPE_INVALID);
		}
		
		PropsConfig base = propsManager.getPropsConfig(sourceBaseId);
		if(base == null) {
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		
		int maxAmount = base.getMaxAmount();
		ChainLock lock = LockUtils.getLock(player.getPackLock());
		try {
			lock.lock();
			int sourceCount = userProps.getCount();
			int targetCount = userProps.getCount();
			if(sourceCount <= 0 || targetCount <= 0) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			} else if(!userProps.validBackpack(backpack) || !targetProps.validBackpack(targetPack)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if(userProps.getPlayerId() != playerId || targetProps.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			} else if(userProps.isTrading() || targetProps.isTrading()) {
				return ResultObject.ERROR(ITEM_CANNOT_USE);
			}
			
			int canAttackCount = Math.min(maxAmount - targetCount, sourceCount);
			if(canAttackCount <= 0) {
				return ResultObject.ERROR(MAX_STACK_INVALID);
			}
			
			userProps.decreaseItemCount(canAttackCount);
			targetProps.increaseItemCount(canAttackCount);
			propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, userProps);
			GoodsMoveLogger.log(player, backpack, STORAGE_BACKPACK, LoggerGoods.outcomeProps(userPropsId, userProps.getBaseId(), canAttackCount ) );
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(userProps, targetProps);
		
		Collection<BackpackEntry> entries = voFactory.getUserPropsEntries(userProps, targetProps);
		return ResultObject.SUCCESS(entries);
	}

	/**
	 * 把用户道具放入仓库中
	 * 
	 * @param  player				角色对象
	 * @param  userPropsId			用户道具ID
	 * @param  index				下标
	 * @return {@link ResultObject}	用户模块返回值
	 */
	private ResultObject<Collection<BackpackEntry>> putProps2StorateNoStack
							(Player player,	long userPropsId, int index) {
		long playerId = player.getId();
		int backpack = DEFAULT_BACKPACK;
		UserProps userProps = propsManager.getUserProps(userPropsId);
		if(userProps == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(userProps.getCount() <= 0) {
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		} else if(!userProps.validBackpack(backpack)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userProps.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userProps.isTrading()) {
			return ResultObject.ERROR(ITEM_CANNOT_USE);
		}
		
		int oldPosition = userProps.getIndex();
		int currStoreSize = propsManager.getBackpackSize(playerId, STORAGE_BACKPACK);
		ChainLock lock = LockUtils.getLock(player, player.getPackLock());
		try {
			lock.lock();
			if(userProps.getCount() <= 0) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			} else if(!userProps.validBackpack(backpack)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if(userProps.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			}
			
			int maxStorageSize = player.getMaxStoreSize();
			if(currStoreSize >= maxStorageSize) {
				return ResultObject.ERROR(BACKPACK_FULLED);
			}
			
			userProps.setIndex(index);
			userProps.setBackpack(STORAGE_BACKPACK);
			dbService.submitUpdate2Queue(userProps);
			propsManager.changeUserPropsBackpack(playerId, backpack, STORAGE_BACKPACK, userProps);
		} finally {
			lock.unlock();
		}
		
		int baseId = userProps.getBaseId();
		GoodsMoveLogger.log(player, backpack, STORAGE_BACKPACK, LoggerGoods.outcomeProps(userPropsId, baseId, userProps.getCount() ) );
		
		Quality quality = userProps.getQuality();
		Collection<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		backpackEntries.add(BackpackEntry.valueProps(userPropsId, baseId, 0, backpack, quality, oldPosition, userProps.isBinding()));
		backpackEntries.add(voFactory.getUserPropsEntry(userProps));
		return ResultObject.SUCCESS(backpackEntries);
	}

	/**
	 * 把用户道具放入仓库中
	 * 
	 * @param  player				角色对象
	 * @param  userPropsId			用户道具ID
	 * @param  index				下标
	 * @return {@link ResultObject}	用户模块返回值
	 */
	private ResultObject<Collection<BackpackEntry>> checkPropsFromStorateNoStack(
						Player player, long userPropsId, int amount, int index ,int backpack) {
		long playerId = player.getId();
		UserProps userProps = propsManager.getUserProps(userPropsId);
		if(userProps == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(userProps.getCount() <= 0) {
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		} else if(!userProps.validBackpack(backpack)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userProps.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userProps.isTrading()) {
			return ResultObject.ERROR(ITEM_CANNOT_USE);
		} else if(amount <= 0) {
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
//		boolean clearCache = false;
		UserProps createUserProps = null;
		int targetBackpack = DEFAULT_BACKPACK;
		int currBackpackSize = propsManager.getBackpackSize(playerId, targetBackpack);
		ChainLock lock = LockUtils.getLock(player.getPackLock());
		try {
			lock.lock();
			if(userProps.getCount() < amount) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			} else if(!userProps.validBackpack(backpack)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if(userProps.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			} else if(!player.canAddNew2Backpack(currBackpackSize + 1, DEFAULT_BACKPACK)) {
				return ResultObject.ERROR(BACKPACK_FULLED);
			}
			
			createUserProps = new UserProps();
			createUserProps.setIndex(index);
			createUserProps.setCount(amount);
			createUserProps.setPlayerId(playerId);
			createUserProps.setBackpack(targetBackpack);
			createUserProps.setBaseId(userProps.getBaseId());
			createUserProps.setBinding(userProps.isBinding());
			createUserProps.setQuality(userProps.getQuality());
			createUserProps.setGoodsType(userProps.getGoodsType());
			createUserProps.setExpiration(userProps.getExpiration());
			createUserProps = propsManager.createUserProps(createUserProps);
//			clearCache = userProps.getCount() <= 0;
			propsManager.put2UserPropsIdsList(playerId, targetBackpack, createUserProps);
			userProps.decreaseItemCount(amount);
			propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, userProps);
		} catch (Exception e) {
			return ResultObject.ERROR(FAILURE);
		} finally {
			lock.unlock();
		}
		
		GoodsMoveLogger.log(player, backpack, targetBackpack, LoggerGoods.outcomeProps(userPropsId, userProps.getBaseId(), amount ) );
		dbService.submitUpdate2Queue(userProps);
//		if(clearCache) {
//			propsManager.removeFromUserPropsIdsList(playerId, backpack, userProps);
//		}
		Collection<BackpackEntry> backpackEntries = voFactory.getUserPropsEntries(userProps, createUserProps);
		return ResultObject.SUCCESS(backpackEntries);
	}
	
	/**
	 * 从仓库中取出物品
	 * 
	 * @param  playerId					角色ID
	 * @param  goodsId					移动的用户道具/用户装备ID
	 * @param  goodsType				移动的物品类型
	 * @param  amount					需要移动的数量
	 * @param  userPropsId				需要堆叠的物品ID.
	 * @return {@link ResultObject}		返回值封装对象
	 */
	
	public ResultObject<Collection<BackpackEntry>> checkoutFromStorage(long playerId, 
			long goodsId, int goodsType, int amount, long userPropsId, int index) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		if (player == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if (battle == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		} else if (battle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		}
		
		if(goodsType == GoodsType.EQUIP) {
			return this.checkEquipFromStorate(player, goodsId, index,BackpackType.STORAGE_BACKPACK);
		} else if(goodsType == GoodsType.PROPS && userPropsId <= 0L) {	//没有需要堆叠的, 放在在空格上
			return checkPropsFromStorateNoStack(player, goodsId, amount, index , BackpackType.STORAGE_BACKPACK);
		} else if(goodsType == GoodsType.PROPS && userPropsId > 0L) {	//有需要堆叠的, 放到堆叠的道具上
			return checkPropsFromStorateStack(player, goodsId, userPropsId, amount,BackpackType.STORAGE_BACKPACK);
		}
		return ResultObject.ERROR(TYPE_INVALID);
	}

	/**
	 * 交换物品的背包号
	 * 
	 * @param  playerId					角色ID
	 * @param  goodsId					发起交换的物品ID
	 * @param  goodsType				发起交换的物品类型
	 * @param  targetId					被交换的物品ID
	 * @param  targetType				被交换的物品ID
	 * @return {@link ResultObject}		返回值封装对象
	 */
	
	public ResultObject<Collection<BackpackEntry>> swapBackpack(long playerId, 
						long goodsId, int goodsType, long targetId, int targetType) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		if (player == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if (battle == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		} else if (battle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		}

		if(goodsType == GoodsType.EQUIP && targetType == GoodsType.EQUIP) {
			return processEquipSwapEquip(player, goodsId, targetId);
		} else if(goodsType == GoodsType.EQUIP && targetType == GoodsType.PROPS) {
			return processEquipSwapProps(player, goodsId, targetId);
		} else if(goodsType == GoodsType.PROPS && targetType == GoodsType.EQUIP) {
			return processPropsSwapEquip(player, goodsId, targetId);
		} else if(goodsType == GoodsType.PROPS && targetType == GoodsType.PROPS) {
			return processPropsSwapProps(player, goodsId, targetId);
		}
		
		return ResultObject.ERROR(TYPE_INVALID);
	}

	/**
	 * 处理道具和道具互换背包号
	 * 
	 * @param  player					角色对象
	 * @param  userPropsId				用户道具ID
	 * @param  targetPropsId			目标用户道具ID
	 * @return {@link ResultObject}		返回值对象
	 */
	private ResultObject<Collection<BackpackEntry>> processPropsSwapProps(Player 
			player,	long userPropsId, long targetPropsId) {
		long playerId = player.getId();
		UserProps sourceProps = propsManager.getUserProps(userPropsId);
		if(sourceProps == null || sourceProps.getCount() <= 0) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(!sourceProps.validBackpack(SWAP_BACKPACK_BACKPACKS)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(sourceProps.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(sourceProps.isTrading()) {
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		}
		
		UserProps targetProps = propsManager.getUserProps(targetPropsId);
		if(targetProps == null || targetProps.getCount() <= 0) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(!targetProps.validBackpack(SWAP_BACKPACK_BACKPACKS)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(targetProps.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(targetProps.isTrading()) {
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		}
		
		if(userPropsId == targetPropsId) {
			return ResultObject.ERROR(FAILURE);
		} 

		int sourceBackpack = sourceProps.getBackpack();
		int targetBackpack = targetProps.getBackpack();
		if(sourceBackpack == targetBackpack) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int sourceIndex = -1;
		int targetIndex = -1;
		ChainLock lock = LockUtils.getLock(player.getPackLock());
		try {
			lock.lock();
			if(sourceProps.getCount() <= 0 || targetProps.getCount() <= 0) {
				return ResultObject.ERROR(ITEM_NOT_FOUND);
			} else if(sourceProps.getPlayerId() != playerId || targetProps.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			} else if(!sourceProps.validBackpack(SWAP_BACKPACK_BACKPACKS) || !targetProps.validBackpack(SWAP_BACKPACK_BACKPACKS)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} 

			sourceBackpack = sourceProps.getBackpack();
			targetBackpack = targetProps.getBackpack();
			if(sourceBackpack == targetBackpack) {
				return ResultObject.ERROR(FAILURE);
			}
			
			sourceIndex = sourceProps.getIndex();
			targetIndex = targetProps.getIndex();
			sourceProps.setIndex(targetIndex);
			targetProps.setIndex(sourceIndex);
			sourceProps.setBackpack(targetBackpack);
			targetProps.setBackpack(sourceBackpack);
			
			propsManager.changeUserPropsBackpack(playerId, sourceBackpack, targetBackpack, sourceProps);
			propsManager.changeUserPropsBackpack(playerId, targetBackpack, sourceBackpack, targetProps);
		} finally {
			lock.unlock();
		}
		
		int sourceBaseId = sourceProps.getBaseId();
		int targetBaseId = targetProps.getBaseId();
		Quality sourceQuality = sourceProps.getQuality();
		Quality targetQuality = targetProps.getQuality();
//		dbService.updateEntityIntime(sourceProps, targetProps);
		dbService.submitUpdate2Queue(sourceProps, targetProps);
//		propsManager.removeUserPropsIdList(playerId, sourceBackpack, targetBackpack);
//		propsManager.changeUserPropsBackpack(playerId, sourceBackpack, targetBackpack, sourceProps);
//		propsManager.changeUserPropsBackpack(playerId, targetBackpack, sourceBackpack, targetProps);
		
		Collection<BackpackEntry> entries = new ArrayList<BackpackEntry>();
//      传递到构造方法的参数错误,导致物品丢失 bug liuyuhua 2012-04-06
//		entries.add(BackpackEntry.valueProps(userPropsId, sourceBaseId, sourceBackpack, 0, sourceQuality, sourceIndex));
//		entries.add(BackpackEntry.valueProps(targetPropsId, targetBaseId, targetBackpack, 0, targetQuality, targetIndex));
		entries.add(BackpackEntry.valueProps(userPropsId, sourceBaseId, 0, sourceBackpack, sourceQuality, sourceIndex, sourceProps.isBinding()));
		entries.add(BackpackEntry.valueProps(targetPropsId, targetBaseId, 0, targetBackpack, targetQuality, targetIndex, targetProps.isBinding()));
		entries.addAll(voFactory.getUserPropsEntries(sourceProps, targetProps));
		return ResultObject.SUCCESS(entries);
	}

	
	/**
	 * 处理用户道具和用户装备互换背包号  (2012-04-06 新增 liuyuhua)
	 * 
	 * @param player                   角色对象
	 * @param userPropsId              用户道具ID
	 * @param userEquipId              用户装备ID
	 * @return {@link ResultObject}    返回值对象
	 */
	private ResultObject<Collection<BackpackEntry>> processPropsSwapEquip(Player 
			player,	long userPropsId, long userEquipId) {
		
		long playerId = player.getId();
		UserProps userProps = propsManager.getUserProps(userPropsId);
		if(userProps == null || userProps.getCount() <= 0) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(!userProps.validBackpack(SWAP_BACKPACK_BACKPACKS)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userProps.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userProps.isTrading()) {
			return ResultObject.ERROR(ITEM_CANNOT_USE);
		}
		
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if(userEquip == null || userEquip.getCount() <= 0) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(!userEquip.validBackpack(SWAP_BACKPACK_BACKPACKS)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userEquip.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userEquip.isTrading()) {
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		}
		
		int propsBackpack = userProps.getBackpack();
		int equipBackpack = userEquip.getBackpack();
		
		if(equipBackpack == propsBackpack) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int equipIndex = -1;
		int propsIndex = -1;
		ChainLock lock = LockUtils.getLock(player.getPackLock());
		try {
			lock.lock();
			if(userEquip.getCount() <= 0) {
				return ResultObject.ERROR(EQUIP_NOT_FOUND);
			} else if(userProps.getCount() <= 0) {
				return ResultObject.ERROR(ITEM_NOT_FOUND);
			} else if(userEquip.getPlayerId() != playerId || userProps.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			} else if(!userEquip.validBackpack(SWAP_BACKPACK_BACKPACKS) 
				   || !userProps.validBackpack(SWAP_BACKPACK_BACKPACKS)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} 

			propsBackpack = userProps.getBackpack();
			equipBackpack = userEquip.getBackpack();
			if(equipBackpack == propsBackpack) {
				return ResultObject.ERROR(FAILURE);
			}
			
			propsIndex = userProps.getIndex();
			equipIndex = userEquip.getIndex();
			userProps.setIndex(equipIndex);
			userEquip.setIndex(propsIndex);
			userProps.setBackpack(equipBackpack);
			userEquip.setBackpack(propsBackpack);
			
			propsManager.changeUserPropsBackpack(playerId, propsBackpack, equipBackpack, userProps);
			propsManager.changeUserEquipBackpack(playerId, equipBackpack, propsBackpack, userEquip);
		} finally {
			lock.unlock();
		}
		
		int propsBaseId = userProps.getBaseId();
		int equipBaseId = userEquip.getBaseId();
		Quality propsQuality = userProps.getQuality();
		Quality equipQuality = userEquip.getQuality();
		dbService.submitUpdate2Queue(userProps, userEquip);
		Collection<BackpackEntry> entries = new ArrayList<BackpackEntry>();
		entries.add(BackpackEntry.valueProps(userPropsId, propsBaseId, 0, propsBackpack, propsQuality, propsIndex, userProps.isBinding()));
		entries.add(BackpackEntry.valueEquip(userEquipId, equipBaseId, 0, equipBackpack, equipQuality, equipIndex, userEquip.isBinding()));
		entries.add(userEquip);
		entries.add(userProps);
		
		return ResultObject.SUCCESS(entries);
	}
	
	
	/**
	 * 处理用户装备和用户道具互换背包号
	 * 
	 * @param  player					角色对象
	 * @param  userEquipId				用户装备ID
	 * @param  userPropsId				用户道具ID
	 * @return {@link ResultObject}		返回值对象
	 */
	private ResultObject<Collection<BackpackEntry>> processEquipSwapProps(Player 
									player,	long userEquipId, long userPropsId) {
		long playerId = player.getId();
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if(userEquip == null || userEquip.getCount() <= 0) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(!userEquip.validBackpack(SWAP_BACKPACK_BACKPACKS)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userEquip.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userEquip.isTrading()) {
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		}
		
		UserProps userProps = propsManager.getUserProps(userPropsId);
		if(userProps == null || userProps.getCount() <= 0) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(!userProps.validBackpack(SWAP_BACKPACK_BACKPACKS)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userProps.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userProps.isTrading()) {
			return ResultObject.ERROR(ITEM_CANNOT_USE);
		}
		
		int equipBackpack = userEquip.getBackpack();
		int propsBackpack = userProps.getBackpack();
		if(equipBackpack == propsBackpack) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int equipIndex = -1;
		int propsIndex = -1;
		ChainLock lock = LockUtils.getLock(player.getPackLock());
		try {
			lock.lock();
			if(userEquip.getCount() <= 0) {
				return ResultObject.ERROR(EQUIP_NOT_FOUND);
			} else if(userProps.getCount() <= 0) {
				return ResultObject.ERROR(ITEM_NOT_FOUND);
			} else if(userEquip.getPlayerId() != playerId || userProps.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			} else if(!userEquip.validBackpack(SWAP_BACKPACK_BACKPACKS) 
				   || !userProps.validBackpack(SWAP_BACKPACK_BACKPACKS)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} 

			equipBackpack = userEquip.getBackpack();
			propsBackpack = userProps.getBackpack();
			if(equipBackpack == propsBackpack) {
				return ResultObject.ERROR(FAILURE);
			}
			
			equipIndex = userEquip.getIndex();
			propsIndex = userProps.getIndex();
			userEquip.setIndex(propsIndex);
			userProps.setIndex(equipIndex);
			userEquip.setBackpack(propsBackpack);
			userProps.setBackpack(equipBackpack);
			
			propsManager.changeUserEquipBackpack(playerId, equipBackpack, propsBackpack, userEquip);
			propsManager.changeUserPropsBackpack(playerId, propsBackpack, equipBackpack, userProps);
		} finally {
			lock.unlock();
		}
		
		int equipBaseId = userEquip.getBaseId();
		int propsBaseId = userProps.getBaseId();
		Quality equipQuality = userEquip.getQuality();
		Quality propsQuality = userProps.getQuality();
//		dbService.updateEntityIntime(userProps, userEquip);
		dbService.submitUpdate2Queue(userProps, userEquip);
//		propsManager.removeUserEquipIdList(playerId, equipBackpack, propsBackpack);
//		propsManager.removeUserPropsIdList(playerId, equipBackpack, propsBackpack);
//		propsManager.changeUserEquipBackpack(playerId, equipBackpack, propsBackpack, userEquip);
//		propsManager.changeUserPropsBackpack(playerId, propsBackpack, equipBackpack, userProps);
		
		Collection<BackpackEntry> entries = new ArrayList<BackpackEntry>();
//      传递到构造方法的参数错误,导致物品丢失 bug liuyuhua 2012-04-06
//		entries.add(BackpackEntry.valueProps(userPropsId, propsBaseId, propsBackpack, 0, propsQuality, propsIndex));
//		entries.add(BackpackEntry.valueEquip(userEquipId, equipBaseId, equipBackpack, 0, equipQuality, equipIndex));
		entries.add(BackpackEntry.valueProps(userPropsId, propsBaseId, 0, propsBackpack, propsQuality, propsIndex, userProps.isBinding()));
		entries.add(BackpackEntry.valueEquip(userEquipId, equipBaseId, 0, equipBackpack, equipQuality, equipIndex, userEquip.isBinding()));
		entries.add(userEquip);
		entries.add(userProps);
		return ResultObject.SUCCESS(entries);
	}

	/**
	 * 处理用户装备和用户装备互换背包号
	 * 
	 * @param  player					角色对象
	 * @param  userPropsId				用户装备ID
	 * @param  targetPropsId			目标用户装备ID
	 * @return {@link ResultObject}		
	 */
	private ResultObject<Collection<BackpackEntry>> processEquipSwapEquip(Player 
								player,	long userEquipId, long targetEquipId) {
		long playerId = player.getId();
		UserEquip sourceEquip = propsManager.getUserEquip(userEquipId);
		if(sourceEquip == null || sourceEquip.getCount() <= 0) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(!sourceEquip.validBackpack(SWAP_BACKPACK_BACKPACKS)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(sourceEquip.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(sourceEquip.isTrading()) {
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		}
		
		UserEquip targetEquip = propsManager.getUserEquip(targetEquipId);
		if(targetEquip == null || targetEquip.getCount() <= 0) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(!targetEquip.validBackpack(SWAP_BACKPACK_BACKPACKS)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(targetEquip.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(targetEquip.isTrading()) {
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		}
		
		if(userEquipId == targetEquipId) {
			return ResultObject.ERROR(FAILURE);
		} 

		int sourceBackpack = sourceEquip.getBackpack();
		int targetBackpack = targetEquip.getBackpack();
		if(sourceBackpack == targetBackpack) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int sourceIndex = -1;
		int targetIndex = -1;
		ChainLock lock = LockUtils.getLock(player.getPackLock());
		try {
			lock.lock();
			if(sourceEquip.getCount() <= 0 || targetEquip.getCount() <= 0) {
				return ResultObject.ERROR(ITEM_NOT_FOUND);
			} else if(sourceEquip.getPlayerId() != playerId || targetEquip.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			} else if(!sourceEquip.validBackpack(SWAP_BACKPACK_BACKPACKS) || !targetEquip.validBackpack(SWAP_BACKPACK_BACKPACKS)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} 

			sourceBackpack = sourceEquip.getBackpack();
			targetBackpack = targetEquip.getBackpack();
			if(sourceBackpack == targetBackpack) {
				return ResultObject.ERROR(FAILURE);
			}
			
			sourceIndex = sourceEquip.getIndex();
			targetIndex = targetEquip.getIndex();
			sourceEquip.setIndex(targetIndex);
			targetEquip.setIndex(sourceIndex);
			sourceEquip.setBackpack(targetBackpack);
			targetEquip.setBackpack(sourceBackpack);
			
			propsManager.changeUserEquipBackpack(playerId, sourceBackpack, targetBackpack, sourceEquip);
			propsManager.changeUserEquipBackpack(playerId, targetBackpack, sourceBackpack, targetEquip);
		} finally {
			lock.unlock();
		}
		
		int sourceBaseId = sourceEquip.getBaseId();
		int targetBaseId = targetEquip.getBaseId();
		Quality sourceQuality = sourceEquip.getQuality();
		Quality targetQuality = targetEquip.getQuality();
//		dbService.updateEntityIntime(sourceEquip, targetEquip);
		dbService.submitUpdate2Queue(sourceEquip, targetEquip);
//		propsManager.removeUserEquipIdList(playerId, sourceBackpack, targetBackpack);
//		propsManager.changeUserEquipBackpack(playerId, sourceBackpack, targetBackpack, sourceEquip);
//		propsManager.changeUserEquipBackpack(playerId, targetBackpack, sourceBackpack, targetEquip);
		
		Collection<BackpackEntry> entries = new ArrayList<BackpackEntry>();
//      传递到构造方法的参数错误,导致物品丢失 bug liuyuhua 2012-04-06
//		entries.add(BackpackEntry.valueEquip(userEquipId, sourceBaseId, sourceBackpack, 0, sourceQuality, sourceIndex));
//		entries.add(BackpackEntry.valueEquip(targetEquipId, targetBaseId, targetBackpack, 0, targetQuality, targetIndex));
		entries.add(BackpackEntry.valueEquip(userEquipId, sourceBaseId, 0, sourceBackpack, sourceQuality, sourceIndex, sourceEquip.isBinding()));
		entries.add(BackpackEntry.valueEquip(targetEquipId, targetBaseId, 0, targetBackpack, targetQuality, targetIndex, targetEquip.isBinding()));
		entries.addAll(voFactory.getUserEquipEntries(sourceEquip, targetEquip));
		return ResultObject.SUCCESS(entries);
	}

	/**
	 * 扩展背包
	 * 
	 * @param playerId                  角色ID
	 * @param userItems                 扩展符用户道具信息, 格式: 用户道具ID_数量|...
	 * @param autoBuyCount              自动购买道具数量
	 * @param backpack                  背包类型
	 * @return {@link ResultObject}     返回值封装对象
	 */
	
	public ResultObject<Collection<BackpackEntry>> expandBackpack(long playerId, 
			                String userItems, int autoBuyCount, int backpack) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		if (battle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		} else if (autoBuyCount < 0) {
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		int requiredCount   = 0;
		int maxBackpackSize = 0;
		int pageBackSize    = PlayerRule.PAGE_BACKPACK_SIZE;
		if (backpack == DEFAULT_BACKPACK) {
			maxBackpackSize = player.getMaxBackSize();
		} else if (backpack == STORAGE_BACKPACK) {
			maxBackpackSize = player.getMaxStoreSize();
		} else {
			return ResultObject.ERROR(TYPE_INVALID);
		}
		
		if (maxBackpackSize >= PlayerRule.MAX_BACKPACK_SIZE) {
			return ResultObject.ERROR(BACKPACK_PAGE_LIMIT);
		}
		
		List<LoggerGoods> goodsLoggers = new ArrayList<LoggerGoods>(2);
		Map<Long, Integer> userBackItems = this.spliteUserItems(userItems);
		for (Map.Entry<Long, Integer> entry : userBackItems.entrySet()) {
			long userPropsId = entry.getKey();
			int propsCount   = entry.getValue();
			
			if (propsCount <= 0) {
				return ResultObject.ERROR(INPUT_VALUE_INVALID);
			}
			
			UserProps userProps = propsManager.getUserProps(userPropsId);
			if (userProps == null) {
				return ResultObject.ERROR(ITEM_NOT_FOUND);
			} else if (userProps.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			} else if (userProps.getBackpack() != DEFAULT_BACKPACK) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if (userProps.getCount() < propsCount) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			} else if(userProps.isTrading()) {
				return ResultObject.ERROR(ITEM_CANNOT_USE);
			}
			
			int baseId = userProps.getBaseId();
			int childType = PropsChildType.CONTAINER_PAGE_TYPE;
			PropsConfig config = propsManager.getPropsConfig(baseId);
			if (config == null) {
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			} else if (config.getChildType() != childType) {
				return ResultObject.ERROR(TYPE_INVALID);
			}
			
			requiredCount += propsCount;
			goodsLoggers.add(LoggerGoods.outcomeProps(userPropsId, baseId, propsCount));
		}
		
		int backpackPage        = maxBackpackSize / pageBackSize;
		int nextBackSize        = pageBackSize + maxBackpackSize;
		
		int expandBackpackCost = 0;//自动购买的价格
		PropsConfig propsConfig = propsManager.getPropsConfig(PropsRule.OPEN_PET_SOLT_PROPS_BASE_ID);
		if(propsConfig == null){
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		expandBackpackCost = propsConfig.getMallPrice() * autoBuyCount;
		
		int expandBackpackCount = PropsRule.getExpandBackpackItemCount(backpackPage, backpack);
		if (player.getGolden() < expandBackpackCost) {
			return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
		} else if (expandBackpackCount != requiredCount + autoBuyCount) {
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		}
		
		ChainLock lock = LockUtils.getLock(player.getPackLock());
		try {
			lock.lock();
			
			if (backpack == DEFAULT_BACKPACK) {
				maxBackpackSize = player.getMaxBackSize();
			} else if (backpack == STORAGE_BACKPACK) {
				maxBackpackSize = player.getMaxStoreSize();
			}
			
			if (maxBackpackSize >= PlayerRule.MAX_BACKPACK_SIZE) {
				return ResultObject.ERROR(BACKPACK_PAGE_LIMIT);
			}
			
			backpackPage        = maxBackpackSize / pageBackSize;
			nextBackSize        = pageBackSize + maxBackpackSize;
			expandBackpackCost  = PropsRule.getExpandBackAutoBuyCostGold(autoBuyCount);
			expandBackpackCount = PropsRule.getExpandBackpackItemCount(backpackPage, backpack);
			if (player.getGolden() < expandBackpackCost) {
				return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
			} else if (expandBackpackCount != requiredCount + autoBuyCount) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			}
			
			if (backpack == DEFAULT_BACKPACK) {
				player.setMaxBackSize(nextBackSize);
			} else if (backpack == STORAGE_BACKPACK) {
				player.setMaxStoreSize(nextBackSize);
			}
			player.decreaseGolden(expandBackpackCost);
			if(expandBackpackCost > 0){
				goodsLoggers.add(LoggerGoods.outcomePropsAutoBuyGolden(PropsRule.OPEN_PET_SOLT_PROPS_BASE_ID, autoBuyCount, expandBackpackCost));
			}
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(player);
		List<UserProps> userPropsList = propsManager.costUserPropsList(userBackItems);
		Collection<BackpackEntry> backpackEntries = voFactory.getUserPropsEntries(userPropsList);
		LoggerGoods[] goodsLoggerArray = goodsLoggers.toArray(new LoggerGoods[goodsLoggers.size()]);
		if(goodsLoggerArray.length > 0) {
			GoodsLogger.goodsLogger(player, Source.BACKPACK_EXPAND, goodsLoggerArray);
		}
		if(expandBackpackCost != 0){
			GoldLogger.outCome(Source.BACKPACK_EXPAND, expandBackpackCost, player, goodsLoggerArray);
		}
		return ResultObject.SUCCESS(backpackEntries);
	}

	/**
	 * 计算修理装备需要的费用
	 * 
	 * @param  playerId					角色ID
	 * @param  backpackInfos			背包信息. 格式: 背包1_背包2_...
	 * @return {@link Integer}			需要扣除的货币信息
	 */
	
	public int calcRepairCostSilver(long playerId, String backpackInfos) {
		return calculateRepairSilver(getBackpackEquips(playerId, backpackInfos));
	}

	/**
	 * 计算修理装备需要的费用
	 * 
	 * @param  backpackEquips			背包信息
	 * @return {@link Integer}			需要扣除的货币信息
	 */
	private int calculateRepairSilver(Collection<UserEquip> backpackEquips) {
		int totalCost = 0;
		int formulaKey = FormulaKey.EQUIP_REPAIR_COST_SILVER;
		for (UserEquip userEquip : backpackEquips) {
			int currentEndure = userEquip.getCurrentEndure();
			EquipConfig equipConfig = userEquip.getEquipConfig();
			int currentMaxEndure = userEquip.getCurrentMaxEndure();
			int silverPrice = equipConfig == null ? 0 : equipConfig.getSilverPrice();
			totalCost += FormulaHelper.invoke(formulaKey, currentEndure, currentMaxEndure, silverPrice).intValue();
		}
		return totalCost;
	}
	
	/**
	 * 查询角色的装备列表 
	 * 
	 * @param  playerId				角色ID
	 * @param  backpackInfos		背包信息
	 * @return {@link Collection}	装备列表
	 */
	private Collection<UserEquip> getBackpackEquips(long playerId, String backpackInfos) {
		if(StringUtils.isBlank(backpackInfos)) {
			return Collections.emptySet();
		}
		
		Set<Integer> backpackSet = new HashSet<Integer>();
		for (String element : backpackInfos.split(Splitable.ATTRIBUTE_SPLIT)) {
			Integer backpack = Integer.valueOf(element);
			if(ArrayUtils.contains(REPAIR_BACKPACKS, backpack)) {
				backpackSet.add(backpack);
			}
		}
		
		Collection<UserEquip> userEquips = new HashSet<UserEquip>();
		for (Integer backpack : backpackSet) {
			List<UserEquip> cacheEquips = propsManager.listUserEquip(playerId, backpack);
			if(cacheEquips != null && !cacheEquips.isEmpty()) {
				userEquips.addAll(cacheEquips);
			}
		}
		return userEquips;
		
	}
	
	/**
	 * 修理角色的装备
	 * 
	 * @param  playerId					角色ID
	 * @param  backpackInfos			背包信息
	 * @return {@link Integer}			返回值信息
	 */
	
	public int repairUserEquips(long playerId, String backpackInfos) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		Collection<UserEquip> userEquips = this.getBackpackEquips(playerId, backpackInfos);
		if(userEquips == null || userEquips.isEmpty()) {
			return NO_EQUIP_2_REPAIR;
		}
		
		int costSilver = 0;
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		Set<EndureInfo> endureInfos = new HashSet<EndureInfo>();
		ChainLock lock = LockUtils.getLock(player, battle);
		try {
			lock.lock();
			costSilver = this.calculateRepairSilver(userEquips);
			if(costSilver <= 0) {
				return NO_EQUIP_2_REPAIR;
			}
			
			if(player.getSilver() < costSilver) {
				return SILVER_NOT_ENOUGH;
			}
			
			boolean hasZeroEquip = false;
			player.decreaseSilver(costSilver);
			for (UserEquip userEquip : userEquips) {
				int currentEndure = userEquip.getCurrentEndure();
				if(currentEndure <= 0 && userEquip.validBackpack(DRESSED_BACKPACK)) {
					hasZeroEquip = true;
				}
				userEquip.updateCurrentEndure2Max();
				long userEquipId = userEquip.getId();
				int backpack = userEquip.getBackpack();
				currentEndure = userEquip.getCurrentEndure();
				endureInfos.add(EndureInfo.valueOf(userEquipId, backpack, currentEndure));
			}
			
			if(hasZeroEquip) {
				battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
			}
		} finally {
			lock.unlock();
		}
		
		
		List<Long> playerIdList = Arrays.asList(playerId);
		List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
		dbService.submitUpdate2Queue(player, userDomain.getBattle(), userEquips);
		EquipPushHelper.pushEquipEndureDamageInfo(playerId, endureInfos);
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, AttributeKeys.SILVER);
		if(costSilver != 0) {
			SilverLogger.outCome(Source.PROPS_REPAIR_USEREQUIP, costSilver, player);
		}
		return SUCCESS;
	}

	/**
	 * 从抽奖仓库中取出物品
	 * 
	 * @param  playerId					角色ID
	 * @param  goodsId					移动的用户道具/用户装备ID
	 * @param  goodsType				移动的物品类型
	 * @param  userPropsId				需要堆叠的物品ID.
	 * @param  amount					需要移动的数量
	 * @param  index					物品的下标
	 * @return {@link ResultObject}		返回值封装对象
	 */
	
	public ResultObject<Collection<BackpackEntry>> checkoutFromLotteryStorage(
			long playerId, long goodsId, int goodsType, int amount,
			long userPropsId, int index) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		if (player == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if (battle == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		} else if (battle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		}
		
		if(index > player.getMaxBackSize()){
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		if(goodsType == GoodsType.EQUIP) {
			return this.checkEquipFromStorate(player, goodsId, index,BackpackType.LOTTERY_BACKPACK );
		} else if(goodsType == GoodsType.PROPS && userPropsId <= 0L) {	//没有需要堆叠的, 放在在空格上
			return checkPropsFromStorateNoStack(player, goodsId, amount, index,BackpackType.LOTTERY_BACKPACK );
		} else if(goodsType == GoodsType.PROPS && userPropsId > 0L) {	//有需要堆叠的, 放到堆叠的道具上
			return checkPropsFromStorateStack(player, goodsId, userPropsId, amount ,BackpackType.LOTTERY_BACKPACK);
		}
		return ResultObject.ERROR(TYPE_INVALID);
	}

	
	/**
	 * 装备闪光
	 * @param playerId  玩家的ID
	 */
	private void equipBlink(UserDomain userDomain){
		if(userDomain == null){
			return;
		}
		
		long playerId = userDomain.getId();
		int backpack = BackpackType.DRESSED_BACKPACK;
		List<UserEquip> equips = propsManager.listUserEquip(playerId, backpack);
		if(equips == null || equips.isEmpty()){
			return;
		}
		
		int sevenCount = 0;  //7星含以上的件数
		int elevenCount = 0; //11星含以上的件数
		Player player = userDomain.getPlayer();
		for (UserEquip userEquip : equips) {
			 if(userEquip == null){
				 continue;
			 }
			 
			 if(userEquip.getStarLevel() >= 7 && userEquip.getStarLevel() < 11){
				 sevenCount += 1;
			 } else if(userEquip.getStarLevel() >= 11){
				 elevenCount += 1;
			 }
		}
		
		if(userDomain.getGameMap() == null){
			return ;
		}
		
		//这里是根据层次来写的
		if(elevenCount >= PropsRule.EQUIP_BLINK_COUNT){
			if(player.getBlinkType() != BlinkType.ELEVEN){
				player.setBlinkType(BlinkType.ELEVEN);
				Set<Long> playerIdList = userDomain.getGameMap().getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
				UserPushHelper.pushAttribute2AreaMember(player.getId(), playerIdList, Arrays.asList(userDomain.getUnitId()), AttributeKeys.EQUIP_BLINK);
			}
			return;
		}else if(sevenCount >= PropsRule.EQUIP_BLINK_COUNT){
			if(player.getBlinkType() != BlinkType.SEVEN){
				player.setBlinkType(BlinkType.SEVEN);
				Set<Long> playerIdList = userDomain.getGameMap().getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
				UserPushHelper.pushAttribute2AreaMember(player.getId(), playerIdList, Arrays.asList(userDomain.getUnitId()), AttributeKeys.EQUIP_BLINK);
			}
			return;
		}else if((elevenCount + sevenCount) >= PropsRule.EQUIP_BLINK_COUNT){
			if(player.getBlinkType() != BlinkType.SEVEN){
				player.setBlinkType(BlinkType.SEVEN);
				Set<Long> playerIdList = userDomain.getGameMap().getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
				UserPushHelper.pushAttribute2AreaMember(player.getId(), playerIdList, Arrays.asList(userDomain.getUnitId()), AttributeKeys.EQUIP_BLINK);
			}
			return;
		}else{
			if(player.getBlinkType() != BlinkType.NONE){
				player.setBlinkType(BlinkType.NONE);
				Set<Long> playerIdList = userDomain.getGameMap().getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
				UserPushHelper.pushAttribute2AreaMember(player.getId(), playerIdList, Arrays.asList(userDomain.getUnitId()), AttributeKeys.EQUIP_BLINK);
			}
		}
	}

	/**
	 * 神武进度突破
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquipId				用户装备ID
	 * @param  shenwuId					当前装备的神武阶
	 * @param  userProps				消耗的用户道具. 用户道具ID1_使用数量1|用户道具ID2_使用数量2|...
	 * @return {@link ShenwuResult}		返回值封装对象
	 */
	
	public ShenwuResult doEquipShenwuTempo(long playerId, long userEquipId, int shenwuId, String userProps) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ShenwuResult.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle.isDead()) {
			return ShenwuResult.ERROR(PLAYER_DEADED);
		}
		
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if(userEquip == null) {
			return ShenwuResult.ERROR(PLAYER_NOT_FOUND);
		} else if(!userEquip.validBackpack(CAN_FORGE_BACKPACK)) {
			return ShenwuResult.ERROR(NOT_IN_BACKPACK);
		} else if(userEquip.getCount() <= 0) {
			return ShenwuResult.ERROR(NOT_IN_BACKPACK);
		} else if(userEquip.getPlayerId() != playerId) {
			return ShenwuResult.ERROR(BELONGS_INVALID);
		} else if(userEquip.isTrading()) {
			return ShenwuResult.ERROR(EQUIP_CANNOT_USE);
		} else if(userEquip.getQuality() == Quality.WHITE) {
			return ShenwuResult.ERROR(EQUIP_CANNOT_USE);
		} else if(!userEquip.canTempoShenwu(shenwuId)) {
			return ShenwuResult.ERROR(INPUT_VALUE_INVALID);
		}
		
		EquipConfig equipConfig = userEquip.getEquipConfig();
		if(equipConfig == null) {
			return ShenwuResult.ERROR(EQUIP_NOT_FOUND);
		}
		
		int equipType = equipConfig.getPropsType();
		ShenwuConfig shenwuConfig = shenwuService.getShenwuConfig(shenwuId, equipType);
		if(shenwuConfig == null) {
			return ShenwuResult.ERROR(BASEDATA_NOT_FOUND);
		}
		
		int tempoItemId = shenwuConfig.getTempoItemId();
		int tempoItemCount = shenwuConfig.getTempoItemCount();
		PropsConfig propsConfig = propsManager.getPropsConfig(tempoItemId);
		if(propsConfig == null) {
			return ShenwuResult.ERROR(ITEM_NOT_FOUND);
		}
		
		SynthObject itemObject = this.checkUserItemCountByItemId(playerId, userProps, tempoItemId);
		if(itemObject.getResult() < SUCCESS) {
			return ShenwuResult.ERROR(itemObject.getResult());
		} else if(itemObject.getBindingCount() != tempoItemCount) {
			return ShenwuResult.ERROR(INPUT_VALUE_INVALID);
		}
		
		Player player = userDomain.getPlayer();
		int equipBackpack = userEquip.getBackpack();
		Fightable beforable = battle.getAndCopyAttributes();
		ChainLock lock = LockUtils.getLock(player.getPackLock());
		try {
			lock.lock();
			if(!userEquip.validBackpack(CAN_FORGE_BACKPACK)) {
				return ShenwuResult.ERROR(NOT_IN_BACKPACK);
			} else if(userEquip.getCount() <= 0) {
				return ShenwuResult.ERROR(NOT_IN_BACKPACK);
			} else if(userEquip.getPlayerId() != playerId) {
				return ShenwuResult.ERROR(BELONGS_INVALID);
			} else if(userEquip.isTrading()) {
				return ShenwuResult.ERROR(EQUIP_CANNOT_USE);
			} else if(!userEquip.canTempoShenwu(shenwuId)) {
				return ShenwuResult.ERROR(INPUT_VALUE_INVALID);
			}
			
			shenwuConfig = shenwuService.getShenwuConfig(shenwuId, equipType);
			if(shenwuConfig == null) {
				return ShenwuResult.ERROR(BASEDATA_NOT_FOUND);
			}
			
			//验证是否可以突破
			if(!userEquip.validTempoAttributes(shenwuId)) {
				return ShenwuResult.ERROR(EQUIP_SHENWU_CANNOT_CAMPO);
			}
			
			int job = equipConfig.getJob();
			int shenwuTempo = userEquip.getShenwuTempo();
			int addTempoValue = shenwuConfig.getAddTempoValue();
			int maxTempoValue = shenwuConfig.getMaxTempoValue();
			if(shenwuTempo + addTempoValue >= maxTempoValue) {
				userEquip.setShenwuTempo(0);
				int nextShenwuId = shenwuId + 1;
				userEquip.updateTempoShenwuState(shenwuId, true);
				ShenwuConfig nextShenwu = shenwuService.getShenwuConfig(nextShenwuId, equipType);
				if(nextShenwu != null && nextShenwu.getLevel() <= equipConfig.getLevel()) {
					userEquip.updateTempoShenwuState(nextShenwuId, false);
					List<ShenwuAttributeConfig> nextAttributes = shenwuService.listShenwuAttribute(nextShenwuId, equipType, job);
					userEquip.newShenwuAttributeVO(nextShenwuId, nextAttributes);
					userEquip.updateShenwuAttributeMap();
				}
				userEquip.updateShenwuSwitches();
				
				BulletinConfig bConfig = resourceService.get(NoticeID.DO_SHENWU_TEMPO, BulletinConfig.class);      // 神武公告.... 2012.8.9 chaoping
				if (bConfig != null) {
					Map<String, Object> params = new HashMap<String, Object>(2);
					params.put(NoticeRule.playerName, player.getName());
					params.put(NoticeRule.number, shenwuId);
					NoticePushHelper.pushNotice(bConfig.getId(), NoticeType.HONOR, params, bConfig.getPriority());
				}
			} else {
				userEquip.setShenwuTempo(shenwuTempo + addTempoValue);
			}
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(userEquip);
		List<LoggerGoods> loggerGoods = new ArrayList<LoggerGoods>(0);
		Map<Long, Integer> bindingItems = itemObject.getBindingItems();
		Collection<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		List<UserProps> costUserPropsList = propsManager.costUserPropsList(bindingItems);
		if(costUserPropsList != null && !costUserPropsList.isEmpty()) {
			backpackEntries.addAll(voFactory.getUserPropsEntries(costUserPropsList));
			for(UserProps up : costUserPropsList){
				Integer count = bindingItems.get(up.getId());
				if(count != null && count != 0) {
					loggerGoods.add(LoggerGoods.outcomeProps(up.getId(), up.getBaseId(), Math.abs(count)));
				}
			}
		}
		
		if(!loggerGoods.isEmpty()) {
			GoodsLogger.goodsLogger(player, Source.EQUIP_SHENWU_TEMPO, loggerGoods.toArray(new LoggerGoods[loggerGoods.size()]));
		}
		
		forgePlayerFlushable(userDomain, beforable, equipBackpack, false);
		return ShenwuResult.SUCCESS(userEquip, backpackEntries);
	}

	/**
	 * 神武属性打造
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquipId				用户装备ID
	 * @param  shenwuId					当前装备的神武阶
	 * @param  userProps				消耗的用户道具. 用户道具ID1_使用数量1|用户道具ID2_使用数量2|...
	 * @return {@link ResultObject}		返回值封装对象
	 */
	
	public ShenwuResult doShenwuAttributeForge(long playerId, long userEquipId, int shenwuId, String userProps) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ShenwuResult.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle.isDead()) {
			return ShenwuResult.ERROR(PLAYER_DEADED);
		}
		
		//检查装备的背包, 数量等
		Player player = userDomain.getPlayer();
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if(userEquip == null) {
			return ShenwuResult.ERROR(EQUIP_NOT_FOUND);
		} else if(!userEquip.validBackpack(CAN_FORGE_BACKPACK)) {
			return ShenwuResult.ERROR(NOT_IN_BACKPACK);
		} else if(userEquip.getCount() <= 0) {
			return ShenwuResult.ERROR(NOT_IN_BACKPACK);
		} else if(userEquip.getPlayerId() != playerId) {
			return ShenwuResult.ERROR(BELONGS_INVALID);
		} else if(userEquip.isTrading()) {
			return ShenwuResult.ERROR(EQUIP_CANNOT_USE);
		} else if(!userEquip.canTempoShenwu(shenwuId)) {
			return ShenwuResult.ERROR(INPUT_VALUE_INVALID);
		}
		
		//基础装备校验
		EquipConfig equipConfig = userEquip.getEquipConfig();
		if(equipConfig == null) {
			return ShenwuResult.ERROR(EQUIP_NOT_FOUND);
		}
		
		//神武属性校验
		int equipType = equipConfig.getPropsType();
		ShenwuConfig shenwuConfig = shenwuService.getShenwuConfig(shenwuId, equipType);
		if(shenwuConfig == null) {
			return ShenwuResult.ERROR(BASEDATA_NOT_FOUND);
		}
		
		//基础道具校验
		int attrItemCount = shenwuConfig.getAttributeCount();
		int attributeItemId = shenwuConfig.getAttributeItemId();
		PropsConfig propsConfig = propsManager.getPropsConfig(attributeItemId);
		if(propsConfig == null) {
			return ShenwuResult.ERROR(ITEM_NOT_FOUND);
		}
		
		//验证数量是否足够
		SynthObject itemObject = this.checkUserItemCountByItemId(playerId, userProps, attributeItemId);
		if(itemObject.getResult() < SUCCESS) {
			return ShenwuResult.ERROR(itemObject.getResult());
		} else if(itemObject.getBindingCount() != attrItemCount) {
			return ShenwuResult.ERROR(INPUT_VALUE_INVALID);
		}
		
		int job = equipConfig.getJob();
		int equipBackpack = userEquip.getBackpack();
		Fightable beforable = battle.getAndCopyAttributes();
		ChainLock lock = LockUtils.getLock(player.getPackLock());
		try {
			lock.lock();
			if(!userEquip.validBackpack(CAN_FORGE_BACKPACK)) {
				return ShenwuResult.ERROR(NOT_IN_BACKPACK);
			} else if(userEquip.getCount() <= 0) {
				return ShenwuResult.ERROR(NOT_IN_BACKPACK);
			} else if(userEquip.getPlayerId() != playerId) {
				return ShenwuResult.ERROR(BELONGS_INVALID);
			} else if(userEquip.isTrading()) {
				return ShenwuResult.ERROR(EQUIP_CANNOT_USE);
			} else if(!userEquip.canTempoShenwu(shenwuId)) {
				return ShenwuResult.ERROR(INPUT_VALUE_INVALID);
			}
			
			shenwuConfig = shenwuService.getShenwuConfig(shenwuId, equipType);
			if(shenwuConfig == null) {
				return ShenwuResult.ERROR(BASEDATA_NOT_FOUND);
			}
			
			List<ShenwuAttributeConfig> attributeList = shenwuService.listShenwuAttribute(shenwuId, equipType, job);
			userEquip.updateShenwuAttributes(shenwuId, attributeList);
			userEquip.addTempoShenwuState(shenwuId, 1);
			userEquip.updateShenwuSwitches();
			userEquip.updateShenwuAttributeMap();
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(userEquip);
		List<LoggerGoods> loggerGoods = new ArrayList<LoggerGoods>(0);
		Collection<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		List<UserProps> costUserPropsList = propsManager.costUserPropsList(itemObject.getBindingItems());
		if(costUserPropsList != null && !costUserPropsList.isEmpty()) {
			backpackEntries.addAll(voFactory.getUserPropsEntries(costUserPropsList));
			for(UserProps up : costUserPropsList){
				int baseId = up.getBaseId();
				long userPropsId = up.getId();
				Integer count = itemObject.getBindingItems().get(userPropsId);
				if(count != null && count != 0) {
					loggerGoods.add(LoggerGoods.outcomeProps(userPropsId, baseId, Math.abs(count)));
				}
			}
		}
		
		if(!loggerGoods.isEmpty()) {
			GoodsLogger.goodsLogger(player, Source.EQUIP_SHENWU_FORGE, loggerGoods.toArray(new LoggerGoods[loggerGoods.size()]));
		}
		
		forgePlayerFlushable(userDomain, beforable, equipBackpack, false);
		achieveFacade.firstAchieved(playerId, FIRST_ACHIEVE, FIRST_EQUIP_SHENWU);
		return ShenwuResult.SUCCESS(userEquip, backpackEntries);
	}

	/**
	 * 装备星级继承
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquipId				原始用户装备ID
	 * @param  targetEquipId			目标装备ID
	 * @param  userPropsId				继承石保护道具
	 * @return {@link ResultObject}		返回值封装对象
	 */
	@SuppressWarnings("unchecked")
	
	public ResultObject<Collection<BackpackEntry>> equipExtendStar(long playerId, long userEquipId, long targetEquipId, long userPropsId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		if(player == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if(userEquip == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if(!userEquip.validBackpack(CAN_EXTENDS_BACKPACK)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userEquip.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userEquip.getCount() <= 0) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userEquip.isTrading()) {
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		} 
		
		UserEquip targetEquip = propsManager.getUserEquip(targetEquipId);
		if(targetEquip == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if(!targetEquip.validBackpack(CAN_EXTENDS_BACKPACK)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(targetEquip.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(targetEquip.getCount() <= 0) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(targetEquip.isTrading()) {
			return ResultObject.ERROR(EQUIP_CANNOT_USE);
		}
		
		int equipStarLevel = userEquip.getStarLevel();
		int targetStarLevel = targetEquip.getStarLevel();
		if(equipStarLevel <= 0) {
			return ResultObject.ERROR(LEVEL_INVALID);
		} else if(equipStarLevel <= targetStarLevel) {
			return ResultObject.ERROR(LEVEL_INVALID);
		}
		
		
		UserProps userProps = propsManager.getUserProps(userPropsId);
		if(userProps == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(!userProps.validBackpack(DEFAULT_BACKPACK)) {
			return ResultObject.ERROR(NOT_IN_BACKPACK);
		} else if(userProps.getCount() <= 0) {
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		} else if(userProps.getBaseId() != PropsRule.EXTEND_LEVEL_PROPSID) {
			return ResultObject.ERROR(TYPE_INVALID);
		} else if(userProps.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		}

		EquipConfig userEquipConfig = userEquip.getEquipConfig();
		EquipConfig targetEquipConfig = targetEquip.getEquipConfig();
		if(userEquipConfig == null || targetEquipConfig == null) {
			return ResultObject.ERROR(EQUIP_NOT_FOUND);
		} else if(userEquipConfig.getPropsType() != targetEquipConfig.getPropsType()) { //相同部件才可以星级转换
			return ResultObject.ERROR(TYPE_INVALID);
		}
		
		int costSilver = 0;
		int resetStarLevel = 0;
		PlayerBattle battle = userDomain.getBattle();
		int sourceBackpack = userEquip.getBackpack();
		int targetBackpack = targetEquip.getBackpack();
		int equipPrice = targetEquipConfig.getSilverPrice();
		Fightable beforeAttribute = battle.getAndCopyAttributes();
		ChainLock lock = LockUtils.getLock(player, player.getPackLock());
		try {
			lock.lock();
			if(!userEquip.validBackpack(CAN_EXTENDS_BACKPACK)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if(userEquip.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			} else if(userEquip.getCount() <= 0) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if(userEquip.isTrading()) {
				return ResultObject.ERROR(EQUIP_CANNOT_USE);
			} 
			
			if(!targetEquip.validBackpack(CAN_EXTENDS_BACKPACK)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if(targetEquip.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			} else if(targetEquip.getCount() <= 0) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if(targetEquip.isTrading()) {
				return ResultObject.ERROR(EQUIP_CANNOT_USE);
			}
			
			equipStarLevel = userEquip.getStarLevel();
			targetStarLevel = targetEquip.getStarLevel();
			if(equipStarLevel <= 0) {
				return ResultObject.ERROR(LEVEL_INVALID);
			} else if(equipStarLevel <= targetStarLevel) {
				return ResultObject.ERROR(LEVEL_INVALID);
			}
			
			userEquipConfig = userEquip.getEquipConfig();
			targetEquipConfig = targetEquip.getEquipConfig();
			if(userEquipConfig == null || targetEquipConfig == null) {
				return ResultObject.ERROR(EQUIP_NOT_FOUND);
			} 

			if(userEquipConfig.getPropsType() != targetEquipConfig.getPropsType()) { //相同部件才可以星级转换
				return ResultObject.ERROR(TYPE_INVALID);
			}
			
			if(!userProps.validBackpack(DEFAULT_BACKPACK)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if(userProps.getCount() <= 0) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			} else if(userProps.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			} else if(userProps.getBaseId() != PropsRule.EXTEND_LEVEL_PROPSID) {
				return ResultObject.ERROR(TYPE_INVALID);
			}

			costSilver = PropsRule.getExtendStarCost(equipPrice, equipStarLevel);
			if(player.getSilver() < costSilver) {
				return ResultObject.ERROR(SILVER_NOT_ENOUGH);
			}
			
			userEquip.setBinding(true);
			targetEquip.setBinding(true);
			userProps.decreaseItemCount(1);
			player.decreaseSilver(costSilver);
			int targetNewLevel = equipStarLevel;
			userEquip.setStarLevel(resetStarLevel);
			targetEquip.setStarLevel(targetNewLevel);
			EquipHelper.refreshEquipStarAttributes(userEquip);
			EquipHelper.refreshEquipStarAttributes(targetEquip);
			dbService.submitUpdate2Queue(userProps, userEquip, targetEquip, player);
		} finally {
			lock.unlock();
		}
		
		propsManager.removeUserPropsIfCountNotEnough(userProps);
		Collection<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>(3);
		backpackEntries.add(voFactory.getUserPropsEntry(userProps));
		backpackEntries.addAll(voFactory.getUserEquipEntries(userEquip, targetEquip));
		LoggerGoods loggerGoods = LoggerGoods.outcomeProps(PropsRule.EXTEND_LEVEL_PROPSID, 1);
		if(costSilver != 0) {
			SilverLogger.outCome(Source.EQUIP_EXTENDS_STAR, costSilver, player, loggerGoods);
		}
		
		int backpack = DEFAULT_BACKPACK;
		if(ArrayUtils.contains(FLUSHABLE_BACKPACK, sourceBackpack) || ArrayUtils.contains(FLUSHABLE_BACKPACK, targetBackpack)) {
			backpack = DRESSED_BACKPACK;
		}
		
		this.forgePlayerFlushable(userDomain, beforeAttribute, backpack, true);
		GoodsLogger.goodsLogger(player, Source.EQUIP_EXTENDS_STAR, loggerGoods);
		return ResultObject.SUCCESS(backpackEntries);
	}
}
