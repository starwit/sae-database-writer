package de.starwit;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.List;

import de.starwit.visionapi.Messages.TrackedDetection;
import de.starwit.visionapi.Messages.TrackingOutput;

public class DataBaseConnection {

    private Connection conn;

    private boolean connected = false;
    
    public void createConnection() {
        try {
            conn = DriverManager.getConnection("jdbc:postgresql://brain01.starwit.home:30897/sae", "sae", "sae");
            if (conn != null) {
                connected = true;
            } else {
                connected = false;
            }

        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } 
    }

    public void insertNewDetection(TrackingOutput to) {

        String query = "INSERT INTO \"Detection\" (\"CAPTURE_TS\", \"CLASS_ID\", \"CONFIDENCE\", \"OBJECT_ID\", \"MIN_X\", \"MIN_Y\",\"MAX_X\",\"MAX_Y\") ";
        query += "VALUES (?,?,?,?,?,?,?,?)";
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

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
                PreparedStatement preStmt = conn.prepareStatement(query);
                
                preStmt.setTimestamp(1, timestamp, null);
                preStmt.setInt(2, classId);
                preStmt.setFloat(3, confidence);
                preStmt.setString(4, ""+oId);
                preStmt.setInt(5, minX);
                preStmt.setInt(6, minY);
                preStmt.setInt(7, maxX);
                preStmt.setInt(8, maxY);
                preStmt.executeUpdate();
            }  catch (SQLException e) {
                System.out.println("Query didn't work " + e.getMessage());
            }
        }
    }

    public void insertSampleData() {        
        String query = "INSERT INTO \"Detection\" (\"CAPTURE_TS\", \"CLASS_ID\", \"CONFIDENCE\", \"OBJECT_ID\", \"MIN_X\", \"MIN_Y\",\"MAX_X\",\"MAX_Y\") ";
        query += "VALUES (?,?,?,?,?,?,?,?)";
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        byte[] object_id = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09};

        try {
            PreparedStatement preStmt = conn.prepareStatement(query);
            preStmt.setTimestamp(1, timestamp, null);
            preStmt.setInt(2, 0);
            preStmt.setFloat(3, (float)90.0);
            preStmt.setBytes(4, object_id);
            preStmt.setInt(5, 1);
            preStmt.setInt(6, 1);
            preStmt.setInt(7, 100);
            preStmt.setInt(8, 100);
            System.out.println(preStmt.toString());
            preStmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Creating Statement didn't work " + e.getMessage());
        }



    }

    public static void main( String[] args ) {
        DataBaseConnection dc =  new DataBaseConnection();
        dc.createConnection();
        //dc.insertSampleData();
    }

    public boolean isConnected() {
        return connected;
    }    
}
