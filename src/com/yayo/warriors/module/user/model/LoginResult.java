package com.yayo.warriors.module.user.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.yayo.warriors.module.user.vo.LoginVO;

/**
 * 登录返回值
 * 
 * @author Hyint
 */
public class LoginResult {
	
	/** 在线角色列表. */
	private Set<Long> onlinePlayers = new HashSet<Long>();
	
	/** 登录VO对象 */
	private List<LoginVO> loginVoList = new ArrayList<LoginVO>();
	
	public Set<Long> getOnlinePlayers() {
		return onlinePlayers;
	}
	
	public void addOnlinePlayers(long playerId) {
		onlinePlayers.add(playerId);
	}
	
	public List<LoginVO> getLoginVoList() {
		return this.loginVoList;
	}
	
	public void addLoginVo2List(LoginVO loginVo) {
		if(loginVo != null && !loginVoList.contains(loginVo)) {
			loginVoList.add(loginVo);
		}
	}
	
	public static LoginResult valueOf() {
		return new LoginResult();
	}
}
