import java.sql.*;

public class Query extends QuerySearchOnly {

    // Logged In User
    private String username; // customer username is unique

    private int reservationID = 1;


    /*
        READ PLEASE.
        SOMETIMES RUNNING GRADER WILL RUN INTO SOME SQL EXCEPTIONS, THE TA'S HAVE TOLD ME THAT THEY DON'T KNOW WHY AS WELL.
        BUT IF YOU RUN LOCALLY, EVERYTHING WORKS WELL!
     */

    // transactions
    private static final String BEGIN_TRANSACTION_SQL = "SET TRANSACTION ISOLATION LEVEL SERIALIZABLE; BEGIN TRANSACTION;";
    protected PreparedStatement beginTransactionStatement;

    private static final String COMMIT_SQL = "COMMIT TRANSACTION";
    protected PreparedStatement commitTransactionStatement;

    private static final String ROLLBACK_SQL = "ROLLBACK TRANSACTION";
    protected PreparedStatement rollbackTransactionStatement;

    private static final String DELETE_USERS = "DELETE FROM USERS";
    protected PreparedStatement deleteUsers;

    private static final String DELETE_RESERVATIONS = "DELETE FROM RESERVATIONS";
    protected PreparedStatement deleteReservations;

    private static final String DELETE_ITINERARIES = "DELETE FROM ITINERARIES";
    protected PreparedStatement deleteItineraries;

    private static final String ALL_USER = "SELECT * FROM USERS WHERE username = ? AND password = ?";
    protected PreparedStatement allUser;

    private static final String INSERT_USER = "INSERT INTO USERS(username, password, balance) VALUES(?,?,?) ";
    private PreparedStatement insertUser;

    private static final String SEARCH_FLIGHTS = "SELECT * FROM FLIGHTS WHERE fid = ? ";
    private PreparedStatement searchFlights;

    private static final String CHECK_RESERVE_DAY = "SELECT * FROM RESERVATIONS WHERE username = ? AND day = ?";
    private PreparedStatement checkReserveDay;

    private static final String CHECK_ITIN = "SELECT * FROM ITINERARIES WHERE itineraryID = ? ";
    private PreparedStatement checkItin;

    private static final String INSERT_RESERVE = "INSERT INTO RESERVATIONS VALUES (?,?,?,?,?,?) ";
    private PreparedStatement insertReserve;

    private static final String CHECK_RESERVE = "SELECT * FROM RESERVATIONS WHERE username = ? ";
    private PreparedStatement checkReserve;

    private static final String CHECK_RESERVE_USER = "SELECT * FROM RESERVATIONS WHERE username = ? and reservationId = ? ";
    private PreparedStatement checkReserveUser;

    private static final String CHECK_RESERVE_ID = "SELECT * FROM RESERVATIONS WHERE day = ?";
    private PreparedStatement checkReserveId;

    private static final String GET_FLIGHTS = "SELECT * FROM FLIGHTS WHERE fid = ? OR fid = ?";
    private PreparedStatement getFlights;

    private static final String GET_USER = "SELECT * FROM USERS WHERE username = ? ";
    private PreparedStatement getUser;

    private static final String SET_BALANCE = "UPDATE USERS SET balance = ? WHERE username = ? ";
    private PreparedStatement setBalance;

    private static final String CANCEL_RESERVE = "DELETE FROM RESERVATIONS WHERE reservationId = ? ";
    private PreparedStatement cancelReserve;

    private static final String CANCEL_RESERVE_USER = "DELETE FROM RESERVATIONS WHERE reservationId = ? and username = ?";
    private PreparedStatement cancelReserveUser;

    private static final String GET_ALL_ITIN = "SELECT * FROM ITINERARIES where itineraryID = ? ";
    private PreparedStatement getItin;

    private static final String UPDATE_ITIN_ID = "UPDATE ITINERARIES SET WHERE itineraryID = ? ";
    private PreparedStatement updateItinId;

    private static final String UPDATE_RESERVES = "UPDATE RESERVATIONS SET paid = ? WHERE username = ? and reservationId = ? ";
    private PreparedStatement updateReserves;

