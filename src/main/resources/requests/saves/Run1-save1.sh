#!/bin/sh
echo "1. Bob is a lambda student who wants to move."
echo ""

sleep $*;
echo "2. Bob needs to transport his bed from his parents house (Nice) to his new student apartment (Sophia)."
echo ""

sleep $*;
echo "3. Bob is a smart guy: he decides to use BlablaMove."
echo ""

sleep $*;
echo "4. He Logins on BlablaMove: he has the right amount of points."
echo ">> Processing Bob login..."
# LOGIN CURL TODO

sleep $*;
echo "5. He fills a form with following information:"
echo "\t-start location"
echo "\t-arrival location"
echo "\t-bed's size and weight"
echo "\t-move date"
echo "\t-maximum points to spend"
echo ""
curl -H "Accept: application/json" -H "Content-type: application /json" -X POST -d

sleep $*;
echo "6. The system gives him a list of results that match his requirements"
echo ""

sleep $*;
echo "7.Bob chose a ride for his bed."
echo ""
curl -H "Accept: application/json" -H "Content-type: application /json" -X POST -d

sleep $*;
echo "8. The system answer him with a recap."
echo ""

sleep $*;
echo "9. Bob confirm."
echo ""
curl -H "Accept: application/json" -H "Content-type: application /json" -X POST -d

sleep $*;
echo "10. He receives a confirmation mail from BlablaMove: Charlie can help him to move his things."
echo ""

sleep $*;
echo "11. --- Ellipse  ---"
echo ""

sleep $*;
echo "12.At the chosen date, Charlie goes to Bob house and take his bed."
echo ""

sleep $*;
echo "13.Charlie goes to Sophia."
echo ""

sleep $*;
echo "14.Bob receive an notification that confirm the delivery of his bed."
echo ""
curl -H "Accept: application/json" -H "Content-type: application/json" -X POST -d

sleep $*;
echo "15.Bob can now confirm the transaction to BlablaMove."
echo ""
curl -H "Accept: application/json" -H "Content-type: application/json" -X POST -d

sleep $*;
echo "16.After the confirmation from Bob,the BalblaMove collects the points that were needed for this transaction."