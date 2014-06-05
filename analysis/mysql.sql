-- MySQL dump 10.13  Distrib 5.1.31, for Win32 (ia32)
--
-- Host: localhost    Database: maria
-- ------------------------------------------------------
-- Server version	5.1.31-community

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Temporary table structure for view `avghouseholdstate`
--

DROP TABLE IF EXISTS `avghouseholdstate`;
/*!50001 DROP VIEW IF EXISTS `avghouseholdstate`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE TABLE `avghouseholdstate` (
  `runID` int(11),
  `tick` double,
  `avgcapital` double,
  `avglabour` double,
  `avgacai` decimal(14,4),
  `avgmanioc` decimal(14,4),
  `avgfields` decimal(14,4),
  `avgforest` decimal(14,4),
  `avgfallow` decimal(14,4),
  `avgother` decimal(14,4)
) ENGINE=MyISAM */;
SET character_set_client = @saved_cs_client;

--
-- Temporary table structure for view `endingaveragecapitallabour`
--

DROP TABLE IF EXISTS `endingaveragecapitallabour`;
/*!50001 DROP VIEW IF EXISTS `endingaveragecapitallabour`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE TABLE `endingaveragecapitallabour` (
  `runID` int(11),
  `labourMultiplier` double,
  `capitalMultiplier` double,
  `acaiMultiplier` double,
  `maniocMultiplier` double,
  `timberMultiplier` double,
  `acaiLabour` double,
  `maniocLabour` double,
  `fallowLabour` double,
  `forestFallowLabour` double,
  `acaiCost` double,
  `maniocCost` double,
  `fallowCost` double,
  `forestFallowCost` double,
  `maintainAcaiLabour` double,
  `maintainManiocLabour` double,
  `maintainAcaiCost` double,
  `maintainManiocCost` double,
  `harvestAcaiLabour` double,
  `harvestManiocLabour` double,
  `harvestTimberLabour` double,
  `avg(labour)` double,
  `avg(capital)` double
) ENGINE=MyISAM */;
SET character_set_client = @saved_cs_client;

--
-- Temporary table structure for view `endinghouseholdstate`
--

DROP TABLE IF EXISTS `endinghouseholdstate`;
/*!50001 DROP VIEW IF EXISTS `endinghouseholdstate`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE TABLE `endinghouseholdstate` (
  `runID` int(11),
  `labourMultiplier` double,
  `capitalMultiplier` double,
  `acaiMultiplier` double,
  `maniocMultiplier` double,
  `timberMultiplier` double,
  `acaiLabour` double,
  `maniocLabour` double,
  `fallowLabour` double,
  `forestFallowLabour` double,
  `acaiCost` double,
  `maniocCost` double,
  `fallowCost` double,
  `forestFallowCost` double,
  `maintainAcaiLabour` double,
  `maintainManiocLabour` double,
  `maintainAcaiCost` double,
  `maintainManiocCost` double,
  `harvestAcaiLabour` double,
  `harvestManiocLabour` double,
  `harvestTimberLabour` double,
  `householdID` int(11),
  `labour` double,
  `capital` double
) ENGINE=MyISAM */;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `tblhousehold`
--

