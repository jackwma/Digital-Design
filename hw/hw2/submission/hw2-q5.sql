/* 	Wenjie Ma
	CSE 414 Spring 2019
	Dan Suciu 
	HW 2 */

select C.cid as name, avg(F.canceled) as percent
from CARRIERS as C, FLIGHTS as F
where F.origin_city = "Seattle WA"
and F.carrier_id = C.cid
order by percent ASC
group by F.carrier_id;
having percent > 0.005;

--6 rows