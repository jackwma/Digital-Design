/* 	Wenjie Ma
	CSE 414 Spring 2019
	Dan Suciu 
	HW 2 */


select C.cid as carrier, max(F.price) as max_price
from FLIGHTS as F, CARRIERS as C
where (F.origin_city = "Seattle WA"
and F.dest_city = "New York NY")
OR (F.origin_city = "New York NY"
and F.dest_city = "Seattle WA")
and F.carrier_id = C.cid
group by F.carrier_id;

-- 3 rows