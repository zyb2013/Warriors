SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for userActive
-- ----------------------------
DROP TABLE IF EXISTS `userActive`;
CREATE TABLE `userActive` (
   `playerId` bigint(20) NOT NULL DEFAULT '0' COMMENT '玩家的ID',
   `levelActive` longtext COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '冲级活动已领取的奖励列表',
   `rankActive` longtext COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '排名活动已领取的奖励列表',
   `exChangeActive` longtext COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '兑换活动领取的奖励列表',
   PRIMARY KEY (`playerId`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


-- ----------------------------
-- Table structure for userAllianceTask
-- ----------------------------
DROP TABLE IF EXISTS `userAllianceTask`;
CREATE TABLE `userAllianceTask` (
 `playerId` BIGINT(20) NOT NULL DEFAULT '0' COMMENT '玩家的ID',
 `progresstask` VARCHAR(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '任务序列',
 `rewardstask`  VARCHAR(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '已经完成的任务',
 `startTime` BIGINT(20) NOT NULL DEFAULT '0' COMMENT '任务开始时间',

   PRIMARY KEY (`playerId`)
 ) ENGINE=INNODB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


-- ----------------------------
-- Table structure for alliance
-- ----------------------------
DROP TABLE IF EXISTS `alliance`;
CREATE TABLE `alliance` (
  `allianceId` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '公会ID ',
  `name` VARCHAR(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '帮会名字',
  `playerId` BIGINT(20) NOT NULL DEFAULT '0' COMMENT '创建者(帮主)',
  `state` INT(5) NOT NULL COMMENT '工会的状态',
  `camp` INT(5) NOT NULL DEFAULT '0' COMMENT '帮派所属阵营',
  `notice` VARCHAR(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '公告信息',
  `level` INT(5) NOT NULL DEFAULT '0' COMMENT '工会等级',
  `prestige` INT(5) NOT NULL DEFAULT '0' COMMENT '威望值',
  `silver` BIGINT(20) NOT NULL DEFAULT '0' COMMENT '资金',
  `tokenPropsCount` INT(5) NOT NULL DEFAULT '0' COMMENT '捐献的令牌',
 
  `prolawNum` INT(5) NOT NULL DEFAULT '0' COMMENT ' 护法人数',
  `elderNum` INT(5) NOT NULL COMMENT '长老人数',
  `deputymasterNum` INT(5) NOT NULL DEFAULT '0' COMMENT '副帮主',
  `vilidaState` INT(5) NOT NULL DEFAULT '0' COMMENT '帮派设置验证状态',
  `masterName` VARCHAR(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '帮主的名字',
  `booksLevel` INT(5) NOT NULL DEFAULT '0' COMMENT '藏经阁等级',
  `shopLevel`  INT(5) NOT NULL DEFAULT '0' COMMENT '商店等级',
  `daisLevel`  INT(5) NOT NULL DEFAULT '0' COMMENT  '祭台等级',
  `arenaLevel` INT(5) NOT NULL DEFAULT '0' COMMENT  '演舞台等级',
  `skills` VARCHAR(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '帮派研究所获得技能',
  `levelupRecord` LONGTEXT COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '帮派升级记录',
  `levelActive` longtext COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '冲级活动,帮主已领取的奖励列表',
 
  PRIMARY KEY (`allianceId`),
  KEY `IDX_ALLIANCE_STATE` (`state`)
) ENGINE=INNODB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for backpack
-- ----------------------------
DROP TABLE IF EXISTS `backpack`;
CREATE TABLE `backpack` (
  `packageType` int(5) NOT NULL DEFAULT '0' COMMENT '背包类型',
  `playerId` bigint(20) NOT NULL COMMENT '角色ID',
  `packageInfo` blob NULL,
  PRIMARY KEY (`packageType`,`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for dropRecord
-- ----------------------------
DROP TABLE IF EXISTS `dropRecord`;
CREATE TABLE `dropRecord` (
  `id` bigint(20) NOT NULL default '0' COMMENT '所属者的ID',
  `day` int(5) NOT NULL default '0' COMMENT '记录的日期',
  `dropInfo` blob NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for userEscortTask
-- ----------------------------
DROP TABLE IF EXISTS `userEscortTask`;
 CREATE TABLE `userEscortTask` (
   `playerId` bigint(20) NOT NULL default '0' COMMENT '角色ID',
   `actionTimes` int(11) NOT NULL default '0' COMMENT '当天接受任务的次数',
   `lastDate` bigint(20) NOT NULL default '0' COMMENT '刷新任务的时间',
   `plunderTimes` int(5) NOT NULL default '0' COMMENT '被劫镖的次数',
   `beplunderTimes` int(5) NOT NULL default '0' COMMENT '劫他人的镖的次数',
   `quality` int(5) NOT NULL default '0' COMMENT '任务品质',
   `status` int(5) NOT NULL default  '0' COMMENT '任务的状态',
   `taskId` int(11) NOT NULL default '0' COMMENT '接受任务的编号',
   `timeCalcer` bigint(20) NOT NULL default '0' COMMENT '接受任务的开始时间',
   `protection` int(3) NOT NULL default '0' COMMENT '是否有保护',
   `acceptLevel` int(5) NOT NULL DEFAULT '0' COMMENT '接受该任务时的玩家等级',
   PRIMARY KEY (`playerId`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for friend
-- ----------------------------
DROP TABLE IF EXISTS `friend`;
CREATE TABLE `friend` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `playerId` bigint(20) DEFAULT NULL default '0',
  `state` int(11) NOT NULL default '0',
  `targetId` bigint(20) DEFAULT NULL default '0',
  `type` int(11) DEFAULT NULL default '0',
  `value` int(11) NOT NULL default '0',
  PRIMARY KEY (`id`),
  KEY `IDX_PLAYERID_STATE` (`playerId`,`state`),
  KEY `IDX_TARGETID_STATE` (`state`,`targetId`),
  KEY `IDX_TARGETID_STATE_TYPE` (`state`,`targetId`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for friendStreasure
-- ----------------------------
DROP TABLE IF EXISTS `friendsTreasure`;
CREATE TABLE `friendsTreasure` (
  `playerId` bigint(20) NOT NULL default '0',
  `blessExp` int(11) NOT NULL default '0',
  `isReward` int(5) NOT NULL default '0',
  `params` varchar(255) NOT NULL DEFAULT '',
  `cleanDate` int(11) NOT NULL default '0',
  `wineMeasure` int(11) NOT NULL default '0',
  `isDrinked` int(5) NOT NULL default '0',
  `greetFriends` longtext COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for laveMallProp
-- ----------------------------
DROP TABLE IF EXISTS `laveMallProp`;
CREATE TABLE `laveMallProp` (
  `mallId` int(11) NOT NULL default '0',
  `count` int(11) NOT NULL default '0',
  PRIMARY KEY (`mallId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for marketItem
-- ----------------------------
DROP TABLE IF EXISTS `marketItem`;
CREATE TABLE `marketItem` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `baseId` int(11) NOT NULL default '0',
  `goodsId` bigint(20) NOT NULL default '0',
  `playerId` bigint(20) NOT NULL default '0',
  `sellGolden` bigint(20) NOT NULL default '0',
  `sellSilver` bigint(20) NOT NULL default '0',
  `state` int(11) DEFAULT NULL default '0',
  `sellCount` int(11) NOT NULL default '0',
  `type` int(11) DEFAULT NULL default '0',
  PRIMARY KEY (`id`),
  KEY `IDX_PLAYERID_STATE` (`playerId`,`state`),
  KEY `IDX_PLAYERID_STATE_TYPE` (`playerId`,`state`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for meridian
-- ----------------------------
DROP TABLE IF EXISTS `meridian`;
CREATE TABLE `meridian` (
  `playerId` bigint(20) NOT NULL DEFAULT '0' COMMENT '角色ID',
  `acquiredExp` int(11) NOT NULL default '0',
  `laveTimes` int(11) NOT NULL default '0',
  `meridianIds` int(11) DEFAULT NULL default '0',
  `meridians` blob NULL,
  `stagePass` int(5) NOT NULL default '0',
  `updateTime` datetime DEFAULT NULL,
  PRIMARY KEY (`playerId`),
  KEY `IDX_PLAYERID_MERIDIANIDS` (`playerId`, `meridianIds`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for onlineStatistic
-- ----------------------------
DROP TABLE IF EXISTS `onlineStatistic`;
CREATE TABLE `onlineStatistic` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `maxCount` int(11) NOT NULL default '0',
  `minCount` int(11) NOT NULL default '0',
  `recordDate` varchar(128) COLLATE utf8_unicode_ci DEFAULT NULL,
  `recordTime` varchar(128) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_RECORD_DATE` (`recordDate`),
  KEY `IDX_RECORD_TIME` (`recordTime`),
  KEY `IDX_RECORD_DATE_TIME` (`recordDate`,`recordTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for player
-- ----------------------------
DROP TABLE IF EXISTS `player`;
CREATE TABLE `player` (
  `playerId` bigint(20) NOT NULL AUTO_INCREMENT,
  `camp` int(11) DEFAULT NULL default '0',
  `createTime` datetime DEFAULT NULL,
  `deletable` int(5) NOT NULL  default '0',
  `deleteTime` datetime DEFAULT NULL,
  `forbidChat` varchar(128) COLLATE utf8_unicode_ci DEFAULT NULL,
  `forbidLogin` varchar(128) COLLATE utf8_unicode_ci DEFAULT NULL,
  `golden` bigint(20) NOT NULL default '0',
  `coupon` bigint(20) NOT NULL default '0',
  `icon` int(11) NOT NULL default '0',
  `loginCount` int(11) NOT NULL default '0',
  `loginDays` int(11) NOT NULL default '0',
  `continueDays` int(11) NOT NULL default '0',
  `continueMaxDays` int(11) NOT NULL default '0',
  `loginTime` datetime DEFAULT NULL,
  `logoutTime` datetime DEFAULT NULL,
  `maxBackSize` int(11) NOT NULL default '0',
  `maxPetSlotSize` int(11) NOT NULL default '0',
  `maxStoreSize` int(11) NOT NULL default '0',
  `name` varchar(128) COLLATE utf8_unicode_ci DEFAULT NULL,
  `onlineTimes` bigint(20) NOT NULL default '0',
  `serverId` int(11) NOT NULL default '0',
  `sex` int(11) DEFAULT NULL default '0',
  `silver` bigint(20) NOT NULL default '0',
  `title` int(11) NOT NULL default '0',
  `userName` varchar(128) COLLATE utf8_unicode_ci DEFAULT '',
  `password` varchar(128) COLLATE utf8_unicode_ci DEFAULT '',
  `receiveInfo` blob NULL,
  `guide` blob NULL,
  `adult` int(11) NOT NULL default '0',
  `capacity` int(5) NOT NULL default '0',
  `fashionShow` int(3) NOT NULL default '0',
  `petexperience` int(3) NOT NULL default '0',
  PRIMARY KEY (`playerId`),
  UNIQUE KEY `IDX_NAME` (`name`),
  KEY `IDX_USERNAME_DELETABLE` (`deletable`,`userName`),
  KEY `IDX_USERNAME` (`userName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for playerBattle
-- ----------------------------
DROP TABLE IF EXISTS `playerBattle`;
CREATE TABLE `playerBattle` (
  `playerId` bigint(20) NOT NULL default '0',
  `addHpMax` int(11) NOT NULL default '0',
  `addMpMax` int(11) NOT NULL default '0',
  `constitution` int(11) NOT NULL default '0',
  `dexerity` int(11) NOT NULL default '0',
  `dodge` int(11) NOT NULL default '0',
  `exp` bigint(20) NOT NULL default '0' COMMENT '角色经验',
  `gas` int(11) NOT NULL default '0',
  `hit` int(11) NOT NULL default '0',
  `hp` int(11) NOT NULL default '0',
  `hpBag` int(11) NOT NULL default '0',
  `petHpBag` int(11) NOT NULL default '0',
  `intellect` int(11) NOT NULL default '0',
  `job` int(11) DEFAULT NULL default '0',
  `level` int(11) NOT NULL default '0',
  `mode` int(11) DEFAULT NULL default '0',
  `moveSpeed` int(11) NOT NULL default '0',
  `mp` int(11) NOT NULL default '0',
  `mpBag` int(11) NOT NULL default '0',
  `physicalAttack` int(11) NOT NULL default '0',
  `physicalCritical` int(11) NOT NULL default '0',
  `physicalDefense` int(11) NOT NULL default '0',
  `spirituality` int(11) NOT NULL default '0',
  `strength` int(11) NOT NULL default '0',
  `theurgyAttack` int(11) NOT NULL default '0',
  `theurgyCritical` int(11) NOT NULL default '0',
  `theurgyDefense` int(11) NOT NULL default '0',
  PRIMARY KEY (`playerId`),
  KEY `IDX_PLAYERID_JOB_LEVEL` (`playerId`, `job`,`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for playerMotion
-- ----------------------------
DROP TABLE IF EXISTS `playerMotion`;
CREATE TABLE `playerMotion` (
  `playerId` bigint(20) NOT NULL default '0',
  `mapId` int(11) DEFAULT NULL default '0',
  `x` int(11) DEFAULT NULL default '0',
  `y` int(11) DEFAULT NULL default '0',
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for playerTitle
-- ----------------------------
DROP TABLE IF EXISTS `playerTitle`;
CREATE TABLE `playerTitle` (
  `playerId` bigint(20) NOT NULL default '0',
  `gainTitles` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `titleParams` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for playerVip
-- ----------------------------
DROP TABLE IF EXISTS `playerVip`;
CREATE TABLE `playerVip` (
  `playerId` bigint(20) NOT NULL default '0',
  `lastCleanDate` int(11) NOT NULL default '0',
  `param` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `vipEndTime` bigint(20) NOT NULL default '0',
  `vipLevel` int(11) NOT NULL default '0',
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for systemConfig
-- ----------------------------
DROP TABLE IF EXISTS `systemConfig`;
CREATE TABLE `systemConfig` (
  `id` int(11) NOT NULL default '0',
  `info` blob NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for taskComplete
-- ----------------------------
DROP TABLE IF EXISTS `taskComplete`;
CREATE TABLE `taskComplete` (
  `playerId` bigint(20) NOT NULL default '0',
  `completes` blob NULL,
  `mapTaskCompletes` blob NULL,
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for userAlliance
-- ----------------------------
DROP TABLE IF EXISTS `userAlliance`;
CREATE TABLE `userAlliance` (
  `playerId` BIGINT(20) NOT NULL DEFAULT '0',
  `allianceId` BIGINT(20) NOT NULL DEFAULT '0',
  `allianceName` VARCHAR(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `donate` INT(11) NOT NULL DEFAULT '0',
  `hisdonate` INT(11) NOT NULL DEFAULT '0',
  `jiontime` BIGINT(20) NOT NULL DEFAULT '0',
  `title` INT(11) DEFAULT NULL DEFAULT '0',
  `refreshTime` BIGINT(20) NOT NULL DEFAULT '0',
  `donatePropsCount` INT(5) NOT NULL DEFAULT '0',
  `donateSilverCount` INT(5) NOT NULL DEFAULT '0',  
  `divineCount` INT(5) NOT NULL DEFAULT '0',
  `leavetime` BIGINT(20) NOT NULL DEFAULT '0' COMMENT '离开帮派时间',
  `skills` VARCHAR(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '帮派研究所获得技能',
  PRIMARY KEY (`playerId`),
  KEY `IDX_ALLIANCEID` (`allianceId`)
) ENGINE=INNODB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for userBuffer
-- ----------------------------
DROP TABLE IF EXISTS `userBuffer`;
CREATE TABLE `userBuffer` (
  `playerId` bigint(20) NOT NULL DEFAULT '0' COMMENT '角色ID',
  `skillBuffers` blob NOT NULL COMMENT '技能BUFF',
  `skillDebuffers` blob NOT NULL COMMENT '技能DEBUFF',
  `itemBuffers` blob NOT NULL COMMENT '道具DEBUFF',
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for userCoolTime
-- ----------------------------
DROP TABLE IF EXISTS `userCoolTime`;
CREATE TABLE `userCoolTime` (
  `playerId` bigint(20) NOT NULL default '0',
  `coolTime` blob NULL,
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for userDungeon
-- ----------------------------
DROP TABLE IF EXISTS `userDungeon`;
CREATE TABLE `userDungeon` (
  `playerId` bigint(20) NOT NULL default '0',
  `data` blob NULL,
  `hisData` blob NULL,
  `dungeonBaseId` int(11) NOT NULL default '0',
  `dungeonId` bigint(20) NOT NULL default '0',
  `story` blob NULL,
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for userEquip
-- ----------------------------
DROP TABLE IF EXISTS `userEquip`;
CREATE TABLE `userEquip` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backpack` int(11) NOT NULL default '0',
  `baseId` int(11) NOT NULL default '0',
  `count` int(11) NOT NULL default '0',
  `positionId` int(11) DEFAULT NULL default '0',
  `quality` int(11) DEFAULT NULL default '0',
  `additionAttributes` varchar(512) COLLATE utf8_unicode_ci DEFAULT NULL,
  `attributes` varchar(512) COLLATE utf8_unicode_ci DEFAULT NULL,
  `binding` int(5) NOT NULL default '0',
  `currentEndure` int(11) NOT NULL default '0',
  `currentMaxEndure` int(11) NOT NULL default '0',
  `discardTime` datetime DEFAULT NULL,
  `expiration` datetime DEFAULT NULL,
  `holeAttributes` varchar(512) COLLATE utf8_unicode_ci DEFAULT NULL,
  `playerId` bigint(20) NOT NULL default '0',
  `starLevel` int(11) NOT NULL default '0',
  `shenwuTempo` INT(5) NOT NULL DEFAULT '0',
  `shenwuSwitch` VARCHAR(255) NULL,
  `shenwuAttributes` blob NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_PLAYERID_BACKPACK` (`backpack`,`playerId`),
  KEY `IDX_PLAYERID_BACKPACK_COUNT` (`backpack`,`count`,`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for userHorse
-- ----------------------------
DROP TABLE IF EXISTS `userHorse`;
CREATE TABLE `userHorse` (
  `playerId` bigint(20) NOT NULL default '0',
  `exp` int(11) NOT NULL default '0',
  `level` int(11) NOT NULL default '0',
  `models` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`playerId`),
  KEY `IDX_HORSE_LEVEL` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for userLoopTask
-- ----------------------------
DROP TABLE IF EXISTS `userLoopTask`;
CREATE TABLE `userLoopTask` (
  `playerId` bigint(20) NOT NULL default '0',
  `amount` int(11) NOT NULL default '0',
  `completes` int(11) NOT NULL default '0',
  `conditions` bigint(20) DEFAULT NULL default '0',
  `dayOfWeek` int(11) NOT NULL default '0',
  `quality` int(11) NOT NULL default '0',
  `rewardInfo` varchar(512) COLLATE utf8_unicode_ci DEFAULT NULL,
  `status` int(11) NOT NULL default '0',
  `taskLevel` int(11) NOT NULL default '0',
  `taskParams` varchar(512) COLLATE utf8_unicode_ci DEFAULT NULL,
  `totalAmount` int(11) NOT NULL default '0',
  `type` int(11) NOT NULL,
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for userMapTask
-- ----------------------------
DROP TABLE IF EXISTS `userMapTask`;
CREATE TABLE `userMapTask` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `chain` int(5) NOT NULL default '0',
  `playerId` bigint(20) NOT NULL default '0',
  `status` int(5) NOT NULL default '0',
  `taskEvent` blob NULL,
  `taskId` int(5) NOT NULL default '0',
  PRIMARY KEY (`id`),
  KEY `IDX_PLAYERID_CHAIN` (`chain`,`playerId`),
  KEY `IDX_PLAYERID` (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for userMortalBody
-- ----------------------------
DROP TABLE IF EXISTS `userMortalBody`;
CREATE TABLE `userMortalBody` (
  `playerId` bigint(20) NOT NULL default '0',
  `mortalBody` blob NULL,
  `mortalLevel` int(11) DEFAULT NULL default '0',
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for userPet
-- ----------------------------
DROP TABLE IF EXISTS `userPet`;
CREATE TABLE `userPet` (
   `petId` bigint(20) NOT NULL AUTO_INCREMENT,
   `playerId` bigint(20) NOT NULL default '0',
   `baseId` int(5) NOT NULL default '0',
   `model` int(5) NOT NULL default '0',
   `icon` int(5) NOT NULL default '0',
   `name` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
   `energy` int(5) NOT NULL default '0',
   `slot` int(5) NOT NULL default '0',
   `skill` longtext COLLATE utf8_unicode_ci,
   `status` int(5) NOT NULL default '0',
   `startTraingTime` bigint(20) NOT NULL default '0',
   `totleTraingTime` bigint(20) NOT NULL default '0',
   PRIMARY KEY (`petId`),
   KEY `IDX_PLAYERID_STATE` (`playerId`,`status`),
   KEY `IDX_PLAYERID_BASEID` (`baseId`,`playerId`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci; 
 
 
 
-- ----------------------------
-- Table structure for userPetBattle
-- ----------------------------
DROP TABLE IF EXISTS `userPetBattle`;
CREATE TABLE `userPetBattle` (
   `petId` bigint(20) NOT NULL default '0', 
   `hp` int(11) NOT NULL default '0',
   `exp` bigint(20) NOT NULL default '0',
   `level` int(5) NOT NULL default '0',
   `quality` int(3) NOT NULL default '0',
   `savvy` int(5) NOT NULL default '0',
   `job` int(3) DEFAULT NULL default '0',
   `mergedLevel` int(5) NOT NULL default '0',
   `mergedTime` bigint(20) NOT NULL default '0',
   `mergedBlessPercent` int(5) NOT NULL default '0',
   `mergedBless` int(5) NOT NULL default '0',
   `fighting` int(11) NOT NULL default '0',
   PRIMARY KEY (`petId`),
   KEY `IDX_PETID_FIGHTING` (`petId`,`fighting`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
-- ----------------------------
-- Table structure for userProps
-- ----------------------------
DROP TABLE IF EXISTS `userProps`;
CREATE TABLE `userProps` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backpack` int(11) NOT NULL default '0',
  `baseId` int(11) NOT NULL default '0',
  `count` int(11) NOT NULL default '0',
  `positionId` int(11) DEFAULT NULL default '0',
  `quality` int(11) DEFAULT NULL default '0',
  `binding` int(5) NOT NULL default '0',
  `discardTime` datetime DEFAULT NULL,
  `expiration` datetime DEFAULT NULL,
  `playerId` bigint(20) NOT NULL default '0',
  PRIMARY KEY (`id`),
  KEY `IDX_PLAYERID_BACKPACK` (`backpack`,`playerId`),
  KEY `IDX_PLAYERID_BACKPACK_COUNT` (`backpack`,`count`,`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for userSkill
-- ----------------------------
DROP TABLE IF EXISTS `userSkill`;
CREATE TABLE `userSkill` (
  `playerId` bigint(20) NOT NULL default '0',
  `activeSkill` blob NULL COMMENT '主动技能',
  `passiveSkill` blob NULL COMMENT '被动技能',
  `skillLevels` int(11) NOT NULL default '0',
  PRIMARY KEY (`playerId`),
  KEY `IDX_PLAYERID_SKILLLEVELS` (`playerId`, `skillLevels`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for userTask
-- ----------------------------
DROP TABLE IF EXISTS `userTask`;
CREATE TABLE `userTask` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `chain` int(11) NOT NULL default '0',
  `playerId` bigint(20) NOT NULL default '0',
  `status` int(11) NOT NULL default '0',
  `taskEvent` blob NULL,
  `taskId` int(11) NOT NULL default '0',
  PRIMARY KEY (`id`),
  KEY `IDX_PLAYERID` (`playerId`),
  KEY `IDX_PLAYERID_CHAIN` (`chain`,`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for userTrain
-- ----------------------------
DROP TABLE IF EXISTS `userTrain`;
CREATE TABLE `userTrain` (
  `playerId` bigint(20) NOT NULL default '0',
  `received` int(5) NOT NULL default '0',
  `startTime` bigint(20) NOT NULL default '0',
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for registerStatistic
-- ----------------------------
DROP TABLE IF EXISTS `registerStatistic`;
CREATE TABLE `registerStatistic` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `campKnife` int(11) NOT NULL,
  `campNone` int(11) NOT NULL,
  `campSword` int(11) NOT NULL,
  `jobTianlong` int(11) NOT NULL,
  `jobTianshan` int(11) NOT NULL,
  `jobXiaoyao` int(11) NOT NULL,
  `jobXingxiu` int(11) NOT NULL,
  `recordDate` varchar(255) DEFAULT NULL,
  `recordTime` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_RECORD_DATE` (`recordDate`),
  KEY `IDX_RECORD_DATE_TIME` (`recordDate`,`recordTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for campOnline
-- ----------------------------
DROP TABLE IF EXISTS `campOnline`;
CREATE TABLE `campOnline` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `recordDate` varchar(255) DEFAULT NULL,
  `recordTime` varchar(255) DEFAULT NULL,
  `knifeOnline` int(11) NOT NULL,
  `knifeTotal` int(11) NOT NULL,
  `swordOnline` int(11) NOT NULL,
  `swordTotal` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for UserOnlineGift
-- ----------------------------
DROP TABLE IF EXISTS `userOnlineGift`;
CREATE TABLE `userOnlineGift` (
  `playerId` bigint(20) NOT NULL default '0',
  `onlineGiftId` int(11) NOT NULL default '0',
  `cleanDate` int(11) NOT NULL DEFAULT '0',
  `onlineTime` bigint(20) DEFAULT '0',
  `openTime` datetime DEFAULT NULL,
  `endTime` datetime DEFAULT NULL,
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for userCampTask
-- ----------------------------
DROP TABLE IF EXISTS `userCampTask`;
CREATE TABLE `userCampTask` (
   `playerId` bigint(20) NOT NULL default '0',
   `level` int(3) NOT NULL DEFAULT '0' COMMENT '阵营任务等级',
   `startTime` bigint(20) NOT NULL default '0' COMMENT '开始时间',
   `progresstask` longtext COLLATE utf8_unicode_ci COMMENT '正在处理中的人阵营任务',
   `rewardstask` longtext COLLATE utf8_unicode_ci COMMENT '已经领取过奖励的阵营任务',
   PRIMARY KEY (`playerId`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
 
-- ----------------------------
-- Table structure for userPracticeTask
-- ----------------------------
DROP TABLE IF EXISTS `userPracticeTask`;
CREATE TABLE `userPracticeTask` (
  `playerId` bigint(20) NOT NULL default '0' COMMENT '角色ID',
  `amount` int(11) NOT NULL default '0' COMMENT '任务剩余的完成数量',
  `completes` int(11) NOT NULL default '0' COMMENT '任务的完成次数',
  `conditions` bigint(20) DEFAULT NULL default '0' COMMENT '任务的完成条件',
  `dayOfWeek` int(11) NOT NULL default '0' COMMENT '任务所属于的一年后总的第几天',
  `quality` int(11) NOT NULL default '0' COMMENT '任务的品质',
  `rewardInfo` varchar(512) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '领取任务的条件',
  `status` int(11) NOT NULL default '0' COMMENT '任务的状态',
  `taskLevel` int(11) NOT NULL default '0' COMMENT '接任务时候的角色的等级',
  `taskParams` varchar(512) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '任务的条件参数',
  `totalAmount` int(11) NOT NULL default '0' COMMENT '完成任务的总条件',
  `type` int(11) NOT NULL,
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

DROP TABLE IF EXISTS `userTreasure`;
CREATE TABLE `userTreasure` (
  `playerId` bigint(20) NOT NULL,
  `mapId` int(11) NOT NULL default '0',
  `propsId` int(11) NOT NULL default '0',
  `rewardId` int(11) NOT NULL default '0',
  `quality` int(11) NOT NULL default '0',
  `userPropsId` bigint(20) NOT NULL,
  `x` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  `status` int(11) DEFAULT NULL,
  `openedTreasure` longtext COLLATE utf8_unicode_ci COMMENT '已经开启的藏宝图',
  `openBox` varchar(255) COLLATE utf8_unicode_ci COMMENT '已经打开的藏宝图宝箱',
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `userMail`;
CREATE TABLE `userMail` (
  `playerId` BIGINT(20) NOT NULL DEFAULT '0',
  `mailIds` blob NULL,
  `received` blob NULL,
  `mailState` blob NULL,
  `delMails` blob NULL,
  PRIMARY KEY (`playerId`)
) ENGINE=INNODB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


DROP TABLE IF EXISTS `mail`;
CREATE TABLE `mail` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `senderId` BIGINT(20) NOT NULL DEFAULT '0',
  `silverRewards` BIGINT(20) NOT NULL DEFAULT '0',
  `goldenRewards` BIGINT(20) NOT NULL DEFAULT '0',
  `title` VARCHAR(255) COLLATE utf8_unicode_ci DEFAULT '',
  `propsRewards` VARCHAR(255) COLLATE utf8_unicode_ci DEFAULT '',
  `equipRewards` VARCHAR(255) COLLATE utf8_unicode_ci DEFAULT '',
  `content` longtext NOT NULL DEFAULT '',
  `sendTime` datetime DEFAULT NULL,
  `endTime` datetime DEFAULT NULL,
  `conditions` VARCHAR(255) COLLATE utf8_unicode_ci DEFAULT '',
  `mailType` int(5) NOT NULL DEFAULT '0',
  `couponRewards` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


DROP TABLE IF EXISTS `playerCampBattleHistory`;
CREATE TABLE `playerCampBattleHistory` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `battleDate` date DEFAULT NULL,
  `bossHurtHP` int(11) NOT NULL,
  `campTitle` int(11) DEFAULT NULL,
  `hurtBossScores` int(11) NOT NULL,
  `killPlayers` int(11) NOT NULL,
  `playerId` bigint(20) DEFAULT NULL,
  `scores` int(11) NOT NULL,
  `status` int(11) DEFAULT NULL,
  `salaryReward` date DEFAULT NULL,
  `suitReward` bit(1) DEFAULT NULL,
  `camp` int(11) DEFAULT NULL,
  `level` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `IDX_BATTLEDATE_CAMP` (`battleDate`,`camp`),
  KEY `IDX_PLAYERID_CAMP` (`playerId`,`camp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `campBattleHistory`;
CREATE TABLE `campBattleHistory` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `battleDate` date DEFAULT NULL,
  `bossHurtHP` int(11) NOT NULL,
  `camp` int(11) DEFAULT NULL,
  `killPlayers` int(11) NOT NULL,
  `ownPoints` varchar(255) DEFAULT NULL,
  `pointScores` int(11) NOT NULL,
  `scores` int(11) NOT NULL,
  `win` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_BATTLEDATE_CAMP` (`battleDate`,`camp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `userGift`;
CREATE TABLE `userGift` (
  `playerId` bigint(20) NOT NULL DEFAULT '0',
  `receivedGiftId` longtext NOT NULL DEFAULT '',
  `receiveTime` datetime DEFAULT NULL,
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `dailyRecord`;
CREATE TABLE `dailyRecord` (
  `playerId` bigint(20) NOT NULL,
  `campBattleReward` longtext,
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `playerBattleField`;
CREATE TABLE `playerBattleField` (
  `playerId` bigint(20) NOT NULL,
  `battleDate` datetime DEFAULT NULL,
  `collectHonor` int(11) DEFAULT '0',
  `deaths` int(11) DEFAULT '0',
  `fightHonor` int(11) DEFAULT '0',
  `killPlayers` int(11) DEFAULT '0',
  `preBattleDate` datetime DEFAULT NULL,
  `preTotalHonor` int(11) DEFAULT '0',
  `rewardDate` datetime DEFAULT NULL,
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `monsterInfo`;
CREATE TABLE `monsterInfo` (
  `branch` int(11) NOT NULL,
  `resurrection` longtext,
  PRIMARY KEY (`branch`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for rechargeRecord
-- ----------------------------
DROP TABLE IF EXISTS `rechargeRecord`;
CREATE TABLE `rechargeRecord` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `playerId` bigint(20) NOT NULL DEFAULT '0' COMMENT '色角ID',
  `recharge` blob,
  `recordTime` date NOT NULL COMMENT '记录的时间. yyyy-MM-dd.',
  `totalRecharge` bigint(20) NOT NULL DEFAULT '0' COMMENT '单日累积充值金额',
  PRIMARY KEY (`id`),
  KEY `IDX_PLAYERID_RECORDTIME` (`playerId`,`recordTime`),
  KEY `IDX_PLAYERID` (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for rechargeGift
-- ----------------------------
DROP TABLE IF EXISTS `rechargeGift`;
CREATE TABLE `rechargeGift` (
  `playerId` bigint(20) NOT NULL DEFAULT '0' COMMENT '角色的ID',
  `rewardInfo` blob COMMENT '已领取奖励的信息',
  PRIMARY KEY (`playerId`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for userAchieve
-- ----------------------------
DROP TABLE IF EXISTS `userAchieve`;
CREATE TABLE `userAchieve` (
  `playerId` bigint(20) NOT NULL,
  `achieves` varchar(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `achieved` longtext COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for playerBuyLimit
-- ----------------------------
DROP TABLE IF EXISTS `playerBuyLimit`;
CREATE TABLE `playerBuyLimit` (
  `playerId` bigint(20) NOT NULL,
  `goodsBuyCount` longtext COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for playerBuyLimit
-- ----------------------------
DROP TABLE IF EXISTS `userLevelStatistic`;
CREATE TABLE `userLevelStatistic` (
  `recordDate` varchar(30) NOT NULL,
  `data` blob NULL,
  PRIMARY KEY (`recordDate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for registerDetailStatistic
-- ----------------------------
DROP TABLE IF EXISTS `registerDetailStatistic`;
CREATE TABLE `registerDetailStatistic` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loginCount` bigint(20) NOT NULL default '0',
  `userCount` int(10) NOT NULL default '0',
  `createCount` int(10) NOT NULL default '0',
  `startTime` datetime DEFAULT NULL,
  `endTime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_REGISTER_DETAIL_STATISTIC_RECORDTIME` (`startTime`,`endTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



