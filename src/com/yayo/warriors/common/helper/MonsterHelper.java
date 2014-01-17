package com.yayo.warriors.common.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.push.Pusher;
import com.yayo.warriors.basedb.adapter.SkillService;
import com.yayo.warriors.basedb.model.BigMapConfig;
import com.yayo.warriors.basedb.model.BulletinConfig;
import com.yayo.warriors.basedb.model.CoolTimeConfig;
import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.basedb.model.SkillConfig;
import com.yayo.warriors.basedb.model.SkillEffectConfig;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.module.buffer.model.Buffer;
import com.yayo.warriors.module.cooltime.entity.UserCoolTime;
import com.yayo.warriors.module.cooltime.manager.CoolTimeManager;
import com.yayo.warriors.module.fight.facade.FightFacade;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.map.types.ScreenType;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.entity.MonsterInfo;
import com.yayo.warriors.module.monster.manager.MonsterManager;
import com.yayo.warriors.module.monster.model.IMonsterConfig;
import com.yayo.warriors.module.monster.model.MonsterAttach;
import com.yayo.warriors.module.monster.model.MonsterBattle;
import com.yayo.warriors.module.monster.model.MonsterBuffer;
import com.yayo.warriors.module.notice.rule.NoticeRule;
import com.yayo.warriors.module.notice.type.NoticeID;
import com.yayo.warriors.module.notice.type.NoticeType;
import com.yayo.warriors.module.skill.type.SkillEffectType;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.rule.PlayerRule;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.handler.map.MapCmd;
import com.yayo.warriors.type.ElementType;

/**
 * 怪物的帮助类
 * 
 * @author Hyint
 */
@Component
public class MonsterHelper {

	@Autowired
	private MapFacade mapFacade ;
	@Autowired
	private UserManager userManager;
	@Autowired
	private FightFacade fightFacade;
	@Autowired
	private SkillService skillService;
	@Autowired
	private CoolTimeManager coolTimeManager;
	@Autowired
	private WorldPusherHelper worldPusherHelper;
	@Autowired
	private Pusher pusher;
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private MonsterManager monsterManager;
	@Autowired
	private DbService dbService;
	
	/** 静态引用 */
	private static final ObjectReference<MonsterHelper> ref = new ObjectReference<MonsterHelper>();
	/** 日志 */
	protected static final Logger logger = LoggerFactory.getLogger(MonsterHelper.class);
	
	@PostConstruct
	protected void init(){
		ref.set(this);
	}
	
	private static MonsterHelper getInstance() {
		return ref.get();
	}
	
	
	public static UserDomain getPlayerMotion(long playerId){
		return getInstance().userManager.getUserDomain(playerId);
	}
	
	/**
	 * 记录怪物复活时间
	 * @param branch
	 * @param monsterConfigId
	 * @param reviveTime
	 * @param monsterKiller
	 * @param isBoss
	 * @param screenType
	 */
	public static void recordMonsterResurrection(int branch, int monsterConfigId, long reviveTime , long monsterKiller, boolean isBoss, int screenType){
		if(!isBoss || reviveTime <= 0 ){
			return ;
		}
		if(screenType == ScreenType.FIELD.ordinal() || screenType == ScreenType.NEUTRAL.ordinal() ){
			MonsterInfo monsterInfo = getInstance().monsterManager.getMonsterInfo(branch);
			monsterInfo.recordResurrection(monsterConfigId, monsterKiller, reviveTime);
			getInstance().dbService.submitUpdate2Queue(monsterInfo);
		}
	}
	
