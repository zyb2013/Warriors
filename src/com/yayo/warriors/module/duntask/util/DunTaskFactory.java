package com.yayo.warriors.module.duntask.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.yayo.warriors.module.duntask.model.DunTask;
import com.yayo.warriors.module.duntask.model.DunTaskEvent;
import com.yayo.warriors.module.duntask.vo.DunTaskEventVo;
import com.yayo.warriors.module.duntask.vo.DunTaskVo;

/**
 * 副本任务构造工厂 
 * @author liuyuhua
 */
public class DunTaskFactory {
	
	/**
	 * 构建DunTaskVo对象
	 * @param duntask   副本任务对象
	 * @return {@link DunTaskVo}
	 */
	public static DunTaskVo buildDunTaskVo(DunTask duntask){
		DunTaskVo taskVo = new DunTaskVo();
		if(duntask == null){
			return taskVo;
		}
		
		taskVo.setId(duntask.getId());
		taskVo.setBaseId(duntask.getBaseId());
		taskVo.setState(duntask.getState());
		Collection<DunTaskEvent> events = duntask.getEvents();
		if(events != null && !events.isEmpty()){
			Collection<DunTaskEventVo> eventVos = new ArrayList<DunTaskEventVo>();
			for(DunTaskEvent dunTaskEvent : events){
				if(dunTaskEvent != null){
					DunTaskEventVo vo = new DunTaskEventVo();
					vo.setId(dunTaskEvent.getId());
					vo.setType(dunTaskEvent.getType());
					vo.setCompleteCount(dunTaskEvent.getCompleteCount());
					vo.setCurrentCount(dunTaskEvent.getCurrentCount());
					vo.setCondition(dunTaskEvent.getCondition());
					eventVos.add(vo);
				}
			}
			taskVo.setEvents(eventVos.toArray(new DunTaskEventVo[eventVos.size()]));
		}
		return taskVo;
	}
	
	/**
	 * 构建 DunTaskVo对象 集合
	 * @param duntasks   副本任务对象集合
	 * @return {@link Collection<DunTaskVo>}
	 */
	public static List<DunTaskVo> buildDunTask4Collection(Collection<DunTask> duntasks) {
		List<DunTaskVo> duntaskVos = new ArrayList<DunTaskVo>();
		if(duntasks == null || duntasks.isEmpty()){
			return duntaskVos;
		}
		for(DunTask duntask : duntasks){
			DunTaskVo vo = buildDunTaskVo(duntask);
			duntaskVos.add(vo);
		}
		return duntaskVos;
	}
	
	/**
	 * 构建 DunTaskVo 对象 数组
	 * @param duntasks 副本任务对象集合
	 * @return {@link DunTaskVo[]}
	 */
	public static  List<DunTaskVo> buildDunTask4Array(Collection<DunTask> duntasks){
		List<DunTaskVo> duntaskVos = buildDunTask4Collection(duntasks);
		if(duntaskVos == null || duntaskVos.isEmpty()){
			return new ArrayList<DunTaskVo>(0);
		}
		return duntaskVos;
	}
	
}
