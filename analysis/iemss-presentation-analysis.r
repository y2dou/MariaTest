setwd("~/Documents/eclipse-workspace/MariaPrototype/analysis")
classpath = paste(getwd(), "../lib/mysql-connector-java-5.1.13-bin.jar", sep="/")
drv <- JDBC("com.mysql.jdbc.Driver", classpath)
conn <- dbConnect(drv, "jdbc:mysql://localhost/maria", "maria", "mariaprototype")

sensitivityScatterplotPair("constantprice_sensitivity_iemss", "acaiPrice", "capital", use.lowess=TRUE, title1 = "Linear programming", title2="Linear programming, predictive", title3="Decision trees")
sensitivityScatterplotPair("variableprice_sensitivity_price_iemss", "acaiMultiplier", "capital", use.lowess=TRUE, title1 = "Linear programming", title2="Linear programming, predictive", title3="Decision trees")
sensitivityScatterplotPair("constantprice_sensitivity_offervalue100_lambdaoffers_iemss", "lambdaOffers", "capital", xtransform = function(x) { 1 / x }, xlab="mean offer interarrival time (years)", use.lowess=TRUE, title1 = "Linear programming", title2="Linear programming, predictive", title3="Decision trees")
sensitivityScatterplotPair("variableprice_sensitivity_offervalue100_lambdaoffers_iemss", "lambdaOffers", "capital", xtransform = function(x) { 1 / x }, xlab="mean offer interarrival time (years)", use.lowess=TRUE, title1 = "Linear programming", title2="Linear programming, predictive", title3="Decision trees")
sensitivityScatterplotPair("variableprice_sensitivity_20offers_offervalueaverage_iemss", "offerValueAverage", "capital", xlab="mean offer value", use.lowess=TRUE, title1 = "Linear programming", title2="Linear programming, predictive", title3="Decision trees")
