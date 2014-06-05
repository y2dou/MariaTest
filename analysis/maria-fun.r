# Maria data analysis
library(RJDBC)
#library(plotrix)
library(Cairo)
#library(ggplot2)

names.landuse = c("forest", "fallow", "açaí", "garden", "other")
colors.landuse = c("#154229", "#CC723E", "#54CC3E", "#FF534C", "#99867B")
xlim = c(1,40)

################################################################
# Presentation-quality functions

landuse.legend = function(...) {
  op <- par(xpd=FALSE,mar=c(0,0,0,0),oma=c(0,0,0,0))
  plot(1,2,col="white",axes=FALSE,xlab="",ylab="")
  legend("center",legend=names.landuse[names.landuse!='other'],fill=colors.landuse[names.landuse!='other'],...)
  par(op)
}

econTrajectory = function(runID, householdID, xlim=NULL, ylim=c(0,500), ...) {
  # prepare 3 charts: capital, labour and land trajectory
  #op = par(mfrow=c(3,2))
  op = par(no.readonly = TRUE, mar=c(4.5, 4, 2, 2) + 0.1, oma=c(0,0,0,0))

  # set 3 chart layout
  new.layout <- layout(matrix(c(1,1,2,3), 4, 1, byrow=TRUE))
  #layout.show(new.layout)

  d1 <- dbGetQuery(conn, "select tick, capital, labour, forest, fallow, acai, maniocgarden, other from tblHouseholdState where runID = ? and householdID = ?", runID, householdID)

  if (is.null(xlim))
    xlim = c(min(d1$tick),max(d1$tick))

  singleHouseholdAbsolute(runID,householdID,xlim=xlim,ylim=ylim,xlab="tick",ylab="# of land cells",...)
  plot(capital ~ tick, data = d1, ylim=c(0, max(d1$capital)), pch=19, cex=0.3, col="blue")
  lines(capital ~ tick, data = d1, col="blue")
  plot(labour ~ tick, data = d1, ylim=c(0, max(d1$labour)), pch=19, cex=0.3, col="red", ylab="labour availability")
  lines(labour ~ tick, data = d1, col="red")

  par(op)
}

