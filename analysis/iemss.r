# iemss

library(RJDBC)
library(Cairo)

names.landuse = c("forest", "fallow", "açaí", "garden", "other")
colors.landuse = c("#154229", "#CC723E", "#54CC3E", "#FF534C", "#99867B")
hashdensities.landuse = c(4,4,4,4,12)
hashangles.landuse = c(45,90,135,180,270)
xlim = c(1,40)

# setup
setwd("C:/Users/arcabrer/repastS-workspace/MariaPrototype/analysis")
drv <- JDBC("com.mysql.jdbc.Driver","../lib/mysql-connector-java-5.1.7-bin.jar")
conn <- dbConnect(drv, "jdbc:mysql://localhost/maria", "maria", "mariaprototype")

setwd("C:/Users/arcabrer/Documents/AIRD/Papers/iemss/figures")

CairoPDF(file="result-legend.pdf", width = 2.8, height = 0.3, pointsize=8)
op=par(mar=c(0,0,0,0))
landuse.legend(horiz=TRUE,box.lwd=0.6,cex=1)
par(op)
dev.off()

# figure set 1

# figure 1
windowsFonts(sans=windowsFont("Calibri"))
CairoPDF(file="result-decisionmethodpair.pdf", width = 5.7, height = 2.8, pointsize=10,family="sans")
econTrajectoryPair(1296,1297,5,6,ylim1=c(0,150),ylim2=c(0,150), labour=FALSE, capital=TRUE, capital.ylim=c(0,12000), singleTrajectory=TRUE)
dev.off()

# thick line
CairoPDF(file="result-decisionmethodpair-variable.pdf", width = 5.7, height = 2.8, pointsize=10,family="sans")
econTrajectoryPair(1413,1415,3,4,ylim1=c(0,150),ylim2=c(0,150), labour=FALSE, capital.ylim=c(0,12000))
dev.off()

Cairo(file="result-decisionmethodpair-multisited.pdf", width = 5.7, height = 3.8, units="in", type="pdf", pointsize=10,family="sans")
econTrajectoryPair(1913,1915,10,5,ylim1=c(0,120),ylim2=c(0,120), title1="Optimizing\n(Linear Programming)", title2="Heuristic\n(Decision Tree)", capital.ylim=c(0,12000), labour=TRUE)
dev.off()

Cairo(file="result-migration-multisited-composite.pdf", width = 5.7, height = 2.4, units="in", type="pdf", pointsize=8,family="sans")
op = par(mfrow=c(1,2), mar=c(0, 4, 0, 2) + 0.1, oma=c(1,0,3,0))
tmp <- linkedHouseholdsNumComposite(1913,main="",ylim=c(0,10))
tmp <- linkedHouseholdsNumComposite(1915,main="",ylim=c(0,10))
rm(tmp)
dev.off()
