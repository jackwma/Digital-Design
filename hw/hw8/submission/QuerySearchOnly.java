import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;

/**
 * Runs queries against a back-end database.
 * This class is responsible for searching for flights.
 */
public class QuerySearchOnly
{
    // `dbconn.properties` config file
    private String configFilename;

    // DB Connection
    protected Connection conn;

    private static final String BEGIN_TRANSACTION_SQL = "SET TRANSACTION ISOLATION LEVEL SERIALIZABLE; BEGIN TRANSACTION;";
    protected PreparedStatement beginTransactionStatement;

    private static final String COMMIT_SQL = "COMMIT TRANSACTION";
    protected PreparedStatement commitTransactionStatement;

    private static final String ROLLBACK_SQL = "ROLLBACK TRANSACTION";
    protected PreparedStatement rollbackTransactionStatement;

    private static final String CHECK_FLIGHT_CAPACITY = "SELECT capacity FROM Flights WHERE fid = ?";
    private static final String DIRECT_SEARCH = "SELECT TOP (?) fid, day_of_month, carrier_id, flight_num, origin_city, dest_city, actual_time, capacity, price FROM FLIGHTS WHERE origin_city = ? AND dest_city = ? AND day_of_month = ? AND canceled = 0 ORDER BY actual_time ASC";
    private static final String INDIRECT_SEARCH =
            "SELECT TOP(?) X.fid AS fid1,X.day_of_month AS day1,X.carrier_id AS carrier1,X.flight_num AS fnum1,X.origin_city AS origin1,X.dest_city AS dest1,X.actual_time AS time1, X.capacity AS capacity1,X.price AS price1, Y.fid AS fid2,Y.day_of_month AS day2,Y.carrier_id AS carrier2,Y.flight_num AS fnum2,Y.origin_city AS origin2,Y.dest_city AS dest2,Y.actual_time as time2, Y.capacity AS capacity2,Y.price AS price2, X.actual_time + Y.actual_time AS time3 FROM FLIGHTS X, FLIGHTS Y\n" +
                    "WHERE X.origin_city = ? AND X.dest_city = Y.origin_city AND Y.dest_city = ? AND Y.day_of_month = ? AND X.day_of_month = Y.day_of_month AND NOT X.dest_city = ?\n AND X.canceled = 0 AND Y.canceled = 0 ORDER BY time3, X.fid, Y.fid ASC";
    protected PreparedStatement checkFlightCapacityStatement;
    protected PreparedStatement directFlightStatement;
    protected PreparedStatement indirectFlightStatement;

    private static final String INSERT_ITINERARY = "INSERT INTO ITINERARIES VALUES(?, ?, ?, ?)";
    protected PreparedStatement insertItin;

    private static final String CLEAR_ITINERARY = "DELETE FROM ITINERARIES";
    protected PreparedStatement clearItin;

    private int directFlight;
    protected int itineraryCount;

    class Flight
    {
        public int fid;
        public int dayOfMonth;
        public String carrierId;
        public String flightNum;
        public String originCity;
        public String destCity;
        public int time;
        public int capacity;
        public int price;

        @Override
        public String toString()
        {
            return "ID: " + fid + " Day: " + dayOfMonth + " Carrier: " + carrierId +
                    " Number: " + flightNum + " Origin: " + originCity + " Dest: " + destCity + " Duration: " + time +
                    " Capacity: " + capacity + " Price: " + price;
        }
    }

    public QuerySearchOnly(String configFilename)
    {
        this.configFilename = configFilename;
    }

    /** Open a connection to SQL Server in Microsoft Azure.  */
    public void openConnection() throws Exception
    {
        Properties configProps = new Properties();
        configProps.load(new FileInputStream(configFilename));

        String jSQLDriver = configProps.getProperty("flightservice.jdbc_driver");
        String jSQLUrl = configProps.getProperty("flightservice.url");
        String jSQLUser = configProps.getProperty("flightservice.sqlazure_username");
        String jSQLPassword = configProps.getProperty("flightservice.sqlazure_password");

        /* load jdbc drivers */
        Class.forName(jSQLDriver).newInstance();

        /* open connections to the flights database */
        conn = DriverManager.getConnection(jSQLUrl, // database
                jSQLUser, // user
                jSQLPassword); // password

        conn.setAutoCommit(true); //by default automatically commit after each statement
    /* In the full Query class, you will also want to appropriately set the transaction's isolation level:
          conn.setTransactionIsolation(...)
       See Connection class's JavaDoc for details.
    */
        conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    }


