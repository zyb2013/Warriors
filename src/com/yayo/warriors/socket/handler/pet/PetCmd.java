package com.yayo.warriors.socket.handler.pet;

import java.util.Map;


public interface PetCmd {
	int LOAD_PETIDS = 1;
	int GET_PET_ATTRIBUTE = 2;
	int UPDATE_PET_MOTION = 3;
	int PET_GO_FINGHTING = 4;
	int PET_BACK = 5;
	int PET_FREE = 6;
	int PET_OPEN_DRAW = 7;
	int PET_MIX = 8;
	int PET_GET_EGG = 9;
	int PET_DRAW_EGG = 10;
	int PET_FREE_EGG = 11;
	int OPET_PET_SLOT = 12;
	int PET_USE_PROPS = 13;
	int LOAD_FAMOUS_GENERAL = 14;  
	int PET_LEVEL_UP = 15;
	int PET_TRAINING_MERGED = 16;
	int PET_MERGED = 17;
	int NEW_PET_TRAINING_SAVVY = 18;
	int COMEBACK_PET = 19;
	int START_TRAING_PET = 20;
	int CALC_TRAING_PET = 21;
	int FINISH_TRAING_PET = 22;
	
	int PUSH_PET_GO_FIGHTING = 100;
	int PUSH_PET_BACK = 101;
	int PUSH_PET_ATTRIBUTE = 102;
	int PUSH_PET_MERGED = 103;
	int PUSH_PET_LEVEL_UP = 104;
	
}