econTrajectoryPair = function ( runID1, runID2, householdID1, householdID2, composite=FALSE, singleTrajectory=TRUE, capital=TRUE, labour=TRUE, xlim=NULL, ylim1=NULL, ylim2=NULL, title1="", title2="", ...) {
  if (!(composite && singleTrajectory))
    return                 # must have one of these

  # prepare 3 charts: capital, labour and land trajectory
  op = par(no.readonly = TRUE, mar=c(0, 4, 3, 2) + 0.1, oma=c(4.5,0,2,0))
                                                              
  # set 6 chart layout
  matrix.dims = c(1,2,1,2)
  
  if (composite && singleTrajectory)
    matrix.dims = c(matrix.dims, max(matrix.dims)+1,max(matrix.dims)+2, max(matrix.dims)+1,max(matrix.dims)+2)
  
  if (capital)
    matrix.dims = c(matrix.dims, max(matrix.dims)+1,max(matrix.dims)+2)
  
  if (labour)
    matrix.dims = c(matrix.dims, max(matrix.dims)+1,max(matrix.dims)+2)
    
  new.layout <- layout(matrix(matrix.dims, 2 * singleTrajectory + 2 * composite + capital + labour, 2, byrow=TRUE))
  #layout.show(new.layout)
  
  d1 <- dbGetQuery(conn, "select tick, capital, labour, forest, fallow, acai, maniocgarden, other from tblHouseholdState where runID = ? and householdID = ?", runID1, householdID1)
  d2 <- dbGetQuery(conn, "select tick, capital, labour, forest, fallow, acai, maniocgarden, other from tblHouseholdState where runID = ? and householdID = ?", runID2, householdID2)

  if (is.null(xlim))
    xlim = c(min(d1$tick),max(d1$tick))

  if (composite) {
    landUseTrajectoryComposite(runID1, axes=FALSE, main=title1, xlab="tick", xlim=xlim, ylim=ylim1)
    axis(2)
    
    if (!singleTrajectory && !capital && !labour){
      title(xlab="tick")
      axis(1)
    }
    
    landUseTrajectoryComposite(runID2, axes=FALSE, main=title2, xlab="tick", xlim=xlim, ylim=ylim2)
    axis(2)
    
    if (!singleTrajectory && !capital && !labour){
      title(xlab="tick")
      axis(1)
    }
  }

  if (singleTrajectory) {
    singleHouseholdAbsolute(runID1,householdID1,xlim=xlim,ylim=ylim1,xlab="tick",ylab="# of land cells (one household)",main=ifelse(composite,'',title1),axes=FALSE)
    axis(2)
    
    if (!capital && !labour) {
      title(xlab="tick")
      axis(1)
    }
    
    singleHouseholdAbsolute(runID2,householdID2,xlim=xlim,ylim=ylim2,xlab="tick",ylab="# of land cells (one household)",main=ifelse(composite,'',title2),axes=FALSE)
    axis(2)
    
    if (!capital && !labour){
      title(xlab="tick")
      axis(1)
    }
  }
  
  if (capital) {
    plot(capital ~ tick, data = d1, ylim=c(0, max(d1$capital,d2$capital)), pch=19, cex=0.3, col="blue",xlab="tick", axes=FALSE)
    lines(capital ~ tick, data = d1, col="blue")
    axis(2)
    
    if (!labour) {
      title(xlab="tick")
      axis(1)
    }
    
    plot(capital ~ tick, data = d2, ylim=c(0, max(d1$capital,d2$capital)), pch=19, cex=0.3, col="blue",xlab="tick", axes=FALSE)
    lines(capital ~ tick, data = d2, col="blue")
    axis(2)
  }

  if (labour) {
    plot(labour ~ tick, data = d1, ylim=c(0, max(d1$labour,d2$labour)), pch=19, cex=0.3, col="red", xlab="tick", ylab="labour availability",axes=FALSE)
    lines(labour ~ tick, data = d1, col="red")
    axis(2)
  }        
  title(xlab="tick")
  axis(1)
  
  if (labour) {
    plot(labour ~ tick, data = d2, ylim=c(0, max(d1$labour,d2$labour)), pch=19, cex=0.3, col="red", xlab="tick", ylab="labour availability",axes=FALSE)
    lines(labour ~ tick, data = d2, col="red")
    axis(2)
  }
  title(xlab="tick")
  axis(1)

  par(op)
}

sensitivityScatterplotPair = function(sweepName, xName, yName, paramNames=c(), paramValues=c(), tick=40, xlim=NULL, ylim=NULL, title1=NULL, title2=NULL, xlab=NULL, ylab=NULL, cex=0.12, percent.of=NULL, xtransform = NULL, ytransform = NULL, use.lowess=FALSE, ...) {
  op = par(no.readonly = TRUE, mar=c(4.5, 4, 3, 2) + 0.1, oma=c(0,0,0,0))
  
  # set 2 chart layout
  new.layout <- layout(matrix(c(1,2), 1, 2, byrow=TRUE))
  
  paramNames1=c(paramNames,"percentOptimalHouseholds")  
  paramNames2=c(paramNames,"percentHeuristicHouseholds")
  paramValues=c(paramValues,1)
  
  d1 <- sensitivityQuery(sweepName,xName,yName,tick=tick,paramNames1,paramValues)
  d2 <- sensitivityQuery(sweepName,xName,yName,tick=tick,paramNames2,paramValues)
  
  if (is.null(ylim))
    ylim = c(min(d1[,yName],d2[,yName]),max(d1[,yName],d2[,yName]))
  
  sensitivityScatterplot(sweepName,xName,yName,tick=tick,paramNames=paramNames1,paramValues=paramValues,d=d1,percent.of=percent.of,xlim=xlim,ylim=ylim,xlab=xlab,ylab=ylab,cex=cex,xtransform=xtransform,ytransform=ytransform,use.lowess=use.lowess,main=title1, ...)
  sensitivityScatterplot(sweepName,xName,yName,tick=tick,paramNames=paramNames2,paramValues=paramValues,d=d2,percent.of=percent.of,xlim=xlim,ylim=ylim,xlab=xlab,ylab=ylab,cex=cex,xtransform=xtransform,ytransform=ytransform,use.lowess=use.lowess,main=title2, ...)

  par(op)
}