    public void closeConnection() throws Exception
    {
        conn.close();
    }

    /**
     * prepare all the SQL statements in this method.
     * "preparing" a statement is almost like compiling it.
     * Note that the parameters (with ?) are still not filled in
     */
    public void prepareStatements() throws Exception
    {
        beginTransactionStatement = conn.prepareStatement(BEGIN_TRANSACTION_SQL);
        commitTransactionStatement = conn.prepareStatement(COMMIT_SQL);
        rollbackTransactionStatement = conn.prepareStatement(ROLLBACK_SQL);

        checkFlightCapacityStatement = conn.prepareStatement(CHECK_FLIGHT_CAPACITY);

        directFlightStatement = conn.prepareStatement(DIRECT_SEARCH);

        indirectFlightStatement = conn.prepareStatement(INDIRECT_SEARCH);

        insertItin = conn.prepareStatement(INSERT_ITINERARY);

        clearItin = conn.prepareStatement(CLEAR_ITINERARY);
    }


    /**
     * Implement the search function.
     *
     * Searches for flights from the given origin city to the given destination
     * city, on the given day of the month. If {@code directFlight} is true, it only
     * searches for direct flights, otherwise it searches for direct flights
     * and flights with two "hops." Only searches for up to the number of
     * itineraries given by {@code numberOfItineraries}.
     *
     * The results are sorted based on total flight time.
     *
     * @param originCity
     * @param destinationCity
     * @param directFlight if true, then only search for direct flights, otherwise include indirect flights as well
     * @param dayOfMonth
     * @param numberOfItineraries number of itineraries to return
     *
     * @return If no itineraries were found, return "No flights match your selection\n".
     * If an error occurs, then return "Failed to search\n".
     *
     * Otherwise, the sorted itineraries printed in the following format:
     *
     * Itinerary [itinerary number]: [number of flights] flight(s), [total flight time] minutes\n
     * [first flight in itinerary]\n
     * ...
     * [last flight in itinerary]\n
     *
     * Each flight should be printed using the same format as in the {@code Flight} class. Itinerary numbers
     * in each search should always start from 0 and increase by 1.
     *
     * @see Flight#toString()
     */
    public String transaction_search(String originCity, String destinationCity, boolean directFlight, int dayOfMonth,
                                     int numberOfItineraries)
    {
        String output;
        itineraryCount = 0;
        if (directFlight) {
            try {
                beginTransaction();
                clearItin.executeUpdate();
                output = searchDirectQuery(originCity, destinationCity, dayOfMonth, numberOfItineraries);
                commitTransaction();
            } catch (SQLException e){
                e.printStackTrace();
                return "Failed to search\n";
            }
            if (output.length() == 0) {
                return "No flights match your selection\n";
            } else {
                return output;
            }
        } else {
            try {
                beginTransaction();
                clearItin.executeUpdate();
                output = searchIndirectQuery(originCity, destinationCity, dayOfMonth, numberOfItineraries);
                commitTransaction();
            } catch (SQLException e) {
                e.printStackTrace();
                return "Failed to search\n";
            }
            if (output.length() == 0) {
                return "No flights match your selection\n";
            } else {
                return output;
            }
        }
    }


    private String searchDirectQuery(String originCity, String destinationCity,
                                     int dayOfMonth, int numberOfItineraries) throws SQLException {
        directFlight = 0;
        String output = "";

        directFlightStatement.clearParameters();
        directFlightStatement.setInt(1, numberOfItineraries);
        directFlightStatement.setString(2, originCity);
        directFlightStatement.setString(3, destinationCity);
        directFlightStatement.setInt(4, dayOfMonth);


        ResultSet tempSet = directFlightStatement.executeQuery();

        while (tempSet.next()) {

            int result_dayOfMonth = tempSet.getInt("day_of_month");
            String result_carrierId = tempSet.getString("carrier_id");
            int result_fid = tempSet.getInt("fid");
            String result_originCity = tempSet.getString("origin_city");
            String result_destCity = tempSet.getString("dest_city");
            int result_time = tempSet.getInt("actual_time");
            int result_capacity = tempSet.getInt("capacity");
            int result_price = tempSet.getInt("price");
            String result_flightNum = tempSet.getString("flight_num");

            Flight flight = new Flight();
            flight.dayOfMonth = result_dayOfMonth;
            flight.carrierId = result_carrierId;
            flight.fid = result_fid;
            flight.flightNum = result_flightNum;
            flight.originCity = result_originCity;
            flight.destCity = result_destCity;
            flight.time = result_time;
            flight.capacity = result_capacity;
            flight.price = result_price;

            insertItin.clearParameters();
            insertItin.setInt(1, itineraryCount);
            insertItin.setInt(2, result_fid);
            insertItin.setInt(3, -1);
            insertItin.setInt(4, result_dayOfMonth);
            insertItin.executeUpdate();

            output += "Itinerary " + itineraryCount + ": 1 flight(s), " + flight.time + " minutes\n";
            output += flight.toString() + "\n";
            directFlight++;

            itineraryCount++;
        }
        tempSet.close();
        return output;
    }

