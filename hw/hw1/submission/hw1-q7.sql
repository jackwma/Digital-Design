/* Wenjie Ma
CSE 414
Homework 1 Q6 */

/*
Write a SQL query that returns all restaurants that are within and including 10 mins from your house
*/

select * from MyRestaurants 
WHERE distance <= 10;