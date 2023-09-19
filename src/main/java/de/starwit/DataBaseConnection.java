package de.starwit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HexFormat;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.starwit.visionapi.Messages.TrackedDetection;
import de.starwit.visionapi.Messages.TrackingOutput;

public class DataBaseConnection {

    private static DataBaseConnection instance;

    private final Logger log = LogManager.getLogger(this.getClass());
    private final Config config;
    private final String insertQuery;

    private Connection conn;

    private DataBaseConnection() {
        this.config = Config.getInstance();

        this.insertQuery = new StringBuilder("INSERT INTO \"" + config.dbHypertable + "\" ")
            .append("(\"CAPTURE_TS\", \"CLASS_ID\", \"CONFIDENCE\", \"OBJECT_ID\", \"MIN_X\", \"MIN_Y\",\"MAX_X\",\"MAX_Y\", \"CAMERA_ID\") ")
            .append("VALUES (?,?,?,?,?,?,?,?,?)")
            .toString();
    }
    
    public void connect() {
        try {
            String url = config.dbJdbcUrl;
            String user = config.dbUsername;
            String pw = config.dbPassword;
            conn = DriverManager.getConnection(url, user, pw);
            log.info("Successfully connected to Postgres DB at {}", url);
        } catch (SQLException e) {
            log.error("Couldn't connect to database with error", e);
            this.close();
        } 
    }

    public synchronized void insertNewDetection(TrackingOutput to) {
        if (this.conn == null) {
            this.connect();
        }

        try {
            PreparedStatement preStmt = conn.prepareStatement(this.insertQuery);

            Timestamp captureTimestamp = new Timestamp(to.getFrame().getTimestampUtcMs());
            
            List<TrackedDetection> list = to.getTrackedDetectionsList();
            for (TrackedDetection td : list) {
                int classId = td.getDetection().getClassId();
                float confidence = td.getDetection().getConfidence();
                byte[] objectID = td.getObjectId().toByteArray();
                String oId = HexFormat.of().formatHex(objectID);
                int minX = td.getDetection().getBoundingBox().getMinX();
                int minY = td.getDetection().getBoundingBox().getMinY();
                int maxX = td.getDetection().getBoundingBox().getMaxX();
                int maxY = td.getDetection().getBoundingBox().getMaxY();
    
                preStmt.setTimestamp(1, captureTimestamp, null);
                preStmt.setInt(2, classId);
                preStmt.setFloat(3, confidence);
                preStmt.setString(4, oId);
                preStmt.setInt(5, minX);
                preStmt.setInt(6, minY);
                preStmt.setInt(7, maxX);
                preStmt.setInt(8, maxY);
                preStmt.setString(9, to.getFrame().getSourceId());
                preStmt.addBatch();
            }

            log.debug("created batched prep stmt");

            preStmt.executeBatch();

            log.debug("inserted new data");
        } catch (SQLException e) {
            log.error("error executing insert query", e);
            this.close();
        }
    }
    
    private void close() {
        if (this.conn != null) {
            try {
                this.conn.close();
            } catch (SQLException ex) {
                log.warn("Error closing Postgres connection", ex);
            }
            this.conn = null;
        }
    }
    
    public void stop() {
        try {
            conn.close();
        } catch (SQLException e) {
            log.warn("Couldn't close database connection", e);
        }
    }

    public static DataBaseConnection getInstance() {
        if (instance == null) {
            instance = new DataBaseConnection();
        }
        return instance;
    }
}