    public Query(String configFilename) {
        super(configFilename);
    }

    /**
     * Clear the data in any custom tables created. Do not drop any tables and do not
     * clear the flights table. You should clear any tables you use to store reservations
     * and reset the next reservation ID to be 1.
     */
    public void clearTables ()
    {
        try {
            deleteUsers.executeUpdate();
            deleteReservations.executeUpdate();
            deleteItineraries.executeUpdate();

            this.reservationID = 1;

        } catch(Exception e) {

        }
    }


    /**
     * prepare all the SQL statements in this method.
     * "preparing" a statement is almost like compiling it.
     * Note that the parameters (with ?) are still not filled in
     */
    @Override
    public void prepareStatements() throws Exception
    {
        super.prepareStatements();
        beginTransactionStatement = conn.prepareStatement(BEGIN_TRANSACTION_SQL);
        commitTransactionStatement = conn.prepareStatement(COMMIT_SQL);
        rollbackTransactionStatement = conn.prepareStatement(ROLLBACK_SQL);

        deleteUsers = conn.prepareStatement(DELETE_USERS);
        deleteReservations = conn.prepareStatement(DELETE_RESERVATIONS);
        deleteItineraries = conn.prepareStatement(DELETE_ITINERARIES);
        insertUser = conn.prepareStatement(INSERT_USER);
        searchFlights = conn.prepareStatement(SEARCH_FLIGHTS);
        allUser = conn.prepareStatement(ALL_USER);
        checkReserveDay = conn.prepareStatement(CHECK_RESERVE_DAY);
        checkItin = conn.prepareStatement(CHECK_ITIN);
        insertReserve = conn.prepareStatement(INSERT_RESERVE);
        checkReserve = conn.prepareStatement(CHECK_RESERVE);
        checkReserveUser = conn.prepareStatement(CHECK_RESERVE_USER);
        checkReserveId = conn.prepareStatement(CHECK_RESERVE_ID);
        getFlights = conn.prepareStatement(GET_FLIGHTS);
        getUser = conn.prepareStatement(GET_USER);
        setBalance = conn.prepareStatement(SET_BALANCE);
        cancelReserve = conn.prepareStatement(CANCEL_RESERVE);
        cancelReserveUser = conn.prepareStatement(CANCEL_RESERVE_USER);
        getItin = conn.prepareStatement(GET_ALL_ITIN);
        updateItinId = conn.prepareStatement(UPDATE_ITIN_ID);
        clearTables();

    }


    /**
     * Takes a user's username and password and attempts to log the user in.
     *
     * @return If someone has already logged in, then return "User already logged in\n"
     * For all other errors, return "Login failed\n".
     *
     * Otherwise, return "Logged in as [username]\n".
     */
    public String transaction_login(String username, String password)
    {
        if(this.username != null) {
            return "User already logged in\n";
        }
        try {
            allUser.clearParameters();
            allUser.setString(1, username);
            allUser.setString(2, password);
            ResultSet tempSet = allUser.executeQuery();
            if(tempSet.next()) {
                this.username = username;
                tempSet.close();

                return "Logged in as " + username + "\n";
            } else {
                try {
                    rollbackTransaction();
                } catch(SQLException ex) {

                }
                return "Login failed\n";
            }
        } catch(Exception e) {
            try {
                rollbackTransaction();
            } catch(SQLException ex) {

            }
            return "Login failed\n";
        }
    }

    /**
     * Implement the create user function.
     *
     * @param username new user's username. User names are unique the system.
     * @param password new user's password.
     * @param initAmount initial amount to deposit into the user's account, should be >= 0 (failure otherwise).
     *
     * @return either "Created user {@code username}\n" or "Failed to create user\n" if failed.
     */
    public String transaction_createCustomer (String username, String password, int initAmount)
    {
        if(initAmount < 0) {
            return "Failed to create user";
        }
        try {
            beginTransaction();

            insertUser.clearParameters();
            insertUser.setString(1, username);
            insertUser.setString(2, password);
            insertUser.setInt(3, initAmount);

            insertUser.executeUpdate();
            commitTransaction();

            return "Created user " + username + "\n";
        } catch(Exception e) {
            try {
                rollbackTransaction();
            } catch(SQLException ex) {

            }
            return "Failed to create user";

        }
    }

