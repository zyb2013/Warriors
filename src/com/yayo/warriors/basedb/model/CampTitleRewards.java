package com.yayo.warriors.basedb.model;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.InitializeBean;
import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.NumberUtil;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.module.campbattle.type.CampTitle;
import com.yayo.warriors.type.IndexName;

/**
 * 阵营官衔奖励（俸禄）
 * @author jonsai
 *
 */
@Resource
public class CampTitleRewards implements InitializeBean{
	
	/** id */
	@Id
	private int id;
	
	/** 阵营 */
	@Index(name = IndexName.CAMP_TITLE_REWARD, order = 0)
	private int camp;
	
	/** 阵营官衔 {@link CampTitle}  */
	@Index(name = IndexName.CAMP_TITLE_REWARD, order = 1)
	private int campTitle;
	
	/** 经验 */
	private String exp;
	
	/** 铜币 */
	private String silver;
	
	/** 道具 */
	private String props;
	
	/** 时装 */
	private String suit;
	
	@JsonIgnore
	private Map<Integer, Integer> propsRewards = null;

	@JsonIgnore
	private Integer[] suitRewards = null;
	
	
	public void afterPropertiesSet() {
		if( StringUtils.isNotBlank(this.props) ){
			this.propsRewards = NumberUtil.delimiterString2Map2(this.props, Integer.class, Integer.class);
		}
		
		if(StringUtils.isNotBlank(this.suit) ){
			suitRewards = NumberUtil.convertArray(this.suit, Splitable.ELEMENT_SPLIT, Integer.class);
		}
	}

	public Map<Integer, Integer> getPropsRewards() {
		return propsRewards;
	}



	public Integer[] getSuitRewards() {
		return suitRewards;
	}



	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCamp() {
		return camp;
	}

	public void setCamp(int camp) {
		this.camp = camp;
	}

	public int getCampTitle() {
		return campTitle;
	}

	public void setCampTitle(int campTitle) {
		this.campTitle = campTitle;
	}

	public String getExp() {
		return exp;
	}

	public void setExp(String exp) {
		this.exp = exp;
	}

	public String getSilver() {
		return silver;
	}

	public void setSilver(String silver) {
		this.silver = silver;
	}

	public String getProps() {
		return props;
	}

	public void setProps(String props) {
		this.props = props;
	}

	public String getSuit() {
		return suit;
	}

	public void setSuit(String suit) {
		this.suit = suit;
	}

	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CampTitleRewards other = (CampTitleRewards) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