DROP TABLE IF EXISTS `tblhousehold`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `tblhousehold` (
  `householdID` int(11) NOT NULL DEFAULT '0',
  `runID` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`householdID`,`runID`),
  KEY `runID` (`runID`),
  CONSTRAINT `tblhousehold_ibfk_1` FOREIGN KEY (`runID`) REFERENCES `tblrun` (`runID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `tblhouseholdaction`
--

DROP TABLE IF EXISTS `tblhouseholdaction`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
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
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `tblhouseholdnetwork`
--

DROP TABLE IF EXISTS `tblhouseholdnetwork`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `tblhouseholdnetwork` (
  `runID` int(11) NOT NULL DEFAULT '0',
  `sourceHousehold` int(11) NOT NULL DEFAULT '0',
  `targetHousehold` int(11) NOT NULL DEFAULT '0',
  `weight` double NOT NULL,
  PRIMARY KEY (`runID`,`sourceHousehold`,`targetHousehold`),
  KEY `sourceHousehold` (`sourceHousehold`,`runID`),
  KEY `targetHousehold` (`targetHousehold`,`runID`),
  CONSTRAINT `tblhouseholdnetwork_ibfk_1` FOREIGN KEY (`sourceHousehold`, `runID`) REFERENCES `tblhousehold` (`householdID`, `runID`) ON DELETE CASCADE,
  CONSTRAINT `tblhouseholdnetwork_ibfk_2` FOREIGN KEY (`targetHousehold`, `runID`) REFERENCES `tblurbanagent` (`urbanagentid`, `runid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `tblhouseholdstate`
--

DROP TABLE IF EXISTS `tblhouseholdstate`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `tblhouseholdstate` (
  `householdID` int(11) NOT NULL,
  `runID` int(11) NOT NULL,
  `tick` double NOT NULL,
  `stage` varchar(12) NOT NULL,
  `capital` double NOT NULL,
  `labour` double NOT NULL,
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
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `tblland`
--

DROP TABLE IF EXISTS `tblland`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
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
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `tblrun`
--

DROP TABLE IF EXISTS `tblrun`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `tblrun` (
  `runID` int(11) NOT NULL AUTO_INCREMENT,
  `labourMultiplier` double NOT NULL,
  `capitalMultiplier` double NOT NULL,
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
  `numHouseholds` int(11) NOT NULL DEFAULT '20',
  `numPersons` int(11) NOT NULL DEFAULT '144',
  `numOffers` int(11) NOT NULL DEFAULT '5',
  `acaiPrice` double NOT NULL DEFAULT '-1',
  `maniocPrice` double NOT NULL DEFAULT '-1',
  `timberPrice` double NOT NULL DEFAULT '-1',
  PRIMARY KEY (`runID`)
) ENGINE=InnoDB AUTO_INCREMENT=18446 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `tblurbanagent`
--

DROP TABLE IF EXISTS `tblurbanagent`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
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
  PRIMARY KEY (`urbanagentid`,`runid`),
  KEY `runid` (`runid`),
  CONSTRAINT `tblurbanagent_ibfk_1` FOREIGN KEY (`runid`) REFERENCES `tblrun` (`runID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `tblurbanagentstate`
--

DROP TABLE IF EXISTS `tblurbanagentstate`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `tblurbanagentstate` (
  `urbanagentid` int(11) NOT NULL,
  `runid` int(11) NOT NULL,
  `tick` double NOT NULL,
  `capital` double NOT NULL,
  `stage` varchar(45) NOT NULL,
  PRIMARY KEY (`urbanagentid`,`runid`,`tick`,`stage`) USING BTREE,
  CONSTRAINT `FK_tblurbanagentstate_1` FOREIGN KEY (`urbanagentid`, `runid`) REFERENCES `tblurbanagent` (`urbanagentid`, `runid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Final view structure for view `avghouseholdstate`
--

/*!50001 DROP TABLE `avghouseholdstate`*/;
/*!50001 DROP VIEW IF EXISTS `avghouseholdstate`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`maria`@`%` SQL SECURITY DEFINER */
/*!50001 VIEW `avghouseholdstate` AS select `tblhouseholdstate`.`runID` AS `runID`,`tblhouseholdstate`.`tick` AS `tick`,avg(`tblhouseholdstate`.`capital`) AS `avgcapital`,avg(`tblhouseholdstate`.`labour`) AS `avglabour`,avg(`tblhouseholdstate`.`acai`) AS `avgacai`,avg(`tblhouseholdstate`.`maniocgarden`) AS `avgmanioc`,avg(`tblhouseholdstate`.`fields`) AS `avgfields`,avg(`tblhouseholdstate`.`forest`) AS `avgforest`,avg(`tblhouseholdstate`.`fallow`) AS `avgfallow`,avg(`tblhouseholdstate`.`other`) AS `avgother` from `tblhouseholdstate` group by `tblhouseholdstate`.`runID`,`tblhouseholdstate`.`tick` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `endingaveragecapitallabour`
--

/*!50001 DROP TABLE `endingaveragecapitallabour`*/;
/*!50001 DROP VIEW IF EXISTS `endingaveragecapitallabour`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`maria`@`%` SQL SECURITY DEFINER */
/*!50001 VIEW `endingaveragecapitallabour` AS select `endinghouseholdstate`.`runID` AS `runID`,`endinghouseholdstate`.`labourMultiplier` AS `labourMultiplier`,`endinghouseholdstate`.`capitalMultiplier` AS `capitalMultiplier`,`endinghouseholdstate`.`acaiMultiplier` AS `acaiMultiplier`,`endinghouseholdstate`.`maniocMultiplier` AS `maniocMultiplier`,`endinghouseholdstate`.`timberMultiplier` AS `timberMultiplier`,`endinghouseholdstate`.`acaiLabour` AS `acaiLabour`,`endinghouseholdstate`.`maniocLabour` AS `maniocLabour`,`endinghouseholdstate`.`fallowLabour` AS `fallowLabour`,`endinghouseholdstate`.`forestFallowLabour` AS `forestFallowLabour`,`endinghouseholdstate`.`acaiCost` AS `acaiCost`,`endinghouseholdstate`.`maniocCost` AS `maniocCost`,`endinghouseholdstate`.`fallowCost` AS `fallowCost`,`endinghouseholdstate`.`forestFallowCost` AS `forestFallowCost`,`endinghouseholdstate`.`maintainAcaiLabour` AS `maintainAcaiLabour`,`endinghouseholdstate`.`maintainManiocLabour` AS `maintainManiocLabour`,`endinghouseholdstate`.`maintainAcaiCost` AS `maintainAcaiCost`,`endinghouseholdstate`.`maintainManiocCost` AS `maintainManiocCost`,`endinghouseholdstate`.`harvestAcaiLabour` AS `harvestAcaiLabour`,`endinghouseholdstate`.`harvestManiocLabour` AS `harvestManiocLabour`,`endinghouseholdstate`.`harvestTimberLabour` AS `harvestTimberLabour`,avg(`endinghouseholdstate`.`labour`) AS `avg(labour)`,avg(`endinghouseholdstate`.`capital`) AS `avg(capital)` from `endinghouseholdstate` group by `endinghouseholdstate`.`runID`,`endinghouseholdstate`.`labourMultiplier`,`endinghouseholdstate`.`capitalMultiplier`,`endinghouseholdstate`.`acaiMultiplier`,`endinghouseholdstate`.`maniocMultiplier`,`endinghouseholdstate`.`timberMultiplier`,`endinghouseholdstate`.`acaiLabour`,`endinghouseholdstate`.`maniocLabour`,`endinghouseholdstate`.`fallowLabour`,`endinghouseholdstate`.`forestFallowLabour`,`endinghouseholdstate`.`acaiCost`,`endinghouseholdstate`.`maniocCost`,`endinghouseholdstate`.`fallowCost`,`endinghouseholdstate`.`forestFallowCost`,`endinghouseholdstate`.`maintainAcaiLabour`,`endinghouseholdstate`.`maintainManiocLabour`,`endinghouseholdstate`.`maintainAcaiCost`,`endinghouseholdstate`.`maintainManiocCost`,`endinghouseholdstate`.`harvestAcaiLabour`,`endinghouseholdstate`.`harvestManiocLabour`,`endinghouseholdstate`.`harvestTimberLabour` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `endinghouseholdstate`
--

/*!50001 DROP TABLE `endinghouseholdstate`*/;
/*!50001 DROP VIEW IF EXISTS `endinghouseholdstate`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`maria`@`%` SQL SECURITY DEFINER */
/*!50001 VIEW `endinghouseholdstate` AS select `tblrun`.`runID` AS `runID`,`tblrun`.`labourMultiplier` AS `labourMultiplier`,`tblrun`.`capitalMultiplier` AS `capitalMultiplier`,`tblrun`.`acaiMultiplier` AS `acaiMultiplier`,`tblrun`.`maniocMultiplier` AS `maniocMultiplier`,`tblrun`.`timberMultiplier` AS `timberMultiplier`,`tblrun`.`acaiLabour` AS `acaiLabour`,`tblrun`.`maniocLabour` AS `maniocLabour`,`tblrun`.`fallowLabour` AS `fallowLabour`,`tblrun`.`forestFallowLabour` AS `forestFallowLabour`,`tblrun`.`acaiCost` AS `acaiCost`,`tblrun`.`maniocCost` AS `maniocCost`,`tblrun`.`fallowCost` AS `fallowCost`,`tblrun`.`forestFallowCost` AS `forestFallowCost`,`tblrun`.`maintainAcaiLabour` AS `maintainAcaiLabour`,`tblrun`.`maintainManiocLabour` AS `maintainManiocLabour`,`tblrun`.`maintainAcaiCost` AS `maintainAcaiCost`,`tblrun`.`maintainManiocCost` AS `maintainManiocCost`,`tblrun`.`harvestAcaiLabour` AS `harvestAcaiLabour`,`tblrun`.`harvestManiocLabour` AS `harvestManiocLabour`,`tblrun`.`harvestTimberLabour` AS `harvestTimberLabour`,`tblhouseholdstate`.`householdID` AS `householdID`,`tblhouseholdstate`.`labour` AS `labour`,`tblhouseholdstate`.`capital` AS `capital` from (`tblrun` left join `tblhouseholdstate` on((`tblrun`.`runID` = `tblhouseholdstate`.`runID`))) where (`tblhouseholdstate`.`tick` = 38) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2009-07-06 20:14:19
