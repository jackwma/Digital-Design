/* 	Wenjie Ma
	CSE 414 Spring 2019
	Dan Suciu 
	HW 2 */


select distinct C.cid as name
from CARRIERS as C, FLIGHTS as F
where C.cid = F.carrier.id
group by carrier_name, F.month_id, F.day_of_month
having count(*) > 1000;

--12 rows