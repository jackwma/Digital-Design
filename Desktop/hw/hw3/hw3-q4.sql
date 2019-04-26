/* 	Wenjie Ma
	CSE 414 Spring 2019
	Dan Suciu 
	HW 3 
List all cities that cannot be reached from Seattle though a direct flight but can be reached with
one stop (i.e., with any two flights that go through an intermediate city).
Do not include Seattle as one of these destinations (even though you could get back with two flights).
(15 points)
Name the output column city. Order the output ascending by city.
*/

with notOriginCity as (
	select F2.origin_city, F2.dest_city
	from Flights as F2
	where F2.dest_city != 'Seattle WA'
	and F2.dest_city not in (
	    select F1.dest_city
        from Flights as F1
        where F1.origin_city = 'Seattle WA'
    )
    group by F2.origin_city, F2.dest_city
)
select distinct ND.dest_city as city
from Flights as F, notOriginCity as ND
where F.dest_city = ND.origin_city
and F.origin_city = 'Seattle WA'
group by ND.dest_city
order by city ASC;