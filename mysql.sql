CREATE TABLE IF NOT EXISTS tblRun (
runID INT AUTO_INCREMENT,
labourMultiplier DOUBLE NOT NULL,
capitalMultiplier DOUBLE NOT NULL,
acaiMultiplier DOUBLE NOT NULL,
maniocMultiplier DOUBLE NOT NULL,
timberMultiplier DOUBLE NOT NULL,
acaiLabour DOUBLE NOT NULL,
maniocLabour DOUBLE NOT NULL,
timberLabour DOUBLE NOT NULL,
PRIMARY KEY (runID) );

CREATE TABLE IF NOT EXISTS tblHousehold (
householdID INT,
runID INT,
PRIMARY KEY (householdID, runID),
FOREIGN KEY (runID) REFERENCES tblRun(runID) ON DELETE CASCADE );

CREATE TABLE IF NOT EXISTS tblHouseholdState (
householdID INT NOT NULL,
runID INT NOT NULL,
tick DOUBLE NOT NULL,
stage VARCHAR(12) NOT NULL,
capital DOUBLE NOT NULL,
labour DOUBLE NOT NULL,
acai INT NOT NULL,
maniocgarden INT NOT NULL,
fields INT NOT NULL,
forest INT NOT NULL,
fallow INT NOT NULL,
other INT NOT NULL,
PRIMARY KEY (householdID, runID, tick, stage),
FOREIGN KEY (runID) REFERENCES tblRun (runID) ON DELETE CASCADE,
FOREIGN KEY (householdID, runID) REFERENCES tblHousehold(householdID, runID) ON DELETE CASCADE );

CREATE TABLE IF NOT EXISTS tblHouseholdMembers (
householdID INT NOT NULL,
runID INT NOT NULL,
tick DOUBLE NOT NULL,
stage VARCHAR(12) NOT NULL,
memberID INT NOT NULL,
age INT NOT NULL,
isFemale BIT NOT NULL,
PRIMARY KEY (householdID, runID, tick, stage, memberID),
FOREIGN KEY (runID) REFERENCES tblRun (runID) ON DELETE CASCADE,
FOREIGN KEY (householdID, runID) REFERENCES tblHousehold(householdID, runID) ON DELETE CASCADE );

CREATE TABLE IF NOT EXISTS tblHouseholdAction (
actionID INT AUTO_INCREMENT PRIMARY KEY,
householdID INT NOT NULL,
runID INT NOT NULL,
tick DOUBLE NOT NULL,
stage VARCHAR(12) NOT NULL,
actionName VARCHAR(12) NOT NULL,
FOREIGN KEY (householdID, runID) REFERENCES tblHousehold(householdID, runID) ON DELETE CASCADE );

CREATE TABLE IF NOT EXISTS tblHouseholdNetwork (
runID INT,
sourceHousehold INT,
targetHousehold INT,
weight DOUBLE NOT NULL,
PRIMARY KEY (runID, sourceHousehold, targetHousehold),
FOREIGN KEY (sourceHousehold, runID) REFERENCES tblHousehold(householdID, runID) ON DELETE CASCADE,
FOREIGN KEY (targetHousehold, runID) REFERENCES tblHousehold(householdID, runID) ON DELETE CASCADE );

CREATE INDEX idxTick ON tblHouseholdState (tick);
CREATE INDEX idxCapital ON tblHouseholdState (capital);
CREATE INDEX idxLabour ON tblHouseholdState (labour);
CREATE INDEX idxAcai ON tblHouseholdState (acai);
CREATE INDEX idxManioc ON tblHouseholdState (maniocgarden);
CREATE INDEX idxTimber ON tblHouseholdState (timber);
CREATE INDEX idxFallow ON tblHouseholdState (fallow);
CREATE INDEX idxFields ON tblHouseholdState (fields);
CREATE INDEX idxForest ON tblHouseholdState (forest);
CREATE INDEX idxOther ON tblHouseholdState (other);
CREATE INDEX avgCovering ON tblHouseholdState (capital, labour, acai, maniocgarden, timber, fallow, fields, forest, other);

DROP VIEW IF EXISTS `endinghouseholdstate`;
CREATE OR REPLACE ALGORITHM=UNDEFINED DEFINER=`maria`@`%` SQL SECURITY DEFINER VIEW `endinghouseholdstate` AS select `tblrun`.`labourMultiplier` AS `labourMultiplier`,`tblrun`.`capitalMultiplier` AS `capitalMultiplier`,`tblrun`.`acaiMultiplier` AS `acaiMultiplier`,`tblrun`.`maniocMultiplier` AS `maniocMultiplier`,`tblrun`.`timberMultiplier` AS `timberMultiplier`,`tblhouseholdstate`.`labour` AS `labour`,`tblhouseholdstate`.`capital` AS `capital` from (`tblrun` left join `tblhouseholdstate` on((`tblrun`.`runID` = `tblhouseholdstate`.`runID`))) where (`tblhouseholdstate`.`tick` = 38);

