//need to run tblrun first//
CREATE TABLE `tblhousehold` (
  `householdID` int(11) NOT NULL DEFAULT '0',
  `runID` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`householdID`,`runID`),
  KEY `runID` (`runID`),
  CONSTRAINT `tblhousehold_ibfk_1` FOREIGN KEY (`runID`) REFERENCES `tblrun` (`runID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `tblhouseholdaction` (
  `actionID` int(11) NOT NULL AUTO_INCREMENT,
  `householdID` int(11) NOT NULL,
  `runID` int(11) NOT NULL,
  `tick` double NOT NULL,
  `stage` varchar(12) NOT NULL,
  `actionName` varchar(12) NOT NULL,
  PRIMARY KEY (`actionID`),
  KEY `householdID` (`householdID`,`runID`),
  CONSTRAINT `tblhouseholdaction_ibfk_1` FOREIGN KEY (`householdID`, `runID`) REFERENCES `tblhousehold` (`householdID`, `runID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `tblhouseholdmembers` (
  `householdID` int(11) NOT NULL,
  `runID` int(11) NOT NULL,
  `tick` double NOT NULL,
  `stage` varchar(12) NOT NULL,
  `memberID` int(11) NOT NULL,
  `age` int(11) NOT NULL,
  `isFemale` bit(1) NOT NULL,
  PRIMARY KEY (`householdID`,`runID`,`tick`,`stage`,`memberID`),
  KEY `runID` (`runID`),
  CONSTRAINT `tblhouseholdmembers_ibfk_1` FOREIGN KEY (`runID`) REFERENCES `tblrun` (`runID`) ON DELETE CASCADE,
  CONSTRAINT `tblhouseholdmembers_ibfk_2` FOREIGN KEY (`householdID`, `runID`) REFERENCES `tblhousehold` (`householdID`, `runID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `tblhouseholdstate` (
  `householdID` int(11) NOT NULL,
  `runID` int(11) NOT NULL,
  `tick` double NOT NULL,
  `stage` varchar(12) NOT NULL,
  `capital` double NOT NULL,
  `labour` double NOT NULL,
  `cashTran` double NOT NULL,
  `acai` int(11) NOT NULL,
  `maniocgarden` int(11) NOT NULL,
  `fields` int(11) NOT NULL,
  `forest` int(11) NOT NULL,
  `fallow` int(11) NOT NULL,
  `other` int(11) NOT NULL,
  `harvestacai` double NOT NULL,
  `harvestmanioc` double NOT NULL,
  `harvesttimber` double NOT NULL,
  PRIMARY KEY (`householdID`,`runID`,`tick`,`stage`) USING BTREE,
  KEY `runID` (`runID`),
  KEY `idxTick` (`tick`),
  CONSTRAINT `tblhouseholdstate_ibfk_1` FOREIGN KEY (`runID`) REFERENCES `tblrun` (`runID`) ON DELETE CASCADE,
  CONSTRAINT `tblhouseholdstate_ibfk_2` FOREIGN KEY (`householdID`, `runID`) REFERENCES `tblhousehold` (`householdID`, `runID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `tblland` (
  `cellid` int(11) NOT NULL,
  `runid` int(11) NOT NULL,
  `yeardeforested` double NOT NULL DEFAULT '-9999',
  `owner` int(11) DEFAULT NULL,
  PRIMARY KEY (`cellid`,`runid`),
  KEY `FK_tblland_1` (`runid`),
  KEY `FK_tblland_2` (`owner`,`runid`),
  CONSTRAINT `FK_tblland_1` FOREIGN KEY (`runid`) REFERENCES `tblrun` (`runID`) ON DELETE CASCADE,
  CONSTRAINT `FK_tblland_2` FOREIGN KEY (`owner`, `runid`) REFERENCES `tblhousehold` (`householdID`, `runID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `tblrun` (
  `runID` int(11) NOT NULL AUTO_INCREMENT,
  `labourMultiplier` double NOT NULL,
  `capitalMultiplier` double NOT NULL,
  `cashTransfer` double NOT NULL,
  `acaiMultiplier` double NOT NULL,
  `maniocMultiplier` double NOT NULL,
  `timberMultiplier` double NOT NULL,
  `acaiLabour` double NOT NULL DEFAULT '0.1',
  `fallowLabour` double NOT NULL DEFAULT '0.05',
  `maniocLabour` double NOT NULL DEFAULT '0.1',
  `acaiCost` double NOT NULL DEFAULT '10',
  `maniocCost` double NOT NULL DEFAULT '100',
  `fallowCost` double NOT NULL DEFAULT '50',
  `maintainAcaiLabour` double NOT NULL DEFAULT '0.01',
  `maintainAcaiCost` double NOT NULL DEFAULT '5',
  `maintainManiocLabour` double NOT NULL DEFAULT '0.04',
  `maintainManiocCost` double NOT NULL DEFAULT '10',
  `rundate` datetime DEFAULT NULL,
  `forestFallowLabour` double NOT NULL DEFAULT '0',
  `forestFallowCost` double NOT NULL DEFAULT '0',
  `harvestAcaiLabour` double NOT NULL DEFAULT '0',
  `harvestManiocLabour` double NOT NULL DEFAULT '0',
  `harvestTimberLabour` double NOT NULL DEFAULT '0',
  `percentHeuristicHouseholds` double NOT NULL DEFAULT '1',
  `percentOptimalHouseholds` double NOT NULL DEFAULT '0',
  `percentForwardOptimalHouseholds` double NOT NULL DEFAULT '0',
  `percentFullForwardOptimalHouseholds` double NOT NULL DEFAULT '0',
  `numHouseholds` int(11) NOT NULL DEFAULT '20',
  `numPersons` int(11) NOT NULL DEFAULT '144',
  `numOffers` int(11) NOT NULL DEFAULT '5',
  `acaiPrice` double NOT NULL DEFAULT '-1',
  `maniocPrice` double NOT NULL DEFAULT '-1',
  `timberPrice` double NOT NULL DEFAULT '-1',
  `sweepName` varchar(128) DEFAULT NULL,
  `offerValueLow` double NOT NULL DEFAULT '1000000',
  `offerValueHigh` double NOT NULL DEFAULT '10000000',
  `lambdaOffers` double NOT NULL DEFAULT '0',
  `randomSeed` int(11) DEFAULT NULL,
  PRIMARY KEY (`runID`),
  KEY `Index_SweepName` (`sweepName`)
) ENGINE=InnoDB AUTO_INCREMENT=2759 DEFAULT CHARSET=latin1;

CREATE TABLE `tblurbanagent` (
  `urbanagentid` int(10) NOT NULL,
  `runid` int(10) NOT NULL,
  `settlementyear` double NOT NULL,
  `leavingyear` double DEFAULT NULL,
  `leavingstage` varchar(45) DEFAULT NULL,
  `age` int(10) NOT NULL,
  `isfemale` tinyint(1) NOT NULL,
  `wage` int(10) NOT NULL,
  `employer` varchar(45) NOT NULL,
  `sourceHousehold` int(11) NOT NULL,
  PRIMARY KEY (`urbanagentid`,`runid`),
  KEY `runid` (`runid`),
  KEY `FK_tblurbanagent_2` (`sourceHousehold`,`runid`),
  CONSTRAINT `FK_tblurbanagent_2` FOREIGN KEY (`sourceHousehold`, `runid`) REFERENCES `tblhousehold` (`householdID`, `runID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `tblurbanagent_ibfk_1` FOREIGN KEY (`runid`) REFERENCES `tblrun` (`runID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `tblurbanagentstate` (
  `urbanagentid` int(11) NOT NULL,
  `runid` int(11) NOT NULL,
  `tick` double NOT NULL,
  `capital` double NOT NULL,
  `stage` varchar(45) NOT NULL,
  PRIMARY KEY (`urbanagentid`,`runid`,`tick`,`stage`) USING BTREE,
  CONSTRAINT `FK_tblurbanagentstate_1` FOREIGN KEY (`urbanagentid`, `runid`) REFERENCES `tblurbanagent` (`urbanagentid`, `runid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