	/**
	 * 检查怪物的BUFF过期信息
	 * 
	 * @param monsterBuffer
	 * @return
	 */
	public static boolean calcMonsterBufferTimeOut(MonsterBuffer monsterBuffer) {
		if(monsterBuffer == null || monsterBuffer.isBufferEmpty()) {
			return false;
		}
		
		boolean hasTimeOut = false;
		ChainLock lock = LockUtils.getLock(monsterBuffer);
		try {
			lock.lock();
			Map<Integer, Buffer> bufferInfoMap = monsterBuffer.getBufferInfoMap();
			for (Iterator<Entry<Integer, Buffer>> it = bufferInfoMap.entrySet().iterator(); it.hasNext();) {
				Entry<Integer, Buffer> entry = it.next();
				Buffer buffer = entry.getValue();
				if(buffer == null || buffer.isTimeOut()) {
					it.remove();
					hasTimeOut = true;
				}
			}
		} finally {
			lock.unlock();
		}
 		return hasTimeOut;
	}
	
	/**
	 * 计算怪物的战斗属性
	 * 
	 * @param  battle					怪物的战斗属性
	 * @return {@link MonsterBattle}	怪物战斗属性
	 */
	public static MonsterBattle calcMonsterBattle(MonsterBattle battle, MonsterDomain monsterDomain) {
		if(battle == null || !battle.isFlushable()) {
			return battle;
		}
		
		boolean isFullHpMp = false;
		ChainLock lock = LockUtils.getLock(battle);
		try {
			lock.lock();
			if(!battle.isFlushable()) {
				return battle;
			}
			
			int currentHp = battle.getHp();
			int currentMp = battle.getMp();
			isFullHpMp = battle.isFullHpMpFlushable();
			battle.updateFlushable(Flushable.FLUSHABLE_NOT);
			MonsterFightConfig monsterFight = battle.getMonsterFight();
			MonsterAttach monsterAttach = getInstance().getMonsterAttach(monsterDomain);
			
			Fightable attributes = new Fightable();
			attributes.add(AttributeKeys.HP_MAX, monsterFight.getHp());
			attributes.add(AttributeKeys.MP_MAX, monsterFight.getMp());
			attributes.add(AttributeKeys.HP, monsterFight.getHp());
			attributes.add(AttributeKeys.MP, monsterFight.getMp());
			attributes.add(AttributeKeys.HIT, monsterFight.getHit());
			attributes.add(AttributeKeys.LEVEL, monsterFight.getLevel());
			attributes.add(AttributeKeys.DODGE, monsterFight.getDodge());
			attributes.add(AttributeKeys.BLOCK, monsterFight.getBlock());
			attributes.add(AttributeKeys.PIERCE, monsterFight.getPierce());
			attributes.add(AttributeKeys.RAPIDLY, monsterFight.getRapidly());
			attributes.add(AttributeKeys.STRENGTH, monsterFight.getStrength());
			attributes.add(AttributeKeys.DEXERITY, monsterFight.getDexerity());
			attributes.add(AttributeKeys.DUCTILITY, monsterFight.getDuctility());
			attributes.add(AttributeKeys.INTELLECT, monsterFight.getIntellect());
			attributes.add(AttributeKeys.MOVE_SPEED, monsterFight.getMove_speed());
			attributes.add(AttributeKeys.CONSTITUTION, monsterFight.getConstitution());
			attributes.add(AttributeKeys.SPIRITUALITY, monsterFight.getSpirituality());
			attributes.add(AttributeKeys.THEURGY_ATTACK, monsterFight.getTheurgy_attack());
			attributes.add(AttributeKeys.THEURGY_DEFENSE, monsterFight.getTheurgy_defense());
			attributes.add(AttributeKeys.THEURGY_CRITICAL, monsterFight.getTheurgy_critical());
			attributes.add(AttributeKeys.PHYSICAL_ATTACK, monsterFight.getPhysical_attack());
			attributes.add(AttributeKeys.PHYSICAL_DEFENSE, monsterFight.getPhysical_defense());
			attributes.add(AttributeKeys.PHYSICAL_CRITICAL, monsterFight.getPhysical_critical());
			attributes.add(AttributeKeys.IMMOBILIZE_DEFENSE, monsterFight.getImmobilizeDefense());
			
			attributes.addAll(monsterAttach.getBeforeBufferable());										// 计算用户BUFF信息和返回延迟附加属性
			PlayerRule.processAfterBufferAttach(monsterAttach.getAfterBufferable(), attributes);		// 所有属性计算完, 再计算角色的BUFF信息
			
			battle.updateFightableAttributes(attributes);
			battle.setHpMax(attributes.getAttribute(AttributeKeys.HP_MAX));
			battle.setMpMax(attributes.getAttribute(AttributeKeys.MP_MAX));
			battle.setHp(isFullHpMp ? attributes.getAttribute(AttributeKeys.HP_MAX) : currentHp);
			battle.setMp(isFullHpMp ? attributes.getAttribute(AttributeKeys.MP_MAX) : currentMp);
		} finally {
			lock.unlock();
		}
		return battle;
	}
	
