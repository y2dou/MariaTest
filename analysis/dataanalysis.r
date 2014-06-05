# CairoWin(width = 7, height = 4) # set output device
# CairoWin(width = 7, height = 8, pointsize=16)
# CairoFonts

#setwd("D:/Raymond/repastS-1.2.0-workspace/MariaPrototype/analysis")   # set to base folder of project
#setwd("C:/Users/arcabrer/repastS-workspace/MariaPrototype/analysis")
setwd("~/Documents/eclipse-workspace/MariaPrototype/analysis")
classpath = paste(getwd(), "../lib/mysql-connector-java-5.1.7-bin.jar", sep="/")
drv <- JDBC("com.mysql.jdbc.Driver", classpath)
conn <- dbConnect(drv, "jdbc:mysql://localhost/maria", "maria", "mariaprototype")

CairoPDF(file="result-legend.pdf", width = 5, height = 0.5, pointsize=8)
landuse.legend(horiz=TRUE,box.lwd=0.6)
dev.off()

# analyze two runs, single household each

# Decision Method comparison




# parameter sweep: effect on some output variable, like capital or # of multisited households or acai harvest

####################################
# constant price scenario

CairoPDF(file="result-decisionmethodpair.pdf", width = 5.7, height = 4, pointsize=10)
econTrajectoryPair(1296,1297,5,6,ylim1=c(0,150),ylim2=c(0,150), labour=FALSE, title1="Optimizing\n(Linear Programming)", title2="Heuristic\n(Decision Tree)", capital=TRUE, singleTrajectory=TRUE)
dev.off()

CairoPDF(file="result-decisionmethodpair-composite.pdf", width = 5.7, height = 2.9, pointsize=10)
econTrajectoryPair(1296,1297,5,6,ylim1=c(0,150),ylim2=c(0,150), labour=FALSE, title1="Optimizing\n(Linear Programming)", title2="Heuristic\n(Decision Tree)", ylab="tick", singleTrajectory=FALSE, composite=TRUE, capital=FALSE)
dev.off()

Cairo(file="result-constantprice-sweepcapital.pdf", width = 5.7, height = 4.2, units="in", type="pdf", pointsize=8, family="Helvetica Neue")
sensitivityScatterplotPair("constantprice_sensitivity_20090723", "acaiPrice", "capital", xlab="acai price", title1="Optimizing\n(Linear Programming)", title2="Heuristic\n(Decision Tree)", cex=0.005, use.lowess=TRUE)
dev.off()

Cairo(file="result-constantprice-montecarlo.pdf", width = 5.7, height = 4.2, units="in", type="pdf", pointsize=8, family="Helvetica Neue")
sensitivityScatterplotPair("constantprice_montecarlo_20090805", "runID", "capital", title1="Optimizing\n(Linear Programming)", title2="Heuristic\n(Decision Tree)", cex=0.005, use.lowess=TRUE)
dev.off()

#d[order(d$acaiPrice),] # use this to identify break in trend

econTrajectoryPair(1296,1297,3,5,ylim1=c(0,150),ylim2=c(0,150), title1="acaiPrice = 3.33e-04", title2="acaiPrice = 3.36e-04", composite=TRUE)

Cairo(file="result-constantprice-abovebreakeven.pdf", width = 5.7, height = 3, units="in", type="pdf", pointsize=8, family="Helvetica Neue")
sensitivityScatterplotPair("constantprice_sensitivity_20090723", "acaiPrice", "capital",xlim=c(0.0003345,0.000418125),xlab="acai price",ylab="capital",cex=0.005, use.lowess=TRUE)
dev.off()

# land use sensitivity plots

# should graph this on a log scale
#sensitivityScatterplotPair("constantprice_sensitivity_20090708-2", "acaiPrice", "maniocgarden", xlim=c(0.0003345,0.000418125),xlab="acai price",ylab="# of manioc cells")
#sensitivityScatterplotPair("constantprice_sensitivity_20090708-2", "acaiPrice", "harvestmanioc")

# or should this be graphed on a stacked area chart?
#sensitivityScatterplotPair("constantprice_sensitivity_20090708-2", "acaiPrice", "acai")