sensitivityBoxplotPair = function(sweepName, xName, yName, paramNames=c(), paramValues=c(), tick=40, xlim=NULL, ylim=NULL, xlab=NULL, ylab=NULL, title1=NULL, title2=NULL, cex=0.12, percent.of=NULL, xtransform = NULL, ytransform = NULL, use.lowess=FALSE, ...) {
  op = par(no.readonly = TRUE, mar=c(4.5, 4, 3, 2) + 0.1, oma=c(0,0,0,0))
  
  # set 2 chart layout
  new.layout <- layout(matrix(c(1,2), 1, 2, byrow=TRUE))
  
  paramNames1=c(paramNames,"percentOptimalHouseholds")  
  paramNames2=c(paramNames,"percentHeuristicHouseholds")
  paramValues=c(paramValues,1)
  
  d1 <- sensitivityQuery(sweepName,xName,yName,tick=tick,paramNames1,paramValues)
  d2 <- sensitivityQuery(sweepName,xName,yName,tick=tick,paramNames2,paramValues)
  
  if (is.null(ylim))
    ylim = c(min(d1[,yName],d2[,yName]),max(d1[,yName],d2[,yName]))
  
  sensitivityBoxplot(sweepName,xName,yName,tick=tick,paramNames=paramNames1,paramValues=paramValues,d=d1,percent.of=percent.of,xlim=xlim,ylim=ylim,xlab=xlab,ylab=ylab,xtransform=xtransform,ytransform=ytransform,use.lowess=use.lowess,main=title1, ...)
  sensitivityBoxplot(sweepName,xName,yName,tick=tick,paramNames=paramNames2,paramValues=paramValues,d=d2,percent.of=percent.of,xlim=xlim,ylim=ylim,xlab=xlab,ylab=ylab,xtransform=xtransform,ytransform=ytransform,use.lowess=use.lowess,main=title2, ...)

  par(op)
}


########################################################3
# Exploratory functions

singleHousehold = function (runID, householdID, xlim=c(0,1), ylim=c(0,1), xlab="tick", ylab="cells", ...) {
  d <- dbGetQuery(conn, "select tick, capital, labour, forest, fallow, acai, maniocgarden, other from tblHouseholdState where runID = ? and householdID = ?", runID, householdID)
  d <- prepareHouseholdResultSet (d)
  #str(d)
  #plot(capital ~ tick, data = d)

  #stackpoly(d[,c(15:11)], stack=TRUE, axis4=FALSE, col=rev(colors.landuse), xlim=xlim, ylim=ylim, border="black",...)
  stackedPlot(d[,c(15:11)],d[,1],col=rev(colors.landuse), xlab=xlab, ylab=ylab, ylim=ylim, xlim=xlim, ...)
  #legend("bottomright", col=colors.landuse, legend=names.landuse, bg="white", pch=19)

  d
}

# single household land use trajectory, in absolutes
singleHouseholdAbsolute = function (runID, householdID, xlim=c(0,1), ylim=c(0,500), xlab="tick", ylab="cells", ...) {
  d <- dbGetQuery(conn, "select tick, capital, labour, forest, fallow, acai, maniocgarden, other from tblHouseholdState where runID = ? and householdID = ?", runID, householdID)
  d <- prepareHouseholdResultSet (d)
  
  #stackpoly(d[,c(8:4)], stack=TRUE, axis4=FALSE, col=rev(colors.landuse), xlim=xlim, ylim=ylim, border="black",...)
  stackedPlot(d[,c(8:4)],d[,1],col=rev(colors.landuse), xlab=xlab, ylab=ylab, ylim=ylim, xlim=xlim, ...)
  #legend("bottomright", col=colors.landuse, legend=names.landuse, bg="white", pch=19)

  d
}

