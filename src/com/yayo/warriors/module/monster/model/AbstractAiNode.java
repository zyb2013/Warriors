package com.yayo.warriors.module.monster.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yayo.warriors.module.monster.domain.MonsterDomain;

/**
 *	ai动作节点
 */
public abstract class AbstractAiNode {

	/** 下一个ai动作 */
	protected AbstractAiNode aiNode ;
	/**
	 * 执行当前AI
	 */
	protected static final boolean EXECUTE_CURRENT_AI = false ;
	/**
	 * 执行下一个AI
	 */
	protected static final boolean EXECUTE_NEXT_AI = true  ;
	
	/** 日志 */
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * 取得下一个动作
	 * @return
	 */
	public AbstractAiNode nextNode(){
		return aiNode ;
	}

	/**
	 * 满足当前条件  false执行当前AI  true执行下一个AI
	 * @return
	 */
	public abstract boolean meetConditions(MonsterDomain monsterModel);

	/**
	 * 是否有下一个接口
	 * @return
	 */
	public boolean hasNextNode(){
		return aiNode != null ;
	}

	public void setNodes(AbstractAiNode[] iAiNodes , int index, int maxIndex) {
		this.aiNode = iAiNodes[index] ;
		if(index != maxIndex){
			aiNode.setNodes(iAiNodes ,index+1,maxIndex);
		}
	}

	public void executeNode(MonsterDomain monsterModel) {
		boolean meetCnd = this.meetConditions(monsterModel) ;
		if(meetCnd && this.hasNextNode()){
//			if(monsterModel.hasAttackTarget()){
//			} 
//			else{
				aiNode.executeNode(monsterModel);
//			}
			
		}else if(!meetCnd){
			this.executeAi(monsterModel);
		}else{
			//nothing ai空闲
		}
	}

	/**
	 * 执行ai
	 * @param monsterModel
	 */
	public abstract void executeAi(MonsterDomain monsterModel);
}
