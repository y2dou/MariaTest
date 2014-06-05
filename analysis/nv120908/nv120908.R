###############################################
# Some basic statistical analysis of the survey data codified by Nathan Vogt
# on September 12, 2008.
#
# Analysis explores the differences between communities and possibly the drivers
# of acai cultivation and selling.
#
# Raymond Cabrera
# September 16, 2008


# Questions that need to be answered:
#
# distribution of land in upland, varzea
#   how much land in capoeira? acai?
# distribution of income in upland, varzea

# factors influencing acai cultivation

###############################################
# Roças       fields
# Quintais    (quintals) housegarden
# Capoeira    forest-fallow (varzea), fallow (upland)
# Mata        forest

# Income codes:
# 1)  acai
# 2)  agriculture (could be any crops like corn, manioc, beans, etc.)
# 3)  retirement (type of social security) or other benefits (disabled people, etc.
# 4)  bolsa familia is a program to support the poorest of families, a type of welfare and also incentive to send children to school rather than asking to work for the household 
# 5)  Fishing
# 6)  Commerce, self employed (merchant who buys/sells  - can be the local fruit or fish market, transporting goods along the rivers between rural community and urban center, etc.)
# 7)  Employed laborer in private sector - can be hired for daily work (clearning a field, harvesting acai) or monthly (house cleaner, shopkeeper, etc)
# 8)  Employed laborer for public sector (government employee) - can be a school teacher, mayors office, public works, etc.


###############################################
# Data Preparation
###############################################

setwd("C:/Users/arcabrer/repastS-workspace/MariaPrototype/analysis/nv120908")
source("nv120908_header.R")


###############################################
# Exploratory Analysis
###############################################

# family size
d.family.hist = hist(d.paricatuba$personsRural, breaks=seq(from=0,to=20))

# characterize the surveyed communities

summary(d$community)
par(mfrow=c(1,1))
plot(d$isOwner ~ d$community, main = "Are owners of the lot", col=color.yesno, xlab="Community", ylab="%")
plot(d$lotsize ~ d$community, main = "Lot size by community", xlab="Community", ylab="Lot Size")
plot(d$income1 ~ d$community, main = "Primary income by community", 
  col=color.income, xlab="Community", ylab="Income Type")
legend(x="bottomleft",legend=c(1,2,3,4,5,6,7,8,99),fill=color.income,bg="white")

############################################################################
# communities by biophsyical attributes (upland vs. floodplain)
par(mfrow=c(2,1))
plot(d$totalUplandHoldings ~ d$community, main = "Total upland holdings by community")
plot(d$totalRiverineHoldings ~ d$community, main = "Total riverine holdings by community")


par(mfrow=c(1,1))
plot(d$percentUplandHoldings ~ d$community, main = "Percent upland holdings by community")
plot(d$percentRiverineHoldings ~ d$community, main = "Percent riverine holdings by community")


#############################################################################
# Buyers of acai
plot(d.paricatuba$acaiBuyer,col=color.buyer, main="Buyers of Acai")

plot(d$acaiBuyer ~ d$community, main = "Buyers of Acai by community", col=color.buyer)
legend(x="topleft",legend=levels(d$acaiBuyer),fill=color.buyer,bg="white")

##############################################################################
# Land use distribution

# plot distribution in varzea
plot(d$biophysical ~ d$percentRoca)

# roca (housegarden)
par(mfrow=c(2,1))
hist(d.upland.strict$percentRoca)
hist(d.riverine.strict$percentRoca)

# quintais
par(mfrow=c(2,1))
hist(d.upland.strict$percentQuintais)
hist(d.riverine.strict$percentQuintais)

# capoeira
par(mfrow=c(2,1))
hist(d.upland.strict$percentCapoeira)
hist(d.riverine.strict$percentCapoeira)

# mata
par(mfrow=c(2,1))
hist(d.upland.strict$percentMata)
hist(d.riverine.strict$percentMata)

# acai
par(mfrow=c(1,1))
hist(d.riverine.strict$kgPerHaAcai)

#############################################################################
# primary income distribution
# primary questionnaire-based biophysical identification
income = table(d$income1,d$biophysical)
income[,1] = income[,1] / sum(income[,1])
income[,2] = income[,2] / sum(income[,2])

barplot(income,col=color.income,main="Primary income by riverine or upland (based on questionnaire)")
legend(x="bottomleft",legend=incomeSource,fill=color.income,bg="white")