sensitivityQuery = function (sweepName, xName, yName, paramNames=NULL, paramValues=NULL, tick=40) {
  params = ""
  if (!is.null(paramNames) && length(paramNames) > 0) {
    # add other parameters to the SQL condition clause
    for (i in 1:length(paramNames)) {
      params <- paste(params,"AND ",paramNames[i],"= ?")
    }
  }
  
  if (xName == 'links' || yName == 'links')
    query <- paste("select viewrun.runid, tblhouseholdstate.householdid,",xName,",",yName,"from viewrun inner join tblhouseholdstate on viewrun.runid = tblhouseholdstate.runid left outer join linkedhouseholdcount on viewrun.runid = linkedhouseholdcount.runid and tblhouseholdstate.householdid = linkedhouseholdcount.householdid and tblhouseholdstate.tick = linkedhouseholdcount.tick where sweepName = ? and tblhouseholdstate.tick = ?", params, sep=" ")
  else if (xName == 'runID')
    query <- paste("select viewrun.runid AS runID, tblhouseholdstate.householdid,",yName,"from viewrun inner join tblhouseholdstate on viewrun.runid = tblhouseholdstate.runid where sweepName = ? and tblhouseholdstate.tick = ?", params, sep=" ")
  else
    query <- paste("select viewrun.runid, tblhouseholdstate.householdid,",xName,",",yName,"from viewrun inner join tblhouseholdstate on viewrun.runid = tblhouseholdstate.runid where sweepName = ? and tblhouseholdstate.tick = ?", params, sep=" ")
  dbGetQuery(conn, query, sweepName, tick, paramValues)
}

sensitivityScatterplot = function (sweepName, xName, yName, paramNames=NULL, paramValues=NULL, tick=40, d=NULL, xlim=NULL, ylim=NULL, xlab=NULL, ylab=NULL, cex=0.12, percent.of=NULL, xtransform = NULL, ytransform = NULL, use.lowess=FALSE, ...) {
  if (is.null(d))
    d <- sensitivityQuery(sweepName, xName, yName, paramNames, paramValues, tick)
    
  dd <- d
  
  if (is.null(xlab)) xlab = xName
  if (is.null(ylab)) ylab = yName
  
  if (!is.null(xtransform))
    dd[,xName] <- xtransform(dd[,xName])
    
  if (!is.null(ytransform))
    dd[,yName] <- ytransform(dd[,yName])
  
  if (is.null(xlim))
    xlim <- c(min(dd[,xName]),max(dd[,xName]))
    
  if (is.null(ylim))
    ylim <- c(min(dd[,yName]),max(dd[,yName]))
  
  if (!is.null(percent.of)) {
    if (!is.null(percent.of[1])) {
      dd[,xName] = dd[,xName] / percent.of[1] * 100
      xlim = xlim / percent.of[1] * 100
    }
    
    if (!is.null(percent.of[2])) {
      dd[,yName] = dd[,yName] / percent.of[2] * 100
      ylim = ylim / percent.of[2] * 100
    }
  }
  
  # filter out d by xlim and ylim
  dd <- subset(dd,dd[,xName] >= xlim[1] & dd[,xName] <= xlim[2])
  dd <- subset(dd,dd[,yName] >= ylim[1] & dd[,yName] <= ylim[2])
  
  
  plot(dd[,xName],dd[,yName],xlab=xlab,ylab=ylab,xlim=xlim,ylim=ylim,pch=19,cex=cex, ...)
  # should do non-parametric smoothing
  
  if (use.lowess)
    lines(lowess(dd[,xName],dd[,yName]), col="red", lwd=2)
    
}


