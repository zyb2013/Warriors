package com.yayo.warriors.socket.handler.pet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import com.yayo.common.socket.type.ResponseCode;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.pet.entity.Pet;
import com.yayo.warriors.module.pet.facade.PetFacade;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.pet.model.PetZoom;
import com.yayo.warriors.module.pet.vo.EggVo;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import flex.messaging.io.amf.ASObject;
import static com.yayo.warriors.module.pet.constant.PetConstant.*;

@Component
public class PetHandle extends BaseHandler {

	@Autowired
	private PetFacade petFacade;
	@Autowired
	private PetManager petManager;
	@Autowired
	private UserManager userManager;
	
	
	protected int getModule() {
		return Module.PET;
	}

	
	protected void inititialize() {
		
		putInvoker(PetCmd.LOAD_PETIDS,new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadPetIds(session,request,response);
			}
		});
		
		putInvoker(PetCmd.GET_PET_ATTRIBUTE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				getPetAttribute(session, request, response);
			}
		});
		
		putInvoker(PetCmd.UPDATE_PET_MOTION, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				updatePetMotion(session, request, response);
			}
		});
		
		putInvoker(PetCmd.PET_GO_FINGHTING, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				goFinghting(session, request, response);
			}
		});
		
		putInvoker(PetCmd.PET_BACK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				backPet(session, request, response);
			}
		});
		
		putInvoker(PetCmd.PET_FREE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				freePet(session, request, response);
			}
		});
		
		putInvoker(PetCmd.PET_OPEN_DRAW, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				drawPet(session, request, response);
			}
		});
		
		putInvoker(PetCmd.PET_MIX, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				mixPet(session, request, response);
			}
		});
		
		putInvoker(PetCmd.PET_GET_EGG, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				eggCache(session, request, response);
			}
		});
		
		putInvoker(PetCmd.PET_DRAW_EGG, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				drawEgg(session, request, response);
			}
		});
		
		putInvoker(PetCmd.PET_FREE_EGG, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				freeEgg(session, request, response);
			}
		});
		
		putInvoker(PetCmd.OPET_PET_SLOT, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				openPetSolt(session, request, response);
			}
		});
		
		putInvoker(PetCmd.PET_USE_PROPS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				useProps(session, request, response);
			}
		});
		
		putInvoker(PetCmd.LOAD_FAMOUS_GENERAL, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadFamous(session, request, response);
			}
		});
		
		putInvoker(PetCmd.PET_LEVEL_UP, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				petLevelup(session, request, response);
			}
		});
		
		putInvoker(PetCmd.PET_TRAINING_MERGED, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				trainingMerged(session, request, response);
			}
		});
		
		putInvoker(PetCmd.PET_MERGED, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				petMerged(session, request, response);
			}
		});
		
		putInvoker(PetCmd.NEW_PET_TRAINING_SAVVY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
			}
		});
		
		putInvoker(PetCmd.COMEBACK_PET, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				comebackPet(session, request, response);
			}
		});
		
		putInvoker(PetCmd.START_TRAING_PET, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				startTraingPet(session, request, response);
			}
		});
		
		putInvoker(PetCmd.CALC_TRAING_PET, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				calcTraingPet(session, request, response);
			}
		});
		
		putInvoker(PetCmd.FINISH_TRAING_PET, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				finishTraingPet(session, request, response);
			}
		});
	}
	
	
	protected void finishTraingPet(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		long petId    = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PET_ID)) {
				petId = ((Number)aso.get(ResponseKey.PET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = petFacade.finishTraingPet(playerId, petId);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, result);
		resultMap.put(ResponseKey.PET_ID, petId);
		response.setValue(resultMap);
		session.write(response);
	}
	
	
	protected void calcTraingPet(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		long petId    = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PET_ID)) {
				petId = ((Number)aso.get(ResponseKey.PET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Map<String,Object>> result = petFacade.calcTraingPet(playerId, petId);
		if(result.getResult() == SUCCESS){
			response.setValue(result.getValue());
			session.write(response);
		}else{
			Map<String, Object> map = new HashMap<String, Object>(1);
			map.put(ResponseKey.RESULT, result.getResult());
			response.setValue(map);
			session.write(response);
		}
		
	}
	
	
	
	protected void startTraingPet(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		long petId    = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PET_ID)) {
				petId = ((Number)aso.get(ResponseKey.PET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Pet> result = petFacade.startTraingPet(playerId, petId);
		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		resultMap.put(ResponseKey.RESULT, result.getResult());
		resultMap.put(ResponseKey.PET_ID, petId);
		if(result.getResult() == SUCCESS){
			Pet pet = result.getValue();
			resultMap.put(ResponseKey.START_TIME, pet.getStartTraingTime());
		}
		
		response.setValue(resultMap);
		session.write(response);
	}
	
	
	
	protected void comebackPet(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		int result = petFacade.comebackPet(playerId);
	    response.setValue(result);
		session.write(response);
	}
	
	
	
	protected void trainingNewSavvy(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		long petId    = 0;
		String userItem = "";
		int autoBuyCount = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PET_ID)) {
				petId = ((Number)aso.get(ResponseKey.PET_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.PROPS_ID)) {
				userItem = (String)aso.get(ResponseKey.PROPS_ID);
			}
			if(aso.containsKey(ResponseKey.AUTO_BUY_COUNT)) {
				autoBuyCount = ((Number)aso.get(ResponseKey.AUTO_BUY_COUNT)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		 ResultObject<Boolean> result = petFacade.trainingPetSavvy(playerId, petId, userItem, autoBuyCount);
		 Map<String, Object> map = new HashMap<String, Object>(2);
		 if(result.getResult() == SUCCESS){
			 map.put(ResponseKey.RESULT, result.getResult());
			 map.put(ResponseKey.STATE, result.getValue());
		     response.setValue(map);
			 session.write(response);
		 }else{
			 response.setValue(result.getResult());
			 session.write(response);
		 }
	}
	
	
	
	
	protected void petMerged(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		long petId    = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PET_ID)) {
				petId = ((Number)aso.get(ResponseKey.PET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = petFacade.mergedPet(playerId, petId);
		response.setValue(result);
		session.write(response);
	}
	
	
	protected void trainingMerged(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		long petId    = 0;
		String userItem = "";
		int autoBuyCount = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PET_ID)) {
				petId = ((Number)aso.get(ResponseKey.PET_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.PROPS_ID)) {
				userItem = (String)aso.get(ResponseKey.PROPS_ID);
			}
			if(aso.containsKey(ResponseKey.AUTO_BUY_COUNT)) {
				autoBuyCount = ((Number)aso.get(ResponseKey.AUTO_BUY_COUNT)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Map<String,Object>> result = petFacade.trainingMerged(playerId, petId, userItem, autoBuyCount);
		if(result.getResult() == SUCCESS){
			response.setValue(result.getValue());
			session.write(response);
		}else{
			response.setValue(result.getResult());
			session.write(response);
		}
	}
	
	
	protected void petLevelup(IoSession session, Request request, Response response){
		long playerId = this.sessionManager.getPlayerId(session);
		int exp = 0;
		long petId = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PET_ID)) {
				petId = ((Number)aso.get(ResponseKey.PET_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.EXP)) {
				exp = ((Number)aso.get(ResponseKey.EXP)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Map<String,Object>> result = petFacade.addPetExp(playerId, petId, exp);
		if(result.getResult() == SUCCESS){
			response.setValue(result.getValue());
			session.write(response);
		}else{
			response.setValue(result.getResult());
			session.write(response);
		}
	}
	
	
	protected void loadFamous(IoSession session, Request request, Response response){
		long playerId = this.sessionManager.getPlayerId(session);
		Collection<Integer> result = this.petFacade.loadFamous(playerId);
		if(result == null){
			result = new ArrayList<Integer>();
		}
		
		response.setValue(result.toArray());
		session.write(response);
	}
	
	
	protected void useProps(IoSession session, Request request, Response response){
		long playerId = this.sessionManager.getPlayerId(session);
		long petId    = 0;
		long propsId  = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PET_ID)) {
				petId = ((Number)aso.get(ResponseKey.PET_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.PROPS_ID)) {
				propsId = ((Number)aso.get(ResponseKey.PROPS_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = this.petFacade.useProps(playerId, petId, propsId);
		response.setValue(result);
		session.write(response);
	}
	
	
	protected void freeEgg(IoSession session, Request request, Response response){
		long playerId = this.sessionManager.getPlayerId(session);
		Object[] key = null;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.KEY)) {
				key = (Object[]) aso.get(ResponseKey.KEY);
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Integer> result = this.petFacade.freeEggPet(playerId, key);
		if(result.getResult() == SUCCESS){
			Map<String,Object> map = new HashMap<String, Object>(2);
			map.put(ResponseKey.KEY, key);
			map.put(ResponseKey.EXP, result.getValue());
			response.setValue(map);
			session.write(response);
		}else{
			response.setValue(result.getResult());
			session.write(response);
		}
	}
	
	
	protected void drawEgg(IoSession session, Request request, Response response){
		long playerId = this.sessionManager.getPlayerId(session);
		int key = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.KEY)) {
				key = ((Number)aso.get(ResponseKey.KEY)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Long> result = this.petFacade.drawEggPet(playerId, key);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put(ResponseKey.RESULT, result.getResult());
		if(result.getResult() == SUCCESS){
			map.put(ResponseKey.PET_ID, result.getValue());
			map.put(ResponseKey.KEY, key);
		}
		
		response.setValue(map);
		session.write(response);
	}
	
	
	protected void eggCache(IoSession session, Request request, Response response){
		long playerId = this.sessionManager.getPlayerId(session);
		PetZoom petZoom = this.petFacade.getPetZoom(playerId);
		if(petZoom != null){
			List<EggVo> vos = petZoom.getEggVo();
			Map<String,Object> result = new HashMap<String, Object>(2);
			result.put(ResponseKey.RESULT, SUCCESS);
			result.put(ResponseKey.EGG_VO, vos.toArray());
			response.setValue(result);
			session.write(response);
		}

	}
	
	
	
	protected void mixPet(IoSession session, Request request, Response response){
		long playerId = this.sessionManager.getPlayerId(session);
		long mPetId   = 0;
		long dPetId   = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PET_ID)) {
				mPetId = ((Number)aso.get(ResponseKey.PET_ID)).longValue();
			}
			
			if(aso.containsKey(ResponseKey.DPET_ID)) {
				dPetId = ((Number)aso.get(ResponseKey.DPET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Long> result = this.petFacade.mixPet(playerId, mPetId, dPetId);
		
		Map<String,Object> map = new HashMap<String,Object>(2);
		map.put(ResponseKey.RESULT, result.getResult());
		if(result.getResult() == SUCCESS){
			map.put(ResponseKey.PET_ID,  result.getValue());
			map.put(ResponseKey.DPET_ID, dPetId);
		}
		response.setValue(map);
		session.write(response);
	}
	
	
	
	protected void caclEnergy(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
	 	ResultObject<Integer> result = petFacade.caclPetEnergy(playerId);
		
	 	Map<String,Object> map = new HashMap<String,Object>(2);
		map.put(ResponseKey.RESULT, result.getResult());
		if(result.getResult() == SUCCESS){
			int energy = result.getValue();
			map.put(ResponseKey.ENERGY, energy);
		}
		response.setValue(map);
		session.write(response);
	}
	
	
	protected void drawPet(IoSession session, Request request, Response response){
		long playerId = this.sessionManager.getPlayerId(session);
		String eggItem = "";
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PROPS_ID)) {
				eggItem = (String) aso.get(ResponseKey.PROPS_ID);
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<PetZoom> result = petFacade.openEggDraw(playerId, eggItem);
		
		Map<String,Object> map = new HashMap<String,Object>(2);
		map.put(ResponseKey.RESULT, result.getResult());
		if(result.getResult() == SUCCESS){
			PetZoom petZoom = result.getValue();
			map.put(ResponseKey.EGG_VO, petZoom.getEggVo().toArray());
		}
		response.setValue(map);
		session.write(response);
		
	}
	
	
	protected void freePet(IoSession session, Request request, Response response){
		long playerId = this.sessionManager.getPlayerId(session);
		long petId = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PET_ID)) {
				petId = ((Number)aso.get(ResponseKey.PET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Integer> result = petFacade.freePet(playerId, petId);
		
		Map<String,Object> map = new HashMap<String,Object>(2);
		map.put(ResponseKey.RESULT, result.getResult());
		if(result.getResult() == SUCCESS){
			map.put(ResponseKey.PET_ID, petId);
			map.put(ResponseKey.EXP, result.getValue());
		}
		response.setValue(map);
		session.write(response);
		
	}
	
	
	protected void backPet(IoSession session, Request request, Response response){
		long playerId = this.sessionManager.getPlayerId(session);
		ResultObject<Long> result = petFacade.goBack(playerId);
		Map<String,Object> resultMap = new HashMap<String, Object>();
		if(result.getResult() == SUCCESS){
			resultMap.put(ResponseKey.RESULT, result.getResult());
			resultMap.put(ResponseKey.PET_ID, result.getValue());
		}else{
			resultMap.put(ResponseKey.RESULT, result.getResult());
		}
		response.setValue(resultMap);
		session.write(response);
	}
	
	
	protected void goFinghting(IoSession session, Request request, Response response){
		long playerId = this.sessionManager.getPlayerId(session);
		long petId = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PET_ID)) {
				petId = ((Number)aso.get(ResponseKey.PET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Integer> result = petFacade.petGoFighting(playerId, petId);
		Map<String,Object> map = new HashMap<String,Object>(3);
		map.put(ResponseKey.RESULT, result.getResult());
		if(result.getResult() == SUCCESS){
			map.put(ResponseKey.PET_ID, petId);
			map.put(ResponseKey.ENERGY, result.getValue());
		}
		response.setValue(map);
		session.write(response);
	}
	

	protected void updatePetMotion(IoSession session, Request request, Response response){
		long playerId = this.sessionManager.getPlayerId(session);
		long petId = 0;
		int x = 0;
		int y = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PET_ID)) {
				petId = ((Number)aso.get(ResponseKey.PET_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.X)){
				x =  ((Number)aso.get(ResponseKey.X)).intValue();
			}
			if(aso.containsKey(ResponseKey.Y)){
				y =  ((Number)aso.get(ResponseKey.Y)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = petFacade.updatePetMotion(playerId, petId, x, y);
		response.setValue(result);
		session.write(response);
	}
	
	
	
	protected void getPetAttribute(IoSession session, Request request, Response response){
		long playerId = this.sessionManager.getPlayerId(session);
		Object[] params = null;
		long petId = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PET_ID)) {
				petId = ((Number)aso.get(ResponseKey.PET_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.PARAMS)) {
				params = (Object[])aso.get(ResponseKey.PARAMS);
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Object[] values = petManager.getPetAttributes(playerId,petId, params);
		resultMap.put(ResponseKey.PET_ID, petId);
		resultMap.put(ResponseKey.PARAMS, params);
		resultMap.put(ResponseKey.VALUES, values);
		
		response.setValue(resultMap);
		session.write(response);
	}
	
	
	protected void loadPetIds(IoSession session, Request request, Response response){
		long playerId = this.sessionManager.getPlayerId(session);
		UserDomain userDomain = userManager.getUserDomain(playerId);
		Set<Long> petIds = this.petFacade.getPlayerPetIds(playerId);
		int solt = this.petFacade.getPetSoltSize(playerId);
		if(petIds != null && userDomain != null){
			Map<String,Object> map = new HashMap<String, Object>();
			map.put(ResponseKey.RESULT, SUCCESS);
			map.put(ResponseKey.PET_IDS, petIds.toArray());
			map.put(ResponseKey.SOLTSIZE, solt);
			map.put(ResponseKey.EXP, userDomain.getPlayer().getPetexperience());
			response.setValue(map);
			session.write(response);
		}
	}
	
	
	protected void openPetSolt(IoSession session, Request request, Response response){
		long playerId = this.sessionManager.getPlayerId(session);
		String propsId = null;
		int autoBuyCount = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PROPS_ID)) {
				propsId = (String) aso.get(ResponseKey.PROPS_ID);
			}
			if(aso.containsKey(ResponseKey.AUTO_BUY_COUNT)){
				autoBuyCount = ((Number)aso.get(ResponseKey.AUTO_BUY_COUNT)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Integer> result = this.petFacade.openPetSolt(playerId, propsId, autoBuyCount);
		if(result.getResult() == SUCCESS){
			response.setValue(result.getValue());
			session.write(response);
		}else{
			response.setValue(result.getResult());
			session.write(response);
		}
	}

}
