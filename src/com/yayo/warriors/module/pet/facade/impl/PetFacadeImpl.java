package com.yayo.warriors.module.pet.facade.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import static com.yayo.warriors.constant.CommonConstant.BACKPACK_INVALID;
import static com.yayo.warriors.constant.CommonConstant.BASEDATA_NOT_FOUND;
import static com.yayo.warriors.constant.CommonConstant.BELONGS_INVALID;
import static com.yayo.warriors.constant.CommonConstant.COOL_TIMING;
import static com.yayo.warriors.constant.CommonConstant.FAILURE;
import static com.yayo.warriors.constant.CommonConstant.INPUT_VALUE_INVALID;
import static com.yayo.warriors.constant.CommonConstant.ITEM_CANNOT_USE;
import static com.yayo.warriors.constant.CommonConstant.ITEM_NOT_ENOUGH;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.cache.CachedService;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.common.utility.DateUtil;
import com.yayo.common.utility.EnumUtils;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.adapter.PetService;
import com.yayo.warriors.basedb.model.BulletinConfig;
import com.yayo.warriors.basedb.model.CoolTimeConfig;
import com.yayo.warriors.basedb.model.PetConfig;
import com.yayo.warriors.basedb.model.PetMergedConfig;
import com.yayo.warriors.basedb.model.PetTrainConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.FormulaHelper;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.NoticePushHelper;
import com.yayo.warriors.common.helper.PetPushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.battlefield.facade.BattleFieldFacade;
import com.yayo.warriors.module.campbattle.facade.CampBattleFacade;
import com.yayo.warriors.module.cooltime.entity.UserCoolTime;
import com.yayo.warriors.module.cooltime.manager.CoolTimeManager;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.dungeon.manager.DungeonManager;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.logger.log.GoldLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.log.PetLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.LogPropsID;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.GameScreen;
import com.yayo.warriors.module.map.manager.GameMapManager;
import com.yayo.warriors.module.notice.rule.NoticeRule;
import com.yayo.warriors.module.notice.type.NoticeID;
import com.yayo.warriors.module.notice.type.NoticeType;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.pet.entity.Pet;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.pet.facade.PetFacade;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.pet.model.PetMotion;
import com.yayo.warriors.module.pet.model.PetZoom;
import com.yayo.warriors.module.pet.rule.PetAttributeRule;
import com.yayo.warriors.module.pet.rule.PetRule;
import com.yayo.warriors.module.pet.types.PetJob;
import com.yayo.warriors.module.pet.types.PetStatus;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.module.props.type.PropsChildType;
import com.yayo.warriors.module.server.listener.DataRemoveListener;
import com.yayo.warriors.module.task.facade.TaskFacade;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.entity.PlayerMotion;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.type.ElementType;
import com.yayo.warriors.type.FormulaKey;
import com.yayo.warriors.type.GoodsType;

import static com.yayo.warriors.module.pack.type.BackpackType.DEFAULT_BACKPACK;
import static com.yayo.warriors.module.pet.constant.PetConstant.*;


@Component
public class PetFacadeImpl implements PetFacade , DataRemoveListener {
	
	@Autowired
	private PetManager petManager;
	@Autowired
	private DbService dbService;
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private PetService petService;
	@Autowired
	private TaskFacade taskFacade;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private CachedService cachedService;
	@Autowired
	private PetPushHelper petPushHelper;
	@Autowired
	private DungeonManager dungeonManager;
	@Autowired
	private GameMapManager gameMapManager;
	@Autowired
	private CampBattleFacade campBattleFacade;
	@Autowired
	private BattleFieldFacade battleFieldFacade;
	@Autowired
	private CoolTimeManager coolTimeManager;
	
	
	private static final ConcurrentHashMap<Long, PetZoom> PETZOOM = new ConcurrentHashMap<Long, PetZoom>();
	
	private static final String PREFIX = "PET_";  
	private static final String FAMOUS = "FAMOUS_";
	
	
	