sensitivityBoxplot = function (sweepName, xName, yName, paramNames=NULL, paramValues=NULL, tick=40, d=NULL, xlim=NULL, ylim=NULL, xlab=NULL, ylab=NULL, percent.of=NULL, xtransform = NULL, ytransform = NULL, ...) {
  if (is.null(d))
    d <- sensitivityQuery(sweepName, xName, yName, paramNames, paramValues, tick)
    
  dd <- d
  
  if (is.null(xlab)) xlab = xName
  if (is.null(ylab)) ylab = yName
  
  if (!is.null(xtransform))
    dd[,xName] <- xtransform(dd[,xName])
    
  if (!is.null(ytransform))
    dd[,yName] <- ytransform(dd[,yName])
  
  if (is.null(xlim))
    xlim <- c(min(dd[,xName]),max(dd[,xName]))
    
  if (is.null(ylim))
    ylim <- c(min(dd[,yName]),max(dd[,yName]))
  
  if (!is.null(percent.of)) {
    if (!is.null(percent.of[1])) {
      dd[,xName] = dd[,xName] / percent.of[1] * 100
      xlim = xlim / percent.of[1] * 100
    }
    
    if (!is.null(percent.of[2])) {
      dd[,yName] = dd[,yName] / percent.of[2] * 100
      ylim = ylim / percent.of[2] * 100
    }
  }
  
  # filter out d by xlim and ylim
  dd <- subset(dd,dd[,xName] >= xlim[1] & dd[,xName] <= xlim[2])
  dd <- subset(dd,dd[,yName] >= ylim[1] & dd[,yName] <= ylim[2])
  
  
  boxplot(dd[,yName]~dd[,xName],xlab=xlab,ylab=ylab,xlim=xlim,ylim=ylim, ...)
  dd
}

landUseTrajectoryComposite = function (runID, stage = 'report', xlab="tick", xlim=c(0,40), ylim=NULL, lwd.base=0.3, ...) {
  d <- dbGetQuery(conn,"select householdID, tick, acai, maniocgarden, fallow, forest from tblhouseholdstate where runid = ? and stage = ?",runID,stage)
  
  if (is.null(ylim))
    ylim = c(0,max(d$acai,d$maniocgarden,d$fallow))
  
  plot(c(),c(),xlim=xlim,ylim=ylim,xlab=xlab,ylab="# of cells (each household)",...)
  
  ltys <- rep(1:6,ceiling(length(unique(d$householdID))/6))
  
  for (i in unique(d$householdID)) {
    d <- rbind(c(i,0,0,0,0,0),d)
    
    col.transform <- function(color) {
      color
    }
    
    lwd <- i/20.0 + lwd.base
    
    lines(d[d$householdID==i,]$tick,d[d$householdID==i,]$acai,col=col.transform(colors.landuse[3]),lwd=lwd,lty=ltys[i])
    lines(d[d$householdID==i,]$tick,d[d$householdID==i,]$maniocgarden,col=col.transform(colors.landuse[4]),lwd=lwd,lty=ltys[i])
    lines(d[d$householdID==i,]$tick,d[d$householdID==i,]$fallow,col=col.transform(colors.landuse[2]),lwd=lwd,lty=ltys[i])
  }
  
}

linkedHouseholdsNumComposite = function (runID, xlab="tick", xlim=c(0,40), ylim=NULL, lwd.base=0.3, ...) {
  d <- dbGetQuery(conn,"select sourceHousehold, tick, count(urbanagentid) as links from tblurbanagent u NATURAL JOIN tblurbanagentstate s where runid = ? group by sourceHousehold, tick",runID)
  d.household <- dbGetQuery(conn,"select householdid from tblhousehold where runid = ?",runID)
  
  if (is.null(ylim)) {
    if (length(row.names(d))>0)
      ylim = c(0,max(d$links))
    else
      ylim = c(0,1)
  }
  
  plot(c(),c(),xlim=xlim,ylim=ylim,xlab=xlab,ylab="# of off-site agents",...)
  
                          
  for (i in unique(d.household$householdID)) {
    for (j in seq(from=xlim[1],to=xlim[2],by=1)) {
      if (length(row.names(d[d$sourceHousehold==i,])) == 0 || !any(d[d$sourceHousehold==i,]$tick == j)) {
        d <- rbind(c(i,j,0),d)
      }
    }
  }
  
  names(d) <- c("sourceHousehold","tick","links")
  d <- d[order(d$sourceHousehold,d$tick),]
  
  ltys <- rep(1:6,ceiling(length(unique(d$sourceHousehold))/6))
  
  cols <- rainbow(length(unique(d$sourceHousehold)))
  
  
  for (i in unique(d$sourceHousehold)) {
    lwd <- i/20.0 + lwd.base
    
    lines(d[d$sourceHousehold==i,]$tick,d[d$sourceHousehold==i,]$links,col=cols[i],lwd=lwd,lty=ltys[i])
  }
  
  d
}


