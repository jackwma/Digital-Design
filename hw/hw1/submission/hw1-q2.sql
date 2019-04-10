/* Wenjie Ma
CSE 414
Homework 1 Q2 */

.header on
.mode column


/* 
Create a table called MyRestaurants with the following attributes 
(you can pick your own names for the attributes, just make sure it is clear which one is for which):
Name of the restaurant: a varchar field
Type of food they make: a varchar field
Distance (in minutes) from your house: an int

Date of your last visit: a varchar field, interpreted as date
Whether you like it or not: an int, interpreted as a Boolean
*/
create table MyRestaurants (
	name varchar(20),
   	foodType varchar(20),
   	distance int,
   	lastVisit varchar(20),
   	like int
   	);

