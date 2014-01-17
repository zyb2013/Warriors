package com.yayo.warriors.module.props.dao;

import java.util.List;

public interface UserEquipDao {

	List<Long> getUserEquipIdList(long playerId, int backpack);

}