	/**
	 * 查询怪物的附加属性
	 * 
	 * @param  monsterDomain			怪物的域模型
	 * @return {@link MonsterAttach}	怪物的附加属性信息
	 */
	public MonsterAttach getMonsterAttach(MonsterDomain monsterDomain) {
		MonsterAttach monsterAttach = new MonsterAttach();
		MonsterBuffer monsterBuffer = monsterDomain.getMonsterBuffer(false);
		getInstance().getUserBufferAttach(monsterBuffer, monsterAttach);
		return monsterAttach;
	}
	
	/**
	 * 获得用户BUFF附加属性
	 * 
	 * @param  monsterBuffer	怪物的Buffer对象
	 * @param  monsterAttach	怪物附加属性对象
	 */
	private void getUserBufferAttach(MonsterBuffer monsterBuffer, MonsterAttach monsterAttach) {
		for (Buffer buffer : monsterBuffer.getAndCopyBufferInfo()) {
			if(buffer == null || buffer.isTimeOut()) {
				continue;
			}
			
			int effectId = buffer.getId();
			SkillEffectConfig skillEffect = skillService.getSkillEffectConfig(effectId);
			if(skillEffect == null) {
				continue;
			}
			
			/* 增加力量, 增加敏捷, 增加体质, 增加精神, 增加智力. 策划会在BUFF上避免这几个属性值的BUFF */
			int damageValue = buffer.getDamage();
			int effectType = skillEffect.getEffectType();
			SkillEffectType skillEffectType = SkillEffectType.getSkillEffectType(effectType);
			if(skillEffectType != null) {
				if(skillEffectType.getAttribute() >= 0) {
					monsterAttach.getBeforeBufferable().add(skillEffectType.getAttribute(), damageValue);
				} else {
					monsterAttach.getAfterBufferable().add(effectType, damageValue);
				}
			}
		}
	}

	/**
	 * 怪物攻击玩家. 根据技能ID攻击, 如果是AOE的话, 会根据x, y来遍历玩家
	 * 
	 * @param  monsterId							怪物ID
	 * @param  targetId								被攻击者的ID
	 * @param  unitType								战斗单位类型
	 * @param  xPoint								释放技能的X坐标点
	 * @param  yPoint								释放技能的Y坐标点
	 * @param  skillId								使用的技能ID
	 * @return {@link Integer}						战斗模块返回值
	 */
	public static int monsterFight(long monsterId, long targetId, ElementType unitType, int xPoint, int yPoint, int skillId){
		return getInstance().fightFacade.monsterFight(monsterId, targetId, unitType, xPoint, yPoint, skillId);
	}
	
	/**
	 * 查询角色CD时间
	 * 
	 * @param  playerId					角色ID
	 * @return {@link UserCoolTime}		用户CD时间对象
	 */
	public static UserCoolTime getUserCoolTime(long playerId){
		MonsterHelper helper = getInstance();
		return helper.coolTimeManager.getUserCoolTime(playerId);
	}
	
	/**
	 * 查询基础技能对象
	 * 
	 * @param  skillId						基础技能ID
	 * @return {@link SkillConfig}			基础技能对象
	 */
	public static SkillConfig getSkillConfig(int skillId){
		return getInstance().skillService.getSkillConfig(skillId);
	}
	
