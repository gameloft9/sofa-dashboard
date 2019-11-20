cd `dirname $0`

./term.sh netpay-alipay-open4 &
./term.sh netpay-gateway4 &
./term.sh netpay-postman4 &      
./term.sh netpay-web-manage4 &
./term.sh netpay-alipay-acquire4 &
./term.sh netpay-db-service4 &  
./term.sh netpay-jobs4 &    
./term.sh netpay-route-server4 & 
./term.sh netpay-wxpay-open4 &
./term.sh netpay-bills4 &
./term.sh netpay-portal4 &
./term.sh netpay-db-bills4 &
./term.sh netpay-risk-control4 &
./term.sh netpay-manager4 &
./term.sh netpay-http-post4 &
./term.sh netpay-public-service4 &
./term.sh netpay-mer-portal4 &

#sleep 30
