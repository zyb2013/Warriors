package com.yayo.warriors.module.alliance.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.alliance.constant.AllianceConstant.*;
import static com.yayo.warriors.module.pack.type.BackpackType.DEFAULT_BACKPACK;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.adapter.AllianceBuildService;
import com.yayo.warriors.basedb.model.AllianceBuildConfig;
import com.yayo.warriors.basedb.model.AllianceConfig;
import com.yayo.warriors.basedb.model.AllianceDivineConfig;
import com.yayo.warriors.basedb.model.AllianceShopConfig;
import com.yayo.warriors.basedb.model.AllianceSkillConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.AlliancePushHelper;
import com.yayo.warriors.common.helper.FormulaHelper;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.module.alliance.entity.Alliance;
import com.yayo.warriors.module.alliance.entity.PlayerAlliance;
import com.yayo.warriors.module.alliance.facade.AllianceBuildFacade;
import com.yayo.warriors.module.alliance.manager.AllianceManager;
import com.yayo.warriors.module.alliance.model.DonateRecord;
import com.yayo.warriors.module.alliance.model.Record;
import com.yayo.warriors.module.alliance.rule.AllianceRule;
import com.yayo.warriors.module.alliance.types.BuildType;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.log.SilverLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.module.props.type.PropsChildType;
import com.yayo.warriors.module.task.facade.AllianceTaskFacade;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.type.FormulaKey;
import com.yayo.warriors.type.GoodsType;

/**
 * 帮派建筑类玩法接口实现类
 * @author liuyuhua
 */
@Component
public class AllianceBuildFacadeImpl implements AllianceBuildFacade {
	
	@Autowired
	private AllianceBuildService allianceBuildService; 
	@Autowired
	private AllianceManager allianceManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private DbService dbService;
	@Autowired
	private FormulaHelper formulaHelper;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private AllianceTaskFacade allianceTaskFacade;
	@Autowired
	private AlliancePushHelper pushHelper;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	
	@SuppressWarnings("unchecked")
	public ResultObject<Map<String,Object>> donateProps(long playerId, String userItems) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(userItems == null || userItems.isEmpty()){
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(!playerAlliance.isExistAlliance()){
			return ResultObject.ERROR(PLAYER_NOT_EXIST_ALLIANCE);
		}
		
		Alliance alliance = allianceManager.getAlliance(playerAlliance.getAllianceId());
		if(alliance == null || alliance.isDrop()){
			return ResultObject.ERROR(ALLIANCE_NOT_FOUND);
		}
		
		
		int totlePropsCount = 0;//令牌数量
		
		Map<Long, Integer> costUserProps = this.spliteUserItems(userItems);
		if(costUserProps == null || costUserProps.isEmpty()){
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		List<LoggerGoods> loggerGoods = new ArrayList<LoggerGoods>(1);
		for(Entry<Long, Integer> entry : costUserProps.entrySet()){
			long userPropsId = entry.getKey();//道具的ID
			int count = entry.getValue();//道具的数量
			if (count <= 0) {
				return ResultObject.ERROR(INPUT_VALUE_INVALID);
			}
			
			UserProps userProps = propsManager.getUserProps(userPropsId);
			if (userProps == null) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			}

			if (userProps.getPlayerId() != playerId) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			} else if (!userProps.validBackpack(DEFAULT_BACKPACK)) {
				return ResultObject.ERROR(BACKPACK_INVALID);
			} else if (userProps.getCount() < count) {
				return ResultObject.ERROR(INPUT_VALUE_INVALID);
			} else if(userProps.isTrading()) {
				return ResultObject.ERROR(ITEM_CANNOT_USE);
			} 
			
			PropsConfig propsConfig = userProps.getPropsConfig();
			if (propsConfig == null) {
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			} else if (propsConfig.getChildType() != PropsChildType.ALLIANCE_DONATE_PROPS_TYPE) {
				return ResultObject.ERROR(BELONGS_INVALID);
			}
			
			totlePropsCount += count;
			loggerGoods.add(LoggerGoods.outcomeProps(userPropsId, propsConfig.getId(), count));//增加日志
		}
		
