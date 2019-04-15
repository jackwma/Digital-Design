/* 	Wenjie Ma
	CSE 414 Spring 2019
	Dan Suciu 
	HW 2 */


select sum(f.capacity) as capacity
from FLIGHTS as F, MONTHS as M
where (F.origin_city = "Seattle WA"
and F.dest_city = "San Francisco CA")
OR (F.origin_city = "San Francisco CA"
and F.dest_city = "Seattle WA")
and M.month = "July"
and F.day_of_month = 10
and F.month_id = M.mid;

-- 1 row