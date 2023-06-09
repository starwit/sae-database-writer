package de.starwit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;

import de.starwit.visionapi.Messages.TrackedDetection;
import de.starwit.visionapi.Messages.TrackingOutput;

public class DataBaseConnection {

    private final Logger log = LogManager.getLogger(this.getClass());
    private Properties config;
    
    private Connection conn;

    private boolean connected = false;

    public DataBaseConnection(Properties props) {
        config=props;
    }
    
    public void createConnection() {
        try {
            String url = config.getProperty("db.url") + "/" + config.getProperty("db.schema");
            String user = config.getProperty("db.username");
            String pw = config.getProperty("db.password");
            conn = DriverManager.getConnection(url, user, pw);
            if (conn != null) {
                connected = true;
            } else {
                connected = false;
            }
        } catch (SQLException e) {
            log.error("Couldn't connect to database with error " + e.getMessage());
        } 
    }

    public void insertNewDetection(TrackingOutput to) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String table = config.getProperty("db.hypertable");

        String query = "INSERT INTO \"" + table + "\" ";
        query += "(\"CAPTURE_TS\", \"CLASS_ID\", \"CONFIDENCE\", \"OBJECT_ID\", \"MIN_X\", \"MIN_Y\",\"MAX_X\",\"MAX_Y\") ";
        query += "VALUES (?,?,?,?,?,?,?,?)";
        
        PreparedStatement preStmt;
        try {
            preStmt = conn.prepareStatement(query);
        } catch (SQLException e) {
            log.warn("creation of prepared statement didn't work " + e.getMessage());
            return;
        }

        List<TrackedDetection> list = to.getTrackedDetectionsList();
        for (TrackedDetection td : list) {
            int classId = td.getDetection().getClassId();
            float confidence = td.getDetection().getConfidence();
            byte[] objectID = td.getObjectId().toByteArray();
            int oId = ByteBuffer.wrap(objectID).getInt();
            int minX = td.getDetection().getBoundingBox().getMinX();
            int minY = td.getDetection().getBoundingBox().getMinY();
            int maxX = td.getDetection().getBoundingBox().getMaxX();
            int maxY = td.getDetection().getBoundingBox().getMaxY();

            try {

                preStmt.setTimestamp(1, timestamp, null);
                preStmt.setInt(2, classId);
                preStmt.setFloat(3, confidence);
                preStmt.setString(4, ""+oId);
                preStmt.setInt(5, minX);
                preStmt.setInt(6, minY);
                preStmt.setInt(7, maxX);
                preStmt.setInt(8, maxY);
                preStmt.addBatch();
            }  catch (SQLException e) {
                log.warn("Adding insert to prepStmt didn't work " + e.getMessage());
            }
            log.info("created batched prep stmt");
            try {
                preStmt.executeBatch();
                log.info("inserted new data");
            } catch (SQLException e) {
                log.warn("executing bached prepared stmt didn't work " + e.getMessage());
            }
        }
    }

    public boolean isConnected() {
        return connected;
    }
    
    public void stop() {
        try {
            conn.close();
        } catch (SQLException e) {
            log.warn("Couldn't close database connection " + e.getMessage());
        }
        connected = false;
    }
}
