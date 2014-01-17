package com.yayo.warriors.module.user.vo;

import java.io.Serializable;

import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 登录VO对象
 * 
 * @author Hyint
 */
public class LoginVO implements Serializable {
	private static final long serialVersionUID = -2332484760406083463L;

	/** 角色的Id*/
	private Long id;

	/** 角色的职业 */
	private int job;
	 
	/** 角色的性别*/
	private int sex;
	
	/** 角色的头像*/
	private int icon;
	
	/** 角色的名字*/
	private String name;
	
	// Getter and Setter

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public int getJob() {
		return job;
	}

	public void setJob(int job) {
		this.job = job;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static LoginVO valueOf(UserDomain userDomain) {
		if(userDomain == null) {
			return null;
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		LoginVO loginVO = new LoginVO(); 
		loginVO.setId(player.getId());
		loginVO.setName(player.getName());
		loginVO.setIcon(player.getIcon());
		loginVO.setSex(player.getSex().ordinal());
		loginVO.setJob(battle.getJob().ordinal());
		return loginVO;
	}
	
	@Override
	public String toString() {
		return "LoginVO [id=" + id + ", job=" + job + ", sex=" + sex 
				+ ", icon="	+ icon + ", name=" + name + "]";
	}
	
	
}
