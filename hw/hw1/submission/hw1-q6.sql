/* Wenjie Ma
CSE 414
Homework 1 Q6 */

/*
Write a SQL query that returns all restaurants that you like, 
but have not visited
since more than 3 months ago.
*/
select * from MyRestaurants 
WHERE like = 1
AND (select julianday('now') - julianday(lastVisit)) > 90;