    private String searchIndirectQuery(String originCity, String destinationCity,
                                       int dayOfMonth, int numberOfItineraries) throws SQLException {
        String output = searchDirectQuery(originCity, destinationCity, dayOfMonth, numberOfItineraries);

        indirectFlightStatement.clearParameters();
        indirectFlightStatement.setInt(1, numberOfItineraries - directFlight);
        indirectFlightStatement.setString(2, originCity);
        indirectFlightStatement.setString(3, destinationCity);
        indirectFlightStatement.setInt(4, dayOfMonth);
        indirectFlightStatement.setString(5, destinationCity);

        ResultSet tempSet = indirectFlightStatement.executeQuery();
        while (tempSet.next()) {

            int result_dayOfMonth = tempSet.getInt("day1");
            String result_carrierId = tempSet.getString("carrier1");
            int result_fid = tempSet.getInt("fid1");
            String result_originCity = tempSet.getString("origin1");
            String result_destCity = tempSet.getString("dest1");
            int result_time = tempSet.getInt("time1");
            int result_capacity = tempSet.getInt("capacity1");
            int result_price = tempSet.getInt("price1");
            String result_flightNum = tempSet.getString("fnum1");

            int fid1 = result_fid;

            Flight flight1 = new Flight();
            flight1.dayOfMonth = result_dayOfMonth;
            flight1.carrierId = result_carrierId;
            flight1.fid = result_fid;
            flight1.flightNum = result_flightNum;
            flight1.originCity = result_originCity;
            flight1.destCity = result_destCity;
            flight1.time = result_time;
            flight1.capacity = result_capacity;
            flight1.price = result_price;

            result_dayOfMonth = tempSet.getInt("day2");
            result_carrierId = tempSet.getString("carrier2");
            result_fid = tempSet.getInt("fid2");
            result_originCity = tempSet.getString("origin2");
            result_destCity = tempSet.getString("dest2");
            result_time = tempSet.getInt("time2");
            result_capacity = tempSet.getInt("capacity2");
            result_price = tempSet.getInt("price2");
            result_flightNum = tempSet.getString("fnum2");

            Flight flight2 = new Flight();
            flight2.dayOfMonth = result_dayOfMonth;
            flight2.carrierId = result_carrierId;
            flight2.fid = result_fid;
            flight2.flightNum = result_flightNum;
            flight2.originCity = result_originCity;
            flight2.destCity = result_destCity;
            flight2.time = result_time;
            flight2.capacity = result_capacity;
            flight2.price = result_price;

            insertItin.clearParameters();
            insertItin.setInt(1, itineraryCount);
            insertItin.setInt(2, fid1);
            insertItin.setInt(3, result_fid);
            insertItin.setInt(4, result_dayOfMonth);
            insertItin.executeUpdate();

            int combinedTime = flight1.time + flight2.time;
            output += "Itinerary " + itineraryCount + ": 2 flight(s), " + combinedTime + " minutes\n";
            output += flight1.toString() + "\n";
            output += flight2.toString() + "\n";

            itineraryCount++;
        }
        tempSet.close();
        return output;
    }

    public void beginTransaction() throws SQLException
    {
        conn.setAutoCommit(false);
        beginTransactionStatement.executeUpdate();
    }

    public void commitTransaction() throws SQLException
    {
        commitTransactionStatement.executeUpdate();
        conn.setAutoCommit(true);
    }

    public void rollbackTransaction() throws SQLException
    {
        rollbackTransactionStatement.executeUpdate();
        conn.setAutoCommit(true);
    }
}
