package com.yayo.warriors.module.monster.model;

import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.module.monster.domain.MonsterDomain;

/**
 * 怪物ai
 */
public class MonsterAiLevel {

	/** ai动作 */
	private AbstractAiNode monsterAiNode;
	
	/** ai等级 */
	private AiLevel aiLevel ;
	
	private static final MonsterAiLevel[] monsterAiLevels = new MonsterAiLevel[ AiLevel.values().length ];
	
	private MonsterAiLevel(){}
	
	public static MonsterAiLevel getAiByLevel(MonsterFightConfig monsterFightConfig) {
		if(monsterFightConfig == null){
			return null;
		}
		int ailevel = monsterFightConfig.getAilevel();
		MonsterAiLevel monsterAiLevel = monsterAiLevels[ailevel - 1];
		if(monsterAiLevel == null){
			monsterAiLevel = new MonsterAiLevel();
			monsterAiLevel.createAiNode(monsterFightConfig);
			monsterAiLevels[ailevel - 1] = monsterAiLevel;
		}
		return monsterAiLevel;
	}

	/**
	 * 创建ai节点
	 * @param monsterFightConfig
	 */
	private void createAiNode(MonsterFightConfig monsterFightConfig) {
		aiLevel = AiLevel.getByLevel(monsterFightConfig.getAilevel());
		monsterAiNode = new AbstractAiNode() {
			
			@Override
			public boolean meetConditions(MonsterDomain monsterModel) {
				return true;
			}
			
			@Override
			public void executeAi(MonsterDomain monsterEntity) {
			}
		};
		
		monsterAiNode.setNodes(aiLevel.aiNodes(), 0, aiLevel.nodesMaxIndex());
	}

	/**
	 * 执行ai动作
	 * @param monsterEntity
	 */
	public void execute(MonsterDomain monsterEntity) {
		monsterAiNode.executeNode(monsterEntity);
	}

}
