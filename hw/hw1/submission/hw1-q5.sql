/* Wenjie Ma
CSE 414
Homework 1 Q5 */

/*
Write a SQL query that returns only the name and distance of all restaurants within and
including 20 minutes of your house. 
The query should list the restaurants in alphabetical order of names.
*/

select name, distance from MyRestaurants 
WHERE distance <= 20
ORDER by name;

