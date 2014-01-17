package com.yayo.warriors.socket.handler.props;

import static com.yayo.common.socket.type.ResponseCode.*;
import static com.yayo.warriors.constant.CommonConstant.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.handler.BaseHandler;
import com.yayo.common.socket.handler.Invoker;
import com.yayo.common.socket.message.Request;
import com.yayo.common.socket.message.Response;
import com.yayo.common.utility.EnumUtils;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.pack.facade.BackpackFacade;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.facade.PropsFacade;
import com.yayo.warriors.module.props.model.AttributeVO;
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.module.props.model.ShenwuResult;
import com.yayo.warriors.module.props.model.SynthStoneResult;
import com.yayo.warriors.module.props.model.UserEquipVO;
import com.yayo.warriors.module.props.model.WashAttributeVO;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.PortableType;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.type.ElementType;
import com.yayo.warriors.type.GoodsType;

import flex.messaging.io.amf.ASObject;

@Component
public class PropsHandler extends BaseHandler {
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private PropsFacade propsFacade;
	@Autowired
	private BackpackFacade packageFacade;

	
	protected int getModule() {
		return Module.PROPS;
	}

	
	protected void inititialize() {
		
		putInvoker(PropsCmd.GET_PACKAGE_INFO, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				getPackageInfo(session, request, response);
			}
		});

		putInvoker(PropsCmd.UPDATE_PACKAGE_INFO, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				updatePackageInfo(session, request, response);
			}
		});
		
		putInvoker(PropsCmd.CHECK_ROLE_PACKINFO, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				checkRolePackInfo(session, request, response);
			}
		});
		
		putInvoker(PropsCmd.GET_USER_PROPS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				getUserProps(session, request, response);
			}
		});
		
		putInvoker(PropsCmd.USE_USERPROPS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				useUserProps(session, request, response);
			}
		});
		
		putInvoker(PropsCmd.DROP_USERPROPS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				dropUserProps(session, request, response);
			}
		});
		
		putInvoker(PropsCmd.SELL_USERPROPS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				sellUserProps(session, request, response);
			}
		});
		
		putInvoker(PropsCmd.MERGE_USERPROPS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				mergeUserProps(session, request, response);
			}
		});
		
		putInvoker(PropsCmd.SPLITE_USERPROPS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				spliteUserProps(session, request, response);
			}
		});
		
		putInvoker(PropsCmd.SYNTH_EXCHANGE_STONE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				synthEnchangeStone(session, request, response);
			}
		});
		
		putInvoker(PropsCmd.SYNTH_SHARIPU_ITEM, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				synthSharipuItem(session, request, response);
			}
		});

		putInvoker(PropsCmd.ASCENT_EQUIP_STARLEVEL, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				ascentEquipStarLevel(session, request, response);
			}
		});

		putInvoker(PropsCmd.ASCENT_EQUIP_RANKLEVEL, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				ascentEquipRankLevel(session, request, response);
			}
		});

		putInvoker(PropsCmd.ENCHANGE_EQUIP_STONE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				enchangeEquipStone(session, request, response);
			}
		});
		
		putInvoker(PropsCmd.REMOVE_EQUIP_STONE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				removeEquipStone(session, request, response);
			}
		});
		
		putInvoker(PropsCmd.RECAST_EQUIP_ATTRIBUTE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				upgradeEquipAttribute(session, request, response);
			}
		});
		
		putInvoker(PropsCmd.POLISHED_EQUIP_ATTRIBUTE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				polishedEquipAttribute(session, request, response);
			}
		});
		
		putInvoker(PropsCmd.SELECT_WASH_EQUIP_ATTRIBUTE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				selectPolishedEquipAttribute(session, request, response);
			}
		});

		putInvoker(PropsCmd.REFINING_EQUIP_ATTRIBUTE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				refiningEquipAttribute(session, request, response);
			}
		});
		
		putInvoker(PropsCmd.EMENDATION_EQUIP_ATTRIBUTE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				emendationEquipAttribute(session, request, response);
			}
		});

		putInvoker(PropsCmd.UPDATE_ENTRY_POSITION, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				updateEntryPosition(session, request, response);
			}
		});
		
		putInvoker(PropsCmd.AUTO_ADJUST_BACKPACK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				autoAdjustBackpack(session, request, response);
			}
		});
		
		putInvoker(PropsCmd.ROLE_DRESS_EQUIP, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				roleDressEquip(session, request, response);
			}
		});

		putInvoker(PropsCmd.QUERY_USER_EQUIP, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				queryUserEquip(session, request, response);
			}
		});
		
		
		putInvoker(PropsCmd.QUERY_USER_EQUIP_ARRAY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				queryUserEquipArray(session, request, response);
			}
		});
		
		putInvoker(PropsCmd.LIST_USER_EQUIP, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				listUserEquip(session, request, response);
			}
		});

		putInvoker(PropsCmd.LIST_USER_PROPS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				listUserProps(session, request, response);
			}
		});

		putInvoker(PropsCmd.PUT_2_STORAGE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				put2Storage(session, request, response);
			}
		});
		
		putInvoker(PropsCmd.CHECKOUT_FROM_STORAGE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				checkoutFromStorage(session, request, response);
			}
		});
		
		putInvoker(PropsCmd.SWAP_GOODS_BACKPACK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				swapGoodsBackpack(session, request, response);
			}
		});

		putInvoker(PropsCmd.ARTIFICE_PROPS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				artificeProps(session, request, response);
			}
		});

		putInvoker(PropsCmd.RESOLVE_USER_EQUIP, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				resolveUserEquip(session, request, response);
			}
		});
		
		putInvoker(PropsCmd.EXPAND_BACKPACK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				expandBackpack(session, request, response);
			}
		});

		putInvoker(PropsCmd.CALC_REPARE_COSTSILVER, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				calcRepareCostSilver(session, request, response);
			}
		});

		putInvoker(PropsCmd.REPARE_USER_EQUIP, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				repareUserEquip(session, request, response);
			}
		});

		putInvoker(PropsCmd.EQUIP_STARLEVEL_EXTENDS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				equipStarLevelExtends(session, request, response);
			}
		});
		
		putInvoker(PropsCmd.CHECKOUT_PROPS_FROM_STORAGE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				checkoutFromLotteryStorage(session, request, response);
			}
		});
		
		putInvoker(PropsCmd.USE_PORTABLE_BAG, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				usePortableBag(session, request, response);
			}
		});
		
		putInvoker(PropsCmd.BATCH_BUY_AND_USE_PROPS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				batchBuyAndUserProps(session, request, response);
			}
		});

		putInvoker(PropsCmd.SHENWU_TEMPO_FORGE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				shenwuTempoForge(session, request, response);
			}
		});

		putInvoker(PropsCmd.SHENWU_ATTRIBUTE_FORGE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				shenwuAttributeForge(session, request, response);
			}
		});
	}


	protected void equipStarLevelExtends(IoSession session, Request request, Response response) {
		long userEquipId = 0L;
		long userPropsId = 0L;
		long targetEquipId = 0L;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.USER_EQUIP_ID)) {
				userEquipId = ((Number) aso.get(ResponseKey.USER_EQUIP_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.TARGET_EQUIP_ID)) {
				targetEquipId = ((Number) aso.get(ResponseKey.TARGET_EQUIP_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.USER_PROPSID)) {
				userPropsId = ((Number) aso.get(ResponseKey.USER_PROPSID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Collection<BackpackEntry>> resultObject = propsFacade.equipExtendStar(playerId, userEquipId, targetEquipId, userPropsId);
		response.setValue(resultObject.getResult());
		session.write(response);
		pushBackEntriesAndPushMoney(playerId, resultObject.getValue(), false);
	}


	protected void shenwuAttributeForge(IoSession session, Request request, Response response) {
		int rank = 0;
		long userEquipId = 0L;
		String userProps = "";
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.USER_PROPS)) {
				userProps = (String) aso.get(ResponseKey.USER_PROPS);
			}
			if(aso.containsKey(ResponseKey.RANK)) {
				rank = ((Number) aso.get(ResponseKey.RANK)).intValue();
			}
			if(aso.containsKey(ResponseKey.USER_EQUIP_ID)) {
				userEquipId = ((Number) aso.get(ResponseKey.USER_EQUIP_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		ShenwuResult shenwuResult = propsFacade.doShenwuAttributeForge(playerId, userEquipId, rank, userProps);
		resultMap.put(ResponseKey.RESULT, shenwuResult.getResult());
		if(shenwuResult.getUserEquip() != null) {
			resultMap.put(ResponseKey.USER_EQUIP_ID, shenwuResult.getUserEquip().getId());
			resultMap.put(ResponseKey.SHENWU_TEMPO, shenwuResult.getUserEquip().getShenwuTempo());
			resultMap.put(ResponseKey.SHENWU_SWITCH, shenwuResult.getUserEquip().getShenwuSwitch());
			resultMap.put(ResponseKey.SHENWU_ATTRIBUTES, shenwuResult.getUserEquip().getShenwuAttributes());
		}
		
		response.setValue(resultMap);
		session.write(response);
		pushBackEntriesAndPushMoney(playerId, shenwuResult.getBackpackEntries(), false);
	}


	protected void shenwuTempoForge(IoSession session, Request request, Response response) {
		int rank = 0;
		long userEquipId = 0L;
		String userProps = "";
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.RANK)) {
				rank = ((Number) aso.get(ResponseKey.RANK)).intValue();
			}
			if(aso.containsKey(ResponseKey.USER_PROPS)) {
				userProps = (String) aso.get(ResponseKey.USER_PROPS);
			}
			if(aso.containsKey(ResponseKey.USER_EQUIP_ID)) {
				userEquipId = ((Number) aso.get(ResponseKey.USER_EQUIP_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
	
		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		ShenwuResult shenwuResult = propsFacade.doEquipShenwuTempo(playerId, userEquipId, rank, userProps);
		resultMap.put(ResponseKey.RESULT, shenwuResult.getResult());
		if(shenwuResult.getUserEquip() != null) {
			resultMap.put(ResponseKey.USER_EQUIP_ID, shenwuResult.getUserEquip().getId());
			resultMap.put(ResponseKey.SHENWU_TEMPO, shenwuResult.getUserEquip().getShenwuTempo());
			resultMap.put(ResponseKey.SHENWU_SWITCH, shenwuResult.getUserEquip().getShenwuSwitch());
			resultMap.put(ResponseKey.SHENWU_ATTRIBUTES, shenwuResult.getUserEquip().getShenwuAttributes());
		}
		
		response.setValue(resultMap);
		session.write(response);
		pushBackEntriesAndPushMoney(playerId, shenwuResult.getBackpackEntries(), false);
	}


	protected void repareUserEquip(IoSession session, Request request, Response response) {
		String backpackInfo = "";
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.INFO)) {
				backpackInfo = (String) aso.get(ResponseKey.INFO);
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		response.setValue(propsFacade.repairUserEquips(playerId, backpackInfo));
		session.write(response);
	}


	protected void calcRepareCostSilver(IoSession session, Request request, Response response) {
		String backpackInfo = "";
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.INFO)) {
				backpackInfo = (String) aso.get(ResponseKey.INFO);
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int costSilver = propsFacade.calcRepairCostSilver(playerId, backpackInfo);
		response.setValue(costSilver);
		session.write(response);
	}

	
	protected void swapGoodsBackpack(IoSession session, Request request, Response response) {
		long goodsId = 0L;				
		long targetId = 0L;				
		int goodsType = 0;				
		int targetType = 0;				
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.GOODS_ID)) {
				goodsId = ((Number) aso.get(ResponseKey.GOODS_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.GOODS_TYPE)) {
				goodsType = ((Number) aso.get(ResponseKey.GOODS_TYPE)).intValue();
			}
			if(aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.TARGET_TYPE)) {
				targetType = ((Number) aso.get(ResponseKey.TARGET_TYPE)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Collection<BackpackEntry>> resultObject = propsFacade.
			swapBackpack(playerId, goodsId, goodsType, targetId, targetType);
		response.setValue(resultObject.getResult());
		session.write(response);
		
		
		this.pushBackEntriesAndPushMoney(playerId, resultObject.getValue(), false);
	}

	
	protected void checkoutFromStorage(IoSession session, Request request, Response response) {
		int index = -1;					
		int amount = 0;					
		long goodsId = 0L;				
		int goodsType = 0;				
		long userPropsId = 0L;			
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.GOODS_ID)) {
				goodsId = ((Number) aso.get(ResponseKey.GOODS_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.GOODS_TYPE)) {
				goodsType = ((Number) aso.get(ResponseKey.GOODS_TYPE)).intValue();
			}
			if(aso.containsKey(ResponseKey.USER_PROPSID)) {
				userPropsId = ((Number) aso.get(ResponseKey.USER_PROPSID)).longValue();
			}
			if(aso.containsKey(ResponseKey.AMOUNT)) {
				amount = ((Number) aso.get(ResponseKey.AMOUNT)).intValue();
			}
			if(aso.containsKey(ResponseKey.INDEX)) {
				index = ((Number) aso.get(ResponseKey.INDEX)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Collection<BackpackEntry>> resultObject = propsFacade.checkoutFromStorage(playerId, 
														goodsId, goodsType, amount, userPropsId, index);
		response.setValue(resultObject.getResult());
		session.write(response);
		
		this.pushBackEntriesAndPushMoney(playerId, resultObject.getValue(), false);
	}
	

	protected void checkoutFromLotteryStorage(IoSession session, Request request, Response response) {
		int index = -1;					
		int amount = 0;					
		long goodsId = 0L;				
		int goodsType = 0;				
		long userPropsId = 0L;			
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.GOODS_ID)) {
				goodsId = ((Number) aso.get(ResponseKey.GOODS_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.GOODS_TYPE)) {
				goodsType = ((Number) aso.get(ResponseKey.GOODS_TYPE)).intValue();
			}
			if(aso.containsKey(ResponseKey.USER_PROPSID)) {
				userPropsId = ((Number) aso.get(ResponseKey.USER_PROPSID)).longValue();
			}
			if(aso.containsKey(ResponseKey.AMOUNT)) {
				amount = ((Number) aso.get(ResponseKey.AMOUNT)).intValue();
			}
			if(aso.containsKey(ResponseKey.INDEX)) {
				index = ((Number) aso.get(ResponseKey.INDEX)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Collection<BackpackEntry>> resultObject = propsFacade.checkoutFromLotteryStorage(playerId, 
														goodsId, goodsType, amount, userPropsId, index);
		response.setValue(resultObject.getResult());
		session.write(response);
		
		this.pushBackEntriesAndPushMoney(playerId, resultObject.getValue(), false);
	}


	protected void put2Storage(IoSession session, Request request, Response response) {
		int index = -1;					
		long goodsId = 0L;				
		int goodsType = 0;				
		long userPropsId = 0L;			
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.GOODS_ID)) {
				goodsId = ((Number) aso.get(ResponseKey.GOODS_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.GOODS_TYPE)) {
				goodsType = ((Number) aso.get(ResponseKey.GOODS_TYPE)).intValue();
			}
			if(aso.containsKey(ResponseKey.USER_PROPSID)) {
				userPropsId = ((Number) aso.get(ResponseKey.USER_PROPSID)).longValue();
			}
			if(aso.containsKey(ResponseKey.INDEX)) {
				index = ((Number) aso.get(ResponseKey.INDEX)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Collection<BackpackEntry>> resultObject = propsFacade
			.put2Storage(playerId, goodsId, goodsType, userPropsId, index);
		response.setValue(resultObject.getResult());
		session.write(response);
		
		this.pushBackEntriesAndPushMoney(playerId, resultObject.getValue(), false); 
	}


	protected void resolveUserEquip(IoSession session, Request request, Response response) {
		String userEquips = "";
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.USER_EQUIPS)) {
				userEquips = (String) aso.get(ResponseKey.USER_EQUIPS);
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Collection<BackpackEntry>> resultObject = propsFacade.resolveUserEquips(playerId, userEquips);
		response.setValue(resultObject.getResult());
		session.write(response);
		pushBackEntriesAndPushMoney(playerId, resultObject.getValue(), false, AttributeKeys.SILVER);
	}

	
	protected void artificeProps(IoSession session, Request request, Response response) {
		int count = 0;
		int propsId = 0;
		String userProps = "";
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.USER_PROPS)) {
				userProps = (String)aso.get(ResponseKey.USER_PROPS);
			}
			if(aso.containsKey(ResponseKey.COUNT)) {
				count = ((Number)aso.get(ResponseKey.COUNT)).intValue();
			}
			if(aso.containsKey(ResponseKey.PROPS_ID)) {
				propsId = ((Number)aso.get(ResponseKey.PROPS_ID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Collection<BackpackEntry>> resultObject = propsFacade.aritificeProps(playerId, propsId, count, userProps);
		response.setValue(resultObject.getResult());
		session.write(response);
		
		pushBackEntriesAndPushMoney(playerId, resultObject.getValue(), false, AttributeKeys.SILVER);
	}


	protected void refiningEquipAttribute(IoSession session, Request request, Response response) {
		String userProps = "";
		long userEquipId = 0L;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.USER_EQUIP_ID)) {
				userEquipId = ((Number) aso.get(ResponseKey.USER_EQUIP_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.USER_PROPS)) {
				userProps = (String) aso.get(ResponseKey.USER_PROPS);
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Collection<BackpackEntry>> resultObject = propsFacade.refineEquipAttribute(playerId, userEquipId, userProps);
		response.setValue(resultObject.getResult());
		session.write(response);
		boolean backEntriesEmptyPush = resultObject.getResult() >= SUCCESS;
		pushBackEntriesAndPushMoney(playerId, resultObject.getValue(), backEntriesEmptyPush);
	}


	protected void listUserEquip(IoSession session, Request request, Response response) {
		int backpack = -1;
		long targetId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.PACKAGE_TYPE)) {
				backpack = ((Number) aso.get(ResponseKey.PACKAGE_TYPE)).intValue();
			}
			if (aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		List<BackpackEntry> backpackEntries = propsFacade.listEquipBackpackEntry(targetId, backpack);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.TARGET_ID, targetId);
		resultMap.put(ResponseKey.ENTRY, backpackEntries.toArray());
		response.setValue(resultMap);
		session.write(response);
	}


	protected void listUserProps(IoSession session, Request request, Response response) {
		int backpack = -1;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.PACKAGE_TYPE)) {
				backpack = ((Number) aso.get(ResponseKey.PACKAGE_TYPE)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		List<BackpackEntry> backpackEntries = propsFacade.listPropsBackpackEntry(playerId, backpack);
		response.setValue(backpackEntries.toArray());
		session.write(response);
	}


	protected void emendationEquipAttribute(IoSession session, Request request, Response response) {
		long userEquipId = 0L;
		int autoBuyCount = 0;
		String userProps = "";
		String luckyProps = "";
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.AUTO_BUY_COUNT)) {
				autoBuyCount = ((Number) aso.get(ResponseKey.AUTO_BUY_COUNT)).intValue();
			}
			if(aso.containsKey(ResponseKey.USER_EQUIP_ID)) {
				userEquipId = ((Number) aso.get(ResponseKey.USER_EQUIP_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.USER_PROPS)) {
				userProps = (String) aso.get(ResponseKey.USER_PROPS);
			}
			if(aso.containsKey(ResponseKey.LUCKY_USER_PROPS)) {
				luckyProps = (String) aso.get(ResponseKey.LUCKY_USER_PROPS);
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Collection<BackpackEntry>> emendationResult = propsFacade.
		emendationEquipAttribute(playerId, userEquipId, userProps, luckyProps,	autoBuyCount);
	
		
		Collection<BackpackEntry> backpackEntries = emendationResult.getValue();
		if(backpackEntries != null && !backpackEntries.isEmpty()) {
			pushBackEntriesAndPushMoney(playerId, backpackEntries, false, AttributeKeys.SILVER, AttributeKeys.GOLDEN);
		}
		
		response.setValue(emendationResult.getResult());
		session.write(response);
		
	}


	protected void polishedEquipAttribute(IoSession session, Request request, Response response) {
		long userEquipId = 0L;
		int autoBuyCount = 0;
		String userProps = "";
		String safeIndex = "";
		String lockProps = "";
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.INDEX)) {
				safeIndex = (String) aso.get(ResponseKey.INDEX);
			}
			if(aso.containsKey(ResponseKey.USER_EQUIP_ID)) {
				userEquipId = ((Number) aso.get(ResponseKey.USER_EQUIP_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.USER_PROPS)) {
				userProps = (String) aso.get(ResponseKey.USER_PROPS);
			}
			if(aso.containsKey(ResponseKey.LOCK_PROPS)) {
				lockProps = (String) aso.get(ResponseKey.LOCK_PROPS);
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Collection<BackpackEntry>> polishedResult = propsFacade.polishedEquipAdditions(playerId, 
											  userEquipId, userProps, autoBuyCount, safeIndex, lockProps);
		
		int result = polishedResult.getResult();

		if(result >= SUCCESS) {
			pushBackEntriesAndPushMoney(playerId, polishedResult.getValue(), true, AttributeKeys.GOLDEN, AttributeKeys.SILVER);
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, result);
		WashAttributeVO washAttributeVO = propsFacade.getWashAttributeVO(playerId);
		if(washAttributeVO != null && washAttributeVO.getUserEquipId() == userEquipId) {
			Map<Integer, AttributeVO> attributes = washAttributeVO.getAttributes();
			resultMap.put(ResponseKey.ATTRIBUTES, attributes.values().toArray());
		}
		
		response.setValue(resultMap);
		session.write(response);
	}


	protected void selectPolishedEquipAttribute(IoSession session, Request request, Response response) {
		boolean select = false;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.TYPE)) {
				select = (Boolean) aso.get(ResponseKey.TYPE);
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
		}
		
		int result = propsFacade.selectPolishedEquipAddition(playerId, select);
		response.setValue(result);
		session.write(response);
	}


	protected void upgradeEquipAttribute(IoSession session, Request request, Response response) {
		long userEquipId = 0L;
		int autoBuyCount = 0;
		String userProps = "";
		String luckyProps = "";
		Set<Integer> selectParams = new HashSet<Integer>();
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.AUTO_BUY_COUNT)) {
				autoBuyCount = ((Number) aso.get(ResponseKey.AUTO_BUY_COUNT)).intValue();
			}
			if(aso.containsKey(ResponseKey.USER_EQUIP_ID)) {
				userEquipId = ((Number) aso.get(ResponseKey.USER_EQUIP_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.USER_PROPS)) {
				userProps = (String) aso.get(ResponseKey.USER_PROPS);
			}
			if(aso.containsKey(ResponseKey.LUCKY_USER_PROPS)) {
				luckyProps = (String) aso.get(ResponseKey.LUCKY_USER_PROPS);
			}
			if(aso.containsKey(ResponseKey.PARAMS)) {
				Object[] params = (Object[]) aso.get(ResponseKey.PARAMS);
				for (Object param : params) {
					selectParams.add((Integer) param);
				}
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Collection<BackpackEntry>> upgradeResult = propsFacade.recastEquipAttribute(playerId, 
											userEquipId, userProps, luckyProps, autoBuyCount, selectParams);

		Collection<BackpackEntry> backpackEntries = upgradeResult.getValue();
		if(backpackEntries != null && !backpackEntries.isEmpty()) { 
			pushBackEntriesAndPushMoney(playerId, backpackEntries, false, AttributeKeys.SILVER, AttributeKeys.GOLDEN);
		}
		
		response.setValue(upgradeResult.getResult());
		session.write(response);
		
	}


	protected void removeEquipStone(IoSession session, Request request, Response response) {
		int index = -1;
		long userEquipId = 0L;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.USER_EQUIP_ID)) {
				userEquipId = ((Number) aso.get(ResponseKey.USER_EQUIP_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.INDEX)) {
				index = ((Number) aso.get(ResponseKey.INDEX)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Collection<BackpackEntry>> removeResult = propsFacade.removeEquipEnchange(playerId, userEquipId, index);
		if(removeResult.getValue() != null && !removeResult.getValue().isEmpty()) {
			pushBackEntriesAndPushMoney(playerId, removeResult.getValue(), false);
		}
		
		response.setValue(removeResult.getResult());
		session.write(response);
	}

	
	protected void enchangeEquipStone(IoSession session, Request request, Response response) {
		long userEquipId = 0L;
		String enchangeInfos = "";
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.INFO)) {
				enchangeInfos = (String) aso.get(ResponseKey.INFO);
			}
			if(aso.containsKey(ResponseKey.USER_EQUIP_ID)) {
				userEquipId = ((Number) aso.get(ResponseKey.USER_EQUIP_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Collection<BackpackEntry>> enchangeResult = propsFacade.enchangeEquip(playerId, userEquipId, enchangeInfos);
		int result = enchangeResult.getResult();
		Collection<BackpackEntry> backpackEntries = enchangeResult.getValue();
		if(backpackEntries != null && !backpackEntries.isEmpty()) {
			pushBackEntriesAndPushMoney(playerId, backpackEntries, false);
		}
		response.setValue(result);
		session.write(response);
	}

	
	protected void ascentEquipRankLevel(IoSession session, Request request, Response response) {
		int autoBuyCount = 0;
		long userEquipId = 0L;
		String userProps = "";
		String safeUserProps1 = "";
		String safeUserProps2 = "";
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.AUTO_BUY_COUNT)) {
				autoBuyCount = ((Number) aso.get(ResponseKey.AUTO_BUY_COUNT)).intValue();
			}
			if(aso.containsKey(ResponseKey.USER_PROPS)) {
				userProps = (String) aso.get(ResponseKey.USER_PROPS);
			}
			if(aso.containsKey(ResponseKey.USER_EQUIP_ID)) {
				userEquipId = ((Number) aso.get(ResponseKey.USER_EQUIP_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.SAFE_USER_PROPS1)) {
				safeUserProps1 = (String) aso.get(ResponseKey.SAFE_USER_PROPS1);
			}
			if(aso.containsKey(ResponseKey.SAFE_USER_PROPS2)) {
				safeUserProps2 = (String) aso.get(ResponseKey.SAFE_USER_PROPS2);
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Collection<BackpackEntry>> resultObject = propsFacade.ascentEquipRank(playerId, 
										userEquipId, userProps, safeUserProps1,	safeUserProps2, autoBuyCount);
		
		int result = resultObject.getResult();
		
		Collection<BackpackEntry> entries = resultObject.getValue();
		pushBackEntriesAndPushMoney(playerId, entries, false, AttributeKeys.SILVER, AttributeKeys.GOLDEN);
		response.setValue(result);
		session.write(response);
		
	}

	
	protected void ascentEquipStarLevel(IoSession session, Request request, Response response) {
		int autoBuyCount = 0;			
		String userItems = "";			
		long userEquipId = 0L;			
		long userPropsId = 0L;			
		String luckyUserItems = "";		
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.AUTO_BUY_COUNT)) {
				autoBuyCount = ((Number) aso.get(ResponseKey.AUTO_BUY_COUNT)).intValue();
			}
			if(aso.containsKey(ResponseKey.USER_PROPS)) {
				userItems = (String) aso.get(ResponseKey.USER_PROPS);
			}
			if(aso.containsKey(ResponseKey.USER_EQUIP_ID)) {
				userEquipId = ((Number) aso.get(ResponseKey.USER_EQUIP_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.LUCKY_USER_PROPS)) {
				luckyUserItems = (String) aso.get(ResponseKey.LUCKY_USER_PROPS);
			}
			if(aso.containsKey(ResponseKey.USER_PROPSID)) {
				userPropsId = ((Number) aso.get(ResponseKey.USER_PROPSID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Collection<BackpackEntry>> resultObject = propsFacade.ascentEquipStar(playerId, userEquipId, userItems, luckyUserItems, userPropsId, autoBuyCount);
		
		Collection<BackpackEntry> backpackEntries = resultObject.getValue();
		pushBackEntriesAndPushMoney(playerId, backpackEntries, false);
		
		response.setValue(resultObject.getResult());
		session.write(response);
		
	}


	protected void synthSharipuItem(IoSession session, Request request, Response response) {
		long userItemId = 0L;
		int autoBuyCount = 0;
		String userItems = "";
		long targetItemId = 0L;
		String luckyUserItems = "";
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.LUCKY_USER_PROPS)) {
				luckyUserItems = (String) aso.get(ResponseKey.LUCKY_USER_PROPS);
			}
			if(aso.containsKey(ResponseKey.USER_PROPS)) {
				userItems = (String) aso.get(ResponseKey.USER_PROPS);
			}
			if(aso.containsKey(ResponseKey.USER_PROPSID)) {
				userItemId = ((Number) aso.get(ResponseKey.USER_PROPSID)).longValue();
			}
			if(aso.containsKey(ResponseKey.TARGET_PROPSID)) {
				targetItemId = ((Number) aso.get(ResponseKey.TARGET_PROPSID)).longValue();
			}
			if(aso.containsKey(ResponseKey.AUTO_BUY_COUNT)) {
				autoBuyCount = ((Number) aso.get(ResponseKey.AUTO_BUY_COUNT)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Collection<UserProps>> resultObject = null;
		if(userItemId == targetItemId) {
			resultObject = propsFacade.synthSharipuSameItem(playerId, userItemId, userItems, luckyUserItems, autoBuyCount);
		} else {
			resultObject = propsFacade.synthSharipuDiverse(playerId, userItemId, targetItemId, userItems, luckyUserItems, autoBuyCount);
		}
		
		int result = resultObject.getResult();
		Collection<UserProps> propsList = resultObject.getValue();
		if(propsList != null && !propsList.isEmpty()) {
			List<BackpackEntry> backpackEntries = voFactory.getUserPropsEntries(propsList);
			pushBackEntriesAndPushMoney(playerId, backpackEntries, false, AttributeKeys.SILVER, AttributeKeys.GOLDEN);
		}
		response.setValue(result);
		session.write(response);
		
	}

	
	private void pushBackEntriesAndPushMoney(long playerId, Collection<BackpackEntry> backpackEntries, boolean entriesEmptyPushAttr, Object...attributes) {
		boolean pushAttributes = false;
		if(backpackEntries != null && !backpackEntries.isEmpty()) {
			pushAttributes = true;
			MessagePushHelper.pushUserProps2Client(playerId, -1, false, backpackEntries);
		}
		
		if((pushAttributes || entriesEmptyPushAttr) && attributes.length > 0) {
			List<Long> playerIdList = Arrays.asList(playerId);
			List<UnitId> unitIdList = Arrays.asList(UnitId.valueOf(playerId, ElementType.PLAYER));
			UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, attributes);
		}
	}

	
	protected void synthEnchangeStone(IoSession session, Request request, Response response) {
		String bindUserItems = "";
		String unBindUserItems = "";
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.BIND_USER_PROPS)) {
				bindUserItems = (String) aso.get(ResponseKey.BIND_USER_PROPS);
			}
			if (aso.containsKey(ResponseKey.UNBIND_USER_PROPS)) {
				unBindUserItems = (String) aso.get(ResponseKey.UNBIND_USER_PROPS);
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		SynthStoneResult synthResult = propsFacade.synthStoneItem(playerId, bindUserItems, unBindUserItems);
		
		if(synthResult.getUserPropsList() != null && !synthResult.getUserPropsList().isEmpty()) {
			List<BackpackEntry> backpackEntries = voFactory.getUserPropsEntries(synthResult.getUserPropsList());
			pushBackEntriesAndPushMoney(playerId, backpackEntries, true, AttributeKeys.SILVER);
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		resultMap.put(ResponseKey.RESULT, synthResult.getResult());
		resultMap.put(ResponseKey.SUCCESS_COUNT, synthResult.getSuccessCount());
		resultMap.put(ResponseKey.FAILURE_COUNT, synthResult.getFailureCount());
		response.setValue(resultMap);
		session.write(response);
	}




	private void queryUserEquip(IoSession session, Request request, Response response) {
		long userEquipId = 0L;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.USER_EQUIP_ID)) {
				userEquipId = ((Number) aso.get(ResponseKey.USER_EQUIP_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		response.setValue(voFactory.getUserEquipVO(userEquipId));
		session.write(response);
	}


	private void queryUserEquipArray(IoSession session, Request request, Response response) {
		Object[] userEquipId = null;
		List<UserEquipVO> userEquipVOList = new ArrayList<UserEquipVO>(0);
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.USER_EQUIP_ID)) {
				userEquipId = (Object[]) aso.get(ResponseKey.USER_EQUIP_ID);
			}
			
			for (Object equipId : userEquipId) {
				long equipLongId = ((Number)equipId).longValue();
				UserEquipVO userEquipVO = voFactory.getUserEquipVO(equipLongId);
				if(userEquipVO != null) {
					userEquipVOList.add(userEquipVO);
				}
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		response.setValue(userEquipVOList.toArray());
		session.write(response);
	}

	protected void mergeUserProps(IoSession session, Request request, Response response) {
		long userPropsId = 0L;
		long targetPropsId = 0L;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.USER_PROPSID)) {
				userPropsId = ((Number) aso.get(ResponseKey.USER_PROPSID)).longValue();
			}
			if (aso.containsKey(ResponseKey.TARGET_ID)) {
				targetPropsId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		int result = propsFacade.mergeUserProps(playerId, userPropsId, targetPropsId);
	};


	protected void spliteUserProps(IoSession session, Request request, Response response) {
		int count = 0;
		long userPropsId = 0L;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.USER_PROPSID)) {
				userPropsId = ((Number) aso.get(ResponseKey.USER_PROPSID)).longValue();
			}
			if (aso.containsKey(ResponseKey.COUNT)) {
				count = ((Number) aso.get(ResponseKey.COUNT)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		int result = propsFacade.spliteUserProps(playerId, userPropsId, count);
	}


	protected void autoAdjustBackpack(IoSession session, Request request, Response response) {
		int backpack = -1;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.PACKAGE_TYPE)) {
				backpack = ((Number) aso.get(ResponseKey.PACKAGE_TYPE)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		int result = propsFacade.settleBackpackPos(playerId, backpack);
	}


	protected void roleDressEquip(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int index = -1;
		long userEquipId = -1L;
		long targetEquipId = -1L;
		boolean dressType = false;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TYPE)) {
				dressType = (Boolean) aso.get(ResponseKey.TYPE);
			}
			if (aso.containsKey(ResponseKey.INDEX)) {
				index = ((Number) aso.get(ResponseKey.INDEX)).intValue();
			}
			if (aso.containsKey(ResponseKey.TARGET_ID)) {
				targetEquipId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
			if (aso.containsKey(ResponseKey.USER_EQUIP_ID)) {
				userEquipId = ((Number) aso.get(ResponseKey.USER_EQUIP_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		ResultObject<BackpackEntry[]> resultObject = null;
		if (dressType) {
			resultObject = propsFacade.dressUserEquip(playerId, userEquipId, targetEquipId);
		} else {
			resultObject = propsFacade.undressUserEquip(playerId, userEquipId, index);
		}

		
		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		resultMap.put(ResponseKey.TYPE, dressType);
		resultMap.put(ResponseKey.RESULT, resultObject.getResult());
		resultMap.put(ResponseKey.ENTRY, resultObject.getValue());
		
		response.setValue(resultMap);
		session.write(response);
	}


	protected void updateEntryPosition(IoSession session, Request request, Response response) {
		Object[] info = null;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.INFO)) {
				info = (Object[]) aso.get(ResponseKey.INFO);
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		propsFacade.updateBackpackEntryPosition(playerId, info);
	}


	protected void dropUserProps(IoSession session, Request request, Response response) {
		int goodsType = -1;
		long userPropsId = -1;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.GOODS_TYPE)) {
				goodsType = ((Number) aso.get(ResponseKey.GOODS_TYPE)).intValue();
			}
			if (aso.containsKey(ResponseKey.USER_PROPSID)) {
				userPropsId = ((Number) aso.get(ResponseKey.USER_PROPSID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		ResultObject<BackpackEntry> resultObject = null;
		if (goodsType == GoodsType.PROPS) {
			resultObject = propsFacade.dropUserProps(playerId, userPropsId);
		} else if (goodsType == GoodsType.EQUIP) {
			resultObject = propsFacade.dropUserEquip(playerId, userPropsId);
		}

		if (resultObject != null && resultObject.getValue() != null) {
			BackpackEntry backpackEntry = resultObject.getValue();                         
			int dropCount = backpackEntry.getCount();
			backpackEntry.setCount(0);
			pushBackEntriesAndPushMoney(playerId, Arrays.asList(backpackEntry), false);
			GoodsVO goodsVO = GoodsVO.valueOf(backpackEntry.getBaseId(), backpackEntry.getGoodsType(), -dropCount);
			MessagePushHelper.pushGoodsCountChange2Client(playerId, goodsVO);
		}
	}


	protected void sellUserProps(IoSession session, Request request, Response response) {
		int goodsType = -1;
		long userPropsId = -1;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.GOODS_TYPE)) {
				goodsType = ((Number) aso.get(ResponseKey.GOODS_TYPE)).intValue();
			}
			if (aso.containsKey(ResponseKey.USER_PROPSID)) {
				userPropsId = ((Number) aso.get(ResponseKey.USER_PROPSID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		ResultObject<BackpackEntry> resultObject = null;
		if (goodsType == GoodsType.PROPS) {
			resultObject = propsFacade.sellUserProps(playerId, userPropsId);
		} else if (goodsType == GoodsType.EQUIP) {
			resultObject = propsFacade.sellUserEquip(playerId, userPropsId);
		} else {
			resultObject = ResultObject.ERROR(FAILURE);
		}

		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		resultMap.put(ResponseKey.RESULT, resultObject.getResult());
		
		if(resultObject.getValue() != null){
			this.pushBackEntriesAndPushMoney(playerId, Arrays.asList(resultObject.getValue()), false, AttributeKeys.SILVER);
			resultMap.put(ResponseKey.BASEID, resultObject.getValue().getBaseId());
			resultMap.put(ResponseKey.GOODS_TYPE, goodsType);
		}
		
		response.setValue(resultMap);
		session.write(response);
	}


	protected void getUserProps(IoSession session, Request request, Response response) {
		Object[] userPropsIds = null;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.USER_PROPSID)) {
				userPropsIds = (Object[]) aso.get(ResponseKey.USER_PROPSID);
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		List<UserProps> userProps = propsFacade.queryUserProps(userPropsIds);
		response.setValue(userProps.toArray());
		session.write(response);
	}


	protected void checkRolePackInfo(IoSession session, Request request, Response response) {
		int backpack = -1;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.PACKAGE_TYPE)) {
				backpack = ((Number) aso.get(ResponseKey.PACKAGE_TYPE)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		List<BackpackEntry> entries = voFactory.listBackpackEntry(playerId, backpack);
		long[] cdTimeArray = voFactory.getCoolTime(playerId, entries);
		Map<String, Object> resultMap = new HashMap<String, Object>(4);
		resultMap.put(ResponseKey.TYPE, false);
		resultMap.put(ResponseKey.PACKAGE_TYPE, backpack);
		resultMap.put(ResponseKey.ENTRY, entries.toArray());
		resultMap.put(ResponseKey.COOL_TIME, cdTimeArray);
		response.setValue(resultMap);
		sessionManager.write(session, response);
	}


	protected void useUserProps(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long userItemId = 0L;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.USER_PROPSID)) {
				userItemId = ((Number) aso.get(ResponseKey.USER_PROPSID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		int result = propsFacade.useProps(playerId, userItemId, 1);
		response.setValue(result);
		session.write(response);
	}


	protected void getPackageInfo(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int packageType = 0;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.PACKAGE_TYPE)) {
				packageType = ((Number) aso.get(ResponseKey.PACKAGE_TYPE)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		byte[] position = packageFacade.getPackagePosition(playerId, packageType);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.INFO, position);
		resultMap.put(ResponseKey.PACKAGE_TYPE, packageType);
		response.setValue(resultMap);
		session.write(response);
	}


	protected void updatePackageInfo(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int packageType = -1;
		byte[] packageInfo = null;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.PACKAGE_TYPE)) {
				packageType = ((Number) aso.get(ResponseKey.PACKAGE_TYPE)).intValue();
			}
			if (aso.containsKey(ResponseKey.INFO)) {
				packageInfo = (byte[]) aso.get(ResponseKey.INFO);
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		packageFacade.saveTempPosition(playerId, packageType, packageInfo);
	}


	protected void expandBackpack(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		BackpackEntry[] backpackEntries = null;
		int backpack     = -1;
		String userItems = "";
		int autoBuyCount = 0;
		
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.USER_PROPS)) {
				userItems = (String) aso.get(ResponseKey.USER_PROPS);
			}
			if (aso.containsKey(ResponseKey.PACKAGE_TYPE)) {
				backpack = ((Number) aso.get(ResponseKey.PACKAGE_TYPE)).intValue();
			}
			if (aso.containsKey(ResponseKey.AUTO_BUY_COUNT)) {
				autoBuyCount = ((Number) aso.get(ResponseKey.AUTO_BUY_COUNT)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		} 
		
		ResultObject<Collection<BackpackEntry>> result = propsFacade.expandBackpack(playerId, userItems, autoBuyCount, backpack);
		
		if (result.getValue() != null) {
			Collection<BackpackEntry> entries = result.getValue();
			backpackEntries = result.getValue().toArray(new BackpackEntry[entries.size()]);
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, result.getResult());
		resultMap.put(ResponseKey.PACKAGE_TYPE, backpack);
		
		response.setValue(resultMap);
		session.write(response);
		pushUserBackpack2Client(playerId, backpackEntries, AttributeKeys.GOLDEN, AttributeKeys.BACKPACK_SIZE, AttributeKeys.STORAGE_SIZE);
	}
	
	
	
	
	private void pushUserBackpack2Client(long playerId, BackpackEntry[] backpackEntries, Object... attributes) {
		int backpackType = BackpackType.DEFAULT_BACKPACK;
		List<Long> playerIds = Arrays.asList(playerId);
		List<UnitId> playerUnitIds = Arrays.asList(UnitId.valueOf(playerId, ElementType.PLAYER));
		
		if (backpackEntries != null && backpackEntries.length > 0) {
			MessagePushHelper.pushUserProps2Client(playerId, backpackType, false, backpackEntries);
		}
		
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIds, playerUnitIds, attributes);
		
	}

	private void usePortableBag(IoSession session, Request request, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		ASObject params = (ASObject) request.getValue();
		int result = FAILURE;
		if(params != null){
			try {
				int type = (Integer)params.get(ResponseKey.TYPE);
				PortableType pType = EnumUtils.getEnum(PortableType.class, type);
				result = propsFacade.usePortableBag(playerId, true, pType);
				if(result == SUCCESS){
					result = type;
				}
			} catch (Exception e) {
			}
		}
		response.setValue(result);
		session.write(response);
	}

	private void batchBuyAndUserProps(IoSession session, Request request,Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		ASObject params = (ASObject)request.getValue();
		int result = FAILURE;
		if(params != null){
			try {
				int propsId = (Integer)params.get(ResponseKey.PROPS_ID);
				int count = (Integer)params.get(ResponseKey.COUNT);
				result = propsFacade.batchBuyOrUseProps(playerId, propsId, count, true, false);
				
			} catch (Exception e) {
			}
		}
		response.setValue(result);
		session.write(response);
	}
	
}