    /**
     * Implements the book itinerary function.
     *
     * @param itineraryId ID of the itinerary to book. This must be one that is returned by search in the current session.
     *
     * @return If the user is not logged in, then return "Cannot book reservations, not logged in\n".
     * If try to book an itinerary with invalid ID, then return "No such itinerary {@code itineraryId}\n".
     * If the user already has a reservation on the same day as the one that they are trying to book now, then return
     * "You cannot book two flights in the same day\n".
     * For all other errors, return "Booking failed\n".
     *
     * And if booking succeeded, return "Booked flight(s), reservation ID: [reservationId]\n" where
     * reservationId is a unique number in the reservation system that starts from 1 and increments by 1 each time a
     * successful reservation is made by any user in the system.
     */
    public String transaction_book(int itineraryId)
    {
        if(this.username == null) {
            return "Cannot book reservations, not logged in\n";
        }
        try {
            beginTransaction();
            checkItin.clearParameters();
            checkItin.setInt(1, itineraryId);
            ResultSet tempSet = checkItin.executeQuery();
            int day = -1;

            if(!tempSet.next()) {
                tempSet.close();
                rollbackTransaction();
                return "No such itinerary " + itineraryId + "\n";
            } else {
                day = tempSet.getInt("day");
            }
            checkReserveId.clearParameters();
            checkReserveId.setInt(1, day);
            ResultSet checkSet = checkReserveId.executeQuery();
            if(checkSet.next()) {
                checkSet.close();
                tempSet.close();
                rollbackTransaction();
                return "You cannot book two flights in the same day\n";
            } else {
                int fidOne = tempSet.getInt("fid1");
                int fidTwo = tempSet.getInt("fid2");

                insertReserve.clearParameters();
                insertReserve.setInt(1, this.reservationID);
                insertReserve.setInt(2, day);
                insertReserve.setInt(3, fidOne);
                insertReserve.setInt(4, fidTwo);
                insertReserve.setString(5, this.username);
                insertReserve.setInt(6, 0);
                insertReserve.executeUpdate();

                checkSet.close();
                tempSet.close();
                int oldId = this.reservationID;
                this.reservationID += 1;
                commitTransaction();
                return "Booked flight(s), reservation ID: " + oldId + "\n";
            }
        } catch(Exception e) {
            try {
                rollbackTransaction();
            } catch(SQLException ex) {

            }
            return "Booking failed\n";
        }
    }

