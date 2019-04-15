/* 	Wenjie Ma
	CSE 414 Spring 2019
	Dan Suciu 
	HW 2 */

/* 
Find the day of the week with the longest average arrival delay.
Return the name of the day and the average delay.
Name the output columns day_of_week and delay, in that order. (Hint: consider using LIMIT. Look up what it does!)
*/

select W.day_of_week as day_of_week, avg(F.arrival_delay) as delay
from WEEKDAYS as W, FLIGHTS as F
where W.day_of_week = F.day_of_week_id
group by day_of_week
order by delay DESC
LIMIT 1;

--1 row