		if((totlePropsCount + playerAlliance.getDonatePropsCount()) > AllianceRule.MAX_DONATE_PROPS_DAILY_COUNT){
			return ResultObject.ERROR(OVER_DONATE_PROPS_DAILY_VALUE);
		}
		
		List<UserProps> userPropsList = propsManager.costUserPropsList(costUserProps);
		if(userPropsList == null || userPropsList.isEmpty()){
			return ResultObject.ERROR(FAILURE);
		}
		MessagePushHelper.pushUserProps2Client(playerId, DEFAULT_BACKPACK, false, userPropsList);//发送物品更新
		
		DonateRecord donateRecord = alliance.getDonateRecord4PlayerId(playerId);//今日贡献值记录
		
		ChainLock lock = LockUtils.getLock(alliance,playerAlliance);
		try {
			lock.lock();
			if((totlePropsCount + playerAlliance.getDonatePropsCount()) > AllianceRule.MAX_DONATE_PROPS_DAILY_COUNT){
				return ResultObject.ERROR(OVER_DONATE_PROPS_DAILY_VALUE);
			}
			
			alliance.setHaveDonate(true);//标识:有人捐献过
			alliance.setTokenPropsCount(alliance.getTokenPropsCount() + totlePropsCount);
			int donate = totlePropsCount * AllianceRule.DONATE_PROPS_REWARD_VALUE;
			playerAlliance.increaseDonate(donate);
			playerAlliance.increaseHisDonate(donate);
			playerAlliance.increasePropsDaily(totlePropsCount);
			
			/** 帮派捐献记录*/
			if(donateRecord == null){
				donateRecord = DonateRecord.valueOf(playerId, player.getName(), donate);
				alliance.getDonateRecoreds().add(donateRecord);
			}else{
				donateRecord.increaseDonate(donate);
			}
			alliance.sortDonateRecored();//巨献值排序
			
		}finally{
			lock.unlock();
		}
		
		
		alliance.addRecordLog(Record.log4Props(player.getName(), totlePropsCount));//帮派捐献记录
		
		dbService.submitUpdate2Queue(alliance,playerAlliance);
		GoodsLogger.goodsLogger(player, Source.ALLIANCE_DONATE_PROPS, loggerGoods.toArray(new LoggerGoods[loggerGoods.size()]));
		