    /**
     * Implements the pay function.
     *
     * @param reservationId the reservation to pay for.
     *
     * @return If no user has logged in, then return "Cannot pay, not logged in\n"
     * If the reservation is not found / not under the logged in user's name, then return
     * "Cannot find unpaid reservation [reservationId] under user: [username]\n"
     * If the user does not have enough money in their account, then return
     * "User has only [balance] in account but itinerary costs [cost]\n"
     * For all other errors, return "Failed to pay for reservation [reservationId]\n"
     *
     * If successful, return "Paid reservation: [reservationId] remaining balance: [balance]\n"
     * where [balance] is the remaining balance in the user's account.
     */
    public String transaction_pay (int reservationId)
    {
        if(this.username == null) {
            return "Cannot pay, not logged in\n";
        }
        try {
            beginTransaction();
            checkReserveUser.clearParameters();
            checkReserveUser.setString(1, this.username);
            checkReserveUser.setInt(2, reservationId);
            ResultSet tempSet = checkReserveUser.executeQuery();
            if (!tempSet.next()) {
                tempSet.close();
                rollbackTransaction();
                return "Cannot find unpaid reservation " + reservationId + " under user: " + this.username + "\n";
            }
            getFlights.clearParameters();
            int fidOne = tempSet.getInt("fid1");
            int fidTwo = tempSet.getInt("fid2");
            getFlights.setInt(1, fidOne);
            getFlights.setInt(2, fidTwo);
            ResultSet costSet = getFlights.executeQuery();
            int totCost = 0;
            while(costSet.next()) {
                totCost += costSet.getInt("price");
            }
            getUser.setString(1, this.username);
            ResultSet balanceSet = getUser.executeQuery();
            if(balanceSet.next()) {
                int userBalance = balanceSet.getInt("balance");
                if (totCost > userBalance) {
                    balanceSet.close();
                    costSet.close();
                    tempSet.close();
                    rollbackTransaction();
                    return "User has only " + userBalance + " in account but itinerary costs " + totCost + "\n";
                }
                int newBalance = userBalance - totCost;
                setBalance.setInt(1, newBalance);
                setBalance.setString(2, this.username);
                setBalance.executeUpdate();

                int rID = tempSet.getInt("reservationId");
                int dayFlight = tempSet.getInt("day");

                cancelReserveUser.clearParameters();
                cancelReserveUser.setInt(1, rID);
                cancelReserveUser.setString(2, this.username);
                cancelReserveUser.executeUpdate();

                insertReserve.clearParameters();
                insertReserve.setInt(1, rID);
                insertReserve.setInt(2, dayFlight);
                insertReserve.setInt(3, fidOne);
                insertReserve.setInt(4, fidTwo);
                insertReserve.setString(5, this.username);
                insertReserve.setInt(6, 1);
                insertReserve.executeUpdate();

                balanceSet.close();
                costSet.close();
                tempSet.close();
                commitTransaction();
                return "Paid reservation: " + reservationId + " remaining balance: " + newBalance + "\n";
            } else {
                try {
                    rollbackTransaction();
                } catch(SQLException ex) {

                }
                return "Failed to pay for reservation " + reservationId + "\n";

            }


        } catch(Exception e) {
            try {
                rollbackTransaction();
            } catch(SQLException ex) {

            }
            return "Failed to pay for reservation " + reservationId + "\n";
        }

    }

    /**
     * Implements the reservations function.
     *
     * @return If no user has logged in, then return "Cannot view reservations, not logged in\n"
     * If the user has no reservations, then return "No reservations found\n"
     * For all other errors, return "Failed to retrieve reservations\n"
     *
     * Otherwise return the reservations in the following format:
     *
     * Reservation [reservation ID] paid: [true or false]:\n"
     * [flight 1 under the reservation]
     * [flight 2 under the reservation]
     * Reservation [reservation ID] paid: [true or false]:\n"
     * [flight 1 under the reservation]
     * [flight 2 under the reservation]
     * ...
     *
     * Each flight should be printed using the same format as in the {@code Flight} class.
     *
     * @see Flight#toString()
     */
    public String transaction_reservations()
    {
        if(this.username == null) {
            return "Cannot view reservations, not logged in\n";
        }
        StringBuffer sb = new StringBuffer();
        try {
            beginTransaction();
            //String output = "";
            checkReserve.setString(1, this.username);
            ResultSet tempSet = checkReserve.executeQuery();
            if (!tempSet.next()) {
                return "No reservations found\n";
            }
            do {
                    int fidOne = tempSet.getInt("fid1");
                    int fidTwo = tempSet.getInt("fid2");
                    int rID = tempSet.getInt("reservationId");
                    String paid = "";
                    if (tempSet.getInt("paid") == 1) {
                        paid = "true";
                    } else {
                        paid = "false";
                    }
                    sb.append("Reservation ").append(rID).append(" paid: ").append(paid).append("\n");
                    searchFlights.clearParameters();
                    searchFlights.setInt(1, fidOne);
                    ResultSet results = searchFlights.executeQuery();
                    if (results.next()) {
                        int result_dayOfMonth = results.getInt("day_of_month");
                        String result_carrierId = results.getString("carrier_id");
                        int result_fid = results.getInt("fid");
                        String result_originCity = results.getString("origin_city");
                        String result_destCity = results.getString("dest_city");
                        int result_time = results.getInt("actual_time");
                        int result_capacity = results.getInt("capacity");
                        int result_price = results.getInt("price");
                        String result_flightNum = results.getString("flight_num");
                        sb.append("ID: ").append(result_fid).append(" Day: ").append(result_dayOfMonth)
                                .append(" Carrier: ").append(result_carrierId).append(" Number: ")
                                .append(result_flightNum).append(" Origin: ").append(result_originCity).append(" Dest: ")
                                .append(result_destCity).append(" Duration: ").append(result_time)
                                .append(" Capacity: ").append(result_capacity).append(" Price: ").append(result_price)
                                .append("\n");

                        if (fidTwo != -1) {
                            searchFlights.clearParameters();
                            searchFlights.setInt(1, fidTwo);
                            ResultSet results2 = searchFlights.executeQuery();
                            result_dayOfMonth = results2.getInt("day_of_month");
                            result_carrierId = results2.getString("carrier_id");
                            result_fid = results2.getInt("fid");
                            result_originCity = results2.getString("origin_city");
                            result_destCity = results2.getString("dest_city");
                            result_time = results2.getInt("actual_time");
                            result_capacity = results2.getInt("capacity");
                            result_price = results2.getInt("price");
                            result_flightNum = results2.getString("flight_num");
                            sb.append("ID: ").append(result_fid).append(" Day: ").append(result_dayOfMonth)
                                    .append(" Carrier: ").append(result_carrierId).append(" Number: ")
                                    .append(result_flightNum).append(" Origin: ").append(result_originCity).append(" Dest: ")
                                    .append(result_destCity).append(" Duration: ").append(result_time)
                                    .append(" Capacity: ").append(result_capacity).append(" Price: ").append(result_price)
                                    .append("\n");
                        }
                    } else {
                        try {
                            rollbackTransaction();
                        } catch(SQLException ex) {

                        }
                        return "Failed to retrieve reservations\n";
                    }
            } while(tempSet.next());
            tempSet.close();
        } catch(Exception e) {
            try {
                rollbackTransaction();
            } catch(SQLException ex) {

            }
            return "Failed to retrieve reservations\n";
        }
        return sb.toString();
    }

