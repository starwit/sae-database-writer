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
    private final Logger log = LogManager.getLogger(this.getClass());
    private final Config config;

    private static DataBaseConnection instance;

    private final String INSERT_QUERY;
    private PreparedStatement insertStatement;

    private Connection conn;

    private DataBaseConnection() {
        this.config = Config.getInstance();
        this.INSERT_QUERY = """
                INSERT INTO \s""" + config.dbHypertable
                + """
                         \s("capture_ts", "class_id", "confidence", "object_id", "min_x", "min_y", "max_x", "max_y", "camera_id")
                        VALUES (?,?,?,?,?,?,?,?,?)
                        """;
    }

    public boolean connect() {
        if (this.conn == null) {
            try {
                String url = config.dbJdbcUrl;
                String user = config.dbUsername;
                String pw = config.dbPassword;
                conn = DriverManager.getConnection(url, user, pw);
                this.insertStatement = conn.prepareStatement(this.INSERT_QUERY);
                log.info("Successfully connected to Postgres DB at {}", url);
                return true;
            } catch (SQLException e) {
                log.error("Couldn't connect to database with error", e);
                this.close();
                return false;
            }
        } else {
            return true;
        }
    }

    public synchronized void insertNewDetection(TrackingOutput to) {
        if (!this.connect()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.warn("Thread was interrupted while waiting after database connection failure", e);
            }
            return;
        }

        try {
            Timestamp captureTimestamp = new Timestamp(to.getFrame().getTimestampUtcMs());

            List<TrackedDetection> list = to.getTrackedDetectionsList();
            for (TrackedDetection td : list) {
                int classId = td.getDetection().getClassId();
                float confidence = td.getDetection().getConfidence();
                byte[] objectID = td.getObjectId().toByteArray();
                String oId = HexFormat.of().formatHex(objectID);
                float minX = td.getDetection().getBoundingBox().getMinX();
                float minY = td.getDetection().getBoundingBox().getMinY();
                float maxX = td.getDetection().getBoundingBox().getMaxX();
                float maxY = td.getDetection().getBoundingBox().getMaxY();

                insertStatement.setTimestamp(1, captureTimestamp, null);
                insertStatement.setInt(2, classId);
                insertStatement.setFloat(3, confidence);
                insertStatement.setString(4, oId);
                insertStatement.setFloat(5, minX);
                insertStatement.setFloat(6, minY);
                insertStatement.setFloat(7, maxX);
                insertStatement.setFloat(8, maxY);
                insertStatement.setString(9, to.getFrame().getSourceId());
                insertStatement.addBatch();
            }

            log.debug("created batched prep stmt");

            insertStatement.executeBatch();

            log.debug("inserted new data");
        } catch (SQLException e) {
            log.error("error executing insert query", e);
            this.close();
        }
    }

    public void close() {
        try {
            if (insertStatement != null) {
                insertStatement.close();
            }
            if (this.conn != null) {

                this.conn.close();

            }
        } catch (SQLException ex) {
            log.warn("Error closing Postgres connection", ex);
        } finally {
            this.conn = null;
            this.insertStatement = null;
        }
    }

    public static DataBaseConnection getInstance() {
        if (instance == null) {
            instance = new DataBaseConnection();
        }
        return instance;
    }
}
