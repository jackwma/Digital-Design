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

  //check if it's direct flight
  private int directFlight;
  private int ItineraryCount;

  // Canned queries
  private static final String CHECK_FLIGHT_CAPACITY = "SELECT capacity FROM Flights WHERE fid = ?";
  private static final String DIRECT_SEARCH = "SELECT TOP (?) fid, day_of_month, carrier_id, flight_num, origin_city, dest_city, actual_time, capacity, price FROM FLIGHTS WHERE origin_city = ? AND dest_city = ? AND day_of_month = ? AND canceled = 0 ORDER BY actual_time ASC";
  private static final String INDIRECT_SEARCH = "SELECT TOP (?) F1.fid as f1fid, F1.day_of_month as f1dom, F1.carrier_id as f1cid, F1.flight_num as f1fnum, F1.origin_city as f1oc, F1.dest_city as f1dc, F1.actual_time as f1at, F1.capacity as f1c, F1.price as f1p, F2.fid as f2fid, F2.day_of_month as f2dom, F2.carrier_id as f2cid, F2.flight_num as f2fnum, F2.origin_city as f2oc, F2.dest_city as f2dc, F2.actual_time as f2at, F2.capacity as f2c, F2.price as f2p FROM FLIGHTS F1, FLIGHTS F2 WHERE F1.origin_city = ? AND F1.dest_city = F2.origin_city AND F2.dest_city = ? AND F1.day_of_month = F2.day_of_month AND F2.day_of_month = ? AND F1.canceled = 0 AND F2.canceled = 0 ORDER BY (F1.actual_time + F2.actual_time) ASC";
  protected PreparedStatement checkFlightCapacityStatement;
  protected PreparedStatement directFlightStatement;
  protected PreparedStatement indirectFlightStatement;


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
    //checkFlightCapacityStatement = conn.prepareStatement(CHECK_FLIGHT_CAPACITY);

    directFlightStatement = conn.prepareStatement(DIRECT_SEARCH);

    indirectFlightStatement = conn.prepareStatement(INDIRECT_SEARCH);
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
    // Please implement your own (safe) version that uses prepared statements rather than string concatenation.
    // You may use the `Flight` class (defined above).
    String output = "";
    try {
      if(directFlight) {
        output = directQuery(originCity, destinationCity, dayOfMonth, numberOfItineraries);
      } else {
        output = indirectQuery(originCity, destinationCity, dayOfMonth, numberOfItineraries);
      }
    } catch(SQLException e) {

    } 
    if(output.length() == 0) {
      return "No flights match your selection\n";
    } else {
      return output;
    }
  }

  /**
   * Same as {@code transaction_search} except that it only performs single hop search and
   * do it in an unsafe manner.
   *
   * @param originCity
   * @param destinationCity
   * @param directFlight
   * @param dayOfMonth
   * @param numberOfItineraries
   *
   * @return The search results. Note that this implementation *does not conform* to the format required by
   * {@code transaction_search}.
   */
  private String transaction_search_unsafe(String originCity, String destinationCity, boolean directFlight,
                                           int dayOfMonth, int numberOfItineraries)
  {
    StringBuffer sb = new StringBuffer();

    try
    {
      // one hop itineraries
      String unsafeSearchSQL =
              "SELECT TOP (" + numberOfItineraries + ") day_of_month,carrier_id,flight_num,origin_city,dest_city,actual_time,capacity,price "
                      + "FROM Flights "
                      + "WHERE origin_city = \'" + originCity + "\' AND dest_city = \'" + destinationCity + "\' AND day_of_month =  " + dayOfMonth + " "
                      + "ORDER BY actual_time ASC";

      Statement searchStatement = conn.createStatement();
      ResultSet oneHopResults = searchStatement.executeQuery(unsafeSearchSQL);

      while (oneHopResults.next())
      {
        int result_dayOfMonth = oneHopResults.getInt("day_of_month");
        String result_carrierId = oneHopResults.getString("carrier_id");
        String result_flightNum = oneHopResults.getString("flight_num");
        String result_originCity = oneHopResults.getString("origin_city");
        String result_destCity = oneHopResults.getString("dest_city");
        int result_time = oneHopResults.getInt("actual_time");
        int result_capacity = oneHopResults.getInt("capacity");
        int result_price = oneHopResults.getInt("price");

        sb.append("Day: ").append(result_dayOfMonth)
                .append(" Carrier: ").append(result_carrierId)
                .append(" Number: ").append(result_flightNum)
                .append(" Origin: ").append(result_originCity)
                .append(" Destination: ").append(result_destCity)
                .append(" Duration: ").append(result_time)
                .append(" Capacity: ").append(result_capacity)
                .append(" Price: ").append(result_price)
                .append('\n');
      }
      oneHopResults.close();
    } catch (SQLException e) { e.printStackTrace(); }

    return sb.toString();
  }

  private String directQuery(String originCity, String destinationCity, int dayOfMonth, int numberOfItineraries) throws SQLException {
    ItineraryCount = 0;
    String output = "";
    directFlight = 0;

    directFlightStatement.clearParameters();
    directFlightStatement.setInt(1, numberOfItineraries);
    directFlightStatement.setString(2, originCity);
    directFlightStatement.setString(3, destinationCity);
    directFlightStatement.setInt(4, dayOfMonth);

    ResultSet results = directFlightStatement.executeQuery();

    while(results.next()) {
      int result_fid = results.getInt("fid");
      int result_dayOfMonth = results.getInt("day_of_month");
      String result_carrierId = results.getString("carrier_id");
      String result_flightNum = results.getString("flight_num");
      String result_originCity = results.getString("origin_city");
      String result_destCity = results.getString("dest_city");
      int result_time = results.getInt("actual_time");
      int result_capacity = results.getInt("capacity");
      int result_price = results.getInt("price");

      Flight F1 = new Flight();

      F1.dayOfMonth = result_dayOfMonth;
      F1.carrierId = result_carrierId;
      F1.fid = result_fid;
      F1.flightNum = result_flightNum;
      F1.originCity = result_originCity;
      F1.destCity = result_destCity;
      F1.time = result_time;
      F1.capacity = result_capacity;
      F1.price = result_price;

      output += "Itinerary " + ItineraryCount + ": 1 flight(s), " + F1.time + " minutes\n";
      output += F1.toString() + "\n";
      directFlight++;
      ItineraryCount++;
    }
    results.close();
    return output;
  }

  private String indirectQuery(String originCity, String destinationCity, int dayOfMonth, int numberOfItineraries) throws SQLException {
    String output = directQuery(originCity, destinationCity, dayOfMonth, numberOfItineraries);
    int remaining = numberOfItineraries - directFlight;
    if(remaining > 0) {
      indirectFlightStatement.clearParameters();
      indirectFlightStatement.setInt(1, remaining);
      indirectFlightStatement.setString(2, originCity);
      indirectFlightStatement.setString(3, destinationCity);
      indirectFlightStatement.setInt(4, dayOfMonth);

      ResultSet results2 = indirectFlightStatement.executeQuery();

      while(results2.next()) {

        int result_fid = results2.getInt("f1fid");
        int result_dayOfMonth = results2.getInt("f1dom");
        String result_carrierId = results2.getString("f1cid");
        String result_flightNum = results2.getString("f1fnum");
        String result_originCity = results2.getString("f1oc");
        String result_destCity = results2.getString("f1dc");
        int result_time = results2.getInt("f1at");
        int result_capacity = results2.getInt("f1c");
        int result_price = results2.getInt("f1p");

        Flight F1 = new Flight();
        F1.fid = result_fid;
        F1.dayOfMonth = result_dayOfMonth;
        F1.carrierId = result_carrierId;
        F1.flightNum = result_flightNum;
        F1.originCity = result_originCity;
        F1.destCity = result_destCity;
        F1.time = result_time;
        F1.capacity = result_capacity;
        F1.price = result_price;

        result_fid = results2.getInt("f2fid");
        result_dayOfMonth = results2.getInt("f2dom");
        result_carrierId = results2.getString("f2cid");
        result_originCity = results2.getString("f2oc");
        result_destCity = results2.getString("f2dc");
        result_time = results2.getInt("f2at");
        result_capacity = results2.getInt("f2c");
        result_price = results2.getInt("f2p");
        result_flightNum = results2.getString("f2fnum");

        Flight F2 = new Flight();
        F2.fid = result_fid;
        F2.dayOfMonth = result_dayOfMonth;
        F2.carrierId = result_carrierId;
        F2.flightNum = result_flightNum;
        F2.originCity = result_originCity;
        F2.destCity = result_destCity;
        F2.time = result_time;
        F2.capacity = result_capacity;
        F2.price = result_price;


        int combinedDuration = F1.time + F2.time;
        output += "Itinerary " + ItineraryCount + ": 2 flight(s), " + combinedDuration + " minutes\n";
        output += F1.toString() + "\n";
        output += F2.toString() + "\n";

        ItineraryCount++;
      }
      results2.close();
    }
    return output;

  }
  /**
   * Shows an example of using PreparedStatements after setting arguments.
   * You don't need to use this method if you don't want to.
   */
  private int checkFlightCapacity(int fid) throws SQLException
  {
    checkFlightCapacityStatement.clearParameters();
    checkFlightCapacityStatement.setInt(1, fid);
    ResultSet results = checkFlightCapacityStatement.executeQuery();
    results.next();
    int capacity = results.getInt("capacity");
    results.close();

    return capacity;
  }


}