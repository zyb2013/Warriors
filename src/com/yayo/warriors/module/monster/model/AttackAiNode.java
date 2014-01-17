package com.yayo.warriors.module.monster.model;



import com.yayo.warriors.basedb.model.CoolTimeConfig;
import com.yayo.warriors.basedb.model.SkillConfig;
import com.yayo.warriors.common.helper.MonsterHelper;
import com.yayo.warriors.module.map.domain.GameScreen;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.map.util.MapUtils;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.skill.type.CastTarget;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.type.ElementType;

/**
 * 攻击AI
 * （如果没有攻击目标会主动寻找）
 * @author haiming
 *
 */
public class AttackAiNode extends AbstractAiNode{

	@Override
	public boolean meetConditions(MonsterDomain monsterModel) {
		if(monsterModel == null){
			return EXECUTE_CURRENT_AI;
		}
		if(!monsterModel.hasAttackTarget()){
			monsterModel.redictAttackPlayer();
		}
		if(monsterModel.hasAttackTarget()){
//			ISpire target = monsterModel.getCanAttackPlayer();
//			int attactdistance = monsterModel.getCurrentUseSkillAttackDistance() ;
//			if(MapUtils.checkPosScopeInfloatNoScale(target, monsterModel, attactdistance)){
//				return EXECUTE_CURRENT_AI ;
//			}
			return EXECUTE_CURRENT_AI;
		} else {
			monsterModel.goHome();
		}
		return EXECUTE_NEXT_AI ;
	}

	@Override
	public void executeAi(MonsterDomain monsterModel) {
		if(monsterModel == null){
			return ;
		}
		SkillMonitor skillMonitor = monsterModel.getCurrentUseSkillMonitor();
		if(skillMonitor == null){
			return ;
		}
		
		int skillId = skillMonitor.getSkillId();
		SkillConfig skillConfig = MonsterHelper.getSkillConfig(skillId);
		if(skillConfig == null) {
			return;
		}
		
		int attactdistance = monsterModel.getCurrentUseSkillAttackDistance();
		if(attactdistance < 0){
			return;
		}
		
		int castXPoint = 0;									//释放技能的X坐标
		int castYPoint = 0;									//释放技能的Y坐标
		int castTarget = skillConfig.getCastTarget();		//释放目标
		int currentCoolTimeId = skillConfig.getCdId();		//技能CDID
		ISpire target = monsterModel.getCanAttackPlayer();
		boolean castTargetSkill = castTarget == CastTarget.TARGET.ordinal();
		if (target == null) {
			return;
		}
		
		if(monsterModel.getGameMap() != target.getGameMap() ){
			monsterModel.removeFromMonsterView(target);
			monsterModel.removeAttackTarget();
			return ;
		}
		
		GameScreen currentScreen = target.getCurrentScreen();
		if( currentScreen != null && !currentScreen.checkInThisGameScreen(target) ){
			currentScreen.leaveScreen(target);
			monsterModel.removeFromMonsterView(target);
			monsterModel.removeAttackTarget();
			return ;
		}

		// 玩家死了 移除仇恨
		boolean targetDead = false;
		if (target instanceof UserDomain) {
			if (((UserDomain) target).getBattle().isDead()) {//怪物不能攻击死亡
				targetDead = true;
			}
		}else if (target instanceof PetDomain) {
			if (((PetDomain) target).getBattle().isDeath()) {
				targetDead = true;
			}
		}else if(target instanceof MonsterDomain){
			if (((MonsterDomain) target).getMonsterBattle().isDead()) {
				targetDead = true;
			}
		}
		if(targetDead){
			monsterModel.removeAttackTarget();
			monsterModel.findTargetPlayer();	//再寻找目标
			if(!monsterModel.hasAttackTarget()){
				monsterModel.goHome();
			}
			return;
		}
		
//		logger.error("怪物[{}]攻击单位[{}]", monsterModel.getMonsterFightConfig().getName(), target.getUnitId());
		
		if(castTargetSkill) {
			if(MapUtils.checkPosScopeInfloat(target, monsterModel, Math.max(attactdistance, 1) ) ){
//				System.err.println(String.format("时间[%d], 怪物[%d,%d]攻击目标[%d, %d], 距离:%d", System.nanoTime(), monsterModel.getX(), monsterModel.getY(), target.getX(), target.getY(), MapUtils.calcDistance(monsterModel.getX(), monsterModel.getY(), target.getX(), target.getY() ) ) );
				MonsterHelper.monsterFight(monsterModel.getId(), target.getId(), target.getType(), castXPoint, castYPoint, skillId);
				
			}
		} else { // 释放AOE技能
			castXPoint = monsterModel.getX();
			castYPoint = monsterModel.getY();
			MonsterHelper.monsterFight(monsterModel.getId(), -1, ElementType.MONSTER, castXPoint, castYPoint, skillId);
		}

		/** 怪物攻击   添加冷却时间*/
		CoolTimeConfig currentCD = MonsterHelper.getCoolTimeConfig(currentCoolTimeId);
		int coolTime = 1000;
		if(currentCD == null){
			logger.error("cd[{}]基础数据不存在", currentCoolTimeId);
		} else {
			coolTime = currentCD.getCoolTime();
		}
		monsterModel.attacked(skillMonitor, coolTime );
	}
}
