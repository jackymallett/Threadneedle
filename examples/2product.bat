load defaultSimulation.json
set seed 7272911
step 3600
htmlcharts results/default "6 workers"
reset
#
load defaultSimulation.json
set seed 7272911
printmoney Farm-5 200
printmoney Farm-7 200
step 3600
htmlcharts results/default-200 Farm "6 Workers + 200 for Farm"
reset
#
load configs/2productproblem-7.json
set seed 7272911
step 3600
htmlcharts results/7w  "7 Workers"
reset
#
load configs/2productproblem-7.json
set seed 7272911
printmoney Farm-5 200
printmoney Farm-7 200
step 3600
htmlcharts results/7w-200 "7 Workers-200"
reset
#
load configs/2productproblem-13.json
set seed 7272911
printmoney Farm-5 200
printmoney Farm-7 200
step 3600
htmlcharts results/13w  "13 Workers"
reset
#
load configs/2productproblem-13.json
set seed 7272911
step 3600
htmlcharts results/13w-200 "13 Workers"
#
