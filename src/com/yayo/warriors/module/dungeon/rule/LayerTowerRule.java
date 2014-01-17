package com.yayo.warriors.module.dungeon.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.yayo.warriors.module.dungeon.model.Dungeon;
import com.yayo.warriors.module.dungeon.types.DungeonState;
import com.yayo.warriors.module.dungeon.types.DungeonType;

/**
 * 千层塔玩法
 * @author liuyuhua
 */
@Component
public class LayerTowerRule extends BaseDungeonRule {
	
	private final Logger logger = LoggerFactory.getLogger(getClass()); 
	
	@Override
	public int getDungeonType() {
		return DungeonType.LAYER_TOWER;
	}

	@Override
	public void action(Dungeon dungeon) {
		if(dungeon == null){
			logger.error("副本对象为空");
			return;
		}
		
		if(this.validation(dungeon)){
			return;
		}
		
		if(dungeon.getState() == DungeonState.SUCCESS){
			return;
		}
		
		/** 当怪物全部被杀完,即副本已经完成*/
		this.doSuccess(dungeon);

	}

}
