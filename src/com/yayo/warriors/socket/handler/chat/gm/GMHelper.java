package com.yayo.warriors.socket.handler.chat.gm;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.yayo.warriors.module.chat.parser.context.GMCommandParser;
import com.yayo.warriors.module.chat.parser.context.GMContext;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.util.GameConfig;


@Component
public class GMHelper implements ApplicationListener<ContextRefreshedEvent> {
	@Autowired
	private GMContext gmContext;
	
	public boolean executeCode(UserDomain userDomain, String info) {
		if(!GameConfig.isEnableGM() || StringUtils.isBlank(info)) {
			return false;
		}
		
		if(!info.startsWith(GmType.GM_START)) {
			return false;
		}
		
		String[] arrays = info.split(" ");
		if(arrays.length <= 1) {
			return false;
		}
		
		String start = arrays[0].trim();
		if(!start.equalsIgnoreCase(GmType.GM_START)) {
			return false;
		}
		
		String command = arrays[1].trim();
		if(StringUtils.isBlank(command)) {
			return false;
		}
		
		GMCommandParser parser = gmContext.getParser(command);
		return parser != null && parser.execute(userDomain, arrays);
	}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
//		String attribute = arrays[1].trim();
//		if(arrays.length > 2) {
//			if(attribute.equalsIgnoreCase("silver")) {
//				Integer addSilver = Integer.valueOf(arrays[2].trim());
//				return adminFacade.addMoney(playerId, 0, addSilver, true);
//			} else if(attribute.equalsIgnoreCase("golden")) {
//				Integer addGolden = Integer.valueOf(arrays[2].trim());
//				return adminFacade.addMoney(playerId, addGolden, 0, true);
//			} else if(attribute.equalsIgnoreCase("exp")) {
//				return addPlayerExp(playerId, Long.valueOf(arrays[2].trim()));
//			} else if(attribute.equalsIgnoreCase("hp")) {
//				return addPlayerHP(playerId, Integer.valueOf(arrays[2].trim()));
//			} else if(attribute.equalsIgnoreCase("mp")) {
//				return addPlayerMP(playerId, Integer.valueOf(arrays[2].trim()));
//			} else if(attribute.equalsIgnoreCase("gas")) {
//				return addPlayerGas(playerId, Integer.valueOf(arrays[2].trim()));
//			} else if(attribute.equalsIgnoreCase("sp")) {
//				return addPlayerSP(playerId, Integer.valueOf(arrays[2].trim()));
//			} else if(attribute.equalsIgnoreCase("props")) { //添加道具
//				return addPlayerProps(playerId, Integer.valueOf(arrays[2].trim()), Integer.valueOf(arrays[3].trim()));
//			} else if(attribute.equalsIgnoreCase("equip")) { //增加装备
//				return addPlayerEquips(playerId, Integer.valueOf(arrays[2].trim()), Integer.valueOf(arrays[3].trim()));
//			} else if(attribute.equalsIgnoreCase("horse")) { //幻化坐骑
//				int count = Integer.valueOf(arrays[2].trim()) ;
//				for(int i = 0 ; i < count ; i ++){
//					horseFacade.defineFancy(playerId, 99);
//				}
//				return true;
//			} else if(attribute.equalsIgnoreCase("eqs")) { //增加大量装备
//				for(int i = 2 ; i < arrays.length ; i ++){
//					addPlayerEquips(playerId, Integer.valueOf(arrays[i].trim()),1);
//				}
//				return true ;
//			} else if(attribute.equalsIgnoreCase("cache")) { //清除缓存
//				resourceService.reloadAll();
//				return true;
//			} else if(attribute.equalsIgnoreCase("robot")) {
//				return addRobot(playerId, Integer.valueOf(arrays[2].trim()),Integer.valueOf(arrays[3].trim())); 
//			} else if(attribute.equalsIgnoreCase("dungeon")){
//				dungeonFacade.enterDungeon(playerId, Integer.valueOf(arrays[2].trim()));
//				return true;
//			} else if(attribute.equalsIgnoreCase("userTask")) {
//				return updateUserTask(Long.valueOf(arrays[2]));
//			} else if (attribute.equalsIgnoreCase("level")) {
//				return addPlayerLevel(playerId, Integer.valueOf(arrays[2].trim()));
//			} else if(attribute.equalsIgnoreCase("completeTask")) {
//				return fastCompleteTask(playerId, Integer.valueOf(arrays[2].trim()));
//			} else if (attribute.equalsIgnoreCase("mortalLevel")) {
//				return addMortalLevel(playerId, Integer.valueOf(arrays[2].trim()));
//			} else if (attribute.equalsIgnoreCase("guide")) {
//				return updateGuideStep(playerId, arrays);
//			} else if(attribute.equalsIgnoreCase("go")){
//				return go(playerId, arrays);
//			} else if(attribute.equalsIgnoreCase("pethp")){
//				return addPetHp(playerId, Integer.valueOf(arrays[2].trim()));
//			}
//		}
//		
//		if(attribute.equalsIgnoreCase("removeTask")) {
//			taskManager.removeAllTask(playerId);
//			return true;
//		} else if(attribute.equalsIgnoreCase("shutdown")) {
//			return adminFacade.shutdownServer();
//		} else if(attribute.equalsIgnoreCase("removeCamp")) {
//			UserDomain userDomain = userManager.getUserDomain(playerId);
//			Player player = userDomain.getPlayer();
//			Camp camp = player.getCamp();
//			onlineStatisticManager.subCampOnline(camp);
//			player.setCamp(Camp.NONE);
//			onlineStatisticManager.addCampOnline(Camp.NONE);
////			dbService.updateEntityIntime(player);
//			dbService.submitUpdate2Queue(player);
//			Collection<Long> playerIdList = Arrays.asList(playerId);
//			UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, Arrays.asList(userDomain.getUnitId()), AttributeKeys.CAMP);
//			return true;
//		} else if(attribute.equalsIgnoreCase("rank")){
//			rankManager.refreshAllRank();
//			return true;
//		} else if(attribute.equalsIgnoreCase("clearBackpack")){
//			clearBackpack(playerId);
//			return true;
//		}
//		return false;
//	}
//
//	private boolean go(long playerId, String... arrays) {
//		if(arrays == null || arrays.length < 5){
//			return false;
//		}
//		String mapName = arrays[2];
//		BigMapConfig bigMapConfig = resourceService.getByUnique(IndexName.BIG_MAP_NAME, BigMapConfig.class, mapName);
//		if(bigMapConfig != null){
//			int x = Integer.valueOf(arrays[3]);
//			int y = Integer.valueOf(arrays[4]);
//			return mapFacade.go(playerId, bigMapConfig.getMapId(), x, y,0);
//		}
//		return false;
//	}
//
//	/**
//	 * 清空背包
//	 * @param playerId
//	 */
//	@SuppressWarnings("unchecked")
//	private void clearBackpack(long playerId) {
//		List<UserProps> userPropsList = propsManager.listUserProps(playerId, BackpackType.DEFAULT_BACKPACK);
//		List<UserEquip> userEquipList = propsManager.listUserEquip(playerId, BackpackType.DEFAULT_BACKPACK);
//		UserDomain userDomain = userManager.getUserDomain(playerId);
//		ChainLock lock = LockUtils.getLock(userDomain.getPackLock());
//		lock.lock();
//		try {
//			for(UserProps userProps : userPropsList){
//				userProps.setCount(0);
//			}
//			
//			for(UserEquip userEquip : userEquipList){
//				userEquip.setCount(0);
//			}
//			dbService.submitUpdate2Queue(userPropsList, userEquipList);
//			propsManager.put2UserEquipIdsList(playerId, BackpackType.DROP_BACKPACK, userEquipList);
//			propsManager.removeFromEquipIdsList(playerId, BackpackType.DEFAULT_BACKPACK, userEquipList);
//			propsManager.put2UserPropsIdsList(playerId, BackpackType.DROP_BACKPACK, userPropsList);
//			propsManager.removeFromUserPropsIdsList(playerId, BackpackType.DEFAULT_BACKPACK, userPropsList);
//			
//		} finally {
//			lock.unlock();
//		}
//		
//		List<BackpackEntry> backpackEntrys = new ArrayList<BackpackEntry>();
//		backpackEntrys.addAll(userPropsList);
//		backpackEntrys.addAll(userEquipList);
//		MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, backpackEntrys);
//	}
//	
//	/**
//	 * 快速完成任务
//	 * 
//	 * @param  	playerId
//	 * @param 	taskId
//	 * @return  {@link Boolean}
//	 */
//	private boolean fastCompleteTask(long playerId, int taskId) {
//		try {
//			Set<Integer> taskIds = new HashSet<Integer>();
//			TaskConfig task = resourceService.get(taskId, TaskConfig.class);
//			while(task != null) {
//				taskId = task.getId();
//				taskIds.add(taskId);
//				task = null;
//				TaskConfig previousTask = taskManager.getPreviousTask(taskId);
//				if(previousTask != null) {
//					task = resourceService.get(previousTask.getId(), TaskConfig.class);
//				}
//			}
//			
//			TaskComplete taskComplete = taskManager.getTaskComplete(playerId);
//			taskComplete.getCompleteIdSet().clear();
//			taskComplete.getCompleteIdSet().addAll(taskIds);
//			taskComplete.updateCompleteSet();
//			taskManager.fastCompleteTask(playerId, taskComplete);
//			return true;
//		} catch (Exception e) {
//			LOGGER.error("gm指令报错:{}", e);
//			return false;
//		}
//	}
//
//	/**
//	 * 更新用户任务
//	 * 
//	 * @param userTaskId
//	 * @return
//	 */
//	private boolean updateUserTask(long userTaskId){ 
//		UserTask userTask = taskManager.getUserTask(userTaskId);
//		if(userTask == null) {
//			return false;
//		}
//		
//		ChainLock lock = LockUtils.getLock(userTask);
//		try {
//			lock.lock();
//			TaskEvent[] taskEvents = userTask.getTaskEvents();
//			for (TaskEvent taskEvent : taskEvents) {
//				taskEvent.setAmount(0);
//				taskEvent.updateTaskState();
//			}
//			userTask.updateUserTaskEvents();
//			userTask.checkUserTaskStatus();
//		} finally {
//			lock.unlock();
//		}
//		dbService.submitUpdate2Queue(userTask);
//		TaskPushHelper.pushUserTask2Client(userTask.getPlayerId(), Arrays.asList(userTask));
//		return true;
//	}
//	/**
//	 * 增加角色装备
//	 * 
//	 * @param  playerId
//	 * @param  equipId		
//	 * @param valueOf2
//	 * @return
//	 */
//	private boolean addPlayerEquips(long playerId, int equipId, int count) {
//		UserDomain userDomain = userManager.getUserDomain(playerId);
//		if(userDomain == null || count <= 0) {
//			return false;
//		}
//		int backpack = DEFAULT_BACKPACK;
//		Player player = userDomain.getPlayer();
//		List<UserEquip> userEquips = EquipHelper.newUserEquips(playerId, backpack, equipId, false, count);
//		if(userEquips == null || userEquips.isEmpty()) {
//			return false;
//		}
//		
//		int currSize = propsManager.getBackpackSize(playerId, backpack);
//		ChainLock lock = LockUtils.getLock(userDomain.getPackLock());
//		try {
//			lock.lock();
//			if(!player.canAddNew2Backpack(currSize + count, backpack)) {
//				return false;
//			}
//			
//			userEquips = propsManager.createUserEquip(userEquips);
//			propsManager.put2UserEquipIdsList(playerId, backpack, userEquips);
//		} catch (Exception e) {
//			LOGGER.error("{}", e);
//			return false;
//		} finally {
//			lock.unlock();
//		}
//		
//		List<BackpackEntry> backpackEntries = voFactory.getUserEquipEntries(userEquips);
//		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, backpackEntries);
//		return true;
//	}
//
//	/**
//	 * 给角色增加道具
//	 * 
//	 * @param  playerId			角色ID
//	 * @param  propsId			道具ID
//	 * @param  count			道具数量	
//	 * @return {@link Boolean}	是否添加成功
//	 */
//	private boolean addPlayerProps(long playerId, int propsId, int count) {
//		UserDomain userDomain = userManager.getUserDomain(playerId);
//		if(userDomain == null || count <= 0) {
//			return false;
//		}
//		
//		Player player = userDomain.getPlayer();
//		PropsConfig props = propsManager.getPropsConfig(propsId);
//		if(props == null) {
//			return false;
//		}
//
//		int backpack = BackpackType.DEFAULT_BACKPACK;
//		PropsStackResult stackResult = PropsHelper.calcPropsStack(playerId, backpack, propsId, count, false);
//		List<UserProps> newPropsList = stackResult.getNewUserProps();
//		int currBackSize = propsManager.getBackpackSize(playerId, backpack);
//		ChainLock lock = LockUtils.getLock(userDomain.getPackLock());
//		try {
//			lock.lock();
//			if(!newPropsList.isEmpty()) {
//				if(!player.canAddNew2Backpack(currBackSize + newPropsList.size(), backpack)) {
//					return false;
//				}
//				newPropsList = propsManager.createUserProps(newPropsList);
//				propsManager.put2UserPropsIdsList(playerId, backpack, newPropsList);
//			}
//		} catch (Exception e) {
//			LOGGER.error("角色: [{}] 增加道具异常:{}", playerId, e);
//			return false;
//		} finally {
//			lock.unlock();
//		}
//		
//		List<BackpackEntry> backpackEntrys = new ArrayList<BackpackEntry>();
//		if(!newPropsList.isEmpty()) {
//			backpackEntrys.addAll(voFactory.getUserPropsEntries(newPropsList));
//		}
//		
//		Map<Long, Integer> mergeProps = stackResult.getMergeProps();
//		
//		if(!mergeProps.isEmpty()) {
//			List<UserProps> updateProps = propsManager.updateUserPropsList(mergeProps);
//			backpackEntrys.addAll(voFactory.getUserPropsEntries(updateProps));
//		}
//		
//		if(!backpackEntrys.isEmpty()) {
//			BackpackEntry[] backpackArray = backpackEntrys.toArray(new BackpackEntry[backpackEntrys.size()]);
//			MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, backpackArray);
//		}
//		return true;
//	}
//
//	/**
//	 * 增加角色的经验值
//	 * 
//	 * @param  playerId			角色ID
//	 * @param  addExp			增加经验
//	 * @return {@link Boolean}	是否合法
//	 */
//	private boolean addPlayerExp(long playerId, long addExp) {
//		return userManager.addPlayerExp(playerId, addExp, true);
//	}
//
//	/**
//	 * 增加角色HP
//	 * 
//	 * @param playerId		
//	 * @param addHp
//	 * @return
//	 */
//	private boolean addPlayerHP(long playerId, int addHp) {
//		UserDomain userDomain = userManager.getUserDomain(playerId);
//		if(userDomain == null) {
//			return false;
//		}
//		
//		PlayerBattle playerBattle = userDomain.getBattle();
//		if(playerBattle == null) {
//			return false;
//		}
//		
//		int hpAttribute = AttributeKeys.HP;
//		boolean isDead = playerBattle.isDead(); 
//		playerBattle.increaseAttribute(hpAttribute, addHp);
//		Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
//		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, Arrays.asList(userDomain.getUnitId()), hpAttribute);
//		dbService.submitUpdate2Queue(playerBattle);
//		if(isDead) {
//			UserPushHelper.pushPlayerRecurrent(userDomain.getUnitId(), playerBattle.getHp(), playerIdList);
//		}
//		return true;
//	}
//	/**
//	 * 增加角色Gas
//	 * 
//	 * @param  playerId			角色ID	
//	 * @param  addGas			增加的真气		
//	 * @return {@link Boolean}	返回真气值	
//	 */
//	private boolean addPlayerGas(long playerId, int addGas) {
//		UserDomain userDomain = userManager.getUserDomain(playerId);
//		if(userDomain == null) {
//			return false;
//		}
//		
//		PlayerBattle playerBattle = userDomain.getBattle();
//		if(playerBattle == null) {
//			return false;
//		}
//		
//		int gasAttribute = AttributeKeys.GAS;
//		playerBattle.increaseAttribute(gasAttribute, addGas);
//		Collection<Long> playerIdList = Arrays.asList(playerId);
//		List<UnitId> playerUnits = Arrays.asList(userDomain.getUnitId());
//		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, playerUnits, gasAttribute);
//		dbService.submitUpdate2Queue(playerBattle);
//		return true;
//	}
//
//	/**
//	 * 增加角色MP
//	 * 
//	 * @param playerId		
//	 * @param addMp
//	 * @return
//	 */
//	private boolean addPlayerMP(long playerId, int addMp) {
//		UserDomain userDomain = userManager.getUserDomain(playerId);
//		if(userDomain == null) {
//			return false;
//		}
//		
//		PlayerBattle playerBattle = userDomain.getBattle();
//		if(playerBattle == null) {
//			return false;
//		}
//		
//		int mpAttribute = AttributeKeys.MP;
//		playerBattle.increaseAttribute(mpAttribute, addMp);
//		Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
//		List<UnitId> playerUnits = Arrays.asList(userDomain.getUnitId());
//		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, playerUnits, mpAttribute);
//		dbService.submitUpdate2Queue(playerBattle);
//		return true;
//	}
//	
//	
//	private boolean addPlayerLevel(long playerId, int addLevel) {
//		UserDomain userDomain = userManager.getUserDomain(playerId);
//		if(userDomain == null) {
//			return false;
//		}
//		
//		PlayerBattle playerBattle = userDomain.getBattle();
//		if(playerBattle == null) {
//			return false;
//		}
//		int level = Math.abs(addLevel);
//		if (level > PlayerRule.MAX_PLAYER_LEVEL) {
//			level = PlayerRule.MAX_PLAYER_LEVEL;
//		} else if (level <= 0) {
//			level = PlayerRule.INIT_DEFAULT_LEVEL;
//		} 
//		
//		playerBattle.setExp(0);
//		playerBattle.setAttribute(AttributeKeys.LEVEL, level);
//		playerBattle.setFlushable(Flushable.FLUSHABLE_NORMAL);
//		Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
//		Object[] attrs = ArrayUtils.add(AttributeRule.AREA_MEMBER_VIEWS_PARAMS, AttributeKeys.LEVEL);
//		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, Arrays.asList(userDomain.getUnitId()), attrs);
//		dbService.submitUpdate2Queue(playerBattle);
//		return true;
//	}
//	
//	
//	private boolean addMortalLevel(long playerId, int mortalLevel) {
//		UserDomain userDomain = userManager.getUserDomain(playerId);
//		if(userDomain == null) {
//			return false;
//		}
//		
//		int level = Math.abs(mortalLevel);
//		if (level > MortalRule.MAX_LEVEL) {
//			level = MortalRule.MAX_LEVEL;
//		}
//		
//		PlayerBattle playerBattle = userDomain.getBattle();
//		UserMortalBody mortal = mortalManager.getUserMortalBody(playerBattle);
//		if(mortal == null){
//			return false;
//		}
//		
//		for (int i = 0; i <= MortalRule.TYPE_LIMIT; i++) {
//			mortal.putMortalLevel(i, level);
//			MortalPushHelper.pushChangeAttr2Client(playerId, i, level, new int[0], new int[0]);
//		}
//		mortal.updateMortalBodyMap();
//		playerBattle.setFlushable(Flushable.FLUSHABLE_NORMAL);
//		
//		//推送玩家属性的改变给周围的玩家
//		Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
//		List<UnitId> unitIds = Arrays.asList(userDomain.getUnitId());
//		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIds, AttributeRule.AREA_MEMBER_VIEWS_PARAMS);
//		UserPushHelper.pushAttribute2AreaMember(playerId, Arrays.asList(playerId), unitIds,  AttributeRule.ATTRIBUTE_KEYS);
//		return true;
//	}
//	
//	/**
//	 * 修改新手引导步骤
//	 * @param playerId
//	 * @param params
//	 * @return
//	 */
//	private boolean updateGuideStep(long playerId, String... params) {
//		if(params == null || params.length < 3){
//			return false;
//		}
//		UserDomain userDomain = userManager.getUserDomain(playerId);
//		if(userDomain == null) {
//			return false;
//		}
//		
//		boolean add = params[2].trim().equals("+");
//		Player player = userDomain.getPlayer();
//		Set<Integer> guides = player.getGuides();
//		synchronized (guides) {
//			if(params.length > 3){
//				String[] steps = params[3].split(",");
//				for(int i = 0; i< steps.length; i++){
//					Integer step = Integer.valueOf(params[i].trim());
//					if(add){
//						guides.add( step );
//					} else{
//						guides.remove( step );
//					}
//				}
//				
//			} else {
//				guides.clear();
//			}
//			
//		}
//		//推送玩家属性改变
//		List<UnitId> unitIds = Arrays.asList(userDomain.getUnitId());
//		UserPushHelper.pushAttribute2AreaMember(playerId, Arrays.asList(playerId), unitIds, AttributeKeys.GUIDE_INFO);
//		return true;
//	}
//	
//	
//	/**
//	 * 增加角色SP
//	 * 
//	 * @param playerId		
//	 * @param addHp
//	 * @return
//	 */
//	private boolean addPlayerSP(long playerId, int addSp) {
////		PlayerBattle playerBattle = userFacade.getPlayerBattle(playerId);
////		if(playerBattle != null) {
////			int spAttribute = AttributeKeys.SP;
////			playerBattle.increaseAttribute(spAttribute, addSp);
////			Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
////			List<UnitId> playerUnits = Arrays.asList(UnitId.valueOf(playerId, ElementType.PLAYER));
////			UserPushHelper.pushAttribute2AreaMember(playerIdList, playerUnits, spAttribute);
////			return true;
////		}
//		return false;
//	}
//	
//	private boolean addPetHp(long playerId,int hp){
//		PetDomain petDomain = petManager.getFightingPet(playerId);
//		if(petDomain == null){
//			return false;
//		}
//		
//		PetBattle petBattle = petDomain.getBattle();
//		ChainLock lock = LockUtils.getLock(petBattle);
//		try {
//			lock.lock();
//			if(hp < 0){
//				petBattle.setHp(petBattle.getHp() - (hp*-1));
//			}else{
//				petBattle.setHp(petBattle.getHp() + hp);
//			}
//		}finally{
//			lock.unlock();
//		}
//		
//		petPushHelper.pushPetAttribute(Arrays.asList(playerId), playerId, petDomain.getId(), PetAttributeRule.PET_HP);
//		
//		return true;
//	}
//
//	private boolean addRobot(long playerId, int addCount, int branching) {
//		robotControler.addThread(addCount,branching);
//		return true;
//	}
	
	
	public void onApplicationEvent(ContextRefreshedEvent event) {
//		robotControler.addThread(50000);
	}
	
	
}
