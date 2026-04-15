#!/bin/bash
API="http://localhost:8080"
echo "=== 1. Register demo user ==="
curl -s -X POST "$API/auth/signup" \
-H "Content-Type: application/json" \
-d '{"email":"demo@pharmacy.com","password":"demo123"}'
echo -e "\n=== 2. Make demo user a merchant ==="
curl -s -X POST "$API/merchant/response" \
-H "Content-Type: application/json" \
-d '{"email":"demo@pharmacy.com"}'
echo -e "\n=== 3. Place demo orders ==="
curl -s -X POST "$API/api/orders" \
-H "Content-Type: application/json" \
-d '{
"userEmail":"demo@pharmacy.com",
"deliveryAddress":"3, High Level Drive, Sydenham, SE26 3ET",
"cardType":"VISA","cardFirstFour":"4242",
"cardLastFour":"1234","cardExpiry":"12/27",
"items":[{"productId":1,"quantity":10},
{"productId":2,"quantity":5},
{"productId":3,"quantity":20}]
}'
echo -e "\n=== 4. Update order status ==="
curl -s -X PUT "$API/api/orders/1/status" \
-H "Content-Type: application/json" \
-d '{"status":"Dispatched"}'
echo -e "\n=== 5. Create promotion ==="
curl -s -X POST "$API/promo/create" \
-H "Content-Type: application/json" \
-d '{"name":"Spring Sale","start":"2026-04-01","end":"2026-04-30"}'
curl -s -X POST "$API/promo-product/add" \
-H "Content-Type: application/json" \
-d '{"productId":1,"discount":20,"promotionName":"Spring Sale"}'
curl -s -X POST "$API/promo-product/add" \
-H "Content-Type: application/json" \
-d '{"productId":2,"discount":15,"promotionName":"Spring Sale"}'
curl -s -X POST "$API/promo-product/add" \
-H "Content-Type: application/json" \
-d '{"productId":5,"discount":10,"promotionName":"Spring Sale"}'
echo -e "\n=== 6. Register non-commercial customer ==="
curl -s -X POST "$API/auth/register" \
-H "Content-Type: application/json" \
-d '{"email":"customer@test.com"}'
echo -e "\n\n=== DONE ==="
echo "Merchant: demo@pharmacy.com / demo123"
echo "Frontend: http://localhost:3000"