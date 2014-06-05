#######################################################################
# Prices


#######################################################################
# Sensitivity analysis
percent.sweep50 = seq(0.75, 1.25, by = 0.01)

# root scenarios
sweep.acaiprice = formatParameterList(percent.sweep50 * 0.0003)
sweep.acaimultiplier = formatParameterList(percent.sweep50 * 0.000003)
sweep.maniocprice = formatParameterList(percent.sweep50 * 0.002)

# sensitivity analyses
sweep.acaiprice = formatParameterList(percent.sweep50 * 0.0003345)
sweep.acaimultiplier = formatParameterList(percent.sweep50 * 0.00000279)
sweep.maniocprice = formatParameterList(percent.sweep50 * 0.002)

sweep.offervalueaverage = formatParameterList(seq(from=250,to=750,by=50))
