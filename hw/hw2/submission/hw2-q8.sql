/* 	Wenjie Ma
	CSE 414 Spring 2019
	Dan Suciu 
	HW 2 */


select C.cid as name, sum(F.departure_delay) as delay
from FLIGHTS as F, CARRIERS as C
where C.cid = F.carrier_id
group by F.carrier_id
having delay > 0;

--22 rows