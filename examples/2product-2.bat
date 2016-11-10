load examples/2product-6w.json
step 3600
htmlcharts results/2product-6w "6 workers"
reset
#
load examples/2product-6w.json
printmoney Farm-5 200
printmoney Farm-7 200
step 3600
htmlcharts results/2product-6w-200 Farm "6 Workers + 200 for Farm"
reset
#
load examples/2product-6w.json
set M-Food payDividend true
set M-Milk payDividend true
step 3600
htmlcharts results/2product-6w-div "6 workers - market dividend"
reset
#
load examples/2productproblem-13.json
step 3600
htmlcharts results/13w "13 Workers"
reset
#
load examples/2productproblem-13.json
printmoney Farm-5 200
printmoney Farm-7 200
step 3600
htmlcharts results/13w-200  "13 Workers +200 @ Farm"
reset
#
load examples/2productproblem-13.json
step 3600
printmoney Farm-5 200
printmoney Farm-7 200
step 3600
htmlcharts results/13w-200-2 "13 Workers + 200@Farm @ step 3600 "
reset
#
