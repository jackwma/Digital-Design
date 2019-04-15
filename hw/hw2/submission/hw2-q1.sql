/* 	Wenjie Ma
	CSE 414 Spring 2019
	Dan Suciu 
	HW 2 */ 

select distinct F.flight_num as flight_num
from flights as F, carriers as C, weekdays as W
where F.carrier_id = C.cid
and F.day_of_week_id = W.did
and W.day_of_week = "Monday"
and C.name = "Alaska Airlines Inc."
and F.origin_city = "Seattle WA"
and F.dest_city = "Boston MA";

--3 rows