	/**
	 * 查询基础冷却时间对象
	 * 
	 * @param  coolTimeId				冷却时间ID
	 * @return {@link CoolTimeConfig}	冷却时间对象
	 */
	public static CoolTimeConfig getCoolTimeConfig(int coolTimeId){
		MonsterHelper helper = getInstance();
		return helper.coolTimeManager.getCoolTimeConfig(coolTimeId);
	}

	/**
	 * 刷新怪物
	 * @param monsterDomain
	 * @param collection
	 */
	public static void refreshMonster(MonsterDomain monsterDomain, Collection<ISpire> collection) {
		MonsterHelper helper = getInstance();
		for(ISpire spire : collection){
			if(spire instanceof UserDomain){
				UserDomain userDomain = (UserDomain)spire ;
				userDomain.putCanViewSpire(monsterDomain);
				helper.worldPusherHelper.putMessage2Queue(spire);
			}
		}
	}
	
	/**
	 * 推送世界BOSS复活公告
	 * @param monsterFightConfig
	 * @param monsterConfig
	 */
	public static void pushBossResurrection(MonsterDomain monsterDomain){
		if(!monsterDomain.getMonsterBattle().isDead()){
			return ;
		}
		MonsterFightConfig monsterFightConfig = monsterDomain.getMonsterFightConfig();
		if(monsterFightConfig != null && monsterFightConfig.isBoss() && monsterFightConfig.getReviveTime() != -1){
			BulletinConfig bConfig = NoticePushHelper.getConfig(NoticeID.BOSS_REFRESH, BulletinConfig.class);
			if (bConfig != null) {
				IMonsterConfig monsterConfig = monsterDomain.getMonsterConfig();
				HashMap<String, Object> paramsMap = new HashMap<String, Object>(2);
				paramsMap.put(NoticeRule.monsterBaseId, monsterFightConfig.getName());
				paramsMap.put(NoticeRule.x, monsterConfig.getBornX());
				paramsMap.put(NoticeRule.y, monsterConfig.getBornY());
				BigMapConfig config = NoticePushHelper.getConfig(monsterDomain.getMapId(), BigMapConfig.class);
				if(config != null){
					paramsMap.put(NoticeRule.map, config.getName() );
				}
				NoticePushHelper.pushNotice(NoticeID.BOSS_REFRESH, NoticeType.HONOR, paramsMap, bConfig.getPriority());
			}
		}
	}
	
	public static void changeMap(ISpire targetSpire, GameMap gameMap, int x, int y){
		MonsterHelper helper = getInstance();
		helper.mapFacade.changeMap(targetSpire, gameMap, x, y);
	}

	/**
	 * 移除怪物
	 * @param id
	 * @param canWatchPlayerIds
	 */
	public static void removeMoster(ISpire monster, Collection<ISpire> canWatchPlayers) {
		MonsterHelper helper = getInstance();
		for(ISpire player : canWatchPlayers){
			if(player instanceof UserDomain){
				UserDomain userDomain = (UserDomain)player ;
				userDomain.putHideSpire(monster);
//				System.err.println(String.format("玩家[%s]接收到清除怪物[%d]尸体", userDomain.getPlayer().getName(), monster.getId() ));
				helper.worldPusherHelper.putMessage2Queue(player);
			}
		}
	}
	
	/**
	 * 推送怪物path
	 * @param monster
	 * @param canWatchPlayers
	 */
	public static void pushMonsterPath(MonsterDomain monsterDomain, Collection<Long> canWathcPlayerIds){
		MonsterHelper helper = getInstance();
		Map<String, Object> monsterPathVo = helper.voFactory.getMonsterPathVo(monsterDomain, new Object[]{monsterDomain.getX(), monsterDomain.getY() });
		
		Map<String,Object> result = new HashMap<String, Object>(1);
		Response actionResponse = Response.defaultResponse(Module.MAP, MapCmd.PUT_MAP_IPSIRE, result);
		result.put(WorldPusherHelper.motionSpire, new Object[]{monsterPathVo});
		helper.pusher.pushMessage(canWathcPlayerIds, actionResponse);
	}
}