	public ResultObject<Long> goBack(long playerId) {
		UserDomain userDomain = this.userManager.getUserDomain(playerId);
		PlayerDungeon playerDungeon = dungeonManager.getPlayerDungeon(playerId);
		if(userDomain == null || playerDungeon == null){
			return ResultObject.ERROR(FAILURE);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		Fightable beforable = battle.getAndCopyAttributes(); 
		PetDomain petDomain = this.petManager.goBack(playerId);
		if(petDomain == null){
			return ResultObject.ERROR(PET_IS_NOT_FIGHTING);
		}
		
		GameScreen gameScreen = userDomain.getCurrentScreen();
		if(gameScreen == null){
			return ResultObject.ERROR(MAP_NOT_FOUND);
		}
		
		GameMap gameMap = gameScreen.getGameMap();
		if(gameMap == null){
			return ResultObject.ERROR(MAP_NOT_FOUND);
		}
		
		gameMap.leaveMap(petDomain);
		Collection<Long> viewPlayers = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
		UserPushHelper.pushPlayerAttributeChange(beforable, userDomain); 
		this.petPushHelper.petBack(playerId, viewPlayers);
		this.petPushHelper.pushPetAttribute(Arrays.asList(playerId), playerId, petDomain.getId(), PetAttributeRule.PET_HP);
		return ResultObject.SUCCESS(petDomain.getId());
	}
	
	
	public ResultObject<Integer> caclPetEnergy(long playerId) {
		UserDomain userDomain = this.userManager.getUserDomain(playerId);
		PlayerDungeon playerDungeon = dungeonManager.getPlayerDungeon(playerId);
		if(userDomain == null || playerDungeon == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PetDomain petDomain = this.petManager.caclPetEnergy(playerId);
		if(petDomain == null){
			return ResultObject.ERROR(PET_NOT_FIGHTING);
		}
		
		Pet pet = petDomain.getPet();
		if(pet.getEnergy() <= 0){
			this.petManager.goBack(playerId);
			
			GameScreen gameScreen = userDomain.getCurrentScreen();
			if(gameScreen == null){
				return ResultObject.ERROR(MAP_NOT_FOUND);
			}
			
			GameMap gameMap = gameScreen.getGameMap();
			if(gameMap == null){
				return ResultObject.ERROR(MAP_NOT_FOUND);
			}
			
			Collection<Long> viewPlayers = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
			this.petPushHelper.petBack(playerId, viewPlayers);
		}
		
		return ResultObject.SUCCESS(pet.getEnergy());
	}
	
	
	
	public int updatePetMotion(long playerId,long petId,int x, int y) {
		UserDomain userDomain = this.userManager.getUserDomain(playerId);
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		GameMap gameMap =  userDomain.getCurrentScreen().getGameMap();
		
		PetDomain petDomain = this.petManager.getFightingPet(playerId);
		if(petDomain == null){
			return PET_IS_NOT_FIGHTING;
		}
		
		PetMotion motion = petDomain.getMotion();
		if(motion == null){
			motion = PetMotion.valueOf(petId);
		}
		GameScreen toGameScreen = gameMap.getGameScreen(x,y) ;
		if(toGameScreen != null){
			petDomain.changeScreen(toGameScreen);
			motion.setX(x);
			motion.setY(y);
		} else {
			petDomain.changeScreen(userDomain.getCurrentScreen());
			motion.setX(userDomain.getX());
			motion.setY(userDomain.getY());
		}
		
		return SUCCESS;
	}

	
	public ResultObject<Integer> petGoFighting(long playerId, long petId) {
		UserDomain userDomain = this.userManager.getUserDomain(playerId);
		PlayerDungeon playerDungeon = dungeonManager.getPlayerDungeon(playerId);
		if(userDomain == null || playerDungeon == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PetDomain petDomain = petManager.getPetDomain(petId);
		if(petDomain == null){
			return ResultObject.ERROR(PET_NOT_FOUND);
		}
		
		if(petDomain.getPlayerId() != playerId){
			return ResultObject.ERROR(PET_NOT_FOUND);
		}
		
		if(campBattleFacade.isInCampBattle(userDomain)){
			return ResultObject.ERROR(PET_CAMP_BATTLE_MERGED_NO_FIGHT);
		}
		
		if(battleFieldFacade.isInBattleField(playerId)){
			return ResultObject.ERROR(PET_BATTLE_FIELD_MERGED_NO_FIGHT);
		}
		
		GameScreen gameScreen = userDomain.getCurrentScreen();
		if(gameScreen == null){
			return ResultObject.ERROR(MAP_NOT_FOUND);
		}
		
		GameMap gameMap = gameScreen.getGameMap();
		if(gameMap == null){
			return ResultObject.ERROR(MAP_NOT_FOUND);
		}
		
		int result = petManager.goFighting(userDomain, petDomain);
		if(result != SUCCESS){
			return ResultObject.ERROR(result);
		}
		
		PlayerMotion playerMotion = userDomain.getMotion();
		PetMotion petMotion = petDomain.getMotion();
		Pet pet = petDomain.getPet();
		
		if(petMotion == null){
			petMotion = PetMotion.valueOf(petId);
		}
		
		petMotion.setX(playerMotion.getX());
		petMotion.setY(playerMotion.getY());
		petDomain.changeMap(gameMap, playerMotion.getX(), playerMotion.getY());
		
		Collection<Long> viewPlayers = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
		this.petPushHelper.petGoFighting(playerId, petId, petMotion.getX(), petMotion.getY(), viewPlayers);
		return ResultObject.SUCCESS(pet.getEnergy());
	}


	
	
	public int getPetSoltSize(long playerId) {
		UserDomain userDomain = this.userManager.getUserDomain(playerId);
		Player player = userDomain.getPlayer();
		if(player == null){
			return FAILURE;
		}
		
		int slot = player.getMaxPetSlotSize();
		return slot;
	}
	
	
	
	public Set<Long> getPlayerPetIds(long playerId) {
		UserDomain userDomain = this.userManager.getUserDomain(playerId);
		if(userDomain == null){
			return new HashSet<Long>(0);
		}
		return this.petManager.getPlayerPetIds(playerId);
	}
	
	
	
	public  ResultObject<Integer> freePet(long playerId, long petId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(FAILURE);
		}
		
		PetDomain petDomain = petManager.getPetDomain(petId);
		if(petDomain == null){
			return ResultObject.ERROR(PET_NOT_FOUND);
		}
		
		if(petDomain.getPlayerId() != playerId){
			return ResultObject.ERROR(PET_NOT_FOUND);
		}
		
		int quality = petDomain.getBattle().getQuality();
		int petLevel = petDomain.getBattle().getLevel();
		Player player = userDomain.getPlayer();
		if(!petManager.disbandPet(playerId, petId)){
			return ResultObject.ERROR(PET_DISBAND_FAILURE);
		}
		
		int exp = FormulaHelper.invoke(FormulaKey.PET_FREE_EXP, petLevel,quality).intValue();
		ChainLock lock = LockUtils.getLock(player);
		try {
			lock.lock();
			player.setPetexperience(player.getPetexperience() + exp);
		}finally{
			lock.unlock();
		}
	    
		GameMap gameMap = petDomain.getGameMap();
		if(gameMap != null){
			 gameMap.leaveMap(petDomain);
		}
		petDomain.leaveScreen();
		
		dbService.submitUpdate2Queue(player);
		return ResultObject.SUCCESS(player.getPetexperience());
	}
	
	
	private String getFamousCommomCacheKey(long playerId){
		return PREFIX + FAMOUS + playerId;
	}
	
	
	public void removePetFamousCache(long playerId) {
		String key = this.getFamousCommomCacheKey(playerId);
		this.cachedService.removeFromCommonCache(key);
	}
	
	
	
	@SuppressWarnings("unchecked")
	public  ResultObject<Boolean> trainingPetSavvy(long playerId, long petId, String userItem,int autoBuyCount) {
		UserDomain userDomain = this.userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PetDomain petDomain = petManager.getPetDomain(petId);
		if(petDomain == null){
			return ResultObject.ERROR(PET_NOT_FOUND);
		}
		
		Pet pet = petDomain.getPet();
		Player player = userDomain.getPlayer();
		PetBattle petBattle = petDomain.getBattle();
		PlayerBattle battle = userDomain.getBattle();
		if(pet.getPlayerId() != userDomain.getId()){
			return ResultObject.ERROR(PET_NOT_FOUND);
		}
		
		if(petBattle.getSavvy() >= PetRule.MAX_PET_SAVVY_LEVEL){
			return ResultObject.ERROR(PET_GROW_IS_FULL);
		}
		
		PetTrainConfig trainConfig = petManager.getPetTrainConfig(petBattle.getSavvy());
		if(trainConfig == null){
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		
		if(battle.getLevel() < trainConfig.getLevelLimit()){
			return ResultObject.ERROR(PET_PLAYER_LEVEL_NOT_ENOUGH);
		}
		
		autoBuyCount = Math.abs(autoBuyCount);
		int needGolden = 0; 
		if(autoBuyCount > 0){
			PropsConfig propsConfig = propsManager.getPropsConfig(trainConfig.getSavvyProps());
			if(propsConfig == null){
				return ResultObject.ERROR(BASEDATA_NOT_FOUND); 
			}
			needGolden = propsConfig.getMallPrice() * autoBuyCount;
			if(needGolden > player.getGolden()){
				return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
			}
		}
		
		int goodsItemBaseId = 0;
		int totle = 0; 
		List<String[]> propslist = Tools.delimiterString2Array(userItem);
		Map<Long,Integer> userBackItems = new HashMap<Long, Integer>(1);
		if(propslist != null && !propslist.isEmpty()){
			for(String[] strProps : propslist){
				if(strProps.length < 2){
					continue;
				}
				long propsId = Long.parseLong(strProps[0]);
				int number = Math.abs(Integer.parseInt(strProps[1]));
				if(number == 0){
					continue;
				}
				if(userBackItems.get(propsId) != null){
					int num = userBackItems.get(propsId);
					userBackItems.put(propsId, number + num);
				}else{
					userBackItems.put(propsId, number);
				}
				totle += number; 
			}
		}
		
		if((totle + autoBuyCount) != trainConfig.getNumber()){
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		}
		
		List<LoggerGoods> goodsInfos = new ArrayList<LoggerGoods>(3);
		for(Map.Entry<Long, Integer> entry : userBackItems.entrySet()){
			long userPropsId = entry.getKey();
			int propsCount   = entry.getValue();
			if (propsCount <= 0) {
				return ResultObject.ERROR(INPUT_VALUE_INVALID);
			}
		
			UserProps userProps = propsManager.getUserProps(userPropsId);
			if (userProps == null) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			}

			if (userProps.getPlayerId() != playerId) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			}else if (!userProps.validBackpack(DEFAULT_BACKPACK)) {
				return ResultObject.ERROR(BACKPACK_INVALID);
			}else if(userProps.isTrading()) {
				return ResultObject.ERROR(ITEM_CANNOT_USE);
			}else if(userProps.getCount() < propsCount){
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			}
			
			PropsConfig propsConfig = userProps.getPropsConfig();
			if (propsConfig == null) {
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			}else if(propsConfig.getChildType() != PropsChildType.PET_TRAIN_SAVVY_TYPE){
				return ResultObject.ERROR(BELONGS_INVALID);
			}
			
			goodsItemBaseId = propsConfig.getId();
			goodsInfos.add(LoggerGoods.outcomeProps(userPropsId, propsConfig.getId(), propsCount));
		}
		
		if(needGolden > 0 && autoBuyCount > 0){
			ChainLock lock = LockUtils.getLock(player);
			try {
				lock.lock();
				if(player.getGolden() < needGolden){
					return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
				}
				player.decreaseGolden(needGolden);
			}finally{
				lock.unlock();
			}
			goodsInfos.add(LoggerGoods.outcomePropsAutoBuyGolden(trainConfig.getSavvyProps(), autoBuyCount, needGolden));
		}
		List<UserProps> userPropsList = propsManager.costUserPropsList(userBackItems);
		
		int savvyRate    = trainConfig.getSavvyRate();
		int ranSavvyRate = Tools.getRandomInteger(trainConfig.getTrainfullRate());
		int savvyLevel = 0; 
		boolean isSuccess = false; 
		if(ranSavvyRate <= savvyRate){
			savvyLevel = petBattle.getSavvy() + 1;
			isSuccess = true;
			
			if (savvyLevel == 10 || savvyLevel == 20 || savvyLevel == 30 || savvyLevel == 50) {
				BulletinConfig config = NoticePushHelper.getConfig(NoticeID.PET_SAVVY_UP, BulletinConfig.class);
				Map<String, Object> params = new HashMap<String, Object>(5);
				params.put(NoticeRule.playerId, playerId);
				params.put(NoticeRule.playerName, userDomain.getPlayer().getName());
				params.put(NoticeRule.number, savvyLevel);
				params.put(NoticeRule.pet, pet.getName());
				params.put(NoticeRule.title, savvyLevel);
				if (config != null) NoticePushHelper.pushNotice(NoticeID.PET_SAVVY_UP , NoticeType.HONOR, params, config.getPriority());
			}
		}else{
			savvyLevel = trainConfig.getSavvyFail();
			isSuccess = false;
		}
		ChainLock lock = LockUtils.getLock(pet, petBattle);
		try {
			lock.lock();
			petBattle.setSavvy(savvyLevel);
			petBattle.setFlushable(Flushable.FLUSHABLE_NORMAL);
		} catch (Exception e) {
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(pet,player,petBattle);
		taskFacade.updatePetSavvyLevelTask(player.getId());
		if(!userPropsList.isEmpty()){
			MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, userPropsList);
			MessagePushHelper.pushGoodsCountChange2Client(playerId, GoodsVO.valueOf(goodsItemBaseId, GoodsType.PROPS, (totle * -1)));
		}
		
		LoggerGoods[] loggerGoodsArray = goodsInfos.toArray(new LoggerGoods[goodsInfos.size()]);
		if(loggerGoodsArray.length > 0){
			GoodsLogger.goodsLogger(player, Source.PET_SAVVY_TRAINING, loggerGoodsArray);
			PetLogger.petSavvyTraining(player, petBattle, needGolden, LogPropsID.PET_SAVVY_PROPS, (totle + autoBuyCount), loggerGoodsArray);//记录日志
		}
		
		if(needGolden != 0) {
			GoldLogger.outCome(Source.PET_SAVVY_TRAINING, needGolden, player, loggerGoodsArray);
		}
		
		List<Long> playerIdList = Arrays.asList(playerId);
		List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, AttributeKeys.GOLDEN);
		return ResultObject.SUCCESS(isSuccess);
	}

	
	public ResultObject<Long> mixPet(long playerId, long petId, long targetPetId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(petId == targetPetId){
			return ResultObject.ERROR(PET_MIX_THE_SAME);
		}
		
		if(petManager.checkFighting(playerId, petId)){
			return ResultObject.ERROR(PET_IS_FIGHTING);
		}
		
		if(petManager.checkFighting(playerId, targetPetId)){
			return ResultObject.ERROR(PET_IS_FIGHTING);
		}
		
		PetDomain petDomain = petManager.getPetDomain(petId);
		PetDomain targetDomain = petManager.getPetDomain(targetPetId);
		if(petDomain == null || targetDomain == null){
			return ResultObject.ERROR(PET_NOT_FOUND);
		}
		
		if(petDomain.getPlayerId() != playerId || targetDomain.getPlayerId() != playerId){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		
		Pet targetPet = targetDomain.getPet();
		PetBattle petBattle = petDomain.getBattle();
		PetBattle targetBattle = targetDomain.getBattle();
		
		int level = 0;
		int savvy = 0;
		int quality = 0;
		if(petBattle.getLevel() >= targetBattle.getLevel()){
			level = petBattle.getLevel();
		}else{
			level = FormulaHelper.invoke(FormulaKey.PET_EXTEND_LEVEL, targetBattle.getLevel(),petBattle.getLevel()).intValue();
		}
		
		if(petBattle.getSavvy() >= targetBattle.getSavvy()){
			savvy = petBattle.getSavvy();
		}else{
			savvy = FormulaHelper.invoke(FormulaKey.PET_EXTEND_SAVVY, targetBattle.getSavvy(),petBattle.getSavvy()).intValue();
		}
		quality = FormulaHelper.invoke(FormulaKey.PET_EXTEND_QUALITY, petBattle.getQuality(), targetBattle.getQuality()).intValue();
		
		ChainLock lock = LockUtils.getLock(petBattle,targetPet);
		try {
			lock.lock();
			if(targetPet.getStatus() == PetStatus.DROP){
				return ResultObject.ERROR(PET_NOT_FOUND);
			}
			targetPet.setStatus(PetStatus.DROP);
			petBattle.setLevel(level);
			petBattle.setQuality(quality);
			petBattle.setSavvy(savvy);
			petBattle.setFlushable(Flushable.FLUSHABLE_LEVEL_UP);
		}finally{
			lock.unlock();
		}

		dbService.submitUpdate2Queue(petBattle);
		dbService.updateEntityIntime(targetPet);
		petManager.remove(playerId, targetPetId);
		return ResultObject.SUCCESS(petId);
	}
	
	
	
	public PetZoom getPetZoom(long playerId){
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return null;
		}
		PetZoom petZoom = PETZOOM.get(playerId);
		if(petZoom != null){
			return petZoom;
		}
		petZoom = petManager.getPetZooms(playerId);
		PETZOOM.putIfAbsent(playerId, petZoom);
		petZoom = PETZOOM.get(playerId);
		return petZoom;
	}

	
	public ResultObject<PetZoom> openEggDraw(long playerId, String eggItem) {
		UserDomain userDomain = this.userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(eggItem == null || eggItem.isEmpty()){
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		PetZoom petZoom = this.getPetZoom(playerId);
		if(petZoom == null){
			return ResultObject.ERROR(FAILURE);
		}
		
		Player player = userDomain.getPlayer(); 
		
		int totle = 0;
		
		List<String[]> propsSplit = Tools.delimiterString2Array(eggItem);
		
		Map<Long,Integer> userBackItems = new HashMap<Long, Integer>();
		for(String[] eggStr : propsSplit){
			if(eggStr.length < 2){
				continue;
			}
			long propsId = Long.parseLong(eggStr[0]);
			int number = Math.abs(Integer.parseInt(eggStr[1]));
			if(number == 0){
				continue;
			}
			if(userBackItems.get(propsId) != null){
				int num = userBackItems.get(propsId);
				userBackItems.put(propsId, number + num);
			}else{
				userBackItems.put(propsId, number);
			}
			totle += number;
		}
		
		int surplus = PetRule.MAX_PET_OPEN_EGG_CACHE_COUNT - petZoom.size(); 
		if(totle > surplus){
			return ResultObject.ERROR(PET_EGG_CACHE_ENOUGH);
		}
		if(petZoom.size() >= PetRule.MAX_PET_OPEN_EGG_CACHE_COUNT){
			return ResultObject.ERROR(PET_EGG_CACHE_ENOUGH);
		}
		
		List<UserProps> userPropsList = new ArrayList<UserProps>(3);
		
		for(Map.Entry<Long, Integer> entry : userBackItems.entrySet()){
			long userPropsId = entry.getKey();
			int propsCount   = entry.getValue();
			if (propsCount <= 0) {
				return ResultObject.ERROR(INPUT_VALUE_INVALID);
			}
		
			UserProps userProps = propsManager.getUserProps(userPropsId);
			if (userProps == null) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			}
			
			if (userProps.getPlayerId() != playerId) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			}else if (!userProps.validBackpack(DEFAULT_BACKPACK)) {
				return ResultObject.ERROR(BACKPACK_INVALID);
			}else if(userProps.isTrading()) {
				return ResultObject.ERROR(ITEM_CANNOT_USE);
			}
			
			PropsConfig propsConfig = userProps.getPropsConfig();
			if (propsConfig == null) {
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			}else if(propsConfig.getChildType() != PropsChildType.PET_RAN_EGG_TYPE){
			    if(propsConfig.getChildType() != PropsChildType.PET_EGG_TYPE){
			    	return ResultObject.ERROR(BELONGS_INVALID);
			    }
		    }
			
			List<LoggerGoods> goodsInfos = new ArrayList<LoggerGoods>(3);
			goodsInfos.add(LoggerGoods.outcomeProps(userPropsId, propsConfig.getId(), propsCount));
			ChainLock lock = LockUtils.getLock(player.getPackLock(),petZoom);
			try {
				lock.lock();
				if(userProps.getCount() < propsCount){
					if(!userPropsList.isEmpty()){
						MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, userPropsList);
					}
					return ResultObject.SUCCESS(petZoom);
				}
				
				for(int i = 0 ; i < propsCount ; i ++){
					int dropNo = propsConfig.getAttrValueInt();
					PetConfig petConfig = petService.initEggPet(dropNo); 	
					if(petConfig == null){
						return ResultObject.ERROR(BASEDATA_NOT_FOUND);
					}
					int aptitudeNo = petConfig.getAptitudeNo();
					int quality = petService.initPetQuality(aptitudeNo);
					PetJob job = EnumUtils.getEnum(PetJob.class, petConfig.getJob());
					if(job == null){
						continue;
					}
					PetBattle petBattle = PetRule.createPetBattle(null, job, quality);
					Pet pet = PetRule.createPet(playerId, petConfig);
					pet.setStatus(PetStatus.UNDRAW);
					if(propsConfig.getChildType() == PropsChildType.PET_EGG_TYPE){
						pet.setSpecify(true);
						BulletinConfig config = NoticePushHelper.getConfig(NoticeID.OBTAIN_FAMOUS_PET, BulletinConfig.class);
						if (config != null) {
							Map<String, Object> params = new HashMap<String, Object>(3);
							params.put(NoticeRule.playerId, playerId);
							params.put(NoticeRule.playerName, player.getName());
							params.put(NoticeRule.name, petConfig.getName());
							NoticePushHelper.pushNotice(NoticeID.OBTAIN_FAMOUS_PET, NoticeType.HONOR, params, config.getPriority());
						}
					}else{
						BulletinConfig config = NoticePushHelper.getConfig(NoticeID.GET_PET, BulletinConfig.class);
						if (config != null && quality >= PetRule.MIN_PET_QUALITY_NOTICE) {
							Map<String, Object> params = new HashMap<String, Object>(3);
							params.put(NoticeRule.playerId, playerId);
							params.put(NoticeRule.playerName, player.getName());
							params.put(NoticeRule.name, petConfig.getName());
							NoticePushHelper.pushNotice(NoticeID.GET_PET, NoticeType.HONOR, params, config.getPriority());
						}
					}
					
					PetDomain petDomain = petManager.createUnDrawPetDomain(pet, petBattle);
					if(petDomain == null){
						MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, userProps);
						dbService.submitUpdate2Queue(userProps); 
						ResultObject.ERROR(FAILURE);
					}
					
					petZoom.addPetDomain(petDomain);
					userProps.decreaseItemCount(1);
				}