Cairo(file="result-constantprice-abovebreakeven-composite.pdf", width = 5.7, height = 3.6, units="in", type="pdf", pointsize=10)
econTrajectoryPair(1421,1423,3,5,ylim1=c(0,150),ylim2=c(0,150), title1="Optimizing\n(Linear Programming)", title2="Heuristic\n(Decision Tree)", composite=TRUE,capital=FALSE,labour=FALSE,singleTrajectory=FALSE)
dev.off()

# graph land use trajectories, one line per house (distinguished by line type) per land use (colour)


#sensitivityScatterplotPair("constantprice_sensitivity_20090708-2", "acaiPrice", "harvestacai")

#################################
# variable acai price scenario
Cairo(file="result-decisionmethodpair-variable.pdf", width = 5.7, height = 3.2, units="in", type="pdf", pointsize=10, family="Helvetica Neue")
econTrajectoryPair(1413,1415,3,4,ylim1=c(0,150),ylim2=c(0,150), labour=FALSE, title1="Optimizing\n(Linear Programming)", title2="Heuristic\n(Decision Tree)")
dev.off()

Cairo(file="result-decisionmethodpair-variable-composite.pdf", width = 5.7, height = 2.8, units="in", type="pdf", pointsize=10, family="Helvetica Neue")
econTrajectoryPair(1413,1415,3,4,ylim1=c(0,150),ylim2=c(0,150), labour=FALSE, title1="Optimizing\n(Linear Programming)", title2="Heuristic\n(Decision Tree)", singleTrajectory=FALSE, composite=TRUE, capital=FALSE)
dev.off()

# sensitivity analysis

Cairo(file="result-variableprice-montecarlo.pdf", width = 5.7, height = 4.2, units="in", type="pdf", pointsize=8, family="Helvetica Neue")
sensitivityScatterplotPair("variableprice_montecarlo_20090805", "runID", "capital", cex=0.005, use.lowess=TRUE)
dev.off()

Cairo(file="result-variableprice-sweepcapital.pdf", width = 5.7, height = 3.8, units="in", type="pdf", pointsize=8, family="Helvetica Neue")
sensitivityScatterplotPair("variableprice_sensitivity_price_20090723-4", "acaiMultiplier", "capital", xlab="price scaling factor", ylab="capital", title1="Optimizing\n(Linear Programming)", title2="Heuristic\n(Decision Tree)", use.lowess=TRUE)
dev.off()

# examine difference on either side of singularity
Cairo(file="result-variableprice-sweepcapital-2runcomparison.pdf", width = 7, height = 5.25, units="in", type="pdf", pointsize=12, family="Helvetica Neue")
econTrajectoryPair(1506,1546,3,4,ylim1=c(0,225),ylim2=c(0,225), labour=FALSE, title1="price scaling factor = 5.75e^-06", title2="price scaling factor = 1.75e^-05", singleTrajectory=FALSE, composite=TRUE, capital=TRUE)
dev.off()

Cairo(file="result-variableprice-sweepcapital-2runcomparison-capitallabour", width = 7, height = 4, units="in", type="pdf", pointsize=14, family="Helvetica Neue")
op=par(mfrow = c(1,2),cex=0.5,pch=19)
d.variable.belowconvergence = dbGetQuery(conn, 'SELECT *, (acai+maniocgarden+forest+fallow) AS total FROM tblhouseholdstate t WHERE runID=? and tick=40 ORDER BY labour',1506)
plot(capital~labour,data=d.variable.belowconvergence, main="price scaling factor = 5.75e^-06")
lines(lowess(d.variable.belowconvergence$labour,d.variable.belowconvergence$capital),col="red",lwd=2)

d.variable.aboveconvergence = dbGetQuery(conn, 'SELECT *, (acai+maniocgarden+forest+fallow) AS total FROM tblhouseholdstate t WHERE runID=? and tick=40 ORDER BY labour',1546)
plot(capital~labour,data=d.variable.aboveconvergence, main="price scaling factor = 1.75e^-05")
lines(lowess(d.variable.aboveconvergence$labour,d.variable.aboveconvergence$capital),col="red",lwd=2)
par(op)
dev.off()

# examine very successful decision tree agents


