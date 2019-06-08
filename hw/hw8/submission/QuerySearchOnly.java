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


    // Canned queries
    private static final String CHECK_FLIGHT_CAPACITY = "SELECT capacity FROM Flights WHERE fid = ?";
    protected PreparedStatement checkFlightCapacityStatement;


    private static final String CHECK_FLIGHT_CAPACITY = "SELECT capacity FROM Flights WHERE fid = ?";
    private static final String DIRECT_SEARCH = "SELECT TOP (?) fid, day_of_month, carrier_id, flight_num, origin_city, dest_city, actual_time, capacity, price FROM FLIGHTS WHERE origin_city = ? AND dest_city = ? AND day_of_month = ? AND canceled = 0 ORDER BY actual_time ASC";
    private static final String INDIRECT_SEARCH = "SELECT TOP (?) F1.fid as f1fid, F1.day_of_month as f1dom, F1.carrier_id as f1cid, F1.flight_num as f1fnum, F1.origin_city as f1oc, F1.dest_city as f1dc, F1.actual_time as f1at, F1.capacity as f1c, F1.price as f1p, F2.fid as f2fid, F2.day_of_month as f2dom, F2.carrier_id as f2cid, F2.flight_num as f2fnum, F2.origin_city as f2oc, F2.dest_city as f2dc, F2.actual_time as f2at, F2.capacity as f2c, F2.price as f2p FROM FLIGHTS F1, FLIGHTS F2 WHERE F1.origin_city = ? AND F1.dest_city = F2.origin_city AND F2.dest_city = ? AND F1.day_of_month = F2.day_of_month AND F2.day_of_month = ? AND F1.canceled = 0 AND F2.canceled = 0 ORDER BY (F1.actual_time + F2.actual_time) ASC";
    protected PreparedStatement checkFlightCapacityStatement;
    protected PreparedStatement directFlightStatement;
    protected PreparedStatement indirectFlightStatement;

    private static final String INSERT_ITINERARY = "INSERT INTO ITINERARIES VALUES(?, ?, ?, ?)";
    protected PreparedStatement insertItineraryStatement;

    private static final String CLEAR_ITINERARY = "DELETE FROM ITINERARIES";
    protected PreparedStatement clearItineraryStatement;

    private int direct;
    protected int itineraryIndex;

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

        flightStatementDirect = conn.prepareStatement(DIRECT_SEARCH);

        flightStatementIndirect = conn.prepareStatement(INDIRECT_SEARCH);

        insertItineraryStatement = conn.prepareStatement(INSERT_ITINERARY);

        clearItineraryStatement = conn.prepareStatement(CLEAR_ITINERARY);
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
        String result;
        itineraryIndex = 0;
        if (directFlight) {
            try {
                beginTransaction();
                clearItineraryStatement.executeUpdate();
                result = searchDirectQuery(originCity, destinationCity, dayOfMonth, numberOfItineraries);
                commitTransaction();
            } catch (SQLException e){
                e.printStackTrace();
                return "Failed to search\n";
            }
            if (result.length() == 0) {
                return "No flights match your selection\n";
            } else {
                return result;
            }
        } else {
            try {
                beginTransaction();
                clearItineraryStatement.executeUpdate();
                result = searchIndirectQuery(originCity, destinationCity, dayOfMonth, numberOfItineraries);
                commitTransaction();
            } catch (SQLException e) {
                e.printStackTrace();
                return "Failed to search\n";
            }
            if (result.length() == 0) {
                return "No flights match your selection\n";
            } else {
                return result;
            }
        }
    }


    private String searchDirectQuery(String originCity, String destinationCity,
                                     int dayOfMonth, int numberOfItineraries) throws SQLException {
        direct = 0;
        String result = "";

        flightStatementDirect.clearParameters();
        flightStatementDirect.setInt(1, numberOfItineraries);
        flightStatementDirect.setString(2, originCity);
        flightStatementDirect.setString(3, destinationCity);
        flightStatementDirect.setInt(4, dayOfMonth);

        ResultSet results = flightStatementDirect.executeQuery();
        while (results.next()) {

            int result_dayOfMonth = results.getInt("day_of_month");
            String result_carrierId = results.getString("carrier_id");
            int result_fid = results.getInt("fid");
            String result_originCity = results.getString("origin_city");
            String result_destCity = results.getString("dest_city");
            int result_time = results.getInt("actual_time");
            int result_capacity = results.getInt("capacity");
            int result_price = results.getInt("price");
            String result_flightNum = results.getString("flight_num");

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

            insertItineraryStatement.clearParameters();
            insertItineraryStatement.setInt(1, itineraryIndex);
            insertItineraryStatement.setInt(2, result_fid);
            insertItineraryStatement.setInt(3, -1);
            insertItineraryStatement.setInt(4, result_dayOfMonth);
            insertItineraryStatement.executeUpdate();

            result += "Itinerary " + itineraryIndex + ": 1 flight(s), " + flight.time + " minutes\n";
            result += flight.toString() + "\n";
            direct++;

            itineraryIndex++;
        }
        results.close();
        return result;
    }

    private String searchIndirectQuery(String originCity, String destinationCity,
                                       int dayOfMonth, int numberOfItineraries) throws SQLException {
        String result = searchDirectQuery(originCity, destinationCity, dayOfMonth, numberOfItineraries);

        flightStatementIndirect.clearParameters();
        flightStatementIndirect.setInt(1, numberOfItineraries - direct);
        flightStatementIndirect.setString(2, originCity);
        flightStatementIndirect.setString(3, destinationCity);
        flightStatementIndirect.setInt(4, dayOfMonth);
        flightStatementIndirect.setString(5, destinationCity);

        ResultSet results = flightStatementIndirect.executeQuery();
        while (results.next()) {

            int result_dayOfMonth = results.getInt("day1");
            String result_carrierId = results.getString("carrier1");
            int result_fid = results.getInt("fid1");
            String result_originCity = results.getString("origin1");
            String result_destCity = results.getString("dest1");
            int result_time = results.getInt("time1");
            int result_capacity = results.getInt("capacity1");
            int result_price = results.getInt("price1");
            String result_flightNum = results.getString("fnum1");

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

            result_dayOfMonth = results.getInt("day2");
            result_carrierId = results.getString("carrier2");
            result_fid = results.getInt("fid2");
            result_originCity = results.getString("origin2");
            result_destCity = results.getString("dest2");
            result_time = results.getInt("time2");
            result_capacity = results.getInt("capacity2");
            result_price = results.getInt("price2");
            result_flightNum = results.getString("fnum2");

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

            insertItineraryStatement.clearParameters();
            insertItineraryStatement.setInt(1, itineraryIndex);
            insertItineraryStatement.setInt(2, fid1);
            insertItineraryStatement.setInt(3, result_fid);
            insertItineraryStatement.setInt(4, result_dayOfMonth);
            insertItineraryStatement.executeUpdate();

            int combinedTime = flight1.time + flight2.time;
            result += "Itinerary " + itineraryIndex + ": 2 flight(s), " + combinedTime + " minutes\n";
            result += flight1.toString() + "\n";
            result += flight2.toString() + "\n";

            itineraryIndex++;
        }
        results.close();
        return result;
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