				userPropsList.add(userProps);
				dbService.submitUpdate2Queue(userProps); 
				propsManager.removeUserPropsIfCountNotEnough(playerId, userProps.getBackpack(), userProps);
				
			}finally{
				lock.unlock();
			}
			
			GoodsLogger.goodsLogger(player, Source.PET_OPE_EGG, goodsInfos.toArray(new LoggerGoods[goodsInfos.size()]));
			PetLogger.petOpenEgg(player, propsConfig.getId() , propsCount, goodsInfos.toArray(new LoggerGoods[goodsInfos.size()]));
		}
		
		
		if(!userPropsList.isEmpty()){
			List<BackpackEntry> backpackEntrys = voFactory.getUserPropsEntries(userPropsList);
			MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, backpackEntrys);
		}
		
		return ResultObject.SUCCESS(petZoom);
	}

	
	public ResultObject<Long> drawEggPet(long playerId, long petId) {
		UserDomain userDomain = this.userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PetZoom petZoom = this.getPetZoom(playerId);
		if(petZoom == null){
			return ResultObject.ERROR(FAILURE);
		}
		
		Player player = userDomain.getPlayer();
		Collection<PetDomain> petDomains =  petManager.getPetDomains(playerId);
		
		ChainLock lock = LockUtils.getLock(player, petZoom);
		try {
			lock.lock();
			
			int petSlot = player.getMaxPetSlotSize();
			int currentSlot = petDomains == null ? 0 : petDomains.size();
			if(currentSlot >= petSlot){
				return ResultObject.ERROR(PET_SLOT_ENOUGH);
			}
			
			PetDomain petDomain = petZoom.removePetDomain(petId);
			if(petDomain == null){
				return  ResultObject.ERROR(PET_EGG_CACHE_KEY_NOT_FOUNT);
			}
			
			Pet pet = petDomain.getPet();
			PetBattle petBattle = petDomain.getBattle();
			if(pet == null || petBattle == null){
				return ResultObject.ERROR(FAILURE);
			}
			pet.setStatus(PetStatus.ACTIVE); 
			petManager.addPetInfo(petDomain);
			
			dbService.updateEntityIntime(pet);
			this.removePetFamousCache(playerId); 
			return ResultObject.SUCCESS(pet.getId());
		}finally{
			lock.unlock();
		}

	}

	
	public ResultObject<Integer> freeEggPet(long playerId, Object[] keys) {
		UserDomain userDomain = this.userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		PetZoom petZoom = this.getPetZoom(playerId);
		if(petZoom == null){
			return ResultObject.ERROR(FAILURE);
		}
		
		if(keys == null || keys.length == 0){
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		ChainLock lock = LockUtils.getLock(player,petZoom);
		try {
			lock.lock();
			int petexperience = 0;
			List<Pet> pets = new ArrayList<Pet>(2);
			for(Object key:keys){
				try {
					long petId = Long.parseLong(key.toString());
					PetDomain petDomain = petZoom.removePetDomain(petId);
					if(petDomain != null){
						Pet pet = petDomain.getPet();
						PetBattle battle = petDomain.getBattle();
						int exp = FormulaHelper.invoke(FormulaKey.PET_FREE_EXP, battle.getLevel(),battle.getQuality()).intValue();
						petexperience += exp;
						pet.setStatus(PetStatus.DROP);
						pets.add(pet);
					}
				} catch (Exception e) {
				}
			}
			
			if(!pets.isEmpty()){
				dbService.updateEntityIntime(pets.toArray(new Pet[pets.size()]));
			}
			player.setPetexperience(player.getPetexperience() + petexperience);
		}finally{
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(player);
		return ResultObject.SUCCESS(player.getPetexperience());
	}


	
	@SuppressWarnings("unchecked")
	public int useProps(long playerId, long petId, long propsId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		UserCoolTime userCoolTime = coolTimeManager.getUserCoolTime(playerId);
		if(userDomain == null || userCoolTime == null){
			return PLAYER_NOT_FOUND;
		}
		
		if(!petManager.isPlayerPet(playerId, petId)){
			return PET_NOT_FOUND;
		}
		
		PetDomain petDomain = petManager.getPetDomain(petId);
		if(petDomain == null){
			return PET_NOT_FOUND;
		}
		
		Pet pet = petDomain.getPet();
		PetBattle petBattle = petDomain.getBattle();
		
		UserProps props = propsManager.getUserProps(propsId);
		if(props == null){
			return ITEM_NOT_ENOUGH;
		} else if(props.getPlayerId() != playerId){
			return ITEM_NOT_ENOUGH;
		} else if(props.getBackpack() != BackpackType.DEFAULT_BACKPACK){
			return BACKPACK_INVALID;
		} else if(props.getCount() < PetRule.USE_ENGRY_AND_HP_ITEM_COUNT){
			return ITEM_NOT_ENOUGH;
		} else if(props.isOutOfExpiration()) {
			return OUT_OF_EXPIRATION;
		} else if(props.isTrading()) {
			return ITEM_CANNOT_USE;
		}
		
		int propsBaseId = props.getBaseId();
		PropsConfig propsConfig = propsManager.getPropsConfig(propsBaseId);
		if(propsConfig == null){
			return BASEDATA_NOT_FOUND;
		}
		
		if(propsConfig.getChildType() != PropsChildType.PET_HP_DRUG_ITEM){
		   if(propsConfig.getChildType() != PropsChildType.PET_DRUG_ENGRY_ITEM){
			    return BELONGS_INVALID;
		   }
		}
		
		CoolTimeConfig coolTime = coolTimeManager.getCoolTimeConfig(propsConfig.getCdId());
		if(coolTime == null){
			return BASEDATA_NOT_FOUND;
		}
		
		
		ChainLock lock = LockUtils.getLock(userDomain.getPackLock());
		try {
			lock.lock();
			if(props.getBackpack() != BackpackType.DEFAULT_BACKPACK){
				return BACKPACK_INVALID;
			} else if(props.getCount() < PetRule.USE_ENGRY_AND_HP_ITEM_COUNT){
				return ITEM_NOT_ENOUGH;
			}else if(coolTime != null && userCoolTime.isCoolTiming(coolTime.getId())) {
				return COOL_TIMING;
			}
			
			userCoolTime.addCoolTime(coolTime.getId(), coolTime.getCoolTime());
			props.decreaseItemCount(PetRule.USE_ENGRY_AND_HP_ITEM_COUNT);
			propsManager.removeUserPropsIfCountNotEnough(playerId, props.getBackpack(), props);
		} finally {
			lock.unlock();
		}
		
		this.dbService.submitUpdate2Queue(props,userCoolTime);
		MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, props);
		MessagePushHelper.pushGoodsCountChange2Client(playerId, GoodsVO.valueOf(props.getBaseId(), GoodsType.PROPS, -1));

		int attrValue = (int)Math.round(propsConfig.getAttrValue());
		if(propsConfig.getChildType() == PropsChildType.PET_HP_DRUG_ITEM){
			boolean gofight = this.petManager.checkFighting(playerId, petId);
			this.useHpProps(userDomain, petBattle, attrValue, gofight);
		}
		
		if(propsConfig.getChildType() == PropsChildType.PET_DRUG_ENGRY_ITEM){
			this.useEnergyProps(playerId, pet ,attrValue);
		}
		
		return SUCCESS;
	}
	
	
	private void useEnergyProps(long playerId,Pet pet,int attrValue) {
		if(pet == null || attrValue <= 0){
			return;
		}
		
		ChainLock lock = LockUtils.getLock(pet);
		try {
			lock.lock();
			pet.increaseEnergy(attrValue);
		}finally{
			lock.unlock();
		}
		
		this.petPushHelper.pushPetAttribute(Arrays.asList(playerId), playerId, pet.getId(), PetAttributeRule.PET_ENGER);
	}
	

	private void useHpProps(UserDomain userDomain,PetBattle petBattle,int attrValue,boolean gofight) {
		if(userDomain == null || petBattle == null){
			return;
		}
		
		Player player = userDomain.getPlayer();
		PlayerMotion playerMotion = userDomain.getMotion();
		PlayerDungeon playerDungeon = dungeonManager.getPlayerDungeon(player.getId());
		if(playerDungeon == null){
			return;
		}
		
		if(attrValue <= 0){
			return;
		}
		
		ChainLock lock = LockUtils.getLock(petBattle);
		try {
			lock.lock();
			petBattle.increaseHp(attrValue);
		}finally{
			lock.unlock();
		}
		
		if(gofight){
			Collection<Long> viewPlayers = null;
			if(playerDungeon.isDungeonStatus()){
				GameMap gameMap = gameMapManager.getTemporaryMap(playerDungeon.getDungeonId(), player.getBranching());
				if(gameMap != null){
					viewPlayers = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
				}
			}else{
				GameMap gameMap = gameMapManager.getGameMapById(playerMotion.getMapId(), player.getBranching());
				if(gameMap != null){
					viewPlayers = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
				}
			}
			
			this.petPushHelper.pushPetAttribute(viewPlayers, player.getId(),  petBattle.getId(), PetAttributeRule.PET_HP);
		}else{
			this.petPushHelper.pushPetAttribute(Arrays.asList(player.getId()), player.getId(), petBattle.getId(), PetAttributeRule.PET_HP);
		}
	}

	
	@SuppressWarnings("unchecked")
	public Set<Integer> loadFamous(long playerId) {
		String key = this.getFamousCommomCacheKey(playerId);
		Set<Integer> result = (Set<Integer>) this.cachedService.getFromCommonCache(key);
		if(result != null){
			return result;
		}
		
		List<Integer> famous = this.petManager.getFamousPetIds(playerId);
		result = new HashSet<Integer>(famous);
		this.cachedService.put2CommonCache(key, result);
		return (Set<Integer>) this.cachedService.getFromCommonCache(key);
	}

	@SuppressWarnings("unchecked")
	
	public ResultObject<Map<String,Object>> addPetExp(long playerId, long petId, int exp) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		exp = Math.abs(exp);
		if(exp == 0){
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle playerBattle = userDomain.getBattle();
		if(player.getPetexperience() < exp){
			return ResultObject.ERROR(PET_EXP_ENOUGH);
		}
		
		PetDomain petDomain = petManager.getPetDomain(petId);
		if(petDomain == null){
			return ResultObject.ERROR(PET_NOT_FOUND);
		}
		
		if(petDomain.getPlayerId() != playerId){
			return ResultObject.ERROR(PET_NOT_FOUND);
		}
		
		Pet pet = petDomain.getPet();
		PetBattle petBattle = petDomain.getBattle();
		if(petBattle.getLevel() >= PetRule.MAX_PET_LEVEL){
			return ResultObject.ERROR(PET_LEVEL_IS_FULL);
		}
		
		if(petBattle.getLevel() >= playerBattle.getLevel()){
			return ResultObject.ERROR(PET_LEVEL_CANT_OVER_PLAYER_LEVEL);
		}
		
		int beforeLevel = petBattle.getLevel();
		
		ChainLock lock = LockUtils.getLock(player,petBattle);
		try {
			lock.lock();
			if(player.getPetexperience() < exp){
				return ResultObject.ERROR(PET_EXP_ENOUGH);
			}
			player.setPetexperience(player.getPetexperience() - exp);
			petBattle.increaseExp(exp);
		}finally{
			lock.unlock();
		}
		
		petBattle = petDomain.getBattle();
		
	    int leveupCount = petBattle.getLevel() - beforeLevel;
	    if(leveupCount > 0 && pet.isStatus(PetStatus.FIGHTING)){
	    	GameMap gameMap = userDomain.getGameMap();
	    	if(gameMap != null){
	    		Collection<Long> playerIds = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
	    		petPushHelper.pushPetLevelUp(playerIds, playerId, petId);
	    	}
	    }
		
		dbService.submitUpdate2Queue(player,petBattle);
		Map<String,Object> result = new HashMap<String, Object>(4);
		result.put(ResponseKey.EXP, player.getPetexperience());
		result.put(ResponseKey.PET_ID, petId);
		result.put(ResponseKey.PET_EXP, petBattle.getExp()); 
		result.put(ResponseKey.PET_LEVEL, petBattle.getLevel());
		return ResultObject.SUCCESS(result);
	}

	
	@SuppressWarnings("unchecked")
	public ResultObject<Map<String, Object>> trainingMerged(long playerId, long petId, String userItem, int autoBuyCount) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PetDomain petDomain = petManager.getPetDomain(petId);
		if(petDomain == null){
			return ResultObject.ERROR(PET_NOT_FOUND);
		}
		
		if(userDomain.getPlayerId() != petDomain.getPlayerId()){
			return ResultObject.ERROR(PET_NOT_FOUND); 
		}
		
		Player player = userDomain.getPlayer();
		Pet pet = petDomain.getPet();
		PetBattle petBattle = petDomain.getBattle();
		if(petBattle.getLevel() < PetRule.PET_TRAING_MERGED_LEVEL){
			return ResultObject.ERROR(PET_LEVEL_ENOUGH);
		}
		
		PetMergedConfig mergedConfig = petManager.getPetMergedConfig(petBattle.getMergedLevel() + 1);
		if(mergedConfig == null){
			return ResultObject.ERROR(BASEDATA_NOT_FOUND); 
		}
		
		if(mergedConfig.getQuality() > petBattle.getQuality()){
			return ResultObject.ERROR(PET_MERGED_LEVEL_IS_FULL);
		}
		
		autoBuyCount = Math.abs(autoBuyCount);
		int needGolden = 0;
		if(autoBuyCount > 0){
			PropsConfig propsConfig = propsManager.getPropsConfig(mergedConfig.getPropsId());
			if(propsConfig == null){
				return ResultObject.ERROR(BASEDATA_NOT_FOUND); 
			}
			needGolden = propsConfig.getMallPrice() * autoBuyCount;
			if(needGolden > player.getGolden()){
				return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
			}
		}
		
		int goodsItemBaseId = 0;
		int totle = 0; 
		List<String[]> propslist = Tools.delimiterString2Array(userItem);
		Map<Long,Integer> userBackItems = new HashMap<Long, Integer>();
		if(propslist != null && !propslist.isEmpty()){
			for(String[] strProps : propslist){
				if(strProps.length < 2){
					continue;
				}
				long propsId = Long.parseLong(strProps[0]);
				int number = Math.abs(Integer.parseInt(strProps[1]));
				if(number == 0){
					continue;
				}
				if(userBackItems.get(propsId) != null){
					int num = userBackItems.get(propsId);
					userBackItems.put(propsId, number + num);
				}else{
					userBackItems.put(propsId, number);
				}
				totle += number;
			}
		}
		
		if((totle + autoBuyCount) != mergedConfig.getNumber()){
			return ResultObject.ERROR(ITEM_NOT_ENOUGH);
		}
		
		List<LoggerGoods> goodsInfos = new ArrayList<LoggerGoods>(3);
		for(Map.Entry<Long, Integer> entry : userBackItems.entrySet()){
			long userPropsId = entry.getKey();
			int propsCount   = entry.getValue();
			if (propsCount <= 0) {
				return ResultObject.ERROR(INPUT_VALUE_INVALID);
			}
		
			UserProps userProps = propsManager.getUserProps(userPropsId);
			if (userProps == null) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			}

			if (userProps.getPlayerId() != playerId) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			}else if (!userProps.validBackpack(DEFAULT_BACKPACK)) {
				return ResultObject.ERROR(BACKPACK_INVALID);
			}else if(userProps.isTrading()) {
				return ResultObject.ERROR(ITEM_CANNOT_USE);
			}else if(userProps.getCount() < propsCount){
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			}
			
			PropsConfig propsConfig = userProps.getPropsConfig();
			if (propsConfig == null) {
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			}else if(propsConfig.getChildType() != PropsChildType.PET_MERGED_PROPS_TYPE){
				return ResultObject.ERROR(BELONGS_INVALID);
			}
			
			goodsItemBaseId = propsConfig.getId();
			goodsInfos.add(LoggerGoods.outcomeProps(userPropsId, propsConfig.getId(), propsCount));
		}
		
		if (needGolden > 0 && autoBuyCount > 0) {
			ChainLock lock = LockUtils.getLock(player);
			try {
				lock.lock();
				if (player.getGolden() < needGolden) {
					return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
				}
				player.decreaseGolden(needGolden);
			} finally {
				lock.unlock();
			}
			goodsInfos.add(LoggerGoods.outcomePropsAutoBuyGolden(mergedConfig.getPropsId(), autoBuyCount, needGolden));
		}
		
		List<UserProps> userPropsList = propsManager.costUserPropsList(userBackItems);
		boolean isSuccess = false;
		ChainLock lock = LockUtils.getLock(petBattle);
		try {
			lock.lock();
			petBattle.checkOverMerged();
			if(petBattle.getMergedBless() < mergedConfig.getBaseBless()){ 
				petBattle.setMergedBless(mergedConfig.getSingleBless() + petBattle.getMergedBless());
				isSuccess = false;
			}else{
				int rand = Tools.getRandomInteger(mergedConfig.getFullValue()); 
				if(rand <= (mergedConfig.getBasePercent() + petBattle.getMergedBlessPercent())){
					petBattle.setMergedLevel(petBattle.getMergedLevel() + 1);
					petBattle.setMergedBlessPercent(0);
					petBattle.setMergedBless(0);
					isSuccess = true;
					
					Map<String, Object> params = new HashMap<String, Object>(3);
					params.put(NoticeRule.playerId, playerId);
					params.put(NoticeRule.playerName, userDomain.getPlayer().getName());
					params.put(NoticeRule.number, petBattle.getMergedLevel());
					params.put(NoticeRule.pet, pet.getName());
					
					if (petBattle.getMergedLevel() < 15) {
						BulletinConfig config = NoticePushHelper.getConfig(NoticeID.PET_FIT_UP, BulletinConfig.class);
						if (config != null) NoticePushHelper.pushNotice(NoticeID.PET_FIT_UP, NoticeType.HONOR, params, config.getPriority());
					} 
				}else{
					petBattle.setMergedBless(petBattle.getMergedBless() + mergedConfig.getSingleBless() ); //增加单次祝福值
					petBattle.setMergedBlessPercent(petBattle.getMergedBlessPercent() + mergedConfig.getSinglePercent());//增加单次契合度比率
					isSuccess = false;
				}
			}
		}finally{
			lock.unlock();
		}
		
		dbService.updateEntityIntime(petBattle,player);
		
		if(needGolden > 0 && autoBuyCount > 0){
			GoldLogger.outCome(Source.PET_MERGED_TRAINING, needGolden, player, goodsInfos.toArray(new LoggerGoods[goodsInfos.size()]));
		}
		
		GoodsLogger.goodsLogger(player, Source.PET_MERGED_TRAINING, goodsInfos.toArray(new LoggerGoods[goodsInfos.size()]));
		PetLogger.petMergedTraining(player, petBattle, needGolden, LogPropsID.PET_MERGED_PROPS, (totle + autoBuyCount), goodsInfos.toArray(new LoggerGoods[goodsInfos.size()]));
		
		if(!userPropsList.isEmpty()){
			MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, userPropsList);
			MessagePushHelper.pushGoodsCountChange2Client(playerId, GoodsVO.valueOf(goodsItemBaseId, GoodsType.PROPS, (totle * -1)));
		}

		List<Long> playerIdList = Arrays.asList(player.getId());
		List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(player.getId(), playerIdList, unitIdList, AttributeKeys.GOLDEN);
		
		Map<String,Object> result = new HashMap<String, Object>(4);
		result.put(ResponseKey.PET_ID, pet.getId());
		result.put(ResponseKey.LEVEL, petBattle.getMergedLevel());
		result.put(ResponseKey.PET_MERGED_BLESS, petBattle.getMergedBless());
		result.put(ResponseKey.STATE, isSuccess);
		
		return ResultObject.SUCCESS(result);
	}

	
	public int mergedPet(long playerId, long petId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		PetDomain petDomain = petManager.getPetDomain(petId);
		if(petDomain == null){
			return PET_NOT_FOUND;
		}
		
		if(petDomain.getPlayerId() != playerId){
			return PET_NOT_FOUND;
		}
		
		Pet pet = petDomain.getPet();
		PetBattle petBattle = petDomain.getBattle();
		if(petBattle.getLevel() < PetRule.PET_TRAING_MERGED_LEVEL){
			return PET_LEVEL_ENOUGH;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		Fightable beforable = battle.getAndCopyAttributes();
		
		
		int result = petManager.mergedPet(userDomain, petDomain);
		if(result != SUCCESS){
			return result;
		}
		
		GameMap gameMap = userDomain.getGameMap();
		if(gameMap != null){
			Collection<Long> viewPlayers = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
			this.petPushHelper.petMerged(playerId, petId, pet.getBaseId(), petBattle.getQuality(), viewPlayers);
		}
		
		GameMap gameMap2 = petDomain.getGameMap();
		if(gameMap2 != null){
			gameMap2.leaveMap(petDomain);
		}
		petDomain.leaveScreen();
		
		UserPushHelper.pushPlayerAttributeChange(beforable, userDomain);
		return result;
	}

	
	public int comebackPet(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		PetDomain petDomain = petManager.getPetMerged(userDomain.getBattle());
		if(petDomain == null){
			return PET_NOT_MERGED_STATUS;
		}

		if(campBattleFacade.isInCampBattle(userDomain)){
			return PET_CAMP_BATTLE_MERGED_NO_FIGHT;
		}
		
		if(battleFieldFacade.isInBattleField(playerId)){
			return PET_BATTLE_FIELD_MERGED_NO_FIGHT;
		}
		
		Pet pet = petDomain.getPet();
		PetMotion petMotion = petDomain.getMotion();
		PlayerMotion playerMotion = userDomain.getMotion();
		PlayerBattle playerBattle = userDomain.getBattle();
		Fightable beforable = playerBattle.getAndCopyAttributes();
		ChainLock lock = LockUtils.getLock(petDomain,playerBattle);
		try {
			lock.lock();
			if(!pet.isStatus(PetStatus.MERGED)){
				return PET_NOT_MERGED_STATUS;
			}
			pet.setStatus(PetStatus.FIGHTING);
			playerBattle.setFlushable(Flushable.FLUSHABLE_NORMAL);
		}finally{
			lock.unlock();
		}
		
		GameMap gampMap = userDomain.getGameMap();
		petMotion.setX(playerMotion.getX());
		petMotion.setY(playerMotion.getY());
		petDomain.changeMap(gampMap, playerMotion.getX(), playerMotion.getY());
		Collection<Long> viewPlayers = gampMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
		this.petPushHelper.petGoFighting(playerId, pet.getId(), petMotion.getX(), petMotion.getY(), viewPlayers);
		UserPushHelper.pushPlayerAttributeChange(beforable, userDomain); 
		return SUCCESS;
	}

	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		long playerId = messageInfo.getPlayerId();
		PETZOOM.remove(playerId);
	}

	
	public ResultObject<Pet> startTraingPet(long playerId, long petId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PetDomain petDomain = petManager.getPetDomain(petId);
		if(petDomain == null){
			return ResultObject.ERROR(PET_NOT_FOUND);
		}
		
		if(petDomain.getPlayerId() != playerId){
			return ResultObject.ERROR(PET_NOT_FOUND);
		}
		
		List<PetDomain> petDomains = petManager.getPetDomains(playerId);
		if(petDomains.isEmpty()){
			return ResultObject.ERROR(PET_NOT_FOUND);
		}
		
		Pet pet = petDomain.getPet();
		if(pet.isTraing()){
			return ResultObject.ERROR(PET_HAVE_TRAING);
		}
		
		Player player = userDomain.getPlayer();
		ChainLock lock = LockUtils.getLock(player);
		try {
			lock.lock();
			if(pet.isTraing()){
				return ResultObject.ERROR(PET_HAVE_TRAING);
			}
			
			for(PetDomain tmpDomain : petDomains){
				if(tmpDomain != null){
					Pet tmpPet = tmpDomain.getPet();
					if(tmpPet.isTraing()){
						return ResultObject.ERROR(PET_HAVE_TRAING);
					}
				}
			}
			
			pet.setStartTraingTime(System.currentTimeMillis());
			pet.setTotleTraingTime(0);
		}finally{
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(pet);
		return ResultObject.SUCCESS(pet);
	}

	
	public int finishTraingPet(long playerId, long petId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		PetDomain petDomain = petManager.getPetDomain(petId);
		if(petDomain == null){
			return PET_NOT_FOUND;
		}
		
		if(petDomain.getPlayerId() != playerId){
			return PET_NOT_FOUND;
		}
		
		Pet pet = petDomain.getPet();
		if(!pet.isTraing()){
			return PET_NOT_HAVE_TRAING;
		}
		
		Player player = userDomain.getPlayer();
		ChainLock lock = LockUtils.getLock(player);
		try {
			lock.lock();
			if(!pet.isTraing()){
				return PET_NOT_HAVE_TRAING;
			}
			
			pet.setTotleTraingTime(0);
			pet.setStartTraingTime(0);
		}finally{
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(pet);
		return SUCCESS;
	}

	
	@SuppressWarnings("unchecked")
	public  ResultObject<Map<String,Object>> calcTraingPet(long playerId, long petId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PetDomain petDomain = petManager.getPetDomain(petId);
		if(petDomain == null){
			return ResultObject.ERROR(PET_NOT_FOUND);
		}
		
		if(petDomain.getPlayerId() != playerId){
			return ResultObject.ERROR(PET_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle playerBattle = userDomain.getBattle();
		Pet pet = petDomain.getPet();
		PetBattle petBattle = petDomain.getBattle();
		if(!pet.isTraing()){
			return ResultObject.ERROR(PET_NOT_HAVE_TRAING);
		}
		
		if(pet.isOverTraing()){
			return ResultObject.ERROR(PET_TRAING_FULL);
		}
		
		if(petBattle.getLevel() >= playerBattle.getLevel()){
			ResultObject.ERROR(PET_LEVEL_CANT_OVER_PLAYER_LEVEL);
		}

		int exp = 0;
		int leveupCount = 0;
		int beforeLevel = petBattle.getLevel();
		
	    ChainLock lock = LockUtils.getLock(player,petBattle);
	    try {
	    	lock.lock();
			if(pet.isOverTraing()){
				return ResultObject.ERROR(PET_TRAING_FULL);
			}
	    	
	    	long timeSecond = DateUtil.getCurrentSecond() - (pet.getStartTraingTime()/1000); 
	    	long timeMinute = timeSecond / 60; 
	    	if(timeMinute == 0){
	    		return ResultObject.ERROR(PET_CALC_TIME_NOT_REACH);
	    	}
	    	
	        long totleTime = (28800L - pet.getTotleTraingTime())/60;
	        timeMinute = totleTime - timeMinute < 0 ? timeMinute - totleTime : timeMinute;
	    	pet.setStartTraingTime(System.currentTimeMillis());
	    	pet.setTotleTraingTime(pet.getTotleTraingTime() + timeSecond);
	    	
	    	int playerLevel = playerBattle.getLevel();
	    	
	    	if(petBattle.getLevel() < 30){
	    		exp = FormulaHelper.invoke(FormulaKey.PET_TRAING_EXP_BEFORE,playerLevel,timeMinute).intValue();
	    	}else{
	    		exp = FormulaHelper.invoke(FormulaKey.PET_TRAING_EXP_AFTER,playerLevel,timeMinute).intValue();
	    	}
	    	
	    	exp = exp < 0 ? 0 : exp;
			petBattle.increaseExp(exp);
		}finally{
			lock.unlock();
		}
	    
	    petBattle = petDomain.getBattle();
	    leveupCount = petBattle.getLevel() - beforeLevel;
	    if(leveupCount > 0 && pet.isStatus(PetStatus.FIGHTING)){
	    	GameMap gameMap = userDomain.getGameMap();
	    	if(gameMap != null){
	    		Collection<Long> playerIds = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
	    		petPushHelper.pushPetLevelUp(playerIds, playerId, petId);
	    	}
	    }
	    
	    dbService.submitUpdate2Queue(pet,petBattle);
	    Map<String, Object> resultObject = new HashMap<String, Object>(8);
	    resultObject.put(ResponseKey.RESULT, SUCCESS);
	    resultObject.put(ResponseKey.PET_ID, petId);
	    resultObject.put(ResponseKey.LEVEL, petBattle.getLevel());
	    resultObject.put(ResponseKey.EXP, petBattle.getExp());
	    resultObject.put(ResponseKey.ADD_EXP, exp);
	    resultObject.put(ResponseKey.LEVEUP_COUNT, leveupCount);
	    resultObject.put(ResponseKey.START_TIME, pet.getStartTraingTime());
	    resultObject.put(ResponseKey.TOTLE_TIME, pet.getTotleTraingTime());
		return ResultObject.SUCCESS(resultObject);
	}

	
	public ResultObject<Integer> openPetSolt(long playerId, String propsIds,int autoBuyCount) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		
		if(propsIds == null){
			return ResultObject.ERROR(INPUT_VALUE_INVALID);
		}
		
		List<String[]> propslist = Tools.delimiterString2Array(propsIds);
		Map<Long,Integer> mapProps = new HashMap<Long, Integer>(3);
		
		if(propslist != null){
			for(String[] strProps : propslist){
				if(strProps.length < 2){
					continue;
				}
				try {
					long propsId = Long.parseLong(strProps[0]);
					int count = Math.abs(Integer.parseInt(strProps[1]));
					if(mapProps.get(propsId) == null){
						mapProps.put(propsId, count);
					}else{
						int tmpCount = mapProps.get(propsId) + count;
						mapProps.put(propsId, tmpCount);
					}
				} catch (Exception e) {
					return ResultObject.ERROR(INPUT_VALUE_INVALID);
				}
			}
		}

		
		List<LoggerGoods> logGoodsInfos = new ArrayList<LoggerGoods>(2);
		int totleCount = 0;
		int costGold = 0;
		for(Entry<Long,Integer> entry : mapProps.entrySet()){
			long propsId = entry.getKey();
			int count = entry.getValue();
			
			UserProps userProps = propsManager.getUserProps(propsId);
			if(userProps == null){
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			} else if(userProps.getPlayerId() != player.getId()){
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			} else if(userProps.getBackpack() != BackpackType.DEFAULT_BACKPACK){
				return ResultObject.ERROR(BACKPACK_INVALID);
			} else if(userProps.getCount() < count){
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			} else if(userProps.isOutOfExpiration()) {
				return ResultObject.ERROR(OUT_OF_EXPIRATION);
			}
			
			PropsConfig propsConfig = propsManager.getPropsConfig(userProps.getBaseId());
			if(propsConfig == null){
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			} else if(propsConfig.getChildType() != PropsChildType.CONTAINER_PAGE_TYPE){
				return ResultObject.ERROR(BELONGS_INVALID);
			}
			
			int baseId = propsConfig.getId();
			logGoodsInfos.add(LoggerGoods.outcomeProps(propsId, baseId , count));
			totleCount += count;
			
		}
		
		int needNumber = FormulaHelper.invoke(FormulaKey.PET_OPEN_SOLT_FORMULA, player.getMaxPetSlotSize()).intValue();
		
		ChainLock lock = LockUtils.getLock(player.getPackLock());
		try {
			lock.lock();
			if((totleCount + autoBuyCount) !=  needNumber){
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			}
			
			if(autoBuyCount > 0){
				PropsConfig propsConfig = propsManager.getPropsConfig(PetRule.OPEN_PET_SOLT_PROPS_BASE_ID);
				if(propsConfig == null){
					return ResultObject.ERROR(BASEDATA_NOT_FOUND);
				} else if(propsConfig.getChildType() != PropsChildType.CONTAINER_PAGE_TYPE){
					return ResultObject.ERROR(BELONGS_INVALID);
				}
				costGold = autoBuyCount * propsConfig.getMallPrice();
				if(costGold > player.getGolden()){
					return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
				}
				
				logGoodsInfos.add(LoggerGoods.outcomePropsAutoBuyGolden(PetRule.OPEN_PET_SOLT_PROPS_BASE_ID, autoBuyCount, costGold));
				player.decreaseGolden(costGold);
			}
			
			player.setMaxPetSlotSize(player.getMaxPetSlotSize() + 1);
			
		}finally{
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(player);
		List<UserProps> costUserPropsList = propsManager.costUserPropsList(mapProps);
		MessagePushHelper.pushUserProps2Client(playerId, DEFAULT_BACKPACK, false, costUserPropsList);
		
		if(costGold > 0){
			GoldLogger.outCome(Source.PET_OPEN_SOLT, costGold, player, logGoodsInfos.toArray(new LoggerGoods[logGoodsInfos.size()]));
			List<Long> playerIdList = Arrays.asList(player.getId());
			List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
			UserPushHelper.pushAttribute2AreaMember(player.getId(), playerIdList, unitIdList,AttributeKeys.GOLDEN);
		}
		GoodsLogger.goodsLogger(player, Source.PET_OPEN_SOLT, logGoodsInfos.toArray(new LoggerGoods[logGoodsInfos.size()]));
		
		return ResultObject.SUCCESS(player.getMaxPetSlotSize());
	}
}