DROP VIEW IF EXISTS `endingaveragecapitallabour`;
CREATE OR REPLACE ALGORITHM=UNDEFINED DEFINER=`maria`@`%` SQL SECURITY DEFINER VIEW `endingaveragecapitallabour` AS select `endinghouseholdstate`.`labourMultiplier` AS `labourMultiplier`,`endinghouseholdstate`.`capitalMultiplier` AS `capitalMultiplier`,`endinghouseholdstate`.`acaiMultiplier` AS `acaiMultiplier`,`endinghouseholdstate`.`maniocMultiplier` AS `maniocMultiplier`,`endinghouseholdstate`.`timberMultiplier` AS `timberMultiplier`,avg(`endinghouseholdstate`.`labour`) AS `avg(labour)`,avg(`endinghouseholdstate`.`capital`) AS `avg(capital)` from `endinghouseholdstate` group by `endinghouseholdstate`.`labourMultiplier`,`endinghouseholdstate`.`capitalMultiplier`,`endinghouseholdstate`.`acaiMultiplier`,`endinghouseholdstate`.`maniocMultiplier`,`endinghouseholdstate`.`timberMultiplier`;

DROP VIEW IF EXISTS `avghouseholdstate`;
CREATE OR REPLACE ALGORITHM=UNDEFINED DEFINER=`maria`@`%` SQL SECURITY DEFINER VIEW `avghouseholdstate` AS select `tblhouseholdstate`.`runID` AS `runID`,labourmultiplier AS labourmultiplier, capitalmultiplier AS capitalmultiplier, acaimultiplier as acaimultiplier, maniocmultiplier AS maniocmultiplier, timbermultiplier, `tblhouseholdstate`.`tick` AS `tick`,avg(`tblhouseholdstate`.`capital`) AS `avgcapital`,avg(`tblhouseholdstate`.`labour`) AS `avglabour`,avg(`tblhouseholdstate`.`acai`) AS `avgacai`,avg(`tblhouseholdstate`.`maniocgarden`) AS `avgmanioc`,avg(`tblhouseholdstate`.`fields`) AS `avgfields`,avg(`tblhouseholdstate`.`forest`) AS `avgforest`,avg(`tblhouseholdstate`.`fallow`) AS `avgfallow`,avg(`tblhouseholdstate`.`other`) AS `avgother` from tblrun inner join `tblhouseholdstate` using (runid) group by `tblhouseholdstate`.`runID`,`tblhouseholdstate`.`tick`;

DELIMITER $$

DROP PROCEDURE IF EXISTS `MARIA`.`getAvgHouseholdTrajectory` $$
CREATE PROCEDURE `MARIA`.`getAvgHouseholdTrajectory` (IN in_labourMultiplier DOUBLE, IN in_capitalMultiplier DOUBLE, IN in_acaiMultiplier DOUBLE, IN in_maniocMultiplier DOUBLE, IN in_timberMultiplier DOUBLE)
BEGIN
	SELECT tick, avg(capital) AS avgcapital, avg(labour) AS avglabour, avg(acai) AS avgacai, avg(maniocgarden) AS avgmaniocgarden, avg(forest) AS avgforest, avg(fields) AS avgfields, avg(fallow) AS avgfallow, avg(other) AS avgother FROM tblHouseholdState WHERE runID =
  (
    SELECT runID 
    FROM tblRun 
    WHERE labourMultiplier = in_labourMultiplier AND capitalMultiplier = in_capitalMultiplier AND acaiMultiplier = in_acaiMultiplier AND timberMultiplier = in_timberMultiplier AND maniocMultiplier = in_maniocMultiplier 
    LIMIT 1
  )
;
END $$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS `MARIA`.`getAvgHouseholdState` $$
CREATE PROCEDURE `MARIA`.`getAvgHouseholdState` (IN in_tick INT, IN in_labourMultiplier DOUBLE, IN in_capitalMultiplier DOUBLE, IN in_acaiMultiplier DOUBLE, IN in_maniocMultiplier DOUBLE, IN in_timberMultiplier DOUBLE)
BEGIN
	SELECT tick, avg(capital) AS avgcapital, avg(labour) AS avglabour, avg(acai) AS avgacai, avg(maniocgarden) AS avgmaniocgarden, avg(forest) AS avgforest, avg(fields) AS avgfields, avg(fallow) AS avgfallow, avg(other) AS avgother FROM tblHouseholdState WHERE runID =
  (
    SELECT runID 
    FROM tblRun 
    WHERE labourMultiplier = in_labourMultiplier AND capitalMultiplier = in_capitalMultiplier AND acaiMultiplier = in_acaiMultiplier AND timberMultiplier = in_timberMultiplier AND maniocMultiplier = in_maniocMultiplier
    LIMIT 1
  ) AND tick = in_tick
;
END $$

DELIMITER ;

GRANT INSERT ON *.* TO 'maria'@'localhost';
