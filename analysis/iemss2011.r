# iemss

library(RJDBC)
library(Cairo)

names.landuse = c("forest", "fallow", "acai", "garden", "other")
colors.landuse = c("#154229", "#CC723E", "#54CC3E", "#FF534C", "#99867B")
hashdensities.landuse = c(4,4,4,4,12)
hashangles.landuse = c(45,90,135,180,270)
xlim = c(1,40)

# setup
setwd("/Users/raymond/Documents/eclipse-workspace/MariaPrototype/analysis")
drv <- JDBC("com.mysql.jdbc.Driver","../lib/mysql-connector-java-5.1.13-bin.jar")
conn <- dbConnect(drv, "jdbc:mysql://localhost/maria", "maria", "mariaprototype")

# load functions
source('iemss-presentation-fun.r')

setwd("/Users/raymond/Documents/AIRS/Papers/iEMSs special issue/figures")

CairoPDF(file="result-legend.pdf", width = 2.8, height = 0.3, pointsize=8)
op=par(mar=c(0,0,0,0))
landuse.legend(horiz=TRUE,box.lwd=0.6,cex=1)
par(op)
dev.off()

#windowsFonts(sans=windowsFont("Calibri"))

# figure set 1

# pick households which match in terms of labour availability:
# select runid from tblrun where sweepname = 'constantprice_iemss2011';
# select * from tblhouseholdstate where tick = 1 and runid >= 3277 and runid <= 3280 order by labour;

# figure 1: constant price scenario
CairoPDF(file="result-decisionmethodpair-constant1.pdf", width = 5.7, height = 2.8, pointsize=10,family="sans")
econTrajectoryPair(3931,3932,7,4,ylim1=c(0,150),ylim2=c(0,150), labour=FALSE, capital=TRUE, capital.ylim=c(0,14000), singleTrajectory=TRUE)
dev.off()

CairoPDF(file="result-decisionmethodpair-constant2.pdf", width = 5.7, height = 2.8, pointsize=10,family="sans")
econTrajectoryPair(3933,3934,14,9,ylim1=c(0,150),ylim2=c(0,150), labour=FALSE, capital=TRUE, capital.ylim=c(0,14000), singleTrajectory=TRUE)
dev.off()

# figure 2a: variable price scenario; 0 discount rate
CairoPDF(file="result-decisionmethodpair-variable1.pdf", width = 5.7, height = 2.8, pointsize=10,family="sans")
econTrajectoryPair(3725,3726,19,16,ylim1=c(0,150),ylim2=c(0,150), labour=FALSE, xlim=c(0,40), capital.ylim=c(0,16000))
dev.off()

CairoPDF(file="result-decisionmethodpair-variable2.pdf", width = 5.7, height = 2.8, pointsize=10,family="sans")
econTrajectoryPair(3727,3728,17,0,ylim1=c(0,150),ylim2=c(0,150), labour=FALSE, xlim=c(0,40), capital.ylim=c(0,16000))
dev.off()

# low price multiplier (thesis; 0.000009)
CairoPDF(file="result-decisionmethodpair-variable1.pdf", width = 5.7, height = 2.8, pointsize=10,family="sans")
econTrajectoryPair(3927,3928,6,11,ylim1=c(0,150),ylim2=c(0,150), labour=FALSE, xlim=c(30,38), capital.ylim=c(0,16000))
dev.off()

CairoPDF(file="result-decisionmethodpair-variable2.pdf", width = 5.7, height = 2.8, pointsize=10,family="sans")
econTrajectoryPair(3929,3930,14,17,ylim1=c(0,150),ylim2=c(0,150), labour=FALSE, xlim=c(30,38), capital.ylim=c(0,16000))
dev.off()

# threshold: price multiplier = 1.4 (0.0000126)
CairoPDF(file="result-decisionmethodpair-sensitivity1-variable1.pdf", width = 5.7, height = 2.8, pointsize=10,family="sans")
econTrajectoryPair(3983,3984,6,1,ylim1=c(0,150),ylim2=c(0,150), labour=FALSE, xlim=c(32,38), capital.ylim=c(0,16000))
dev.off()

CairoPDF(file="result-decisionmethodpair-sensitivity1-variable2.pdf", width = 5.7, height = 2.8, pointsize=10,family="sans")
econTrajectoryPair(3985,3986,4,5,ylim1=c(0,150),ylim2=c(0,150), labour=FALSE, xlim=c(32,38), capital.ylim=c(0,16000))
dev.off()