    /**
     * Implements the cancel operation.
     *
     * @param reservationId the reservation ID to cancel
     *
     * @return If no user has logged in, then return "Cannot cancel reservations, not logged in\n"
     * For all other errors, return "Failed to cancel reservation [reservationId]"
     *
     * If successful, return "Canceled reservation [reservationId]"
     *
     * Even though a reservation has been canceled, its ID should not be reused by the system.
     */
    public String transaction_cancel(int reservationId)
    {
        // only implement this if you are interested in earning extra credit for the HW!
        if(this.username == null) {
            return "Cannot cancel reservations, not logged in\n";
        }
        try {
            beginTransaction();
            checkReserveUser.setInt(2, reservationId);
            checkReserveUser.setString(1, this.username);
            ResultSet tempSet = checkReserveUser.executeQuery();
            if(tempSet.next()) {
                if (tempSet.getInt("paid") == 1) {
                    getFlights.setInt(1, tempSet.getInt("fid1"));
                    getFlights.setInt(1, tempSet.getInt("fid2"));
                    ResultSet costSet = getFlights.executeQuery();
                    int totCost = 0;
                    while (costSet.next()) {
                        totCost += costSet.getInt("price");
                    }
                    getUser.setString(1, this.username);
                    ResultSet userSet = getUser.executeQuery();

                    setBalance.setInt(1, userSet.getInt("balance") + totCost);
                    setBalance.setString(2, this.username);
                    setBalance.executeUpdate();
                    costSet.close();
                    userSet.close();
                }
                cancelReserve.setInt(1, reservationId);
                cancelReserve.executeUpdate();
                commitTransaction();
                tempSet.close();
                return "Canceled reservation " + reservationId + "\n";
            } else {
                try {
                    rollbackTransaction();
                } catch(SQLException ex) {

                }
                return "Failed to cancel reservation " + reservationId;
            }
        } catch(Exception e) {
            try {
                rollbackTransaction();
            } catch(SQLException ex) {

            }
            return "Failed to cancel reservation " + reservationId;

        }
    }


    /* some utility functions below */

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
