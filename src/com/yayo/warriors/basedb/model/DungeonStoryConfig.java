package com.yayo.warriors.basedb.model;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.dungeon.model.StoryVerify;

/**
 * 剧情副本配置
 * @author liuyuhua
 */
@Resource
public class DungeonStoryConfig {
	
	/** 剧情副本ID
	 * <per>{@link DungeonConfig#getId()}</per>
	 * */
	@Id
	private int id;
	
	/** 验证条件(开启条件) {类型_等级_数量|类型_等级_数量}*/
	private String verify;
	
	@JsonIgnore
	private List<StoryVerify> sotryVerifies = null;
	
	/**
	 * 获取需要验证的条件
	 * @return {@link List} 获取验证结果集
	 */
	public List<StoryVerify> getStoryVerifies(){
	    if(sotryVerifies != null){
	    	return this.sotryVerifies;
	    }
	    
	    synchronized (this) {
		    if(sotryVerifies != null){
		    	return this.sotryVerifies;
		    }
		    
		    sotryVerifies = new ArrayList<StoryVerify>();
			List<String[]> verifys = Tools.delimiterString2Array(verify);
		    if(verifys == null || verifys.isEmpty()){
		    	return sotryVerifies;
		    }
			
		    for(String[] verify : verifys){
		    	if(verify.length < 3){
		    		continue;
		    	}
		    	
		    	int type   = Integer.parseInt(verify[0]);
		    	int param1 = Integer.parseInt(verify[1]);
		    	int param2 = Integer.parseInt(verify[2]);
		    	sotryVerifies.add(StoryVerify.valueOf(type, param1, param2));
		    }
		    
		    return sotryVerifies;
		}
		
	}
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getVerify() {
		return verify;
	}

	public void setVerify(String verify) {
		this.verify = verify;
	}
}
