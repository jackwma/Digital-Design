/* 	Wenjie Ma
	CSE 414 Spring 2019
	Dan Suciu 
	HW 2 */


select C.cid as name_of_the_carrier, f1.fid as f1_flight_num, f1.origin_city as f1.origin_city, f1_dest_city as f1_dest_city, f1.actual_time as f1_actual_time,
f2.fid as f2_flight_num, f2.origin_city as f2_origin_city, f2.dest_city as f2_dest_city, f2.actual_time as f2_actual_time, (f1.actual_time + f2.actual_time) as total_flight_time
from FLIGHTS as f1, FLIGHTS as f2, CARRIERS as C, MONTHS as M
where f1.carrier_id = C.cid
and M.month = "July"
and f1.day_of_month = 15
and f2.day_of_month = 15
and total_flight_time < 420
and f1.month_id = M.mid
and f2.month_id = M.mid
and f1.origin_city = "Seattle WA"
and f2.origin_city != "Seattle WA"
and f1.dest_city != "Boston MA"
and f2.dest_city = "Boston MA"
and f1.dest_city = f2.origin_city
and f1.carrier_id = f2.carrier_id

--1472 rows