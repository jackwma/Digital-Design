/* Wenjie Ma
CSE 414
Homework 1 Q4 */

/* print the results in comma-separated form */

.mode csv 
select * from MyRestaurants;

/* print the results in list form, delimited by "|" */

.mode list
.separator "|"

select * from MyRestaurants;

/* print the results in column form, and make each column have width 15 */

.mode column
.width 15 15 15 15 15

select * from MyRestaurants;

/* for each of the formats above, 
 try printing/not printing the column headers with the results
 */

/* header ON */

.header on
.mode csv
select * from MyRestaurants;

.mode list
.separator "|"

select * from MyRestaurants;

.mode column
.width 15 15 15 15 15

select * from MyRestaurants;

/* header OFF */
.header off
.mode csv
select * from MyRestaurants;

.mode list
.separator "|"

select * from MyRestaurants;

.mode column
.width 15 15 15 15 15

select * from MyRestaurants;