/* 	Wenjie Ma
	CSE 414 Spring 2019
	Dan Suciu 
	HW 3 
Express the same query as above, but do so without using a nested query. Again, name the output column
carrier and order ascending. (8 points)
*/

select distinct C.name as carrier
from Carriers as C and Flights as F
where F.origin_city = 'Seattle WA'
and F.dest_city = 'San Francisco CA'
and F.carrier_id = C.cid
order by C.name ASC;