###################################
# multi-sited households: urban employment
Cairo(file="result-decisionmethodpair-multisited.pdf", width = 5.7, height = 4.2, units="in", type="pdf", pointsize=12, family="Helvetica Neue")
econTrajectoryPair(1913,1915,10,5,ylim1=c(0,120),ylim2=c(0,120), title1="Optimizing\n(Linear Programming)", title2="Heuristic\n(Decision Tree)", labour=TRUE)
dev.off()

Cairo(file="result-decisionmethodpair-multisited-composite.pdf", width = 5.7, height = 3.8, units="in", type="pdf", pointsize=10, family="Helvetica Neue")
econTrajectoryPair(1913,1915,10,5,ylim1=c(0,150),ylim2=c(0,150), title1="Optimizing\n(Linear Programming)", title2="Heuristic\n(Decision Tree)", labour=FALSE,singleTrajectory=FALSE,capital=FALSE,composite=TRUE)
dev.off()

Cairo(file="result-migration-multisited-composite.pdf", width = 5.7, height = 3.2, units="in", type="pdf", pointsize=8, family="Helvetica Neue")
op = par(mfrow=c(1,2))
tmp <- linkedHouseholdsNumComposite(1913,main="Optimizing\n(Linear Programming)")
tmp <- linkedHouseholdsNumComposite(1915,main="Heuristic\n(Decision Tree)")
rm(tmp)
dev.off()

#sensitivityScatterplotPair("constantprice_sensitivity_offervalue50_lambdaoffers_20090723", "lambdaOffers", "capital", xlab="mean offer interarrival time (years)", ylab="capital", xtransform = function(x) { 1 / x }, log="x")

#sensitivityScatterplotPair("variableprice_sensitivity_offervalueaverage_20090723", "offerValueAverage", "capital", xlab="average offer value", ylab="capital",xlim=c(0,400),ylim=c(7000,40000),use.lowess=TRUE)
#sensitivityScatterplotPair("variableprice_sensitivity_offervalueaverage_20090723", "offerValueAverage", "linkedhouseholds", xlab="average offer value", ylab="capital")



# sensitivity analysis
Cairo(file="result-multisited-sweepcapital-10offers.pdf", width = 5.7, height = 3, units="in", type="pdf", pointsize=8, family="Helvetica Neue")
sensitivityScatterplotPair("variableprice_sensitivity_10offers_offervalueaverage_20090725-1", "offerValueAverage", "capital", ylim=c(7500,15500), xlab="average offer value", ylab="capital", title1="Optimizing\n(Linear Programming)", title2="Heuristic\n(Decision Tree)",use.lowess=TRUE)
dev.off()

Cairo(file="result-multisited-sweepcapital-20offers.pdf", width = 5.7, height = 3, units="in", type="pdf", pointsize=8, family="Helvetica Neue")
sensitivityScatterplotPair("variableprice_sensitivity_20offers_offervalueaverage_20090725-1", "offerValueAverage", "capital", ylim=c(7500,15500), xlab="average offer value", ylab="capital", title1="Optimizing\n(Linear Programming)", title2="Heuristic\n(Decision Tree)",use.lowess=TRUE)
dev.off()

Cairo(file="result-multisited-sweepcapital-offervalue4.pdf", width = 5.7, height = 3, units="in", type="pdf", pointsize=8, family="Helvetica Neue")
sensitivityScatterplotPair("variableprice_sensitivity_offervalue4_lambdaoffers_20090725-1", "lambdaOffers", "capital", ylim=c(6000,13000), xlab="average offer arrival rate", ylab="capital", title1="Optimizing\n(Linear Programming)", title2="Heuristic\n(Decision Tree)",use.lowess=TRUE)
dev.off()

Cairo(file="result-multisited-sweepcapital-offervalue10.pdf", width = 5.7, height = 3, units="in", type="pdf", pointsize=8, family="Helvetica Neue")
sensitivityScatterplotPair("variableprice_sensitivity_offervalue10_lambdaoffers_20090725-1", "lambdaOffers", "capital", ylim=c(6000,13000), xlab="average offer arrival rate", ylab="capital", title1="Optimizing\n(Linear Programming)", title2="Heuristic\n(Decision Tree)",use.lowess=TRUE)
dev.off()


