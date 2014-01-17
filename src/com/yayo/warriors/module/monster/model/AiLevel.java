package com.yayo.warriors.module.monster.model;

import com.yayo.common.utility.EnumUtils;


/**
 * ai等级定义
 */
public enum AiLevel {
	/** 1.	不动, 不主动打人, 不反击 */
	LEVEL1(1, new BasicAiNode()),
	
	/** 2.	不动, 不主动打人, 反击(被打了才攻击) */
	LEVEL2(2, new BasicAiNode(), new AStartAiNode(), new AttackAiNode()),
	
	/** 3.	不动, 主动打人, 反击 */
	LEVEL3(3, new BasicAiNode(), new WarnAiNode(), new AStartAiNode(), new AttackAiNode()),
	
	/** 4.	乱走, 不主动打人, 反击 */
	LEVEL4(4, new BasicAiNode(), new WalkAiNode(), new AStartAiNode(), new AttackAiNode()),
	
	/** 5.	乱走, 主动打人, 反击 */
	LEVEL5(5, new BasicAiNode(), new WarnAiNode(), new WalkAiNode(), new AStartAiNode(), new AttackAiNode()),
	;
	
	private int level ;
	private AbstractAiNode[] iAiNodes;
	
	
	AiLevel(int level ,AbstractAiNode... aiNodes){
		this.level = level ;
		this.iAiNodes = aiNodes ;
	}

	public static AiLevel getByLevel(int ailevel) {
		return EnumUtils.getEnum(AiLevel.class, ailevel - 1);
	}

	public AbstractAiNode[] aiNodes() {
		return iAiNodes;
	}
	
	public int getLevel(){
		return this.level ;
	}

	public int nodesMaxIndex() {
		return this.aiNodes().length - 1;
	}

}
