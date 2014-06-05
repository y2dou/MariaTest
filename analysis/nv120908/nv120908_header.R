###############################################
# Data preparation file for statistical analysis of the survey data
# recoded by Nathan Vogt on September 12, 2008.
#
# Modifications to the original Excel file were:
#   * conversion to CSV
#   * removal of erroneous comma in the lotsize column
#   * conversion of lotsize dimensions (assumed to be in meters) to hectares
#   * renaming of headers
#
# Raymond Cabrera
# September 16, 2008

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

# set working directory and load data file
d = read.csv("NV120908.csv")

# codes
yesNo = c("Yes", "No")
incomeSource.portuguese = c("acai", "agriculture", "aposent/beneficios", "bolsa familia",
  "pesca", "commercial", "empregada/diarista", "function publico", "outro", "NA")
incomeSource = c("acai", "agriculture", "retirement/pension", "welfare",
  "fishing", "self-employed", "private sector", "public sector", "other", "NA")

color.yesno = c("blue","white")
color.income = rainbow(9)
color.buyer = rainbow(6)

# add derived columns
d$percentUplandHoldings = d$totalUplandHoldings / d$totalHoldings
d$percentRiverineHoldings = d$totalRiverineHoldings / d$totalHoldings

# measure holdings in 15m x 15m cells
d$totalUplandCells = d$totalUplandHoldings / 0.0225
d$totalRiverineCells = d$totalRiverineHoldings / 0.0225
d$totalCells = d$totalHoldings / 0.0225

# correct columns
d[d$acaiSold==99,]$acaiSold = 0

# set ordinal variables
#d$household = factor(d$household)
d$isOwner = factor(d$isOwner, labels = yesNo)

d$income1 = factor(d$income1)
d$income2 = factor(d$income2)
d$income3 = factor(d$income3)
d$income4 = factor(d$income4)
d$income5 = factor(d$income5)

d$parentsOwned = factor(d$parentsOwned, labels = yesNo)
d$grandparentsOwned = factor(d$grandparentsOwned, labels = yesNo)
d$greatgrandparentsOwned = factor(d$greatgrandparentsOwned, labels = yesNo)

d$acaiBuyer = factor(d$acaiBuyer, labels = c("Middleman", "Local processor", 
  "Large processor-factory", "Consumer", "Local Merchant", "Other"))


d.upland = d[d$biophysical=="Upland",]
d.riverine = d[d$biophysical=="Riverine",]

d.upland$community = factor(d.upland$community)
d.riverine$community = factor(d.riverine$community)

d.upland.strict = d[d$totalRiverineHoldings==0 & d$totalUplandHoldings > 0,]
d.riverine.strict = d[d$totalUplandHoldings==0 & d$totalRiverineHoldings > 0,]

d.upland.strict$community = factor(d.upland.strict$community)
d.riverine.strict$community = factor(d.riverine.strict$community)


d.mixed.strict = d[d$totalRiverineHoldings > 0 & d$totalUplandHoldings > 0,]



# break down dataset by community

# upland communities
d.antonio = d[d$community=="Antônio Vieira",]
d.carmo = d[d$community=="Carmo de Maruanum",]
d.maruanum = d[d$community=="Maruanum II",]
d.santaluzia = d[d$community=="Santa Luzia",]
d.torrao = d[d$community=="Torrão de Maruanum",]

# riverine communities
d.benfica = d[d$community=="Benfica",]
d.fozdemazagao = d[d$community=="Foz de Mazagao",]
d.marajo = d[d$community=="Marajó Açú",]          # outliers are upland
d.paricatuba = d[d$community=="Paricatuba",]      # whisker is about 0.5
d.saomiguel = d[d$community=="São Miguel",]       # whisker is about 0.5

# in-between
d.jagarajo = d[d$community=="Jagarajó",]          # mostly upland
d.praiagrande = d[d$community=="Praia Grande",]   # nearly even      (0.5)
d.saopedro = d[d$community=="São Pedro",]         # more so riverine (0.237)
