/* 	Wenjie Ma
	CSE 414 Spring 2019
	Dan Suciu 
	HW 3 
List all cities that cannot be reached from Seattle through a direct flight nor with one stop
(i.e., with any two flights that go through an intermediate city). Warning: this query might take a while to execute.
We will learn about how to speed this up in lecture. (15 points)
Name the output column city. Order the output ascending by city.
(You can assume all cities to be the collection of all origin_city or all dest_city)
(Note: Do not worry if this query takes a while to execute. We are mostly concerned with the results)
[Output relation cardinality: 3 or 4, depending on what you consider to be the set of all cities]
*/

select distinct F.origin_city as city
from Flights as F
where F.origin_city not in (
    select ND.origin_city as NotDirectcity
    from Flights as tempF, Flights as ND
    where tempF.dest_city = ND.origin_city
    and tempF.origin_city = 'Seattle WA'
    and ND.origin_city <> 'Seattle WA'
    and tempF.dest_city <> 'Seattle WA'
)
and F.origin_city not in (
	select F4.dest_city
	from Flights as F4
	where F4.origin_city = 'Seattle WA'
)
and F.dest_city <> 'Seattle WA'
order by city ASC;