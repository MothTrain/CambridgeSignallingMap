package aradnezami.cambridgesignallingmap;

import aradnezami.cambridgesignallingmap.NRFeed.Client.NRFeedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.*;


/**
 * The SQLReplayerClient class is used to query an NROD data server's MySQL logs
 * on historical TD events. This class makes uses of {@link Future} to concurrently
 * query for messages if the events buffer grows too small. If {@link #nextEvent()} is
 * queried while the events buffer is empty and the future has not completed, then
 * it will block.
 */
public class SQLReplayerClient {
    private int messagesRead = 0;
    private final int minimumBufferSize = 200000; // Approx 2 days of data
    private final int messagesPerRead = 300000; // Approx 3 days of data

    private final ArrayList<RawEvent> eventBuffer = new ArrayList<>();

    private final String query = "SELECT Logs.Log_Id, Logs.Time, Logs.LogType, CClass_Logs.From_Berth, CClass_Logs.To_Berth, CClass_Logs.Descr, SClass_Logs.Address, SClass_Logs.Data FROM Logs LEFT JOIN CClass_Logs ON Logs.CClass_Id = CClass_Logs.CClass_Id  LEFT JOIN SClass_Logs ON Logs.SClass_Id = SClass_Logs.SClass_Id ORDER BY Time ASC LIMIT ? OFFSET ?;";

    private final @NotNull String DBName;
    private final @NotNull String hostName;
    private final @NotNull String username;
    private final @NotNull String password;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    @Nullable
    private Future<ArrayList<RawEvent>> SQLQueryFuture;
    @NotNull
    private final Callable<ArrayList<RawEvent>> SQLQueryCallable = this::retrieveEvents;



    /**
     * Creates an SQLReplayerClient and begins to concurrently query the MySQL database.
     * @param dbName Name of the database to be queried
     * @param hostName Hostname of the data server
     * @param username MySQL username
     * @param password MySQL user password
     */
    public SQLReplayerClient(@NotNull String dbName,
                             @NotNull String hostName,
                             @NotNull String username,
                             @NotNull String password) {

        this.DBName = dbName;
        this.hostName = hostName;
        this.username = username;
        this.password = password;

        SQLQueryFuture = executorService.submit(SQLQueryCallable);
    }



    /**
     * Returns the next available message from the MySQL database. If the events buffer is empty
     * and the query to the database has not completed, this method will block until the query
     * is complete. If the buffer size drops below {@link #minimumBufferSize} the method will
     * also automatically begin to query the database concurrently
     * @return The next available event from the database
     * @throws SQLException If there is an issue retrieving logs from the MySQL server
     */
    public @NotNull RawEvent nextEvent() throws SQLException {
        if (eventBuffer.size() < minimumBufferSize && SQLQueryFuture==null) {
            SQLQueryFuture = executorService.submit(SQLQueryCallable);

        } else if ((SQLQueryFuture!=null && SQLQueryFuture.isDone()) || eventBuffer.isEmpty()) {
            try {
                eventBuffer.addAll(SQLQueryFuture.get());
            } catch (InterruptedException ignored) {
            } catch (ExecutionException e) {
                if (e.getCause() instanceof SQLException) {
                    throw new NRFeedException("An SQLException occurred while querying the DB",
                            e.getCause(),
                            "An error occurred while querying the data server");
                } else {
                    throw new RuntimeException(e.getCause());
                }
            }
            SQLQueryFuture = null;
        }

        return eventBuffer.remove(0);
    }




    private synchronized ArrayList<RawEvent> retrieveEvents() throws SQLException {
        String connectionUrl = "jdbc:mysql://"+ hostName +"/"+ DBName +"?serverTimezone=UTC";

        try (Connection conn = DriverManager.getConnection(connectionUrl, username, password);
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setInt(1, messagesPerRead);
            statement.setInt(2, messagesRead);

            ResultSet rs = statement.executeQuery();

            ArrayList<RawEvent> events = toRawEvents(rs);
            messagesRead += messagesPerRead;
            return events;
        }
    }


    private ArrayList<RawEvent> toRawEvents(ResultSet rs) throws SQLException {
        ArrayList<RawEvent> events = new ArrayList<>();
        while (rs.next()) {
             RawEvent event = switch (rs.getString("LogType")) {
                 case "C" -> new RawEvent(
                         rs.getLong("Time"),
                         rs.getString("From_Berth"),
                         rs.getString("To_Berth"),
                         rs.getString("Descr")
                 );
                 case "S" -> new RawEvent(
                         rs.getLong("Time"),
                         rs.getInt("Address"),
                         rs.getInt("Data")
                 );
                 case "R" -> new RawEvent(
                         rs.getLong("Time")
                 );
                 default -> null;
            };
             if (event == null) {
                 System.out.println("Warning: event found with LogType != to C, S or R");
                 continue;
             }
             events.add(event);
        }

        return events;
    }


    /**
     * Represents an event as retrieved from the data server MySQL database. This may be either an S or C class
     * event, but may also be a reset where {@code this.type = 'R'}, meaning that messages have been missed
     * and so any S and C class state should be reset.
     */
    public static class RawEvent {
        public final long timestamp;
        public final char type;

        /**
         * Null if event type is not S
         */
        @Nullable
        public final Integer S_Address;
        /**
         * Null if event type is not S
         */
        @Nullable
        public final Integer S_Data;

        /**
         * Null if event type is not C
         */
        @Nullable
        public final String C_From;
        /**
         * Null if event type is not C
         */
        @Nullable
        public final String C_To;
        /**
         * Null if event type is not C
         */
        @Nullable
        public final String C_Describer;


        /**
         * Creates a reset event
         */
        public RawEvent(long timestamp) {
            this.timestamp = timestamp;
            type = 'R';

            S_Address = null;
            S_Data = null;
            C_From = null;
            C_To = null;
            C_Describer = null;
        }

        /**
         * Creates an S class event
         */
        public RawEvent(long timestamp,
                        int address,
                        int data) {

            this.timestamp = timestamp;
            this.S_Address = address;
            this.S_Data = data;
            this.type = 'S';

            this.C_From = null;
            this.C_To = null;
            this.C_Describer = null;
        }

        /**
         * Creates a C class event
         */
        public RawEvent(long timestamp,
                        @NotNull String from,
                        @NotNull String to,
                        @NotNull String describer) {

            this.timestamp = timestamp;
            this.C_From = from;
            this.C_To = to;
            this.C_Describer = describer;
            this.type = 'C';

            this.S_Address = null;
            this.S_Data = null;
        }
    }
}
