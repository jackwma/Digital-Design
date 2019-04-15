/*  Wenjie Ma
    CSE 414 Spring 2019
    Dan Suciu 
    HW 2 - Creating a table */


PRAGMA foreign_keys=ON;
.mode csv

# creating tables 
CREATE TABLE CARRIERS (
	cid varchar(7) PRIMARY KEY,
	name varchar(83)
);

.import carriers.csv Carriers

CREATE TABLE MONTHS (
	mid int PRIMARY KEY,
	month varchar(9)
);
.import months.csv Months


CREATE TABLE WEEKDAYS (
	did int PRIMARY KEY,
	day_of_week varchar(9)
);
.import weekdays.csv Weekdays

.table

CREATE TABLE Flights (
	fid int, 
    month_id int,        -- 1-12
    day_of_month int,    -- 1-31 
    day_of_week_id int,  -- 1-7, 1 = Monday, 2 = Tuesday, etc
    carrier_id varchar(7), 
    flight_num int,
    origin_city varchar(34), 
    origin_state varchar(47), 
    dest_city varchar(34), 
    dest_state varchar(46), 
    departure_delay int, -- in mins
    taxi_out int,        -- in mins
    arrival_delay int,   -- in mins
    canceled int,        -- 1 means canceled
    actual_time int,     -- in mins
    distance int,        -- in miles
    capacity int, 
    price int
);
.import flights-small.csv Flights