# economic disparity
Cairo(file="result-multisited-sweepcapital-10offers-boxplot.pdf", width = 5.7, height = 3, units="in", type="pdf", pointsize=8, family="Helvetica Neue")
sensitivityBoxplotPair("variableprice_sensitivity_10offers_offervalueaverage_20090725-1", "offerValueAverage", "capital", xlim=c(0,28), xlab="average offer value", ylab="capital", lwd=0.05, pars=list(outpch=19,outcex=0.08,outcol="red",boxlwd=0.6,boxcol="darkgrey",whisklwd=0.6), xtransform=round)
dev.off()

Cairo(file="result-multisited-sweepcapital-20offers-boxplot.pdf", width = 5.7, height = 3, units="in", type="pdf", pointsize=8, family="Helvetica Neue")
sensitivityBoxplotPair("variableprice_sensitivity_20offers_offervalueaverage_20090725-1", "offerValueAverage", "capital", xlim=c(0,28), xlab="average offer value", ylab="capital", lwd=0.05, pars=list(outpch=19,outcex=0.08,outcol="red",boxlwd=0.6,boxcol="darkgrey",whisklwd=0.6), xtransform=round)
dev.off()

Cairo(file="result-multisited-sweepcapital-offervalue10-boxplot.pdf", width = 5.7, height = 3, units="in", type="pdf", pointsize=8, family="Helvetica Neue")
sensitivityBoxplotPair("variableprice_sensitivity_offervalue10_lambdaoffers_20090725-1", "lambdaOffers", "capital", xlim=c(0,21), xlab="average offer arrival rate", ylab="capital", title1="Optimizing\n(Linear Programming)", title2="Heuristic\n(Decision Tree)", lwd=0.05, pars=list(outpch=19,outcex=0.08,outcol="red",boxlwd=0.6,boxcol="darkgrey",whisklwd=0.6), xtransform=round)
dev.off()


# cluster plot or KDE these: better yet, just put them in a table
sensitivityScatterplotPair("variableprice_sensitivity_10offers_offervalueaverage_20090724-1", "offerValueAverage", "links", xlab="average offer value", ylab="capital")
sensitivityScatterplotPair("variableprice_sensitivity_20offers_offervalueaverage_20090724-1", "offerValueAverage", "links", xlab="average offer value", ylab="capital")

# number of offers
sensitivityScatterplotPair("variableprice_sensitivity_offervalue50_lambdaoffers_20090723-6", "lambdaOffers", "capital", xlab="mean offer interarrival time (years)", ylab="capital", xtransform = function(x) { 1 / x }, use.lowess=TRUE)
sensitivityScatterplotPair("variableprice_sensitivity_offervalue100_lambdaoffers_20090723-6", "lambdaOffers", "capital", xlab="mean offer interarrival time (years)", ylab="capital", xtransform = function(x) { 1 / x }, use.lowess=TRUE)
sensitivityScatterplotPair("variableprice_sensitivity_offervalue200_lambdaoffers_20090723-2", "lambdaOffers", "capital", xlab="mean offer interarrival time (years)", ylab="capital", xtransform = function(x) { 1 / x }, use.lowess=TRUE)

# migration trends



###########################################################################
# presentation slides
landUseTrajectoryComposite(2724, axes=FALSE, xlab="tick")
d.household <- linkedHouseholdsNumComposite(2724)

# transform this to something excel could use
d.household.data <- data.frame(row.names=unique(d.household$tick)[order(unique(d.household$tick))])
#d.household.data <- cbind(unique(d.household$tick)[order(unique(d.household$tick))])

for (i in unique(d.household$sourceHousehold)) {
  d.household.data <- cbind(d.household.data,d.household[d.household$sourceHousehold==i,]$links)
}
names(d.household.data) <- unique(d.household$sourceHousehold)

#row.names(d.household.data) <- c("tick",unique(d.household$sourceHousehold))
row.names(d.household.data) <- unique(d.household$tick)[order(unique(d.household$tick))]
write.csv(d.household.data,file="linkedhouseholds.csv")


###########################################################################