		Map<String,Object> resultObject = new HashMap<String, Object>(5);
		resultObject.put(ResponseKey.RESULT, SUCCESS);
		resultObject.put(ResponseKey.DONATE, playerAlliance.getDonate());
		resultObject.put(ResponseKey.HIS_DONATE, playerAlliance.getHisdonate());
		resultObject.put(ResponseKey.COUNT, alliance.getTokenPropsCount());
		resultObject.put(ResponseKey.DONATE_PROPS_COUNT, playerAlliance.getDonatePropsCount());
		return ResultObject.SUCCESS(resultObject);
	}
	
	
	@SuppressWarnings({ "static-access", "unchecked" })
	public ResultObject<Map<String,Object>> donateSilver(long playerId, int silver) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(silver <= 0){
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(!playerAlliance.isExistAlliance()){
			return ResultObject.ERROR(PLAYER_NOT_EXIST_ALLIANCE);
		}
		
		Alliance alliance = allianceManager.getAlliance(playerAlliance.getAllianceId());
		if(alliance == null || alliance.isDrop()){
			return ResultObject.ERROR(ALLIANCE_NOT_FOUND);
		}
		
		if(silver%AllianceRule.MIN_DONATE_SILVER != 0){
			return ResultObject.ERROR(DONATE_SILVER_FAILURE);
		}
		
		if(silver < AllianceRule.MIN_DONATE_SILVER){
			return ResultObject.ERROR(DONATE_SILVER_FAILURE);
		}
		
		if(playerAlliance.getDonateSilverCount() >= AllianceRule.MAX_DONATE_SILVER_DAILY_COUNT){
			return ResultObject.ERROR(OVER_DONATE_SILVER_DAILY_VALUE);
		}
		
		if(silver + playerAlliance.getDonateSilverCount() > AllianceRule.MAX_DONATE_SILVER_DAILY_COUNT){ //计算差值
			silver = AllianceRule.MAX_DONATE_SILVER_DAILY_COUNT -  playerAlliance.getDonateSilverCount();
		}
		
		int donate = formulaHelper.invoke(FormulaKey.ALLIANCE_DONATE_FORMULA, silver).intValue(); //可以获得的贡献值
		DonateRecord donateRecord = alliance.getDonateRecord4PlayerId(playerId);//今日贡献值记录
		
		Player player = userDomain.getPlayer();
		ChainLock lock = LockUtils.getLock(player,playerAlliance,alliance);
		try {
			lock.lock();
			if(playerAlliance.getDonateSilverCount() >= AllianceRule.MAX_DONATE_SILVER_DAILY_COUNT){
				return ResultObject.ERROR(OVER_DONATE_SILVER_DAILY_VALUE);
			}
			
			if(player.getSilver() < silver){
				return ResultObject.ERROR(SILVER_NOT_ENOUGH);
			}
			long totleSilver = alliance.getSilver() + silver;
			
			AllianceConfig config = allianceManager.getAllianceConfig(alliance.getLevel());
			if(config == null){
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			}
			
			player.decreaseSilver(silver);
			
			if(config.getSilverLimit() <= totleSilver){
				alliance.setSilver(config.getSilverLimit());
			}else{
				alliance.increaseSilver(silver);
			}
			
			playerAlliance.increaseDonate(donate); //增加当前贡献值
			playerAlliance.increaseHisDonate(donate); //增加历史贡献值
			playerAlliance.increaseSilverDaily(silver);//今日捐献的铜币数量
			
			
			/** 捐献记录*/
			alliance.setHaveDonate(true);//标识:有人捐献过
			if(donateRecord == null){
				donateRecord = DonateRecord.valueOf(playerId, player.getName(), donate);
				alliance.getDonateRecoreds().add(donateRecord);
			}else{
				donateRecord.increaseDonate(donate);
			}
			alliance.sortDonateRecored();//排序今日捐献记录
			
		}finally{
			lock.unlock();
		}
		
		alliance.addRecordLog(Record.log4Silver(player.getName(), silver));//增加记录
		SilverLogger.outCome(Source.ALLIANCE_DONATE_SILVER, silver, player); //日志记录
		List<Long> playerIdList = Arrays.asList(playerId);
		List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, AttributeKeys.SILVER);
		dbService.updateEntityIntime(alliance,player,playerAlliance);
		allianceTaskFacade.updateDonate(playerId);//帮派任务更新捐献事件类型任务
		
		Map<String,Object> resultObject = new HashMap<String, Object>(5);
		resultObject.put(ResponseKey.RESULT, SUCCESS);
		resultObject.put(ResponseKey.DONATE, playerAlliance.getDonate());
		resultObject.put(ResponseKey.HIS_DONATE, playerAlliance.getHisdonate());
		resultObject.put(ResponseKey.SILVER, alliance.getSilver());
		resultObject.put(ResponseKey.DONATE_SILVER_COUNT, playerAlliance.getDonateSilverCount());
		return ResultObject.SUCCESS(resultObject);
	}
	
	
	@SuppressWarnings("unchecked")
	public int levelupBuild(long playerId, int type) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null){
			return PLAYER_NOT_FOUND;
		}
		
		if(!playerAlliance.isExistAlliance()){
			return PLAYER_NOT_EXIST_ALLIANCE;
		}
		
		if(!AllianceRule.vilidateTitle(playerAlliance.getTitle(), AllianceRule.LEVELUP_BUILDING)){
			return PLAYER_NOT_POWER;
		}
		
		Alliance alliance = allianceManager.getAlliance(playerAlliance.getAllianceId());
		if(alliance == null || alliance.isDrop()){
			return ALLIANCE_NOT_FOUND;
		}
		
		String name = userDomain.getPlayer().getName();
		int level = 0;//等级
		if(type == BuildType.ALLIANCE){
			level = alliance.getLevel();
		}else if(type == BuildType.ARENA){
			level = alliance.getArenaLevel();
		}else if(type == BuildType.BOOKS){
			level = alliance.getBooksLevel();
		}else if(type == BuildType.DAIS){
			level = alliance.getDaisLevel();
		}else if(type == BuildType.SHOP){
			level = alliance.getShopLevel();
		}else{
			return INPUT_VALUE_INVALID;
		}
		
		AllianceBuildConfig config =  allianceBuildService.getAllianceBuildConfig(type, level);
		if(config == null){
			return BASEDATA_NOT_FOUND;
		}
		
		if(level >= config.getLevelLimit()){
			return OVER_LEVELUP_LIMIT;
		}
		
		if(alliance.getLevel() < config.getContent()){
			return ALLIANCE_LEVEL_ENOUGH;
		}
		
		int costTokenCount = 0;//需要扣减的令牌数量 
		Map<Long,Integer> costTokenlist = config.getItemList();
		for(Entry<Long, Integer> entry : costTokenlist.entrySet()){
			costTokenCount += entry.getValue();
		}
		
		if(alliance.getSilver() < config.getSilver() || alliance.getTokenPropsCount() < costTokenCount){
			return RESOURCE_ENOUGH;
		}
		
		int currentLevel = 0;//当前建筑物等级
		ChainLock lock = LockUtils.getLock(alliance);
		try {
			lock.lock();
			if(alliance.getSilver() < config.getSilver() || alliance.getTokenPropsCount() < costTokenCount){
				return RESOURCE_ENOUGH;
			}
			
			alliance.decreaseSilver(config.getSilver());
			alliance.decreaseTokenProps(costTokenCount);
			
			if(type == BuildType.ALLIANCE){
				alliance.setLevel(alliance.getLevel() + 1);
				currentLevel = alliance.getLevel();
				alliance.addLevelupRecords(currentLevel);//添加帮派升级时间(用于运营活动需求)
			}else if(type == BuildType.ARENA){
				alliance.setArenaLevel(alliance.getArenaLevel() + 1);
				currentLevel = alliance.getArenaLevel();
			}else if(type == BuildType.BOOKS){
				alliance.setBooksLevel(alliance.getBooksLevel() + 1);
				currentLevel = alliance.getBooksLevel();
			}else if(type == BuildType.DAIS){
				alliance.setDaisLevel(alliance.getDaisLevel() + 1);
				currentLevel = alliance.getDaisLevel();
			}else if(type == BuildType.SHOP){
				alliance.setShopLevel(alliance.getShopLevel() + 1);
				currentLevel = alliance.getShopLevel();
			}
			
		
		}finally{
			lock.unlock();
		}
		
		alliance.addRecordLog(Record.log4Build(name, currentLevel, type)); //记录消耗历史
		dbService.submitUpdate2Queue(alliance,playerAlliance);
		List<Long> members = allianceManager.getMembers4Player(playerId);
		if(members != null && !members.isEmpty()){
			pushHelper.pushBuildLevel(members,  alliance.getSilver(), alliance.getTokenPropsCount(), type, currentLevel);
		}
		
		return SUCCESS;
	}
	

	
	public Map<String,Object> sublistRecord(long playerId,int start,int count) {
		Map<String,Object> resultObject = new HashMap<String, Object>(3);
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return resultObject;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null){
			return resultObject;
		}
		
		Alliance alliance = allianceManager.getAlliance(playerAlliance.getAllianceId());
		if(alliance == null || alliance.isDrop()){
			return resultObject;
		}
		
		List<Record> records = alliance.getRecordLog();
		List<Record> pageRecord = Tools.pageResult(records, (start * count), count);
		if(pageRecord == null){
			pageRecord = new ArrayList<Record>(1);
		}
		resultObject.put(ResponseKey.DATA, pageRecord.toArray());
		resultObject.put(ResponseKey.NUMBER, records.size());
		resultObject.put(ResponseKey.PAGE_START, start);
		return resultObject;
	}
	
	
	/**
	 * 截取出字符串.
	 * 
	 * @param userItems		用户道具信息. 格式: 用户道具ID_数量|用户道具ID_数量|...
	 * @return
	 */
	private Map<Long, Integer> spliteUserItems(String userItems) {
		Map<Long, Integer> maps = new HashMap<Long, Integer>();
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

	
	public ResultObject<PlayerAlliance> shoppingAlliance(long playerId, int shopId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		AllianceShopConfig config = allianceManager.getAllianceShopConfig(shopId);
		if(config == null){
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(!playerAlliance.isExistAlliance()){
			return ResultObject.ERROR(PLAYER_NOT_EXIST_ALLIANCE);
		}
		
		Alliance alliance = allianceManager.getAlliance(playerAlliance.getAllianceId());
		if(alliance == null || alliance.isDrop()){
			return ResultObject.ERROR(ALLIANCE_NOT_FOUND);
		}
		
		if(playerAlliance.getDonate() < config.getDonate()){
			return ResultObject.ERROR(PLAYER_DONATE_ENOUGH);
		}
		
		PropsConfig propsConfig = propsManager.getPropsConfig(config.getPropsId());
		if(propsConfig == null){
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		
		UserProps userProps = UserProps.valueOf(playerId, propsConfig.getId() , 1 , BackpackType.DEFAULT_BACKPACK, null, true);
		
		ChainLock lock = LockUtils.getLock(playerAlliance,player.getPackLock());
		try {
			lock.lock();
			if(playerAlliance.getDonate() < config.getDonate()){
				return ResultObject.ERROR(PLAYER_DONATE_ENOUGH);
			}
			
			int currentPackSize = propsManager.getBackpackSize(player.getId(), BackpackType.DEFAULT_BACKPACK);
			if(player.getMaxBackSize() - currentPackSize <= 0){
				return ResultObject.ERROR(BACKPACK_FULLED);
			}
			
			playerAlliance.decreaseDonate(config.getDonate());
			userProps = propsManager.createUserProps(userProps);
			propsManager.put2UserPropsIdsList(playerId, BackpackType.DEFAULT_BACKPACK, userProps);
		}finally{
			lock.unlock();
		}
		
		GoodsVO goodsVo = GoodsVO.valueOf(userProps.getBaseId(), GoodsType.PROPS, userProps.getCount());
		MessagePushHelper.pushGoodsCountChange2Client(playerId, goodsVo);//推送给客户端购买东西
		
		dbService.submitUpdate2Queue(playerAlliance);//保存
		//推送物品给客户端
		MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, userProps);
		return ResultObject.SUCCESS(playerAlliance);
	}

	
	public ResultObject<Map<String,Object>> divineAlliance(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null || !playerAlliance.isExistAlliance()){
			return ResultObject.ERROR(PLAYER_NOT_EXIST_ALLIANCE);
		}
		
		if(playerAlliance.getDonate() < AllianceRule.ALLIANCE_DIVINE_NEED_DONATE){
			return ResultObject.ERROR(PLAYER_DONATE_ENOUGH);
		}
		
		if(playerAlliance.getDivineCount() >= AllianceRule.ALLIANCE_DIVINE_COUNT){
			return ResultObject.ERROR(DONATE_OVER_COUNT);
		}
		
		AllianceDivineConfig divineConfig = allianceBuildService.getDivineAlliance();
		if(divineConfig == null){
			logger.error("玩家[{}],帮派占卦,无法随机出AllianceDivineConfig",playerId);
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		
		PropsConfig propsConfig = propsManager.getPropsConfig(divineConfig.getPropsId());
		if(propsConfig == null){
			logger.error("玩家[{}],帮派占卦,获取道具基础ID[{}],物品不存在",playerId,divineConfig.getPropsId());
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		UserProps userProps = UserProps.valueOf(playerId, propsConfig.getId() , 1 , BackpackType.DEFAULT_BACKPACK, null, true);
		
		ChainLock lock = LockUtils.getLock(playerAlliance,player.getPackLock());
		try {
			lock.lock();
			if(playerAlliance.getDonate() < AllianceRule.ALLIANCE_DIVINE_NEED_DONATE){
				return ResultObject.ERROR(PLAYER_DONATE_ENOUGH);
			}
			
			if(playerAlliance.getDivineCount() >= AllianceRule.ALLIANCE_DIVINE_COUNT){
				return ResultObject.ERROR(DONATE_OVER_COUNT);
			}
			
			int currentPackSize = propsManager.getBackpackSize(player.getId(), BackpackType.DEFAULT_BACKPACK);
			if(player.getMaxBackSize() - currentPackSize <= 0){
				return ResultObject.ERROR(BACKPACK_FULLED);
			}
			
			playerAlliance.decreaseDonate(AllianceRule.ALLIANCE_DIVINE_NEED_DONATE);
			playerAlliance.setDivineCount(playerAlliance.getDivineCount() + 1);
			userProps = propsManager.createUserProps(userProps);
			propsManager.put2UserPropsIdsList(playerId, BackpackType.DEFAULT_BACKPACK, userProps);
		}finally{
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(playerAlliance);//保存
		//推送物品给客户端
		MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, userProps);
		
		Map<String,Object> resultObject = new HashMap<String, Object>(4);
		resultObject.put(ResponseKey.RESULT, SUCCESS);
		resultObject.put(ResponseKey.DONATE, playerAlliance.getDonate());
		resultObject.put(ResponseKey.COUNT, playerAlliance.getDivineCount());
		resultObject.put(ResponseKey.BASEID, propsConfig.getId());
		return ResultObject.SUCCESS(resultObject);
	}

	
	public ResultObject<Alliance> researchSkill(long playerId, int researchId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		AllianceSkillConfig skillConfig = allianceManager.getAllianceSkillConfig(researchId);
		if(skillConfig == null){
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null){
			return ResultObject.ERROR(ALLIANCE_NOT_FOUND);
		}
		
		Alliance alliance = allianceManager.getAlliance(playerAlliance.getAllianceId());
		if(alliance == null || alliance.isDrop()){
			return ResultObject.ERROR(ALLIANCE_NOT_FOUND);
		}
		
		if(alliance.getBooksLevel() < skillConfig.getBuildLevel()){
			return ResultObject.ERROR(BUILDER_LEVEL_ENOUGH);
		}
		
		if(!AllianceRule.vilidateTitle(playerAlliance.getTitle(), AllianceRule.LEVELUP_BUILDING)){
			return ResultObject.ERROR(PLAYER_NOT_POWER);
		}
	
		if(skillConfig.getResearchSkillCondition().length > 0){//判断是否有前置要求
			int exSkillId = skillConfig.getResearchSkillCondition()[0]; //所需要的前置技能ID
			int exSkillLevel = skillConfig.getResearchSkillCondition()[1];//所需要的前置技能等级
			
			if(exSkillId != 0 && exSkillLevel != 0){
				if(alliance.getSkillMap().get(exSkillId) == null){
				    return ResultObject.ERROR(ALLIANCE_EXSKILL_ENOUGH);
				}
				
				int currentSkillLevel = alliance.getSkillMap().get(exSkillId);
				if(currentSkillLevel < exSkillLevel){
					return ResultObject.ERROR(ALLIANCE_EXSKILL_ENOUGH);
				}
			}
		}
		
		int researchSilver = skillConfig.getResearchSilver();//研究所需要的资金
		if(alliance.getSilver() < researchSilver){
			return ResultObject.ERROR(RESOURCE_ENOUGH);
		}
		
		ChainLock lock = LockUtils.getLock(alliance);
		try {
			lock.lock();
			if(alliance.getSilver() < researchSilver){
				return ResultObject.ERROR(RESOURCE_ENOUGH);
			}
			
			alliance.decreaseSilver(researchSilver);//扣减帮派资源
			int skillId = skillConfig.getSkillId();
			int skillLevel = skillConfig.getSkillLevel();
			alliance.researchSkill(skillId, skillLevel);
			
		}finally{
			lock.unlock();
		}
		
		List<Long> members = allianceManager.getMembers4Player(playerId);
		if(members != null && !members.isEmpty()){
			pushHelper.pushResearchSkill(members,  alliance.getSkills(),  alliance.getSilver());
		}
		
	    dbService.submitUpdate2Queue(alliance);
		return ResultObject.SUCCESS(alliance);
	}

	
	public ResultObject<PlayerAlliance> studySkill(long playerId, int researchId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		AllianceSkillConfig skillConfig = allianceManager.getAllianceSkillConfig(researchId);
		if(skillConfig == null){
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null){
			return ResultObject.ERROR(ALLIANCE_NOT_FOUND);
		}
		
		Alliance alliance = allianceManager.getAlliance(playerAlliance.getAllianceId());
		if(alliance == null || alliance.isDrop()){
			return ResultObject.ERROR(ALLIANCE_NOT_FOUND);
		}
		
		int studySkillId = skillConfig.getSkillId(); //需要学习的技能
		int studySkillLevel = skillConfig.getSkillLevel();//需要学习的技能等级
		if(alliance.getSkillMap().get(studySkillId) == null){
			return ResultObject.ERROR(ALLIANCE_SKILL_NOT_FOUND);
		}else{
			int currentSkillLevel = alliance.getSkillMap().get(studySkillId);
			if(currentSkillLevel < studySkillLevel){
				return ResultObject.ERROR(ALLIANCE_SKILL_NOT_FOUND);
			}
		}
		
		if(playerAlliance.getSkillMap().get(studySkillId) != null){
			int currentSkillLevel = playerAlliance.getSkillMap().get(studySkillId);
			if(currentSkillLevel >= studySkillLevel){
				return ResultObject.ERROR(ALLIANCE_SKILL_STUDYED);
			}
		}
		
		
		if(playerAlliance.getDonate() < skillConfig.getDonate()){
			return ResultObject.ERROR(PLAYER_DONATE_ENOUGH);
		}
		
		Player player = userDomain.getPlayer();
		if(player.getSilver() < skillConfig.getLearnSilver()){
			return ResultObject.ERROR(SILVER_NOT_ENOUGH);
		}
		
		ChainLock lock = LockUtils.getLock(playerAlliance,player,battle);
		try {
			lock.lock();
			if(playerAlliance.getDonate() < skillConfig.getDonate()){
				return ResultObject.ERROR(PLAYER_DONATE_ENOUGH);
			}
			
			if(player.getSilver() < skillConfig.getLearnSilver()){
				return ResultObject.ERROR(SILVER_NOT_ENOUGH);
			}
			
			player.decreaseSilver(skillConfig.getLearnSilver());
			playerAlliance.decreaseDonate(skillConfig.getDonate());
			playerAlliance.studySkill(studySkillId, studySkillLevel);
			playerAlliance.setFlushable(Flushable.FLUSHABLE_NORMAL);
			battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
		}finally{
			lock.unlock();
		}
		
		List<Long> playerIdList = Arrays.asList(playerId);
		List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, AttributeKeys.SILVER);
		
		dbService.submitUpdate2Queue(playerAlliance);
		return ResultObject.SUCCESS(playerAlliance);
	}

	
	public List<DonateRecord> sublistDonateRecords(long playerId, int start, int count) {
		List<DonateRecord> result = new ArrayList<DonateRecord>(count);
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return result;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null || !playerAlliance.isExistAlliance()){
			return result;
		}
		
		Alliance alliance = allianceManager.getAlliance(playerAlliance.getAllianceId());
		if(alliance.isHaveDonate()){
			ChainLock lock = LockUtils.getLock(alliance);
			try {
				lock.lock();
				if(alliance.isHaveDonate()){
					List<DonateRecord> list = alliance.getDonateRecoreds();
					Collections.sort(list);//排序
					alliance.setHaveDonate(false);//标识
				}
			}finally{
				lock.unlock();
			}
		}
		
		List<DonateRecord> list = alliance.getDonateRecoreds();
		if(list == null || list.isEmpty()){
			return result;
		}
		
		List<DonateRecord> pageRecord =  Tools.pageResult(list, (start * count), count);
		result.addAll(pageRecord);
		return result;
	}

	
	public int sizeDonateRecord4Alliance(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return 0;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null || !playerAlliance.isExistAlliance()){
			return 0;
		}
		
		Alliance alliance = allianceManager.getAlliance(playerAlliance.getAllianceId());
		if(alliance == null || alliance.isDrop()){
			return 0;
		}
		
		return alliance.getDonateRecoreds().size();
	}

}