# threshold: price multiplier = 1.45 (0.000013225)
CairoPDF(file="result-decisionmethodpair-sensitivity2-variable1.pdf", width = 5.7, height = 2.8, pointsize=10,family="sans")
econTrajectoryPair(3987,3988,17,10,ylim1=c(0,150),ylim2=c(0,150), labour=FALSE, xlim=c(32,38), capital.ylim=c(0,16000))
dev.off()

CairoPDF(file="result-decisionmethodpair-sensitivity2-variable2.pdf", width = 5.7, height = 2.8, pointsize=10,family="sans")
econTrajectoryPair(3989,3990,8,9,ylim1=c(0,150),ylim2=c(0,150), labour=FALSE, xlim=c(32,38), capital.ylim=c(0,16000))
dev.off()



# high price multiplier (0.000015)
CairoPDF(file="result-decisionmethodpair-variable1.pdf", width = 5.7, height = 2.8, pointsize=10,family="sans")
econTrajectoryPair(3839,3840,2,1,ylim1=c(0,150),ylim2=c(0,150), labour=FALSE, xlim=c(32,38), capital.ylim=c(0,16000))
dev.off()

CairoPDF(file="result-decisionmethodpair-variable2.pdf", width = 5.7, height = 2.8, pointsize=10,family="sans")
econTrajectoryPair(3841,3842,20,6,ylim1=c(0,150),ylim2=c(0,150), labour=FALSE, xlim=c(32,38), capital.ylim=c(0,16000))
dev.off()





Cairo(file="result-decisionmethodpair-multisited1.pdf", width = 5.7, height = 3.8, units="in", type="pdf", pointsize=10,family="sans")
econTrajectoryPair(3509,3510,3,17,ylim1=c(0,120),ylim2=c(0,120), title1="LP", title2="LP: Simple forecast", xlim=c(25,35), capital.ylim=c(0,16000), labour=TRUE)
dev.off()

Cairo(file="result-decisionmethodpair-multisited2.pdf", width = 5.7, height = 3.8, units="in", type="pdf", pointsize=10,family="sans")
econTrajectoryPair(3511,3512,10,17,ylim1=c(0,120),ylim2=c(0,120), title1="LP: Evaluated forecast", title2="Heuristics", xlim=c(25,35), capital.ylim=c(0,16000), labour=TRUE)
dev.off()



Cairo(file="result-migration-multisited-composite.pdf", width = 5.7, height = 2.4, units="in", type="pdf", pointsize=8,family="sans")
op = par(mfrow=c(1,2), mar=c(0, 4, 0, 2) + 0.1, oma=c(1,0,3,0))
tmp <- linkedHouseholdsNumComposite(1913,main="",ylim=c(0,10))
tmp <- linkedHouseholdsNumComposite(1915,main="",ylim=c(0,10))
rm(tmp)
dev.off()


# sensitivity analyses
CairoPDF(file="result-sensitivity-variable.pdf", width = 11.4, height = 3.2, pointsize=10,family="sans")
sensitivityScatterplotPair('variableprice_sensitivity_price_iemss2011','acaiMultiplier','capital',xlab="price multiplier",tick=35,percent.of=c(0.0015,100), use.lowess=TRUE, title1="LP", title2="LP: Simple Forecasting", title3="LP: Evaluative Forecasting", title4="Heuristic")
dev.off()

CairoPDF(file="result-sensitivity-offervalue.pdf", width = 11.4, height = 3.2, pointsize=10,family="sans")
sensitivityScatterplotPair('variableprice_sensitivity_20offers_offervalueaverage_iemss2011','offerValueAverage','capital', tick=35, use.lowess=TRUE, title1="LP", title2="LP: Simple Forecasting", title3="LP: Evaluative Forecasting", title4="Heuristic", xlab="average wage")
dev.off()

CairoPDF(file="result-sensitivity-numberofoffers.pdf", width = 11.4, height = 3.2, pointsize=10,family="sans")
#sensitivityScatterplotPair('variableprice_sensitivity_offervalue100_lambdaoffers_iemss2011','lambdaOffers','capital', tick=35, use.lowess=TRUE, xtransform=function(x){1/x}, title1="LP", title2="LP: Simple Forecasting", title3="LP: Evaluative Forecasting", title4="Heuristic", xlab="average years between offers (1/λ)")
sensitivityScatterplotPair('variableprice_sensitivity_offervalue100_lambdaoffers3_iemss2011','lambdaOffers','capital', tick=35, use.lowess=TRUE, title1="LP", title2="LP: Simple Forecasting", title3="LP: Evaluative Forecasting", title4="Heuristic", xlab="average offers per year (λ)", xlim=c(0,1.2))
dev.off()


# multisited households: investigate why (migration trajectory) 