###################################
# sensitivity to timber prices, ease of deforestation




####################################
# design a market bust such that acai fails: where's the price break now?


##########################################################################
# experimental section






d <- dbGetQuery(conn, "call avghouseholdstate(?)", runID)

#select labourMultiplier, capitalMultiplier, acaiMultiplier, maniocMultiplier, timberMultiplier, avg(capital)
#from tblRun left outer join tblHouseholdState on tblRun.runID = tblHouseholdState.runID
#group by labourMultiplier, capitalMultiplier, acaiMultiplier, maniocMultiplier, timberMultiplier
#where tick=38 and labourMultiplier=0.01 and capitalMultiplier=0.01 and acaiMultiplier=0.01 and maniocMultiplier=0.01 and timberMultiplier=0.01

# find magic numbers which balance average capital, long run

# compare average ending capital with parameters
d <- dbGetQuery(conn, "select * from endingAverageCapitalLabour")
#d <- dbGetQuery(conn, "call getAvgHouseholdState(38, 1,1,1,1,1)")

# look at land use trajectories from different scenarios
d.trajectory <- dbGetQuery(conn, "select * from tblrun inner join avghouseholdState using runid")

# restrict resultset to fixed parameters
d.labour <- subset(d, capitalMultiplier == 1 & acaiMultiplier == 1 & maniocMultiplier == 1 & timberMultiplier == 1)
plot(avg.capital. ~ labourMultiplier, data=d.labour, log="x", xlab="Labour multiplier", ylab="Average Household Capital")
plot(avg.labour. ~ labourMultiplier, data=d.labour, log="x", xlab="Labour multiplier", ylab="Average EOY Remaining Household Labour")

d.capital <- subset(d, labourMultiplier == 1 & acaiMultiplier == 1 & maniocMultiplier == 1 & timberMultiplier == 1)
plot(avg.capital. ~ capitalMultiplier, data=d.capital, log="x", xlab="Capital multiplier", ylab="Average Household Capital")
plot(avg.labour. ~ capitalMultiplier, data=d.capital, log="x", xlab="Capital multiplier", ylab="Average EOY Remaining Household Labour")

d.acai <- subset(d, capitalMultiplier == 1 & labourMultiplier == 1 & maniocMultiplier == 1 & timberMultiplier == 1)
plot(avg.capital. ~ acaiMultiplier, data=d.acai, log="xy", xlab="Acai multiplier", ylab="Average Household Capital")
plot(avg.labour. ~ acaiMultiplier, data=d.acai, log="x", xlab="Acai multiplier", ylab="Average EOY Remaining Household Labour")

d.manioc <- subset(d, capitalMultiplier == 1 & labourMultiplier == 1 & acaiMultiplier == 1 & timberMultiplier == 1)
plot(avg.capital. ~ maniocMultiplier, data=d.manioc, log="xy", xlab="Manioc multiplier", ylab="Average Household Capital")
plot(avg.labour. ~ maniocMultiplier, data=d.manioc, log="x", xlab="Manioc multiplier", ylab="Average EOY Remaining Household Labour")

d.timber <- subset(d, capitalMultiplier == 1 & labourMultiplier == 1 & maniocMultiplier == 1 & acaiMultiplier == 1)
plot(avg.capital. ~ timberMultiplier, data=d.timber, log="xy", xlab="Timber multiplier", ylab="Average Household Capital")
plot(avg.labour. ~ timberMultiplier, data=d.timber, log="x", xlab="Timber multiplier", ylab="Average EOY Remaining Household Labour")


#d.capitallabour <- subset(d, acaiMultiplier == 0.01 & maniocMultiplier == 1 & timberMultiplier == 1)

# see the trajectories more closely
d.trajectory.scenario1 <- subset(d.trajectory, labourMultiplier = 1 & capitalMultiplier = 1 & acaiMultiplier = 0.01 & maniocMultiplier = 0.01 & timberMultiplier = 0.01)
plot(avgcapital ~ tick, d.trajectory.scenario1)
stackpoly(subset(d.trajectory.scenario, select=), stack=TRUE, axis4=FALSE, col=colors.landuse, ylim=c(0,1), border="black")


dbDisconnect(conn)