avgState = function (runID, ylim=c(0,1), xlab="Year", ylab="Land Use Composition", ...) {
  d <- dbGetQuery(conn, "call avghouseholdstate(?)", runID)
  d <- prepareHouseholdResultSet (d)
  stackpoly(d[,c(17:13)], stack=TRUE, axis4=FALSE, col=rev(colors.landuse), ylim=ylim, xlab=xlab, ylab=ylab, border="black",...)
  legend("topleft", col=colors.landuse, legend=names.landuse, bg="white", pch=19)
}

avgCapital = function (runID) {
  d <- dbGetQuery(conn, "call avghouseholdstate(?)", runID)
  d <- prepareHouseholdResultSet (d)
  plot(capital ~ tick, data = d,
    xlab="Year", ylab="Capital")
}

avgLabour = function (runID) {
  d <- dbGetQuery(conn, "call avghouseholdstate(?)", runID)
  d <- prepareHouseholdResultSet (d)
  plot(labour ~ tick, data = d,
    xlab="Year", ylab="Labour (person-hours)",
    ylim=c(0,5))
}

##############################################################
# Utility functions

formatParameterList = function (parameterList) {
  paste(format(parameterList, scientific=FALSE, nsmall=1), sep=" ", collapse= " ")
}

prepareHouseholdResultSet = function (resultSet) {
  names(resultSet) <- tolower(names(resultSet)) # make column names lowercase
  resultSet$x <- resultSet$tick

  #cols.percentlanduse = unlist(lapply(cols.landuse, function(x) paste("percent", x, sep="")))

  resultSet$total = resultSet$acai + resultSet$maniocgarden + resultSet$forest + resultSet$fallow + resultSet$other
  resultSet$percentforest = resultSet$forest / resultSet$total
  resultSet$percentfallow = resultSet$fallow / resultSet$total
  resultSet$percentacai = resultSet$acai / resultSet$total
  resultSet$percentmaniocgarden = resultSet$maniocgarden / resultSet$total
  resultSet$percentother = resultSet$other / resultSet$total
  
  #resultSet$meanOffers = ifelse(resultSet$lambdaOffers > 0, 1 / resultSet$lambdaOffers, resultSet$numOffers)
  resultSet
}


# https://stat.ethz.ch/pipermail/r-help/2005-August/077475.html
stackedPlot <- function(data, time=NULL, col=1:length(data), xlim=NULL, ylim=NULL, ...) {
  if (is.null(xlim))
    xlim = range(time)

  if (is.null(ylim))
    ylim = c(0,max(rowSums(data)))

  if (is.null(time))
    time <- 1:length(data[[1]]);

  plot(0,0
       , xlim = xlim
       , ylim = ylim
       , t="n"
       , ...
       );

  for (i in length(data):1) {

    # Die Summe bis zu aktuellen Spalte
    prep.data <- rowSums(data[1:i]);

    # Das Polygon muss seinen ersten und letzten Punkt auf der Nulllinie haben
    prep.y <- c(0
                , prep.data
                , 0
                )

    prep.x <- c(time[1]
                , time
                , time[length(time)]
                )

    polygon(prep.x, prep.y
            , col=col[i]
            , border = "black"
            );
  }
}

#####################################################################
# Data loading functions

# load prices from stream
acai.prices = function (multiplier = 1, constant = -1, ticks = 40) {
  d <- scan("../auxdata/prices/acai.prices.txt", nmax=ticks)
  
  if (constant > 0)
    replicate(length(d), constant)
  else
    d <- d * multiplier
    
  d
}

manioc.prices = function (multiplier = 1, constant = -1, ticks = 40) {
  d <- scan("../auxdata/prices/maniocgarden.prices.txt", nmax=ticks)  
                        
  if (constant > 0)
    replicate(length(d), constant)
  else
    d <- d * multiplier
    
  d
}

timber.prices = function (multiplier = 1, constant = -1, ticks = 40) {
  d <- scan("../auxdata/prices/timber.prices.txt", nmax=ticks)  
  
  if (constant > 0)
    replicate(length(d), constant)
  else
    d <- d * multiplier
    
  d
}
