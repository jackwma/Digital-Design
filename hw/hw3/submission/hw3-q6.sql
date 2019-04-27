/* 	Wenjie Ma
	CSE 414 Spring 2019
	Dan Suciu 
	HW 3 
List the names of carriers that operate flights from Seattle to San Francisco, CA.
Return each carrier's name only once. Use a nested query to answer this question. (7 points)
Name the output column carrier. Order the output ascending by carrier.
[Output relation cardinality: 4]
*/

select distinct C.name as carrier
from Carriers as C,
(select F.carrier_id
 from Flights as F
 where F.origin_city = 'Seattle WA'
 and F.dest_city = 'San Francisco CA') as F
where F.carrier_id = C.cid
order by C.name ASC;

/*
runtime: 4 seconds

output: 
Alaska Airlines Inc.
SkyWest Airlines Inc.
United Air Lines Inc.
Virgin America

*/