# strict biophysical, primary income
income = cbind(table(d.riverine.strict$income1),table(d.upland.strict$income1))
income[,1] = income[,1] / sum(income[,1])
income[,2] = income[,2] / sum(income[,2])
colnames(income) = c("Riverine", "Upland")

barplot(income,col=color.income,main="Primary income by riverine or upland (strict classification)")
legend(x="bottomleft",legend=incomeSource,fill=color.income,bg="white")

# secondary income
income = cbind(table(d.riverine.strict$income2),table(d.upland.strict$income2))
income[,1] = income[,1] / sum(income[,1])
income[,2] = income[,2] / sum(income[,2])
colnames(income) = c("Riverine", "Upland")

barplot(income,col=color.income,main="Secondary income by riverine or upland (strict classification)")
legend(x="bottomleft",legend=incomeSource,fill=color.income,bg="white")


income = cbind(table(d.paricatuba$income1),table(d.paricatuba$income2))
income[,1] = income[,1] / sum(income[,1])
income[,2] = income[,2] / sum(income[,2])
colnames(income) = c("Primary", "Secondary")

barplot(income,col=color.income,main="Primary and secondary income in Paricatuba")
legend(x="bottomleft",legend=incomeSource,fill=color.income,bg="white")


##############################################################################
# effects on kgPerHaAcai and price

#############################################
# Basic Descriptive Statistics

# what affects kgPerHaAcai?


#############################################
# OLS Regression
fit.ols = lm( kgPerHaAcai ~ community + totalRiverineHoldings + linkedHHs + years + parentsOwned + grandparentsOwned + greatgrandparentsOwned + totalRoca + totalQuintais + totalCapoeira + totalMata + acaiBuyer + personsRural, data = d.riverine.strict )
d.riverine.strict$ols.resid = residuals(fit.ols)
summary(fit.ols)
anova(fit.ols)

fit.step.ols = step(fit.ols)
summary(fit.step.ols)

# density inversely related to amount of riverine holdings

fit.ols = lm( totalAcai ~ totalRoca + totalQuintais + totalCapoeira + totalMata, data = d.paricatuba )
d.paricatuba$ols.resid = residuals(fit.ols)
summary(fit.ols)
anova(fit.ols)

fit.step.ols = step(fit.ols)
summary(fit.step.ols)


#############################################
# GLS Regression


# how about totalAcai?

plotset = function (variable = d.riverine.strict$totalAcai, data=d.riverine.strict) {
  on = par(mfrow=c(3,3))
  
  # economic factors
  plot(variable ~ acaiBuyer, data=data)
  plot(variable ~ community, data=data)
  plot(variable ~ isOwner, data=data)
  
  # bounding variables
  plot(variable ~ totalRiverineHoldings, data=data)
  plot(variable ~ totalQuintais, data=data) # no linearity, but boundedness?
  plot(variable ~ kgPerHaAcai, data=data)
  
  # "human factors"
  plot(variable ~ years, data=data)
  plot(variable ~ personsRural, data=data)
  plot(variable ~ linkedHHs, data=data)
  par(on)
}



# make this into a function, then do it by community

#############################################
# OLS Regression
fit.ols = lm( totalAcai ~ community + totalRiverineHoldings + linkedHHs + years + parentsOwned + grandparentsOwned + greatgrandparentsOwned + totalRoca + totalQuintais + totalCapoeira + totalMata + acaiBuyer + personsRural, data = d.riverine.strict )
d.riverine.strict$ols.resid = residuals(fit.ols)
summary(fit.ols)
anova(fit.ols)

fit.step.ols = step(fit.ols)
summary(fit.step.ols)


# now what about acaiSold?
#############################################
# OLS Regression
fit.ols = lm( acaiSold ~ community + totalRiverineHoldings + linkedHHs + years + parentsOwned + grandparentsOwned + greatgrandparentsOwned + totalRoca + totalQuintais + totalCapoeira + totalMata + acaiBuyer + personsRural, data = d.riverine.strict )
d.riverine.strict$ols.resid = residuals(fit.ols)
summary(fit.ols)
anova(fit.ols)

fit.step.ols = step(fit.ols)
summary(fit.step.ols)


##############################################################################3
# Model calibration

d = d.paricatuba[d.paricatuba$totalUplandHoldings == 0,]
d = d[d$totalRiverineHoldings > 0,]

# plot d$totalRiverineCells
# check age of household
# # of linked households, to whom sold, lot size, % of lot use
# important: calibrate acai sold!!
