package com.yayo.warriors.module.mail.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.gift.model.GiftRewardInfo;
import com.yayo.warriors.module.mail.model.MailType;

@Entity
@Table(name="mail")
public class Mail extends BaseModel<Long> {

	private static final long serialVersionUID = -6789998409922803253L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private Long senderId;
	
	private String title = "";
	
	@Lob
	private String content = "";
	
	@Column(name="conditions")
	private String condition = "";
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date sendTime = new Date();
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date endTime = new Date();
	
	private String propsRewards = "";
	
	private String equipRewards = "";
	
	private long silverRewards;
	
	private long goldenRewards;
	
	private long couponRewards;
	
	private int mailType = MailType.PLAYER;
	
	@Transient
	private transient List<GiftRewardInfo> rewardInfo = null;
	
	@Transient
	private transient Map<String, String[]> conditionMap = null; 
	
	public static Mail valueOf() {
		Mail mail = new Mail();
		return mail;
	}
	
	private static boolean checkRewardForm(String info, int length) {
		if (info == null) {
			return true;
		} else if (info.trim().length() == 0) {
			return false;
		}
		
		List<String[]> array = Tools.delimiterString2Array(info);
		if(array == null || array.isEmpty()) {
			return true;
		}
		
		for (String[] element : array) {
			if(element == null || element.length < length) {
				return true;
			} else if(!element[0].matches("\\d+") || !element[1].matches("\\d+")) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Long getSenderId() {
		return senderId;
	}

	public void setSenderId(Long senderId) {
		this.senderId = senderId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}
	
	public String getPropsRewards() {
		return propsRewards;
	}

	public void setPropsRewards(String propsRewards) {
		this.propsRewards = propsRewards;
	}

	public String getEquipRewards() {
		return equipRewards;
	}

	public void setEquipRewards(String equipRewards) {
		this.equipRewards = equipRewards;
	}
	
	public List<GiftRewardInfo> getRewardInfo() {
		return rewardInfo;
	}

	public void setRewardInfo(List<GiftRewardInfo> rewardInfo) {
		this.rewardInfo = rewardInfo;
	}
	
	public long getSilverRewards() {
		return silverRewards;
	}

	public void setSilverRewards(long silverRewards) {
		this.silverRewards = silverRewards;
	}

	public long getGoldenRewards() {
		return goldenRewards;
	}

	public void setGoldenRewards(long goldenRewards) {
		this.goldenRewards = goldenRewards;
	}
	
	public int getMailType() {
		return mailType;
	}

	public void setMailType(int mailType) {
		this.mailType = mailType;
	}
	
	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	public Date getEndTime() {
		return endTime;
	}

	public boolean isTimeOut() {
		return this.getEndTime().before(new Date());
	}
	
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
	public long getCouponRewards() {
		return couponRewards;
	}

	public void setCouponRewards(long couponRewards) {
		this.couponRewards = couponRewards;
	}
	
	public List<GiftRewardInfo> getGiftRewardInfos() {
		if (this.rewardInfo != null) {
			return this.rewardInfo;
		}
		
		List<GiftRewardInfo> infos = new ArrayList<GiftRewardInfo>();
		synchronized (this) {
			if (this.rewardInfo == null) {
				Map<Integer, Integer> mergeProps = new HashMap<Integer, Integer>();       // 需要合并的道具
				List<String[]> propsArrays = Tools.delimiterString2Array(propsRewards);
				List<String[]> equipArrays = Tools.delimiterString2Array(equipRewards);
				
				if (propsArrays != null) {
					for (String[] array : propsArrays) {
						int baseId = Integer.valueOf(array[0]);
						int count = Integer.valueOf(array[1]);
						int cacheNum = mergeProps.containsKey(baseId) ? mergeProps.get(baseId) : 0;
						mergeProps.put(baseId, count + cacheNum);
					}
					infos = GiftRewardInfo.propsRewardList(mergeProps);                   // 获得道具奖励列表
				}
				
				if (equipArrays != null) {
					for (String[] array : equipArrays) {
						int baseId = Integer.valueOf(array[0]);
						int count = Integer.valueOf(array[1]);
						infos.add(GiftRewardInfo.equipReward(baseId, count));
					}
				}
			}
		}
		
		this.rewardInfo = infos;
		return infos;
	}
	
	public Map<String, String[]> getConditions() {
		if (this.conditionMap != null) {
			return this.conditionMap;
		}
		
		this.conditionMap = Tools.delimiterString2Map(this.condition);
		return this.conditionMap;
	}
	
	/** 是否有附件 */
	public boolean haveReward() {
		return goldenRewards > 0 || silverRewards > 0 || !getGiftRewardInfos().isEmpty();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Mail))
			return false;
		Mail other = (Mail) obj;
		return id != null && other.id != null && id.equals(other.id);
	}

	@Override
	public String toString() {
		return "Mail [id=" + id + ", senderId=" + senderId + ", title=" + title
				+ ", content=" + content + ", condition=" + condition
				+ ", sendTime=" + sendTime + ", endTime=" + endTime
				+ ", propsRewards=" + propsRewards + ", equipRewards="
				+ equipRewards + ", silverRewards=" + silverRewards
				+ ", goldenRewards=" + goldenRewards + ", mailType=" + mailType
				+ "]";
	}

}
