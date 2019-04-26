/* 	Wenjie Ma
	CSE 414 Spring 2019
	Dan Suciu 
	HW 3 
Find all origin cities that only serve flights shorter than 3 hours.
You can assume that flights with NULL actual_time are not 3 hours or more. (15 points)
Name the output column city and sort them. List each city only once in the result.
[Output relation cardinality: 109]
*/

with maxFlights as(
    select F1.origin_city as origin_city, max(F1.actual_time) as max
    from Flights as F1
    group by F1.origin_city
)
select distinct F.origin_city as city
from FLIGHTS as F, maxFlights as MF
where F.actual_time = MF.max
and MF.origin_city = F.origin_city
and (F.actual_time < 180
OR F.actual_time = NULL);
