/* Wenjie Ma
CSE 414
Homework 1 Q1 */

.header on
.mode column

/*
Create a table Edges(Source, Destination) where 
both Source and Destination are integers.
*/
create table Edges
(Source int, Destination int);

/*
Insert the tuples (10,5), (6,25), (1,3), and (4,4)
*/
insert into Edges values(10,5);
insert into Edges values(6,25);
insert into Edges values(1,3);
insert into Edges values(4,4);

/*
Write a SQL statement that returns all tuples.
*/
select * from edges;

/*
Write a SQL statement that returns only column Source for all tuples.
*/
select source from edges;

/*
Write a SQL statement that returns all tuples where Source > Destination.
*/
select * from edges 
where Source > Destination;

/*
Now insert the tuple ('-1','2000'). Do you get an error? 
Why? This is a tricky question, you might want to check the documentation.
*/
insert into edges values('-1','2000');

/*
Any column in an SQLite version 3 database, 
except an INTEGER PRIMARY KEY column, 
may be used to store a value of any storage class.
Therefore, no error was caught.
*/

select * from edges;