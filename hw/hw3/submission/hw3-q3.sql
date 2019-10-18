/* 	Wenjie Ma
	CSE 414 Spring 2019
	Dan Suciu 
	HW 3 
For each origin city, find the percentage of departing flights shorter than 3 hours.
For this question, treat flights with NULL actual_time values as longer than 3 hours. (15 points)
Name the output columns origin_city and percentage
Order by percentage value, ascending. Be careful to handle cities without any flights shorter than 3 hours.
We will accept either 0 or NULL as the result for those cities.
Report percentages as percentages not decimals (e.g., report 75.25 rather than 0.7525).
*/


with shorterFlights as (
	select shortF.origin_city, count(shortF.actual_time) as numerator
	from Flights as shortF
	where shortF.actual_time < 180
	and shortF.actual_time is not NULL
	group by shortF.origin_city
)
select distinct F.origin_city as origin_city, 100*(cast(SF.numerator as float)/count(F.actual_time)) as percentage
from Flights as F left join shorterFlights as SF
on F.origin_city = SF.origin_city
group by F.origin_city, SF.numerator
order by percentage ASC;

/*
runtime: 66 seconds

output: 
Guam TT	
Pago Pago TT
Aguadilla PR	29.433962264150
Anchorage AK	32.146037399821
San Juan PR	33.890360709190
Charlotte Amalie VI	40.000000000000
Ponce PR	41.935483870967
Fairbanks AK	50.691244239631
Kahului HI	53.664998528113
Honolulu HI	54.908808692277
San Francisco CA	56.307656826568
Los Angeles CA	56.604107648725
Seattle WA	57.755416553349
Long Beach CA	62.454116413214
Kona HI	63.282107574094
New York NY	63.481519772551
Las Vegas NV	65.163009288383
Christiansted VI	65.333333333333
Newark NJ	67.137355584082
Worcester MA	67.741935483870

*/